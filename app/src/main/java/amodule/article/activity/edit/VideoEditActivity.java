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
    protected boolean canAddLink() {
        return false;
    }

    @Override
    protected boolean isEnableEditText() {
        return false;
    }

    @Override
    protected int getMaxImageCount() {
        return 0;
    }

    @Override
    protected int getMaxVideoCount() {
        return 1;
    }

    @Override
    protected int getMaxTextCount() {
        return 5000;
    }

    @Override
    protected int getMaxURLCount() {
        return 0;
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

    @Override
    protected String checkData() {
        if (TextUtils.isEmpty(editTitle.getText())) {
            return "标题不能为空";
        }
        boolean isHasVideo = mixLayout.hasVideo();
        if (!isHasVideo) {
            return "视频不能为空";
        }
        if(mixLayout.getTextCount() > getMaxTextCount()){
            return "文字不能超过" + getMaxTextCount() + "字";
        }
        return null;
    }
}
