package amodule.lesson.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.stat.StatisticsManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvHorizatolListView;
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
    private TextView mTitleText,mSubTitleText;
    private RvHorizatolListView mRvHorizatolListView;
    private Adapter mAdapter;
    private List<Map<String,String>> mData= new ArrayList<>();
    public CourseHorizontalView(@NonNull Context context) {
        super(context);
        initialize(context,null,0);
    }

    public CourseHorizontalView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs,0);
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
        int dp10 = Tools.getDimen(context,R.dimen.dp_10);
        mRvHorizatolListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.right = dp10;
            }
        });
        mAdapter = new Adapter(context,mData);
        mRvHorizatolListView.setAdapter(mAdapter);
        Log.i("tzy", "initialize: ");
    }

    public void setData(Map<String,String> data){
        mTitleText.setText(checkStrNull(data.get("title")));
        mSubTitleText.setText(checkStrNull(data.get("subTitle")));
        List<Map<String,String>> tempData = StringManager.getListMapByJson(data.get("info"));
        mData.clear();
        mData.addAll(tempData);
        mAdapter.notifyDataSetChanged();
    }

    private String checkStrNull(String text) {
        return TextUtils.isEmpty(text) ? "" : text;
    }

    class Adapter extends RvBaseAdapter<Map<String,String>>{

        public Adapter(Context context, @Nullable List<Map<String, String>> data) {
            super(context, data);
        }

        @Override
        public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.view_course_horizontal_item,parent,false);
            return new ViewHolder(itemView);
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }
    }

    class ViewHolder extends RvBaseViewHolder<Map<String,String>>{

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            TextView title = findViewById(R.id.text);
        }
    }
}
