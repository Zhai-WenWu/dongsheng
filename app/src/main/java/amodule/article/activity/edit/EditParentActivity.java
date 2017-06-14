package amodule.article.activity.edit;

import android.content.Context;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.article.activity.ArticleVideoSelectorActivity;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadParentSQLite;
import amodule.article.view.EditBottomControler;
import amodule.article.view.EditTextView;
import amodule.article.view.InputUrlDialog;
import amodule.article.view.TextAndImageMixLayout;
import amodule.article.view.VideoShowView;
import amodule.dish.db.UploadDishData;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;
import aplug.recordervideo.db.RecorderVideoData;
import xh.windowview.XhDialog;

import static amodule.article.view.richtext.RichText.FORMAT_BOLD;
import static amodule.article.view.richtext.RichText.FORMAT_UNDERLINED;

/**
 * PackageName : amodule.article.activity
 * Created by MrTrying on 2017/5/19 09:19.
 * E_mail : ztanzeyu@gmail.com
 */
public abstract class EditParentActivity extends BaseActivity implements View.OnClickListener {

    public static final String TYPE_ARTICLE = "1";
    public static final String TYPE_VIDEO = "2";

    private final int REQUEST_SELECT_IMAGE = 0x01;
    private final int REQUEST_SELECT_VIDEO = 0x02;

    public static final int DATA_TYPE_ARTICLE = 100;
    public static final int DATA_TYPE_VIDEO = 101;

    protected EditText editTitle;
    private EditBottomControler editBottomControler;
    protected TextAndImageMixLayout mixLayout;
    private LinearLayout contentLayout;
    private ScrollView scrollView;

    protected UploadParentSQLite sqLite;

    protected UploadArticleData uploadArticleData;
    private String code;

    private boolean isKeyboradShow = false;

    /** 定时存草稿 */
    protected Timer timer;
    private int taskTime = 30 * 1000;

    private String mPageTag = "EditParentActivity";
    private final String mArticlePageTag = "ArticleEidtActiivty";
    private final String mVideoPageTag = "VideoEditActivity";
    private Context mCurrentContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sufureView页面闪烁
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        initActivity("", 5, 0, 0, R.layout.a_article_edit_activity);
        mCurrentContext = this;
        if (this instanceof ArticleEidtActiivty) {
            mPageTag = mArticlePageTag;
            XHClick.mapStat(this, "a_post_button", "文章", "进入编辑文章页面");
        } else if (this instanceof VideoEditActivity) {
            mPageTag = mVideoPageTag;
            XHClick.mapStat(this, "a_post_button", "短视频", "进入编辑视频页面");
        }
    }

    protected void initView(String title) {
        //处理状态栏引发的问题
        if (Tools.isShowTitle()) {
            final RelativeLayout bottomBarLayout = (RelativeLayout) findViewById(R.id.edit_controler_layout);
            rl.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            final boolean preIsKeyboradShow = isKeyboradShow;
                            int heightDiff = rl.getRootView().getHeight() - rl.getHeight();
                            Rect r = new Rect();
                            rl.getWindowVisibleDisplayFrame(r);
                            int screenHeight = rl.getRootView().getHeight();
                            int heightDifference = screenHeight - (r.bottom - r.top);
                            int heightDifference2 = screenHeight - (r.bottom - r.top);
                            isKeyboradShow = heightDifference > 200;
                            heightDifference = isKeyboradShow ? heightDifference - heightDiff : 0;
                            bottomBarLayout.setPadding(0, 0, 0, heightDifference);
                            int[] location = new int[2];
                            mixLayout.getCurrentEditText().getRichText().getLocationOnScreen(location);
                            DisplayMetrics dm = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(dm);
                            int distance = dm.heightPixels - location[1] - (editBottomControler.isShowEditLayout() ? dp_50 + dp_64 : dp_50);
                            Log.i("tzy","distance = " + distance);
                            Log.i("tzy","heightDifference2 = " + heightDifference2);
                            Log.i("tzy","isKeyboradShow = " + isKeyboradShow);
                            Log.i("tzy","preIsKeyboradShow = " + preIsKeyboradShow);
                            if(isKeyboradShow
                                    && !preIsKeyboradShow
                                    && distance <= heightDifference2
                                    && !editTitle.isFocused()){
                                scrollView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollView.scrollBy(0, dp_50);
                                    }
                                }, 200);
                            }
                        }
                    });
        }
        String color = Tools.getColorStr(this, R.color.common_top_bg);
        Tools.setStatusBarColor(this, Color.parseColor(color));
        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(title);

        TextView nestStep = (TextView) findViewById(R.id.nextStep);
        int dp_20 = (int) this.getResources().getDimension(R.dimen.dp_20);
        nestStep.setPadding(dp_20, 0, dp_20, 0);
        nestStep.setVisibility(View.VISIBLE);
        nestStep.setOnClickListener(this);
        ImageView leftImgBtn = (ImageView) findViewById(R.id.leftImgBtn);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) leftImgBtn.getLayoutParams();
        params.setMargins((int) getResources().getDimension(R.dimen.dp_18), 0, 0, 0);
        leftImgBtn.setImageResource(R.drawable.image_selector_close);
        int dp_2 = (int) this.getResources().getDimension(R.dimen.dp_2);
        int dp_8 = (int) this.getResources().getDimension(R.dimen.dp_8);
        leftImgBtn.setPadding(dp_2, dp_8, dp_2, dp_8);
        findViewById(R.id.back).setOnClickListener(this);

        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        editBottomControler.setEditLayoutVisibility(false);
                        ToolsDevice.keyboardControl(false, EditParentActivity.this, mixLayout.getCurrentEditText().getRichText());
                        break;
                }
                return false;
            }
        });
        contentLayout = (LinearLayout) findViewById(R.id.content_layout);
        int contentLayoutHeight = ToolsDevice.getWindowPx(this).heightPixels - Tools.getDimen(this, R.dimen.dp_45) - Tools.getStatusBarHeight(this);
        contentLayout.setMinimumHeight(contentLayoutHeight);
        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("tzy","contentLayout click");
                editTitle.clearFocus();
                mixLayout.getCurrentEditText().getRichText().requestFocus();
                ToolsDevice.keyboardControl(true, EditParentActivity.this, mixLayout.getCurrentEditText().getRichText());
            }
        });

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
                if (s.length() > 0 && s.length() > 64) {
                    editTitle.setText(s.subSequence(0, 64));
                    editTitle.setSelection(editTitle.getText().length());
                    Tools.showToast(EditParentActivity.this, "标题最多64字");
                    ToolsDevice.keyboardControl(false, EditParentActivity.this, editTitle);
                }
            }
        });
        editTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    editBottomControler.setVisibility(View.GONE);
                }
            }
        });
        mixLayout = (TextAndImageMixLayout) findViewById(R.id.text_image_mix_ayout);
        mixLayout.setMaxVideoCount(getMaxVideoCount());
        mixLayout.setMaxTextCount(getMaxTextCount());
        mixLayout.setSingleVideo("2".equals(getType()));
        mixLayout.setType(getType());
        mixLayout.setOnFocusChangeCallback(new EditTextView.OnFocusChangeCallback() {
            @Override
            public void onFocusChange(EditTextView v, boolean hasFocus) {
                if(hasFocus){
                    if(editBottomControler.getVisibility() == View.GONE){
                        scrollView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.scrollBy(0, -dp_64);
                            }
                        }, 200);
                    }
                    editBottomControler.setVisibility(View.VISIBLE);
                }
            }
        });
        mixLayout.setOnScorllEndCallback(new TextAndImageMixLayout.OnScorllEndCallback() {
            @Override
            public void onScorllEnd() {
                scrollView.scrollTo(0,mixLayout.getHeight());
            }
        });
        mixLayout.setOnSelectBoldCallback(new EditTextView.OnSelectBoldCallback() {
            @Override
            public void onSelectBold(boolean isSelected) {
                if(editBottomControler.isShowEditLayout()){
                    editBottomControler.setTextBoldImageSelection(isSelected);
                }
            }
        });
        mixLayout.setOnSelectUnderlineCallback(new EditTextView.OnSelectUnderlineCallback() {
            @Override
            public void onSelectUnderline(boolean isSelected) {
                if(editBottomControler.isShowEditLayout()){
                    editBottomControler.setTextUnderlineImageSelection(isSelected);
                }
            }
        });
        if("2".equals(getType())){
            mixLayout.setVideo(new VideoShowView.VideoDefaultClickCallback() {
                @Override
                public void defaultClick() {
                    Intent intent = new Intent(EditParentActivity.this, ArticleVideoSelectorActivity.class);
                    startActivityForResult(intent, REQUEST_SELECT_VIDEO);
                    switch (mPageTag) {
                        case mArticlePageTag:
                            XHClick.mapStat(mCurrentContext, "a_ArticleEdit", "编辑文章内容", "添加视频");
                            break;
                        case mVideoPageTag:
                            XHClick.mapStat(mCurrentContext, "a_ShortVideoEdit", "编辑视频内容", "添加视频");
                            break;
                    }
                }
            });
        }

        //初始化底部编辑控制
        initEditBottomControler();
    }

    /** 初始化底部编辑控制 */
    private void initEditBottomControler() {
        editBottomControler = (EditBottomControler) findViewById(R.id.edit_controler);
        if (getMaxImageCount() != 0)
            editBottomControler.setOnSelectImageCallback(
                    new EditBottomControler.OnSelectImageCallback() {
                        @Override
                        public void onSelectImage() {
                            if (mixLayout.getImageCount() >= getMaxImageCount()) {
                                Tools.showToast(EditParentActivity.this, "最多可选取" + getMaxImageCount() + "张图片");
                                return;
                            }
                            Intent intent = new Intent(EditParentActivity.this, ImageSelectorActivity.class);
                            intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_MODE, ImageSelectorConstant.MODE_MULTI);
                            ArrayList<String> imageArray = mixLayout.getImageArray();
                            int maxImageCount = getMaxImageCount() - imageArray.size();
                            intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT, maxImageCount > 10 ? 10 : maxImageCount);
                            intent.putExtra(ImageSelectorConstant.EXTRA_NOT_SELECTED_LIST, imageArray);
                            startActivityForResult(intent, REQUEST_SELECT_IMAGE);
                            switch (mPageTag) {
                                case mArticlePageTag:
                                    XHClick.mapStat(mCurrentContext, "a_ArticleEdit", "编辑文章内容", "添加图片");
                                    break;
                            }
                        }
                    });
        if (getMaxVideoCount() != 0)
            editBottomControler.setOnSelectVideoCallback(
                    new EditBottomControler.OnSelectVideoCallback() {
                        @Override
                        public void onSelectVideo() {
                            if (getMaxVideoCount() >= 2
                                    && mixLayout.getVideoCount() >= getMaxVideoCount()) {
                                Tools.showToast(EditParentActivity.this, "最多可选取" + getMaxVideoCount() + "个视频");
                                return;
                            }
                            Intent intent = new Intent(EditParentActivity.this, ArticleVideoSelectorActivity.class);
                            ArrayList<String> imageArray = mixLayout.getVideoArrayList();
                            intent.putStringArrayListExtra(ArticleVideoSelectorActivity.EXTRA_UNSELECT_VIDEO,imageArray);
                            startActivityForResult(intent, REQUEST_SELECT_VIDEO);
                            switch (mPageTag) {
                                case mArticlePageTag:
                                    XHClick.mapStat(mCurrentContext, "a_ArticleEdit", "编辑文章内容", "添加视频");
                                    break;
                                case mVideoPageTag:
                                    XHClick.mapStat(mCurrentContext, "a_ShortVideoEdit", "编辑视频内容", "添加视频");
                                    break;
                            }
                        }
                    });
        if (canAddLink())
            editBottomControler.setOnAddLinkCallback(
                    new EditBottomControler.OnAddLinkCallback() {
                        @Override
                        public void onAddLink() {
                            switch (mPageTag) {
                                case mArticlePageTag:
                                    XHClick.mapStat(mCurrentContext, "a_ArticleEdit", "编辑文章内容", "添加超链接");
                                    break;
                            }
                            if (mixLayout.getURLCount() >= getMaxURLCount()) {
                                Tools.showToast(EditParentActivity.this, "链接最多" + getMaxURLCount() + "条");
                                return;
                            }
                            //收起键盘
                            if (isKeyboradShow)
                                ToolsDevice.keyboardControl(!isKeyboradShow, EditParentActivity.this, mixLayout.getCurrentEditText().getRichText());
                            final int start = mixLayout.getSelectionStart();
                            final int end = mixLayout.getSelectionEnd();
                            final InputUrlDialog dialog = new InputUrlDialog(EditParentActivity.this);
                            dialog.setDescDefault(mixLayout.getSelectionText());
                            dialog.setOnReturnResultCallback(
                                    new InputUrlDialog.OnReturnResultCallback() {
                                        @Override
                                        public void onSure(String url, String desc) {
                                            mixLayout.addLink(url, desc, start, end);
                                            dialog.dismiss();
                                        }

                                        @Override
                                        public void onCannel() {
                                            dialog.dismiss();
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

        if (isEnableEditText())
            editBottomControler.setOnTextEidtCallback(
                    new EditBottomControler.OnTextEditCallback() {
                        @Override
                        public void onEditControlerShow(boolean isShow) {
                            int bottom = isShow ? dp_50 + dp_64 : dp_50;
                            contentLayout.setPadding(0, 0, 0, bottom);
                            switch (mPageTag) {
                                case mArticlePageTag:
                                    XHClick.mapStat(mCurrentContext, "a_ArticleEdit", "编辑文章内容", "字体样式");
                                    break;
                            }
                        }

                        @Override
                        public void onTextBold() {
                            mixLayout.setupTextBold();
                            if(mixLayout.getCurrentEditText().getRichText().contains(FORMAT_BOLD))
                                editBottomControler.setTextBoldImageSelection(true);
                        }

                        @Override
                        public void onTextUnderLine() {
                            mixLayout.setupUnderline();
                            if(mixLayout.getCurrentEditText().getRichText().contains(FORMAT_UNDERLINED))
                                editBottomControler.setTextUnderlineImageSelection(true);
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

    int dp_50, dp_64;

    protected void initData(UploadParentSQLite uploadParentSQLite) {
        dp_50 = Tools.getDimen(this, R.dimen.dp_50);
        dp_64 = Tools.getDimen(this, R.dimen.dp_64);
        sqLite = uploadParentSQLite;
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (uploadArticleData != null) {
                    editTitle.setText(uploadArticleData.getTitle());
                    mixLayout.setSingleVideo(TYPE_VIDEO.equals(getType()));
                    mixLayout.setXHServiceData(uploadArticleData.getContent());

                    mixLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            mixLayout.getCurrentEditText().getRichText().clearFocus();
                            editTitle.requestFocus();
                            ToolsDevice.keyboardControl(true, EditParentActivity.this, editTitle);
                        }
                    });
                }
            }
        };

        //通过code判断从数据库拿数据还是从服务端拿数据
        code = getIntent().getStringExtra("code");

        if (TextUtils.isEmpty(code)) {
            mixLayout.setSecondEdit(true);

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
            timingSave();
        } else {
            StringBuilder sbuilder = new StringBuilder().append("code=").append(code).append("&type=RAW");
            ReqEncyptInternet.in().doEncypt(getEditApi(), sbuilder.toString(), new InternetCallback(this) {
                @Override
                public void loaded(int i, String s, Object o) {
                    if (i == ReqInternet.REQ_OK_STRING) {
                        ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(o);
                        if (arrayList.size() > 0) {
                            Map<String, String> map = arrayList.get(0);
                            uploadArticleData = new UploadArticleData();
                            uploadArticleData.setCode(code);
                            uploadArticleData.setTitle(map.get("title"));
                            uploadArticleData.setContent(map.get("raw"));
                            uploadArticleData.setIsOriginal(map.get("isOriginal"));
                            uploadArticleData.setRepAddress(map.get("repAddress"));
                            uploadArticleData.setClassCode(map.get("classCode"));
                            handler.sendEmptyMessage(0);
                        } else {
                            Tools.showToast(EditParentActivity.this, "数据错误");
                            EditParentActivity.this.finish();
                        }
                    }
                }
            });
        }
    }

    public abstract String getEditApi();

    public abstract void onNextSetp();

    public abstract String getType();

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
            return "文字最多" + getMaxTextCount() + "字";
        }
        return null;
    }

    private boolean isFist = true; //是不是第一次执行定时操作

    private void startTimeSaveDraft() {
        String checkStr = checkData();
        if (TextUtils.isEmpty(checkStr)) {
            if (isFist) {
                isFist = false;
                Tools.showToast(EditParentActivity.this, "内容已保存");
                saveDraft();
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
        Log.i("articleUpload", "saveDraft() content:" + content);
        uploadArticleData.setContent(content);
        Log.i("tzy", "content = " + content);
        uploadArticleData.setVideoArray(mixLayout.getVideoArrayMap());
        uploadArticleData.setImgArray(mixLayout.getImageMapArray());
        uploadArticleData.setUploadType(UploadDishData.UPLOAD_DRAF);

        if (uploadArticleData.getId() > 0) {
            id = sqLite.update(uploadArticleData.getId(), uploadArticleData);
        } else {
            id = sqLite.insert(uploadArticleData);
            if (id > 0)
                uploadArticleData.setId(id);
        }
        Log.i("articleUpload", "saveDraft() 保存后id:" + id);
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
                    boolean isVideo = "2".equals(getType());
                    CharSequence text = "";
                    if (!isVideo)
                        text = mixLayout.getCurrentEditText().getSelectionEndContent();
                    mixLayout.addVideo(coverPath, videoPath, !isVideo, text);
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
        if(isKeyboradShow)
            ToolsDevice.keyboardControl(false,this,editTitle);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nextStep:
                onNextSetp();
                break;
            case R.id.back:
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
        if (TextUtils.isEmpty(code)) {
            switch (getType()){
                case TYPE_ARTICLE:
                    if (!TextUtils.isEmpty(editTitle.getText().toString())
                            || mixLayout.hasText()
                            || mixLayout.hasImage()
                            || mixLayout.hasVideo())
                        Tools.showToast(this, "内容已保存");
                    break;
                case TYPE_VIDEO:
                    if (!TextUtils.isEmpty(editTitle.getText().toString())
                            || mixLayout.hasText())
                        Tools.showToast(this, "内容已保存");
                    break;
            }

            saveDraft();
            finshActivity();
        } else {
            final XhDialog xhDialog = new XhDialog(EditParentActivity.this);
            xhDialog.setTitle("二次编辑的内容将不会保存到草稿箱，是否继续退出？")
                    .setSureButton("留下编辑", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            xhDialog.cancel();
                        }
                    }).setCanselButton("清空并退出", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    xhDialog.cancel();
                    finshActivity();
                }
            }).show();
        }

    }

    private void finshActivity() {
        switch (mPageTag) {
            case mVideoPageTag:
                XHClick.mapStat(this, "a_ShortVideoEdit", "关闭页面", "");
                break;
            case mArticlePageTag:
                XHClick.mapStat(this, "a_ArticleEdit", "关闭页面", "");
                break;
        }
        EditParentActivity.this.finish();
    }
}
