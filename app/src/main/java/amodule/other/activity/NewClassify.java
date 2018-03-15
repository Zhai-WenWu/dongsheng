package amodule.other.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.SetDataView;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.health.activity.DetailHealth;
import amodule.health.activity.HealthTest;
import amodule.health.activity.MyPhysique;
import amodule.search.avtivity.HomeSearch;
import amodule.search.data.SearchConstant;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;
import aplug.basic.SubBitmapTarget;
import third.ad.BannerAd;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class NewClassify extends BaseActivity {
    private final int LOAD_LEVEL_TWO_UI = 1;

    private int index = 0;
    private String nameTitle = "", type = "caipu", title = "分类", coverStr = "";
    private Handler handler = null;
    private ArrayList<Map<String, String>> leftListData = null;
    private ArrayList<ArrayList<Map<String, String>>> rightScrollData = null;
    private ArrayList<Map<String, String>> allData;

    private ListView leftListView;
    private ScrollView rightScrollView;
    private LinearLayout rightContentLayout, rightScrollLayout;

    private String mEventId = "a_menu_table";

    private ImageView classifyActivityImg;
    private String statistics = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            nameTitle = bundle.getString("name");
            type = bundle.getString("type");
            if ("caipu".equals(type) || TextUtils.isEmpty(type)) {
                type = "caipu";
                title = "菜谱分类";
                coverStr = "搜菜谱  如：糖醋排骨  或  鸡蛋";
                mEventId = "a_menu_table";
                statistics = "other_detail_sort";
            } else if ("jiankang".equals(type)) {
                title = "美食养生";
                coverStr = "搜养生内容";
                mEventId = "a_health_chart";
                statistics = "other_health_sort";
            }
        }
        handler = new Handler(new Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case LOAD_LEVEL_TWO_UI:
                        if (nameTitle != null && nameTitle.equals("时辰")) {
                            setActImg();
                        } else {
                            classifyActivityImg.setVisibility(View.GONE);
                        }
                        setAdShow();
                        //统计
                        XHClick.mapStat(NewClassify.this, mEventId, "左侧栏目", nameTitle);
                        //更新2级数据
                        setRightData(index);
                        //清空UI并重新创建UI
                        rightContentLayout.removeAllViews();
                        setTableData(rightScrollData);
                        //2级目录和1级列表延迟滑动到顶部
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                rightScrollView.smoothScrollTo(0, 0);
                                AppCommon.scorllToIndex(leftListView, index);
                                findViewById(R.id.classify_layout).setVisibility(View.VISIBLE);
                                loadManager.hideProgressBar();
                            }
                        }, 100);
                        break;
                }
                return false;
            }
        });
        initActivity(title, 2, 0, R.layout.c_view_bar_title, R.layout.a_xh_classify_new);
        loadManager.showProgressBar();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                init();
            }
        }, 100);
        initAd();
    }


    @Override
    protected void onResume() {
        super.onResume();
        setAdShow();
    }

    private void setAdShow() {
        if (nameTitle != null && nameTitle.equals("时辰")) {
            ((View) findViewById(R.id.classify_ad_bd_layout).getParent()).setVisibility(View.GONE);
            ((View) findViewById(R.id.classify_ad_banner_layout).getParent()).setVisibility(View.GONE);
        } else {
            ((View) findViewById(R.id.classify_ad_bd_layout).getParent()).setVisibility(View.VISIBLE);
            ((View) findViewById(R.id.classify_ad_banner_layout).getParent()).setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        //体质测试回来刷新UI
        if (nameTitle != null && nameTitle.equals("体质"))
            handler.sendEmptyMessage(LOAD_LEVEL_TWO_UI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//		leftListData.clear();
//		rightScrollData.clear();
//		allData.clear();
        handler.removeCallbacksAndMessages(null);
        System.gc();
    }

    private void init() {
        initUI();
        initData();
        initLeftLsit();
        //初始化二级布局
        handler.sendEmptyMessage(LOAD_LEVEL_TWO_UI);
    }

    //初始化UI
    private void initUI() {
        ((TextView) findViewById(R.id.layout_text_cover)).setText(coverStr);
        leftListView = (ListView) findViewById(R.id.classify_left_list);
        leftListView.setDivider(null);
        rightScrollView = (ScrollView) findViewById(R.id.classify_right_scrollView);
        rightContentLayout = (LinearLayout) findViewById(R.id.classify_right_content_layout);
        rightScrollLayout = (LinearLayout) findViewById(R.id.classify_right_layout);

        RelativeLayout view = (RelativeLayout) findViewById(R.id.search_fake_layout);
        view.setClickable(true);
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                XHClick.track(v.getContext(), "点击" + title + "页的搜索框");
                XHClick.mapStat(NewClassify.this, mEventId, "搜索", "");
//				Intent intent = new Intent(NewClassify.this, HomeSearch.class);
//				intent.putExtra("type", type);
//				intent.putExtra("from", "频道");T
//				startActivity(intent);


                Intent intent = new Intent(NewClassify.this, HomeSearch.class);
                intent.putExtra(SearchConstant.SEARCH_TYPE, SearchConstant.SEARCH_CAIPU);
                intent.putExtra(SearchConstant.SEARCH_WORD, "鱼");
                startActivity(intent);
            }
        });
        int scroll_width = ToolsDevice.getWindowPx(this).widthPixels * 570 / 750;
        classifyActivityImg = (ImageView) findViewById(R.id.classify_act);
        classifyActivityImg.getLayoutParams().height = (scroll_width - Tools.getDimen(this, R.dimen.dp_25)) / 4;
    }


    //初始化数据
    private void initData() {
        leftListData = new ArrayList<Map<String, String>>();
        rightScrollData = new ArrayList<ArrayList<Map<String, String>>>();
        String jsonData = AppCommon.getAppData(this, type);
        allData = UtilString.getListMapByJson(jsonData);
        if (nameTitle == null)
            nameTitle = allData.get(0).get("name");
        int i = 0;
        for (Map<String, String> data_map : allData) {
            data_map.put("title", data_map.get("name"));
            data_map.put("select", "0");
            if (nameTitle != null && nameTitle.equals(data_map.get("name"))) {
                index = i;
                data_map.put("select", "1");
            }
            leftListData.add(data_map);
            i++;
        }
        if (index == 0) {
            leftListData.get(0).put("select", "1");
        }
    }

    //初始化一级title
    private void initLeftLsit() {
        int list_width = ToolsDevice.getWindowPx(this).widthPixels * 180 / 750;
        final AdapterSimple adapter = new AdapterSimple(leftListView, leftListData,
                R.layout.a_xh_classify_item_left,
                new String[]{"title", "select"},
                new int[]{R.id.classify_left_title, R.id.classify_left_item});
        adapter.viewWidth = list_width;
        adapter.viewHeight = list_width / 2;
        adapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                int id = view.getId();
                switch (id) {
                    case R.id.classify_left_item:
                        TextView tv = (TextView) view.findViewById(R.id.classify_left_title);
                        if (data.equals("0")) {
                            tv.setTextColor(Color.parseColor("#4c4c4c"));
                            view.setBackgroundColor(Color.TRANSPARENT);
                            view.findViewById(R.id.line_left).setVisibility(View.GONE);
                            view.findViewById(R.id.line_right).setVisibility(View.GONE);
                        } else {
                            String color = Tools.getColorStr(NewClassify.this, R.color.comment_color);
                            tv.setTextColor(Color.parseColor(color));
                            view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            view.findViewById(R.id.line_left).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.line_right).setVisibility(View.GONE);
                        }
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });
        leftListView.getLayoutParams().width = list_width;
        leftListView.setAdapter(adapter);
        leftListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				loadManager.showProgressBar();
                for (int i = 0; i < leftListData.size(); i++) {
                    if (i == position)
                        leftListData.get(i).put("select", "1");
                    else
                        leftListData.get(i).put("select", "0");
                }
                adapter.notifyDataSetChanged();
                index = position;
                nameTitle = leftListData.get(index).get("title");
                handler.sendEmptyMessage(LOAD_LEVEL_TWO_UI);
            }
        });
    }

    //设置二级数据
    private void setRightData(int i) {
        //清除数据
        rightScrollData.clear();
        if (allData.size() > 0) {
            ArrayList<Map<String, String>> data = UtilString.getListMapByJson(allData.get(i).get("tags"));
            for (Map<String, String> dataMap : data) {
                ArrayList<Map<String, String>> data_list_3 = UtilString.getListMapByJson(dataMap.get("data"));
                Map<String, String> classifyName = new HashMap<String, String>();
                classifyName.put("classfiyName", dataMap.get("name"));
                if (nameTitle != null && nameTitle.equals("体质")) {
                    String str = AppCommon.isHealthTest();
                    String result = praseCrowd(str);
                    if (!result.equals("")) {
                        for (Map<String, String> map : data_list_3) {
                            if (map.get("name").equals(result))
                                map.put("mark", "我");
                            else
                                map.put("mark", "hide");
                        }
                    }
                }
                data_list_3.add(0, classifyName);
                rightScrollData.add(data_list_3);
            }
        }
    }

    private void setTableData(ArrayList<ArrayList<Map<String, String>>> info) {
        int infoLength = info.size();
        for (int i = 0; i < infoLength; i++) {
            final ArrayList<Map<String, String>> list = info.get(i);
            LayoutInflater.from(this).inflate(R.layout.a_xh_classify_item_right, rightContentLayout);
            TextView classify_tv = (TextView) rightContentLayout.getChildAt(i).findViewById(R.id.classify_right_title);
            classify_tv.setText(list.get(0).get("classfiyName"));
            if (list.get(0).get("classfiyName").length() == 0) {
                classify_tv.setVisibility(View.GONE);
                rightContentLayout.findViewById(R.id.classify_right_title_rela).setVisibility(View.GONE);
            }
            //移除第一个数据
            list.remove(0);
            //加载table数据
            String[] key_arr = null;
            int[] id_arr = null;
            //动态设置item宽高
            int width = (ToolsDevice.getWindowPx(this).widthPixels - Tools.getDimen(this, R.dimen.dp_90) - Tools.getDimen(this, R.dimen.dp_30)) / 3;
            int height = (int) (width / 1.9);
            if (nameTitle != null && nameTitle.equals("体质")) {
                key_arr = new String[]{"name", "mark"};
                id_arr = new int[]{R.id.classify_right_table_tv, R.id.classify_right_table_ico};
                //显示体质测试按钮
                testBtnShow(i, height - ToolsDevice.dp2px(this, 5));
                //显示体质测试结果
                myPhysiqueShow(i, height - ToolsDevice.dp2px(this, 5));
            } else {
                key_arr = new String[]{"name"};
                id_arr = new int[]{R.id.classify_right_table_tv};
            }
            TableLayout table = (TableLayout) rightContentLayout.getChildAt(i).findViewById(R.id.classify_right_table);
            AdapterSimple adapter = new AdapterSimple(table, list, R.layout.a_xh_classify_item_right_table, key_arr, id_arr);
            SetDataView.view(table, 3, adapter, null, new SetDataView.ClickFunc[]{new SetDataView.ClickFunc() {
                @Override
                public void click(int index, View v) {
                    //统计
                    XHClick.mapStat(NewClassify.this, mEventId, "右侧标签", list.get(index).get("name"));
                    AppCommon.openUrl(NewClassify.this, list.get(index).get("url"), false);
                }

                ;
            }}, LayoutParams.MATCH_PARENT, height);
        }
    }

    //显示体质测试结果
    private void myPhysiqueShow(int index, int height) {
        final String result = AppCommon.isHealthTest();
        if (!result.equals("")) {
            TextView classify_right_my_tizhi = (TextView) rightContentLayout.getChildAt(index).findViewById(R.id.classify_right_my_tizhi);
            if (height > 0)
                classify_right_my_tizhi.getLayoutParams().height = height;
            classify_right_my_tizhi.setText("我的体质：" + praseCrowd(result));
            classify_right_my_tizhi.setVisibility(View.VISIBLE);
            classify_right_my_tizhi.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(NewClassify.this, MyPhysique.class);
                    intent.putExtra("params", result);
                    startActivity(intent);
                }
            });
        }
    }

    //显示体质测试按钮
    private void testBtnShow(int index, int height) {
        Button tizhi_btn = (Button) rightContentLayout.getChildAt(index).findViewById(R.id.classify_right_btn_ceshi);
        if (height > 0)
            tizhi_btn.getLayoutParams().height = height;
        tizhi_btn.setVisibility(View.VISIBLE);
        tizhi_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewClassify.this, HealthTest.class);
                startActivity(intent);
            }
        });
    }

    XHAllAdControl xhAllAdControl;

    //菜谱分类和养生分类
    private void initAd() {
        ImageView imageView = (ImageView) findViewById(R.id.ad_banner_item_iv_single);
        final String adid = "caipu".equals(type) ? AdPlayIdConfig.Dish_CLASSIFY : AdPlayIdConfig.HEALTH_ClASSIFY;
        ArrayList<String> list = new ArrayList<>();
        list.add(adid);
        xhAllAdControl = new XHAllAdControl(list, (isRefresh,map) -> {
            if (map.containsKey(adid)) {
                BannerAd bannerAd = new BannerAd(NewClassify.this, xhAllAdControl, imageView);
                bannerAd.marginLeft = ToolsDevice.dp2px(NewClassify.this, 60);
                bannerAd.marginRight = ToolsDevice.dp2px(NewClassify.this, 60);
                map = StringManager.getFirstMap(map.get(adid));
                bannerAd.onShowAd(map);
                xhAllAdControl.onAdBind(0, imageView, "");
            }
        }, this, "");
        xhAllAdControl.registerRefreshCallback();
    }

    //设置活动相关
    private void setActImg() {
        ReqInternet.in().doGet(StringManager.api_soIndex + "?type=" + type, new InternetCallback() {

            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    // 分类页活动加载
                    ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(returnObj);
                    Map<String, String> mapReturn = listReturn.get(0);
                    if (mapReturn.containsKey("activity") &&
                            !mapReturn.get("activity").equals("null") &&
                            !mapReturn.get("activity").equals("")) {
                        mapReturn = UtilString.getListMapByJson(mapReturn.get("activity")).get(0);
                        final String act_url = mapReturn.get("url");
                        int dp_5 = Tools.getDimen(NewClassify.this, R.dimen.dp_5);
                        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(NewClassify.this)
                                .load(mapReturn.get("img"))
                                .setImageRound(dp_5)
                                .build();
                        if (bitmapRequest != null)
                            bitmapRequest.into(new SubBitmapTarget() {

                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                                    classifyActivityImg.setImageBitmap(bitmap);
                                    classifyActivityImg.setVisibility(View.VISIBLE);
                                }
                            });
                        classifyActivityImg.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (act_url != null)
                                    AppCommon.openUrl(NewClassify.this, act_url, true);
                            }
                        });
//						if(type.equals("jiankang")){
//							if(nameTitle != null && nameTitle.equals("时辰"))
//								classifyActivityImg.setVisibility(View.VISIBLE);
//							else
//								classifyActivityImg.setVisibility(View.GONE);
//						}
//						int dp_7_5 = Tools.getDimen(NewClassify.this, R.dimen.dp_7_5);
//						int dp_10 = Tools.getDimen(NewClassify.this, R.dimen.dp_10);
//						int dp_15 = Tools.getDimen(NewClassify.this, R.dimen.dp_15);
//						//判断广告位是否显示
//						if(classifyActivityImg.getVisibility() == View.GONE)
//							rightScrollLayout.setPadding(dp_10, dp_7_5 , dp_10, dp_15);
//						else if(classifyActivityImg.getVisibility() == View.VISIBLE)
//							rightScrollLayout.setPadding(dp_10, dp_5, dp_10, dp_15);
                    }
                }
            }
        });
    }

    //解析体质
    private String praseCrowd(String str) {
        ArrayList<Map<String, String>> resultList = UtilString.getListMapByJson(str);
        String result = null;
        if (str != null && str.length() != 0 && resultList.size() > 0)
            result = resultList.get(0).get("name");
        else
            result = "";
        return result;
    }

}