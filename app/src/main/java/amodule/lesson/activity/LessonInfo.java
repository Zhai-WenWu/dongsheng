package amodule.lesson.activity;

import android.os.Bundle;

import com.xiangha.R;

import acore.override.activity.base.BaseAppCompatActivity;
import amodule.lesson.controler.data.LessonInfoDataMananger;
import amodule.lesson.controler.view.LessonInfoUIMananger;

public class LessonInfo extends BaseAppCompatActivity {

    private LessonInfoUIMananger mUIMananger;
    private LessonInfoDataMananger mDataMananger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_lesson_info_layout);
    }

    private void initialize(){
        initializeUI();
        initializeData();
        
    }

    private void initializeUI() {
        mUIMananger = new LessonInfoUIMananger(this);
    }

    private void initializeData() {
        mDataMananger = new LessonInfoDataMananger(this);
    }

    /**处理外带数据*/
    private void setPreData(){

    }

    /**加载数据*/
    private void loadData(){

    }

    /***/
    private void refresh(){

    }

}
