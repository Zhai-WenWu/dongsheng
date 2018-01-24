package third.share.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;

import third.share.module.ShareModule;

/**
 * Created by sll on 2018/1/24.
 */

public class ShareAdapter extends BaseAdapter {

    private ArrayList<ShareModule> mDatas;

    public void setData(ArrayList<ShareModule> datas) {
        mDatas = datas;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas == null ? null : mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
            View view = parent.inflate(parent.getContext(), R.layout.a_user_home_share_item, null);
            holder = new ViewHolder(parent.getContext(), view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.setData(mDatas.get(position));
        return holder.mItemView;
    }


    private class ViewHolder{
        public View mItemView;
        private Context mContext;
        private ImageView mShareImage;
        private ImageView mIntegralImage;
        private TextView mShareTitle;
        public ViewHolder(Context context, View itemView) {
            mContext = context;
            mItemView = itemView;
            mShareImage = (ImageView) mItemView.findViewById(R.id.share_logo);
            mIntegralImage = (ImageView) mItemView.findViewById(R.id.integral_tip);
            mShareTitle = (TextView) mItemView.findViewById(R.id.share_name);
        }

        public void setData(ShareModule data) {
            if (data == null)
                return;
            mShareImage.setImageResource(data.getResId());
            mIntegralImage.setVisibility(data.isIntegralTipShow() ? View.VISIBLE : View.GONE);
            mShareTitle.setText(data.getTitle());
        }
    }
}
