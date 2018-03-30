package amodule.lesson.controler.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseAppCompatActivity;
import amodule.lesson.adapter.LessonInfoAdapter;

/**
 * Description :
 * PackageName : amodule.vip.controller.data
 * Created by tanze on 2018/3/29 17:10.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonInfoDataMananger {

    public static final String DATA_TYPE_LESSON_INFO = "lessonInfo";
    public static final String DATA_TYPE_TEACHER = "teacher";
    public static final String DATA_TYPE_LESSON_INTROCTION = "lessonIntroction";
    public static final String DATA_TYPE_COMMENT = "comment";
    public static final String DATA_TYPE_LESSON_CONTENT = "lessonContent";
    public static final String DATA_TYPE_GUESS_LIKE = "guessLike";

    private final BaseAppCompatActivity mActivity;

    private LessonInfoAdapter mAdapter;
    private List<Map<String,String>> mData = new ArrayList<>();
    private OnLoadedDataCallback mOnLoadedDataCallback;

    public LessonInfoDataMananger(BaseAppCompatActivity activity) {
        mActivity = activity;
        mAdapter = new LessonInfoAdapter(activity,mData);
    }


    /**加载数据*/
    public void loadData(){
        loadTopInfo();
        loadVipButtonData();
    }

    private void loadTopInfo() {

    }

    private void laodOtherData(){

    }

    private void loadTeacherIntroductoinData(){

    }

    private void loadLessonIntroductionData(){

    }

    private void loadHaFriendCommentData(){}
    private void loadLessonContentData(){}
    private void loadGuessYouLike(){}
    private void loadVipButtonData(){}

    public void refresh(){}

    public LessonInfoAdapter getAdapter() {
        return mAdapter;
    }

    public void handlerLoadedDataCallback(String dataType, String data){
        if(mOnLoadedDataCallback != null){
            mOnLoadedDataCallback.onLoadedData(dataType,data);
        }
    }

    public interface OnLoadedDataCallback{
        void onLoadedData(String dataType,String data);
    }

    public void setOnLoadedDataCallback(OnLoadedDataCallback onLoadedDataCallback) {
        mOnLoadedDataCallback = onLoadedDataCallback;
    }
}
