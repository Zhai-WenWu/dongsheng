package amodule.article.db;

import android.content.Context;


/**
 * Created by XiangHa on 2017/5/22.
 */
public class UploadArticleSQLite extends UploadParentSQLite {
    
    public UploadArticleSQLite(Context context) {
        super(context, TB_NAME,1);
    }

    private static final String TB_NAME = "tb_uploadAriticle";
}
