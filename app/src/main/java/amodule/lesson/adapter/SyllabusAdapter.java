package amodule.lesson.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;

import acore.logic.stat.StatModel;
import acore.logic.stat.StatisticsManager;
import amodule.lesson.model.SyllabusStatModel;

public class SyllabusAdapter extends BaseExpandableListAdapter {
    private Activity mActivity;
    private List<String> groupList = new ArrayList<>();
    private List<List<String>> childList = new ArrayList<>();
    private int mChildSelectIndex;
    private int mGroupSelectIndex;
    private List<List<SyllabusStatModel>> mStatJsonList;
    private String activityName;

    public void setGroupList(List<String> groupList) {
        this.groupList = groupList;
    }

    public void setChildList(List<List<String>> childList) {
        this.childList = childList;
    }

    public SyllabusAdapter(Activity activity) {
        this.mActivity = activity;
    }

    public void setSelectIndex(int groupSelectNum, int childSelectNum) {
        activityName = mActivity.getClass().getSimpleName();
        this.mChildSelectIndex = childSelectNum;
        this.mGroupSelectIndex = groupSelectNum;
    }

    //        获取分组的个数
    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    //        获取指定分组中的子选项的个数
    @Override
    public int getChildrenCount(int groupPosition) {
        return childList.get(groupPosition).size();
    }

    //        获取指定的分组数据
    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    //        获取指定分组中的指定子选项数据
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childList.get(groupPosition).get(childPosition);
    }

    //        获取指定分组的ID, 这个ID必须是唯一的
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    //        获取子选项的ID, 这个ID必须是唯一的
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    //        分组和子选项是否持有稳定的ID, 就是说底层数据的改变会不会影响到它们。
    @Override
    public boolean hasStableIds() {
        return true;
    }

    //        获取显示指定分组的视图
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_classcard_group, parent, false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_group_title);
            groupViewHolder.tvRight = (TextView) convertView.findViewById(R.id.tv_right);
            groupViewHolder.ivRight = (ImageView) convertView.findViewById(R.id.iv_right);
            groupViewHolder.llGroup = (LinearLayout) convertView.findViewById(R.id.ll_group);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        groupViewHolder.tvTitle.setText(groupList.get(groupPosition));
        int childrenCount = getChildrenCount(groupPosition);
        if (childrenCount > 0) {
            groupViewHolder.ivRight.setVisibility(View.VISIBLE);
            groupViewHolder.tvRight.setVisibility(View.VISIBLE);
            groupViewHolder.tvRight.setText(childrenCount + "节");
            if (isExpanded) {
                groupViewHolder.ivRight.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.class_card_group_close));
                groupViewHolder.llGroup.setBackgroundResource(R.drawable.bg_circle_f5f7fa_top_10);
            } else {
                groupViewHolder.ivRight.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.class_card_group_open));
                groupViewHolder.llGroup.setBackgroundResource(R.drawable.bg_circle_f5f7fa_10);
            }
        } else {
            SyllabusStatModel syllabusStatModel = mStatJsonList.get(0).get(groupPosition);
            if (!syllabusStatModel.isShow()) {
                StatisticsManager.saveData(StatModel.createListShowModel(activityName, "", "1" + String.valueOf(groupPosition + 1), "", syllabusStatModel.getStat()));
                syllabusStatModel.setShow(true);
            }
            groupViewHolder.ivRight.setVisibility(View.GONE);
            groupViewHolder.tvRight.setVisibility(View.GONE);
            groupViewHolder.llGroup.setBackgroundResource(R.drawable.bg_circle_f5f7fa_10);
        }
        if (mGroupSelectIndex == groupPosition) {
            groupViewHolder.tvTitle.setTextColor(mActivity.getResources().getColor(R.color.color_fa273b));
        } else {
            groupViewHolder.tvTitle.setTextColor(mActivity.getResources().getColor(R.color.c_3e3e3e));
        }

        return convertView;
    }

    //        获取显示指定分组中的指定子选项的视图
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_classcard_child, parent, false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_child_title);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }

        childViewHolder.tvTitle.setText(childList.get(groupPosition).get(childPosition));
        SyllabusStatModel syllabusStatModel = mStatJsonList.get(groupPosition).get(childPosition);
        if (!syllabusStatModel.isShow()) {
            StatisticsManager.saveData(StatModel.createListShowModel(activityName, "", String.valueOf(groupPosition + 1) + String.valueOf(childPosition + 1), "", syllabusStatModel.getStat()));
            syllabusStatModel.setShow(true);
        }

        if (childPosition == getChildrenCount(groupPosition) - 1) {
            convertView.setBackgroundResource(R.drawable.bg_circle_f5f7fa_bottom_10);
        } else {
            convertView.setBackgroundColor(mActivity.getResources().getColor(R.color.c_f5f7fa));
        }

        if (groupPosition == mGroupSelectIndex && childPosition == mChildSelectIndex) {
            childViewHolder.tvTitle.setTextColor(mActivity.getResources().getColor(R.color.color_fa273b));
            childViewHolder.tvTitle.setCompoundDrawables(mActivity.getResources().getDrawable(R.drawable.class_card_item_playing), null, null, null);
        } else {
            childViewHolder.tvTitle.setTextColor(mActivity.getResources().getColor(R.color.c_3e3e3e));
            childViewHolder.tvTitle.setCompoundDrawables(mActivity.getResources().getDrawable(R.drawable.class_card_item_play), null, null, null);
        }
        return convertView;
    }

    //        指定位置上的子元素是否可选中
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void setStatData(List<List<SyllabusStatModel>> statJsonList) {
        this.mStatJsonList = statJsonList;
    }

    class GroupViewHolder {
        TextView tvTitle;
        TextView tvRight;
        ImageView ivRight;
        LinearLayout llGroup;
    }

    class ChildViewHolder {
        TextView tvTitle;
    }
}
