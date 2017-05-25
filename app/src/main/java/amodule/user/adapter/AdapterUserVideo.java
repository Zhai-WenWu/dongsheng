package amodule.user.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.adapter.AdapterSimple;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.quan.view.NormalContentView;
import amodule.quan.view.NormarlContentItemImageVideoView;
import amodule.user.view.UserHomeItem;
import amodule.user.view.UserHomeVideoItem;
import xh.windowview.XhDialog;

/**
 * 我的页面：视频
 */
public class AdapterUserVideo extends AdapterSimple {

    private Activity mContext;
    private List<Map<String, String>> mData;

    private UserHomeItem.DeleteCallback mDeleteCallback;

    public AdapterUserVideo(Activity con, View parent, List<Map<String, String>> data, int resource, String[] from, int[] to, UserHomeItem.DeleteCallback callBack) {
        super(parent, data, resource, from, to);
        mContext = con;
        mData = data;
        mDeleteCallback = callBack;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null || convertView.getTag() == null) {
            convertView = new UserHomeVideoItem(parent.getContext());
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
        }

        public void setData(final Map<String, String> map, int position) {
            itemView.setData(map, position);
        }
    }
}
