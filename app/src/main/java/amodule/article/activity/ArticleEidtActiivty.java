package amodule.article.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadArticleSQLite;
import amodule.article.view.EditBottomControler;
import amodule.article.view.InputUrlDialog;
import amodule.article.view.TextAndImageMixLayout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;
import aplug.recordervideo.db.RecorderVideoData;
import xh.windowview.XhDialog;

/**
 * PackageName : amodule.article.activity
 * Created by MrTrying on 2017/5/19 09:19.
 * E_mail : ztanzeyu@gmail.com
 */

public class ArticleEidtActiivty extends BaseActivity implements View.OnClickListener {

    private final int REQUEST_SELECT_IMAGE = 0x01;
    private final int REQUEST_SELECT_VIDEO = 0x02;

    private EditBottomControler editBottomControler;
    private TextAndImageMixLayout mixLayout;

    private UploadArticleSQLite sqLite;

    private EditText editTitle;
    private UploadArticleData uploadArticleData;
    private String code;

    /**
     * 定时存草稿
     */
    private Timer timer;
    private int taskTime = 30 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sufureView页面闪烁
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        initActivity("发文章", 2, 0, 0, R.layout.a_article_edit_activity);

        initView();
        initData();
    }

    private void initView() {
        String color = Tools.getColorStr(this, R.color.common_top_bg);
        Tools.setStatusBarColor(this, Color.parseColor(color));

        TextView title = (TextView) findViewById(R.id.title);
        title.setText("写文章");
        findViewById(R.id.nextStep).setVisibility(View.VISIBLE);
        findViewById(R.id.nextStep).setOnClickListener(this);
        ImageView close = (ImageView) findViewById(R.id.leftImgBtn);
        close.setImageResource(R.drawable.i_close);
        close.setOnClickListener(this);

        SpannableString ss = new SpannableString("标题（64字以内）");
        int titleSize = Tools.getDimen(this, R.dimen.dp_25);
        int hintSize = Tools.getDimen(this, R.dimen.dp_14);
        ss.setSpan(new AbsoluteSizeSpan(titleSize), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new AbsoluteSizeSpan(hintSize), 2, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        editTitle = (EditText) findViewById(R.id.edit_title);
        editTitle.setHint(ss);
        editTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    if (s.length() > 59) {
                        editTitle.setText(s.subSequence(0, 59));
                    }
                }
            }
        });
        mixLayout = (TextAndImageMixLayout) findViewById(R.id.text_image_mix_ayout);

        //初始化底部编辑控制
        initEditBottomControler();
    }

    /**
     * 初始化底部编辑控制
     */
    private void initEditBottomControler() {
        editBottomControler = (EditBottomControler) findViewById(R.id.edit_controler);
        editBottomControler.setOnSelectImageCallback(
                new EditBottomControler.OnSelectImageCallback() {
                    @Override
                    public void onSelectImage() {
                        Intent intent = new Intent(ArticleEidtActiivty.this, ImageSelectorActivity.class);
                        intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_MODE, ImageSelectorConstant.MODE_MULTI);
                        ArrayList<String> imageArray = mixLayout.getImageArray();
                        intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT, 10 - imageArray.size());
                        intent.putExtra(ImageSelectorConstant.EXTRA_NOT_SELECTED_LIST, imageArray);
                        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
                    }
                });
        editBottomControler.setOnSelectVideoCallback(
                new EditBottomControler.OnSelectVideoCallback() {
                    @Override
                    public void onSelectVideo() {
                        Intent intent = new Intent(ArticleEidtActiivty.this,ArticleVideoSelectorActivity.class);
                        startActivityForResult(intent, REQUEST_SELECT_VIDEO);
                    }
                });
        editBottomControler.setOnAddLinkCallback(
                new EditBottomControler.OnAddLinkCallback() {
                    @Override
                    public void onAddLink() {
                        final int start = mixLayout.getSelectionStart();
                        final int end = mixLayout.getSelectionEnd();
                        InputUrlDialog dialog = new InputUrlDialog(ArticleEidtActiivty.this);
                        dialog.setDescDefault(mixLayout.getSelectionText());
                        dialog.setOnReturnResultCallback(
                                new InputUrlDialog.OnReturnResultCallback() {
                                    @Override
                                    public void onSure(String url, String desc) {
                                        mixLayout.addLink(url, desc, start, end);
                                    }

                                    @Override
                                    public void onCannel() {

                                    }
                                });
                        dialog.show();
                    }
                });
        editBottomControler.setOnKeyboardControlCallback(
                new EditBottomControler.OnKeyboardControlCallback() {
                    @Override
                    public void onKeyboardControlSwitch() {
                        Toast.makeText(ArticleEidtActiivty.this, "onKeyboardControlSwitch", Toast.LENGTH_SHORT).show();
                    }
                });
        editBottomControler.setOnTextEidtCallback(
                new EditBottomControler.OnTextEidtCallback() {
                    @Override
                    public void onTextBold() {
                        mixLayout.setupTextBold();
                    }

                    @Override
                    public void onTextUnderLine() {
                        mixLayout.setupUnderline();
                    }

                    @Override
                    public void onTextCenter() {
                        mixLayout.setupTextCenter();
                    }
                });
    }

    private void initData() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (uploadArticleData != null) {
                    editTitle.setText(uploadArticleData.getTitle());
                }
            }
        };
        //通过code判断从数据库拿数据还是从服务端拿数据
        code = getIntent().getStringExtra("code");
        sqLite = new UploadArticleSQLite(ArticleEidtActiivty.this);
        if (TextUtils.isEmpty(code)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    uploadArticleData = sqLite.getDraftData();
                    handler.sendEmptyMessage(0);
                }
            }).start();
        } else {
            ReqEncyptInternet.in().doEncypt(StringManager.api_getArticleInfo, "", new InternetCallback(this) {
                @Override
                public void loaded(int i, String s, Object o) {
                    if (i == ReqInternet.REQ_OK_STRING) {
                        uploadArticleData = new UploadArticleData();

                        handler.sendEmptyMessage(0);
                    }
                }
            });
        }
        timingSave();
    }

    private void onNextSetp() {
        Log.i("tzy",mixLayout.getXHServiceData());
        String checkStr = checkData();
        if (TextUtils.isEmpty(checkStr)) {
            Intent intent = new Intent(this, ArticleSelectActiivty.class);
            startActivity(intent);
        } else {
            Tools.showToast(this, checkStr);
        }
    }

    private String checkData() {
        if (TextUtils.isEmpty(editTitle.getText())) {
            return "标题不能为空";
        }
        boolean isHasText = mixLayout.hasText();
        boolean isHasImg = mixLayout.hasImage();
        boolean isHasVideo = mixLayout.hasVideo();
        if(!isHasText && !isHasImg && !isHasVideo){
            return "内容不能为空";
        }
        if(!isHasText){
            return "内容文字不能为空";
        }
        return null;
    }

    private boolean isFist = true; //是不是第一次执行定时操作

    private void startTimeSaveDraft() {
        String checkStr = checkData();
        if (TextUtils.isEmpty(checkStr)) {
            if (isFist) {
                isFist = false;
                timer.cancel();
                timer = null;
                final XhDialog xhDialog = new XhDialog(ArticleEidtActiivty.this);
                xhDialog.setTitle("系统将自动为你保存最后1篇为草稿").setSureButton("我知道了", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveDraft();
                        timingSave();
                        xhDialog.cancel();
                    }
                }).show();
            }else{
                saveDraft();
            }
        }
    }

    private void timingSave() {
        timer = new Timer();
        final Handler handler = new Handler();
        TimerTask tt = new TimerTask() {

            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        startTimeSaveDraft();
                    }
                });
            }
        };
        timer.schedule(tt, taskTime, taskTime);
    }

    private int saveDraft() {
        int id;
        uploadArticleData.setTitle(String.valueOf(editTitle.getText()));
        uploadArticleData.setContent(mixLayout.getXHServiceData());
        uploadArticleData.setVideo(mixLayout.getFirstVideoUrl());
        uploadArticleData.setImg(mixLayout.getFirstImage());
        uploadArticleData.setVideoImg(mixLayout.getFirstCoverImage());

        if (uploadArticleData.getId() > 0) {
            id = sqLite.update(uploadArticleData.getId(), uploadArticleData);
        } else {
            id = sqLite.insert(uploadArticleData);
            if (id > 0) {
                uploadArticleData.setId(id);
            }
        }
        if (id > 0) {
            Tools.showToast(ArticleEidtActiivty.this, "保存成功");
        }
        return id;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_SELECT_IMAGE:
                    List<String> imagePathArray = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);
                    mixLayout.addImageArray(imagePathArray);
                    break;
                case REQUEST_SELECT_VIDEO:
                    String videoPath = data.getStringExtra(MediaStore.Video.Media.DATA);
                    String coverPath = data.getStringExtra(RecorderVideoData.video_img_path);
                    mixLayout.addVideo(coverPath, videoPath,true,mixLayout.getCurrentEditText().getSelectionEndContent());
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nextStep:
                onNextSetp();
                break;
            case R.id.leftImgBtn:
                onClose();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        onClose();
    }

    private void onClose() {
        if (TextUtils.isEmpty(checkData())) {
            final XhDialog xhDialog = new XhDialog(this);
            xhDialog.setTitle("是否保存草稿？").setCanselButton("否", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = uploadArticleData.getId();
                    Tools.showToast(ArticleEidtActiivty.this, "不保存：" + id);
                    if (id > 0) {
                        boolean isDelete = sqLite.deleteById(id);
                        Tools.showToast(ArticleEidtActiivty.this, "删除：" + isDelete);
                    }
                    xhDialog.cancel();
                    ArticleEidtActiivty.this.finish();
                }
            }).setSureButton("是", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveDraft();
                    xhDialog.cancel();
                    ArticleEidtActiivty.this.finish();
                }
            }).show();
        } else {
            ArticleEidtActiivty.this.finish();
        }
    }
}
