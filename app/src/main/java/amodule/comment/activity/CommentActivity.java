package amodule.comment.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.article.activity.ReportActivity;
import amodule.comment.view.ViewCommentItem;
import amodule.user.activity.login.LoginByAccout;
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
    private int dropPage = 1,upDropPage = 1,downDropPage = 1,slide = 1,from = 1;

    private EditText commend_write_et;
    private ImageView writePen;
    private TextView sendTv;
    private View sendProgress;

    private String gotoCommentId,gotoReplayId;

    private String contentTongjiId,likeTongjiId = "a_like",reportTongjiId="a_report",deleteTongjiId = "a_delete";
    private String likeTwoLeven,reportTwoLeven,deleteTwoLeven;

    private boolean isShowKeyboard = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("评论", 5, 0, R.layout.a_common_post_new_title, R.layout.a_comment_activity);
//        setContentView(R.layout.a_comment_activity);
        initView();
        initData();
    }

    private void initView() {
        final RelativeLayout bottomBarLayout = (RelativeLayout) findViewById(R.id.a_comment_keyboard_parent);
        final int dp45 = Tools.getDimen(this,R.dimen.dp_45);
        rl.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        int heightDiff = rl.getRootView().getHeight() - rl.getHeight();
                        Rect r = new Rect();
                        rl.getWindowVisibleDisplayFrame(r);
                        int screenHeight = rl.getRootView().getHeight();
                        int heightDifference = screenHeight - (r.bottom - r.top);
                        isShowKeyboard = heightDifference > 200;
                        heightDifference = isShowKeyboard ? heightDifference - heightDiff + dp45 : 0;
                        bottomBarLayout.setPadding(0, 0, 0, heightDifference);
                    }
                });
        listArray = new ArrayList<>();
        findViewById(R.id.commend_hind).setOnClickListener(this);
        sendTv = (TextView) findViewById(R.id.comment_send);
        sendTv.setOnClickListener(this);
        sendTv.setClickable(false);
        sendProgress = findViewById(R.id.comment_send_progress);
        writePen = (ImageView) findViewById(R.id.commend_write_pen);
        commend_write_et = (EditText) findViewById(R.id.commend_write_et);
        commend_write_et.setOnClickListener(this);
        commend_write_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && StringManager.api_addForum.equals(currentUrl)){
                    XHClick.mapStat(CommentActivity.this,contentTongjiId,"点击评论框","");
                }
                changeKeyboard(hasFocus);
            }
        });
        commend_write_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                sendTv.setClickable(s.length() > 0);
                sendTv.setTextColor(s.length() > 0 ? Color.parseColor("#333333") : Color.parseColor("#cccccc"));
            }
        });
        downRefreshList = (DownRefreshList) findViewById(R.id.comment_listview);
        downRefreshList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                Log.i("commentReplay","downRefreshList onTouch() isShowKeyboard:" + isShowKeyboard);
                if(isShowKeyboard) {
                    currentUrl = StringManager.api_addForum;
                    changeKeyboard(false);
                }
                return false;
            }
        });
        adapterSimple = new AdapterSimple(downRefreshList, listArray, R.layout.a_comment_item, new String[]{}, new int[]{}) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                final ViewCommentItem viewCommentItem = (ViewCommentItem) view.findViewById(R.id.comment_item);
                viewCommentItem.setCommentItemListener(getCommentItenListener(viewCommentItem,position));
                viewCommentItem.setUserInforListenr(getUserInforListener());
                viewCommentItem.setData(listArray.get(position));
                return view;
            }
        };
    }

    private void changeDataChange(){
        Log.i("commentActivity","changeDataChange() size:" + listArray.size());
        if(listArray.size() == 0){
            downRefreshList.setVisibility(View.GONE);
            findViewById(R.id.commend_hind).setVisibility(View.VISIBLE);
        }else{
            downRefreshList.setVisibility(View.VISIBLE);
            downRefreshList.onRefreshComplete();
            findViewById(R.id.commend_hind).setVisibility(View.GONE);
        }
    }

    private ViewCommentItem.OnCommentItenListener getCommentItenListener(final ViewCommentItem viewCommentItem, final int position){
        return new ViewCommentItem.OnCommentItenListener() {
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
            public void onReportCommentClick(String comment_id, String comment_user_code, String comment_user_name, String reportContent,String reportType) {
                Tools.showToast(CommentActivity.this,"举报评论 " + comment_id);
                XHClick.mapStat(CommentActivity.this,reportTongjiId,reportTwoLeven,reportType);
                Intent intent = new Intent(CommentActivity.this,ReportActivity.class);
                intent.putExtra("type",type);
                intent.putExtra("code",code);
                intent.putExtra("commentId",comment_id);
                intent.putExtra("userCode",comment_user_code);
                intent.putExtra("reportName",comment_user_name);
                intent.putExtra("reportContent",reportContent);
                startActivity(intent);
            }

            @Override
            public void onReportReplayClick(String comment_id, String replay_id, String replay_user_code, String replay_user_name, String reportContent) {
                XHClick.mapStat(CommentActivity.this,reportTongjiId,reportTwoLeven,"点击楼中楼的举报");
                Intent intent = new Intent(CommentActivity.this,ReportActivity.class);
                intent.putExtra("type",type);
                intent.putExtra("code",code);
                intent.putExtra("commentId",comment_id);
                intent.putExtra("replayId",replay_id);
                intent.putExtra("userCode",replay_user_code);
                intent.putExtra("reportName",replay_user_name);
                intent.putExtra("reportContent",reportContent);
                startActivity(intent);
            }

            @Override
            public void onDeleteReplayClick(String comment_id, String replay_id) {
                XHClick.mapStat(CommentActivity.this,deleteTongjiId,deleteTwoLeven,"点击楼中楼的删除按钮");
                requstInternet(StringManager.api_delReplay,"type=" + type + "&code=" + code + "&commentId="+comment_id + "&replayId=" + replay_id);
            }

            @Override
            public void onDeleteCommentClick(final String comment_id,String deleteType) {
                XHClick.mapStat(CommentActivity.this,deleteTongjiId,deleteTwoLeven,deleteType);
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
                                    changeDataChange();
                                }
                            }
                        });
                        xhDialog.cancel();
                    }
                }).show();
            }

            @Override
            public void onPraiseClick(String comment_id) {
                if(!LoginManager.isLogin()){
                    Tools.showToast(CommentActivity.this,"请先登录或注册哦~");
                    Intent intent = new Intent(CommentActivity.this, LoginByAccout.class);
                    startActivity(intent);
                    return;
                }
                XHClick.mapStat(CommentActivity.this,contentTongjiId,"点赞","");
                XHClick.mapStat(CommentActivity.this,likeTongjiId,likeTwoLeven,"");
                String params = "type=" + type + "&code=" + code + "&commentId=" + comment_id;
                ReqEncyptInternet.in().doEncypt(StringManager.api_likeForum, params, new InternetCallback(CommentActivity.this) {
                    @Override
                    public void loaded(int i, String s, Object o) {
                        if(i >= ReqInternet.REQ_OK_STRING) {
                            ArrayList<Map<String,String>> arrayList = StringManager.getListMapByJson(o);
                            if(arrayList.size() > 0) {
                                Map<String, String> map = listArray.get(position);
                                map.put("is_fabulous", "2");
                                map.put("fabulous_num", arrayList.get(0).get("num"));
                                adapterSimple.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }

            @Override
            public void onContentReplayClick(String comment_id,String replay_code, String replay_name,String type) {
                XHClick.mapStat(CommentActivity.this,contentTongjiId,"回复",type);
                Log.i("commentReplay","onContentReplayClick() replay_name:" + replay_name);
                commend_write_et.setHint(" 回复" + replay_name);
                currentUrl = StringManager.api_addReplay;
                currentParams = "&commentId=" + comment_id + "&replyUcode=" + replay_code;
                changeKeyboard(true);
            }
        };
    }

    private ViewCommentItem.OnUserInforListener getUserInforListener(){
        return new ViewCommentItem.OnUserInforListener() {
            @Override
            public void onReplayUserNameClick(boolean isAuther, String userName) {
                XHClick.mapStat(CommentActivity.this,contentTongjiId,"用户信息",isAuther ? "点击楼层作者用户名" : "点击楼层其他用户名");
            }
            @Override
            public void onCommentUserNameClick(String userName) {
                XHClick.mapStat(CommentActivity.this,contentTongjiId,"用户信息","点击评论用户名");
            }
            @Override
            public void onCommentUserIconClick() {
                XHClick.mapStat(CommentActivity.this,contentTongjiId,"用户信息","点击用户头像");
            }
            @Override
            public void onCommentUserVipClick() {
                XHClick.mapStat(CommentActivity.this,contentTongjiId,"用户信息","点击会员icon");
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
        gotoCommentId = getIntent().getStringExtra("commentId");
        gotoReplayId = getIntent().getStringExtra("replayId");
        String fromType = getIntent().getStringExtra("from");
        if(!TextUtils.isEmpty(fromType)){
            from = Integer.parseInt(fromType);
        }

        if(TextUtils.isEmpty(type)) {
            type = "1";
            code = "520";
        }

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(code)) {
            Tools.showToast(this, "缺少 类型 或 主题");
            finish();
        }

        if("1".equals(type)){
            contentTongjiId = "a_article_comment";
            likeTwoLeven = "文章评论点赞量";
            reportTwoLeven = "文章";
            deleteTwoLeven = "文章";
        }else if("2".equals(type)){
            contentTongjiId = "a_video_comment";
            likeTwoLeven = "视频评论点赞量";
            reportTwoLeven = "视频";
            deleteTwoLeven = "视频";
        }else{
            Tools.showToast(this, "类型不对");
            finish();
        }



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
            slide = 2;
            if(listArray.size() > 0){
                gotoCommentId = listArray.get(0).get("comment_id");
            }
            dropPage = upDropPage;
        } else {
            currentPage++;
            slide = 1;
            if(listArray.size() > 0){
                gotoCommentId = listArray.get(listArray.size() - 1).get("comment_id");
            }
            dropPage = downDropPage;
        }

        loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage, listArray.size() == 0);
        String params = "type=" + type + "&code=" + code;
        if(!TextUtils.isEmpty(gotoCommentId))
            params +=  "&commentId=" + gotoCommentId;
        if(!TextUtils.isEmpty(gotoReplayId))
            params += "&replayId=" + gotoReplayId;;
        params += "&from=" + from + "&slide=" + slide + "&dropPage=" + dropPage;
        Log.i("commentReplay","getCommentData() params:" + params);
        ReqEncyptInternet.in().doEncypt(StringManager.api_forumList, params, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String s, Object o) {
                int loadCount = 0;
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    from = 1;
                    ArrayList<Map<String,String>> arrayList =StringManager.getListMapByJson(o);
                    if(arrayList.size() > 0) {
                        Map<String,String> dataMap = arrayList.get(0);
                        String dataList = dataMap.get("list");
                        String dataPage = dataMap.get("page");
                        arrayList = StringManager.getListMapByJson(dataList);
                        if(isForward){
                            if(dropPage == 1){
                                listArray.clear();
                                Log.i("commentReplay","清除数据，重新加载");
                            }
                            listArray.addAll(0,arrayList);
                        }else{
                            listArray.addAll(arrayList);
                        }
                        if(!TextUtils.isEmpty(dataPage) && !"null".equals(dataPage))
                            if(isForward)
                                upDropPage = Integer.parseInt(dataPage);
                            else
                                downDropPage = Integer.parseInt(dataPage);

                        adapterSimple.notifyDataSetChanged();
                        loadCount = arrayList.size();
                        ;
                        if (everyPage == 0)
                            everyPage = loadCount;
                    }
                }
                changeDataChange();
                currentPage = loadManager.changeMoreBtn(downRefreshList, flag, everyPage, loadCount, currentPage, listArray.size() == 0,"没有更多评论啦");
            }
        });
    }

    private String currentUrl = StringManager.api_addForum,currentParams;
    private boolean isSend = false,isAddForm;
    private synchronized void sendData(){
        if(isSend)return;
        isSend = true;
        if(!LoginManager.isLogin()){
            Tools.showToast(this,"请先登录或注册哦~");
            Intent intent = new Intent(CommentActivity.this, LoginByAccout.class);
            startActivity(intent);
        }
        sendProgress.setVisibility(View.VISIBLE);
        String content = commend_write_et.getText().toString();
        if(content.length() == 0){
            Tools.showToast(this,"发送内容不能为空");
            return;
        }
        if(content.length() > 2000){
            Tools.showToast(this,"发送内容不能超过2000字");
            return;
        }

        String newParams;
        isAddForm = false;
        if(StringManager.api_addForum.equals(currentUrl)){
            isAddForm = true;
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
        Log.i("commentReplay","sendData() newParams:" + newParams);
        ReqEncyptInternet.in().doEncypt(currentUrl,newParams,new InternetCallback(this){
            @Override
            public void loaded(int flag, String s, Object o) {
                if(flag >= ReqInternet.REQ_OK_STRING) {
                    changeKeyboard(false);
//                    if(isAddForm){
//                        ArrayList<Map<String,String>> arrayList = StringManager.getListMapByJson(o);
//                        if(arrayList.size() > 0){
//                            Map<String,String> newCotent = new HashMap<>();
//                            String comment_id = arrayList.get(0).get("comment_id");
//                            newCotent.put("comment_id",comment_id);
//                            newCotent.put("create_time","刚刚");
//                            newCotent.put("fabulous_num","0");
//                            newCotent.put("is_anchor","1");
//                            newCotent.put("is_del_report","2");
//                            newCotent.put("is_fabulous","1");
//                            newCotent.put("replay_count","0");
//                            newCotent.put("replay_num","0");
//                            newCotent.put("replay","");
//                            JSONArray customerJsonArray = new JSONArray();
//                            newCotent.put("customer","");
//
//                        }
//                    }else{
//
//                    }
                }else{
                    sendProgress.setVisibility(View.GONE);
                }
                isSend = false;
            }
        });
    }

    private void changeKeyboard(boolean isShow){
        isShowKeyboard = isShow;
        int dp10 = Tools.getDimen(this,R.dimen.dp_10);
        if(isShow){
            int dp13 = Tools.getDimen(this,R.dimen.dp_13);
            commend_write_et.requestFocus();
            ToolsDevice.keyboardControl(true,CommentActivity.this,commend_write_et);
            commend_write_et.setHintTextColor(Color.parseColor("#cdcdcd"));
            commend_write_et.setPadding(dp13,dp10,dp13,dp10);
            sendTv.setVisibility(View.VISIBLE);
            writePen.setVisibility(View.GONE);
        }else{
            int dp30 = Tools.getDimen(this,R.dimen.dp_30);
            sendTv.setVisibility(View.GONE);
            writePen.setVisibility(View.VISIBLE);
            commend_write_et.setHint(" 写评论");
            commend_write_et.setHintTextColor(Color.parseColor("#333333"));
            commend_write_et.setText("");
            commend_write_et.setPadding(dp30,dp10,0,dp10);
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
                XHClick.mapStat(CommentActivity.this,contentTongjiId,"点击评论框","");
                changeKeyboard(true);
                break;
        }
    }
}
