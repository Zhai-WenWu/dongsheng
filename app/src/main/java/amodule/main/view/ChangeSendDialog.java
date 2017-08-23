package amodule.main.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import amodule.article.activity.edit.ArticleEidtActivity;
import amodule.article.activity.edit.VideoEditActivity;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.tools.DeviceUtilDialog;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.login.BindPhoneNum;
import amodule.user.activity.login.LoginByAccout;
import aplug.recordervideo.activity.RecorderActivity;
import aplug.recordervideo.tools.ToolsCammer;
import xh.windowview.XhDialog;

public class ChangeSendDialog extends Dialog {

    private Activity activity;
    protected View view;
    protected int height;

    private ImageView closeImage;
    private GridView mGridView;
    private List<Map<String, String>> list;

    public ChangeSendDialog(Activity activity) {
        super(activity, R.layout.a_main_change_send);
        this.activity = activity;
        this.getWindow().setBackgroundDrawableResource(R.color.c_white_transparent_EE);
        /* 无标题栏 */
        this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        Window dialogWindow = activity.getWindow();

		/* 设置为全屏 */
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        lp.height = WindowManager.LayoutParams.FLAG_FULLSCREEN;

        this.getWindow().setGravity(Gravity.BOTTOM);

        // view
        this.view = this.getLayoutInflater().inflate(R.layout.a_main_change_send, null);
        Display display = this.getWindow().getWindowManager().getDefaultDisplay();

        this.height = display.getHeight();
        this.addContentView(view, new LayoutParams(display.getWidth(), LayoutParams.WRAP_CONTENT));

        // 对话框设置监听
        this.setOnDismissListener(onDismissListener);
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
        findViewById(R.id.activityLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDialog();
            }
        });
        findViewById(R.id.close_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDialog();
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

    public void onClick(String tag) {
        if (TextUtils.isEmpty(tag)) return;
        switch (tag) {
            case "1": //发贴
                closeDialog();
                XHClick.mapStat(activity, "a_post_button", "发贴子", "");
                Intent subIntent = new Intent(activity, UploadSubjectNew.class);
                subIntent.putExtra("skip", true);
                activity.startActivity(subIntent);
                XHClick.track(activity, "发美食贴");
                break;
            case "2": //发菜谱
                if (!LoginManager.isLogin()) {
                    Intent intent = new Intent(activity, LoginByAccout.class);
                    activity.startActivity(intent);
                    return;
                }
                XHClick.mapStat(activity, "uploadDish", "uploadDish", "从导航发", 1);
                XHClick.mapStat(activity, "a_post_button", "发菜谱", "");
                closeDialog();
                Intent dishIntent = new Intent(activity, UploadDishActivity.class);
                activity.startActivity(dishIntent);
                XHClick.track(activity, "发菜谱");
                break;
//            case "3": //小视频
//                closeDialog();
//                if (LoginManager.canPublishShortVideo()) {
//                    XHClick.mapStat(activity, "a_post_button", "小视频", "");
//                    Intent smallVideo = new Intent(activity, MediaRecorderActivity.class);
//                    activity.startActivity(smallVideo);
//                    XHClick.track(activity, "发小视频贴");
//                } else {
//                    Tools.showToast(activity, "请绑定手机号");
//                    Intent bindPhone = new Intent(activity, LoginByAccout.class);
//                    bindPhone.putExtra("type", "bind");
//                    bindPhone.putExtra("title", "绑定手机号");
//                    activity.startActivity(bindPhone);
//                }
//                break;
            case "4": //录制菜谱
                DeviceUtilDialog deviceUtilDialog = new DeviceUtilDialog(activity);
                deviceUtilDialog.deviceStorageSpaceState(1024, 500, new DeviceUtilDialog.DeviceCallBack() {
                    @Override
                    public void backResultState(Boolean state) {
                        if (!state) {
                            if (ToolsCammer.checkSuporRecorder(true)) {
                                XHClick.mapStat(activity, "a_post_button", "录制菜谱", "");
                                Intent recoreVideo = new Intent(activity, RecorderActivity.class);
                                activity.startActivity(recoreVideo);
                                activity.finish();
                            } else {
                                final XhDialog xhDialog = new XhDialog(activity);
                                xhDialog.setTitle("您的设备不支持1080p视频拍摄！")
                                        .setSureButton("确定", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                activity.finish();
                                                xhDialog.cancel();
                                            }
                                        }).show();
                            }
                        } else {
                            activity.finish();
                        }
                    }
                });
                break;
            case "5": //发视频菜谱
                closeDialog();
                XHClick.mapStat(activity, "a_post_button", "发视频菜谱", "");
                Intent videoDish = new Intent(activity, UploadDishActivity.class);
                videoDish.putExtra(UploadDishActivity.DISH_TYPE_KEY, UploadDishActivity.DISH_TYPE_VIDEO);
                activity.startActivity(videoDish);
                break;
            case "6":
                closeDialog();
                if(!LoginManager.isLogin()) {
                    activity.startActivity(new Intent(activity, LoginByAccout.class));
                }else if (LoginManager.isBindMobilePhone())
                    activity.startActivity(new Intent(activity, ArticleEidtActivity.class));
                else
                    activity.startActivity(new Intent(activity, BindPhoneNum.class));
//                    showDialog("文章", StringManager.api_applyArticlePower);
                break;
            case "7":
                closeDialog();
                if(!LoginManager.isLogin()) {
                    activity.startActivity(new Intent(activity, LoginByAccout.class));
                }else if (LoginManager.isBindMobilePhone())
                    activity.startActivity(new Intent(activity, VideoEditActivity.class));
                else
                    activity.startActivity(new Intent(activity, BindPhoneNum.class));
//                    showDialog("短视频", StringManager.api_applyVideoPower);
                break;
        }
    }

    private void showDialog(final String text, final String url) {
        final XhDialog dialog = new XhDialog(activity);
        dialog.setTitle("暂无发布\"" + text + "\"的权限，是否申请发布权限？")
                .setSureButton("是", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        XHClick.mapStat(activity, "a_post_button", text, "权限弹框 - 是");
                        AppCommon.openUrl(activity, url, true);
                        dialog.cancel();
                    }
                })
                .setCanselButton("否", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        XHClick.mapStat(activity, "a_post_button", text, "权限弹框 - 否");
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void show() {
        super.show();
        Animation cycleAnim = AnimationUtils.loadAnimation(activity, R.anim.shake);
        mGridView.startAnimation(cycleAnim);

        final Animation scale_to_visibilty = AnimationUtils.loadAnimation(activity, R.anim.rotate_45);
        closeImage.setAnimation(scale_to_visibilty);
    }

    /**
     * 关闭dialog
     */
    public void closeDialog() {
        Animation cycleAnim = AnimationUtils.loadAnimation(activity, R.anim.drop);
        mGridView.startAnimation(cycleAnim);
        closeImage.clearAnimation();
        Animation scale_to_nothing = AnimationUtils.loadAnimation(activity, R.anim.rotate_ninus_45);
        closeImage.startAnimation(scale_to_nothing);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                ChangeSendDialog.this.dismiss();
                activity.overridePendingTransition(R.anim.in_from_nothing, R.anim.out_to_nothing);
            }
        }, 280);
    }


    private OnDismissListener onDismissListener = new OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            closeDialog();
        }
    };
}
