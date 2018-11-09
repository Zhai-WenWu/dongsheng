package amodule.search.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import amodule.search.adapter.SearchHorizonAdapter;

/**
 * Description :
 * PackageName : amodule.search.view
 * Created by mrtrying on 2018/11/8 15:41.
 * e_mail : ztanzeyu@gmail.com
 */
public class SearchHorizonLayout extends RelativeLayout {
    private ImageView mRefreshIcon, mRefreshIconBg;
    private RecyclerView mRecyclerView;
    private SearchHorizonAdapter mAdapter;
    private List<Map<String, String>> mData = new ArrayList<>();
    private String word;

    private SearchHorizonAdapter.OnItemClickListener mOnItemClickListener;

    public SearchHorizonLayout(Context context) {
        super(context);
        initialize(context);
    }

    public SearchHorizonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public SearchHorizonLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        LayoutInflater.from(context).inflate(R.layout.c_view_search_horizon, this, true);
        mRefreshIcon = findViewById(R.id.icon_refresh);
        mRefreshIconBg = findViewById(R.id.icon_refresh_bg);
        mRecyclerView = findViewById(R.id.search_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new SearchHorizonAdapter(context, mData);
        mRecyclerView.setAdapter(mAdapter);

        setListener();
        //TODO test
        setSearchWord("");
    }

    private void setListener() {
        mRefreshIcon.setOnClickListener(v -> {
            setSearchWord(word);
            //TODO
            Toast.makeText(getContext(), "mRefreshIcon", Toast.LENGTH_SHORT).show();
        });
        mAdapter.setOnItemClickListener(this::handleOnItemClick);
    }

    public void setSearchWord(String word) {
        this.word = word;
        //TODO
        String[] arr = {"排骨", "五花肉", "里脊", "西红柿", "螃蟹"};
        for (int i = 0; i < arr.length; i++) {
            Map<String, String> map = new HashMap<>();
            map.put("text", arr[i]);
            mData.add(map);
        }
        mAdapter.notifyDataSetChanged();
        if (mData.isEmpty()) {
            hideRefreshIcon();
        } else {
            showRefreshIcon();
        }
    }

    public void showRefreshIcon() {
        mRefreshIcon.setVisibility(VISIBLE);
        mRefreshIconBg.setVisibility(VISIBLE);
    }

    public void hideRefreshIcon() {
        mRefreshIcon.setVisibility(GONE);
        mRefreshIconBg.setVisibility(GONE);
    }

    private void handleOnItemClick(View v, Map<String, String> data) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onClick(v, data);
        }
    }

    public void setOnItemClickListener(SearchHorizonAdapter.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }
}
