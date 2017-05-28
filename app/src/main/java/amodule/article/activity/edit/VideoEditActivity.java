package amodule.article.activity.edit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import acore.override.XHApplication;
import acore.tools.Tools;
import amodule.article.activity.ArticleSelectActiivty;
import amodule.article.db.UploadVideoSQLite;

/**
 * Created by Fang Ruijiao on 2017/5/28.
 */

public class VideoEditActivity extends EditParentActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView("发视频");
        initData(new UploadVideoSQLite(XHApplication.in().getApplicationContext()));
    }

    @Override
    public void onNextSetp() {
        String checkStr = checkData();
        if (TextUtils.isEmpty(checkStr)) {
            saveDraft();
            timer.cancel();
            Intent intent = new Intent(this, ArticleSelectActiivty.class);
            intent.putExtra("draftId", uploadArticleData.getId());
            intent.putExtra("dataType", EditParentActivity.TYPE_VIDEO);
            startActivity(intent);
            finish();
        } else {
            Tools.showToast(this, checkStr);
        }
    }
}
