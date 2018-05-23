package amodule.user.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.popdialog.util.Tools;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.FavoriteHelper;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import acore.widget.LayoutScroll;
import acore.widget.rvlistview.RvListView;
import amodule.article.view.BottomDialog;
import amodule.home.activity.HomeSecondListActivity;
import amodule.user.activity.login.LoginByAccout;
import amodule.user.adapter.AdapterModuleS0;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;

import static acore.tools.ObserverManager.NOTIFY_FAVORITE;
import static acore.tools.ObserverManager.NOTIFY_LOGIN;
import static acore.tools.ObserverManager.NOTIFY_LOGOUT;

/**
 * 我的收藏页面改版
 */
public class MyFavorite extends BaseAppCompatActivity implements View.OnClickListener, IObserver{
    private ArrayList<Map<String, String>> mData = new ArrayList<>();
    private LayoutScroll mLayoutScroll;
    private PtrClassicFrameLayout refreshLayout;
    private LinearLayout noDataLayout;
    private RvListView rvListview;
    private AdapterModuleS0 myFavorite;
    private int currentpage = 0, everyPage = 0;//页面号码
    private String pageTime;

    private final String mStatisticId = "a_my_collection";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.a_my_favorite);
        initUi();
        initData();
        ObserverManager.getInstance().registerObserver(this, NOTIFY_LOGIN, NOTIFY_LOGOUT, NOTIFY_FAVORITE);
    }

    private void initUi() {
        TextView title = (TextView) findViewById(R.id.title);
        title.setText("我的收藏");
        mLayoutScroll = (LayoutScroll) findViewById(R.id.scroll_body);
        refreshLayout = (PtrClassicFrameLayout) findViewById(R.id.refresh_list_view_frame);
        rvListview = (RvListView) findViewById(R.id.rvListview);
        //屏幕高-（topbar高 + 底部导航高）+ 搜索框高
        refreshLayout.post(()->refreshLayout.getLayoutParams().height = ToolsDevice.getWindowPx(this).heightPixels - Tools.getDimen(this,R.dimen.dp_53));

        noDataLayout = (LinearLayout) findViewById(R.id.noData_layout);
        mLayoutScroll.init(acore.tools.Tools.getDimen(this,R.dimen.dp_45));
        mLayoutScroll.setTouchView(rvListview);
        mLayoutScroll.setTouchView(refreshLayout);

        findViewById(R.id.seek_layout).setOnClickListener(this);
        findViewById(R.id.title).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.btn_no_data).setOnClickListener(this);

        refreshLayout.getHeader().setBackgroundColor(Color.parseColor("#f2f2f2"));
    }

    private void initData() {
        myFavorite = new AdapterModuleS0(this, mData);
        myFavorite.setStatisticId(mStatisticId);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable drawable = getResources().getDrawable(R.drawable.item_decoration);
        itemDecoration.setDrawable(drawable);
        rvListview.addItemDecoration(itemDecoration);
        rvListview.setOnItemClickListener((view, holder, position) -> {
            if (mData == null || position < 0 || position >= mData.size())
                return;
            Map<String, String> item = mData.get(position);
            if (item == null)
                return;
            Map<String, String> itemParameter = StringManager.getFirstMap(item.get("parameter"));
            AppCommon.openUrl(MyFavorite.this, itemParameter.get("url"), true);
        });
        rvListview.setOnItemLongClickListener((view, holder, position) -> {
            showBottomDialog(position);
            return true;
        });
        loadManager.setLoading(refreshLayout,
                rvListview, myFavorite, true,
                v -> requestData(true),
                v -> requestData(false));
        loadManager.getSingleLoadMore(rvListview).setBackgroundColor(Color.parseColor("#F2F2F2"));
        View emptyFooter = new View(this);
        emptyFooter.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,Tools.getDimen(this,R.dimen.dp_45)));
        rvListview.addFooterView(emptyFooter);
    }

    /** 请求数据 */
    private void requestData(final boolean isRefresh) {
        if (!LoginManager.isLogin()) {
            loadManager.hideProgressBar();
            return;
        }
        currentpage = isRefresh ? 1 : ++currentpage;
        everyPage = isRefresh ? 0 : everyPage;
        if (isRefresh)
            pageTime = "";
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("page", String.valueOf(currentpage));
        if (!TextUtils.isEmpty(pageTime)) {
            params.put("pageTime", pageTime);
        }
        loadManager.changeMoreBtn(ReqInternet.REQ_OK_STRING, -1, -1, currentpage, mData.isEmpty());
        ReqEncyptInternet.in().doEncypt(StringManager.API_COLLECTIONLIST, params, new InternetCallback() {
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
                    if (maps.containsKey("pageTime") && !TextUtils.isEmpty(maps.get("pageTime"))
                            && TextUtils.isEmpty(pageTime)) {
                        pageTime = maps.get("pageTime");
                    }
                }
//                if (everyPage == 0) {
//                    everyPage = loadCount;
//                }
                //服务端数据有问题
                everyPage = 5;
                if (isRefresh) {
                    refreshLayout.refreshComplete();
                }
                loadManager.changeMoreBtn(flag, everyPage, loadCount, currentpage, mData.isEmpty());
                handlerNoDataLayout();
            }
        });
    }

    private void handlerNoDataLayout() {
        if (mData == null)
            return;
        final boolean dataIsEmpty = mData.isEmpty();
        noDataLayout.setVisibility(dataIsEmpty ? View.VISIBLE : View.GONE);
        rvListview.setVisibility(dataIsEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!LoginManager.isLogin() && loadManager != null)
            loadManager.hideProgressBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstance().unRegisterObserver(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                XHClick.mapStat(this, mStatisticId, "点击返回", "");
                break;
            case R.id.title:
                gotoTop();
                break;
            case R.id.seek_layout:
                gotoSearch();
                XHClick.mapStat(this, mStatisticId, "点击搜索框", "");
                break;
            //回到首页第一页
            case R.id.btn_no_data:
                gotoPage();
                break;
            default:
                break;
        }
    }

    private void gotoSearch() {
        startActivity(new Intent(this, SreachFavoriteActivity.class));
    }

    private void gotoPage() {
        startActivity(new Intent(this, HomeSecondListActivity.class).putExtra(HomeSecondListActivity.TAG, "day"));
    }

    private void gotoLogin() {
        startActivity(new Intent(this, LoginByAccout.class));
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

    private BottomDialog mBottomDialog;
    private void showBottomDialog(final int position) {
        if (mData == null || position < 0 || position >= mData.size())
            return;
        Map<String, String> item = mData.get(position);
        if (item == null)
            return;
        Map<String, String> itemParameter = StringManager.getFirstMap(item.get("parameter"));
        if (itemParameter.isEmpty()) return;
        final String code = itemParameter.get("code");
        final String type = itemParameter.get("type");
        if (TextUtils.isEmpty(code) || TextUtils.isEmpty(type)) {
            return;
        }
        Map<String, String> itemB = StringManager.getFirstMap(item.get("B"));
        final String typeName = itemB.get("text1");
        if(mBottomDialog == null){
            mBottomDialog = new BottomDialog(this);
            mBottomDialog.addButton("取消收藏",
                    v ->  {
                FavoriteHelper.instance().setFavoriteStatus(this, code, typeName, type, null);
                XHClick.mapStat(this, mStatisticId, "点击取消收藏按钮", "");
            }
            );
            mBottomDialog.setCannleClick("", v -> {XHClick.mapStat(this, mStatisticId, "点击取消", "");});
        }else{
            mBottomDialog.setItemClick(0,
                    v -> {
                        FavoriteHelper.instance().setFavoriteStatus(this, code, typeName, type, null);
                        XHClick.mapStat(this, mStatisticId, "点击取消收藏按钮", "");
                    }
            );
        }
        mBottomDialog.show();
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        switch (name) {
            case NOTIFY_LOGIN:
                if (data != null && data instanceof Boolean && (Boolean) data) {
                    requestData(true);
                }
                break;
            case NOTIFY_LOGOUT:
                if (data != null && data instanceof Boolean && (Boolean) data) {
                    //清空数据
                    mData.clear();
                    myFavorite.notifyDataSetChanged();
                }
                break;
            case NOTIFY_FAVORITE:
                if (data != null && data instanceof Map) {
                    Map<String, String> mapData = (Map<String, String>)data;
                    Map<String,String> map = StringManager.getFirstMap(mapData.get("data"));
                    String state = map.get("state");
                    if (TextUtils.equals(state, "2")) {//收藏
                        if (LoginManager.isLogin() && !map.isEmpty()) {
                            mData.add(0, map);
                            rvListview.notifyItemViewInserted(0);
                        }
                    } else {//取消收藏
                        if (LoginManager.isLogin() && !map.isEmpty()) {
                            removeData(map);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private void removeData(Map data) {
        Map<String, String> map = StringManager.getFirstMap(data.get("parameter"));
        String type1 = map.get("type");
        String code1 = map.get("code");
        for (int i = 0; i < mData.size(); i ++) {
            Map<String, String> value = mData.get(i);
            Map<String, String> parameterMap = StringManager.getFirstMap(value.get("parameter"));
            String type2 = parameterMap.get("type");
            String code2 = parameterMap.get("code");
            if (TextUtils.equals(type1, type2) && TextUtils.equals(code1, code2)) {
                mData.remove(value);
                rvListview.notifyItemViewRemove(i);
                handlerNoDataLayout();
                break;
            }
        }
    }

    public void onRefresh() {
        if(!mLayoutScroll.isShow()){
            mLayoutScroll.show();
        }
        if (LoginManager.isLogin() && refreshLayout != null) {
            refreshLayout.autoRefresh();
            if(rvListview != null)
                rvListview.scrollToPosition(0);
        }
    }
}
