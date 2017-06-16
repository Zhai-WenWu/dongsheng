package amodule.main.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Map;

import amodule.main.bean.HomeModuleBean;
import amodule.main.view.item.HomeADItem;
import amodule.main.view.item.HomeAlbumItem;
import amodule.main.view.item.HomePostItem;
import amodule.main.view.item.HomeRecipeItem;
import amodule.main.view.item.HomeTxtItem;
import third.ad.control.AdControlParent;

/**
 * 首页对个类型item
 */
public class AdapterListView extends BaseAdapter{

    public final static String type_tagImage = "1";//大图
    public final static String type_rightImage = "2";//右图
    public final static String type_threeImage = "3";//三图
    public final static String type_noImage = "4";//无图
    public final static String type_levelImage = "5";//蒙版图

    private ArrayList<Map<String,String>> mapArrayList;
    private Context context;
    private Activity mAct;
    private HomeModuleBean moduleBean;

    private AdControlParent mAdControlParent;

    public AdapterListView(View view, Activity mActivity,ArrayList<Map<String,String>> mapArrayList,AdControlParent adControlParent){
        this.context= view.getContext();
        this.mAct=mActivity;
        this.mapArrayList = mapArrayList;
        mAdControlParent = adControlParent;
    }
    public void setHomeModuleBean(HomeModuleBean moduleBean){
        this.moduleBean= moduleBean;
    }
    @Override
    public int getCount() {
        return mapArrayList.size();
    }

    @Override
    public Map<String,String> getItem(int position) {
        return mapArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String, String> map = mapArrayList.get(position);
        String itemType = getItemType(position);
        switch (itemType){
            case type_tagImage://大图
                ViewDishViewHolder viewHolder = null;
                if (convertView == null
                        || !(convertView.getTag() instanceof ViewDishViewHolder)) {
                    viewHolder = new ViewDishViewHolder(new HomeRecipeItem(context));
                    convertView = viewHolder.view;
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewDishViewHolder) convertView.getTag();
                }
                if (type_tagImage.equals(itemType)) {
                    viewHolder.view.setVideoClickCallBack(new HomeRecipeItem.VideoClickCallBack() {
                        @Override
                        public void videoOnClick(int position) {
                            if (mVideoClickCallBack != null) {
                                mVideoClickCallBack.videoOnClick(position);
                            }
                        }
                    });
                }
                viewHolder.setData(map, position);
                break;
            case type_levelImage://蒙版
                ViewAlbumViewHolder viewAlbumViewHolder = null;
                if (convertView == null
                        || !(convertView.getTag() instanceof ViewAlbumViewHolder)) {
                    viewAlbumViewHolder = new ViewAlbumViewHolder(new HomeAlbumItem(context));
                    convertView = viewAlbumViewHolder.view;
                    convertView.setTag(viewAlbumViewHolder);
                } else {
                    viewAlbumViewHolder = (ViewAlbumViewHolder) convertView.getTag();
                }
                viewAlbumViewHolder.setData(map, position);
                break;
            case type_threeImage://美食贴
                ViewTiziViewHolder viewTiziViewHolder = null;
                if (convertView == null
                        || !(convertView.getTag() instanceof ViewTiziViewHolder)) {
                    viewTiziViewHolder = new ViewTiziViewHolder(new HomePostItem(context));
                    convertView = viewTiziViewHolder.view;
                    convertView.setTag(viewTiziViewHolder);
                } else {
                    viewTiziViewHolder = (ViewTiziViewHolder) convertView.getTag();
                }
                viewTiziViewHolder.setData(map, position);
                break;

            case type_rightImage://右图
            case type_noImage://无图
            default://找不到样式类型，指定默认-----无图样式

                ViewTxtViewHolder viewTxtViewHolder = null;
                if (convertView == null
                        || !(convertView.getTag() instanceof ViewTxtViewHolder)) {
                    viewTxtViewHolder = new ViewTxtViewHolder(new HomeTxtItem(context));
                    convertView = viewTxtViewHolder.view;
                    convertView.setTag(viewTxtViewHolder);
                } else {
                    viewTxtViewHolder = (ViewTxtViewHolder) convertView.getTag();
                }
                viewTxtViewHolder.setData(map, position);
                break;
        }
        return convertView;
    }


    @Override
    public int getViewTypeCount() {
        return 9;
    }

    public String getItemType(int position) {
        Map<String, String> item = getItem(position);
        return (item == null || item.size() <= 0 || !item.containsKey("style") || TextUtils.isEmpty(item.get("style"))) ? type_noImage : item.get("style");
    }

    /**
     * 专辑
     */
    public class ViewAlbumViewHolder{
        HomeAlbumItem view;
        public ViewAlbumViewHolder(HomeAlbumItem view){
            this.view=view;
        }
        public void setData(Map<String,String> map, int position){
            if(view!=null){
                view.setHomeModuleBean(moduleBean);
                view.setAdControl(mAdControlParent);
                view.setData(map, position);
                if(viewClickCallBack!=null)view.setRefreshTag(viewClickCallBack);
            }
        }
    }

    /**
     * 贴子
     */
    public class ViewTiziViewHolder{
        HomePostItem view;
        public ViewTiziViewHolder(HomePostItem view){
            this.view=view;
        }
        public void setData(Map<String,String> map, int position){
            if(view!=null){
                view.setHomeModuleBean(moduleBean);
                view.setAdControl(mAdControlParent);
                view.setData(map, position);
                if(viewClickCallBack!=null)view.setRefreshTag(viewClickCallBack);
            }
        }
    }
    /**
     * 菜谱
     */
    public class ViewDishViewHolder{
        HomeRecipeItem view;
        public ViewDishViewHolder(HomeRecipeItem view){
            this.view=view;
        }
        public void setData(Map<String,String> map, int position) {
            if (view != null) {
                view.setHomeModuleBean(moduleBean);
                view.setAdControl(mAdControlParent);
                view.setData(map, position);
                if (viewClickCallBack != null) view.setRefreshTag(viewClickCallBack);
            }
        }
    }
    /**
     * 文章
     */
    public class ViewTxtViewHolder{
        HomeTxtItem view;
        public ViewTxtViewHolder(HomeTxtItem view){
            this.view=view;
        }
        public void setData(Map<String,String> map, int position){
            if(view!=null){
                view.setHomeModuleBean(moduleBean);
                view.setAdControl(mAdControlParent);
                view.setData(map, position);
                if(viewClickCallBack!=null)view.setRefreshTag(viewClickCallBack);
            }
        }
    }
    /**
     * View Ad类型
     */
    public class ViewAdViewHolder{
        HomeADItem view;
        public ViewAdViewHolder(HomeADItem view){
            this.view=view;
        }
        public void setData(Map<String,String> map, int position){
            if(view!=null){
                view.setHomeModuleBean(moduleBean);
                view.setAdControl(mAdControlParent);
                view.setData(map, position);
                if(viewClickCallBack!=null)view.setRefreshTag(viewClickCallBack);
            }
        }
    }

    private AdapterHome.ViewClickCallBack viewClickCallBack;
    public void setViewOnClickCallBack(AdapterHome.ViewClickCallBack viewClickCallBack){
        this.viewClickCallBack= viewClickCallBack;
    }

    private HomeRecipeItem.VideoClickCallBack mVideoClickCallBack;
    public void setVideoClickCallBack (HomeRecipeItem.VideoClickCallBack clickCallBack) {
        mVideoClickCallBack = clickCallBack;
    }
}