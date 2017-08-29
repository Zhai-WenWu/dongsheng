package third.mall.adapter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import third.mall.activity.CommodDetailActivity;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilImage;
import acore.tools.Tools;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.xianghatest.R;

/**
 * 购物车adapter二级
 *
 * @author Administrator
 */
public class AdapterShoppingItem extends MallAdapterSimple {
	private Context context;
	private ArrayList<Map<String, String>> data;
	private interNumProudct internumproudct;

	public AdapterShoppingItem(Context context, View parent, ArrayList<Map<String, String>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.context = context;
		this.data = data;
	}

	public void setInterNumproudct(interNumProudct internumproudct) {
		this.internumproudct = internumproudct;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Map<String, String> map = data.get(position);
		// 缓存视图
		ViewCache viewCache;
		if (convertView == null) {
			viewCache = new ViewCache();
			convertView = LayoutInflater.from(context).inflate(R.layout.a_mall_shopping_listview_item, parent, false);
			viewCache.setView(convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) convertView.getTag();
		}
		viewCache.setValue(map, position);
		if (internumproudct != null) {
			viewCache.setInterNumproudctItem(internumproudct);
		}

		return convertView;
	}

	private class ViewCache {

		private ImageView shopping_item_commod_choose;
		private ImageView shopping_item_commod_iv;
		private TextView shopping_item_commod_text;
		private RelativeLayout shopping_item_commod_rela;
		private TextView shopping_item_commod_price;
		private TextView shopping_item_commod_none;
		private RelativeLayout shopping_item_commod_cut;
		private TextView shopping_item_commod_num;
		private RelativeLayout shopping_item_commod_add;
		private interNumProudct internumproudctitem;
		private String choose_state = "1";//选择状态 0-未选中，1-选中
		private String edit_product = "0";//选择状态 0-结算，1-编辑
		private int max_sale_num = 0;
		private int saleable_num = 0;

		public void setInterNumproudctItem(interNumProudct internumproudctitem) {
			this.internumproudctitem = internumproudctitem;
		}

		public void setView(View view) {
			shopping_item_commod_choose = (ImageView) view.findViewById(R.id.shopping_item_commod_choose);
			shopping_item_commod_iv = (ImageView) view.findViewById(R.id.shopping_item_commod_iv);
			shopping_item_commod_text = (TextView) view.findViewById(R.id.shopping_item_commod_text);
			shopping_item_commod_rela = (RelativeLayout) view.findViewById(R.id.shopping_item_commod_rela);
			shopping_item_commod_price = (TextView) view.findViewById(R.id.shopping_item_commod_price);
			shopping_item_commod_none = (TextView) view.findViewById(R.id.shopping_item_commod_none);
			shopping_item_commod_cut = (RelativeLayout) view.findViewById(R.id.shopping_item_commod_cut);
			shopping_item_commod_num = (TextView) view.findViewById(R.id.shopping_item_commod_num);
			shopping_item_commod_add = (RelativeLayout) view.findViewById(R.id.shopping_item_commod_add);
			shopping_item_commod_rela.setVisibility(View.VISIBLE);

		}

		public void setValue(final Map<String, String> map, final int position) {
			if ("2".equals(map.get("stock_flag"))) {
				int num = Integer.parseInt(map.get("num"));
				int saleable_num = Integer.parseInt(map.get("saleable_num"));
				if (num >= saleable_num) {
					map.put("num", saleable_num + "");
					shopping_item_commod_none.setText("(仅剩" + saleable_num + "件)");
					shopping_item_commod_none.setVisibility(View.VISIBLE);
				} else {
					shopping_item_commod_none.setVisibility(View.GONE);
				}
			} else {
				shopping_item_commod_none.setVisibility(View.GONE);
			}
			//设置图片
			setViewImage(shopping_item_commod_iv, map.get("img"));
			shopping_item_commod_text.setText(map.get("title"));
			shopping_item_commod_price.setText("¥" + map.get("discount_price"));
			shopping_item_commod_num.setText(map.get("num"));
			max_sale_num = Integer.parseInt(map.get("max_sale_num"));
			//商品状态
			saleable_num = Integer.parseInt(map.get("saleable_num"));

//			setCommodState(saleable_num);
			if (map.containsKey("choose_state")) {
				choose_state = map.get("choose_state");
			}
			map.put("choose_state", choose_state);
			if (map.containsKey("edit_product")) {
				edit_product = map.get("edit_product");
			}
			if ("1".equals(edit_product)) {
				shopping_item_commod_choose.setEnabled(true);
			} else {
			}
			map.put("edit_product", edit_product);
			if (!map.containsKey("choose_state") || "1".equals(map.get("choose_state"))) {
				shopping_item_commod_choose.setImageResource(R.drawable.z_mall_shopcat_choose);
			} else if ("0".equals(map.get("choose_state"))) {
				shopping_item_commod_choose.setImageResource(R.drawable.z_mall_shopcat_no_choose);
			}
			//设置当前状态

			//选中点击
			shopping_item_commod_choose.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (choose_state.equals("1")) {//取消
						choose_state = "0";
						shopping_item_commod_choose.setImageResource(R.drawable.z_mall_shopcat_no_choose);
					} else if (choose_state.equals("0")) {//选中
						choose_state = "1";
						shopping_item_commod_choose.setImageResource(R.drawable.z_mall_shopcat_choose);
					}
					map.put("choose_state", choose_state);
					internumproudctitem.setProudctChooseAndchange(data, position);
				}
			});
			//减少数量
			shopping_item_commod_cut.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int num_now = Integer.parseInt((String) shopping_item_commod_num.getText());
					if (num_now <= 1) {
					} else {
						num_now--;
						updateCartInfo(map, map.get("code"), num_now, false, position);
					}

				}
			});
			//增加数量
			shopping_item_commod_add.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int num_now = Integer.parseInt((String) shopping_item_commod_num.getText());
					if (saleable_num > 0 && num_now < saleable_num) {//达到最大购买数量
						if (max_sale_num > 0 && num_now >= max_sale_num) {
							Tools.showToast(context, "该单品最大可购买" + num_now + "件");
						} else {
							num_now++;
							updateCartInfo(map, map.get("code"), num_now, true, position);
						}

					} else {
						Tools.showToast(context, "该单品最大可购买" + num_now + "件");
					}
				}
			});
			//点击事件
			shopping_item_commod_iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, CommodDetailActivity.class);
					intent.putExtra("product_code", map.get("code"));
					context.startActivity(intent);
				}
			});
			shopping_item_commod_text.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, CommodDetailActivity.class);
					intent.putExtra("product_code", map.get("code"));
					context.startActivity(intent);
				}
			});
		}

		/**
		 * 修改商品数量
		 *
		 * @param code
		 * @param num
		 */
		private void updateCartInfo(final Map<String, String> maps, String code, final int num, final boolean state, final int position) {
			shopping_item_commod_cut.setEnabled(false);
			shopping_item_commod_add.setEnabled(false);
			String param = "product_code=" + code + "&product_num=" + num;
			MallReqInternet.in().doPost(MallStringManager.mall_updateCartInfo, param, new MallInternetCallback(context) {

				@Override
				public void loadstat(int flag, String url, Object msg, Object... stat) {
					if (flag >= ReqInternet.REQ_OK_STRING) {
						shopping_item_commod_none.setVisibility(View.GONE);
						shopping_item_commod_num.setText(num + "");
						maps.put("num", num + "");

						if ("0".equals(maps.get("choose_state"))) {
							if (choose_state.equals("1")) {//取消
								choose_state = "0";
								shopping_item_commod_choose.setImageResource(R.drawable.z_mall_shopcat_no_choose);
							} else if (choose_state.equals("0")) {//选中
								choose_state = "1";
								shopping_item_commod_choose.setImageResource(R.drawable.z_mall_shopcat_choose);
							}
							maps.put("choose_state", choose_state);
							internumproudctitem.setProudctChooseAndchange(data, position);
						} else {
							if (internumproudctitem != null) {
								internumproudctitem.setProudctChooseAndchange(data, position);
							}
						}

					} else if (flag == UtilInternet.REQ_CODE_ERROR) {
						Map<String, String> map = (Map<String, String>) msg;
						Tools.showToast(context, map.get("msg") + "");
					}
					shopping_item_commod_cut.setEnabled(true);
					shopping_item_commod_add.setEnabled(true);

				}
			});
		}
	}

	@Override
	public void setViewImage(final ImageView v, String value) {
		v.setVisibility(View.VISIBLE);
		// 异步请求网络图片
		if (value.indexOf("http") == 0) {
			if (v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(value))
				return;

			v.setScaleType(ScaleType.CENTER_CROP);
			//设置默认图
			if (v.getId() == R.id.iv_userImg_one ||
					v.getId() == R.id.iv_userImg_two ||
					v.getId() == R.id.iv_userImg_three) {

			} else {
				InputStream is = v.getResources().openRawResource(imgResource);
				Bitmap bitmap = UtilImage.inputStreamTobitmap(is);
				if (roundImgPixels > 0)
					bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, roundType, roundImgPixels);
				UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
			}

			if (value.length() < 10)
				return;

			v.setTag(TAG_ID, value);
			BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mParent.getContext())
					.load(value)
					.setImageRound(roundImgPixels)
					.setSaveType(imgLevel)
					.build();
			if (bitmapRequest != null)
				bitmapRequest.into(getTarget(v, value));
		}
		// 直接设置为内部图片
		else if (value.indexOf("ico") == 0) {
			InputStream is = v.getResources().openRawResource(Integer.parseInt(value.replace("ico", "")));
			Bitmap bitmap = UtilImage.inputStreamTobitmap(is);
			bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, roundType, roundImgPixels);
			UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
		}
		// 隐藏
		else if (value.equals("hide") || value.length() == 0)
			v.setVisibility(View.GONE);
			// 直接加载本地图片
		else if (!value.equals("ignore")) {
			Bitmap bmp = UtilImage.imgPathToBitmap(value, imgWidth, imgHeight, false, null);
			v.setScaleType(scaleType);
			v.setImageBitmap(bmp);
		}
		// 如果为ignore,则忽略图片
	}

	/**
	 * 商品回调接口
	 *
	 * @author Administrator
	 */
	public interface interNumProudct {
		/**
		 * 商品回调
		 *
		 * @param data---当前商家的全部商品
		 * @param position
		 */
		void setProudctChooseAndchange(ArrayList<Map<String, String>> data, int position);
	}
}
