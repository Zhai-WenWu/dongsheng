package amodule.user.view;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import amodule.main.activity.MainCircle;
import amodule.quan.activity.ShowSubject;
import amodule.user.db.BrowseHistorySqlite;
import aplug.basic.ReqInternet;

/**
 * PackageName : amodule.user.view
 * Created by MrTrying on 2016/8/17 11:13.
 * E_mail : ztanzeyu@gmail.com
 */
public class HistorySubjectView extends HistoryView{
	private final int LOAD_OVER = 2;
	private final int REFRESH_OVER = 3;
	private Handler mHandler;
	private AdapterSimple mAdapter;
	private List<Map<String, String>> mData = new ArrayList<>();
	private int currentPage = 0;

	public HistorySubjectView(BaseActivity context){
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
				mContext.startActivity(new Intent(mContext,MainCircle.class));
				mContext.finish();
			}
		});
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(mContext, ShowSubject.class);
				intent.putExtra("code",mData.get(position).get("code"));
				mContext.startActivity(intent);
			}
		});
		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				final DialogManager dialogManager = new DialogManager(mContext);
				dialogManager.createDialog(new ViewManager(dialogManager)
						.setView(new TitleMessageView(mContext).setText("确定删除该条浏览记录?"))
						.setView(new HButtonView(mContext)
								.setNegativeText("取消", new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										dialogManager.cancel();
									}
								})
								.setPositiveText("确定", new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										dialogManager.cancel();
										Map<String,String> map = mData.get(position);
										BrowseHistorySqlite sqlite = new BrowseHistorySqlite(mContext);
										sqlite.deleteByCode(BrowseHistorySqlite.TB_SUBJECT_NAME, map.get("code"));
										mData.remove(map);
										mAdapter.notifyDataSetChanged();
									}
								}))).show();
				return true;
			}
		});
	}

	@Override
	protected AdapterSimple getAdapter() {
		mAdapter = new AdapterSimple(mListView, mData,
				R.layout.a_history_item_quan,
				new String[] { "title", "content", "nickName", "commentNum", "likeNum" },
				new int[] { R.id.quansearch_title,R.id.quansearch_content, R.id.quansearch_userName,
						R.id.quansearch_ping, R.id.quansearch_zan });
		mAdapter.scaleType= ImageView.ScaleType.CENTER_CROP;
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
				List<Map<String, String>> data = sqlite.loadByPage(BrowseHistorySqlite.TB_SUBJECT_NAME, ++currentPage);
				for(int index = 0 ; index < data.size() ; index ++){
					Map<String,String> map = data.get(index);
					map.put("commentNum", map.get("commentNum").equals("0") ? "" : map.get("commentNum") + "评论/");
					map.put("likeNum", map.get("likeNum").equals("0") ? "" : map.get("likeNum") + "赞");
					map.put("content", TextUtils.isEmpty(map.get("content")) ? "   " : map.get("content"));
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
		sqlite.deleteByCode(BrowseHistorySqlite.TB_SUBJECT_NAME,null);
		mData.clear();
		Message msg = mHandler.obtainMessage(LOAD_OVER, 0, 0);
		mHandler.sendMessage(msg);
	}

	@Override
	public boolean hasData() {
		return mData != null && mData.size() > 0;
	}

}
