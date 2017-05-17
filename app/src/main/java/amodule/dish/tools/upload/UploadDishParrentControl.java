package amodule.dish.tools.upload;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Timer;
import java.util.TimerTask;

import acore.tools.Tools;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.db.UploadDishData;
import amodule.dish.db.UploadDishSqlite;
import amodule.dish.tools.UploadDishSpeechTools;

/**
 * Created by Fang Ruijiao on 2016/10/25.
 */
public abstract class UploadDishParrentControl {

    public final int uploadDishNew = 1;
    public final int uploadDishActivity = 2;
    public final int uploadDishEdit = 3;
    public final int uploadDishDraft = 4;

    protected LayoutInflater inflater;
    protected UploadDishActivity mAct;

    protected UploadDishData uploadDishData = null;
    /** 定时存草稿*/
    protected Timer timer;
    private int taskTime = 30 * 1000;
    private int timeSaveNum = 1;

    /**标记当前是否为编辑状态*/
    protected boolean isUploadDishEdit = false;
    /**标记是否可发布*/
    protected boolean ifCanUpload = true;

    /**界面标题*/
    private String mTitleName;

    public UploadDishParrentControl(UploadDishActivity act, int contentXml,String modifyTitle){
        mAct = act;
        mTitleName = mAct.getIntent().getStringExtra("titleName");
        if(TextUtils.isEmpty(mTitleName))mTitleName = modifyTitle;
        inflater = LayoutInflater.from(mAct);
        mAct.initActivity("", 6, 0, 0, R.layout.a_dish_upload_new_layout);
        View viewParent = inflater.inflate(contentXml, null);
        initTitle(viewParent);
        RelativeLayout relativeLayout = (RelativeLayout) mAct.findViewById(R.id.activityLayout);
        relativeLayout.addView(viewParent);

        TextView titleView = (TextView) mAct.findViewById(R.id.title);
        titleView.setText(mTitleName);
        mAct.findViewById(R.id.rightText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isUploadDishEdit){//除编辑菜谱，其他操作点返回均保存草稿
                    int value = onSaveDraft(UploadDishData.UPLOAD_DRAF);
                    if (value > 0)
                        Tools.showToast(mAct.getApplicationContext(), "已保存该菜谱为草稿");
                }
            }
        });
        mAct.findViewById(R.id.ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mAct.loadManager.setLoading(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
            }
        });
    }

    protected void initTitle(View view) {
        if(Tools.isShowTitle()) {
            mAct.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        if(Tools.isShowTitle()) {
            int dp_45 = Tools.getDimen(mAct, R.dimen.dp_45);
            int height = dp_45 + Tools.getStatusBarHeight(mAct);

            RelativeLayout bar_title = (RelativeLayout)view.findViewById(R.id.title_all_rela);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
            bar_title.setLayoutParams(layout);
            bar_title.setPadding(0, Tools.getStatusBarHeight(mAct), 0, 0);
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //草稿id
        int draftId = mAct.getIntent().getIntExtra("id",0);
        //网上菜谱code
        String code=mAct.getIntent().getStringExtra("code");
        //菜谱名字
        String name=mAct.getIntent().getStringExtra("name");
        //活动id
        String activityId=mAct.getIntent().getStringExtra("activityId");
        //名字中要替换的部分
        String removeName=mAct.getIntent().getStringExtra("removeName");
        String stateStr = mAct.getIntent().getStringExtra("state");

        if(stateStr == null)
            stateStr = UploadDishActivity.UPLOAD_DISH_NEW;
        int state = Integer.parseInt(stateStr);
        if(state == uploadDishEdit){
            UploadDishSqlite mDishSqlite=new UploadDishSqlite(mAct.getApplicationContext());
            draftId = mDishSqlite.selectIdByCode(code);
            if(draftId > 0){
                state = uploadDishDraft;
            }
        }
        switch(state){
            case uploadDishNew:
                uploadDishData = new UploadDishData();
                initView();
                break;
            case uploadDishActivity:
                uploadDishData = new UploadDishData();
                uploadDishData.setName(name);
                uploadDishData.setActivityId(activityId);
                uploadDishData.setRemoveName(removeName);
                initView();
                break;
            case uploadDishEdit:
                isUploadDishEdit = true;
                initNetDishView(code);
                break;
            case uploadDishDraft:
                initDraftDishView(draftId);
                break;
        }
    }

    private void timingSave(){
        final Handler handler = new Handler();
        TimerTask tt = new TimerTask() {

            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        int value = onSaveDraft(UploadDishData.UPLOAD_DRAF);
                        if (value > 0 && timeSaveNum % 2 == 1)
                            Tools.showToast(mAct.getApplicationContext(), "已保存该菜谱为草稿");
                        timeSaveNum ++;
                    }
                });
            }
        };
        timer.schedule(tt, taskTime, taskTime);
    }

    protected abstract void initView();
    /**
     * 从数据库读取草稿数据并回调initView
     */
    protected abstract void initDraftDishView(final int id);
    /**
     * 获取网络菜谱数据并回调initView
     */
    protected abstract void initNetDishView(String dishCode);
    /**
     * @param subjectType
     * @return : > 0 报错成功 = -3 报错失败 !=-3内容为空
     */
    public abstract int onSaveDraft(String subjectType);

    protected abstract void onClickUploadDish();

    public void onResume() {
        timer = new Timer();
        timingSave();
    }

    public void onPause() {
        onSaveDraft(UploadDishData.UPLOAD_DRAF);
        timer.cancel();
    }

    public void onBackPressed() {
        if(!isUploadDishEdit){//除编辑菜谱，其他操作点返回均保存草稿
            int value = onSaveDraft(UploadDishData.UPLOAD_DRAF);
            if (value > 0)
                Tools.showToast(mAct.getApplicationContext(), "已保存该菜谱为草稿");
        }
        mAct.finish();
    }

    public void onDestroy() {
        timer.cancel();
        UploadDishSpeechTools.createUploadDishSpeechTools().onDestroy();
    }
    /**
     * 根据不同的requestCode，将text数据交给不同的组件控制类的结果处理方法
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {}
}
