package amodule.main.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xianghatest.R;

import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import acore.tools.ToolsDevice;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

/**
 * Created by Fang Ruijiao on 2016/12/29.
 */

public class AdapterHomeDish extends AdapterSimple {

    private Activity mAct;
    List<? extends Map<String, ?>> mData;

    public AdapterHomeDish(Activity act,View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(parent, data, resource, from, to);
        mAct = act;
        mData = data;
    }

    @Override
    public void setViewImage(ImageView v, String value) {
        if(v.getId() == R.id.ad_hint){
            if (value.equals("hide") || value.length() == 0)
                v.setVisibility(View.GONE);
            else{
                v.setVisibility(View.VISIBLE);
            }
        }else {
            super.setViewImage(v, value);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        if(convertView != null){
            if(mData.get(position).containsKey("adImg"))
                convertView.findViewById(R.id.item_model_video).setVisibility(View.GONE);
            else
                convertView.findViewById(R.id.item_ad_img).setVisibility(View.GONE);
        }
        return convertView;
    }

    @Override
    public SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                ImageView img = null;
                if (v.getTag(TAG_ID).equals(url))
                    img = v;
                if (img != null && bitmap != null) {
                    if (v.getId() == R.id.item_author_image) {
                        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        v.setImageBitmap(UtilImage.toRoundCorner(v.getResources(),bitmap,1,500));
                    }else if (v.getId() == R.id.item_ad_img) {
                        int imgWidth = ToolsDevice.getWindowPx(mAct).widthPixels, imgHeight = imgWidth * bitmap.getHeight() / bitmap.getWidth();
                        v.setScaleType(ImageView.ScaleType.FIT_XY);
                        imgZoom=true;
                        UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
                    }else {
                        // 图片圆角和宽高适应
                        v.setScaleType(scaleType);
                        UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
                    }
                }
            }
        };
    }
}
