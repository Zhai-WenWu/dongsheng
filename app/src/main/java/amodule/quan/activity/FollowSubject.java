package amodule.quan.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.DownRefreshList;
import amodule.quan.adapter.AdapterFollowSubject;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

/**
 * 跟帖列表
 * Created by Fang Ruijiao on 2017/7/19.
 */
public class FollowSubject extends BaseActivity {

    private DownRefreshList circle_list;

    private int mCurrentPage = 0;
    private int mEveryPageNum = 0;
    private AdapterFollowSubject mAdapter;
    private ArrayList<Map<String, String>> mListData = new ArrayList<>();
    private boolean mLoadOver = false;

    private String dishCode;
    private String title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.a_follow_subject);
        dishCode = getIntent().getStringExtra("dishCode");
        title = getIntent().getStringExtra("title");
        if(TextUtils.isEmpty(dishCode)){
            Tools.showToast(this,"菜谱code为空");
            this.finish();
        }
        //处理title字数
        if(!TextUtils.isEmpty(title)&&title.length()>12){
            title=title.substring(0,12);
        }
        init();
    }

    private void init(){
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView titleTv = (TextView) findViewById(R.id.title);
        titleTv.setText(TextUtils.isEmpty(title)?"跟帖列表":title);
        findViewById(R.id.circle_share).setVisibility(View.GONE);
        circle_list = (DownRefreshList) findViewById(R.id.circle_list);
        mAdapter = new AdapterFollowSubject(this, circle_list, mListData);
        mAdapter.setStiaticData("a_learndish_tie");
        if (!mLoadOver) {
            loadManager.setLoading(circle_list, mAdapter, true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setRquest(false);
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setRquest(true);
                }
            });
            mLoadOver = true;
        }
    }

    private void setRquest(final boolean isRefresh) {
        if (isRefresh) {
            mCurrentPage = 0;
            mEveryPageNum = 0;
        }
        mCurrentPage++;
        String url = StringManager.api_getFollowSubjectList + "?dishCode=" + dishCode + "&page=" + mCurrentPage;
        // 更新加载按钮状态
        loadManager.changeMoreBtn(circle_list, ReqInternet.REQ_OK_STRING, -1, -1, mCurrentPage, isRefresh);
        if (isRefresh) {
            loadManager.hideProgressBar();
        }
        ReqInternet.in().doGet(url, new InternetCallback(this) {

            @Override
            public void loaded(int flag, String url, Object msg) {

                int loadCount = 0;
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    if (isRefresh) {
                        mListData.clear();
                    }
                    List<Map<String, String>> returnData = StringManager.getListMapByJson(msg);
                    if(returnData.size() > 0) {
                        returnData = StringManager.getListMapByJson(returnData.get(0).get("data"));
                        if (returnData.size() > 0) {
                            for (int index = 0, length = returnData.size(); index < length; index++) {
                                Map<String, String> map = returnData.get(index);
                                // 请求的第一页数据中，包含公告、置顶、活动的item数据是不记录在每页的数据count中的
                                String style = map.get("style");
                                if (style != null) {
                                    map.put("dataType", "1");
                                    map.put("isSafa", "yes");
                                    // 添加是否定位字段
                                    mListData.add(map);
                                    if (!style.equals("5") && !style.equals("6")) {
                                        loadCount++;
                                    }
                                }
                            }
                        }
                    }

                }
                if (mEveryPageNum == 0) {
                    mEveryPageNum = loadCount;
                }
                mAdapter.notifyDataSetChanged();
                mCurrentPage = loadManager.changeMoreBtn(flag, mEveryPageNum, loadCount, mCurrentPage, isRefresh);
                circle_list.onRefreshComplete();
                if (isRefresh)
                    circle_list.setSelection(1);
                //如果没有数据显示提示
                if(mListData.size() ==0){
                    findViewById(R.id.a_follow_subject_nodata).setVisibility(View.VISIBLE);
                    circle_list.setVisibility(View.GONE);
                }else{
                    findViewById(R.id.a_follow_subject_nodata).setVisibility(View.GONE);
                    circle_list.setVisibility(View.VISIBLE);
                }
            }
        });
    }

}
