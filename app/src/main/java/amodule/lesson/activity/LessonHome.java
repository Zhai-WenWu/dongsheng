package amodule.lesson.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.xiangha.R;

import acore.override.activity.base.BaseAppCompatActivity;

public class LessonHome extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("",2,0,0,R.layout.a_lesson_home);
    }
}
