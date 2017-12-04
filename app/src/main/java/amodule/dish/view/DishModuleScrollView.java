package amodule.dish.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
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
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvHorizatolListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import aplug.basic.SubBitmapTarget;

/**
 * 模块化；横滑
 */
public class DishModuleScrollView extends ItemBaseView{
    private TextView module_tv,module_more;
    private RvHorizatolListView rvHorizatolListView;
    private AdapterModuleScroll adapterModuleScroll;
    private ArrayList<Map<String,String>> mapList = new ArrayList<>();
    private DishGridDialog dishGridDialog;
    private String dishCode,courseCode,chapterCode;

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
        module_more= (TextView) findViewById(R.id.module_more);

    }

    /**
     * 设置数据：
     * @param listMaps 展示数据体
     * @param code 菜谱code
     * @param courseCode 课程code
     * @param chapterCode 章节code
     */
    public void setData(ArrayList<Map<String,String>> listMaps,String code,String courseCode,String chapterCode){
        this.dishCode = code;
        this.courseCode = courseCode;
        this.chapterCode = chapterCode;
        if(listMaps==null||listMaps.size()<=0)return;
        Map<String,String> map= listMaps.get(0);
        ArrayList<Map<String,String>> listTemp = StringManager.getListMapByJson(map.get("list"));
        if(map.containsKey("title")&& !TextUtils.isEmpty(map.get("title"))){
            module_tv.setText(map.get("title"));
            module_tv.setVisibility(View.VISIBLE);
        }else module_tv.setVisibility(View.GONE);
        module_more.setVisibility(map.containsKey("type")&&"2".equals(map.get("type"))?View.VISIBLE:View.GONE);
        int size= listTemp.size();
        for( int i = 0; i<size; i++){
            mapList.add(listTemp.get(i));
        }
        if(rvHorizatolListView.getAdapter()==null){
            adapterModuleScroll = new AdapterModuleScroll(context,mapList);
            rvHorizatolListView.setAdapter(adapterModuleScroll);
        }
        adapterModuleScroll.notifyDataSetChanged();
        module_more.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(dishCode))return;
                if(dishGridDialog==null){
                    dishGridDialog= new DishGridDialog(context,dishCode,courseCode,chapterCode);
                    dishGridDialog.setOnItemClickCallback(new DishGridDialog.OnItemClickCallback() {
                        @Override
                        public void onItemClick(View view, int position, Map<String, String> stringStringMap) {
                            //点击回调
                            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),stringStringMap.get("appurl"),false);
                            dishGridDialog.dismiss();
                        }
                    });
                }
                dishGridDialog.show();
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
    public class AdapterModuleScroll extends RvBaseAdapter<Map<String,String>>{

        public AdapterModuleScroll(Context context, @Nullable List<Map<String, String>> data) {
            super(context, data);
        }

        @Override
        public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
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
