package amodule.lesson.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.xiangha.R;

import java.util.Map;

import acore.logic.LoginManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import amodule.lesson.controler.data.LessonInfoDataMananger;
import amodule.lesson.controler.view.LessonInfoUIMananger;
import amodule.main.Main;

import static acore.tools.ObserverManager.NOTIFY_VIPSTATE_CHANGED;
import static amodule.lesson.controler.data.LessonInfoDataMananger.DATA_TYPE_COMMENT;
import static amodule.lesson.controler.data.LessonInfoDataMananger.DATA_TYPE_GUESS_LIKE;
import static amodule.lesson.controler.data.LessonInfoDataMananger.DATA_TYPE_LESSON_CONTENT;
import static amodule.lesson.controler.data.LessonInfoDataMananger.DATA_TYPE_LESSON_INFO;
import static amodule.lesson.controler.data.LessonInfoDataMananger.DATA_TYPE_VIP_BUTTON;

public class LessonInfo extends BaseAppCompatActivity implements IObserver {

    public static final String TAG = "tzy";
    public static final String EXTRA_CODE = "code";
    public static final String EXTRA_INFO_JSON = "extraInfo";


    private LessonInfoUIMananger mUIMananger;
    private LessonInfoDataMananger mDataMananger;

    private boolean isOpenVip = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 7, 0, 0, R.layout.a_lesson_info_layout);
        initialize();
        setPreData();
        ObserverManager.getInstance().registerObserver(this, NOTIFY_VIPSTATE_CHANGED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isOpenVip) {
            refresh();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstance().unRegisterObserver(this);
    }

    /** 处理外带数据 */
    private void setPreData() {
        Intent intent = getIntent();
        Map<String, String> preData = StringManager.getFirstMap(intent.getStringExtra(EXTRA_INFO_JSON));
        mUIMananger.setHeaderData(preData);
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
                    mUIMananger.setHeaderData(data);
                    loadManager.hideProgressBar();
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
                case DATA_TYPE_VIP_BUTTON:
                    mUIMananger.setVipButton(data);
                    break;
                default:
                    break;
            }
        });
    }

    /** 加载数据 */
    private void loadData() {
        Log.d(TAG, "loadData: ");
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
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (isOpenVip) {
            Main.colse_level = 7;
        }
        Log.d("tzy", "onBackPressed: ");
        super.onBackPressed();
    }
}
