package aplug.feedback.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.user.activity.MyMessage;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.feedback.adapter.AdapterFeedback;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;
import third.push.xg.XGPushServer;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 *
 * @author zeyue_t
 * @time 2015年6月4日下午3:49:52
 */
@SuppressLint("ClickableViewAccessibility")
public class Feedback extends BaseActivity implements OnClickListener {
    private static final int FEEDBACK_UPLOADIMG = 4000;
    public static Handler handler = null;
    private DownRefreshList feekback_list;
    private EditText feebback_reply_content, keyboradEdit;

    private ArrayList<Map<String, String>> contentList = null;
    private AdapterFeedback adapter;
    private String timePage = "";
    private String Token = "";
    private String mImageUrl = "";
    private static String feekback_text = "", feekUrl = "", from = "";
    private boolean LoadOver = false, addHeaderOver = false;

    private String backData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            feekUrl = bundle.getString("feekUrl");
            from = bundle.getString("from");
            backData = bundle.getString("backData");
            doTongJi();//统计
        }
        Log.i("FeedbackFuntion", "backData:" + backData);
        Token = XGPushServer.getXGToken(this);
        initActivity("香哈小秘书", 2, 0, R.layout.c_view_bar_title, R.layout.a_xh_feedback);
        // 设置加载
        loadManager.setLoading("香哈小秘书", new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                loadManager.setLoading(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        init();
                    }
                });
            }
        });

        handler = new Handler(new Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_FROM_NOTIFY:
                        contentList.clear();
                        getFeekbackInfo(true);
                        break;
                    case MSG_IMG_UPLOAD:
                        contentList.get(contentList.size() - 1).put("progress_img", "hide");
                        contentList.get(contentList.size() - 1).put("progress_text", "hide");
                        adapter.notifyDataSetChanged();
                        if (!feekback_text.equals(""))
                            feebback_reply_content.setText(feekback_text);
                        break;
                    case MSG_TEXT_UPLOAD:
                        contentList.get(contentList.size() - 1).put("progress_img", "hide");
                        contentList.get(contentList.size() - 1).put("progress_text", "hide");
                        adapter.notifyDataSetChanged();
                        feekback_text = "";
                        break;
                }
                return false;
            }
        });
    }

    private void init() {
        AppCommon.feekbackMessage = 0;
        MyMessage.notifiMessage(MyMessage.MSG_FEEKBACK_ONREFURESH, 0, null);
        feekback_list = (DownRefreshList) findViewById(R.id.feebback_reply_list);
        findViewById(R.id.feekback_img_choice).setOnClickListener(this);
        findViewById(R.id.feebback_send).setOnClickListener(this);
        feebback_reply_content = (EditText) findViewById(R.id.feebback_reply_content);
        keyboradEdit = (EditText) findViewById(R.id.feekback_keyboard_view);
        contentList = new ArrayList<>();
        adapter = new AdapterFeedback(Feedback.this, feekback_list, contentList, 0, null, null);
        loadEvent();
//		 getFeekbackInfo(false);
        feekback_list.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    default:
                        ToolsDevice.keyboardControl(false, Feedback.this, feebback_reply_content);
                }
                return false;
            }
        });
    }

    private void loadEvent() {
        if (!LoadOver) {
            loadManager.showProgressBar();
            loadManager.setLoading(feekback_list, adapter, false, new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    getFeekbackInfo(true);
                }
            }, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 刷新页面
                    if (contentList.size() == 0)
                        getFeekbackInfo(true);
                    else
                        getFeekbackInfo(false);
                }
            });
            LoadOver = true;
        }
    }

    /**
     * 获取网络数据
     *
     * @param isForward 是否是向上加载
     */
    private void getFeekbackInfo(final boolean isForward) {
        feekback_list.bigDownText = "下拉加载上一页";
        feekback_list.bigReleaseText = "松开加载上一页";
        String params;
//		Token = "6d9c4cc747fefc8942fc975557cf68fa643b27a8";
        if (isForward) {
            params = "?token=" + Token;
        } else
            params = "?token=" + Token + "&timePage=" + timePage;
        ReqInternet.in().doGet(StringManager.api_getDialogInfo + params, new InternetCallback(this) {

            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    if (isForward) contentList.clear();
                    // LogManager.print("i", returnObj.toString());
                    ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(returnObj);
                    // 除第一次加载外都要获取timePage
                    if (listReturn.size() > 0)
                        timePage = listReturn.get(listReturn.size() - 1).get("addTime");
                    // 向上加载
                    for (int i = 0; i < listReturn.size(); i++) {
                        Map<String, String> mapReturn = listReturn.get(i);
                        if (mapReturn != null
                                && mapReturn.get("img").equals(""))
                            mapReturn.put("img", "hide");
                        mapReturn.put("progress_img", "hide");
                        mapReturn.put("progress_text", "hide");
                        mapReturn.put("author", mapReturn.get("author") + "");
//						if(mapReturn.get("author").equals("1") && i % 3 == 0 && mapReturn.get("type").equals("1")){
//							mapReturn.put("url", "dishInfo.app?code=77620866");
//							mapReturn.put("title", "菜谱：测试"+i);
//						}
//						if(i % 4 == 0){
//							mapReturn.put("url", "http://www.baidu.com");
//							mapReturn.put("type", "3");
//							mapReturn.put("title", "用外部浏览器打开。。。");
//						}
                        contentList.add(0, mapReturn);
                    }
                    if (!TextUtils.isEmpty(backData)) {
                        sendFeekback(backData);
                        backData = null;
                    }
                    if (!addHeaderOver && listReturn.size() < 10) {
                        Map<String, String> mapReturn = new HashMap<>();
                        mapReturn.put("img", "hide");
                        mapReturn.put("progress_img", "hide");
                        mapReturn.put("progress_text", "hide");
                        mapReturn.put("author", "1");
                        mapReturn.put("type", "1");
                        mapReturn.put("content", "您好，我是香哈小秘书，有什么建议、问题，可以随时给我说哦！活动、获奖通知也将在这里通知。");
                        mapReturn.put("timeShow", "hide");
                        contentList.add(0, mapReturn);
                        addHeaderOver = true;
                    }
                    adapter.notifyDataSetChanged();
                    feekback_list.setVisibility(View.VISIBLE);
                    if (isForward)
                        feekback_list.setSelection(adapter.getCount() - 1);
                    else
                        feekback_list.setSelection(1);
                }
                feekback_list.onRefreshComplete();
                loadManager.hideProgressBar();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.feebback_send:
                if (feekUrl != null && !feekUrl.equals("")) {
                    sendFeekback(feekUrl);
                    feekUrl = "";
                }
                feekback_text = feebback_reply_content.getText().toString();
                sendFeekback(feekback_text);
                feebback_reply_content.setText("");
                break;
            case R.id.feekback_img_choice:
                chooseImg();
                break;
        }
    }

    private void chooseImg() {
        Intent intent = new Intent();
        intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_MODE, ImageSelectorConstant.MODE_SINGLE);
        intent.setClass(this, ImageSelectorActivity.class);
        startActivityForResult(intent, FEEDBACK_UPLOADIMG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FEEDBACK_UPLOADIMG
                && data != null) {
            ArrayList<String> array = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);//ArrayList<String>
            if (array.size() > 0) {
                mImageUrl = array.get(0);
                if (!TextUtils.isEmpty(mImageUrl)) {
                    sendFeekImg(mImageUrl);
                } else {
                    Tools.showToast(this, "选择图片有误，请重新选择");
                }
            }
        }
    }

    // 发送反馈img
    private void sendFeekImg(final String imgUrl) {
//		String param = "token="+Token+ "&content= &uploadImg_img_0=" + imgUrl;
        String addTime = Tools.getAssignTime("yyyy-MM-dd HH:mm:ss", 0);
        String timeShow = Tools.getAssignTime("HH:mm", 0);
        Map<String, String> map = new HashMap<String, String>();
        map.put("author", "2");
        map.put("timeShow", timeShow);
        map.put("addTime", addTime);
        map.put("img", imgUrl);
        map.put("once", "0");
        map.put("progress_img", "start");
        map.put("progress_text", "hide");
        contentList.add(map);
        adapter.notifyDataSetChanged();
        feekback_list.setSelection(adapter.getCount() - 1);
        LinkedHashMap<String, String> fileMap = new LinkedHashMap<String, String>();
        fileMap.put("token", Token);
        fileMap.put("content", "");
        fileMap.put("uploadImg_img_0", imgUrl);
        ReqInternet.in().doPostImg(StringManager.api_sendDialog, fileMap, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING)
                    notifySendMsg(MSG_IMG_UPLOAD);
            }
        });
        mImageUrl = "";
    }

    // 发送反馈信息
    private void sendFeekback(String feektext) {
        if (feektext.length() != 0) {
            String addTime = Tools.getAssignTime("yyyy-MM-dd HH:mm:ss", 0);
            String timeShow = Tools.getAssignTime("HH:mm", 0);
            Map<String, String> map = new HashMap<>();
            map.put("author", "2");
            map.put("content", feektext);
            map.put("timeShow", timeShow);
            map.put("addTime", addTime);
            map.put("img", "hide");
            map.put("progress_img", "hide");
            if (feekUrl != null && feekUrl.equals(feektext))
                map.put("progress_text", "hide");
            else
                map.put("progress_text", "start");
            contentList.add(map);
            adapter.notifyDataSetChanged();
            feekback_list.setSelection(adapter.getCount() - 1);
            String param = "token=" + Token + "&content=" + feektext;
            ReqInternet.in().doPost(StringManager.api_sendDialog, param, new InternetCallback(this) {
                @Override
                public void loaded(int flag, String url, Object returnObj) {
                    if (flag >= UtilInternet.REQ_OK_STRING) {
//						Tools.showToast(Feedback.this, returnObj.toString());
                        notifySendMsg(MSG_TEXT_UPLOAD);
                    }
                }
            });
        } else
            Tools.showToast(this, "请输入反馈内容");
    }

    public static void notifySendMsg(int what) {
        Message msg = handler.obtainMessage();
        switch (what) {
            case MSG_IMG_UPLOAD:
                msg.what = MSG_IMG_UPLOAD;
                break;
            case MSG_TEXT_UPLOAD:
                msg.what = MSG_TEXT_UPLOAD;
                break;
            case MSG_FROM_NOTIFY:
                msg.what = MSG_FROM_NOTIFY;
                break;
        }
        if (handler != null)
            handler.sendMessage(msg);
    }

    /**
     * 统计反馈的来源
     * 1 从首页   2 从菜谱   3 从个人
     */
    private void doTongJi() {
        if (from != null && !from.equals("")) {
            if (from.equals("1")) {
                XHClick.onEvent(Feedback.this, "appClick", "反馈从首页");
            } else if (from.equals("2")) {
                XHClick.onEvent(Feedback.this, "appClick", "反馈从菜谱");
            } else if (from.equals("3")) {
                XHClick.onEvent(Feedback.this, "appClick", "反馈从个人");
            } else if (from.equals("4")) {
                XHClick.onEvent(Feedback.this, "appClick", "反馈从商品");
            } else if (from.equals("5")) {
                XHClick.onEvent(Feedback.this, "appClick", "反馈从发现圈子");
            }
        }
    }

    public final static int MSG_IMG_UPLOAD = 1;
    public final static int MSG_FROM_NOTIFY = 2;
    public final static int MSG_TEXT_UPLOAD = 3;
}