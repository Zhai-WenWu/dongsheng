package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.ToolsDevice;
import amodule.dish.activity.ShortVideoDetailActivity;
import amodule.dish.video.module.ShortVideoDetailADModule;
import amodule.dish.video.module.ShortVideoDetailModule;
import amodule.main.view.item.BaseItemView;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;

/**
 * 短视频itemView
 */
public class ShortVideoADItemView extends BaseItemView implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private final int FIXED_TEXT_COUNT = 50;

    private int mScreenW, mScreenH;
    private int mFixedHW;

    private Context context;
    private ImageView mThumbImg;
    private ImageView mBackImg;
    private ImageView mHeaderImg;
    private TextView mUserName;
    private ImageView mAttentionImage;
    private ImageView mLikeImg;
    private ImageView mMoreImg;
    private View mEmptyView;
    private TextView mTitleText;
    private View mBottomCommentLayout;
    private View mBottomShareLayout;
    private View mBottomGoodLayout;
    private ImageView mCommentImg;
    private TextView mCommentNumText;
    private ImageView mShareImg;
    private TextView mShareNum;
    private ImageView mGoodImg;
    private TextView mGoodText;
    private Button mSeeDetailButton;
    private ProgressBar mBottomProgress;

    private ShortVideoDetailModule mData;//全部
    private boolean mIsSelf;

    private int mPos;
    private int position;

    public ShortVideoADItemView(Context context) {
        this(context, null);
    }

    public ShortVideoADItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.item_short_video_ad_view, this, true);
        initView();
    }

    public void initView() {
        mSeeDetailButton = findViewById(R.id.see_detail_btn);
        mThumbImg = findViewById(R.id.image_thumb);
        mBackImg = findViewById(R.id.image_back);
        mHeaderImg = findViewById(R.id.image_user_header);
        mUserName = findViewById(R.id.text_user_name);
        mAttentionImage = findViewById(R.id.img_attention);
        mLikeImg = findViewById(R.id.image_like);
        mMoreImg = findViewById(R.id.image_more);
        mEmptyView = findViewById(R.id.view_empty);
        mTitleText = findViewById(R.id.text_title);
        mBottomProgress = findViewById(R.id.bottom_progressbar);
        mBottomCommentLayout = findViewById(R.id.layout_bottom_comment);
        mCommentImg = mBottomCommentLayout.findViewById(R.id.image3);
        mCommentNumText = mBottomCommentLayout.findViewById(R.id.text3);
        mBottomShareLayout = findViewById(R.id.layout_bottom_share);
        mShareImg = mBottomShareLayout.findViewById(R.id.image2);
        mShareNum = mBottomShareLayout.findViewById(R.id.text2);
        mBottomGoodLayout = findViewById(R.id.layout_bottom_good);
        mGoodImg = mBottomGoodLayout.findViewById(R.id.image1);
        mGoodText = mBottomGoodLayout.findViewById(R.id.text1);

        initData();

        addListener();
    }

    private void initData() {
        DisplayMetrics dm = ToolsDevice.getWindowPx(getContext());
        mScreenW = dm.widthPixels;
        mScreenH = dm.heightPixels;
        mFixedHW = 667 / 375;
    }

    private void addListener() {
        mSeeDetailButton.setOnClickListener(this);
        mBackImg.setOnClickListener(this);
        mHeaderImg.setOnClickListener(this);
        mUserName.setOnClickListener(this);
        mAttentionImage.setOnClickListener(this);
        mLikeImg.setOnClickListener(this);
        mMoreImg.setOnClickListener(this);
        mEmptyView.setOnClickListener(this);
        mBottomCommentLayout.setOnClickListener(this);
        mBottomGoodLayout.setOnClickListener(this);
        mBottomShareLayout.setOnClickListener(this);
    }

    /** 开始播放入口 */
    public void prepareAsync() {
    }

    public void resumeVideo() {
    }

    /** 暂停 */
    public void pauseVideo() {
    }

    /** 重置数据 */
    public void releaseVideo() {
    }

    public boolean isPlaying() {
        return false;
    }

    /**
     * 设置数据
     *
     * @param module
     */
    public void setData(ShortVideoDetailModule module, int position) {
        mData = module;
        this.position = position;
        if (mData == null)
            return;
        mUserName.setText(mData.getCustomerModel().getNickName());
        mIsSelf = TextUtils.equals(LoginManager.userInfo.get("code"), mData.getCustomerModel().getUserCode());
        if (mIsSelf) {
            mAttentionImage.setVisibility(View.GONE);
            mLikeImg.setVisibility(View.GONE);
            mMoreImg.setVisibility(View.VISIBLE);
        } else {
            mAttentionImage.setVisibility(mData.getCustomerModel().isFollow() ? View.GONE : View.VISIBLE);
            mMoreImg.setVisibility(View.GONE);
            mLikeImg.setSelected(mData.isFav());
            mLikeImg.setVisibility(View.VISIBLE);
        }

        loadUserHeader(mData.getCustomerModel().getHeaderImg());
        BitmapRequestBuilder builder = LoadImage.with(getContext())
                .load(module.getImageModel().getImageUrl())
                .setPlaceholderId(R.color.transparent)
                .build();
        if (builder != null) {
            builder.into(mThumbImg);
        }

        mCommentImg.setImageResource(R.drawable.short_video_detail_comment);
        mCommentNumText.setText(mData.getCommentNum());

        mGoodImg.setImageResource(R.drawable.bg_select_good);
        mGoodImg.setSelected(mData.isLike());
        mGoodText.setText(mData.getLikeNum());

        mShareImg.setImageResource(R.drawable.short_video_detail_share);
        mShareNum.setText(mData.getShareNum());

        mTitleText.setText("");
        String title = mData.getName();
        if (!TextUtils.isEmpty(title)) {
            mTitleText.setVisibility(View.VISIBLE);
            mTitleText.setText(title);
        } else {
            mTitleText.setVisibility(View.GONE);
        }
    }

    public void setPos(int pos) {
        mPos = pos;
    }

    public int getPos() {
        return mPos;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                closeActivity();
                XHClick.mapStat(getContext(), ShortVideoDetailActivity.STA_ID, "返回", "");
                break;
            default:
                if (mOnADClickCallback != null) {
                    int index = ((ShortVideoDetailADModule) mData).adRealPosition;
                    mOnADClickCallback.onADClick(this, index, String.valueOf(index + 1));
                }
                break;
        }
    }

    private void closeActivity() {
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    public void gotoUser() {
        AppCommon.openUrl(mData.getCustomerModel().getGotoUrl(), true);
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
                    mHeaderImg.post(() -> {
                        if (!mHeaderImg.getTag(TAG_ID).equals(url))
                            return;
                        mHeaderImg.setImageBitmap(bitmap);
                    });
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public final String getString(@StringRes int resId) {
        return getResources().getString(resId);
    }

    public interface OnADShowCallback {
        void onAdShow(int index, View view, String listIndex);
    }

    public OnADShowCallback mOnADShowCallback;

    public void setOnADShowCallback(OnADShowCallback onADShowCallback) {
        mOnADShowCallback = onADShowCallback;
    }

    public interface OnADClickCallback {
        void onADClick(View view, int index, String listIndex);
    }

    public OnADClickCallback mOnADClickCallback;

    public void setOnADClickCallback(OnADClickCallback onADClickCallback) {
        mOnADClickCallback = onADClickCallback;
    }
}
//1062