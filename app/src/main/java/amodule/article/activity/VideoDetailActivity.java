package amodule.article.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
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
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.LogManager;
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
import third.share.BarShare;
import third.share.activity.ShareActivityDialog;
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

public class VideoDetailActivity extends BaseAppCompatActivity {

    private TextView mTitle;
    private ImageView rightButton;
    private RelativeLayout dredgeVipLayout;
    private RelativeLayout allTitleRelaPort;
    private TextView dredgeVipImmediately;
//    private PtrClassicFrameLayout refreshLayout;
    private ListView listView;
    /** 头部view */
    private VideoAllHeaderView mHaederLayout;
    private CommentBar mCommentBar;
    private XHWebView xhWebView;

    private VideoDetailAdapter detailAdapter;
    private VideoAdContorler mVideoAdContorler;
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
    private boolean isOnce = true;

    private String data_type = "";//推荐列表过来的数据
    private String module_type = "";
    private Long startTime;//统计使用的时间
    public boolean isPortrait = false;
    int statusBarH = 0;

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
    public void onBackPressed() {
        if(mHaederLayout != null && mHaederLayout.onBackPressed()){
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (loadManager != null)
            loadManager.hideProgressBar();
        page = 0;
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
        if (mHaederLayout != null) {
            mHaederLayout.onResume();
        }
        Glide.with(this).resumeRequests();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHaederLayout != null)
            mHaederLayout.onPause();
        Glide.with(this).pauseRequests();
        ToolsDevice.keyboardControl(false,this,mCommentBar);
    }

    @Override
    protected void onDestroy() {
        long nowTime = System.currentTimeMillis();
        if (startTime > 0 && (nowTime - startTime) > 0 && !TextUtils.isEmpty(data_type) && !TextUtils.isEmpty(module_type)) {
            XHClick.saveStatictisFile("VideoDetail", module_type, data_type, code, "", "stop", String.valueOf((nowTime - startTime) / 1000), "", "", "", "");
        }
        if (mHaederLayout != null)
            mHaederLayout.onDestroy();
        super.onDestroy();
    }

    private void initBundle() {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            code = bundle.getString("code");
            data_type = bundle.getString("data_type");
            module_type = bundle.getString("module_type");
        }
        startTime = System.currentTimeMillis();
    }

    private void initView() {
        initActivity("", 2, 0, 0, R.layout.a_video_detail);
        //处理状态栏引发的问题
        initStatusBar();
        //初始化title
        RelativeLayout allTitleRela = (RelativeLayout) findViewById(R.id.relativeLayout_global);
        allTitleRelaPort = (RelativeLayout) findViewById(R.id.all_title_rela_transparent);
        initTitle(allTitleRela);
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
        statusBarH = Tools.getStatusBarHeight(this);
        if (Tools.isShowTitle()) {
            final RelativeLayout bottomBarLayout = (RelativeLayout) findViewById(R.id.edit_controler_layout);
            //设置layout监听，处理键盘弹出的高度问题
            rl.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            int heightDiff = rl.getRootView().getHeight() - rl.getHeight() + (isPortrait ? statusBarH : 0);
                            Rect r = new Rect();
                            rl.getWindowVisibleDisplayFrame(r);
                            int screenHeight = rl.getRootView().getHeight();
                            int heightDifference = screenHeight - (r.bottom - r.top);
                            isKeyboradShow = heightDifference > 200;
                            heightDifference = isKeyboradShow ? heightDifference - heightDiff  : 0;
                            bottomBarLayout.setPadding(0, 0, 0, heightDifference);
                        }
                    });
            bottomBarLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (TextUtils.isEmpty(mCommentBar.getEditText().getText().toString()))
                        mCommentBar.setEditTextShow(false);
                    ToolsDevice.keyboardControl(false, VideoDetailActivity.this, mCommentBar.getEditText());
                    return false;
                }
            });
        }
    }

    /** 初始化title */
    private void initTitle(View view) {
        View leftClose = view.findViewById(R.id.leftClose);
        leftClose.setVisibility(View.VISIBLE);
        leftClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Main.colse_level = 1;
                VideoDetailActivity.this.finish();
            }
        });
        mTitle = (TextView) view.findViewById(R.id.title);
        int dp85 = Tools.getDimen(this,R.dimen.dp_85);
        mTitle.setPadding(dp85,0,dp85,0);
        rightButton = (ImageView) view.findViewById(R.id.rightImgBtn2);
        ImageView leftImage = (ImageView) view.findViewById(R.id.leftImgBtn);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) leftImage.getLayoutParams();
        layoutParams.setMargins(Tools.getDimen(this, R.dimen.dp_15), 0, 0, 0);
        leftImage.setLayoutParams(layoutParams);
//        RelativeLayout titleBar = (RelativeLayout) view.findViewById(R.id.relativeLayout_global);
//        titleBar.setBackgroundColor(Color.parseColor("#00FFFFFE"));

        view.findViewById(R.id.back).setOnClickListener(
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
//        refreshLayout = (PtrClassicFrameLayout) findViewById(R.id.refresh_list_view_frame);
//        refreshLayout.setHorizontalFadingEdgeEnabled(true);
        listView = (ListView) findViewById(R.id.listview);
        //initListView
        mHaederLayout = new VideoAllHeaderView(this);
        mHaederLayout.setType(TYPE_VIDEO);
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
                            //刷新头部评论
                            mHaederLayout.setupCommentNum(commentNum);
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
//        refreshLayout.setPtrHandler(new PtrDefaultHandler() {
//            @Override
//            public void onRefreshBegin(PtrFrameLayout frame) {
//                refreshData(false);
//            }
//        });
        listView.setAdapter(detailAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            int dp45 = Tools.getDimen(VideoDetailActivity.this,R.dimen.dp_45);
            int screenH = ToolsDevice.getWindowPx(VideoDetailActivity.this).heightPixels;
            String comTopBgColor = getResources().getString(R.color.common_top_bg);
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
                    int mixHeight = (isPortrait ? 0 : statusBarH) + dp45;
                    //设置Vip框是否显示
                    if(mHaederLayout.getVideoHeaderView().getLimitTime() > 0){
                        boolean isVipShow = viewBottom <= mixHeight;
                        dredgeVipLayout.setVisibility(isVipShow?View.VISIBLE:View.GONE);
                    }
                    //设置评论框是否显示
                    if(isPortrait && mHaederLayout.getVideoHeaderView().getLimitTime() <= 0){
                        boolean isCommentShow = screenH - viewBottom > mCommentBar.getHeight()
                                && dredgeVipLayout.getVisibility() == View.GONE;
                        mCommentBar.setVisibility(isCommentShow?View.VISIBLE:View.GONE);
                    }
                    //
                    allTitleRelaPort.setBackgroundColor(Color.parseColor(isPortrait && viewBottom <= dp45 + statusBarH ? comTopBgColor : "#00000000"));
                }
            }
        });
        View view = new View(this);
        view.setMinimumHeight(Tools.getDimen(this, R.dimen.dp_43));
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
        String params = new StringBuilder().append("code=").append(code).append("&type=RAW").toString();
        ReqEncyptInternet.in().doEncypt(StringManager.api_getVideoInfo, params, new InternetCallback(this) {

            @Override
            public void getPower(int flag, String url, Object obj) {
//                refreshLayout.refreshComplete();
                loadManager.hideProgressBar();
                //权限检测
                if(permissionMap.isEmpty()
                        && !TextUtils.isEmpty((String)obj) && !"[]".equals(obj)
                        && page == 0){
                    if(TextUtils.isEmpty(lastPermission)){
                        lastPermission = (String) obj;
                    }else{
                        contiunRefresh = !lastPermission.equals(obj.toString());
                        if(contiunRefresh)
                            lastPermission = obj.toString();
                    }
                    permissionMap = StringManager.getFirstMap(obj);
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
                //没有数据直接退出
                if(TextUtils.isEmpty((String) object) && isOnce){
                    VideoDetailActivity.this.finish();
                    return;
                }
                //加载过一次后
                isOnce = false;
                //无论如何都刷新用户数据
                if (!onlyUser)
                    mHaederLayout.setUserData(StringManager.getFirstMap(object));
                //判断权限是否更新
                if(!hasPagePermission || !contiunRefresh) {
                    loadManager.hideProgressBar();
                    return;
                }
                if (!onlyUser)
                    resetData();
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    analysVideoData(onlyUser, StringManager.getFirstMap(object),detailPermissionMap);
                }
                if (!onlyUser)
                    requestRelateData(false);//请求

            }
        });
    }

    private void analysVideoData(boolean onlyUser, @NonNull final Map<String, String> mapVideo, Map<String, String> detailPermissionMap){
        if (mapVideo.isEmpty()) return;

        //判断是否是长视频
        isPortrait = isPortraitVideo(mapVideo);
        if(isPortrait){
            handlerPortrait();
        }

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
        //获取是否可以编辑
        boolean hasEditPermission = "2".equals(mapVideo.get("isEdit"));
        //作者 或者 有编辑权限
        final boolean canEdit = isAuthor || hasEditPermission;
        rightButton.setImageResource(canEdit ? R.drawable.i_ad_more : R.drawable.z_z_topbar_ico_share);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canEdit) {
                    showBottomDialog(isAuthor);
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
            String buttonStr = commonPermission.get("button2");
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

    /**
     * 判断视屏长宽问题
     * @param data
     * @return true：竖   false：横
     */
    private boolean isPortraitVideo(Map<String, String> data){
        try{
            Map<String, String> videoData = StringManager.getFirstMap(data.get("video"));
            if(videoData.containsKey("width") && !TextUtils.isEmpty(videoData.get("width"))
                    && videoData.containsKey("height") && !TextUtils.isEmpty(videoData.get("height"))){
                float videoW = Integer.parseInt(videoData.get("width"));
                float videoH = Integer.parseInt(videoData.get("height"));
                //视频比例大于3：4则为竖屏视频
                return VideoPlayerController.isPortraitVideo(videoW,videoH);
            }
        }catch (Exception e){
            //数据异常
            LogManager.print("e","Exception::" + e.getMessage());
            return false;
        }
        return false;
    }

    private void handlerPortrait(){
        findViewById(R.id.relativeLayout_global).setVisibility(View.GONE);
        allTitleRelaPort.setVisibility(View.VISIBLE);
        allTitleRelaPort.setBackgroundColor(Color.parseColor("#00000000"));
        View view = findViewById(R.id.relativeLayout_global_transparent);
        view.setBackgroundColor(Color.parseColor("#00FFFFFE"));
        initTitle(allTitleRelaPort);
        if(Tools.isShowTitle()){
            Window window = this.getWindow();
            //设置Window为全透明
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
            //获取父布局
            View mContentChild = mContentView.getChildAt(0);
            //不预留系统栏位置
            if (mContentChild != null) {
                ViewCompat.setFitsSystemWindows(mContentChild, false);
            }
            int height = Tools.getDimen(this, R.dimen.dp_46) + statusBarH;
            RelativeLayout all_title = (RelativeLayout) findViewById(R.id.all_title_rela_transparent);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
            all_title.setLayoutParams(layout);
            all_title.setPadding(0, statusBarH, 0, 0);
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
                }
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
     * @param map map数据
     * @param styleDataList 图片的styleData
     * @return 返回处理后的map
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
     * @param ArrayRelate 相关推荐数据
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
                }
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
        detailAdapter.notifyDataSetChanged();
    }

    private void showBottomDialog(boolean isAuthor) {
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
        });
        //是作者显示删除按钮
        if(isAuthor){
            dialog.addButton("删除", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDeleteDialog();
                }
            });
        }
        dialog.show();
    }

    private Bitmap shareImageBitmap = null;

    private void handlerShareData() {
        if (!TextUtils.isEmpty(shareMap.get("img"))) {
            shareMap.put("imgType", BarShare.IMG_TYPE_WEB);

        } else {
            shareMap.put("img", String.valueOf(R.drawable.share_launcher));
            shareMap.put("imgType", BarShare.IMG_TYPE_RES);
        }
    }

    private void openShare() {
        if (shareMap.isEmpty()) {
            Tools.showToast(this, "数据处理中，请稍候");
            return;
        }

        Intent intent = new Intent(this, ShareActivityDialog.class);
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
                }).setSureButtonTextColor("#333333")
                .setCancelButtonTextColor("#333333")
                .show();
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
                        }
                    }
                });
    }

    private void statistics(String twoLevel, String threeLevel) {
        XHClick.mapStat(this, "a_ShortVideoDetail", twoLevel, threeLevel);
    }
}
