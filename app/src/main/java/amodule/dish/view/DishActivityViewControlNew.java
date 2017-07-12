package amodule.dish.view;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.tools.ADDishContorl;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import third.video.VideoPlayerController;

/**
 * Created by Fang Ruijiao on 2017/7/12.
 */
public class DishActivityViewControlNew {

    private Activity mAct;
    private RelativeLayout bar_title_1;
    private XHWebView mXhWebView;
    private XhScrollView mScrollView;
    private View view_oneImage;
    private RelativeLayout dishVidioLayout;
    private XHWebView xhWebView;

    private int wm_height;//屏幕高度
    //标题 + 状态栏的高度
    private int topRedundant;
    //底部全导航的高度
    private int bottomRedundant;
    private int adBarHeight = 0;//广告所用bar高度
    //广告所用bar高度;图片/视频高度
    private int statusBarHeight = 0,videoLayoutHeight;
    private int startY;

    private String state;
    private LoadManager loadManager;
    private String code;
    private boolean isHasVideoOnClick = false;
    private boolean isShowTitleColor=false;
    private boolean isHasVideo = false;
    private boolean isRecored = false;
    private boolean isGoLoading = true;
    private int firstItemIndex = 0;

    private Map<String, String> dishInfoMap;//dishInfo数据
    private String dishJson;//历史记录中dishInfo的数据

    private DishTitleViewControl dishTitleViewControl;
    private DishHeaderViewNew dishHeaderView;
    private ADDishContorl adDishContorl;


    private DishViewCallBack callBack;

    public DishActivityViewControlNew(Activity activity){
        this.mAct = activity;
        wm_height = activity.getWindowManager().getDefaultDisplay().getHeight();
        topRedundant = Tools.getDimen(activity, R.dimen.dp_45) ;
        bottomRedundant = 0;
    }

    public void init(String state, LoadManager loadManager, String code, DishViewCallBack callBack) {
        this.state = state;
        this.loadManager = loadManager;
        this.code = code;
        this.callBack = callBack;
        //处理标题
        dishTitleViewControl= new DishTitleViewControl(mAct);
        dishTitleViewControl.initView(mAct);
        dishTitleViewControl.setstate(state);
        initView();
    }

    private void initView(){
        WebviewManager manager = new WebviewManager(mAct,loadManager,true);
        xhWebView = manager.createWebView(R.id.XHWebview);

        int titleHeight = Tools.getDimen(mAct,R.dimen.dp_45);

        bar_title_1 = (RelativeLayout) mAct.findViewById(R.id.a_dish_detail_new_title);
        statusBarHeight = Tools.getStatusBarHeight(mAct);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bar_title_1.getLayoutParams();
        layoutParams.height = titleHeight + statusBarHeight;
        View title_state_bar = mAct.findViewById(R.id.title_state_bar);
        layoutParams = (RelativeLayout.LayoutParams) title_state_bar.getLayoutParams();
        layoutParams.height = statusBarHeight;

        mXhWebView = (XHWebView) mAct.findViewById(R.id.a_dish_detail_new_web);
        //头部view处理
        dishHeaderView= (DishHeaderViewNew) mAct.findViewById(R.id.a_dish_detail_new_headview);
        videoLayoutHeight = ToolsDevice.getWindowPx(mAct).widthPixels * 9 / 16 + titleHeight + statusBarHeight;
        dishHeaderView.initView(mAct,videoLayoutHeight);
        mScrollView = (XhScrollView) mAct.findViewById(R.id.a_dish_detail_new_scrollview);
        setGlideViewListener();
        mXhWebView.loadUrl("http://appweb.xiangha.com/zhishi/nousInfo?code=240922");
    }

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
                                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, videoLayoutHeight + y);
                                        dishVidioLayout.setLayoutParams(layoutParams);
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
                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, videoLayoutHeight);
                            dishVidioLayout.setLayoutParams(layoutParams);
                        }
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 解析数据-----业务标示是“dishInfo”
     *
     * @param list
     * @param permissionMap
     */
    public void analyzeDishInfoData(ArrayList<Map<String, String>> list, Map<String, String> permissionMap) {
        dishInfoMap = list.get(0);
        isHasVideo = dishInfoMap.get("hasVideo").equals("2");
        XHClick.track(mAct,isHasVideo?"浏览视频菜谱详情页":"浏览图文菜谱详情页");
        dishTitleViewControl.setData(dishInfoMap,code,dishJson,isHasVideo,dishInfoMap.get("dishState"),loadManager);
        Map<String, String> customer = StringManager.getFirstMap(list.get(0).get("customer"));
        if (customer != null&& !TextUtils.isEmpty(customer.get("code")) && LoginManager.userInfo != null
                && customer.get("code").equals(LoginManager.userInfo.get("code"))) {
            state = "";
            dishTitleViewControl.setstate(state);
        }
        dishTitleViewControl.setViewState();
        if(permissionMap != null && permissionMap.containsKey("offLine")){
            Map<String,String> offlineMap = StringManager.getFirstMap(permissionMap.get("offLine"));
            offlineMap = StringManager.getFirstMap(offlineMap.get("common"));
            dishTitleViewControl.setOfflineLayoutVisibility("2".equals(offlineMap.get("isShow")));
        }

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
        if (dishHeaderView != null)
            dishHeaderView.setLoginStatus();
        xhWebView.setVisibility(View.GONE);
    }

    /**
     * @param pagePermission
     * @return
     */
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
     * 设置是否可加载
     * @param isGoLoading
     */
    public void setIsGoLoading(boolean isGoLoading){
        this.isGoLoading=isGoLoading;
    }

    /**
     * 添加广告控制
     * @param adDishContorl
     */
    public void setAdDishControl(ADDishContorl adDishContorl){
        this.adDishContorl=adDishContorl;
    }

    /**
     * 外部设置json
     * @param dishjson
     */
    public void setDishJson(String dishjson){
        this.dishJson=dishjson;
    }

    /**
     * 获取头部控件
     * @return
     */
    public DishTitleViewControl getDishTitleViewControl(){
        return dishTitleViewControl;
    }

    /**
     * 获取头view
     * @return
     */
    public DishHeaderViewNew getDishHeaderView(){
        return dishHeaderView;
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
        public void getVideoPlayerController(VideoPlayerController mVideoPlayerController);
        public void gotoRequest();

    }

}
