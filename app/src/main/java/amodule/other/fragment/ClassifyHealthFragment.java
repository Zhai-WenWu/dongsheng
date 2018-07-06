package amodule.other.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.SetDataView;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseFragmentActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
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

/**
 * Created by sll on 2017/4/24.
 */

public class ClassifyHealthFragment extends Fragment {

    private final int LOAD_LEVEL_TWO_UI = 1;

    private ArrayList<Map<String, String>> mLeftListData = null;
    private ArrayList<ArrayList<Map<String, String>>> mRightScrollData = null;
    private ArrayList<Map<String, String>> mAllData;

    private View mRootView;
    private ListView mLeftListView;
    private ScrollView mRightScrollView;
    private LinearLayout mRightContentLayout;
    private ImageView mActivityImg;
    private TextView mSearchHint;
    private ImageView mImageView;

    private BaseFragmentActivity mActivity;

    private Bundle mBundle;

    private String mNameTitle = "", mType = "caipu", mTitle = "分类", mCoverStr = "";
    private String mEventId = "a_menu_table";
    private String mStatistics = "";
    private String mXhIndex="",mSelectIndex="";
    private int mIndex = 0;

    private Handler mHandler = null;

    @Override
    public void onAttach(Activity activity) {
        mActivity = (BaseFragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBundle = this.getArguments();
        mRootView = inflater.inflate(R.layout.a_xh_classify_new, null);
        mLeftListView = (ListView) mRootView.findViewById(R.id.classify_left_list);
        mLeftListView.setDivider(null);
        mRightScrollView = (ScrollView) mRootView.findViewById(R.id.classify_right_scrollView);
        mRightContentLayout = (LinearLayout) mRootView.findViewById(R.id.classify_right_content_layout);
        RelativeLayout view = (RelativeLayout) mRootView.findViewById(R.id.search_fake_layout);
        view.setClickable(true);
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                XHClick.track(v.getContext(), "点击" + mTitle + "页的搜索框");
                XHClick.mapStat(mActivity, mEventId, "搜索", "");
                Intent intent = new Intent(mActivity, HomeSearch.class);
                intent.putExtra(SearchConstant.SEARCH_TYPE, SearchConstant.SEARCH_CAIPU);
                intent.putExtra(SearchConstant.SEARCH_WORD, "鱼");
                startActivity(intent);
            }
        });
        int scroll_width = ToolsDevice.getWindowPx(getContext()).widthPixels * 570 / 750;
        mActivityImg = (ImageView) mRootView.findViewById(R.id.classify_act);
        mActivityImg.getLayoutParams().height = (scroll_width - Tools.getDimen(getContext(), R.dimen.dp_25)) / 4;
        mSearchHint = (TextView) mRootView.findViewById(R.id.layout_text_cover);

        initData();
        mActivity.loadManager.showProgressBar();
        mSearchHint.setText(mCoverStr);
        getData();
        initAd();
        mHandler.sendEmptyMessage(LOAD_LEVEL_TWO_UI);
        return mRootView;
    }

    /**
     * 初始化数据，从Bundle中取出数据。
     */
    private void initData() {
        if (mBundle != null) {
            mNameTitle = mBundle.getString("name");
            mType = mBundle.getString("type");
            mTitle = mBundle.getString("title");
            mCoverStr = mBundle.getString("coverStr");
            mEventId = mBundle.getString("eventId");
            mStatistics = mBundle.getString("statistics");
            mXhIndex = mBundle.getString("xhindex");
            mSelectIndex = mBundle.getString("mSelectedPos");
        }

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case LOAD_LEVEL_TWO_UI:
                        if (mNameTitle != null && mNameTitle.equals("时辰")) {
                            setActImg();
                        } else {
                            mActivityImg.setVisibility(View.GONE);
                        }
                        setAdShow();
                        //统计
                        XHClick.mapStat(mActivity, mEventId, "左侧栏目", mNameTitle);
                        //更新2级数据
                        setRightData(mIndex);
                        //清空UI并重新创建UI
                        mRightContentLayout.removeAllViews();
                        setTableData(mRightScrollData);
                        //2级目录和1级列表延迟滑动到顶部
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mRightScrollView.smoothScrollTo(0, 0);
                                AppCommon.scorllToIndex(mLeftListView, mIndex);
                                mRootView.findViewById(R.id.classify_layout).setVisibility(View.VISIBLE);
                                mActivity.loadManager.hideProgressBar();
                            }
                        }, 100);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * 设置活动相关
     */
    private void setActImg() {
        ReqInternet.in().doGet(StringManager.api_soIndex + "?type=" + mType, new InternetCallback() {

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
                        int dp_5 = Tools.getDimen(mActivity, R.dimen.dp_5);
                        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mActivity)
                                .load(mapReturn.get("img"))
                                .setImageRound(dp_5)
                                .build();
                        if (bitmapRequest != null)
                            bitmapRequest.into(new SubBitmapTarget() {

                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                                    mActivityImg.setImageBitmap(bitmap);
                                    mActivityImg.setVisibility(View.VISIBLE);
                                }
                            });
                        mActivityImg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (act_url != null)
                                    AppCommon.openUrl(mActivity, act_url, true);
                            }
                        });
                    }
                }
            }
        });
    }

    private void setAdShow() {
        if (mNameTitle != null && mNameTitle.equals("时辰")) {
            ((View) mRootView.findViewById(R.id.classify_ad_bd_layout).getParent()).setVisibility(View.GONE);
            ((View) mRootView.findViewById(R.id.classify_ad_banner_layout).getParent()).setVisibility(View.GONE);
        } else {
            ((View) mRootView.findViewById(R.id.classify_ad_bd_layout).getParent()).setVisibility(View.VISIBLE);
            ((View) mRootView.findViewById(R.id.classify_ad_banner_layout).getParent()).setVisibility(View.VISIBLE);
        }
    }

    private void setTableData(ArrayList<ArrayList<Map<String, String>>> info) {
        int infoLength = info.size();
        for (int i = 0; i < infoLength; i++) {
            final ArrayList<Map<String, String>> list = info.get(i);
            LayoutInflater.from(mActivity).inflate(R.layout.a_xh_classify_item_right, mRightContentLayout);
            TextView classify_tv = (TextView) mRightContentLayout.getChildAt(i).findViewById(R.id.classify_right_title);
            classify_tv.setText(list.get(0).get("classfiyName"));
            if (list.get(0).get("classfiyName").length() == 0) {
                classify_tv.setVisibility(View.GONE);
                mRightContentLayout.findViewById(R.id.classify_right_title_rela).setVisibility(View.GONE);
            }
            //移除第一个数据
            list.remove(0);
            //加载table数据
            String[] key_arr = null;
            int[] id_arr = null;
            //动态设置item宽高
            int width = (ToolsDevice.getWindowPx(mActivity).widthPixels - Tools.getDimen(mActivity, R.dimen.dp_90) - Tools.getDimen(mActivity, R.dimen.dp_30)) / 3;
            int height = (int) (width / 1.9);
            if (mNameTitle != null && mNameTitle.equals("体质")) {
                key_arr = new String[]{"name", "mark"};
                id_arr = new int[]{R.id.classify_right_table_tv, R.id.classify_right_table_ico};
                //显示体质测试按钮
                testBtnShow(i, height - ToolsDevice.dp2px(mActivity, 5));
                //显示体质测试结果
                myPhysiqueShow(i, height - ToolsDevice.dp2px(mActivity, 5));
            } else {
                key_arr = new String[]{"name"};
                id_arr = new int[]{R.id.classify_right_table_tv};
            }
            TableLayout table = (TableLayout) mRightContentLayout.getChildAt(i).findViewById(R.id.classify_right_table);
            AdapterSimple adapter = new AdapterSimple(table, list, R.layout.a_xh_classify_item_right_table, key_arr, id_arr);
            SetDataView.view(table, 3, adapter, null, new SetDataView.ClickFunc[]{new SetDataView.ClickFunc() {
                @Override
                public void click(int index, View v) {
                    //统计
                    XHClick.mapStat(mActivity, mEventId, "右侧标签", list.get(index).get("name"));
                    AppCommon.openUrl(mActivity, list.get(index).get("url"), false);
                }

                ;
            }}, ViewGroup.LayoutParams.MATCH_PARENT, height);
        }
    }

    //显示体质测试结果
    private void myPhysiqueShow(int index, int height) {
        final String result = AppCommon.isHealthTest();
        if (!result.equals("")) {
            TextView classify_right_my_tizhi = (TextView) mRightContentLayout.getChildAt(index).findViewById(R.id.classify_right_my_tizhi);
            if (height > 0)
                classify_right_my_tizhi.getLayoutParams().height = height;
            classify_right_my_tizhi.setText("我的体质：" + praseCrowd(result));
            classify_right_my_tizhi.setVisibility(View.VISIBLE);
            classify_right_my_tizhi.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mActivity, MyPhysique.class);
                    intent.putExtra("params", result);
                    startActivity(intent);
                }
            });
        }
    }

    //显示体质测试按钮
    private void testBtnShow(int index, int height) {
        Button tizhi_btn = (Button) mRightContentLayout.getChildAt(index).findViewById(R.id.classify_right_btn_ceshi);
        if (height > 0)
            tizhi_btn.getLayoutParams().height = height;
        tizhi_btn.setVisibility(View.VISIBLE);
        tizhi_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, HealthTest.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onAdShow() {
        if(xhAllAdControl != null && mImageView != null){
            int[] location = new int[2];
            mImageView.getLocationOnScreen(location);
            if(location[0] >= 0 && location[0] <= ToolsDevice.getWindowPx(XHApplication.in()).widthPixels
                    && location[1] >= 0 && location[1] <= ToolsDevice.getWindowPx(XHApplication.in()).heightPixels){
                xhAllAdControl.onAdBind(0, mImageView, "");
            }
        }
    }

    XHAllAdControl xhAllAdControl;

    /**
     * 初始化广告
     */
    private void initAd() {
        mImageView = (ImageView) mRootView.findViewById(R.id.ad_banner_item_iv_single);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mImageView.getLayoutParams();
        layoutParams.setMargins(0,Tools.getDimen(getContext(),R.dimen.dp_10),0,0);
        String adPlayId = AdPlayIdConfig.HEALTH_ClASSIFY;
        if (mType.equals("caipu")) {
            adPlayId = AdPlayIdConfig.Dish_CLASSIFY;
        }
        ArrayList<String> ads = new ArrayList<>();
        ads.add(adPlayId);
        final String finalAdPlayId = adPlayId;
        xhAllAdControl = new XHAllAdControl(ads, getActivity(), "");
        xhAllAdControl.start((isRefresh,map) -> {
            if (map.containsKey(finalAdPlayId)) {
                BannerAd bannerAd = new BannerAd(mActivity, xhAllAdControl, mImageView);
                bannerAd.marginLeft = ToolsDevice.dp2px(mActivity, 60);
                bannerAd.marginRight = ToolsDevice.dp2px(mActivity, 60);
                map = StringManager.getFirstMap(map.get(finalAdPlayId));
                bannerAd.onShowAd(map);
                if(TextUtils.equals(mXhIndex,mSelectIndex)){
                    onAdShow();
                }
            }
        });
        xhAllAdControl.registerRefreshCallback();
    }

    /**
     * 获取左侧一级列表数据
     */
    private void getData() {
        mLeftListData = new ArrayList<Map<String, String>>();
        String jsonStr = AppCommon.getAppData(mActivity, mType);
        mAllData = UtilString.getListMapByJson(jsonStr);
        if (mNameTitle == null)
            mNameTitle = mAllData.get(0).get("name");
        int i = 0;
        for (Map<String, String> data_map : mAllData) {
            data_map.put("title", data_map.get("name"));
            data_map.put("select", "0");
            if (mNameTitle != null && mNameTitle.equals(data_map.get("name"))) {
                mIndex = i;
                data_map.put("select", "1");
            }
            mLeftListData.add(data_map);
            i++;
        }
        if (mIndex == 0) {
            mLeftListData.get(0).put("select", "1");
        }
        initLeftList();
    }

    private void initLeftList() {
        int list_width = ToolsDevice.getWindowPx(mActivity).widthPixels * 180 / 750;
        final AdapterSimple adapter = new AdapterSimple(mLeftListView, mLeftListData,
                R.layout.a_xh_classify_item_left,
                new String[]{"title", "select"},
                new int[]{R.id.classify_left_title, R.id.classify_left_item});
        adapter.viewWidth = list_width;
        adapter.viewHeight = list_width / 2;
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
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
                            String color = Tools.getColorStr(mActivity, R.color.comment_color);
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
        mLeftListView.getLayoutParams().width = list_width;
        mLeftListView.setAdapter(adapter);
        mLeftListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < mLeftListData.size(); i++) {
                    if (i == position)
                        mLeftListData.get(i).put("select", "1");
                    else
                        mLeftListData.get(i).put("select", "0");
                }
                adapter.notifyDataSetChanged();
                mIndex = position;
                mNameTitle = mLeftListData.get(mIndex).get("title");
                mHandler.sendEmptyMessage(LOAD_LEVEL_TWO_UI);
            }
        });
    }

    //设置二级数据
    private void setRightData(int i) {
        if (mRightScrollData == null)
            mRightScrollData = new ArrayList<ArrayList<Map<String, String>>>();
        else
            mRightScrollData.clear();
        if (mAllData.size() > 0) {
            ArrayList<Map<String, String>> data = UtilString.getListMapByJson(mAllData.get(i).get("tags"));
            for (Map<String, String> dataMap : data) {
                ArrayList<Map<String, String>> data_list_3 = UtilString.getListMapByJson(dataMap.get("data"));
                Map<String, String> classifyName = new HashMap<String, String>();
                classifyName.put("classfiyName", dataMap.get("name"));
                if (mNameTitle != null && mNameTitle.equals("体质")) {
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
                mRightScrollData.add(data_list_3);
            }
        }
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

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
