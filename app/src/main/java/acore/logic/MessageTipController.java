package acore.logic;

import android.text.TextUtils;

import java.util.Random;

import acore.logic.load.LoadManager;
import acore.tools.LogManager;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.qiyu.QiYvHelper;

/**
 * Description :
 * PackageName : acore.logic
 * Created by mrtrying on 2018/1/6 10:53:27.
 * e_mail : ztanzeyu@gmail.com
 */
public class MessageTipController {
    private static int qiyvMessage = 0;//七鱼新消息条数
    private static int quanMessage = 0; // 美食圈新消息条数
    private static int feekbackMessage = 0; // 系统新消息条数
    private static int myQAMessage = 0;//我的问答新消息条数

    private static int fiallNum = 0;

    public static boolean hasArbitration;

    private static volatile MessageTipController mInstance = null;
    private MessageTipController(){}

    public static MessageTipController getInstance(){
        if(null == mInstance){
            synchronized (MessageTipController.class){
                mInstance = new MessageTipController();
            }
        }
        return mInstance;
    }

    /** 获取公用数据消息 */
    public static void getCommonData(final InternetCallback callback) {
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
                } else{
                    if (callback != null)
                        callback.loaded(flag, url, "加载失败");
                }
            }
        });
    }

    private static boolean handlerMessageData(String returnObj) {
        if(TextUtils.isEmpty(returnObj)) return false;
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

    public static int getMessageNum() {
        return quanMessage + feekbackMessage + myQAMessage + qiyvMessage;
    }

    public static void loadQiyuUnreadCount(){
        loadQiyuUnreadCount(count -> {
            setMessageCount();
        });
    }

    public static void loadQiyuUnreadCount(QiYvHelper.NumberCallback callback){
        QiYvHelper.getInstance().getUnreadCount(count -> {
            qiyvMessage = count >= 0 ? count : qiyvMessage;
            if(count >= 0 && callback != null)
                callback.onNumberReady(count);
        });
    }

    public static void setMessageCount(){
        ObserverManager.getInstence().notify(ObserverManager.NOTIFY_MESSAGE_REFRESH,null,"");
    }

    public static void setQiyvMessage(int qiyvMessage) {
        MessageTipController.qiyvMessage = qiyvMessage;
        setMessageCount();
    }

    public static void setQuanMessage(int quanMessage) {
        MessageTipController.quanMessage = quanMessage;
        setMessageCount();
    }

    public static void autoIncreaseOneFeek(){
        MessageTipController.feekbackMessage ++;
        setMessageCount();
    }

    public static void setFeekbackMessage(int feekbackMessage) {
        MessageTipController.feekbackMessage = feekbackMessage;
        setMessageCount();
    }

    public static void setMyQAMessage(int myQAMessage) {
        MessageTipController.myQAMessage = myQAMessage;
        setMessageCount();
    }

    public static int getQiyvMessage() {
        return qiyvMessage;
    }

    public static int getQuanMessage() {
        return quanMessage;
    }

    public static int getFeekbackMessage() {
        return feekbackMessage;
    }

    public static int getMyQAMessage() {
        return myQAMessage;
    }
}
