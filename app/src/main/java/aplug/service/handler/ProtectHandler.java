package aplug.service.handler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import aplug.service.base.ServiceManager;

/**
 * Created by Jerry on 2016/7/6.
 */
public class ProtectHandler extends Handler {
    private Context context=null;

    public ProtectHandler(Context theContext){
        super();
        this.context=theContext;
    }

    @Override
    public void handleMessage(Message msg) {
        switch(msg.what){
            case HANDLER_MSG_START_PROTECT_SERVICE:
                ServiceManager.startService(context, ServiceManager.CLASS_PROTECT_SERVICE);
                break;
            case HANDLER_MSG_START_SERVICE:
                ServiceManager.startService(context,(Class)msg.obj);
                break;
            case HANDLER_MSG_SEND:
                break;
        }
    }

    /** 开守护服务 */
    public static final int HANDLER_MSG_START_PROTECT_SERVICE=10;
    /** 开普通服务 */
    public static final int HANDLER_MSG_START_SERVICE=20;
    /** 发送消息 */
    public static final int HANDLER_MSG_SEND=30;
}
