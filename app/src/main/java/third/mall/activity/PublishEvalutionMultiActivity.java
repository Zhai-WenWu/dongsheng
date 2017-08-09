package third.mall.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;
import third.mall.adapter.AdapterEvalution;

public class PublishEvalutionMultiActivity extends BaseActivity {
    public static final String EXTRAS_CODE = "code";

    private TextView rightText;
    private PtrClassicFrameLayout refershLayout;
    private ListView commodList;

    private AdapterEvalution adapter;

    private List<Map<String,String>> data = new ArrayList<>();

    private String orderCode = "";
    private int page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("发布评价",6,0,R.layout.c_view_bar_title,R.layout.activity_publish_evalution_multi);

        initData();

        initView();

        setLoading();
    }

    private void initData() {
        Intent intent = getIntent();
        if(intent != null){
            orderCode = intent.getStringExtra(EXTRAS_CODE);
        }
    }

    private void initView() {
        rightText = (TextView) findViewById(R.id.rightText);
        refershLayout = (PtrClassicFrameLayout) findViewById(R.id.refresh_list_view_frame);
        commodList = (ListView) findViewById(R.id.commod_list);

        rightText.setText("发布");
        rightText.setVisibility(View.VISIBLE);
    }

    private void setLoading() {
        adapter = new AdapterEvalution(this,data);
        loadManager.setLoading(refershLayout, commodList, adapter, true,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO 刷新
                        refersh();
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO 加载
                        loadData();
                    }
                });
    }

    private void refersh(){
        data.clear();
        loadData();
    }

    private void loadData(){
        //TODO 测试数据
        for(int index = 0; index < 10;index ++){
            Map<String,String> map = new HashMap<>();
            map.put("title","标题随便写-----   " + index);
            map.put("stars","5");
            map.put("status",(index % 2 + 1) + "");
            map.put("img","http://s1.cdn.xiangha.com/caipu/201706/1411/141104182336.jpg/NjAwX2MxXzQwMA.webp");
            data.add(map);
        }
        adapter.notifyDataSetChanged();
        loadManager.hideProgressBar();
    }

    private void publishMutilEvalution(){
        //TODO 批量发布
    }


}
