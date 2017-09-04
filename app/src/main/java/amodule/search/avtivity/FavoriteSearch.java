package amodule.search.avtivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.search.view.SearchFavoriteDish;
import amodule.search.view.SearchFavoriteNouse;
import amodule.search.view.SearchFavoriteSubject;

/**
 * 搜索我的收藏页面
 * @author FangRuijiao
 */
public class FavoriteSearch extends BaseActivity implements OnClickListener{

	private String[] name = {"菜谱","美食贴","头条"};
	private int[] resultId = {R.drawable.z_search_icon_dish,R.drawable.z_search_icon_subject,R.drawable.z_search_icon_nouse};
	private LinearLayout ll_search,ll_result;
	private EditText ed_search_main;
	private TextView tv_soContent;

	private SearchFavoriteNouse searNouse;
	private SearchFavoriteDish searDish;
	private SearchFavoriteSubject searSubject;

	private int id = 0;
	private int searchTag = 0;

	private boolean isOneSearch = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		initActivity("",2,0,0,R.layout.a_favorite_search);
		init();
//		initTitle();
	}
	/**
	 * 初始化区分数据模块
	 */
	private void initTitle() {
		if(Tools.isShowTitle()) {
			int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
			int height = dp_45 + Tools.getStatusBarHeight(this);

			RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.all_title_rela);
			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
			bar_title.setLayoutParams(layout);
			bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
		}
	}

	private void init(){
		ll_search = (LinearLayout)findViewById(R.id.ll_search);
		ll_result = (LinearLayout)findViewById(R.id.ll_result);
		ed_search_main = (EditText)findViewById(R.id.ed_search_main);
		tv_soContent = (TextView)findViewById(R.id.tv_soContent);

		searNouse = new SearchFavoriteNouse(this);
		searDish = new SearchFavoriteDish(this);
		searSubject = new SearchFavoriteSubject(this);

		findViewById(R.id.btn_search_main).setOnClickListener(this);
		findViewById(R.id.btn_ed_clear_main).setOnClickListener(this);
		findViewById(R.id.btn_back).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		ed_search_main.requestFocus();
		ed_search_main.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void afterTextChanged(Editable s) {
				itemInfalter(ed_search_main.getText().toString());
				if (ed_search_main.getText().toString().length() > 0) {
					findViewById(R.id.btn_ed_clear_main).setVisibility(View.VISIBLE);
				} else {
					findViewById(R.id.btn_ed_clear_main).setVisibility(View.GONE);
				}
			}
		});
		ed_search_main.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_ENTER:
				case KeyEvent.ACTION_DOWN:
					String str = ed_search_main.getText().toString();
					if (str.trim().length() == 0) {
						Tools.showToast(FavoriteSearch.this, "请输入查询关键字");
						return true;
					}
					searchClick();
					return true;
				default:
					return false;
				}
			}
		});

		loadManager.setFailClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				initData(searchTag);
			}
		});
		loadManager.hideProgressBar();
	}

	private void itemInfalter(String searchContent) {
		if(ll_search.getChildCount() == 0){
			for (int i = 0; i < name.length; i++) {
				LayoutInflater inflater = LayoutInflater.from(this);
				View itemView = inflater.inflate(R.layout.a_favorite_search_choose_item, null);
				TextView content = (TextView)itemView.findViewById(R.id.tv_content);
				TextView tv_hint = (TextView)itemView.findViewById(R.id.tv_hint);
				ImageView iv_hint = (ImageView)itemView.findViewById(R.id.iv_hint);
				if(i == 1){
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ToolsDevice.dp2px(this, (float)15.5), ToolsDevice.dp2px(this, (float)13.5));
					params.setMargins(0, ToolsDevice.dp2px(this, (float)1.5), 0, 0);
					iv_hint.setLayoutParams(params);
				}
				if(i == 2){
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ToolsDevice.dp2px(this, 16), ToolsDevice.dp2px(this, 16));
//					params.setMargins(0, ToolsDevice.dp2px(this, 2), 0, 0);
					iv_hint.setLayoutParams(params);
				}

				content.setText(searchContent);
				tv_hint.setText("在" + name[i] + "中搜索");
				iv_hint.setBackgroundResource(resultId[i]);
				itemView.setClickable(true);

				itemView.setTag("" + id++);
				itemView.setOnClickListener(this);
				ll_search.addView(itemView);
			}
		}else{
			for (int i = 0; i < ll_search.getChildCount(); i++) {
				View v = ll_search.getChildAt(i);
				TextView itemView = (TextView)v.findViewById(R.id.tv_content);
				itemView.setText(searchContent);
			}
		}
	}

	@Override
	public void onClick(View v) {
		ToolsDevice.keyboardControl(false, this, ed_search_main);
		switch(v.getId()){
		case R.id.ll_content:
			int tag = Integer.parseInt(v.getTag().toString());
			onSearch(tag);
			break;
		case R.id.btn_search_main:
			searchClick();
			break;
			case R.id.btn_ed_clear_main :
				ed_search_main.setText("");
				break;
		}
	}

	private void searchClick() {
		if(isOneSearch){
			isOneSearch = false;
			onSearch(searchTag);
		}else{
			doubleSearch(searchTag);
		}
	}

	private void onSearch(int tag){
		findViewById(R.id.ll_search).setVisibility(View.GONE);
		ll_result.setVisibility(View.VISIBLE);
		searchTag = tag;
		tv_soContent.setText("来自: " + name[tag] + "收藏");
		initData(tag);
	}

	private void initData(int tag){
		isOneSearch = false;
		switch(tag){
		case 0:
			tv_soContent.setVisibility(View.VISIBLE);
			searDish.searData(ed_search_main.getText().toString());
			View viewDish = searDish.getListView();
			ll_result.removeView(viewDish);
			ll_result.addView(viewDish);
			break;
		case 1:
			tv_soContent.setVisibility(View.VISIBLE);
			searSubject.searData(ed_search_main.getText().toString());
			View view = searSubject.getListView();
			ll_result.removeView(view);
			ll_result.addView(view);
			break;
		case 2:
			tv_soContent.setVisibility(View.VISIBLE);
			searNouse.searData(ed_search_main.getText().toString());
			View viewNouse = searNouse.getListView();
			ll_result.removeView(viewNouse);
			ll_result.addView(viewNouse);
			break;
		}
	}

	/**
	 * 在原来的搜索结果上再次点击搜索
	 * @param tag
	 */
	private void doubleSearch(int tag){
		switch(tag){
		case 0:
			searDish.newSearch(ed_search_main.getText().toString());
			break;
		case 1:
			searSubject.newSearch(ed_search_main.getText().toString());
			break;
		case 2:
			searNouse.newSearch(ed_search_main.getText().toString());
			break;
		}
	}

}
