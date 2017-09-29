package third.qiyu;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.qiyukf.unicorn.api.ConsultSource;
import com.qiyukf.unicorn.api.OnBotEventListener;
import com.qiyukf.unicorn.api.OnMessageItemClickListener;
import com.qiyukf.unicorn.api.ProductDetail;
import com.qiyukf.unicorn.api.RequestCallback;
import com.qiyukf.unicorn.api.SavePowerConfig;
import com.qiyukf.unicorn.api.StatusBarNotificationConfig;
import com.qiyukf.unicorn.api.UICustomization;
import com.qiyukf.unicorn.api.Unicorn;
import com.qiyukf.unicorn.api.YSFOptions;
import com.qiyukf.unicorn.api.YSFUserInfo;
import com.qiyukf.unicorn.ui.activity.ServiceMessageActivity;
import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import acore.logic.LoginManager;
import acore.override.XHApplication;
import acore.tools.StringManager;
import amodule.user.activity.login.LoginByAccout;


/**
 * Created by sll on 2017/9/26.
 */

public class QiYvHelper {

    private static QiYvHelper mQiYvHelper;
    private YSFOptions mOptions;
    private com.qiyukf.unicorn.api.UnreadCountChangeListener mUnreadCountListener;

    private boolean mSetUserInfo;
    private boolean mSetDefUI;

    private QiYvHelper() {

    }

    /**
     * 获取QiYvHelper对象
     * @return
     */
    public static QiYvHelper getInstance() {
        synchronized (QiYvHelper.class) {
            if (mQiYvHelper == null)
                mQiYvHelper = new QiYvHelper();
            return mQiYvHelper;
        }
    }

    /**
     * 初始化七鱼SDK
     * @param context
     */
    public void initSDK(Context context) {
        String appKey = "419831f89a538914cb168cd01d1675f4";
        Unicorn.init(context, appKey, initOptions(), new GlideImageLoader(context));
        toggleNotification(true);
    }

    /**
     * 初始化配置信息
     * @return 如果返回值为null，则全部使用默认参数。
     */
    private YSFOptions initOptions() {
        mOptions = new YSFOptions();
        mOptions.statusBarNotificationConfig = new StatusBarNotificationConfig();
        mOptions.savePowerConfig = new SavePowerConfig();
        mOptions.statusBarNotificationConfig.bigIconUri = "drawable-xhdpi://" + R.drawable.ic_launcher;
        mOptions.statusBarNotificationConfig.notificationSmallIconId = R.drawable.ic_launcher;
        mOptions.statusBarNotificationConfig.notificationEntrance = ServiceMessageActivity.class;
        mOptions.onBotEventListener = new OnBotEventListener() {
            @Override
            public boolean onUrlClick(Context context, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(intent);
                return true;
            }
        };
        return mOptions;
    }

    /**
     * 使用默认自定义UI
     * 此方法可以在需要的地方设置，否则使用七鱼默认的样式。
     * @param context
     */
    public void useDefCustomUI(Context context) {
        if (mOptions == null)
            return;
        mSetDefUI = true;
        mOptions.uiCustomization = new UICustomization();
        mOptions.uiCustomization.titleBackgroundResId = R.color.common_top_bg;
        mOptions.uiCustomization.titleCenter = true;
        mOptions.uiCustomization.titleBarStyle = 1;
        if (LoginManager.userInfo != null && !TextUtils.isEmpty(LoginManager.userInfo.get("img")))
            mOptions.uiCustomization.rightAvatar = LoginManager.userInfo.get("img");
    }

    /**
     * 设置用户信息
     */
    private void setUserInfo() {
        Map<String, String> userData = LoginManager.userInfo;
        if (userData == null || userData.isEmpty())
            return;
        YSFUserInfo userInfo = new YSFUserInfo();
        userInfo.userId = userData.get("code");
        JSONArray dataArray = new JSONArray();
        try {
            JSONObject userNick = new JSONObject();
            userNick.put("key", "real_name");
            userNick.put("value", userData.get("nickName"));
            dataArray.put(userNick);

            JSONObject userPhone = new JSONObject();
            userPhone.put("key", "mobile_phone");
            userPhone.put("value", userData.get("tel"));
            dataArray.put(userPhone);

            JSONObject userEmail = new JSONObject();
            userEmail.put("key", "email");
            userEmail.put("value", TextUtils.isEmpty(userData.get("email")) ? "无" : userData.get("email"));
            dataArray.put(userEmail);

            JSONObject userID = new JSONObject();
            userID.put("index", 0);
            userID.put("key", "user_id");
            userID.put("label", "用户ID");
            userID.put("value", userData.get("code"));
            dataArray.put(userID);

            JSONObject userVIP = new JSONObject();
            userVIP.put("index", 1);
            userVIP.put("key", "user_vip");
            userVIP.put("label", "是否VIP");
            Map<String, String> vipJson = StringManager.getFirstMap(userData.get("vip"));
            userVIP.put("value", TextUtils.equals(vipJson.get("isVip"), "2") ? "是" : "否");
            dataArray.put(userVIP);

            JSONObject userLevel = new JSONObject();
            userLevel.put("index", 2);
            userLevel.put("key", "user_level");
            userLevel.put("label", "用户等级");
            userLevel.put("value", userData.get("lv"));
            dataArray.put(userLevel);

            JSONObject regTime = new JSONObject();
            regTime.put("index", 3);
            regTime.put("key", "reg_data");
            regTime.put("label", "注册日期");
            regTime.put("value", "2015-12-22 15:38:54");
            dataArray.put(regTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        userInfo.data = dataArray.toString();

        Unicorn.setUserInfo(userInfo, new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailed(int i) {

            }

            @Override
            public void onException(Throwable throwable) {

            }
        });
        mSetUserInfo = true;
    }

    /**
     * 打开咨询页面
     * @param context
     */
    public void startServiceAcitivity(Context context, final OnSessionLifeCycleListener listener, Map<String, String> infoMap, Map<String, String> customMap) {
        if (context == null)
            return;
        if (!LoginManager.isLogin()) {
            context.startActivity(new Intent(context, LoginByAccout.class));
            return;
        }
        if (!mSetUserInfo)
            setUserInfo();
        if (!mSetDefUI)
            useDefCustomUI(context);
        /**
         * 设置访客来源，标识访客是从哪个页面发起咨询的，用于客服了解用户是从什么页面进入。
         * 三个参数分别为：来源页面的url，来源页面标题，来源页面额外信息（可自由定义）。
         * 设置来源后，在客服会话界面的"用户资料"栏的页面项，可以看到这里设置的值。
         */

        boolean mapNull = (infoMap == null || infoMap.isEmpty());
        ProductDetail.Builder builder = new ProductDetail.Builder();
        if (!mapNull) {
            int show = 0;
            try {
                show = Integer.valueOf(infoMap.get("show"));
            } catch (Exception e) {
            }
            builder.setTitle(TextUtils.isEmpty(infoMap.get("title")) ? "" : infoMap.get("title"))
                    .setDesc(TextUtils.isEmpty(infoMap.get("desc")) ? "" : infoMap.get("desc"))
                    .setPicture(TextUtils.isEmpty(infoMap.get("imgUrl")) ? "" : infoMap.get("imgUrl"))
                    .setNote((TextUtils.isEmpty(infoMap.get("note1")) ? "" : infoMap.get("note1")) + "    " + (TextUtils.isEmpty(infoMap.get("note2")) ? "" : infoMap.get("note2")))
                    .setShow(show)
                    .setAlwaysSend(TextUtils.equals(infoMap.get("alwaysSend"), "1"));
            if (!TextUtils.isEmpty(infoMap.get("clickUrl")))
                builder.setUrl(infoMap.get("clickUrl"));
        }

        String pageUrl = null;
        String pageTitle = null;
        String pageCustom = null;
        if (customMap != null && !customMap.isEmpty()) {
            pageUrl = customMap.get("pageUrl");
            pageTitle = customMap.get("pageTitle");
            pageCustom = customMap.get("pageCustom");
        }

        ConsultSource source = new ConsultSource(pageUrl == null ? "" : pageUrl, pageTitle == null ? "" : pageTitle, pageCustom == null ? "" : pageCustom);
        source.productDetail = builder.create();

        /**
         * 请注意： 调用该接口前，应先检查Unicorn.isServiceAvailable()，
         * 如果返回为false，该接口不会有任何动作
         *
         * @param context 上下文
         * @param title   聊天窗口的标题
         * @param source  咨询的发起来源，包括发起咨询的url，title，描述信息等
         */
        if (Unicorn.isServiceAvailable())
            Unicorn.openServiceActivity(context, "商城在线客服", source);
    }

    /**
     * 对话周期监听
     * 适用于会话窗口是Fragment，Activity不适用。
     */
    public interface OnSessionLifeCycleListener {
        void onLeaveSession();
    }

    /**
     * 当关联的用户从 APP 注销后，调用此方法，
     */
    public void onUserLogout() {
        mSetDefUI = false;
        mSetUserInfo = false;
        Unicorn.addUnreadCountChangeListener(mUnreadCountListener, false);
        Unicorn.logout();
    }

    /**
     * 当用户登录后，设置用户信息。
     */
    public void onUserLogin() {
        setUserInfo();
    }

    public void addOnUrlItemClickListener(final OnUrlItemClickListener listener) {
        if (mOptions != null) {
            mOptions.onMessageItemClickListener = new OnMessageItemClickListener() {
                @Override
                public void onURLClicked(Context context, String url) {
                    if (listener != null)
                        listener.onURLClicked(context, url);
                }
            };
        }
    }

    public interface OnUrlItemClickListener {
        void onURLClicked(Context context, String url);
    }

    /**
     * 设置未读消息数变化监听
     * @param listener
     * @param add true为添加，false为撤销
     */
    public void addUnreadCountChangeListener (final UnreadCountChangeListener listener, final boolean add) {
        if (mUnreadCountListener == null) {
            mUnreadCountListener = new com.qiyukf.unicorn.api.UnreadCountChangeListener() {
                @Override
                public void onUnreadCountChange(int count) {
                    if (listener != null && add)
                        listener.onUnreadCountChange(count);
                }
            };
        }
        Unicorn.addUnreadCountChangeListener(mUnreadCountListener, add);
    }

    public interface UnreadCountChangeListener {
        /**
         * 未读消息数变化回调
         * @param count 当前未读数
         */
        void onUnreadCountChange(int count);
    }

    /**
     * 切换通知开关
     * @param on true 打开消息提醒
     */
    private void toggleNotification (boolean on) {
        Unicorn.toggleNotification(on);
    }

    /**
     * 清除缓存文件
     */
    public void clearCache () {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Unicorn.clearCache();
            }
        }).start();
    }

    /**
     * 获取未读消息数
     * @return
     */
    public void getUnreadCount(final NumberCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int count = Unicorn.getUnreadCount();
                Handler handler = new Handler(XHApplication.in().getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null)
                            callback.onNumberReady(count);
                    }
                });
            }
        }).start();
    }

    public interface NumberCallback {
        void onNumberReady(int count);
    }

    /**
     * 销毁Helper
     */
    public void destroyQiYvHelper() {
        onUserLogout();
        mUnreadCountListener = null;
        mOptions = null;
        mQiYvHelper = null;
        clearCache();
    }

}
