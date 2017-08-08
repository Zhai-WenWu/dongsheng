package amodule.main.view.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ScrollLinearListLayout;
import amodule.dish.activity.DetailDish;
import amodule.main.activity.MainHomePageNew;
import amodule.main.adapter.AdapterHomeDish;
import amodule.user.activity.FriendHome;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.ad.control.AdControlHomeDish;
import third.mall.tool.ToolView;
import xh.basic.tool.UtilString;

/**
 * PackageName : amodule.main.view.home
 * Created by MrTrying on 2016/9/12 10:17.
 * E_mail : ztanzeyu@gmail.com
 */
public class HomeDish {
    private Activity mActivity;
    private LoadManager mLoadManager;
    private AdapterHomeDish mAdapter;
    /** 头部layout */
    private LinearLayout headerTitle;
    /** 今日佳作数据 */
    private ArrayList<Map<String, String>> mData = null;

    private int mEveryPageCount = 0;
    private int mCurrentPage = 0;
    /** 是否加载完成 */
    private boolean mLoadOver = false;
    private int textCount = 0;
    private int imgHeight = 0;

    private int beforeNum = 0; //用于加广告

    /**
     * @param activity
     * @param loadManager
     */
    public HomeDish(Activity activity, LoadManager loadManager) {
        this.mActivity = activity;
        this.mLoadManager = loadManager;
        mData = new ArrayList<>();
    }

    /** 初始化header */
    private void initHeader(ListView listview) {
        headerTitle = new LinearLayout(mActivity);
        headerTitle.setBackgroundColor(Color.parseColor("#FFFFFF"));
        headerTitle.setOrientation(LinearLayout.VERTICAL);
        HomeTitle homeTitle = new HomeTitle(mActivity);
        homeTitle.setTitle("最新佳作");
        int dp_15 = (int) mActivity.getResources().getDimension(R.dimen.dp_15);
        homeTitle.findViewById(R.id.title_icon).setVisibility(View.GONE);
        homeTitle.setPadding(dp_15, 0, 0, 0);
        homeTitle.setCanClick(false);
        headerTitle.addView(homeTitle, LinearLayout.LayoutParams.MATCH_PARENT, Tools.getDimen(mActivity, R.dimen.dp_50));
        headerTitle.setVisibility(View.GONE);
        listview.addHeaderView(headerTitle);
    }

    /** 初始化数据 */
    public void loadData(ListView listview, ScrollLinearListLayout mHeaderListView) {
        if (!mLoadOver) {
            textCount = (int) (setTextViewNum(0) * 2);
            imgHeight = (int) (ToolsDevice.getWindowPx(mActivity).widthPixels * 450 / 750f);
            initHeader(listview);
            mAdapter = new AdapterHomeDish(mActivity,listview, mData,
                    R.layout.a_home_dish_item,
                    new String[]{"name", "timeShowV43", "dishTaste", "isExclusive", "nickName", "userimg", "isGourmet","img","adImg","isAd"},
                    new int[]{R.id.item_dish_name, R.id.item_dish_time, R.id.item_dish_taste, R.id.item_sole,
                            R.id.item_author_name, R.id.item_author_image, R.id.item_author_gourmet,R.id.item_model_video,R.id.item_ad_img,R.id.ad_hint}) {
                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(TextUtils.isEmpty(mData.get(position).get("adstyle"))) {
                                XHClick.track(v.getContext(), "点击首页的最新佳作");
                                XHClick.mapStat(mActivity, MainHomePageNew.STATISTICS_ID, "今日佳作", "点击内容");
                                Intent intent = new Intent(mActivity, DetailDish.class);
                                intent.putExtra("code", mData.get(position).get("code"));
                                mActivity.startActivity(intent);
                            }else{
                                AdControlHomeDish.getInstance().onAdClick(mData.get(position));
                            }
                        }
                    });
                    view.findViewById(R.id.ad_hint).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AdControlHomeDish.getInstance().onAdHintClick(mActivity,mData.get(position),"a_home_adver","今日佳作第" + mData.get(position).get("index") + "位广告");
                        }
                    });
                    if(TextUtils.isEmpty(mData.get(position).get("adstyle"))) {
                        view.findViewById(R.id.item_author_name).setOnClickListener(getOnClickListener(position, mData));
                        view.findViewById(R.id.item_author_image).setOnClickListener(getOnClickListener(position, mData));
                    }else{
                        AdControlHomeDish.getInstance().onAdShow(mData.get(position),view);
                        view.findViewById(R.id.item_author_name).setClickable(false);
                        view.findViewById(R.id.item_author_image).setClickable(false);
                    }
                    return view;
                }

                private View.OnClickListener getOnClickListener(final int position, final List<Map<String, String>> data) {
                    return new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            XHClick.track(v.getContext(), "点击首页的最新佳作");
                            XHClick.mapStat(mActivity, MainHomePageNew.STATISTICS_ID,"今日佳作","点击头像");
                            Intent intent = new Intent(mActivity, FriendHome.class);
                            if (TextUtils.isEmpty(data.get(position).get("userCode"))) {
                                return;
                            }
                            intent.putExtra("code", data.get(position).get("userCode"));
                            mActivity.startActivity(intent);
                        }
                    };
                }
            };
            mAdapter.videoImgId = R.id.item_model_video;
            mAdapter.playImgWH = Tools.getDimen(mActivity, R.dimen.dp_41);
            mAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    int id = view.getId();
                    switch (id) {
                        //处理独家标签
                        case R.id.item_sole:
                            ViewGroup parent = (ViewGroup) view.getParent();
                            if (data != null) {
                                parent.setVisibility("2".equals(data.toString()) ? View.VISIBLE : View.GONE);
                            } else {
                                parent.setVisibility(View.GONE);
                            }
                            return true;
                        //处理美食家icon
                        case R.id.item_author_gourmet:
                            view.setVisibility("2".equals(data.toString()) ? View.VISIBLE : View.GONE);
                            return true;
                        case R.id.item_model_video:
                            if(imgHeight != 0){
                                view.getLayoutParams().height = imgHeight;
                            }
                            return true;
                    }
                    return false;
                }
            });
            //初始化加载
            mLoadManager.setLoading(mHeaderListView, listview, mAdapter, true, new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    loadDishData(false);
                }
            });
            mLoadOver = true;
        } else {
            //刷新
            loadDishData(true);
        }
    }

    /**
     * 获取数据
     *
     * @param isRefresh
     */
    private void loadDishData(final boolean isRefresh) {
        if (isRefresh) {
            beforeNum = 0;
            mCurrentPage = 0;
            mEveryPageCount = 0;
        }
        mCurrentPage++;
        mLoadManager.changeMoreBtn(ReqInternet.REQ_OK_STRING, -1, -1, mCurrentPage, false);
        String params = "?page=" + mCurrentPage;
        ReqInternet.in().doGet(StringManager.api_homeDish + params, new InternetCallback(mActivity) {

            @Override
            public void loaded(int flag, String url, Object msg) {
                int loadCount = 0;
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    if (isRefresh) {
                        mData.clear();
                    }
                    ArrayList<Map<String, String>> returnData = UtilString.getListMapByJson(msg);
                    ArrayList<Map<String, String>> todayData = UtilString.getListMapByJson(returnData.get(0).get("todayData"));
                    for (int i = 0, length = todayData.size(); i < length; i++) {
                        Map<String, String> map = todayData.get(i);
                        Map<String, String> customerMap = StringManager.getFirstMap(map.get("customers"));
                        String dishTaste = map.get("dishTaste");
                        if(!TextUtils.isEmpty(dishTaste)){
                            if(dishTaste.length() > textCount){
                                dishTaste = dishTaste.substring(0,textCount - 3);
                                dishTaste += "...";
                                map.put("dishTaste",dishTaste);
                            }
                        }
                        if (customerMap != null) {
                            map.put("nickName", customerMap.get("nickName") + "");
                            map.put("userimg", customerMap.get("img") + "");
                            map.put("isGourmet", customerMap.get("isGourmet") + "");
                            map.put("userCode", customerMap.get("code") + "");
                        } else {
                            map.put("nickName", "hide");
                            map.put("userimg", "hide");
                            map.put("isGourmet", "hide");
                            map.put("userCode", "");
                        }
                        mData.add(map);
                        loadCount++;
                    }
                    //判断头部是否显示
                    if (mData.size() > 0) {
                        headerTitle.setVisibility(View.VISIBLE);
                    } else {
                        headerTitle.setVisibility(View.GONE);
                    }
                }
                if (mEveryPageCount == 0) {
                    mEveryPageCount = loadCount;
               }
                AdControlHomeDish.getInstance().getNewAdData(mData,false);
                beforeNum += loadCount;
                mAdapter.notifyDataSetChanged();
                mCurrentPage = mLoadManager.changeMoreBtn(flag, mEveryPageCount, loadCount, mCurrentPage, false);
            }
        });
    }

    private int setTextViewNum(int distance_commend) {
        WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        int tv_distance = (int) mActivity.getResources().getDimension(R.dimen.dp_13);
        int distance = (int) mActivity.getResources().getDimension(R.dimen.dp_125);

        int waith = wm.getDefaultDisplay().getWidth();
        int tv_waith = waith - distance - distance_commend;
        int tv_pad = ToolView.dip2px(mActivity, 1.0f);
        int num = (tv_waith + tv_pad) / (tv_distance + tv_pad);
        return num;
    }
}
