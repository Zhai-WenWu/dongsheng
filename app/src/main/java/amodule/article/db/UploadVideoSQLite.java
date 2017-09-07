package amodule.article.db;

import android.content.Context;


/**
 * Created by XiangHa on 2017/5/22.
 */
public class UploadVideoSQLite extends UploadParentSQLite {

    private static final String TB_NAME = "tb_uploadVideo";

    public UploadVideoSQLite(Context context) {
        super(context, TB_NAME,1);
    }

}
