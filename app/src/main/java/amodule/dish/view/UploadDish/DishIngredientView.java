package amodule.dish.view.UploadDish;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.widget.EditTextNoEmoji;
import amodule.dish.db.UploadDishData;
import xh.basic.tool.UtilString;

/**
 * 食材和辅料控制
 * @author FangRuijiao
 * DishIngredientView
 * @TODO 
 * @data 2015年10月14日下午4:36:25 
 */
@SuppressLint("InflateParams")
public class DishIngredientView{

	private BaseActivity mAct;
	//因为需要 startActivityForResult 必须是Activity
	private LayoutInflater inflater;
	
	private LinearLayout linearFood,linearFuliao;

	private boolean isVideoIngredient = false;

	/**
	 * 食材和辅料控制构造
	 */
	public DishIngredientView(BaseActivity act, View ingredientView, UploadDishData dishData,boolean isVideoIngredient) {
		super();
		mAct = act;
		this.isVideoIngredient = isVideoIngredient;
		inflater = LayoutInflater.from(act);
		linearFood = (LinearLayout) ingredientView.findViewById(R.id.linear_food);
		linearFuliao = (LinearLayout) ingredientView.findViewById(R.id.linear_fuliao);
		ingredientView.findViewById(R.id.relative_addFood).setOnClickListener(onAddClick);
		ingredientView.findViewById(R.id.relative_addFuliao).setOnClickListener(onAddClick);
		
		initFoodOrFuliao(dishData.getFood(), linearFood,true,1);
		initFoodOrFuliao(dishData.getBurden(), linearFuliao,false,1);
	}

	/**
	 * 获取食材数据
	 * @return
	 */
	public String getFoodData(){
		return getCount(linearFood, true);
	}
	/**
	 * 获取辅料数据
	 * @return
	 */
	public String getIngredientData(){
		return getCount(linearFuliao, false);
	}
	
	/**
	 * 添加一条辅料或食材
	 * @param count
	 * @param linear
	 */
	private void initFoodOrFuliao(String count, LinearLayout linear,boolean isFood,int modifyNum) {
		linear.removeAllViews();
		ArrayList<Map<String, String>> list = UtilString.getListMapByJson(count);
		if (list.size() > 0) {
			linear.setVisibility(View.VISIBLE);
			for (int i = 0; i < list.size(); i++) {
				String hintName = "如:油";
				String hintNumber = "如:适量";
				if(isFood){
					hintName = "如:土豆";
					hintNumber = "如:1个";
				}
				View itemView = addFood(linear, hintName, hintNumber);
				Map<String, String> map = list.get(i);
				EditText name = (EditText) itemView.findViewById(R.id.et_foodName);
				EditText number = (EditText) itemView.findViewById(R.id.et_foodNumber);
				name.setText(map.get("name"));
				number.setText(map.get("number"));
			}
		}else{
			String hintName = "如:油";
			String hintNumber = "如:适量";
			if(isFood){
				hintName = "如:土豆";
				hintNumber = "如:1个";
			}
			for(int i = 0; i < modifyNum; i++){
				addFood(linear, hintName, hintNumber);
			}
		}
	}
	
	/**
	 * 编辑辅料或食材时需要先获取当前条目数据
	 * @param linear 辅料或食材条目的父类容器
	 * @param isFood 为了区分是辅料还是食材
	 * @return 条目集合的为json格式
	 */
	private String getCount(LinearLayout linear, boolean isFood) {
		if (linear.getChildCount() == 0)
			return "";
		JSONArray array = new JSONArray();
		for (int i = 0; i < linear.getChildCount(); i++) {
			View view = linear.getChildAt(i);
			EditText name = (EditText) view.findViewById(R.id.et_foodName);
			EditText number = (EditText) view.findViewById(R.id.et_foodNumber);
			String foodName = StringManager.getUploadString(name.getText().toString());
			if (foodName != null && foodName.length() > 0) {
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("name", foodName);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				String nu = StringManager.getUploadString(number.getText().toString());
				if (TextUtils.isEmpty(nu)){
					nu = "";
				}
				try {
					jsonObject.put("number", nu);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				array.put(jsonObject);
			}
		}
		return array.toString();
	}
	
	/**
	 * 添加辅料,食材的点击事件
	 */
	private OnClickListener onAddClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(isVideoIngredient){
				if(linearFuliao.getChildCount() + linearFood.getChildCount() >= 30) {
					Toast.makeText(mAct,"食材最多30个",Toast.LENGTH_SHORT).show();
					return;
				}
			}
			switch (v.getId()) {
			case R.id.relative_addFuliao:
				addFood(linearFuliao, "如:油", "如:适量");
				break;
			case R.id.relative_addFood:
				addFood(linearFood, "如:土豆", "如:1个");
				break;
			}
		}
	};
	
	public View addFood(LinearLayout parentLayout,String hintName,String hintNumber){
		View itemView = inflater.inflate(R.layout.a_dish_upload_food_item_et, null);
		final EditTextNoEmoji name = (EditTextNoEmoji)itemView.findViewById(R.id.et_foodName);
		name.setHint(hintName);
		final EditText number = (EditText)itemView.findViewById(R.id.et_foodNumber);
		number.setHint(hintNumber);
		if(isVideoIngredient){
//			name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
			setMaxNum(name,10,"食材最长10个字");
			setMaxNum(number,5,"食材用量最长5个字");
//			number.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
		}
		parentLayout.addView(itemView);
		return itemView;
	}

	private void setMaxNum(final EditText et, final int maxNum, final String hint){
		et.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if(s.length() > maxNum){
					Toast.makeText(mAct,hint,Toast.LENGTH_SHORT).show();
					et.setText(s.subSequence(0,maxNum));
					try {
						et.setSelection(maxNum);
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			}
		});
	}
}
