package amodule.dish.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ListView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.adapter.AdapterDishNew;
import amodule.dish.db.DataOperate;
import amodule.dish.view.manager.DetailDishDataManager;
import amodule.dish.view.manager.DetailDishViewManager;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * 菜谱详情页原生标准
 */
public class DetailDishNew extends BaseAppCompatActivity {
    private String data_type = "";
    private String module_type = "";
    private String img = "";//预加载图片
    public String code, dishTitle, state;//页面开启状态所必须的数据。
    private String courseCode;//课程分类
    private String chapterCode;//章节分类
    public static long startTime = 0;
    private Handler handlerScreen;
    private ListView listView;
    private DetailDishViewManager detailDishViewManager;//view控制器
    private DetailDishDataManager detailDishDataManager;//数据控制器
    private AdapterDishNew adapterDishNew;
    private ArrayList<Map<String,String>> maplist = new ArrayList<>();


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
        if (detailDishViewManager == null) detailDishViewManager = new DetailDishViewManager(this,listView);
        if (detailDishDataManager == null) detailDishDataManager = new DetailDishDataManager(code);
        detailDishDataManager.setDishDataCallBack(new DetailDishDataManager.DishDataCallBack() {
            @Override
            public void handlerTypeData(String type, ArrayList<Map<String,String>> list) {
                dishTypeData(type,list);
            }
        });
    }
    private void dishTypeData(String type,ArrayList<Map<String,String>> list){
        switch (type){
            case DetailDishDataManager.DISH_DATA_TOP:
                detailDishViewManager.handlerHeaderView(list,null);
                break;
            case DetailDishDataManager.DISH_DATA_BASE:
                detailDishViewManager.handlerDishData(list);
                break;
            case DetailDishDataManager.DISH_DATA_USER:
                detailDishViewManager.handlerUserData(list);
                break;
            case DetailDishDataManager.DISH_DATA_INGRE:
                detailDishViewManager.handlerIngreView(list);
                break;
            case DetailDishDataManager.DISH_DATA_BANNER:
                break;
            case DetailDishDataManager.DISH_DATA_STEP://步骤
                maplist.addAll(list);
                break;
            default:
                break;
        }
        adapterDishNew.notifyDataSetChanged();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void refresh() {
    }

    public void handleData() {

    }
}
