package amodule.search.avtivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xiangha.R;

import acore.override.activity.base.BaseActivity;
import acore.tools.ChannelUtil;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.search.data.SearchConstant;
import amodule.search.view.GlobalSearchView;
import amodule.search.view.SearchResultAdDataProvider;


/**
 * Created by ：airfly on 2016/10/10 15:15.
 */

public class HomeSearch extends BaseActivity {

    public static final String STATISTICS_ID = "a_search430";
    private int limitSearchType;
    private String searchKey;
    private GlobalSearchView globalSearchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        initActivity("", 2, 0, 0, R.layout.a_search_global);
        initData();
        initView();
        SearchResultAdDataProvider.getInstance().getAdData();
    }


    private void initData() {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            limitSearchType = bundle.getInt(SearchConstant.SEARCH_TYPE, SearchConstant.SEARCH_CAIPU);
            searchKey = bundle.getString("s");
        }

        Log.e("渠道号"," "+ ChannelUtil.getChannel(this));
    }

    private void initView() {

        initTitle();
        globalSearchView = (GlobalSearchView) findViewById(R.id.bar_search_global);
        globalSearchView.init(this,searchKey, limitSearchType);
    }

    private void initTitle() {
        if (Tools.isShowTitle()) {
            LinearLayout linearView = new LinearLayout(this);
//            int dp_46 = Tools.getDimen(getApplicationContext(), R.dimen.dp_46);
            int height = /*dp_46 + */Tools.getStatusBarHeight(getApplicationContext());
            linearView.setBackgroundColor(getResources().getColor(R.color.common_top_bg));
            linearView.setOrientation(LinearLayout.VERTICAL);
            RelativeLayout.LayoutParams params =new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            RelativeLayout activityLayout = (RelativeLayout) findViewById(R.id.activityLayout);
            activityLayout.addView(linearView,0,params);//
        }else{
            findViewById(R.id.all_title_rela).setBackgroundColor(getResources().getColor(R.color.common_top_bg));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SearchResultAdDataProvider.getInstance().getXhAllAdControl().releaseView();
        System.gc();
    }


    @Override
    public void onBackPressed() {

        if (!globalSearchView.hideSecondLevelView()) {
            super.onBackPressed();
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                ToolsDevice.keyboardControl(false, this, globalSearchView);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

}
