package amodule.dish.view.UploadDish;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TableLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.List;
import java.util.Map;

import acore.logic.SetDataView;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.activity.upload.UploadDishActivity;

/**
 * 其他选项的单个itemView
 */
public class DishOtherItemView extends RelativeLayout {
	public static final int STYLE_NORMAL = 3;
	public static final int STYLE_CHOSE = 2;
	public static final int STYLE_SPECIAL = 5;
	private TextView mTitle;
	private TextView mInfo;
	private ImageView mIcon;
	private View mLine;
	private PopupWindow mPopupWindow;

	private UploadDishActivity mAct;
	private List<Map<String, String>> mContentData;
	private String mKey = "";
	private String mCode = "";

	public DishOtherItemView(Context context) {
		super(context);
		initView();
	}

	public DishOtherItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public DishOtherItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	/** 初始化 */
	private void initView() {
		LayoutInflater.from(getContext()).inflate(R.layout.a_dish_other_item_layout, this);
		mTitle = (TextView) findViewById(R.id.dish_other_item_text);
		mInfo = (TextView) findViewById(R.id.dish_other_item_info);
		mIcon = (ImageView) findViewById(R.id.dish_other_item_icon);
		mLine = findViewById(R.id.dish_other_item_line);
	}
	
	public void setActivity(BaseActivity act){
		if(act instanceof UploadDishActivity){
			this.mAct = (UploadDishActivity) act;
		}
	}

	public void setTitle(String text) {
		if (mTitle != null && !TextUtils.isEmpty(text)) {
			mTitle.setText(text);
		}
	}

	public void setPopupWindowData(int style, List<Map<String, String>> contentData) {
		this.mContentData = contentData;
		mPopupWindow = new PopupWindow(getContext());
		ViewGroup viewGroup = getContentLayout(style,contentData);
		if (viewGroup == null) {
			return;
		}
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				mIcon.setImageResource(R.drawable.i_view_circle_down);
			}
		});
		mPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
		mPopupWindow.setHeight(Tools.getMeasureHeight(viewGroup));
		mPopupWindow.setContentView(viewGroup);
		// 设置popupWindow弹出窗体的背景
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable(null, ""));
	}

	private ViewGroup getContentLayout(int style , List<Map<String, String>> contentData) {
		TableLayout contentTable = new TableLayout(getContext());
		contentTable.setBackgroundColor(Color.parseColor("#CB000000"));
		contentTable.setLayoutParams(
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		int left = Tools.getDimen(getContext(), R.dimen.dp_30);
		int right = left;
		int top = Tools.getDimen(getContext(), R.dimen.dp_15);
		int bottom = Tools.getDimen(getContext(), R.dimen.dp_15);
		contentTable.setPadding(left, top, right, bottom);
		AdapterSimple adapter = new AdapterSimple(contentTable, contentData, R.layout.a_dish_other_item_popup_item,
				new String[] { "name", "selected" },
				new int[] { R.id.dish_other_item_popup_iten_text, R.id.dish_other_item_popup_iten_text });
		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				switch (view.getId()) {
				case R.id.dish_other_item_popup_iten_text:
					String str = data.toString();
					if (str.startsWith("selected")) {
						boolean selected = Boolean.parseBoolean(str.replace("selected", ""));
						view.setSelected(selected);
						return true;
					}
					break;
				}
				return false;
			}
		});
		setDataView(style,contentTable, adapter);
		return contentTable;
	}

	private synchronized void setDataView(final int span,final TableLayout contentTable, final AdapterSimple adapter) {
		if (contentTable.getChildCount() > 0) {
			contentTable.removeAllViews();
		}
		SetDataView.view(contentTable, span, adapter, null, new SetDataView.ClickFunc[] { new SetDataView.ClickFunc() {
			@Override
			public void click(int index, View view) {
				int length = mContentData.size();
				for (int i = 0; i < length; i++) {
					Map<String, String> map = mContentData.get(i);
					map.put("selected", "selected" + (i == index));
					if(i == index){
						mCode = map.get("code");
						mInfo.setText(map.get("name"));
						mInfo.setVisibility(View.VISIBLE);
						mIcon.setVisibility(View.GONE);
					}
				}
				adapter.notifyDataSetChanged();
				setDataView(span,contentTable, adapter);
				dismissPopupWindow();
			}
		} }, ViewGroup.LayoutParams.MATCH_PARENT, Tools.getDimen(getContext(), R.dimen.dp_33_5));
	}

	public void showPopuWindow() {
//		requestFocus();
		int[] location = new int[2];
		getLocationOnScreen(location);
		int screenHeight = ToolsDevice.getWindowPx(getContext()).heightPixels;
		int offsetY = Tools.getMeasureHeight(mPopupWindow.getContentView()) + location[1] + Tools.getDimen(getContext(), R.dimen.dp_43_5);
		if(offsetY > screenHeight && mAct != null){
			ScrollView scrollView = (ScrollView) mAct.findViewById(R.id.scrollView);
			if(scrollView == null){
				ListView listView = (ListView)mAct.findViewById(R.id.a_dish_upload_new_video_layout_listview);
				listView.scrollBy(0, Tools.getDimen(getContext(),R.dimen.dp_43));
			}else {
				scrollView.scrollBy(0, offsetY - screenHeight);
			}
		}
		mPopupWindow.showAtLocation(this, Gravity.NO_GRAVITY, location[0],
				location[1] + Tools.getDimen(getContext(), R.dimen.dp_43_5));
		mIcon.setImageResource(R.drawable.i_view_circle_up);
	}

	public void dismissPopupWindow() {
		mPopupWindow.dismiss();
	}
	
	public void hasMarginToLine(){
		RelativeLayout.LayoutParams rllp = (LayoutParams) mLine.getLayoutParams();
		int left = Tools.getDimen(getContext(), R.dimen.dp_16);
		rllp.setMargins(left, 0, 0, 0);
		mLine.setLayoutParams(rllp);
	}
	
	public void setKey(String key){
		this.mKey = key;
	}
	
	public String getKey(){
		return mKey;
	}
	
	public void setCode(String code){
		this.mCode = code;
	}
	
	public String getCode(){
		return mCode;
	}
	
	public void setTextInfo(String name){
		mInfo.setText(name);
		mInfo.setVisibility(View.VISIBLE);
		mIcon.setVisibility(View.GONE);
	}

}
