package third.mall.view;

import java.util.ArrayList;
import java.util.Map;

import acore.widget.FlowLayout;
import acore.widget.FlowLayout.OnItemClickListenerById;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.xianghatest.R;

public class SearchHotView extends RelativeLayout{

	private Context context;
	private FlowLayout flowLayout;
	private interfaceCallBack callback;
	public SearchHotView(Context context) {
		super(context);
	}
	public SearchHotView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context= context;
		initView();
	}
	private void initView() {
		LayoutInflater.from(context).inflate(R.layout.view_search_hot, this, true);
		this.setVisibility(View.GONE);
		flowLayout=(FlowLayout) findViewById(R.id.flowLayout);
	}
	
	public void setInterface(interfaceCallBack callback){
		this.callback= callback;
	}
	/**
	 * 处理数据
	 * @param list
	 */
	public void setData(final ArrayList<Map<String,String>> list){
		if(list.size()>0){
			this.setVisibility(View.VISIBLE);
				flowLayout.setOnItemClickListenerById(R.id.tv_content, new OnItemClickListenerById(){
					
					@Override
					public void onClick(View v, int position) {
						if(callback!=null){
							callback.getData(list.get(position).get(""));
						}
					}
				} );
				flowLayout.initFlowLayout(list, R.layout.item_mall_search_view, new String[]{""}, new int[]{R.id.tv_content});
		}else{
			this.setVisibility(View.GONE);
		}
	}
	
	public interface interfaceCallBack{
		public void getData(String data);
	}

}
