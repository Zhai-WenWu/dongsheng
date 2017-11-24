package amodule.dish.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;
import java.util.ArrayList;
import java.util.Map;
import acore.override.view.ItemBaseView;
import acore.tools.Tools;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

/**
 * 模块化；横滑
 */
public class DishModuleScrollView extends ItemBaseView{
    private TextView module_tv;
    private HorizontalScrollView horizontal_scroll;
    private LinearLayout horizontal_scroll_linear;
    public DishModuleScrollView(Context context) {
        super(context, R.layout.dish_module_scroll_view);
    }

    public DishModuleScrollView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.dish_module_scroll_view);
    }

    public DishModuleScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.dish_module_scroll_view);
    }

    @Override
    public void init() {
        super.init();
        module_tv= (TextView) findViewById(R.id.module_tv);
        horizontal_scroll= (HorizontalScrollView) findViewById(R.id.horizontal_scroll);
        horizontal_scroll_linear= (LinearLayout) findViewById(R.id.horizontal_scroll_linear);
    }
    public void setData(ArrayList<Map<String,String>> listMaps){
//        if(listMaps==null||listMaps.size()<=0)return;
//        int size= listMaps.size();
        for(int i=0;i<10;i++){
            View view=LayoutInflater.from(context).inflate(R.layout.dish_module_skill,null);
            view.findViewById(R.id.skill_line).setVisibility(i>0?View.VISIBLE:View.GONE);
            ImageView img_skill= (ImageView) view.findViewById(R.id.img_skill);
            TextView text1= (TextView) view.findViewById(R.id.text1);
            TextView text2= (TextView) view.findViewById(R.id.text2);
//            setViewImage(img_skill,listMaps.get(i),"img");
            setViewImage(img_skill,"http://s1.cdn.xiangha.com/caipu/201705/1516/181531209303.jpg/OTAweDYwMA");
//            text1.setText(listMaps.get(i).get("text"));
            text1.setText("测试数据");
            text2.setText("测试数据");


            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            horizontal_scroll_linear.addView(view);
        }
        horizontal_scroll.setVisibility(VISIBLE);
    }

    public SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                ImageView img = null;
                if (v.getTag(TAG_ID).equals(url))
                    img = v;
                if (img != null && bitmap != null) {
                    // 图片圆角和宽高适应auther_userImg
                    v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    v.setImageBitmap(UtilImage.toRoundCorner(v.getResources(),bitmap,1, Tools.getDimen(context,R.dimen.dp_20)));
                }
            }
        };
    }
}
