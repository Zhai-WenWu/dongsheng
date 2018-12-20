package amodule.lesson.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.stat.RvBaseViewHolderStat;
import acore.logic.stat.intefaces.OnItemClickListenerRvStat;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvHorizatolListView;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;

public class StudySyllabusView extends RelativeLayout {
    public static final String MOUDEL_NAME = "横滑课程表";
    private final Context mContext;
    private RvHorizatolListView rvHorizatolListView;
    private View view;
    private SyllabusAdapter syllabusAdapter;
    private ArrayList<Map<String, String>> mapList;
    private int mSelectIndex;
    private TextView classNumTv;
    private TextView titleTv;
    private boolean changeTvColer;
    private boolean listNeedScroll = true;

    public TextView getClassNumTv() {
        return classNumTv;
    }

    public void setChangeTvColer(boolean changeTvColer) {
        this.changeTvColer = changeTvColer;
    }

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
        classNumTv = findViewById(R.id.tv_class_num);
        titleTv = findViewById(R.id.tv_title);
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

        rvHorizatolListView.setOnItemClickListener(new OnItemClickListenerRvStat(MOUDEL_NAME) {

            @Override
            protected String getStatData(int position) {
                return mapList.get(position).get("statJson");
            }

            @Override
            public void onItemClicked(View view, RecyclerView.ViewHolder holder, int position) {
                listNeedScroll = false;
                onSyllabusSelect.onSelect(position, mapList.get(position).get("code"));
            }
        });
    }

    public void setData(Map<String, String> courseListMap, int childIndex) {
        ArrayList<Map<String, String>> info = StringManager.getListMapByJson(courseListMap.get("chapterList"));
        Map<String, String> lessonListMap = info.get(0);//第几章
        mapList = StringManager.getListMapByJson(lessonListMap.get("lessonList"));
        syllabusAdapter.setData(mapList);
        this.mSelectIndex = childIndex;
        syllabusAdapter.notifyDataSetChanged();
        LinearLayoutManager mLayoutManager = (LinearLayoutManager) rvHorizatolListView.getLayoutManager();
        if (listNeedScroll)
            mLayoutManager.scrollToPositionWithOffset(mSelectIndex, 0);
        listNeedScroll = true;
        int size = mapList.size();
        StringBuilder stringBuilder = new StringBuilder(String.valueOf(size) + "讲");
        if (size > 3) {
            stringBuilder.append(">");
            classNumTv.setEnabled(true);
        } else {
            classNumTv.setEnabled(false);
        }
        classNumTv.setText(stringBuilder);
        titleTv.setText(courseListMap.get("title"));
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

    public class ViewHolder extends RvBaseViewHolderStat<Map<String, String>> {
        private ItemSyllabus view;

        public ViewHolder(@NonNull ItemSyllabus itemView) {
            super(itemView, MOUDEL_NAME);
            this.view = itemView;
        }

        @Override
        public boolean isShown(Map<String, String> data) {
            return TextUtils.equals("2", data.get("isShow"));
        }

        @Override
        public void hasShown(Map<String, String> data) {
            data.put("isShow", "2");
        }

        @Override
        public String getStatJson(Map<String, String> data) {
            return data.get("statJson");
        }

        @Override
        public void overrideBindData(int position, @Nullable Map<String, String> data) {
            view.setData(data, position, mSelectIndex);
            if (changeTvColer) {
                view.setTextColer();
            }
        }
    }

    public OnSyllabusSelect onSyllabusSelect;

    public void setOnSyllabusSelect(OnSyllabusSelect onSyllabusSelect) {
        this.onSyllabusSelect = onSyllabusSelect;
    }

    public interface OnSyllabusSelect {
        void onSelect(int position, String code);
    }
}
