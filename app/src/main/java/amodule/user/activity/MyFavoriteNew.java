package amodule.user.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.FavoriteHelper;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import amodule.article.view.BottomDialog;
import amodule.main.Main;
import amodule.main.activity.MainHome;
import amodule.main.activity.MainHomePage;
import amodule.user.adapter.AdapterModuleS0;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;

import static amodule.main.Main.TAB_HOME;

/**
 * 我的收藏页面改版
 */
public class MyFavoriteNew extends BaseActivity implements View.OnClickListener {

    private ArrayList<Map<String, String>> mData = new ArrayList<>();
    private PtrClassicFrameLayout refreshLayout;
    private RelativeLayout seekLayout;
    private LinearLayout noDataLayout;
    private RvListView rvListview;
    private AdapterModuleS0 myFavorite;
    private int currentpage = 0, everyPage = 0;//页面号码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.a_my_favorite);
        initUi();
        initData();
    }

    private void initUi() {
        TextView title = (TextView) findViewById(R.id.title);
        title.setText("我的收藏");
        TextView rightText = (TextView) findViewById(R.id.rightText);
        rightText.setText("浏览历史");
        rightText.setVisibility(View.VISIBLE);
        rightText.setTextColor(Color.parseColor("#999999"));
        refreshLayout = (PtrClassicFrameLayout) findViewById(R.id.refresh_list_view_frame);
        rvListview = (RvListView) findViewById(R.id.rvListview);
        seekLayout = (RelativeLayout) findViewById(R.id.seek_layout);
        noDataLayout = (LinearLayout) findViewById(R.id.noData_layout);

        seekLayout.setOnClickListener(this);
        rightText.setOnClickListener(this);
        findViewById(R.id.title).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.btn_no_data).setOnClickListener(this);

        refreshLayout.getHeader().setBackgroundColor(Color.parseColor("#f2f2f2"));
    }

    private void initData() {
        myFavorite = new AdapterModuleS0(this, mData);
        myFavorite.setStatisticId("a_my_collection");
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable drawable = getResources().getDrawable(R.drawable.item_decoration);
        itemDecoration.setDrawable(drawable);
        rvListview.addItemDecoration(itemDecoration);
        rvListview.setOnItemClickListener((view, holder, position) -> {
            if (mData == null || position < 0 || position >= mData.size())
                return;
            Map<String, String> item = mData.get(position);
            if (item == null) {
                return;
            }
            Map<String, String> itemParameter = StringManager.getFirstMap(item.get("parameter"));
            AppCommon.openUrl(MyFavoriteNew.this, itemParameter.get("url"), true);
        });
        rvListview.setOnItemLongClickListener((view, holder, position) -> {
            showBottomDialog(position);
            return true;
        });
        loadManager.setLoading(refreshLayout,
                rvListview, myFavorite, true,
                v -> requestData(true),
                v -> requestData(false)
        );
        loadManager.getSingleLoadMore(rvListview).setBackgroundColor(Color.parseColor("#F2F2F2"));
    }

    /**
     * 请求数据
     */
    private void requestData(final boolean isRefresh) {
        currentpage = isRefresh ? 1 : ++currentpage;
        everyPage = isRefresh ? 0 : everyPage;
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("page", String.valueOf(currentpage));
        loadManager.changeMoreBtn(ReqInternet.REQ_OK_STRING, -1, -1, currentpage, false);
        ReqEncyptInternet.in().doEncypt(StringManager.API_COLLECTIONLIST, params, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                int loadCount = 0;
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    if (isRefresh)
                        mData.clear();
                    Map<String, String> maps = StringManager.getFirstMap(msg);
                    if (maps.containsKey("list") && !TextUtils.isEmpty(maps.get("list"))) {
                        ArrayList<Map<String, String>> listMaps = StringManager.getListMapByJson(maps.get("list"));
                        loadCount = listMaps.size();
                        mData.addAll(listMaps);
                        myFavorite.notifyDataSetChanged();
                    }
                }
                if (everyPage == 0) {
                    everyPage = loadCount;
                }
                if (isRefresh) {
                    refreshLayout.refreshComplete();
                }
                loadManager.changeMoreBtn(flag, everyPage, loadCount, currentpage, mData.isEmpty());
                handlerNoDataLayout();
            }
        });
    }

    private void handlerNoDataLayout() {
        if (mData == null) {
            return;
        }
        final boolean dataIsEmpty = mData.isEmpty();
        noDataLayout.setVisibility(dataIsEmpty ? View.VISIBLE : View.GONE);
        rvListview.setVisibility(dataIsEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title:
                gotoTop();
                break;
            case R.id.back:
                finish();
                break;
            case R.id.rightText:
                startActivity(new Intent(this, BrowseHistory.class));
                break;
            case R.id.seek_layout:
                startActivity(new Intent(this, SreachFavoriteActivity.class));
                break;
            //回到首页第一页
            case R.id.btn_no_data:
                if (Main.allMain != null) {
                    Main.allMain.setCurrentTabByClass(MainHome.class);
                    if (Main.allMain.allTab != null && Main.allMain.allTab.get(MainHomePage.KEY) != null) {
                        ((MainHome) Main.allMain.allTab.get(MainHomePage.KEY)).setCurrentTab(TAB_HOME);
                    }
                }
                Main.colse_level = 1;
                finish();
                break;
            default:
                break;
        }
    }

    long firstClickTime = 0;

    private void gotoTop() {
        if (firstClickTime == 0) {
            firstClickTime = System.currentTimeMillis();
        } else {
            long currentTime = System.currentTimeMillis();
            if (currentTime - firstClickTime < 500) {
                rvListview.scrollToPosition(0);
            }
            firstClickTime = 0;
        }
    }

    private void showBottomDialog(final int position) {
        if (mData == null || position < 0 || position >= mData.size())
            return;
        Map<String, String> item = mData.get(position);
        if (item == null) {
            return;
        }
        Map<String, String> itemParameter = StringManager.getFirstMap(item.get("parameter"));
        if (itemParameter.isEmpty()) return;
        final String code = itemParameter.get("code");
        final String type = itemParameter.get("type");
        if (TextUtils.isEmpty(code) || TextUtils.isEmpty(type)) {
            return;
        }
        Map<String, String> itemB = StringManager.getFirstMap(item.get("B"));
        final String typeName = itemB.get("text1");
        BottomDialog dialog = new BottomDialog(this);
        dialog.addButton("取消收藏",
                v -> FavoriteHelper.instance().setFavoriteStatus(MyFavoriteNew.this, code, typeName, type,
                        new FavoriteHelper.FavoriteStatusCallback() {
                            @Override
                            public void onSuccess(boolean state) {
                                if (state) {
                                    return;
                                }
                                mData.remove(position);
                                rvListview.notifyItemViewRemove(position);
                                handlerNoDataLayout();
                            }

                            @Override
                            public void onFailed() {
                            }
                        }));
        dialog.show();
    }

}