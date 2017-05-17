package aplug.service.base;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import aplug.service.handler.NormalHandler;

/**
 * Created by Jerry on 2016/7/6.
 */
public class NormalService extends Service {
    private Messenger serverMessenger = new Messenger(new NormalHandler(this));

    @Override
    public void onCreate() {
        super.onCreate();
        start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serverMessenger.getBinder();
    }

    private void start(){
        //启动线程每分钟轮训
        new Thread(new Runnable() {
            @Override
            public void run() {
                String serviceName = ServiceManager.CLASS_PROTECT_SERVICE.getSimpleName();
                while(true) {
                    try {
                        if(!ServiceManager.isServiceRunning(NormalService.this,serviceName)){
                            Message message = new Message();
                            message.what = NormalHandler.HANDLER_MSG_START_PROTECT_SERVICE;
                            serverMessenger.send(message);
                        }
//                        FileManager.saveFileToCompletePath(FileManager.getSDDir() + "log/test-" + Tools.getAssignTime("yyyyMMdd-HH:mm:ss",0),"" ,false);
//                        Log.d("controlService", "轮训判断守护服务");
                        Thread.sleep(ServiceManager.NORMAL_POLLING);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
