package amodule.article.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import aplug.imageselector.bean.Folder;
import aplug.recordervideo.db.RecorderVideoData;
import aplug.recordervideo.tools.FileToolsCammer;

/**
 * 文件夹Adapter
 */
public class ArticleVideoFolderAdapter extends BaseAdapter {

    private Context mContext;
    private Map<String, List<Map<String, String>>> mVideos;
    private List<String> mParentPaths = new ArrayList<String>();

    int imageSize;

    public ArticleVideoFolderAdapter(Context context) {
        mContext = context;
        imageSize = mContext.getResources().getDimensionPixelOffset(R.dimen.dp_72);
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
        if (convertView == null) {
            convertView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.article_video_list_item_folder, viewGroup, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.bindData(i);
        return convertView;
    }

    class ViewHolder {
        ImageView cover;
        TextView name;

        ViewHolder(View view) {
            cover = (ImageView) view.findViewById(R.id.cover);
            name = (TextView) view.findViewById(R.id.name);
            view.setTag(this);
        }

        void bindData(int position) {
            if (mParentPaths == null || mParentPaths.size() <= 0)
                return;
            String parentPath = mParentPaths.get(position);
            String videoPath = null;
            List<Map<String, String>> videos = new ArrayList<Map<String, String>>();
            if (!TextUtils.isEmpty(parentPath)) {
                videos = mVideos.get(parentPath);
                if (videos != null && videos.size() > 0) {
                    Map<String, String> firstVideo = videos.get(0);
                    if (firstVideo != null && firstVideo.containsKey(RecorderVideoData.video_path)) {
                        videoPath = firstVideo.get(RecorderVideoData.video_path);
                    }
                }
            }
            if (!TextUtils.isEmpty(videoPath)) {
                cover.setImageBitmap(FileToolsCammer.getBitmapByImgPath(videoPath));
            } else {
                cover.setImageDrawable(cover.getResources().getDrawable(R.drawable.default_error));
            }
            if (!TextUtils.isEmpty(parentPath) && videos != null && videos.size() > 0) {
                name.setText(parentPath + "(" + videos.size() + ")");
            } else {
                name.setText("");
            }
        }
    }

}
