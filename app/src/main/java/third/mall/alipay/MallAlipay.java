package third.mall.alipay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import acore.tools.PageStatisticsUtils;
import acore.tools.Tools;
import third.mall.activity.OrderStateActivity;
import third.mall.activity.PaySuccedActvity;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import acore.logic.PayCallback;
import third.mall.override.MallBaseActivity;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;


/**
 * 支付宝——单例
 *
 * @author Administrator
 *
 */
public class MallAlipay {

	// 商户PID
	public static final String PARTNER = "2088802482143347";
	// 商户收款账号
	public static final String SELLER = "xiangha@gmail.com";
	// 商户私钥，pkcs8格式
	public static final String RSA_PRIVATE = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMBMxhIPKEadkR9v8mrvygjv1YDZewHAhO33ARkKisMlL82JKqwuLBQrM224OVcXKo68G0FjjKr2RJdhW+O2eMJJRs51ite0VZ6iLWBEF72NUOGOhzl3subsa23XPH4BxHB+SpykoLKvzY08dtIBk8c5b6crZGR23Giz/8lSXw9hAgMBAAECgYAfEgiK0HIkfr79Alx71MSjDwVDLWCHlvCjdl5yClcDUtXXcss3SLqMg7JqjvKM1MxmhZQty4Tl9qZ8gxmSwF/gVGSz9Ahh80Nf6pQODBoFPwzeR5U3Ti5yXV7XFZEAVbdBCt0UgjsJCa+TRjx0FsxpTHNWs0Lkn+9lS6T35/EgcQJBAPTRKjUJKi6USi0eJryU1J9wcPVq4a1VC86Tac7xOXtbxeAdjjavo4PfTNKPygRCAH/b51ShKox+rVKSpHJSrH0CQQDJFXwy3PeIrDIasK5qqu/dsz8yD3vmXlOEg7PzmOY4ns1tszluXRhwEW2/kJHiLoyoTNAxC7VvW6WxlnKUQXe1AkBzEAO4XZBXyBZ80hj+tSyhqyVME2nyH3CnLJ2kR7fuhJmh1gJLLY26oy7mH/Kgwayea2p0WjM3SSqJDqb/nF+5AkEAum1I8H8cn4HGEiisDAjeydRdSrRAUpwxIjJYrAedqfDQ1FvNaxy0g3IlJe2K0wAFOCO/ATmxxMRbIgIxyHHJ4QJBAIHglMk2/YY3szu0SK4FLBEjK/4oQPioeGYSiIroA8DU8u3rpE4hOgAFixZnIrVL/2l4guurot0rf2saEmVBpbY=";
	// 支付宝公钥
	public static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDATMYSDyhGnZEfb/Jq78oI79WA2XsBwITt9wEZCorDJS/NiSqsLiwUKzNtuDlXFyqOvBtBY4yq9kSXYVvjtnjCSUbOdYrXtFWeoi1gRBe9jVDhjoc5d7Lm7Gtt1zx+AcRwfkqcpKCyr82NPHbSAZPHOW+nK2Rkdtxos//JUl8PYQIDAQAB";
	private static final int SDK_PAY_FLAG = 1;

//	private static final int SDK_CHECK_FLAG = 2;
	private Activity mAct;
	private ArrayList<Map<String, String>> listMapByJson;
	private static MallAlipay mallAlipay = null;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SDK_PAY_FLAG: {
					PayResult payResult = new PayResult((String) msg.obj);

					// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
//				String resultInfo = payResult.getResult();
				String resultStatus = payResult.getResultStatus();
				if(PayCallback.getPayCallBack() != null){
					String data = "";
					if (TextUtils.equals(resultStatus, "9000")) {
						data = "支付成功";
					} else {
						// 判断resultStatus 为非“9000”则代表可能支付失败
						// “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
						if (TextUtils.equals(resultStatus, "8000")) {//处理中
							data = "支付结果确认中";
						} else {//支付失败
							// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
							data = "亲，支付失败了";
						}
					}
					PayCallback.getPayCallBack().onPay(TextUtils.equals(resultStatus, "9000"),data);
				}else {
					// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
					if (TextUtils.equals(resultStatus, "9000")) {
						Toast.makeText(mAct, "支付成功", Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(mAct, PaySuccedActvity.class);
						intent.putExtra("amt", listMapByJson.get(0).get("amt"));
						intent.putExtra(MallBaseActivity.PAGE_FROM, PageStatisticsUtils.getPageName(mAct));
						mAct.startActivity(intent);
					} else {
						// 判断resultStatus 为非“9000”则代表可能支付失败
						// “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
						if (TextUtils.equals(resultStatus, "8000")) {//处理中
							Toast.makeText(mAct, "支付结果确认中", Toast.LENGTH_SHORT).show();
						} else {//支付失败
							// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
							Toast.makeText(mAct, "亲，支付失败了，再试试吧", Toast.LENGTH_SHORT).show();
							if (!TextUtils.isEmpty(MallCommon.payment_order_id)) {//我的订单
								Intent intent = new Intent();
								intent.setClass(mAct, OrderStateActivity.class);
								intent.putExtra("order_id", MallCommon.payment_order_id);
								intent.putExtra("order_satus", "payment_order");
								intent.putExtra(MallBaseActivity.PAGE_FROM, PageStatisticsUtils.getPageName(mAct));
								mAct.startActivity(intent);
							}

						}
//					else if(TextUtils.equals(resultStatus, "6001""4000")){//用户中途取消
//
//					}else if(TextUtils.equals(resultStatus, "6002")){//网络连接中断
//
//					}else {
//
//					}
					}
					mAct.finish();
				}
				break;
			}
//			case SDK_CHECK_FLAG: {
//				Toast.makeText(mAct, "检查结果为：" + msg.obj, Toast.LENGTH_SHORT).show();
//				break;
//			}
			}
			mAct = null;
		};
	};

	private MallAlipay() {
	}

	public static synchronized MallAlipay getInstance() {
		if (mallAlipay == null) {
			mallAlipay = new MallAlipay();
		}
		return mallAlipay;
	}

	/**
	 * 调用SDK支付
	 *
	 */
	public void pay(Activity activity,ArrayList<Map<String, String>> listMapByJson) {
		mAct = activity;
		this.listMapByJson= listMapByJson;
		// 订单
		String orderInfo = getOrderInfo(listMapByJson.get(0).get("bill_payment_no"),
				listMapByJson.get(0).get("payment_subject"),
				listMapByJson.get(0).get("payment_subject"),
				listMapByJson.get(0).get("amt"),
				listMapByJson.get(0).get("alipay_callback"),
				listMapByJson.get(0).get("expire_date"));

		// 对订单做RSA 签名
		String sign = sign(orderInfo);
		try {
			// 仅需对sign 做URL编码
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// 完整的符合支付宝参数规范的订单信息
		final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();
		Runnable payRunnable = new Runnable() {
			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(mAct);
				// 调用支付接口，获取支付结果
				if(!TextUtils.isEmpty(payInfo)){
					String result = alipay.pay(payInfo,true);
	
					Message msg = new Message();
					msg.what = SDK_PAY_FLAG;
					msg.obj = result;
					mHandler.sendMessage(msg);
				}
			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}

	/**
	 * 直接开启支付
	 * payInfo
	 */
	public void startAlipay(Activity activity,final String payInfo){
		mAct = activity;
		Runnable payRunnable = new Runnable() {
			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(mAct);
				// 调用支付接口，获取支付结果
				if (!TextUtils.isEmpty(payInfo)) {
					String result = alipay.pay(payInfo, true);

					Message msg = new Message();
					msg.what = SDK_PAY_FLAG;
					msg.obj = result;
					mHandler.sendMessage(msg);
				}
			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}

	/**
	 * 配置支付宝信息，创建订单信息
	 *
	 */
	public String getOrderInfo(String bill_payment_no, String subject, String body, String price, String alipay_callback, String expire_date) {

		// 签约合作者身份ID
		String orderInfo = "partner=" + "\"" + PARTNER + "\"";

		// 签约卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

		// 商户网站唯一订单号
		orderInfo += "&out_trade_no=" + "\"" + bill_payment_no + "\"";

		// 商品名称
		orderInfo += "&subject=" + "\"" + subject + "\"";

		// 商品详情
		orderInfo += "&body=" + "\"" + body + "\"";

		// 商品金额
		orderInfo += "&total_fee=" + "\"" + price + "\"";

		// 服务器异步通知页面路径
		orderInfo += "&notify_url=" + "\"" + alipay_callback + "\"";

		// 服务接口名称， 固定值
		orderInfo += "&service=\"mobile.securitypay.pay\"";

		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";

		// 参数编码， 固定值
		orderInfo += "&_input_charset=\"utf-8\"";

		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=\"" + expire_date + "\"";

		// extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
		// orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
		orderInfo += "&return_url=\"m.alipay.com\"";

		// 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
		// orderInfo += "&paymethod=\"expressGateway\"";

		return orderInfo;
	}

	/*
	 *生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
	 */
	public String getOutTradeNo() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
		Date date = new Date();
		String key = format.format(date);

		Random r = new Random();
		key = key + r.nextInt();
		key = key.substring(0, 15);
		return key;
	}

	/**
	 * 对订单信息进行签名
	 *
	 * @param content
	 *   待签名订单信息
	 */
	public String sign(String content) {
		return SignUtils.sign(content, RSA_PRIVATE);
	}

	/**
	 * get the sign type we use. 获取签名方式
	 *
	 */
	public String getSignType() {
		return "sign_type=\"RSA\"";
	}

	private void getRequestSign(final String orderInfo){
		try {
			String actionUrl = MallStringManager.mall_getToken;
			String param = "url=" + Base64.encode(orderInfo.getBytes()) + "&type=sync&config=1";
			MallReqInternet.in().doPost(actionUrl, param, new MallInternetCallback(mAct) {
				@Override
				public void loadstat(int flag, String url, Object msg, Object... stat) {

					if (flag >= UtilInternet.REQ_OK_STRING) {
						startPay(orderInfo,msg);
					} else if (flag == UtilInternet.REQ_CODE_ERROR) {
						Map<String, String> map = (Map<String, String>) msg;
						Tools.showToast(mAct, map.get("msg"));
					}
				}

			});
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public void startPay(String orderInfo,Object msg){
		ArrayList<Map<String,String>> list=UtilString.getListMapByJson(msg);
		if(list.get(0).containsKey("sign")&&!TextUtils.isEmpty(list.get(0).get("sign"))) {
			// 完整的符合支付宝参数规范的订单信息
			final String payInfo = orderInfo + "&sign=\"" + list.get(0).get("sign") + "\"&" + "sign_type=\""+list.get(0).get("sign_type")+"\"";;
			Runnable payRunnable = new Runnable() {
				@Override
				public void run() {
					// 构造PayTask 对象
					PayTask alipay = new PayTask(mAct);
					// 调用支付接口，获取支付结果
					if (!TextUtils.isEmpty(payInfo)) {
						String result = alipay.pay(payInfo, true);

						Message msg = new Message();
						msg.what = SDK_PAY_FLAG;
						msg.obj = result;
						mHandler.sendMessage(msg);
					}
				}
			};

			// 必须异步调用
			Thread payThread = new Thread(payRunnable);
			payThread.start();
		}
	}

	/**
	 * 直接开启支付
	 * payInfo
	 */
	public void startAlipay(final String payInfo){
		Runnable payRunnable = new Runnable() {
			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(mAct);
				// 调用支付接口，获取支付结果
				if (!TextUtils.isEmpty(payInfo)) {
					String result = alipay.pay(payInfo, true);

					Message msg = new Message();
					msg.what = SDK_PAY_FLAG;
					msg.obj = result;
					mHandler.sendMessage(msg);
				}
			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}
}
