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

public class SyllabusAdapter extends BaseExpandableListAdapter {
    private Activity mActivity;
    private List<String> groupList = new ArrayList<>();
    private List<List<String>> childList = new ArrayList<>();
    private int mChildSelectIndex;
    private int mGroupSelectIndex;

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

        if (getChildrenCount(groupPosition) > 0) {
            groupViewHolder.ivRight.setVisibility(View.VISIBLE);
            groupViewHolder.tvRight.setVisibility(View.VISIBLE);
            if (isExpanded) {
                groupViewHolder.ivRight.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.class_card_group_close));
                groupViewHolder.llGroup.setBackgroundResource(R.drawable.bg_circle_f5f7fa_top_10);
            } else {
                groupViewHolder.ivRight.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.class_card_group_open));
                groupViewHolder.llGroup.setBackgroundResource(R.drawable.bg_circle_f5f7fa_10);
            }
        } else {
            convertView.setBackgroundResource(R.drawable.bg_circle_f5f7fa_10);
            groupViewHolder.ivRight.setVisibility(View.GONE);
            groupViewHolder.tvRight.setVisibility(View.GONE);
//            if (groupPosition == mGroupSelectIndex) {
//                convertView.setBackgroundResource(R.drawable.bg_circle_f5f7fab_10);
//            } else {
//                convertView.setBackgroundResource(R.drawable.bg_circle_f5f7fa_10);
//            }

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


        if (childPosition == getChildrenCount(groupPosition) - 1) {
//            if (childPosition == mChildSelectIndex && groupPosition == mGroupSelectIndex) {
//                convertView.setBackgroundResource(R.drawable.bg_circle_f5f7fab_bottom_10);
//            } else {
            convertView.setBackgroundResource(R.drawable.bg_circle_f5f7fa_bottom_10);
        } else {
//            if (childPosition == mChildSelectIndex && groupPosition == mGroupSelectIndex) {
//                convertView.setBackgroundColor(mActivity.getResources().getColor(R.color.c_ffbe03));
//            } else {//c_ffbe03
            convertView.setBackgroundColor(mActivity.getResources().getColor(R.color.c_f5f7fa));

        }
        childViewHolder.tvTitle.setText(childList.get(groupPosition).get(childPosition));
        return convertView;
    }

    //        指定位置上的子元素是否可选中
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
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
