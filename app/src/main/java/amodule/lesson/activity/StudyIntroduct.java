package amodule.lesson.activity;

import android.os.Bundle;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.lesson.view.StudylIntroductionView;

public class StudyIntroduct extends BaseAppCompatActivity {
    public static final String EXTRA_DESC = "desc";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StudylIntroductionView introductionView = new StudylIntroductionView(this);
        setContentView(introductionView);

        String extra = getIntent().getStringExtra(EXTRA_DESC);
        introductionView.setData(extra);
    }
}
