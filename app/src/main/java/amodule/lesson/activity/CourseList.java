package amodule.lesson.activity;

import android.os.Bundle;

import com.xiangha.R;

import acore.override.activity.base.BaseAppCompatActivity;

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
    }
}
