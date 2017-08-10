package third.mall.view;

import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.xianghatest.R;

public class MallAdvertItemView extends ViewItemBase{

	private ImageView iv_img;
	
	private Context context;
	private TextView itemText1/*,text_price*/,text_rela_price;
	
	public MallAdvertItemView(Context context) {
		super(context);
		this.context=context;
		initView();
	}

	private void initView() {
		LayoutInflater.from(context).inflate(R.layout.a_mall_advert_item_view, this, true);
		iv_img=(ImageView) findViewById(R.id.iv_img);
		itemText1 = (TextView) findViewById(R.id.itemText1);
//		text_price = (TextView) findViewById(R.id.text_price);
		text_rela_price = (TextView) findViewById(R.id.text_rela_price);
	}

	public void setData(Map<String,String> map){
		setViewImage(iv_img, map.get("img"));
		itemText1.setText(map.get("title"));
//		text_price.setText("￥"+map.get("price"));
		text_rela_price.setText("¥"+map.get("real_price"));
//		text_rela_price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		
	}
}
