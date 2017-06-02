package amodule.comment.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.article.activity.ReportActivity;
import amodule.comment.view.ViewCommentItem;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
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
    private ImageView writePen;
    private TextView sendTv;
    private View sendProgress;

    private String gotoCommentId,gotoReplayId;

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
        writePen = (ImageView) findViewById(R.id.commend_write_pen);
        commend_write_et = (EditText) findViewById(R.id.commend_write_et);
        commend_write_et.setOnClickListener(this);
        commend_write_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                changeKeyboard(hasFocus);
            }
        });
        downRefreshList = (DownRefreshList) findViewById(R.id.comment_listview);
        downRefreshList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                sendTv.setVisibility(View.GONE);
                currentUrl = StringManager.api_addForum;
                changeKeyboard(false);
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
                        ReqEncyptInternet.in().doEncypt(StringManager.api_replayList, "type=" + type + "&code=" + code + "&commentId=" + comment_id , new InternetCallback(CommentActivity.this) {
                            @Override
                            public void loaded(int flag, String s, Object o) {
                                if(flag >= ReqInternet.REQ_OK_STRING){
//                                    ArrayList<Map<String,String>> arrayList = StringManager.getListMapByJson(o);
//                                    Map<String,String> oldCommentMap = listArray.get(position);
//                                    oldCommentMap.put("replay_num","0");
//                                    String oldReplay = oldCommentMap.get("replay");
//                                    ArrayList<Map<String,String>> oldReplayArray = StringManager.getListMapByJson(oldReplay);
//                                    oldReplayArray.addAll(arrayList);
//                                    adapterSimple.notifyDataSetChanged();
                                    viewCommentItem.addReplayView(o.toString());
                                }
                            }
                        });
                    }

                    @Override
                    public void onReportCommentClick(String comment_id, String comment_user_code, String comment_user_name, String reportContent) {
                        Tools.showToast(CommentActivity.this,"举报评论 " + comment_id);
                        Intent intent = new Intent(CommentActivity.this,ReportActivity.class);
                        intent.putExtra("type",type);
                        intent.putExtra("code",code);
                        intent.putExtra("commendId",comment_id);
                        intent.putExtra("userCode",comment_user_code);
                        intent.putExtra("reportName",comment_user_name);
                        intent.putExtra("reportContent",reportContent);
                        startActivity(intent);
                    }

                    @Override
                    public void onReportReplayClick(String comment_id, String replay_id, String replay_user_code, String replay_user_name, String reportContent) {
                        Tools.showToast(CommentActivity.this,"举报回复 " + comment_id + "  " +replay_id);
                        Intent intent = new Intent(CommentActivity.this,ReportActivity.class);
                        intent.putExtra("type",type);
                        intent.putExtra("code",code);
                        intent.putExtra("commendId",comment_id);
                        intent.putExtra("replayId",replay_id);
                        intent.putExtra("userCode",replay_user_code);
                        intent.putExtra("reportName",replay_user_name);
                        intent.putExtra("reportContent",reportContent);
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteReplayClick(String comment_id, String replay_id) {
                        requstInternet(StringManager.api_delReplay,"type=" + type + "&code=" + code + "&commentId="+comment_id + "&replayId=" + replay_id);
                    }

                    @Override
                    public void onDeleteCommentClick(final String comment_id) {
                        final XhDialog xhDialog = new XhDialog(CommentActivity.this);
                        xhDialog.setTitle("确认删除我的评论？").setCanselButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                xhDialog.cancel();
                            }
                        }).setSureButton("确认", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String params = "type=" + type + "&code=" + code + "&commentId="+comment_id;
                                ReqEncyptInternet.in().doEncypt(StringManager.api_delForum, params, new InternetCallback(CommentActivity.this) {
                                    @Override
                                    public void loaded(int flag, String s, Object o) {
                                        if(flag >= ReqInternet.REQ_OK_STRING){
                                            listArray.remove(position);
                                            adapterSimple.notifyDataSetChanged();
                                        }
                                    }
                                });
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
                        commend_write_et.setHint(" 回复" + replay_name);
                        changeKeyboard(true);
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
        gotoCommentId = getIntent().getStringExtra("gotoCommentId");
        gotoReplayId = getIntent().getStringExtra("gotoReplayId");

        if(TextUtils.isEmpty(type)) {
            type = "1";
            code = "520";
        }

//        gotoCommentId = "1";
//        gotoReplayId = "1";

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

        loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage, listArray.size() == 0);
        String params = "type=" + type + "&code=" + code + "&page=" + currentPage;
        if(!TextUtils.isEmpty(gotoCommentId))
            params +=  "&commentId=" + gotoCommentId;
        if(!TextUtils.isEmpty(gotoReplayId))
            params += "&replayId=" + gotoReplayId;;
        ReqEncyptInternet.in().doEncypt(StringManager.api_forumList, params, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String s, Object o) {
                int loadCount;
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    if (isForward) listArray.clear();
                    ArrayList<Map<String,String>> arrayList =StringManager.getListMapByJson(o);
                    if(arrayList.size() > 0) {
                        Map<String,String> dataMap = arrayList.get(0);
                        String dataList = dataMap.get("list");
                        String dataPage = dataMap.get("page");
                        if(!TextUtils.isEmpty(dataPage)){
                            currentPage = Integer.parseInt(dataPage);
                        }
                        listArray.addAll(StringManager.getListMapByJson(dataList));
                        adapterSimple.notifyDataSetChanged();
                        loadCount = arrayList.size();
                        ;
                        if (everyPage == 0)
                            everyPage = loadCount;
                        downRefreshList.setVisibility(View.VISIBLE);
                        currentPage = loadManager.changeMoreBtn(downRefreshList, flag, everyPage, loadCount, currentPage, listArray.size() == 0);
                        downRefreshList.onRefreshComplete();
                    }
                }
            }
        });
    }

    private String currentUrl = StringManager.api_addForum,currentParams;
    private void sendData(){
        sendProgress.setVisibility(View.VISIBLE);
        String content = commend_write_et.getText().toString();
        if(content.length() > 2000){
            Tools.showToast(this,"发送内容不能超过2000字");
            return;
        }

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
        ReqEncyptInternet.in().doEncypt(currentUrl,newParams,new InternetCallback(this){
            @Override
            public void loaded(int flag, String s, Object o) {
                if(flag >= ReqInternet.REQ_OK_STRING) {
                    changeKeyboard(false);
                }
            }
        });
    }

    private void changeKeyboard(boolean isShow){
        if(isShow){
            ToolsDevice.keyboardControl(true,CommentActivity.this,commend_write_et);
            commend_write_et.setHintTextColor(Color.parseColor("#cdcdcd"));
            commend_write_et.setPadding(Tools.getDimen(this,R.dimen.dp_13),Tools.getDimen(this,R.dimen.dp_10),0,Tools.getDimen(this,R.dimen.dp_10));
            sendTv.setVisibility(View.VISIBLE);
            writePen.setVisibility(View.GONE);
        }else{
            sendTv.setVisibility(View.GONE);
            writePen.setVisibility(View.VISIBLE);
            commend_write_et.setHint(" 写评论");
            commend_write_et.setHintTextColor(Color.parseColor("#333333"));
            commend_write_et.setText("");
            commend_write_et.setPadding(Tools.getDimen(this,R.dimen.dp_30),Tools.getDimen(this,R.dimen.dp_10),0,Tools.getDimen(this,R.dimen.dp_10));
            commend_write_et.clearFocus();
            sendProgress.setVisibility(View.GONE);
            ToolsDevice.keyboardControl(false,CommentActivity.this,commend_write_et);
        }
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
                changeKeyboard(true);
                break;
        }
    }
}
