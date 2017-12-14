package amodule.dish.view.manager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.tools.StringManager;
import amodule.dish.activity.DetailDish;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import xh.basic.internet.UtilInternet;

/**
 * 菜谱详情页面数据管理者
 */
public class DetailDishDataManager {
    public final static String DISH_DATA_TOP = "dish_top";//topInfo数据类型
    public final static String DISH_DATA_INGRE = "dish_ingre";//用料
    public final static String DISH_DATA_BANNER = "dish_banner";//banner
    public final static String DISH_DATA_STEP = "dish_step";//步骤
    public final static String DISH_DATA_TIE = "dish_tie";//用户信息
    public final static String DISH_DATA_QA = "dish_qa";//问答
    public final static String DISH_DATA_RELATION = "dish_relation";//菜谱公共数据接口
    public final static String DISH_DATA_RNTIC = "dish_rntic";//菜谱小技巧
    private String dishCode;//菜谱code
    private Context mContext ;
    private String customerCode;//用户code
    //权限
    private Map<String,String> permissionMap = new HashMap<>();
    private Map<String,String> detailPermissionMap = new HashMap<>();
    private String lastPermission = "";
    private boolean hasPermission = true;
    private boolean contiunRefresh = true;
    private boolean loadOver = false;
    private DetailDish detailAct;
    private String courseCode,chapterCode;

    public DetailDishDataManager(String code, DetailDish detailAct,String courseCode,String chapterCode) {
        dishCode = code;
        this.courseCode= courseCode;
        this.chapterCode= chapterCode;
        this.detailAct = detailAct;
        mContext=detailAct.getApplicationContext();
        resetData();
        reqTopInfo(true);
    }
    public void setDataNew(String code,String courseCode,String chapterCode){
        this.dishCode = code;
        this.courseCode= courseCode;
        this.chapterCode= chapterCode;
        resetData();
    }
    //重置权限数据
    private void resetData(){
        loadOver = false;
        hasPermission = true;
        contiunRefresh = true;
        detailPermissionMap.clear();
        permissionMap.clear();
    }

    /**
     * 重置处理数据
     */
    public void resetTopInfo(){
        if(detailAct!=null)detailAct.hidePermissionData();
        detailPermissionMap.clear();
        permissionMap.clear();
    }
    /**
     * 第一次请求接口合集
     */
    public void reqOne() {
        reqPublicData();
        reqIngre();
        reqBanner();
        reqAnticData();
        reqTwo();
    }

    public void reqTwo() {
        reqStep();
        reqQAData();
    }

    /**
     * 请求topInfo数据---第一请求，有权限请求
     * boolean isGon
     */
    public void reqTopInfo(boolean isGon) {
        String params = "dishCode=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_MAIN8_TOPINFP, getOtherCode(params), new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object object) {
                if(flag>=UtilInternet.REQ_OK_STRING) {
                    if(!hasPermission || !contiunRefresh) return;
                    detailAct.reset();
                    customerCode= StringManager.getFirstMap(object).get("customerCode");
                    if (!TextUtils.isEmpty(object.toString()) && !object.toString().equals("[]")) {
                        Map<String,String> mapTemp = StringManager.getFirstMap(object);
                        if(mapTemp!=null&&mapTemp.containsKey("dishState")&&"4".equals(mapTemp.get("dishState"))){
                            if(detailAct!=null)detailAct.finish();
                            return;
                        }
                        handleDataSuccess(flag, DISH_DATA_TOP, object);
                        if(isGon)reqOne();
                        Map<String,String> maps= StringManager.getFirstMap(object);
                        if(maps.containsKey("isHide")&&!"2".equals(maps.get("isHide"))){
                            reqOtherTieData();
                        }
                    }

                }
            }

            @Override
            public void getPower(int flag, String url, Object obj) {
                //权限检测
                if(TextUtils.isEmpty((String)obj) || "[]".equals(obj)||"{}".equals(obj))hasPermission=true;
                if(permissionMap.isEmpty() && !TextUtils.isEmpty((String)obj) && !"[]".equals(obj)&& !"{}".equals(obj)){
                    if(TextUtils.isEmpty(lastPermission)){
                        lastPermission = (String) obj;
                    }else{
                        contiunRefresh = !lastPermission.equals(obj.toString());
                        if(contiunRefresh)
                            lastPermission = obj.toString();
                    }
                    permissionMap = StringManager.getFirstMap(obj);
                    if(permissionMap.containsKey("page")){
                        Map<String,String> pagePermission = StringManager.getFirstMap(permissionMap.get("page"));
                        hasPermission = detailAct.analyzePagePermissionData(pagePermission);
                        if(!hasPermission) return;
                    }
                    if(permissionMap.containsKey("detail"))
                        detailPermissionMap = StringManager.getFirstMap(permissionMap.get("detail"));
                }else if(loadOver && TextUtils.isEmpty(lastPermission)){
                    contiunRefresh = false;
                }
            }
        });
    }

    /**
     * 请求用料数据
     */
    private void reqIngre() {
        String params = "code=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_GETDISHBURDENBYCODE, getOtherCode(params), new InternetCallback(mContext) {
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
        String params = "dishCode=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_MAIN8_DISHMAKE, getOtherCode(params), new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String url, Object object) {
                handleDataSuccess(flag,DISH_DATA_STEP,object);
            }
        });
    }

    /**
     * 请求帖子数据
     */
    private void reqOtherTieData(){
        String params = "dishCode=" + dishCode;
        //获取帖子数据
        ReqEncyptInternet.in().doEncypt(StringManager.API_MAIN8_TIEINFO,getOtherCode(params), new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String s, Object object) {
                handleDataSuccess(flag,DISH_DATA_TIE,object);
            }
        });
    }
    /**
     * 请求问答数据
     */
    public void reqQAData(){
        String params = "dishCode=" + dishCode;
        //获取帖子数据
        ReqEncyptInternet.in().doEncypt(StringManager.API_MAIN8_QAINFO,getOtherCode(params), new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String s, Object object) {
                handleDataSuccess(flag,DISH_DATA_QA,object);
            }
        });
    }
    /**
     * 请求小技巧
     */
    public void reqAnticData(){
        String params = "dishCode=" + dishCode;
        //获取帖子数据
        ReqEncyptInternet.in().doEncypt(StringManager.API_MAIN8_ANTIC,getOtherCode(params), new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String s, Object object) {
                handleDataSuccess(flag,DISH_DATA_RNTIC,object);
            }
        });
    }

    /**
     * 公共接口数据
     */
    public void reqPublicData(){
        String params = "dishCode=" + dishCode;
        //获取点赞数据
        ReqEncyptInternet.in().doEncypt(StringManager.API_MAIN8_RELATIONBYCODE, getOtherCode(params), new InternetCallback(mContext) {
            @Override
            public void loaded(int flag, String s, Object object) {
                handleDataSuccess(flag,DISH_DATA_RELATION,object);
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
            dishDataCallBack.handlerTypeData(type, list,type.equals(DISH_DATA_TOP)?detailPermissionMap:null);
        }
    }

    public DishDataCallBack dishDataCallBack;
    /**
     * 接口请求数据回调
     */
    public interface DishDataCallBack {
        public void handlerTypeData(String type, ArrayList<Map<String,String>> list,Map<String,String> permissionMap);
    }
    public void setDishDataCallBack(DishDataCallBack callBack) {
        dishDataCallBack = callBack;
    }
    private String getOtherCode(@NonNull String params){
        if(!TextUtils.isEmpty(courseCode))params+="&courseCode="+courseCode;
        if(!TextUtils.isEmpty(chapterCode))params+="&chapterCode="+chapterCode;
        return params;
    }
}
