package amodule.dish.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import acore.logic.load.AutoLoadMore;
import acore.logic.load.LoadManager;
import acore.logic.load.LoadMoreManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.RvGridView;
import amodule.dish.adapter.AdapterGridDish;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;

import static acore.tools.StringManager.api_getVideoList;

/**
 * Description :
 * PackageName : amodule.dish.view
 * Created on 2017/12/2 11:47.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class DishGridDialog extends Dialog {

    LoadMoreManager mLoadMore;
    RvGridView mGridView;
    private AdapterGridDish mAdapter;
    private List<Map<String, String>> mData = new ArrayList<>();
    private int mCurrentPage = 0;
    private int mEveryPageCount = 10;
    private LinkedHashMap<String, String> params = new LinkedHashMap<>();

    private String mCurrentCode;
    private OnItemClickCallback mOnItemClickCallback;

    public DishGridDialog(@NonNull Context context, @NonNull String code) {
        this(context, code, "", "");
    }

    public DishGridDialog(@NonNull Context context, @NonNull String code, String chapterCode, String courseCode) {
        super(context, R.style.dishGridStyle);
        setCancelable(true);
        mLoadMore = new LoadMoreManager(context);
        //初始化参数
        updateParam(code, chapterCode, courseCode);
        //初始化UI
        initUI();
    }

    View contentView;
    private void initUI() {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.a_dish_grid_dialog, null);
        RelativeLayout rootLayout = new RelativeLayout(getContext());
        rootLayout.setBackgroundColor(Color.parseColor("#88000000"));
        rootLayout.setOnClickListener(v -> {});
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) (ToolsDevice.getWindowPx(getContext()).heightPixels * 2 / 3f));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rootLayout.addView(contentView, layoutParams);
        setContentView(rootLayout, new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));

        contentView.findViewById(R.id.back_icon).setOnClickListener(v -> dismiss());
        mGridView = (RvGridView) contentView.findViewById(R.id.rvGridView);
        final int padding_5 = Tools.getDimen(getContext(), R.dimen.dp_5);
        final int padding_4 = Tools.getDimen(getContext(), R.dimen.dp_4);
        mGridView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view) - mGridView.getHeaderViewsSize();
                outRect.top = (position == 0 || position == 1) ? padding_4 : padding_5;
                outRect.left = padding_4;
                outRect.right = padding_4;
                outRect.bottom = padding_5;
            }
        });

        mGridView.setOnItemClickListener((view, holder, position) -> {
            if (mOnItemClickCallback != null) {
                mOnItemClickCallback.onItemClick(view, position, mData.get(position));
            }
        });
        mAdapter = new AdapterGridDish(getContext(), mData);
        mGridView.setAdapter(mAdapter);
        View.OnClickListener clicker = v -> loadData();
        Button loadMore = mLoadMore.newLoadMoreBtn(mGridView, clicker);
        loadMore.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Tools.getDimen(getContext(), R.dimen.dp_45)));
        AutoLoadMore.setAutoMoreListen(mGridView, loadMore, clicker);
    }

    public void updateParam(String code) {
        updateParam(code, "", "");
    }

    private final String PARAM_KEY_CODE = "code";
    private final String PARAM_KEY_CHAPTERCODE = "chapterCode";
    private final String PARAM_KEY_COURSECODE = "courseCode";

    public DishGridDialog updateParam(@NonNull String code, String chapterCode, String courseCode) {
        this.mCurrentCode = code;
        handlerParams(PARAM_KEY_CODE,code);
        handlerParams(PARAM_KEY_CHAPTERCODE,chapterCode);
        handlerParams(PARAM_KEY_COURSECODE,courseCode);
        return this;
    }

    /**
     * 处理参数
     * @param key
     * @param value
     * @return
     */
    private String handlerParams(String key, String value) {
        return TextUtils.isEmpty(value) ? params.remove(key) : params.put(key, value);
    }

    @Override
    public void show() {
        if (TextUtils.isEmpty(mCurrentCode)) {
            Toast.makeText(getContext(), "参数错误", Toast.LENGTH_SHORT).show();
            return;
        }
        super.show();
        refresh();
//        if (contentView != null) {
//            contentView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.in_from_bottom_alpha));
//        }
    }

    boolean isDissmiss = false;

    @Override
    public void dismiss() {
//        if (contentView != null && !isDissmiss) {
//            isDissmiss = true;
//            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.out_from_bottom_alpha);
//            animation.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    dismiss();
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//
//                }
//            });
//            contentView.startAnimation(animation);
//        } else {
            ReqEncyptInternet.in().cancelRequset(new StringBuffer(api_getVideoList).append(params).toString());
            super.dismiss();
//        }
    }

    private void refresh(){
        mCurrentPage = 0;
        loadData();
    }

    //请求数据
    private void loadData() {
        mCurrentPage++;
        changeMoreBtn(mGridView, ReqEncyptInternet.REQ_OK_STRING, -1, -1, mCurrentPage, mData.size() == 0);
        params.put("page", String.valueOf(mCurrentPage));
        ReqEncyptInternet.in().doEncypt(api_getVideoList, params, new InternetCallback(getContext()) {
            @Override
            public void loaded(int flag, String s, Object o) {
                int loadCount = 0;
                if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                    Map<String, String> temp = StringManager.getFirstMap(o);
                    ArrayList<Map<String, String>> data = StringManager.getListMapByJson(temp.get("data"));
                    loadCount = data.size();
                    Stream.of(data).forEach(map -> {
                        Map<String, String> dish = StringManager.getFirstMap(map.get("dish"));
                        transferData(map, dish, "time");
                        transferData(map, dish, "name");
                        transferData(map, dish, "image", map.get("image"));
                        map.put("isCurrent", TextUtils.equals(mCurrentCode, dish.get("code")) ? "2" : "1");
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

                changeMoreBtn(mGridView, flag, mEveryPageCount, loadCount, mCurrentPage, false);
            }
        });
    }

    private void transferData(Map<String, String> target, Map<String, String> source, String key) {
        transferData(target, source, key, "");
    }

    private void transferData(Map<String, String> target, Map<String, String> source, String key, String defaultValue) {
        if (null == target
                || null == source
                || source.isEmpty()
                || TextUtils.isEmpty(key)) {
            return;
        }
        String timeValue = source.get(key);
        target.put(key, TextUtils.isEmpty(timeValue) ? defaultValue : timeValue);
    }

    private int changeMoreBtn(Object key, int flag, int everyPageNum, int actPageNum, int nowPage, boolean isBlankSpace) {
        Button loadMoreBtn = mLoadMore.getLoadMoreBtn(key);
        if (loadMoreBtn == null) {
            return 0;
        }
        loadMoreBtn.setVisibility(View.VISIBLE);
        if (flag >= ReqInternet.REQ_OK_STRING) {
            if (isBlankSpace) {
//                showProgressBar();
//                hideLoadFaildBar();
            }
            // 激活加载更多时
            if (actPageNum == -1 && everyPageNum == -1) {
                if (nowPage <= 1)
                    loadMoreBtn.setVisibility(View.GONE);
                else
                    loadMoreBtn.setText("加载中...");
                loadMoreBtn.setEnabled(false);
                return nowPage;
            }
            // 加载完毕
            else if ((actPageNum > 0) ||
                    (everyPageNum == LoadManager.FOOTTIME_PAGE && actPageNum > 0)) {
                loadMoreBtn.setText("点击加载更多");
                loadMoreBtn.setEnabled(true);
            } else {
                loadMoreBtn.setText("没有更多了");
                loadMoreBtn.setEnabled(false);
            }
            if (actPageNum <= 0 && nowPage == 1)
                loadMoreBtn.setVisibility(View.GONE);
        }
        return loadOver(key, flag, nowPage, isBlankSpace);
    }

    /**
     * 加载完毕
     *
     * @param flag
     * @param nowPage
     * @param isBlankSpace ：当前页面是否是百页，也就是页面有没有数据
     *
     * @return
     */
    private int loadOver(Object key, int flag, int nowPage, boolean isBlankSpace) {
        Button loadMoreBtn = mLoadMore.getLoadMoreBtn(key);
        if (flag >= ReqInternet.REQ_OK_STRING) {
//            hideProgressBar();
//            hideLoadFaildBar();
        }
        // 加载失败
        else if (nowPage == 1) {
            XHConf.net_timeout += 1000;
            if (loadMoreBtn != null)
                loadMoreBtn.setEnabled(true);
//            if (isShowingProgressBar()) {
//                hideProgressBar();
//                if (isBlankSpace) {
//                    showLoadFaildBar();
//                }
//            }
            nowPage--;
        } else if (loadMoreBtn != null) {
//            hideProgressBar();
            loadMoreBtn.setText("加载失败，点击重试");
            loadMoreBtn.setEnabled(true);
            nowPage--;
        }
        return nowPage;
    }

    public interface OnItemClickCallback {
        void onItemClick(View view, int position, Map<String, String> stringStringMap);
    }

    public DishGridDialog setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        mOnItemClickCallback = onItemClickCallback;
        return this;
    }
}
