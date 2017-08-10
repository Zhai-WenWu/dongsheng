package amodule.quan.activity.upload;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import amodule.quan.activity.upload.adapter.AdapterChooseCircle;
import amodule.quan.db.CircleData;
import amodule.quan.db.CircleSqlite;

/**
 * Created by Fang Ruijiao on 2016/8/26.
 */

public class UploadChooseCircle extends BaseActivity {

    private ArrayList<Map<String, String>> topList;

    private String chooseCid = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("选择圈子",3,0, R.layout.a_common_post_new_title,R.layout.a_common_post_choose_circle);
        String cid = getIntent().getStringExtra("cid");
        if(!TextUtils.isEmpty(cid)){
            chooseCid = cid;
        }
        boolean isOkData = initData();
        if(isOkData){
            initView();
        }else{
            finish();
        }
    }

    private boolean initData(){
        topList = new ArrayList<Map<String, String>>();
        CircleSqlite circleSqlite = new CircleSqlite(this);
        ArrayList<CircleData> array = circleSqlite.getAllCircleData();
        if(array == null || array.size() == 0)
            return false;
//        Map<String,String> map;
        for(CircleData item : array){
            Map<String,String> map = new HashMap<>();
            map.put("name", item.getName());
            map.put("cid", item.getCid());
            topList.add(map);
        }
        return true;
    }

    private void initView(){
        findViewById(R.id.upload).setVisibility(View.VISIBLE);
        findViewById(R.id.upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chooseCid.equals("-1")){
                    Tools.showToast(UploadChooseCircle.this,"请选择圈子");
                    return;
                }
                Intent it = new Intent();
                it.putExtra("chooseCid",chooseCid);
                setResult(RESULT_OK,it);
                finish();
            }
        });
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ListView listView = (ListView)findViewById(R.id.a_post_choose_list);
        final AdapterChooseCircle adapter = new AdapterChooseCircle(listView,topList,
                R.layout.a_common_post_choose_circle_item,
                new String[]{"name"},
                new int[]{R.id.a_post_choose_item_tv});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chooseCid = topList.get(position).get("cid");
                for(int index = 0; index < topList.size(); index ++){
                    Map<String,String> map = topList.get(index);
                    if(index == position){
                        map.put("isChoose","true");
                    }else{
                        map.put("isChoose","false");
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

}