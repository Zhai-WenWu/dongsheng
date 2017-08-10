package third.mall.dialog;

import android.app.Activity;
import android.content.Context;
import android.widget.ListView;


import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.Tools;
import aplug.basic.ReqInternet;
import third.mall.adapter.AdapterFavorable;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import xh.basic.tool.UtilString;

/**
 * 选择优惠券的dialog
 * 
 * @author yujian
 *
 */
public class BuyDialog extends SimpleDialog {

	private AdapterFavorable adapter;
	private Context context;
	private ArrayList<Map<String,String>> list_coupon = new ArrayList<Map<String,String>>();
	private String shop_code;
	private showCallBack callback;
	public BuyDialog(Activity activity, String shop_code) {
		super(activity, R.layout.dialog_mall_favorable);
		this.context = activity;
		this.shop_code= shop_code;
		setLatyoutHeight();
		init();
		setCallBack(null);
	}

	public void init() {
		ListView listview_dialog = (ListView) view.findViewById(R.id.listview_dialog);
		adapter=new AdapterFavorable(context,listview_dialog, list_coupon, R.layout.item_mall_favorable_dialog, null, null);
		listview_dialog.setAdapter(adapter);
		setRequest(shop_code);
	}
	public void setCallBack(showCallBack callbacks){
		if(null==callbacks){
			callback= new showCallBack() {
				
				@Override
				public void setShow() {
					BuyDialog.this.show();
				}
			};
		}else
			this.callback= callbacks;
	}
	/**
	 * 请求获取优惠券信息
	 */
	private void setRequest(String shop_code) {
		String url=MallStringManager.mall_getShopCouponInfo+"?shop_code="+shop_code;
		MallReqInternet.in().doGet(url, new MallInternetCallback(context) {
			
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {

				list_coupon.clear();
				if(flag>=ReqInternet.REQ_OK_STRING){
					ArrayList<Map<String,String>> lists= UtilString.getListMapByJson(msg);
					ArrayList<Map<String,String>> list_map= UtilString.getListMapByJson(lists.get(0).get("shop_coupon_package"));
					for (int i = 0,size=list_map.size(); i < size; i++) {
						list_coupon.add(list_map.get(i));
					}
					if(list_coupon.size()>0){
						adapter.notifyDataSetChanged();
						if(callback!=null){
							callback.setShow();
						}
					}
				}else if (flag==ReqInternet.REQ_CODE_ERROR && msg instanceof Map ){
					Map<String,String> map=(Map<String, String>) msg;
					Tools.showToast(context, map.get("msg"));
				}
			
			}
		});
	}

	public interface showCallBack{
		public void setShow();
	}
}
