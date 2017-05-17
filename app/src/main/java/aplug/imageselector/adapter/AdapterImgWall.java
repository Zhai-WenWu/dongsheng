package aplug.imageselector.adapter;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import aplug.imageselector.ImgWallActivity;

import com.bumptech.glide.Glide;
import com.xiangha.R;

/**
 * Title:AdapterImgWall.java Copyright: Copyright (c) 2014~2017
 * 
 * @author zeyu_t
 * @date 2014年9月16日
 */
public class AdapterImgWall extends PagerAdapter {
	private Context context;
	private ImgWallActivity mAct;
	private ArrayList<String> urls;
	private int mChildCount = 0;

	public AdapterImgWall(ImgWallActivity act, ArrayList<String> urls) {
		this.mAct = act;
		this.context = act;
		this.urls = urls;
	}
	
     @Override public void notifyDataSetChanged() {         
           mChildCount = getCount();
           super.notifyDataSetChanged();
     }
 
     @Override public int getItemPosition(Object object){          
           if ( mChildCount > 0) {
	           mChildCount --;
	           return POSITION_NONE;
           }
           return super.getItemPosition(object);
     }

	@Override public int getCount() {
		return urls != null ? urls.size() : 0;
	}

	@Override public boolean isViewFromObject(View view, Object obj) {
		return view == obj;

	}

	@Override public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	/**
	 * 载入图片进去
	 */
	@Override 
	public Object instantiateItem(ViewGroup container, int position) {
		// 如果访问网络下载图片，此处可以进行异步加载
		final ImageView imageView = new ImageView(context);
		setImgListener(position, imageView);
		String url = urls.get(position);
		if (url != null) {
			if(url.startsWith("http")){
				Glide.with(mAct)
					.load(url)
					.error(R.drawable.default_error)
					.placeholder(R.drawable.mall_recommed_product_backgroup)
					.into(imageView);
			}else {
//				int width = ToolsDevice.getWindowPx(mAct).widthPixels;
//				int height = ToolsDevice.getWindowPx(mAct).heightPixels;
				File imageFile = new File(url);
				// 显示图片
				Glide.with(mAct)
					.load(imageFile)
					.error(R.drawable.default_error)
					.placeholder(R.drawable.mall_recommed_product_backgroup)
					.into(imageView);
			}
		}
		((ViewPager) container).addView(imageView, 0);
		return imageView;
	}

	private void setImgListener(final int position, final ImageView imageView) {
		//设置点击退出
		imageView.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				
			}
		});
		//设置长按保存
//		imageView.setOnLongClickListener(new OnLongClickListener() {
//
//			@Override
//			public boolean onLongClick(View v) {
//				mAct.img_save.setVisibility(View.VISIBLE);
//				return true;
//			}
//		});
	}
}
