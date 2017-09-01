package amodule.dish.view;

import android.app.Activity;
import android.graphics.Color;
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

import com.xianghatest.R;

import org.json.JSONObject;

import java.util.ArrayList;
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
import third.mall.widget.ScrollViewContainer;
import third.video.VideoPlayerController;

import static amodule.dish.activity.DetailDish.startTime;
import static amodule.dish.activity.DetailDish.tongjiId;
import static java.lang.System.currentTimeMillis;
import static xh.basic.tool.UtilString.getListMapByJson;

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

    private int wm_height;//屏幕高度
    private int adBarHeight = 0;//广告所用bar高度
    //广告所用bar高度;图片/视频高度
    private int statusBarHeight = 0,videoLayoutHeight;
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
    private String dishJson = "";
    private int titleHeight;//标题高度
    private boolean isLoadWebViewData=false;//是否webview加载过数据

    public DishActivityViewControlNew(Activity activity){
        this.mAct = activity;
        wm_height = activity.getWindowManager().getDefaultDisplay().getHeight();
    }

    public void init(String state, LoadManager loadManager, String code, DishViewCallBack callBack) {
        this.state = state;
        this.loadManager = loadManager;
        mDishCode = code;
        this.callBack = callBack;
        initView();
    }

    /**
     * view的初始化
     */
    private void initView(){
        Log.i("zyj","H5______initView::"+(System.currentTimeMillis()-startTime));
        titleHeight = Tools.getDimen(mAct,R.dimen.dp_45);
        initTitle();
        templateWebView = (TemplateWebView) mAct.findViewById(R.id.a_dish_detail_new_web);
        templateWebView.initBaseData(mAct,loadManager);
        templateWebView.setWebViewCallBack(new TemplateWebView.OnWebviewStateCallBack() {
            @Override
            public void onLoadFinish() {
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
        handlerDishWebviewData();
        //头部view处理
        dishHeaderView= (DishHeaderViewNew) mAct.findViewById(R.id.a_dish_detail_new_headview);
        videoLayoutHeight = ToolsDevice.getWindowPx(mAct).widthPixels * 9 / 16 + titleHeight + statusBarHeight;
        dishHeaderView.initView(mAct,videoLayoutHeight);

        mScrollView = (XhScrollView) mAct.findViewById(R.id.a_dish_detail_new_scrollview);
        setGlideViewListener();

        //处理标题导航栏
        dishTitleViewControl= new DishTitleViewControlNew(mAct, new DishTitleViewControlNew.OnDishTitleControlListener() {
            @Override
            public String getOffDishJson() {
                return dishJson;
            }
        });
        dishTitleViewControl.initView(mAct);
        dishTitleViewControl.setstate(state);
        //底部view
        mFootControl = new DishFootControl(mAct,mDishCode);
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
                                    if (videoLayoutHeight + y <= wm_height * 2 / 3) {
                                        mMoveLen = y;
                                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, videoLayoutHeight + y);
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
        Log.i("zyj","analyzeDishInfoData::"+(System.currentTimeMillis()-startTime));
        dishJson = dishInfo;
        ArrayList<Map<String, String>> list = StringManager.getListMapByJson(dishInfo);
        if(list.size() == 0) return;
        dishInfoMap = list.get(0);
        isHasVideo = "2".equals(dishInfoMap.get("type"));
        if(!isHasVideo){mTimer = new MyTimer(handler);}
        XHClick.track(mAct,isHasVideo?"浏览视频菜谱详情页":"浏览图文菜谱详情页");
        if(isHasVideo)tongjiId="a_menu_detail_video";
        dishTitleViewControl.setData(dishInfoMap,mDishCode,isHasVideo,dishInfoMap.get("dishState"),loadManager);

        Map<String, String> customer = StringManager.getFirstMap(dishInfoMap.get("customer"));
        if (customer != null&& !TextUtils.isEmpty(customer.get("code")) && LoginManager.userInfo != null
                && customer.get("code").equals(LoginManager.userInfo.get("code"))) {
            state = "";
            dishTitleViewControl.setstate(state);
        }
        if(customer != null&& !TextUtils.isEmpty(customer.get("code")) ){
        mFootControl.setAuthorCode(customer.get("code"));}
        dishTitleViewControl.setViewState();


        //头部view
        dishHeaderView.setData(list, new DishHeaderViewNew.DishHeaderVideoCallBack() {
            @Override
            public void videoImageOnClick() {
                String color = Tools.getColorStr(mAct, R.color.common_top_bg);
                bar_title_1.setBackgroundColor(Color.parseColor(color));
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
        },permissionMap);
        mFootControl.setDishInfo(dishInfoMap.get("name"));
        mScrollView.setVisibility(View.VISIBLE);
    }

    private boolean saveHistory = false;
    public void saveHistoryToDB(final String burden) {
        if (!saveHistory) {
            saveHistory = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = handlerJSONData(burden);
                    HistoryData data = new HistoryData();
                    data.setBrowseTime(currentTimeMillis());
                    data.setCode(mDishCode);
                    data.setDataJson(jsonObject.toString());
                    BrowseHistorySqlite sqlite = new BrowseHistorySqlite(XHApplication.in());
                    sqlite.insertSubject(BrowseHistorySqlite.TB_DISH_NAME, data);
                }
            }).start();
        }
    }
    private JSONObject handlerJSONData(String burdens) {
        JSONObject jsonObject = new JSONObject();
        try {
            ArrayList<Map<String, String>> dishInfoArray = getListMapByJson(dishJson);
            if (dishInfoArray.size() > 0) {
                Map<String, String> dishInfo = dishInfoArray.get(0);
                jsonObject.put("name", dishInfo.get("name"));
                jsonObject.put("img", dishInfo.get("img"));
                jsonObject.put("code", mDishCode);
                jsonObject.put("isFine", dishInfo.get("isFine"));
                jsonObject.put("favorites", dishInfo.get("favorites"));
                jsonObject.put("allClick", dishInfo.get("allClick"));
                jsonObject.put("exclusive", dishInfo.get("exclusive"));
                jsonObject.put("hasVideo", dishInfo.get("type"));
                jsonObject.put("isMakeImg", dishInfo.get("isMakeImg"));
                jsonObject.put("burdens", burdens);
            }
        } catch (Exception e) { }
        return jsonObject;
    }


    public void analyzeUserShowDishInfoData(String dishJson){
        mFootControl.initUserDish(dishJson);
    }

    public void analyzeDishLikeNumberInfoData(String dishJson){
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
            String color = Tools.getColorStr(mAct, R.color.common_top_bg);
            bar_title_1.setBackgroundColor(Color.parseColor(color));
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

    public void onDestroy() {
        if(dishHeaderView!=null)dishHeaderView.onDestroy();
    }

    public boolean onBackPressed(){
        return dishHeaderView.onBackPressed();
    }

    /**
     * 外部设置是否是视频
     * @param isHasVideo
     */
    public void setHasVideo(boolean isHasVideo){
        this.isHasVideo= isHasVideo;
    }

    public void reset(){
        dishTitleViewControl.reset();
        dishHeaderView.reset();
        setLoginStatus();
        if(pageXhWebView!=null)pageXhWebView.setVisibility(View.GONE);
    }

    public void setLoginStatus(){
        if (dishHeaderView != null)
            dishHeaderView.setLoginStatus();
    }

    /**
     * 处理dishWebView的数据
     */
    public void handlerDishWebviewData(){
        if(!isLoadWebViewData) {
            Log.i("zyj","H5______handlerDishWebviewData::"+(System.currentTimeMillis()-startTime));
            templateWebView.loadData(XHTemplateManager.XHDISHLAYOUT,XHTemplateManager.TEMPLATE_MATCHING.get(XHTemplateManager.XHDISHLAYOUT),new String[]{mDishCode});
            isLoadWebViewData = true;
        }
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
            pageXhWebView.setVisibility(View.VISIBLE);
            return false;
        }
        if(pageXhWebView!=null)pageXhWebView.setVisibility(View.GONE);
        return true;
    }

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
        if(mMoveLen<=0){mMoveLen=0;}
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, videoLayoutHeight+mMoveLen);
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
}
