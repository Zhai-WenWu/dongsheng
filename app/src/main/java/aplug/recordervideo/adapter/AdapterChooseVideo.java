package aplug.recordervideo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.recordervideo.db.RecorderVideoData;
import aplug.recordervideo.view.RecorderVideoPreviewView;

/**
 * Created by XiangHa on 2016/10/14.
 */
public class AdapterChooseVideo extends RecyclerView.Adapter<AdapterChooseVideo.ViewHolder>{

    private Context context;
    private ArrayList<Map<String,String>> arrayList;

    private OnChooseAdaperListener chooseAdaperListener;
    private OnSelectAdaperListener mOnSelectListener;
    private OnDeleteListener mOnDeleteListener;

    private boolean mIsShowSelectHint;

    public AdapterChooseVideo(Context con,ArrayList<Map<String,String>> array,boolean isShowSelectHint){
        context = con;
        arrayList = array;
        mIsShowSelectHint = isShowSelectHint;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(View.inflate(context, R.layout.a_recorder_video_choose_item, null));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final String time = arrayList.get(position).get(RecorderVideoData.video_time);
        final String path = arrayList.get(position).get(RecorderVideoData.video_path);
        final String videoState1 = arrayList.get(position).get(RecorderVideoData.video_state);
        final String isDelete1 = arrayList.get(position).get(RecorderVideoData.video_isDelete);
        final String videoShowTime = arrayList.get(position).get(RecorderVideoData.video_show_time);
        final String videoOldLongTime = arrayList.get(position).get(RecorderVideoData.video_long_time);

        if(TextUtils.isEmpty(path)){
            holder.mItemView.setVisibility(View.INVISIBLE);
        }else{
            holder.mItemView.setVisibility(View.VISIBLE);
            holder.videoPreviewView.setData(path,isDelete1,videoState1,mIsShowSelectHint);
            holder.longTimeTv.setText(videoShowTime);
            if(chooseAdaperListener != null){
                holder.videoPreviewView.setOnReselectListener(new RecorderVideoPreviewView.OnReselectListener() {
                    @Override
                    public void onReselect(int index) {
                        chooseAdaperListener.onClick(position,videoOldLongTime);
                    }
                });
            }
            if(mOnSelectListener != null){
                holder.videoPreviewView.setOnSelectListener(new RecorderVideoPreviewView.OnSelectListener() {
                    @Override
                    public void onSelect() {
                        mOnSelectListener.onSelect(position);
                    }
                });
            }
            if(mOnDeleteListener != null){
                holder.videoPreviewView.setOnDeleteListener(new RecorderVideoPreviewView.OnDeleteListener() {
                    @Override
                    public void onDelete(int index) {
                        mOnDeleteListener.onDelete(position,path);
                    }
                });
            }
            if(!TextUtils.isEmpty(time)){
                holder.timeTv.setVisibility(View.VISIBLE);
                holder.timeTv.setText(time);
            }else{
                holder.timeTv.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        int viewHeight = 0;
        public View mItemView;
        public RecorderVideoPreviewView videoPreviewView;
        public TextView timeTv,longTimeTv;

        public ViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            videoPreviewView = (RecorderVideoPreviewView)itemView.findViewById(R.id.a_video_choose_img1);
            timeTv = (TextView) itemView.findViewById(R.id.a_video_choose_time);
            longTimeTv = (TextView) videoPreviewView.findViewById(R.id.choose_video_item_time);

            int viewWidth = (ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_18)) / 2;
            viewHeight = (int) (viewWidth / 16f * 9);
//            //设置view宽高
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) videoPreviewView.getLayoutParams();
            layoutParams.width = viewWidth;
            layoutParams.height = viewHeight;
        }
    }

    public void setOnChooseListener(OnChooseAdaperListener listener){
        chooseAdaperListener = listener;
    }

    public void setOnSelectListener(OnSelectAdaperListener selectListener) {
        this.mOnSelectListener = selectListener;
    }

    public void setOnDeleteListener(OnDeleteListener mOnDeleteListener) {
        this.mOnDeleteListener = mOnDeleteListener;
    }

    public interface OnChooseAdaperListener{
        public void onClick(int index,String longTime);
    }

    public interface OnSelectAdaperListener{
        public void onSelect(int index);
    }

    public interface OnDeleteListener{
        public void onDelete(int index,String videoPath);
    }
}
