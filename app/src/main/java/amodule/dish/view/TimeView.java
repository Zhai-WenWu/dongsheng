package amodule.dish.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.SyntaxTools;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.activity.DetailDish;
import amodule.dish.adapter.AdapterTimeDish;
import amodule.dish.business.DishAdDataControl;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

@SuppressLint("InflateParams")
public class TimeView {
    private BaseActivity mAct;
    private View view;
    private LoadManager loadManager = null;
    private ArrayList<Map<String, String>> arrayList;
    private AdapterTimeDish adapter;
    private int currentPage = 0, everyPage = 0, loadPage = 0;
    private String type;
    public boolean isToday = false;
    private String g1;
    private ShareImg shareImag;
    private ListView mListView;
    private DishAdDataControl adDataControl;

    public TimeView(BaseActivity act, String type, String g1, ShareImg shareImage) {
        super();
        this.mAct = act;
        this.type = type;
        this.g1 = g1;
        this.shareImag = shareImage;
        adDataControl = new DishAdDataControl();
    }

    public View onCreateView() {
        view = LayoutInflater.from(mAct).inflate(R.layout.a_dish_caidan_list, null);
        loadManager = new LoadManager(mAct, mAct.rl);
        initView();
        return view;
    }

    /**
     * 初始化
     */
    private void initView() {
        mListView = (ListView) view.findViewById(R.id.dish_menu_listview);
        arrayList = new ArrayList<Map<String, String>>();
        // 绑定列表数据
        adapter = new AdapterTimeDish(mAct, mListView, arrayList,
                R.layout.a_dish_time_item,
                new String[]{"name", "nickName", "userImg", "isGourment", "isToday", "allClick", "favorites", "isJin", "isYou","isAd"},
                new int[]{R.id.item_title_tv, R.id.user_name, R.id.iv_userImg, R.id.iv_userType, R.id.dish_recom_item_today
                        , R.id.dish_time_item_allClick, R.id.dish_time_item_allFave, R.id.iv_itemIsFine, R.id.iv_itemIsGood,R.id.ad_hint_imv});
        adapter.imgWidth = ToolsDevice.getWindowPx(mAct).widthPixels - Tools.getDimen(mAct, R.dimen.dp_20);//20=10*2
        adapter.scaleType = ScaleType.CENTER_CROP;
        adapter.isAnimate = true;


        loadManager.setLoading(mListView, adapter, true, new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                loadData();
            }
        });
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent intent = new Intent(mAct, DetailDish.class);
                if (position < arrayList.size()) {
                    intent.putExtra("code", arrayList.get(position).get("code"));
                    intent.putExtra("name", arrayList.get(position).get("name"));
                    mAct.startActivity(intent);
                }
            }
        });
    }

    @SuppressLint("NewApi")
    public void loadData() {
        currentPage++;
        loadManager.changeMoreBtn(mListView, UtilInternet.REQ_OK_STRING, -1, -1, currentPage, arrayList.size() == 0);
        String url = null;
        if (type.equals("recommend"))
            url = StringManager.api_getDishList + "?type=" + type + "&page=" + currentPage;
        else if (type.equals("typeRecommend"))
            url = StringManager.api_getDishList + "?type=" + type + "&g1=" + g1 + "&page=" + currentPage;
        else
            url = StringManager.api_getDishList + "?type=" + type + "&g1=" + g1 + "&page=" + currentPage;
        ReqInternet.in().doGet(url, new InternetCallback(mAct) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> returnList = UtilString.getListMapByJson(returnObj);
                    for (int i = 0; i < returnList.size(); i++) {
                        Map<String, String> map = returnList.get(i);
                        if (i == 0) {
                            shareImag.showImg(returnList.get(i).get("img"));
                        }
                        if (type.equals("typeRecommend") && map.get("isToday").equals("1") && !isToday) {
                            map.put("isToday", "往期推荐");
                            isToday = true;
                        } else
                            map.put("isToday", "hide");
                        if (!map.containsKey("hasVideo")) {
                            map.put("hasVideo", "1");
                        }
                        map.put("allClick", map.get("allClick") + "浏览");
                        map.put("favorites", map.get("favorites") + "收藏");
                        map.put("isYou", "hide");
                        map.put("isJin", "hide");
                        if ("2".equals(map.get("level"))) { //优质
                            map.put("isYou", "优质");
                            map.put("isJin", "hide");

                        } else if ("3".equals(map.get("level"))) { //精华
                            map.put("isYou", "hide");
                            map.put("isJin", "精华");

                        }

                        String customer = map.get("customer");
                        if (!TextUtils.isEmpty(customer)) {
                            ArrayList<Map<String, String>> customerArray = UtilString.getListMapByJson(customer);
                            if (customer.length() > 0) {
                                Map<String, String> customerMap = customerArray.get(0);
                                map.put("nickName", customerMap.get("nickName"));
                                map.put("userCode", customerMap.get("code"));
                                map.put("isGourmet", customerMap.get("isGourmet"));
                                String userImg = customerMap.get("img");
                                if (TextUtils.isEmpty(userImg) || "null".equals(userImg)) {
                                    map.put("userImg", map.get("img"));
                                } else {
                                    map.put("userImg", userImg);
                                }
                            }
                        }
                        arrayList.add(map);
                    }
                    loadPage = returnList.size();
                    adapter.notifyDataSetChanged();
                }
                if (everyPage == 0) everyPage = loadPage;
                currentPage = loadManager.changeMoreBtn(mListView, flag, everyPage, loadPage, currentPage, arrayList.size() == 0);
                // 如果总数据为空,显示没有消息
                if (flag >= UtilInternet.REQ_OK_STRING && arrayList.size() == 0) {
                    view.findViewById(R.id.dish_menu_noData).setVisibility(View.VISIBLE);
                }
                // 否则显示结果
                else
                    view.findViewById(R.id.dish_menu_listview).setVisibility(View.VISIBLE);


                if (currentPage == 1 && arrayList != null && arrayList.size() > 0) {
                    SyntaxTools.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adDataControl.getDishAdData(mAct, new DishAdDataControl.DishAdDataControlCallback() {
                                @Override
                                public void onGetDataComplete() {
                                    adDataControl.addAdDataToList(arrayList);
                                    SyntaxTools.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.setAdControl(adDataControl.xhAllAdControl);
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            });
                        }
                    });

                }

            }
        });
    }

    public interface ShareImg {
        public abstract void showImg(String str);
    }
}
