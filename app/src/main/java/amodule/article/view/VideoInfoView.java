package amodule.article.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import amodule.article.activity.ReportActivity;
import amodule.article.activity.VideoDetailActivity;
import amodule.article.view.richtext.RichParser;

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
                    arrow.setImageResource(R.drawable.arrow_down);
                } else {
                    content.setVisibility(VISIBLE);
                    arrow.setSelected(true);
                    arrow.setImageResource(R.drawable.arrow_up);
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

        StringBuilder stringBuilder = new StringBuilder();
        String rawStr = videoMap.get("raw");
        List<Map<String,String>> array = StringManager.getListMapByJson(rawStr);
        for(Map<String,String> map:array){
            if("text".equals(map.get("type"))){
                stringBuilder.append(map.get("html"));
            }
        }
        contentText = RichParser.fromHtml(stringBuilder.toString()).toString().trim();
        Log.i("tzy","contentText = " +contentText);
//        contentText = "两年半之前，在一个由四个人组成的 Android 团队的帮助下，我开始从后端开发转向移动开发。一年之后，我加入了一个已经完成了B轮融资的初创公司，在那里主要做 Android 开发的工作。在一个小团队里工作，既能很好地保持独立，还不耽误向同事学习。";
        if(!TextUtils.isEmpty(contentText)){
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
