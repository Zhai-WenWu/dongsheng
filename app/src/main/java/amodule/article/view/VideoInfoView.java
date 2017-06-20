package amodule.article.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.LoginManager;
import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import amodule.article.activity.ReportActivity;
import amodule.article.activity.VideoDetailActivity;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/6/19 15:56.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoInfoView extends ItemBaseView {
    private TextView title, content, playCount, commetnCount, report, dateText;
    private ImageView arrow;

    private String contentText = "";

    public VideoInfoView(Context context) {
        super(context, R.layout.v_video_info_view_layout);
    }

    public VideoInfoView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.v_video_info_view_layout);
    }

    public VideoInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.v_video_info_view_layout);
    }

    @Override
    public void init() {
        super.init();
        arrow = (ImageView) findViewById(R.id.content_arrow);
        title = (TextView) findViewById(R.id.title);
        content = (TextView) findViewById(R.id.content);
        playCount = (TextView) findViewById(R.id.play_count);
        commetnCount = (TextView) findViewById(R.id.comment_count);
        report = (TextView) findViewById(R.id.report);
        dateText = (TextView) findViewById(R.id.date_text);

        arrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrow.isSelected()) {
                    content.setVisibility(GONE);
                    arrow.setSelected(false);
                } else {
                    content.setVisibility(VISIBLE);
                    arrow.setSelected(true);
                }
            }
        });
    }

    public void setData(final Map<String, String> videoMap) {
        if (videoMap == null) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);

        contentText = videoMap.get("content");
        if(!TextUtils.isEmpty(contentText) && !"[]".equals(content)){
            arrow.setVisibility(VISIBLE);
            content.setText(contentText);
        }else arrow.setVisibility(GONE);
        setViewText(title, videoMap, "title");
        setViewTextWithSuffix(playCount, videoMap, "clickAll", "播放");
        setViewTextWithSuffix(commetnCount, videoMap, "commentNumber", "评论");
        setViewText(dateText, videoMap, "addTime");

        final Map<String, String> customerData = StringManager.getFirstMap(videoMap.get("customer"));
        final String userCode = customerData.get("code");
        final boolean isAuthor = LoginManager.isLogin()
                && !TextUtils.isEmpty(LoginManager.userInfo.get("code"))
                && !TextUtils.isEmpty(userCode)
                && userCode.equals(LoginManager.userInfo.get("code"));
        findViewById(R.id.report_layout).setVisibility(isAuthor ? GONE : VISIBLE);
        report.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ReportActivity.class);
                intent.putExtra("code", videoMap.get("code"));
                intent.putExtra("type", mCurrType);
                intent.putExtra("userCode", userCode);
                intent.putExtra("reportName", customerData.get("nickName"));
                intent.putExtra("reportContent", videoMap.get("title"));
                intent.putExtra("reportType", "1");
                getContext().startActivity(intent);
            }
        });
    }

    private String mCurrType = "";
    public void setType(String type) {
        mCurrType = type;
    }

}
