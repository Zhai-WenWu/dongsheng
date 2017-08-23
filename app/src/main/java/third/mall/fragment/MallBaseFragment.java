package third.mall.fragment;

import android.support.v4.app.Fragment;
import android.util.Log;

public abstract class MallBaseFragment extends Fragment {
	protected boolean isVisible=false;
	/** 保存板块信息的key */
	protected static final String ORDERDATA = "orderData";
	public String id;
	public boolean isload=false;
	public boolean LoadState=false;

	/**
	 * 在这里实现Fragment数据的缓加载.
	 * 
	 * @param isVisibleToUser
	 */
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		LoadState= isload;
		if (getUserVisibleHint()) {
			isVisible = true;
			onVisible();
		} else {
			isVisible = false;
			onInvisible();
		}
		Log.i("isVisible"+id, isVisible+"");
	}

	protected void onVisible() {
		preLoad();
	}

	protected abstract void preLoad();

	protected void onInvisible() {
	}
	public void refresh(){}
}
