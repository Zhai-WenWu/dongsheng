package amodule.dish.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvGridView;
import amodule.dish.adapter.AdapterGridDish;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;

/**
 * Description :
 * PackageName : amodule.dish.activity
 * Created on 2017/12/1 16:18.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class GridDish extends BaseAppCompatActivity {

    public static final String EXTRA_CODE = "code";

    private PtrClassicFrameLayout mRefreshLayout;
    private RvGridView mGridView;
    private AdapterGridDish mAdapter;
    private List<Map<String, String>> mData = new ArrayList<>();

    private int mCurrentPage = 0;
    private int mEveryPageCount = 10;
    private LinkedHashMap<String, String> params = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("课程选集", 2, 0, R.layout.c_view_title_bar, R.layout.a_dish_grid);
        initExtra();
        initUI();
    }

    private void initExtra() {
        Bundle bundle = getIntent().getExtras();
        String code = bundle.getString(EXTRA_CODE);
        code = "99399090";
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "数据错误", Toast.LENGTH_SHORT).show();
            finish();
        }
        params.put(EXTRA_CODE, code);
    }

    //初始化UI
    private void initUI() {
        mRefreshLayout = (PtrClassicFrameLayout) findViewById(R.id.refresh_list_view_frame);
        mGridView = (RvGridView) findViewById(R.id.rvGridView);
        final int padding_5 = Tools.getDimen(this, R.dimen.dp_5);
        final int padding_4 = Tools.getDimen(this, R.dimen.dp_4);
        mGridView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view) - mGridView.getHeaderViewsSize();
                outRect.top = (position == 0 || position == 1) ? padding_5 * 4 : padding_5;
                outRect.left = padding_4;
                outRect.right = padding_4;
                outRect.bottom = padding_5;
            }
        });
        mAdapter = new AdapterGridDish(this, mData);
        loadManager.setLoading(mRefreshLayout, mGridView, mAdapter, true,
                v -> refresh(),
                v -> loadData());
    }

    //刷新
    private void refresh() {
        mCurrentPage = 0;
        loadData();
    }

    private void loadData() {
        mCurrentPage++;
        loadManager.changeMoreBtn(ReqEncyptInternet.REQ_OK_STRING, -1, -1, mCurrentPage, mData.size() == 0);
        params.put("page", String.valueOf(mCurrentPage));
        ReqEncyptInternet.in().doEncypt(StringManager.api_getVideoList, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                int loadCount = 0;
                if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                    Map<String, String> temp = StringManager.getFirstMap(o);
                    ArrayList<Map<String, String>> data = StringManager.getListMapByJson(temp.get("data"));
                    loadCount = data.size();
                    Stream.of(data).forEach(map -> {
                        Map<String, String> dish = StringManager.getFirstMap(map.get("dish"));
                        transferData(map,dish,"time");
                        transferData(map,dish,"name");
                        transferData(map,dish,"image",map.get("image"));

                        mData.add(map);
                    });
                    if (mCurrentPage == 1) {
                        mAdapter.updateData(data);
                    } else {
                        mData.addAll(data);
                        mAdapter.notifyDataSetChanged();
                    }
                }
                if (mEveryPageCount == 0) {
                    mEveryPageCount = loadCount;
                }

                loadManager.changeMoreBtn(flag, mEveryPageCount, loadCount, mCurrentPage, false);
            }
        });
    }

    private void transferData(Map<String,String> target,Map<String,String> source,String key){
        transferData(target,source,key,"");
    }

    private void transferData(Map<String,String> target,Map<String,String> source,String key,String defaultValue){
        if(null == target
                || null == source
                || source.isEmpty()
                || TextUtils.isEmpty(key)){
            return;
        }
        String timeValue = source.get(key);
        target.put(key, TextUtils.isEmpty(timeValue) ? defaultValue : timeValue);
    }
}
