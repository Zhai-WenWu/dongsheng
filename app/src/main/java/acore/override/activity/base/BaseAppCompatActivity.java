package acore.override.activity.base;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.popdialog.GoodCommentDialogControl;
import com.popdialog.util.GoodCommentManager;
import com.xiangha.R;

import acore.logic.ActivityMethodManager;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.notification.controller.NotificationSettingController;
import acore.override.XHApplication;
import acore.tools.LogManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.PopWindowDialog;
import acore.widget.UploadFailPopWindowDialog;
import acore.widget.UploadNetChangeWindowDialog;
import acore.widget.UploadSuccessPopWindowDialog;
import amodule.main.view.CommonBottomView;
import amodule.main.view.CommonBottonControl;
import third.share.BarShare;

import static acore.tools.Tools.getApiSurTime;
import static android.content.Intent.FLAG_ACTIVITY_NO_USER_ACTION;

/**
 * PackageName : acore.override.activity.base
 * Created by MrTrying on 2017/7/26 15:26.
 * E_mail : ztanzeyu@gmail.com
 */

public class BaseAppCompatActivity extends AppCompatActivity {
    public RelativeLayout rl;
    protected int level = 5;
    public BarShare barShare;
    public LoadManager loadManager = null;
    public boolean keyBoard_visible = false;
    protected ActivityMethodManager mActMagager;

    private BaseActivity.OnKeyBoardListener mOnKeyBoardListener;

    public CommonBottomView mCommonBottomView;
    public String className;
    public CommonBottonControl control;

    public PopWindowDialog mFavePopWindowDialog = null;
    public static PopWindowDialog mUpDishPopWindowDialog = null;
    public static UploadSuccessPopWindowDialog mUploadDishVideoSuccess = null;
    public static UploadFailPopWindowDialog mUploadDishVideoFail = null;
    public static UploadNetChangeWindowDialog mUploadNetChangeWindowDialog = null;

    private long homebackTime;
    private boolean isForeground = true;

    private long resumeTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActMagager = new ActivityMethodManager(this);

        Intent intent = getIntent();
        int i = intent.getIntExtra(XHClick.KEY_NOTIFY_CLICK, 0);
        if (i == XHClick.VALUE_NOTIFY_CLICK) {
            XHClick.statisticsNotifyClick(intent);
        }
    }

    /**
     * Activity标题初始化
     *
     * @param title       ：标题
     * @param level       ：等级
     * @param color       :背景色
     * @param barTitleXml ：标题xml
     * @param contentXml  ：主内容xml
     */
    public void initActivity(String title, int level, int color, int barTitleXml, int contentXml) {
        initActivity(title, level, color, barTitleXml, contentXml, R.color.common_bg);
    }

    public void initActivity(String title, int level, int color, int barTitleXml, int contentXml, int contentBgResId) {
        this.level = level;
        className = this.getComponentName().getClassName();

        control = new CommonBottonControl();
        if (barTitleXml > 0) {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE); // 声明使用自定义标题
            View view_all = LayoutInflater.from(this).inflate(R.layout.a_all, null);
            RelativeLayout all_title = (RelativeLayout) view_all.findViewById(R.id.all_title);
            RelativeLayout all_content = (RelativeLayout) view_all.findViewById(R.id.all_content);
            all_content.setBackgroundResource(contentBgResId);
            all_content.addView(control.setCommonBottonView(className, this, contentXml));
            View view_title = LayoutInflater.from(this).inflate(barTitleXml, null);
//			if(Tools.isShowTitle()&&showStateColor(className)) {
//				int topbarHeight = Tools.getDimen(this, R.dimen.topbar_height);
//				int height = topbarHeight + Tools.getStatusBarHeight(this);
//				RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
//				all_title.setLayoutParams(layout);
//				all_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
//			}
            all_title.addView(view_title);
            setContentView(view_all);

            mCommonBottomView = control.mCommonBottomView;
            if (!TextUtils.isEmpty(title)) {
                TextView titleV = (TextView) findViewById(R.id.title);
                titleV.setMaxWidth(ToolsDevice.getWindowPx(this).widthPixels - ToolsDevice.dp2px(this, 45 + 40));
                titleV.setText(title);
            }
            if (color > 0)
                findViewById(R.id.barTitle).setBackgroundResource(color);
            // 设置返回按钮;
            View btn_back = findViewById(R.id.ll_back);
            ImageView img_back = (ImageView) findViewById(R.id.leftImgBtn);
            TextView tv_back = (TextView) findViewById(R.id.leftText);
            View.OnClickListener backClick = getBackBtnAction();
            if (tv_back != null) {
                tv_back.setClickable(true);
                tv_back.setOnClickListener(backClick);
                tv_back.setVisibility(View.INVISIBLE);
            }
            if (btn_back != null) {
                btn_back.setClickable(true);
                btn_back.setOnClickListener(backClick);
                btn_back.setVisibility(View.VISIBLE);
            }
            if (img_back != null) {
                img_back.setClickable(true);
                img_back.setOnClickListener(backClick);
                img_back.setVisibility(View.VISIBLE);
            }
        } else {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(control.setCommonBottonView(className, this, contentXml));
            mCommonBottomView = control.mCommonBottomView;
        }
        if (Tools.isShowTitle() && showStateColor(className)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        String colors = Tools.getColorStr(this, R.color.common_top_bg);
        Tools.setStatusBarColor(this, Color.parseColor(colors));
        setCommonStyle();
    }

    public View.OnClickListener getBackBtnAction() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseAppCompatActivity.this.onBackPressed();
            }
        };
    }

    /**
     * 设置公用属性
     */
    public void setCommonStyle() {
        // 设置样式
        rl = (RelativeLayout) findViewById(R.id.activityLayout);
        if (rl != null) {
            loadManager = new LoadManager(this, rl);
            //设置压缩模式下键盘的监听
            rl.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                private int preHeight = 0;

                @Override
                public void onGlobalLayout() {
                    int heightDiff = rl.getRootView().getHeight() - rl.getHeight();
                    if (preHeight == heightDiff) {
                        return;
                    }
                    preHeight = heightDiff;
                    if (heightDiff > (ToolsDevice.getWindowPx(BaseAppCompatActivity.this).heightPixels / 4)) {
                        if (!keyBoard_visible) {
                            keyBoard_visible = true;
                            if (mOnKeyBoardListener != null) mOnKeyBoardListener.show();
                        }
                    } else {
                        if (keyBoard_visible) {
                            keyBoard_visible = false;
                            if (mOnKeyBoardListener != null) mOnKeyBoardListener.hint();
                        }
                    }
                }
            });
        }
    }

    private boolean showStateColor(String name) {
        if (name.equals("amodule.quan.activity.ShowSubject") || name.equals("amodule.quan.activity.upload.UploadSubjectNew")) {
            return false;
        }
        return true;
    }

    public void setOnKeyBoardListener(BaseActivity.OnKeyBoardListener onKeyBoardListener) {
        mOnKeyBoardListener = onKeyBoardListener;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mActMagager != null){
            mActMagager.onRestart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeTime = System.currentTimeMillis();
        //Log.i("FRJ", "className:::" + className);
        if (XHApplication.in() == null) {
            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            return;
        }
        if (!isForeground) {
            long newHomebackTime = System.currentTimeMillis();
            getApiSurTime("homeback", homebackTime, newHomebackTime);
        }
        isForeground = true;

        if(mActMagager != null)
            mActMagager.onResume(level);
        if (mCommonBottomView != null)
            CommonBottomView.BottomViewBuilder.getInstance().refresh(mCommonBottomView);

        if (mFavePopWindowDialog != null && mFavePopWindowDialog.isHasShow()) {
            mFavePopWindowDialog.onResume();
        }
        if (mUpDishPopWindowDialog != null && mUpDishPopWindowDialog.isHasShow()) {
            mUpDishPopWindowDialog.onResume();
        }
        if (mUploadDishVideoSuccess != null && mUploadDishVideoSuccess.isHasShow()) {
            mUploadDishVideoSuccess.onResume();
        }
        if (mUploadDishVideoFail != null && mUploadDishVideoFail.isHasShow()) {
            mUploadDishVideoFail.onResume();
        }
        if (mUploadNetChangeWindowDialog != null && mUploadNetChangeWindowDialog.isHasShow()) {
            mUploadNetChangeWindowDialog.onResume();
        }
        //好评统计
        GoodCommentManager.setStictis(BaseAppCompatActivity.this, new GoodCommentDialogControl.OnCommentTimeStatisticsCallback() {
            @Override
            public void onStatistics(String typeStr, String timeStr) {
                XHClick.mapStat(BaseAppCompatActivity.this, "a_evaluate420", typeStr, timeStr);
            }
        });

//		Log.e("activityName",getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mActMagager != null){
            mActMagager.onPause();
        }
        if (mFavePopWindowDialog != null && mFavePopWindowDialog.isHasShow()) {
            mFavePopWindowDialog.onPause();
        }
        if (mUpDishPopWindowDialog != null && mUpDishPopWindowDialog.isHasShow()) {
            mUpDishPopWindowDialog.onPause();
        }

        if (mUploadDishVideoSuccess != null && mUploadDishVideoSuccess.isHasShow()) {
            mUploadDishVideoSuccess.onPause();
        }
        if (mUploadDishVideoFail != null && mUploadDishVideoFail.isHasShow()) {
            mUploadDishVideoFail.onPause();
        }
        if (mUploadNetChangeWindowDialog != null && mUploadNetChangeWindowDialog.isHasShow()) {
            mUploadNetChangeWindowDialog.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!Tools.isAppOnForeground()) {
            isForeground = false;
            homebackTime = System.currentTimeMillis();
        }
        if (mActMagager != null){
            mActMagager.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Util.isOnMainThread()) {
            Glide.get(XHApplication.in()).clearMemory();
            LogManager.print("d", "***********Glide is already clearMemory...");
        }
        if(mActMagager != null){
            mActMagager.onDestroy();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if(mActMagager != null){
            mActMagager.onUserLeaveHint();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
//        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onBackPressed() {
        boolean isShowPop = false;
        if (mFavePopWindowDialog != null && mFavePopWindowDialog.isHasShow()) {
            mFavePopWindowDialog.closePopWindowDialog();
            mFavePopWindowDialog = null;
            isShowPop = true;
        }
        if (mUpDishPopWindowDialog != null && mUpDishPopWindowDialog.isHasShow()) {
            mUpDishPopWindowDialog.closePopWindowDialog();
            mUpDishPopWindowDialog = null;
            isShowPop = true;
        }

        if (mUploadDishVideoSuccess != null && mUploadDishVideoSuccess.isHasShow()) {
            mUploadDishVideoSuccess.closePopWindowDialog();
            mUploadDishVideoSuccess = null;
            isShowPop = true;
        }
        if (mUploadDishVideoFail != null && mUploadDishVideoFail.isHasShow()) {
            mUploadDishVideoFail.closePopWindowDialog();
            mUploadDishVideoFail = null;
            isShowPop = true;
        }

        if (mUploadNetChangeWindowDialog != null && mUploadNetChangeWindowDialog.isHasShow()) {
            mUploadNetChangeWindowDialog.closePopWindowDialog();
            mUploadNetChangeWindowDialog = null;
            isShowPop = true;
        }
        if (!isShowPop) {
            super.onBackPressed();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        intent.addFlags(FLAG_ACTIVITY_NO_USER_ACTION);
        super.startActivity(intent);
        // 设置切换动画，从右边进入，左边退出
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent.addFlags(FLAG_ACTIVITY_NO_USER_ACTION);
        super.startActivityForResult(intent, requestCode);
        // 设置切换动画，从右边进入，左边退出
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    public interface OnKeyBoardListener {
        public void show();

        public void hint();
    }

    public ActivityMethodManager getActMagager() {
        return mActMagager;
    }
}
