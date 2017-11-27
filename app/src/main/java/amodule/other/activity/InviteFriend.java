package amodule.other.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class InviteFriend extends BaseActivity {

	private ImageView inviteFriendImage;
	private int QR_WIDTH = 100, QR_HEIGHT = 100;
	
	private LinearLayout llContent;
	private LinearLayout recomme_linear;
	private RelativeLayout recomme_already;
	private TextView friends_number, friends_name, friends_number_sure;
	private EditText recomme_number;
	private Handler handler;
	private final int DATA = 1;
	private final int NO_DATA = 2;
	private final int NEW_DATA = 3;
	private final int SHOW_DIALOG = 4;
	private final int SUCESS= 5;
	private final int DATA_SUCESS= 6;
	private String father_name;
	private RelativeLayout xiangha_recomme_already;
	private int old_user=0;
	private TextView invite_xiangha_text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("邀请好友", 2, 0, R.layout.c_view_bar_title, R.layout.a_xh_invite_friend);
		intiView();
		initData();
	}

	@SuppressLint("HandlerLeak")
	private void intiView() {
		QR_WIDTH = ToolsDevice.dp2px(this, 150);
		QR_HEIGHT = QR_WIDTH;
		llContent = (LinearLayout) findViewById(R.id.ll_content);
		inviteFriendImage = (ImageView) findViewById(R.id.invite_friend_image);
		// 修改布局
		LinearLayout invite_friend_linear = (LinearLayout) findViewById(R.id.invite_friend_linear);
		// 原先布局隐藏
		findViewById(R.id.line_invitefriends).setVisibility(View.GONE);

		// 添加布局
		View view = LayoutInflater.from(this).inflate(R.layout.a_xh_invite_friends_linear, invite_friend_linear);
		recomme_linear = (LinearLayout) view.findViewById(R.id.invite_friends_recomme_linear);
		recomme_already = (RelativeLayout) view.findViewById(R.id.invite_friends_recomme_already);
		friends_number = (TextView) view.findViewById(R.id.invite_friends_number);
		friends_number_sure = (TextView) view.findViewById(R.id.invite_friends_number_sure);
		friends_name = (TextView) view.findViewById(R.id.invite_friends_name);
		recomme_number = (EditText) view.findViewById(R.id.edit_num);

		TextView friends_explain = (TextView) view.findViewById(R.id.invite_friends_explain);
		setTextcolorSpan(friends_explain);
		
		xiangha_recomme_already = (RelativeLayout) findViewById(R.id.invite_xiangha_recomme_already);
		TextView invite_xiangha_name=(TextView) findViewById(R.id.invite_xiangha_name);
		SpannableStringBuilder builder = new SpannableStringBuilder(invite_xiangha_name.getText().toString());
		ForegroundColorSpan redSpan = new ForegroundColorSpan(this.getResources().getColor(R.color.c_black_text));
		builder.setSpan(redSpan, 7, 10, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		invite_xiangha_name.setText(builder);
		invite_xiangha_text = (TextView) findViewById(R.id.invite_xiangha_text);
		
		invite_xiangha_text.setOnClickListener(new OnClickListener() {//点击
			
			@Override
			public void onClick(View v) {
				invite_xiangha_text.setEnabled(false);
				load_inviteCustomer();
			}
		});
		
		recomme_number.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus)
					recomme_number.setHint("");
			}
		});
		friends_number_sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String str = (String) friends_number_sure.getText();
				if ("邀请".equals(str)) {
					ToolsDevice.keyboardControl(false, InviteFriend.this, recomme_number);
					startShare();
				} else if ("登录".equals(str)) {
					Intent intent = new Intent(InviteFriend.this, LoginByAccout.class);
					startActivity(intent);
				}
			}
		});
		// 发出验证码验证
		view.findViewById(R.id.invite_friends_sure).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String value = recomme_number.getText().toString();
				ToolsDevice.keyboardControl(false, InviteFriend.this, recomme_number);
				if(LoginManager.userInfo.size()<=0){
					Intent intent = new Intent(InviteFriend.this, LoginByAccout.class);
					startActivity(intent);
					return;
				}
				if (TextUtils.isEmpty(value)) {
					Tools.showToast(InviteFriend.this, "您还没有填写验证码");
					return;
				}
				load_parseInvitationCode();
			}
		});

		//处理界面回调
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case DATA:// 是意见被用户推荐
					recomme_linear.setVisibility(View.GONE);
					recomme_already.setVisibility(View.VISIBLE);
					xiangha_recomme_already.setVisibility(View.GONE);
					setTextName(father_name);
					break;
				case NO_DATA:// 老用户---未被邀请
					old_user=1;
					recomme_linear.setVisibility(View.GONE);
					recomme_already.setVisibility(View.GONE);
					findViewById(R.id.recomme_line).setVisibility(View.VISIBLE);
					xiangha_recomme_already.setVisibility(View.VISIBLE);
					// 测试————-使用验证码
//					recomme_linear.setVisibility(View.VISIBLE);
//					findViewById(R.id.recomme_line).setVisibility(View.VISIBLE);
					break;
				case DATA_SUCESS://老用户已被邀请
					
					recomme_linear.setVisibility(View.GONE);
					recomme_already.setVisibility(View.VISIBLE);
					xiangha_recomme_already.setVisibility(View.GONE);
					friends_name.setText("你的设备已被“香哈网”邀请");
					break;
				case NEW_DATA:// 新用户
					recomme_linear.setVisibility(View.VISIBLE);
					recomme_already.setVisibility(View.GONE);
					xiangha_recomme_already.setVisibility(View.GONE);
					break;
				case SHOW_DIALOG://弹出对话框
					father_name = (String) msg.obj;
					String obj_name= "你确定被“%s”邀请嘛！";
					String message= String.format(obj_name, father_name);
					showDialog(message);
					break;
				case SUCESS://被邀请成功
					recomme_linear.setVisibility(View.GONE);
					recomme_already.setVisibility(View.VISIBLE);
					xiangha_recomme_already.setVisibility(View.GONE);
					setTextName(father_name);
					break;
				}
			}
		};
	}

	/**
	 * 请求二维码
	 */
	private void initData() {
		loadManager.setLoading(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loadNetData();
			}
		});
	}

	private void loadNetData() {
		String url = StringManager.api_getDownloadUrl;
		ReqInternet.in().doGet(url, new InternetCallback(this) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				loadManager.loadOver(flag, 1,true);
				if (flag >= UtilInternet.REQ_OK_STRING) {
					llContent.setVisibility(View.VISIBLE);
					Bitmap shareImg = getQRImage(returnObj.toString());
					inviteFriendImage.setImageBitmap(shareImg);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (LoginManager.isLogin()&&LoginManager.userInfo.size() > 0&&!TextUtils.isEmpty(LoginManager.userInfo.get("inviteCode"))) {
			friends_number.setText(LoginManager.userInfo.get("inviteCode"));
			friends_number_sure.setText("邀请");
			xiangha_recomme_already.setVisibility(View.GONE);
			load_inviteCheck();
//			Tools.showToast(this, ":::;" + LoginManager.userInfo.get("inviteCode"));
		} else {
			friends_number.setText("请登录获取邀请码");
			friends_number_sure.setText("登录");
			recomme_linear.setVisibility(View.VISIBLE);
			recomme_already.setVisibility(View.GONE);
			xiangha_recomme_already.setVisibility(View.GONE);
		}
		// 在此请求接口
	}

	/**
	 * 验证用户是否能被邀请
	 */
	private void load_inviteCheck() {
		String url = StringManager.api_inviteCheck;
		ReqInternet.in().doGet(url, new InternetCallback(this) {

			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					ArrayList<Map<String, String>> str = UtilString.getListMapByJson(returnObj);
					if (str.size() > 0) {
						Map<String, String> map = str.get(0);
						String state = map.get("flag");
						if (!TextUtils.isEmpty(state)) {
							if (Integer.parseInt(state) == 1) {//已被邀请过
								ArrayList<Map<String, String>> cusInfos = UtilString.getListMapByJson(map.get("cusInfo"));
								if (cusInfos.size() > 0) {
									Map<String, String> mapcodes = cusInfos.get(0);
									String code = mapcodes.get("code");
									if (!TextUtils.isEmpty(code)) {
										if (!"0".equals(code)) {
											father_name = mapcodes.get("nickName");
											handler.sendEmptyMessage(DATA);
										} else {//老用户
											handler.sendEmptyMessage(DATA_SUCESS);
										}
									}
								}
							} else if (Integer.parseInt(state) == 2) {//未被邀请过
								handler.sendEmptyMessage(NEW_DATA);
							}else if(Integer.parseInt(state) == 3){//未被邀请老用户
								handler.sendEmptyMessage(NO_DATA);
							}
						}
					}
				}
			}
		});
	}

	/**
	 * 根据邀请码获取用户信息
	 */
	private void load_parseInvitationCode() {
		String value = recomme_number.getText().toString();
		String url = StringManager.api_parseInvitationCode;
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("invCode", value);
		ReqInternet.in().doPost(url, map, new InternetCallback(this) {

			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					ArrayList<Map<String, String>> strs = UtilString.getListMapByJson(returnObj);
					if (strs.size() > 0) {
						Map<String, String> map = strs.get(0);
						String nickname = map.get("nickName");
						Message msg = new Message(); 
						msg.what=SHOW_DIALOG;
						msg.obj=nickname;
						handler.sendMessage(msg);
					}
				}
			}
		});
	}

	/**
	 * 添加邀请
	 */
	private void load_inviteCustomer() {
		String value = recomme_number.getText().toString();
		String url = StringManager.api_inviteCustomer;
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("invCode", value);
		ReqInternet.in().doPost(url, map, new InternetCallback(this) {

			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					if(old_user==0)
						handler.sendEmptyMessage(SUCESS);
					else
						handler.sendEmptyMessage(DATA_SUCESS);
				}
				invite_xiangha_text.setEnabled(true);
				
				Tools.showToast(InviteFriend.this, returnObj.toString());
			}
		});

	}

	/**
	 * 分享 SHARE_MEDIA.QQ：qq SHARE_MEDIA.WEIXIN SHARE_MEDIA.SMS SHARE_MEDIA.SINA
	 * 
	 */
	private void startShare() {
		XHClick.mapStat(this, "a_share400", "邀请好友", "");
		String imgType = BarShare.IMG_TYPE_RES;
		String title = "香哈菜谱，让“吃饭”花样多多";
		String clickUrl = StringManager.api_inviteCustomer_new+"?code="+LoginManager.userInfo.get("code");// 此地址为应用商地址，运营要求！
		String content = "自从有了香哈，每天都有不同吃法，真是又好用又省心，家人都称赞我是一级大厨！一起来玩吧。";
		String imgUrl = "" + R.drawable.share_launcher;
		Resources res = getResources();
		Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.share_launcher);
		BarShare barShare = new BarShare(this, "邀请好友","");

		if (bmp == null) {
			barShare.setShare(imgType, title, content, imgUrl, clickUrl);
		} else {
			barShare.setShare(title, content, bmp, clickUrl);
		}
		barShare.openShare();
	}

	// 生成QR图
	private Bitmap getQRImage(String text) {
		try {
			// 需要引入core包
			QRCodeWriter writer = new QRCodeWriter();

			if (text == null || "".equals(text) || text.length() < 1) {
				return null;
			}
			// 把输入的文本转为二维码
			BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);

			// martix.getHeight());

			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
			int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
			for (int y = 0; y < QR_HEIGHT; y++) {
				for (int x = 0; x < QR_WIDTH; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * QR_WIDTH + x] = 0xff000000;
					} else {
						pixels[y * QR_WIDTH + x] = 0xffffffff;
					}
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);

			bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 设置部分字变色
	 * 
	 * @param text
	 */
	private void setTextcolorSpan(TextView text) {
		SpannableStringBuilder builder = new SpannableStringBuilder(text.getText().toString());
		ForegroundColorSpan redSpan = new ForegroundColorSpan(this.getResources().getColor(R.color.c_black_text));
		ForegroundColorSpan redSpan1 = new ForegroundColorSpan(this.getResources().getColor(R.color.c_black_text));
		ForegroundColorSpan redSpan2 = new ForegroundColorSpan(this.getResources().getColor(R.color.c_black_text));
		builder.setSpan(redSpan, 23, 25, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		builder.setSpan(redSpan1, 28, 32, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		builder.setSpan(redSpan2, 40, 43, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		text.setText(builder);
	}

	/**
	 * 设置用户被谁邀请
	 * 
	 * @param data
	 */
	private void setTextName(String data) {
		String no_name = this.getResources().getString(R.string.invite_friends_name);
		String name = String.format(no_name, data);
		friends_name.setText(name);
	}

	/**
	 * 展示一个dialog
	 * 
	 * @param name
	 */
	private void showDialog(String name) {
		final DialogManager dialogManager = new DialogManager(this);
		dialogManager.createDialog(new ViewManager(dialogManager)
				.setView(new TitleMessageView(this).setText(name))
				.setView(new HButtonView(this)
						.setNegativeText("取消", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								dialogManager.cancel();
							}
						})
						.setPositiveText("确定", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								dialogManager.cancel();
								//正式添加邀请
								load_inviteCustomer();
							}
						}))).show();
	}
}
