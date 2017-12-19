package amodule.lesson.activity;

import android.os.Bundle;

import com.xiangha.R;

import acore.override.activity.base.BaseAppCompatActivity;

public class LessonListPage extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("",2,0,0,R.layout.lesson_list_page);
    }
}
