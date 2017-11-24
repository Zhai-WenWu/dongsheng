package amodule.dish.activity;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
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
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.adapter.AdapterDishNew;
import amodule.dish.db.DataOperate;
import amodule.dish.view.manager.DetailDishDataManager;
import amodule.dish.view.manager.DetailDishViewManager;
import amodule.main.Main;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;

import static amodule.dish.activity.DetailDish.tongjiId;

/**
 * 菜谱详情页原生标准
 */
public class DetailDishNew extends BaseAppCompatActivity implements IObserver {
    private String data_type = "";
    private String module_type = "";
    private String img = "";//预加载图片
    public String code, dishTitle, state,dishName;//页面开启状态所必须的数据。
    private String courseCode;//课程分类
    private String chapterCode;//章节分类
    public static long startTime = 0;
    private Handler handlerScreen;
    private ListView listView;
    private DetailDishViewManager detailDishViewManager;//view控制器
    private DetailDishDataManager detailDishDataManager;//数据控制器
    private AdapterDishNew adapterDishNew;
    private ArrayList<Map<String,String>> maplist = new ArrayList<>();
    private Map<String,String> mapTop = new HashMap<>();
    private boolean isHasVideo;//当前是否是视频
    private String customerCode;
    private RelativeLayout dredgeVipFullLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            courseCode = bundle.getString("courseCode", "");
            chapterCode = bundle.getString("chapterCode", "");
            if (dishTitle == null) dishTitle = "香哈菜谱";
            state = bundle.getString("state");
            data_type = bundle.getString("data_type");
            module_type = bundle.getString("module_type");
            img = bundle.getString("img");
            //保存历史记录
            DataOperate.saveHistoryCode(code);
        }
        if (TextUtils.isEmpty(code)) {
            Tools.showToast(getApplicationContext(), "抱歉，未找到相应菜谱");
            this.finish();
            return;
        }
        //保持高亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        handlerScreen = new Handler();
        handlerScreen.postDelayed(new Runnable() {
            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }, 15 * 60 * 1000);
        //sufureView页面闪烁
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        XHClick.track(XHApplication.in(), "浏览菜谱详情页");
        ObserverManager.getInstence().registerObserver(this,ObserverManager.NOTIFY_LOGIN,ObserverManager.NOTIFY_FOLLOW,ObserverManager.NOTIFY_PAYFINISH);
    }

    /**
     * 处理页面Ui
     */
    private void initView() {
        initActivity("", 2, 0, 0, R.layout.a_detail_dish);
        listView = (ListView) findViewById(R.id.listview);
    }

    /**
     * 处理页面Ui
     */
    private void initData() {
        adapterDishNew = new AdapterDishNew(listView,maplist);
        listView.setAdapter(adapterDishNew);
        if (detailDishViewManager == null) {
            detailDishViewManager = new DetailDishViewManager(this, listView, state);
            detailDishViewManager.initBeforeData(img);
        }

        if (detailDishDataManager == null) detailDishDataManager = new DetailDishDataManager(code,this);
        detailDishDataManager.setDishDataCallBack(new DetailDishDataManager.DishDataCallBack() {
            @Override
            public void handlerTypeData(String type, ArrayList<Map<String,String>> list,Map<String,String> PermissionMap) {
                dishTypeData(type,list,PermissionMap);
            }
        });
        adapterDishNew.setClickCallBack(new AdapterDishNew.ItemOnClickCallBack() {
            @Override
            public void onClickPosition(int position) {
                if(!getStateMakes(maplist)){//无图时不执行
                    return;
                }
                XHClick.mapStat(DetailDishNew.this, tongjiId, "菜谱区域的点击", "步骤图点击");
                Intent intent = new Intent(DetailDishNew.this, MoreImageShow.class);
                ArrayList<Map<String, String>> listdata = new ArrayList<Map<String, String>>();
                listdata.addAll(maplist);
                if (!TextUtils.isEmpty(mapTop.get("remark"))) {
                    Map<String, String> map_temp = new HashMap<String, String>();
                    if(mapTop != null){
                        map_temp.put("img", mapTop.get("img"));
                    }else{
                        map_temp.put("img", "");
                    }
                    map_temp.put("info", "小贴士：\n" + mapTop.get("remark"));
                    map_temp.put("num", String.valueOf(maplist.size() + 1));
                    listdata.add(map_temp);
                }
                intent.putExtra("data", listdata);
                intent.putExtra("index", position);
                intent.putExtra("key", tongjiId);
                DetailDishNew.this.startActivity(intent);
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
                detailDishViewManager.handlerHoverViewCode(code);
                customerCode= StringManager.getFirstMap(mapTop.get("customer")).get("customerCode");
                if (!TextUtils.isEmpty(customerCode)&&LoginManager.userInfo != null && customerCode.equals(LoginManager.userInfo.get("code"))){
                        state = "";
                }
                detailDishViewManager.handlerTitle(mapTop,code,isHasVideo,mapTop.get("dishState"),loadManager,state);//title导航
                detailDishViewManager.handlerDishData(list);//菜谱基本信息
                detailDishViewManager.handlerExplainView(list);//小贴士
                requestWeb(mapTop);
                break;
            case DetailDishDataManager.DISH_DATA_INGRE://用料
                detailDishViewManager.handlerIngreView(list);
                break;
            case DetailDishDataManager.DISH_DATA_BANNER://banner
                detailDishViewManager.handlerBannerView(list);
                break;
            case DetailDishDataManager.DISH_DATA_STEP://步骤
                if(list!=null&&list.size()>0&&!TextUtils.isEmpty(list.get(0).get("list")))maplist.addAll(StringManager.getListMapByJson(list.get(0).get("list")));
                detailDishViewManager.handlerStepView(list);
                break;
            case DetailDishDataManager.DISH_DATA_TIE://帖子
                detailDishViewManager.handlerRecommedAndAd(list,code,dishName);
            case DetailDishDataManager.DISH_DATA_QA://问答
                detailDishViewManager.handlerQAView(list);
            case DetailDishDataManager.DISH_DATA_LIKE://点赞
                detailDishViewManager.handlerHoverViewLike(list);
                break;
            default:
                break;
        }
        adapterDishNew.notifyDataSetChanged();
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
    protected void onDestroy() {
        super.onDestroy();
        //反注册。
        ObserverManager.getInstence().unRegisterObserver(ObserverManager.NOTIFY_LOGIN,ObserverManager.NOTIFY_FOLLOW,ObserverManager.NOTIFY_PAYFINISH);
        if(detailDishViewManager!=null)detailDishViewManager.onDestroy();
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
            XHWebView pageXhWebView = manager.createWebView(R.id.XHWebview);
            String url = pagePermission.get("url");
            pageXhWebView.loadUrl(url);
            RelativeLayout bar_title_2 = (RelativeLayout) dredgeVipFullLayout.findViewById(R.id.dish_title_page);
            bar_title_2.findViewById(R.id.back).setOnClickListener(backClickListener);
            bar_title_2.findViewById(R.id.leftClose).setOnClickListener(backClickListener);
            bar_title_2.findViewById(R.id.leftClose).setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bar_title_2.getLayoutParams();
            int statusBarHeight = Tools.getStatusBarHeight(this);
            layoutParams.height = Tools.getDimen(this,R.dimen.dp_45) + statusBarHeight;
            View title_state_bar_page = findViewById(R.id.title_state_bar_page);
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
                    XHClick.mapStat(DetailDishNew.this, tongjiId, "顶部导航栏", "返回点击量");
                    DetailDishNew.this.finish();
                    break;
                case R.id.leftClose:
                    XHClick.mapStat(DetailDishNew.this, tongjiId, "顶部导航栏", "关闭点击量");
                    Main.colse_level = 1;
                    DetailDishNew.this.finish();
                    break;
            }
        }
    };
    private void requestWeb(Map<String,String> map) {
        if(map != null){
            SpecialWebControl.initSpecialWeb(this,rl,"dishInfo",map.get("name"),code);
        }
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        switch (name){
            case ObserverManager.NOTIFY_LOGIN://登陆

                break;
            case ObserverManager.NOTIFY_FOLLOW://关注
                break;
            case ObserverManager.NOTIFY_PAYFINISH://支付
                break;
        }
    }
}
