package amodule.search.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.search.adapter.AdapterSearchUser;
import amodule.user.activity.FriendHome;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * Created by ：airfly on 2016/10/17 20:34.
 */

public class HaYouSearchResultView extends RelativeLayout {

    private BaseActivity mActivity;
    private ListView listView;
    private ArrayList<Map<String, String>> arrayList = new ArrayList<Map<String, String>>();
    private String searchKey;
    private LoadManager loadManager;
    private int currentPage = 0;
    private int everyPage = 0;
    private AdapterSearchUser customerAdapter;
    private LinearLayout ll_noData;

    public HaYouSearchResultView(Context context) {
        this(context, null);
    }

    public HaYouSearchResultView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HaYouSearchResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.a_search_cutomer_new, this, true);
    }


    public void init(BaseActivity activity) {
        mActivity = activity;
        loadManager = mActivity.loadManager;
        initView();
    }

    private void initView() {

        listView = (ListView) findViewById(R.id.customer_lv_showResult);
        listView.setDivider(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                Intent intent = new Intent(mActivity, FriendHome.class);
                Bundle bundle = new Bundle();
                bundle.putString("code", arrayList.get(position).get("code"));
                intent.putExtras(bundle);
                mActivity.startActivity(intent);
            }
        });
        customerAdapter = new AdapterSearchUser(mActivity, listView, arrayList,
                R.layout.a_my_item_fans_search,
                new String[]{"img", "nickName", "folState"},
                new int[]{R.id.fans_user_img, R.id.fans_user_name, R.id.fans_user_item_choose});
        customerAdapter.roundImgPixels = ToolsDevice.dp2px(mActivity, 500);
        customerAdapter.scaleType = ImageView.ScaleType.CENTER_CROP;
        ll_noData = (LinearLayout) findViewById(R.id.v_no_data_search);
        ll_noData.setVisibility(View.GONE);
    }


    public void search(String key) {

        if (TextUtils.isEmpty(key))
            return;

        searchKey = key;
        currentPage = 0;
        arrayList.clear();
        resultLoad();

    }

    private void resultLoad() {
        mActivity.loadManager.showProgressBar();
        loadManager.setLoading(listView, customerAdapter, true, new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!searchKey.equals("")) {
                    getData();
                } else {
                    mActivity.loadManager.hideProgressBar();
                }
            }
        });
    }


    private void getData() {
        mActivity.loadManager.showProgressBar();
        currentPage++;
        loadManager.loading(listView, arrayList.size() == 0);
        String url = StringManager.api_soList + "?type=customer&s=" + searchKey + "&page=" + currentPage;
        ReqInternet.in().doGet(url, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                int loadPage = 0;
                if (flag >= UtilInternet.REQ_OK_STRING) { // 表示成功
                    if (currentPage == 1)
                        arrayList.clear();
                    Map<String, String> map = StringManager.getFirstMap(returnObj);
//                    if (map.containsKey("soCi")) {
//                        ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(map.get("soCi"));
//                        customerAdapter.setSearchWords(getSoCiArray(listReturn));
//                    }
                    if (map.containsKey("customers") && !map.get("customers").equals("null")) {
                        ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(map.get("customers"));
                        for (Map<String, String> mapReturn : listReturn) {
                            mapReturn.put("folState", "folState" + mapReturn.get("folState"));
                            mapReturn.put("lv", "lv" + mapReturn.get("lv"));
                            arrayList.add(mapReturn);
                        }
                        loadPage = listReturn.size();
                        customerAdapter.notifyDataSetChanged();
                    }
                    hideProgresBar();
                }
                if (everyPage == 0)
                    everyPage = loadPage;
                // 如果总数据为空,显示没有消息
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    ll_noData.setVisibility(arrayList.size() == 0 ? View.VISIBLE : View.GONE);
                    listView.setVisibility(arrayList.size() == 0 ? View.GONE : View.VISIBLE);
                    loadManager.hideLoadFaildBar();
                } else {
                    ll_noData.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                }

                loadManager.loadOver(flag,listView, loadPage);
            }
        });
    }

    private void hideProgresBar() {
        loadManager.hideProgressBar();
    }

    //获取搜索词数组
    private String[] getSoCiArray(ArrayList<Map<String, String>> listReturn) {
        int length = listReturn.size();
        String[] str = new String[length];
        for (int i = 0; i < length; i++)
            str[i] = listReturn.get(i).get("");
        return str;
    }


}
