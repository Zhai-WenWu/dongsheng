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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

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
import third.mall.aplug.MallStringManager;

/**
 * Description : //TODO
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

    public DishGridDialog(@NonNull Context context, @NonNull String code) {
        super(context, R.style.dishGridStyle);
        Window window = getWindow();
        window.setWindowAnimations(R.style.dishGridAnim);
        setCancelable(true);
        mLoadMore = new LoadMoreManager(context);
        //初始化参数
        if(!TextUtils.isEmpty(code)){
            params.put("code",code);
        }
        //初始化UI
        initUI();
    }

    private void initUI() {
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.a_dish_grid_content,null);
        RelativeLayout rootLayout = new RelativeLayout(getContext());
        rootLayout.setBackgroundColor(Color.parseColor("#33000000"));
        rootLayout.setOnClickListener(v -> dismiss());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) (ToolsDevice.getWindowPx(getContext()).heightPixels *2/3f));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rootLayout.addView(contentView,layoutParams);
        setContentView(rootLayout,new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT));

        mGridView = (RvGridView) contentView.findViewById(R.id.rvGridView);
        final int padding = Tools.getDimen(getContext(), R.dimen.dp_5);
        mGridView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view) - mGridView.getHeaderViewsSize();
                outRect.top = (position == 0 || position == 1) ? padding * 4 : padding;
                outRect.left = padding;
                outRect.right = padding;
                outRect.bottom = padding;
            }
        });
        mAdapter = new AdapterGridDish(getContext(), mData);
        mGridView.setAdapter(mAdapter);
        View.OnClickListener clicker =  v -> loadData();
        Button loadMore = mLoadMore.newLoadMoreBtn(mGridView, clicker);
        loadMore.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,Tools.getDimen(getContext(),R.dimen.dp_45)));
        AutoLoadMore.setAutoMoreListen(mGridView, loadMore, clicker);
    }

    @Override
    public void show() {
        super.show();
        mLoadMore.getLoadMoreBtn(mGridView).performClick();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        ReqEncyptInternet.in().cancelRequset(new StringBuffer(StringManager.api_getVideoList).append(params).toString());
    }

    //请求数据
    private void loadData() {
        mCurrentPage++;
        changeMoreBtn(mGridView,ReqEncyptInternet.REQ_OK_STRING, -1, -1, mCurrentPage, mData.size() == 0);
        params.put("page", String.valueOf(mCurrentPage));
        ReqEncyptInternet.in().doEncypt(StringManager.api_getVideoList, params, new InternetCallback(getContext()) {
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

                changeMoreBtn(mGridView,flag, mEveryPageCount, loadCount, mCurrentPage, false);
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

    public int changeMoreBtn(Object key, int flag, int everyPageNum, int actPageNum, int nowPage, boolean isBlankSpace) {
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
                loadMoreBtn.setText("— 吃,也是一种艺术 —");
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
    public int loadOver(int flag, int nowPage, boolean isBlankSpace) {
        return loadOver(null, flag, nowPage, isBlankSpace);
    }

    public int loadOver(Object key, int flag, int nowPage, boolean isBlankSpace) {
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

}
