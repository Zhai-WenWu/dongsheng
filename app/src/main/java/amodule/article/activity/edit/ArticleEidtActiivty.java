package amodule.article.activity.edit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import acore.override.XHApplication;
import acore.tools.Tools;
import amodule.article.activity.ArticleSelectActiivty;
import amodule.article.db.UploadArticleSQLite;

/**
 * PackageName : amodule.article.activity
 * Created by MrTrying on 2017/5/19 09:19.
 * E_mail : ztanzeyu@gmail.com
 */
public class ArticleEidtActiivty extends EditParentActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView("发文章");
        initData(new UploadArticleSQLite(XHApplication.in().getApplicationContext()));
    }

    @Override
    public void onNextSetp() {
        String checkStr = checkData();
        if (TextUtils.isEmpty(checkStr)) {
            saveDraft();
            timer.cancel();
            Intent intent = new Intent(this, ArticleSelectActiivty.class);
            intent.putExtra("draftId", uploadArticleData.getId());
            intent.putExtra("dataType", EditParentActivity.TYPE_ARTICLE);
            startActivity(intent);
            finish();
        } else {
            Tools.showToast(this, checkStr);
        }
    }
}
