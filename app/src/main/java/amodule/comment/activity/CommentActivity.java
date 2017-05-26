package amodule.comment.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.DownRefreshList;
import amodule.comment.view.ViewCommentItem;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import xh.basic.internet.UtilInternet;

/**
 * Created by Fang Ruijiao on 2017/5/25.
 */
public class CommentActivity extends BaseActivity implements View.OnClickListener{

    private DownRefreshList downRefreshList;
    private AdapterSimple adapterSimple;
    private ArrayList<Map<String, String>> listArray;
    private String type,code;
    private int currentPage = 0, everyPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sufureView页面闪烁
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        initActivity("评论", 5, 0, 0, R.layout.a_comment_activity);

        initView();
        initData();
    }

    private void initView(){
        findViewById(R.id.commend_hind).setOnClickListener(this);
        downRefreshList = (DownRefreshList) findViewById(R.id.commend_listview);
        adapterSimple = new AdapterSimple(downRefreshList,listArray,R.layout.a_comment_item,new String[]{},new int[]{}){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ViewCommentItem viewCommentItem = (ViewCommentItem) view.findViewById(R.id.comment_item);
                viewCommentItem.setData(listArray.get(position));
                return view;
            }
        };
        downRefreshList.setAdapter(adapterSimple);
    }

    private void initData(){
        type = getIntent().getStringExtra("type");
        code = getIntent().getStringExtra("code");
        if(TextUtils.isEmpty(type) || TextUtils.isEmpty(code)){
            Tools.showToast(this,"缺少 类型 或 主题");
            finish();
        }
        listArray = new ArrayList<>();
        loadManager.showProgressBar();
        loadManager.setLoading(downRefreshList, adapterSimple, true, new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                getCommentData(false);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCommentData(true);
            }
        });

    }

    private void getCommentData(final boolean isForward){
        if (isForward) {
            currentPage = 1;
        } else
            currentPage++;
        loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage, listArray.size() == 0);
        String params = "?type=" + type + "&code=" + code + "&page=" + currentPage;
        ReqEncyptInternet.in().doEncypt(StringManager.api_forumList, params, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String s, Object o) {
                int loadCount = 0;
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    if (isForward) listArray.clear();
                    ArrayList<Map<String,String>> arrayList = StringManager.getListMapByJson(o);


                    loadCount = arrayList.size();;
                    if (everyPage == 0)
                        everyPage = loadCount;
                    currentPage = loadManager.changeMoreBtn(downRefreshList, flag, everyPage, loadCount, currentPage, listArray.size() == 0);
                    downRefreshList.setVisibility(View.VISIBLE);
                    adapterSimple.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.commend_hind:
                break;
        }
    }
}
