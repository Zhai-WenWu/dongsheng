package amodule.search.view;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.TagTextView;
import amodule.main.view.item.BaseItemView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.web.FullScreenWeb;
import xh.basic.internet.UtilInternet;

public class SearchVIPLessonView extends RelativeLayout {

    private List<Map<String, String>> mData = new ArrayList<>();

    private RecyclerView mRecyclerView;

    private final String mStatisticsId = "a_searesult_vip";

    public SearchVIPLessonView(Context context) {
        super(context);
        initView(context);
    }

    public SearchVIPLessonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchVIPLessonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_search_viplesson, this);
        mRecyclerView = findViewById(R.id.recycler_view);


        setVisibility(View.GONE);
    }

    public void searchLesson(String searchKey, final LessonCallback callback) {
        if (TextUtils.isEmpty(searchKey)){
            return;
        }
        setVisibility(View.GONE);
        String params = "keywords=" + searchKey;
        ReqEncyptInternet.in().doEncypt(StringManager.API_SEARCH_COURSE_DISH, params, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqEncyptInternet.REQ_OK_STRING) {
                    onDataReady(o, callback);
                } else {
                    onDataReady(null, callback);
                }
            }
        });
    }

    private void onDataReady(Object o, LessonCallback callback) {
        mData = StringManager.getListMapByJson(o);

        setVisibility(mData.isEmpty() ? GONE : VISIBLE);
    }

    public interface LessonCallback {
        void callback();
    }
}
