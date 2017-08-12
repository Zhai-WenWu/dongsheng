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

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import amodule.answer.db.AskAnswerSQLite;
import amodule.answer.model.AskAnswerModel;
import amodule.answer.view.AskAnswerImgController;
import amodule.answer.view.AskAnswerImgItemView;
import amodule.article.activity.ArticleVideoSelectorActivity;
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

    public static final String TAG = "BaseEditActivity";

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
            mAnswerCode = bundle.getString("answerCode");
            String askMoreStr = bundle.getString("isAskMore");
            if ("2".equals(askMoreStr))
                mIsAskMore = true;
            String answerMoreStr = bundle.getString("mIsAnswerMore");
            if ("2".equals(answerMoreStr))
                mIsAnswerMore = true;
        }
        mModel = new AskAnswerModel();
        mSQLite = new AskAnswerSQLite(XHApplication.in().getApplicationContext());
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
        int rightPadding = 0;
        int delIconWH = 0;
        int playIconWH = -1;
        if (this instanceof AnswerEditActivity) {
            rightPadding = getResources().getDimensionPixelSize(R.dimen.dp_5);
            width = rightPadding + getResources().getDimensionPixelSize(R.dimen.dp_45);
            height = getResources().getDimensionPixelSize(R.dimen.dp_45);
            delIconWH = getResources().getDimensionPixelSize(R.dimen.dp_17);
            playIconWH = getResources().getDimensionPixelSize(R.dimen.dp_16);
        } else if (this instanceof AskEditActivity) {
            rightPadding = getResources().getDimensionPixelSize(R.dimen.dp_5);
            width = rightPadding + getResources().getDimensionPixelSize(R.dimen.dp_37);
            height = getResources().getDimensionPixelSize(R.dimen.dp_37);
            delIconWH = getResources().getDimensionPixelSize(R.dimen.dp_16);
            playIconWH = getResources().getDimensionPixelSize(R.dimen.dp_16);
        }
        mImgController = new AskAnswerImgController(this, mImgsContainer, width, height, rightPadding, delIconWH, playIconWH);
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
                            String tjId = getTjId();
                            if (!TextUtils.isEmpty(tjId)) {
                                intent.putExtra("tjId", tjId);
                                intent.putExtra("tag", TAG);
                            }
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
                            String tjId = getTjId();
                            if (!TextUtils.isEmpty(tjId)) {
                                intent1.putExtra("tjId", tjId);
                                intent1.putExtra("tag", TAG);
                            }
                            startActivityForResult(intent1, REQUEST_SELECT_IMAGE);
                        }
                        break;
                    case R.id.upload:
                        saveDraft();
                        if (handleUpload()) {//单独处理提问的上传
                            break;
                        }
                        //处理回答的上传
                        Intent intent = new Intent(BaseEditActivity.this, AskAnswerUploadListActivity.class);
                        intent.putExtra("draftId", (int)mModel.getmId());
                        intent.putExtra("isAutoUpload", true);
                        startActivity(intent);
                        XHClick.mapStat(BaseEditActivity.this, getTjId(), "点击发布按钮", "");
                        break;
                    case R.id.back:
                        XHClick.mapStat(BaseEditActivity.this, getTjId(), "点击返回按钮", "");
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
        mImgController.setOnDelListener(new AskAnswerImgController.OnDelListener() {
            @Override
            public void onDel(Map<String, String> dataMap) {
                XHClick.mapStat(BaseEditActivity.this, getTjId(), "删除图片", "");
            }
        });
        mJsAppCommon.setOnPayFinishListener(new JsAppCommon.OnPayFinishListener() {
            @Override
            public void onPayFinish(boolean succ, Object data) {
                onPayFin(succ, data);
            }
        });
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

    protected long saveDraft() {
        long rowId = -1;
        if (mModel == null) {
            return rowId;
        }
        Editable editable = mEditText.getText();
        ArrayList<Map<String, String>> videoArrs = mImgController.getVideosArray();
        ArrayList<Map<String, String>> imgArrs = mImgController.getImgsArray();
        long id = mModel.getmId();
        if ((editable == null || TextUtils.isEmpty(editable.toString())) && mImgsContainer != null && mImgsContainer.getChildCount() <= 0) {
            if (id > 0)
                mSQLite.deleteData((int) id);
            return rowId;
        }
        mModel.setmText(editable == null ? "" : editable.toString());
        mModel.setmTitle(mQATitle == null ? "" : mQATitle);
        mModel.setmVideos(videoArrs);
        mModel.setmImgs(imgArrs);
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
        if (saveDraft() != -1)
            Tools.showToast(this, "内容已保存");
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

    protected String getTjId() {
        String tjId = null;
        if (TextUtils.isEmpty(mQAType))
            return tjId;
        switch (mQAType) {
            case AskAnswerModel.TYPE_ANSWER:
            case AskAnswerModel.TYPE_ANSWER_AGAIN:
                tjId = "a_answer";
                break;
            case AskAnswerModel.TYPE_ASK:
                tjId = "a_ask_publish";
                break;
            case AskAnswerModel.TYPE_ASK_AGAIN:
                tjId = "a_ask_publish2";
                break;
        }
        return tjId;
    }
}
