package amodule.article.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Map;

import acore.override.helper.XHActivityManager;
import amodule.article.view.RecommendItemView;
import amodule.main.adapter.AdapterListView;
import amodule.main.view.item.HomeAlbumItem;

/**
 * 文章详情页adapter
 */
public class ArticleDetailAdapter extends BaseAdapter {
    public final static int  Type_recommed=1;//推荐类型
    private ArrayList<Map<String,String>> listMap;

    public ArticleDetailAdapter(ArrayList<Map<String,String>> list){
        this.listMap =list;
    }
    @Override
    public int getCount() {
        return listMap.size();
    }

    @Override
    public Map<String, String> getItem(int position) {
        return listMap.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String,String> map = listMap.get(position);
        int dataType=getItemViewType(position);
        switch (dataType){
            case Type_recommed:
                RecommedViewHolder viewHolder=null;
                if (convertView == null
                        || !(convertView.getTag() instanceof RecommedViewHolder)) {
                    viewHolder = new RecommedViewHolder(new RecommendItemView(XHActivityManager.getInstance().getCurrentActivity()));
                    convertView = viewHolder.view;
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (RecommedViewHolder) convertView.getTag();
                }
                viewHolder.setData(map, position);
            break;
        }
        return convertView;
    }
    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        return Integer.parseInt(getItem(position).get("datatype"));
    }
    private class RecommedViewHolder{
        private RecommendItemView view;
        public RecommedViewHolder(RecommendItemView itemView){
            this.view=itemView;
        }
        public void setData(Map<String,String> map,int position){
            if(view!=null) {
                view.setData(map);
            }
        }
    }
}
