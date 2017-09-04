package amodule.nous.view;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.SyntaxTools;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.nous.adapter.AdapterNousHome;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.ad.scrollerAd.XHAllAdControl;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

import static third.ad.tools.AdPlayIdConfig.MAIN_HOME_ZHISHI_LIST;

public class FragmentNous {

    private DownRefreshList listNous;
    private View view;

    private BaseActivity mAct;
    LoadManager loadManager = null;    // 加载管理
    private static Handler handler = null;
    private AdapterNousHome adapter;
    private ArrayList<Map<String, String>> listDataNous;

    private final int MSG_NOUS_OK = 1;
    public boolean LoadOver = false;
    private String url;
    private int currentPage = 0, everyPage = 0;
    private int adCount = 0;
    private List<Integer> mAds = new ArrayList<>();
    private XHAllAdControl xhAllAdControl;
    private ArrayList<Map<String, String>> adArray = new ArrayList<>();

    public FragmentNous(BaseActivity act, String url, String name) {
        this.mAct = act;
        this.url = url;
        mAds.add(2);
        mAds.add(8);
        mAds.add(16);
        mAds.add(23);
        mAds.add(33);
        mAds.add(43);
    }


    private void getAdData() {
        adArray.clear();
        ArrayList<String> adPosList = new ArrayList<>();
        Collections.addAll(adPosList, MAIN_HOME_ZHISHI_LIST);
        xhAllAdControl = new XHAllAdControl(adPosList, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(Map<String, String> map) {
                if (map != null && map.size() > 0) {
                    for (String adKey : MAIN_HOME_ZHISHI_LIST) {
                        String adStr = map.get(adKey);
                        if (!TextUtils.isEmpty(adStr)) {
                            ArrayList<Map<String, String>> adList = StringManager.getListMapByJson(adStr);
                            if (adList != null && adList.size() > 0) {
                                Map<String, String> adDataMap = adList.get(0);
                                adArray.add(adDataMap);
                            }
                        }
                    }
                    adCount = adArray.size();
                    SyntaxTools.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setDataNous();
                        }
                    });
                }
            }
        }, mAct, "other_top_list");
    }

    /**
     * 刷新数据
     */
    private void setDataNous() {
        for (int i = 0; i < adCount; i++) {
            if (i == mAds.size() - 1) {
                mAds.add(mAds.get(i) + 9);
            }
            if (listDataNous.size() > mAds.get(i)) {
                Map<String, String> twiceMap = listDataNous.get(mAds.get(i));
                twiceMap.put("ad", "show");
            }
            adapter.notifyDataSetChanged();
        }
    }

    public FragmentNous() {
        super();
    }

    //当ViewPager切换到其它界面,此方法会重新执行
    @SuppressLint("InflateParams")
    public View onCreateView() {
        view = LayoutInflater.from(mAct).inflate(R.layout.a_nous_fragment, null);
        loadManager = mAct.loadManager;
        currentPage = 0;
        LoadOver = false;
        return view;
    }

    public View getView() {
        return listNous;
    }

    @SuppressLint("HandlerLeak")
    public void init() {
        // 结果显示
        mAct.loadManager.showProgressBar();
        listNous = (DownRefreshList) view.findViewById(R.id.nous_list);
        listNous.setDivider(null);
        listDataNous = new ArrayList<>();
        adapter = new AdapterNousHome(mAct, listNous, listDataNous,
                R.layout.a_nous_item,
                new String[]{"img", "title", "allClick"},
                new int[]{R.id.iv_nousCover, R.id.tv_nousTitle, R.id.tv_allClick}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Map<String, String> map = listDataNous.get(position);
                if ("hide".equals(map.get("ad"))) {
                    view.findViewById(R.id.ad_layout).setVisibility(View.GONE);
                } else if (adArray.size() != 0) {
                    final RelativeLayout adLayout = (RelativeLayout) view.findViewById(R.id.ad_layout);
                    int adIndex = -1;
                    for (int i = 0; i < mAds.size(); i++) {
                        if (position == mAds.get(i)) {
                            adIndex = i;
                            break;
                        }
                    }

                    if (adIndex > -1 && adArray != null && adIndex < adArray.size()) {
                        final Map<String, String> map2 = adArray.get(adIndex);
                        if (map2 != null && map2.size() > 0) {
                            setAdView(adLayout, map2);
                            if (!"2".equals(map2.get("isShow"))) {
                                xhAllAdControl.onAdBind(Integer.valueOf(map2.get("index")), adLayout, (adIndex + 1) + "");
                                map2.put("isShow", "2");
                            }

                            final int finalAdIndex = adIndex;
                            view.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    xhAllAdControl.onAdClick(Integer.valueOf(map2.get("index")), (finalAdIndex + 1) + "");
                                }
                            });

                            adLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }

                if (position == 0 && view.findViewById(R.id.ad_layout).getVisibility() == View.GONE) {
                    view.findViewById(R.id.bottom_line).setVisibility(View.GONE);
                } else view.findViewById(R.id.bottom_line).setVisibility(View.VISIBLE);
                return view;
            }

            /**
             * 设置广告view
             * @param adLayout
             * @param map
             */

            private void setAdView(RelativeLayout adLayout,Map<String,String> map) {
                View view;
                if (adLayout.getChildCount() <= 0) {
                    view = LayoutInflater.from(mAct).inflate(R.layout.ad_baidu_view_nous, adLayout);
                } else {
                    view = adLayout.getChildAt(0);
                }
                if (view != null) {
                    ImageView img = (ImageView) view.findViewById(R.id.iv_nousCover_ad);
                    if (!TextUtils.isEmpty(map.get("imgUrl"))) {
                        setViewImage(img, map.get("imgUrl"));
                    } else if (!TextUtils.isEmpty(map.get("iconUrl"))) {
                        setViewImage(img, map.get("iconUrl"));
                    }
                    TextView textName = (TextView) view.findViewById(R.id.tv_nousTitle_ad);
                    TextView textAllClick = (TextView) view.findViewById(R.id.tv_allClick_ad);
                    setViewText(textName, map.get("title") + "，" + map.get("desc"));
                    Random random = new Random();
                    int v = random.nextInt(5000 - 3000) + 3000;
                    setViewText(textAllClick, v + "浏览");

                    if("1".equals(map.get("adType"))){
                        ((TextView)view.findViewById(R.id.tv_ad_tag)).setText("香哈");
                    }else{
                        ((TextView)view.findViewById(R.id.tv_ad_tag)).setText("广告");
                    }
                }
            }
        };
        adapter.contentWidth = ToolsDevice.getWindowPx(mAct).widthPixels - Tools.getDimen(mAct, R.dimen.dp_120);//12=15*2+80+10

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;
                switch (what) {
                    case MSG_NOUS_OK: // tab Hot数据加载完成;
                        mAct.loadManager.hideProgressBar();
                        listNous.setVisibility(View.VISIBLE);
                        break;
                }
            }
        };
        getData();

    }

    private void getData() {
        if (!LoadOver) {
            mAct.loadManager.showProgressBar();
            loadManager.setLoading(listNous, adapter, true, new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    getNousData(false);
                }
            }, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getNousData(true);
                }
            });
            LoadOver = true;
            getAdData();
        }
    }

    public void refresh() {
        listNous.setSelection(0);
        listNous.onRefreshStart();
        getNousData(true);
        getAdData();
    }

    private void getNousData(final boolean isForward) {
        if (isForward) {
            currentPage = 1;
        } else
            currentPage++;
        mAct.loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage, listDataNous.size() == 0);
        String getUrl;
        if (url.equals("")) {
            getUrl = StringManager.api_nousList + "?type=new" + "&page=" + currentPage;
        } else {
            getUrl = StringManager.api_nousList + "?type=classify&pinyin=" + url + "&page=" + currentPage;
        }
        ReqInternet.in().doGet(getUrl, new InternetCallback(mAct) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                int loadCount = 0;
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    if (isForward) listDataNous.clear();
                    if (currentPage == 1 && returnObj.toString().length() < 100) {
                        return;
                    }
                    // 解析数据
                    ArrayList<Map<String, String>> list = UtilString.getListMapByJson(returnObj);
                    Map<String, String> map = list.get(0);
                    if (map.containsKey("activity")) {
                        ArrayList<Map<String, String>> list2 = UtilString.getListMapByJson(map.get("activity"));
                        if (list2.size() > 0) {
                            listDataNous.add(list2.get(0));
                        }
                    }
                    ArrayList<Map<String, String>> list3 = UtilString.getListMapByJson(map.get("nous"));
                    for (int i = 0; i < list3.size(); i++) {
                        loadCount++;
                        Map<String, String> map2 = new HashMap<>();
                        map2.put("img", list3.get(i).get("img"));
                        map2.put("title", list3.get(i).get("title"));
                        map2.put("content", list3.get(i).get("content"));
                        map2.put("allClick", list3.get(i).get("allClick") + "浏览");
                        map2.put("code", list3.get(i).get("code"));
                        map2.put("soruce", list3.get(i).get("soruce"));
                        map2.put("ad", "hide");
                        listDataNous.add(map2);
                    }
                    if (adCount > 0) {
                        for (int i = 0; i < mAds.size(); i++) {
                            if (listDataNous.size() > mAds.get(i) && adCount > i) {
                                Map<String, String> twiceMap = listDataNous.get(mAds.get(i));
                                twiceMap.put("ad", "show");
                            }
                        }
                    }
                    handler.sendEmptyMessage(MSG_NOUS_OK);
                    adapter.notifyDataSetChanged();
                    // 如果是重新加载的,选中第一个tab.
                    if (isForward)
                        listNous.setSelection(1);
                }
                if (everyPage == 0)
                    everyPage = loadCount;
                currentPage = loadManager.changeMoreBtn(listNous, flag, everyPage, loadCount, currentPage, listDataNous.size() == 0);
                listNous.setVisibility(View.VISIBLE);
                listNous.onRefreshComplete();
            }
        });
    }

}
