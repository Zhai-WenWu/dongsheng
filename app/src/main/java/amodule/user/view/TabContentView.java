package amodule.user.view;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

import java.util.Map;

import acore.widget.DownRefreshList;
import acore.widget.LayoutScroll;

public abstract class TabContentView implements TabContentFactory{

	public View view;
	public DownRefreshList theListView=null;
	public LayoutScroll scrollLayout;
	public LinearLayout backLayout;
	public TextView friend_info;

	private Map<String, String> mDataMap;
	
	@Override
	public View createTabContent(String tag) {
		return view;
	}
	
	/**
	 * 开始第一次加载，需给theListView赋值
	 */
	public abstract void initLoad();

	public void setDataMap(Map<String, String> dataMap) {
		mDataMap = dataMap;
	}

	public Map<String, String> getDataMap() {
		return mDataMap;
	}

	/**
	 * 页面被激活
	 */
	public void onResume(final String tag){
		if(theListView!=null&&tag!="resume") theListView.setTag(tag);
	}
	public void finish(){

	}
	/**
	 * 离开页面时返回当前list的back高度
	 */
	public String onPause(){
		String tag="0";
		if(theListView!=null){
			//判断浮动层是否显示，不显示则记录list滑动高度，显示tag=float
			if(scrollLayout.getScrollY()>0)
				tag=backLayout.getScrollY()+"";
			else if(scrollLayout.getVisibility()==View.VISIBLE)
				tag="float";
		}
		return tag;
	}

	/**
	 * 非自己的个人主页关闭状态数据的过滤。
	 * 此方法只有在浏览他人主页时调用。
	 * 规则：status：
	 * 			1.关闭：仅自己可见，他人不可见。自己可见时显示文案：审核未通过。
	 * 			2.开启：所有人可见。
	 * 			3.删除：所有人不可见。
	 * @param originalDatas 获取到的别人的数据体
	 * @return 如果需要过滤掉，则返回true，否则返回false。
	 */
	public boolean filterOthersData (Map<String, String> originalDatas) {
		if (originalDatas != null && originalDatas.size() > 0) {
			String status = originalDatas.get("status");
			if (!TextUtils.isEmpty(status) && ("1".equals(status) || "3".equals(status)))
				return true;
		}
		return false;
	}
}
