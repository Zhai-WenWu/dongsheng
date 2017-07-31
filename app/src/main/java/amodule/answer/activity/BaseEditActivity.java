package amodule.answer.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import amodule.answer.db.AskAnswerSQLite;
import amodule.answer.model.AskAnswerModel;
import amodule.answer.view.AskAnswerImgController;
import amodule.answer.view.AskAnswerImgItemView;
import amodule.article.activity.ArticleVideoSelectorActivity;
import amodule.user.activity.ModifyPassword;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;
import aplug.recordervideo.db.RecorderVideoData;
import aplug.shortvideo.activity.VideoFullScreenActivity;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;

/**
 * Created by sll on 2017/7/18.
 */

public class BaseEditActivity extends BaseActivity {

    private Timer mTimer;
    private TimerTask mTimerTask;
    private final int mIntervalTime = 30 * 1000;

    protected final int REQUEST_SELECT_IMAGE = 0x01;
    protected final int REQUEST_SELECT_VIDEO = 0x02;

    protected String mQACode;
    protected String mAnswerCode;
    protected String mDishCode;//菜谱code
    protected String mAuthorCode;//作者code
    protected String mQAType;//问答类型
    protected boolean mIsAskMore;//是否是追问
    protected boolean mIsAnswerMore;//是否是追问
    protected String mAnonymity;//是否匿名 "1":否 "2":是
    protected String mType = "5";//类型：5-菜谱问答
    protected String mQATitle = "";//问答相关问题的标题

    private TextView mTitle;
    private TextView mUpload;
    protected EditText mEditText;
    private LinearLayout mImgsContainer;
    protected TextView mCountText;
    private ImageView mVideoImgBtn;
    private ImageView mPhotoImgBtn;

    protected XHWebView mWebView;
    protected WebviewManager mWebViewManager;
    protected JsAppCommon mJsAppCommon;

    protected AskAnswerImgController mImgController;
    protected AskAnswerModel mModel;
    protected AskAnswerSQLite mSQLite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    protected void initData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mQACode = bundle.getString("qaCode");
            mDishCode = bundle.getString("code");
            mAuthorCode = bundle.getString("authorCode");
            mType = bundle.getString("type", "5");
            mQATitle = bundle.getString("qaTitle", "");
            mIsAskMore = bundle.getBoolean("isAskMore", false);
            mIsAnswerMore = bundle.getBoolean("mIsAnswerMore", false);
        }
        mModel = new AskAnswerModel();
        mSQLite = new AskAnswerSQLite(XHApplication.in().getApplicationContext());
        //TODO Test
        mQATitle = "红烧肉";
        mDishCode = "123456";
        mQACode = "222";
        mAuthorCode = "88888";
    }

    protected void initView(String title, int contentResId) {
        initActivity(title, 2, 0, 0, contentResId);
        mTitle = (TextView) findViewById(R.id.title);
        mUpload = (TextView) findViewById(R.id.upload);
        mEditText = (EditText) findViewById(R.id.edittext);
        mImgsContainer = (LinearLayout) findViewById(R.id.imgs);
        mCountText = (TextView) findViewById(R.id.count_text);
        mVideoImgBtn = (ImageView) findViewById(R.id.video_select_btn);
        mPhotoImgBtn = (ImageView) findViewById(R.id.img_select_btn);
        mTitle.setText(title);
        mUpload.setText("发布");
        mUpload.setEnabled(false);
        mUpload.setVisibility(View.VISIBLE);

        mWebViewManager = new WebviewManager(this, loadManager, true);
        mWebView = mWebViewManager.createWebView(R.id.XHWebview, false);
        mWebViewManager.setJSObj(mWebView, mJsAppCommon = new JsAppCommon(this, mWebView,loadManager,null));

        int width = 0;
        int height = 0;
        if (this instanceof AnswerEditActivity) {
            width = getResources().getDimensionPixelSize(R.dimen.dp_50);
            height = getResources().getDimensionPixelSize(R.dimen.dp_50);
        } else if (this instanceof AskEditActivity) {
            width = getResources().getDimensionPixelSize(R.dimen.dp_36);
            height = getResources().getDimensionPixelSize(R.dimen.dp_36);
        }
        mImgController = new AskAnswerImgController(this, mImgsContainer, width, height);
        addListener();
    }

    private void addListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.video_select_btn:
                        if (mImgController.checkCondition(true)) {
                            ArrayList<Map<String, String>> imageArray = mImgController.getVideosArray();
                            ArrayList<String> videos = new ArrayList<String>();
                            for (Map<String, String> map : imageArray) {
                                videos.add(map.get("video"));
                            }
                            Intent intent = new Intent(BaseEditActivity.this, ArticleVideoSelectorActivity.class);
                            intent.putStringArrayListExtra(ArticleVideoSelectorActivity.EXTRA_UNSELECT_VIDEO, videos);
                            startActivityForResult(intent, REQUEST_SELECT_VIDEO);
                        }
                        break;
                    case R.id.img_select_btn:
                        if (mImgController.checkCondition(false)) {
                            ArrayList<Map<String, String>> imageArray1 = mImgController.getImgsArray();
                            ArrayList<String> imgs = new ArrayList<String>();
                            for (Map<String, String> map : imageArray1) {
                                imgs.add(map.get("img"));
                            }
                            Intent intent1 = new Intent(BaseEditActivity.this, ImageSelectorActivity.class);
                            intent1.putExtra(ImageSelectorConstant.EXTRA_SELECT_MODE, ImageSelectorConstant.MODE_MULTI);
                            int maxImageCount = mImgController.getImgFixedSize() - mImgController.getDatas().size();
                            intent1.putExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT, maxImageCount > 10 ? 10 : maxImageCount);
                            intent1.putExtra(ImageSelectorConstant.EXTRA_NOT_SELECTED_LIST, imgs);
                            startActivityForResult(intent1, REQUEST_SELECT_IMAGE);
                        }
                        break;
                    case R.id.upload:
                        saveDraft();
                        if (handleUpload()) {
                            break;
                        }
                        Intent intent = new Intent(BaseEditActivity.this, AskAnswerUploadListActivity.class);
                        intent.putExtra("draftId", (int)mModel.getmId());
                        startActivity(intent);
                        break;
                    case R.id.back:
                        handleBackClick();
                        finish();
                        break;
                }
            }
        };
        mUpload.setOnClickListener(listener);
        mVideoImgBtn.setOnClickListener(listener);
        mPhotoImgBtn.setOnClickListener(listener);
        findViewById(R.id.back).setOnClickListener(listener);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = mEditText.getText().length();
                if (length > 0) {
                    if (!mUpload.isEnabled())
                        mUpload.setEnabled(true);
                } else
                    mUpload.setEnabled(false);
                onEditTextChanged(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mImgController.setOnVideoClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BaseEditActivity.this, VideoFullScreenActivity.class);
                intent.putExtra(VideoFullScreenActivity.EXTRA_VIDEO_URL, ((AskAnswerImgItemView)v).getData().get("videoPath"));
                intent.putExtra(VideoFullScreenActivity.EXTRA_VIDEO_TYPE, VideoFullScreenActivity.LOCAL_VIDEO);
                startActivity(intent);
            }
        });
        mImgController.setOnPhotoClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
//        mJsAppCommon.setOnPayFinishListener(new JsAppCommon.OnPayFinishListener() {
//            @Override
//            public void onPayFinish(boolean succ, Object data) {
//                onPayFinish(succ, data);
//            }
//        });
    }

    protected void onPayFin(boolean succ, Object data){};

    protected boolean handleUpload() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        endTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    protected void onEditTextChanged(CharSequence s, int start, int before, int count) {

    }

    protected void startTimer() {
        if (mTimer == null)
            mTimer = new Timer();
        if (mTimerTask == null)
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            saveDraft();
                        }
                    });
                }
            };
        mTimer.schedule(mTimerTask, mIntervalTime, mIntervalTime);
    }

    protected void endTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        if(mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    private String combineContent() {
        JSONObject contentObj = new JSONObject();
        try {
            Editable text = mEditText.getText();
            contentObj.put("text", text == null ? "" : text.toString());
            contentObj.put("imgs", Tools.list2JsonArray(mImgController.getImgsArray()).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contentObj.toString();
    }

    protected long saveDraft() {
        if (mModel == null)
            return -1;
        long rowId = -1;
        ArrayList<Map<String, String>> imgs = mImgController.getImgsArray();
        Editable editable = mEditText.getText();
        mModel.setmText(editable == null ? "" : editable.toString());
        mModel.setmTitle(mQATitle == null ? "" : mQATitle);
        mModel.setmVideos(mImgController.getVideosArray());
        mModel.setmImgs(mImgController.getImgsArray());
        mModel.setmDishCode(mDishCode == null ? "" : mDishCode);
        mModel.setmQACode(mQACode == null ? "" : mQACode);
        mModel.setmAnswerCode(mAnswerCode == null ? "" : mAnswerCode);
        mModel.setmType(mQAType);
        mModel.setmAnonymity(mAnonymity);
        mModel.setmAuthorCode(mAuthorCode);
        if (mModel.getmId() > 0) {
            mSQLite.updateData((int) mModel.getmId(), mModel);
            rowId = mModel.getmId();
        } else {
            rowId = mSQLite.insertData(mModel);
            if (rowId > 0)
                mModel.setmId(rowId);
        }
        return rowId;
    }

    private void handleBackClick() {
        if (!TextUtils.isEmpty(mEditText.getText()) || mImgsContainer.getChildCount() > 0) {
            if (saveDraft() != -1)
                Tools.showToast(this, "内容已保存");
        }
    }

    @Override
    public void onBackPressed() {
        handleBackClick();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (mImgController == null)
                return;
            switch (requestCode) {
                case REQUEST_SELECT_IMAGE:
                    List<String> imagePathArray = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);
                    if (imagePathArray != null && !imagePathArray.isEmpty()) {
                        for (String str : imagePathArray) {
                            if (!TextUtils.isEmpty(str)) {
                                Map<String, String> dataMap = new HashMap<String, String>();
                                dataMap.put("img", str);
                                mImgController.addData(dataMap);
                            }
                        }
                    }
                    break;
                case REQUEST_SELECT_VIDEO:
                    String videoPath = data.getStringExtra(MediaStore.Video.Media.DATA);
                    String coverPath = data.getStringExtra(RecorderVideoData.video_img_path);
                    if (!TextUtils.isEmpty(videoPath) && !TextUtils.isEmpty(coverPath)) {
                        Map<String, String> dataMap = new HashMap<String, String>();
                        dataMap.put("thumImg", coverPath);
                        dataMap.put("video", videoPath);
                        mImgController.addData(dataMap);
                    }
                    break;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
