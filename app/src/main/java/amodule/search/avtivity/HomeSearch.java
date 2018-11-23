package amodule.search.avtivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.xiangha.R;

import acore.override.activity.base.BaseActivity;
import acore.tools.ChannelUtil;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import amodule.search.data.SearchConstant;
import amodule.search.view.GlobalSearchView;

public class HomeSearch extends BaseActivity {

    public static final String STATISTICS_ID = "a_search430";
    public static final String EXTRA_JSONDATA = "jsonData";
    public static final String EXTRA_IS_NOW_SEARCH = "isNowSearch";
    public static int startCount = 0;
    public static boolean isShowDefault;
    private int limitSearchType;
    private String searchKey;
    private String jsonData;
    private boolean isNowSearch = false;
    private GlobalSearchView globalSearchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        startCount++;
        Log.i("tzy", "HomeSearch::onCreate: " + startCount);
        long startTime = System.currentTimeMillis();
        initActivity("", 2, 0, 0, R.layout.a_search_global);
        initData();
        initView();
//        SearchResultAdDataProvider.getInstance().getAdData();
        Log.i("tzy", "onCreate :: time = " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    private void initData() {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            limitSearchType = bundle.getInt(SearchConstant.SEARCH_TYPE, SearchConstant.SEARCH_CAIPU);
            searchKey = bundle.getString("s");
            jsonData = bundle.getString(EXTRA_JSONDATA);
            isNowSearch = !TextUtils.equals("1",bundle.getString(EXTRA_IS_NOW_SEARCH));
        }
        if(startCount != 1){
            level = 3;
        }

        Log.i("渠道号", " " + ChannelUtil.getChannel(this));
    }

    private void initView() {

        initTitle();
        globalSearchView = findViewById(R.id.bar_search_global);
        if (TextUtils.isEmpty(jsonData)) {
            globalSearchView.init(this, searchKey, limitSearchType,isNowSearch);
        } else {
            globalSearchView.init(this, jsonData);
        }
    }

    private void initTitle() {
        findViewById(R.id.all_title_rela).setBackgroundColor(getResources().getColor(R.color.common_top_bg));
        String colors = Tools.getColorStr(this, R.color.common_top_bg);
        Tools.setStatusBarColor(this, Color.parseColor(colors));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(level == 2 && isShowDefault){
            isShowDefault = false;
            globalSearchView.showDefaultSearchView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startCount--;
        Log.i("tzy", "HomeSearch::onDestroy: " + startCount);
        System.gc();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        if(startCount > 7){
            Main.colse_level = 3;
            isShowDefault = true;
        }
        Log.i("tzy", "HomeSearch::finish: " + startCount);
        Log.i("tzy", "HomeSearch::finish:colse_level=" + Main.colse_level);
        super.finish();
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
