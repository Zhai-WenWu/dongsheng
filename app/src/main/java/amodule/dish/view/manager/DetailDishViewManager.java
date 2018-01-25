package amodule.dish.view.manager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

import acore.logic.LoginManager;
import acore.logic.ConfigMannager;
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
import third.video.VideoPlayerController;

import static acore.logic.ConfigMannager.KEY_CAIPUVIP;

/**
 * 当前只处理View的拼装
 * 不能牵扯如何业务逻辑处理----因为当前页面业务确定，采用直接数据指向方法（不抽象不模糊）
 */
public class DetailDishViewManager {
    private RelativeLayout dishVidioLayout;
    private ListView listView;
    private int firstItemIndex = -1,startY;
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
    public View noStepView;
    private RelativeLayout bar_title_1;
    private boolean isLoadVip= false;
    private LinearLayout linearLayoutOne;

    /**
     * 对view进行基础初始化
     */
    public DetailDishViewManager(Activity activity, ListView listView, String state) {
        wm_height = activity.getWindowManager().getDefaultDisplay().getHeight();
        mAct = activity;
        this.listView = listView;
        titleHeight = Tools.getDimen(mAct,R.dimen.topbar_height);
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
        headerLayoutHeight = ToolsDevice.getWindowPx(mAct).widthPixels * 9 / 16;
        //图片视频信息
        dishHeaderViewNew = new DishHeaderViewNew(mAct);
        dishHeaderViewNew.initView(mAct, headerLayoutHeight);
        dishVipView = new DishVipView(mAct);
        dishVipView.setVisibility(View.GONE);
        //用户信息和菜谱基础信息
        dishAboutView= new DishAboutView(mAct);
        dishAboutView.setVisibility(View.INVISIBLE);
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
        //处理特殊逻辑判断
        linearLayoutOne= new LinearLayout(mAct);
        RelativeLayout.LayoutParams layoutParamsOne = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayoutOne.setLayoutParams(layoutParamsOne);
        linearLayoutOne.setOrientation(LinearLayout.VERTICAL);
        layoutHeader.addView(linearLayoutOne);

        textStep = new TextView(activity);
        textStep.setPadding(Tools.getDimen(activity, R.dimen.dp_20), Tools.getDimen(activity, R.dimen.dp_35), 0,  Tools.getDimen(activity, R.dimen.dp_14));
        textStep.setTextSize(Tools.getDimenSp(activity, R.dimen.sp_18));
        textStep.setTextColor(Color.parseColor("#333333"));
        TextPaint tp = textStep.getPaint();
        tp.setFakeBoldText(true);
        textStep.setVisibility(View.GONE);
        noStepView=LayoutInflater.from(mAct).inflate(R.layout.dish_no_step,null);
        noStepView.setVisibility(View.GONE);
        layoutHeader.addView(textStep);
        layoutHeader.addView(noStepView);
        dishQAView = new DishQAView(mAct);
        dishQAView.setVisibility(View.GONE);

        //foot
        dishExplainView = new DishExplainView(mAct);
        dishExplainView.setVisibility(View.GONE);
        dishRecommedAndAdView= new DishRecommedAndAdView(mAct);
        dishRecommedAndAdView.setVisibility(View.GONE);
        layoutFooter.addView(dishExplainView);
        layoutFooter.addView(dishQAView);
        RelativeLayout layout= new RelativeLayout(mAct);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,Tools.getDimen(mAct,R.dimen.dp_60));
        layout.setLayoutParams(layoutParams);
        layoutFooter.addView(dishRecommedAndAdView);
        layoutFooter.addView(layout);
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
        String colors = Tools.getColorStr(mAct, R.color.common_top_bg);
        bar_title_1.setBackgroundResource(R.color.common_top_bg);
        Tools.setStatusBarColor(mAct, Color.parseColor(colors));
    }
    /**
     * 处理预先加载数据
     */
    public void initBeforeData(String img,String dishInfo){
        if(dishAboutView != null && !TextUtils.isEmpty(dishInfo)) {
            dishAboutView.setVisibility(View.VISIBLE);
            Map<String,String> map=StringManager.getFirstMap(Uri.decode(dishInfo));
            int height=0;
            if(map.containsKey("type")&&!TextUtils.isEmpty(map.get("type"))&&"2".equals(map.get("type"))){
                height=ToolsDevice.getWindowPx(mAct).widthPixels * 9 / 16;
            }
            if (dishHeaderViewNew != null&& !TextUtils.isEmpty(img))dishHeaderViewNew.setImg(img,height);
            if(TextUtils.isEmpty(img)&&map.containsKey("img")&&!TextUtils.isEmpty(map.get("img"))&&dishHeaderViewNew!=null)dishHeaderViewNew.setImg(map.get("img"),height);
            dishAboutView.setData(map, mAct,true);
            Map<String,String> mapTemp = StringManager.getFirstMap(map.get("customer"));
            if(mapTemp.containsKey("nickName")&&!TextUtils.isEmpty(mapTemp.get("nickName")))handlerTitleName(mapTemp.get("nickName"));
            initVipView(map.containsKey("type")?map.get("type"):"");
        }else if (dishHeaderViewNew != null&& !TextUtils.isEmpty(img))dishHeaderViewNew.setImg(img,0);
    }
    public void initVipView(String type){
        if(isLoadVip)return;isLoadVip=true;
        String caipuVipConfig = ConfigMannager.getConfigByLocal(KEY_CAIPUVIP);
        if(TextUtils.isEmpty(caipuVipConfig)){isLoadVip=false;return;}
        Map<String,String> configMap = StringManager.getFirstMap(caipuVipConfig);
        String key = !TextUtils.isEmpty(type)&&"2".equals(type) ? "caipuVideo" : "caipu";
        configMap = StringManager.getFirstMap(configMap.get(key));
        String delayDayValue = configMap.get("delayDay");
        int delayDay = TextUtils.isEmpty(delayDayValue) ? 7 : Integer.parseInt(delayDayValue);
        boolean isShowByConfig = "2".equals(configMap.get("isShow"));
        boolean isShowByUser=false;
        if(LoginManager.isLogin()){
            boolean isFastExpiry = LoginManager.getUserVipMaturityDay() <= delayDay
                    && LoginManager.getUserVipMaturityDay() >= 0;
            if(isFastExpiry)
                configMap.put("title",configMap.get("renewTitle"));
            isShowByUser = !LoginManager.isVIP() || isFastExpiry;
        }else{
            boolean isFastExpiry = LoginManager.getTempVipMaturityDay() <= delayDay
                    && LoginManager.getTempVipMaturityDay() >= 0;
            if(isFastExpiry)
                configMap.put("title",configMap.get("renewTitle"));
            isShowByUser = !LoginManager.isTempVip() || isFastExpiry;
        }
        configMap.put("isShow", isShowByConfig && isShowByUser ? "2" : "1");
        handlerVipView(configMap);
    }
    public void handlerIsSchool(String isSchool){
        linearLayoutOne.removeAllViews();
        if(!TextUtils.isEmpty(isSchool)&&"2".equals(isSchool)){
            linearLayoutOne.addView(dishModuleScrollView);
            linearLayoutOne.addView(dishIngreDataShow);
        }else {
            linearLayoutOne.addView(dishIngreDataShow);
            linearLayoutOne.addView(dishModuleScrollView);
        }
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

    public void handlerShareData(String shareStr) {
        if (dishTitleViewControl != null) {
            dishTitleViewControl.setShareData(shareStr);
        }
    }

    /**
     * 处理header图片，和视频数据
     */
    public void handlerHeaderView(ArrayList<Map<String, String>> list, Map<String, String> permissionMap) {
        if("2".equals(list.get(0).get("type"))){
            isHasVideo=true;
        }
        if(!isHasVideo)mTimer = new MyTimer(handler);
        handlerHeight(isHasVideo);
        if (dishHeaderViewNew != null) {
            dishHeaderViewNew.setDistance(0);
            dishHeaderViewNew.setDishCallBack(new DishHeaderViewNew.DishHeaderVideoCallBack() {
                @Override
                public void videoImageOnClick() {
                }
                @Override
                public void getVideoControl(VideoPlayerController mVideoPlayerController, RelativeLayout dishVidioLayouts, View view_oneImage) {
                    dishVidioLayout=dishVidioLayouts;
                }
            });
            dishHeaderViewNew.setData(list, permissionMap);
            dishVidioLayout=dishHeaderViewNew.getViewLayout();
        }
    }
    private void handlerHeight(boolean isvideo){
        headerLayoutHeight=isvideo?ToolsDevice.getWindowPx(mAct).widthPixels * 9 / 16:ToolsDevice.getWindowPx(mAct).widthPixels *5/6;
        if(dishHeaderViewNew!=null)dishHeaderViewNew.paramsLayout(headerLayoutHeight);
    }
    public void handlerLoginStatus(){
        if (dishHeaderViewNew != null)
            dishHeaderViewNew.setLoginStatus();
    }
    /**
     * 处理菜谱基本信息
     */
    public void handlerDishData(ArrayList<Map<String, String>> list) {
        if(dishAboutView!=null) {
            dishAboutView.setVisibility(View.VISIBLE);
            dishAboutView.setData(list.get(0), mAct,false);
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
     */
    public void handlerVipView(Map<String,String> relation){
        if(dishVipView != null){
            if(relation.containsKey("isShow")&&"2".equals(relation.get("isShow"))) {
                dishVipView.setVisibility(View.VISIBLE);
                dishVipView.setData(relation);
                Log.i("xianghaTag","VIP::::handlerVipView:::title:::"+relation.get("title"));
            }else dishVipView.setVisibility(View.GONE);
        }
    }
    /**
     * 处理用料
     */
    public void handlerIngreView(ArrayList<Map<String, String>> list) {
        if(list!=null&&list.size()>0) {
            dishIngreDataShow.setVisibility(View.VISIBLE);
            dishIngreDataShow.setData(list);
        }else dishIngreDataShow.setVisibility(View.GONE);
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
    public void handlerSkillView(ArrayList<Map<String, String>> list,String dishCode,String courseCode,String chapterCode,DishModuleScrollView.onDishModuleClickCallBack callback) {
        if(dishModuleScrollView!=null&&"2".equals(list.get(0).get("isShow"))){
            dishModuleScrollView.setVisibility(View.VISIBLE);
            if(list.get(0).containsKey("list")) {
                dishModuleScrollView.setData(list, dishCode, courseCode, chapterCode);
                dishModuleScrollView.setCallBack(callback);
            }
        }else  dishModuleScrollView.setVisibility(View.GONE);
    }
    /**
     * 处理步骤相关view
     */
    public void handlerStepView(ArrayList<Map<String,String>> list){
        if(list!=null&&list.size()>0) {
            Map<String,String> map= list.get(0);
            if(map.containsKey("title")&&!TextUtils.isEmpty(map.get("title"))){
                textStep.setVisibility(View.VISIBLE);
                textStep.setText(map.get("title"));
            }else textStep.setVisibility(View.GONE);
            if("1".equals(map.get("isShow"))) {
                if("2".equals(map.get("isCourseDish"))){
                    textStep.setVisibility(View.GONE);
                    return;
                }
                textStep.setPadding(Tools.getDimen(mAct, R.dimen.dp_20), Tools.getDimen(mAct, R.dimen.dp_35), 0, 0);
                noStepView.setVisibility(View.VISIBLE);
                ((TextView) noStepView.findViewById(R.id.dish_no_step_tv)).setText(map.get("promptMsg"));
            }

        }
        textStep.setVisibility(View.VISIBLE);
    }
    /**
     * 处理小贴士信息
     */
    public void handlerExplainView(Map<String,String> map) {
        if(dishExplainView!=null){
            dishExplainView.setVisibility(View.VISIBLE);
        }
        if(dishExplainView!=null && map!=null && !TextUtils.isEmpty(map.get("remark"))){
            dishExplainView.setVisibility(View.VISIBLE);
            dishExplainView.setData(map);
        }else dishExplainView.hideViewRemark();
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
            Map<String,String> map= list.get(0);
            if(!map.containsKey("list")||TextUtils.isEmpty(map.get("list"))||"[]".equals(map.get("list"))){
                dishQAView.setVisibility(View.GONE);
            }else {
                dishQAView.setData(list);
                dishQAView.setVisibility(View.VISIBLE);
            }
        }
    }
    /**
     * 处理底部推荐
     * @param list
     */
    public void handlerRecommedAndAd(ArrayList<Map<String, String>> list,String code,String name){
        if(list==null)return;
        Map<String,String> temp=list.get(0);
        if(dishRecommedAndAdView!=null&&list!=null&&!TextUtils.isEmpty(temp.get("list"))
                &&!"[]".equals(temp.get("list"))&&!"{}".equals(temp.get("list"))){
            dishRecommedAndAdView.setVisibility(View.VISIBLE);
            dishRecommedAndAdView.initData(code,name);
            dishRecommedAndAdView.initUserDish(list);
        }
    }
    /**
     * 处理浮动推荐
     */
    public void handlerHoverView(Map<String,String> map,String code,String dishName){
        if(dishHoverViewControl!=null){
            dishHoverViewControl.initData(map,code,dishName);
        }
    }
    public void onResume(){
        if(dishHeaderViewNew!=null)dishHeaderViewNew.onResume();
    }
    public void onPause(){
        if(dishHeaderViewNew!=null)dishHeaderViewNew.onPause();
    }
    public void onDestroy() {
        if(dishHeaderViewNew!=null)dishHeaderViewNew.onDestroy();
        if(mTimer!=null) {
            mTimer.cancel();
            mTimer = null;
        }
    }
    public void onReset(){
        if(dishHeaderViewNew!=null)dishHeaderViewNew.onReset();
    }
    public boolean onBackPressed(){
        if(dishHeaderViewNew==null)return false;
        return dishHeaderViewNew.onBackPressed();
    }
    /**
     * listview滑动监听
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void setListViewListener() {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                firstItemIndex=firstVisibleItem;
                if(layoutFooter!=null){
                    onSrollView();
                }
            }
        });
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!isHasVideo) {
                            if (firstItemIndex == 0 && !isRecored && isShowScreen()) {
                                startY = (int) event.getY();
                                isRecored = true;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!isHasVideo) {
                            int tempY = (int) event.getY();
                            if (!isRecored && firstItemIndex == 0&&isShowScreen()) {// 如果首item索引为0，且尚未记录startY,则在拖动时记录之，并执行isRecored
                                isRecored = true;
                                startY = tempY;
                            } else if (firstItemIndex == 0&&isShowScreen()) {
                                int y = tempY - startY;
                                if (wm_height > 0 && y > 0) {
                                    if (headerLayoutHeight + y <= wm_height * 2 / 3) {
                                        mMoveLen = y;
                                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, headerLayoutHeight + y);
                                        if(dishVidioLayout!=null&& layoutParams!=null)dishVidioLayout.setLayoutParams(layoutParams);
                                    }
                                }
                            }
                        }
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
                        break;
                }
                return false;
            }
        });
    }
    private boolean isShowScreen(){
        int[] location = new int[2];
        dishHeaderViewNew.getLocationOnScreen(location);
        if(location[1]>Tools.getDimen(mAct,R.dimen.topbar_height)){
            return true;
        }
        return false;
    }
    private int mMoveLen = 0;
    private MyTimer mTimer;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mMoveLen!=0){
                mMoveLen= mMoveLen-3;
            }else if (mTimer != null){
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
    public void handleVipState(boolean isVip) {
        if (dishHeaderViewNew != null)
            dishHeaderViewNew.handleVipState(isVip);
    }
    /**
     * scrollview滚动监听
     */
    public void onSrollView(){
        for (int i = 0; i < layoutFooter.getChildCount(); i++) {
            View dishAdDataView = layoutFooter.getChildAt(i);
            if (dishAdDataView != null && dishAdDataView instanceof DishExplainView) {
                int[] viewLocation = new int[2];
                dishAdDataView.getLocationOnScreen(viewLocation);
                if ((viewLocation[1] > Tools.getStatusBarHeight(mAct)
                        && viewLocation[1] < Tools.getScreenHeight() - ToolsDevice.dp2px(mAct, 57))) {
                    ((DishExplainView) dishAdDataView).onListScroll();
                }
            }
        }
    }
    public void hideLayout(){
        if(dishHoverViewControl!=null)dishHoverViewControl.hindGoodLayout();
    }

    public void setOnVideoCanPlay(VideoPlayerController.OnVideoCanPlayCallback callback){
        if(dishHeaderViewNew != null){
            dishHeaderViewNew.setOnVideoCanPlay(callback);
        }
    }
}
