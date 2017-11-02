package amodule.article.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.article.activity.edit.ArticleEidtActivity;
import amodule.article.activity.edit.EditParentActivity;
import amodule.article.activity.edit.VideoEditActivity;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadArticleSQLite;
import amodule.article.db.UploadParentSQLite;
import amodule.article.db.UploadVideoSQLite;
import amodule.dish.db.UploadDishData;
import amodule.main.Main;
import amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver;
import amodule.user.activity.FriendHome;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

import static amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver.SECONDE_EDIT;

/**
 * Created by XiangHa on 2017/5/22.
 * 文章或视频发布选择分类
 */
public class ArticleSelectActivity extends BaseActivity implements View.OnClickListener{

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

    private boolean isSecondEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataType = getIntent().getIntExtra("dataType",0);
        if(dataType == EditParentActivity.DATA_TYPE_ARTICLE) {
            initActivity("发文章", 5, 0, R.layout.a_article_select_title, R.layout.a_article_select_activity);
            sqLite = new UploadArticleSQLite(XHApplication.in().getApplicationContext());
        }else if(dataType == EditParentActivity.DATA_TYPE_VIDEO) {
            initActivity("发视频", 5, 0, R.layout.a_article_select_title, R.layout.a_article_select_activity);
            sqLite = new UploadVideoSQLite(XHApplication.in().getApplicationContext());
        }else{
            Tools.showToast(this,"发布数据类型为空");
            finish();
        }
        initData();
        initView();
    }

    private void initView(){
        RelativeLayout allContent = (RelativeLayout) findViewById(R.id.all_content);
        if (allContent != null)
            allContent.setBackgroundColor(Color.parseColor("#ffffff"));
        View upload = findViewById(R.id.upload);
        upload.setVisibility(View.GONE);
        upload.setOnClickListener(this);
        TextView link = (TextView) findViewById(R.id.article_select_check_original_link);
        link.setText(Html.fromHtml("<u>《香哈原创声明》</u>"));
        link.setOnClickListener(this);
        originalImg = (ImageView) findViewById(R.id.article_select_check_original);
        originalImg.setOnClickListener(this);
        findViewById(R.id.article_select_check_original_hint).setOnClickListener(this);
        reprintImg = (ImageView) findViewById(R.id.article_select_check_reprint);
        reprintImg.setOnClickListener(this);
        reprintLink = (EditText) findViewById(R.id.article_select_check_reprint_link);
        reprintLink.setOnClickListener(this);
        reprintLink.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 200){
                    Tools.showToast(ArticleSelectActivity.this,"最多200字");
                    reprintLink.setText(s.subSequence(0,200));
                }
            }
        });
        findViewById(R.id.article_select_check_hint).setOnClickListener(this);
        if(2 == uploadArticleData.getIsOriginal()){
            isCheck = 2;
            originalImg.setImageResource(R.drawable.i_article_select_yes);
        }else if(1 == uploadArticleData.getIsOriginal()){
            isCheck = 1;
            reprintImg.setImageResource(R.drawable.i_article_select_yes);
            reprintLink.setText(uploadArticleData.getRepAddress());
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
                if(position % 3 == 0){
                    ((LinearLayout)textView.getParent()).setGravity(Gravity.LEFT);
                }else if(position % 3 == 2) {
                    ((LinearLayout)textView.getParent()).setGravity(Gravity.RIGHT);
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
                        XHClick.mapStat(ArticleSelectActivity.this, "a_ArticleEdit", "下一步_选择分类", textView.getText() + "");
                    }else{
                        textView.setBackgroundResource(R.drawable.article_select_classify_no);
                        textView.setTextColor(Color.parseColor("#333333"));
                    }
                }
            }
        });
        loadManager.setLoading(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getClassifyData();
            }
        },true);
    }

    private void initData(){
        draftId = getIntent().getIntExtra("draftId",-1);
        uploadArticleData = sqLite.selectById(draftId);
        checkCode = uploadArticleData.getClassCode();
        isSecondEdit = !TextUtils.isEmpty(uploadArticleData.getCode());
    }

    private boolean isLoading = false;
    private synchronized void getClassifyData(){
        if(isLoading)return;
        isLoading = true;
        loadManager.showProgressBar();
        String url = "";
        if(dataType == EditParentActivity.DATA_TYPE_ARTICLE)
            url = StringManager.api_getArticleClass;
        else if(dataType == EditParentActivity.DATA_TYPE_VIDEO)
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
                        findViewById(R.id.upload).setVisibility(View.VISIBLE);
                        findViewById(R.id.article_select_other).setVisibility(View.VISIBLE);
                        reprintLink.clearFocus();
                        loadManager.hideProgressBar();
                        isLoading = false;
                        return;
                    }
                }
                isLoading = false;
                loadManager.hideProgressBar();
                loadManager.showLoadFaildBar();

            }
        });
    }

    private void upload(){
        uploadArticleData.setClassCode(checkCode);
        uploadArticleData.setIsOriginal(isCheck);
        uploadArticleData.setRepAddress(String.valueOf(reprintLink.getText()));
        sqLite.update(draftId,uploadArticleData);
        if(sqLite.checkHasMedia(draftId)) {
            Intent intent = new Intent(this, ArticleUploadListActivity.class);
            intent.putExtra("draftId", draftId);
            intent.putExtra("dataType", dataType);
            intent.putExtra("coverPath", uploadArticleData.getImg());
            intent.putExtra("isAutoUpload", true);
            String videoPath = "";
            ArrayList<Map<String,String>> videoArray = uploadArticleData.getVideoArray();
            if(videoArray.size() > 0){
                videoPath = videoArray.get(0).get("video");
            }
            intent.putExtra("finalVideoPath", videoPath);
            startActivity(intent);
        }else{
            InternetCallback internetCallback = new InternetCallback(ArticleSelectActivity.this) {
                @Override
                public void loaded(int flag, String s, Object o) {
                    if(flag >= ReqInternet.REQ_OK_STRING){
                        sqLite.deleteById(draftId);
                    }else{
                        uploadArticleData.setUploadType(UploadDishData.UPLOAD_FAIL);
                        sqLite.update(draftId,uploadArticleData);
                    }
                }
            };
            if(dataType == EditParentActivity.DATA_TYPE_ARTICLE) {
                uploadArticleData.upload(StringManager.api_articleAdd,internetCallback);
            }else if(dataType == EditParentActivity.DATA_TYPE_VIDEO) {
                uploadArticleData.upload(StringManager.api_videoAdd,internetCallback);
            }
            gotoFriendHome();
        }
        finish();
        tjClosePage();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        if(dataType == EditParentActivity.DATA_TYPE_ARTICLE) {
            intent.setClass(this, ArticleEidtActivity.class);
            XHClick.mapStat(this, "a_ArticleEdit", "下一步", "返回");
        } else if(dataType == EditParentActivity.DATA_TYPE_VIDEO)
            intent.setClass(this,VideoEditActivity.class);

        intent.putExtra("draftId",draftId);
        startActivity(intent);
        tjClosePage();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.upload:
                if(TextUtils.isEmpty(checkCode)){
                    Tools.showToast(ArticleSelectActivity.this,"请选择分类");
                    return;
                }
                if(isCheck > 0) {
                    if(ToolsDevice.getNetActiveState(ArticleSelectActivity.this)) {
                        if (sqLite.checkHasMedia(draftId)) {
                            upload();
                        } else {
                            hintDilog();
                        }
                    }else{
                        Tools.showToast(ArticleSelectActivity.this,"网络错误，请检查网络或重试");
                    }
                }else{
                    Tools.showToast(ArticleSelectActivity.this,"请选择原创/转载");
                    return;
                }
                XHClick.mapStat(this, "a_ArticleEdit", "下一步", "发布");
                break;
            case R.id.article_select_check_original_link:
                XHClick.mapStat(this, "a_ArticleEdit", "下一步_文章来源", "《香哈原创声明》");
                AppCommon.openUrl(ArticleSelectActivity.this,StringManager.api_agreementOriginal,true);
                break;
            case R.id.article_select_check_reprint:
            case R.id.article_select_check_hint:
            case R.id.article_select_check_reprint_link:
                isCheck = 1;
                reprintImg.setImageResource(R.drawable.i_article_select_yes);
                originalImg.setImageResource(R.drawable.i_article_select_no);
                XHClick.mapStat(this, "a_ArticleEdit", "下一步_文章来源", "转载");
                break;
            case R.id.article_select_check_original:
            case R.id.article_select_check_original_hint:
                isCheck = 2;
                originalImg.setImageResource(R.drawable.i_article_select_yes);
                reprintImg.setImageResource(R.drawable.i_article_select_no);
                XHClick.mapStat(this, "a_ArticleEdit", "下一步_文章来源", "原创");
                break;

        }
    }

    private void hintDilog(){
        if ("wifi".equals(ToolsDevice.getNetWorkType(ArticleSelectActivity.this))) {
            upload();
        }else{
            final DialogManager dialogManager = new DialogManager(ArticleSelectActivity.this);
            dialogManager.createDialog(new ViewManager(dialogManager)
                    .setView(new TitleMessageView(ArticleSelectActivity.this).setText("当前不是WiFi环境，是否发布？"))
                    .setView(new HButtonView(ArticleSelectActivity.this)
                            .setNegativeText("取消", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogManager.cancel();
                                }
                            })
                            .setPositiveText("确定", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    upload();
                                    dialogManager.cancel();
                                }
                            }))).show();
        }
    }

    private void gotoFriendHome() {
        Log.i("articleUpload","gotoFriendHome() FriendHome.isAlive:" + FriendHome.isAlive + "   code:" + LoginManager.userInfo.get("code"));
        Main.colse_level = 5;
        if (FriendHome.isAlive) {
            Intent broadIntent = new Intent();
            broadIntent.setAction(UploadStateChangeBroadcasterReceiver.ACTION);
            String type = "";
            if (this.dataType == EditParentActivity.DATA_TYPE_ARTICLE)
                type = "2";
            else if (this.dataType == EditParentActivity.DATA_TYPE_VIDEO)
                type = "1";
            if (!TextUtils.isEmpty(type))
                broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.DATA_TYPE, type);
            broadIntent.putExtra(SECONDE_EDIT,isSecondEdit ? "2" : "1");
            Main.allMain.sendBroadcast(broadIntent);
        } else {
            Intent intent = new Intent();
            intent.putExtra("code", LoginManager.userInfo.get("code"));
            if(dataType == EditParentActivity.DATA_TYPE_ARTICLE)
                intent.putExtra("index", 3);
            else if(dataType == EditParentActivity.DATA_TYPE_VIDEO)
                intent.putExtra("index", 2);
            intent.putExtra(SECONDE_EDIT,isSecondEdit ? "2" : "1");
            intent.setClass(this, FriendHome.class);
            startActivity(intent);
        }
        finish();
    }

    //统计 关闭发布页面
    private void tjClosePage() {
        XHClick.mapStat(this, "a_post_button","关闭发布页面","");
    }
}
