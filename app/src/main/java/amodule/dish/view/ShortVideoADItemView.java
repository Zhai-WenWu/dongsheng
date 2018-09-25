package amodule.dish.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
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
import acore.tools.Tools;
import amodule.dish.activity.ShortVideoDetailActivity;
import amodule.dish.video.module.ShortVideoDetailADModule;
import amodule.main.view.item.BaseItemView;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;

import static third.ad.scrollerAd.XHScrollerAdParent.ADKEY_GDT;
import static third.ad.scrollerAd.XHScrollerAdParent.TAG_GDT;

/**
 * 短视频itemView
 */
public class ShortVideoADItemView extends BaseItemView implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private Context context;
    private ImageView mThumbImg;
    private ImageView mBackImg;
    private ImageView mHeaderImg;
    private TextView mUserName;
    private ImageView mLikeImg;
    private View mEmptyView;
    private TextView mTitleText;
    private View mBottomCommentLayout;
    private View mBottomShareLayout;
    private View mBottomGoodLayout;
    private TextView mCommentNumText;
    private TextView mShareNum;
    private ImageView mGoodImg;
    private TextView mGoodText;
    private TextView mSeeDetailButton;
    private ImageView madTag;

    private ShortVideoDetailADModule mData;//全部

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
        mLikeImg = findViewById(R.id.image_like);
        mEmptyView = findViewById(R.id.view_empty);
        mTitleText = findViewById(R.id.text_title);
        mBottomCommentLayout = findViewById(R.id.layout_bottom_comment);
        mCommentNumText = mBottomCommentLayout.findViewById(R.id.text3);
        mBottomShareLayout = findViewById(R.id.layout_bottom_share);
        mShareNum = mBottomShareLayout.findViewById(R.id.text2);
        mBottomGoodLayout = findViewById(R.id.layout_bottom_good);
        mGoodImg = mBottomGoodLayout.findViewById(R.id.image1);
        mGoodText = mBottomGoodLayout.findViewById(R.id.text1);
        madTag = findViewById(R.id.view_ad_tag);

        addListener();
    }

    private void addListener() {
        findViewById(R.id.see_detail_Layout).setOnClickListener(this);
        madTag.setOnClickListener(this);
        mBackImg.setOnClickListener(this);
        mHeaderImg.setOnClickListener(this);
        mUserName.setOnClickListener(this);
        mLikeImg.setOnClickListener(this);
        mTitleText.setOnClickListener(this);
        mEmptyView.setOnClickListener(this);
//        mBottomCommentLayout.setOnClickListener(this);
//        mBottomGoodLayout.setOnClickListener(this);
//        mBottomShareLayout.setOnClickListener(this);
    }

    /** 开始播放入口 */
    public void prepareAsync() {
        //曝光回调
        if (!this.mData.isShown) {
            this.mData.isShown = true;
            if (mOnADShowCallback != null) {
                mOnADShowCallback.onAdShow(this.mData.adRealPosition, this, String.valueOf(this.mData.adRealPosition + 1));
            }
        }
        animScale();
        //查看详情动画
//        postDelayed(this::anim, 3000);
    }

    private void animScale() {
        if(animatorSet == null){
            //图片动画
            ObjectAnimator imageAnimX = ObjectAnimator.ofFloat(mThumbImg, "scaleX", 1.5f, 1.0f);
            imageAnimX.setDuration(2000);
            ObjectAnimator imageAnimY = ObjectAnimator.ofFloat(mThumbImg, "scaleY", 1.5f, 1.0f);
            imageAnimY.setDuration(2000);
            animatorSet = new AnimatorSet();
            animatorSet.playTogether(imageAnimX, imageAnimY);
        }
        animatorSet.start();
    }

    private AnimatorSet animatorSet;
    ObjectAnimator animator;

    private void anim() {
        if(animator == null){
            animator = ObjectAnimator.ofFloat(mSeeDetailButton, "alpha", 0f, 1f);
            animator.setDuration(300);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mSeeDetailButton.setVisibility(VISIBLE);
                }
            });
        }
        animator.start();
    }

    public void resumeVideo() {
    }

    /** 暂停 */
    public void pauseVideo() {
    }

    /** 重置数据 */
    public void releaseVideo() {
        if(animator != null){
            animator.cancel();
        }
        if(animatorSet != null){
            animatorSet.cancel();
        }
        mSeeDetailButton.setVisibility(GONE);
        mThumbImg.setScaleX(1.5f);
        mThumbImg.setScaleY(1.5f);
    }

    public boolean isPlaying() {
        return false;
    }

    /**
     * 设置数据
     *
     * @param module
     */
    public void setData(ShortVideoDetailADModule module, int position) {
        mData = module;
        this.position = position;
        if (mData == null)
            return;
        mUserName.setText(mData.getCustomerModel().getNickName());
        mLikeImg.setSelected(mData.isFav());
        mLikeImg.setVisibility(View.VISIBLE);

        loadUserHeader(mData.getCustomerModel().getHeaderImg());
        BitmapRequestBuilder builder = LoadImage.with(getContext())
                .load(module.getImageModel().getImageUrl())
                .setPlaceholderId(R.color.transparent)
                .build();
        if (builder != null) {
            builder.into(mThumbImg);
        }

        mCommentNumText.setText(mData.getCommentNum());

        mGoodImg.setSelected(mData.isLike());
        mGoodText.setText(mData.getLikeNum());

        mShareNum.setText(mData.getShareNum());

        mTitleText.setText("");
        String title = mData.getName();
        if (!TextUtils.isEmpty(title)) {
            mTitleText.setVisibility(View.VISIBLE);
            mTitleText.setText(title);
        } else {
            mTitleText.setVisibility(View.GONE);
        }

        if(ADKEY_GDT.equals(mData.adType)){
            madTag.setImageResource(R.drawable.icon_ad_gdt);
            madTag.getLayoutParams().width = Tools.getDimen(getContext(),R.dimen.dp_48);
            madTag.getLayoutParams().height = Tools.getDimen(getContext(),R.dimen.dp_18);
        }else {
            madTag.setImageResource(R.drawable.icon_video_ad);
            madTag.getLayoutParams().width = Tools.getDimen(getContext(),R.dimen.dp_36);
            madTag.getLayoutParams().height = Tools.getDimen(getContext(),R.dimen.dp_18);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                closeActivity();
                XHClick.mapStat(getContext(), ShortVideoDetailActivity.STA_ID, "返回", "");
                break;
            case R.id.view_ad_tag:
                if (mOnAdHintClickListener != null) {
                    mOnAdHintClickListener.onAdHintClick(mData.adRealPosition,String.valueOf(mData.adPositionInData));
                }
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

    private OnADShowCallback mOnADShowCallback;

    public void setOnADShowCallback(OnADShowCallback onADShowCallback) {
        mOnADShowCallback = onADShowCallback;
    }

    public interface OnADClickCallback {
        void onADClick(View view, int index, String listIndex);
    }

    private OnADClickCallback mOnADClickCallback;

    public void setOnADClickCallback(OnADClickCallback onADClickCallback) {
        mOnADClickCallback = onADClickCallback;
    }

    public interface OnAdHintClickListener {
        void onAdHintClick(int indexInData,String promotionIndex);
    }

    private OnAdHintClickListener mOnAdHintClickListener;

    public void setOnAdHintClickListener(OnAdHintClickListener onAdHintClickListener) {
        mOnAdHintClickListener = onAdHintClickListener;
    }
}
//1062