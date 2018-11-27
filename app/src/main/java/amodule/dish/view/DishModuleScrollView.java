package amodule.dish.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
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
import acore.logic.LoginManager;
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvHorizatolListView;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.vip.DeviceVipManager;
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
    private String dishCode;
    private String type;

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
        int padding = Tools.getDimen(getContext(),R.dimen.dp_20);
        rvHorizatolListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view);
                if (position == 0) {
                    outRect.left = padding;
                } else if (position == mapList.size() - 1) {
                    outRect.right = padding;
                }
            }
        });
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
        mapList.clear();
        if(listMaps==null||listMaps.size()<=0)return;
        Map<String,String> map= listMaps.get(0);
        ArrayList<Map<String,String>> listTemp = StringManager.getListMapByJson(map.get("list"));
        if(map.containsKey("title")&& !TextUtils.isEmpty(map.get("title"))){
            module_tv.setText(map.get("title"));
            module_tv.setVisibility(View.VISIBLE);
        }else module_tv.setVisibility(View.GONE);
        if(map.containsKey("type"))type=map.get("type");
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
        if(dishGridDialog != null && !TextUtils.isEmpty(dishCode)){
            dishGridDialog.updateParam(dishCode);
        }
        module_more.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(dishCode))return;
                if(dishGridDialog==null){
                    dishGridDialog= new DishGridDialog(context,dishCode,courseCode,chapterCode);
                    dishGridDialog.setOnItemClickCallback(new DishGridDialog.OnItemClickCallback() {
                        @Override
                        public void onItemClick(View view, int position, Map<String, String> stringStringMap) {
                            if(!LoginManager.isLogin()&&!DeviceVipManager.isDeviceVip()&&stringStringMap.containsKey("isShow")&&!"1".equals(stringStringMap.get("isShow"))){
                                String url="xiangha://welcome?VipWebView.app?url=https%3A%2F%2Fappweb.xiangha.com%2Fvip%2Fmyvip%3Fpayset%3D2%26fullScreen%3D2%26vipFrom%3D%E9%A6%99%E5%93%88%E8%AF%BE%E7%A8%8B%E8%AF%A6%E6%83%85%E9%A1%B5%E7%AB%8B%E5%88%BB%E6%8B%A5%E6%9C%89%E7%89%B9%E6%9D%83%E6%8C%89%E9%92%AE";
                                if(XHActivityManager.getInstance().getCurrentActivity() != null)
                                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),url,false);
                                return;
                            }
                            //点击回调
                            if(callBack!=null)callBack.getDataUrl(stringStringMap.get("appUrl"));
//                            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),stringStringMap.get("appurl"),false);
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
            view.setDishModuleClickCallBack(callBack,type);
        }
    }
    public onDishModuleClickCallBack callBack;
    public void setCallBack(onDishModuleClickCallBack callBack){
        this.callBack = callBack;
    }
    public interface onDishModuleClickCallBack{
        public void getDataUrl(String url);
    }
}
