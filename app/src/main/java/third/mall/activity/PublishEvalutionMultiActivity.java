package third.mall.activity;

import android.os.Bundle;

import com.xiangha.R;

import acore.override.activity.base.BaseActivity;

public class PublishEvalutionMultiActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("发布评价",6,0,R.layout.c_view_title_bar,R.layout.activity_publish_evalution_multi);
    }
}
