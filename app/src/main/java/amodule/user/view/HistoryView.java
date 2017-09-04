package amodule.user.view;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;

/**
 * PackageName : amodule.user.view
 * Created by MrTrying on 2016/8/17 18:49.
 * E_mail : ztanzeyu@gmail.com
 */
public abstract class HistoryView{
	public BaseActivity mContext;
	public View mRoot;
	public LoadManager mLoadManager;
	public TextView noDataText;
	public Button noDataBtn;
	public ListView mListView;
	public PtrClassicFrameLayout refreshLayout;

	public HistoryView(BaseActivity activity){
		this.mContext = activity;
		mLoadManager = mContext.loadManager;
	}

	public HistoryView onCreateView(){
		mRoot = LayoutInflater.from(mContext).inflate(R.layout.a_user_browse_history_view,null);
		mListView = (ListView) mRoot.findViewById(R.id.list_view);
		refreshLayout = (PtrClassicFrameLayout) mRoot.findViewById(R.id.refresh_list_view_frame);
		noDataText = (TextView) mRoot.findViewById(R.id.no_data_text);
		noDataBtn = (Button) mRoot.findViewById(R.id.btn_no_data);
		mLoadManager.setLoading(refreshLayout, mListView, getAdapter(), true, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData(true);
			}
		}, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData(false);
			}
		});
		initView();
		return this;
	}

	public View getRootView(){
		return mRoot;
	}

	protected abstract void initView();
	protected abstract AdapterSimple getAdapter();
	protected abstract void loadData(boolean isRefresh);
	public abstract void cleanData();
	public abstract boolean hasData();
}
