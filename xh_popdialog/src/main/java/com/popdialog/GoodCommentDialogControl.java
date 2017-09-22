package com.popdialog;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.mrtrying.xh_popdialog.R;
import com.popdialog.base.BaseDialogControl;
import com.popdialog.util.FileManager;
import com.popdialog.util.GoodCommentManager;
import com.popdialog.util.StringManager;
import com.popdialog.util.Tools;
import com.popdialog.view.PopDialog;

import java.util.ArrayList;
import java.util.Map;


import static com.popdialog.AllPopDialogControler.TAG;
import static com.popdialog.util.GoodCommentManager.okShow;
import static java.lang.System.currentTimeMillis;

/**
 * 好评弹框控制
 * <p>
 * 1、需要服务端控制的：
 * （1） 好评弹框次数，服务端可以修改，ios默认5次，安卓默认2次
 * （2） 好评弹框文案如下，服务端可以修改（包括主标题，副标题，左边关闭按钮，右边跳转按钮）
 * <p>
 * 2、弹框时机客户端控制
 * （1） 从后台开关开启时计算
 * （2） 第一次弹框：为第二次打开app的时候
 * （3） 每天只弹框1次，弹框的间隔时间从服务端获取
 * （4） 一个版本最多弹框10次（客户端写死），多次开关也是计算累加10次的
 * （5） 点击“还不错”之后，本版本就不再弹了，新版本重新开始，点“不好用”关闭弹框，下次还会弹
 * ------------------------------------------------------------------------------
 * 数据修复改已最后一为准。
 */
public class GoodCommentDialogControl extends BaseDialogControl {
    private static final int MAX_NUM = 10;//最大展示次数
    private static final int DEFAULT_NUM = 2;//默认次数
    private int space_minute = 24 * 60; //每次弹间隔时间，0为不限制
    private boolean IsShow = true;//展示开关
    private int num;

    private int goodSpaceTime = 3 * 30; //好评过的3个月不再弹

    private ArrayList<Map<String, String>> mLists;

    private OnGoodCommentClickCallback onGoodCommentClickCallback;

    public GoodCommentDialogControl(Activity activity) {
        super(activity);
    }

    @Override
    public void isShow(String data, OnPopDialogCallback callback) {
        Log.i(TAG, "FullSrceenDialogControl :: mActivity = " + mActivity + " ; data = " + data);
        if (mActivity == null || TextUtils.isEmpty(data)) {
            callback.onNextShow();
            return;
        }
        if (!IsShow || okShow) {
            callback.onNextShow();
            return;
        }
        //具体显示策略
        String show_num = (String) FileManager.loadShared(mActivity, FileManager.GOODCOMMENT_SHOW_NUM, FileManager.GOODCOMMENT_SHOW_NUM);
        String show_time = (String) FileManager.loadShared(mActivity, FileManager.GOODCOMMENT_SHOW_TIME, FileManager.GOODCOMMENT_SHOW_TIME);
        if (TextUtils.isEmpty(show_num) || TextUtils.isEmpty(show_time)) {
            callback.onNextShow();
            //第一次进来---初始化一些数据
            FileManager.saveShared(mActivity, FileManager.GOODCOMMENT_SHOW_NUM, FileManager.GOODCOMMENT_SHOW_NUM, "0");
            FileManager.saveShared(mActivity, FileManager.GOODCOMMENT_SHOW_NUM_ALL, FileManager.GOODCOMMENT_SHOW_NUM_ALL, "0");
            FileManager.saveShared(mActivity, FileManager.GOODCOMMENT_SHOW_TIME, FileManager.GOODCOMMENT_SHOW_TIME, String.valueOf(currentTimeMillis()));
        } else {//多次进来
            String show_num_all = (String) FileManager.loadShared(mActivity, FileManager.GOODCOMMENT_SHOW_NUM_ALL, FileManager.GOODCOMMENT_SHOW_NUM_ALL);
            int all_num = Integer.parseInt(show_num_all);
            if (all_num > MAX_NUM) {
                okShow = true;
                callback.onNextShow();
                return;
            }
            //解析数据
            analysisData(data, all_num, show_num, show_time, callback);
        }
    }

    /**
     * 获取服务端要修改的数据
     *
     * @param data      数据
     * @param all_num   目前一共显示了几次
     * @param show_num  显示次数
     * @param show_time 上一次显示时间
     */
    private void analysisData(String data, int all_num, String show_num, String show_time, OnPopDialogCallback callback) {
        mLists = StringManager.getListMapByJson(data);
        if (all_num == 0) {
            FileManager.saveShared(mActivity, FileManager.GOODCOMMENT_SHOW_TIME, FileManager.GOODCOMMENT_SHOW_TIME, String.valueOf(currentTimeMillis()));
            num = Integer.parseInt(show_num);
            makeDataShow(callback);
        } else {
            long time = Long.parseLong(show_time);
            if (mLists.get(0).containsKey("showTimeInterval"))
                space_minute = Integer.parseInt(mLists.get(0).get("showTimeInterval"));
            if (currentTimeMillis() - time >= space_minute * 60 * 1000) {
                num = Integer.parseInt(show_num);
                makeDataShow(callback);
            } else {
                callback.onNextShow();
            }
        }
    }

    /**
     * 处理规则
     */
    private void makeDataShow(OnPopDialogCallback callback) {
        String list_num = mLists.get(0).get("num");
        String list_isShow = mLists.get(0).get("isShow");
        if (!TextUtils.isEmpty(list_num) && !TextUtils.isEmpty(list_isShow)) {
            int dedult_num = DEFAULT_NUM;
            dedult_num = Integer.parseInt(list_num);
            if (list_isShow.equals("2")) {
                IsShow = true;
            } else {
                IsShow = false;
                FileManager.saveShared(mActivity, FileManager.GOODCOMMENT_SHOW_NUM, FileManager.GOODCOMMENT_SHOW_NUM, "0");
            }
            if (IsShow && num < dedult_num) {
                String goGoodTime = (String) FileManager.loadShared(mActivity, FileManager.GOODCOMMENT_INFO, FileManager.GOODCOMMENT_TIME);
                if (!TextUtils.isEmpty(goGoodTime)) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - Long.parseLong(goGoodTime) < goodSpaceTime * 24 * 60 * 60 * 1000) {
                        callback.onNextShow();
                        return;
                    }
                }
                String show_time_num = (String) FileManager.loadShared(mActivity, FileManager.GOODCOMMENT_INFO, FileManager.GOODCOMMENT_SHOW_TIME_NUM);
                String show_time = (String) FileManager.loadShared(mActivity, FileManager.GOODCOMMENT_INFO, FileManager.GOODCOMMENT_SHOW_TIME);
                int showTimeNum = 0;
                if (!TextUtils.isEmpty(show_time_num)) {
                    showTimeNum = Integer.parseInt(show_time_num);
                }
                showTimeNum = showTimeNum % (dedult_num + 1);
                if (showTimeNum >= dedult_num
                        && (!TextUtils.isEmpty(show_time)
                        && System.currentTimeMillis() - Long.parseLong(show_time) < goodSpaceTime * 24 * 60 * 60 * 1000)) {
                    callback.onNextShow();
                    return;
                }
                callback.onCanShow();
            } else {
                callback.onNextShow();
            }
        } else {
            callback.onNextShow();
        }
    }

    @Override
    public void show() {
        //变化数据---增加数量变化的值
        FileManager.saveShared(mActivity, FileManager.GOODCOMMENT_SHOW_NUM, FileManager.GOODCOMMENT_SHOW_NUM, String.valueOf(++num));
        String show_num_all = (String) FileManager.loadShared(mActivity, FileManager.GOODCOMMENT_SHOW_NUM_ALL, FileManager.GOODCOMMENT_SHOW_NUM_ALL);
        int all_num = Integer.parseInt(show_num_all);
        FileManager.saveShared(mActivity, FileManager.GOODCOMMENT_SHOW_NUM_ALL, FileManager.GOODCOMMENT_SHOW_NUM_ALL, String.valueOf(++all_num));
        FileManager.saveShared(mActivity, FileManager.GOODCOMMENT_SHOW_TIME, FileManager.GOODCOMMENT_SHOW_TIME, String.valueOf(currentTimeMillis()));
        String show_time_num = (String) FileManager.loadShared(mActivity, FileManager.GOODCOMMENT_INFO, FileManager.GOODCOMMENT_SHOW_TIME_NUM);
        int showTimeNum = 0;
        if (!TextUtils.isEmpty(show_time_num)) {
            showTimeNum = Integer.parseInt(show_time_num);
        }
        showTimeNum++;
        FileManager.saveShared(mActivity, FileManager.GOODCOMMENT_INFO, FileManager.GOODCOMMENT_SHOW_TIME_NUM, String.valueOf(showTimeNum));
        showDialog();
    }

    /** 展示对话框 */
    private void showDialog() {
        final Map<String, String> map = mLists.get(0);
        ArrayList<Map<String, String>> listColor = StringManager.getListMapByJson(mLists.get(0).get("color"));
        Map<String, String> mapColor = listColor.get(0);
        String alertType = map.get("alertType");
//        alertType = "3";
        String message = map.get("subtitle");
//        message = "赏个五星好评吧！再也不用担心错过心爱的大局热点";
        int lineNumber = Tools.getTextNumbers(mActivity, Tools.getDimen(mActivity, R.dimen.dp_222), Tools.getDimen(mActivity, R.dimen.dp_12));
        String titleColor = "#000000", messageColor = "#000000";
        String confirmButtonColor = "#007aff", cancelButtonColor = "#007aff";
        boolean cancelBold = false, sureBold = false;
        int layoutId;
        if (message.length() > lineNumber) {
            layoutId = R.layout.dialog_common;
        } else {
            layoutId = R.layout.dialog_goodcomment;
        }

        final String twoLevel;
        if ("1".equals(alertType)) {
            cancelBold = true;
            twoLevel = "原生左侧加粗";
        } else if ("2".equals(alertType)) {
            sureBold = true;
            twoLevel = "原生右侧加粗";
        } else {
            twoLevel = "自定义样式";
            titleColor = "#333333";
            messageColor = "#999999";
            confirmButtonColor = mapColor.get("confirmButtonText");
            cancelButtonColor = mapColor.get("cancelButtonText");
        }

        final PopDialog popDialog = new PopDialog(mActivity, layoutId);
        popDialog.setTitle(map.get("popText"), titleColor)
                .setMessage(message, messageColor)
                .setCanselButton(map.get("cancelButtonText"), cancelButtonColor, cancelBold, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popDialog.cancel();
                        if (onGoodCommentClickCallback != null) {
                            onGoodCommentClickCallback.onClickCannel(twoLevel, map.get("cancelButtonText"));
                        }
                    }
                }).setSureButton(map.get("confirmButtonText"), confirmButtonColor, sureBold, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popDialog.cancel();
                GoodCommentManager.setGoodComment("首页弹框确认", mActivity);
                if (onGoodCommentClickCallback != null) {
                    onGoodCommentClickCallback.onClickSure(twoLevel, map.get("confirmButtonText"));
                }
            }
        }).show();
        if (!TextUtils.isEmpty(map.get("contentTime"))) {
            GoodCommentManager.contentTime = Integer.parseInt(mLists.get(0).get("contentTime"));
        }
    }

    public interface OnGoodCommentClickCallback {
        void onClickSure(String type, String text);

        void onClickCannel(String twoLevel, String text);
    }

    public interface OnCommentTimeStatisticsCallback {
        void onStatistics(String typeStr, String timeStr);
    }

    /*------------------------------------------------- Get & Set ---------------------------------------------------------------*/

    public OnGoodCommentClickCallback getOnGoodCommentClickCallback() {
        return onGoodCommentClickCallback;
    }

    public void setOnGoodCommentClickCallback(OnGoodCommentClickCallback onGoodCommentClickCallback) {
        this.onGoodCommentClickCallback = onGoodCommentClickCallback;
    }

}
