package amodule.main.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.xiangha.R;

import acore.override.activity.mian.MainBaseActivity;
import acore.widget.rvlistview.RvListView;
import amodule.main.view.item.HomeItem;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;

/**
 * Created by sll on 2017/11/13.
 */

public class HomeSecondListActivity extends MainBaseActivity {

    private int mHeaderCount;

    private PtrClassicFrameLayout mPtrFrameLayout;
    private RvListView mRv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.home_second_list_layout);
        initData();
        initView();
        addListener();
    }

    private void initData() {

    }

    private void addListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.back:
                        HomeSecondListActivity.this.finish();
                        break;
                }
            }
        };
        findViewById(R.id.back).setOnClickListener(listener);
        mRv.setOnItemClickListener(new RvListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                if (view instanceof HomeItem) {
                    ((HomeItem)view).onClickEvent(view);
                }
            }
        });
    }

    private void initView() {
        mRv = (RvListView) findViewById(R.id.recycler_view);
        mRv.addHeaderView(createHeaderView());
        mHeaderCount++;
        mPtrFrameLayout = (PtrClassicFrameLayout) findViewById(R.id.refresh_list_view_frame);
        mPtrFrameLayout.disableWhenHorizontalMove(true);
    }

    private View createHeaderView() {
        return null;
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


}
