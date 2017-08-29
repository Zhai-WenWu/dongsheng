package third.mall.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.adapter.AdapterSimple;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import third.mall.activity.MyOrderActivity;
import third.mall.adapter.AdapterMyOrderItemNew;
import third.mall.adapter.AdapterMyOrderNew;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallCommon.InterfaceMallReqIntert;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.bean.OrderBean;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * 订单fragment
 * 
 * @author Administrator
 *
 */
public class MallOrderFragment extends MallBaseFragment implements OnClickListener {

	private MyOrderActivity mAct;
	private View view;
	private boolean isPrepared = false;
	LoadManager loadManager = null;
	private int currentPage = 0,everypage;
	public boolean LoadOver = false;
	private DownRefreshList order_list;
	public ArrayList<Map<String, String>> listData;
	public AdapterSimple adapter;
	private MallCommon common;
	private Handler handler;
	private String icon_but;

	public static Fragment getIntanse(OrderBean orderBean, String icon_but){
		MallOrderFragment fragment = new MallOrderFragment();
		return setArgumentsToFragment(fragment, orderBean,icon_but);
	}
	/** 将储块信息存板到Argument中 */
	public static Fragment setArgumentsToFragment(Fragment fragment, OrderBean orderBean,String icon_but) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(ORDERDATA,orderBean);
//		bundle.putParcelable(ORDERDATA, orderBean);
		bundle.putString("icon_but", icon_but);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		OrderBean data = (OrderBean) getArguments().getParcelable(ORDERDATA);
		OrderBean data = (OrderBean) getArguments().getSerializable(ORDERDATA);
		id = data.getId();
		icon_but=getArguments().getString("icon_but");
	}
	public MallOrderFragment() {
	}
	@Override
	public void onAttach(Activity activity) {
		mAct = (MyOrderActivity) activity;
		super.onAttach(activity);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = LayoutInflater.from(mAct).inflate(R.layout.a_mall_myorder_fragment, null);
		RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.activityLayout);
		loadManager = new LoadManager(mAct, rl);
		currentPage = 0;
		isPrepared = true;
		LoadOver = false;
		preLoad();
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void preLoad() {
		if (!isPrepared || !isVisible) {
			return;
		}
		// 填充各控件的数据
		// 防止二次生成
		if (!LoadOver) {
			init();
		}
	}
	@Override
	public void refresh() {
		if (LoadOver) {
			if (ToolsDevice.getNetActiveState(mAct)) {
//				mAct.progressBar.setVisibility(View.VISIBLE);
				setRequest(true);
			}
		}
	}

	public DownRefreshList getListView(){
		if(order_list==null)
			order_list = (DownRefreshList) view.findViewById(R.id.order_list);
		return order_list;
	}
	/**
	 * 初始化数据
	 */
	private void init() {
		common=new MallCommon(mAct);
		order_list = (DownRefreshList) view.findViewById(R.id.order_list);
		view.findViewById(R.id.shoppingcat_go).setOnClickListener(this);
		listData = new ArrayList<>();
		if(Integer.parseInt(id)>1){
			adapter = new AdapterMyOrderItemNew(mAct, order_list, listData, R.layout.a_mall_myorder_item_2, null, null,Integer.parseInt(id));
			
		}else{
			adapter =new AdapterMyOrderNew(mAct, order_list, listData, R.layout.a_mall_myorder_item_2, null, null, Integer.parseInt(id));
//			adapter = new AdapterMyOrder(mAct, order_list, listData, R.layout.a_mall_myorder_item, null, null,Integer.parseInt(id));
		}
		order_list.setDivider(null);
		adapter.scaleType = ScaleType.CENTER_CROP;
		adapter.isAnimate = true;
		clickListView();
	}

	private void clickListView() {
		if (!LoadOver) {
			loadManager.showProgressBar();
			loadManager.setLoading(order_list, adapter, true, new OnClickListener() {

				@Override
				public void onClick(View v) {
					setRequest(false);
				}
			}, new OnClickListener() {

				@Override
				public void onClick(View v) {
					setRequest(true);
				}
			});
			handler= new Handler(){
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case MallCommon.sucess_data:
						view.findViewById(R.id.shoppingcat_no_data).setVisibility(View.GONE);
						order_list.setVisibility(View.VISIBLE);
						break;

					case MallCommon.sucess_data_no:
						view.findViewById(R.id.shoppingcat_no_data).setVisibility(View.VISIBLE);
						order_list.setVisibility(View.GONE);
						break;
					}
					super.handleMessage(msg);
				}
			};
			LoadOver = true;
		}
	}

	/**
	 * 请求网络
	 * @param isStart
	 */
	private void setRequest(final boolean isStart) {
		isload=true;
		if(isStart)
			currentPage=1;
		else
			currentPage++;
		loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage,listData.size()==0);
		String url = MallStringManager.mall_api_listOrder_v3 + "?type=" + id + "&pn=" + currentPage + "&" + common.setStatistic(icon_but);
		MallReqInternet.in().doGet(url, new MallInternetCallback(mAct) {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {

				int loadCount = 0;
				loadManager.loadOver(flag, 1,true);
				if(flag>=UtilInternet.REQ_OK_STRING){
					if(isStart){
						listData.clear();
					}
					ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
					loadCount=listMapByJson.size();
					for (int i = 0; i < listMapByJson.size(); i++) {
						listData.add(listMapByJson.get(i));
					}
					String mall_stat_statistic = null;
					if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
						mall_stat_statistic=(String) stat[0];
					}
					if(adapter instanceof AdapterMyOrderItemNew){
						((AdapterMyOrderItemNew) adapter).setUrl(url,mall_stat_statistic);
					}else if(adapter instanceof AdapterMyOrderNew){
						((AdapterMyOrderNew) adapter).setUrl(url,mall_stat_statistic);
					}
					adapter.notifyDataSetChanged();
					if(listData.size()>0){
						handler.sendEmptyMessage(MallCommon.sucess_data);
					}else{
						handler.sendEmptyMessage(MallCommon.sucess_data_no);
					}
					if (isStart)
						order_list.setSelection(1);
				}else if(flag==UtilInternet.REQ_CODE_ERROR&& msg instanceof Map){
					Map<String,String> map= (Map<String, String>) msg;
					//处理code过期问题
					if(MallCommon.code_past.equals(map.get("code"))){
						common.setLoading(new InterfaceMallReqIntert() {
							
							@Override
							public void setState(int state) {
								if(state>=UtilInternet.REQ_OK_STRING){
									setRequest(true);
								}else if(state==UtilInternet.REQ_CODE_ERROR){
									loadManager.loadOver(state, 1,true);
								}
							}
						});
					}
				}
				loadManager.hideProgressBar();
				if(everypage==0)
					everypage= loadCount;
				currentPage = loadManager.changeMoreBtn(flag, loadCount, loadCount, currentPage,listData.size()==0);
				order_list.onRefreshComplete();
			
			}
		});
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.shoppingcat_go:

			break;
		}
	}
}
