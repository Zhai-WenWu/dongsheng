package amodule.article.adapter;

import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.recordervideo.tools.FileToolsCammer;

public class ArticleVideoSelectorAdapter extends RecyclerView.Adapter<ArticleVideoSelectorAdapter.ViewHolder> implements View.OnClickListener {

    private ArrayList<Map<String,String>> mDatas;
    private ViewGroup.LayoutParams mItemLayoutParams;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.articlevideo_seletor_list_item, null, false);
        if (mItemLayoutParams == null) {
            int width = ToolsDevice.getWindowPx(parent.getContext()).widthPixels;
            int columnSpace = Tools.getDimen(parent.getContext(), R.dimen.dp_1_5);
            int columnWidth = (int) ((width - columnSpace * 5) / 4f);
            mItemLayoutParams = new ViewGroup.LayoutParams(columnWidth, columnWidth);
        }
        itemView.setLayoutParams(mItemLayoutParams);
        itemView.setOnClickListener(this);
        ViewHolder holder = new ViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder != null) {
            holder.bindData(mDatas.get(position), position);
            holder.itemView.setTag(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void setData(ArrayList<Map<String,String>> datas) {
        if (datas == null || datas.size() <= 0)
            return;
        mDatas = datas;
        notifyDataSetChanged();
    }

    public Map<String, String> getData(int position) {
        if (mDatas == null || position < 0 || position > (mDatas.size() - 1))
            return null;
        return mDatas.get(position);
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null)
            mOnItemClickListener.onItemClick((Integer) v.getTag());
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    private OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        int position;
        ImageView image;
        TextView timeTag;
        Map<String,String> data;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.img_video);
            timeTag = (TextView) itemView.findViewById(R.id.time_tag);
        }

        public void bindData(Map<String,String> mapData, int pos){
            if(mapData == null) return;
            data = mapData;
            position = pos;
            String path = mapData.get(MediaStore.Video.Media.DATA);
            String videoShowTime = mapData.get(MediaStore.Video.Media.DURATION);
            if (!TextUtils.isEmpty(path)) {
                String imgPath = FileToolsCammer.getVideoThumbnailPath(path, MediaStore.Images.Thumbnails.MINI_KIND);
                if (imgPath != null)
                    Glide.with(image.getContext()).load(new File(imgPath)).centerCrop().into(image);
                else
                    image.setImageResource(0);
                //直接使用 Glide 加载图片
//                    Glide.with(image.getContext()).load(new File(path)).centerCrop().into(image);
            } else
                image.setImageResource(0);
            if (!TextUtils.isEmpty(videoShowTime))
                timeTag.setText(FileToolsCammer.formatVideoTimeByMills(Long.parseLong(videoShowTime)));
            else
                timeTag.setText("");
        }
    }
}
