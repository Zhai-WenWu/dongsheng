package amodule.main.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

public class AdapterIndexModule extends AdapterSimple{

	private Context mContext;
	private List<Map<String,String>> mData = null;
	public AdapterIndexModule(View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.mData = (List<Map<String, String>>) data;
		this.mContext = parent.getContext();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Map<String, String> map = mData.get(position);
		// 缓存视图
		ViewCache viewCache = null;
		if (convertView == null) {
			viewCache = new ViewCache();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_circle_new_module_gridview, parent, false);
			viewCache.setView(convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) convertView.getTag();
		}
		viewCache.setValue(map, position);

		return convertView;
	}
	private class ViewCache {
		
		private ImageView imageview;
		private TextView text;
		public void setView(View view){
			imageview= (ImageView) view.findViewById(R.id.imageview);
			text=(TextView) view.findViewById(R.id.textview);
		}
		public void setValue(Map<String, String> map, int position) {
			text.setText(map.get("name"));
			setViewImage(imageview, map.get("img"));
		}
	}
	
	@Override
	public SubBitmapTarget getTarget(final ImageView v, final String url) {
		return new SubBitmapTarget(){
			@Override
			public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
				ImageView img = null;
				if (v.getTag(TAG_ID).equals(url))
					img = v;
				if (img != null && bitmap != null) {
					// 图片圆角和宽高适应 
					v.setScaleType(scaleType);
					
					UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
					AdapterIndexModule.this.notifyDataSetChanged();
					if(isAnimate){
//						AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
//						alphaAnimation.setDuration(300);
//						v.setAnimation(alphaAnimation);
					}
				}
			}};
	}
}
