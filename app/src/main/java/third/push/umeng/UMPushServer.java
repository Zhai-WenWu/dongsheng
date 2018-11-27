package third.push.umeng;

import android.content.Context;
import android.util.Log;

import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;

import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.xiaomi.MiPushRegistar;

import acore.logic.XHClick;
import acore.override.XHApplication;
import aplug.service.alarm.PushAlarm;

/**
 * PackageName : third.push.xm
 * Created by MrTrying on 2016/8/16 14:30.
 * E_mail : ztanzeyu@gmail.com
 */
public class UMPushServer {
	private Context mContext;
	private PushAgent mPushAgent;

	public UMPushServer(Context context){
		this.mContext = context;
		mPushAgent = PushAgent.getInstance(context);
		PushAlarm.startTimingWake(context);
	}

	public void register(){
		//注册推送服务--可在子线程执行，已验证没有问题
		new Thread(new Runnable() {
			@Override
			public void run() {
		PushAgent mPushAgent = PushAgent.getInstance(mContext);
//注册推送服务，每次调用register方法都会回调该接口
		mPushAgent.register(new IUmengRegisterCallback() {

			@Override
			public void onSuccess(String deviceToken) {
				//注册成功会返回device token
				Log.i("xianghaTag","deviceToken::"+deviceToken);
				XHClick.mapStat(XHApplication.in(),"a_push_umstate","onSuccess","");
			}
			@Override
			public void onFailure(String s, String s1) {
				XHClick.mapStat(XHApplication.in(),"a_push_umstate","onFailure","");
			}
		});
		//小米通道
		MiPushRegistar.register(mContext, "2882303761517138495", "5711713867495");
		//华为通道
		HuaWeiRegister.register(mContext);
		mPushAgent.setPushIntentServiceClass(UMPushService.class);

			}
		}).start();
	}

	public void addAlias(String account){
		mPushAgent.addAlias(account, "xiangha", new UTrack.ICallBack() {
			@Override
			public void onMessage(boolean b, String s) {

			}
		});
	}

	public void removeAlias(String account){
		mPushAgent.deleteAlias(account, "xiangha", new UTrack.ICallBack() {
			@Override
			public void onMessage(boolean b, String s) {

			}
		});
	}

	IUmengRegisterCallback callback = new IUmengRegisterCallback() {

		@Override
		public void onSuccess(String deviceToken) {
			Log.d("UMPushServer","IUmengRegisterCallback() deviceToken:" + deviceToken);
			//注册成功会返回device token
		}

		@Override
		public void onFailure(String s, String s1) {
			Log.d("UMPushServer","IUmengRegisterCallback() s:" + s + "    s1:" +s1);
		}
	};

}
