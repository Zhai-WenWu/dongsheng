package amodule.article.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;

/**
 * Created by Fang Ruijiao on 2017/5/22.
 */
public class ArticleSelectActiivty extends BaseActivity implements View.OnClickListener{

    private GridView gridView;
    private ArrayList<Map<String, String>> data;
    private AdapterSimple adapterSimple;

    private ImageView reprintImg,originalImg;
    private String checkCode;
    private int isCheck = 0; //1:转载内容   2：原创内容

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("发文章", 2, 0, R.layout.a_common_post_new_title, R.layout.a_article_select_activity);
        initView();
        getClassifyData();
    }

    private void initView(){
        findViewById(R.id.all_content).setBackgroundColor(Color.parseColor("#ffffff"));
        View upload = findViewById(R.id.upload);
        upload.setVisibility(View.VISIBLE);
        upload.setOnClickListener(this);
        TextView link = (TextView) findViewById(R.id.article_select_check_original_link);
        link.setText(Html.fromHtml("<u>《香哈原创声明》</u>"));
        link.setOnClickListener(this);
        reprintImg = (ImageView) findViewById(R.id.article_select_check_reprint);
        reprintImg.setOnClickListener(this);
        originalImg = (ImageView) findViewById(R.id.article_select_check_original);
        originalImg.setOnClickListener(this);

        data = new ArrayList<>();
        gridView = (GridView) findViewById(R.id.article_select_gridview);
        adapterSimple = new AdapterSimple(gridView,data,R.layout.a_article_select_activity_item,
                new String[]{"name"},
                new int[]{R.id.article_select_classify_text});
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i = 0; i < data.size(); i ++){
                    TextView textView = (TextView) gridView.getChildAt(i).findViewById(R.id.article_select_classify_text);
                    if(i == position){
                        textView.setBackgroundResource(R.drawable.article_select_classify_yes);
                        textView.setTextColor(Color.parseColor("#ffffff"));
                        checkCode = data.get(i).get("code");
                    }else{
                        textView.setBackgroundResource(R.drawable.article_select_classify_no);
                        textView.setTextColor(Color.parseColor("#333333"));
                    }
                }
            }
        });
    }


    private void getClassifyData(){
//        ReqEncyptInternet.in().doEncypt(StringManager.api_getArticleClass, "", new InternetCallback(this) {
//            @Override
//            public void loaded(int i, String s, Object o) {
//                if(i == ReqInternet.REQ_OK_STRING){
//                    ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(o);
//                    if(arrayList.size() > 0){
//                        data.addAll(arrayList);
//                        adapterSimple.notifyDataSetChanged();
//                    }
//                }
//            }
//        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ArrayList<Map<String,String>> arrayList = new ArrayList<>();
                Map<String, String> map;
                for(int i = 0; i < 5; i ++){
                    map = new HashMap<>();
                    map.put("code","" + i);
                    map.put("name","技巧：" + i);
                    arrayList.add(map);
                }
                data.addAll(arrayList);
                gridView.setAdapter(adapterSimple);
            }
        },500);
    }

    private void upload(){

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.upload:
                if(TextUtils.isEmpty(checkCode)){
                    Tools.showToast(ArticleSelectActiivty.this,"请选择分类");
                    return;
                }
                if(isCheck > 0){
                    upload();
                }else{
                    Tools.showToast(ArticleSelectActiivty.this,"请选择原创/转载");
                    return;
                }
                break;
            case R.id.article_select_check_original_link:
                Tools.showToast(ArticleSelectActiivty.this,"香哈原创声明");
                break;
            case R.id.article_select_check_reprint:
                isCheck = 1;
                reprintImg.setImageResource(R.drawable.i_article_select_yes);
                originalImg.setImageResource(R.drawable.i_article_select_no);
                break;
            case R.id.article_select_check_original:
                isCheck = 2;
                originalImg.setImageResource(R.drawable.i_article_select_yes);
                reprintImg.setImageResource(R.drawable.i_article_select_no);
                break;

        }
    }
}
