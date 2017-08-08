/**
 * 根据地址,查看图片.
 * @author intBird 20140213.
 * 
 */
package aplug.imageselector;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.xianghatest.R;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import aplug.basic.LoadImage;

@SuppressLint("NewApi")
public class ShowImageActivity extends BaseActivity {
	private PhotoView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_img_show);
		setCommonStyle();
		init();
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
	
	private void init() {
		imageView = (PhotoView) findViewById(R.id.iv_showChoosedImgs);
		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		loadImage();
	}

	private void loadImage() {
		loadManager.showProgressBar();
		String url = getIntent().getStringExtra("url");
		BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(this)
			.load(url)
			.setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
				@Override
				public boolean onResourceReady(Bitmap arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3, boolean arg4) {
					loadManager.hideProgressBar();
					return false;
				}
				
				@Override
				public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
					loadManager.hideProgressBar();
					Tools.showToast(ShowImageActivity.this, "加载大图失败");
					onBackPressed();
					return false;
				}
			})
			.build();
		if(bitmapRequest != null)
			bitmapRequest.into(imageView);
	}
}
