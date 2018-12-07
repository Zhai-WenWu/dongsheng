package amodule.lesson.activity;

import android.os.Bundle;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import amodule.lesson.view.StudyAskView;
import amodule.lesson.view.StudylIntroductionView;

public class StudyAsk extends BaseAppCompatActivity {
    public static final String EXTRA_DESC = "desc";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StudyAskView studyAskView = new StudyAskView(this);
        setContentView(studyAskView);

//        String extra = getIntent().getStringExtra(EXTRA_DESC);
//        introductionView.setData(StringManager.getFirstMap(extra));
    }
}
