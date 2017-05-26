package amodule.article.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;
import acore.tools.StringManager;

import static amodule.dish.activity.DetailDish.tongjiId;

/**
 * 文章header头view
 */
public class ArticleHeaderView extends ItemBaseView {
    private TextView acticle_title, user_name, user_about, follow_tv;
    private RelativeLayout exp_user_rela, follow_rela;
    private ImageView auther_userImg;
    private Map<String, String> mapUser;

    public ArticleHeaderView(Context context) {
        super(context, R.layout.view_article_header);
    }

    public ArticleHeaderView(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs, R.layout.view_article_header);
    }

    public ArticleHeaderView(Context context, AttributeSet attrs, int defStyleAttr, int layoutId) {
        super(context, attrs, defStyleAttr, R.layout.view_article_header);
    }

    @Override
    public void init() {
        super.init();
        acticle_title = (TextView) findViewById(R.id.acticle_title);
        exp_user_rela = (RelativeLayout) findViewById(R.id.exp_user_rela);
        auther_userImg = (ImageView) findViewById(R.id.auther_userImg);
        user_name = (TextView) findViewById(R.id.user_name);
        user_about = (TextView) findViewById(R.id.user_about);
        follow_tv = (TextView) findViewById(R.id.follow_tv);
        follow_rela = (RelativeLayout) findViewById(R.id.follow_rela);
        follow_rela.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), tongjiId, "用户点击", "头像点击量");
                if (mapUser != null && !mapUser.isEmpty()) {
                    AppCommon.onAttentionClick(mapUser.get("code"), "follow");
                    mapUser.put("isFollow", "2");
                    setMapFollowSate();
                }
            }
        });
    }

    /**
     * 处理数据
     *
     * @param map
     */
    public void setData(@NonNull Map<String, String> map) {
        if (map.isEmpty()) return;
        //标题
        if (map.containsKey("title") && !TextUtils.isEmpty(map.get("title"))) {
            acticle_title.setText(map.get("title"));
            acticle_title.setVisibility(VISIBLE);
        } else acticle_title.setVisibility(GONE);
        //用户
        if (map.containsKey("customer") && !TextUtils.isEmpty(map.get("customer"))) {
            exp_user_rela.setVisibility(VISIBLE);
            mapUser = StringManager.getFirstMap(map.get("customer"));
            if (mapUser.containsKey("img") && !TextUtils.isEmpty(mapUser.get("img"))) {
                setViewImage(auther_userImg, mapUser.get("img"));
            }
            if (mapUser.containsKey("isGourment") && "2".equals(mapUser.get("isGourment"))) {
                findViewById(R.id.cusType).setVisibility(VISIBLE);
            } else findViewById(R.id.cusType).setVisibility(GONE);
            if (mapUser.containsKey("nickName") && !TextUtils.isEmpty(mapUser.get("nickName"))) {
                user_name.setVisibility(VISIBLE);
                user_name.setText(mapUser.get("nickName"));
            } else user_name.setVisibility(GONE);
            if (mapUser.containsKey("info") && !TextUtils.isEmpty(mapUser.get("info"))) {
                user_about.setVisibility(VISIBLE);
                user_about.setText(mapUser.get("info"));
            } else user_about.setVisibility(GONE);

            setMapFollowSate();
            //点击事件
            auther_userImg.setOnClickListener(onClickListener);
            user_name.setOnClickListener(onClickListener);
            user_about.setOnClickListener(onClickListener);

        } else exp_user_rela.setVisibility(GONE);
    }

    /**
     * 用户点击跳转页面
     */
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), tongjiId, "用户点击", "头像点击量");
//            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), mapUser.get("url"),true);
        }
    };

    /**
     * 处理关注状态变化
     */
    private void setMapFollowSate() {
        //关注
        if (mapUser.containsKey("isFollow") && !"3".equals(mapUser.get("isFollow"))) {//1，未关注，2，已关注，3自己
            follow_rela.setVisibility(VISIBLE);
            if ("1".equals(mapUser.get("isFollow"))) {//未关注
                follow_rela.setBackgroundResource(R.drawable.bg_circle_red_5);
                follow_rela.setClickable(true);
                follow_tv.setText("关注");
                follow_tv.setTextColor(Color.parseColor("#fffffe"));
            } else {
                follow_rela.setBackgroundColor(Color.parseColor("#fffffe"));
                follow_rela.setClickable(false);
                follow_tv.setText("已关注");
                follow_tv.setTextColor(Color.parseColor("#333333"));
            }
        } else follow_rela.setVisibility(GONE);
    }
}
