package third.ad.tools;

import android.app.Activity;
import android.view.ViewGroup;

import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashAdListener;

import java.util.ArrayList;
import java.util.List;

import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.ad.db.bean.XHSelfNativeData;

/**
 * Description :
 * PackageName : third.ad.tools
 * Created by mrtrying on 2018/3/8 16:33:12.
 * e_mail : ztanzeyu@gmail.com
 */
public class XHSelfAdTools {

    public void loadNativeData(final XHSelfCallback callback) {
        ReqInternet.in().doGet("", new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i > ReqInternet.REQ_OK_STRING) {
                    List<XHSelfNativeData> list = new ArrayList<>();
                    if (callback != null) {
                        callback.onNativeLoad(list);
                    }
                } else {
                    if (callback != null) {
                        callback.onNativeFail();
                    }
                }
            }
        });
    }

    interface XHSelfCallback {
        public void onNativeLoad(List<XHSelfNativeData> list);
        public void onNativeFail();
    }
}
