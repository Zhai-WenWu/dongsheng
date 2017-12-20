package third.mall.adapter;

import java.util.List;
import java.util.Map;

import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilImage;
import acore.tools.Tools;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import aplug.basic.InternetCallback;

import com.xiangha.R;

public class AdapterRecommed extends MallAdapterSimple{
	private List<? extends Map<String, ?>> data;
	private Context mContext;
	public int roundImgPixels = 0;
	@SuppressWarnings("unchecked")
	public AdapterRecommed(View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.mContext = parent.getContext();
		this.data= data;
		roundImgPixels=(int) parent.getContext().getResources().getDimension(R.dimen.dp_5);
		
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view =  super.getView(position, convertView, parent);
		Map<String,String> map=(Map<String, String>) data.get(position);
		ImageView recommed_iv = (ImageView) view.findViewById(R.id.recommed_iv);
		view.findViewById(R.id.recommed_content).getLayoutParams().width=viewWidth - Tools.getDimen(mContext,  R.dimen.dp_15);
		recommed_iv.getLayoutParams().width = viewWidth - Tools.getDimen(mContext,  R.dimen.dp_15);
		recommed_iv.getLayoutParams().height = viewWidth - Tools.getDimen(mContext,  R.dimen.dp_15);
		TextView recommed_price=(TextView) view.findViewById(R.id.recommed_price);
		recommed_price.setText("¥"+map.get("price"));
		setViewImage(recommed_iv, map.get("img"));
		return view;
	}
	
	public InternetCallback getCallback(final ImageView v) {
		return new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					ImageView img = null;
					if (v.getTag(TAG_ID).equals(url))
						img = v;
					if (img != null && returnObj != null) {
						// 图片圆角和宽高适应
						v.setScaleType(scaleType);
						Bitmap bitmap = UtilImage.toRoundCorner(v.getResources(), (Bitmap) returnObj, roundType, roundImgPixels);
						UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
					}
				}
			}
		};
	}

}
