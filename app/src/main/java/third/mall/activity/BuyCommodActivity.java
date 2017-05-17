package third.mall.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import aplug.basic.LoadImage;
import third.mall.alipay.MallPayActivity;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallCommon.InterfaceMallReqIntert;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

import static com.xiangha.R.id.mall_buycommod_scrollview;

/**
 * 购物页面
 *
 * @author yu
 */
public class BuyCommodActivity extends BaseActivity implements OnClickListener {

	private String code, imageUrl, title, shop_name, price;
	private int num = 1;
	private TextView buycommod_commod_explian_data_num;//数量
	private TextView buycommod_commod_price_res;//商品金额
	private TextView buycommod_commod_price_pos;//邮费
	private TextView buycommod_commod_price_end;//合计
	private TextView buycommod_commod_explian_data_price;//单价
	private static final int SHOW_OK = 1;
	public static final int OK_ADDRESS = 100;
	private String consignee_name = "";
	private String consignee_tel = "";
	private String consignee_address = "";
	private String address_id = "";
	private boolean num_state = false;//无货
	private int max_num = 0;
	private Handler handler;
	private RelativeLayout mall_rela_end;
	private int two_state = 0;
	private boolean back_state = false;
	private MallCommon common;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			code = bundle.getString("code");
			imageUrl = bundle.getString("imageUrl");
			title = bundle.getString("title");
			shop_name = bundle.getString("shop_name");
			price = bundle.getString("price");
		}
		initActivity("立即购买", 3, 0, 0, R.layout.a_mall_buycommod);
		common = new MallCommon(this);
		initView();
		initData();

	}

	@SuppressLint("HandlerLeak")
	private void initData() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case SHOW_OK:
						if (two_state >= 2) {
							back_state = true;
							loadManager.hideLoadFaildBar();
							loadManager.hideProgressBar();
							findViewById(mall_buycommod_scrollview).setVisibility(View.VISIBLE);
							findViewById(R.id.mall_rela_end).setVisibility(View.VISIBLE);
						}
						break;
				}
				super.handleMessage(msg);
			}
		};
		loadManager.setLoading(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setRequest();
				//延迟加载
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						setRequestAddress();
					}
				}, 100);
			}
		});
	}

	private void initView() {
		((TextView) findViewById(R.id.title)).setText("立即购买");
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.buycommod_commod_explian_data_cut).setOnClickListener(this);
		findViewById(R.id.buycommod_commod_explian_data_add).setOnClickListener(this);
		buycommod_commod_explian_data_num = (TextView) findViewById(R.id.buycommod_commod_explian_data_num);
		buycommod_commod_explian_data_price = (TextView) findViewById(R.id.buycommod_commod_explian_data_price);
		buycommod_commod_price_res = (TextView) findViewById(R.id.buycommod_commod_price_res);
		buycommod_commod_price_pos = (TextView) findViewById(R.id.buycommod_commod_price_pos);
		buycommod_commod_price_end = (TextView) findViewById(R.id.buycommod_commod_price_end);
		findViewById(R.id.buycommod_commod_price_buy).setOnClickListener(this);
		((TextView) findViewById(R.id.buycommod_commod_merchant_name)).setText(shop_name);
		((TextView) findViewById(R.id.buycommod_commod_explian_text)).setText(title);
		ImageView buycommod_commod_explian_iv = (ImageView) findViewById(R.id.buycommod_commod_explian_iv);
		setImageView(buycommod_commod_explian_iv, imageUrl);
		buycommod_commod_explian_data_price.setText("¥" + price);

		findViewById(R.id.buycommod_consignee_linear_none).setOnClickListener(this);
		findViewById(R.id.buycommod_consignee_rela_data).setOnClickListener(this);
		findViewById(R.id.buycommod_consignee_rela).setOnClickListener(this);
		//界面显示控制
		findViewById(mall_buycommod_scrollview).setVisibility(View.GONE);
		mall_rela_end = (RelativeLayout) findViewById(R.id.mall_rela_end);
		mall_rela_end.setVisibility(View.GONE);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.back:
				if (back_state)
					showDialg();
				else
					this.finish();
				break;
			case R.id.buycommod_commod_explian_data_cut://减少
				if (num > 1) {
					num--;
					setRequest();
				} else {
					Tools.showToast(this, "最少选择1件商品");
					return;
				}

				break;
			case R.id.buycommod_commod_explian_data_add://添加
				if (num_state) {
//				Tools.showToast(this, "商品库存已不足");
					return;
				}
				if (max_num > 0 && num >= max_num) {
					Tools.showToast(this, "本商品仅限" + num + "件");
					return;
				}
				num++;
				setRequest();
				break;
			case R.id.buycommod_commod_price_buy://结算
				findViewById(R.id.buycommod_commod_price_buy).setEnabled(false);
				createOrder();
				break;
			case R.id.buycommod_consignee_linear_none:
			case R.id.buycommod_consignee_rela_data:
			case R.id.buycommod_consignee_rela:
				Intent intent = new Intent(this, AddressActivity.class);
				intent.putExtra("address_id", address_id);
				this.startActivityForResult(intent, OK_ADDRESS);
				break;
		}
	}

	@Override
	public void onBackPressed() {
		if (back_state)
			showDialg();
		else
			this.finish();
	}

	private void showDialg() {
		final Dialog dialog = new Dialog(this, R.style.dialog);
		dialog.setContentView(R.layout.a_mall_alipa_dialog);
		Window window = dialog.getWindow();
		window.findViewById(R.id.dialog_title).setVisibility(View.GONE);
		TextView dialog_message = (TextView) window.findViewById(R.id.dialog_message);
		dialog_message.setText("确认取消此订单？");
		TextView dialog_cancel = (TextView) window.findViewById(R.id.dialog_cancel);
		TextView dialog_sure = (TextView) window.findViewById(R.id.dialog_sure);
		dialog_cancel.setText("取消");
		dialog_sure.setText("确定");
		dialog_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		dialog_sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
				BuyCommodActivity.this.finish();
			}
		});
		dialog.show();
	}

	private void setRequest() {
		findViewById(R.id.buycommod_commod_explian_data_cut).setEnabled(false);
		findViewById(R.id.buycommod_commod_explian_data_add).setEnabled(false);
		String actionUrl = MallStringManager.mall_api_computeOrderAmt;
		String param = "product_code=" + code + "&product_num=" + num;
		MallReqInternet.in().doPost(actionUrl, param, new MallInternetCallback(this) {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				two_state++;
				loadManager.showProgressBar();
				loadManager.loadOver(flag, 1, true);
				if (flag >= UtilInternet.REQ_OK_STRING) {
					ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
					parsrInfoData(listMapByJson);
					num_state = false;
					handler.sendEmptyMessage(SHOW_OK);
				} else if (flag == UtilInternet.REQ_CODE_ERROR && msg instanceof Map) {//code错误
					Map<String, String> map = (Map<String, String>) msg;
					if ("5000001".equals(map.get("code"))) {//有库存不足
						num_state = true;
						num--;
					}
					setCodeDetla(map);
					Tools.showToast(BuyCommodActivity.this, map.get("msg"));
				}
				findViewById(R.id.buycommod_commod_explian_data_add).setEnabled(true);
				findViewById(R.id.buycommod_commod_explian_data_cut).setEnabled(true);
				buycommod_commod_explian_data_num.setText(num + "");
				if (num == 0)
					setNone_Num();

			}
		});
	}

	/**
	 * 处理token过期
	 *
	 * @param map
	 */
	private void setCodeDetla(Map<String, String> map) {
		//处理code过期问题
		if (MallCommon.code_past.equals(map.get("code"))) {
			common.setLoading(new InterfaceMallReqIntert() {

				@Override
				public void setState(int state) {
					if (state >= UtilInternet.REQ_OK_STRING) {
						setRequest();
					} else if (state == UtilInternet.REQ_CODE_ERROR) {
						loadManager.loadOver(state, 1, true);
					}
				}
			});
		}
	}

	private void parsrInfoData(ArrayList<Map<String, String>> listMapByJson) {
		Map<String, String> map = listMapByJson.get(0);
		findViewById(R.id.buycommod_commod_explian_data_none).setVisibility(View.GONE);
		buycommod_commod_price_res.setText("¥" + map.get("product_amt"));
		buycommod_commod_price_pos.setText("¥" + map.get("postage_amt"));
		buycommod_commod_price_end.setText("合计：¥" + map.get("amt"));
		max_num = Integer.parseInt(map.get("max_sale_num"));
	}

	/**
	 * 加载图片
	 *
	 * @param iv
	 * @param imageUrl
	 */
	private void setImageView(final ImageView iv, String imageUrl) {
		BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(this).load(imageUrl).build();
		if (bitmapRequest != null)
			bitmapRequest.into(iv);
	}

	/**
	 * 创建订单
	 */
	private void createOrder() {
		if (TextUtils.isEmpty(consignee_name) || TextUtils.isEmpty(consignee_tel) || TextUtils.isEmpty(consignee_address)) {
			findViewById(R.id.buycommod_commod_price_buy).setEnabled(true);
			Tools.showToast(this, "请先填写收货地址");
			return;
		}
		if (num <= 0) {
			Tools.showToast(this, "当前商品无货");
			return;
		}
		try {
			JSONArray array = new JSONArray();
			JSONObject object = new JSONObject();
			object.put("product_code", code);
			object.put("product_num", num + "");
			array.put(object);
			JSONObject object_address = new JSONObject();
			object_address.put("consignee_name", consignee_name);
			object_address.put("consignee_tel", consignee_tel);
			object_address.put("consignee_address", consignee_address);
			String url = MallStringManager.mall_api_createOrder;
			String param = "order_info=" + array.toString() + "&shipping_info=" + object_address.toString() + "&address_id=" + address_id;
			MallReqInternet.in().doPost(url, param, new MallInternetCallback(this) {

				@Override
				public void loadstat(int flag, String url, Object msg, Object... stat) {
					if (flag >= UtilInternet.REQ_OK_STRING) {
						ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
						setDataPay(listMapByJson);
					} else if (flag == UtilInternet.REQ_CODE_ERROR && msg instanceof Map) {
						Map<String, String> map = (Map<String, String>) msg;
						if ("5000001".equals(map.get("code"))) {//有库存不足
							Tools.showToast(BuyCommodActivity.this, "无货");
						} else {
							//处理code过期问题
							if (MallCommon.code_past.equals(map.get("code"))) {
								common.setLoading(new InterfaceMallReqIntert() {

									@Override
									public void setState(int state) {
										if (state >= UtilInternet.REQ_OK_STRING) {
											createOrder();
										} else if (state == UtilInternet.REQ_CODE_ERROR) {
											loadManager.loadOver(state, 1, true);
										}
									}
								});
							}
							Tools.showToast(BuyCommodActivity.this, map.get("msg"));
						}
					}
					findViewById(R.id.buycommod_commod_price_buy).setEnabled(true);

				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
			findViewById(R.id.buycommod_commod_price_buy).setEnabled(true);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case OK_ADDRESS:
				if (data != null) {
					findViewById(R.id.buycommod_consignee_linear_none).setVisibility(View.GONE);
					findViewById(R.id.buycommod_consignee_rela_data).setVisibility(View.VISIBLE);
					consignee_name = data.getStringExtra("consumer_name");
					consignee_tel = data.getStringExtra("consumer_mobile");
					consignee_address = data.getStringExtra("address_detail");
					address_id = data.getStringExtra("address_id");
					((TextView) findViewById(R.id.buycommod_consignee_man_name)).setText(consignee_name);
					((TextView) findViewById(R.id.buycommod_consignee_man_number)).setText(consignee_tel);
					((TextView) findViewById(R.id.buycommod_consignee_man_address)).setText(consignee_address);
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void setDataPay(ArrayList<Map<String, String>> listMapByJson) {
		Intent intent = new Intent(this, MallPayActivity.class);
		intent.putExtra("payment_order_id", listMapByJson.get(0).get("payment_order_id"));
		intent.putExtra("payment_order_amt", listMapByJson.get(0).get("payment_order_amt"));
		this.startActivity(intent);
	}

	/**
	 * 获取收货地址
	 */
	private void setRequestAddress() {
		loadManager.showProgressBar();
		String url = MallStringManager.mall_api_getShippingAddress;
		MallReqInternet.in().doGet(url, new MallInternetCallback(this) {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				loadManager.showProgressBar();
				loadManager.loadOver(flag, 1, true);
				if (flag >= UtilInternet.REQ_OK_STRING) {
					two_state++;
					ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
					setParseInfoAddress(listMapByJson);
					handler.sendEmptyMessage(SHOW_OK);
				} else if (flag == UtilInternet.REQ_CODE_ERROR) {
					//处理code过期问题
					Map<String, String> map = (Map<String, String>) msg;
					//处理code过期问题
					if (MallCommon.code_past.equals(map.get("code"))) {
						common.setLoading(new InterfaceMallReqIntert() {

							@Override
							public void setState(int state) {
								if (state >= UtilInternet.REQ_OK_STRING) {
									setRequestAddress();
								} else if (state == UtilInternet.REQ_CODE_ERROR) {
									loadManager.loadOver(state, 1, true);
								}
							}
						});
					}
				}


			}
		});
	}

	/**
	 * 解析地址数据
	 *
	 * @param listMapByJson
	 */
	private void setParseInfoAddress(ArrayList<Map<String, String>> listMapByJson) {
		if (listMapByJson.size() > 0) {
			findViewById(R.id.buycommod_consignee_linear_none).setVisibility(View.GONE);
			findViewById(R.id.buycommod_consignee_rela_data).setVisibility(View.VISIBLE);
			consignee_name = listMapByJson.get(0).get("consumer_name");
			consignee_tel = listMapByJson.get(0).get("consumer_mobile");
			consignee_address = listMapByJson.get(0).get("address_detail");
			address_id = listMapByJson.get(0).get("address_id");
			((TextView) findViewById(R.id.buycommod_consignee_man_name)).setText(consignee_name);
			((TextView) findViewById(R.id.buycommod_consignee_man_number)).setText(consignee_tel);
			((TextView) findViewById(R.id.buycommod_consignee_man_address)).setText(consignee_address);
		} else {
			findViewById(R.id.buycommod_consignee_linear_none).setVisibility(View.VISIBLE);
			findViewById(R.id.buycommod_consignee_rela_data).setVisibility(View.GONE);
		}
	}

	/**
	 * 解析address
	 */
	private void setAddress() {
		String strs = (String) UtilFile.loadShared(this, "user_address", "user_address");
		if (TextUtils.isEmpty(strs)) {
			findViewById(R.id.buycommod_consignee_linear_none).setVisibility(View.VISIBLE);
			findViewById(R.id.buycommod_consignee_rela_data).setVisibility(View.GONE);
		} else {
			findViewById(R.id.buycommod_consignee_linear_none).setVisibility(View.GONE);
			findViewById(R.id.buycommod_consignee_rela_data).setVisibility(View.VISIBLE);
			String[] address_data = strs.split("#");
			((TextView) findViewById(R.id.buycommod_consignee_man_name)).setText(address_data[0]);
			((TextView) findViewById(R.id.buycommod_consignee_man_number)).setText(address_data[1]);
			((TextView) findViewById(R.id.buycommod_consignee_man_address)).setText(address_data[2] + address_data[3]);
		}
	}

	/**
	 * 库存不足
	 */
	private void setNone_Num() {
		if (num_state) {//库存不足
			((TextView) findViewById(R.id.buycommod_commod_explian_text)).setTextColor(Color.parseColor("#8a8f97"));
			buycommod_commod_explian_data_price.setTextColor(Color.parseColor("#8a8f97"));
			findViewById(R.id.buycommod_commod_explian_data_none).setVisibility(View.VISIBLE);
			buycommod_commod_explian_data_num.setTextColor(Color.parseColor("#8a8f97"));

		} else {
			((TextView) findViewById(R.id.buycommod_commod_explian_text)).setTextColor(Color.parseColor("#333333"));
			buycommod_commod_explian_data_price.setTextColor(Color.parseColor("#E62323"));
			findViewById(R.id.buycommod_commod_explian_data_none).setVisibility(View.GONE);
			buycommod_commod_explian_data_num.setTextColor(Color.parseColor("#333333"));

		}
	}
}
