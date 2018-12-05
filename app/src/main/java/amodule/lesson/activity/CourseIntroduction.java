package amodule.lesson.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import amodule.lesson.adapter.CourseIntroductionAdatper;
import amodule.lesson.controler.data.CourseDataController;
import amodule.lesson.view.ChefIntroductionView;
import amodule.lesson.view.CourseHorizontalView;
import amodule.lesson.view.CourseIntroduceHeader;
import amodule.lesson.view.CourseVerticalView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

import static acore.tools.StringManager.API_COURSE_TOP;

/**
 * Description :
 * PackageName : amodule.lesson.activity
 * Created by mrtrying on 2018/12/3 15:26.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseIntroduction extends BaseAppCompatActivity {

    public static final String EXTRA_CEODE = "code";

    private CourseIntroduceHeader mCourseIntroduceHeader;
    private ChefIntroductionView mChefIntroductionView;
    private CourseHorizontalView mCourseHorizontalView;
    private CourseVerticalView mCourseVerticalView;
    private RvListView mRvListView;
    private CourseIntroductionAdatper mAdatper;

    private List<Map<String,String>> mData = new ArrayList<>();



    private String mCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initExtraData();
        initActivity("",2,0,0,R.layout.a_course_introduce);

        initUI();
        setLoad();
    }

    private void initExtraData() {
        Intent intent = getIntent();
        if(intent == null){
            return;
        }
        mCode = intent.getStringExtra(EXTRA_CEODE);
        mCode = "123";
    }

    private void initUI() {
        initTitle();
        mRvListView = findViewById(R.id.rv_list_view);
        mAdatper = new CourseIntroductionAdatper(this,mData);
        mRvListView.setAdapter(mAdatper);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        mCourseIntroduceHeader = new CourseIntroduceHeader(this);
        mCourseIntroduceHeader.setLayoutParams(params);
        mRvListView.addHeaderView(mCourseIntroduceHeader);

        mChefIntroductionView = new ChefIntroductionView(this);
        mChefIntroductionView.setLayoutParams(params);
        mRvListView.addHeaderView(mChefIntroductionView);

        mCourseHorizontalView = new CourseHorizontalView(this);
        mCourseHorizontalView.setLayoutParams(params);
        mRvListView.addHeaderView(mCourseHorizontalView);

        mCourseVerticalView = new CourseVerticalView(this);
        mCourseVerticalView.setLayoutParams(params);
        mRvListView.addFooterView(mCourseVerticalView);
    }

    private void initTitle() {
        if (Tools.isShowTitle()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int topbarHeight = Tools.getDimen(this, R.dimen.topbar_height);
            final int statusBarHeight = Tools.getStatusBarHeight(this);
        }
    }

    private void setLoad() {
        loadManager.setLoading(v -> loadCourseTopData());
    }

    private void loadCourseTopData(){
        if(TextUtils.isEmpty(mCode)) {
            Toast.makeText(this,"参数错误",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        CourseDataController.loadCourseTopData(mCode,new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                if(flag >= ReqEncyptInternet.REQ_OK_STRING){
                    //TODO 处理数据
//                    Map<String,String> resultMap = StringManager.getFirstMap(o);
//                    mCourseIntroduceHeader.setData(resultMap);

                }else{

                }
                loadManager.loadOver(flag);
            }
        });
        String data = "";
        Map<String,String> resultMap = StringManager.getFirstMap(data);
        mCourseIntroduceHeader.setData(resultMap);
    }

}
