package third.push.xg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.cache.CacheManager;

import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.LogManager;

/**
 * 
 * @author zeyu_t
 * @date 2014年11月7日 下午3:45:23
 */
@SuppressLint("SimpleDateFormat")
public class XGPushServer {
	public Context mContext;
	public static String returnFlag = "";

	public XGPushServer(Context mContext) {
		this.mContext = mContext;
	}

	// 初始化推送,且注销用户与设备绑定
	public String initPush() {
		return initPush("*");
	}

	/**
	 *
	 * @param userID : 用户code，传code表示当前设备跟用户绑定，不传或者传*: 表示跟之前的用户解绑
	 * @return
     */
	public String initPush(String userID) {
		if(TextUtils.isEmpty(userID)){
			userID = "*";
		}
		XGPushConfig.enableDebug(mContext, false);
//		try {
//		StatConfig.setAppKey(mContext, "Z5F1L87AHYA");
//		// 开启信鸽Pro
//		XGPro.enableXGPro(mContext, false);
//		// 开启MTA debug，发布时一定要删除本行或设置为false
//		StatConfig.setDebugEnable(true);
//	} catch (Exception err) {
//		Log.e("TPush", "开启信鸽Pro失败", err);
//		Toast.makeText(mContext, "开启信鸽Pro失败", Toast.LENGTH_SHRT).show();
//	}
		XGPushManager.registerPush(mContext.getApplicationContext(), userID, new XGIOperateCallback() {

			@Override
			public void onSuccess(Object obj, int flag) {
				XHClick.onEvent(mContext,"xg_register","成功");
				saveXGToken(obj);
			}

			@Override
			public void onFail(Object obj, int errCode, String msg) {
				XHClick.onEvent(mContext,"xg_register","失败");
				registerFail(obj, errCode, msg);
			}
		});
		return returnFlag;
	}

	/**
	 * 注册失败
	 * @param obj
	 * @param errCode
	 * @param msg
	 */
	private void registerFail(Object obj, int errCode, String msg) {
		LogManager.reportError("信鸽注册_errCode_" + errCode, null);
		returnFlag = errCode + "";
	}
	
	/**
	 * 注册成功，存储Token
	 * @param obj
	 */
	private void saveXGToken(Object obj) {
		if (FileManager.ifFileModifyByCompletePath(FileManager.getDataDir() + FileManager.file_XGToken, -1)==null) {
			// 存储Token
			FileManager.saveFileToCompletePath(FileManager.getDataDir() + FileManager.file_XGToken, obj.toString(), false);
			CacheManager.getRegisterInfo(mContext);
		}
		if (FileManager.loadShared(mContext, FileManager.xmlFile_appInfo, FileManager.xmlKey_XGToken).toString().length() < 40) {
			// 存储Token
			Map<String, String> map = new HashMap<String, String>();
			map.put(FileManager.xmlKey_XGToken, obj.toString());
			FileManager.saveShared(mContext, FileManager.xmlFile_appInfo, map);
			CacheManager.getRegisterInfo(mContext);
		}
		returnFlag = "";
	}
	
	/**
	 * 获取设备token
	 * @return token
	 */
	public static String getXGToken(Context context) {
		if (context != null) {
			if (XGPushConfig.getToken(context).length() >= 40) {
				return XGPushConfig.getToken(context);
			}
			String token = FileManager.loadShared(context, FileManager.xmlFile_appInfo, FileManager.xmlKey_XGToken).toString();
			if (token.length() >= 40) {
				return token;
			}
		} else {
			String token = FileManager.readFile(FileManager.getDataDir() + FileManager.file_XGToken);
			if (token.length() >= 40)
				return token;
		}
		return "000000000000000";
	}


}
