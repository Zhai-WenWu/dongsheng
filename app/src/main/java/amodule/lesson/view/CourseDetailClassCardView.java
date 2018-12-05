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

import acore.tools.Tools;
import acore.widget.rvlistview.RvHorizatolListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.dish.view.DishSkillView;

public class CourseDetailClassCardView extends RelativeLayout {

    private final Context mContext;
    private RvHorizatolListView rvHorizatolListView;
    private View view;

    public CourseDetailClassCardView(Context context) {
        this(context, null);
    }

    public CourseDetailClassCardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CourseDetailClassCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        view = LayoutInflater.from(context).inflate(R.layout.view_course_class_card, this, true);
        initView();
    }

    private void initView() {
        ArrayList<Map<String, String>> mapList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            mapList.add(new ArrayMap<>());
        }
        rvHorizatolListView = (RvHorizatolListView) view.findViewById(R.id.rvHorizatolListView);
        rvHorizatolListView.setAdapter(new AdapterModuleScroll(mContext, mapList));
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

    public class AdapterModuleScroll extends RvBaseAdapter<Map<String, String>> {

        public AdapterModuleScroll(Context context, @Nullable List<Map<String, String>> data) {
            super(context, data);
        }

        @Override
        public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(new ClassCardItemView(mContext));
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }
    }

    public class ViewHolder extends RvBaseViewHolder<Map<String, String>> {
        private ClassCardItemView view;

        public ViewHolder(@NonNull ClassCardItemView itemView) {
            super(itemView);
            this.view = itemView;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            view.setData(data, position);
        }
    }
}
