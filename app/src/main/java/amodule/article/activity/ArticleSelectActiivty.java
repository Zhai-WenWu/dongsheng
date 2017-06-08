package amodule.article.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.logic.AppCommon;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.article.activity.edit.ArticleEidtActiivty;
import amodule.article.activity.edit.EditParentActivity;
import amodule.article.activity.edit.VideoEditActivity;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadArticleSQLite;
import amodule.article.db.UploadParentSQLite;
import amodule.article.db.UploadVideoSQLite;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import xh.windowview.XhDialog;

/**
 * Created by Fang Ruijiao on 2017/5/22.
 */
public class ArticleSelectActiivty extends BaseActivity implements View.OnClickListener{

    private GridView gridView;
    private ArrayList<Map<String, String>> data;
    private AdapterSimple adapterSimple;

    private ImageView reprintImg,originalImg;
    private EditText reprintLink;
    private String checkCode;
    private int isCheck = 0; //1:转载内容   2：原创内容

    private UploadParentSQLite sqLite;
    private UploadArticleData uploadArticleData;

    private int dataType;
    private int draftId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataType = getIntent().getIntExtra("dataType",0);
        if(dataType == EditParentActivity.TYPE_ARTICLE) {
            initActivity("发文章", 5, 0, R.layout.a_article_select_title, R.layout.a_article_select_activity);
            sqLite = new UploadArticleSQLite(XHApplication.in().getApplicationContext());
        }else if(dataType == EditParentActivity.TYPE_VIDEO) {
            initActivity("发视频", 5, 0, R.layout.a_article_select_title, R.layout.a_article_select_activity);
            sqLite = new UploadVideoSQLite(XHApplication.in().getApplicationContext());
        }else{
            Tools.showToast(this,"发布数据类型为空");
            finish();
        }
        initData();
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
        originalImg = (ImageView) findViewById(R.id.article_select_check_original);
        originalImg.setOnClickListener(this);
        reprintImg = (ImageView) findViewById(R.id.article_select_check_reprint);
        reprintImg.setOnClickListener(this);
        reprintLink = (EditText) findViewById(R.id.article_select_check_reprint_link);
        if(2 == uploadArticleData.getIsOriginal()){
            isCheck = 2;
            originalImg.setImageResource(R.drawable.i_article_select_yes);
        }else if(1 == uploadArticleData.getIsOriginal()){
            isCheck = 1;
            reprintImg.setImageResource(R.drawable.i_article_select_yes);
        }

        data = new ArrayList<>();
        int windW = ToolsDevice.getWindowPx(this).widthPixels;
        final int itemSpace = (windW - Tools.getDimen(this,R.dimen.dp_20) * 2 - Tools.getDimen(this,R.dimen.dp_97) * 3) / 4;
        final int dp3 = Tools.getDimen(this,R.dimen.dp_3);
        Log.i("articleSelect","articleSelect windW:" + windW + "    itemSpace:" + itemSpace);
        gridView = (GridView) findViewById(R.id.article_select_gridview);
        adapterSimple = new AdapterSimple(gridView,data,R.layout.a_article_select_activity_item,
                new String[]{"name"},
                new int[]{R.id.article_select_classify_text}){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(R.id.article_select_classify_text);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
                if(position % 3 == 0){
                    layoutParams.setMargins(0,0,itemSpace - dp3,0);
                }else if(position % 3 == 2) {
                    layoutParams.setMargins(itemSpace - dp3,0,0,0);
                }else{
                    layoutParams.setMargins(itemSpace,0,itemSpace,0);
                }
                if(data.get(position).get("code").equals(checkCode)){
                    textView.setBackgroundResource(R.drawable.article_select_classify_yes);
                    textView.setTextColor(Color.parseColor("#ffffff"));
                }
                return view;
            }
        };

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i = 0; i < data.size(); i ++){
                    TextView textView = (TextView) gridView.getChildAt(i).findViewById(R.id.article_select_classify_text);
                    if(i == position){
                        textView.setBackgroundResource(R.drawable.article_select_classify_yes);
                        textView.setTextColor(Color.parseColor("#ffffff"));
                        checkCode = data.get(i).get("code");
                        XHClick.mapStat(ArticleSelectActiivty.this, "a_ArticleEdit", "下一步_选择分类", textView.getText() + "");
                    }else{
                        textView.setBackgroundResource(R.drawable.article_select_classify_no);
                        textView.setTextColor(Color.parseColor("#333333"));
                    }
                }
            }
        });
    }

    private void initData(){
        draftId = getIntent().getIntExtra("draftId",-1);
        uploadArticleData = sqLite.selectById(draftId);
        checkCode = uploadArticleData.getClassCode();
    }

    private void getClassifyData(){
        loadManager.showProgressBar();
        String url = "";
        if(dataType == EditParentActivity.TYPE_ARTICLE)
            url = StringManager.api_getArticleClass;
        else if(dataType == EditParentActivity.TYPE_VIDEO)
            url = StringManager.getVideoClass;
        ReqEncyptInternet.in().doEncypt(url, "", new InternetCallback(this) {
            @Override
            public void loaded(int i, String s, Object o) {
                Log.i("commentUpload","getClassifyData() falg:" + i + "  return data:" + o);
                if(i >= ReqInternet.REQ_OK_STRING){
                    ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(o);
                    if(arrayList.size() > 0){
                        data.addAll(arrayList);
                        gridView.setAdapter(adapterSimple);
                        findViewById(R.id.article_select_classify).setVisibility(View.VISIBLE);
                        gridView.setVisibility(View.VISIBLE);
                        findViewById(R.id.article_select_other).setVisibility(View.VISIBLE);
                        loadManager.hideProgressBar();
                        return;
                    }
                }
                loadManager.hideProgressBar();
                loadManager.showLoadFaildBar();
            }
        });
    }

    private void upload(){
        Log.i("tzy","content = " + uploadArticleData.getContent());
        uploadArticleData.setClassCode(checkCode);
        uploadArticleData.setIsOriginal(isCheck);
        uploadArticleData.setRepAddress(String.valueOf(reprintLink.getText()));
        sqLite.update(draftId,uploadArticleData);
        if(sqLite.checkHasMedia(draftId)) {
            Intent intent = new Intent(this, ArticleUploadListActivity.class);
            intent.putExtra("draftId", draftId);
            intent.putExtra("dataType", dataType);
            intent.putExtra("coverPath", uploadArticleData.getImg());
            String videoPath = "";
            ArrayList<Map<String,String>> videoArray = uploadArticleData.getVideoArray();
            if(videoArray.size() > 0){
                videoPath = videoArray.get(0).get("video");
            }
            intent.putExtra("finalVideoPath", videoPath);
            startActivity(intent);
        }else{
            InternetCallback internetCallback = new InternetCallback(ArticleSelectActiivty.this) {
                @Override
                public void loaded(int i, String s, Object o) {

                }
            };
            if(dataType == EditParentActivity.TYPE_ARTICLE) {
                uploadArticleData.upload(StringManager.api_articleAdd,internetCallback);
            }else if(dataType == EditParentActivity.TYPE_VIDEO) {
                uploadArticleData.upload(StringManager.api_videoAdd,internetCallback);
            }
        }
        finish();
        tjClosePage();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        if(dataType == EditParentActivity.TYPE_ARTICLE) {
            intent.setClass(this, ArticleEidtActiivty.class);
            XHClick.mapStat(this, "a_ArticleEdit", "下一步", "返回");
        } else if(dataType == EditParentActivity.TYPE_VIDEO)
            intent.setClass(this,VideoEditActivity.class);
        startActivity(intent);
        tjClosePage();
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
                    if ("wifi".equals(ToolsDevice.getNetWorkType(ArticleSelectActiivty.this))) {
                        upload();
                    }else{
                        final XhDialog xhDialog = new XhDialog(ArticleSelectActiivty.this);
                        xhDialog.setTitle("当前不是WiFi环境，是否发布？")
                                .setCanselButton("暂不发布", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        xhDialog.cancel();
                                    }
                                }).setSureButton("继续", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                upload();
                                xhDialog.cancel();
                            }
                        }).show();
                    }
                }else{
                    Tools.showToast(ArticleSelectActiivty.this,"请选择原创/转载");
                    return;
                }
                XHClick.mapStat(this, "a_ArticleEdit", "下一步", "发布");
                break;
            case R.id.article_select_check_original_link:
                Tools.showToast(ArticleSelectActiivty.this,"香哈原创声明");
                XHClick.mapStat(this, "a_ArticleEdit", "下一步_文章来源", "《香哈原创声明》");
                AppCommon.openUrl(ArticleSelectActiivty.this,StringManager.api_agreementOriginal,true);
                break;
            case R.id.article_select_check_reprint:
                isCheck = 1;
                reprintImg.setImageResource(R.drawable.i_article_select_yes);
                originalImg.setImageResource(R.drawable.i_article_select_no);
                XHClick.mapStat(this, "a_ArticleEdit", "下一步_文章来源", "转载");
                break;
            case R.id.article_select_check_original:
                isCheck = 2;
                originalImg.setImageResource(R.drawable.i_article_select_yes);
                reprintImg.setImageResource(R.drawable.i_article_select_no);
                XHClick.mapStat(this, "a_ArticleEdit", "下一步_文章来源", "原创");
                break;

        }
    }

    //统计 关闭发布页面
    private void tjClosePage() {
        XHClick.mapStat(this, "a_post_button","关闭发布页面","");
    }
}
