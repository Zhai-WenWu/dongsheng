/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package third.aliyun.edit.effects.audiomix;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import third.aliyun.edit.effects.control.EffectInfo;
import third.aliyun.edit.effects.control.OnItemClickListener;
import third.aliyun.edit.util.MusicBean;


public class OnlineAudioMixNewAdapter extends RvBaseAdapter<MusicBean> {
    private Context mContext;
    private OnItemClickListener mItemClick;
    private ArrayList<MusicBean> data = new ArrayList<>();
    private List<MusicBean> temp ;
    private int selectedIndex = 0;
    private static final int MUSIC_TYPE = 5;

    public OnlineAudioMixNewAdapter(Context context, @Nullable List<MusicBean> data) {
        super(context, data);
        this.mContext= context;
        this.data= (ArrayList<MusicBean>) data;
    }
    public void setData(ArrayList<MusicBean> data) {
        if (data == null) {
            return;
        }
        this.data = data;
        notifyDataSetChanged();
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClick = listener;
    }

    public void clearSelect(){
        int lastIndex = selectedIndex;
        selectedIndex = Integer.MAX_VALUE;
        notifyItemChanged(lastIndex);
    }
    public void setSelectedIndex(int index){
        selectedIndex = index;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public RvBaseViewHolder<MusicBean> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(new AudioItemView(mContext));
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    class ItemViewHolder extends RvBaseViewHolder<MusicBean> {
        private View itemView;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView= itemView;
        }

        @Override
        public void bindData(int position, @Nullable MusicBean data) {
            if(itemView instanceof AudioItemView){
                ((AudioItemView)itemView).setdata(position,data,selectedIndex);
                ((AudioItemView)itemView).setItemClick(new OnItemClickListener(){
                    @Override
                    public boolean onItemClick(EffectInfo effectInfo, int index) {
                        Log.i("xianghaTag","index:::"+index);
                        if(audioDownClick){
                            audioDownNoNext=true;
                        }
                        setSelectedIndex(index);
                        if(mItemClick!=null)mItemClick.onItemClick(effectInfo,index);
                        return false;
                    }
                });
                ((AudioItemView)itemView).setAudioItemClick(new AudioItemClick() {
                    @Override
                    public void audioClick(int position) {
                        audioDownClick= true;
                        audioChoosePosition= position;
                        audioDownNoNext=false;
                    }
                    @Override
                    public void audioDownLoadClick(EffectInfo info, int position) {
                        audioDownClick=false;
                        if(!audioDownNoNext&&audioChoosePosition==position){
                            selectedIndex = position;
                            if(mItemClick!=null)mItemClick.onItemClick(info,position);
                        }
                        notifyDataSetChanged();

                    }
                });
            }
        }
    }
    private boolean audioDownClick = false;
    private boolean audioDownNoNext = false;
    private int audioChoosePosition = -1;
    public interface AudioItemClick{
        public void audioClick(int position);
        public void audioDownLoadClick(EffectInfo info, int position);
    }
}
