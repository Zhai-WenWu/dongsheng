package amodule.lesson.controler.data;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Predicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import amodule.lesson.adapter.LessonInfoAdapter;
import amodule.lesson.view.info.ItemImage;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.basic.XHInternetCallBack;

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
public class LessonInfoDataMananger {
    public static final String TAG = "tzy";

    public static final String DATA_TYPE_LESSON_INFO = "lessonInfo";
    public static final String DATA_TYPE_LESSON_INTROCTION = "lessonIntroction";
    public static final String DATA_TYPE_COMMENT = "comment";
    public static final String DATA_TYPE_LESSON_CONTENT = "lessonContent";
    public static final String DATA_TYPE_GUESS_LIKE = "guessLike";

    public static final String DATA_TYPE_VIP_BUTTON = "vipButton";

    private final BaseAppCompatActivity mActivity;

    private LessonInfoAdapter mAdapter;
    private List<Map<String, String>> mData = new ArrayList<>();
    private Map<String, List<Map<String, String>>> mImgsArray = new HashMap<>();
    private OnLoadedDataCallback mOnLoadedDataCallback;

    public LessonInfoDataMananger(BaseAppCompatActivity activity) {
        mActivity = activity;
        mAdapter = new LessonInfoAdapter(activity, mData);
        mAdapter.setMoreCallbcak(new ItemImage.OnClickMoreCallbcak() {
            @Override
            public void onClickMore(String type) {
                if (TextUtils.isEmpty(type)) {
                    return;
                }
                List<Map<String, String>> imgs = mImgsArray.get(type);
                if (imgs == null || imgs.isEmpty()) {
                    return;
                }
                for (int i = 0; i < mData.size(); i++) {
                    Map<String, String> data = mData.get(i);
                    if (data.containsKey("end")
                            && type.equals(data.get("end"))) {
                        mData.remove(i);
                        mData.addAll((i + 1), imgs);
                        notifyDataSetChanged();
                        return;
                    }
                }
            }
        });
    }

    /** 刷新 */
    public void refresh() {
        loadData();
    }

    /** 加载数据 */
    public void loadData() {
        loadTopInfo();
        loadVipButtonData();
    }

    private void loadVipButtonData() {
        handlerRequest(DATA_TYPE_VIP_BUTTON);
    }

    private void loadTopInfo() {
        handlerRequest(DATA_TYPE_LESSON_INFO);
    }

    private void loadOtherData() {
        loadLessonIntroductionData();
        loadHaFriendCommentData();
        loadLessonContentData();
        loadGuessYouLike();
    }

    private void loadLessonIntroductionData() {
        handlerRequest(DATA_TYPE_LESSON_INTROCTION);
    }

    private void loadHaFriendCommentData() {
        handlerRequest(DATA_TYPE_COMMENT);
    }

    private void loadLessonContentData() {
        handlerRequest(DATA_TYPE_LESSON_CONTENT);
    }

    private void loadGuessYouLike() {
        handlerRequest(DATA_TYPE_GUESS_LIKE);
    }

    private void handlerRequest(final String dataType) {
        if (TextUtils.isEmpty(dataType)) {
            return;
        }
        String apiUrl = null;
        switch (dataType) {
            case DATA_TYPE_LESSON_INFO:
                apiUrl = API_SCHOOL_TOPINFO;
                break;
            //内部处理数据
            case DATA_TYPE_LESSON_INTROCTION:
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
            case DATA_TYPE_VIP_BUTTON:
                apiUrl = API_SCHOOL_INFO_VIPBUTTON;
                break;
            default:
                return;
        }
        apiUrl += "?courseCode=70&debug=4d5c01842f37d90651f9693783c6564279fed6f4&isDelCache=false";
        //发起请求
        ReqInternet.in().doGet(apiUrl, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    handInnerLoadedDataCallback(dataType, (String) o);
                } else {
                    //TODO 处理错误的情况
                }
            }
        });
    }

    private void handInnerLoadedDataCallback(String dataType, String data) {
        switch (dataType) {
            case DATA_TYPE_LESSON_INFO:
                //特殊处理
                loadOtherData();
                handlerLoadedDataCallback(dataType, data);
                break;
            case DATA_TYPE_LESSON_INTROCTION:
                //内部处理
                analysisDatas(data);
                break;
            default:
                handlerLoadedDataCallback(dataType, data);
                break;
        }
    }

    private void analysisDatas(String data) {
        if (TextUtils.isEmpty(data)) {
            return;
        }
        Map<String, String> map = StringManager.getFirstMap(data);
        List<Map<String, String>> moduleDatas = StringManager.getListMapByJson(map.get("list"));
        for (int i = 0; i < moduleDatas.size(); i++) {
            final String type = String.valueOf(i);
            Map<String, String> moduleData = moduleDatas.get(i);
            //额外数据
            Map<String, String> extraData = StringManager.getFirstMap(moduleData.get("extraData"));
            //列表
            Map<String, String> widgetData = StringManager.getFirstMap(moduleData.get("widgetData"));
            //添加title
            mData.add(handlerTitleData(type, widgetData.get("text1"), extraData.get("top")));
            //添加第一张图片
            mData.add(handlerImageData(type, widgetData.get("defaultImg"), widgetData.get("text2"), extraData.get("bottom")));
            //暂存
            List<Map<String, String>> imgs = StringManager.getListMapByJson(widgetData.get(""));
            List<Map<String, String>> imageArray = new ArrayList<>();
            Stream.of(imgs)
                    .filter(value -> !TextUtils.isEmpty(value.get("")))
                    .forEach(value -> imageArray.add(handlerImageData("", value.get(""), "", "")));
            if (!imageArray.isEmpty() && TextUtils.isEmpty(extraData.get("bottom"))) {
                imageArray.get(imageArray.size() - 1).put("bottom", extraData.get("bottom"));
            }
            mImgsArray.put(String.valueOf(i), imageArray);
        }
        //刷新UI
        notifyDataSetChanged();
    }

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

    private Map<String, String> handlerImageData(String type, String imgUrl, @Nullable String text2, String extraData) {
        Map<String, String> data = new HashMap<>();
        data.put(KEY_VIEW_TYPE, String.valueOf(VIEW_TYPE_IMAGE));
        data.put("img", imgUrl);
        if (!TextUtils.isEmpty(type)) {
            data.put("end", type);
        }
        if (!TextUtils.isEmpty(text2)) {
            data.put("text2", imgUrl);
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

    public interface OnLoadedDataCallback {
        void onLoadedData(String dataType, String data);
    }
}
