package amodule.topic.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import acore.tools.FileManager;
import acore.tools.ImgManager;
import amodule.topic.activity.SearchTopicActivity;
import amodule.topic.style.CustomClickableSpan;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

public class TopicHeaderView extends RelativeLayout {

    private CustomClickableSpan mCustomClickableSpan;

    private ImageView mUserRearImg;
    private ImageView mUserFrontImg;
    private TextView mTopicUser;
    private ImageView mTopicAttention;
    private TextView mTopicInfo;
    private TextView mTopicNum;
    private View mShadePanel;
    public TopicHeaderView(Context context) {
        super(context);
        initView(context);
    }

    public TopicHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TopicHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.topic_header_layout, this, true);
        mUserRearImg = findViewById(R.id.user_rear_img);
        mUserFrontImg = findViewById(R.id.user_front_img);
        ImageView topicImage = findViewById(R.id.topic_image);
        mTopicUser = findViewById(R.id.topic_user);
        mTopicAttention = findViewById(R.id.topic_attention);
        mTopicInfo = findViewById(R.id.topic_info);
        mTopicNum = findViewById(R.id.topic_num);
        mShadePanel = findViewById(R.id.shade);
        topicImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, SearchTopicActivity.class));
            }
        });
//        String imageUrl = "https://s3.cdn.xiangha.com/img/201809/1211/s/5b9885523976a.png/NTAweDA";
//        Glide.with(context).load(imageUrl).into(topicImage);
    }

    public void showUserImage(String url, OnClickListener listener) {
        if (TextUtils.isEmpty(url)) {
            hideTopicImage();
            return;
        }
        mUserFrontImg.setOnClickListener(listener);
        mUserFrontImg.setTag(R.string.tag, url);
        Glide.with(getContext()).load(url).downloadOnly(new SimpleTarget<File>() {

            @Override
            public void onLoadFailed(Exception e, Drawable drawable) {
                super.onLoadFailed(e, drawable);
                hideTopicImage();
            }

            @Override
            public void onResourceReady(File file, GlideAnimation<? super File> glideAnimation) {
                try {
                    InputStream is = new FileInputStream(file);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    mUserFrontImg.setVisibility(View.VISIBLE);
                    mUserFrontImg.setImageBitmap(bitmap);
                    bitmap = ImgManager.RSBlur(getContext(),bitmap,10);
                    mUserRearImg.setImageBitmap(bitmap);
                    mShadePanel.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                    hideTopicImage();
                }
            }
        });
    }

    public void showTopicUser(String userName, OnClickListener listener) {
        if (TextUtils.isEmpty(userName)) {
            hideTopicUser();
            return;
        }
        mTopicUser.setVisibility(View.VISIBLE);
        SpannableStringBuilder ssb = new SpannableStringBuilder(userName);
        if (mCustomClickableSpan == null) {
            mCustomClickableSpan = new CustomClickableSpan();
            mCustomClickableSpan.setTextColor(Color.parseColor("#ffd914"));
            mCustomClickableSpan.setHasUnderline(false);
        }
        mCustomClickableSpan.setOnClickListener(listener);
        ssb.setSpan(mCustomClickableSpan, 0, userName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append(" 创建的话题");
        mTopicUser.setMovementMethod(LinkMovementMethod.getInstance());
        mTopicUser.setText(ssb);
        requestLayout();
    }

    public void showTopicAttention(boolean attentioned, OnClickListener listener){
        mTopicAttention.setVisibility(View.VISIBLE);
        mTopicAttention.setOnClickListener(listener);
        mTopicAttention.setEnabled(!attentioned);
        requestLayout();
    }

    public void setAttentionEnable(boolean enable) {
        mTopicAttention.setEnabled(enable);
        requestLayout();
    }

    public void showTopicInfo(String info) {
        if (TextUtils.isEmpty(info)) {
            hideTopicInfo();
            return;
        }
        mTopicInfo.setVisibility(View.VISIBLE);
        mTopicInfo.setText(info);
        requestLayout();
    }

    public void showTopicNum(String numStr) {
        if (TextUtils.isEmpty(numStr)) {
            hideTopicNum();
            return;
        }
        mTopicNum.setVisibility(View.VISIBLE);
        SpannableStringBuilder ss = new SpannableStringBuilder("— ");
        ss.append(numStr).append(" 人参与 —");
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.white)), 2, numStr.length() + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTopicNum.setText(ss);
        requestLayout();
    }

    public void hideTopicImage() {
        mUserFrontImg.setVisibility(View.GONE);
    }

    public void hideTopicUser() {
        mTopicUser.setVisibility(View.GONE);
    }

    public void hideTopicAttention() {
        mTopicAttention.setVisibility(View.GONE);
    }

    public void hideTopicInfo() {
        mTopicInfo.setVisibility(View.GONE);
    }

    public void hideTopicNum() {
        mTopicNum.setVisibility(View.GONE);
    }

}
