package third.mall.view;

import third.mall.tool.ToolFile;
import acore.tools.FileManager;
import acore.tools.Tools;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.xiangha.R;

;

/**
 * 搜索头布局
 * 
 * @author yujian
 *
 */
public class MallSearchTitle extends RelativeLayout implements OnClickListener {
	private InterfaceCallBack callback;
	public EditText ed_search_mall;
	private Context context;

	public MallSearchTitle(Context context) {
		super(context);
		this.context = context;
		initView();
	}

	public MallSearchTitle(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initView();
	}

	public MallSearchTitle(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 设置接口回调
	 * 
	 * @param callback
	 */
	public void setInterface(InterfaceCallBack callbacks) {
		if (null == callbacks) {
			callback = new InterfaceCallBack() {
				@Override
				public void setSearch(String content) {
				}

				@Override
				public void delContent() {
				}

				@Override
				public void back() {
				}
			};
		} else
			this.callback = callbacks;
	}

	private void initView() {
		LayoutInflater.from(context).inflate(R.layout.c_mall_view_search_title, this, true);
		setInterface(null);
		findViewById(R.id.btn_ed_clear_mall).setVisibility(View.GONE);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_search_mall).setOnClickListener(this);
		findViewById(R.id.btn_ed_clear_mall).setOnClickListener(this);
		ed_search_mall = (EditText) findViewById(R.id.ed_search_mall);
		ed_search_mall.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (ed_search_mall.getText().toString().length() > 0) {
					findViewById(R.id.btn_ed_clear_mall).setVisibility(View.VISIBLE);
				} else {
					findViewById(R.id.btn_ed_clear_mall).setVisibility(View.GONE);
					callback.delContent();
				}
			}
		});
		// 监听搜索
		ed_search_mall.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					checkContent(ed_search_mall.getText().toString());
					return true;
				}
				return false;
			}
		});
		initTitle();
	}

	private void initTitle() {
		if(Tools.isShowTitle()) {
			int dp_45 = Tools.getDimen(context, R.dimen.dp_45);
			int height = dp_45 + Tools.getStatusBarHeight(context);

			RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.title_all_rela);
			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
			bar_title.setLayoutParams(layout);
			bar_title.setPadding(0, Tools.getStatusBarHeight(context), 0, 0);
		}
	}

	public interface InterfaceCallBack {
		public void setSearch(String content);

		public void back();

		public void delContent();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			callback.back();
			break;
		case R.id.btn_search_mall:
			checkContent(ed_search_mall.getText().toString());
			break;
		case R.id.btn_ed_clear_mall:
			ed_search_mall.setText("");
			callback.delContent();
			break;
		}
	}

	/**
	 * 检查内容数据
	 * 
	 * @param string
	 */
	private void checkContent(String string) {
		if (!TextUtils.isEmpty(string)) {
			callback.setSearch(string);
			ToolFile.setSharedPreference(context, FileManager.MALL_SEARCH_HISTORY, string);
		} else {
			Tools.showToast(context, "请输入搜索词");
		}
	}

	public void setEditTextFocus(boolean isInput) {
		// 手动弹出键盘
		InputMethodManager inputManager = (InputMethodManager) ed_search_mall.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (isInput) {
			ed_search_mall.setFocusable(true);
			ed_search_mall.setFocusableInTouchMode(true);
			ed_search_mall.requestFocus();
			inputManager.showSoftInput(ed_search_mall, 0);
		}else{
			ed_search_mall.clearFocus();
			inputManager.hideSoftInputFromWindow(ed_search_mall.getWindowToken(), 0);
		}
	}
}
