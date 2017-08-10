package amodule.main.view.circle;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.xianghatest.R;

import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.widget.CusTableLayout;
import amodule.quan.activity.CircleFind;

/**
 * PackageName : amodule.main.view.circle
 * Created by MrTrying on 2016/8/24 18:13.
 * E_mail : ztanzeyu@gmail.com
 */
public class CircleHeaderAllQuan extends RelativeLayout {
	private CusTableLayout headerTableLayout;
	private List<Map<String, String>> circleData;
	private String stiaticID = "";

	public CircleHeaderAllQuan(Context context) {
		this(context,null,0);
	}

	public CircleHeaderAllQuan(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public CircleHeaderAllQuan(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LayoutInflater.from(context).inflate(R.layout.a_circle_header_all_quan , this);
		initView();
	}

	private void initView() {
		//设置全部圈子监听
		findViewById(R.id.circle_all_title_layout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				XHClick.mapStat(getContext(),stiaticID,"圈子","全部");
				getContext().startActivity(new Intent(getContext(), CircleFind.class));
			}
		});
		headerTableLayout = (CusTableLayout) findViewById(R.id.circle_table);
		headerTableLayout.setDashWidth(acore.tools.Tools.getDimen(getContext(),R.dimen.dp_0_5));
		headerTableLayout.setWidthAndHieghtScale(258 / 90f);
		//设置监听
		headerTableLayout.setOnItemClickListenerById(0, new CusTableLayout.OnItemClickListenerById() {
			@Override
			public void onClick(View v, int position) {
				if(mOnItemClickCallback != null){
					mOnItemClickCallback.onClick(v,circleData.get(position));
				}
			}
		});
	}

	public void setCircleData(List<Map<String, String>> data){
		if(data != null && data.size() != 0){
			this.circleData = data;
			//初始化布局
			headerTableLayout.setCustomTable(3, circleData,
					R.layout.a_circle_home_circle_item,
					new String[]{"name"},
					new int[]{R.id.circle_name}, null);
			setVisibility(VISIBLE);
		}else{
			setVisibility(GONE);
		}

	}

	private OnItemClickCallback mOnItemClickCallback;
	public interface OnItemClickCallback{
		public void onClick(View v,Map<String,String> map);
	}
	public void setmOnItemClickCallback(OnItemClickCallback mOnItemClickCallback){
		this.mOnItemClickCallback = mOnItemClickCallback;
	}

	public String getStiaticID() {
		return stiaticID;
	}

	public void setStiaticID(String stiaticID) {
		this.stiaticID = stiaticID;
	}
}
