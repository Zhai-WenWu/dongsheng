package amodule.user.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import amodule.user.view.UserHomeItem;
import amodule.user.view.UserHomeTxtItem;
import amodule.user.view.UserHomeVideoItem;

/**
 * 我的页面：视频
 */
public class AdapterUserVideo extends AdapterSimple {

    private Activity mContext;
    private List<Map<String, String>> mData;

    public AdapterUserVideo(Activity con, View parent, List<Map<String, String>> data, int resource, String[] from, int[] to) {
        super(parent, data, resource, from, to);
        mContext = con;
        mData = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null || convertView.getTag() == null) {
            convertView = new UserHomeVideoItem(mContext);
            holder = new ViewHolder((UserHomeVideoItem) convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.setData(mData.get(position), position);
        return convertView;
    }

    public class ViewHolder {
        UserHomeVideoItem itemView;

        public ViewHolder(UserHomeVideoItem view) {
            this.itemView = view;
            if (itemView != null){
                itemView.setOnItemClickListener((itemView, dataMap) -> {
                    if (mOnItemClickListener != null)
                        mOnItemClickListener.onItemClick(itemView, dataMap);
                });
                itemView.setDeleteClickListener(data -> {
                    if(mOnDeleteClickCallback != null){
                        mOnDeleteClickCallback.onDeleteClick(data);
                    }
                });
            }

        }

        public void setData(final Map<String, String> map, int position) {
            itemView.setData(map, position);
        }
    }

    private UserHomeItem.OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(UserHomeItem.OnItemClickListener clickListener) {
        this.mOnItemClickListener = clickListener;
    }

    private UserHomeTxtItem.OnDeleteClickCallback mOnDeleteClickCallback;
    public void setOnDeleteClickCallback(UserHomeTxtItem.OnDeleteClickCallback onDeleteClickCallback) {
        mOnDeleteClickCallback = onDeleteClickCallback;
    }
}
