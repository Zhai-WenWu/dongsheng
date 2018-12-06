package amodule.lesson.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import acore.logic.stat.intefaces.OnClickListenerStat;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import amodule.dish.activity.MenuDish;
import amodule.lesson.adapter.CourseIntroductionAdatper;
import amodule.lesson.controler.data.CourseDataController;
import amodule.lesson.view.ChefIntroductionView;
import amodule.lesson.view.CourseHorizontalView;
import amodule.lesson.view.CourseIntroduceHeader;
import amodule.lesson.view.CourseIntroductionBottomView;
import amodule.lesson.view.CourseVerticalView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import third.share.BarShare;

import static acore.tools.StringManager.API_COURSE_TOP;

/**
 * Description :
 * PackageName : amodule.lesson.activity
 * Created by mrtrying on 2018/12/3 15:26.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseIntroduction extends BaseAppCompatActivity {

    public static final String EXTRA_CEODE = "code";

    private ImageView mShareIcon;
    private CourseIntroduceHeader mCourseIntroduceHeader;
    private ChefIntroductionView mChefIntroductionView;
    private CourseHorizontalView mCourseHorizontalView;
    private CourseVerticalView mCourseVerticalView;
    private CourseIntroductionBottomView mBottomView;
    private RvListView mRvListView;
    private CourseIntroductionAdatper mAdatper;

    private List<Map<String,String>> mData = new ArrayList<>();

    private String mCode;
    private Map<String,String> shareMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化数据
        initExtraData();
        initActivity("",2,0,0,R.layout.a_course_introduce);
        //初始化UI
        initUI();
        //设置加载
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
        mBottomView = findViewById(R.id.course_bottom_layout);
        mRvListView = findViewById(R.id.rv_list_view);
        mAdatper = new CourseIntroductionAdatper(this,mData);
        mRvListView.setAdapter(mAdatper);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        mCourseIntroduceHeader = new CourseIntroduceHeader(this);
        mCourseIntroduceHeader.setLayoutParams(params);
        mRvListView.addHeaderView(mCourseIntroduceHeader);

        mCourseHorizontalView = new CourseHorizontalView(this);
        mCourseHorizontalView.setLayoutParams(params);
        mRvListView.addHeaderView(mCourseHorizontalView);

        mChefIntroductionView = new ChefIntroductionView(this);
        mChefIntroductionView.setLayoutParams(params);
        mRvListView.addHeaderView(mChefIntroductionView);

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

        findViewById(R.id.back).setOnClickListener(getBackBtnAction());
        mShareIcon = findViewById(R.id.share_icon);
        mShareIcon.setOnClickListener(new OnClickListenerStat() {
            @Override
            public void onClicked(View v) {
                doShare();
            }
        });

    }

    private void doShare() {
        barShare = new BarShare(this, "","");
        barShare.setShare(BarShare.IMG_TYPE_WEB, shareMap.get("title"), shareMap.get("content"),
                shareMap.get("img"), shareMap.get("url"));
        barShare.openShare();
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

                }
                Map<String,String> resultMap = StringManager.getFirstMap(o);
                mCourseIntroduceHeader.setData(resultMap);
                shareMap = StringManager.getFirstMap(resultMap.get("shareData"));

                if(shareMap.isEmpty()){

                }
                loadCourseDescData();
                loadCourseListData();
//                loadManager.loadOver(flag);
            }
        });

    }

    private void loadCourseDescData(){
        CourseDataController.loadCourseDescData(mCode, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                if(flag >= ReqEncyptInternet.REQ_OK_STRING){
                    //TODO 处理数据
                    Map<String,String> resultMap = StringManager.getFirstMap(o);
                    long start = System.currentTimeMillis();
                    mChefIntroductionView.setData(StringManager.getFirstMap(resultMap.remove("chefDesc")));
                    Set<Map.Entry<String,String>> entrySet = resultMap.entrySet();
                    for(Map.Entry<String,String> entry:entrySet){
                        Map<String,String> itemData = StringManager.getFirstMap(entry.getValue());
                        mData.add(itemData);
                    }
                    mAdatper.notifyDataSetChanged();
                    Log.i("tzy", "loaded: " + (System.currentTimeMillis() - start));
                }else{

                }
                Log.i("tzy", "loadCourseDescData: ");
            }
        });

    }

    private void loadCourseListData(){
        CourseDataController.loadCourseListData(mCode,"1", new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                if(flag >= ReqEncyptInternet.REQ_OK_STRING){
                    //TODO 处理数据
                    Map<String,String> resultMap = StringManager.getFirstMap(o);
                    mCourseVerticalView.setData(resultMap);
                    mCourseHorizontalView.setData(resultMap);
                }else{

                }
                Log.i("tzy", "loadCourseListData: ");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCourseIntroduceHeader != null){
            mCourseIntroduceHeader.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCourseIntroduceHeader != null){
            mCourseIntroduceHeader.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCourseIntroduceHeader != null){
            mCourseIntroduceHeader.onDestroy();
        }
    }

    @Override
    public void onBackPressed() {
        if(mCourseIntroduceHeader != null && mCourseIntroduceHeader.onBackPressed()){
            return;
        }
        super.onBackPressed();
    }
}
