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

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;
import amodule.dish.activity.DetailDish;
import aplug.basic.SubBitmapTarget;

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
        if(listMaps==null||listMaps.size()<=0)return;
        int size= listMaps.size();
        for( int i = 0; i<size; i++){
            Map<String,String> map= listMaps.get(i);
            View view=LayoutInflater.from(context).inflate(R.layout.dish_module_skill,null);
            view.findViewById(R.id.skill_line).setVisibility(i>0?View.VISIBLE:View.GONE);
            ImageView img_skill= (ImageView) view.findViewById(R.id.img_skill);
            TextView text1= (TextView) view.findViewById(R.id.text1);
            TextView text2= (TextView) view.findViewById(R.id.text2);
            view.findViewById(R.id.skill_vip).setVisibility("2".equals(map.get("isVip"))?VISIBLE:GONE);
            setViewImage(img_skill,map,"img");
            if("2".equals(map.get("isVideo")))text1.setText(map.get("videoTime"));
            text2.setText(map.get("text"));

            final String url = map.get("url");
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), DetailDish.tongjiId_detail, "食材小技巧", "食材小技巧点击量");
                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),url,false);
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
                    v.setImageBitmap(bitmap);
//                    v.setImageBitmap(UtilImage.toRoundCorner(v.getResources(),bitmap,1, Tools.getDimen(context,R.dimen.dp_4)));
                }
            }
        };
    }
}
