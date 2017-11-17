package amodule.dish.activity;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ListView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.Tools;
import amodule.dish.adapter.AdapterDishNew;
import amodule.dish.db.DataOperate;
import amodule.dish.view.manager.DetailDishDataManager;
import amodule.dish.view.manager.DetailDishViewManager;

import static amodule.dish.activity.DetailDish.tongjiId;

/**
 * 菜谱详情页原生标准
 */
public class DetailDishNew extends BaseAppCompatActivity {
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
    private Map<String,String> mapBase = new HashMap<>();
    private boolean isHasVideo;//当前是否是视频
    private String customerCode;

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

        if (detailDishDataManager == null) detailDishDataManager = new DetailDishDataManager(code);
        detailDishDataManager.setDishDataCallBack(new DetailDishDataManager.DishDataCallBack() {
            @Override
            public void handlerTypeData(String type, ArrayList<Map<String,String>> list) {
                dishTypeData(type,list);
            }
        });
        adapterDishNew.setClickCallBack(new AdapterDishNew.ItemOnClickCallBack() {
            @Override
            public void onClickPosition(int position) {
                if(!getStateMakes(maplist)){//无图时不执行
                    return;
                }
//                if(!isOnClickImageShow){
//                    isOnClickImageShow=true;
//                    ++showNumLookImage;
//                }
                XHClick.mapStat(DetailDishNew.this, tongjiId, "菜谱区域的点击", "步骤图点击");
                Intent intent = new Intent(DetailDishNew.this, MoreImageShow.class);
                ArrayList<Map<String, String>> listdata = new ArrayList<Map<String, String>>();
                listdata.addAll(maplist);
                if (!TextUtils.isEmpty(mapBase.get("remark"))) {
                    Map<String, String> map_temp = new HashMap<String, String>();
                    if(mapTop != null){
                        map_temp.put("img", mapTop.get("img"));
                    }else{
                        map_temp.put("img", "");
                    }
                    map_temp.put("info", "小贴士：\n" + mapBase.get("remark"));
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
    private void dishTypeData(String type,ArrayList<Map<String,String>> list){
        switch (type){
            case DetailDishDataManager.DISH_DATA_TOP:
                mapTop= list.get(0);
                dishName= mapTop.get("name");
                isHasVideo = "2".equals(mapTop.get("type"));
                detailDishViewManager.handlerHeaderView(list,null);
                detailDishViewManager.handlerHoverViewCode(code);
                customerCode= mapTop.get("customerCode");
                if (!TextUtils.isEmpty(customerCode)&&LoginManager.userInfo != null && customerCode.equals(LoginManager.userInfo.get("code"))){
                        state = "";
                }
                detailDishViewManager.handlerTitle(mapTop,code,isHasVideo,mapTop.get("dishState"),loadManager,state);
                break;
            case DetailDishDataManager.DISH_DATA_BASE:
                mapBase = list.get(0);
                detailDishViewManager.handlerDishData(list);
                detailDishViewManager.handlerExplainView(list);
                break;
            case DetailDishDataManager.DISH_DATA_USER:
                detailDishViewManager.handlerUserData(list);
                detailDishViewManager.handlerTitleName(list.get(0).get("nickName"));
                break;
            case DetailDishDataManager.DISH_DATA_INGRE:
                detailDishViewManager.handlerIngreView(list);
                break;
            case DetailDishDataManager.DISH_DATA_BANNER:
                detailDishViewManager.handlerBannerView(list);
                break;
            case DetailDishDataManager.DISH_DATA_STEP://步骤
                maplist.addAll(list);
                break;
            case DetailDishDataManager.DISH_DATA_TIE:
                detailDishViewManager.handlerRecommedAndAd(list,code,dishName);
            case DetailDishDataManager.DISH_DATA_LIKE:
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
}
