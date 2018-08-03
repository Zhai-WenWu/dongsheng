package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private LinearLayout mLayoutTopic;
    private LinearLayout mLayoutAddress;
    private TextView mTopicText;
    private TextView mAddressText;
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

    private Map<String, String> mData;//全部
    private Map<String, String> mTopicMap;//话题
    private Map<String, String> mAddressMap;//地址
    private Map<String, String> mUserMap;//用户
    private Map<String, String> mVideoMap;//视频
    private boolean mIsSelf;

    private AtomicBoolean mGoodLoaded;
    private AtomicBoolean mAttentionLoading;
    private AtomicBoolean mFavLoading;
    private AtomicBoolean mDelLoading;
    private boolean mRepeatEnable;
    private String mVideoUrl;
    private String mTopicClickUrl;
    private String mAddressClickUrl;

    private int mCommentsNum;
    private int mPos;
    private ShortVideoPlayer mPlayerView;
    private CommentDialog mCommentDialog;

    private ShortVideoDetailFragment.OnPlayPauseClickListener mOnPlayPauseListener;
    private ShortVideoDetailFragment.OnSeekBarTrackingTouchListener mOnSeekBarTrackingTouchListener;
    private int position;

    private Handler mMainHandler;

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
        mThumbImg = findViewById(R.id.image_thumb);
        mVideoLayout = findViewById(R.id.surface_container);
        mTitleLayout = findViewById(R.id.layout_title);
        mBackImg = findViewById(R.id.image_back);
        mHeaderImg = findViewById(R.id.image_user_header);
        mUserName = findViewById(R.id.text_user_name);
        mAttentionText = findViewById(R.id.text_attention);
        mLikeImg = findViewById(R.id.image_like);
        mMoreImg = findViewById(R.id.image_more);
        mEmptyView = findViewById(R.id.view_empty);
        mScrollBarTopLayout = findViewById(R.id.layout_scrollbar_top);
        mPlayPauseImg = findViewById(R.id.image_play_pause);
        mInfoLayout = findViewById(R.id.layout_info);
        mLayoutTopic = findViewById(R.id.layout_topic);
        mLayoutAddress = findViewById(R.id.layout_address);
        mAddressText = findViewById(R.id.text_address);
        mTopicText = findViewById(R.id.text_topic);
        mTitleText = findViewById(R.id.text_title);
        mBottomLayout = findViewById(R.id.layout_bottom_info);
        mBottomProgress = findViewById(R.id.bottom_progressbar);
        mBottomCommentLayout = findViewById(R.id.layout_bottom_comment);
        mCommentImg = mBottomCommentLayout.findViewById(R.id.image);
        ConstraintLayout.LayoutParams commentLp = (ConstraintLayout.LayoutParams) mCommentImg.getLayoutParams();
        commentLp.width = getResources().getDimensionPixelSize(R.dimen.dp_28);
        commentLp.height = getResources().getDimensionPixelSize(R.dimen.dp_28);
        mCommentNumText = mBottomCommentLayout.findViewById(R.id.text);
        mCommentHint = findViewById(R.id.comment_hint);
        mBottomShareLayout = findViewById(R.id.layout_bottom_share);
        mShareImg = mBottomShareLayout.findViewById(R.id.image);
        ConstraintLayout.LayoutParams shareLp = (ConstraintLayout.LayoutParams) mShareImg.getLayoutParams();
        shareLp.width = getResources().getDimensionPixelSize(R.dimen.dp_27);
        shareLp.height = getResources().getDimensionPixelSize(R.dimen.dp_27);
        mShareNum = mBottomShareLayout.findViewById(R.id.text);
        mBottomGoodLayout = findViewById(R.id.layout_bottom_good);
        mGoodImg = mBottomGoodLayout.findViewById(R.id.image);
        ConstraintLayout.LayoutParams goodLp = (ConstraintLayout.LayoutParams) mGoodImg.getLayoutParams();
        goodLp.width = getResources().getDimensionPixelSize(R.dimen.dp_30);
        goodLp.height = getResources().getDimensionPixelSize(R.dimen.dp_23);
        mGoodText = mBottomGoodLayout.findViewById(R.id.text);

        mScrollBarTopLayout.setVisibility(View.GONE);
        mPlayPauseImg.setSelected(true);
        mPlayerView= findViewById(R.id.short_video);

        mPlayerView.setShowFullAnimation(false);
        mPlayerView.setIsTouchWiget(false);
        initData();

        addListener();
    }

    private void initData() {
        mGoodLoaded = new AtomicBoolean(false);
        mAttentionLoading = new AtomicBoolean(false);
        mFavLoading = new AtomicBoolean(false);
        mDelLoading = new AtomicBoolean(false);
        mRepeatEnable = true;
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
        mBottomLayout.setOnClickListener(this);
        mLayoutTopic.setOnClickListener(this);
        mLayoutAddress.setOnClickListener(this);

        mPlayerView.setStandardVideoAllCallBack(new StandardVideoAllCallBack() {
            @Override
            public void onClickStartThumb(String url, Object... objects) {}
            @Override
            public void onClickBlank(String url, Object... objects) {}
            @Override
            public void onClickBlankFullscreen(String url, Object... objects) {}
            @Override
            public void onPrepared(String url, Object... objects) {
                changePlayPauseUI(true);
                changeThumbImageState(false);
            }
            @Override
            public void onClickStartIcon(String url, Object... objects) {}
            @Override
            public void onClickStartError(String url, Object... objects) {}
            @Override
            public void onClickStop(String url, Object... objects) {
                changePlayPauseUI(false);
            }

            @Override
            public void onClickStopFullscreen(String url, Object... objects) {}
            @Override
            public void onClickResume(String url, Object... objects) {
                changePlayPauseUI(true);
            }

            @Override
            public void onClickResumeFullscreen(String url, Object... objects) {}
            @Override
            public void onClickSeekbar(String url, Object... objects) {
                XHClick.mapStat(getContext(), ShortVideoDetailActivity.STATISTIC_ID, "视频", "进度条");
            }
            @Override
            public void onClickSeekbarFullscreen(String url, Object... objects) {}
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
            public void onEnterFullscreen(String url, Object... objects) {}
            @Override
            public void onQuitFullscreen(String url, Object... objects) {}
            @Override
            public void onQuitSmallWidget(String url, Object... objects) {}
            @Override
            public void onEnterSmallWidget(String url, Object... objects) {}
            @Override
            public void onTouchScreenSeekVolume(String url, Object... objects) {}
            @Override
            public void onTouchScreenSeekPosition(String url, Object... objects) {}
            @Override
            public void onTouchScreenSeekLight(String url, Object... objects) {}
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
//        Log.i("xianghaTag","item_______________________prepareAsync____"+position);
        if (mMainHandler == null)
            mMainHandler = new Handler(Looper.getMainLooper());
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mPlayerView.startPlayLogic();
            }
        });
    }
    public void resumeVideoView(){
//        Log.i("xianghaTag","item_______________________resumeVideoView____"+position);
        mPlayerView.onVideoResume();
    }
    /**
     * 暂停
     */
    public void pauseVideoView(){
//        Log.i("xianghaTag","item_______________________pauseVideoView____"+position);
        mPlayerView.onVideoPause();
    }

    /**
     * 重置数据
     */
    public void releaseVideoView(){
//        Log.i("xianghaTag","item_______________________releaseVideoView____"+position);
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
            mLikeImg.setSelected(TextUtils.equals("2", mData.get("isFav")));
            mLikeImg.setVisibility(View.VISIBLE);
        }
        mVideoMap = StringManager.getFirstMap(mData.get("video"));
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mThumbImg.getLayoutParams();
        DisplayMetrics dm = ToolsDevice.getWindowPx(getContext());
        int screenW = dm.widthPixels;
        int screenH = dm.heightPixels;
        int vW = Integer.parseInt(mVideoMap.get("width"));
        int vH = Integer.parseInt(mVideoMap.get("height"));
        int heightImg = screenW * vH / vW;
        lp.width = screenW;
        lp.height = heightImg;
        mThumbImg.setLayoutParams(lp);
        mVideoUrl = StringManager.getFirstMap(mVideoMap.get("videoUrl")).get("defaultUrl");
        mCommentImg.setImageResource(R.drawable.short_video_detail_comment);
        mCommentNumText.setText(mData.get("commentNum"));
        mGoodImg.setImageResource(R.drawable.bg_select_good);
        mGoodImg.setSelected(TextUtils.equals("2", mData.get("isLike")));
        mGoodText.setText(mData.get("likeNum"));
        mShareImg.setImageResource(R.drawable.short_video_detail_share);
        mShareNum.setText(mData.get("shareNum"));
        mTitleText.setText("");
        String title = mData.get("name");
        if (!TextUtils.isEmpty(title)) {
            mTitleText.setVisibility(View.VISIBLE);
            if (TextUtils.equals(mData.get("isEssence"), "2")) {
                IconTextSpan.Builder ib = new IconTextSpan.Builder(context);
                ib.setBgColorInt(getResources().getColor(R.color.color_fa273b));
                ib.setTextColorInt(getResources().getColor(R.color.c_white_text));
                ib.setText("精选");
                ib.setRadius(2f);
                ib.setRightMargin(3);
                ib.setBgHeight(18f);
                ib.setTextSize(12f);
                StringBuffer sb = new StringBuffer(" ");
                sb.append(title).append(title);
                SpannableStringBuilder ssb = new SpannableStringBuilder(sb.toString());
                ssb.setSpan(ib.build(), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mTitleText.setText(ssb);
            } else {
                mTitleText.setText(title);
            }
        } else {
            mTitleText.setVisibility(View.GONE);
        }
        mTopicMap = StringManager.getFirstMap(mData.get("topic"));
        mTopicClickUrl = mTopicMap.get("url");
        String topicTitle = mTopicMap.get("title");
        if (!TextUtils.isEmpty(topicTitle)) {
            GradientDrawable drawable = new GradientDrawable();
            String bgColor = mTopicMap.get("bgColor");
            if (TextUtils.isEmpty(bgColor))
                bgColor = "#66000000";
            drawable.setColor(Color.parseColor(bgColor));
            drawable.setCornerRadius(2f);
            mLayoutTopic.setBackground(drawable);
            String textColor = mTopicMap.get("color");
            if (TextUtils.isEmpty(textColor))
                textColor = "#ffffff";
            mTopicText.setTextColor(Color.parseColor(textColor));
            mTopicText.setText(topicTitle);
            mLayoutTopic.setVisibility(View.VISIBLE);
        } else {
            mLayoutTopic.setVisibility(View.GONE);
        }
        mAddressMap= StringManager.getFirstMap(mData.get("address"));
        mAddressClickUrl = mAddressMap.get("url");
        String address = mAddressMap.get("title");
        if(!mAddressMap.isEmpty()&&!TextUtils.isEmpty(address)){
            GradientDrawable drawable = new GradientDrawable();
            String bgColor = mAddressMap.get("bgColor");
            if (TextUtils.isEmpty(bgColor))
                bgColor = "#66000000";
            drawable.setColor(Color.parseColor(bgColor));
            drawable.setCornerRadius(2f);
            mLayoutAddress.setBackground(drawable);
            String textColor = mAddressMap.get("color");
            if (TextUtils.isEmpty(textColor))
                textColor = "#ffffff";
            mTopicText.setTextColor(Color.parseColor(textColor));
            mAddressText.setText(address);
            mLayoutAddress.setVisibility(View.VISIBLE);
        }else{
            mLayoutAddress.setVisibility(View.GONE);
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
        FavoriteHelper.instance().setFavoriteStatus(context.getApplicationContext(), mData.get("code"), mData.get("name"),
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
            case R.id.layout_address:
                if (!TextUtils.isEmpty(mAddressClickUrl)) {
                    AppCommon.openUrl(mAddressClickUrl, true);
                }
                break;
            case R.id.layout_topic:
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
            case R.id.layout_bottom_info:
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
        Map<String,String> mShareMap = StringManager.getFirstMap(mData.get("share"));
        Intent intent = new Intent(context, ShareActivityDialog.class);
        intent.putExtra("tongjiId", ShortVideoDetailActivity.STATISTIC_ID);
        intent.putExtra("shareTwoContent", "分享框");
        intent.putExtra("isHasReport", !mIsSelf);
        intent.putExtra("nickName", mUserMap.get("nickName"));
        intent.putExtra("code", mUserMap.get("code"));
        intent.putExtra("shareFrom", "菜谱详情页");
        intent.putExtra("reportUrl", "Feedback.app?feekUrl=https://www.xiangha.com/caipu/" + mUserMap.get("code") + ".html");
        intent.putExtra("imgUrl", mShareMap.get("img"));
        intent.putExtra("title", mShareMap.get("title"));
        intent.putExtra("content", mShareMap.get("content"));
        String clickUrl = mShareMap.get("url");
        intent.putExtra("clickUrl", clickUrl);
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
                    if (mMainHandler == null)
                        mMainHandler = new Handler(Looper.getMainLooper());
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
        if (mOnSeekBarTrackingTouchListener != null)
            mOnSeekBarTrackingTouchListener.onStartTrackingTouch(seekBar.getProgress());
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
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
