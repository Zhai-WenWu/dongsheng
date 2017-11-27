package amodule.dish.view.manager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import acore.logic.load.LoadManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.view.DishADBannerView;
import amodule.dish.view.DishAboutView;
import amodule.dish.view.DishExplainView;
import amodule.dish.view.DishHeaderViewNew;
import amodule.dish.view.DishHoverViewControl;
import amodule.dish.view.DishIngreDataShow;
import amodule.dish.view.DishModuleScrollView;
import amodule.dish.view.DishQAView;
import amodule.dish.view.DishRecommedAndAdView;
import amodule.dish.view.DishTitleViewControl;
import amodule.dish.view.DishVipView;
import amodule.dish.view.XhScrollView;
import third.video.VideoPlayerController;

/**
 * 当前只处理View的拼装
 * 不能牵扯如何业务逻辑处理----因为当前页面业务确定，采用直接数据指向方法（不抽象不模糊）
 */
public class DetailDishViewManager {
    private RelativeLayout bar_title_1,dishVidioLayout;
    public static int showNumLookImage = 0;//点击展示次数
    private boolean isHasVideoOnClick = false;
    private boolean isShowTitleColor=false;
    private View view_oneImage;
    private ListView listView;
    private int firstItemIndex,startY;
    private boolean isHasVideo=false,isRecored=false;
    private int wm_height;//屏幕高度

    public DishTitleViewControl dishTitleViewControl;
    public DishHoverViewControl dishHoverViewControl;
    public LinearLayout layoutHeader;
    public LinearLayout layoutFooter;
    public Activity mAct;
    private TextView textStep;
    //广告所用bar高度;图片/视频高度
    private int statusBarHeight = 0, headerLayoutHeight;
    private int titleHeight;//标题高度
    //头部信息
    public DishHeaderViewNew dishHeaderViewNew;
    public DishVipView dishVipView;
    public DishAboutView dishAboutView;
    public DishIngreDataShow dishIngreDataShow;
    public DishRecommedAndAdView dishRecommedAndAdView;
    public DishExplainView dishExplainView;
    public DishADBannerView dishADBannerView;
    public DishQAView dishQAView;
    public DishModuleScrollView dishModuleScrollView;


    /**
     * 对view进行基础初始化
     */
    public DetailDishViewManager(Activity activity, ListView listView,String state) {
        wm_height = activity.getWindowManager().getDefaultDisplay().getHeight();
        mAct = activity;
        this.listView = listView;
        titleHeight = Tools.getDimen(mAct,R.dimen.dp_45);
        dishTitleViewControl = new DishTitleViewControl(activity);
        dishTitleViewControl.initView(activity);
        dishTitleViewControl.setstate(state);
        initTitle();

        dishHoverViewControl = new DishHoverViewControl(activity);
        dishHoverViewControl.initView();

        if (layoutHeader == null) {
            layoutHeader = new LinearLayout(activity);
            layoutHeader.setOrientation(LinearLayout.VERTICAL);
            layoutFooter = new LinearLayout(activity);
            layoutFooter.setOrientation(LinearLayout.VERTICAL);
        }
        titleHeight = Tools.getDimen(mAct, R.dimen.dp_45);
        statusBarHeight = Tools.getStatusBarHeight(mAct);
        headerLayoutHeight = ToolsDevice.getWindowPx(mAct).widthPixels * 9 / 16 + titleHeight + statusBarHeight;
        //图片视频信息
        dishHeaderViewNew = new DishHeaderViewNew(mAct);
        dishHeaderViewNew.initView(mAct, headerLayoutHeight);
        dishVipView = new DishVipView(mAct);
        dishVipView.setVisibility(View.GONE);
        //用户信息和菜谱基础信息
        dishAboutView= new DishAboutView(mAct);
        dishAboutView.setVisibility(View.GONE);
        //用料
        dishIngreDataShow= new DishIngreDataShow(mAct);
        dishIngreDataShow.setVisibility(View.GONE);
        //banner
        dishADBannerView= new DishADBannerView(mAct);
        dishADBannerView.setVisibility(View.GONE);
        //小技巧
        dishModuleScrollView= new DishModuleScrollView(mAct);
        dishModuleScrollView.setVisibility(View.GONE);
        layoutHeader.addView(dishVipView);
        layoutHeader.addView(dishAboutView);
        layoutHeader.addView(dishADBannerView);
        layoutHeader.addView(dishIngreDataShow);
        layoutHeader.addView(dishModuleScrollView);
        textStep = new TextView(activity);
        textStep.setPadding(Tools.getDimen(activity, R.dimen.dp_20), Tools.getDimen(activity, R.dimen.dp_20), 0, 0);
        textStep.setTextSize(Tools.getDimenSp(activity, R.dimen.sp_18));
        textStep.setTextColor(Color.parseColor("#333333"));
        TextPaint tp = textStep.getPaint();
        tp.setFakeBoldText(true);
        textStep.setText("做法");
        textStep.setVisibility(View.GONE);
        layoutHeader.addView(textStep);
        dishQAView = new DishQAView(mAct);
        dishQAView.setVisibility(View.GONE);
        //foot
        dishExplainView = new DishExplainView(mAct);
        dishExplainView.setVisibility(View.GONE);
        dishRecommedAndAdView= new DishRecommedAndAdView(mAct);
        dishRecommedAndAdView.setVisibility(View.GONE);

        layoutFooter.addView(dishExplainView);
        layoutFooter.addView(dishQAView);
        layoutFooter.addView(dishRecommedAndAdView);
        listView.addHeaderView(dishHeaderViewNew);
        listView.addHeaderView(layoutHeader);
        listView.addFooterView(layoutFooter);
        listView.setVisibility(View.VISIBLE);
        setListViewListener();
    }

    /**
     * 处理标题
     */
    private void initTitle(){
        bar_title_1 = (RelativeLayout) mAct.findViewById(R.id.a_dish_detail_new_title);
//        statusBarHeight = Tools.getStatusBarHeight(mAct);
//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bar_title_1.getLayoutParams();
//        layoutParams.height = titleHeight + statusBarHeight;
//        View title_state_bar = mAct.findViewById(R.id.title_state_bar);
//        layoutParams = (RelativeLayout.LayoutParams) title_state_bar.getLayoutParams();
//        layoutParams.height = statusBarHeight;
    }
    /**
     * 处理预先加载数据
     */
    public void initBeforeData(String img){
        if (dishHeaderViewNew != null&& !TextUtils.isEmpty(img))dishHeaderViewNew.setImg(img);
    }
    public void handlerRelationData(ArrayList<Map<String,String>> list){

    }
    /**
     * 处理标题信息数据
     */
    public void handlerTitle(Map<String, String> dishInfoMaps,String code,boolean isHasVideo,String dishState,LoadManager loadManager,String state) {
        if(dishTitleViewControl!=null){
            dishTitleViewControl.setData(dishInfoMaps,code,isHasVideo,dishState,loadManager);
            dishTitleViewControl.setstate(state);
            dishTitleViewControl.setViewState();
        }
    }
    public void handlerTitleName(String name){
        if(dishTitleViewControl!=null){
            dishTitleViewControl.setNickName(name);
        }
    }
    /**
     * 处理header图片，和视频数据
     */
    public void handlerHeaderView(ArrayList<Map<String, String>> list, Map<String, String> permissionMap) {
        if("2".equals(list.get(0).get("type"))){
            isHasVideo=true;
        }
        if(!isHasVideo){
            mTimer = new MyTimer(handler);
            headerLayoutHeight=ToolsDevice.getWindowPx(mAct).widthPixels *5/6;
        }else{
            headerLayoutHeight=ToolsDevice.getWindowPx(mAct).widthPixels * 9 / 16 + titleHeight + statusBarHeight ;
        }
        if (dishHeaderViewNew != null) {
            dishHeaderViewNew.setDistance(0);
            dishHeaderViewNew.setDishCallBack(new DishHeaderViewNew.DishHeaderVideoCallBack() {
                @Override
                public void videoImageOnClick() {
                    Log.i("wyl","videoImageOnClick");
                    bar_title_1.setBackgroundResource(R.color.common_top_bg);
                }
                @Override
                public void getVideoControl(VideoPlayerController mVideoPlayerController, RelativeLayout dishVidioLayouts, View view_oneImage) {
                    Log.i("wyl","getVideoControl");
                    dishVidioLayout=dishVidioLayouts;
                    DetailDishViewManager.this.view_oneImage= view_oneImage;
                    setViewOneState();
                }
            });
            dishHeaderViewNew.setData(list, permissionMap);
            dishVidioLayout=dishHeaderViewNew.getViewLayout();
        }
    }

    /**
     * 处理菜谱基本信息
     */
    public void handlerDishData(ArrayList<Map<String, String>> list) {
        if(dishAboutView!=null) {
            dishAboutView.setVisibility(View.VISIBLE);
            dishAboutView.setData(list.get(0), mAct);
            //设置标题用户名：
            if(list.get(0).containsKey("customer")){
                Map<String,String> map = StringManager.getFirstMap(list.get(0).get("customer"));
                handlerQAUserView(map);
                if(map.containsKey("nickName")&&!TextUtils.isEmpty(map.get("nickName")))handlerTitleName(map.get("nickName"));
            }
        }
    }
    /**
     * 处理用户权限信息
     */
    public void handlerUserPowerData(Map<String,String> map) {
        if(dishAboutView!=null) {
            dishAboutView.setUserPowerData(map);
        }
    }
    /**
     * 处理vip按钮
     * @param map
     */
    public void handlerVipView(Map<String,String> map){
        if(dishVipView != null){
            dishVipView.setVisibility(View.VISIBLE);
            dishVipView.setData(map);
        }
    }
    /**
     * 处理用料
     */
    public void handlerIngreView(ArrayList<Map<String, String>> list) {
        if(dishIngreDataShow!=null) {
            dishIngreDataShow.setVisibility(View.VISIBLE);
            dishIngreDataShow.setData(list);
        }
    }
    /**
     * 处理广告信息
     */
    public void handlerBannerView(ArrayList<Map<String, String>> list) {
            if(dishADBannerView!=null&& list!=null && list.size()>0&&!TextUtils.isEmpty(list.get(0).get("img"))){
                dishADBannerView.setVisibility(View.VISIBLE);
                dishADBannerView.setData(list.get(0));
            }
    }
    /**
     * 处理小技巧view
     */
    public void handlerSkillView(ArrayList<Map<String, String>> list) {
        if(dishModuleScrollView!=null&&"2".equals(list.get(0).get("isShow"))){
            dishModuleScrollView.setVisibility(View.VISIBLE);
            if(list.get(0).containsKey("list"))dishModuleScrollView.setData(StringManager.getListMapByJson(list.get(0).get("list")));
        }
    }
    /**
     * 处理步骤相关view
     */
    public void handlerStepView(ArrayList<Map<String,String>> list){
        if(list!=null&&list.size()>0&&"1".equals(list.get(0).get("isShow"))) {
            textStep.setText(list.get(0).get("promptMsg"));
        }
        textStep.setVisibility(View.VISIBLE);
    }
    /**
     * 处理小贴士信息
     */
    public void handlerExplainView(ArrayList<Map<String, String>> list) {
        if(dishExplainView!=null){
            dishExplainView.setVisibility(View.VISIBLE);
            dishExplainView.setData(list.get(0));
        }
    }

    /**
     * 处理用户信息问答
     */
    private void handlerQAUserView(Map<String,String> maps){
        if(dishQAView!=null){
            dishQAView.setUserMap(maps);
        }
    }
    /**
     * 处理问答
     * @param list
     */
    public void handlerQAView(ArrayList<Map<String, String>> list){
        if(dishQAView!=null){
            dishQAView.setData(list);
            dishQAView.setVisibility(View.VISIBLE);
        }
    }
    /**
     * 处理底部推荐
     * @param list
     */
    public void handlerRecommedAndAd(ArrayList<Map<String, String>> list,String code,String name){
        if(dishRecommedAndAdView!=null){
            dishRecommedAndAdView.setVisibility(View.VISIBLE);
            dishRecommedAndAdView.initData(code,name);
            dishRecommedAndAdView.initUserDish(list);
        }
    }
    /**
     * 处理浮动推荐
     */
    public void handlerHoverViewCode(String code){
        if(dishHoverViewControl!=null){
//            dishHoverViewControl.setCode(code);
        }
    }
    /**
     * 处理浮动推荐
     */
    public void handlerHoverView(Map<String,String> map){
        if(dishHoverViewControl!=null){
            dishHoverViewControl.initData(map);
        }
    }
    public void onResume(){
        if(dishHeaderViewNew!=null)dishHeaderViewNew.onResume();
    }
    public void onPause(){

    }
    public void onDestroy() {
        if(dishHeaderViewNew!=null)dishHeaderViewNew.onDestroy();
    }

    public void refresh() {
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
        if (Math.abs(location[1]) > view_height) {
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
    /**
     * listview滑动监听
     */
    private void setListViewListener() {
        setViewOneState();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                /** 当滑动时停止播放 ...star.... */
//                changeGifState(view,scrollState);
                /** 当滑动时停止播放 .....end....*/
                switch (scrollState) {
                    // 当不滚动时
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        setViewOneState();
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        setViewOneState();
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        break;
                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                firstItemIndex = firstVisibleItem;
            }
        });
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!isHasVideo) {
                            if (firstItemIndex == 0 && !isRecored) {
                                startY = (int) event.getY();
                                isRecored = true;
                            }
                        }
                        oneY=(int) event.getY();
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
                        if(oneY==0)oneY=(int) event.getY();
                        int nowY = (int) event.getY();
                        scrollviewState(oneY,nowY);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (!isHasVideo) {
                            isRecored = false;
                            startY = 0;
                            if(mTimer!=null){
                                mTimer.schedule(2);
                            }
                        }
                        oneY=0;
                        break;
                }
                return false;
            }
        });
    }
    private int oneY = 0;
    private void scrollviewState(int oneY,int nowY){
        Log.i("xianghaTag","oneY::"+oneY+":::nowY:::"+nowY);
        if(nowY < oneY && ((oneY - nowY) > 1)){//向下滑动
            bar_title_1.setVisibility(View.GONE);
        }else if(nowY > oneY && (nowY - oneY) >=1){//向上滑动
            bar_title_1.setVisibility(View.VISIBLE);
        }
    }
    private int mMoveLen = 0;
    private MyTimer mTimer;
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
        if(dishVidioLayout==null)return;

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
}
