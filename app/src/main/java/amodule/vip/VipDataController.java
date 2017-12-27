package amodule.vip;

import java.util.LinkedHashMap;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.StringManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Description :
 * PackageName : amodule.vip
 * Created by mrtrying on 2017/12/19 17:02:00.
 * e_mail : ztanzeyu@gmail.com
 */
public class VipDataController {

    public static final String KEY_BTN_SHOW = "vipButtonIsShow";
    public static final String KEY_BTN_DATA = "vipButton";

    private boolean mVipBtnShow;
    private String mTextColor;
    private String mBgColor;
    private String mTitle;
    private String mUrl;
    private String mSourcePage = "";

    private Runnable mRun;

    private boolean mNeedRef;

    public void loadVIPButtonData(){
        String url = StringManager.API_SCHOOL_VIPBUTTON;
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("sourcePage", mSourcePage);
        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                if(i>=ReqEncyptInternet.REQ_OK_STRING){
                    Map<String, String> data = StringManager.getFirstMap(o);
                    mVipBtnShow = "2".equals(data.get(KEY_BTN_SHOW));
                    Map<String, String> buttonData = StringManager.getFirstMap(data.get(KEY_BTN_DATA));
                    mTitle = buttonData.get("title");
                    mTextColor = buttonData.get("color");
                    mBgColor = buttonData.get("bgColor");
                    mUrl = buttonData.get("url");
                    if (mRun != null)
                        mRun.run();
                }
            }
        });
    }

    public void setDataCallback(Runnable run) {
        mRun = run;
    }

    public boolean isVipBtnShow() {
        return mVipBtnShow;
    }

    public String getTextColor() {
        return mTextColor;
    }

    public String getBgColor() {
        return mBgColor;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public Runnable getRun() {
        return mRun;
    }

    public void setSourcePage(String sourcePage) {
        mSourcePage = sourcePage;
    }

    public void setNeedRefresh(boolean refresh) {
        mNeedRef = refresh;
    }

    public void onResume() {
        if (mNeedRef) {
            mNeedRef = false;
            loadVIPButtonData();
        }
    }

    public void onDestroy() {
        mRun = null;
        mNeedRef = false;
        mVipBtnShow = false;
    }
}
