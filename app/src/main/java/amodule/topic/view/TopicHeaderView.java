package amodule.topic.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.xiangha.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.ImgManager;
import acore.tools.StringManager;
import amodule.search.view.MultiTagView;
import amodule.topic.style.CustomClickableSpan;
import aplug.basic.LoadImage;

public class TopicHeaderView extends RelativeLayout {

    private CustomClickableSpan mCustomClickableSpan;

    private ImageView mUserRearImg;
    private TextView mTopicInfo;
    private TextView mTopicNum;
    private View mShadePanel;
    private LinearLayout mActivityLayout;
    private TextView mBottomLinkTv;
    private Map<String, String> mLink;
    private String mContent;
    private String mNum;
    private TextView mActivityTv;
    private Context mContext;
    private RelativeLayout containerLayout;
    private MultiTagView mSocialiteTable;
    private ArrayList<Map<String, String>> userList;
    private ArrayList<Map<String, String>> userNameList;
    private Map<String, String> user;

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
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.topic_header_layout, this, true);
        mUserRearImg = findViewById(R.id.user_rear_img);
        mTopicInfo = findViewById(R.id.topic_info);
        mTopicNum = findViewById(R.id.topic_num);
        mBottomLinkTv = findViewById(R.id.tv_bottom_link);
        mBottomLinkTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        mShadePanel = findViewById(R.id.shade);
        mActivityLayout = findViewById(R.id.ll_activity_btn);
        mActivityTv = findViewById(R.id.activity_btn);
        containerLayout = findViewById(R.id.rl_container);
        mSocialiteTable = findViewById(R.id.socialite_table);
        mSocialiteTable.setPressColor("#00000000");
        mSocialiteTable.setNormalCorlor("#00000000");

//        String imageUrl = "https://s3.cdn.xiangha.com/img/201809/1211/s/5b9885523976a.png/NTAweDA";
//        Glide.with(context).load(imageUrl).into(topicImage);

        //判断哪种活动类型
//        switch (mActivityType) {
//            case "0":
//                mActivityLayout.setVisibility(GONE);
//                mBottomLinkTv.setText("点击此查看活动详情");
//                break;
//            case "1":
//                mBottomLinkTv.setText("点击参加活动");
//                break;
//            case "2":
//                mShadePanel.setVisibility(GONE);
//                containerLayout.setVisibility(GONE);
//                break;
//        }

    }

    public void showUserImage(String url) {
        if (TextUtils.isEmpty(url)) {
//            hideTopicImage();
            return;
        }
//        mUserFrontImg.setOnClickListener(listener);
//        mUserFrontImg.setTag(R.string.tag, url);
        Glide.with(getContext()).load(url).downloadOnly(new SimpleTarget<File>() {

            @Override
            public void onLoadFailed(Exception e, Drawable drawable) {
                super.onLoadFailed(e, drawable);
//                hideTopicImage();
            }

            @Override
            public void onResourceReady(File file, GlideAnimation<? super File> glideAnimation) {
                try {
                    InputStream is = new FileInputStream(file);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
//                    mUserFrontImg.setVisibility(View.VISIBLE);
//                    mUserFrontImg.setImageBitmap(bitmap);
                    bitmap = ImgManager.RSBlur(getContext(), bitmap, 10);
                    mUserRearImg.setImageBitmap(bitmap);
                    mShadePanel.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
//                    hideTopicImage();
                }
            }
        });
    }


    public void setAttentionEnable(boolean enable) {
//        mTopicAttention.setEnabled(enable);
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

        requestLayout();
    }

//    public void hideTopicImage() {
//        mUserFrontImg.setVisibility(View.GONE);
//    }
//
//    public void hideTopicUser() {
//        mTopicUser.setVisibility(View.GONE);
//    }
//
//    public void hideTopicAttention() {
//        mTopicAttention.setVisibility(View.GONE);
//    }

    public void hideTopicInfo() {
        mTopicInfo.setVisibility(View.GONE);
    }

    public void hideTopicNum() {
        mTopicNum.setVisibility(View.GONE);
    }

    public void showTopicData(String mActivityType, Map<String, String> infoMap) {
        switch (mActivityType) {

            case "0":
                mActivityLayout.setVisibility(GONE);
//                LoadImage.with(mContext).load(infoMap.get("image")).build().into(mUserRearImg);

                //参与人数
                mNum = infoMap.get("num");
                if (!TextUtils.isEmpty(mNum)) {
                    SpannableStringBuilder ss = new SpannableStringBuilder();
                    ss.append(mNum).append("人参与");
                    mTopicNum.setText(ss);
                } else {
                    mTopicNum.setVisibility(GONE);
                }

                //话题content
                mContent = infoMap.get("content");
                if (!TextUtils.isEmpty(mContent)) {
                    mTopicInfo.setText(mContent);
                } else {
                    mTopicInfo.setVisibility(GONE);
                }

                //底部查看活动详情链接
                mLink = StringManager.getFirstMap(infoMap.get("link"));
                if (mLink != null) {
                    String text = mLink.get("text");
                    if (!TextUtils.isEmpty(text)) {
                        mBottomLinkTv.setText(mLink.get("text"));
                    } else {
                        mBottomLinkTv.setText("点击此查看活动详情");
                    }

                } else {
                    mBottomLinkTv.setVisibility(GONE);
                }

                //社交达人
                user = StringManager.getFirstMap(infoMap.get("users"));
                userList = StringManager.getListMapByJson(user.get("info"));
                userNameList = new ArrayList<>();
                for (int i = 0; i < userList.size(); i++) {
                    ArrayMap<String, String> map = new ArrayMap<>();
                    map.put("hot", "@" + userList.get(i).get("nickName") + "、");
                    userNameList.add(map);
                }

                mSocialiteTable.addTags(userNameList, new MultiTagView.MutilTagViewCallBack() {
                    @Override
                    public void onClick(int tagIndexr) {

                    }
                });

                break;
            case "1":
                mBottomLinkTv.setText("点击参加活动");
                LoadImage.with(mContext).load(infoMap.get("image")).build().into(mUserRearImg);

                //参与人数
                mNum = infoMap.get("num");
                if (!TextUtils.isEmpty(mNum)) {
                    SpannableStringBuilder ss = new SpannableStringBuilder();
                    ss.append(mNum).append("人参与");
                    mTopicNum.setText(ss);
                } else {
                    mTopicNum.setVisibility(GONE);
                }

                //话题content
                mContent = infoMap.get("content");
                if (!TextUtils.isEmpty(mContent)) {
                    mTopicInfo.setText(mContent);
                } else {
                    mTopicInfo.setVisibility(GONE);
                }

                //详情链接
                mLink = StringManager.getFirstMap(infoMap.get("link"));
                if (mLink != null) {
                    String text = mLink.get("text");
                    if (!TextUtils.isEmpty(text)) {
                        mActivityTv.setText(mLink.get("text"));
                    } else {
                        mActivityTv.setText("点击此查看活动详情");
                    }

                } else {
                    mActivityLayout.setVisibility(GONE);
                }

                //社交达人
                user = StringManager.getFirstMap(infoMap.get("users"));
                userList = StringManager.getListMapByJson(user.get("info"));
                userNameList = new ArrayList<>();
                for (int i = 0; i < userList.size(); i++) {
                    ArrayMap<String, String> map = new ArrayMap<>();
                    map.put("hot", "@" + userList.get(i).get("nickName") + "、");
                    userNameList.add(map);
                }

                mSocialiteTable.addTags(userNameList, new MultiTagView.MutilTagViewCallBack() {
                    @Override
                    public void onClick(int tagIndexr) {

                    }
                });

                break;
            case "2":
                mShadePanel.setVisibility(GONE);
                containerLayout.setVisibility(GONE);
                Map<String, String> activityInfo = StringManager.getFirstMap(infoMap.get("activityInfo"));
                String url = activityInfo.get("url");
                ViewGroup.LayoutParams layoutParams = mUserRearImg.getLayoutParams();
                Glide.with(mContext)
                        .load(url)
                        .asBitmap()//强制Glide返回一个Bitmap对象
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                int width = bitmap.getWidth();
                                int height = bitmap.getHeight();
                                layoutParams.height = height;
                                mUserRearImg.setLayoutParams(layoutParams);
                                mUserRearImg.setImageBitmap(bitmap);
                            }
                        });
                break;
        }

    }
}
