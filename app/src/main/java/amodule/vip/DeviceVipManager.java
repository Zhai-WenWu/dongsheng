package amodule.vip;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.MessageView;
import com.xh.view.TitleView;
import com.xh.view.VButtonView;
import com.xiangha.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import acore.logic.ConfigMannager;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.stat.StatModel;
import acore.logic.stat.StatisticsManager;
import acore.observer.ObserverManager;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.user.activity.login.LoginByBindPhone;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import xh.basic.internet.UtilInternet;

import static acore.logic.ConfigMannager.KEY_DEVICE_VIP_GUIDE;

public class DeviceVipManager {

    private static boolean mIsDeviceVip = false;
    private static boolean mIsInitDeviceVipData = false;
    private static boolean mAutoBindDeviceVip;
    private static boolean mBindVipDialogShowing;

    /**
     * 设置自动绑定vip
     * @param auto
     */
    public static void setAutoBindDeviceVip(boolean auto) {
        mAutoBindDeviceVip = auto;
    }

    /**
     * vip是否绑定
     * @return
     */
    public static boolean isAutoBindDevideVip() {
        return mAutoBindDeviceVip;
    }

    /**
     *是否是临时vip
     *
     * 此方法只有在特殊时候需要单独判断是否临时会员的时候才能在外部调用；
     * 目前外部用到的地方：
     *      1.DishSkillView：点击事件中的判断
     *      2.DetailDishViewManager：菜谱详情页用于判断是否显示VIP相关的View
     *      3.MainMyself：我的页面
     *          （1）：初始化页面时，会员迁移View的显隐
     *          （2）：登录成功时，会员迁移弹框的显示
     * @return
     */
    public synchronized static boolean isDeviceVip() {
        if (!mIsInitDeviceVipData) {//进入APP时初始化到内存
            mIsDeviceVip = "2".equals(FileManager.loadShared(XHApplication.in(), FileManager.xmlFile_appInfo, "isTempVip"));
            mIsInitDeviceVipData = true;
        }
        return mIsDeviceVip;
    }

    /**
     * 设置是否是临时vip
     * @param deviceVip
     */
    public static void setDeviceVip(final boolean deviceVip) {
        mIsDeviceVip = deviceVip;
        FileManager.saveShared(XHApplication.in(),FileManager.xmlFile_appInfo,"isTempVip",deviceVip ? "2" : "");
    }

    public static void saveDeviceVipMaturityDay(String maturityTimeStr){
        if(TextUtils.isEmpty(maturityTimeStr)){
            FileManager.saveShared(XHApplication.in(),FileManager.xmlFile_appInfo,"maturity_day","");
            FileManager.saveShared(XHApplication.in(),FileManager.xmlFile_appInfo,"maturity_temp_time","");
            return;
        }
        int maturityDay = (int) ((Long.parseLong(maturityTimeStr)*1000-System.currentTimeMillis()) / (24 * 60 * 60 * 1000f));
        FileManager.saveShared(XHApplication.in(),FileManager.xmlFile_appInfo,"maturity_day",String.valueOf(maturityDay));
        FileManager.saveShared(XHApplication.in(),FileManager.xmlFile_appInfo,"maturity_temp_time",maturityTimeStr);
    }

    public static int getDeviceVipMaturityDay(){
        Object obj = FileManager.loadShared(XHApplication.in(),FileManager.xmlFile_appInfo,"maturity_day");
        if(null != obj && !TextUtils.isEmpty(obj.toString())){
            return Integer.parseInt(obj.toString());
        }
        return -1;
    }

    public static String getDeviceVipMaturityTime() {
        String tempTime = (String) FileManager.loadShared(XHApplication.in(),FileManager.xmlFile_appInfo,"maturity_temp_time");
        if (TextUtils.isEmpty(tempTime) || "null".equals(tempTime)) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date(Long.parseLong(tempTime) * 1000));
    }

    /**
     * 绑定vip
     * @param context
     */
    public static void bindYiYuanVIP(final Context context, IDeviceVipStat statImpl) {
        mAutoBindDeviceVip = false;
        ReqEncyptInternet.in().doEncypt(StringManager.api_yiyuan_binduser, "", new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= UtilInternet.REQ_OK_STRING) {
                    Map<String, String> state = StringManager.getFirstMap(o);
                    if ("2".equals(state.get("state"))) {
                        setDeviceVip(false);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showBindSuccDialog();
                            }
                        }, 200);
                        ObserverManager.getInstance().notify(ObserverManager.NOTIFY_YIYUAN_BIND, null, state);
                        if (statImpl != null && statImpl instanceof DeviceVipStatModel) {
                            DeviceVipStatModel statModel = (DeviceVipStatModel) statImpl;
                            XHClick.mapStat(context, statModel.getEventID(), statModel.getTwoLevelBindSuccess(), statModel.getThreeLevel1());
                        }
                        return;
                    } else {
                        //绑定失败
                        Toast.makeText(context, "绑定失败", Toast.LENGTH_SHORT).show();
                        ObserverManager.getInstance().notify(ObserverManager.NOTIFY_YIYUAN_BIND, null, state);
                    }
                } else {
                    //绑定失败
                    Toast.makeText(context, "绑定失败", Toast.LENGTH_SHORT).show();
                    ObserverManager.getInstance().notify(ObserverManager.NOTIFY_YIYUAN_BIND, null, null);
                }
                if (statImpl != null && statImpl instanceof DeviceVipStatModel) {
                    DeviceVipStatModel statModel = (DeviceVipStatModel) statImpl;
                    XHClick.mapStat(context, statModel.getEventID(), statModel.getTwoLevelBindSuccess(), statModel.getThreeLevel2());
                }
            }
        });
    }

    /**
     * 初始化临时会员绑定状态
     * @param context
     * @param callback
     */
    public static void initDeviceVipBindState(Context context, final LoginManager.VipStateCallback callback) {
        ReqEncyptInternet.in().doEncypt(StringManager.api_yiyuan_bindstate, "", new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object obj) {
                Map<String, String> data = StringManager.getFirstMap(obj);
                boolean isTempVip = "2".equals(data.get("isBindingVip"));
                setDeviceVip(isTempVip);
                Map<String, String> dataContentMap = StringManager.getFirstMap(data.get("data"));
                Map<String, String> vipContentMap = StringManager.getFirstMap(dataContentMap.get("vip"));
                String vipMaturityTime = vipContentMap.get("maturity_time");//单位都是秒
                saveDeviceVipMaturityDay(vipMaturityTime);
                String nickName = dataContentMap.get("nickName");
                saveDeviceVipNickname(nickName);
                if (callback != null)
                    callback.callback(isTempVip);
            }
        });
    }

    private static void saveDeviceVipNickname(String nickName) {
        FileManager.saveShared(XHApplication.in(),FileManager.xmlFile_appInfo,"temp_vip_nickName", TextUtils.isEmpty(nickName) ? "" : nickName);
    }

    public static String getDeviceVipNickname() {
        return (String) FileManager.loadShared(XHApplication.in(), FileManager.xmlFile_appInfo, "temp_vip_nickName");
    }

    /**
     * 根据天数间隔控制每天第一次进入【我的】页面是否要显示绑定弹框
     * @return true 显示绑定弹框
     */
    public static boolean checkShowDeviceVipDialog () {
        if (mBindVipDialogShowing || !isDeviceVip())
            return false;
        mBindVipDialogShowing = true;
        String deviceVipGuideStr = ConfigMannager.getConfigByLocal(KEY_DEVICE_VIP_GUIDE);
        Map<String,String> map = StringManager.getFirstMap(deviceVipGuideStr);
        if(map.isEmpty() || !"2".equals(map.get("isShow"))){
            mBindVipDialogShowing = false;
            return false;
        }

        String lastShowDay = (String) FileManager.loadShared(XHApplication.in(),FileManager.xmlFile_appInfo,"deviceVipGuide");
        String currentDay = Tools.getAssignTime("yyyyMMdd", 0);
        boolean isShow;
        if(TextUtils.isEmpty(lastShowDay)){
            isShow = true;
        }else{
            int intervalDay = Tools.parseIntOfThrow(map.get("interval"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            try {
                Date currentDate = sdf.parse(currentDay);
                Date lastShowDate = sdf.parse(lastShowDay);
                final int days = Tools.getIntervalDaysFromTwoDate(currentDate, lastShowDate);
                isShow = days > intervalDay && intervalDay >= 0;
            } catch (ParseException e) {
                e.printStackTrace();
                mBindVipDialogShowing = false;
                return false;
            }

        }
        FileManager.saveShared(XHApplication.in(),FileManager.xmlFile_appInfo,"deviceVipGuide", currentDay);
        return isShow;
    }

    public static void showBindVipDialog(IDeviceVipStat statImpl) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Activity context = XHActivityManager.getInstance().getCurrentActivity();
                if (context == null) {
                    return;
                }
                if (context.isFinishing() || context.isDestroyed()) {
                    showBindVipDialog(statImpl);
                    return;
                }
                DeviceVipStatModel model1 = new DeviceVipStatModel("立即绑定点击次数", "以后再说点击次数");
                final DialogManager dialogManager = new DialogManager(context);
                dialogManager.createDialog(new ViewManager(dialogManager)
                        .setView(new TitleView(context).setText("会员权益绑定提醒"))
                        .setView(new MessageView(context).setText(R.string.devicevip_dialog_desc))
                        .setView(new VButtonView(context).setPositiveText("立即绑定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogManager.cancel();
                                if (LoginManager.isLogin()) {
                                    DeviceVipManager.bindYiYuanVIP(context, statImpl);
                                } else {
                                    DeviceVipManager.setAutoBindDeviceVip(true);
                                    Intent intent = new Intent(context, LoginByBindPhone.class);
                                    if (statImpl != null && statImpl instanceof DeviceVipStatModel) {
                                        intent.putExtra(DeviceVipStatModel.TAG, (DeviceVipStatModel)statImpl);
                                    }
                                    context.startActivity(intent);
                                }
                                XHClick.mapStat(context, model1.getEventID(), model1.getTwoLevelBindDialog(), model1.getThreeLevel1());
                                StatisticsManager.saveData(StatModel.createBtnClickModel(context.getClass().getSimpleName(), "会员绑定弹框", "立即绑定"));
                            }
                        }).setPositiveTextColor(Color.parseColor("#007aff")).setPositiveTextBold(true)
                                .setNegativeText("以后再说", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogManager.cancel();
                                        XHClick.mapStat(context, model1.getEventID(), model1.getTwoLevelBindDialog(), model1.getThreeLevel2());
                                        StatisticsManager.saveData(StatModel.createBtnClickModel(context.getClass().getSimpleName(), "会员绑定弹框", "以后再说"));
                                    }
                                }).setNegativeTextColor(Color.parseColor("#007aff")))).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mBindVipDialogShowing = false;
                    }
                });
                dialogManager.show();
            }
        }, 100);
    }
    
    private static void showBindSuccDialog() {
        final Context currContext = XHActivityManager.getInstance().getCurrentActivity();
        final DialogManager dialogManager = new DialogManager(currContext);
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleView(currContext).setText(R.string.devicevip_bind_succ_title))
                .setView(new MessageView(currContext).setText(R.string.devicevip_succ_desc))
                .setView(new HButtonView(currContext).setNegativeText(R.string.str_know, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogManager.cancel();
                        DeviceVipStatModel model = new DeviceVipStatModel("我知道了点击次数", null);
                        XHClick.mapStat(currContext, model.getEventID(), model.getTwoLevelBindSuccTipDialog(), model.getThreeLevel1());
                        StatisticsManager.saveData(StatModel.createBtnClickModel(currContext.getClass().getSimpleName(), "绑定成功", currContext.getResources().getString(R.string.str_know)));
                    }
                }).setNegativeTextColor(Color.parseColor("#007aff")))).show();
    }

}
