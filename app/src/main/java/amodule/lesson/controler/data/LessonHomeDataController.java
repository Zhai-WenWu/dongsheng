package amodule.lesson.controler.data;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Description :
 * PackageName : amodule.lesson.controler.data
 * Created by mrtrying on 2017/12/19 11:23:19.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonHomeDataController {

    private Activity mActivity;

    private ArrayList<Map<String, String>> mData = new ArrayList<>();

    private int mCurrentPage = 0;

    private OnLoadDataCallback mOnLoadDataCallback = new DefaultOnLoadDataCallback();

    private NotifyDataSetChangedCallback mNotifyDataSetChangedCallback;

    private HasDataCallback mNoDataCallback;

    public LessonHomeDataController(Activity activity) {
        this.mActivity = activity;
    }

    public void laodRemoteHeaderData(@Nullable InternetCallback callback) {
        String url = StringManager.API_SCHOOL_HOME;
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        ReqEncyptInternet.in().doEncypt(url, params, callback);
    }

    public void laodRemoeteExtraData(boolean refresh) {
        if(refresh)
            mCurrentPage = 0;
        mOnLoadDataCallback.onPrepare(refresh);
        mCurrentPage++;
        String url = StringManager.API_SCHOOL_COURSELIST;
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("page",String.valueOf(mCurrentPage));
        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback(mActivity) {
            @Override
            public void loaded(int i, String s, Object o) {
                int loadCount = 0;
                if (i >= ReqEncyptInternet.REQ_OK_STRING) {
                    mOnLoadDataCallback.onSuccess(refresh);
                    List<Map<String, String>> tempData = StringManager.getListMapByJson(o);
                    if(refresh)
                        mData.clear();
                    Stream.of(tempData).forEach(map -> {
                        Map<String,String> widgetMap = StringManager.getFirstMap(map.get("widgetData"));
                        mData.add(widgetMap);
                    });
                    loadCount = tempData.size();
                    notifyDataSetChanged();
                    if(null != mNoDataCallback){
                        mNoDataCallback.hasData(mData.isEmpty());
                    }
                } else {
                    mOnLoadDataCallback.onFailed(refresh);
                }
                mOnLoadDataCallback.onAfter(refresh, i, loadCount);
            }
        });
    }

    private void notifyDataSetChanged(){
        if(null != mNotifyDataSetChangedCallback){
            mNotifyDataSetChangedCallback.notifyDataSetChanged();
        }
    }

    public ArrayList<Map<String, String>> getData() {
        return mData;
    }

    public OnLoadDataCallback getOnLoadDataCallback() {
        return mOnLoadDataCallback;
    }

    public void setOnLoadDataCallback(OnLoadDataCallback onLoadDataCallback) {
        if (null == onLoadDataCallback) return;
        mOnLoadDataCallback = onLoadDataCallback;
    }

    class DefaultOnLoadDataCallback implements OnLoadDataCallback {

        @Override
        public void onPrepare(boolean refersh) {

        }

        @Override
        public void onAfter(boolean refersh, int flag, int loadCount) {

        }

        @Override
        public void onSuccess(boolean refersh) {

        }

        @Override
        public void onFailed(boolean refersh) {

        }
    }

    public void setNotifyDataSetChangedCallback(NotifyDataSetChangedCallback notifyDataSetChangedCallback) {
        mNotifyDataSetChangedCallback = notifyDataSetChangedCallback;
    }

    public void setNoDataCallback(HasDataCallback noDataCallback) {
        mNoDataCallback = noDataCallback;
    }

    public interface OnLoadDataCallback {
        void onPrepare(boolean refersh);

        void onAfter(boolean refersh, int flag, int loadCount);

        void onSuccess(boolean refersh);

        void onFailed(boolean refersh);
    }

    public interface NotifyDataSetChangedCallback {
        void notifyDataSetChanged();
    }

    public interface HasDataCallback {
        void hasData(boolean hasData);
    }
}
