package amodule.search.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;

/**
 * Description :
 * PackageName : amodule.search.adapter
 * Created by mrtrying on 2018/11/8 17:39.
 * e_mail : ztanzeyu@gmail.com
 */
public class SearchHorizonAdapter extends RvBaseAdapter<Map<String,String>> {
    private OnItemClickListener mOnItemClickListener;
    public SearchHorizonAdapter(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.c_view_search_horizon_item,parent,false);
        ItemViewHolder viewHolder = new ItemViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    class ItemViewHolder extends RvBaseViewHolder<Map<String,String>>{

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            itemView.setOnClickListener(v -> handleItemClick(v,data));
            TextView textView = findViewById(R.id.text);
            textView.setText(data.get("name"));
        }
    }

    private void handleItemClick(View v,Map<String, String> data) {
        if(mOnItemClickListener != null){
            mOnItemClickListener.onClick(v,data);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onClick(View v,Map<String, String> data);
    }
}
