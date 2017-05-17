package aplug.service.base;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Collections;

import aplug.service.handler.ProtectHandler;

/**
 * Created by Jerry on 2016/7/6.
 */
public class ProtectService extends Service {
    private final ArrayList<Class<? extends Service>> serviceArray =new ArrayList<Class<? extends Service>>();
    private ProtectHandler protectHandler=new ProtectHandler(this);
    private Messenger serverMessenger = new Messenger(protectHandler);

    @Override
    public void onCreate() {
        super.onCreate();
        loadNormalService();
        start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //增加被保护的服务
    public void addProtectService(Class<? extends Service> service){
        serviceArray.add(service);
    }

    private void loadNormalService(){
        serviceArray.clear();
        Collections.addAll(serviceArray, ServiceManager.CLASS_NORMAL_SERVICES);
    }

    private void start(){
        //启动线程每分钟轮训
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    for(Class<? extends Service> theClass : serviceArray){
                        if(!ServiceManager.isServiceRunning(ProtectService.this,theClass.getName())){
                            Message message= new Message();
                            message.what= ProtectHandler.HANDLER_MSG_START_SERVICE;
                            message.obj=theClass;
                            try {
                                serverMessenger.send(message);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        Thread.sleep(ServiceManager.PROTECT_POLLING);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
