package amodule.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;

/**
 * 社区-圈子adapter
 */

public class AdapterQuan extends AdapterSimple {

    private Context mContext;
    private List<Map<String,String>> mData = null;
    public AdapterQuan(View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(parent, data, resource, from, to);
        this.mData = (List<Map<String, String>>) data;
        this.mContext = parent.getContext();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Map<String, String> map = mData.get(position);
        // 缓存视图
        ViewCache viewCache = null;
        if (convertView == null) {
            viewCache = new ViewCache();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.view_main_circle_item, parent, false);
            viewCache.setView(convertView);
            convertView.setTag(viewCache);
        } else {
            viewCache = (ViewCache) convertView.getTag();
        }
        viewCache.setValue(map, position);

        return convertView;
    }
    private class ViewCache {

        private TextView view_my_circle_item_title;
        private TextView view_my_circle_item_num;
        private ImageView view_quan_circlefind;
        public void setView(View view){
            view_my_circle_item_title=(TextView) view.findViewById(R.id.view_my_circle_item_title);
            view_my_circle_item_num=(TextView) view.findViewById(R.id.view_my_circle_item_num);
            view_quan_circlefind= (ImageView) view.findViewById(R.id.view_quan_circlefind);

        }
        public void setValue(Map<String, String> map, int position) {
            if(map.containsKey("isCircleFind")){
                view_my_circle_item_title.setVisibility(View.GONE);
                view_my_circle_item_num.setVisibility(View.GONE);
                view_quan_circlefind.setVisibility(View.VISIBLE);
            }else{
                view_my_circle_item_title.setVisibility(View.VISIBLE);
                view_my_circle_item_num.setVisibility(View.VISIBLE);
                view_quan_circlefind.setVisibility(View.GONE);
                view_my_circle_item_title.setText(map.get("name"));
                view_my_circle_item_num.setText(map.get("dayHotNum"));

            }
        }
    }
}
