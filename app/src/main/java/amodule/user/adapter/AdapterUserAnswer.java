package amodule.user.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import amodule.user.view.UserHomeAnswerItem;
import amodule.user.view.UserHomeItem;

/**
 * 我的页面：问答
 */
public class AdapterUserAnswer extends AdapterSimple {

    private Activity mContext;
    private List<Map<String, String>> mData;

    public AdapterUserAnswer(Activity con, View parent, List<Map<String, String>> data, int resource, String[] from, int[] to) {
        super(parent, data, resource, from, to);
        mContext = con;
        mData = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null || convertView.getTag() == null) {
            convertView = new UserHomeAnswerItem(mContext);
            holder = new ViewHolder((UserHomeAnswerItem) convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.setData(mData.get(position), position);
        return convertView;
    }

    public class ViewHolder {
        UserHomeAnswerItem itemView;

        public ViewHolder(UserHomeAnswerItem view) {
            this.itemView = view;
            if (itemView != null)
                itemView.setOnItemClickListener(new UserHomeItem.OnItemClickListener() {
                    @Override
                    public void onItemClick(Map<String, String> dataMap) {
                        if (mOnItemClickListener != null)
                            mOnItemClickListener.onItemClick(dataMap);
                    }
                });
        }

        public void setData(final Map<String, String> map, int position) {
            itemView.setData(map, position);
        }
    }
    private UserHomeItem.OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(UserHomeItem.OnItemClickListener clickListener) {
        this.mOnItemClickListener = clickListener;
    }

}
