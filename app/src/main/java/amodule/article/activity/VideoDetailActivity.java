package amodule.article.activity;


import acore.tools.StringManager;
import amodule.article.activity.edit.VideoEditActivity;

/**
 * PackageName : amodule.article.activity
 * Created by MrTrying on 2017/5/31 20:01.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoDetailActivity extends ArticleDetailActivity {

    @Override
    public String getType() {
        return TYPE_VIDEO;
    }

    @Override
    public String getMAPI() {
        return StringManager.replaceUrl(StringManager.api_Video);
    }

    @Override
    public String getTitleText() {
        return "视频详情页";
    }

    @Override
    public String getInfoAPI() {
        return StringManager.api_getVideoInfo;
    }

    @Override
    public String getRelatedAPI() {
        return StringManager.api_getVideoRelated;
    }

    @Override
    public String getPraiseAPI() {
        return StringManager.api_likeVideo;
    }

    @Override
    public Class<?> getIntentClass() {
        return VideoEditActivity.class;
    }
}
