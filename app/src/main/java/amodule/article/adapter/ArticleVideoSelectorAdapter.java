package amodule.article.adapter;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.recordervideo.tools.FileToolsCammer;

public class ArticleVideoSelectorAdapter extends BaseAdapter {

    private ArrayList<Map<String,String>> mDatas;
    private GridView.LayoutParams mItemLayoutParams;
    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Map<String, String> getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(ArrayList<Map<String,String>> datas) {
        if (datas == null || datas.size() <= 0)
            return;
        mDatas = datas;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.articlevideo_seletor_list_item, null, false);
            if (mItemLayoutParams == null) {
                int width = ToolsDevice.getWindowPx(parent.getContext()).widthPixels;

                int columnSpace = Tools.getDimen(parent.getContext(), R.dimen.dp_3);
                int columnWidth = (int) ((width - columnSpace * 5) / 4f);
                mItemLayoutParams = new GridView.LayoutParams(columnWidth, columnWidth);
            }
            convertView.setLayoutParams(mItemLayoutParams);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();
        if (viewHolder != null) {
            viewHolder.bindData(mDatas.get(position), position);
        }
        return convertView;
    }

    class ViewHolder {
        int position;
        ImageView image;
        TextView timeTag;
        Map<String,String> data;

        ViewHolder(View view){
            image = (ImageView) view.findViewById(R.id.img_video);
            timeTag = (TextView) view.findViewById(R.id.time_tag);
        }

        public void bindData(Map<String,String> mapData, int pos){
            if(mapData == null) return;
            data = mapData;
            position = pos;
            String path = mapData.get(MediaStore.Video.Media.DATA);
            String videoShowTime = mapData.get(MediaStore.Video.Media.DURATION);
            if (!TextUtils.isEmpty(path))
                image.setImageBitmap(FileToolsCammer.getBitmapByImgPath(path));
            if (!TextUtils.isEmpty(videoShowTime))
                timeTag.setText(videoShowTime);
        }
    }
}
