package amodule.lesson.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import amodule.lesson.adapter.CourseVideoContentAdapter;
import amodule.lesson.controler.data.CourseDataController;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

public class StudyPoint extends BaseAppCompatActivity {
    public static final String EXTRA_CODE = "code";
    public static final String EXTRA_TITLE = "title";
    private CourseVideoContentAdapter mVideoDetailAdapter;
    private ArrayList<Map<String, String>> videoList = new ArrayList<>();
    private RvListView mCourseList;
    private String mCode;
    private String titleStr;

    public static void startActivity(Context context){
        context.startActivity(new Intent(context,StudyPoint.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        titleStr = getIntent().getStringExtra(EXTRA_TITLE);
        initActivity(titleStr, 2, 0, R.layout.c_view_bar_title, R.layout.a_study_point);
        mCourseList = findViewById(R.id.course_list);
        mVideoDetailAdapter = new CourseVideoContentAdapter(this, videoList);
        mCourseList.setAdapter(mVideoDetailAdapter);
        loadManager.setLoad(v->loadLessonPointData());
    }

    private void loadLessonPointData() {
        CourseDataController.loadLessonPointData("0", new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> mLessonInfoMap = StringManager.getFirstMap(o);
                    initPointData(mLessonInfoMap);
                }
            }
        });
    }

    private void initPointData(Map<String, String> pointMap) {
        //学习要点
        ArrayList<Map<String, String>> infoList = StringManager.getListMapByJson(pointMap.get("info"));
        for (Map<String, String> info : infoList) {
            ArrayList<Map<String, String>> vidList = StringManager.getListMapByJson(info.get("info"));
            boolean isFirstItem = true;
            for (Map<String, String> vidInfo : vidList) {
                Map<String, String> stringMap = new ArrayMap<>();
                if (isFirstItem) {
                    stringMap.put("subTitle", info.get("subTitle"));
                    isFirstItem = false;
                }
                stringMap.put("content", vidInfo.get("content"));
                stringMap.put("img", vidInfo.get("img"));
                videoList.add(stringMap);
            }
        }
        mVideoDetailAdapter.setData(videoList);
        mVideoDetailAdapter.notifyDataSetChanged();
    }
}
