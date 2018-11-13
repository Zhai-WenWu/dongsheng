package amodule.topic.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
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
import com.popdialog.util.ToolsDevice;
import com.xiangha.R;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.tools.ImgManager;
import acore.tools.StringManager;
import amodule.dish.activity.ListDish;
import amodule.search.view.MultiTagView;
import amodule.topic.activity.TopicInfoActivity;
import amodule.topic.style.CustomClickableSpan;
import amodule.user.activity.FriendHome;
import anet.channel.util.StringUtils;
import aplug.basic.LoadImage;
import third.aliyun.work.AliyunCommon;

public class TopicHeaderView extends RelativeLayout {

    private CustomClickableSpan mCustomClickableSpan;

    private ImageView mUserRearImg;
    private TextView mTopicInfo;
    private TextView mTopicNum;
    private View mShadePanel;
    private TextView mBottomLinkTv;
    private Map<String, String> mLink;
    private Map<String, String> mActivityInfo;
    private String mContent;
    private String mNum;
    private TextView mActivityTv;
    private Context mContext;
    private RelativeLayout containerLayout;
    private MultiTagView mSocialiteTable;
    private ArrayList<Map<String, String>> userList;
    private ArrayList<Map<String, String>> userNameList;
    private Map<String, String> user;
    private TextView tv_socialite;

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
        mActivityTv = findViewById(R.id.activity_btn);
        containerLayout = findViewById(R.id.rl_container);
        mSocialiteTable = findViewById(R.id.socialite_table);
        tv_socialite = findViewById(R.id.tv_socialite);
        mSocialiteTable.setPressColor("#00000000");
        mSocialiteTable.setNormalCorlor("#00000000");

    }

    public void initData(Map<String, String> infoMap) {
        String image = infoMap.get("image");
        if (!TextUtils.isEmpty(image) && image != "null") {
            LoadImage.with(mContext).load(image).build().into(mUserRearImg);
            Glide.with(mContext).load(image).downloadOnly(new SimpleTarget<File>() {
                @Override
                public void onResourceReady(File file, GlideAnimation<? super File> glideAnimation) {
                    try {
                        InputStream is = new FileInputStream(file);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        bitmap = ImgManager.RSBlur(getContext(), bitmap, 10);
                        mUserRearImg.setImageBitmap(bitmap);
                        mShadePanel.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

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

        //社交达人
        user = StringManager.getFirstMap(infoMap.get("users"));
        userList = StringManager.getListMapByJson(user.get("info"));
        userNameList = new ArrayList<>();
        int size = userList.size();
        if (size > 0) {


            for (int i = 0; i < size; i++) {
                ArrayMap<String, String> map = new ArrayMap<>();

                if (size == 1) {
                    map.put("hot", "@" + userList.get(i).get("nickName"));//@一个人没顿号
                } else {
                    if (i < size - 1) {
                        map.put("hot", "@" + userList.get(i).get("nickName") + "、");
                    } else {
                        map.put("hot", "@" + userList.get(i).get("nickName"));//最后一个人没顿号
                    }
                }
                userNameList.add(map);
            }

            mSocialiteTable.addTags(userNameList, new MultiTagView.MutilTagViewCallBack() {
                @Override
                public void onClick(int tagIndexr) {
                    Intent intent = new Intent(mContext, FriendHome.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("code", userList.get(tagIndexr).get("code"));
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                }
            });
        } else {
            mSocialiteTable.setVisibility(GONE);
            tv_socialite.setVisibility(GONE);
        }

        //底部查看活动详情链接
        mLink = StringManager.getFirstMap(infoMap.get("link"));
        if (mLink != null) {
            String text = mLink.get("text");
            if (!TextUtils.isEmpty(text)) {
                mBottomLinkTv.setText(mLink.get("text"));
            } else {
                mBottomLinkTv.setVisibility(GONE);
            }

        } else {
            mBottomLinkTv.setVisibility(GONE);
        }

        mBottomLinkTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), mLink.get("url"), true);
            }
        });

    }

    public void showTopicData(String mActivityType, String mTopicCode, Map<String, String> infoMap) {
        switch (mActivityType) {

            case "0":
                mActivityTv.setVisibility(GONE);
                initData(infoMap);

                break;
            case "1":
                initData(infoMap);

                setWrapContentNotOfBackground(mActivityTv);

                //详情链接
                mActivityInfo = StringManager.getFirstMap(infoMap.get("activityInfo"));
                if (mActivityInfo != null) {
                    String text = mActivityInfo.get("text");
                    if (!TextUtils.isEmpty(text)) {
                        mActivityTv.setText(mActivityInfo.get("text"));
                    } else {
                        mActivityTv.setVisibility(GONE);
                    }
                } else {
                    mActivityTv.setVisibility(GONE);
                }

                mActivityTv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), mActivityInfo.get("url"), true);
                    }
                });

                break;
            case "2":
                mShadePanel.setVisibility(GONE);
                containerLayout.setVisibility(GONE);
                Map<String, String> activityInfo = StringManager.getFirstMap(infoMap.get("activityInfo"));
                String url = activityInfo.get("url");
                String imageWidth = activityInfo.get("imageWidth");
                String imageHeight = activityInfo.get("imageHeight");
                int w = Integer.parseInt("500");
                int h = Integer.parseInt("750");
                int widthPixels = ToolsDevice.getWindowPx(mContext).widthPixels;

                ViewGroup.LayoutParams layoutParams = mUserRearImg.getLayoutParams();
                float f = (float) widthPixels / w;
                layoutParams.height = (int) (f * h);
                mUserRearImg.setLayoutParams(layoutParams);
                LoadImage.with(mContext).load(url).build().into(mUserRearImg);
                break;
        }

    }

    public void setWrapContentNotOfBackground(View view) {
        Drawable drawable = view.getBackground();
        view.setBackground(null);

        view.post(() -> {
            int width = view.getWidth();
            int height = view.getHeight();
            if (width <= 480) {
                width = 480;
            }
            view.setBackground(drawable);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = width;
            layoutParams.height = height;
        });
    }
}
