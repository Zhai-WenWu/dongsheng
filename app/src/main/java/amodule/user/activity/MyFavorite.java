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
import acore.logic.LoginManager;
import acore.logic.MessageTipController;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import amodule.article.view.BottomDialog;
import amodule.main.Main;
import amodule.main.activity.MainHomePage;
import amodule.main.delegate.ISetMessageTip;
import amodule.main.view.MessageTipIcon;
import amodule.user.activity.SreachFavoriteActivity;
import amodule.user.activity.login.LoginByAccout;
import amodule.user.adapter.AdapterModuleS0;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;

import static acore.tools.ObserverManager.NOTIFY_FAVORITE;
import static acore.tools.ObserverManager.NOTIFY_LOGIN;
import static acore.tools.ObserverManager.NOTIFY_LOGOUT;
import static acore.tools.ObserverManager.NOTIFY_UNFAVORITE;

/**
 * 我的收藏页面改版
 */
public class MyFavorite extends MainBaseActivity implements View.OnClickListener, IObserver, ISetMessageTip {
    public static final String KEY = "MyFavorite";
    private ArrayList<Map<String, String>> mData = new ArrayList<>();
    private PtrClassicFrameLayout refreshLayout;
    private MessageTipIcon mMessageTipIcon;
    private LinearLayout noDataLayout;
    private RelativeLayout noLoginLayout;
    private RvListView rvListview;
    private AdapterModuleS0 myFavorite;
    private int currentpage = 0, everyPage = 0;//页面号码
    private String pageTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_my_favorite);
        Main.allMain.allTab.put(KEY, this);
        initUi();
        initData();
        //未登录自动跳转去登录
        if (!LoginManager.isLogin()) {
            gotoLogin();
        }
        ObserverManager.getInstance().registerObserver(this, NOTIFY_LOGIN, NOTIFY_LOGOUT,
                NOTIFY_FAVORITE, NOTIFY_UNFAVORITE);
    }

    private void initUi() {
        TextView title = (TextView) findViewById(R.id.title);
        title.setText("我的收藏");
        mMessageTipIcon = (MessageTipIcon) findViewById(R.id.message_tip);
        refreshLayout = (PtrClassicFrameLayout) findViewById(R.id.refresh_list_view_frame);
        rvListview = (RvListView) findViewById(R.id.rvListview);
        noDataLayout = (LinearLayout) findViewById(R.id.noData_layout);
        noLoginLayout = (RelativeLayout) findViewById(R.id.no_login_rela);
        noLoginLayout.setVisibility(LoginManager.isLogin() ? View.GONE : View.VISIBLE);
        noLoginLayout.setOnClickListener(this);

        findViewById(R.id.noLogin_layout).setOnClickListener(this);
        findViewById(R.id.seek_layout).setOnClickListener(this);
        findViewById(R.id.title).setOnClickListener(this);
        findViewById(R.id.back).setVisibility(View.GONE);
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
        mMessageTipIcon.setMessage(MessageTipController.newInstance().getMessageNum());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstance().unRegisterObserver(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title:
                gotoTop();
                break;
            case R.id.noLogin_layout:
                gotoLogin();
                break;
            case R.id.seek_layout:
                gotoSearch();
                break;
            //回到首页第一页
            case R.id.btn_no_data:
                gotoHomePage();
                break;
            default:
                break;
        }
    }

    private void gotoSearch() {
        startActivity(new Intent(this, SreachFavoriteActivity.class));
    }

    private void gotoHomePage() {
        if (Main.allMain != null)
            Main.allMain.setCurrentTabByClass(MainHomePage.class);
        Main.colse_level = 1;
        finish();
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
        BottomDialog dialog = new BottomDialog(this);
        dialog.addButton("取消收藏",
                v -> FavoriteHelper.instance().setFavoriteStatus(this, code, typeName, type, null)
        ).show();
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        switch (name) {
            case NOTIFY_LOGIN:
                if (data != null && data instanceof Boolean && (Boolean) data) {
                    refreshLoginstatus();
                    requestData(true);
                }
                break;
            case NOTIFY_LOGOUT:
                if (data != null && data instanceof Boolean && (Boolean) data) {
                    refreshLoginstatus();
                    //清空数据
                    mData.clear();
                    myFavorite.notifyDataSetChanged();
                }
                break;
            case NOTIFY_FAVORITE:
                if (LoginManager.isLogin()
                        && data != null && data instanceof Map) {
                    mData.add(0, (Map<String, String>) data);
                    rvListview.notifyItemViewInserted(0);
                }
                break;
            case NOTIFY_UNFAVORITE:
                if (LoginManager.isLogin()
                        && data != null && data instanceof Map) {
                    removeData((Map) data);
                }
                break;
            default:
                break;
        }
    }

    private void removeData(Map data) {
        Map<String, String> map = StringManager.getFirstMap(data.get("parameter"));
        for (Map<String, String> value : mData) {
            Map<String, String> parameterMap = StringManager.getFirstMap(value.get("parameter"));
            if (TextUtils.equals(map.toString(), parameterMap.toString())) {
                int position = mData.indexOf(value);
                if (position >= 0 && position < mData.size()) {
                    mData.remove(position);
                    rvListview.notifyItemViewRemove(position);
                    handlerNoDataLayout();
                }
                break;
            }
        }
    }

    public void onRefresh() {
        if (LoginManager.isLogin() && refreshLayout != null) {
            refreshLayout.autoRefresh();
            if(rvListview != null)
                rvListview.scrollToPosition(0);
        }
    }

    private void refreshLoginstatus() {
        if (noLoginLayout != null)
            noLoginLayout.setVisibility(LoginManager.isLogin() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void setMessageTip(int tipCournt) {
        if (mMessageTipIcon != null) {
            mMessageTipIcon.setMessage(tipCournt);
        }
    }
}
