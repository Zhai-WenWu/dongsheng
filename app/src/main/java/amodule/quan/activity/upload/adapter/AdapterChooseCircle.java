package amodule.quan.activity.upload.adapter;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;

/**
 * Created by xiangha on 2016/8/26.
 */

public class AdapterChooseCircle extends AdapterSimple{

    private List<? extends Map<String, String>> mData;


    public AdapterChooseCircle(View parent, List<? extends Map<String, String>> data, int resource, String[] from, int[] to) {
        super(parent, data, resource, from, to);
        mData = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(R.id.a_post_choose_item_tv);
        Map<String,String> map = mData.get(position);

        String isChoose = map.get("isChoose");
        if("true".equals(isChoose)){
            textView.setTextColor(Color.parseColor("#ff533c"));
            view.findViewById(R.id.a_post_choose_item_img_select).setVisibility(View.VISIBLE);
            view.findViewById(R.id.a_post_choose_item_img_unselect).setVisibility(View.GONE);
        }else{
            textView.setTextColor(Color.parseColor("#333333"));
            view.findViewById(R.id.a_post_choose_item_img_select).setVisibility(View.GONE);
            view.findViewById(R.id.a_post_choose_item_img_unselect).setVisibility(View.VISIBLE);
        }

        return view;
    }
}
