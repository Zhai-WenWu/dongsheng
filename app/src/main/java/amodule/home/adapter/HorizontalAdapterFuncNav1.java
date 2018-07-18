package amodule.home.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule._common.utility.WidgetUtility;
import amodule.home.view.HomeFuncNavView1;
import amodule.home.viewholder.ViewHolder1;
import amodule.home.viewholder.XHBaseRvViewHolder;

/**
 *
 */
public class HorizontalAdapterFuncNav1 extends RvBaseAdapter<Map<String, String>> {
    private List<Map<String, String>> mDatas;
    public HorizontalAdapterFuncNav1( Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
        mDatas = data;
    }
    private int itemWaith;
    public void setItemWaith(int itemWaith){
        this.itemWaith= itemWaith;
    }
    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.widget_func_nav_1_item, null));
    }

    @Override
    public void onBindViewHolder(RvBaseViewHolder<Map<String, String>> holder, int position) {
        if (mDatas == null || mDatas.isEmpty() || mDatas.size() <= position)
            return;
        super.onBindViewHolder(holder,position);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    class ViewHolder extends XHBaseRvViewHolder {
        private TextView textView;
        private ImageView imageView;
        private LinearLayout item_layout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_1);
            imageView = (ImageView) itemView.findViewById(R.id.icon);
            item_layout = (LinearLayout) itemView.findViewById(R.id.item_layout);
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            super.bindData(position, data);
            WidgetUtility.setTextToView(textView, data.get("text1"), false);
            if(itemWaith>0) {
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(itemWaith,ViewGroup.LayoutParams.WRAP_CONTENT);
                item_layout.setLayoutParams(layoutParams);
            }
            if(null != imageView){
                if(!TextUtils.isEmpty(data.get("img"))) {
                    Glide.with(getContext()).load(data.get("img")).into(imageView);
                }
                else if(data.containsKey("drawableId")&&!TextUtils.isEmpty(data.get("drawableId"))){
                    Glide.with(getContext()).load("").placeholder(Integer.parseInt(data.get("drawableId"))).into(imageView);
                }
            }
        }
    }
}
