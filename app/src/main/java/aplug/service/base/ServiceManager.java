package aplug.service.base;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import aplug.service.CoreService;

/**
 * Created by Jerry on 2016/7/6.
 */
public class ServiceManager {
	/** 守护进程轮询时间 */
	public static final int PROTECT_POLLING = 4 * 1000;//4s
	/** 正常进程轮询时间 */
	public static final int NORMAL_POLLING = 3 * 1000;//3s
	/** 业务进程轮询时间 */
	public static final int CORE_POLLING = 5 * 60 * 1000;//5min
	/** 守护进程唤醒闹钟时间间隔 */
	public static final int ALARM_INTERVAL = 30 * 60 * 1000;// 30min
	public static final int ACTIVITY_LIFE_MIN = 2;// 2min
	public static final int ACTIVITY_LIFE_MAX = 5;// 5min


	/** 守护进程class */
	public static final Class<? extends ProtectService> CLASS_PROTECT_SERVICE = ProtectService.class;
	/** 其他业务进程 */
	public static final Class<? extends NormalService>[] CLASS_NORMAL_SERVICES = new Class[]{CoreService.class};

	/**
	 * 开启守护进程
	 *
	 * @param context 上下文
	 */
	public static void startProtectService(Context context) {
		startService(context, CLASS_PROTECT_SERVICE);
	}

	public static void startService(Context context, Class<? extends Service> theClass) {
		try{
			Intent intent = new Intent(context, theClass);
			context.startService(intent);
		}catch (Exception e){
			//针对oppo手机
		}
	}

    /**
     * 检查Service状态
     * @param context 上下文
     * @param className 类名
     * @return service是否正在运行
     */
    public static boolean isServiceRunning(Context context,String className){
	    boolean isServiceRunning = false;
		try{
			ActivityManager manager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
			if(manager == null){
				return isServiceRunning;
			}
			List<ActivityManager.RunningServiceInfo> serviceInfos = manager.getRunningServices(Integer.MAX_VALUE);
			if(serviceInfos != null){
				for(int index = 0 ; index < serviceInfos.size() ; index ++){
					ActivityManager.RunningServiceInfo serviceInfo = serviceInfos.get(index);
					if(serviceInfo != null
							&& serviceInfo.service != null
							&& className.equals(serviceInfo.service.getClassName())){
						isServiceRunning = true;
						break;
					}
				}
			}
		}catch (Exception e){

		}
	    return isServiceRunning;
    }
}
