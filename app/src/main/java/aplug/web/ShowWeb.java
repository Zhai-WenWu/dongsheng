/**
 * @author Jerry
 * 2013-1-5 下午2:21:53
 * Copyright: Copyright (c) xiangha.com 2011
 */
package aplug.web;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.WebActivity;
import acore.tools.FileManager;
import acore.observer.IObserver;
import acore.observer.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.CommentBar;
import amodule.user.activity.login.LoginByAccout;
import aplug.web.tools.JSAction;
import aplug.web.tools.JsAppCommon;
import aplug.web.tools.WebviewManager;
import aplug.web.view.XHWebView;
import third.mall.activity.ShoppingActivity;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallStringManager;
import xh.basic.tool.UtilString;


/**
 * 打开网页，bundle中传入url，当页面加载完会获取页面title来设置
 *
 * @author Jerry
 */
public class  ShowWeb extends WebActivity implements IObserver {
	protected String url = "", htmlData = "";
	protected Button rightBtn;
	protected ImageView favoriteNousImageView;
	protected String nousCodeString = "";
	protected RelativeLayout shareLayout;
	protected RelativeLayout favLayout,homeLayout;
	protected RelativeLayout editControlerLayout;
	protected CommentBar commentBar;
	protected TextView favoriteNousTextView,title;
	private RelativeLayout shopping_layout;
	private TextView mall_news_num,mall_news_num_two;
	private JsAppCommon jsAppCommon;
	private String data_type="";
	private String code="";//code--首页使用功能
	private String module_type="";
	private String userCode = "";
	private boolean hardwareAccelerated;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		initExtras();
		initActivity("", 3, 0, R.layout.c_view_bar_nouse_title, R.layout.xh_webview);
		initTitleView();
		initWeb();
		initCommentBar();
		if(mCommonBottomView!=null){
			mCommonBottomView.setVisibility(setShowCommonBottomView()?View.VISIBLE:View.GONE);
		}
		setTitle();
		// 设置加载
		loadManager.setLoading(v -> loadData());
		MallCommon common= new MallCommon(this);
		common.setStatisticStat(url);
		ObserverManager.getInstance().registerObserver(this,ObserverManager.NOTIFY_LOGIN,
				ObserverManager.NOTIFY_LOGOUT, ObserverManager.NOTIFY_SHARE, ObserverManager.NOTIFY_AUTHORIZE_THIRD);
//		webview.upWebViewNum();
	}

	protected void initExtras(){
		Bundle bundle = this.getIntent().getExtras();
		// 正常调用
		if (bundle != null) {
			url = bundle.getString("url");
			data_type = bundle.getString("data_type");
			code= bundle.getString("code");
			module_type= bundle.getString("module_type");
			hardwareAccelerated = !TextUtils.equals("1",bundle.getString("hardware"));
			JSAction.loadAction = bundle.getString("doJs") != null ? bundle.getString("doJs") : "";

		}
	}

	protected void initWeb(){
		webViewManager = new WebviewManager(this,loadManager,true);
		webview = webViewManager.createWebView(R.id.XHWebview);
		//关闭加速
		if(!hardwareAccelerated){
			webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		webview.setOnWebNumChangeCallback(new XHWebView.OnWebNumChangeCallback() {
			@Override
			public void onChange(int num) {
				ImageView close = (ImageView) findViewById(R.id.leftClose);
				close.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						ShowWeb.this.finish();
					}
				});
				boolean isShow = num > 1
						&& (findViewById(R.id.leftText) != null && findViewById(R.id.leftText).getVisibility() != View.VISIBLE);
				close.setVisibility(isShow ? View.VISIBLE : View.GONE);
			}
		});
		webViewManager.setJSObj(webview, jsAppCommon=new JsAppCommon(this, webview,loadManager,barShare));
		jsAppCommon.setUrl(url);
		htmlData = "";
	}

	protected int heightDifference = -1;
	protected void initCommentBar() {
		editControlerLayout = (RelativeLayout) findViewById(R.id.edit_controler_layout);
		editControlerLayout.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()){
					case MotionEvent.ACTION_DOWN:
						resetCommentBar();
						break;
				}
				return false;
			}
		});
		commentBar = (CommentBar) findViewById(R.id.comment_bar);
		commentBar.setVisibility(View.GONE);
		commentBar.setOnPublishCommentCallback(new CommentBar.OnPublishCommentCallback() {
			@Override
			public boolean onPrePublishComment() {
				if(!LoginManager.isLogin()){
					Tools.showToast(ShowWeb.this,"请登录");
					startActivity(new Intent(ShowWeb.this, LoginByAccout.class));
					return true;
				}
				if(!ToolsDevice.isNetworkAvailable(ShowWeb.this)){
					Tools.showToast(ShowWeb.this,"请检查网络连接");
					return true;
				}
				return false;
			}

			@Override
			public void onPublishComment(String content) {
				if(webview != null)
					webview.loadUrl("Javascript:publishComment(\""+content+"\",\"" + userCode + "\"");
				resetCommentBar();
			}
		});
		setOnKeyBoardListener(new OnKeyBoardListener() {
			@Override
			public void show() {
				if(heightDifference != -1){
					int heightDiff = rl.getRootView().getHeight() - rl.getHeight();
					Rect r = new Rect();
					rl.getWindowVisibleDisplayFrame(r);
					int screenHeight = rl.getRootView().getHeight();
					heightDifference = screenHeight - (r.bottom - r.top);
					boolean isKeyboradShow = heightDifference > 200;
					heightDifference = isKeyboradShow ? heightDifference - heightDiff : 0;
				}
				editControlerLayout.setPadding(0, 0, 0, heightDifference);
			}

			@Override
			public void hint() {
				editControlerLayout.setPadding(0, 0, 0, 0);
			}
		});
	}

	public void showCommentBar(String userName,String userCode){
		this.userCode = userCode;
		editControlerLayout.setVisibility(View.VISIBLE);
		commentBar.setVisibility(View.VISIBLE);
		commentBar.setEditTextHint("回复 ：" + userName);
	}

	public void resetCommentBar(){
		commentBar.setEditTextHint("回复 ");
		commentBar.hide();
		userCode = "";
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(shopping_layout.getVisibility()==View.VISIBLE){
			if(MallCommon.num_shopcat>0){
				if(MallCommon.num_shopcat>9){
					mall_news_num.setVisibility(View.GONE);
					mall_news_num_two.setVisibility(View.VISIBLE);
					if(MallCommon.num_shopcat>99)
						mall_news_num_two.setText("99+");
					else
						mall_news_num_two.setText(""+MallCommon.num_shopcat);
				}else{
					mall_news_num.setVisibility(View.VISIBLE);
					mall_news_num_two.setVisibility(View.GONE);
					mall_news_num.setText(""+MallCommon.num_shopcat);
				}
			}else{
				mall_news_num.setVisibility(View.GONE);
				mall_news_num_two.setVisibility(View.GONE);
			}
			if(LoginManager.isLogin()){
				MallCommon.getShoppingNum(this,mall_news_num,mall_news_num_two);
			}
		}
	}
	@Override
	protected void onRestart() {
		super.onRestart();
		if(loadManager != null)
			loadManager.hideProgressBar();
		//同cookie并刷新
		syncCookieReload();
	}

	@Override
	public OnClickListener getBackBtnAction() {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				ShowWeb.this.onBackPressed();
			}
		};
	}

	protected void initTitleView(){
		title = (TextView)findViewById(R.id.title);
 		rightBtn = (Button) findViewById(R.id.rightBtn1);
		shareLayout = (RelativeLayout) findViewById(R.id.shar_layout);
		favLayout = (RelativeLayout) findViewById(R.id.fav_layout);
		homeLayout = (RelativeLayout) findViewById(R.id.home_layout);
//		收藏按钮图片
        favoriteNousImageView = (ImageView) findViewById(R.id.img_fav);
        favoriteNousTextView = (TextView) findViewById(R.id.tv_fav);
        shopping_layout = (RelativeLayout) findViewById(R.id.shopping_layout);
        mall_news_num = (TextView) findViewById(R.id.mall_news_num);
        mall_news_num_two = (TextView) findViewById(R.id.mall_news_num_two);
    }

    protected void setTitle() {
		if(TextUtils.isEmpty(url))return;
        //积分商城
        if (url.indexOf(StringManager.api_scoreStore) == 0) {
            //返回,返回上一界面
            findViewById(R.id.leftImgBtn).setVisibility(View.GONE);
            findViewById(R.id.v_marginL5).setVisibility(View.VISIBLE);
            TextView tv_back = (TextView) findViewById(R.id.leftText);
            tv_back.setText("关闭");
        } else if (url.indexOf(StringManager.api_exchangeList) == 0) {
            title.setText("兑换记录");
        } else if (url.indexOf(StringManager.api_scoreList) == 0) {
            title.setText("我的积分");
        } else if (url.indexOf(StringManager.api_nouseInfo) == 0) {
            title.setText("香哈头条");
            XHClick.track(this, "浏览知识");
        } else if (url.indexOf(MallStringManager.mall_web_shop_detail) == 0) {
            homeLayout.setVisibility(View.VISIBLE);
            LayoutParams params = new LayoutParams(Tools.getDimen(this, R.dimen.dp_30), LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, Tools.getDimen(this, R.dimen.dp_5), 0);
            ImageView iv = (ImageView) findViewById(R.id.img_home);
            iv.setLayoutParams(params);
            iv.setImageResource(R.drawable.z_mall_shopcat_iv);
            homeLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ShowWeb.this, ShoppingActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        } else if (url.indexOf(MallStringManager.mall_web_apiUrl + "/v1/shop/home?") == 0) {
//			findViewById(R.id.mall_mercat_linear).setVisibility(View.GONE);
//			//联系客服
//			findViewById(R.id.product_feek).setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					Intent intent = new Intent(ShowWeb.this, Feedback.class);
//					intent.putExtra("feekUrl", url);
//					intent.putExtra(Feedback.EXTRA_FROM, Feedback.FROM_COMMOD);
//					ShowWeb.this.startActivity(intent);
//				}
//			});
//			//店铺详情
//			findViewById(R.id.product_mercat).setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					String urls= url.replace(MallStringManager.mall_web_apiUrl+"/v1/shop/home?", "");
//					Map<String, String> map=UtilString.getMapByString(urls, "&", "=");
//					String url_new=MallStringManager.mall_web_shop_detail+"?shop_code="+map.get("shop_code");
//					AppCommon.openUrl(ShowWeb.this, url_new, true);
//				}
//			});
            XHClick.track(this, "浏览店铺页");
        }
    }

    /**
     * 重写initLoading,将自己传递给MainActivity 主要是获取自身的webView的url;
     */
    @Override
    public void loadData() {
        loadManager.setLoading(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selfLoadUrl(url, true);
            }
        });
    }

    @Override
    public boolean selfLoadUrl(String theUrl, boolean openThis) {
        boolean isSameUrl = theUrl != null
                && webview.getmUrl() != null
                && !theUrl.equals(webview.getUrl());
        boolean flag = super.selfLoadUrl(theUrl, openThis);
        if (isSameUrl) {
            webview.upWebViewNum();
        }
        return flag;
    }

    /**
     * 设置展示
     */
	protected boolean setShowCommonBottomView() {
        String msg = FileManager.getFromAssets(this, "showCommonBottom");
        ArrayList<Map<String, String>> list = UtilString.getListMapByJson(UtilString.getListMapByJson(msg).get(0).get("h5"));
        if (url.contains("?")) {
            if (list.get(0).containsKey(url.substring(0, url.indexOf("?")))) {
                return true;
            }
        } else {
            if (list.get(0).containsKey(url)) {
                return true;
            }
        }
        return false;
    }

	@Override
	protected void onPause() {
		super.onPause();
		if(loadManager != null){
			loadManager.hideProgressBar();
		}
	}

	@Override
    protected void onDestroy() {
		ObserverManager.getInstance().unRegisterObserver(this);
        super.onDestroy();
    }

	@Override
	public void notify(String name, Object sender, Object data) {
		if (TextUtils.isEmpty(name))
			return;
		switch (name){
			case ObserverManager.NOTIFY_SHARE:
			case ObserverManager.NOTIFY_AUTHORIZE_THIRD:
				handleWebCallback((Map<String, String>) data);
				break;
			case ObserverManager.NOTIFY_LOGIN:
				needSyncCookieReload = true;
				break;
			case ObserverManager.NOTIFY_LOGOUT:
				needSyncCookieReload = true;
				break;
		}
	}

	private boolean needSyncCookieReload= false;
	private void syncCookieReload(){
		if(needSyncCookieReload){
			needSyncCookieReload = false;
			try{
				WebviewManager.syncXHCookie();
				WebviewManager.syncDSCookie();
			}catch (Exception e){
				e.printStackTrace();
			}
			if(webview != null) {
				webview.reload();
			}
		}
	}
}