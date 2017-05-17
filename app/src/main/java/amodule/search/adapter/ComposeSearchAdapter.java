package amodule.search.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import amodule.quan.adapter.AdapterCircle;
import amodule.quan.view.NormalContentView;

/**
 * Created by ：airfly on 2016/10/18 20:31.
 */

public class ComposeSearchAdapter extends AdapterSimple {


    private final List<Map<String, String>> mData;
    private final Activity mContext;


    public ComposeSearchAdapter(Activity context, View parent, List<? extends Map<String, ?>> data) {
        super(parent, data, 0, null, null);
        this.mData = (List<Map<String, String>>) data;
        this.mContext = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String, String> map = mData.get(position);
        ComposeSearchAdapter.NormalContentViewHolder normalContentViewHolder;
        if (convertView == null
                || !(convertView.getTag() instanceof AdapterCircle.NormalContentViewHolder)) {
            NormalContentView normalContentView = new NormalContentView(mContext);
            normalContentView.setStiaticKey("a_search_result");
            normalContentViewHolder = new NormalContentViewHolder(normalContentView);
            convertView = normalContentViewHolder.view;
            convertView.setTag(normalContentViewHolder);
        } else {
            normalContentViewHolder = (ComposeSearchAdapter.NormalContentViewHolder) convertView.getTag();
        }
        normalContentViewHolder.setData(map,position);

        return convertView;
    }


    public class NormalContentViewHolder {
        NormalContentView view;

        public NormalContentViewHolder(NormalContentView view) {
            this.view = view;
        }

        public void setData(final Map<String, String> map,int position) {
            if (view != null) {
                view.initView(map, "",position);
            }
        }

    }
}
