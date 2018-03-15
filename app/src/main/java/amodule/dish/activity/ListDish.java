package amodule.dish.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.FavoriteHelper;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.adapter.ListDishAdapter;
import amodule.user.activity.FriendHome;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * Title:ListDish.java Copyright: Copyright (c) 2014~2017
 *
 * @author zeyu_t
 * @date 2014年10月14日
 */
@SuppressLint("InflateParams")
public class ListDish extends BaseActivity {

    private XHWebView xhWebView;
    private TextView authorName, dishInfo, dishName;

    private ListDishAdapter adapter = null;
    private ArrayList<Map<String, String>> arrayList = null;

    private int currentPage = 0, everyPage = 0, loadPage = 0;
    private String name = "", g1 = "", type = "";
    private String shareImg = "";
    public boolean moreFlag = true, offLineOver = false, infoVoer = false, isToday = false;
    private String lastPermission = "";
    private String shareName = "";
    private String data_type = "";//推荐列表过来的数据
    private String module_type = "";//推荐列表过来的数据
    private Long startTime;//统计使用的时间
    private ArrayList<String> adIds;
    private XHAllAdControl xhAllAdControl;
    private static final Integer[] AD_INSTERT_INDEX = new Integer[]{3, 9, 16, 24, 32, 40, 48, 56, 64, 72};//插入广告的位置。
    private ArrayList<Map<String, String>> adData = new ArrayList<>();
    private ListView listView;
    private Map<String, String> permissionMap = new HashMap<>();
    private Map<String, String> detailPermissionMap = new HashMap<>();
    private boolean hasPermission = true;
    private boolean contiunRefresh = true;
    String classifyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getIntent().getExtras();
        startTime = System.currentTimeMillis();
        if (bundle != null) {
            type = bundle.getString("type");
            g1 = bundle.getString("g1");
            name = bundle.getString("name");
            data_type = bundle.getString("data_type");
            module_type = bundle.getString("module_type");
            shareName = name;
        }
        if ("recommend".equals(type) || "typeRecommend".equals(type))
            initActivity(name, 2, 0, R.layout.c_view_bar_title_time, R.layout.a_dish_caidan_list);
        else
            initActivity("", 2, 0, R.layout.c_view_bar_title, R.layout.a_dish_caidan_list);
        initMenu();
        initAdData();
        initBarView();
        registerObserver();
        WebviewManager manager = new WebviewManager(this, loadManager, true);
        xhWebView = manager.createWebView(R.id.XHWebview);
    }

    private IObserver mIObserver;

    private void registerObserver() {
        mIObserver = new IObserver() {
            @Override
            public void notify(String name, Object sender, Object data) {
                requestFavoriteState();
            }
        };
        ObserverManager.getInstance().registerObserver(mIObserver, ObserverManager.NOTIFY_LOGIN);
    }

    /**
     * 初始化广告数据
     */
    private void initAdData() {
        String[] ids = AdPlayIdConfig.MAIN_HOME_WEEK_GOOD_LIST;
        adIds = new ArrayList<>();
        for (String id : ids) adIds.add(id);

        String statisticKey = "jz_list";
        xhAllAdControl = new XHAllAdControl(adIds, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(boolean isRefresh,Map<String, String> map) {
                //处理广告数据
                int size = adIds.size();
                adData.clear();
                for (int i = 0; i < size; i++) {
                    if (map.containsKey(adIds.get(i)) && !TextUtils.isEmpty(map.get(adIds.get(i)))) {
                        String object = map.get(adIds.get(i));
                        Map<String, String> tempMap = StringManager.getFirstMap(object);
                        //进行数据拼装
                        tempMap.put("adStyle", "1");
                        tempMap.put("name", tempMap.get("desc"));
                        try {
                            tempMap.put("customer", new JSONObject().put("nickName", tempMap.get("title")).toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        tempMap.put("img", tempMap.get("imgUrl"));
                        tempMap.put("adPosition", tempMap.get("index"));
                        tempMap.put("adType", tempMap.get("type"));

                        adData.add(tempMap);

                    } else {
                        adData.add(new HashMap<String, String>());
                    }
                    if(isRefresh){
                        arrayList = handlerAdData(isRefresh,arrayList);
                    }
                }
            }
        }, this, statisticKey, true);
        xhAllAdControl.registerRefreshCallback();
        adapter.setXHAllControl(xhAllAdControl);
        loadManager.setLoading(listView, adapter, true, new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                loadData();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!hasPermission) {
            currentPage = 0;
            hasPermission = true;
            detailPermissionMap.clear();
            permissionMap.clear();
            arrayList.clear();
            loadData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        arrayList.clear();
        System.gc();
        long nowTime = System.currentTimeMillis();
        if (startTime > 0 && (nowTime - startTime) > 0 && !TextUtils.isEmpty(data_type) && !TextUtils.isEmpty(module_type)) {
            XHClick.saveStatictisFile("ListDish", module_type, data_type, g1, "", "stop", String.valueOf((nowTime - startTime) / 1000), "", "", "", "");
        }
        ObserverManager.getInstance().unRegisterObserver(mIObserver);
    }

    //初始化
    private void initMenu() {
        listView = (ListView) findViewById(R.id.dish_menu_listview);
        if (type.equals("recommend") || type.equals("typeRecommend")) {
            TextView title_time = (TextView) findViewById(R.id.title_time);
            title_time.setText("" + Tools.getAssignTime("yyyy-MM-dd", 0));
        } else {
            View view = LayoutInflater.from(ListDish.this).inflate(R.layout.a_dish_head_caidan_view, null);
            dishName = (TextView) view.findViewById(R.id.dish_menu_name);
            authorName = (TextView) view.findViewById(R.id.dish_menu_author_name);
            dishInfo = (TextView) view.findViewById(R.id.dish_menu_info);
            dishInfo.setClickable(true);
            listView.addHeaderView(view, null, false);
        }
        arrayList = new ArrayList<>();
        // 绑定列表数据
        adapter = new ListDishAdapter(this, arrayList);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent intent = new Intent(ListDish.this, DetailDish.class);
                if (!type.equals("recommend") && !type.equals("typeRecommend"))
                    position--;
                if (position > -1 && position < arrayList.size()) {
                    if (arrayList.get(position).containsKey("adStyle") && "1".equals(arrayList.get(position).get("adStyle"))) {
                        if (xhAllAdControl != null) {
                            int adPosition = Integer.parseInt(arrayList.get(position).get("adPosition"));
                            xhAllAdControl.onAdClick(adPosition, String.valueOf(adPosition + 1));
                        }
                    } else {
                        intent.putExtra("code", arrayList.get(position).get("code"));
                        intent.putExtra("img", arrayList.get(position).get("img"));
                        intent.putExtra("name", arrayList.get(position).get("name"));
                        intent.putExtra("dishInfo", getDishInfo(arrayList.get(position)));
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private String getDishInfo(Map<String, String> data) {
        try {
            JSONObject dishInfoJson = new JSONObject();
            dishInfoJson.put("code", data.get("code"));
            dishInfoJson.put("name", data.get("name"));
            dishInfoJson.put("img", data.get("img"));
            dishInfoJson.put("type", TextUtils.equals(data.get("hasVideo"), "2") ? "2" : "1");
            dishInfoJson.put("allClick", data.get("allClick").replace("浏览", ""));
            dishInfoJson.put("favorites", data.get("favorites").replace("收藏", ""));
            dishInfoJson.put("info", data.get("info"));
            JSONObject customerJson = new JSONObject();
            Map<String, String> userInfo = StringManager.getFirstMap(data.get("customer"));
            customerJson.put("customerCode", userInfo.get("code"));
            customerJson.put("nickName", userInfo.get("nickName"));
            customerJson.put("info", "");
            customerJson.put("img", userInfo.get("img"));
            dishInfoJson.put("customer", customerJson);
            return Uri.encode(dishInfoJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    boolean isFav = false;
    ImageView img_fav;

    private void initBarView() {
        // titleBar初始化
        ImageView img_share = (ImageView) findViewById(R.id.rightImgBtn2);
        img_share.setImageResource(R.drawable.z_z_topbar_ico_share);
        img_share.setVisibility(View.VISIBLE);
        img_share.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doShare();
            }
        });
        img_fav = (ImageView) findViewById(R.id.rightImgBtn4);
        img_fav.setVisibility("caidan".equals(type) ? View.VISIBLE : View.GONE);
        img_fav.setImageResource(R.drawable.z_caipu_xiangqing_topbar_ico_fav);
        img_fav.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginManager.isLogin()) {
                    handlerFavorite();
                } else
                    startActivity(new Intent(ListDish.this, LoginByAccout.class));
            }
        });
    }

    private void handlerFavorite() {
        statistics(isFav ? "取消收藏" : "收藏", "");
        FavoriteHelper.instance().setFavoriteStatus(this, g1, classifyName, FavoriteHelper.TYPE_MUNE,
                new FavoriteHelper.FavoriteStatusCallback() {
                    @Override
                    public void onSuccess(boolean state) {
                        isFav = state;
                        img_fav.setImageResource(isFav ? R.drawable.z_caipu_xiangqing_topbar_ico_fav_active : R.drawable.z_caipu_xiangqing_topbar_ico_fav);
                    }

                    @Override
                    public void onFailed() {
                    }
                });
    }

    @SuppressLint("NewApi")
    public void loadData() {
        if (currentPage == 0 && !type.equals("recommend")) {
            requestFavoriteState();
        }
        currentPage++;
        loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage, arrayList.size() == 0);
        String url = null;
        if (type.equals("recommend"))
            url = StringManager.api_getDishList + "?type=" + type + "&page=" + currentPage;
        else if (type.equals("typeRecommend"))
            url = StringManager.api_getDishList + "?type=" + type + "&g1=" + g1 + "&page=" + currentPage;
        else
            url = StringManager.api_getDishList + "?type=" + type + "&g1=" + g1 + "&page=" + currentPage;
        ReqInternet.in().doGet(url, new InternetCallback() {

            @Override
            public void getPower(int flag, String url, Object obj) {
                //权限检测
                if (permissionMap.isEmpty()
                        && !TextUtils.isEmpty((String) obj) && !"[]".equals(obj)
                        && currentPage == 1) {
                    if (TextUtils.isEmpty(lastPermission)) {
                        lastPermission = (String) obj;
                    } else {
                        if (lastPermission.equals(obj.toString())) {
                            contiunRefresh = false;
                            return;
                        }
                    }
                    permissionMap = StringManager.getFirstMap(obj);
                    if (permissionMap.containsKey("page")) {
                        Map<String, String> pagePermission = StringManager.getFirstMap(permissionMap.get("page"));
                        hasPermission = analyzePagePermissionData(pagePermission);
                        if (!hasPermission) return;
                    }

                }
            }

            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    if (!hasPermission || !contiunRefresh) return;

                    ArrayList<Map<String, String>> returnList = UtilString.getListMapByJson(returnObj);
                    if (!type.equals("recommend") && !type.equals("typeRecommend")) {
                        classifyName = returnList.get(0).get("name");
                        String customer = returnList.get(0).get("customer");
                        final ArrayList<Map<String, String>> customers = StringManager.getListMapByJson(customer);
                        String info = returnList.get(0).get("info");
                        String authorName = "";
                        if (customers != null && customers.size() > 0) {
                            authorName = customers.get(0).get("nickName");
                        }
                        if (!TextUtils.isEmpty(classifyName)) {
                            ListDish.this.dishName.setText(classifyName);
                        } else {
                            ListDish.this.dishName.setVisibility(View.GONE);
                        }
                        TextView title = (TextView) findViewById(R.id.title);
                        if (!TextUtils.isEmpty(authorName)) {
                            if (title != null) {
                                String str = "";
                                if (authorName.length() > 11)
                                    str = authorName.substring(0, 11) + "...";
                                else
                                    str = authorName;
                                title.setText(str);
                            }
                            ListDish.this.authorName.setText(authorName);
                            findViewById(R.id.from_container).setVisibility(View.VISIBLE);
                            ListDish.this.authorName.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(ListDish.this, FriendHome.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("code", customers.get(0).get("code"));
                                    intent.putExtras(bundle);
                                    ListDish.this.startActivity(intent);
                                }
                            });
                        } else {
                            if (title != null)
                                title.setText("菜单");
                            ListDish.this.authorName.setVisibility(View.GONE);
                        }
                        if (!TextUtils.isEmpty(info)) {
                            ListDish.this.dishInfo.setText(info);
                        } else {
                            ListDish.this.dishInfo.setVisibility(View.GONE);
                        }
                        shareName = classifyName;
                        returnList = UtilString.getListMapByJson(returnList.get(0).get("dishs"));

                    }
                    loadPage = returnList.size();

                    for (int i = 0; i < returnList.size(); i++) {
                        Map<String, String> map = returnList.get(i);
                        if (i == 0) shareImg = returnList.get(i).get("img");
                        if (map.containsKey("allClick") && !TextUtils.isEmpty(map.get("allClick")))
                            map.put("allClick", map.get("allClick") + "浏览");
                        if (map.containsKey("favorites") && !TextUtils.isEmpty(map.get("favorites")))
                            map.put("favorites", map.get("favorites") + "收藏");
                        if (type.equals("typeRecommend") && map.get("isToday").equals("1") && !isToday) {
                            map.put("isToday", "往期推荐");
                            isToday = true;
                        } else
                            map.put("isToday", "hide");
                        map.put("isDel", "hide");
                        if (!map.containsKey("hasVideo")) {
                            map.put("hasVideo", "1");
                        }
                        arrayList.add(map);
                    }
                    if (!type.equals("recommend") && !type.equals("typeRecommend")) {
                        //插入广告。
                        arrayList = handlerAdData(false,arrayList);
                    }
                    adapter.notifyDataSetChanged();
                }
                if (everyPage == 0) everyPage = loadPage;
                currentPage = loadManager.changeMoreBtn(flag, everyPage, loadPage, currentPage, arrayList.size() == 0);
                // 如果总数据为空,显示没有消息
                if (flag >= UtilInternet.REQ_OK_STRING && arrayList.size() == 0) {
                    findViewById(R.id.dish_menu_noData).setVisibility(View.VISIBLE);
                }
                // 否则显示结果
                else
                    findViewById(R.id.dish_menu_listview).setVisibility(View.VISIBLE);
                xhWebView.setVisibility(View.GONE);
            }
        });
    }

    private void requestFavoriteState() {
        FavoriteHelper.instance().getFavoriteStatus(this, g1, FavoriteHelper.TYPE_MUNE,
                new FavoriteHelper.FavoriteStatusCallback() {
                    @Override
                    public void onSuccess(boolean state) {
                        isFav = state;
                        img_fav.setImageResource(isFav ? R.drawable.z_caipu_xiangqing_topbar_ico_fav_active : R.drawable.z_caipu_xiangqing_topbar_ico_fav);
                    }

                    @Override
                    public void onFailed() {
                        img_fav.setImageResource(R.drawable.z_caipu_xiangqing_topbar_ico_fav);
                    }
                });
    }

    /**
     * @param pagePermission
     *
     * @return
     */
    public boolean analyzePagePermissionData(Map<String, String> pagePermission) {
        if (pagePermission.containsKey("url") && !TextUtils.isEmpty(pagePermission.get("url"))) {
            String url = pagePermission.get("url");
            xhWebView.loadUrl(url);
            xhWebView.setVisibility(View.VISIBLE);
            return false;
        }
        xhWebView.setVisibility(View.GONE);
        return true;
    }

    protected void doShare() {
        XHClick.mapStat(this, "a_share400", "菜谱", "菜单详情页");
        if (TextUtils.isEmpty(shareName)) {
            shareName = "精选菜单";
        }
        String imgType = BarShare.IMG_TYPE_WEB;
        String title = "";
        String clickUrl = "";
        String content = "";
        if (type.equals("recommend")) {
            clickUrl = StringManager.wwwUrl + "caipu/recommend/";
        } else {
            clickUrl = StringManager.wwwUrl + "caipu/caidan/" + g1;
        }
        // 是推荐菜单
        barShare = new BarShare(ListDish.this, "菜单详情", "菜谱");
        if (type.equals("caidan")) {
            // 是推荐菜单
            title = shareName + "，果断收藏！";
            content = shareName + "，各种精选菜谱，非常有用，推荐一下。（香哈菜谱）";
        } else {
            title = "今日推荐菜谱-" + Tools.getAssignTime("MM月dd日", 0);
            clickUrl = StringManager.third_downLoadUrl;
            content = "今日推荐菜谱很不错，每天可以尝试不同的菜，吃货必备呀 ";
        }
        barShare.setShare(imgType, title, content, shareImg, clickUrl);
        barShare.openShare();
    }

    /**
     * 拼装广告数据
     *
     * @param listData
     *
     * @return
     */
    private ArrayList<Map<String, String>> handlerAdData(boolean isRefresh,ArrayList<Map<String, String>> listData) {
        if (adData == null || adData.isEmpty()) {
            return listData;
        }
        for (int i = 0; i < listData.size(); i++) {
            int lenght = AD_INSTERT_INDEX.length;
            for (int j = 0; j < lenght; j++) {
                if (i == AD_INSTERT_INDEX[j]) {//是要插广告的位置
                    Log.i("tzy", "handlerAdData: ==");
                    //数据无不是广告直接插入广告
                    if (!listData.get(i).containsKey("adStyle")
                            || TextUtils.isEmpty(listData.get(i).get("adStyle"))) {
                        //插入广告
                        if (adData.get(j) != null && adData.get(j).size() > 0) {//数据
                            listData.add(i, adData.get(j));
                            Log.i("tzy", "handlerAdData: add");
                        }
                    }else if(isRefresh){
                        //插入广告
                        if (adData.get(j) != null && adData.get(j).size() > 0) {//数据
                            listData.set(i, adData.get(j));
                            Log.i("tzy", "handlerAdData: set");
                        }
                    }//不进行如何操作。
                }
            }
        }
        return listData;
    }

    private void statistics(String twoLevel, String threeLevel) {
        XHClick.mapStat(this, "a_menu_detail", twoLevel, threeLevel);
    }
}
