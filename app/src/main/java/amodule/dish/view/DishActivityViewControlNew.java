package amodule.dish.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.xiangha.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.XHApplication;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import amodule.user.db.BrowseHistorySqlite;
import amodule.user.db.HistoryData;
import aplug.web.tools.WebviewManager;
import aplug.web.tools.XHTemplateManager;
import aplug.web.view.TemplateWebView;
import aplug.web.view.XHWebView;
import third.video.VideoPlayerController;

import static amodule.dish.activity.DetailDish.startTime;
import static amodule.dish.activity.DetailDish.tongjiId;
import static java.lang.System.currentTimeMillis;

/**
 * 菜谱界面的总控制类
 */
public class  DishActivityViewControlNew {
    private Activity mAct;
    private RelativeLayout bar_title_1;
    private TemplateWebView templateWebView;
    private XhScrollView mScrollView;
    private View view_oneImage;
    private RelativeLayout dishVidioLayout;
    private XHWebView pageXhWebView;//页面限制：例如显示一个开通会员页面
    private RelativeLayout dredgeVipFullLayout;

    private int wm_height;//屏幕高度
    private int adBarHeight = 0;//广告所用bar高度
    //广告所用bar高度;图片/视频高度
    private int statusBarHeight = 0,headerLayoutHeight;
    private int startY;

    private String state;
    private LoadManager loadManager;
    private String mDishCode;
    private boolean isHasVideoOnClick = false;
    private boolean isShowTitleColor=false;
    private boolean isHasVideo = false;
    private boolean isRecored = false;
    private int firstItemIndex = 0;

    private Map<String, String> dishInfoMap;//dishInfo数据

    private DishTitleViewControlNew dishTitleViewControl;
    private DishHeaderViewNew dishHeaderView;
    private DishFootControl mFootControl;

    private DishViewCallBack callBack;
    private int titleHeight;//标题高度
    private boolean isLoadWebViewData=false;//是否webview加载过数据
    private boolean isHeaderLoadCallBack=false;//是否执行header的callback

    private String courseCode,chapterCode;//

    public DishActivityViewControlNew(Activity activity){
        this.mAct = activity;
        wm_height = activity.getWindowManager().getDefaultDisplay().getHeight();
    }

    public void init(String state, LoadManager loadManager, DishViewCallBack callBack, TemplateWebView.OnTemplateCallBack onTemplateCallBack) {
        this.state = state;
        this.loadManager = loadManager;
        this.callBack = callBack;
        initView(onTemplateCallBack);
    }

    /**
     * 设置章节code和课程code
     * @param courseCode 课程
     * @param chapterCode 章节
     */
    public void setCode(String courseCode,String chapterCode){
        this.courseCode = courseCode;
        this.chapterCode = chapterCode;
    }
    /**
     * view的初始化
     */
    private void initView(TemplateWebView.OnTemplateCallBack onTemplateCallBack){
        Log.i("zyj","H5______initView::"+(System.currentTimeMillis()-startTime));
        titleHeight = Tools.getDimen(mAct,R.dimen.dp_45);
        initTitle();
        //头部view处理
        dishHeaderView= (DishHeaderViewNew) mAct.findViewById(R.id.a_dish_detail_new_headview);
        headerLayoutHeight = ToolsDevice.getWindowPx(mAct).widthPixels * 9 / 16 + titleHeight + statusBarHeight;
        dishHeaderView.initView(mAct,headerLayoutHeight);

        //处理标题导航栏
        dishTitleViewControl= new DishTitleViewControlNew(mAct);
        dishTitleViewControl.initView(mAct);
        dishTitleViewControl.setstate(state);

        templateWebView = (TemplateWebView) mAct.findViewById(R.id.a_dish_detail_new_web);
        templateWebView.initBaseData(mAct,loadManager);
        templateWebView.setOnTemplateCallBack(onTemplateCallBack);
        templateWebView.setWebViewCallBack(new TemplateWebView.OnWebviewStateCallBack() {
            @Override
            public void onLoadFinish() {
                Log.i(Main.TAG,"模版加载完成");
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mFootControl!=null)mFootControl.showFootView();
                    }
                },1*1000);
            }
            @Override
            public void onLoadStart() {
            }
        });
        //底部view
        mFootControl = new DishFootControl(mAct);

        mScrollView = (XhScrollView) mAct.findViewById(R.id.a_dish_detail_new_scrollview);
        setGlideViewListener();
        templateWebView.setVisibility(View.VISIBLE);

        dredgeVipFullLayout = (RelativeLayout) mAct.findViewById(R.id.dredge_vip_full_layout);
    }

    public void initData(String code){
        this.mDishCode=code;
        handlerDishWebviewData();
        mFootControl.initData(mDishCode);
    }
    /**
     * 处理标题
     */
    private void initTitle(){
        bar_title_1 = (RelativeLayout) mAct.findViewById(R.id.a_dish_detail_new_title);
        statusBarHeight = Tools.getStatusBarHeight(mAct);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bar_title_1.getLayoutParams();
        layoutParams.height = titleHeight + statusBarHeight;
        View title_state_bar = mAct.findViewById(R.id.title_state_bar);
        layoutParams = (RelativeLayout.LayoutParams) title_state_bar.getLayoutParams();
        layoutParams.height = statusBarHeight;
    }

    /**
     * 设置监听。
     */
    private void setGlideViewListener(){
        mScrollView.setOnScrollListener(new XhScrollView.OnScrollListener() {
            @Override
            public void onScroll(int scrollY) {
                setViewOneState();
                int[] location = new int[2];
                int view_height = 0;
                if (view_oneImage != null) {
                    view_oneImage.getLocationOnScreen(location);
                    view_height = view_oneImage.getHeight();
                }
                if (Math.abs(location[1]) < view_height + adBarHeight) {
                    firstItemIndex = 0;
                }else{
                    firstItemIndex = 1;
                }
                if(mFootControl!=null){
                    mFootControl.onSrollView();
                }
            }

            @Override
            public void scrollOritention(int scrollState) {
                if(scrollState==XhScrollView.SCROLL_DOWN){//向下滑动
                    bar_title_1.setVisibility(View.GONE);
                }else if(scrollState==XhScrollView.SCROLL_UP){//向上滑动
                    bar_title_1.setVisibility(View.VISIBLE);
                }
            }
        });
        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (firstItemIndex == 0 && !isHasVideo) {
                            if (!isRecored) {
                                startY = (int) event.getY();
                                isRecored = true;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!isHasVideo) {
                            int tempY = (int) event.getY();
                            if (!isRecored && firstItemIndex == 0) {// 如果首item索引为0，且尚未记录startY,则在拖动时记录之，并执行isRecored
                                isRecored = true;
                                startY = tempY;
                            } else if (firstItemIndex == 0) {
                                int y = tempY - startY;
                                if (wm_height > 0 && y > 0) {
                                    if (headerLayoutHeight + y <= wm_height * 2 / 3) {
                                        mMoveLen = y;
                                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, headerLayoutHeight + y);
                                        dishVidioLayout.setLayoutParams(layoutParams);
                                    }
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        Log.i("zyj","MotionEvent::ACTION_CANCEL");
                    case MotionEvent.ACTION_UP:
                        Log.i("zyj","MotionEvent::ACTION_UP");
                        if (!isHasVideo) {
                            isRecored = false;
                            startY = 0;
                            if(mTimer!=null){
                                mTimer.schedule(2);
                            }
                        }
                        if(null != mFootControl)
                            mFootControl.hindGoodLayout();
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 解析数据-----业务标示是“dishInfo”
     * @param dishInfo
     * @param permissionMap
     */
    public void analyzeDishInfoData(String dishInfo, Map<String, String> permissionMap) {
        Log.i("zyj","analyzeDishInfoData::" + (System.currentTimeMillis() - startTime));
        saveDishInfo(dishInfo);
        ArrayList<Map<String, String>> list = StringManager.getListMapByJson(dishInfo);
        if(list.size() == 0) return;
        dishInfoMap = list.get(0);
        isHasVideo = "2".equals(dishInfoMap.get("type"));
        if(!isHasVideo){
            mTimer = new MyTimer(handler);
            headerLayoutHeight=ToolsDevice.getWindowPx(mAct).widthPixels *5/6;
        }else{
            headerLayoutHeight=ToolsDevice.getWindowPx(mAct).widthPixels * 9 / 16 + titleHeight + statusBarHeight ;
        }
        XHClick.track(mAct,isHasVideo?"浏览视频菜谱详情页":"浏览图文菜谱详情页");
        if(isHasVideo)tongjiId="a_menu_detail_video";
        dishTitleViewControl.setData(dishInfoMap,mDishCode,isHasVideo,dishInfoMap.get("dishState"),loadManager);

        String authorCode = dishInfoMap.get("customerCode");
        if (!TextUtils.isEmpty(authorCode)){
            if(null != mFootControl)
                mFootControl.setAuthorCode(authorCode);
            if(LoginManager.userInfo != null
                    && authorCode.equals(LoginManager.userInfo.get("code"))) {
                state = "";
                dishTitleViewControl.setstate(state);
            }
        }

        dishTitleViewControl.setViewState();

        //头部view
        setDishHeaderViewCallBack();
        dishHeaderView.setData(list,permissionMap);
        mFootControl.setDishInfo(dishInfoMap.get("name"));
        mScrollView.setVisibility(View.VISIBLE);
    }

    private void setDishHeaderViewCallBack(){
        if(dishHeaderView==null)return;
        if(isHeaderLoadCallBack){
            return;
        }
        isHeaderLoadCallBack=true;
        dishHeaderView.setDishCallBack(new DishHeaderViewNew.DishHeaderVideoCallBack() {
            @Override
            public void videoImageOnClick() {
                bar_title_1.setBackgroundResource(R.color.common_top_bg);
                isHasVideoOnClick = true;
            }
            @Override
            public void getVideoControl(VideoPlayerController mVideoPlayerController, RelativeLayout dishVidioLayouts, View view_oneImages) {
                callBack.getVideoPlayerController(mVideoPlayerController);
                dishVidioLayout=dishVidioLayouts;
                view_oneImage=view_oneImages;
                setViewOneState();
                dishTitleViewControl.setVideoContrl(mVideoPlayerController);
            }
        });
    }
    public void setDishOneView(String img){
        if(!TextUtils.isEmpty(img)){
            setDishHeaderViewCallBack();
            dishHeaderView.setImg(img);
            mScrollView.setVisibility(View.VISIBLE);
        }
    }

    private boolean saveDishInfo = false;
    private boolean isSaveApiData = false;
    private boolean isSaveJsData = false;

    private Map<String, String> needSaveDishInfo = new HashMap<>();

    private void saveDishInfo(String dishJson){
        saveDishInfo = true;
        Map<String,String> dishMap = StringManager.getFirstMap(dishJson);
        needSaveDishInfo.put("name", dishMap.get("name"));
        needSaveDishInfo.put("img", dishMap.get("img"));
        needSaveDishInfo.put("code", dishMap.get("code"));
        needSaveDishInfo.put("hasVideo", dishMap.get("type"));
        Log.i("tzy","needSaveDishInfo = " + needSaveDishInfo.toString());
        saveHistoryToDB();
    }

    public void saveApiData(String dataStr){
        isSaveApiData = true;
        Map<String,String> apiDataMap = StringManager.getFirstMap(dataStr);
        needSaveDishInfo.put("isFine", apiDataMap.get("isFine"));
        needSaveDishInfo.put("isMakeImg", apiDataMap.get("isMakeImg"));
        needSaveDishInfo.put("isFav", dishTitleViewControl.isNowFav() ? "2" : "1");
//        dishTitleViewControl.setFavStatus(apiDataMap.get("isFav"));
        Log.i("tzy","needSaveDishInfo = " + needSaveDishInfo.toString());
        saveHistoryToDB();
    }

    public void savaJsAdata(String burdens,String allClick,String favorites,String nickName){
        isSaveJsData = true;
        needSaveDishInfo.put("burdens",burdens);
        needSaveDishInfo.put("allClick",allClick);
        needSaveDishInfo.put("nickName",nickName);
        needSaveDishInfo.put("favorites",favorites);
        Log.i("tzy","needSaveDishInfo = " + needSaveDishInfo.toString());
        if(dishTitleViewControl!=null){
            dishTitleViewControl.setNickName(nickName);
        }
        saveHistoryToDB();
    }

    /**保存数据到数据库*/
    private synchronized void saveHistoryToDB() {
        if (saveDishInfo && isSaveApiData && isSaveJsData) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HistoryData data = new HistoryData();
                    data.setBrowseTime(currentTimeMillis());
                    data.setCode(mDishCode);
                    data.setDataJson(StringManager.getJsonByMap(needSaveDishInfo).toString());
                    BrowseHistorySqlite sqlite = new BrowseHistorySqlite(XHApplication.in());
                    sqlite.insertSubject(BrowseHistorySqlite.TB_DISH_NAME, data);
                }
            }).start();
        }
    }

    public void analyzeUserShowDishInfoData(String dishJson){
        if(null != mFootControl)
            mFootControl.initUserDish(dishJson);
    }

    public void analyzeDishLikeNumberInfoData(String dishJson){
        if(null != mFootControl)
            mFootControl.initLikeState(dishJson);
    }

    /**
     * 设置当前标题颜色状态
     */
    private void setViewOneState() {
        int[] location = new int[2];
        int view_height = 0;
        if (view_oneImage != null) {
            view_oneImage.getLocationOnScreen(location);
            view_height = view_oneImage.getHeight();
        }
        if (Math.abs(location[1]) > view_height - adBarHeight) {
            bar_title_1.clearAnimation();
            //初始化view都为不透明
            if (isHasVideoOnClick) return;
            bar_title_1.setBackgroundResource(R.color.common_top_bg);
            if(!isShowTitleColor){
                AlphaAnimation alphaAnimation= new AlphaAnimation(0,1);
                alphaAnimation.setDuration(1000);
                alphaAnimation.setFillAfter(true);
                bar_title_1.startAnimation(alphaAnimation);
                isShowTitleColor=true;
            }
        } else {
            if (isHasVideoOnClick) return;
            //初始化view都为透明
            if(isShowTitleColor) {
                AlphaAnimation alphaAnimation= new AlphaAnimation(1,0);
                alphaAnimation.setDuration(200);
                alphaAnimation.setFillAfter(true);
                bar_title_1.startAnimation(alphaAnimation);
                isShowTitleColor = false;
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        bar_title_1.setBackgroundResource(R.drawable.bg_dish_title);
                        AlphaAnimation alphaAnimation= new AlphaAnimation(0,1);
                        alphaAnimation.setDuration(500);
                        alphaAnimation.setFillAfter(true);
                        bar_title_1.startAnimation(alphaAnimation);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
            else bar_title_1.setBackgroundResource(R.drawable.bg_dish_title);
        }
    }

    public void onResume() {
        if(dishHeaderView!=null)
            dishHeaderView.onResume();
    }
    public void onPause() {
        if(dishHeaderView!=null)dishHeaderView.onPause();
    }

    public boolean onBackPressed(){
        return dishHeaderView.onBackPressed();
    }

    /**
     * 外部设置是否是视频
     * @param isHasVideo 是否是视频
     */
    public void setHasVideo(boolean isHasVideo){
        this.isHasVideo= isHasVideo;
    }

    public void reset(){
        dishTitleViewControl.reset();
        dishHeaderView.reset();
        setLoginStatus();
        if(pageXhWebView!=null)
            pageXhWebView.setVisibility(View.GONE);
    }

    public void setLoginStatus(){
        if (dishHeaderView != null)
            dishHeaderView.setLoginStatus();
    }

    /**
     * 处理dishWebView的数据
     */
    public void handlerDishWebviewData(){
//        if(!isLoadWebViewData) {
            Log.i("wyl","24");
            Log.i("zyj","H5______handlerDishWebviewData::"+(System.currentTimeMillis()-startTime));
            String[] temp= XHTemplateManager.TEMPLATE_MATCHING.get(XHTemplateManager.XHDISHLAYOUT);
            ArrayList<String> strLists= new ArrayList<>();
            for(String str:temp){
                strLists.add(str);
            }
            strLists.add("<{courseCode}>");
            strLists.add("<{chapterCode}>");

            String[] array=new String[strLists.size()];
            for(int i=0;i<strLists.size();i++){
                array[i]=(String)strLists.get(i);
            }
            templateWebView.loadData(XHTemplateManager.XHDISHLAYOUT,array,new String[]{mDishCode,courseCode,chapterCode
            });
//            isLoadWebViewData = true;
//        }
    }
    /**
     * 页面限制：显示h5页面，例如：显示一个开通会员页面
     * @param pagePermission
     * @return
     */
    public boolean analyzePagePermissionData(Map<String,String> pagePermission){
        if(pagePermission.containsKey("url") && !TextUtils.isEmpty(pagePermission.get("url"))){
            //xhwebView
            WebviewManager manager = new WebviewManager(mAct,loadManager,true);
            pageXhWebView = manager.createWebView(R.id.XHWebview);
            String url = pagePermission.get("url");
            pageXhWebView.loadUrl(url);
            RelativeLayout bar_title_2 = (RelativeLayout) dredgeVipFullLayout.findViewById(R.id.dish_title_page);
            bar_title_2.findViewById(R.id.back).setOnClickListener(backClickListener);
            bar_title_2.findViewById(R.id.leftClose).setOnClickListener(backClickListener);
            bar_title_2.findViewById(R.id.leftClose).setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bar_title_2.getLayoutParams();
            statusBarHeight = Tools.getStatusBarHeight(mAct);
            layoutParams.height = titleHeight + statusBarHeight;
            View title_state_bar_page = mAct.findViewById(R.id.title_state_bar_page);
            layoutParams = (RelativeLayout.LayoutParams) title_state_bar_page.getLayoutParams();
            layoutParams.height = statusBarHeight;
            dredgeVipFullLayout.setVisibility(View.VISIBLE);
            return false;
        }
        if(dredgeVipFullLayout!=null)
            dredgeVipFullLayout.setVisibility(View.GONE);
        return true;
    }

    private View.OnClickListener backClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.back:
                    XHClick.mapStat(mAct, tongjiId, "顶部导航栏", "返回点击量");
                    mAct.finish();
                    break;
                case R.id.leftClose:
                    XHClick.mapStat(mAct, tongjiId, "顶部导航栏", "关闭点击量");
                    Main.colse_level = 1;
                    mAct.finish();
                    break;
            }
        }
    };

    /**
     * 获取头部控件
     * @return
     */
    public DishTitleViewControlNew getDishTitleViewControl(){
        return dishTitleViewControl;
    }

    /**
     * 获取第一页集合
     * @return
     */
    public Map<String, String> getDishInfoMap (){
        return  dishInfoMap;
    }

    /**
     * 外部设置view 高度
     * @param adBarHeight
     */
    public void setAdBarHeight( int adBarHeight){
        this.adBarHeight = adBarHeight;
    }
    public interface DishViewCallBack{
        void getVideoPlayerController(VideoPlayerController mVideoPlayerController);

    }

    private String getMapToJson(Map<String,String> map){
        if(map==null||map.size()>0)return "";
        String data="";
        try{
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
            data= jsonObject.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 刷新数据webview关注
     */
    public void refreshTemplateWebView(){
        if(templateWebView!=null) {
            templateWebView.refreshWebviewMethod("javascript:freshFollow()");
        }
    }

    /**
     * 刷新webview的qa部分
     */
    public void refreshQaWebView(){
        if(templateWebView!=null){
            templateWebView.refreshWebviewMethod("javascript:freshQaList()");
        }
    }

    /**
     * 刷线数据状态
     */
    public void refreshAskStatus(){
        if(mFootControl!=null){
            mFootControl.handlerAskStatus();
        }
    }
    private int mMoveLen = 0;
    private MyTimer mTimer;
    private int tempHeight = -1;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mMoveLen!=0){
                mMoveLen= mMoveLen-3;
            }else{
                mTimer.cancel();
            }
            requestLayout();
        }
    };

    /**
     * 刷新布局
     */
    private void requestLayout(){
        if(dishVidioLayout==null){
            return;
        }
        if(mMoveLen<=0){mMoveLen=0;}
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, headerLayoutHeight+mMoveLen);
        dishVidioLayout.setLayoutParams(layoutParams);
    }
    class MyTimer {
        private Handler handler;
        private Timer timer;
        private MyTimer.MyTask mTask;

        public MyTimer(Handler handler) {
            this.handler = handler;
            timer = new Timer();
        }

        public void schedule(long period) {
            if (mTask != null) {
                mTask.cancel();
                mTask = null;
            }
            mTask = new MyTimer.MyTask(handler);
            timer.schedule(mTask, 0, period);
        }

        public void cancel() {
            if (mTask != null) {
                mTask.cancel();
                mTask = null;
            }
        }

        class MyTask extends TimerTask {
            private Handler handler;

            public MyTask(Handler handler) {
                this.handler = handler;
            }

            @Override
            public void run() {
                handler.obtainMessage().sendToTarget();
            }

        }
    }
    /**
     * 页面销毁时调用
     */
    public void onDestroy(){
        if(dishTitleViewControl!=null){
            dishTitleViewControl.onDestroy();
            dishTitleViewControl=null;
        }
        if(dishHeaderView!=null){
            dishHeaderView.onDestroy();
            dishHeaderView=null;
        }
        if(mFootControl!=null){
            mFootControl.onDestroy();
            mFootControl=null;
        }
        System.gc();
    }

    public boolean isHasVideo() {
        return isHasVideo;
    }
}
