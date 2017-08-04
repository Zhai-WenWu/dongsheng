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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

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
import aplug.web.view.XHWebView;
import third.video.VideoPlayerController;

import static amodule.dish.activity.DetailDish.tongjiId;
import static java.lang.System.currentTimeMillis;
import static xh.basic.tool.UtilString.getListMapByJson;

/**
 * 菜谱界面的总控制类
 * Created by Fang Ruijiao on 2017/7/12.
 */
public class  DishActivityViewControlNew {

    private Activity mAct;
    private RelativeLayout bar_title_1;
    private DishWebView mXhWebView;
    private XhScrollView mScrollView;
    private View view_oneImage;
    private RelativeLayout dishVidioLayout;
    private XHWebView xhWebView;

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

        //处理标题
        dishTitleViewControl= new DishTitleViewControlNew(mAct, new DishTitleViewControlNew.OnDishTitleControlListener() {
            @Override
            public String getOffDishJson() {
                return dishJson;
            }
        });
        dishTitleViewControl.initView(mAct,mXhWebView);
        dishTitleViewControl.setstate(state);
        mFootControl = new DishFootControl(mAct,mDishCode);
    }

    private void initView(){
        View leftClose = mAct.findViewById(R.id.leftClose);
        leftClose.setVisibility(View.VISIBLE);
        leftClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(mAct, tongjiId, "顶部导航栏", "关闭点击量");
                Main.colse_level = 1;
                mAct.finish();
            }
        });

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

        mXhWebView = (DishWebView) mAct.findViewById(R.id.a_dish_detail_new_web);
        mXhWebView.setOnIngreListener(new DishWebView.OnIngreListener() {
            @Override
            public void setOnIngre(String ingre) {
                saveHistoryToDB(ingre);
            }
        });
        //头部view处理
        dishHeaderView= (DishHeaderViewNew) mAct.findViewById(R.id.a_dish_detail_new_headview);
        videoLayoutHeight = ToolsDevice.getWindowPx(mAct).widthPixels * 9 / 16 + titleHeight + statusBarHeight;
        dishHeaderView.initView(mAct,videoLayoutHeight);
        mScrollView = (XhScrollView) mAct.findViewById(R.id.a_dish_detail_new_scrollview);
        setGlideViewListener();
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
                if(mFootControl!=null){
                    mFootControl.onSrollView();
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
    public void analyzeDishInfoData(String dishInfo, Map<String, String> permissionMap,boolean isReadLocal) {
        dishJson = dishInfo;
        ArrayList<Map<String, String>> list = StringManager.getListMapByJson(dishInfo);
        if(list.size() == 0) return;

        dishInfoMap = list.get(0);
        mXhWebView.loadDishData(mDishCode,isReadLocal);

        isHasVideo = "2".equals(dishInfoMap.get("type"));
        XHClick.track(mAct,isHasVideo?"浏览视频菜谱详情页":"浏览图文菜谱详情页");
        if(isHasVideo)tongjiId="a_menu_detail_video";
        dishTitleViewControl.setData(dishInfoMap,mDishCode,isHasVideo,dishInfoMap.get("dishState"),loadManager);

        Map<String, String> customer = StringManager.getFirstMap(dishInfoMap.get("customer"));
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
        mFootControl.setDishInfo(dishInfoMap.get("name"));
        mScrollView.setVisibility(View.VISIBLE);
    }

    private boolean saveHistory = false;
    private void saveHistoryToDB(final String burden) {
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
        xhWebView.setVisibility(View.GONE);
    }

    public void setLoginStatus(){
        if (dishHeaderView != null)
            dishHeaderView.setLoginStatus();
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

}
