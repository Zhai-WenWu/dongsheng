package amodule.user.view;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.nous.activity.HomeNous;
import amodule.user.db.BrowseHistorySqlite;
import aplug.basic.ReqInternet;
import xh.windowview.XhDialog;

/**
 * PackageName : amodule.user.view
 * Created by MrTrying on 2016/8/17 11:13.
 * E_mail : ztanzeyu@gmail.com
 */
public class HistoryNousView extends HistoryView {
	private final int LOAD_OVER = 2;
	private final int REFRESH_OVER = 3;
	private Handler mHandler;
	private AdapterNous mAdapter;
	private List<Map<String, String>> mData = new ArrayList<>();
	private int currentPage = 0;

	public HistoryNousView(BaseActivity context) {
		super(context);
		mHandler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
					case LOAD_OVER:
						mLoadManager.changeMoreBtn(mListView, ReqInternet.REQ_OK_STRING, 10, msg.arg1, currentPage, true);
						mAdapter.notifyDataSetChanged();
						mRoot.findViewById(R.id.noData_layout).setVisibility(mData.size() == 0 ? View.VISIBLE : View.GONE);
						mListView.setVisibility(mData.size() == 0 ? View.GONE : View.VISIBLE);
						break;
					case REFRESH_OVER:
						refreshLayout.refreshComplete();
						break;
					default:
						break;
				}
				return false;
			}
		});
	}

	@Override
	protected void initView() {
		noDataText.setText("暂无浏览记录哦，快去逛逛吧~");
		noDataBtn.setText("去逛逛");
		noDataBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mContext.startActivity(new Intent(mContext, HomeNous.class));
			}
		});
	}

	@Override
	protected AdapterSimple getAdapter() {
		mAdapter = new AdapterNous(mContext, mListView, mData,
				R.layout.a_nous_item_myfavorite,
				new String[]{"img", "title", "content", "allClick"},
				new int[]{R.id.iv_nousCover, R.id.tv_nousTitle, R.id.tv_nousContent1, R.id.tv_allClick});
		mAdapter.contentWidth = ToolsDevice.getWindowPx(mContext).widthPixels - Tools.getDimen(mContext, R.dimen.dp_100);//12=15*2+80+10
		return mAdapter;
	}

	@Override
	protected void loadData(final boolean isRefresh) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (isRefresh) {
					currentPage = 0;
					mData.clear();
				}
				BrowseHistorySqlite sqlite = new BrowseHistorySqlite(mContext);
				List<Map<String, String>> data = sqlite.loadByPage(BrowseHistorySqlite.TB_NOUS_NAME, ++currentPage);
				for (int index = 0; index < data.size(); index++) {
					Map<String, String> map = data.get(index);
					map.put("allClick", map.get("allClick").equals("0") ? "" : map.get("allClick") + "浏览");
				}
				mData.addAll(data);
				if (isRefresh) {
					mHandler.sendEmptyMessage(REFRESH_OVER);
				}
				Message msg = mHandler.obtainMessage(LOAD_OVER, data.size(), 0);
				mHandler.sendMessage(msg);
			}
		}).start();
	}

	@Override
	public void cleanData() {
		BrowseHistorySqlite sqlite = new BrowseHistorySqlite(mContext);
		sqlite.deleteByCode(BrowseHistorySqlite.TB_NOUS_NAME, null);
		mData.clear();
		Message msg = mHandler.obtainMessage(LOAD_OVER, 0, 0);
		mHandler.sendMessage(msg);
	}

	public class AdapterNous extends AdapterSimple {

		public int contentWidth = 0;
		private BaseActivity mAct;
		private List<? extends Map<String, ?>> data;

		public AdapterNous(BaseActivity act, View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
			super(parent, data, resource, from, to);
			this.data = data;
			this.mAct = act;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			Map<String, String> map = (Map<String, String>) data.get(position);
			view.findViewById(R.id.search_fake_layout).setVisibility(View.VISIBLE);
			view.findViewById(R.id.relativeLayout2).setVisibility(View.GONE);
			view.findViewById(R.id.nous_image).setVisibility(View.GONE);
			TextView tv_nousContent = (TextView) view.findViewById(R.id.tv_nousContent1);
			TextView tv_nousContent2 = (TextView) view.findViewById(R.id.tv_nousContent2);
			String content = map.get("content");
			int number = contentWidth / ToolsDevice.sp2px(mAct, Tools.getDimen(mAct, R.dimen.dp_15));
			int number2 = number + (number / 2);
			tv_nousContent.setText(content.length() > number?content.substring(0, number):content);
			if(content.length() > number) {
				if (content.length() <= number2) {
					tv_nousContent2.setText(content.substring(number, content.length()));
				} else {
					tv_nousContent2.setText(content.substring(number, number2) + "...");
				}
			}else{
				tv_nousContent2.setText("");
			}
			setOnclick(map, view, position);
			return view;
		}

		// 绑定点击动作
		private void setOnclick(final Map<String, String> map, View view, final int i) {
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AppCommon.openUrl(mAct, "nousInfo.app?code=" + map.get("code"), true);
				}
			});
			view.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					final XhDialog dialog = new XhDialog(mAct);
					dialog.setTitle("确定删除该条浏览记录?")
							.setCanselButton("确定", new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									BrowseHistorySqlite sqlite = new BrowseHistorySqlite(mContext);
									sqlite.deleteByCode(BrowseHistorySqlite.TB_NOUS_NAME, map.get("code"));
									data.remove(map);
									notifyDataSetChanged();
									dialog.cancel();
								}
							})
							.setSureButtonTextColor("#333333")
							.setSureButton("取消", new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									dialog.cancel();
								}
							});
					dialog.show();
					return true;
				}
			});
		}
	}

	@Override
	public boolean hasData() {
		return mData != null && mData.size() > 0;
	}
}
