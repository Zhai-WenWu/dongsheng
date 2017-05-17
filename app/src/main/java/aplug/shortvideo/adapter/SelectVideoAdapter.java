package aplug.shortvideo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.shortvideo.view.VideoPreviewView;

/**
 * PackageName : aplug.shortvideo.adapter
 * Created by MrTrying on 2016/9/22 10:33.
 * E_mail : ztanzeyu@gmail.com
 */

public class SelectVideoAdapter extends BaseAdapter {

    private Context context;
    /** 数据集合 */
    private List<Map<String, String>> mData = new ArrayList<>();
    int viewHeight = 0;
    int viewWidth = 0;

    private VideoPreviewView.OnDeleteListener mOnDeleteListener = null;
    private VideoPreviewView.OnReselectListener mOnReselectListener = null;
    private VideoPreviewView.OnUnselectListener mOnUnselectListener = null;
    private VideoPreviewView.OnSelectListener mOnSelectListener = null;

    public SelectVideoAdapter(Context context, List<Map<String, String>> data) {
        super();
        this.context = context;
        this.mData = data;
        viewWidth = (ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_2)) / 3;
        viewHeight = (int) (viewWidth * 192 / 248f);
    }

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String, String> data = mData.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = new VideoPreviewView(context);
            viewHolder = new ViewHolder((VideoPreviewView) convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.setData(position,data);
        //设置view宽高
        ViewGroup.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,viewHeight);
        convertView.setLayoutParams(layoutParams);
        return convertView;
    }

    public class ViewHolder {
        VideoPreviewView previewView;

        public ViewHolder(VideoPreviewView view) {
            previewView = view;
        }

        public void setData(int postition,Map<String, String> data) {
            if (previewView != null && data != null) {
                previewView.setPosition(postition);
                previewView.setData(data);
                if(mOnDeleteListener != null){
                    previewView.setmOnDeleteListener(mOnDeleteListener);
                }
                if (mOnReselectListener != null){
                    previewView.setmOnReselectListener(mOnReselectListener);
                }
                if(mOnSelectListener != null){
                    previewView.setmOnSelectListener(mOnSelectListener);
                }
                if(mOnUnselectListener != null){
                    previewView.setmOnUnselectListener(mOnUnselectListener);
                }
            }
        }
    }

    public VideoPreviewView.OnSelectListener getmOnSelectListener() {
        return mOnSelectListener;
    }

    public void setmOnSelectListener(VideoPreviewView.OnSelectListener mOnSelectListener) {
        this.mOnSelectListener = mOnSelectListener;
    }

    public VideoPreviewView.OnUnselectListener getmOnUnselectListener() {
        return mOnUnselectListener;
    }

    public void setmOnUnselectListener(VideoPreviewView.OnUnselectListener mOnUnselectListener) {
        this.mOnUnselectListener = mOnUnselectListener;
    }

    public VideoPreviewView.OnDeleteListener getmOnDeleteListener() {
        return mOnDeleteListener;
    }

    public void setmOnDeleteListener(VideoPreviewView.OnDeleteListener mOnDeleteListener) {
        this.mOnDeleteListener = mOnDeleteListener;
    }

    public VideoPreviewView.OnReselectListener getmOnReselectListener() {
        return mOnReselectListener;
    }

    public void setmOnReselectListener(VideoPreviewView.OnReselectListener mOnReselectListener) {
        this.mOnReselectListener = mOnReselectListener;
    }
}
