package amodule.article.activity.edit;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.article.activity.ArticleVideoSelectorActivity;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadParentSQLite;
import amodule.article.view.EditBottomControler;
import amodule.article.view.InputUrlDialog;
import amodule.article.view.TextAndImageMixLayout;
import amodule.dish.db.UploadDishData;
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
public abstract class EditParentActivity extends BaseActivity implements View.OnClickListener {

    public final static int MAX_IMAGE = 50;

    private final int REQUEST_SELECT_IMAGE = 0x01;
    private final int REQUEST_SELECT_VIDEO = 0x02;

    public static final int TYPE_ARTICLE = 100;
    public static final int TYPE_VIDEO = 101;

    private EditBottomControler editBottomControler;
    protected EditText editTitle;
    protected TextAndImageMixLayout mixLayout;
    private LinearLayout contentLayout;

    protected UploadParentSQLite sqLite;

    protected UploadArticleData uploadArticleData;
    private String code;

    private boolean isKeyboradShow = false;


    /**
     * 定时存草稿
     */
    protected Timer timer;
    private int taskTime = 30 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sufureView页面闪烁
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        initActivity("", 5, 0, 0, R.layout.a_article_edit_activity);
    }

    protected void initView(String title) {
        //处理状态栏引发的问题
        if (Tools.isShowTitle()) {
            final RelativeLayout bottomBarLayout = (RelativeLayout) findViewById(R.id.edit_controler_layout);
            rl.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            int heightDiff = rl.getRootView().getHeight() - rl.getHeight();
                            Rect r = new Rect();
                            rl.getWindowVisibleDisplayFrame(r);
                            int screenHeight = rl.getRootView().getHeight();
                            int heightDifference = screenHeight - (r.bottom - r.top);
                            isKeyboradShow = heightDifference > 200;
                            heightDifference = isKeyboradShow ? heightDifference - heightDiff : 0;
                            bottomBarLayout.setPadding(0, 0, 0, heightDifference);
                        }
                    });
        }
        String color = Tools.getColorStr(this, R.color.common_top_bg);
        Tools.setStatusBarColor(this, Color.parseColor(color));
        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(title);

        findViewById(R.id.nextStep).setVisibility(View.VISIBLE);
        findViewById(R.id.nextStep).setOnClickListener(this);
        ImageView close = (ImageView) findViewById(R.id.leftImgBtn);
        close.setImageResource(R.drawable.i_close);
        close.setOnClickListener(this);

        contentLayout = (LinearLayout) findViewById(R.id.content_layout);

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
                if (s.length() > 0 && s.length() > 59) {
                    editTitle.setText(s.subSequence(0, 59));
                }
            }
        });
        mixLayout = (TextAndImageMixLayout) findViewById(R.id.text_image_mix_ayout);
        mixLayout.setMaxVideoCount(getMaxVideoCount());
        //初始化底部编辑控制
        initEditBottomControler();
    }

    /**
     * 初始化底部编辑控制
     */
    private void initEditBottomControler() {
        editBottomControler = (EditBottomControler) findViewById(R.id.edit_controler);
        if (getMaxImageCount() != 0)
            editBottomControler.setOnSelectImageCallback(
                    new EditBottomControler.OnSelectImageCallback() {
                        @Override
                        public void onSelectImage() {
                            Intent intent = new Intent(EditParentActivity.this, ImageSelectorActivity.class);
                            intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_MODE, ImageSelectorConstant.MODE_MULTI);
                            ArrayList<String> imageArray = mixLayout.getImageArray();
                            intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT, getMaxImageCount() - imageArray.size());
                            intent.putExtra(ImageSelectorConstant.EXTRA_NOT_SELECTED_LIST, imageArray);
                            startActivityForResult(intent, REQUEST_SELECT_IMAGE);
                        }
                    });
        if (getMaxVideoCount() != 0)
            editBottomControler.setOnSelectVideoCallback(
                    new EditBottomControler.OnSelectVideoCallback() {
                        @Override
                        public void onSelectVideo() {
                            Intent intent = new Intent(EditParentActivity.this, ArticleVideoSelectorActivity.class);
                            startActivityForResult(intent, REQUEST_SELECT_VIDEO);
                        }
                    });
        if(canAddLink())
        editBottomControler.setOnAddLinkCallback(
                new EditBottomControler.OnAddLinkCallback() {
                    @Override
                    public void onAddLink() {
                        if (mixLayout.getURLCount() >= getMaxURLCount()) {
                            Tools.showToast(EditParentActivity.this, "链接最大不能超过" + getMaxURLCount() + "条");
                            return;
                        }
                        //收起键盘
                        if (isKeyboradShow)
                            ToolsDevice.keyboardControl(!isKeyboradShow, EditParentActivity.this, mixLayout.getCurrentEditText().getRichText());
                        final int start = mixLayout.getSelectionStart();
                        final int end = mixLayout.getSelectionEnd();
                        InputUrlDialog dialog = new InputUrlDialog(EditParentActivity.this);
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
                        ToolsDevice.keyboardControl(!isKeyboradShow, EditParentActivity.this, mixLayout.getCurrentEditText().getRichText());
                    }
                });
        final int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
        if (isEnableEditText())
            editBottomControler.setOnTextEidtCallback(
                    new EditBottomControler.OnTextEditCallback() {
                        @Override
                        public void onEditControlerShow(boolean isShow) {
                            contentLayout.setPadding(0, 0, 0, isShow ? dp_45 * 2 : dp_45);
                        }

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

    protected abstract boolean canAddLink();

    protected abstract boolean isEnableEditText();

    protected abstract int getMaxImageCount();

    protected abstract int getMaxVideoCount();

    protected abstract int getMaxTextCount();

    protected abstract int getMaxURLCount();

    protected void initData(UploadParentSQLite uploadParentSQLite) {
        sqLite = uploadParentSQLite;
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (uploadArticleData != null) {
                    editTitle.setText(uploadArticleData.getTitle());
                    mixLayout.setXHServiceData(uploadArticleData.getContent());
                }
            }
        };
        //通过code判断从数据库拿数据还是从服务端拿数据
        code = getIntent().getStringExtra("code");

        if (TextUtils.isEmpty(code)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int draftId = getIntent().getIntExtra("draftId", 0);
                    if (draftId > 0) {
                        uploadArticleData = sqLite.selectById(draftId);
                    } else {
                        uploadArticleData = sqLite.getDraftData();
                    }
                    handler.sendEmptyMessage(0);
                }
            }).start();
        } else {
            ReqEncyptInternet.in().doEncypt(StringManager.api_getArticleInfo, "code=" + code, new InternetCallback(this) {
                @Override
                public void loaded(int i, String s, Object o) {
                    if (i == ReqInternet.REQ_OK_STRING) {
                        ArrayList<Map<String,String>> arrayList = StringManager.getListMapByJson(o);
                        if(arrayList.size() > 0) {
                            Map<String,String> map = arrayList.get(0);
                            uploadArticleData = new UploadArticleData();
                            uploadArticleData.setCode(code);
                            uploadArticleData.setTitle(map.get("title"));
                            uploadArticleData.setContent(map.get("content"));
                            uploadArticleData.setIsOriginal(map.get("isOriginal"));
                            uploadArticleData.setRepAddress(map.get("repAddress"));
                            uploadArticleData.setClassCode(map.get("classCode"));
                            handler.sendEmptyMessage(0);
                        }else{
                            Tools.showToast(EditParentActivity.this,"数据错误");
                            EditParentActivity.this.finish();
                        }
                    }
                }
            });
        }
        timingSave();
    }

    public abstract void onNextSetp();

    protected String checkData() {
        if (TextUtils.isEmpty(editTitle.getText())) {
            return "标题不能为空";
        }
        boolean isHasText = mixLayout.hasText();
        boolean isHasImg = mixLayout.hasImage();
        boolean isHasVideo = mixLayout.hasVideo();
        Log.i("articleUpload", "checkData() isHasText:" + isHasText + "  isHasImg=" + isHasImg + "   isHasVideo:" + isHasVideo);
        if (!isHasText && !isHasImg && !isHasVideo) {
            return "内容不能为空";
        }
        if (!isHasText) {
            return "内容文字不能为空";
        }
        if (mixLayout.getTextCount() > getMaxTextCount()) {
            return "文字不能超过" + getMaxTextCount() + "字";
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
                final XhDialog xhDialog = new XhDialog(EditParentActivity.this);
                xhDialog.setTitle("系统将自动为你保存最后1篇为草稿")
                        .setSureButton("我知道了", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                saveDraft();
                                timingSave();
                                xhDialog.cancel();
                            }
                        }).show();
            } else {
                saveDraft();
            }
        }
    }

    private void timingSave() {
        timer = new Timer();
        final Handler handler = new Handler(Looper.getMainLooper());
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

    protected int saveDraft() {
        int id;
        uploadArticleData.setTitle(String.valueOf(editTitle.getText()));
        String content = mixLayout.getXHServiceData();
        Log.i("articleUpload","saveDraft() content:" + content);
        uploadArticleData.setContent(content);
        uploadArticleData.setVideo(mixLayout.getFirstVideoUrl());
        uploadArticleData.setVideoImg(mixLayout.getFirstCoverImage());
        uploadArticleData.setImgArray(mixLayout.getImageMapArray());
        uploadArticleData.setUploadType(UploadDishData.UPLOAD_DRAF);

        if (uploadArticleData.getId() > 0) {
            id = sqLite.update(uploadArticleData.getId(), uploadArticleData);
        } else {
            id = sqLite.insert(uploadArticleData);
            if (id > 0) {
                uploadArticleData.setId(id);
            }
        }
        if (id > 0) {
            Tools.showToast(EditParentActivity.this, "保存成功");
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
                    mixLayout.addVideo(coverPath, videoPath, true, mixLayout.getCurrentEditText().getSelectionEndContent());
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
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
                    Tools.showToast(EditParentActivity.this, "不保存：" + id);
                    if (id > 0) {
                        boolean isDelete = sqLite.deleteById(id);
                        Tools.showToast(EditParentActivity.this, "删除：" + isDelete);
                    }
                    xhDialog.cancel();
                    EditParentActivity.this.finish();
                }
            }).setSureButton("是", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveDraft();
                    xhDialog.cancel();
                    EditParentActivity.this.finish();
                }
            }).show();
        } else {
            finish();
        }
    }
}
