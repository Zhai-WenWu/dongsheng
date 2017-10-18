//package amodule.main.adapter;
//
//import android.content.Context;
//import android.view.View;
//import android.view.ViewGroup;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import acore.override.adapter.AdapterSimple;
//import amodule.main.view.item.HomeAlbumItem;
//import amodule.main.view.item.HomePostItem;
//import amodule.main.view.item.HomeRecipeItem;
//import amodule.main.view.item.HomeTxtItem;
//
///**
// * home的adapter
// */
//public class AdapterHome extends AdapterSimple{
//    private Context context ;
//    private ArrayList<Map<String,String>> listmap;
//
//    public AdapterHome(View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
//        super(parent, data, resource, from, to);
//        this.context=parent.getContext();
//        listmap= (ArrayList<Map<String, String>>) data;
//    }
//    private ViewClickCallBack viewClickCallBack;
//    public void setViewOnClickCallBack(ViewClickCallBack viewClickCallBack){
//        this.viewClickCallBack= viewClickCallBack;
//    }
//
//    @Override
//    public int getViewTypeCount() {
//        return 5;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        Map<String, String> map = listmap.get(position);
//        String type= map.get("type");
//        return Integer.parseInt(type);
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        Map<String, String> map = listmap.get(position);
//        if(4 == getItemViewType(position)){//专辑
//            ViewAlbumViewHolder viewAlbumViewHolder = null;
//            if (convertView == null
//                    || !(convertView.getTag() instanceof ViewAlbumViewHolder)) {
//                viewAlbumViewHolder = new ViewAlbumViewHolder(new HomeAlbumItem(context));
//                convertView = viewAlbumViewHolder.view;
//                convertView.setTag(viewAlbumViewHolder);
//            } else {
//                viewAlbumViewHolder = (ViewAlbumViewHolder) convertView.getTag();
//            }
//            viewAlbumViewHolder.setData(map, position);
//        }else if(1 == getItemViewType(position)
//                    || 2 == getItemViewType(position)){//图文
//            ViewDishViewHolder viewHolder = null;
//            if (convertView == null
//                    || !(convertView.getTag() instanceof ViewDishViewHolder)) {
//                viewHolder = new ViewDishViewHolder(new HomeRecipeItem(context));
//                convertView = viewHolder.view;
//                convertView.setTag(viewHolder);
//            } else {
//                viewHolder = (ViewDishViewHolder) convertView.getTag();
//            }
//            viewHolder.setData(map, position);
//        }else if(5 == getItemViewType(position)){//美食贴
//            ViewTiziViewHolder viewHolder = null;
//            if (convertView == null
//                    || !(convertView.getTag() instanceof ViewTiziViewHolder)) {
//                viewHolder = new ViewTiziViewHolder(new HomePostItem(context));
//                convertView = viewHolder.view;
//                convertView.setTag(viewHolder);
//            } else {
//                viewHolder = (ViewTiziViewHolder) convertView.getTag();
//            }
//            viewHolder.setData(map, position);
//        }else if(3 == getItemViewType(position)){//文章
//            ViewTxtViewHolder viewHolder = null;
//            if (convertView == null
//                    || !(convertView.getTag() instanceof ViewTxtViewHolder)) {
//                viewHolder = new ViewTxtViewHolder(new HomeTxtItem(context));
//                convertView = viewHolder.view;
//                convertView.setTag(viewHolder);
//            } else {
//                viewHolder = (ViewTxtViewHolder) convertView.getTag();
//            }
//            viewHolder.setData(map, position);
//
//        }
//        return convertView;
//    }
//
//    /**
//     * 专辑
//     */
//    public class ViewAlbumViewHolder{
//        HomeAlbumItem view;
//        public ViewAlbumViewHolder(HomeAlbumItem view){
//            this.view=view;
//        }
//        public void setData(Map<String,String> map, int position){
//            if(view!=null){
//                view.setData(map, position);
//                if(viewClickCallBack!=null)view.setRefreshTag(viewClickCallBack);
//            }
//        }
//    }
//
//    /**
//     * 贴子
//     */
//    public class ViewTiziViewHolder{
//        HomePostItem view;
//        public ViewTiziViewHolder(HomePostItem view){
//            this.view=view;
//        }
//        public void setData(Map<String,String> map, int position){
//            if(view!=null){
//                view.setData(map, position);
//                if(viewClickCallBack!=null)view.setRefreshTag(viewClickCallBack);
//            }
//
//        }
//    }
//
//    /**
//     * 菜谱
//     */
//    public class ViewDishViewHolder{
//        HomeRecipeItem view;
//        public ViewDishViewHolder(HomeRecipeItem view){
//            this.view=view;
//        }
//        public void setData(Map<String,String> map, int position){
//            if(view!=null){
//                view.setData(map, position);
//                if(viewClickCallBack!=null)view.setRefreshTag(viewClickCallBack);
//            }
//        }
//    }
//    /**
//     * 文章
//     */
//    public class ViewTxtViewHolder{
//        HomeTxtItem view;
//        public ViewTxtViewHolder(HomeTxtItem view){
//            this.view=view;
//        }
//        public void setData(Map<String,String> map, int position){
//            if(view!=null){
//                view.setData(map, position);
//                if(viewClickCallBack!=null)view.setRefreshTag(viewClickCallBack);
//            }
//        }
//    }
//
//    /**
//     * 子view被点击回调
//     */
////    public interface ViewClickCallBack{
////        /*** @param isOnClick 是否被点击*/
////        public void viewOnClick(boolean isOnClick);
////    }
//}
