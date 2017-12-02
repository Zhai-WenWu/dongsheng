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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import acore.tools.Tools;
import acore.widget.rvlistview.RvHorizatolListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.dish.activity.DetailDish;
import aplug.basic.SubBitmapTarget;

/**
 * 模块化；横滑
 */
public class DishModuleScrollView extends ItemBaseView{
    private TextView module_tv;
    private RvHorizatolListView rvHorizatolListView;
    private AdapterModuleScroll adapterModuleScroll;
    private ArrayList<Map<String,String>> mapList = new ArrayList<>();
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
        rvHorizatolListView= (RvHorizatolListView) findViewById(R.id.rvHorizatolListView);

    }
    public void setData(ArrayList<Map<String,String>> listMaps){
        if(listMaps==null||listMaps.size()<=0)return;
        int size= listMaps.size();
        for( int i = 0; i<size; i++){
            mapList.add(listMaps.get(i));
        }
        if(rvHorizatolListView.getAdapter()==null){
            adapterModuleScroll = new AdapterModuleScroll(context,mapList);
            rvHorizatolListView.setAdapter(adapterModuleScroll);
        }
        adapterModuleScroll.notifyDataSetChanged();
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
    public class AdapterModuleScroll extends RvBaseAdapter<Map<String,String>>{

        public AdapterModuleScroll(Context context, @Nullable List<Map<String, String>> data) {
            super(context, data);
        }

        @Override
        public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.i("xianhaTag","onCreateViewHolder");
            return new ViewHolder(new DishSkillView(context));
        }
        @Override
        public int getItemViewType(int position) {
            return 0;
        }
    }
    public class ViewHolder extends RvBaseViewHolder<Map<String,String>>{
        private DishSkillView view;
        public ViewHolder(@NonNull DishSkillView itemView) {
            super(itemView);
            this.view = itemView;
        }
        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            view.setData(data,position);
        }
    }
}
