package amodule.article.adapter;

import android.content.Context;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xianghatest.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import aplug.recordervideo.tools.FileToolsCammer;

/**
 * 文件夹Adapter
 */
public class ArticleVideoFolderAdapter extends BaseAdapter implements View.OnClickListener{

    private Context mContext;
    private Map<String, List<Map<String, String>>> mVideos;
    private List<String> mParentPaths = new ArrayList<String>();

    private int mLastSelectedPosition = -1;

    public ArticleVideoFolderAdapter(Context context) {
        mContext = context;
    }

    /**
     * 设置数据集
     *
     * @param maps
     */
    public void setData(Map<String, List<Map<String, String>>> maps) {
        if (maps == null || maps.size() <= 0)
            return;
        mVideos = maps;
        Set<String> keySet = maps.keySet();
        if (keySet != null && !keySet.isEmpty()) {
            Iterator<String> keyIter = keySet.iterator();
            while(keyIter.hasNext()) {
                mParentPaths.add(keyIter.next());
            }
        }
        notifyDataSetChanged();
    }

    public void resetSelected() {
        mLastSelectedPosition = -1;
    }

    public void onItemSelected(int position) {
        if (position == mLastSelectedPosition)
            return;
        mLastSelectedPosition = position;
    }

    @Override
    public int getCount() {
        return mParentPaths.size();
    }

    @Override
    public List<Map<String,String>> getItem(int i) {
        return mVideos.get(mParentPaths.get(i));
    }

    public String getParentPathByPos(int pos) {
        String parentPath = "";
        if (pos >= 0 && mParentPaths != null && pos < mParentPaths.size())
            return mParentPaths.get(pos);
        return parentPath;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.image_seletor_list_item_folder, viewGroup, false);
            convertView.setOnClickListener(this);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.bindData(i);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null)
            mOnItemClickListener.onItemClick(((ViewHolder)v.getTag()).position);
    }

    class ViewHolder {
        int position;
        ImageView cover;
        TextView name;
        TextView size;
        ImageView indicator;
        View itemView;
        ViewHolder(View view){
            itemView = view;
            cover = (ImageView)view.findViewById(R.id.cover);
            name = (TextView) view.findViewById(R.id.name);
            size = (TextView) view.findViewById(R.id.size);
            indicator = (ImageView) view.findViewById(R.id.indicator);
            view.setTag(this);
        }

        void bindData(int position) {
            if (mParentPaths == null || mParentPaths.size() <= 0)
                return;
            this.position = position;
            if (position == 0 && mLastSelectedPosition == -1) {
                mLastSelectedPosition = 0;
            }
            indicator.setVisibility((mLastSelectedPosition != -1 && mLastSelectedPosition == position) ? View.VISIBLE : View.INVISIBLE);
            String parentPath = mParentPaths.get(position);
            String videoPath = null;
            List<Map<String, String>> videos = new ArrayList<Map<String, String>>();
            if (!TextUtils.isEmpty(parentPath)) {
                videos = mVideos.get(parentPath);
                if (videos != null && videos.size() > 0) {
                    Map<String, String> firstVideo = videos.get(0);
                    if (firstVideo != null && firstVideo.containsKey(MediaStore.Video.Media.DATA)) {
                        videoPath = firstVideo.get(MediaStore.Video.Media.DATA);
                    }
                }
            }
            if (!TextUtils.isEmpty(videoPath)) {
                cover.setImageBitmap(FileToolsCammer.getBitmapByImgPath(videoPath));
            } else {
                cover.setImageDrawable(cover.getResources().getDrawable(R.drawable.default_error));
            }
            if (!TextUtils.isEmpty(parentPath)) {
                name.setText(parentPath);
            } else {
                name.setText("");
            }
            if (videos != null && videos.size() > 0) {
                size.setText(videos.size()+"张");
            }
            Glide.with(itemView.getContext())
                    .load(new File(FileToolsCammer.getImgPath(videoPath)))
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .error(R.drawable.default_error)
                    .placeholder(R.drawable.mall_recommed_product_backgroup)
                    .centerCrop()
                    .into(cover);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

}
