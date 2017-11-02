package amodule.dish.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import acore.logic.XHClick;
import acore.tools.Tools;
import amodule.dish.BrocastReceiver.BatteryBrocastReceiver;
import amodule.dish.view.TitleProgressView;
import aplug.recordervideo.tools.AudioTools;

/**
 * 设备状态判断UI和控制类
 * 1、判断当前手机存储空间
 * 2、判断当前电量
 * 3、判断当前拍摄设备的比例
 * dialog业务设计：目前弹出dialog 类型为两种，其中一个默认点击关闭当前activity,后一个关闭dialog即可，
 * 扩展接口回调实现
 */
public class DeviceUtilDialog {
    private Activity activity;
    private BatteryBrocastReceiver batteryBrocastReceiver;

    public DeviceUtilDialog(Activity activity) {
        this.activity = activity;
    }

    /**
     * 判断当前设备存储空间是否满足要求
     *
     * @param maxValue       -为0时不进行判断的
     * @param minValue       -为0时不进行判断的
     * @param deviceCallBack 回调
     */
    public void deviceStorageSpaceState(int maxValue, int minValue, DeviceCallBack deviceCallBack) {
        String showInfo = "";
        String btnMsg1 = "";
        String btnMsg2 = "";
        long availableSize = Tools.getSDCardAvailableSize();

        if (maxValue > 0 && minValue > 0) {
            if (availableSize >= minValue && availableSize < maxValue) {
                showInfo = "您的手机存储空间可能不足，需要至少1GB的存储空间~";
                btnMsg1 = "我知道了";
                btnMsg2 = "继续拍摄";
                playAlert();
                showDialog(activity, showInfo,
                        btnMsg1, btnMsg2, false, deviceCallBack);
            } else if (availableSize < minValue) {
                showInfo = "您的手机存储空间不足，无法拍摄！需要至少1GB的存储空间~";
                btnMsg1 = "我知道了";
                playAlert();
                showDialog(activity, showInfo,
                        btnMsg1, deviceCallBack);
            }else{
                deviceCallBack.backResultState(false);
            }
        } else if (minValue > 0) {
            if (availableSize < minValue) {
                showInfo = "您的手机存储空间不足，无法拍摄！需要至少1GB的存储空间~";
                btnMsg1 = "我知道了";
                playAlert();
                showDialog(activity, showInfo,
                        btnMsg1, deviceCallBack);
            }else{
                deviceCallBack.backResultState(false);
            }
        } else {
            Log.e("DeviceUtilDialog ", "参数错误");
        }
    }

    /**
     * 判断当前设备电量状态
     * 电量不小于15%
     *
     * @param deviceCallBack 回调
     */
    public void deviceLowState(final DeviceCallBack deviceCallBack) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryBrocastReceiver = new BatteryBrocastReceiver(
                new BatteryBrocastReceiver.BatteryBrocastReceiverCallback() {
                    @Override
                    public void onGetBatterylevel(int level) {
                        if (level < 15) {
                            XHClick.mapStat(activity,"a_video_splice","电量不足提示","");
                            String showInfo = "手机电量不足，请及时充电~";
                            String btnMsg1 = "我知道了";
                            showDialog(activity, showInfo,
                                    btnMsg1, deviceCallBack);
                        }else{
                            deviceCallBack.backResultState(false);
                        }
                        registerBatteryRecoeiver();
                    }
                });
        activity.registerReceiver(batteryBrocastReceiver, filter);
    }

    public void registerBatteryRecoeiver() {
        if (batteryBrocastReceiver != null) {
            activity.unregisterReceiver(batteryBrocastReceiver);
            batteryBrocastReceiver = null;
        }
    }


    /**
     * 判断设备是否有满足拍摄条件
     * 有16:9的拍摄比例
     *
     * @param isFacingFont   正面摄像头  背面摄像头
     * @param deviceCallBack 回调
     */
    public void deviceShootState(boolean isFacingFont, DeviceCallBack deviceCallBack) {
        boolean isSupport = Tools.isSupportSpeciaVideoSize(isFacingFont);

        if (isSupport) {
            deviceCallBack.backResultState(false);
        } else {
            String showInfo = "你的手机不支持16:9的拍摄，请更换设备！";
            String btnMsg1 = "我知道了";
            showDialog(activity, showInfo,
                    btnMsg1, deviceCallBack);
        }
    }


    /**
     * 提示更新，片头片尾数据
     *
     * @param fleSize
     * @param deviceCallBack
     */
    public void updateDishData(int fleSize, DeviceCallBack deviceCallBack) {

        String showInfo = "完成升级后才能编辑菜谱~\r\n（需下载" + fleSize + "MB文件）";
        String btnMsg1 = "取消";
        String btnMsg2 = "马上升级";
        showDialog(activity, showInfo,
                btnMsg1, btnMsg2, true, deviceCallBack);
    }


    /**
     * 下载数据
     *
     * @param current
     * @param total
     * @param deviceCallBack
     */
    public void downloadData(int current, int total, DeviceCallBack deviceCallBack) {

        String showInfo = "正在下载(" + current + "MB/" + total + "MB)";
        String btnMsg1 = "取消";
        String btnMsg2 = "后台下载";
        showDialog(activity, showInfo,
                btnMsg1, btnMsg2, 30, true, deviceCallBack);
    }

    /**
     * 展示dialog
     */
    private void showDialog(Context context, String msg, String sureMsg,
                            final DeviceCallBack deviceCallBack) {
        final DialogManager dialogManager = new DialogManager(context);
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleMessageView(context).setText(msg))
                .setView(new HButtonView(context)
                        .setNegativeText(sureMsg, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deviceCallBack.backResultState(true);
                                dialogManager.cancel();
                            }
                        }))).setCancelable(false).show();
    }

    /**
     * 展示dialog
     */
    private void showDialog(Context context, String msg, String cancelMsg,
                            String sureMsg, final boolean isPressLeftBtnDoNothing,
                            final DeviceCallBack deviceCallBack) {
        final DialogManager dialogManager = new DialogManager(context);
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleMessageView(context).setText(msg))
                .setView(new HButtonView(context)
                        .setNegativeText(cancelMsg, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deviceCallBack.backResultState(!isPressLeftBtnDoNothing);
                                dialogManager.cancel();
                            }
                        })
                        .setPositiveText(sureMsg, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deviceCallBack.backResultState(isPressLeftBtnDoNothing);
                                dialogManager.cancel();
                            }
                        }))).setCancelable(false).show();
    }


    /**
     * 展示dialog
     */
    private void showDialog(Context context, String msg, String cancelMsg,
                            String sureMsg, int progress,
                            final boolean isPressLeftBtnDoNothing,
                            final DeviceCallBack deviceCallBack) {

        final DialogManager dialogManager = new DialogManager(context);
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleProgressView(context).setTitle(msg).setProgress(progress))
                .setView(new HButtonView(context)
                        .setNegativeText(cancelMsg, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deviceCallBack.backResultState(!isPressLeftBtnDoNothing);
                                dialogManager.cancel();
                            }
                        })
                        .setPositiveText(sureMsg, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deviceCallBack.backResultState(isPressLeftBtnDoNothing);
                                dialogManager.cancel();
                            }
                        }))).setCancelable(false).show();

    }


    public interface DeviceCallBack {
        /**
         * 接口回调
         *
         * @param state true false(对当前不进行任何操作)
         */
        public void backResultState(Boolean state);
    }

    private void playAlert(){
        AudioTools.play(activity, new AudioTools.OnPlayAudioListener() {
            @Override
            public void playOver() {
//                        Toast.makeText(RecorderActivity.this,"播放完毕",Toast.LENGTH_SHORT).show();
            }
        }, R.raw.recorver_star);
    }
}
