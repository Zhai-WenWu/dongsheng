package amodule.search.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Predicate;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.stat.StatModel;
import acore.logic.stat.StatisticsManager;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.TagTextView;
import amodule.main.view.item.BaseItemView;
import amodule.search.adapter.SearchVipLessonAdapter;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.web.FullScreenWeb;
import xh.basic.internet.UtilInternet;

import static acore.logic.stat.StatConf.STAT_TAG;
import static amodule.search.adapter.SearchVipLessonAdapter.VIEW_TYPE_KEY;
import static amodule.search.adapter.SearchVipLessonAdapter.VIEW_TYPE_MULTIPLE;
import static amodule.search.adapter.SearchVipLessonAdapter.VIEW_TYPE_SINGLE;

public class SearchVIPLessonView extends RelativeLayout {


    private TextView mLessonTitle;
    private RecyclerView mRecyclerView;
    private SearchVipLessonAdapter mAdapter;
    private List<Map<String, String>> mData = new ArrayList<>();
    private String searchKey;
    private String mJumpBuyVipUrl;

    private final String mStatisticsId = "a_searesult_vip";
    private final String moduleName = "搜索名厨课";

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
        mLessonTitle = findViewById(R.id.lesson_title);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setTag(STAT_TAG, moduleName);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new SearchVipLessonAdapter(context, mData);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickLesson((position,data) -> {
            if (data == null) {
                return;
            }
            if (!TextUtils.isEmpty(mJumpBuyVipUrl) && !LoginManager.isVIP()) {
                Intent intent = AppCommon.parseURL(XHActivityManager.getInstance().getCurrentActivity(), mJumpBuyVipUrl);
                intent.putExtra(FullScreenWeb.BACK_PAGE, data.get("url"));
                XHActivityManager.getInstance().getCurrentActivity().startActivity(intent);
            } else {
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), data.get("url"), true);
            }
            String twoLevel = mAdapter.getItemCount() == 1 ? "顶部VIP内容点击量" : "顶部VIP内容点击量" + (position+1);
            XHClick.mapStat(getContext(), mStatisticsId, twoLevel, "");
            StatisticsManager.saveData(StatModel.createListClickModel(getContext().getClass().getSimpleName(),moduleName,
                    String.valueOf(position + 1),searchKey,data.get("statJson")));
        });
        int dp_20 = Tools.getDimen(context, R.dimen.dp_20);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                View shadowView = view.findViewById(R.id.shadow_layout);
                if (mAdapter.getItemCount() == 0)
                    return;
                int position = parent.getChildAdapterPosition(view);
                if (mAdapter.getItemCount() == 1) {
                    outRect.left = dp_20 - shadowView.getPaddingLeft();
                    outRect.right = dp_20 - shadowView.getPaddingRight();
                } else if (position == 0) {
                    outRect.left = dp_20 - shadowView.getPaddingLeft();
                    outRect.right = dp_20 / 4 - shadowView.getPaddingRight();
                } else if (position == mAdapter.getItemCount() - 1) {
                    outRect.left = dp_20 / 4 - shadowView.getPaddingLeft();
                    outRect.right = dp_20 - shadowView.getPaddingRight();
                } else {
                    outRect.left = dp_20 / 4 - shadowView.getPaddingLeft();
                    outRect.right = dp_20 / 4 - shadowView.getPaddingRight();
                }
                outRect.top = dp_20 / 4 - shadowView.getPaddingTop();
                outRect.bottom = dp_20 / 4 - shadowView.getPaddingBottom();
            }
        });
        setVisibility(View.GONE);
    }

    public void searchLesson(String searchKey, final LessonCallback callback) {
        if (TextUtils.isEmpty(searchKey)){
            return;
        }
        this.searchKey = searchKey;
        setVisibility(View.GONE);
        String params = "keywords=" + searchKey;
        ReqEncyptInternet.in().doEncypt(StringManager.API_SEARCH_COURSE_DISH, params, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                onDataReady(i >= ReqEncyptInternet.REQ_OK_STRING ? o : null, callback);
            }
        });
    }

    private void onDataReady(Object o, LessonCallback callback) {
        //清空数据
        mData.clear();
        List<String> codeArray = new ArrayList<>();
        if (o != null) {
            //处理数据
            Map<String, String> resultMap = StringManager.getFirstMap(o);
            mJumpBuyVipUrl = resultMap.get("jumpBuyVip");
            Map<String, String> titleMap = StringManager.getFirstMap(resultMap.get("text"));
            String titleText = titleMap.get("title");
            if (TextUtils.isEmpty(titleText)) {
                titleText = "香哈名厨菜 - 会员专享";
            }
            mLessonTitle.setText(titleText);
            List<Map<String, String>> tmepData = StringManager.getListMapByJson(resultMap.get("list"));

            mData.addAll(tmepData);
            Stream.of(mData).forEach(value -> {
                value.putAll(titleMap);
                value.put(VIEW_TYPE_KEY, String.valueOf(mData.size() == 1 ? VIEW_TYPE_SINGLE : VIEW_TYPE_MULTIPLE));
                Map<String, String> customer = StringManager.getFirstMap(value.get("customer"));
                value.put("nickName", customer.get("nickName"));
            });
            Stream.of(mData)
                    .filter(value -> mData.size() <= 2 || mData.indexOf(value) < 2)
                    .forEach(value -> codeArray.add(value.get("code")));
        }
        //刷新
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
        setVisibility(mData.isEmpty() ? GONE : VISIBLE);
        if (mData.size() <= 1) {
            hideTitle();
        } else {
            showTitle();
        }
        //
        if (callback != null) {
            callback.callback(codeArray);
        }
    }

    private void showTitle() {
        findViewById(R.id.lesson_title).setVisibility(VISIBLE);
        findViewById(R.id.lesson_vip_icon).setVisibility(VISIBLE);
    }

    private void hideTitle() {
        findViewById(R.id.lesson_title).setVisibility(GONE);
        findViewById(R.id.lesson_vip_icon).setVisibility(GONE);
    }

    public interface LessonCallback {
        void callback(List<String> codeArray);
    }
}
