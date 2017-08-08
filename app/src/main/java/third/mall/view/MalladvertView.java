package third.mall.view;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import third.mall.activity.CommodDetailActivity;
import third.mall.adapter.AdapterMallAdvert;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import xh.basic.tool.UtilString;
import acore.logic.AppCommon;
import acore.logic.SetDataView;
import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView.ScaleType;
import aplug.basic.ReqInternet;

import com.xianghatest.R;

/**
 * 电商横向广告模块
 * @author yujian
 *
 */
public class MalladvertView extends ViewItemBase {

	private Activity activity;
//	private HorizontalListView horizontalListView;
	private HorizontalScrollView advertView;
	private ArrayList<Map<String,String>> list_data= new ArrayList<Map<String,String>>();
	private AdapterSimple adapter;
	private String mTongjiId;
	public MalladvertView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MalladvertView(Context context) {
		super(context);
	}
	/**
	 * 设置数据，不调用不会显示view
	 * @param msg burden 字段数据；
	 */
	public void setData(Activity context,Object msg,String tongjiId){
		this.activity= context;
		mTongjiId = tongjiId;
		initView();
		if(MallCommon.isShowMallAdvert){
			ArrayList<Map<String,String>> list=UtilString.getListMapByJson(msg);
			if(list.size()>0){
				initData(list);
			}
		}
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		LayoutInflater.from(activity).inflate(R.layout.mall_advert_view, this, true);
		
		advertView=(HorizontalScrollView) findViewById(R.id.scrollview);
//		horizontalListView=(HorizontalListView) findViewById(R.id.horizontalListView);
//		adapter= new AdapterSimple(horizontalListView, list_data, R.layout.a_mall_advert_item_view, new String[] {"img","title" , "price" }, 
//				new int[] { R.id.iv_img,R.id.itemText1, R.id.itemText2 });
//		horizontalListView.setAdapter(adapter);
//		horizontalListView.setVisibility(View.VISIBLE);
		this.setVisibility(View.GONE);
		findViewById(R.id.textview_rela).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AppCommon.openUrl(activity, "xhds.home.app", true);
				XHClick.mapStat(activity, mTongjiId, "你可能会喜欢", "更多的点击量");
			}
		});
		
	}

	/**
	 * 初始化数据
	 */
	private void initData(ArrayList<Map<String,String>> list) {
		String params="ingre="+getrequestData(list).toString();
		MallReqInternet.in().doPost(MallStringManager.mall_dish,params, new MallInternetCallback(getContext()) {
			
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(flag>=ReqInternet.REQ_OK_STRING){
					ArrayList<Map<String,String>> list_temp= UtilString.getListMapByJson(msg);
					if(list_temp.size()>0){
						for (int i = 0,size=list_temp.size(); i < size; i++) {
							list_data.add(list_temp.get(i));
						}
						adapter= new AdapterMallAdvert(activity,advertView, list_data, R.layout.a_mall_advert_item_view, null, 
								null);
						adapter.scaleType = ScaleType.CENTER_CROP;
						SetDataView.ClickFunc[] tableClick = { new SetDataView.ClickFunc() {
							@Override
							public void click(int index, View v) {
								XHClick.mapStat(activity, mTongjiId, "你可能会喜欢", "相关商品的点击量");
								Intent intent = new Intent(activity, CommodDetailActivity.class);
								intent.putExtra("product_code", list_data.get(index).get("product_code") + "");
								activity.startActivity(intent);
							}
						} };
						SetDataView.horizontalView(advertView, adapter, null, tableClick);
						advertView.setVisibility(View.VISIBLE);
						MalladvertView.this.setVisibility(View.VISIBLE);
					}else{
						MalladvertView.this.setVisibility(View.GONE);
					}
					
				}else{
					
				}
			
			}
		});
	}

	/**
	 * 拼装jsonarray
	 * @param isFirst
	 * @return
	 */
	private JSONArray getrequestData(ArrayList<Map<String,String>> list){
		JSONArray jsonArray= new JSONArray();
		try {
			for (int i = 0,size=list.size(); i < size; i++) {
				JSONObject jsonObject=new JSONObject();
				jsonObject.put("name", list.get(i).get("name"));
				jsonObject.put("type", list.get(i).get("type"));
				jsonArray.put(jsonObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonArray;
	}
}
