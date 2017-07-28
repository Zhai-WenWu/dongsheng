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
import amodule.dish.view.DishActivityViewControl;
import amodule.user.db.BrowseHistorySqlite;
import amodule.user.db.HistoryData;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;
import third.video.VideoPlayerController;
import xh.basic.tool.UtilString;

import static com.xiangha.R.id.share_layout;
import static java.lang.System.currentTimeMillis;

/**
 * 新的菜谱详情页
 * -----离线菜谱处理
 * 页面加载原则：头部跟随view一起初始化，底部有数据再进行加载
 */
public class DetailDish extends BaseAppCompatActivity {
    public static String tongjiId = "a_menu_detail_normal430";//统计标示
    private final int LOAD_DISH = 1;
    private final int LOAD_DISH_OVER = 2;

    private ADDishContorl adDishContorl;
    private DishActivityViewControl dishActivityViewControl;//view处理控制

    private Handler mHandler;
    private Map<String,String> permissionMap = new HashMap<>();
    private Map<String,String> detailPermissionMap = new HashMap<>();
    public static int showNumLookImage=0;//点击展示次数
    private int statusBarHeight = 0;//广告所用bar高度
    private int page = 1;
    public String code, dishTitle, state;//页面开启状态所必须的数据。
    public String dishJson;//历史记录中dishInfo的数据
    private String imgLevel = FileManager.save_cache;//图片缓存机制---是离线菜谱改变其缓存机制
    public boolean isHasVideo = false;//是否显示视频数据
    private boolean hasPermission = true;
    private boolean contiunRefresh = true;
    private String lastPermission = "";

    private boolean isBack = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        adDishContorl= new ADDishContorl();
        adDishContorl.getAdData(DetailDish.this);
    }

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
        dishActivityViewControl.setAdDishControl(adDishContorl);
        startTime= System.currentTimeMillis();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        page = 1;
        isHasVideo = false;
        detailPermissionMap.clear();
        permissionMap.clear();
        if(dishActivityViewControl != null)
            dishActivityViewControl.setLoginStatus();
        setRequest(page);
    }

    /**
     * 数据的初始化
     */
    private void init() {
        initActivity(dishTitle, 2, 0, 0, R.layout.a_dish_detail_new);
        dishActivityViewControl= new DishActivityViewControl(this);
        dishActivityViewControl.init(state, loadManager, code, new DishActivityViewControl.DishViewCallBack() {
            @Override
            public void gotoRequest() {
                loadManager.setLoading(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(page >= 3)
                            setRequest(++page);
                    }
                },false);
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
                        if (dishJson.length() > 10 && dishJson.contains("\"makes\":")) {
                            imgLevel = LoadImage.SAVE_LONG;
                            analyzeHistoryData();
                        } else
                            setRequest(page);
                        break;
                    case LOAD_DISH_OVER://数据读取成功
                        findViewById(share_layout).setVisibility(View.VISIBLE);
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 读取是否有事
     */
    private void loadHistoryData() {
        //处理延迟操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                //获取离线菜谱的 json 数据
                dishJson = DataOperate.buyBurden(XHApplication.in(), code);
                dishActivityViewControl.setDishJson(dishJson);
                //获取手机中的离线菜谱数量
                AppCommon.buyBurdenNum = UtilString.getListMapByJson(DataOperate.buyBurden(XHApplication.in(), "")).size();
                mHandler.sendEmptyMessage(LOAD_DISH);
            }
        }).start();
    }

    /**
     * 请求网络
     */
    private void setRequest(final int pages) {
        String params = "?code=" + code + "&pg=" + pages;
        if (!TextUtils.isEmpty(state)) {//是当前用户的菜谱。
            params += "&isNew=1";
        }

        ReqInternet.in().doGet(StringManager.api_getDishInfoNew + params, new InternetCallback(this) {

            @Override
            public void getPower(int flag, String url, Object obj) {
//                Log.i("tzy","obj = " + obj);
                //权限检测
                if(permissionMap.isEmpty()
                        && !TextUtils.isEmpty((String)obj) && !"[]".equals(obj)
                        && pages == 1){
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
                    if(pages == 1){
                        dishActivityViewControl.reset();
                    }
                    if (!TextUtils.isEmpty(o.toString()) && !o.toString().equals("[]")) {
                        analyzeData(StringManager.getListMapByJson(o),detailPermissionMap);
                        if(page < 3)
                            setRequest(++page);
                    } else {
                        dishActivityViewControl.setIsGoLoading(false);
                        loadManager.loadOver(flag, 1, true);
                    }
                } else
                    dishActivityViewControl.setIsGoLoading(false);
                if(ToolsDevice.isNetworkAvailable(context)|| !LoadImage.SAVE_LONG.equals(imgLevel)){
                    loadManager.loadOver(flag, 1, true);
                }else loadManager.hideProgressBar();
            }
        });
    }

    /**
     * 处理历史记录：
     * 历史记录都是:dishInfo的数据
     */
    private void analyzeHistoryData() {
        ArrayList<Map<String, String>> listmaps = UtilString.getListMapByJson(dishJson);
        if (listmaps.size() > 0)
            dishActivityViewControl.setIsGoLoading(true);
        else {
            dishActivityViewControl.setIsGoLoading(false);
            return;
        }
        dishActivityViewControl.analyzeDishInfoData(listmaps, new HashMap<String, String>());
        loadManager.loadOver(ReqInternet.REQ_OK_STRING, 1, true);
        mHandler.sendEmptyMessage(LOAD_DISH_OVER);
    }

    /**
     * 处理业务数据
     *
     * @param list 数据
     * @param permissionMap 权限数据
     */
    private void analyzeData(ArrayList<Map<String, String>> list,Map<String,String> permissionMap) {
        String lable = list.get(0).get("lable");
        ArrayList<Map<String, String>> listmaps = StringManager.getListMapByJson(list.get(0).get("data"));
        //第一页未请求到数据，直接关闭改页面
        if (listmaps.size() == 0 && lable.equals("dishInfo")) {
            Tools.showToast(getApplicationContext(), "抱歉，未找到相应菜谱");
            DetailDish.this.finish();
            return;
        }
        if (lable.equals("dishInfo")) {//第一页整体业务标示
            dishJson = list.get(0).get("data");//第一页数据保留下来
            saveHistoryToDB(dishJson);
            requestWeb();
        }
        dishActivityViewControl.analyzeData(list,permissionMap);
    }
    private boolean saveHistory = false;
    private void saveHistoryToDB(final String dishJson) {
        if (!saveHistory) {
            saveHistory = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = handlerJSONData(dishJson);
                    HistoryData data = new HistoryData();
                    data.setBrowseTime(currentTimeMillis());
                    data.setCode(code);
                    data.setDataJson(jsonObject.toString());
                    BrowseHistorySqlite sqlite = new BrowseHistorySqlite(XHApplication.in());
                    sqlite.insertSubject(BrowseHistorySqlite.TB_DISH_NAME, data);
                }
            }).start();
        }
    }

    private JSONObject handlerJSONData(String dishJson) {
        JSONObject jsonObject = new JSONObject();
        try {
            ArrayList<Map<String, String>> dishInfoArray = StringManager.getListMapByJson(dishJson);
            if (dishInfoArray.size() > 0) {
                Map<String, String> dishInfo = dishInfoArray.get(0);
                jsonObject.put("name", dishInfo.get("name"));
                jsonObject.put("img", dishInfo.get("img"));
                jsonObject.put("code", code);
                jsonObject.put("isFine", dishInfo.get("rank"));
                jsonObject.put("favorites", dishInfo.get("favorites"));
                jsonObject.put("allClick", dishInfo.get("allClick"));
                jsonObject.put("exclusive", dishInfo.get("exclusive"));

                ArrayList<Map<String, String>> videos = StringManager.getListMapByJson(dishInfo.get("video"));
                jsonObject.put("hasVideo", videos.size() > 0 ? 2 : 1);
                ArrayList<Map<String, String>> makes = StringManager.getListMapByJson(dishInfo.get("makes"));
                jsonObject.put("isMakeImg", makes.size() > 0 ? 2 : 1);
                ArrayList<Map<String, String>> burden = StringManager.getListMapByJson(dishInfo.get("burden"));
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < burden.size(); i++) {
                    ArrayList<Map<String, String>> data = StringManager.getListMapByJson(burden.get(i).get("data"));
                    for (int j = 0; j < data.size(); j++) {
                        stringBuffer.append(data.get(j).get("name")).append(",");
                    }
                }
                jsonObject.put("burdens", stringBuffer.toString().substring(0, stringBuffer.length() - 1));
            }
        } catch (Exception e) { }
        return jsonObject;
    }

    private void requestWeb() {
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
        isBack = true;
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        mFavePopWindowDialog=dishActivityViewControl.getDishTitleViewControl().getPopWindowDialog();
        super.onResume();
        Rect outRect = new Rect();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
        statusBarHeight = outRect.top;
        dishActivityViewControl.setStatusBarHeight(statusBarHeight);
        dishActivityViewControl.onResume();
    }

    @Override
    protected void onPause() {
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
        mHandler.removeCallbacksAndMessages(null);
        mHandler=null;
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
        if (requestCode == 1000 && data != null) {
            dishActivityViewControl.getDishHeaderView().getdDshAboutView().setNewFollowState(String.valueOf(data.getExtras().get("folState")));
        }else if(requestCode==10000&& resultCode== Activity.RESULT_OK){
            Map<String, String> dishInfoMap = dishActivityViewControl.getDishInfoMap();
            if (dishInfoMap == null)
                return;
            String subjectCode=dishInfoMap.get("subjectCode");
            String url="subjectInfo.app?code=" + subjectCode + "&title=" + dishInfoMap.get("name");
            AppCommon.openUrl(this,url,true);
        }
    }
}
