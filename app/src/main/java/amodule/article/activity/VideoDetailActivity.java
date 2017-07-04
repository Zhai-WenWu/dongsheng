package amodule.article.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.article.activity.edit.VideoEditActivity;
import amodule.article.adapter.ArticleDetailAdapter;
import amodule.article.adapter.VideoDetailAdapter;
import amodule.article.tools.VideoAdContorler;
import amodule.article.view.BottomDialog;
import amodule.article.view.CommentBar;
import amodule.article.view.VideoAllHeaderView;
import amodule.main.Main;
import amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver;
import amodule.user.activity.FriendHome;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;
import cn.srain.cube.views.ptr.PtrDefaultHandler;
import cn.srain.cube.views.ptr.PtrFrameLayout;
import third.share.BarShare;
import third.share.UserHomeShare;
import third.video.VideoPlayerController;
import xh.windowview.XhDialog;

import static amodule.article.activity.ArticleDetailActivity.TYPE_VIDEO;
import static amodule.article.adapter.ArticleDetailAdapter.TYPE_KEY;
import static amodule.article.adapter.ArticleDetailAdapter.Type_bigAd;
import static amodule.article.adapter.ArticleDetailAdapter.Type_comment;
import static amodule.article.adapter.ArticleDetailAdapter.Type_recommed;

/**
 * PackageName : amodule.article.activity
 * Created by MrTrying on 2017/5/31 20:01.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoDetailActivity extends BaseActivity {

    private TextView mTitle;
    private ImageView rightButton;
    private RelativeLayout dredgeVipLayout;
    private TextView dredgeVipImmediately;
    private PtrClassicFrameLayout refreshLayout;
    private ListView listView;
    /** 头部view */
    private VideoAllHeaderView mHaederLayout;
    private CommentBar mCommentBar;
    private XHWebView xhWebView;

    private VideoDetailAdapter detailAdapter;
    private VideoAdContorler mVideoAdContorler;
    private VideoPlayerController mVideoPlayerController = null;//视频控制器
    private ArrayList<Map<String, String>> allDataListMap = new ArrayList<>();//评论列表和推荐列表对数据集合
    private Map<String, String> commentMap = new HashMap<>();
    private Map<String, String> adDataMap;
    private Map<String, String> shareMap = new HashMap<>();
    private Map<String,String> permissionMap = new HashMap<>();
    private Map<String,String> detailPermissionMap = new HashMap<>();
    private boolean hasPagePermission = true;
    private boolean contiunRefresh = true;
    private String lastPermission = "";
    private boolean isAuthor;
    private Map<String, String> customerData;
    private boolean hasPermission = true;

    private String commentNum = "0";
    private boolean isKeyboradShow = false;
    private String code = "";//请求数据的code
    private int page = 0;//相关推荐的page

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //保持高亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        },5 * 60 * 1000);
        //sufureView页面闪烁
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        initBundle();
        initView();
        initData();
    }
    //**********************************************Activity生命周期方法**************************************************
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mVideoPlayerController != null && !mVideoPlayerController.isError) {
            return mVideoPlayerController.onVDKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mVideoPlayerController != null) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mVideoPlayerController.setIsFullScreen(true);
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                mVideoPlayerController.setIsFullScreen(false);
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (loadManager != null)
            loadManager.hideProgressBar();
        page = 0;
        hasPagePermission = true;
        detailPermissionMap.clear();
        permissionMap.clear();
        if(mHaederLayout != null)
            mHaederLayout.setLoginStatus();
        refreshData(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Rect outRect = new Rect();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
        if (mVideoPlayerController != null) {
            mVideoPlayerController.onResume();
        }
        mHaederLayout.onResume();
        Glide.with(this).resumeRequests();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoPlayerController != null) {
            mVideoPlayerController.onPause();
        }
        mHaederLayout.onPause();
        Glide.with(this).pauseRequests();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoPlayerController != null) {
            mVideoPlayerController.onDestroy();
        }
    }

    private void initBundle() {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null)
            code = bundle.getString("code");
    }

    private void initView() {
        initActivity("", 2, 0, 0, R.layout.a_video_detail);
        //处理状态栏引发的问题
        initStatusBar();
        //初始化title
        initTitle();
        //初始化listview
        initListView();
        //初始化评论框
        initCommentBar();

        initDredgeVip();
    }

    private void initDredgeVip() {
        WebviewManager manager = new WebviewManager(this,loadManager,true);
        xhWebView = manager.createWebView(R.id.XHWebview);
        xhWebView.setVisibility(View.GONE);
        dredgeVipLayout = (RelativeLayout) findViewById(R.id.dredge_vip_bottom_layout);
        dredgeVipImmediately = (TextView) findViewById(R.id.dredge_vip_immediately);
        dredgeVipLayout.setVisibility(View.GONE);
    }

    private void initStatusBar() {
        if (Tools.isShowTitle()) {
            final RelativeLayout bottomBarLayout = (RelativeLayout) findViewById(R.id.edit_controler_layout);
            //设置layout监听，处理键盘弹出的高度问题
            rl.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            int heightDiff = rl.getRootView().getHeight() - rl.getHeight();
                            Rect r = new Rect();
                            rl.getWindowVisibleDisplayFrame(r);
                            int screenHeight = rl.getRootView().getHeight();
                            int heightDifference = screenHeight - (r.bottom - r.top);
                            isKeyboradShow = heightDifference > 200;
                            heightDifference = isKeyboradShow ? heightDifference - heightDiff : 0;
                            bottomBarLayout.setPadding(0, 0, 0, heightDifference);
                        }
                    });
        }
        String color = Tools.getColorStr(this, R.color.common_top_bg);
        Tools.setStatusBarColor(this, Color.parseColor(color));
    }

    /** 初始化title */
    private void initTitle() {
        View leftClose = findViewById(R.id.leftClose);
        leftClose.setVisibility(View.VISIBLE);
        leftClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Main.colse_level = 1;
                VideoDetailActivity.this.finish();
            }
        });
        mTitle = (TextView) findViewById(R.id.title);
        int dp85 = Tools.getDimen(this,R.dimen.dp_85);
        mTitle.setPadding(dp85,0,dp85,0);
        rightButton = (ImageView) findViewById(R.id.rightImgBtn2);
        ImageView leftImage = (ImageView) findViewById(R.id.leftImgBtn);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) leftImage.getLayoutParams();
        layoutParams.setMargins(Tools.getDimen(this, R.dimen.dp_15), 0, 0, 0);
        leftImage.setLayoutParams(layoutParams);
        RelativeLayout titleBar = (RelativeLayout) findViewById(R.id.relativeLayout_global);
        titleBar.setBackgroundColor(Color.parseColor("#00FFFFFE"));

        findViewById(R.id.back).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
    }

    /** 初始化ListView */
    private void initListView() {
        //初始化刷新layout
        refreshLayout = (PtrClassicFrameLayout) findViewById(R.id.refresh_list_view_frame);
        listView = (ListView) findViewById(R.id.listview);
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    //设置触摸收起键盘
                    case MotionEvent.ACTION_MOVE:
                        if (TextUtils.isEmpty(mCommentBar.getEditText().getText().toString()))
                            mCommentBar.setEditTextShow(false);
                        ToolsDevice.keyboardControl(false, VideoDetailActivity.this, mCommentBar.getEditText());
                        break;
                }
                return false;
            }
        });
        //initListView
        mHaederLayout = new VideoAllHeaderView(this);
        mHaederLayout.setType(TYPE_VIDEO);
        mHaederLayout.setCallBack(new VideoAllHeaderView.VideoViewCallBack() {
            @Override
            public void getVideoPlayerController(VideoPlayerController mVideoPlayerController) {
                VideoDetailActivity.this.mVideoPlayerController = mVideoPlayerController;
            }

            @Override
            public void gotoRequest() {

            }
        });
        listView.addHeaderView(mHaederLayout,null,false);
    }

    /** 初始化评论框 */
    private void initCommentBar() {
        mCommentBar = (CommentBar) findViewById(R.id.comment_bar);
        mCommentBar.setCode(code);
        mCommentBar.setType(TYPE_VIDEO);
        mCommentBar.setVisibility(View.GONE);
        mCommentBar.setOnCommentSuccessCallback(new CommentBar.OnCommentSuccessCallback() {
            @Override
            public void onCommentSuccess(boolean isSofa, Object obj) {
                try {
                    if (allDataListMap != null && allDataListMap.size() > 0) {
                        mCommentBar.setEditTextShow(false);
                        Map<String, String> newData = StringManager.getFirstMap(obj);
                        if (newData != null) {
                            int commentCount = Integer.parseInt(commentNum);
                            commentMap.put("commentNum", "" + ++commentCount);
                            commentNum = String.valueOf(commentCount);
                            Map<String, String> dataMap = StringManager.getFirstMap(commentMap.get("data"));
                            ArrayList<Map<String, String>> list = StringManager.getListMapByJson(dataMap.get("list"));
                            list.add(0, newData);
                            JSONArray jsonArray = StringManager.getJsonByArrayList(list);
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("list", jsonArray);
                            commentMap.put("data", jsonObject.toString());
                            commentMap.put(TYPE_KEY, String.valueOf(Type_comment));
                            commentMap.put("commentNum", commentNum);
                            if(allDataListMap.indexOf(commentMap) < 0)
                                allDataListMap.add(commentMap);
                            detailAdapter.notifyDataSetChanged();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int position = allDataListMap.indexOf(commentMap) + listView.getHeaderViewsCount();
                                    Log.i("tzy","position = " + position);
                                    AppCommon.scorllToIndex(listView,position);
                                }
                            },200);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** 数据初始化 **/
    private void initData() {
        if (TextUtils.isEmpty(code)) {
            Tools.showToast(this, "当前数据错误，请重新请求");
            return;
        }
        //初始化Adapter
        detailAdapter = new VideoDetailAdapter(this, allDataListMap, TYPE_VIDEO, code);
        detailAdapter.setOnRabSofaCallback(new ArticleDetailAdapter.OnRabSofaCallback() {
            @Override
            public void onRabSoaf() {
                mCommentBar.doComment("抢沙发");
            }
        });
        detailAdapter.setOnGetBigAdView(new ArticleDetailAdapter.OnGetBigAdView() {
            @Override
            public View getBigAdView(final Map<String, String> map) {
                View adView = mVideoAdContorler.getBigAdView(map);
                mVideoAdContorler.onBigAdBind(adView);
                return adView;
            }
        });
        detailAdapter.setmOnADCallback(new ArticleDetailAdapter.OnADCallback() {
            @Override
            public void onClick(View view, int index, String s) {
                if (mVideoAdContorler != null) mVideoAdContorler.onListAdClick(view, index, s);
            }

            @Override
            public void onBind(int index, View view, String s) {
                if (mVideoAdContorler != null) mVideoAdContorler.onListAdBind(index, view, s);
            }
        });
        refreshLayout.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                refreshData(false);
            }
        });
        listView.setAdapter(detailAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(mHaederLayout != null && mHaederLayout.getVideoHeaderView() != null){
                    View headerView = mHaederLayout.getVideoHeaderView();
                    int[] location = new int[2];
                    headerView.getLocationOnScreen(location);
                    int viewBottom = location[1] + headerView.getHeight();
                    int mixHeight = Tools.getStatusBarHeight(VideoDetailActivity.this) + Tools.getDimen(VideoDetailActivity.this,R.dimen.dp_45);
                    if(viewBottom <= mixHeight && mCommentBar.getVisibility() != View.VISIBLE){
                        dredgeVipLayout.setVisibility(View.VISIBLE);
                    }else
                        dredgeVipLayout.setVisibility(View.GONE);
                }
            }
        });
        View view = new View(this);
        view.setMinimumHeight(Tools.getDimen(this, R.dimen.dp_40));
        listView.addFooterView(view);
        //请求文章数据
        requestVideoData(false);
        //初始化广告
        initAD();
    }

    private void initAD() {
        mVideoAdContorler = new VideoAdContorler();
        mVideoAdContorler.initADData();
        mVideoAdContorler.setOnBigAdCallback(new VideoAdContorler.OnBigAdCallback() {
            @Override
            public void onBigAdData(Map<String, String> adDataMap) {
                if(adDataMap == null || adDataMap.isEmpty())
                    return;
                VideoDetailActivity.this.adDataMap = adDataMap;
                addADMapToData();
            }
        });
    }

    private void addADMapToData(){
        if(isRelateOver
                && adDataMap != null
                && !adDataMap.isEmpty()){
            adDataMap.put(TYPE_KEY,String.valueOf(Type_bigAd));
            for(int index = 0; index < allDataListMap.size();index ++){
                if(String.valueOf(Type_comment).equals(allDataListMap.get(index).get(TYPE_KEY))){
                    allDataListMap.add(index,adDataMap);
                    detailAdapter.notifyDataSetChanged();
                    return;
                }
            }
            allDataListMap.add(adDataMap);
            detailAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 刷新数据
     *
     * @param onlyUser 是否只刷新用户数据
     */
    private void refreshData(boolean onlyUser) {
        requestVideoData(onlyUser);
    }

    /** 重置数据 */
    private void resetData() {
        page = 0;
        isRelateOver = false;
        shareMap.clear();
        if (commentMap != null) commentMap.clear();
        allDataListMap.clear();
        if (detailAdapter != null) detailAdapter.notifyDataSetChanged();

    }

    private void requestVideoData(final boolean onlyUser) {
//        loadManager.showProgressBar();
        StringBuilder params = new StringBuilder().append("code=").append(code).append("&type=RAW");
        ReqEncyptInternet.in().doEncypt(StringManager.api_getVideoInfo, params.toString(), new InternetCallback(this) {

            @Override
            public void getPower(int flag, String url, Object obj) {
                Log.i("tzy","obj = " + obj);
                //权限检测
                if(permissionMap.isEmpty()){
                    if(TextUtils.isEmpty(lastPermission)){
                        lastPermission = (String) obj;
                    }else{
                        if(lastPermission.equals(obj.toString())){
                            contiunRefresh = false;
                            return;
                        }
                    }
                    permissionMap = StringManager.getFirstMap(obj);
                    Log.i("tzy","permissionMap = " + permissionMap.toString());
                    if(permissionMap.containsKey("page")){
                        Map<String,String> pagePermission = StringManager.getFirstMap(permissionMap.get("page"));
                        hasPagePermission = analyzePagePermissionData(pagePermission);
                        if(!hasPagePermission) return;
                    }
                    if(permissionMap.containsKey("detail")){
                        detailPermissionMap = StringManager.getFirstMap(permissionMap.get("detail"));
                    }
                }
            }

            @Override
            public void loaded(int flag, String url, Object object) {
                refreshLayout.refreshComplete();
                loadManager.hideProgressBar();
                if(!hasPagePermission || !contiunRefresh) {
                    loadManager.hideProgressBar();
                    return;
                }
                if (!onlyUser)
                    resetData();
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    analysVideoData(onlyUser, StringManager.getFirstMap(object),detailPermissionMap);
                } else
                    toastFaildRes(flag, true, object);
                if (!onlyUser)
                    requestRelateData(false);//请求

            }
        });
    }

    private void analysVideoData(boolean onlyUser, @NonNull final Map<String, String> mapVideo, Map<String, String> detailPermissionMap){
        if (mapVideo.isEmpty()) return;
        xhWebView.setVisibility(View.GONE);
        mCommentBar.setVisibility(View.VISIBLE);
        customerData = StringManager.getFirstMap(mapVideo.get("customer"));
        final String userCode = customerData.get("code");
        isAuthor = LoginManager.isLogin()
                && !TextUtils.isEmpty(LoginManager.userInfo.get("code"))
                && !TextUtils.isEmpty(userCode)
                && userCode.equals(LoginManager.userInfo.get("code"));
        mHaederLayout.setData(onlyUser,mapVideo,detailPermissionMap);

        if(!TextUtils.isEmpty(customerData.get("nickName"))){
            mTitle.setText(customerData.get("nickName"));
            mTitle.setVisibility(View.VISIBLE);
        }
        rightButton.setImageResource(isAuthor ? R.drawable.i_ad_more : R.drawable.z_z_topbar_ico_share);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAuthor) {
                    showBottomDialog();
                } else {
                    openShare();
                    statistics("分享", "");
                }
            }
        });
        rightButton.setVisibility(View.VISIBLE);

        commentNum = mapVideo.get("commentNumber");
        mCommentBar.setPraiseAPI(StringManager.api_likeVideo);
        mCommentBar.setData(mapVideo);

        mapVideo.remove("html");
        mapVideo.remove("content");
        mapVideo.remove("raw");
        //处理分享数据
        shareMap = StringManager.getFirstMap(mapVideo.get("share"));
        handlerShareData();

        detailAdapter.notifyDataSetChanged();
        listView.setVisibility(View.VISIBLE);

        Map<String,String> commonPermission = StringManager.getFirstMap(detailPermissionMap.get("video"));
        commonPermission = StringManager.getFirstMap(commonPermission.get("common"));
        final String url = commonPermission.get("url");
        if((commonPermission.isEmpty() || StringManager.getBooleanByEqualsValue(commonPermission,"isShow"))
                ){
            //正常
            mCommentBar.setVisibility(View.VISIBLE);
            dredgeVipLayout.setVisibility(View.GONE);
        }else{
            //有限制
            mCommentBar.setVisibility(View.GONE);
            String buttonStr = commonPermission.get("button");
            if(!TextUtils.isEmpty(buttonStr))
                dredgeVipImmediately.setText(buttonStr);
            dredgeVipLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!TextUtils.isEmpty(url))
                        AppCommon.openUrl(VideoDetailActivity.this,url,true);
                }
            });
        }
    }

    private void requestRelateData(boolean onlyUser) {
        String param = "code=" + code + "&page=" + ++page + "&pagesize=10";
        ReqEncyptInternet.in().doEncypt(StringManager.api_getVideoRelated, param, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object object) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> listMap = StringManager.getListMapByJson(object);
                    int size = listMap.size();
                    for (int i = 0; i < size; i++) {
                        Map<String, String> map = listMap.get(i);
                        map.put(TYPE_KEY, String.valueOf(Type_recommed));
                        map.put("isAd", "1");
                        List<Map<String, String>> styleDataList = StringManager.getListMapByJson(map.get("styleData"));
                        handlerStyleData(map, styleDataList);
                    }
                    analysRelateData(listMap);
                    mVideoAdContorler.handlerAdData(allDataListMap);
                    addADMapToData();
                    detailAdapter.notifyDataSetChanged();
//                    loadManager.changeMoreBtn(flag, 10, 0, 3, false);
//                    listView.removeFooterView(loadManager.getSingleLoadMore(listView));
                } else
                    toastFaildRes(flag, true, object);
                Map<String,String> commonPermission = StringManager.getFirstMap(detailPermissionMap.get("video"));
                commonPermission = StringManager.getFirstMap(commonPermission.get("common"));
                if((commonPermission.isEmpty() || StringManager.getBooleanByEqualsValue(commonPermission,"isShow"))
                        ){
                    requestForumData(false);//请求
                }
            }
        });
    }

    public boolean analyzePagePermissionData(Map<String,String> pagePermission){
        if(pagePermission.containsKey("url") && !TextUtils.isEmpty(pagePermission.get("url"))){
            String url = pagePermission.get("url");
            xhWebView.loadUrl(url);
            xhWebView.setVisibility(View.VISIBLE);
            return false;
        }
        xhWebView.setVisibility(View.GONE);
        return true;
    }

    /**
     * 处理styledata数据
     * @param map
     * @param styleDataList
     * @return
     */
    private Map<String, String> handlerStyleData(Map<String, String> map, List<Map<String, String>> styleDataList) {
        for (int index = 0; index < styleDataList.size(); index++) {
            Map<String, String> data = styleDataList.get(index);
            if ("1".equals(data.get("type"))) {
                map.put("img", data.get("url"));
                map.put("videoIconShow", "1");
                return map;
            }
        }
        for (int index = 0; index < styleDataList.size(); index++) {
            Map<String, String> data = styleDataList.get(index);
            if ("2".equals(data.get("type"))) {
                map.put("img", data.get("url"));
                map.put("videoIconShow", "2");
                return map;
            }
        }
        //特殊处理gif图时，img字段没有值的情况
        if (TextUtils.isEmpty(map.get("img"))) {
            for (int index = 0; index < styleDataList.size(); index++) {
                Map<String, String> data = styleDataList.get(index);
                if ("3".equals(data.get("type"))) {
                    map.put("img", data.get("url"));
                    map.put("videoIconShow", "1");
                    return map;
                }
            }
        }
        return map;
    }

    private boolean isRelateOver = false;
    /**
     * 解析推荐数据
     *
     * @param ArrayRelate
     */
    private void analysRelateData(@NonNull ArrayList<Map<String, String>> ArrayRelate) {
        if (ArrayRelate.isEmpty()) return;
        for (Map<String, String> map : ArrayRelate) {
            String clickAll = map.get("clickAll");
            map.put("clickAll", "0".equals(clickAll) ? "" : clickAll + "播放");
            String commentNumber = map.get("commentNumber");
            map.put("commentNumber", "0".equals(commentNumber) ? "" : commentNumber + "评论");
        }
        if (page == 1)
            ArrayRelate.get(0).put("showheader", "1");
        allDataListMap.addAll(ArrayRelate);
        isRelateOver = true;

        detailAdapter.notifyDataSetChanged();
    }

    /** 请求评论列表 */
    private void requestForumData(final boolean isRefresh) {
        String url = StringManager.api_forumList;
        String param = "from=1&type=" + TYPE_VIDEO + "&code=" + code;
        ReqEncyptInternet.in().doEncypt(url, param, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object object) {

                if (flag >= ReqInternet.REQ_OK_STRING) {
                    analysForumData(isRefresh, object);
                } else
                    toastFaildRes(flag, true, object);
            }
        });
    }

    private void analysForumData(boolean isRefresh, Object object) {
        if("0".equals(commentNum)) return;
        commentMap = StringManager.getFirstMap(object);
        commentMap.put(TYPE_KEY, String.valueOf(Type_comment));
        commentMap.put("data", object.toString());
        commentMap.put("commentNum", commentNum);
        if (isRefresh) {
            int commentCount = Integer.parseInt(commentNum);
            commentMap.put("commentNum", "" + ++commentCount);
        }
        if (commentMap != null && allDataListMap.indexOf(commentMap) < 0)
            allDataListMap.add(commentMap);
        Log.i("tzy", "index = " + allDataListMap.indexOf(commentMap));
        detailAdapter.notifyDataSetChanged();
    }

    private void showBottomDialog() {
        ToolsDevice.keyboardControl(false, this, mCommentBar);
        BottomDialog dialog = new BottomDialog(this);
        dialog.addButton("分享", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openShare();
                statistics("更多", "分享");
            }
        }).addButton("编辑", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoDetailActivity.this, VideoEditActivity.class);
                intent.putExtra("code", code);
                startActivity(intent);
                statistics("更多", "编辑");
                VideoDetailActivity.this.finish();
            }
        }).addButton("删除", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDeleteDialog();
            }
        });
        dialog.show();
    }

    private Bitmap shareImageBitmap = null;

    private void handlerShareData() {
        if (!TextUtils.isEmpty(shareMap.get("img"))) {
            shareMap.put("imgType", BarShare.IMG_TYPE_WEB);

        } else {
            shareMap.put("img", String.valueOf(R.drawable.umen_share_launch));
            shareMap.put("imgType", BarShare.IMG_TYPE_RES);
        }
    }

    private void openShare() {
        if (shareMap.isEmpty()) {
            Tools.showToast(this, "数据处理中，请稍候");
            return;
        }

        Intent intent = new Intent(this, UserHomeShare.class);
        intent.putExtra("tongjiId", isAuthor ? "a_my":"a_user");
        intent.putExtra("nickName", customerData.get("nickName"));
        intent.putExtra("imgUrl", shareMap.get("img"));
        intent.putExtra("code", customerData.get("code"));
        intent.putExtra("clickUrl", shareMap.get("url"));
        intent.putExtra("title", shareMap.get("title"));
        intent.putExtra("content", shareMap.get("content"));
        intent.putExtra("type", shareMap.get("imgType"));
        intent.putExtra("shareFrom", "视频详情");
//        intent.putExtra("isHasReport",!isAuthor); //自己的主页不现实举报
        startActivity(intent);
    }

    private void openDeleteDialog() {
        final XhDialog dialog = new XhDialog(this);
        dialog.setTitle("确定删除这个视频吗？")
                .setCanselButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                })
                .setSureButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        deleteThis();
                        statistics("更多", "删除");
                    }
                }).show();
    }

    private void deleteThis() {
        ReqEncyptInternet.in().doEncypt(StringManager.api_videoDel, "code=" + code,
                new InternetCallback(VideoDetailActivity.this) {
                    @Override
                    public void loaded(int flag, String url, Object obj) {
                        if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                            //自动关闭
                            VideoDetailActivity.this.finish();
                            if (FriendHome.isAlive) {
                                Intent broadIntent = new Intent();
                                broadIntent.setAction(UploadStateChangeBroadcasterReceiver.ACTION);
                                broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.DATA_TYPE, "1");
                                broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.ACTION_DEL, "2");
                                Main.allMain.sendBroadcast(broadIntent);
                            }
                        } else {
                            toastFaildRes(flag, true, obj);
                        }
                    }
                });
    }

    private void statistics(String twoLevel, String threeLevel) {
        XHClick.mapStat(this, "a_ShortVideoDetail", twoLevel, threeLevel);
    }
}
