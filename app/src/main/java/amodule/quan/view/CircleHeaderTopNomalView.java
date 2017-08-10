package amodule.quan.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

/**
 * 圈标准头布局
 * @author Administrator
 *
 */
public class CircleHeaderTopNomalView extends RelativeLayout{

	private interfaceTopNomalView mInterfaceTopNomalView;
	public CircleHeaderTopNomalView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.circle_header_item_other, this,true);
	}

	public void setInterface(interfaceTopNomalView mInterfaceTopNomalView){
			this.mInterfaceTopNomalView= mInterfaceTopNomalView;
	}
	public void initView(String content, String backgroupColor, String textColor, int drawable, boolean isShowDelete){
		RelativeLayout quan_other = (RelativeLayout)findViewById(R.id.quan_other);
		if (TextUtils.isEmpty(backgroupColor))
			backgroupColor = "#999999";
		if (TextUtils.isEmpty(textColor))
			textColor = "#333333";
		quan_other.setBackgroundColor(Color.parseColor(backgroupColor));
		TextView tv_content = (TextView)findViewById(R.id.tv_content);
		ImageView img_content = (ImageView)findViewById(R.id.img_content);
		ImageView img_delete = (ImageView)findViewById(R.id.img_delete);
		tv_content.setText(content);
		tv_content.setTextColor(Color.parseColor(textColor));
		setTag(content);
		setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mInterfaceTopNomalView!=null)
					mInterfaceTopNomalView.setItemClick();
			}
		});
		// 是否显示图片
		if (drawable > 0) {
			img_content.setVisibility(View.VISIBLE);
			img_content.setImageResource(drawable);
		} else
			img_content.setVisibility(View.GONE);

		// 是否显示删除控件
		if (isShowDelete) {
			img_delete.setVisibility(View.VISIBLE);
			img_delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(mInterfaceTopNomalView!=null){
						mInterfaceTopNomalView.setClickDel(CircleHeaderTopNomalView.this);
					}
				}
			});
		} else
			img_delete.setVisibility(View.GONE);
	}
	public interface interfaceTopNomalView{
		public void setClickDel(View view);
		public void setItemClick();
	}
}
