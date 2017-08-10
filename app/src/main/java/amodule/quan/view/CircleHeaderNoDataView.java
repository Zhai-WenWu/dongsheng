package amodule.quan.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;
/**
 * 无数据显示的header
 * @author Administrator
 *
 */
public class CircleHeaderNoDataView extends RelativeLayout{

	private interfaceNoDataView mInterfaceNoDataView;
	public CircleHeaderNoDataView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.circle_item_no_attention, this,true);
	}
	public void setInterface(interfaceNoDataView mInterfaceNoDataView){
		this.mInterfaceNoDataView= mInterfaceNoDataView;
	}
	public void initView(String content,String des){
		TextView content_tv = (TextView)findViewById(R.id.content_tv);
		TextView des_tv = (TextView)findViewById(R.id.des_tv);
		if(TextUtils.isEmpty(content)&&TextUtils.isEmpty(des))return;
		
		if(TextUtils.isEmpty(content))
			content_tv.setVisibility(View.INVISIBLE);
		else{
			content_tv.setVisibility(View.VISIBLE);
			content_tv.setText(content);
		}
		if(TextUtils.isEmpty(des))
			des_tv.setVisibility(View.INVISIBLE);
		else{
			des_tv.setVisibility(View.VISIBLE);
			des_tv.setText(des+"→");
		}
		//点击回调
		setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(null!=mInterfaceNoDataView)
					mInterfaceNoDataView.ItemClick();
			}
		});
	}
	public interface interfaceNoDataView{
		public void ItemClick();
	}
}
