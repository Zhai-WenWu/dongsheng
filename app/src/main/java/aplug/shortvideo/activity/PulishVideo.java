package aplug.shortvideo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.quan.activity.CircleHome;
import amodule.quan.activity.FriendQuan;
import amodule.quan.activity.upload.UploadChooseCircle;
import amodule.quan.db.SubjectData;
import amodule.quan.db.SubjectSqlite;
import amodule.quan.tool.UploadSubjectControl;
import amodule.quan.view.CircleLocationView;
import amodule.quan.view.ImgTextCombineLayout;
import amodule.upload.callback.UploadListNetCallBack;
import aplug.basic.BreakPointControl;
import aplug.basic.BreakPointUploadManager;

import static amodule.quan.activity.FriendQuan.REQUEST_CODE_QUAN_FRIEND;

/**
 * PackageName : aplug.shortvideo.activity
 * Created by MrTrying on 2016/9/22 16:55.
 * E_mail : ztanzeyu@gmail.com
 */
public class PulishVideo extends BaseActivity implements View.OnClickListener {
    public static final int REQUEST_SELECT_VIDEO = 2;
    public static final int REQUEST_SELECT_CIRCLE = 3;

    private EditText mTitleEdit;
    private ImageView mPreviewImage;
    private VideoView mVideoView;
    private TextView atUserCount;
    private CircleLocationView locationView;

    private SubjectData mData;

    String mChooseCid = "";
    String content = "";
    String imagePath = null;
    String videoPath = null;
    String videoUrl = null;
    private StringBuffer friends;//@用户

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.short_video_pulish);

        initView();

        initIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTitleEdit.requestFocus();
        mVideoView.resume();
        mVideoView.start();
    }

    private void initTitles() {
        if (Tools.isShowTitle()) {
            int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
            final int statusBarHeight = Tools.getStatusBarHeight(this);
            int height = dp_45 + statusBarHeight;

            RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.title_all_rela);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
            bar_title.setLayoutParams(layout);
            bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
            //初始化底部view被键盘顶起
            final RelativeLayout activityLayout = (RelativeLayout) findViewById(R.id.activityLayout);
            final RelativeLayout bottom_layout = (RelativeLayout) findViewById(R.id.bottom_layout);
            activityLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            int heightDiff = activityLayout.getRootView().getHeight() - activityLayout.getHeight();
                            Rect r = new Rect();
                            activityLayout.getWindowVisibleDisplayFrame(r);
                            int screenHeight = activityLayout.getRootView().getHeight();
                            int heightDifference = screenHeight - (r.bottom - r.top) - statusBarHeight;
                            if (heightDifference > 200) {
                                heightDifference = heightDifference - heightDiff;
                            } else {
                                heightDifference = 0;
                            }
                            bottom_layout.setPadding(0, 0, 0, heightDifference);
                        }
                    });
        }

        TextView title = (TextView) findViewById(R.id.title);
        title.setText("发视频");
        TextView nextStep = (TextView) findViewById(R.id.nextStep);
        int dp_15 = Tools.getDimen(this, R.dimen.dp_15);
        nextStep.setPadding(dp_15, 0, dp_15, 0);
        nextStep.setVisibility(View.VISIBLE);
        nextStep.setOnClickListener(this);
        findViewById(R.id.back_ll).setOnClickListener(this);
    }

    private void initView() {
        mTitleEdit = (EditText) findViewById(R.id.subject_title);
        mPreviewImage = (ImageView) findViewById(R.id.video_preview_image);
        mVideoView = (VideoView) findViewById(R.id.video_preview_videoview);
        atUserCount = (TextView) findViewById(R.id.at_user_count);
        locationView = (CircleLocationView) findViewById(R.id.location_view);

        initTitles();

        locationView.setOnClickListener(this);
        mPreviewImage.setOnClickListener(this);
        mVideoView.setOnClickListener(this);
        findViewById(R.id.at_user_image).setOnClickListener(this);
        findViewById(R.id.select_video).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.rl_background).setOnClickListener(this);
        mTitleEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String content = mTitleEdit.getText().toString();
                if(content.length()>=40){
//                    mTitleEdit.setText(content.substring(0,40));
                    Tools.showToast(PulishVideo.this,"标题不能超过40个字");
                }
            }
        });
    }

    private void initIntent() {
        mData = new SubjectData();
        mData.setVideoType("1");
        mData.setType(SubjectData.TYPE_UPLOAD);

        Intent intent = getIntent();
        String imagePath = intent.getStringExtra(SelectVideoActivity.EXTRAS_IMAGE_PATH);
        String videoPath = intent.getStringExtra(SelectVideoActivity.EXTRAS_VIDEO_PATH);

        final int id = getIntent().getIntExtra("id", -1);
        Log.d("PulishVideo", "SubjectData.id = " + id);
        if (id != -1) {
            mData = SubjectSqlite.getInstance(this).selectById(id);
            Log.d("PulishVideo", "getVideoSImg::" + mData.getVideoSImg()+"::");
            Log.d("PulishVideo", "getVideoLocalPath::" + mData.getVideoLocalPath()+"::");
//            if(!TextUtils.isEmpty(mData.getVideoSImg()))imagePath = mData.getVideoSImg();
//            if(!TextUtils.isEmpty(mData.getVideoLocalPath()))videoPath = mData.getVideoLocalPath();
            if (mData.getContentArray() != null && mData.getContentArray().size() > 0) {
                String[] atUsers = new String[]{};
                content = mData.getContentArray().get(0).get(ImgTextCombineLayout.CONTENT);
                if (!TextUtils.isEmpty(content)) {
                    atUsers = content.split(" ");
                    for (String userName : atUsers) {
                        if (!userName.startsWith("@")) {
                            content = "";

                            break;
                        }
                    }
                }

                if (!TextUtils.isEmpty(content)
                        && atUsers != null
                        && atUsers.length != 0) {
                    atUserCount.setText(String.valueOf(atUsers.length));
                    findViewById(R.id.at_user_count_layout).setVisibility(View.VISIBLE);
                }
            }

            mTitleEdit.setText(mData.getTitle());
        }

        setVideoInfo(imagePath, videoPath);
        //定位
        locationView.onLocationClick();
        String[] localIsShow = FileManager.getSharedPreference(this, FileManager.xmlKey_locationIsShow);
        if (localIsShow != null && localIsShow.length > 1) {
            if ("1".equals(localIsShow[1])) {
                locationView.onLocationClick();
            }
        }
    }

    /**
     * 判断本地文件是否存在
     *
     * @param path 文件路径
     * @return
     */
    private boolean fileIsExsit(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.exists();
    }

    /**
     * 设置@人的数据到贴子内容中
     *
     * @param data 数据
     */
    private void setAtUserData(List<Map<String, String>> data) {
        if (data == null) {
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (Map<String, String> map : data) {
            stringBuffer.append("@").append(map.get("")).append(" ");
        }
        friends=stringBuffer;
        Log.i("zhangyujian","value::::"+stringBuffer.toString());
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put(ImgTextCombineLayout.IMGEURL, "");
        contentMap.put(ImgTextCombineLayout.CONTENT, stringBuffer.toString());
        ArrayList<Map<String, String>> contentArray = new ArrayList<>();
        contentArray.add(contentMap);
        mData.setContentArray(contentArray);
    }

    /**
     * 设置视频数据
     *
     * @param imagePath 图片路径
     * @param videoPath 视频路径
     */
    private void setVideoInfo(String imagePath, String videoPath) {
        if ((fileIsExsit(imagePath) || (!TextUtils.isEmpty(imagePath) && imagePath.startsWith("http://")))
                && fileIsExsit(videoPath)) {
            this.videoPath = videoPath;
            this.imagePath = imagePath;
            mData.setVideoLocalPath(videoPath);
            mData.setVideoSImg(imagePath);
            mData.setVideoSImgLocal(imagePath);
            //上传
            if (!TextUtils.isEmpty(imagePath)) {
                if (!videoPath.startsWith("http://")) {
                    UploadSubjectControl.getInstance().uploadImg(mData.uploadTimeCode, UploadSubjectControl.IMAGE_TYPE_SUBJECT, imagePath);
                }
                //设置图片
                Glide.with(this)
                        .load(imagePath)
//                        .transform(new RoundTransformation(this, Tools.getDimen(this, R.dimen.dp_5)))
                        .into(mPreviewImage);
            }
            if (!TextUtils.isEmpty(videoPath)) {
                uploadVideo(videoPath);
                mVideoView.setVideoPath(videoPath);
                mVideoView.start();
                //静音
                mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setVolume(0f, 0f);
                    }
                });
                mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mVideoView.resume();
                        mVideoView.start();
                    }
                });
            }
        } else {
            this.videoPath = "";
            this.imagePath = "";
            mData.setVideo("");
            mData.setVideoLocalPath("");
            mData.setVideoSImg("");
            mData.setVideoSImgLocal("");
        }
    }

    /**
     * 上传视频
     *
     * @param videoPath 视频路径
     */
    private void uploadVideo(final String videoPath) {

        String md5 = Tools.getMD5(videoPath);
        BreakPointControl breakPointContorl
                = new BreakPointControl(XHApplication.in().getApplicationContext(),
                md5,videoPath, BreakPointUploadManager.TYPE_VIDEO);

        breakPointContorl.start(new UploadListNetCallBack(){
            @Override
            public void onProgress(double progress, String uniqueId) {
            }
            @Override
            public void onSuccess(String url, String uniqueId, JSONObject jsonObject) {

                if (!TextUtils.isEmpty(url)) {
                    videoUrl = url;
                    mData.setVideo(videoUrl);
                }
            }

            @Override
            public void onFaild(String faild, String uniqueId) {
                Tools.showToast(PulishVideo.this, "上传失败");
            }

            @Override
            public void onLastUploadOver(boolean flag, String responseStr) {

            }

            @Override
            public void onProgressSpeed(String uniqueId, long speed) {

            }
        });
    }

//    /**
//     * 上传视频
//     *
//     * @param videoPath 视频路径
//     */
//    private void uploadVideo(final String videoPath) {
//        ReqInternet.in().upLoadMP4(StringManager.api_uploadVideo, "videos", videoPath, new InternetCallback(XHApplication.in()) {
//            @Override
//            public void loaded(int flag, String url, Object msg) {
//                if (flag >= ReqInternet.REQ_OK_STRING) {
//                    if (msg != null && !TextUtils.isEmpty(msg.toString())) {
//                        videoUrl = msg.toString();
//                        mData.setVideo(videoUrl);
//                    }
//                }
//            }
//        });
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_SELECT_VIDEO:
                    String imagePath = data.getStringExtra(SelectVideoActivity.EXTRAS_IMAGE_PATH);
                    String videoPath = data.getStringExtra(SelectVideoActivity.EXTRAS_VIDEO_PATH);
                    setVideoInfo(imagePath, videoPath);
                    break;
                case REQUEST_SELECT_CIRCLE:
                    mChooseCid = data.getStringExtra("chooseCid");
                    mData.setCid(mChooseCid);
                    chooseNextStep(true);
                    break;
                case REQUEST_CODE_QUAN_FRIEND:
                    String rultStr = data.getStringExtra(FriendQuan.FRIENDS_LIST_RESULT);
                    if (!TextUtils.isEmpty(rultStr)) {
                        List<Map<String, String>> rultList = StringManager.getListMapByJson(rultStr);
                        if (rultList.size() != 0) {
                            XHClick.mapStat(PulishVideo.this,"a_post_shortvideo","@好友",String.valueOf(rultList.size()));
                            atUserCount.setText(String.valueOf(rultList.size()));
                            findViewById(R.id.at_user_count_layout).setVisibility(View.VISIBLE);
                            setAtUserData(rultList);
                        } else {
                            findViewById(R.id.at_user_count_layout).setVisibility(View.GONE);
                        }
                    }
                    break;
            }
        }
    }

    private void chooseNextStep(boolean isHasCid) {
        saveDB();

        String tipString = checkUploadData();
        if (TextUtils.isEmpty(tipString)) {
            if (isHasCid) {
                starUpload(mData);
                closePreActivity();
                finish();
            } else {
                startActivityForResult(new Intent(this, UploadChooseCircle.class), REQUEST_SELECT_CIRCLE);
            }
        } else {
            Tools.showToast(this, tipString);
        }
    }


    private void starUpload(SubjectData subjectData) {
        //发送中
        finish();
        if (TextUtils.isEmpty(subjectData.getCode())
                && (UploadSubjectControl.getInstance().getUploadCallback() == null || !subjectData.getCid().equals(getIntent().getStringExtra("cid")))) {
            Intent intent = new Intent(this, CircleHome.class);
            intent.putExtra("cid", subjectData.getCid());
            startActivity(intent);
        }
        UploadSubjectControl upSubCon = UploadSubjectControl.getInstance();
        if (subjectData != null) {
            upSubCon.startUpload(subjectData);
        }
    }

    private final int titleMin = 2;

    private String checkUploadData() {
        mData.setTitle(mTitleEdit.getText().toString());
        if (SubjectData.TYPE_UPLOAD.equals(mData.getType())) {
            String title = mData.getTitle();
            if (TextUtils.isEmpty(title)) {
                XHClick.onEventValue(this, "uploadQuanClick", "uploadQuanClick", "请输入标题", 0);
                return "请输入标题";
            } else if (title.length() < titleMin) {
                XHClick.onEventValue(this, "uploadQuanClick", "uploadQuanClick", "标题至少" + titleMin + "个字", title.length());
                return "标题至少" + titleMin + "个字";
            }
        }
        String image = mData.getVideoSImg();
        String video = mData.getVideo();
        String videoLocalPath = mData.getVideoLocalPath();
        if (TextUtils.isEmpty(image) || TextUtils.isEmpty(videoLocalPath)) {
            return "视频信息错误，请重新选择";
        }
        if (TextUtils.isEmpty(video)) {
            return "视频上传中";
        }
        return "";
    }

    public void saveDB() {
        mData.setLocation(locationView.getLocationJson());
        mData.setIsLocation(locationView.getIsLocation() ? "2" : "1");
        SubjectSqlite.getInstance(this).inser(mData);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            //选择圈子
            case R.id.nextStep:
                hideInput();
                XHClick.mapStat(PulishVideo.this,"a_post_shortvideo","下一步（选择圈子）","");
                chooseNextStep(false);
                break;
            //返回
            case R.id.back_ll:
            case R.id.back:
                XHClick.mapStat(PulishVideo.this,"a_post_shortvideo","返回","");
                onBackPressed();
                break;
            //跳转@
            case R.id.at_user_image:
                Intent atUser = new Intent(this, FriendQuan.class);
                atUser.putExtra("Activity", "video");
                if(friends!=null&&!TextUtils.isEmpty(friends.toString()))
                    atUser.putExtra("value",friends.toString());
                startActivityForResult(atUser,REQUEST_CODE_QUAN_FRIEND);
                break;
            //跳转选择视频
            case R.id.select_video:
                XHClick.mapStat(PulishVideo.this,"a_post_shortvideo","视频更换","");
                Intent selectVideo = new Intent(this, SelectVideoActivity.class);
                selectVideo.putExtra(SelectVideoActivity.EXTRAS_CAN_EDIT, false);
                startActivityForResult(selectVideo, REQUEST_SELECT_VIDEO);
                break;
            //定位
            case R.id.location_view:
                locationView.onLocationClick();
                FileManager.setSharedPreference(this, FileManager.xmlKey_locationIsShow, locationView.getIsLocation() ? "2" : "1");
                break;
            case R.id.video_preview_image:
            case R.id.video_preview_videoview:
                hideInput();
                XHClick.mapStat(PulishVideo.this,"a_post_shortvideo","点击放大预览","");
                Intent startVideo = new Intent(this, VideoFullScreenActivity.class);
                startVideo.putExtra(VideoFullScreenActivity.EXTRA_VIDEO_URL, videoPath);
                startVideo.putExtra(VideoFullScreenActivity.EXTRA_VIDEO_TYPE, VideoFullScreenActivity.LOCAL_VIDEO);
                startActivity(startVideo);
                break;
            case R.id.rl_background:
                ToolsDevice.keyboardControl(true, this, mTitleEdit);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        saveDB();
        super.onBackPressed();
    }


    private void closePreActivity() {
        if (MediaRecorderActivity.rediaRecWeakRef != null) {
            Activity activity = MediaRecorderActivity.rediaRecWeakRef.get();
            MediaRecorderActivity.rediaRecWeakRef = null;
            if (activity != null) {
                activity.finish();
            }
        }
    }
    private void hideInput(){
        if(mTitleEdit!=null) {
            InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(mTitleEdit.getWindowToken(), 0);
        }
    }
}