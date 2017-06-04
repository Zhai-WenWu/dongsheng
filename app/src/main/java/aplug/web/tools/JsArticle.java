package aplug.web.tools;

import android.app.Activity;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.google.gson.annotations.JsonAdapter;

import amodule.article.activity.ReportActivity;
import amodule.other.activity.PlayVideo;

/**
 * PackageName : aplug.web.tools
 * Created by MrTrying on 2017/6/4 10:27.
 * E_mail : ztanzeyu@gmail.com
 */

public class JsArticle extends JsBase {
    public Activity mAct;

    public JsArticle(Activity act){
        this.mAct = act;
        TAG = "article";
    }

    @JavascriptInterface
    public void doArticleReport(final String type,final String code,final String userCode,final String reportName,final String reportContent){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mAct, ReportActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("code", code);
                intent.putExtra("usercode", userCode);
                intent.putExtra("reportName", reportName);
                intent.putExtra("reportContent", reportContent);
                intent.putExtra("reportType", "1");
                mAct.startActivity(intent);
            }
        });
    }

    @JavascriptInterface
    public void doPlayVideo(final String name ,final String videoUrl,final String imgUrl){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mAct, PlayVideo.class);
                intent.putExtra("name",name);
                intent.putExtra("url",videoUrl);
                intent.putExtra("img",imgUrl);
                mAct.startActivity(intent);
            }
        });
    }

    @JavascriptInterface
    public void doShowImages(final String[] imageUrls,final int index){
        handler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

}
