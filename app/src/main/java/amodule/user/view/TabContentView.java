package amodule.user.view;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

import acore.widget.DownRefreshList;
import acore.widget.LayoutScroll;

public abstract class TabContentView implements TabContentFactory{

	public View view;
	public DownRefreshList theListView=null;
	public LayoutScroll scrollLayout;
	public LinearLayout backLayout;
	public TextView friend_info;
	
	@Override
	public View createTabContent(String tag) {
		return view;
	}
	
	/**
	 * 开始第一次加载，需给theListView赋值
	 */
	public abstract void initLoad();
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
}
