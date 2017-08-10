package acore.dialogManager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.XhNewDialog;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.tool.UtilString;

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
public class GoodCommentManager extends DialogManagerParent {
    private int max_show = 10;//最大展示次数
    private int dedult_num = 2;//默认次数
    private int space_minute = 24 * 60; //每次弹间隔时间，0为不限制
    private boolean IsShow = true;//展示开关
    private static boolean okShow = false;//展示用户开关
    private static GoodCommentManager goodCommentManager = null;
    private int num;
    private long time;
    private static int contentTime = 5;

    private int goodSpaceTime = 3 * 30; //好评过的3个月不再弹
    private int goodXSpaceTime = 3 * 30; //弹X次后3个月内不再弹

    private ArrayList<Map<String, String>> mLists;

    /**
     * 外部调用开启这个好评弹框
     */
    @Override
    public void isShow(final OnDialogManagerCallback callback) {
        if (!IsShow || okShow) {
            callback.onGone();
            return;
        }
        Activity mAct = XHActivityManager.getInstance().getCurrentActivity();
        //具体显示策略
        String show_num = (String) FileManager.loadShared(mAct, FileManager.GOODCOMMENT_SHOW_NUM, FileManager.GOODCOMMENT_SHOW_NUM);
        String show_time = (String) FileManager.loadShared(mAct, FileManager.GOODCOMMENT_SHOW_TIME, FileManager.GOODCOMMENT_SHOW_TIME);
        if (TextUtils.isEmpty(show_num) || TextUtils.isEmpty(show_time)) {
            callback.onGone();
            //第一次进来---初始化一些数据
            FileManager.saveShared(mAct, FileManager.GOODCOMMENT_SHOW_NUM, FileManager.GOODCOMMENT_SHOW_NUM, "0");
            FileManager.saveShared(mAct, FileManager.GOODCOMMENT_SHOW_NUM_ALL, FileManager.GOODCOMMENT_SHOW_NUM_ALL, "0");
            FileManager.saveShared(mAct, FileManager.GOODCOMMENT_SHOW_TIME, FileManager.GOODCOMMENT_SHOW_TIME, String.valueOf(currentTimeMillis()));
        } else {//多次进来
            String show_num_all = (String) FileManager.loadShared(mAct, FileManager.GOODCOMMENT_SHOW_NUM_ALL, FileManager.GOODCOMMENT_SHOW_NUM_ALL);
            int all_num = Integer.parseInt(show_num_all);
            if (all_num > max_show) {
                okShow = true;
                callback.onGone();
                return;
            }
            getRequestData(all_num, show_num, show_time, callback);
        }
    }

    /**
     * 获取服务端要修改的数据
     *
     * @param all_num   目前一共显示了几次
     * @param show_num
     * @param show_time 上一次显示时间
     */
    private void getRequestData(final int all_num, final String show_num, final String show_time, final OnDialogManagerCallback callback) {
        final Activity mAct = XHActivityManager.getInstance().getCurrentActivity();
        String url = StringManager.api_toGoodContent;
        ReqInternet.in().doGet(url, new InternetCallback(mAct) {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    mLists = UtilString.getListMapByJson(o);
                    if (all_num == 0) {
                        FileManager.saveShared(mAct, FileManager.GOODCOMMENT_SHOW_TIME, FileManager.GOODCOMMENT_SHOW_TIME, String.valueOf(currentTimeMillis()));
                        num = Integer.parseInt(show_num);
                        makeDataShow(callback);
                    } else {
                        time = Long.parseLong(show_time);
                        if (mLists.get(0).containsKey("showTimeInterval"))
                            space_minute = Integer.parseInt(mLists.get(0).get("showTimeInterval"));
                        if (currentTimeMillis() - time >= space_minute * 60 * 1000) {
                            num = Integer.parseInt(show_num);
                            makeDataShow(callback);
                        } else {
                            callback.onGone();
                        }
                    }
                } else {
                    //不处理
                    callback.onGone();
                }
            }
        });
    }

    /**
     * 处理规则
     */
    private void makeDataShow(OnDialogManagerCallback callback) {
        Activity mAct = XHActivityManager.getInstance().getCurrentActivity();
        String list_num = mLists.get(0).get("num");
        String list_isForm = mLists.get(0).get("isForm");
        if (!TextUtils.isEmpty(list_num) && !TextUtils.isEmpty(list_isForm)) {
            dedult_num = Integer.parseInt(list_num);
//            list_isForm = "2";
//            dedult_num = 100;
            if (list_isForm.equals("2")) {
                IsShow = true;
            } else {
                IsShow = false;
                FileManager.saveShared(mAct, FileManager.GOODCOMMENT_SHOW_NUM, FileManager.GOODCOMMENT_SHOW_NUM, "0");
            }
            if (IsShow && num < dedult_num) {
                String goGoodTime = (String) FileManager.loadShared(mAct, FileManager.GOODCOMMENT_INFO, FileManager.GOODCOMMENT_TIME);
                if(!TextUtils.isEmpty(goGoodTime)){
                    long currentTime = System.currentTimeMillis();
                    if(currentTime - Long.parseLong(goGoodTime) < goodSpaceTime * 24 * 60 * 60 * 1000){
                        callback.onGone();
                        return;
                    }
                }
                String show_time_num = (String) FileManager.loadShared(mAct, FileManager.GOODCOMMENT_INFO, FileManager.GOODCOMMENT_SHOW_TIME_NUM);
                String show_time = (String) FileManager.loadShared(mAct, FileManager.GOODCOMMENT_INFO, FileManager.GOODCOMMENT_SHOW_TIME);
                int showTimeNum = 0;
                if(!TextUtils.isEmpty(show_time_num)){
                    showTimeNum = Integer.parseInt(show_time_num);
                }
                showTimeNum = showTimeNum % (dedult_num + 1);
                if(showTimeNum >= dedult_num && (!TextUtils.isEmpty(show_time) && System.currentTimeMillis() - Long.parseLong(show_time) < goodSpaceTime * 24 * 60 * 60 * 1000)){
                    callback.onGone();
                    return;
                }
                callback.onShow();
            } else {
                callback.onGone();
            }
        } else {
            callback.onGone();
        }
    }

    /**
     * 展示对话框
     */
    private void showDialog() {
        final Activity mAct = XHActivityManager.getInstance().getCurrentActivity();
        Map<String, String> map = mLists.get(0);
        ArrayList<Map<String, String>> listColor = UtilString.getListMapByJson(mLists.get(0).get("color"));
        Map<String, String> mapColor = listColor.get(0);
        String alertType = map.get("alertType");
//        alertType = "3";
        String message = map.get("subtitle");
//        message = "赏个五星好评吧！再也不用担心错过心爱的大局热点";
        int lineNumber = Tools.getTextNumbers(mAct, Tools.getDimen(mAct, R.dimen.dp_222), Tools.getDimen(mAct, R.dimen.dp_12));
        String titleColor = "#000000", messageColor = "#000000";
        String confirmButtonColor = "#007aff", cancelButtonColor = "#007aff";
        boolean cancelBold = false, sureBold = false;
        String tongjiId = "a_NewEvaluate"; //自定义
        String twoLevel;
        int layoutId;
        if (message.length() > lineNumber) {
            layoutId = R.layout.xh_new_dialog;
        } else {
            layoutId = R.layout.a_dialog_goodcomment;
        }

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

        final XhNewDialog xhNewDialog = new XhNewDialog(mAct, layoutId, tongjiId, twoLevel);
        xhNewDialog.setTitle(map.get("popText"), titleColor)
                .setMessage(message, messageColor)
                .setCanselButton(map.get("cancelButtonText"), cancelButtonColor, cancelBold, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        XHClick.mapStat(mAct, "a_evaluate420", "首页弹框关闭", "");
                        xhNewDialog.cancel();
                    }
                }).setSureButton(map.get("confirmButtonText"), confirmButtonColor, sureBold, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xhNewDialog.cancel();
                setGoodComment("首页弹框确认", mAct);
            }
        }).show();
        if (!TextUtils.isEmpty(map.get("contentTime"))) {
            contentTime = Integer.parseInt(mLists.get(0).get("contentTime"));
        }
    }

    /**
     * 直接跳到应用市场评分
     */
    private static void setMoreHot(Activity activity) {
        if (activity == null)
            return;
        String str = "market://details?id=" + activity.getPackageName();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(str));
            activity.startActivity(intent);

        } catch (Exception e) {
        } finally {
            activity = null;
        }
    }

    /**
     * 去好评
     *
     * @param type 好评类型
     */
    public static void setGoodComment(String type, Activity activity) {
        FileManager.saveShared(activity, FileManager.GOODCOMMENT_TYPE, FileManager.GOODCOMMENT_TYPE, type);
        FileManager.saveShared(activity, type, type, String.valueOf(currentTimeMillis()));
        setMoreHot(activity);
    }

    /**
     * 获取记录去好评的的时间
     * 好评类型
     */
    public static void setStictis(Activity activity) {
        String type = (String) FileManager.loadShared(activity, FileManager.GOODCOMMENT_TYPE, FileManager.GOODCOMMENT_TYPE);
        if (!TextUtils.isEmpty(type)) {
            String type_time = (String) FileManager.loadShared(activity, type, type);
            if (!TextUtils.isEmpty(type_time)) {
                long time = Long.parseLong(type_time);
                if (time > 0) {
                    long time_interval = currentTimeMillis() - time;
                    if (time_interval >= contentTime * 1000) {
                        XHClick.mapStat(activity, "a_evaluate420", type, ">" + contentTime);
                        FileManager.saveShared(XHApplication.in(),FileManager.GOODCOMMENT_INFO,FileManager.GOODCOMMENT_TIME,String.valueOf(currentTimeMillis()));
                        okShow = true;
                    } else {
                        String temp = String.valueOf(time_interval / 1000);
                        if (temp.contains(".")) {
                            temp = temp.substring(0, temp.indexOf("."));
                        }
                        XHClick.mapStat(activity, "a_evaluate420", type, temp);
                    }
                }
            }
            contentTime = 5;
            FileManager.saveShared(activity, type, type, "0");
            activity = null;
        }
    }

    @Override
    public void show() {
        Activity mAct = XHActivityManager.getInstance().getCurrentActivity();
        //变化数据---增加数量变化的值
        FileManager.saveShared(mAct, FileManager.GOODCOMMENT_SHOW_NUM, FileManager.GOODCOMMENT_SHOW_NUM,
                String.valueOf(++num));
        String show_num_all = (String) FileManager.loadShared(mAct, FileManager.GOODCOMMENT_SHOW_NUM_ALL, FileManager.GOODCOMMENT_SHOW_NUM_ALL);
        int all_num = Integer.parseInt(show_num_all);
        FileManager.saveShared(mAct, FileManager.GOODCOMMENT_SHOW_NUM_ALL, FileManager.GOODCOMMENT_SHOW_NUM_ALL, String.valueOf(++all_num));
        FileManager.saveShared(mAct, FileManager.GOODCOMMENT_SHOW_TIME, FileManager.GOODCOMMENT_SHOW_TIME, String.valueOf(currentTimeMillis()));
        String show_time_num = (String) FileManager.loadShared(mAct, FileManager.GOODCOMMENT_INFO, FileManager.GOODCOMMENT_SHOW_TIME_NUM);
        int showTimeNum = 0;
        if(!TextUtils.isEmpty(show_time_num)){
            showTimeNum = Integer.parseInt(show_time_num);
        }
        showTimeNum++;
        FileManager.saveShared(mAct, FileManager.GOODCOMMENT_INFO, FileManager.GOODCOMMENT_SHOW_TIME_NUM,String.valueOf(showTimeNum));
        showDialog();
    }

    @Override
    public void cancel() {

    }
}
