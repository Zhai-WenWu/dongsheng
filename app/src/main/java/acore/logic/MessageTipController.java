package acore.logic;

import android.text.TextUtils;
import android.util.Log;

import java.util.Random;

import acore.logic.load.LoadManager;
import acore.tools.LogManager;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.qiyu.QiYvHelper;

import static acore.tools.ObserverManager.NOTIFY_MESSAGE_REFRESH;

/**
 * Description :
 * PackageName : acore.logic
 * Created by mrtrying on 2018/1/6 10:53:27.
 * e_mail : ztanzeyu@gmail.com
 */
public class MessageTipController {
    private int qiyvMessage = 0;//七鱼新消息条数
    private int quanMessage = 0; // 美食圈新消息条数
    private int feekbackMessage = 0; // 系统新消息条数
    private int myQAMessage = 0;//我的问答新消息条数

    private int fiallNum = 0;

    public boolean hasArbitration;

    private static volatile MessageTipController mInstance = null;

    private long lastRequestTime = -1L;

    private MessageTipController() {
    }

    public static MessageTipController newInstance() {
        if (null == mInstance) {
            synchronized (MessageTipController.class) {
                mInstance = new MessageTipController();
            }
        }
        return mInstance;
    }

    /** 获取公用数据消息 */
    public void getCommonData(final InternetCallback callback) {
        final long currentTime = System.currentTimeMillis();
        if(lastRequestTime < 0 || (currentTime - lastRequestTime > 2000 && fiallNum == 0)){
            lastRequestTime = currentTime;
            String url = StringManager.api_commonData + "?m=commonData";
            ReqInternet.in().doGet(url, new InternetCallback() {
                @Override
                public void loaded(int flag, String url, Object returnObj) {
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        fiallNum = 0;
                        handlerMessageData((String) returnObj);
                        if (callback != null)
                            callback.loaded(flag, url, "加载成功");
                    } else if (fiallNum < 3) {
                        fiallNum++;
                        getCommonData(callback);
                    } else {
                        if (callback != null)
                            callback.loaded(flag, url, "加载失败");
                    }
                }
            });
        }
    }

    private boolean handlerMessageData(String returnObj) {
        if (TextUtils.isEmpty(returnObj)) return false;
        String[] alertArr = returnObj.split("-");
        if (alertArr != null && alertArr.length > 2) {
            quanMessage = Integer.parseInt(alertArr[1]);
            feekbackMessage = Integer.parseInt(alertArr[2]);
            if (alertArr.length >= 5) {
                myQAMessage = Integer.parseInt(alertArr[3]) + Integer.parseInt(alertArr[4]);
                if (alertArr.length >= 6)
                    hasArbitration = "2".equals(alertArr[5]);
            }
            try {
                // 所有消息数
                loadQiyuUnreadCount();
                setMessageCount();
                // tok值
                long tok = Integer.parseInt(alertArr[0]);
                int c = (new Random()).nextInt(9) + 1;
                LoadManager.tok = c + "" + (tok + 54321) * c;
                return true;
            } catch (Exception e) {
                LogManager.reportError("获取新消息", e);
            }
        }
        return false;
    }

    public int getMessageNum() {
        Log.i("tzy", "getMessageNum: quan = " + quanMessage);
        Log.i("tzy", "getMessageNum: feekback = " + feekbackMessage);
        Log.i("tzy", "getMessageNum: QA = " + myQAMessage);
        Log.i("tzy", "getMessageNum: qiyu = " + qiyvMessage);
        return quanMessage + feekbackMessage + myQAMessage + qiyvMessage;
    }

    public void loadQiyuUnreadCount() {
        loadQiyuUnreadCount(count -> {
            setMessageCount();
        });
    }

    public void loadQiyuUnreadCount(QiYvHelper.NumberCallback callback) {
        QiYvHelper.getInstance().getUnreadCount(count -> {
            qiyvMessage = count >= 0 ? count : qiyvMessage;
            if (count >= 0 && callback != null)
                callback.onNumberReady(count);
        });
    }

    public void setMessageCount() {
        ObserverManager.getInstance().notify(NOTIFY_MESSAGE_REFRESH, null, "");
    }

    public void setQiyvMessage(int qiyvMessage) {
        this.qiyvMessage = qiyvMessage;
        setMessageCount();
    }

    public void setQuanMessage(int quanMessage) {
        this.quanMessage = quanMessage;
        setMessageCount();
    }

    public void setFeekbackMessage(int feekbackMessage) {
        this.feekbackMessage = feekbackMessage;
        setMessageCount();
    }

    public void setMyQAMessage(int myQAMessage) {
        this.myQAMessage = myQAMessage;
        setMessageCount();
    }

    public int getQiyvMessage() {
        return qiyvMessage;
    }

    public int getQuanMessage() {
        return quanMessage;
    }

    public int getFeekbackMessage() {
        return feekbackMessage;
    }

    public int getMyQAMessage() {
        return myQAMessage;
    }
}
