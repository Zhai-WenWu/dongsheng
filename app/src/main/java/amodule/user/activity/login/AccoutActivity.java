package amodule.user.activity.login;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.bugly.crashreport.CrashReport;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.user.view.LeftAndRightTextView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import third.push.xg.XGPushServer;
import third.share.tools.ShareTools;

/**
 * 用户设置页面
 * Created by ：fei_teng on 2017/2/22 15:00.
 */

public class AccoutActivity extends BaseLoginActivity implements View.OnClickListener {

    private LinearLayout ll_accout;
    private RelativeLayout rl_phone_accout;
    private LeftAndRightTextView view_email_accout;
    private LeftAndRightTextView view_phone_accout;
    private LeftAndRightTextView view_weixin, view_qq, view_weibo, view_meizu;
    private LeftAndRightTextView view_modify_secret;
    private Handler handler;
    public static final int ONREFRESH = 1;
    private Map<String, String> bindMap = new HashMap<>();
    private final String unbindStr = "未绑定";
    private String tel;
    private String zoneCode;
    private View view_accout_below;
    private String mPlatformName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 3, 0, 0, R.layout.user_accout_setting);
        initData();
        initView();
        initTitle();
    }

    protected void initTitle() {
        String colors = Tools.getColorStr(this, R.color.common_top_bg);
        Tools.setStatusBarColor(this, Color.parseColor(colors));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }

    private void initData() {
        Intent intent = getIntent();
        tel = intent.getStringExtra(PHONE_NUM);
        zoneCode = intent.getStringExtra(ZONE_CODE);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ONREFRESH:
                        loadManager.setLoading(new InternetCallback() {
                            @Override
                            public void loaded(int flag, String url, Object returnObj) {
                                loadManager.setLoading(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        getData();
                                    }
                                });
                            }
                        });
                        break;
                }
            }
        };
    }

    private void initView() {
        TextView title = (TextView) findViewById(R.id.title);
        title.setText("账号与安全");
        ll_accout = (LinearLayout) findViewById(R.id.ll_accout);
        rl_phone_accout = (RelativeLayout) findViewById(R.id.rl_phone_accout);
        view_email_accout = (LeftAndRightTextView) findViewById(R.id.view_email_accout);
        view_phone_accout = (LeftAndRightTextView) findViewById(R.id.view_phone_accout);
        view_weixin = (LeftAndRightTextView) findViewById(R.id.view_weixin);
        view_qq = (LeftAndRightTextView) findViewById(R.id.view_qq);
        view_weibo = (LeftAndRightTextView) findViewById(R.id.view_weibo);
        view_meizu = (LeftAndRightTextView) findViewById(R.id.view_meizu);
        view_modify_secret = (LeftAndRightTextView) findViewById(R.id.view_modify_secret);
        view_accout_below = findViewById(R.id.view_accout_below);

        view_meizu.setVisibility(View.GONE);
        view_accout_below.setVisibility(View.GONE);

        addViewListener();
        shwoPhoneNum();
    }

    private void shwoPhoneNum() {
        if (TextUtils.isEmpty(tel)) {
            rl_phone_accout.setVisibility(View.VISIBLE);
            view_phone_accout.setVisibility(View.GONE);
        } else {
            rl_phone_accout.setVisibility(View.GONE);
            view_phone_accout.setVisibility(View.VISIBLE);
            view_phone_accout.setRightText(hidePhoneNum(tel));
        }
    }

    private void addViewListener() {
        ll_accout.setOnClickListener(this);
        findViewById(R.id.tv_help).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);

        view_phone_accout.init("手机号", unbindStr, false, true, null);
        view_email_accout.init("邮箱", unbindStr, false, true, getThirdLAndRCallback("email"));
        view_weixin.init("微信号", unbindStr, true, false, getThirdLAndRCallback("weixin"));
        view_qq.init("QQ号", unbindStr, true, false, getThirdLAndRCallback("qq"));
        view_weibo.init("微博号", unbindStr, false, false, getThirdLAndRCallback("weibo"));
        view_meizu.init("魅族", unbindStr, false, false, getThirdLAndRCallback("meizu"));

        view_modify_secret.init("修改密码", "", false, true,
                new LeftAndRightTextView.LeftAndRightTextViewCallback() {
                    @Override
                    public void onClick() {
                        motifySecret();
                    }
                });
    }

    /**
     * @param typeKey "weibo","qq","weixin"
     */
    private LeftAndRightTextView.LeftAndRightTextViewCallback getThirdLAndRCallback(final String typeKey) {
        return new LeftAndRightTextView.LeftAndRightTextViewCallback() {
            @Override
            public void onClick() {
                String type = bindMap.get(typeKey);
                if (TextUtils.isEmpty(type)) {
                    try {
                        switch (typeKey) {
                            case "email":
                            case "meizui":
                                return;
                            case "weibo":
                                loginByThrid(LoginManager.LOGIN_WB, "微博号");
                                break;
                            case "qq":
                                loginByThrid(LoginManager.LOGIN_QQ, "QQ号");
                                break;
                            case "weixin":
                                loginByThrid(LoginManager.LOGIN_WX, "微信号");
                                break;
                        }
                    }catch (Exception ignored){
                        CrashReport.postCatchedException(ignored);
                    }
                } else
                    showUnbindThirdParty(typeKey);
            }
        };
    }

    private void motifySecret() {
        if (TextUtils.isEmpty(tel)) {
            Intent bindPhoneIntent = new Intent(this, LostSecret.class);
            bindPhoneIntent.putExtra(PATH_ORIGIN, ORIGIN_MODIFY_PSW);
            startActivity(bindPhoneIntent);
        } else {
            final DialogManager dialogManager = new DialogManager(AccoutActivity.this);
            dialogManager.createDialog(new ViewManager(dialogManager)
                    .setView(new TitleMessageView(AccoutActivity.this).setText("修改登录密码" + "\n将给手机" + hidePhoneNum(tel) + "发送验证码"))
                    .setView(new HButtonView(AccoutActivity.this)
                            .setNegativeTextColor(Color.parseColor("#007aff"))
                            .setNegativeText("取消", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogManager.cancel();
                                    XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, "修改密码", "修改密码弹框，点取消");
                                }
                            })
                            .setPositiveTextColor(Color.parseColor("#007aff"))
                            .setPositiveText("确定", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogManager.cancel();
                                    XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, "修改密码", "修改密码弹框，点确定");
                                    gotoInputIdentify(AccoutActivity.this, zoneCode, tel, ORIGIN_MODIFY_PSW);
                                }
                            }))).show();
        }
    }

    public void getData() {
        String param = new StringBuffer()
                .append("type=getData")
                .append("&devCode=").append(XGPushServer.getXGToken(this)).toString();
        ReqInternet.in().doPost(StringManager.api_getUserInfo, param, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> user_info = StringManager.getFirstMap(returnObj);
                    tel = user_info.get("tel");
                    zoneCode = user_info.get("phoneZone");
                    shwoPhoneNum();
                } else
                    setFailClickListener();
            }
        });

        ReqInternet.in().doPost(StringManager.api_getThirdBind, "", new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> thirdBind_info = StringManager.getFirstMap(returnObj);
                    setBindInfo(thirdBind_info);
                } else
                    setFailClickListener();
            }
        });
    }

    private void setFailClickListener(){
        loadManager.setFailClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadManager.hideLoadFaildBar();
                if (handler != null)
                    handler.sendEmptyMessage(ONREFRESH);
            }
        });
    }

    private void setBindInfo(Map<String, String> info) {
        if(info == null) return;

        bindMap.clear();
        String bindStr;
        bindStr = parseData("email", info);
        if (TextUtils.isEmpty(bindStr)) {
            view_accout_below.setVisibility(View.GONE);
        } else {
            view_email_accout.setRightText(bindStr);
            view_accout_below.setVisibility(View.VISIBLE);
        }

        bindStr = parseData("weibo", info);
        view_weibo.setRightText(TextUtils.isEmpty(bindStr) ? unbindStr : bindStr);
        bindStr = parseData("weixin", info);
        view_weixin.setRightText(TextUtils.isEmpty(bindStr) ? unbindStr : bindStr);
        bindStr = parseData("qq", info);
        view_qq.setRightText(TextUtils.isEmpty(bindStr) ? unbindStr : bindStr);

        bindStr = parseData("meizu", info);
        if (TextUtils.isEmpty(bindStr)) {
            view_meizu.setVisibility(View.GONE);
        } else {
            view_meizu.setRightText(bindStr);
            view_meizu.setVisibility(View.VISIBLE);
        }
    }

    private String parseData(String type, Map<String, String> info) {
        String str = info.get(type);
        if (TextUtils.isEmpty(str))
            return null;
        Map<String, String> map = StringManager.getFirstMap(str);
        if (map.size() < 1)
            return null;

        if ("2".equals(map.get("status")) && !TextUtils.isEmpty(map.get("name"))) {
            String name = map.get("name");
            bindMap.put(type, name);
            return name;
        } else
            return null;
    }



    private void showUnbindThirdParty(final String type) {
        String title = "";
        String tongjiStr = "";

        switch (type) {
            case "email": title = "邮箱"; break;
            case "qq": title = "QQ号"; break;
            case "weibo": title = "微博号"; break;
            case "weixin": title = "微信号"; break;
            case "meizu": title = "魅族号"; break;
            default:
                break;
        }

        tongjiStr = "邮箱".equals(title) ? "解绑邮箱" : title;

        if (TextUtils.isEmpty(tel) && bindMap.size() == 1) {
            final String finalTitle1 = tongjiStr;
            final DialogManager dialogManager = new DialogManager(AccoutActivity.this);
            dialogManager.createDialog(new ViewManager(dialogManager)
                    .setView(new TitleMessageView(AccoutActivity.this).setText("该账号为唯一登录方式，\n绑定手机号才能解绑"))
                    .setView(new HButtonView(AccoutActivity.this)
                            .setNegativeTextColor(Color.parseColor("#007aff"))
                            .setNegativeText("我知道了", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogManager.cancel();
                                    XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, finalTitle1, "解绑失败原因：是唯一登录方式");
                                }
                            }))).show();
            return;
        }

        final String finalTitle = tongjiStr;
        final DialogManager dialogManager = new DialogManager(AccoutActivity.this);
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleMessageView(AccoutActivity.this).setText("确认取消绑定该" + title + "？"))
                .setView(new HButtonView(AccoutActivity.this)
                        .setNegativeTextColor(Color.parseColor("#007aff"))
                        .setNegativeText("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogManager.cancel();
                                XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, finalTitle, "弹框解绑，选择取消");
                            }
                        })
                        .setPositiveTextColor(Color.parseColor("#007aff"))
                        .setPositiveText("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogManager.cancel();
                                if ("email".equals(type)) {
                                    XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, "解绑邮箱", "弹框解绑，选择确定");
                                    unbindEmail();
                                } else if ("meizu".equals(type)) {
                                    XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, "解绑魅族号", "弹框解绑，选择确定");
                                    unbindThirdParty(type);
                                } else {
                                    XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, type, "弹框解绑，选择确定");
                                    unbindThirdParty(type);
                                }
                            }
                        }))).show();
    }

    private void unbindEmail() {
        loadManager.showProgressBar();
        String params = "email=" + bindMap.get("email");
        ReqInternet.in().doPost(StringManager.api_unbindEmail, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object msg) {
                loadManager.hideProgressBar();
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> listReturn = StringManager.getListMapByJson(msg);
                    if (listReturn.size() > 0) {
                        Map<String, String> map = listReturn.get(0);
                        if ("2".equals(map.get("result"))) {
                            Tools.showToast(AccoutActivity.this, "解绑成功");
                            XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, "解绑邮箱", "解绑成功");
                            getData();
                        } else {
                            XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, "解绑邮箱", "解绑失败");
                            Tools.showToast(AccoutActivity.this, "解绑失败，" + map.get("reason"));
                        }

                    }
                } else
                    showLoadFaildBar();
            }
        });
    }

    private void unbindThirdParty(final String type) {
        loadManager.showProgressBar();
        String params = "thiredPartyName=" + type;
        ReqInternet.in().doPost(StringManager.api_unbindThirdParty, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object msg) {
                loadManager.hideProgressBar();
                String tempStr = "meizu".equals(type) ? "tempStr" : type;
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> map = StringManager.getFirstMap(msg);
                    if ("2".equals(map.get("result"))) {
                        getData();
                        Tools.showToast(AccoutActivity.this, "解绑成功");
                        XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, tempStr, "解绑成功");
                    } else {
                        Tools.showToast(AccoutActivity.this, "解绑失败，" + map.get("reason"));
                        XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, tempStr, "解绑失败");
                    }
                } else {
                    XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, tempStr, "解绑失败");
                    showLoadFaildBar();
                }
            }
        });
    }

    private void showLoadFaildBar(){
        loadManager.showLoadFaildBar();
        loadManager.setFailClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadManager.hideLoadFaildBar();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_accout:
                if (TextUtils.isEmpty(tel)) {
                    gotoBindPhoneNum(this);
                } else {
                    gotoChangePhone(this, zoneCode, tel);
                }
                break;
            case R.id.tv_help:
                gotoFeedBack();
                break;
            case R.id.title_rela_all:
            case R.id.back:
                onPressTopBar();
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (loadManager.isShowingProgressBar()) {
            loadManager.hideProgressBar();
        } else {
            finish();
        }
    }

    public void loginByThrid(int flag, String name) {
        switch (flag) {
            case LOGIN_QQ:
                mPlatformName = "QQ";
                thirdAuth(ShareTools.QQ_NAME, name);
                break;
            case LOGIN_WX:
                int number = ToolsDevice.isAppInPhone(this, "com.tencent.mm");
                if (number == 0)
                    Tools.showToast(this, "需安装微信客户端才可以登录...");
                else {
                    mPlatformName = "微信";
                    thirdAuth(ShareTools.WEI_XIN, name);
                }
                break;
            case LOGIN_WB:
                mPlatformName = "新浪";
                thirdAuth(ShareTools.SINA_NAME, name);
                break;
        }
    }

    /** 授权。如果授权成功，则获取用户信息</br> */
    public void thirdAuth(final String platform, final String type) {
        loadManager.showProgressBar();
//        Tools.showToast(mAct, "授权开始...");
        Platform pf = ShareSDK.getPlatform(platform);
        if (pf.isAuthValid()) {
            pf.removeAccount(true);
        }
        //false为客户端   true为网页版
        pf.SSOSetting(false);
        //设置监听
        pf.setPlatformActionListener(new PlatformActionListener() {

            @Override
            public void onError(Platform arg0, int arg1, Throwable arg2) {
                AccoutActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, type, "绑定失败");
//                        Tools.showToast(AccoutActivity.this, "授权出错...");
                        loadManager.hideProgressBar();
                        LogManager.reportError("用户授权出错", null);
                    }
                });
            }

            @Override
            public void onComplete(final Platform plat, final int action, final HashMap<String, Object> res) {
                AccoutActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadManager.hideProgressBar();
                        String param = "";
                        if (action == Platform.ACTION_USER_INFOR) {
                            String devCode = XGPushServer.getXGToken(mAct);
                            PlatformDb plfDb = plat.getDb();
                            StringBuffer stringBuffer = new StringBuffer().append("type=thirdLogin")
                                    .append("&devCode=").append(devCode)
                                    .append("&p1=").append(plfDb.getToken())
                                    .append("&p2=").append(plfDb.getUserId())
                                    .append("&p3=").append(mPlatformName)
                                    .append("&p4=").append(plfDb.getUserName())
                                    .append("&p5=").append(plfDb.getUserIcon())
                                    .append("&p6=").append(getGender(plfDb.getUserGender()));
                            if (platform.equals(ShareTools.WEI_XIN)) {
                                stringBuffer.append("&p7=").append(res.get("unionid").toString());
                            }
                            param = stringBuffer.toString();
                            LogManager.print("d", "---------第三方用户信息----------" + res.toString());
                        }
                        //
                        if (param.equals("")) {
                            Tools.showToast(AccoutActivity.this, mPlatformName + "平台获取信息失败");
                            loadManager.hideProgressBar();
                            return;
                        }

                        final String finalParam = param;
                        ReqInternet.in().doPost(StringManager.api_getUserInfo, finalParam.toString(),
                                new InternetCallback() {

                                    @Override
                                    public void loaded(int flag, String url, Object returnObj) {
                                        if (flag >= ReqInternet.REQ_OK_STRING) {
                                            XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, type, "绑定成功");
                                            getData();
                                        } else {
                                            XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, type, "绑定失败");
                                            XHClick.mapStat(AccoutActivity.this, TAG_ACCOCUT, type, "绑定失败原因：已被绑定过");
                                        }
                                        mAct.loadManager.hideProgressBar();
                                    }
                                });
                    }
                });
            }

            @Override
            public void onCancel(Platform arg0, int arg1) {
                Tools.showToast(AccoutActivity.this, "登录失败");
                loadManager.hideProgressBar();
            }
        });
        pf.showUser(null);
    }

    private static String getGender(String gender) {
        if ("m".equals(gender))//男
            return "2";
        else if ("f".equals(gender))//女
            return "3";
        else //默认为中性
            return "1";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }
}
