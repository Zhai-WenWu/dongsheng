package amodule.lesson.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.xiangha.R;

import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.lesson.controler.data.LessonInfoDataMananger;
import amodule.lesson.controler.view.LessonInfoUIMananger;
import amodule.main.Main;

import static acore.tools.ObserverManager.NOTIFY_LESSON_VIPBUTTON;
import static acore.tools.ObserverManager.NOTIFY_VIPSTATE_CHANGED;
import static amodule.lesson.controler.data.LessonInfoDataMananger.DATA_TYPE_COMMENT;
import static amodule.lesson.controler.data.LessonInfoDataMananger.DATA_TYPE_GUESS_LIKE;
import static amodule.lesson.controler.data.LessonInfoDataMananger.DATA_TYPE_LESSON_CONTENT;
import static amodule.lesson.controler.data.LessonInfoDataMananger.DATA_TYPE_LESSON_INFO;
import static amodule.lesson.controler.data.LessonInfoDataMananger.loadVipButtonData;

public class LessonInfo extends BaseAppCompatActivity implements IObserver {

    public static final String TAG = "tzy";

    public static int startCount = 0;

    public static final String STATISTICS_ID_VIP = "vip_middlepage";
    public static final String STATISTICS_ID_NONVIP = "nonvip_middlepage";

    public static final String EXTRA_CODE = "code";
    public static final String EXTRA_INFO_JSON = "extraJson";

    private LessonInfoUIMananger mUIMananger;
    private LessonInfoDataMananger mDataMananger;

    private boolean isOpenVip = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startCount++;
        initActivity("", 7, 0, 0, R.layout.a_lesson_info_layout);
        initialize();
        setPreData();
        ObserverManager.getInstance().registerObserver(this, NOTIFY_VIPSTATE_CHANGED,NOTIFY_LESSON_VIPBUTTON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isOpenVip && Main.colse_level != 7) {
            refresh();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstance().unRegisterObserver(this);
    }

    @Override
    public void finish() {
        startCount--;
        if(startCount <= 1){
            Main.colse_level = 1000;
        }
        super.finish();
    }

    /** 处理外带数据 */
    private void setPreData() {
        Intent intent = getIntent();
        String extraJsonValue = intent.getStringExtra(EXTRA_INFO_JSON);
        if(!TextUtils.isEmpty(extraJsonValue)){
            loadManager.hideProgressBar();
            Map<String, String> preData = StringManager.getFirstMap(intent.getStringExtra(EXTRA_INFO_JSON));
            if(preData != null && !preData.isEmpty()){
                preData.put("isFake","2");
            }
            mUIMananger.setHeaderData(preData);
        }
    }

    private void initialize() {
        initializeUI();
        initializeData();

        loadManager.setLoading(
                mUIMananger.getRvListView(),
                mDataMananger.getAdapter(),
                false,
                v -> loadData()
        );
        mDataMananger.notifyDataSetChanged();

    }

    private void initializeUI() {
        mUIMananger = new LessonInfoUIMananger(this);
    }

    private void initializeData() {
        Intent intent = getIntent();
        String lessonCode = intent.getStringExtra(EXTRA_CODE);
        mDataMananger = new LessonInfoDataMananger(this, lessonCode);
        mDataMananger.setOnLoadedDataCallback((dataType, dataValue) -> {
            final Map<String, String> data = StringManager.getFirstMap(dataValue);
            switch (dataType) {
                case DATA_TYPE_LESSON_INFO:
                    loadSuccess();
                    mUIMananger.setHeaderData(data);
                    break;
                case DATA_TYPE_COMMENT:
                    mUIMananger.setHaFriendCommentData(data);
                    break;
                case DATA_TYPE_LESSON_CONTENT:
                    mUIMananger.setLessonContentData(data);
                    break;
                case DATA_TYPE_GUESS_LIKE:
                    mUIMananger.setGuessYouLikeData(data);
                    break;
//                case DATA_TYPE_VIP_BUTTON:
//                    mUIMananger.setVipButton(data);
//                    break;
                default:
                    break;
            }
        });
        mDataMananger.setOnLoadFailedCallback(dataType -> {
            switch (dataType) {
                case DATA_TYPE_LESSON_INFO:
                    loadFialed();
                    break;
                default:
                    break;
            }
        });
    }

    private void loadFialed() {
        Log.i(TAG, "loadFialed: ");
        mUIMananger.setRvListViewVisibility(View.GONE);
        loadManager.showLoadFaildBar();
        loadManager.hideProgressBar();
    }

    private void loadSuccess() {
        Log.i(TAG, "loadSuccess: ");
        mUIMananger.setRvListViewVisibility(View.VISIBLE);
        loadManager.hideLoadFaildBar();
        loadManager.hideProgressBar();
    }

    /** 加载数据 */
    private void loadData() {
        if(!ToolsDevice.isNetworkAvailable(this)){
            Toast.makeText(this, "请检查网络设置", Toast.LENGTH_SHORT).show();
            loadFialed();
            return;
        }
        mDataMananger.loadData();
    }

    /***/
    private void refresh() {
        mDataMananger.refresh();
        mUIMananger.refresh();
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        switch (name) {
            case NOTIFY_VIPSTATE_CHANGED:
                isOpenVip = LoginManager.isVIP();
                loadVipButtonData();
                break;
            case NOTIFY_LESSON_VIPBUTTON:
                if(mUIMananger != null){
                    mUIMananger.setVipButton(StringManager.getFirstMap(data));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (isOpenVip && startCount >= 7) {
            Main.colse_level = 7;
        }
        XHClick.mapStat(this,getStatisticsId(),"返回按钮","");
        Log.d("tzy", "onBackPressed: ");
        super.onBackPressed();
    }

    public String getStatisticsId(){
        return LoginManager.isVIP()?STATISTICS_ID_VIP:STATISTICS_ID_NONVIP;
    }

}
