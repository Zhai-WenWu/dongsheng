package amodule.lesson.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.xiangha.R;

import java.util.HashMap;
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

public class LessonInfo extends BaseAppCompatActivity implements IObserver {

    private LessonInfoUIMananger mUIMananger;
    private LessonInfoDataMananger mDataMananger;

    private boolean isOpenVip = false;

    //TODO test
    public static void startActivity(@NonNull Context context){
        context.startActivity(new Intent(context,LessonInfo.class));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 5, 0, 0, R.layout.a_lesson_info_layout);
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
        Map<String, String> preData = new HashMap<>();
        //TODO 处理数据
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
    }

    private void initializeUI() {
        mUIMananger = new LessonInfoUIMananger(this);
    }

    private void initializeData() {
        mDataMananger = new LessonInfoDataMananger(this);
        mDataMananger.setOnLoadedDataCallback((dataType, dataValue) -> {
            final Map<String, String> data = StringManager.getFirstMap(dataValue);
            switch (dataType) {
                case LessonInfoDataMananger.DATA_TYPE_LESSON_INFO:
                    mUIMananger.setHeaderData(data);
                    loadManager.hideProgressBar();
                    break;
                case LessonInfoDataMananger.DATA_TYPE_COMMENT:
                    mUIMananger.setHaFriendCommentData(data);
                    break;
                case LessonInfoDataMananger.DATA_TYPE_LESSON_CONTENT:
                    mUIMananger.setLessonContentData(data);
                    break;
                case LessonInfoDataMananger.DATA_TYPE_GUESS_LIKE:
                    mUIMananger.setGuessYouLikeData(data);
                    break;
                default:
                    break;
            }
        });
    }

    /** 加载数据 */
    private void loadData() {
        mDataMananger.loadData();
    }

    /***/
    private void refresh() {
        loadData();
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
        super.onBackPressed();
    }
}
