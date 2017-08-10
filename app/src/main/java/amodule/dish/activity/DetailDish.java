package amodule.dish.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.xiangha.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.SpecialWebControl;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.db.DataOperate;
import amodule.dish.tools.ADDishContorl;
import amodule.dish.view.DishActivityViewControlNew;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.video.VideoPlayerController;
import xh.basic.tool.UtilString;

import static com.xiangha.R.id.share_layout;
import static xh.basic.tool.UtilString.getListMapByJson;

/**
 * 菜谱详情页：头部大图、视频，底部广告以下是原生，中间是h5
 */
public class DetailDish extends BaseAppCompatActivity {
    public static String tongjiId = "a_menu_detail_normal430";//统计标示
    private final int LOAD_DISH = 1;
    private final int LOAD_DISH_OVER = 2;

    private DishActivityViewControlNew dishActivityViewControl;//view处理控制

    private Handler mHandler;
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

    private long startTime= 0;
    private String data_type="";
    private String module_type="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //处理广告
        Bundle bundle = getIntent().getExtras();
        // 正常调用
        if (bundle != null) {
            code = bundle.getString("code");
            dishTitle = bundle.getString("name");
            if (dishTitle == null) dishTitle = "香哈菜谱";
            state = bundle.getString("state");
            data_type=bundle.getString("data_type");
            module_type=bundle.getString("module_type");
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        },5 * 60 * 1000);
        //sufureView页面闪烁
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        init();
        XHClick.track(XHApplication.in(), "浏览菜谱详情页");
        startTime= System.currentTimeMillis();
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
        initData();
        loadManager.setLoading(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadHistoryData();
            }
        });
    }

    /**延迟handler数据回调     */
    private void initData() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case LOAD_DISH://读取历史记录回来
                        Log.i("detailDish","load_dish dishJson:" + dishJson);
                        if (!TextUtils.isEmpty(dishJson) && dishJson.length() > 10) {
                            imgLevel = LoadImage.SAVE_LONG;
                            loadOtherData();
                            analyzeHistoryData();
                        } else {
                            loadDishInfo();
                            loadOtherData();
                        }
                        break;
                    case LOAD_DISH_OVER://数据读取成功
                        findViewById(R.id.share_layout).setVisibility(View.VISIBLE);
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 读取是否有离线菜谱
     */
    private void loadHistoryData() {
        //处理延迟操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!ToolsDevice.getNetActiveState(DetailDish.this)) {
                    //获取离线菜谱的 json 数据
                    dishJson = DataOperate.buyBurden(XHApplication.in(), code);
                }
                //获取手机中的离线菜谱数量
                AppCommon.buyBurdenNum = getListMapByJson(DataOperate.buyBurden(XHApplication.in(), "")).size();
                mHandler.sendEmptyMessage(LOAD_DISH);
            }
        }).start();
    }

    /**
     * 请求网络
     */
    private void loadDishInfo() {
        String params = "code=" + code;
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishTopInfo,params, new InternetCallback(this) {

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
//                    Log.i("tzy","permissionMap = " + permissionMap.toString());
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

    private void loadOtherData(){
        String params = "code=" + code;
        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishTieInfo,params, new InternetCallback(DetailDish.this) {
            @Override
            public void loaded(int i, String s, Object o) {
                if(i >= ReqInternet.REQ_OK_STRING){
                    dishActivityViewControl.analyzeUserShowDishInfoData(String.valueOf(o));
                }
            }
        });

        ReqEncyptInternet.in().doEncypt(StringManager.api_getDishLikeNumStatus, params, new InternetCallback(DetailDish.this) {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING){
                    dishActivityViewControl.analyzeDishLikeNumberInfoData(String.valueOf(o));
                }
            }
        });
    }

    /**
     * 处理离线菜谱数据：
     * 历史记录都是:dishInfo的数据
     */
    private void analyzeHistoryData() {
        dishActivityViewControl.analyzeDishInfoData(dishJson, new HashMap<String, String>(),true);
        loadManager.loadOver(ReqInternet.REQ_OK_STRING, 1, true);
        mHandler.sendEmptyMessage(LOAD_DISH_OVER);
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
        dishActivityViewControl.analyzeDishInfoData(data,permissionMap,false);

    }


    private void requestWeb(String dishJson) {
        Map<String,String> dishInfo = StringManager.getFirstMap(dishJson);
        if(dishInfo != null){
            SpecialWebControl.initSpecialWeb(this,"dishInfo",dishInfo.get("name"),code);
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
        Log.i("DetailDishActivity","onResume() colse_level:" + Main.colse_level);
        mFavePopWindowDialog=dishActivityViewControl.getDishTitleViewControl().getPopWindowDialog();
        super.onResume();
        Rect outRect = new Rect();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
        statusBarHeight = outRect.top;
        dishActivityViewControl.onResume();
    }

    @Override
    protected void onPause() {
        Log.i("DetailDishActivity","onPause()");
        mFavePopWindowDialog=dishActivityViewControl.getDishTitleViewControl().getPopWindowDialog();
        super.onPause();
        dishActivityViewControl.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dishActivityViewControl.onDestroy();
        long nowTime=System.currentTimeMillis();
        if(startTime>0&&(nowTime-startTime)>0&&!TextUtils.isEmpty(data_type)&&!TextUtils.isEmpty(module_type)){
            XHClick.saveStatictisFile("DetailDish",module_type,data_type,code,"","stop",String.valueOf((nowTime-startTime)/1000),"","","","");
        }
        //释放资源。
        if(mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
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
}
