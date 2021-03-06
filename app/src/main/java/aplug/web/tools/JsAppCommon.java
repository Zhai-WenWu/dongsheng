package aplug.web.tools;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.popdialog.util.PushManager;
import com.xiangha.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.PayCallback;
import acore.logic.VersionOp;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.XHApplication;
import acore.override.activity.base.WebActivity;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.observer.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.answer.activity.AnswerEditActivity;
import amodule.answer.activity.AskEditActivity;
import amodule.answer.activity.QAReportActivity;
import amodule.dish.activity.DetailDishWeb;
import amodule.dish.activity.MoreImageShow;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.other.activity.PlayVideo;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.ChooseDish;
import amodule.user.activity.login.LoginByAccout;
import amodule.user.activity.login.LoginByBindPhone;
import amodule.user.db.BrowseHistorySqlite;
import amodule.user.db.HistoryData;
import amodule.vip.DeviceVipManager;
import amodule.vip.DeviceVipStatModel;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.basic.XHInternetCallBack;
import aplug.imageselector.ImgWallActivity;
import aplug.web.ShowWeb;
import aplug.web.view.XHWebView;
import cn.sharesdk.framework.Platform;
import third.mall.activity.EvalutionListActivity;
import third.mall.activity.ShoppingActivity;
import third.mall.alipay.MallAlipay;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.dialog.FavorableDialog;
import third.mall.wx.WxPay;
import third.push.xg.XGPushServer;
import third.share.BarShare;
import third.share.BarShareImage;
import third.share.tools.ShareImage;
import third.share.tools.ShareTools;
import xh.basic.tool.UtilFile;

import static amodule.dish.activity.upload.UploadDishActivity.DISH_TYPE_KEY;
import static amodule.dish.activity.upload.UploadDishActivity.DISH_TYPE_VIDEO;

public class JsAppCommon extends JsBase {
    public Activity mAct;
    private XHWebView mWebView;
    private LoadManager mLoadManager = null;
    private BarShare mBarShare = null;
    public static boolean isReloadWebView = false;
    private String url;
    public String payType = "";//支付业务类型
    public static final String PAY_TYPE_VIP_OPEN="openVip";//支付vip类型--开通
    public static final String PAY_TYPE_VIP_RENEW="vipRenew";//支付vip类型--续费

    public JsAppCommon(Activity activity, XHWebView webView, LoadManager loadManager, BarShare barShare) {
        this.mAct = activity;
        this.mWebView = webView;
        this.mLoadManager = loadManager;
        this.mBarShare = barShare;
        TAG = "appCommon";
    }

    // 打开loading
    @JavascriptInterface
    public void openShadow() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mLoadManager != null) {
                    mLoadManager.showProgressBar();
                    mLoadManager.showProgressShadow();
                }
            }
        });
    }

    /**
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    // 返回按钮，可写js来做返回
    @JavascriptInterface
    public void goBack(final String backAction) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setGoBack(backAction);
                mAct.onBackPressed();
            }
        });
    }

    // 刷新按钮
    @JavascriptInterface
    public void refresh() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(mWebView.getmUrl());
            }
        });
    }

    /**
     * 设置标题头
     *
     * @param title
     */
    @JavascriptInterface
    public void setTitle(final String title) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView titleV = (TextView) mAct.findViewById(R.id.title);
                if (titleV != null && title.length() > 0) titleV.setText(title);
            }
        });
    }

    /**
     * 保持菜谱详情的浏览记录
     *
     * @param burden
     * @param allClick
     * @param favrites
     * @param nickName
     */
    @JavascriptInterface
    public void setIngreStr(final String burden, final String allClick, final String favrites, final String nickName) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mAct != null && mAct instanceof DetailDishWeb)
                    ((DetailDishWeb) mAct).savaJsAdata(burden, allClick, favrites, nickName);
            }
        });
    }

    /**
     * 设置特殊返回控制
     *
     * @param backAction goback webview原生返回
     *                   以back_开头 返回使用back_后的JS
     *                   JS 返回后，在之前页面执行JS
     *                   no 返回后，在之前页面不执行操作
     *                   finish 返回后，直接关闭当前页面
     */
    @android.webkit.JavascriptInterface
    public void setGoBack(String backAction) {
        JSAction.backAction = backAction;
    }

    //打开分享
    @JavascriptInterface
    public void openShare() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mBarShare != null) {
                    mBarShare.openShare();
                }
            }
        });
    }

    /**
     * 初始化分享按钮,并添加分享按钮.
     * title：        分享标题
     * content：  分享内容
     * img：          分享图片
     * url:	   分享链接地址
     * type：        分享类型
     * callback:    回调统计
     */
    @JavascriptInterface
    public void initShare(final String title, final String content, final String img, final String url, final String type, final String callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                initShareBar(title, content, img, url, type, callback, "", "");
            }
        });
    }

    /**
     * 初始化分享按钮,并添加分享按钮.
     * title：        分享标题
     * content：  分享内容
     * img：          分享图片
     * url:	   分享链接地址
     * type：        分享统计类型
     * callback:    回调统计
     * shareType:   分享类型 1：普通分享  2：分享小程序
     * path:        小程序路径
     */
    @JavascriptInterface
    public void initShare(final String title, final String content, final String img, final String url, final String type, final String callback, final String shareType, final String path) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                initShareBar(title, content, img, url, type, callback, shareType, path);
            }
        });
    }

    private void initShareBar(final String title, final String content, final String img, final String url, final String type, final String callback, final String shareType, final String path) {
        if (mAct instanceof WebActivity) {
            ((ShowWeb) mAct).shareCallback = callback;
        }

        if (title != "" && content != "" && img != "" && url != "" && type != "") {
            Log.i("zhangyujian", "type::::" + type);
            mBarShare = new BarShare(mAct, type, "");
            mBarShare.setShare(BarShare.IMG_TYPE_WEB, title, content, img, url);
            mBarShare.setShareProgram(transferData(title, content, img, url, type, shareType, path));
            RelativeLayout shareLayout = (RelativeLayout) mAct.findViewById(R.id.shar_layout);
            if (shareLayout != null) {
                shareLayout.setVisibility(View.VISIBLE);
                shareLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setStiaticType(type);
                        if (mBarShare != null) {
                            mBarShare.openShare();
                            if (callback != null && callback.length() > 0) {
                                ReqInternet.in().doGet(StringManager.apiUrl + callback, new InternetCallback() {
                                    @Override
                                    public void loaded(int flag, String url, Object returnObj) {
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 直接打开一个中间显示的分享页面
     * title：        分享标题
     * content：  分享内容
     * img：          分享图片
     * url:	   分享链接地址
     * type：        分享类型
     * callback:    回调统计
     */
    @JavascriptInterface
    public void openShareNew(final String title, final String content, final String img, final String url, final String type, final String callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                startShareNew(title, content, img, url, type, callback, "", "");
            }
        });
    }

    /**
     * 直接打开一个中间显示的分享页面
     * title：        分享标题
     * content：  分享内容
     * img：          分享图片
     * url:	   分享链接地址
     * type：        分享类型
     * callback:    回调统计
     * shareType:   分享类型 1：普通分享  2：分享小程序
     * path:        小程序路径
     */
    @JavascriptInterface
    public void openShareNew(final String title, final String content, final String img, final String url, final String type, final String callback, final String shareType, final String path) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                startShareNew(title, content, img, url, type, callback, shareType, path);
            }
        });
    }

    private String transferData(final String title, final String content, final String img, final String url, final String type, final String shareType, final String path) {
        String retStr = null;
        JSONObject object = new JSONObject();
        JSONObject entityObject = new JSONObject();
        JSONObject confObject = new JSONObject();
        try {
            object.put("shareType", transferStr(shareType));
            entityObject.put("shareType", transferStr(shareType));
            confObject.put("title", transferStr(title));
            confObject.put("content", transferStr(content));
            confObject.put("img", transferStr(img));
            confObject.put("url", transferStr(url));
            confObject.put("type", transferStr(type));
            confObject.put("path", transferStr(path));
            entityObject.put(shareType, confObject);
            object.put("shareConfig", entityObject);
            retStr = object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retStr;
    }

    private String transferStr(String str) {
        return TextUtils.isEmpty(str) ? "" : str;
    }

    private void startShareNew(final String title, final String content, final String img, final String url, final String type, final String callback, final String shareType, final String path) {
        if (mAct instanceof WebActivity && !TextUtils.isEmpty(callback)) {
            ((WebActivity)mAct).shareCallback = callback;
        }
        if (title != "" && content != "" && img != "" && url != "" && type != "") {
            mBarShare = new BarShare(mAct, type, "");
            mBarShare.setShare(BarShare.IMG_TYPE_WEB, title, content, img, url);
            mBarShare.setShareProgram(transferData(title, content, img, url, type, shareType, path));
            mBarShare.openShare();
        }
    }

    @JavascriptInterface
    public void initShareImage(final String imageUrl, final String callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mAct instanceof WebActivity && !TextUtils.isEmpty(callback)) {
                    ((ShowWeb) mAct).shareCallback = callback;
                }
                RelativeLayout shareLayout = (RelativeLayout) mAct.findViewById(R.id.shar_layout);
                if (shareLayout != null) {
                    shareLayout.setVisibility(View.VISIBLE);
                    shareLayout.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new BarShareImage(mAct, imageUrl).openShareImage();
                        }
                    });
                }
            }
        });
    }

    @JavascriptInterface
    public void openShareImage(final String imageUrl, final String callback) {
        initShareImage(imageUrl, callback);
        handler.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout shareLayout = (RelativeLayout) mAct.findViewById(R.id.shar_layout);
                if (shareLayout != null) {
                    shareLayout.performClick();
                } else {
                    //若为null则直接分享，以免说这是bug
                    new BarShareImage(mAct, imageUrl).openShareImage();
                }
            }
        });
    }

    /**
     * 设置统计类型
     */
    private void setStiaticType(String type) {
        String key = "";
        if (type.equals("知识详情")) {
            key = "分享知识";
        } else if (type.equals("香哈商城") || type.equals("店铺")) {
            key = "分享商品";
        }
        if (!TextUtils.isEmpty(key)) XHClick.track(mAct, key);
    }

    /**
     * 添加小知识浏览记录
     *
     * @param code     : 小知识code
     * @param title
     * @param content
     * @param img
     * @param allClick
     */
    @JavascriptInterface
    public void setNouseHistory(final String code, final String title, final String content, final String img, final String allClick) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HistoryData data = new HistoryData();
                data.setCode(code);
                data.setBrowseTime(System.currentTimeMillis());
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("code", code);
                    jsonObject.put("title", title);
                    jsonObject.put("content", content);
                    jsonObject.put("img", img);
                    jsonObject.put("allClick", allClick);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                data.setDataJson(jsonObject.toString());
                BrowseHistorySqlite sqlite = new BrowseHistorySqlite(mAct);
                sqlite.insertSubject(BrowseHistorySqlite.TB_NOUS_NAME, data);
            }
        }).start();
    }

    /**
     * 购物车显示
     */
    @JavascriptInterface
    public void showCart() {
        handler.post(new Runnable() {

            @Override
            public void run() {
                //购物车
                RelativeLayout shoppingLayout = (RelativeLayout) mAct.findViewById(R.id.shopping_layout_mall);
                shoppingLayout.setVisibility(View.VISIBLE);
                ImageView shopCartImage = (ImageView) mAct.findViewById(R.id.image_cart);
                TextView mall_news_num = (TextView) mAct.findViewById(R.id.mall_news_num_mall);
                TextView mall_news_num_two = (TextView) mAct.findViewById(R.id.mall_news_num_two_mall);
                if (MallCommon.num_shopcat > 0) {
                    if (MallCommon.num_shopcat > 9) {
                        mall_news_num.setVisibility(View.GONE);
                        mall_news_num_two.setVisibility(View.VISIBLE);
                        if (MallCommon.num_shopcat > 99)
                            mall_news_num_two.setText("99+");
                        else
                            mall_news_num_two.setText("" + MallCommon.num_shopcat);
                    } else {
                        mall_news_num.setVisibility(View.VISIBLE);
                        mall_news_num_two.setVisibility(View.GONE);
                        mall_news_num.setText("" + MallCommon.num_shopcat);
                    }
                } else {
                    mall_news_num.setVisibility(View.GONE);
                    mall_news_num_two.setVisibility(View.GONE);
                }
                if (LoginManager.isLogin()) {
                    MallCommon.getShoppingNum(mAct, mall_news_num, mall_news_num_two);
                }
                shopCartImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (LoginManager.userInfo.size() == 0) {
                            Intent intent = new Intent(mAct, LoginByAccout.class);
                            mAct.startActivity(intent);
                        } else {
                            XHClick.mapStat(mAct, "a_mail_h5", "购物车", "");
//							String openurl="xhds.cart.getCartInfo.app";
                            if (!TextUtils.isEmpty(url)) {
                                if (url.contains("?")) {
                                    if (url.contains("&fr")) {
                                        String openurls = url.substring(0, url.indexOf("&fr"));
                                        MallClickContorl.getInstance().setStatisticUrl(openurls, null, url.substring(url.indexOf("&fr") + 1, url.length()), mAct, true);

                                        String mall_stat = (String) UtilFile.loadShared(mAct, FileManager.MALL_STAT, FileManager.MALL_STAT);

                                    }
                                }
                            }
                            Intent intent = new Intent(mAct, ShoppingActivity.class);
                            mAct.startActivity(intent);
                        }
                    }
                });
                mAct.findViewById(R.id.leftText).setVisibility(View.GONE);
                RelativeLayout homeLayout = (RelativeLayout) mAct.findViewById(R.id.mall_home_layout);
                homeLayout.setVisibility(View.GONE);
                homeLayout.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        XHClick.mapStat(mAct, "a_mail_h5", "回首页", "");
                        AppCommon.openUrl(mAct, "xhds.home.app", true);
                        mAct.finish();
                    }
                });
            }
        });
    }

    /**
     * 优惠券
     *
     * @param shop_code
     */
    @JavascriptInterface
    public void getShopCoupon(final String shop_code) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (LoginManager.isLogin()) {
                    FavorableDialog dialog = new FavorableDialog(mAct, shop_code);
                } else {
                    Intent intent_user = new Intent(mAct, LoginByAccout.class);
                    mAct.startActivity(intent_user);
                }
            }
        });
    }

    /**
     * 获取单个优惠券
     *
     * @param code
     */
    @JavascriptInterface
    public void getOneFavorableCoupon(final String code) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (LoginManager.isLogin()) {
                    String param = "shop_coupon_package_code=" + code;
                    MallReqInternet.in().doPost(MallStringManager.mall_getAShopCoupon, param, new MallInternetCallback() {

                        @Override
                        public void loadstat(int flag, String url, Object msg, Object... stat) {
                            if (flag >= ReqInternet.REQ_OK_STRING) {
                                Tools.showToast(mAct, "领取成功");
                                mWebView.loadUrl(mWebView.getUrl());
                            } else if (flag == ReqInternet.REQ_CODE_ERROR && msg instanceof Map) {
                                Map<String, String> map = (Map<String, String>) msg;
                                Tools.showToast(mAct, map.get("msg"));
                            }
                        }
                    });
                } else {
                    Intent intent_user = new Intent(mAct, LoginByAccout.class);
                    mAct.startActivity(intent_user);
                }
            }
        });
    }

    /**
     * 发美食贴
     *
     * @param code  发美食贴code
     */
    @JavascriptInterface
    public void addQuan(String title, String code) {
        //统计发美食贴(计算事件)
        XHClick.onEventValue(mAct, "uploadQuan", "uploadQuan", "从网页发", 1);

        Intent intent = new Intent();
        intent.putExtra("title", title);
        intent.putExtra("dishCode", code);
        intent.setClass(mAct, UploadSubjectNew.class);
        mAct.startActivity(intent);
    }

    /**
     * 发菜谱
     * 参数  name 菜谱名
     */
    @JavascriptInterface
    public void addDish(final String name) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //统计
                XHClick.onEventValue(mAct, "uploadDish", "uploadDish ", "从网页发", 1);
                Intent intent = new Intent();
                intent.setClass(mAct, UploadDishActivity.class);
                intent.putExtra("state", UploadDishActivity.UPLOAD_DISH_ACTIVITY);
                if (!TextUtils.isEmpty(name)) {
                    intent.putExtra("name", name);
                }
                mAct.startActivity(intent);
            }
        });
    }

    /**
     * 发菜谱
     */
    @JavascriptInterface
    public void addDish() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //统计
                XHClick.onEventValue(mAct, "uploadDish", "uploadDish ", "从网页发", 1);
                Intent intent = new Intent();
                intent.setClass(mAct, UploadDishActivity.class);
                intent.putExtra("state", UploadDishActivity.UPLOAD_DISH_NEW);
                mAct.startActivity(intent);
            }
        });
    }

    /**
     * 获取认证菜谱列表
     */
    @JavascriptInterface
    public void approveDish(final String data, final String num) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mAct, ChooseDish.class);
                intent.putExtra("data", data);
                intent.putExtra("num", num);
                mAct.startActivity(intent);
            }
        });
    }

    /**
     * 用户登录
     */
    @JavascriptInterface
    public void login() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                isReloadWebView = true;
                Intent intent = new Intent(mAct, LoginByAccout.class);
                mAct.startActivity(intent);
            }
        });
    }

    /**
     * 检查更新
     */
    @JavascriptInterface
    public void checkUpdate() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mLoadManager != null) {
                    VersionOp.getInstance().toUpdate(new VersionOp.OnCheckUpdataCallback() {
                        @Override
                        public void onPreUpdate() {
                            mLoadManager.startProgress("正在获取最新版本信息");
                        }

                        @Override
                        public void onNeedUpdata() {
                            mLoadManager.dismissProgress();
                        }

                        @Override
                        public void onNotNeed() {
                            mLoadManager.dismissProgress();
                        }

                        @Override
                        public void onFail() {
                            mLoadManager.dismissProgress();
                        }
                    }, true);
                }
            }
        });
    }

    /**
     * 发菜谱(新)
     * 参数有可能是空的字符串
     */
    @JavascriptInterface
    public void addNewDish(final String title, final int id, final String name) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                //统计
                XHClick.onEventValue(mAct, "uploadDish", "uploadDish ", "从网页发", 1);
                Intent intent = new Intent();
                intent.setClass(mAct, UploadDishActivity.class);
                intent.putExtra("state", UploadDishActivity.UPLOAD_DISH_ACTIVITY);
                intent.putExtra("name", title);
                intent.putExtra("activityId", String.valueOf(id));
                intent.putExtra("removeName", name);
                mAct.startActivity(intent);
            }
        });
    }

    /**
     * 发视频菜谱
     * 参数有可能是空的字符串
     *
     * @param title      活动标题
     * @param id         ：活动id
     * @param removeName ：发布菜谱时去掉带的标题部分
     */
    @JavascriptInterface
    public void addVideoDish(final String title, final int id, final String removeName) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.putExtra(DISH_TYPE_KEY, DISH_TYPE_VIDEO);
                intent.setClass(mAct, UploadDishActivity.class);
                intent.putExtra("state", UploadDishActivity.UPLOAD_DISH_ACTIVITY);
                intent.putExtra("name", title);
                intent.putExtra("activityId", String.valueOf(id));
                intent.putExtra("removeName", removeName);
                mAct.startActivity(intent);
            }
        });
    }

    /**
     * 重开页面播放视频
     *
     * @param uu
     * @param vu
     * @param name
     * @param img
     */
    @JavascriptInterface
    public void videoShow(final String uu, final String vu, final String name, final String img) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //统计
                Intent it = new Intent(mAct, PlayVideo.class);
                it.putExtra("vu", vu);
                it.putExtra("uu", uu);
                it.putExtra("name", name);
                it.putExtra("img", img);
                mAct.startActivity(it);
            }
        });
    }

    /**
     * 重开页面播放视频
     *
     * @param url
     * @param name
     * @param img
     */
    @JavascriptInterface
    public void videoShowNew(final String url, final String name, final String img) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //统计
                Intent it = new Intent(mAct, PlayVideo.class);
                it.putExtra("url", url);
                it.putExtra("name", name);
                it.putExtra("img", img);
                mAct.startActivity(it);
            }
        });
    }

    /**
     * 打开图片墙
     *
     * @param imageUrls
     * @param index
     */
    @JavascriptInterface
    public void doShowImages(final String[] imageUrls, final int index) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (imageUrls == null) {
                    return;
                }
                ArrayList<String> data = new ArrayList<>();
                Collections.addAll(data, imageUrls);
                Intent intent = new Intent(mAct, ImgWallActivity.class);
                intent.putStringArrayListExtra("images", data);
                intent.putExtra("index", index);
                mAct.startActivity(intent);
            }
        });
    }

    @JavascriptInterface
    public void showDishStep(final String[] imgArr, final String[] stepArr, final int index) {
        Log.i("zyj", "showDishStep");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (imgArr == null || stepArr == null) {
                    return;
                }
                ArrayList<Map<String, String>> listMaps = new ArrayList<>();
                int sizeImg = imgArr.length;
                int sizeText = stepArr.length;
                int size = sizeText > sizeImg ? sizeText : sizeImg;
                for (int i = 0; i < size; i++) {
                    Map<String, String> map = new HashMap<>();
                    if (i < sizeImg) map.put("img", imgArr[i]);
                    if (i < sizeText) map.put("info", stepArr[i]);
                    listMaps.add(map);
                }
                if (listMaps.size() > 0) {
                    Intent intent = new Intent(mAct, MoreImageShow.class);
                    intent.putExtra("data", listMaps);
                    intent.putExtra("index", index);
                    intent.putExtra("from", "dish");
                    mAct.startActivity(intent);
                }
            }
        });

    }

    /**
     * 我是商家的二级页面，订单管理，右上角‘提款说明’按钮的显示与点击
     *
     * @param title
     * @param content
     */
    @JavascriptInterface
    public void withDraw_desc(final String title, final String content) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (content != "") {
                    final TextView tv_explain = (TextView) mAct.findViewById(R.id.tv_explain);
                    tv_explain.setText(title);
                    tv_explain.setVisibility(View.VISIBLE);
                    tv_explain.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AppCommon.openUrl(mAct, content, true);
                            tv_explain.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    //重新设置webview的高度
    @JavascriptInterface
    public void resize(final float height) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mAct != null && mWebView != null) {
                    ViewGroup.LayoutParams params = mWebView.getLayoutParams();
                    params.height = (int) (height * mAct.getResources().getDisplayMetrics().density);
                    params.width = mAct.getResources().getDisplayMetrics().widthPixels;
                }
            }
        });
    }

    @JavascriptInterface
    public int getStatusMes() {
//		if(mAct != null && mWebView != null && Tools.isShowTitle()) {
//			int tatusHeight = ToolsDevice.px2dp(mAct,Tools.getStatusBarHeight(mAct));
//			return tatusHeight;
//		}
        return 0;
    }

    @JavascriptInterface
    public void getSecretData(String url, String params) {
        if (mAct != null && mWebView != null) {
            url = StringManager.apiUrl + url;
            ReqInternet.in().doPost(url, params, new InternetCallback() {
                @Override
                public void loaded(int i, String s, Object data) {
                    onAcceptCallback(i >= ReqInternet.REQ_OK_STRING, data);
                }
            });
        }
    }

    private void onAcceptCallback(final boolean res, final Object data) {
        if (mAct != null && mWebView != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("Javascript:onAcceptCallback(" + res + "," + data + ")");
                }

            });
        }
    }
    @JavascriptInterface
    public void goPayBs(String url, String params, String type,String typeBs){
        if (!ToolsDevice.isNetworkAvailable(mAct)) {
            onPayCallback(false, "网络异常，请检查网络");
            return;
        }
        Log.i("xianghaTag","goPayBs() url: " + url + "  params:" + params + "  tpye:" + type+"::typeBs::"+typeBs);
        if(!TextUtils.isEmpty(typeBs)) payType = typeBs;//记录当前支付类型
        goPay(url,params,type);
    }

    @JavascriptInterface
    public void goPay(String url, String params, final String type) {
        if (!ToolsDevice.isNetworkAvailable(mAct)) {
            onPayCallback(false, "网络异常，请检查网络");
            return;
        }
//		Tools.showToast(mAct,"url:"+url);
        PayCallback.setPayCallBack(new PayCallback.OnPayCallback() {
            @Override
            public void onPay(boolean isOk, Object data) {
                onPayCallback(isOk, data);
            }
        });
        params += "&userCode=" + LoginManager.userInfo.get("code");
        url = StringManager.apiUrl + url;
        //Log.i("FRJ", "goPay() url: " + url + "  params:" + params + "  tpye:" + type);
        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object data) {

                //Log.i("FRJ", "string = " + s + "  data = " + data);

                if (i >= ReqInternet.REQ_OK_STRING) {
                    if ("1".equals(type)) { //支付宝支付
                        ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(data);
                        if (arrayList.size() > 0) {
                            MallAlipay alipay = MallAlipay.getInstance();
                            alipay.startAlipay(mAct, arrayList.get(0).get("sign"));
                        } else {
                            onPayCallback(false, "开通失败");
                        }
                    } else if ("2".equals(type)) { //微信支付
                        ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(data);
                        if (arrayList.size() > 0) {
                            String sign = arrayList.get(0).get("sign");
                            arrayList = StringManager.getListMapByJson(sign);
                            if (arrayList.size() > 0) {
                                WxPay pay = WxPay.getInstance(mAct);
                                pay.pay(arrayList.get(0));
                            } else {
                                onPayCallback(false, data);
                            }
                        } else {
                            onPayCallback(false, data);
                        }
                    } else if ("4".equals(type)) { //香豆支付
                        onPayCallback(true, data);
                    } else {
                        onPayCallback(false, data);
                    }
                } else {
                    onPayCallback(false, data);
                }
            }
        });
    }


    public void onPayCallback(final boolean isOk, final Object data) {
//		Log.i("xianghaTag","onPayCallback() isOk:" + isOk + "  data: " + data);
        if (mAct != null && mWebView != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
//					StringManager.getListMapByJson(data);
                    String newData = String.valueOf(data);

                    String mm = "Javascript:onPayCallback(" + isOk + ",\"" + newData + "\")";
                    //Log.i("FRJ", "onPayCallback() mm:" + mm);
                    mWebView.loadUrl("Javascript:onPayCallback(" + isOk + ",\"" + newData + "\")");
                    if (mOnPayFinishListener != null) {
                        mOnPayFinishListener.onPayFinish(isOk, data);
                    }
                    if (isOk) {//支付成功，如果是开通的VIP，设置VIP状态。因为无法区分此次支付是否是购买VIP，所以每次支付成功都设置一次。
                        if(PAY_TYPE_VIP_OPEN.equals(payType)||PAY_TYPE_VIP_RENEW.equals(payType)){
                            payType="";
                            payVip();//支付类型
                        }
                    }
                    ObserverManager.getInstance().notify(ObserverManager.NOTIFY_PAYFINISH, null, isOk);
                }
            });
        }
    }
    private void getUserData(){
        String params = "type=getData&devCode=" + XGPushServer.getXGToken(XHApplication.in());
        ReqInternet.in().doPost(StringManager.api_getUserInfo, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    if(XHActivityManager.getInstance().getCurrentActivity()!=null)
                    LoginManager.setDataUser(XHActivityManager.getInstance().getCurrentActivity(), returnObj);
                }
            }
        });
    }
    /**
     * 支付成功通知服务端---不可删除
     */
    private void payVip(){
        String url = StringManager.API_PAYVIP;
        ReqEncyptInternet.in().doEncypt(url, "", new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                DeviceVipManager.initDeviceVipBindState(mAct, new LoginManager.VipStateCallback() {//一元
                    @Override
                    public void callback(boolean isVip) {
                        LoginManager.setVipStateChanged();
                    }
                });
                if(LoginManager.isLogin())getUserData();//登陆状态下更改用户信息
            }
        });
    }

    @JavascriptInterface
    public String getSign() {
        return ReqEncyptInternet.in().getEncryptParam();
    }

    @JavascriptInterface
    public String getCookie() {
        try {
            //cookie结构json
            Map<String,String> mapdata= XHInternetCallBack.getCookieMap();
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, String> entry : mapdata.entrySet()) {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
            String data = jsonObject.toString();
//                    data=data.replace("\"","\\\"");
            data = Uri.encode(data);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }

    @JavascriptInterface
    public String getDsCookie() {
        try {
            //cookie结构json
            Map<String,String> mapdata= XHInternetCallBack.getCookieMap();
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, String> entry : mapdata.entrySet()) {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
            String data = jsonObject.toString();
//            data=data.replace("\"","\\\"");
            data = Uri.encode(data);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }

    public interface OnPayFinishListener {
        void onPayFinish(boolean succ, Object data);
    }

    private OnPayFinishListener mOnPayFinishListener;

    public void setOnPayFinishListener(OnPayFinishListener payFinishListener) {
        mOnPayFinishListener = payFinishListener;
    }

    /**
     * h5跳转到回答/追答编辑页面
     *
     * @param dishId       菜谱code
     * @param authorId     作者code
     * @param qaId         问答code
     * @param answerCode   回答code
     * @param qaTitle      回答的问题
     * @param isAnswerMore 是否是 追答
     */
    @JavascriptInterface
    public void goAnswer(final String dishId, final String authorId, final String qaId, final String answerCode, final String qaTitle, final String isAnswerMore) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putString("code", dishId);
                bundle.putString("authorCode", authorId);
                bundle.putString("qaCode", qaId);
                bundle.putString("answerCode", answerCode);
                bundle.putString("qaTitle", qaTitle);
                bundle.putString("mIsAnswerMore", isAnswerMore);
                Intent intent = new Intent(mAct, AnswerEditActivity.class);
                intent.putExtras(bundle);
                mAct.startActivity(intent);
            }
        });
    }

    /**
     * h5跳转到提问/追问编辑页面
     *
     * @param dishId     菜谱code
     * @param authorId   作者code
     * @param qaId       问答code
     * @param answerCode 回答code
     * @param isAskMore  是否是 追问
     */
    @JavascriptInterface
    public void goAsk(final String dishId, final String authorId, final String qaId, final String answerCode, final String isAskMore) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putString("code", dishId);
                bundle.putString("authorCode", authorId);
                bundle.putString("qaCode", qaId);
                bundle.putString("answerCode", answerCode);
                bundle.putString("isAskMore", isAskMore);
                Intent intent = new Intent(mAct, AskEditActivity.class);
                intent.putExtras(bundle);
                mAct.startActivity(intent);
            }
        });
    }

    /**
     * 问答举报
     *
     * @param nickName      被举报人的昵称
     * @param authorCode    菜谱作者的code
     * @param qaCode        问答code
     * @param askAuthorCode 提问人的code
     * @param dishCode      菜谱code
     */
    @JavascriptInterface
    public void report(final String nickName, final String authorCode, final String qaCode, final String askAuthorCode, final String dishCode) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putString("reportName", nickName);
                bundle.putString("qaCode", qaCode);
                bundle.putString("authorCode", authorCode);
                bundle.putString("askAuthorCode", askAuthorCode);
                bundle.putString("dishCode", dishCode);
                Intent intent = new Intent(mAct, QAReportActivity.class);
                intent.putExtras(bundle);
                mAct.startActivity(intent);
            }
        });
    }

    /**
     * h5页面的一些操作，可以关闭客户端的webview
     */
    @JavascriptInterface
    public void closePayWeb() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mAct instanceof AskEditActivity) {
                    ((AskEditActivity) mAct).closePayWindow();
                }
            }
        });
    }

    public interface OnGetDataListener {
        void getData(String data);
    }

    private OnGetDataListener mOnGetDataListener;

    public void setOnGetDataListener(OnGetDataListener getDataListener) {
        mOnGetDataListener = getDataListener;
    }

    @JavascriptInterface
    public void getTitleBarInfo(String jsonStr) {
        if (mOnGetDataListener != null)
            mOnGetDataListener.getData(jsonStr);
    }

    @JavascriptInterface
    public void openSysSetting() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                PushManager.requestPermission(mAct);
            }
        });
    }

    @JavascriptInterface
    public void showCommentBar(final String userName, final String userCode) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mAct instanceof EvalutionListActivity) {
                    EvalutionListActivity showWeb = (EvalutionListActivity) mAct;
                    showWeb.showCommentBar(userName, userCode);
                }
            }
        });
    }

    @JavascriptInterface
    public void initMallCloseBtn() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mAct instanceof ShowWeb) {
                    ShowWeb showWeb = (ShowWeb) mAct;
                }
            }
        });
    }

    /**
     * webview自己处理物理返回键
     *
     * @param loadUrl
     */
    @JavascriptInterface
    public void handleBackSelf(final String loadUrl) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mWebView.setBackData(loadUrl);
            }
        });
    }

    /**
     * 获取网络状态
     *
     * @return
     */
    @JavascriptInterface
    public String getNetType() {
        return ToolsDevice.getNetWorkSimpleType(mAct);
    }

    /**
     * 来源
     *
     * @param dsfrom
     */
    @JavascriptInterface
    public void dsFrom(String dsfrom) {
        if (!TextUtils.isEmpty(dsfrom)) {
            MallCommon.setStatictisFrom(dsfrom);
        }
    }

    /**
     * 一元购登录
     */
    @JavascriptInterface
    public void vipTransfer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (LoginManager.isLogin()) {
                    DeviceVipStatModel model = new DeviceVipStatModel("已登录_我的会员页面顶部或购买成功提示_成功", "已登录_我的会员页面顶部或购买成功提示_失败");
                    DeviceVipManager.bindYiYuanVIP(mAct, model);
                } else {
                    DeviceVipManager.setAutoBindDeviceVip(true);
                    Intent intent = new Intent(mAct, LoginByBindPhone.class);
                    DeviceVipStatModel model = new DeviceVipStatModel("未登录_我的会员页面顶部或购买成功提示_成功", "未登录_我的会员页面顶部提示_失败");
                    intent.putExtra(DeviceVipStatModel.TAG, model);
                    mAct.startActivity(intent);
                }
            }
        });
    }

    @JavascriptInterface
    public void inputToClipboard(String content){
        if(null == mAct || TextUtils.isEmpty(content)) return;
        handler.post(()->Tools.inputToClipboard(mAct,content));
    }

    /**
     *
     * @param title
     * @param content
     * @param img
     * @param url
     * @param type
     * @param callback
     * @param shareType
     * @param path
     * @param platformType 平台对应类型 Wechat->微信好友 WechatMoments->朋友圈 QQ->腾讯qq QZone->qq空间
     *                     SinaWeibo->新浪微博
     */
    @JavascriptInterface
    public void openShareByType(final String title, final String content, final String img, final
    String url, final String type, final String callback, final String shareType, final String
            path, final String platformType, final String apiUrl, final String params) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mAct instanceof WebActivity && !TextUtils.isEmpty(callback)) {
                    ((WebActivity)mAct).shareCallback = callback;
                }
                if (TextUtils.isEmpty(url) && TextUtils.isEmpty(title) && TextUtils.isEmpty(content) && !TextUtils.isEmpty(img)) {
                    handleShareImage(platformType, img, apiUrl, params);
                    return;
                }
                handleShareOther(title, url, content, img, type, platformType, shareType, path, apiUrl, params);
            }
        });
    }

    private void handleShareOther(String title, String url, String content, String img, String type, String platformType, String shareType, String path, String apiUrl, String params) {
        ShareTools st = ShareTools.getBarShare(mAct);
        Map<String, String> shareMap = new HashMap<>();
        shareMap.put("type", BarShare.IMG_TYPE_WEB);
        shareMap.put("title", title);
        shareMap.put("url", url);
        shareMap.put("content", content);
        shareMap.put("img", img);
        shareMap.put("from", type);
        shareMap.put("parent", "");
        shareMap.put("platform", platformType);
        String sp = transferData(title, content, img, url, type, shareType, path);
        if (!TextUtils.isEmpty(sp))
            shareMap.put("shareParams", sp);

        ShareTools.ActionListener actionListener = new ShareTools.ActionListener() {
            @Override
            public void onComplete(int optionType, int callbackType, Platform platform, String jsonStr) {
                if (!TextUtils.isEmpty(apiUrl)) {
                    String url = StringManager.apiUrl + apiUrl;
                    String p = params + "&userCode=" + LoginManager.userInfo.get("code");
                    ReqEncyptInternet.in().doEncypt(url, p, new InternetCallback() {
                        @Override
                        public void loaded(int i, String s, Object o) {
                            if (o != null)
                                st.notifyCallback(optionType, callbackType, platform, o.toString());
                        }
                    });
                } else {
                    st.notifyCallback(optionType, callbackType, platform, jsonStr);
                }
            }

            @Override
            public void onError(int optionType, int callbackType, Platform platform, String jsonStr) {
                st.notifyCallback(optionType, callbackType, platform, jsonStr);
            }

            @Override
            public void onCancel(int optionType, int callbackType, Platform platform, String jsonStr) {
                st.notifyCallback(optionType, callbackType, platform, jsonStr);
            }
        };

        st.showSharePlatform(shareMap, actionListener);
    }

    private void handleShareImage(String platformType, String img, String apiUrl, String params) {
        ShareImage si = new ShareImage(mAct);
        si.setActionListener(new ShareTools.ActionListener() {
            @Override
            public void onComplete(int optionType, int callbackType, Platform platform, String jsonStr) {
                if (!TextUtils.isEmpty(apiUrl)) {
                    String url = StringManager.apiUrl + apiUrl;
                    String p = params + "&userCode=" + LoginManager.userInfo.get("code");
                    ReqEncyptInternet.in().doEncypt(url, p, new InternetCallback() {
                        @Override
                        public void loaded(int i, String s, Object o) {
                            if (o != null)
                                si.notifyCallback(optionType, callbackType, platform, o.toString());
                        }
                    });
                } else {
                    si.notifyCallback(optionType, callbackType, platform, jsonStr);
                }
            }

            @Override
            public void onError(int optionType, int callbackType, Platform platform, String jsonStr) {
                si.notifyCallback(optionType, callbackType, platform, jsonStr);
            }

            @Override
            public void onCancel(int optionType, int callbackType, Platform platform, String jsonStr) {
                si.notifyCallback(optionType, callbackType, platform, jsonStr);
            }
        });
        si.share(platformType, img);
    }

    /**
     *
     * @param platformType platformType：平台对应类型 Wechat->微信好友 WechatMoments->朋友圈 QQ->腾讯qq
     *                     QZone->qq空间 SinaWeibo->新浪微博
     * @param callback 回调统计
     */
    @JavascriptInterface
    public void getAuthorize(final String platformType, final String callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mAct instanceof WebActivity && !TextUtils.isEmpty(callback))
                    ((WebActivity)mAct).shareCallback = callback;
                ShareTools st = ShareTools.getBarShare(mAct);
                st.requestAuthorize(platformType);
            }
        });
    }

    /**
     * 用于js获取是否开启通知权限
     * @param callback 用于iOS的回调（iOS不能直接返回参数）
     * @return
     */
    @JavascriptInterface
    public String getPushStatus(String callback) {
        return PushManager.isNotificationEnabled(XHApplication.in()) ? "2" : "1";
    }

}
