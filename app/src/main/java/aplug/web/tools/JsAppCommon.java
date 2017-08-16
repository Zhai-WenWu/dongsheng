package aplug.web.tools;

import android.app.Activity;
import android.content.Intent;
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

import com.xianghatest.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.dialogManager.PushManager;
import acore.dialogManager.VersionOp;
import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.PayCallback;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.answer.activity.AnswerEditActivity;
import amodule.answer.activity.AskEditActivity;
import amodule.answer.activity.QAReportActivity;
import amodule.dish.activity.MoreImageShow;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.db.DataOperate;
import amodule.dish.db.DishOffSqlite;
import amodule.dish.view.DishWebView;
import amodule.other.activity.PlayVideo;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.ChooseDish;
import amodule.user.activity.login.LoginByAccout;
import amodule.user.db.BrowseHistorySqlite;
import amodule.user.db.HistoryData;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.imageselector.ImgWallActivity;
import aplug.web.view.XHWebView;
import third.mall.activity.ShoppingActivity;
import third.mall.alipay.MallAlipay;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.dialog.FavorableDialog;
import third.mall.wx.WxPay;
import third.share.BarShare;
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
     * 设置特殊返回控制
     *
     * @param backAction goback webview原生返回
     *                   以back_开头 返回使用back_后的JS
     *                   JS 返回后，在之前页面执行JS
     *                   no 返回后，在之前页面不执行操作
     */
    @android.webkit.JavascriptInterface
    public void setGoBack(String backAction) {
        JSAction.backAction = backAction;
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
//				barShare.setShare(BarShare.IMG_TYPE_WEB, am.get("title"), content, am.get("img"), zhishiurl);
//				if (act.barShare == null) {//由于二级页面也会吊起分享,但是分享内容不同,所以不加判断.防止分享内容改不了
                if (title != "" && content != "" && img != "" && url != "" && type != "") {
                    Log.i("zhangyujian", "type::::" + type);
                    mBarShare = new BarShare(mAct, type, "");
                    mBarShare.setShare(BarShare.IMG_TYPE_WEB, title, content, img, url);
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
                                        ReqInternet.in().doGet(StringManager.apiUrl + callback, new InternetCallback(mAct) {
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
//				}
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
     * 初始化收藏
     *
     * @param code     : 详情的code，例如小知识的code
     * @param isFavStr ：是否收藏，2 为已收藏
     * @param clickUrl ：点击收藏按钮请求的url
     * @param params   ：点击收藏按钮请求的url时的参数
     * @param eventID  : 点击统计id
     * @param statKey  ：二级统计名称
     */
    @JavascriptInterface
    public void initFav(final String code, final String isFavStr, final String clickUrl, final String params, final String eventID, final String statKey) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //设置收藏按钮图片
                boolean isFav = "2".equals(isFavStr);
                final ImageView favoriteNousImageView = (ImageView) mAct.findViewById(R.id.img_fav);
                favoriteNousImageView.setImageResource(isFav ? R.drawable.z_caipu_xiangqing_topbar_ico_fav_active
                        : R.drawable.z_caipu_xiangqing_topbar_ico_fav);
                final TextView favoriteNousTextView = (TextView) mAct.findViewById(R.id.tv_fav);
                favoriteNousTextView.setText(isFav ? "已收藏" : "  收藏  ");
                RelativeLayout favLayout = (RelativeLayout) mAct.findViewById(R.id.fav_layout);
                favLayout.setVisibility(View.VISIBLE);
                favLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (LoginManager.isLogin()) {
                            ReqInternet.in().doPost(clickUrl, params, new InternetCallback(mAct) {
                                @Override
                                public void loaded(int flag, String url, Object returnObj) {
                                    if (flag >= ReqInternet.REQ_OK_STRING) {
                                        Map<String, String> map = StringManager.getListMapByJson(returnObj).get(0);
                                        boolean nowFav = map.get("type").equals("2");
                                        String dishJson = DataOperate.buyBurden(context, code);
                                        if (dishJson.length() > 10 && dishJson.contains("\"makes\":")) {
                                            // 修改splite数据
                                            dishJson = dishJson.replace(nowFav ? "\"isFav\":1" : "\"isFav\":2", nowFav ? "\"isFav\":2" : "\"isFav\":1");
                                            DishOffSqlite sqlite = new DishOffSqlite(context);
                                            sqlite.updateIsFav(code, dishJson);
                                        }
                                        //7.29新添加统计
                                        XHClick.mapStat(mAct, eventID, statKey, nowFav ? "收藏成功" : "取消收藏");
                                        favoriteNousImageView.setImageResource(nowFav ? R.drawable.z_caipu_xiangqing_topbar_ico_fav_active
                                                : R.drawable.z_caipu_xiangqing_topbar_ico_fav);
                                        favoriteNousTextView.setText(nowFav ? "已收藏" : "  收藏  ");
                                    }
                                }
                            });
                        } else {
                            Intent intent = new Intent(mAct, LoginByAccout.class);
                            mAct.startActivity(intent);
                        }
                    }
                });
            }
        });
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

    private JSONObject handlerJSONData(int type, Map<String, String> map) {
        JSONObject jsonObject = new JSONObject();
        switch (type) {
            case 1: //小知识
                try {
                    jsonObject.put("allClick", map.get("allClick") + "");
                    jsonObject.put("code", map.get("code") + "");
                    jsonObject.put("content", map.get("content") + "");
                    jsonObject.put("title", map.get("title") + "");
                    jsonObject.put("img", map.get("img") + "");
                } catch (Exception e) {
                }
                break;
        }
        return jsonObject;
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
                                        ;
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
                    MallReqInternet.in().doPost(MallStringManager.mall_getAShopCoupon, param, new MallInternetCallback(mAct) {

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
     * @param title 标题
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
                if (name != "" && name != null) {
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
//		Tools.showToast(mAct,"login");
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
                    VersionOp.getInstance().toUpdate(mLoadManager, true);
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
                String urlTemp = url;
                Log.i("tzy", "videourl = " + url);
                it.putExtra("url", urlTemp);
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
                for (String url : imageUrls) {
                    data.add(url);
                }
                Intent intent = new Intent(mAct, ImgWallActivity.class);
                intent.putStringArrayListExtra("images", data);
                intent.putExtra("index", index);
                mAct.startActivity(intent);
            }
        });
    }

    @JavascriptInterface
    public void showDishStep(final String[] imgArr, final String[] stepArr, final int index) {
        Log.i("zyj","showDishStep");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (imgArr == null || stepArr == null) {
                    return;
                }
                ArrayList<Map<String, String>> listMaps = new ArrayList<Map<String, String>>();
                int sizeImg = imgArr.length;
                int sizeText = stepArr.length;
                int size = sizeText > sizeImg ? sizeText : sizeImg;
                for (int i = 0; i < size; i++) {
                    Map<String, String> map = new HashMap<String, String>();
                    if (i < sizeImg) map.put("img", imgArr[i]);
                    if (i < sizeText) map.put("info", stepArr[i]);
                    listMaps.add(map);
                }
                if (listMaps.size() > 0) {
                    Intent intent = new Intent(mAct, MoreImageShow.class);
                    intent.putExtra("data",listMaps);
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
                    params.height=(int) (height * mAct.getResources().getDisplayMetrics().density);
                    params.width=mAct.getResources().getDisplayMetrics().widthPixels;
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
            ReqInternet.in().doPost(url, params, new InternetCallback(mAct) {
                @Override
                public void loaded(int i, String s, Object data) {
                    onAcceptCallback(i >= ReqInternet.REQ_OK_STRING, data);
                }
            });
        }
    }

    private void onAcceptCallback(final boolean res, final Object data) {
        if (mAct != null && mWebView != null) {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("Javascript:onAcceptCallback(" + res + "," + data + ")");
                }

            });
        }
    }


    @JavascriptInterface
    public void goPay(String url, String params, final String type) {
//		Tools.showToast(mAct,"url:"+url);
//		Log.i("FRJ","goPay() url: " + url + "  params:" + params + "  tpye:" + type);
        PayCallback.setPayCallBack(new PayCallback.OnPayCallback() {
            @Override
            public void onPay(boolean isOk, Object data) {
                onPayCallback(isOk, data);
            }
        });
        params += "&userCode=" + LoginManager.userInfo.get("code");
        url = StringManager.apiUrl + url;
        Log.i("FRJ", "goPay() url: " + url + "  params:" + params + "  tpye:" + type);
        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback(mAct) {
            @Override
            public void loaded(int i, String s, Object data) {

                Log.i("FRJ", "string = " + s + "  data = " + data);

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
//		Tools.showToast(mAct,"onPayCallback() isOk:" + isOk);
//		Log.i("FRJ","onPayCallback() isOk:" + isOk + "  data: " + data);
        if (mAct != null && mWebView != null) {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    Log.i("FRJ", "onPayCallback() isOk:" + isOk + "  data: " + data);
//					StringManager.getListMapByJson(data);
                    String newData = String.valueOf(data);

                    String mm = "Javascript:onPayCallback(" + isOk + ",\"" + newData + "\")";
                    Log.i("FRJ", "onPayCallback() mm:" + mm);
                    mWebView.loadUrl("Javascript:onPayCallback(" + isOk + ",\"" + newData + "\")");
                    if (mOnPayFinishListener != null) {
                        mOnPayFinishListener.onPayFinish(isOk, data);
                    }
                }
            });
        }
    }

    @JavascriptInterface
    public void onLoadFinishCallback(String data) {
        Log.i(DishWebView.TAG, "onLoadFinishCallback() data:" + data);
//		Tools.showToast(mAct,"onLoadFinishCallback()");
        if (mWebView != null && mWebView instanceof DishWebView)
            ((DishWebView) mWebView).onLoadFinishCallback(data);
    }

    @JavascriptInterface
    public void setIngreStr(String ingreStr) {
//		Tools.showToast(mAct,"setIngreStr():" + ingreStr);
        if (mWebView != null && mWebView instanceof DishWebView)
            ((DishWebView) mWebView).setIngreStr(ingreStr);
    }

    @JavascriptInterface
    public String getSign() {
        return ReqEncyptInternet.in().getEncryptParam();
//        mWebView.loadUrl("Javascript:signCallback(\"" + ReqEncyptInternet.in().getEncryptParam() + "\")");
    }
    @JavascriptInterface
    public String getCookie(){
        try {
                    //cookie结构json
                    Map<String, String> header = ReqInternet.in().getHeader(mAct);
                    String cookieStr = header.containsKey("Cookie") ? header.get("Cookie") : "";
                    Map<String, String> mapdata = StringManager.getMapByString(cookieStr, ";", "=");
                    JSONObject jsonObject = new JSONObject();
                    for (Map.Entry<String, String> entry : mapdata.entrySet()) {
                        jsonObject.put(entry.getKey(), entry.getValue());
                    }
                    String data= jsonObject.toString();
                    data=data.replace("\"","\\\"");
                    return data;
                }catch (Exception e){
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

    @JavascriptInterface
    public void goAnswer(String dishId, String authorId, String qaId, String answerCode, String qaTitle, String isAnswerMore) {
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

    @JavascriptInterface
    public void goAsk(String dishId, String authorId, String qaId, String answerCode, String isAskMore) {
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
	/**
	 * 问答举报
	 * @param nickName 被举报人的昵称
	 * @param authorCode 菜谱作者的code
	 * @param qaCode 问答code
	 * @param askAuthorCode 提问人的code
	 * @param dishCode 菜谱code
	 */
	@JavascriptInterface
	public void report(String nickName, String authorCode, String qaCode, String askAuthorCode, String dishCode) {
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

    @JavascriptInterface
    public void closePayWeb() {
        if (mAct instanceof AskEditActivity) {
            ((AskEditActivity) mAct).closePayWindow();
        }
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
        PushManager.requestPermission();
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
                initShare(title, content, img, url, type, callback);
                if (mBarShare != null) {
                    mBarShare.openShareNewActivity();
                }
            }
        });
    }

}
