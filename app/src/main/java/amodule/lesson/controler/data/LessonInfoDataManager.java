package amodule.lesson.controler.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import amodule.lesson.activity.LessonInfo;
import amodule.lesson.adapter.LessonInfoAdapter;
import amodule.lesson.view.info.ItemImage;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

import static acore.tools.ObserverManager.NOTIFY_LESSON_VIPBUTTON;
import static acore.tools.StringManager.API_SCHOOL_COMMENTCHAPTERS;
import static acore.tools.StringManager.API_SCHOOL_COMMENTINFO;
import static acore.tools.StringManager.API_SCHOOL_COURSEINTRODUCEINFO;
import static acore.tools.StringManager.API_SCHOOL_INFO_VIPBUTTON;
import static acore.tools.StringManager.API_SCHOOL_LIKELIST;
import static acore.tools.StringManager.API_SCHOOL_TOPINFO;
import static amodule.lesson.adapter.LessonInfoAdapter.KEY_VIEW_TYPE;
import static amodule.lesson.adapter.LessonInfoAdapter.VIEW_TYPE_IMAGE;
import static amodule.lesson.adapter.LessonInfoAdapter.VIEW_TYPE_TITLE;

/**
 * Description :
 * PackageName : amodule.vip.controller.data
 * Created by tanze on 2018/3/29 17:10.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonInfoDataManager {
    public static final String TAG = "tzy";

    private static final String DATA_TYPE_LESSON_INTRODUCTION = "lessonIntroduction";
    public static final String DATA_TYPE_LESSON_INFO = "lessonInfo";
    public static final String DATA_TYPE_COMMENT = "comment";
    public static final String DATA_TYPE_LESSON_CONTENT = "lessonContent";
    public static final String DATA_TYPE_GUESS_LIKE = "guessLike";

    private final LessonInfo mActivity;
    private final String lessonCode;

    private LessonInfoAdapter mAdapter;
    private List<Map<String, String>> mData = new ArrayList<>();
    private Map<String, List<Map<String, String>>> mImagesArray = new HashMap<>();
    private OnLoadedDataCallback mOnLoadedDataCallback;
    private OnLoadFailedCallback mOnLoadFailedCallback;

    public LessonInfoDataManager(LessonInfo activity, String lessonCode) {
        mActivity = activity;
        this.lessonCode = lessonCode;
        initializeAdapter(activity);
    }

    private void initializeAdapter(BaseAppCompatActivity activity) {
        mAdapter = new LessonInfoAdapter(activity, mData);
        mAdapter.setMoreCallbcak((String type) -> {
            XHClick.mapStat(mActivity,mActivity.getStatisticsId(),"点击查看更多",type);
            replaceImgsData(type);
        });
        mAdapter.setShowMoreCallback(type -> XHClick.mapStat(mActivity,mActivity.getStatisticsId(),"显示查看更多",type));
    }

    private void replaceImgsData(String type) {
        if (TextUtils.isEmpty(type)) {
            return;
        }
        List<Map<String, String>> imgs = mImagesArray.get(type);
        if (imgs == null || imgs.isEmpty()) {
            return;
        }
        //替换图片，刷新UI
        for (int i = 0; i < mData.size(); i++) {
            Map<String, String> data = mData.get(i);
            if (data.containsKey("end")
                    && type.equals(data.get("end"))) {
                mData.set(i,imgs.remove(0));
                mData.addAll(i+1, imgs);
                notifyDataSetChanged();
                mImagesArray.remove(type);
                return;
            }
        }
    }

    /** 刷新 */
    public void refresh() {
        mData.clear();
        notifyDataSetChanged();
        mImagesArray.clear();
        loadTopInfo();
    }

    /** 加载数据 */
    public void loadData() {
        loadTopInfo();
        loadVipButtonData();
    }

    private static boolean isLoadingVipButtonData = false;
    /**请求VIP Button数据*/
    public static synchronized void loadVipButtonData() {
        //避免同事发出多次请求
        if(isLoadingVipButtonData){
            return;
        }
        isLoadingVipButtonData = true;
        //发起请求
        ReqEncyptInternet.in().doEncypt(API_SCHOOL_INFO_VIPBUTTON,new LinkedHashMap<>(), new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                isLoadingVipButtonData = false;
                if (i >= ReqInternet.REQ_OK_STRING) {
                    ObserverManager.getInstance().notify(NOTIFY_LESSON_VIPBUTTON,null,o);
                }
            }
        });
    }

    //获取top数据
    private void loadTopInfo() {
        handlerRequest(DATA_TYPE_LESSON_INFO);
    }

    //获取其他数据
    private void loadOtherData() {
        loadLessonIntroductionData();
        loadHaFriendCommentData();
        loadLessonContentData();
        loadGuessYouLike();
    }

    //获取课程介绍数据
    private void loadLessonIntroductionData() {
        handlerRequest(DATA_TYPE_LESSON_INTRODUCTION);
    }

    //获取哈友评价数据
    private void loadHaFriendCommentData() {
        handlerRequest(DATA_TYPE_COMMENT);
    }

    //获取课程内容数据
    private void loadLessonContentData() {
        handlerRequest(DATA_TYPE_LESSON_CONTENT);
    }

    //获取猜你喜欢内容
    private void loadGuessYouLike() {
        handlerRequest(DATA_TYPE_GUESS_LIKE);
    }

    /**
     * 处理请求
     * @param dataType 请求类型
     */
    private void handlerRequest(final String dataType) {
        if (TextUtils.isEmpty(dataType)) {
            return;
        }
        String apiUrl;
        switch (dataType) {
            case DATA_TYPE_LESSON_INFO:
                apiUrl = API_SCHOOL_TOPINFO;
                break;
            //内部处理数据
            case DATA_TYPE_LESSON_INTRODUCTION:
                apiUrl = API_SCHOOL_COURSEINTRODUCEINFO;
                break;
            case DATA_TYPE_COMMENT:
                apiUrl = API_SCHOOL_COMMENTINFO;
                break;
            case DATA_TYPE_LESSON_CONTENT:
                apiUrl = API_SCHOOL_COMMENTCHAPTERS;
                break;
            case DATA_TYPE_GUESS_LIKE:
                apiUrl = API_SCHOOL_LIKELIST;
                break;
            default:
                return;
        }
        String params = "courseCode="+lessonCode;
        //发起请求
        ReqEncyptInternet.in().doEncypt(apiUrl,params, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    handInnerLoadedDataCallback(dataType, (String) o);
                } else {
                    handlerLoadFailedCallback(dataType);
                }
            }
        });
    }

    /**
     * 内部处理请求回调
     * @param dataType 请求类型
     * @param data 数据
     */
    private void handInnerLoadedDataCallback(String dataType, String data) {
        switch (dataType) {
            case DATA_TYPE_LESSON_INFO:
                //特殊处理
                loadOtherData();
                handlerLoadedDataCallback(dataType, data);
                break;
            case DATA_TYPE_LESSON_INTRODUCTION:
                //内部处理
                analysisDatas(data);
                break;
            default:
                handlerLoadedDataCallback(dataType, data);
                break;
        }
    }

    /**
     * 解析课程介绍数据
     * @param data 数据
     */
    private void analysisDatas(String data) {
        if (TextUtils.isEmpty(data)) {
            return;
        }

        List<Map<String, String>> moduleDatas = StringManager.getListMapByJson(data);

        for (int i = 0; i < moduleDatas.size(); i++) {

            Map<String, String> moduleData = moduleDatas.get(i);
            //额外数据
            Map<String, String> extraData = StringManager.getFirstMap(moduleData.get("extraData"));
            //列表
            Map<String, String> widgetData = StringManager.getFirstMap(moduleData.get("widgetData"));
            if(!widgetData.isEmpty()){
                final String type = widgetData.get("text1");
                //添加title
                mData.add(handlerTitleData(type, widgetData.get("text1"), extraData.get("top")));

                //暂存
                List<Map<String, String>> imgs = StringManager.getListMapByJson(widgetData.get("imgs"));
                List<Map<String, String>> imageArray = new ArrayList<>();
                Stream.of(imgs)
                        .filter(value -> !TextUtils.isEmpty(value.get("")))
                        .forEach(value -> imageArray.add(handlerImageData(value.get(""))));
                if (!imageArray.isEmpty() && TextUtils.isEmpty(extraData.get("bottom"))) {
                    imageArray.get(imageArray.size() - 1).put("bottom", extraData.get("bottom"));
                    imageArray.get(imageArray.size() - 1).put("isEnd", "2");
                }
                mImagesArray.put(String.valueOf(i), imageArray);
                //添加第一张图片
                if(imageArray.size() < 2){
                    Map<String, String> imageMap = handlerImageData("", widgetData.get("defaultImg"), "", extraData.get("bottom"));
                    imageMap.put("isEnd", "2");
                    mData.add(imageMap);
                }else {
                    String text2 = widgetData.get("text2");
                    mData.add(handlerImageData(type, widgetData.get("defaultImg"), text2, extraData.get("bottom")));
                }
            }
        }
        //刷新UI
        notifyDataSetChanged();
    }

    /**
     * 处理课程介绍title数据
     * @param type
     * @param title
     * @param extraData
     * @return
     */
    private Map<String, String> handlerTitleData(String type, String title, String extraData) {
        Map<String, String> data = new HashMap<>();
        data.put(KEY_VIEW_TYPE, String.valueOf(VIEW_TYPE_TITLE));
        data.put("text1", title);
        if (!TextUtils.isEmpty(type)) {
            data.put("start", type);
        }
        if (!TextUtils.isEmpty(extraData)) {
            data.put("top", extraData);
        }
        return data;
    }

    /**
     * 处理课程介绍图片数据
     * @param imgUrl
     * @return
     */
    private Map<String, String> handlerImageData(String imgUrl) {
        return handlerImageData("",imgUrl,"","");
    }

    /**
     *
     * @param type
     * @param imgUrl
     * @param text2
     * @param extraData
     * @return
     */
    private Map<String, String> handlerImageData(String type, String imgUrl, @Nullable String text2, String extraData) {
        Map<String, String> data = new HashMap<>();
        data.put(KEY_VIEW_TYPE, String.valueOf(VIEW_TYPE_IMAGE));
        data.put("img", imgUrl);
        if (!TextUtils.isEmpty(type)) {
            data.put("end", type);
            data.put("isEnd", "2");
        }
        if (!TextUtils.isEmpty(text2)) {
            data.put("text2", text2);
        }
        if (!TextUtils.isEmpty(extraData)) {
            data.put("bottom", extraData);
        }
        return data;
    }

    private void handlerLoadedDataCallback(String dataType, String data) {
        if (mOnLoadedDataCallback != null) {
            mOnLoadedDataCallback.onLoadedData(dataType, data);
        }
    }

    private void handlerLoadFailedCallback(@NonNull String dataType){
        if(mOnLoadFailedCallback != null){
            mOnLoadFailedCallback.onLoadFailed(dataType);
        }
    }

    public LessonInfoAdapter getAdapter() {
        return mAdapter;
    }

    public void notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void setOnLoadedDataCallback(OnLoadedDataCallback onLoadedDataCallback) {
        mOnLoadedDataCallback = onLoadedDataCallback;
    }

    public void setOnLoadFailedCallback(OnLoadFailedCallback onLoadFailedCallback) {
        mOnLoadFailedCallback = onLoadFailedCallback;
    }

    public interface OnLoadedDataCallback {
        void onLoadedData(String dataType, String data);
    }

    public interface OnLoadFailedCallback{
        void onLoadFailed(String dataType);
    }
}
