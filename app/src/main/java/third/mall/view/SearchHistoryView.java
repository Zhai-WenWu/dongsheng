package third.mall.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import acore.tools.FileManager;
import third.mall.tool.ToolFile;

/**
 * 搜索历史记录
 * 
 * @author yujian
 *
 */
public class SearchHistoryView extends RelativeLayout {

	private Context context;
	private ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
	private AdapterSimple adapter;
	private ListView listview;
	private HistoryCallBack callback;
	private RelativeLayout search_history_rela;
	
	public void setInterface(HistoryCallBack callback){
		this.callback= callback;
	}

	public SearchHistoryView(Context context) {
		super(context);
	}
	public SearchHistoryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initView();
		initData();
	}

	private void initView() {
		LayoutInflater.from(context).inflate(R.layout.view_search_history, this, true);
		listview = (ListView) findViewById(R.id.listview);
		adapter = new AdapterSimple(listview, list, R.layout.item_search_history_view, new String[] { "content" }, new int[] { R.id.tv_content });
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(callback!=null){
					callback.setdata(list.get(position).get("content"));
				}
			}
		});
		search_history_rela=(RelativeLayout) findViewById(R.id.search_history_rela);
		RelativeLayout rela_history_del=(RelativeLayout) findViewById(R.id.rela_history_del);
		RelativeLayout history_del=(RelativeLayout) findViewById(R.id.history_del);
		setListener(rela_history_del);
		setListener(history_del);
	}

	private void setListener(View view){
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ToolFile.setSharedPreference(context, FileManager.MALL_SEARCH_HISTORY, new String[]{""});
				initData();
				XHClick.mapStat(context, "a_mail_search","清空历史搜索","");
			}
		});
	}
	public void initData() {
		list.clear();
		String[] searchWords=ToolFile.getSharedPreference(context, FileManager.MALL_SEARCH_HISTORY);
		for (int length=searchWords.length; length >0; length--) {
			if(!TextUtils.isEmpty(searchWords[length-1])){
				Map<String,String> map= new HashMap<String, String>();
				map.put("content", searchWords[length-1]);
				list.add(map);
			}
		}
		RelativeLayout.LayoutParams layout;
		if(list.size()>0){
			layout= new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			adapter.notifyDataSetChanged();
			listview.setVisibility(View.VISIBLE);
			findViewById(R.id.rela_history_del).setVisibility(View.VISIBLE);
			findViewById(R.id.line).setVisibility(View.VISIBLE);
			findViewById(R.id.history_non).setVisibility(View.GONE);
		}else{
			layout= new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			listview.setVisibility(View.GONE);
			findViewById(R.id.rela_history_del).setVisibility(View.GONE);
			findViewById(R.id.line).setVisibility(View.GONE);
			findViewById(R.id.history_non).setVisibility(View.VISIBLE);
		}
		search_history_rela.setLayoutParams(layout);
	}
	public interface HistoryCallBack{
		public void setdata(String data);
	}

}
