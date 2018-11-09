package amodule.search.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Predicate;
import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import amodule.search.adapter.SearchHorizonAdapter;
import amodule.search.avtivity.HomeSearch;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

import static acore.tools.StringManager.API_SEARCH_RECOM_LABEL;
import static amodule.search.avtivity.HomeSearch.EXTRA_JSONDATA;

/**
 * Description :
 * PackageName : amodule.search.view
 * Created by mrtrying on 2018/11/8 15:41.
 * e_mail : ztanzeyu@gmail.com
 */
public class SearchHorizonLayout extends RelativeLayout {
    public static final int TYPE_WORD = 1;
    public static final int TYPE_DISH = 2;
    private ImageView mRefreshIcon, mRefreshIconBg;
    private RecyclerView mRecyclerView;
    private SearchHorizonAdapter mAdapter;
    private List<Map<String, String>> mData = new ArrayList<>();
    private List<Map<String, String>> wordsList = new ArrayList<>();
    private List<Map<String, String>> changeDataList = new ArrayList<>();
    private int currentType = TYPE_WORD;

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

        //TODO
//        for(int i=0;i<7;i++){
//            Map<String,String> map = new HashMap<>();
//            map.put("name","糖醋排骨");
//            wordsList.add(map);
//        }
//        mData.addAll(wordsList);
//        mAdapter.notifyDataSetChanged();
//        setVisibility(VISIBLE);
    }

    private void setListener() {
        mRefreshIcon.setOnClickListener(v -> {
            switch (currentType) {
                case TYPE_WORD:
                    if (!changeDataList.isEmpty()) {
                        currentType = TYPE_DISH;
                        mData.clear();
                        mData.addAll(changeDataList);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case TYPE_DISH:
                    if (!wordsList.isEmpty()) {
                        currentType = TYPE_WORD;
                        mData.clear();
                        mData.addAll(wordsList);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        });
        mAdapter.setOnItemClickListener(new SearchHorizonAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, Map<String, String> data) {
                JSONArray jsonArray = new JSONArray();

                JSONObject jsonObject = new JSONObject();
                try {
                    if(!TextUtils.equals("2",data.get("type")) && !strList.isEmpty()){
                        jsonArray = StringManager.getJsonByArrayList((ArrayList<Map<String, String>>) strList);
                    }
                    jsonObject.put("name",data.get("name"));
                    jsonObject.put("type",data.get("type"));
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getContext(),HomeSearch.class);
                intent.putExtra(EXTRA_JSONDATA,jsonArray.toString());
                getContext().startActivity(intent);
            }
        });
    }

    List<Map<String, String>> strList = new ArrayList<>();
    public void setWordList(List<Map<String, String>> strList) {
        if(strList == null || strList.isEmpty()){
            setVisibility(GONE);
            return;
        }
        this.strList.clear();
        this.strList.addAll(strList);
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<strList.size();i++){
            Map<String,String> map = strList.get(i);
            if(!TextUtils.isEmpty(map.get("name"))){
                sb.append("soData[").append(i).append("]")
                        .append("=").append(map.get("name"));
                if(i!=strList.size() - 1){
                    sb.append("&");
                }
            }
        }
        ReqEncyptInternet.in().doGetEncypt(API_SEARCH_RECOM_LABEL + "?" + sb.toString(), new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i <= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> resultMap = StringManager.getFirstMap(o);
                    wordsList = StringManager.getListMapByJson(resultMap.get("words"));
                    if (wordsList.isEmpty()) {
                        setVisibility(GONE);
                        return;
                    }
                    mData.clear();
                    mData.addAll(wordsList);
                    mAdapter.notifyDataSetChanged();

                    changeDataList = StringManager.getListMapByJson(resultMap.get("changeData"));
                    if (changeDataList.isEmpty() || !TextUtils.equals("2", resultMap.get("hasChange"))) {
                        hideRefreshIcon();
                    } else {
                        showRefreshIcon();
                    }
                } else {
                    setVisibility(GONE);
                }
            }
        });
    }

    public void showRefreshIcon() {
        mRefreshIcon.setVisibility(VISIBLE);
        mRefreshIconBg.setVisibility(VISIBLE);
    }

    public void hideRefreshIcon() {
        mRefreshIcon.setVisibility(GONE);
        mRefreshIconBg.setVisibility(GONE);
    }

}
