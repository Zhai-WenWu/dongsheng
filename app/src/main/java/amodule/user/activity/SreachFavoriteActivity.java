package amodule.user.activity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
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
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.RvListView;
import amodule._common.conf.FavoriteTypeEnum;
import amodule._common.conf.GlobalFavoriteModule;
import amodule._common.conf.GlobalVariableConfig;
import amodule.article.view.BottomDialog;
import amodule.user.adapter.AdapterModuleS0;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;

/**
 * Description :
 * PackageName : amodule.user.activity
 * Created by MrTrying on 2017/11/2 15:29.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class SreachFavoriteActivity extends BaseActivity implements View.OnClickListener {

    private PtrClassicFrameLayout mRefreshLayout;
    private RvListView mRvListview;
    private EditText mEditText;
    private RelativeLayout mNoDataLayout;
    private TextView mClearSearch;

    private ArrayList<Map<String, String>> mSearchData = new ArrayList<>();
    private AdapterModuleS0 mAdapter;
    private int currentpage = 0, everyPage = 0;//页面号码
    private String searchWord = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.a_search_favorite);
        initUI();
        initData();
    }

    private void initUI() {
        mRefreshLayout = (PtrClassicFrameLayout) findViewById(R.id.refresh_list_view_frame);
        mRvListview = (RvListView) findViewById(R.id.rvListview);
        mEditText = (EditText) findViewById(R.id.ed_search_main);
        mClearSearch = (TextView) findViewById(R.id.btn_ed_clear_main);
        mNoDataLayout = (RelativeLayout) findViewById(R.id.no_data_layout);

        mClearSearch.setOnClickListener(this);
        findViewById(R.id.btn_search_main).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable drawable = getResources().getDrawable(R.drawable.item_decoration);
        itemDecoration.setDrawable(drawable);
        mRvListview.addItemDecoration(itemDecoration);
        mRvListview.setOnItemClickListener(new RvListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                if(mSearchData == null || position < 0 || position >= mSearchData.size())
                    return;
                Map<String, String> item = mSearchData.get(position);
                if(item == null){
                    return;
                }
                Map<String, String> itemParameter =  StringManager.getFirstMap(item.get("parameter"));
                AppCommon.openUrl(SreachFavoriteActivity.this,itemParameter.get("url"),true);
            }
        });
        mRvListview.setOnItemLongClickListener(new RvListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                showBottomDialog(position);
                return true;
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });
        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.ACTION_DOWN:
                        doSearch();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void initData() {
        mAdapter = new AdapterModuleS0(this, mSearchData);
        mAdapter.setStatisticId("a_my_collection_search");
        loadManager.setLoading(mRefreshLayout, mRvListview, mAdapter, true,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestData(true);
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestData(false);
                    }
                });
        loadManager.getSingleLoadMore(mRvListview).setBackgroundColor(Color.parseColor("#F2F2F2"));
        loadManager.hideProgressBar();
    }

    /**
     * 请求数据
     */
    private void requestData(final boolean isRefresh) {
        if (TextUtils.isEmpty(searchWord)) {
            if (isRefresh)
                mRefreshLayout.refreshComplete();
            return;
        }
        currentpage = isRefresh ? 1 : ++currentpage;
        everyPage = isRefresh ? 0 : everyPage;
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("page", String.valueOf(currentpage));
        params.put("name", searchWord);
        loadManager.loading(mRvListview,false);
        ReqEncyptInternet.in().doEncypt(StringManager.API_COLLECTIONLIST, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object msg) {
                int loadCount = 0;
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    if (isRefresh)
                        mSearchData.clear();
                    Map<String, String> maps = StringManager.getFirstMap(msg);
                    if (maps.containsKey("list") && !TextUtils.isEmpty(maps.get("list"))) {
                        ArrayList<Map<String, String>> listMaps = StringManager.getListMapByJson(maps.get("list"));
                        loadCount = listMaps.size();
                        mSearchData.addAll(listMaps);
                        mAdapter.notifyDataSetChanged();
                    }
                }
                if (everyPage == 0) {
                    everyPage = loadCount;
                }
                if (isRefresh)
                    mRefreshLayout.refreshComplete();
                handlerNoData();
                loadManager.loadOver(flag,mRvListview,loadCount);
            }
        });
    }

    private void handlerNoData() {
        if (mSearchData == null) {
            return;
        }
        final boolean dataIsEmpty = mSearchData.isEmpty();
        mNoDataLayout.setVisibility(dataIsEmpty ? View.VISIBLE : View.GONE);
        mRefreshLayout.setVisibility(dataIsEmpty ? View.GONE : View.VISIBLE);
    }

    private void doSearch() {
        searchWord = mEditText.getText().toString().trim();
        if (TextUtils.isEmpty(searchWord)) {
            Tools.showToast(this, "请输入查询关键词");
            return;
        }
        ToolsDevice.keyboardControl(false, this, mEditText);
        mSearchData.clear();
        requestData(true);
    }

    private void clearSearch() {
        searchWord = "";
        mEditText.setText("");
        mEditText.clearFocus();
        ToolsDevice.keyboardControl(false, this, mEditText);
//        mSearchData.clear();
//        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ed_clear_main:
                clearSearch();
                break;
            case R.id.btn_search_main:
                doSearch();
                break;
            case R.id.btn_back:
                finish();
                break;
            default:
                break;
        }
    }

    private void showBottomDialog(final int position) {
        if(mSearchData == null || position < 0 || position >= mSearchData.size())
            return;
        Map<String, String> item = mSearchData.get(position);
        if(item == null){
            return;
        }
        Map<String, String> itemParameter =  StringManager.getFirstMap(item.get("parameter"));
        if (itemParameter.isEmpty()) return;
        final String code = itemParameter.get("code");
        final String type = itemParameter.get("type");
        if (TextUtils.isEmpty(code) || TextUtils.isEmpty(type)) {
            return;
        }
        Map<String, String> itemB = StringManager.getFirstMap(item.get("B"));
        final String typeName = itemB.get("text1");
        BottomDialog dialog = new BottomDialog(this);
        dialog.addButton("取消收藏", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavoriteHelper.instance().setFavoriteStatus(SreachFavoriteActivity.this, code,  typeName, FavoriteTypeEnum.getTypeEnumByStr(type),
                        new FavoriteHelper.FavoriteStatusCallback() {
                            @Override
                            public void onSuccess(boolean state) {
                                if(state){
                                    return;
                                }
                                mSearchData.remove(position);
                                mRvListview.notifyItemViewRemove(position);
                                handlerNoData();

                                GlobalFavoriteModule favoriteModule = new GlobalFavoriteModule();
                                favoriteModule.setFavCode(code);
                                favoriteModule.setFav(state);
                                favoriteModule.setFavType(FavoriteTypeEnum.getTypeEnumByStr(type));
                                GlobalVariableConfig.handleFavoriteModule(favoriteModule);

                            }

                            @Override
                            public void onFailed() {
                            }
                        });
            }
        });
        dialog.show();
    }


}
