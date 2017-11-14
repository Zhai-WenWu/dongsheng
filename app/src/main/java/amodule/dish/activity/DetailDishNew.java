package amodule.dish.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.Tools;
import amodule.dish.db.DataOperate;
import amodule.main.Main;

/**
 * 菜谱详情页原生标准
 */
public class DetailDishNew extends BaseAppCompatActivity{
    private String data_type="";
    private String module_type="";
    private String img = "";//预加载图片
    public String code, dishTitle, state;//页面开启状态所必须的数据。
    private String courseCode;//课程分类
    private String chapterCode;//章节分类
    public static long startTime= 0;
    private Handler handlerScreen;
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
        startTime= System.currentTimeMillis();
        if (bundle != null) {
            code = bundle.getString("code");
            dishTitle = bundle.getString("name");
            courseCode = bundle.getString("courseCode","");
            chapterCode = bundle.getString("chapterCode","");
            if (dishTitle == null) dishTitle = "香哈菜谱";
            state = bundle.getString("state");
            data_type=bundle.getString("data_type");
            module_type=bundle.getString("module_type");
            img=bundle.getString("img");
            //保存历史记录
            DataOperate.saveHistoryCode(code);
        }
        if(TextUtils.isEmpty(code)){
            Tools.showToast(getApplicationContext(), "抱歉，未找到相应菜谱");
            this.finish();
            return;
        }
        //保持高亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        handlerScreen=new Handler();
        handlerScreen.postDelayed(new Runnable() {
            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        },15 * 60 * 1000);
        //sufureView页面闪烁
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        XHClick.track(XHApplication.in(), "浏览菜谱详情页");
    }
    /**
     * 处理页面Ui
     */
    private void initView() {

    }
    /**
     * 处理页面Ui
     */
    private void initData() {
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

    public void refresh(){
    }

    /**
     * 处理第一屏请求接口
     */
    public void setRequestOne(){

    }
    /**
     * 处理第一屏请求接口
     */
    public void setRequestTwo(){

    }
    public void handleData(){

    }
}
