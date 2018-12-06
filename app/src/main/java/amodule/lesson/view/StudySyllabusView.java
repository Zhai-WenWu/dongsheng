package amodule.lesson.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvHorizatolListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;

public class StudySyllabusView extends RelativeLayout {

    private final Context mContext;
    private RvHorizatolListView rvHorizatolListView;
    private View view;
    private SyllabusAdapter syllabusAdapter;
    private ArrayList<Map<String, String>> mapList;

    public StudySyllabusView(Context context) {
        this(context, null);
    }

    public StudySyllabusView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StudySyllabusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        view = LayoutInflater.from(context).inflate(R.layout.view_course_class_card, this, true);
        initView();
    }

    private void initView() {
        mapList = new ArrayList<>();
        rvHorizatolListView = (RvHorizatolListView) view.findViewById(R.id.rvHorizatolListView);
        syllabusAdapter = new SyllabusAdapter(mContext, mapList);
        rvHorizatolListView.setAdapter(syllabusAdapter);
        int padding = Tools.getDimen(getContext(), R.dimen.dp_10);
        rvHorizatolListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view);
                if (position == 0) {
                    outRect.left = 0;
                } else {
                    outRect.left = padding;
                }
            }
        });
    }

    public void setData(Map<String, String> data) {
        ArrayList<Map<String, String>> info = StringManager.getListMapByJson(data.get("info"));
        mapList = StringManager.getListMapByJson(info.get(0).get("lessonList"));
        syllabusAdapter.setData(mapList);
        syllabusAdapter.notifyDataSetChanged();
    }

    public class SyllabusAdapter extends RvBaseAdapter<Map<String, String>> {

        public SyllabusAdapter(Context context, @Nullable List<Map<String, String>> data) {
            super(context, data);
        }

        @Override
        public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(new ItemSyllabus(mContext));
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }
    }

    public class ViewHolder extends RvBaseViewHolder<Map<String, String>> {
        private ItemSyllabus view;

        public ViewHolder(@NonNull ItemSyllabus itemView) {
            super(itemView);
            this.view = itemView;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            view.setData(data, position);
        }
    }
}
