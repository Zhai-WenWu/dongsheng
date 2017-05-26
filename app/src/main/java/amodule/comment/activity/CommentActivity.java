package amodule.comment.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.comment.view.ViewCommentItem;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import xh.windowview.XhDialog;

/**
 * Created by Fang Ruijiao on 2017/5/25.
 */
public class CommentActivity extends BaseActivity implements View.OnClickListener {
    private DownRefreshList downRefreshList;
    private AdapterSimple adapterSimple;
    private ArrayList<Map<String, String>> listArray;
    private String type, code;
    private int currentPage = 0, everyPage = 0;

    private EditText commend_write_et;
    private TextView sendTv;
    private View sendProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("评论", 5, 0, R.layout.a_common_post_new_title, R.layout.a_comment_activity);
//        setContentView(R.layout.a_comment_activity);
        initView();
        initData();
    }

    private void initView() {
        listArray = new ArrayList<>();
        findViewById(R.id.commend_hind).setOnClickListener(this);
        sendTv = (TextView) findViewById(R.id.comment_send);
        sendTv.setOnClickListener(this);
        sendProgress = findViewById(R.id.comment_send_progress);
        commend_write_et = (EditText) findViewById(R.id.commend_write_et);
        downRefreshList = (DownRefreshList) findViewById(R.id.comment_listview);
        downRefreshList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                sendTv.setVisibility(View.GONE);
                commend_write_et.setHint("写评论");
                currentUrl = StringManager.api_addForum;

                ToolsDevice.keyboardControl(false,CommentActivity.this,commend_write_et);
                return false;
            }
        });
        adapterSimple = new AdapterSimple(downRefreshList, listArray, R.layout.a_comment_item, new String[]{}, new int[]{}) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                final ViewCommentItem viewCommentItem = (ViewCommentItem) view.findViewById(R.id.comment_item);
                viewCommentItem.setData(listArray.get(position));
                viewCommentItem.setCommentItemListener(new ViewCommentItem.OnCommentItenListener() {
                    @Override
                    public void onShowAllReplayClick(String comment_id) {
                        try {
                            JSONArray jsonArrayReplay = new JSONArray();
                            for (int k = 0; k < 3; k++) {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("replay_id", "1");
                                jsonObject.put("content", "哈哈哈哈哈哈哈或暗室逢灯辣椒粉啦");
                                jsonObject.put("ucode", "123");
                                jsonObject.put("uname", "米西");
                                jsonObject.put("is_author", "2");
                                jsonObject.put("replay_ucode", "343");
                                jsonObject.put("replay_uname", "雨季不来");
                                jsonObject.put("is_replay_author", "1");
                                jsonObject.put("create_time", "2017-05-24 10:34:27");
                                jsonArrayReplay.put(jsonObject);
                            }
                            viewCommentItem.addReplayView(jsonArrayReplay.toString());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onReportReplayClick(String comment_id, String replay_id) {
                        Tools.showToast(CommentActivity.this,"举报回复 " + comment_id + "  " +replay_id);
                    }

                    @Override
                    public void onReportCommentClick(String comment_id) {
                        Tools.showToast(CommentActivity.this,"举报评论 " + comment_id);
                    }

                    @Override
                    public void onDeleteReplayClick(String comment_id, String replay_id) {
                        Tools.showToast(CommentActivity.this,"删除回复" + comment_id);
                        requstInternet(StringManager.api_delReplay,"type=" + type + "&code=" + code + "&commentId="+comment_id + "&replayId=" + replay_id);
                    }

                    @Override
                    public void onDeleteCommentClick(final String comment_id) {
                        Tools.showToast(CommentActivity.this,"删除评论" + comment_id);
                        final XhDialog xhDialog = new XhDialog(CommentActivity.this);
                        xhDialog.setTitle("确认删除我的评论？").setCanselButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                xhDialog.cancel();
                            }
                        }).setSureButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requstInternet(StringManager.api_delForum,"type=" + type + "&code=" + code + "&commentId="+comment_id);
                                xhDialog.cancel();
                            }
                        }).show();
                    }

                    @Override
                    public void onPraiseClick(String comment_id) {
                        Map<String,String> map = listArray.get(position);
                        map.put("is_fabulous","2");
                        requstInternet(StringManager.api_likeForum,"type=" + type + "&code=" + code + "&commentId=" + comment_id);
                    }

                    @Override
                    public void onContentReplayClick(String comment_id,String replay_code, String replay_name) {
                        commend_write_et.setHint("回复 " + replay_name);
                        ToolsDevice.keyboardControl(true,CommentActivity.this,commend_write_et);
                        sendTv.setVisibility(View.VISIBLE);
                        currentUrl = StringManager.api_addReplay;
                        currentParams = "&commentId=" + comment_id + "&replyUcode=" + replay_code;
                    }
                });
                return view;
            }
        };
    }

    private void requstInternet(String url, String params){
        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback(CommentActivity.this) {
            @Override
            public void loaded(int i, String s, Object o) {

            }
        });
    }

    private void initData() {
        type = getIntent().getStringExtra("type");
        code = getIntent().getStringExtra("code");
//        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(code)) {
//            Tools.showToast(this, "缺少 类型 或 主题");
//            finish();
//        }
        loadManager.showProgressBar();
        loadManager.setLoading(downRefreshList, adapterSimple, true, new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                getCommentData(false);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCommentData(true);
            }
        });

    }

    private void getCommentData(final boolean isForward) {
        if (isForward) {
            currentPage = 1;
        } else
            currentPage++;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                ArrayList<Map<String, String>> arrayList = new ArrayList<Map<String, String>>();
                Map<String, String> map;
                try {
                    for (int i = 0; i < 20; i++) {
                        map = new HashMap<>();
                        map.put("comment_id", "1");
                        map.put("fabulous_num", "100");
                        map.put("replay_num", "3");
                        map.put("is_del_report", "1");
                        JSONArray jsonArray = new JSONArray();
                        for (int k = 0; k < 2; k++) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("text", "附近一小镇的有名小吃，据说还申报了非遗。老板边做边卖边和你说笑，去了几次，基本看会了,附近一小镇的有名小吃，据说还申报了非遗。老板边做边卖边和你说笑，去了几次，基本看会了,附近一小镇的有名小吃，据说还申报了非遗。老板边做边卖边和你说笑，去了几次，基本看会了,附近一小镇的有名小吃，据说还申报了非遗。老板边做边卖边和你说笑，去了几次，基本看会了");
                            JSONArray jsonArray2 = new JSONArray();
                            JSONObject jsonObject2 = new JSONObject();
                            jsonObject2.put("","http://s1.cdn.xiangha.com/quan/201705/2510/59263e464df1c.jpg/MjUwX2MxXzE4MA.webp");
                            jsonArray2.put(jsonObject2);
                            JSONObject jsonObject3 = new JSONObject();
                            jsonObject3.put("","http://s1.cdn.xiangha.com/quan/201705/2510/59263e464df1c.jpg/MjUwX2MxXzE4MA.webp");
                            jsonArray2.put(jsonObject3);
                            jsonObject.put("imgs",jsonArray2);
                            jsonArray.put(jsonObject);
                        }
                        map.put("content",jsonArray.toString());
                        map.put("create_time","刚刚");
                        map.put("is_fabulous","1");
                        JSONArray jsonArrayCustomer = new JSONArray();
                        for (int k = 0; k < 2; k++) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("ucode", "111");
                            jsonObject.put("nick_name", "米西");
                            jsonObject.put("header_img", "http://s1.cdn.xiangha.com/i/201704/1819/58f5fd32928c7.jpg/MTAweDEwMA");
                            jsonObject.put("is_gourmet", "2");
                            jsonObject.put("is_member", "2");
                            jsonObject.put("name_color", "#ff533c");
                            jsonObject.put("is_author", "2");
                            jsonArrayCustomer.put(jsonObject);
                        }
                        map.put("customer",jsonArrayCustomer.toString());

                        JSONArray jsonArrayReplay = new JSONArray();
                        for (int k = 0; k < 3; k++) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("replay_id", "1");
                            jsonObject.put("content", "哈哈哈哈哈哈哈或暗室逢灯辣椒粉啦");
                            jsonObject.put("ucode", "123");
                            jsonObject.put("uname", "米西");
                            jsonObject.put("is_author", "2");
                            jsonObject.put("replay_ucode", "343");
                            jsonObject.put("replay_uname", "雨季不来");
                            jsonObject.put("is_replay_author", "1");
                            jsonObject.put("create_time", "2017-05-24 10:34:27");
                            jsonArrayReplay.put(jsonObject);
                        }
                        map.put("replay",jsonArrayReplay.toString());
                        listArray.add(map);
                        adapterSimple.notifyDataSetChanged();
                        loadManager.hideProgressBar();
                        downRefreshList.setVisibility(View.VISIBLE);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, 500);

//        loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage, listArray.size() == 0);
//        String params = "?type=" + type + "&code=" + code + "&page=" + currentPage;
//        ReqEncyptInternet.in().doEncypt(StringManager.api_forumList, params, new InternetCallback(this) {
//            @Override
//            public void loaded(int flag, String s, Object o) {
//                int loadCount = 0;
//                if (flag >= UtilInternet.REQ_OK_STRING) {
//                    if (isForward) listArray.clear();
//                    ArrayList<Map<String,String>> arrayList = StringManager.getListMapByJson(o);
//
//
//                    loadCount = arrayList.size();;
//                    if (everyPage == 0)
//                        everyPage = loadCount;
//                    currentPage = loadManager.changeMoreBtn(downRefreshList, flag, everyPage, loadCount, currentPage, listArray.size() == 0);
//                    downRefreshList.setVisibility(View.VISIBLE);
//                    adapterSimple.notifyDataSetChanged();
//                }
//            }
//        });
    }

    private String currentUrl,currentParams;
    private void sendData(){
        sendProgress.setVisibility(View.VISIBLE);
        String content = commend_write_et.getText().toString();
        if(content.length() > 2000){
            Tools.showToast(this,"发送内容不能超过2000字");
            return;
        }
        commend_write_et.setText("");
        String newParams;
        if(StringManager.api_addForum.equals(currentUrl)){
            JSONArray jsonArray = new JSONArray();
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("text", content);
                jsonArray.put(jsonObject);
            }catch (JSONException e){
                e.printStackTrace();
            }
            newParams = "type=" + type + "&code=" + code + currentParams + "&content=" + jsonArray.toString();
        }else{
            newParams = "type=" + type + "&code=" + code + currentParams + "&content=" + content;
        }
        ReqInternet.in().doPost(currentUrl,newParams,new InternetCallback(this){

            @Override
            public void loaded(int i, String s, Object o) {
                sendProgress.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.commend_hind:
                break;
            case R.id.comment_send:
                sendData();
                break;
            case R.id.commend_write_et:
                break;
        }
    }
}
