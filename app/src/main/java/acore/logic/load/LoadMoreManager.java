package acore.logic.load;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.xianghatest.R;

import java.util.HashMap;
import java.util.Map;

import acore.tools.Tools;
import acore.tools.ToolsDevice;

public class LoadMoreManager {
	private Context mContext;
	private Map<Object, Button> mLoadMoreMap = new HashMap<Object, Button>();
	private int mHeightLoadmore = 0;
	private float mTextSize = 0;
	
	public LoadMoreManager(Context context){
		this.mContext = context;
		mHeightLoadmore = Tools.getDimen(mContext, R.dimen.dp_100);
		mTextSize = Tools.getDimenSp(mContext, R.dimen.sp_12);
	}

	/**
	 * 创建LoadMoreBtn并且添加绑定关系
	 * @param key
	 * @param clicker
	 * @return
	 */
	public Button newLoadMoreBtn(Object key, OnClickListener clicker) {
		Button loadMoreBtn = new Button(mContext);
		loadMoreBtn.setHeight(mHeightLoadmore);
		loadMoreBtn.setGravity(Gravity.CENTER);
		loadMoreBtn.setText("点击加载更多");
		loadMoreBtn.setTextSize(mTextSize);
		loadMoreBtn.setTextColor(Color.parseColor("#AEAEAE"));
		loadMoreBtn.setBackgroundResource(R.drawable.btn_nocolor);
		int pa = ToolsDevice.dp2px(mContext, 1);
		loadMoreBtn.setShadowLayer(pa, pa, pa,Color.parseColor("#CEFFFFFF"));
		loadMoreBtn.setOnClickListener(clicker);
		//按对应关系存储
		mLoadMoreMap.put(key, loadMoreBtn);
		return loadMoreBtn;
	}
	
	/**
	 * 根据key获取LoadMoreBtn,一个页面若有多个loadMare则需要传入对应的key拿到对应的loadMore
	 * @param key
	 * @return
	 */
	public Button getLoadMoreBtn(Object key){
		Button loadMoreBtn = null;
		if(mLoadMoreMap != null){
			if(key != null){
				loadMoreBtn = mLoadMoreMap.get(key);
			}else if(mLoadMoreMap.size() == 1){
				for (java.util.Map.Entry<Object, Button> entry : mLoadMoreMap.entrySet()) {
					loadMoreBtn = entry.getValue();
					break;
				}
				
			}
		}
		return loadMoreBtn;
	}
}
