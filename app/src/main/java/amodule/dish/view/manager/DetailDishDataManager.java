package amodule.dish.view.manager;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Map;

import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import xh.basic.internet.UtilInternet;

/**
 * 菜谱详情页面数据管理者
 */
public class DetailDishDataManager {
    public final static String DISH_DATA_TOP = "dish_top";//topInfo数据类型
    public final static String DISH_DATA_BASE = "dish_base";//菜谱基础数据类型接口
    public final static String DISH_DATA_INGRE = "dish_ingre";//用料
    public final static String DISH_DATA_BANNER = "dish_banner";//banner
    public final static String DISH_DATA_STEP = "dish_step";//步骤
    public final static String DISH_DATA_USER = "dish_user";//用户信息
    private String dishCode;//菜谱code
    private Context mContext = XHActivityManager.getInstance().getCurrentActivity().getApplicationContext();

    public DetailDishDataManager(String code) {
        dishCode = code;
        reqOne();
        reqTwo();
    }
    /**
     * 第一次请求接口合集
     */
    public void reqOne() {
        reqTopInfo();
        reqDishBase();
        reqIngre();
        reqBanner();
    }

    public void reqTwo() {
        reqStep();
    }

    /**
     * 请求topInfo数据
     */
    private void reqTopInfo() {
        String params = "code=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishTopInfo, params, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object object) {
                handleDataSuccess(flag,DISH_DATA_TOP,object);
            }
        });
    }

    /**
     * 请求菜谱基本信息
     */
    private void reqDishBase() {
        String params = "code=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_GETDISHBASICINFOBYCODE, params, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object object) {
                handleDataSuccess(flag,DISH_DATA_BASE,object);
            }
        });
    }
    /**
     * 请求用户信息
     */
    private void reqDishUser(String userCode) {
        String params = "code=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_GETUSERINFOBYCODE, params, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object object) {
                handleDataSuccess(flag,DISH_DATA_USER,object);
            }
        });
    }
    /**
     * 请求用料数据
     */
    private void reqIngre() {
        String params = "code=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_GETDISHBURDENBYCODE, params, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object object) {
                handleDataSuccess(flag,DISH_DATA_INGRE,object);
            }
        });
    }

    /**
     * 请求banner数据
     */
    private void reqBanner() {
        String params = "code=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_GETBELOWBURDENBANNER, params, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object object) {
                handleDataSuccess(flag,DISH_DATA_BANNER,object);
            }
        });
    }

    /**
     * 请求步骤数据
     */
    private void reqStep() {
        String params = "code=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_getDishMakeByCode, params, new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object object) {
                handleDataSuccess(flag,DISH_DATA_STEP,object);
            }
        });
    }
    /**
     * 集中处理接口返回数据
     * @param flag
     * @param type
     * @param object
     */
    public void handleDataSuccess(int flag, String type,Object object){
        if (flag >= UtilInternet.REQ_OK_STRING && dishDataCallBack != null) {
            ArrayList<Map<String,String>> list=StringManager.getListMapByJson(object);
            if(type.equals(DISH_DATA_BASE)){
                String customerCode= list.get(0).containsKey("customerCode")&& !TextUtils.isEmpty(list.get(0).get("customerCode"))?list.get(0).get("customerCode"):"";
            }
            if(list.size()>0)dishDataCallBack.handlerTypeData(type,list);
        }
    }

    public DishDataCallBack dishDataCallBack;
    /**
     * 接口请求数据回调
     */
    public interface DishDataCallBack {
        public void handlerTypeData(String type, ArrayList<Map<String,String>> list);
    }
    public void setDishDataCallBack(DishDataCallBack callBack) {
        dishDataCallBack = callBack;
    }
}
