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

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.tools.FileManager;
import acore.widget.DownRefreshList;
import third.mall.adapter.AdapterMyFavorable;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallCommon.InterfaceMallReqIntert;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

/**
 * 我的优惠券
 * @author yujian
 *
 */
public class MallMyFavorableFragment extends MallBaseFragment{

	private String id;
	private Activity activity;
	private View view;
	private boolean isPrepared = false;
	LoadManager loadManager = null;
	private int currentPage = 0,everypage;
	public boolean LoadOver = false;
	private DownRefreshList favorable_list;
	public ArrayList<Map<String, String>> listData;
	public AdapterMyFavorable adapter;
	private MallCommon common;
	private Handler handler;
	private View view_foot;
	public static Fragment getInstance(String id){
		MallMyFavorableFragment fragment = new MallMyFavorableFragment();
		return setDataArauments(fragment, id);
	}
	public static Fragment setDataArauments(Fragment fragment,String id){
		Bundle bundle= new Bundle();
		bundle.putString("id", id);
		fragment.setArguments(bundle);
		return fragment;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		id= getArguments().getString("id");
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity= activity;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view= LayoutInflater.from(activity).inflate(R.layout.a_mall_myfavorable_fragment, null);
		RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.activityLayout);
		loadManager = new LoadManager(activity, rl);
		currentPage = 0;
		isPrepared = true;
		LoadOver = false;
		preLoad();
		return view;
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
	private void init() {
		//footview
		favorable_list=(DownRefreshList) view.findViewById(R.id.favorable_list);
		setFootView();
		common=new MallCommon(activity);
		listData = new ArrayList<>();
		adapter=new AdapterMyFavorable(activity,favorable_list, listData, R.layout.item_mall_favorable_dialog, null, null,id);
//		favorable_list.setAdapter(adapter);
		favorable_list.setDivider(null);
		adapter.scaleType = ScaleType.CENTER_CROP;
		adapter.isAnimate = true;
		clickListView();
		view.findViewById(R.id.favorable_more).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String state_index="";
				switch (id) {
					case "1":
						state_index = "未使用";
						break;
					case "2":
						state_index = "已使用";
						break;
					case "3":
						state_index = "已过期";
						break;
				}
				XHClick.mapStat(activity, "a_mail_coupon",state_index,"更多好券");
				String url = MallStringManager.replaceUrl(MallStringManager.mall_web_couponSet) ;
				AppCommon.openUrl(activity, url, true);
			}
		});
	}
	public DownRefreshList getListView(){
		if(favorable_list==null)
			favorable_list= (DownRefreshList) view.findViewById(R.id.favorable_list);
		return favorable_list;
	}
	/**
	 * 处理footView
	 */
	private void setFootView() {
		view_foot = LayoutInflater.from(activity).inflate(R.layout.mall_myfavorable_foot_view, null);
		view_foot.setVisibility(View.GONE);
		if("1".equals(id)){
			view_foot.findViewById(R.id.favorable_ll).setVisibility(View.VISIBLE);
			view_foot.findViewById(R.id.favorable_ll).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					XHClick.mapStat(activity, "a_mail_coupon","未使用","发现更多优惠好券");
					String mall_stat=(String) UtilFile.loadShared(activity, FileManager.MALL_STAT, FileManager.MALL_STAT);
					String url = MallStringManager.replaceUrl(MallStringManager.mall_web_couponSet)+"?"+mall_stat ;
					AppCommon.openUrl(activity, url, true);
				}
			});
			view_foot.findViewById(R.id.item_line_rela).setVisibility(View.GONE);
		}else if("3".equals(id)){
			view_foot.findViewById(R.id.favorable_ll).setVisibility(View.GONE);
			view_foot.findViewById(R.id.item_line_rela).setVisibility(View.VISIBLE);
		}else{
			view_foot.setVisibility(View.GONE);
		}
		
		favorable_list.addFooterView(view_foot);
	}
	private void clickListView() {
		if (!LoadOver) {
			loadManager.showProgressBar();
			loadManager.setLoading(favorable_list, adapter, true, new OnClickListener() {

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
						view.findViewById(R.id.rela_favorable_more).setVisibility(View.GONE);
						favorable_list.setVisibility(View.VISIBLE);
						if(id.equals("1")||id.equals("3"))
							view_foot.setVisibility(View.VISIBLE);
						break;

					case MallCommon.sucess_data_no:
						view.findViewById(R.id.rela_favorable_more).setVisibility(View.VISIBLE);
						favorable_list.setVisibility(View.GONE);
						view_foot.setVisibility(View.GONE);
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
		String url =MallStringManager.getShopCouponList+"?status="+id+"&page="+currentPage+"&"+common.setStatistic("my_coupon");
		MallReqInternet.in().doGet(url, new MallInternetCallback() {
			
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
					adapter.setUrl(url,mall_stat_statistic);
					adapter.notifyDataSetChanged();
					if(listData.size()>0){
						handler.sendEmptyMessage(MallCommon.sucess_data);
					}else{
						handler.sendEmptyMessage(MallCommon.sucess_data_no);
					}
					if (isStart)
						favorable_list.setSelection(1);
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
				favorable_list.onRefreshComplete();
			
			}
		});
	}
}
