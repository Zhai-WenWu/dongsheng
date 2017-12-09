package amodule.dish.activity;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.xiangha.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import acore.logic.LoginManager;
import acore.logic.SpecialWebControl;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.FileManager;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import amodule.dish.adapter.AdapterDishRvListView;
import amodule.dish.db.DataOperate;
import amodule.dish.view.DishModuleScrollView;
import amodule.dish.view.manager.DetailDishDataManager;
import amodule.dish.view.manager.DetailDishViewManager;
import amodule.main.Main;
import amodule.user.db.BrowseHistorySqlite;
import amodule.user.db.HistoryData;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;

import static amodule.dish.activity.DetailDishWeb.tongjiId;
import static java.lang.System.currentTimeMillis;

/**
 * 菜谱详情页原生标准
 */
public class DetailDish extends BaseAppCompatActivity implements IObserver {
    public static String tongjiId_detail = "a_menu_detail_normal";//统计标示
    private String data_type = "";
    private String module_type = "";
    private String img = "";//预加载图片
    public String code, dishTitle, state,dishName;//页面开启状态所必须的数据。
    public static long startTime = 0;
    private Handler handlerScreen;
    private RvListView rvListview;
    private DetailDishViewManager detailDishViewManager;//view控制器
    private DetailDishDataManager detailDishDataManager;//数据控制器
    private ArrayList<Map<String,String>> maplist = new ArrayList<>();
    private Map<String,String> mapTop = new HashMap<>();
    private boolean isHasVideo;//当前是否是视频
    private String customerCode;
    private RelativeLayout dredgeVipFullLayout;
    private XHWebView pageXhWebView;
    private String dishInfo = "";
    private AdapterDishRvListView adapterDishRvListView;
//    private AdapterDishNew adapterDishNew;
    private String courseCode;//课程分类
    private String chapterCode;//章节分类
    private boolean isShowPowerPermission=false;
    private boolean isShowVip = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime= System.currentTimeMillis();
        initBudle();
        initView();
        initData();
    }
    /**
     * 处理页面初始数据
     */
    private void initBudle() {
        Bundle bundle = getIntent().getExtras();
        startTime = System.currentTimeMillis();
        if (bundle != null) {
            code = bundle.getString("code");
            dishTitle = bundle.getString("name");
            if (dishTitle == null) dishTitle = "香哈菜谱";
            courseCode = bundle.getString("courseCode","");
            chapterCode = bundle.getString("chapterCode","");
            state = bundle.getString("state");
            data_type = bundle.getString("data_type");
            module_type = bundle.getString("module_type");
            img = bundle.getString("img");
            dishInfo = bundle.getString("dishInfo");
            DataOperate.saveHistoryCode(code);//保存历史记录
        }
        if (TextUtils.isEmpty(code)) {
            Tools.showToast(getApplicationContext(), "抱歉，未找到相应菜谱");
            this.finish();
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持高亮
        handlerScreen = new Handler();
        handlerScreen.postDelayed(new Runnable() {
            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }, 15 * 60 * 1000);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);//sufureView页面闪烁
        XHClick.track(XHApplication.in(), "浏览菜谱详情页");
        ObserverManager.getInstence().registerObserver(this,ObserverManager.NOTIFY_LOGIN,ObserverManager.NOTIFY_FOLLOW,ObserverManager.NOTIFY_PAYFINISH,ObserverManager.NOTIFY_UPLOADOVER);
    }
    /**
     * 处理页面Ui
     */
    private void initView() {
        initActivity("", 2, 0, 0, R.layout.a_detail_dish);
        rvListview = (RvListView) findViewById(R.id.rvListview);
    }
    /**
     * 处理页面Ui
     */
    private void initData() {
        adapterDishRvListView = new AdapterDishRvListView(this,maplist);
        rvListview.setAdapter(adapterDishRvListView);
        if (detailDishViewManager == null) {//view manager
            detailDishViewManager = new DetailDishViewManager(this, rvListview, state);
            dishInfo= Uri.decode(dishInfo);
            detailDishViewManager.initBeforeData(img,dishInfo);
            handleVipState();
        }
        if (detailDishDataManager == null) detailDishDataManager = new DetailDishDataManager(code,this,courseCode,chapterCode);//数据manager
        detailDishDataManager.setDishDataCallBack(new DetailDishDataManager.DishDataCallBack() {
            @Override
            public void handlerTypeData(String type, ArrayList<Map<String,String>> list,Map<String,String> PermissionMap) {
                dishTypeData(type,list,PermissionMap);
            }
        });
        adapterDishRvListView.setClickCallBack(new AdapterDishRvListView.ItemOnClickCallBack() {
            @Override
            public void onClickPosition(int position) {
                if(!getStateMakes(maplist)){//无图时不执行
                    return;
                }
                XHClick.mapStat(DetailDish.this, DetailDish.tongjiId_detail, "步骤", "步骤图点击量");
                Intent intent = new Intent(DetailDish.this, MoreImageShow.class);
                ArrayList<Map<String, String>> listdata = new ArrayList<>();
                listdata.addAll(maplist);
                if (!TextUtils.isEmpty(mapTop.get("remark"))) {
                    Map<String, String> map_temp = new HashMap();
                    map_temp.put("img", maplist.get(maplist.size()-1).get("img"));
                    map_temp.put("info", "小贴士：\n" + mapTop.get("remark"));
                    map_temp.put("num", String.valueOf(maplist.size() + 1));
                    listdata.add(map_temp);
                }
                intent.putExtra("data", listdata);
                intent.putExtra("index", position);
                intent.putExtra("key", tongjiId);
                DetailDish.this.startActivity(intent);
            }
            @Override
            public void onGifClickPosition(int position) {
            }
        });
    }
    private void dishTypeData(String type,ArrayList<Map<String,String>> list,Map<String,String> map){
        switch (type){
            case DetailDishDataManager.DISH_DATA_TOP://topInfo,菜谱的基本信息和用户的基本信息
                mapTop= list.get(0);
                dishName= mapTop.get("name");
                isHasVideo = "2".equals(mapTop.get("type"));
                detailDishViewManager.handlerHeaderView(list,map);//header
                customerCode= StringManager.getFirstMap(mapTop.get("customer")).get("customerCode");
                if (!TextUtils.isEmpty(customerCode)&&LoginManager.userInfo != null && customerCode.equals(LoginManager.userInfo.get("code"))){
                        state = "";
                }
                detailDishViewManager.handlerTitle(mapTop,code,isHasVideo,mapTop.get("dishState"),loadManager,state);//title导航
                if(isShowVip)detailDishViewManager.initVipView(mapTop.containsKey("type")?mapTop.get("type"):"");
                detailDishViewManager.handlerDishData(list);//菜谱基本信息
                detailDishViewManager.handlerExplainView(mapTop);//小贴士
                requestWeb(mapTop);
                saveDishInfo(mapTop);
                tongjiId_detail=isHasVideo?"a_menu_detail_video":"a_menu_detail_normal";

                break;
            case DetailDishDataManager.DISH_DATA_INGRE://用料
                detailDishViewManager.handlerIngreView(list);
                savaJsAdata(list);
                break;
            case DetailDishDataManager.DISH_DATA_BANNER://banner
                detailDishViewManager.handlerBannerView(list);
                break;
            case DetailDishDataManager.DISH_DATA_STEP://步骤
                if(list!=null&&list.size()>0&&!TextUtils.isEmpty(list.get(0).get("list"))){
                    Map<String,String> mapTemp = list.get(0);
                    maplist.addAll(StringManager.getListMapByJson(mapTemp.get("list")));
                    adapterDishRvListView.setShowDistance(mapTemp.containsKey("isCourseDish")&&"2".equals(mapTemp.get("isCourseDish")));
                }
                detailDishViewManager.handlerStepView(list);
                break;
            case DetailDishDataManager.DISH_DATA_TIE://帖子
                detailDishViewManager.handlerRecommedAndAd(list,code,dishName);
                break;
            case DetailDishDataManager.DISH_DATA_QA://问答
                detailDishViewManager.handlerQAView(list);
                break;
            case DetailDishDataManager.DISH_DATA_RNTIC://技巧
                detailDishViewManager.handlerSkillView(list, code, courseCode, chapterCode, new DishModuleScrollView.onDishModuleClickCallBack() {
                    @Override
                    public void getData(Map<String, String> map) {
                        String url= map.get("appUrl");
                        if(url.contains("xiangha://welcome?"))url=url.replace("xiangha://welcome?","");
                        if(url.contains("?")) {
                            String temp = url.substring(url.indexOf("?")+1,url.length());
                            Map<String,String> map1 = StringManager.getMapByString(temp,"&","=");
                            rvListview.smoothScrollToPosition(0);
                            detailDishDataManager.setDataNew(map1.get("code"),map1.get("courseCode"),map1.get("chapterCode"));
                            detailDishDataManager.reqTopInfo(true);
                        }
                    }
                });
                break;
            case DetailDishDataManager.DISH_DATA_RELATION://公共数据
                Map<String,String> relation= list.get(0);
                detailDishViewManager.handlerUserPowerData(relation);//用户权限
                detailDishViewManager.handlerHoverView(relation,code,dishName);
                if(!isShowVip) {
                    Map<String, String> mapVip = new HashMap<>();
                    mapVip.put("isShow", relation.containsKey("isShow") ? relation.get("isShow") : "");
                    Map<String, String> mapTemp = StringManager.getFirstMap(relation.get("vipButton"));
                    if (mapTemp!=null && mapTemp.size()>0){
                        mapVip.put("text", mapTemp.get("title"));
                        mapVip.put("backColor", mapTemp.get("bgColor"));
                        mapVip.put("textColor", mapTemp.get("color"));
                        mapVip.put("clickUrl", mapTemp.get("url"));
                    }
                    detailDishViewManager.handlerVipView(mapVip);
                }
                showCaipuHint();
                break;
            default:
                break;
        }
        adapterDishRvListView.notifyDataSetChanged();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(detailDishViewManager!=null)detailDishViewManager.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(detailDishViewManager!=null)detailDishViewManager.onPause();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        isHasVideo = false;
        if(detailDishViewManager!=null)detailDishViewManager.handlerLoginStatus();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //反注册。
        ObserverManager.getInstence().unRegisterObserver(ObserverManager.NOTIFY_LOGIN,ObserverManager.NOTIFY_FOLLOW,ObserverManager.NOTIFY_PAYFINISH,ObserverManager.NOTIFY_UPLOADOVER);
        if(detailDishViewManager!=null)detailDishViewManager.onDestroy();
        long nowTime=System.currentTimeMillis();
        if(startTime>0&&(nowTime-startTime)>0&&!TextUtils.isEmpty(data_type)&&!TextUtils.isEmpty(module_type)){
            XHClick.saveStatictisFile("DetailDish",module_type,data_type,code,"","stop",String.valueOf((nowTime-startTime)/1000),"","","","");
        }
        if(handlerScreen!=null){
            handlerScreen.removeCallbacksAndMessages(null);
            handlerScreen=null;
        }
    }
    public void refresh() {
        if(detailDishViewManager!=null)detailDishViewManager.refresh();
    }
    private boolean getStateMakes(ArrayList<Map<String, String>> listdata){
        if(listdata!=null&&listdata.size()>0){
            for(int i=0,size=listdata.size();i<size;i++){
                if(!TextUtils.isEmpty(listdata.get(i).get("img")))
                    return true;
            }
        }
        return false;
    }
    /**
     * 页面限制：显示h5页面，例如：显示一个开通会员页面
     * @param pagePermission
     * @return
     */
    public boolean analyzePagePermissionData(Map<String,String> pagePermission){
        dredgeVipFullLayout = (RelativeLayout)findViewById(R.id.dredge_vip_full_layout);
        if(pagePermission.containsKey("url") && !TextUtils.isEmpty(pagePermission.get("url"))){
            //xhwebView
            WebviewManager manager = new WebviewManager(this,loadManager,true);
            pageXhWebView = manager.createWebView(R.id.XHWebview);
            String url = pagePermission.get("url");
            pageXhWebView.loadUrl(url);
            RelativeLayout bar_title_2 = (RelativeLayout) dredgeVipFullLayout.findViewById(R.id.dish_title_page);
            bar_title_2.findViewById(R.id.back).setOnClickListener(backClickListener);
            bar_title_2.findViewById(R.id.leftClose).setOnClickListener(backClickListener);
            bar_title_2.findViewById(R.id.leftClose).setVisibility(View.VISIBLE);
            dredgeVipFullLayout.setVisibility(View.VISIBLE);
            isShowPowerPermission=true;
            return false;
        }
        if(dredgeVipFullLayout!=null)
            dredgeVipFullLayout.setVisibility(View.GONE);
        return true;
    }
    public void hidePermissionData(){
        if(dredgeVipFullLayout!=null)
            dredgeVipFullLayout.setVisibility(View.GONE);
    }
    private View.OnClickListener backClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.back:
                    XHClick.mapStat(DetailDish.this, tongjiId, "顶部导航栏", "返回点击量");
                    DetailDish.this.finish();
                    break;
                case R.id.leftClose:
                    XHClick.mapStat(DetailDish.this, tongjiId, "顶部导航栏", "关闭点击量");
                    Main.colse_level = 1;
                    DetailDish.this.finish();
                    break;
            }
        }
    };
    private void requestWeb(Map<String,String> map) {
        if(map != null){
            SpecialWebControl.initSpecialWeb(this,rl,"dishInfo",map.get("name"),code);
        }
    }
    private void handleVipState () {
        if (detailDishViewManager != null)
            detailDishViewManager.handleVipState(LoginManager.isVIP());
    }
    @Override
    public void notify(String name, Object sender, Object data) {
        switch (name){
            case ObserverManager.NOTIFY_PAYFINISH://支付
                if(detailDishDataManager!=null&&!isShowPowerPermission)detailDishDataManager.reqQAData();
            case ObserverManager.NOTIFY_LOGIN://登陆
                isShowVip=false;
                refreshTopInfo();
                handleVipState();
            case ObserverManager.NOTIFY_FOLLOW://关注
            case ObserverManager.NOTIFY_UPLOADOVER://问答
                if(detailDishDataManager!=null&&!isShowPowerPermission)detailDishDataManager.reqPublicData();
                break;
        }
    }
    private void refreshTopInfo(){
        if(detailDishDataManager!=null){
            detailDishDataManager.resetTopInfo();
            detailDishDataManager.reqTopInfo(isShowPowerPermission);
        }
    }
    private boolean saveDishInfo = false;
    private boolean isSaveJsData = false;
    private Map<String, String> needSaveDishInfo = new HashMap<>();

    private void saveDishInfo(Map<String,String> map){
        saveDishInfo = true;
        needSaveDishInfo.put("name", map.get("name"));
        needSaveDishInfo.put("img", map.get("img"));
        needSaveDishInfo.put("code", map.get("dishCode"));
        needSaveDishInfo.put("hasVideo", map.get("type"));
        needSaveDishInfo.put("isFine", map.get("isFine"));
        needSaveDishInfo.put("isMakeImg", map.get("isMakeImg"));
        needSaveDishInfo.put("allClick",map.get("allClick"));
        needSaveDishInfo.put("nickName",StringManager.getFirstMap(map.get("customer")).get("nickName"));
        needSaveDishInfo.put("favorites",map.get("favorites"));
        needSaveDishInfo.put("customer", map.get("customer"));
        needSaveDishInfo.put("info", map.get("info"));
        saveHistoryToDB();
    }

    public void savaJsAdata(ArrayList<Map<String,String>> list){
        isSaveJsData = true;
        String burdens="";
        for(Map<String,String> map:list){
            burdens+=map.get("name");
        }
        needSaveDishInfo.put("burdens",burdens);
        saveHistoryToDB();
    }

    /**保存数据到数据库*/
    private synchronized void saveHistoryToDB() {
        if (saveDishInfo && isSaveJsData) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HistoryData data = new HistoryData();
                    data.setBrowseTime(currentTimeMillis());
                    data.setCode(code);
                    data.setDataJson(StringManager.getJsonByMap(needSaveDishInfo).toString());
                    BrowseHistorySqlite sqlite = new BrowseHistorySqlite(XHApplication.in());
                    sqlite.insertSubject(BrowseHistorySqlite.TB_DISH_NAME, data);
                }
            }).start();
        }
    }
    private void showCaipuHint(){
       String hint= (String) FileManager.loadShared(this, FileManager.dish_caipu_hint,FileManager.dish_caipu_hint);
        if(TextUtils.isEmpty(hint)||!"2".equals(hint)){
            findViewById(R.id.dish_show_rela).setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.dish_show_rela).setVisibility(View.GONE);
                    FileManager.saveShared(DetailDish.this, FileManager.dish_caipu_hint,FileManager.dish_caipu_hint,"2");
                }
            },5*1000);
        }
    }
    public void reset(){
        if(detailDishViewManager!=null)detailDishViewManager.handlerLoginStatus();
        if(pageXhWebView!=null)
            pageXhWebView.setVisibility(View.GONE);
    }
}
