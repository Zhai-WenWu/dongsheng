package amodule.dish.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.SpecialWebControl;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.FileManager;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.db.DataOperate;
import amodule.dish.view.DishActivityViewControlNew;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.video.VideoPlayerController;

/**
 * 菜谱详情页：头部大图、视频，底部广告以下是原生，中间是h5
 */
public class DetailDish extends BaseAppCompatActivity implements IObserver {
    public static String tongjiId = "a_menu_detail_normal430";//统计标示
    public static String DishName="amodule.dish.activity.DetailDish";
    private final int LOAD_DISH = 1;
    private final int LOAD_DISH_OVER = 2;

    private DishActivityViewControlNew dishActivityViewControl;//view处理控制

    private Map<String,String> permissionMap = new HashMap<>();
    private Map<String,String> detailPermissionMap = new HashMap<>();
    private int statusBarHeight = 0;//广告所用bar高度
    private String dishJson;
    public String code, dishTitle, state;//页面开启状态所必须的数据。
    private String imgLevel = FileManager.save_cache;//图片缓存机制---是离线菜谱改变其缓存机制
    public boolean isHasVideo = false;//是否显示视频数据
    private boolean hasPermission = true;
    private boolean contiunRefresh = true;
    private String lastPermission = "";

    public static long startTime= 0;
    private String data_type="";
    private String module_type="";
    private int height;
    private String img = "";
    private Handler handlerScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        super.onCreate(savedInstanceState);
        //处理广告
        Bundle bundle = getIntent().getExtras();
        startTime= System.currentTimeMillis();
        // 正常调用
        if (bundle != null) {
            code = bundle.getString("code");
            dishTitle = bundle.getString("name");
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
            DetailDish.this.finish();
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
        Log.i("zyj","activity:::"+(System.currentTimeMillis()-startTime));
        init();
        XHClick.track(XHApplication.in(), "浏览菜谱详情页");
        //注册监听
        ObserverManager.getInstence().registerObserver(this,ObserverManager.NOTIFY_LOGIN,ObserverManager.NOTIFY_FOLLOW,ObserverManager.NOTIFY_PAYFINISH);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isHasVideo = false;
        detailPermissionMap.clear();
        permissionMap.clear();
        dishActivityViewControl.setLoginStatus();
        loadDishInfo();
    }

    /**
     * 数据的初始化
     */
    private void init() {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.a_dish_detail_new);
        level = 2;
        if(Tools.isShowTitle()){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setCommonStyle();
        dishActivityViewControl= new DishActivityViewControlNew(this);
        dishActivityViewControl.init(state, loadManager, code, new DishActivityViewControlNew.DishViewCallBack() {
            @Override
            public void getVideoPlayerController(VideoPlayerController mVideoPlayerController) {
            }
        });
        //优先处理：img
        if(!TextUtils.isEmpty(img)) {
            dishActivityViewControl.setDishOneView(img);
        }
        loadManager.setLoading(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDishInfo();
                loadOtherData();
            }
        });
    }

    /**
     * 请求网络
     */
    private void loadDishInfo() {
        String params = "code=" + code;
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishTopInfo,params, new InternetCallback(this.getApplicationContext()) {

            @Override
            public void getPower(int flag, String url, Object obj) {
                //权限检测
                if(permissionMap.isEmpty() && !TextUtils.isEmpty((String)obj) && !"[]".equals(obj)){
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
                        hasPermission = dishActivityViewControl.analyzePagePermissionData(pagePermission);
                        if(!hasPermission) return;
                    }
                    if(permissionMap.containsKey("detail"))
                        detailPermissionMap = StringManager.getFirstMap(permissionMap.get("detail"));
                }
            }
            @Override
            public void loaded(int flag, String s, Object o) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    if(!hasPermission || !contiunRefresh) return;
                    dishActivityViewControl.reset();
                    if (!TextUtils.isEmpty(o.toString()) && !o.toString().equals("[]")) {
                        analyzeData(String.valueOf(o),detailPermissionMap);
                    } else {
                        loadManager.loadOver(flag, 1, true);
                    }
                }
                if(ToolsDevice.isNetworkAvailable(context)|| !LoadImage.SAVE_LONG.equals(imgLevel)){
                    loadManager.loadOver(flag, 1, true);
                }else loadManager.hideProgressBar();
            }
        });
    }

    /**
     * 请求其他接口数据
     */
    private void loadOtherData(){
        String params = "code=" + code;
        //获取帖子数据
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishTieInfo,params, new InternetCallback(DetailDish.this.getApplicationContext()) {
            @Override
            public void loaded(int i, String s, Object o) {
                if(i >= ReqInternet.REQ_OK_STRING){
                    dishActivityViewControl.analyzeUserShowDishInfoData(String.valueOf(o));
                }
            }
        });

        //获取点赞数据
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishLikeNumStatus, params, new InternetCallback(DetailDish.this.getApplicationContext()) {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING){
                    dishActivityViewControl.analyzeDishLikeNumberInfoData(String.valueOf(o));
                }
            }
        });

        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishstatusValue, params, new InternetCallback(DetailDish.this.getApplicationContext()) {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING){
                    saveApiData(o.toString());
                }
            }
        });
    }

    /**
     * 处理业务数据
     * @param data
     */
    private void analyzeData(String data,Map<String,String> permissionMap) {
        ArrayList<Map<String, String>> list = StringManager.getListMapByJson(data);
        //第一页未请求到数据，直接关闭改页面
        if (list.size() < 1) {
            Tools.showToast(getApplicationContext(), "抱歉，未找到相应菜谱");
            DetailDish.this.finish();
            return;
        }
        requestWeb(data);
        dishActivityViewControl.analyzeDishInfoData(data,permissionMap);
    }

    private void requestWeb(String dishJson) {
        Map<String,String> dishInfo = StringManager.getFirstMap(dishJson);
        if(dishInfo != null){
            SpecialWebControl.initSpecialWeb(this,rl,"dishInfo",dishInfo.get("name"),code);
        }
    }
    //**********************************************Activity生命周期方法**************************************************
    @Override
    public void onBackPressed() {
        mFavePopWindowDialog=dishActivityViewControl.getDishTitleViewControl().getPopWindowDialog();
        if(dishActivityViewControl != null && dishActivityViewControl.onBackPressed()){
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        Log.i("zyj","onResume::"+(System.currentTimeMillis()-startTime));
        mFavePopWindowDialog=dishActivityViewControl.getDishTitleViewControl().getPopWindowDialog();
        super.onResume();
        Rect outRect = new Rect();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
        statusBarHeight = outRect.top;
        if(dishActivityViewControl != null){
            dishActivityViewControl.onResume();
        }
    }

    @Override
    protected void onPause() {
        Log.i("DetailDishActivity","onPause()");
        mFavePopWindowDialog=dishActivityViewControl.getDishTitleViewControl().getPopWindowDialog();
        super.onPause();
        if(dishActivityViewControl != null){
            dishActivityViewControl.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //反注册。
        ObserverManager.getInstence().unRegisterObserver(ObserverManager.NOTIFY_LOGIN,ObserverManager.NOTIFY_FOLLOW,ObserverManager.NOTIFY_PAYFINISH);
        if(dishActivityViewControl != null){
            dishActivityViewControl.onDestroy();
            dishActivityViewControl=null;
            System.gc();
        }
        long nowTime=System.currentTimeMillis();
        if(startTime>0&&(nowTime-startTime)>0&&!TextUtils.isEmpty(data_type)&&!TextUtils.isEmpty(module_type)){
            XHClick.saveStatictisFile("DetailDish",module_type,data_type,code,"","stop",String.valueOf((nowTime-startTime)/1000),"","","","");
        }
        if(handlerScreen!=null){
            handlerScreen.removeCallbacksAndMessages(null);
            handlerScreen=null;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (dishActivityViewControl == null)
            return;
        if(requestCode==10000&& resultCode== Activity.RESULT_OK){
            Map<String, String> dishInfoMap = dishActivityViewControl.getDishInfoMap();
            if (dishInfoMap == null)
                return;
            String subjectCode=dishInfoMap.get("subjectCode");
            String url="subjectInfo.app?code=" + subjectCode + "&title=" + dishInfoMap.get("name");
            AppCommon.openUrl(this,url,true);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i("zyj","onWindowFocusChanged::"+(System.currentTimeMillis()-startTime));
    }

    /**
     * 保存来自Api的数据
     * @param dataStr
     */
    public void saveApiData(String dataStr){
        if(dishActivityViewControl != null)
            dishActivityViewControl.saveApiData(dataStr);
    }

    public void savaJsAdata(String burden,String allClick,String favrites,String nickName){
        if(dishActivityViewControl != null)
            dishActivityViewControl.savaJsAdata(burden,allClick,favrites,nickName);
    }

    /**
     * 监听回调
     * @param name
     * @param sender
     * @param data
     */
    @Override
    public void notify(String name, Object sender, Object data) {
        switch (name){
            case ObserverManager.NOTIFY_LOGIN://登陆
                if(dishActivityViewControl!=null) {
                    dishActivityViewControl.refreshTemplateWebView();
                    dishActivityViewControl.refreshAskStatus();
                    dishActivityViewControl.refreshQaWebView();
                }
                break;
            case ObserverManager.NOTIFY_FOLLOW://关注
                if(dishActivityViewControl!=null) {
                    dishActivityViewControl.refreshTemplateWebView();
                }
                break;
            case ObserverManager.NOTIFY_PAYFINISH://支付
                if(dishActivityViewControl!=null) {
                    dishActivityViewControl.refreshQaWebView();
                }
                break;
        }
    }

    /**
     * 获取图片链接
     * @return
     */
    public String getImg(){
        return img;
    }
}
