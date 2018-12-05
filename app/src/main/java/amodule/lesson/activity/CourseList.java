package amodule.lesson.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import com.xiangha.R;

import acore.override.activity.base.BaseAppCompatActivity;
import amodule.lesson.adapter.ClassCardExListAdapter;

/**
 * Description :
 * PackageName : amodule.lesson.activity
 * Created by mrtrying on 2018/12/3 15:28.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseList extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("课程表",2,0,R.layout.c_view_bar_title,R.layout.a_course_list);
        initView();
    }

    private void initView() {
        ExpandableListView exList = findViewById(R.id.expand_list);
        ClassCardExListAdapter classCardExListAdapter = new ClassCardExListAdapter(this);
        exList.setAdapter(classCardExListAdapter);
    }
}
