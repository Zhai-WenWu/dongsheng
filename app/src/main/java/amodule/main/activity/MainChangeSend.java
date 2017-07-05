package amodule.main.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.FileManager;
import amodule.article.activity.edit.ArticleEidtActiivty;
import amodule.article.activity.edit.VideoEditActivity;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.tools.DeviceUtilDialog;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.login.BindPhoneNum;
import amodule.user.activity.login.LoginByAccout;
import aplug.recordervideo.activity.RecorderActivity;
import aplug.recordervideo.tools.ToolsCammer;
import xh.windowview.XhDialog;

/**
 * Title:MainChangeSend.java
 * Copyright: Copyright (c) 2014~2017
 *
 * @author ruijiao_fang
 * @date 2014年11月10日
 */
@SuppressLint("CutPasteId")
public class MainChangeSend extends BaseActivity {

    private ImageView closeImage;
    private GridView mGridView;
    private List<Map<String, String>> list;

    public static Map<String, String> sendMap;
    public static Map<String, String> dishVideoMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        className = this.getComponentName().getClassName();
        setContentView(R.layout.a_main_change_send);
        FileManager.saveShared(this, FileManager.CIRCLE_HOME_SHOWDIALOG, FileManager.CIRCLE_HOME_SHOWDIALOG, "1");
        init();
    }

    private void init() {
        closeImage = (ImageView) findViewById(R.id.close_image);
        mGridView = (GridView) findViewById(R.id.change_send_gridview);
        initData();
        AdapterSimple adapter = new AdapterSimple(mGridView, list,
                R.layout.a_mian_change_send_item,
                new String[]{"name", "img"}, new int[]{R.id.change_send_gridview_item_name, R.id.change_send_gridview_item_iv}
        );
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                onClick(list.get(position).get("tag"));
            }
        });
    }

    private void initData() {
        int itemNum = 0;
        list = new ArrayList<>();
        if (LoginManager.isShowsendsubjectButton()) {
            addButton("1", R.drawable.pulish_subject, "晒美食");
            itemNum++;
        }
        if (LoginManager.isShowsendDishButton()) {
            addButton("2", R.drawable.send_dish, "写菜谱");
            itemNum++;
        }
        //删除true
        addButton("6", R.drawable.pulish_article, "发文章");
        itemNum++;
        addButton("7", R.drawable.pulish_video, "短视频");
        itemNum++;
//        if (LoginManager.isShowShortVideoButton()) {
//            addButton("3", R.drawable.pulish_video, "小视频");
//            itemNum++;
//        }

        mGridView.setNumColumns(itemNum > 3 ? 4 : itemNum);
    }

    private void addButton(String tag, int img, String name) {
        Map<String, String> uploadVideoDishMap = new HashMap<>();
        uploadVideoDishMap.put("tag", tag);
        uploadVideoDishMap.put("img", "ico" + img);
        uploadVideoDishMap.put("name", name);
        list.add(uploadVideoDishMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Animation cycleAnim = AnimationUtils.loadAnimation(this, R.anim.shake);
        mGridView.startAnimation(cycleAnim);

        final Animation scale_to_visibilty = AnimationUtils.loadAnimation(this, R.anim.rotate_45);
        closeImage.setAnimation(scale_to_visibilty);
    }

    public void onClick(String tag) {
        if (TextUtils.isEmpty(tag)) return;
        switch (tag) {
            case "1": //发贴
                finish();
                XHClick.mapStat(this, "a_post_button", "发贴子", "");
                Intent subIntent = new Intent(this, UploadSubjectNew.class);
                subIntent.putExtra("skip", true);
                startActivity(subIntent);
                XHClick.track(this, "发美食贴");
                break;
            case "2": //发菜谱
                finish();
                if (LoginManager.isLogin()) {
                    XHClick.mapStat(this, "uploadDish", "uploadDish", "从导航发", 1);
                    XHClick.mapStat(this, "a_post_button", "发菜谱", "");
                    Intent dishIntent = new Intent(this, UploadDishActivity.class);
                    this.startActivity(dishIntent);
                    XHClick.track(this, "发菜谱");
                } else {
                    Intent intent = new Intent(this, LoginByAccout.class);
                    startActivity(intent);
                }
                break;
//            case "3": //小视频
//                finish();
//                if (LoginManager.canPublishShortVideo()) {
//                    XHClick.mapStat(this, "a_post_button", "小视频", "");
//                    Intent smallVideo = new Intent(this, MediaRecorderActivity.class);
//                    this.startActivity(smallVideo);
//                    XHClick.track(this, "发小视频贴");
//                } else {
//                    Tools.showToast(this, "请绑定手机号");
//                    Intent bindPhone = new Intent(MainChangeSend.this, BindPhoneNum.class);
//                    startActivity(bindPhone);
//                }
//                break;
            case "4": //录制菜谱
                DeviceUtilDialog deviceUtilDialog = new DeviceUtilDialog(this);
                deviceUtilDialog.deviceStorageSpaceState(1024, 500, new DeviceUtilDialog.DeviceCallBack() {
                    @Override
                    public void backResultState(Boolean state) {
                        if (!state) {
                            if (ToolsCammer.checkSuporRecorder(true)) {
                                XHClick.mapStat(MainChangeSend.this, "a_post_button", "录制菜谱", "");
                                Intent recoreVideo = new Intent(MainChangeSend.this, RecorderActivity.class);
                                MainChangeSend.this.startActivity(recoreVideo);
                                MainChangeSend.this.finish();
                            } else {
                                final XhDialog xhDialog = new XhDialog(MainChangeSend.this);
                                xhDialog.setTitle("您的设备不支持1080p视频拍摄！")
                                        .setSureButton("确定", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                MainChangeSend.this.finish();
                                                xhDialog.cancel();
                                            }
                                        }).show();
                            }
                        } else {
                            MainChangeSend.this.finish();
                        }
                    }
                });
                break;
            case "5": //发视频菜谱
                finish();
                XHClick.mapStat(this, "a_post_button", "发视频菜谱", "");
                Intent videoDish = new Intent(this, UploadDishActivity.class);
                videoDish.putExtra(UploadDishActivity.DISH_TYPE_KEY, UploadDishActivity.DISH_TYPE_VIDEO);
                this.startActivity(videoDish);
                break;
            case "6":
                if (!LoginManager.isLogin()) {
                    finish();
                    startActivity(new Intent(this, LoginByAccout.class));
                } else if (LoginManager.isBindMobilePhone()) {
                    finish();
                    startActivity(new Intent(this, ArticleEidtActiivty.class));
                } else
                    startActivity(new Intent(this, BindPhoneNum.class));
//                    showDialog("发文章", StringManager.api_applyArticlePower);
                break;
            case "7":
                if (!LoginManager.isLogin()) {
                    finish();
                    startActivity(new Intent(this, LoginByAccout.class));
                } else if (LoginManager.isBindMobilePhone()) {
                    finish();
                    startActivity(new Intent(this, VideoEditActivity.class));
                } else
                    startActivity(new Intent(this, BindPhoneNum.class));
//                    showDialog("短视频", StringManager.api_applyVideoPower);
                break;
        }
    }

    private void showDialog(String text, final String url) {
        final XhDialog dialog = new XhDialog(this);
        dialog.setTitle("暂无发布\"" + text + "\"的权限，是否申请发布权限？")
                .setSureButton("是", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppCommon.openUrl(MainChangeSend.this, url, true);
                        dialog.cancel();
                        finish();
                    }
                })
                .setCanselButton("否", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        finish();
                    }
                })
                .show();
    }

    public void onCloseThis(View v) {
        Animation cycleAnim = AnimationUtils.loadAnimation(MainChangeSend.this, R.anim.drop);
        mGridView.startAnimation(cycleAnim);
        closeImage.clearAnimation();
        Animation scale_to_nothing = AnimationUtils.loadAnimation(this, R.anim.rotate_ninus_45);
        closeImage.startAnimation(scale_to_nothing);
        finish();
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    @Override
    public void finish() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainChangeSend.super.finish();
                overridePendingTransition(R.anim.in_from_nothing, R.anim.out_to_nothing);
            }
        }, 280);
    }
}
