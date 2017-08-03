package amodule.dish.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.activity.DetailDish;
import amodule.dish.activity.MoreImageShow;
import amodule.dish.adapter.AdapterDishNew;
import amodule.dish.tools.ADDishContorl;
import aplug.basic.ReqInternet;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import third.video.VideoPlayerController;
import xh.basic.tool.UtilString;

import static amodule.dish.activity.DetailDish.showNumLookImage;
import static amodule.dish.activity.DetailDish.tongjiId;

/**
 * Created by Administrator on 2016/9/22.
 */

public class DishActivityViewControl {
    private Activity activity;
    private RelativeLayout bar_title_1;
    private XHWebView xhWebView;
    private ListView listview;
    private DishTitleViewControl dishTitleViewControl;
    private DishHeaderView dishHeaderView;
    private DishFootView dishFootView;
    private int wm_height;//屏幕高度
    public int firstItemIndex;        //外部重新设置onscroll时需设定此值。列表中首行索引，用来记录其与头部距离
    private boolean isRecored = false;
    private int startY;
    private boolean isHasVideo = false;
    private boolean isShowTitleColor=false;
    private boolean isHasVideoOnClick = false;
    private Map<String, String> dishInfoMap;//dishInfo数据
    private AdapterDishNew adapter;

    private ArrayList<Map<String, String>> stepList;//步骤图list
    private boolean isOnClickImageShow= false;
    private RelativeLayout dishVidioLayout;
    private RelativeLayout dregdeVipLayout;
    private TextView dredgeVipImmediately;
    private View view_oneImage;
    private TextView textPractice;
    private String state;
    private LoadManager loadManager;
    private String code;
    private boolean isGoLoading = true;
    public String dishJson;//历史记录中dishInfo的数据
    private boolean hasPermission = true;

    private int statusBarHeight = 0;//广告所用bar高度
    private DishViewCallBack callBack;
    private ADDishContorl adDishContorl;

    private int scrollMin = 0,scrollMax = 0,contentHeight = 0;
    //标题 + 状态栏的高度
    private int topRedundant;
    //底部全导航的高度
    private int bottomRedundant;

    public DishActivityViewControl(Activity activity){
        this.activity= activity;
        wm_height = activity.getWindowManager().getDefaultDisplay().getHeight();
        topRedundant = Tools.getDimen(activity,R.dimen.dp_45) ;
        bottomRedundant = 0;
    }

    public void init(String state,LoadManager loadManager,String code,DishViewCallBack callBack) {
        this.state= state;
        this.loadManager=loadManager;
        this.code= code;
        this.callBack=callBack;
        listview = (ListView)activity.findViewById(R.id.listview);
        listview.setVisibility(View.GONE);
        //处理标题
        dishTitleViewControl= new DishTitleViewControl(activity);
        dishTitleViewControl.initView(activity);
        dishTitleViewControl.setstate(state);
        initTitle();
        initView();
        stepList = new ArrayList<>();
        setAdapterData(null, true);
    }
    /**
     * 处理标题状态栏
     */
    private void initTitle() {
        bar_title_1 = (RelativeLayout) activity.findViewById(R.id.dish_title_dish);
//        if (Tools.isShowTitle()) {
//            int dp_45 = Tools.getDimen(activity, R.dimen.dp_45);
//            int height = dp_45 + Tools.getStatusBarHeight(activity);
//            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
//            bar_title_1.setLayoutParams(layout);
//            bar_title_1.setPadding(0, Tools.getStatusBarHeight(activity), 0, 0);
//        }
//        bar_title_1.setBackgroundColor(Color.parseColor("#00ffffff"));
//        bar_title_1.setBackgroundResource(R.drawable.bar_dish_gradient);
//        bar_title_1.setBackgroundResource(R.drawable.bg_dish_title);
    }

    /** 初始化view数据-----考虑以后抽成一个单独处理view */
    private void initView() {
        WebviewManager manager = new WebviewManager(activity,loadManager,true);
        xhWebView = manager.createWebView(R.id.XHWebview);
        //开通VIP
        dregdeVipLayout = (RelativeLayout) activity.findViewById(R.id.dredge_vip_bottom_layout);
        dredgeVipImmediately = (TextView) activity.findViewById(R.id.dredge_vip_immediately);

        //头部view处理
        dishHeaderView= new DishHeaderView(activity);
        dishHeaderView.initView(activity);
        //头部view进行预先加载——————
        LinearLayout header_linear_top = new LinearLayout(activity);
        //------------不能加，加上footer点击会慢------------
//        AbsListView.LayoutParams layoutparams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AbsListView.LayoutParams layoutparams_top = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        header_linear_top.setLayoutParams(layoutparams_top);
        header_linear_top.setOrientation(LinearLayout.VERTICAL);

        //底部footview
        dishFootView=new DishFootView(activity);
        dishFootView.initView(activity);

        //做法
        textPractice = new TextView(activity);
        textPractice.setPadding(Tools.getDimen(activity, R.dimen.dp_15), Tools.getDimen(activity, R.dimen.dp_25), 0, 0);
        textPractice.setTextSize(Tools.getDimenSp(activity, R.dimen.sp_18));
        textPractice.setTextColor(Color.parseColor("#333333"));
        TextPaint tp = textPractice.getPaint();
        tp.setFakeBoldText(true);
        textPractice.setText("做法");
        dishHeaderView.addView(textPractice);
        textPractice.setVisibility(View.GONE);

        //吃是一种艺术
        Button loadMoreBtn = new Button(activity);
        loadMoreBtn.setHeight(Tools.getDimen(activity, R.dimen.dp_70));
        loadMoreBtn.setPadding(0,0,0,Tools.getDimen(activity, R.dimen.dp_10));
        loadMoreBtn.setText("— 吃,也是一种艺术 —");
        loadMoreBtn.setTextSize(Tools.getDimenSp(activity, R.dimen.sp_12));
        loadMoreBtn.setTextColor(Color.parseColor("#AEAEAE"));
        loadMoreBtn.setBackgroundColor(activity.getResources().getColor(R.color.common_bg));
        int pa = ToolsDevice.dp2px(activity, 1);
        loadMoreBtn.setShadowLayer(pa, pa, pa,Color.parseColor("#F1EEE4"));
        dishFootView.addView(loadMoreBtn);
        //Listview加载头和顶部view
        listview.addHeaderView(header_linear_top, null, false);
        listview.addHeaderView(dishHeaderView, null, false);
        listview.addFooterView(dishFootView);
        setGlideViewListener();
    }

    public void reset(){
        dishTitleViewControl.reset();
        dishHeaderView.reset();
        dishFootView.reset();
        if(stepList != null)stepList.clear();
        adapter.notifyDataSetChanged();
        xhWebView.setVisibility(View.GONE);
        dregdeVipLayout.setVisibility(View.GONE);
        setLoginStatus();
    }

    public void setLoginStatus(){
        if (dishHeaderView != null)
            dishHeaderView.setLoginStatus();
    }

    /**
     * 添加广告控制
     * @param adDishContorl
     */
    public void setAdDishControl(ADDishContorl adDishContorl){
        this.adDishContorl=adDishContorl;
    }
    /**
     * 外部设置是否是视频
     * @param isHasVideo
     */
    public void setHasVideo(boolean isHasVideo){
        this.isHasVideo= isHasVideo;
    }

    /**
     * 设置是否可加载
     * @param isGoLoading
     */
    public void setIsGoLoading(boolean isGoLoading){
        this.isGoLoading=isGoLoading;
    }
    /**
     * 外部设置json
     * @param dishjson
     */
    public void setDishJson(String dishjson){
        this.dishJson=dishjson;
    }

    /**
     *
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
     * 根据数据处理view
     * @param list
     */
    public void analyzeData(ArrayList<Map<String, String>> list,Map<String,String> permissionMap) {
        String lable = list.get(0).get("lable");
        ArrayList<Map<String, String>> listmaps = UtilString.getListMapByJson(list.get(0).get("data"));
        //当前数据状态
        if (listmaps.size() > 0)
            isGoLoading = true;
        else {
            isGoLoading = false;
            return;
        }
        if (lable.equals("dishInfo")) {//第一页整体业务标示
            dishJson = list.get(0).get("data");//第一页数据保留下来
            analyzeDishInfoData(listmaps,permissionMap);
        } else if (lable.equals("recommendDishMenu")) {//菜谱推荐
            dishFootView.analyzeMenuData(listmaps,permissionMap);
        } else if (lable.equals("relatedRecommend")) {//相关推荐
            dishFootView.analyzeRelatedData(listmaps,permissionMap);
        } else if (lable.equals("wonderfulRecommend")) {//精彩推荐
            dishFootView.analyzeWonderfulData(listmaps,adDishContorl,permissionMap);
        }
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
        XHClick.track(activity,isHasVideo?"浏览视频菜谱详情页":"浏览图文菜谱详情页");
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
        dishHeaderView.setData(list, new DishHeaderView.DishHeaderVideoCallBack() {
            @Override
            public void videoImageOnClick() {
                String color = Tools.getColorStr(activity, R.color.common_top_bg);
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
        adapter.notifyDataSetChanged();

        ArrayList<Map<String,String>> list_makes = new ArrayList<>();
        Map<String,String> commonPermission = StringManager.getFirstMap(permissionMap.get("video"));
        commonPermission = StringManager.getFirstMap(commonPermission.get("common"));
        final String url = commonPermission.get("url");
        if((commonPermission.isEmpty() || StringManager.getBooleanByEqualsValue(commonPermission,"isShow"))
                ){
            hasPermission = true;
            dishTitleViewControl.setOfflineLayoutVisibility(true);
            textPractice.setVisibility(View.VISIBLE);
            //步骤
            list_makes = StringManager.getListMapByJson(list.get(0).get("makes"));
            for (int i = 0, size = list_makes.size(); i < size; i++) {
                list_makes.get(i).put("style", DishStepView.DISH_STYLE_STEP);
            }
        }else{
            hasPermission = false;
            String buttonStr = commonPermission.get("button2");
            if(!TextUtils.isEmpty(buttonStr))
                dredgeVipImmediately.setText(buttonStr);
            dregdeVipLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!TextUtils.isEmpty(url))
                        AppCommon.openUrl(activity,url,true);
                }
            });
        }
        setAdapterData(list_makes, false);
        //小贴士
        Map<String, String> map = new HashMap<>();
        map.putAll(list.get(0));
        map.put("style", DishExplainView.DISH_STYLE_EXP);
        dishFootView.setDataExplain(map,permissionMap);

        //分享
        ArrayList<Map<String, String>> list_share = new ArrayList<>();
        Map<String, String> map_share = new HashMap<>();
        map_share.put("style", DishShareShow.DISH_STYLE_SHARE);
        Map<String, String> mapData = dishTitleViewControl.getShareData();
        map_share.put("type", mapData.get("mType"));
        map_share.put("title", mapData.get("mTitle"));
        map_share.put("clickUrl", mapData.get("mClickUrl"));
        map_share.put("content", mapData.get("mContent"));
        map_share.put("imgUrl", mapData.get("mImgUrl"));
        map_share.put("from", "菜谱详情页");
        map_share.put("parent", "菜谱");
        list_share.add(map_share);
//        setAdapterData(list_share, false);
//        dishFootView.setDataShare(map_share);
        if ("2".equals(list.get(0).get("hasVideo")) && (list_makes == null || list_makes.size() <= 0)) {
            tongjiId = "a_menu_detail_onlyvideo430";
        }
    }

    /**
     * 步骤图处理
     *
     * @param list
     * @param isInit
     */
    private void setAdapterData(final ArrayList<Map<String, String>> list, boolean isInit) {
        if (isInit) {

            adapter = new AdapterDishNew(listview, stepList, 0, null, null);
            adapter.setClickCallBack(new AdapterDishNew.ItemOnClickCallBack() {
                @Override
                public void onClickPosition(int position) {
                    if(!getStateMakes(stepList)){//无图时不执行
                        return;
                    }
                    if(!isOnClickImageShow){
                        isOnClickImageShow=true;
                        ++showNumLookImage;
                    }
                    XHClick.mapStat(activity, tongjiId, "菜谱区域的点击", "步骤图点击");
                    Intent intent = new Intent(activity, MoreImageShow.class);
                    ArrayList<Map<String, String>> listdata = new ArrayList<Map<String, String>>();
                    listdata.addAll(stepList);
                    if (!TextUtils.isEmpty(dishInfoMap.get("remark"))) {
                        Map<String, String> map_temp = new HashMap<String, String>();
                        Map<String,String> dishInfo = StringManager.getFirstMap(dishJson);
                        if(dishInfo != null){
                            map_temp.put("img", dishInfo.get("img"));
                        }else{
                            map_temp.put("img", "");
                        }
                        map_temp.put("info", "小贴士：\n" + dishInfoMap.get("remark"));
                        map_temp.put("num", String.valueOf(stepList.size() + 1));
                        listdata.add(map_temp);
                    }
                    intent.putExtra("data", listdata);
                    intent.putExtra("index", position);
                    intent.putExtra("key", tongjiId);
                    activity.startActivity(intent);
                }
            });
            adapter.setActivity(activity);
            listview.setAdapter(adapter);
        } else {
            stepList.addAll(list);
            adapter.notifyDataSetChanged();
            loadManager.loadOver(ReqInternet.REQ_OK_STRING, 1, true);
            listview.setVisibility(View.VISIBLE);
        }
    }
    /**
     * 当前是否有图片
     * @param listdata
     * @return true --有图片，false---无图片
     */
    private boolean getStateMakes(ArrayList<Map<String, String>> listdata){
        if(listdata != null && listdata.size() > 0){
            for(int i = 0 , size = listdata.size() ; i < size ; i++){
                if(!TextUtils.isEmpty(listdata.get(i).get("img")))
                    return true;
            }
        }
        return false;
    }

    /**
     * listview滑动监听
     */
    private void setGlideViewListener() {
        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                /** 当滑动时停止播放 ...star.... */
                changeGifState(view,scrollState);
                /** 当滑动时停止播放 .....end....*/
                switch (scrollState) {
                    // 当不滚动时
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        setViewOneState();
                        // 判断滚动到底部
                        if (listview.getLastVisiblePosition() >= (listview.getCount() - 3) && isGoLoading&&ToolsDevice.isNetworkAvailable(activity)) {
                            if (!dishFootView.getstartWonderfulState()) {
                                callBack.gotoRequest();
                            }
                        }
                        //分页加载精彩推荐
                        if (listview.getLastVisiblePosition() >= (listview.getCount() - 2) && dishFootView.getstartWonderfulState()) {
                            dishFootView.addWonderfulView(dishFootView.getWonderful_index(), 2);
                        }
                        // 判断滚动到顶部
                        if (listview.getFirstVisiblePosition() == 0) {}
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
                if(dishHeaderView !=null)
                dishHeaderView.onListViewScroll();
                if(dishFootView!=null)
                dishFootView.onListViewScroll();

                if(isHasVideo && dishHeaderView != null && dishHeaderView.getVideoView() != null){
                    View headerView = dishHeaderView.getVideoView();
                    int[] location = new int[2];
                    headerView.getLocationOnScreen(location);
                    int viewBottom = location[1] + headerView.getHeight();
                    int mixHeight = Tools.getStatusBarHeight(activity) + Tools.getDimen(activity,R.dimen.dp_45);
                    if(viewBottom <= mixHeight && !hasPermission){
                        dregdeVipLayout.setVisibility(View.VISIBLE);
                    }else
                        dregdeVipLayout.setVisibility(View.GONE);
                }
            }
        });
        listview.setOnTouchListener(new View.OnTouchListener() {
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
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!isHasVideo) {
                            int tempY = (int) event.getY();
                            if (!isRecored && firstItemIndex == 0) {// 如果首item索引为0，且尚未记录startY,则在拖动时记录之，并执行isRecored
                                isRecored = true;
                                startY = tempY;
                            } else if (firstItemIndex == 0) {
                                int y = tempY - startY;
                                int dp_300 = Tools.getDimen(activity, R.dimen.dp_300);
                                if (wm_height > 0) {
                                    if (dp_300 + y <= wm_height * 2 / 3) {
                                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dp_300 + y);
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
                            int dp_300 = Tools.getDimen(activity, R.dimen.dp_300);
                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dp_300);
                            dishVidioLayout.setLayoutParams(layoutParams);
                        }
                        break;
                }
                return false;
            }
        });
    }


    private void changeGifState(AbsListView view,int scrollState){
        if(1 == 1) return;
        final int length = view.getChildCount();
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
            int index = 0;
            for(; index < length ; index ++){
                View itemView = view.getChildAt(index);
                int top = itemView.getTop();
                int height = itemView.getHeight();
                if(scrollMin == 0) {
                    contentHeight = ToolsDevice.getWindowPx(activity).heightPixels - topRedundant - bottomRedundant;
                    scrollMin = topRedundant;
                    scrollMax = contentHeight - bottomRedundant;
                    if(contentHeight >= height * 3 / 2){
                        scrollMin += height / 3;
                        scrollMax = contentHeight - bottomRedundant  - height / 3;
                    }
                }
                final int value = height + top;
                if(itemView instanceof DishStepView){
                    //在正中间，要是需要自动播就调用
                    if(value <= scrollMax && value >= scrollMin){
                        Log.i("FRJ","onScrollStateChanged 在正中间，要是需要自动播就调用: " + index);
                    }else{
                        Log.i("FRJ","onScrollStateChanged 不在正中间，要是需要停止 : " + index);
                        ((DishStepView)itemView).stopGif();
                    }
                }
            }
        }
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
        if (Math.abs(location[1]) > view_height - statusBarHeight) {
            bar_title_1.clearAnimation();
            //初始化view都为不透明
            if (isHasVideoOnClick) return;
            String color = Tools.getColorStr(activity, R.color.common_top_bg);
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
     * 外部设置view 高度
     * @param statusBarHeight
     */
    public void setStatusBarHeight( int statusBarHeight){
        this.statusBarHeight=statusBarHeight;
    }
    public interface DishViewCallBack{
        public void getVideoPlayerController(VideoPlayerController mVideoPlayerController);
        public void gotoRequest();

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
    public DishHeaderView getDishHeaderView(){
        return dishHeaderView;
    }

    /**
     * 获取第一页集合
     * @return
     */
    public Map<String, String> getDishInfoMap (){
        return  dishInfoMap;
    }
}
