package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.listener.StandardVideoAllCallBack;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.logic.AppCommon;
import acore.logic.FavoriteHelper;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.KeyboardDialog;
import acore.widget.multifunction.IconTextSpan;
import amodule.article.activity.edit.ArticleEidtActivity;
import amodule.article.view.BottomDialog;
import amodule.comment.CommentDialog;
import amodule.dish.activity.ShortVideoDetailActivity;
import amodule.dish.activity.ShortVideoDetailFragment;
import amodule.main.Main;
import amodule.main.view.item.BaseItemView;
import amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver;
import amodule.user.activity.FriendHome;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.basic.SubBitmapTarget;
import aplug.player.ShortVideoPlayer;
import third.share.activity.ShareActivityDialog;
import xh.basic.tool.UtilImage;

/**
 * 短视频itemView
 */
public class ShortVideoItemView extends BaseItemView implements View.OnClickListener, SeekBar.OnSeekBarChangeListener{
    private Context context;
    private KeyboardDialog mKeyboardDialog;
    private ImageView mThumbImg;
    private RelativeLayout mVideoLayout;
    private ConstraintLayout mTitleLayout;
    private ImageView mBackImg;
    private ImageView mHeaderImg;
    private TextView mUserName;
    private TextView mAttentionText;
    private ImageView mLikeImg;
    private ImageView mMoreImg;
    private View mEmptyView;
    private ConstraintLayout mScrollBarTopLayout;
    private ImageView mPlayPauseImg;
    private ConstraintLayout mInfoLayout;
    private TextView mTopicText;
    private TextView mTitleText;
    private ConstraintLayout mBottomLayout;
    private View mBottomCommentLayout;
    private View mBottomShareLayout;
    private View mBottomGoodLayout;
    private ImageView mCommentImg;
    private TextView mCommentNumText;
    private ImageView mShareImg;
    private TextView mShareNum;
    private ImageView mGoodImg;
    private TextView mGoodText;
    private ProgressBar mBottomProgress;
    private TextView mCommentHint;

    private Map<String, String> mData;
    private Map<String, String> mTopicMap;
    private Map<String, String> mUserMap;
    private Map<String, String> mVideoMap;
    private Map<String, String> mFavMap;
    private ArrayList<Map<String, String>> mNumInfoMaps;
    private boolean mIsSelf;

    private AtomicBoolean mGoodLoaded;
    private AtomicBoolean mAttentionLoading;
    private AtomicBoolean mFavLoading;
    private AtomicBoolean mDelLoading;
    private boolean mShowFlag;
    private boolean mRepeatEnable;
    private Runnable mDownTimeRun;
    private Handler mMainHandler;
    private String mVideoUrl;
    private String mTopicClickUrl;

    private int mCommentsNum;
    private int mPos;
    private ShortVideoPlayer mPlayerView;
    private CommentDialog mCommentDialog;

    private ShortVideoDetailFragment.OnPlayPauseClickListener mOnPlayPauseListener;
    private ShortVideoDetailFragment.OnSeekBarTrackingTouchListener mOnSeekBarTrackingTouchListener;
    private int position;

    public ShortVideoItemView(Context context) {
        this(context,null);
    }

    public ShortVideoItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context= context;
        LayoutInflater.from(context).inflate(R.layout.item_short_video_view,this,true);
        initView();
    }
    public void initView(){
        mThumbImg = (ImageView)findViewById(R.id.image_thumb);
        mVideoLayout = (RelativeLayout)findViewById(R.id.surface_container);
        mTitleLayout = (ConstraintLayout) findViewById(R.id.layout_title);
        mBackImg = (ImageView) findViewById(R.id.image_back);
        mHeaderImg = (ImageView) findViewById(R.id.image_user_header);
        mUserName = (TextView) findViewById(R.id.text_user_name);
        mAttentionText = (TextView) findViewById(R.id.text_attention);
        mLikeImg = (ImageView) findViewById(R.id.image_like);
        mMoreImg = (ImageView) findViewById(R.id.image_more);
        mEmptyView = findViewById(R.id.view_empty);
        mScrollBarTopLayout = (ConstraintLayout) findViewById(R.id.layout_scrollbar_top);
        mPlayPauseImg = (ImageView) findViewById(R.id.image_play_pause);
        mInfoLayout = (ConstraintLayout) findViewById(R.id.layout_info);
        mTopicText = (TextView) findViewById(R.id.text_topic);
        mTitleText = (TextView) findViewById(R.id.text_title);
        mBottomLayout = (ConstraintLayout) findViewById(R.id.layout_bottom_info);
        mBottomProgress = (ProgressBar) findViewById(R.id.bottom_progressbar);
        mBottomCommentLayout = findViewById(R.id.layout_bottom_comment);
        mCommentImg = (ImageView) mBottomCommentLayout.findViewById(R.id.image);
        mCommentNumText = (TextView) mBottomCommentLayout.findViewById(R.id.text);
        mCommentHint = (TextView) findViewById(R.id.comment_hint);
        mBottomShareLayout = findViewById(R.id.layout_bottom_share);
        mShareImg = (ImageView) mBottomShareLayout.findViewById(R.id.image);
        mShareNum = (TextView) mBottomShareLayout.findViewById(R.id.text);
        mBottomGoodLayout = findViewById(R.id.layout_bottom_good);
        mGoodImg = (ImageView) mBottomGoodLayout.findViewById(R.id.image);
        mGoodText = (TextView) mBottomGoodLayout.findViewById(R.id.text);

        mScrollBarTopLayout.setVisibility(View.GONE);
        mPlayPauseImg.setSelected(true);
        mPlayerView= (ShortVideoPlayer) findViewById(R.id.short_video);

        mPlayerView.setShowFullAnimation(false);
        mPlayerView.setIsTouchWiget(false);
        initData();

        addListener();
    }

    private void initData() {
        if (mData != null) {
            String commentNum = mData.get("commentNum");
            mCommentsNum = TextUtils.isEmpty(commentNum) ? 0 : Integer.parseInt(commentNum);
        }
        mGoodLoaded = new AtomicBoolean(false);
        mAttentionLoading = new AtomicBoolean(false);
        mFavLoading = new AtomicBoolean(false);
        mDelLoading = new AtomicBoolean(false);
        mShowFlag = true;
        mRepeatEnable = true;
        mDownTimeRun = new Runnable() {
            @Override
            public void run() {
                changeBarsVisibility();
            }
        };
        mMainHandler = new Handler(getContext().getMainLooper());
    }

    private void addListener() {
        mBackImg.setOnClickListener(this);
        mHeaderImg.setOnClickListener(this);
        mUserName.setOnClickListener(this);
        mAttentionText.setOnClickListener(this);
        mLikeImg.setOnClickListener(this);
        mMoreImg.setOnClickListener(this);
        mEmptyView.setOnClickListener(this);
        mPlayPauseImg.setOnClickListener(this);
        mBottomCommentLayout.setOnClickListener(this);
        mBottomGoodLayout.setOnClickListener(this);
        mBottomShareLayout.setOnClickListener(this);
        mCommentHint.setOnClickListener(this);
        mTopicText.setOnClickListener(this);

        mPlayerView.setStandardVideoAllCallBack(new StandardVideoAllCallBack() {
            @Override
            public void onClickStartThumb(String url, Object... objects) {

            }

            @Override
            public void onClickBlank(String url, Object... objects) {

            }

            @Override
            public void onClickBlankFullscreen(String url, Object... objects) {

            }

            @Override
            public void onPrepared(String url, Object... objects) {
                changePlayPauseUI(true);
                changeThumbImageState(false);
            }

            @Override
            public void onClickStartIcon(String url, Object... objects) {

            }

            @Override
            public void onClickStartError(String url, Object... objects) {

            }

            @Override
            public void onClickStop(String url, Object... objects) {
                changePlayPauseUI(false);
            }

            @Override
            public void onClickStopFullscreen(String url, Object... objects) {

            }

            @Override
            public void onClickResume(String url, Object... objects) {
                changePlayPauseUI(true);
            }

            @Override
            public void onClickResumeFullscreen(String url, Object... objects) {

            }

            @Override
            public void onClickSeekbar(String url, Object... objects) {
                XHClick.mapStat(getContext(), ShortVideoDetailActivity.STATISTIC_ID, "视频", "进度条");

            }

            @Override
            public void onClickSeekbarFullscreen(String url, Object... objects) {

            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                changeThumbImageState(true);
                // TODO: 2018/5/29 是否要循环播放？
//                if (mRepeatEnable) {
//                    changePlayPauseUI(true);
//                    prepareAsync();
//                }
            }

            @Override
            public void onEnterFullscreen(String url, Object... objects) {

            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {

            }

            @Override
            public void onQuitSmallWidget(String url, Object... objects) {

            }

            @Override
            public void onEnterSmallWidget(String url, Object... objects) {

            }

            @Override
            public void onTouchScreenSeekVolume(String url, Object... objects) {

            }

            @Override
            public void onTouchScreenSeekPosition(String url, Object... objects) {

            }

            @Override
            public void onTouchScreenSeekLight(String url, Object... objects) {

            }

            @Override
            public void onPlayError(String url, Object... objects) {
                changePlayPauseUI(false);
                changeThumbImageState(true);
            }
        });
    }


    /**
     * 开始播放入口
     */
    public void prepareAsync() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mPlayerView.startPlayLogic();
            }
        });
    }
    public void resumeVideoView(){
        mPlayerView.onVideoResume();
    }
    /**
     * 暂停
     */
    public void pauseVideoView(){
        mPlayerView.onVideoPause();
    }

    /**
     * 重置数据
     */
    public void releaseVideoView(){
        mPlayerView.release();
        mPlayerView.releaseAllVideos();
    }
    public boolean isPlaying(){
        return  mPlayerView.getCurrentState()==GSYVideoPlayer.CURRENT_STATE_PLAYING;
    }
    /**
     * 设置数据
     * @param map
     */
    public void setData(Map<String, String> map,int position) {
        mData = map;
        if (mData == null || mData.isEmpty())
            return;
        this.position = position;
        mUserMap = StringManager.getFirstMap(mData.get("customer"));
        mUserName.setText(mUserMap.get("nickName"));
        mIsSelf = TextUtils.equals(LoginManager.userInfo.get("code"), mUserMap.get("code"));
        if (mIsSelf) {
            mAttentionText.setVisibility(View.GONE);
            mLikeImg.setVisibility(View.GONE);
            mMoreImg.setVisibility(View.VISIBLE);
        } else {
            String isFollow = mUserMap.get("isFollow");
            String followText;
            String followTag;
            switch (isFollow) {
                case "2":
                    followText = getString(R.string.attentioned);
                    followTag = "2";
                    break;
                default:
                    followText = getString(R.string.attention);
                    followTag = "1";
                    break;
            }
            mAttentionText.setText(followText);
            mAttentionText.setVisibility(View.VISIBLE);
            mAttentionText.setTag(TAG_ID, followTag);
            mMoreImg.setVisibility(View.GONE);
            mFavMap = StringManager.getFirstMap(mData.get("favorites"));
            mLikeImg.setSelected(TextUtils.equals("2", mFavMap.get("status")));
            mLikeImg.setVisibility(View.VISIBLE);
        }
        mVideoMap = StringManager.getFirstMap(mData.get("video"));
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mThumbImg.getLayoutParams();
        DisplayMetrics dm = ToolsDevice.getWindowPx(getContext());
        int screenW = dm.widthPixels;
        int screenH = dm.heightPixels;
        int vW = Integer.parseInt(mVideoMap.get("width"));
        int vH = Integer.parseInt(mVideoMap.get("height"));
        // TODO: 2018/5/30 处理视频封面图大小的问题？
        int tempH = vW * screenH / screenW;
        lp.width = vW;
        lp.height = vH;
        mThumbImg.setLayoutParams(lp);
        mVideoUrl = StringManager.getFirstMap(mVideoMap.get("videoUrl")).get("defaultUrl");
        mNumInfoMaps = StringManager.getListMapByJson(mData.get("numInfo"));
        mCommentImg.setImageResource(R.drawable.short_video_detail_comment);
        mCommentNumText.setText(mData.get("commentNum"));
        mGoodImg.setImageResource(R.drawable.bg_select_good);
        mGoodImg.setSelected(TextUtils.equals("2", mData.get("isLike")));
        mGoodText.setText(mNumInfoMaps.get(0).get(""));
        mShareImg.setImageResource(R.drawable.short_video_detail_share);
        mShareNum.setText(mNumInfoMaps.get(1).get(""));
        mTitleText.setText("");
        String title = mData.get("title");
        if (!TextUtils.isEmpty(title)) {
            mTitleText.setVisibility(View.VISIBLE);
            if (TextUtils.equals(mData.get("isEssence"), "2")) {
                IconTextSpan.Builder ib = new IconTextSpan.Builder(context);
                ib.setBgColorInt(getResources().getColor(R.color.icon_text_bg));
                ib.setTextColorInt(getResources().getColor(R.color.c_white_text));
                ib.setText("精选");
                ib.setRadius(2f);
                ib.setRightMargin(3);
                ib.setBgHeight(14f);
                ib.setTextSize(10f);
                StringBuffer sb = new StringBuffer(" ");
                sb.append(title).append(title);
                SpannableStringBuilder ssb = new SpannableStringBuilder(sb.toString());
                ssb.setSpan(ib.build(), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mTitleText.setText(ssb);
            } else {
                mTitleText.setText(title);
            }
        } else {
            mTitleText.setVisibility(View.INVISIBLE);
        }
        mTopicMap = StringManager.getFirstMap(mData.get("topic"));
        mTopicClickUrl = mTopicMap.get("url");
        String topicTitle = mTopicMap.get("title");
        if (!TextUtils.isEmpty(topicTitle)) {
            mTopicText.setVisibility(View.VISIBLE);
            mTopicText.setBackgroundColor(Color.parseColor(mTopicMap.get("bgColor")));
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append("# ").append(topicTitle);
            ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#F6DC2A")), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new ForegroundColorSpan(Color.parseColor(mTopicMap.get("color"))), 1, topicTitle.length() + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTopicText.setText(ssb);
        } else {
            mTopicText.setVisibility(View.INVISIBLE);
        }
        loadUserHeader(mUserMap.get("img"));
        loadVideoImg(mVideoMap.get("videoImg"));
        mPlayerView.setUp(mVideoUrl, false, "");
    }

    public void setPos(int pos) {
        mPos = pos;
    }

    public int getPos() {
        return mPos;
    }

    public String getVideoUrl() {
        return mVideoUrl;
    }

    private void doFavorite() {
        if (!ToolsDevice.isNetworkAvailable(getContext())) {
            Toast.makeText(getContext(), "网络异常，请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkLoginAndHandle()) {
            return;
        }
        if (mFavLoading.get())
            return;
        mFavLoading.set(true);
        FavoriteHelper.instance().setFavoriteStatus(context.getApplicationContext(), mData.get("code"), mData.get("title"),
                FavoriteHelper.TYPE_DISH_VIDEO, new FavoriteHelper.FavoriteStatusCallback() {
                    @Override
                    public void onSuccess(boolean isFav) {
                        mFavLoading.set(false);
                        mLikeImg.setSelected(isFav);
                    }

                    @Override
                    public void onFailed() {
                        mFavLoading.set(false);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_topic:
                if (!TextUtils.isEmpty(mTopicClickUrl)) {
                    AppCommon.openUrl(mTopicClickUrl, true);
                }
                break;
            case R.id.image_back:
                closeActivity();
                XHClick.mapStat(getContext(), ShortVideoDetailActivity.STATISTIC_ID, "顶部栏", "返回");
                break;
            case R.id.image_user_header:
            case R.id.text_user_name:
                gotoUser();
                XHClick.mapStat(getContext(), ShortVideoDetailActivity.STATISTIC_ID, "顶部栏", "头像和昵称");
                break;
            case R.id.text_attention:
                attention();
                break;
            case R.id.image_like:
                doFavorite();
                XHClick.mapStat(getContext(), ShortVideoDetailActivity.STATISTIC_ID, "顶部栏", "收藏");
                break;
            case R.id.image_more:
                showBottomDialog();
                break;
            case R.id.view_empty:
                doEmptyOption();
                break;
            case R.id.image_play_pause:
                handlePlay();
                break;
            case R.id.layout_bottom_share:
                doShare();
                XHClick.mapStat(getContext(), ShortVideoDetailActivity.STATISTIC_ID, "底部栏", "分享按钮");
                break;
            case R.id.layout_bottom_good:
                doGood();
                XHClick.mapStat(getContext(), ShortVideoDetailActivity.STATISTIC_ID, "底部栏", "点赞按钮");
                break;
            case R.id.layout_bottom_comment:
                showComments();
                XHClick.mapStat(getContext(), ShortVideoDetailActivity.STATISTIC_ID, "底部栏", "评论按钮");
                break;
            case R.id.comment_hint:
                showCommentEdit();
                XHClick.mapStat(getContext(), ShortVideoDetailActivity.STATISTIC_ID, "底部栏", "评论输入框");
                break;
        }
    }

    private void showCommentEdit() {
        if (mKeyboardDialog == null) {
            mKeyboardDialog = new KeyboardDialog(getContext());
            mKeyboardDialog.setOnSendClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mKeyboardDialog.cancel();
                    String text = mKeyboardDialog.getText();
                    if (TextUtils.isEmpty(text))
                        return;
                    sendComment(text);
                }
            });
        }
        if (mKeyboardDialog.isShowing())
            return;
        mKeyboardDialog.show();
    }

    private void showComments() {
        if (mData == null)
            return;
        Map<String, String> commentMap = new HashMap<>();
        commentMap.put("from", "2");
        commentMap.put("type", "2");
        commentMap.put("code", mData.get("code"));
        commentMap.put("commentNum", String.valueOf(mCommentsNum));
        if (mCommentDialog == null) {
            mCommentDialog = new CommentDialog(XHActivityManager.getInstance().getCurrentActivity(), commentMap);
            mCommentDialog.setCommentOptionSuccCallback(new CommentDialog.CommentOptionSuccCallback() {
                @Override
                public void onSendSucc() {
                    mCommentsNum ++;
                }

                @Override
                public void onDelSucc() {
                    mCommentsNum = Math.max(-- mCommentsNum, 0);
                }
            });
            mCommentDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mCommentDialog = null;
                }
            });
        }
        if (mCommentDialog.isShowing())
            return;
        mCommentDialog.show();
    }

    private void closeActivity() {
        //TODO
        if(context instanceof Activity) {
            ((Activity)context).finish();
        }
    }

    private void doGood() {
        if (checkLoginAndHandle()) {
            return;
        }
        if (mGoodLoaded.get())
            return;
        mGoodLoaded.set(true);
        String params = "code=" + mData.get("code") + "&type=likeList";
        ReqEncyptInternet.in().doEncypt(StringManager.api_quanSetSubject, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    try {
                        mGoodImg.setSelected(!mGoodImg.isSelected());
//                        int zanNum = Integer.parseInt(mNumInfoMaps.get(0).get(""));
//                        mGoodText.setText(String.valueOf(++zanNum));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mGoodLoaded.set(false);
                }
            }
        });
    }

    private void doShare() {
        if (checkLoginAndHandle())
            return;
        Intent intent = new Intent(context, ShareActivityDialog.class);
        intent.putExtra("tongjiId", ShortVideoDetailActivity.STATISTIC_ID);
        intent.putExtra("shareTwoContent", "分享框");
        intent.putExtra("isHasReport", !mIsSelf);
        intent.putExtra("nickName", mUserMap.get("nickName"));
        intent.putExtra("code", mUserMap.get("code"));
        intent.putExtra("shareFrom", "菜谱详情页");
        intent.putExtra("reportUrl", "Feedback.app?feekUrl=https://www.xiangha.com/caipu/" + mUserMap.get("code") + ".html");
        intent.putExtra("imgUrl", mData.get("shareImg"));
        intent.putExtra("title", mData.get("title"));
        intent.putExtra("content", mData.get("content"));
        String shareStr = mData.get("share");
        Map<String, String> shareMap = StringManager.getFirstMap(shareStr);
        Map<String, String> shareConf = StringManager.getFirstMap(shareMap.get("shareConfig"));
        Map<String, String> shareConf1 = StringManager.getFirstMap(shareConf.get("1"));
        String clickUrl = shareConf1.get("url");
        intent.putExtra("clickUrl", clickUrl);
        if (!TextUtils.isEmpty(shareStr)) {
            intent.putExtra("shareParams", shareStr);
        }
        context.startActivity(intent);
    }

    private void handlePlay() {

        switch (mPlayerView.getCurrentState()) {
            case GSYVideoPlayer.CURRENT_STATE_PLAYING:
                mPlayerView.onVideoPause();
                changePlayPauseUI(false);
                break;
            case GSYVideoPlayer.CURRENT_STATE_ERROR:
                Toast.makeText(getContext(), "视频播放错误", Toast.LENGTH_SHORT).show();
                break;
            case GSYVideoPlayer.CURRENT_STATE_AUTO_COMPLETE:
                prepareAsync();
                changePlayPauseUI(true);
                break;
            case GSYVideoPlayer.CURRENT_STATE_PAUSE:
                mPlayerView.onVideoResume();
                changePlayPauseUI(true);
                break;
            case GSYVideoPlayer.CURRENT_STATE_PLAYING_BUFFERING_START:
                Toast.makeText(getContext(), "正在缓冲", Toast.LENGTH_SHORT).show();
                break;
            case GSYVideoPlayer.CURRENT_STATE_PREPAREING:
                Toast.makeText(getContext(), "正在加载中", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void changePlayPauseUI(boolean isPlaying) {
        mPlayPauseImg.setSelected(isPlaying);
        XHClick.mapStat(getContext(), ShortVideoDetailActivity.STATISTIC_ID, "视频", isPlaying ? "播放" : "暂停");
    }

    private void changeThumbImageState(boolean visible) {
        if (mThumbImg != null) {
            mThumbImg.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void doEmptyOption() {
        changeViewVisibility();
    }

    private void changeViewVisibility() {
        if (mShowFlag) {
            if (mScrollBarTopLayout.getVisibility() == View.VISIBLE) {
                mScrollBarTopLayout.setVisibility(View.GONE);
                changeOthersVisibility();
                endRun();
            } else {
                changeBarsVisibility();
                startRun();
            }
        } else {
            mScrollBarTopLayout.setVisibility(View.VISIBLE);
            changeOthersVisibility();
            startRun();
        }
    }

    private void changeBarsVisibility() {
        switch (mScrollBarTopLayout.getVisibility()) {
            case View.VISIBLE:
                mScrollBarTopLayout.setVisibility(View.GONE);
                mBottomProgress.setVisibility(View.VISIBLE);
                break;
            default:
                mScrollBarTopLayout.setVisibility(View.VISIBLE);
                mBottomProgress.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void changeOthersVisibility() {
        if (mShowFlag) {
            mShowFlag = false;
            mTitleLayout.setVisibility(View.GONE);
            mInfoLayout.setVisibility(View.GONE);
            mBottomLayout.setVisibility(View.GONE);
        } else {
            mShowFlag = true;
            mTitleLayout.setVisibility(View.VISIBLE);
            mInfoLayout.setVisibility(View.VISIBLE);
            mBottomLayout.setVisibility(View.VISIBLE);
        }
    }

    private void startRun() {
        endRun();
        mMainHandler.postDelayed(mDownTimeRun, 3 * 1000);
    }

    private void endRun() {
        mMainHandler.removeCallbacks(mDownTimeRun);
    }

    private void showBottomDialog() {
        BottomDialog dialog = new BottomDialog(getContext());
        dialog.addButton("编辑", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(getContext(), ArticleEidtActivity.class);
                intent.putExtra("code", mData.get("code"));
                context.startActivity(intent);
                XHClick.mapStat(getContext(), ShortVideoDetailActivity.STATISTIC_ID, "更多（自己发布的视频）", "编辑");
            }
        });
        dialog.addButton("删除", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                openDeleteDialog();
                XHClick.mapStat(getContext(), ShortVideoDetailActivity.STATISTIC_ID, "更多（自己发布的视频）", "删除");
            }
        });
        dialog.show();
    }

    private void openDeleteDialog() {
        final DialogManager dialogManager = new DialogManager(getContext());
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleMessageView(getContext()).setText("确定删除这个视频吗？"))
                .setView(new HButtonView(getContext()).setNegativeTextColor(Color.parseColor("#333333"))
                        .setNegativeText("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogManager.cancel();
                            }
                        })
                        .setPositiveTextColor(Color.parseColor("#333333"))
                        .setPositiveText("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogManager.cancel();
                                delete(mData.get("code"));
                            }
                        }))).show();
    }

    private void delete(String code) {
        if (mDelLoading.get())
            return;
        mDelLoading.set(true);
        ReqEncyptInternet.in().doEncypt(StringManager.api_videoDel, "code=" + code,
                new InternetCallback() {
                    @Override
                    public void loaded(int flag, String url, Object obj) {
                        mDelLoading.set(false);
                        if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                            if (FriendHome.isAlive) {
                                Intent broadIntent = new Intent();
                                broadIntent.setAction(UploadStateChangeBroadcasterReceiver.ACTION);
                                broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.DATA_TYPE, "1");
                                broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.ACTION_DEL, "2");
                                Main.allMain.sendBroadcast(broadIntent);
                            }
                        }
                    }
                });
    }

    private void attention() {
        if (checkLoginAndHandle()) {
            return;
        }
        if (mAttentionLoading.get())
            return;
        mAttentionLoading.set(true);
        AppCommon.onAttentionClick(mUserMap.get("code"), "follow", new Runnable() {
            @Override
            public void run() {
                mAttentionLoading.set(false);
                String isFollow = (String) mAttentionText.getTag(TAG_ID);
                if (TextUtils.isEmpty(isFollow))
                    return;
                String followText;
                String followTag;
                switch (isFollow) {
                    case "2":
                        followText = getString(R.string.attention);
                        followTag = "1";
                        break;
                    default:
                        followText = getString(R.string.attentioned);
                        followTag = "2";
                        break;
                }
                mAttentionText.setText(followText);
                mAttentionText.setTag(TAG_ID, followTag);

            }
        });
    }

    public void gotoUser() {
        Intent intent = new Intent(context, FriendHome.class);
        intent.putExtra("code", mUserMap.get("code"));
        intent.putExtra("index", 2);
        context.startActivity(intent);
    }


    private void loadUserHeader(String url) {
        mHeaderImg.setTag(TAG_ID, url);
        BitmapRequestBuilder<GlideUrl, Bitmap> requestBuilder = LoadImage.with(context).load(url)
                .setImageRound(getResources().getDimensionPixelSize(R.dimen.dp_30))
                .setPlaceholderId(R.drawable.bg_round_user_icon)
                .setErrorId(R.drawable.bg_round_user_icon)
                .setSaveType(FileManager.save_cache)
                .build();
        if (requestBuilder != null)
            requestBuilder.into(new SubBitmapTarget() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!mHeaderImg.getTag(TAG_ID).equals(url))
                                return;
                            mHeaderImg.setImageBitmap(UtilImage.toRoundCorner(getResources(), bitmap, 1, 500));
                        }
                    });
                }
            });
    }

    private void loadVideoImg(String url) {
        mThumbImg.setTag(TAG_ID, url);
        BitmapRequestBuilder<GlideUrl, Bitmap> requestBuilder = LoadImage.with(mThumbImg.getContext()).load(url)
                .setSaveType(FileManager.save_cache)
                .build();
        if (requestBuilder != null)
            requestBuilder.into(new SubBitmapTarget() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                    if (bitmap != null && !mThumbImg.getTag(TAG_ID).equals(url))
                        return;
                    UtilImage.setImgViewByWH(mThumbImg, bitmap, 0, 0, false);
                }
            });
    }

    private boolean checkLoginAndHandle() {
        if (LoginManager.isLogin())
            return false;
        Intent intent = new Intent(context, LoginByAccout.class);
        context.startActivity(intent);
        return true;
    }

    public void setOnPlayPauseListener(ShortVideoDetailFragment.OnPlayPauseClickListener onPlayPauseListener) {
        mOnPlayPauseListener = onPlayPauseListener;
    }

    public void setOnSeekBarTrackingTouchListener(ShortVideoDetailFragment.OnSeekBarTrackingTouchListener onSeekBarTrackingTouchListener) {
        mOnSeekBarTrackingTouchListener = onSeekBarTrackingTouchListener;
    }

    /**
     * 发评论
     */
    private void sendComment(String content) {
        if (!LoginManager.isLogin()) {
            getContext().startActivity(new Intent(getContext(), LoginByAccout.class));
            return;
        }
        if (!StringManager.isHasChar(content)) {
            return;
        }

        if (content.length() > 2000) {
            Tools.showToast(getContext(), "发送内容不能超过2000字");
            return;
        }

        ArrayList<Map<String, String>> contentArray = new ArrayList<>();
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("text", content);
        contentMap.put("imgs", "[]");
        contentArray.add(contentMap);
        String contentParams = Tools.list2JsonArray(contentArray).toString();

        StringBuilder sbuild = new StringBuilder();
        sbuild.append("type=").append("2").append("&")
                .append("code=").append(mData.get("code")).append("&")
                .append("content=").append(Uri.encode(contentParams));
        ReqEncyptInternet.in().doEncypt(StringManager.api_addForum, sbuild.toString(),
                new InternetCallback() {
                    @Override
                    public void loaded(int flag, String url, Object obj) {
                        if (flag >= ReqEncyptInternet.REQ_OK_STRING) {
                            mCommentsNum ++;
                        } else {
                            Tools.showToast(getContext(), "评论失败，请重试");
                        }
                    }
                });
    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        endRun();
        if (mOnSeekBarTrackingTouchListener != null)
            mOnSeekBarTrackingTouchListener.onStartTrackingTouch(seekBar.getProgress());
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        startRun();
        if (mOnSeekBarTrackingTouchListener != null)
            mOnSeekBarTrackingTouchListener.onStopTrackingTouch(seekBar.getProgress());
    }

    public interface OnPlayPauseClickListener {
        void onClick(boolean isPlay);
    }

    public interface OnSeekBarTrackingTouchListener {
        void onStartTrackingTouch(int position);

        void onStopTrackingTouch(int position);
    }

    public final String getString(@StringRes int resId) {
        return getResources().getString(resId);
    }
}
