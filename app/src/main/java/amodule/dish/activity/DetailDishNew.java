package amodule.dish.activity;

import android.os.Bundle;

import acore.override.activity.base.BaseAppCompatActivity;

/**
 * 菜谱详情页原生标准
 */
public class DetailDishNew extends BaseAppCompatActivity{
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
