package amodule.quan.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import acore.override.XHApplication;
import amodule.quan.db.PlateData;
import aplug.stickheaderlayout.PlaceHoderHeaderLayout;
import aplug.stickheaderlayout.StickHeaderViewPagerManager;
import third.location.LocationSys;
import third.location.LocationSys.LocationSysCallBack;

public abstract class BaseCirclerFragment extends Fragment{
	/** 保存板块信息的key */
	protected static final String PLATEDATA = "plate_data";
	public static final String CIRCLENAME = "circle_name";
	protected PlaceHoderHeaderLayout placeHoderHeaderLayout;
    protected StickHeaderViewPagerManager manager;
    /** 是否初始化 */
    protected boolean isPrepared = false;
    /** 是否显示 */
    protected boolean isVisible;
    /** 板块的mid */
    protected String mid = "";
    protected String mCircleName = "";
    /** 是否可以使用 ptr 刷新框架*/
    boolean isCanPulltoRefresh = false;
    /** fragment在fragmentManager管理的集合中index */
    int position = 0;
    String isLocation = "1";
	private LocationSys mLocationSys;

	public BaseCirclerFragment(){
		mLocationSys = new LocationSys(XHApplication.in());
		refershLocation();
	}

    /** 将储块信息存板到Argument中 */
    public static Fragment setArgumentsToFragment(Fragment fragment,PlateData plateData){
    	Bundle bundle =new Bundle();
    	bundle.putSerializable(PLATEDATA, plateData);
    	fragment.setArguments(bundle);
    	return fragment;
    }
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PlateData data = (PlateData) getArguments().getSerializable(PLATEDATA);
		mid = data.getMid();
		mCircleName = getArguments().getString(CIRCLENAME);
	}

    @Override
    public final View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        View view = onCreateViewHandler(inflater, container, savedInstanceState);
	    //因为使用空构造器的原因，此变量可能为null
	    if(manager != null){
            manager.addPlaceHoderHeaderLayout(position,placeHoderHeaderLayout,isCanPulltoRefresh);
	    }
        return view;
    }
	
    /**
     * 在这里实现Fragment数据的缓加载.
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }
    
    protected void onVisible(){
        preLoad();
    }
    
    protected abstract void preLoad();
    protected void onInvisible(){}
    
    public void refresh(){
    	refershLocation();
    }
    
    /**
     * 更新定位信息
     */
    public void refershLocation(){
    	//测试设置，强制开启定位
    	if(PlateData.LOCATION.equals(isLocation) && mLocationSys != null){
    		mLocationSys.starLocation(new LocationSysCallBack() {
				@Override public void onLocationFail() { }
			});
    	}
    }


	@Override
    public void onDestroy() {
    	super.onDestroy();
    	if(mLocationSys != null){
    		mLocationSys.stopLocation();
    	}
    }

	public StickHeaderViewPagerManager getManager() {
		return manager;
	}

	public void setManager(StickHeaderViewPagerManager manager) {
		this.manager = manager;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getIsLocation() {
		return isLocation;
	}

	public void setIsLocation(String isLocation) {
		this.isLocation = isLocation;
	}

	public boolean isCanPulltoRefresh() {
		return isCanPulltoRefresh;
	}

	public void setCanPulltoRefresh(boolean canPulltoRefresh) {
		isCanPulltoRefresh = canPulltoRefresh;
	}

	/** */
    public abstract View onCreateViewHandler(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) ;
}
