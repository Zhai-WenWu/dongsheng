package amodule.dish.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;
import acore.widget.rvlistview.RvHorizatolListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.dish.activity.DetailDish;
import aplug.basic.SubBitmapTarget;

/**
 * 模块化；横滑
 */
public class DishSkillView extends ItemBaseView{
    private ImageView img_skill;
    private TextView text1,text2;
    public DishSkillView(Context context) {
        super(context, R.layout.dish_module_skill);
    }

    public DishSkillView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.dish_module_skill);
    }

    public DishSkillView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.dish_module_skill);
    }

    @Override
    public void init() {
        super.init();
        img_skill= (ImageView) findViewById(R.id.img_skill);
        text1= (TextView) findViewById(R.id.text1);
        text2= (TextView) findViewById(R.id.text2);
    }
    public void setData(Map<String,String> data,int position){
        findViewById(R.id.skill_line).setVisibility(position>0?View.VISIBLE:View.GONE);
        findViewById(R.id.skill_vip).setVisibility("2".equals(data.get("iconType"))?VISIBLE:GONE);
        findViewById(R.id.skill_shikan).setVisibility("1".equals(data.get("iconType"))?VISIBLE:GONE);
        setViewImage(img_skill,data,"img");
        if("2".equals(data.get("isVideo")))text1.setText(data.get("videoTime"));
        text2.setText(data.get("text"));
        final String url = data.get("url");
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), DetailDish.tongjiId_detail, "食材小技巧", "食材小技巧点击量");
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),url,false);
            }
        });
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
