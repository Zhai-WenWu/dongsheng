package amodule.lesson.view.introduction;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.stat.StatisticsManager;
import acore.logic.stat.intefaces.OnClickListenerStat;
import acore.logic.stat.intefaces.OnItemClickListenerRvStat;
import acore.tools.ColorUtil;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.RvHorizatolListView;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;

/**
 * Description :
 * PackageName : amodule.lesson.view
 * Created by mrtrying on 2018/12/3 15:54.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseHorizontalView extends FrameLayout {
    final int LAYOUT_ID = R.layout.view_course_horizontal;
    final String MOUDLE_NAME = "横滑课程表";
    private TextView mTitleText, mSubTitleText;
    private RvHorizatolListView mRvHorizatolListView;
    private Adapter mAdapter;
    private List<Map<String, String>> mData = new ArrayList<>();
    private OnItemClickCallback mOnItemClickCallback;
    private String lessonNum;

    public CourseHorizontalView(@NonNull Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public CourseHorizontalView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public CourseHorizontalView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(LAYOUT_ID, this);
        mTitleText = findViewById(R.id.title);
        mSubTitleText = findViewById(R.id.sub_title);
        mRvHorizatolListView = findViewById(R.id.rv_list_view);
        int dp10 = Tools.getDimen(context, R.dimen.dp_10);
        mRvHorizatolListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.right = dp10;
            }
        });
        mAdapter = new Adapter(context, mData);
        mRvHorizatolListView.setAdapter(mAdapter);
        mRvHorizatolListView.setOnItemClickListener(new OnItemClickListenerRvStat(MOUDLE_NAME) {
            @Override
            public void onItemClicked(View view, RecyclerView.ViewHolder holder, int position) {
                handleOnItemClickCallback(position, mData.get(position));
            }

            @Override
            protected String getStatData(int position) {
                return null;
            }
        });
    }

    public void setData(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        String historyLessonCode = StringManager.getFirstMap(data.get("playHistory")).get("lessonCode");
        String historyChapterCode = StringManager.getFirstMap(data.get("playHistory")).get("chapterCode");
        mTitleText.setText(checkStrNull(data.get("title")));
//        mSubTitleText.setText(checkStrNull(data.get("subTitle")));
        List<Map<String, String>> chapterList = StringManager.getListMapByJson(data.get("chapterList"));
        if (!chapterList.isEmpty()) {
            //如果只有一章，则显示课的数据列表
            int lessonNum = Tools.parseIntOfThrow(data.get("chapterNum"), 1);
            if (lessonNum == 1) {
                String chapterCode = chapterList.get(0).get("code");
                ArrayList<Map<String, String>> lessonList = StringManager.getListMapByJson(chapterList.get(0).get("lessonList"));
                for (int i = 0; i < lessonList.size(); i++) {
                    if (TextUtils.equals(historyLessonCode, lessonList.get(i).get("code"))) {
                        setCurrentPosition(i);
                        break;
                    }
                }
                Stream.of(lessonList).forEach(value -> {
                    value.put("chapterCode", chapterCode);
                    value.put("lessonCode", value.get("code"));
                });
            } else {
                for (int i = 0; i < chapterList.size(); i++) {
                    if (TextUtils.equals(historyChapterCode, chapterList.get(i).get("code"))) {
                        setCurrentPosition(i);
                        break;
                    }
                }
                Stream.of(chapterList).forEach(value -> value.put("chapterCode", value.get("code")));
            }
        }
        if (chapterList.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        mData.clear();
        mData.addAll(chapterList);
        mAdapter.notifyDataSetChanged();
    }

    public void setLessonNum(String lessonNum) {
        this.lessonNum = lessonNum;
        mSubTitleText.setText(checkStrNull(lessonNum));
    }

    public void setSubTitleOnClickListener(OnClickListener listener) {
        mSubTitleText.setOnClickListener(new OnClickListenerStat(MOUDLE_NAME) {
            @Override
            public void onClicked(View v) {
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
    }

    public void setOnItemClickListener(OnItemClickCallback callback) {
        mOnItemClickCallback = callback;
    }

    private void handleOnItemClickCallback(int position, Map<String, String> data) {
        if (mOnItemClickCallback != null) {
            mOnItemClickCallback.onItemClick(position, data);
        }
    }

    public void setCurrentPosition(int currentPosition) {
        if (mAdapter != null) {
            mAdapter.setCurrentPosition(currentPosition);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mRvHorizatolListView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    private String checkStrNull(String text) {
        return TextUtils.isEmpty(text) ? "" : text;
    }

    class Adapter extends RvBaseAdapter<Map<String, String>> {
        private int currentPosition = -1;
        int itemWidth, itemHeight;

        public Adapter(Context context, @Nullable List<Map<String, String>> data) {
            super(context, data);
            itemWidth = (ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_50)) / 2;
            itemHeight = (int) (itemWidth / 163f * 85);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.view_course_horizontal_item, parent, false);
            itemView.getLayoutParams().width = itemWidth;
            itemView.getLayoutParams().height = itemHeight;
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RvBaseViewHolder<Map<String, String>> holder, int position) {
            super.onBindViewHolder(holder, position);
            if (holder instanceof ViewHolder) {
                ((ViewHolder) holder).select(currentPosition == position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        void setCurrentPosition(int currentPosition) {
            this.currentPosition = currentPosition;
            notifyDataSetChanged();
        }
    }

    class ViewHolder extends RvBaseViewHolder<Map<String, String>> {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            if (data == null || data.isEmpty()) {
                itemView.setVisibility(GONE);
                return;
            }
            TextView title = findViewById(R.id.text);
            title.setText(data.get("title"));
        }

        public void select(boolean selected) {
            TextView title = findViewById(R.id.text);
            title.setTextColor(ColorUtil.parseColor(selected ? "#DEA73E" : "#3E3E3E"));
            title.setBackgroundResource(selected ? R.drawable.bg_course_horizontal_item_selected : R.drawable.bg_course_horizontal_item);
        }
    }

    public interface OnItemClickCallback {
        void onItemClick(int position, Map<String, String> data);
    }
}
