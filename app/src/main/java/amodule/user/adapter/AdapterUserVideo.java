package amodule.user.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import acore.widget.TagTextView;
import amodule.user.view.UserHomeItem;
import amodule.user.view.UserHomeTxtItem;
import amodule.user.view.UserHomeVideoItem;
import amodule.user.view.UserHomeViewRow;

import static amodule.dish.db.UploadDishData.UPLOAD_DRAF;
import static amodule.dish.db.UploadDishData.UPLOAD_FAIL;

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
    public int getCount() {
        return mData.size() / 3 + (mData.size() % 3 > 0 ? 1 : 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = new UserHomeViewRow(mContext);
            holder = new ViewHolder((UserHomeViewRow) convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        List<Map<String, String>> listData = new ArrayList<>();
        int targetLength = (position + 1) * 3;
        int length = targetLength <= mData.size() ? targetLength : mData.size();
        int dataPosition = position * 3;
        for (int i = position * 3; i < length; i++) {
            listData.add(mData.get(i));
        }
        holder.setData(listData, dataPosition);
        return convertView;
    }

    public class ViewHolder {
        UserHomeViewRow itemView;

        public ViewHolder(UserHomeViewRow view) {
            this.itemView = view;
        }

        public void setData(final List<Map<String, String>> list, int position) {
            itemView.setCreateViewCallback(new UserHomeViewRow.CreateViewCallback() {
                @Override
                public View createView() {
                    UserHomeVideoItem view = new UserHomeVideoItem(mContext);
                    view.setOnItemClickListener(mOnItemClickListener);
                    view.setDeleteClickListener(mOnDeleteClickCallback);
                    return view;
                }

                @Override
                public void bindData(View view, Map<String, String> data) {
                    if (view != null && view instanceof UserHomeVideoItem) {
                        ((UserHomeVideoItem) view).setData(data,itemView.getRowPosition() + position);
                    }
                }
            });
            itemView.setData(list,position);
        }
    }

    private UserHomeItem.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(UserHomeItem.OnItemClickListener clickListener) {
        this.mOnItemClickListener = clickListener;
    }

    private UserHomeItem.OnDeleteClickCallback mOnDeleteClickCallback;

    public void setOnDeleteClickCallback(UserHomeTxtItem.OnDeleteClickCallback onDeleteClickCallback) {
        mOnDeleteClickCallback = onDeleteClickCallback;
    }
}
