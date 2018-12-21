package amodule.lesson.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ExpandableListView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.stat.StatModel;
import acore.logic.stat.StatisticsManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import amodule.lesson.adapter.SyllabusAdapter;
import amodule.lesson.controler.data.CourseDataController;
import amodule.lesson.model.SyllabusStatModel;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

/**
 * Description :
 * PackageName : amodule.lesson.activity
 * Created by mrtrying on 2018/12/3 15:28.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseList extends BaseAppCompatActivity {
    public static final String EXTRA_FROM_STUDY = "from";
    public static final String EXTRA_CODE = "code";
    public static final String EXTRA_TYPE = "type";
    private SyllabusAdapter mSyllabusAdapter;
    private List<String> mGroupList = new ArrayList<>();
    private List<List<String>> mChildList = new ArrayList<>();
    private List<List<SyllabusStatModel>> mStatJsonList = new ArrayList<>();
    private ExpandableListView mExList;
    private int mGroupSelectIndex = 0;
    private int mChildSelectIndex = -1;
    private boolean isFromStudy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("课程表", 2, 0, R.layout.c_view_bar_title, R.layout.a_course_list);
        initView();
        initData();
    }

    private void initView() {
        mExList = findViewById(R.id.expand_list);
        mSyllabusAdapter = new SyllabusAdapter(this);
        mExList.setAdapter(mSyllabusAdapter);
    }

    private void initData() {
        String mCode = getIntent().getStringExtra(EXTRA_CODE);
        isFromStudy = getIntent().getBooleanExtra(EXTRA_FROM_STUDY, false);
        String type = isFromStudy ? "2" : "1";
        loadManager.loading(mExList, true);
        CourseDataController.loadCourseListData(mCode, type, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                loadManager.loaded(mExList);
                if (i >= ReqInternet.REQ_OK_STRING) {
                    Map<String, String> mCourseListMap = StringManager.getFirstMap(o);
                    initCourseListData(mCourseListMap);
                }
            }
        });
    }

    private void initCourseListData(Map<String, String> mCourseListMap) {
        String chapterNum = mCourseListMap.get("chapterNum");
        Map<String, String> playHistory = StringManager.getFirstMap("playHistory");
        String chapterCode = playHistory.get("chapterCode");
        String lessonCode = playHistory.get("lessonCode");
        ArrayList<Map<String, String>> info = StringManager.getListMapByJson(mCourseListMap.get("chapterList"));

        if (TextUtils.equals("1", chapterNum)) {//只有一章
            ArrayList<Map<String, String>> lessonList = StringManager.getListMapByJson(info.get(0).get("lessonList"));
            for (int j = 0; j < lessonList.size(); j++) {
                if (TextUtils.equals(lessonCode, lessonList.get(j).get("code"))) {
                    mGroupSelectIndex = j;
                    break;
                }
            }
            initOne(info);
        } else {//多章
            for (int i = 0; i < info.size(); i++) {
                if (TextUtils.equals(chapterCode, info.get(i).get("code"))) {
                    mGroupSelectIndex = i;
                    mExList.expandGroup(mGroupSelectIndex);
                    ArrayList<Map<String, String>> lessonList = StringManager.getListMapByJson(info.get(i).get("lessonList"));
                    for (int j = 0; j < lessonList.size(); j++) {
                        if (TextUtils.equals(lessonCode, lessonList.get(j).get("code"))) {
                            mChildSelectIndex = j;
                            break;
                        }
                    }
                    break;
                }
            }
            initMore(info);
        }
        mSyllabusAdapter.setChildList(mChildList);
        mSyllabusAdapter.setGroupList(mGroupList);
        mSyllabusAdapter.setStatData(mStatJsonList);
        mSyllabusAdapter.setSelectIndex(mGroupSelectIndex, mChildSelectIndex);
        mSyllabusAdapter.notifyDataSetChanged();

    }

    /**
     * @param info 只有一章
     */
    private void initOne(ArrayList<Map<String, String>> info) {
        ArrayList<Map<String, String>> lessonList = StringManager.getListMapByJson(info.get(0).get("lessonList"));
        ArrayList<String> child = new ArrayList<>();
        ArrayList<SyllabusStatModel> statJson = new ArrayList<>();
        for (Map<String, String> map : lessonList) {
            SyllabusStatModel syllabusStatModel = new SyllabusStatModel();
            syllabusStatModel.setStat(map.get("statJson"));
            statJson.add(syllabusStatModel);
            mGroupList.add(map.get("title"));
            mChildList.add(child);
        }
        mStatJsonList.add(statJson);

        //设置分组项的点击监听事件
        mExList.setOnGroupClickListener((expandableListView, view, i, l) -> {
            mGroupSelectIndex = i;
            mChildSelectIndex = i;
            clickItem(lessonList.get(i).get("code"));
            return false;
        });

    }

    /**
     * @param info 多章
     */
    private void initMore(ArrayList<Map<String, String>> info) {
        for (int i = 0; i < info.size(); i++) {
            mGroupList.add(info.get(i).get("title"));
            ArrayList<Map<String, String>> lessonList = StringManager.getListMapByJson(info.get(i).get("lessonList"));
            ArrayList<String> strings = new ArrayList<>();
            ArrayList<SyllabusStatModel> statJson = new ArrayList<>();
            for (Map<String, String> map : lessonList) {
                SyllabusStatModel syllabusStatModel = new SyllabusStatModel();
                syllabusStatModel.setStat(map.get("statJson"));
                statJson.add(syllabusStatModel);
                strings.add(map.get("title"));
            }
            mChildList.add(strings);
            mStatJsonList.add(statJson);
        }

        //设置子选项点击监听事件
        mExList.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            ArrayList<Map<String, String>> lessonList = StringManager.getListMapByJson(info.get(groupPosition).get("lessonList"));
            mGroupSelectIndex = groupPosition;
            mChildSelectIndex = childPosition;
            StatisticsManager.saveData(StatModel.createListClickModel(getClass().getSimpleName(), "", String.valueOf(groupPosition + 1), "",mStatJsonList.get(groupPosition).get(childPosition).getStat()));
            clickItem(lessonList.get(childPosition).get("code"));
            return true;
        });
    }


    private void clickItem(String code) {
        mSyllabusAdapter.setSelectIndex(mGroupSelectIndex, mChildSelectIndex);
        mSyllabusAdapter.notifyDataSetChanged();
        if (isFromStudy) {
            Intent intent = new Intent();
            intent.putExtra("code", code);
            intent.putExtra(CourseDetail.EXTRA_CHILD, mChildSelectIndex);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Intent it = new Intent(this, CourseDetail.class);
            it.putExtra("code", code);
            it.putExtra(CourseDetail.EXTRA_CHILD, mChildSelectIndex);
            startActivity(it);
        }
    }
}
