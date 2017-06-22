package amodule.article.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import amodule.article.activity.ArticleDetailActivity;
import amodule.main.Main;
import amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver;
import amodule.user.activity.FriendHome;
import amodule.user.activity.login.LoginByAccout;

import static amodule.article.activity.ArticleDetailActivity.TYPE_ARTICLE;
import static amodule.dish.activity.DetailDish.tongjiId;

/**
 * 文章header头view
 */
public class ArticleHeaderView extends ItemBaseView {
    private TextView acticle_title;
    private CustomerView customerView;
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
        customerView = (CustomerView) findViewById(R.id.customer_view);
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
            mapUser = StringManager.getFirstMap(map.get("customer"));
            customerView.setType(mCurrType);
            customerView.setData(mapUser);
        }
    }

    private String mCurrType;
    public void setType(String type) {
        mCurrType = type;
    }

}
