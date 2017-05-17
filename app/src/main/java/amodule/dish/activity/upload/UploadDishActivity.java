package amodule.dish.activity.upload;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.lansosdk.videoeditor.LoadLanSongSdk;
import com.xiangha.R;

import java.lang.ref.WeakReference;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import amodule.dish.db.UploadDishData;
import amodule.dish.tools.upload.UploadDishNormalControl;
import amodule.dish.tools.upload.UploadDishParrentControl;
import amodule.dish.tools.upload.UploadDishVideoControl;
import amodule.main.Main;
import amodule.user.activity.MyDraft;

/**
 * Created by Fang Ruijiao on 2016/10/25.
 */
public class UploadDishActivity extends BaseActivity implements View.OnClickListener{
    /** 发菜谱页面统计ID */
    public static String STATISTICS_ID = "a_write_recipes";
    /** 调整菜谱步骤顺序的次数 */
    public static String STATISTICS_MODIFY_MAKE_ID = "a_dish_upload_make_move";
    public static final String DISH_TYPE_KEY = "type";
    public static final String DISH_TYPE_NORMAL = "normal";
    public static final String DISH_TYPE_VIDEO = "video";

    /**新菜谱 */
    public static final String UPLOAD_DISH_NEW = "1";
    /**活动菜谱 */
    public static final String UPLOAD_DISH_ACTIVITY = "2";
    /**编辑菜谱 */
    public static final String UPLOAD_DISH_EDIT = "3";
    /**草稿菜谱 */
    public static final String UPLOAD_DISH_DRAFT = "4";

    /**添加做法(图片)*/
    public static final int DISH_ADD_MAKE = 2006;
    /**上传单图 */
    public static final int DISH_CHOOSE_SINGLE_IMG = 2008;
    /**从草稿界面进来*/
    public static final int DISH_DRAFT_IN = 2010;
    /**从调整步骤界面进来*/
    public static final int DISH_MAKE_ITEM_OPTION= 2011;
    /**批量添加做法(图片)*/
    public static final int DISH_ADD_MAKE_MAX = 2012;

    //弱引用，在其他界面关闭UploadDishActivity
    public static WeakReference<Activity> uploaDishWeakRef;

    private UploadDishParrentControl uploadDishParrentControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sufureView页面闪烁
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        String dishType = getIntent().getStringExtra(DISH_TYPE_KEY);
        if(TextUtils.isEmpty(dishType) || dishType.equals(DISH_TYPE_NORMAL)){
            uploadDishParrentControl = new UploadDishNormalControl(this);
        }else if(dishType.equals(DISH_TYPE_VIDEO)){
            STATISTICS_ID = "a_write_dishvideo";
            STATISTICS_MODIFY_MAKE_ID = "a_write_dishvideo";
            uploadDishParrentControl = new UploadDishVideoControl(this);
        }
        uploaDishWeakRef = new WeakReference<Activity>(this);
        LoadLanSongSdk.initVideoSdk(getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.a_dish_upload_go_draft_btn:
                XHClick.mapStat(this, UploadDishActivity.STATISTICS_ID, "点击去“草稿箱”", "");
                Intent it = new Intent(this,MyDraft.class);
                int id = uploadDishParrentControl.onSaveDraft(UploadDishData.UPLOAD_DRAF);
                it.putExtra("id", id);
                startActivity(it);
                this.onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(uploadDishParrentControl != null){
            uploadDishParrentControl.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (level < Main.colse_level)
            if (uploadDishParrentControl != null) {
                uploadDishParrentControl.onPause();
            }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(uploadDishParrentControl != null){
            uploadDishParrentControl.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if(uploadDishParrentControl != null){
            uploadDishParrentControl.onDestroy();
        }
        super.onDestroy();
    }
    /**
     * 根据不同的requestCode，将text数据交给不同的组件控制类的结果处理方法
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data == null || uploadDishParrentControl==null)
            return;
        if(uploadDishParrentControl != null){
            uploadDishParrentControl.onActivityResult(requestCode,resultCode,data);
        }
    }
}
