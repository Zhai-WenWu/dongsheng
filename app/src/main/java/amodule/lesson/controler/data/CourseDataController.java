package amodule.lesson.controler.data;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.LinkedHashMap;

import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

import static acore.tools.StringManager.API_LESSON_INFO;
import static acore.tools.StringManager.API_COURSE_DESC;
import static acore.tools.StringManager.API_COURSE_TOP;
import static acore.tools.StringManager.API_LESSON_POINT;
import static acore.tools.StringManager.API_SYLLABUS;

/**
 * Description :
 * PackageName : amodule.lesson.controler.data
 * Created by mrtrying on 2018/12/5 11:46.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseDataController {

    private CourseDataController() {
        throw new IllegalArgumentException("You can't invoke this method.");
    }

    /**
     * @param code
     * @param callback
     */
    public static void loadCourseTopData(String code, @NonNull InternetCallback callback) {
        if (TextUtils.isEmpty(code)) {
            callback.loaded(ReqEncyptInternet.REQ_FAILD, API_COURSE_TOP, "");
            return;
        }
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("code", code);
        ReqEncyptInternet.in().doGetEncypt(API_COURSE_TOP, params, callback);
    }

    /**
     * @param code
     * @param callback
     */
    public static void loadCourseDescData(String code, @NonNull InternetCallback callback) {
        if (TextUtils.isEmpty(code)) {
            callback.loaded(ReqEncyptInternet.REQ_FAILD, API_COURSE_DESC, "");
            return;
        }
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("code", code);
        ReqEncyptInternet.in().doGetEncypt(API_COURSE_DESC, params, callback);
    }

    /**
     * @param code
     * @param callback
     */
    public static void loadLessonInfoData(String chapterCode,String code, @NonNull InternetCallback callback){
        if(TextUtils.isEmpty(code)){
            callback.loaded(ReqEncyptInternet.REQ_FAILD, API_LESSON_INFO,"");
            return;
        }
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("chapterCode",code);
        params.put("code",code);
        ReqEncyptInternet.in().doGetEncypt(API_LESSON_INFO,params,callback);
    }

    /**
     * @param code
     * @param callback
     */
    public static void loadLessonPointData(String code, @NonNull InternetCallback callback){
        if(TextUtils.isEmpty(code)){
            callback.loaded(ReqEncyptInternet.REQ_FAILD, API_LESSON_POINT,"");
            return;
        }
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("code",code);
        ReqEncyptInternet.in().doGetEncypt(API_LESSON_POINT,params,callback);
    }

    /**
     * @param code
     * @param type
     * @param callback
     */
    public static void loadCourseListData(String code, String type, @NonNull InternetCallback callback) {
        if (TextUtils.isEmpty(code) || TextUtils.isEmpty(type)) {
            callback.loaded(ReqEncyptInternet.REQ_FAILD, API_SYLLABUS, "");
            return;
        }
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("code", code);
        params.put("type", type);
        ReqEncyptInternet.in().doGetEncypt(API_SYLLABUS, params, callback);
    }
}
