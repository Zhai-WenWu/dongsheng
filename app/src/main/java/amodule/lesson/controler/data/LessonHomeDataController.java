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

    public LessonHomeDataController(Activity activity) {
        this.mActivity = activity;
    }

    public void laodRemoteHeaderData(@Nullable InternetCallback callback) {
        //TODO ceshi
        String url = StringManager.API_HOMEPAGE_6_0;
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        ReqEncyptInternet.in().doEncypt(url, params, callback);
    }

    public void laodRemoeteExtraData(boolean refresh,@Nullable InternetCallback callback) {
        if(refresh)
            mCurrentPage = 0;
        //TODO ceshi
        mOnLoadDataCallback.onPrepare();
        mCurrentPage++;
        String url = StringManager.API_HOMEPAGE_6_0;
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback(mActivity) {
            @Override
            public void loaded(int i, String s, Object o) {
                int loadCount = 0;
                if (i >= ReqEncyptInternet.REQ_OK_STRING) {
                    mOnLoadDataCallback.onSuccess();
                    List<Map<String, String>> tempData = StringManager.getListMapByJson(o);
                    //TODO
                    tempData.remove(0);
                    tempData.remove(0);
                    tempData.remove(0);
                    if(refresh)
                        mData.clear();
                    Stream.of(tempData).forEach(map -> {
                        Map<String,String> widgetMap = StringManager.getFirstMap(map.get("widgetData"));
                        mData.add(widgetMap);
                    });
                    loadCount = tempData.size();
                    notifyDataSetChanged();
                } else {
                    mOnLoadDataCallback.onFailed();
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
        public void onPrepare() {

        }

        @Override
        public void onAfter(boolean refersh, int flag, int loadCount) {

        }

        @Override
        public void onSuccess() {

        }

        @Override
        public void onFailed() {

        }
    }

    public void setNotifyDataSetChangedCallback(NotifyDataSetChangedCallback notifyDataSetChangedCallback) {
        mNotifyDataSetChangedCallback = notifyDataSetChangedCallback;
    }

    public interface OnLoadDataCallback {
        void onPrepare();

        void onAfter(boolean refersh, int flag, int loadCount);

        void onSuccess();

        void onFailed();
    }

    public interface NotifyDataSetChangedCallback {
        void notifyDataSetChanged();
    }
}
