package amodule.search.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.stat.RvBaseViewHolderStat;
import acore.logic.stat.RvMapViewHolderStat;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.search.view.SearchMultipleVipLessonView;
import amodule.search.view.SearchSingleVipLessonView;

/**
 * Description :
 * PackageName : amodule.search.adapter
 * Created by mrtrying on 2018/11/22 11:28.
 * e_mail : ztanzeyu@gmail.com
 */
public class SearchVipLessonAdapter extends RvBaseAdapter<Map<String,String>> {
    public static  final String VIEW_TYPE_KEY = "view_type";
    public static final int VIEW_TYPE_SINGLE = 1;
    public static final int VIEW_TYPE_MULTIPLE = 2;
    private OnItemClickLesson mOnItemClickLesson;

    public SearchVipLessonAdapter(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_TYPE_SINGLE:
                return new SingleViewHolder(new SearchSingleVipLessonView(mContext),parent);
            case VIEW_TYPE_MULTIPLE:
                return new MultipleViewHolder(new SearchMultipleVipLessonView(mContext),parent);
            default:
                return EmptyViewHolder.of(mContext);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Map<String,String> data = getItem(position);
        if(data == null || TextUtils.isEmpty(data.get(VIEW_TYPE_KEY))){
           return  VIEW_TYPE_MULTIPLE;
        }
        return Tools.parseIntOfThrow(data.get(VIEW_TYPE_KEY),VIEW_TYPE_MULTIPLE);
    }

    private void handleItemClickLesson(int positoin,Map<String,String> data){
        if(mOnItemClickLesson != null){
            mOnItemClickLesson.onItemClick(positoin,data);
        }
    }

    public void setOnItemClickLesson(OnItemClickLesson onItemClickLesson) {
        mOnItemClickLesson = onItemClickLesson;
    }

    class SingleViewHolder extends RvMapViewHolderStat {
        SearchSingleVipLessonView mSingleVipLessonView;

        public SingleViewHolder(@NonNull SearchSingleVipLessonView itemView,ViewGroup parent) {
            super(itemView,parent);
            mSingleVipLessonView = itemView;
        }

        @Override
        public void overrideBindData(int position, @Nullable Map<String, String> data) {
            mSingleVipLessonView.setData(data);
            mSingleVipLessonView.setOnClickListener(v -> handleItemClickLesson(position,data));
        }
    }

    class MultipleViewHolder extends RvMapViewHolderStat {
        SearchMultipleVipLessonView mMultipleVipLessonView;

        public MultipleViewHolder(@NonNull SearchMultipleVipLessonView itemView, View parent) {
            super(itemView, parent);
            mMultipleVipLessonView = itemView;
        }

        @Override
        public void overrideBindData(int position, @Nullable Map<String, String> data) {
            mMultipleVipLessonView.setData(data,position);
            mMultipleVipLessonView.setOnClickListener(v -> handleItemClickLesson(position,data));
        }
    }

    static class EmptyViewHolder extends RvBaseViewHolder<Map<String, String>>{

        public static EmptyViewHolder of(Context context){
            return new EmptyViewHolder(new View(context));
        }


        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            //do nothing.
        }
    }


    public interface OnItemClickLesson{
        void onItemClick(int position,Map<String,String> data);
    }

}
