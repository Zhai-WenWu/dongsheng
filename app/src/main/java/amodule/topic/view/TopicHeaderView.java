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

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.tools.ImgManager;
import acore.tools.StringManager;
import amodule.dish.activity.ListDish;
import amodule.search.view.MultiTagView;
import amodule.topic.style.CustomClickableSpan;
import amodule.user.activity.FriendHome;
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

    }

    public void initData(Map<String, String> infoMap) {
//        LoadImage.with(mContext).load(infoMap.get("image")).build().into(mUserRearImg);

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
        for (int i = 0; i < userList.size(); i++) {
            ArrayMap<String, String> map = new ArrayMap<>();
            map.put("hot", "@" + userList.get(i).get("nickName") + "、");
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

    }

    public void showTopicData(String mActivityType, Map<String, String> infoMap) {
        switch (mActivityType) {

            case "0":
                mActivityLayout.setVisibility(GONE);

                initData(infoMap);

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

                mBottomLinkTv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), mLink.get("url"), true);
                    }
                });

                break;
            case "1":
                mBottomLinkTv.setText("点击参加活动");

                initData(infoMap);

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

                mActivityLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), mLink.get("url"), true);
                    }
                });

                break;
            case "2":
                mShadePanel.setVisibility(GONE);
                containerLayout.setVisibility(GONE);
                Map<String, String> activityInfo = StringManager.getFirstMap(infoMap.get("activityInfo"));
                String url = activityInfo.get("url");
                ViewGroup.LayoutParams layoutParams = mUserRearImg.getLayoutParams();
//                layoutParams.height = activityInfo.get("imageheight");
                mUserRearImg.setLayoutParams(layoutParams);
                LoadImage.with(mContext).load(url).build().into(mUserRearImg);
                break;
        }

    }
}
