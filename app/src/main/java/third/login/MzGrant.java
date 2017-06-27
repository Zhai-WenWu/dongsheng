package third.login;

import android.widget.Toast;

import java.util.Map;

import acore.logic.LoginManager;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.Tools;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import sdk.meizu.auth.MzAuthenticator;
import sdk.meizu.auth.OAuthError;
import sdk.meizu.auth.OAuthToken;
import sdk.meizu.auth.callback.ImplictCallback;
import third.push.xg.XGPushServer;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class MzGrant {
	public final static String CLIENT_ID = "c6UtfKi5x3VHs0TvsMZE";
    public final static String REDIRECT_URL = "http://www.xiangha.com";
    public final static String SCOPE = "uc_basic_info";
    
    private static final String url = "https://open-api.flyme.cn/v2/me?access_token=";
	
	public static void loginFlyme(final BaseLoginActivity mAct){
		MzAuthenticator mAuthenticator = new MzAuthenticator(MzGrant.CLIENT_ID,REDIRECT_URL);

		mAuthenticator.requestImplictAuth(mAct,MzGrant.SCOPE, new ImplictCallback() {
		@Override
		public void onError(OAuthError error){
		    Toast.makeText(mAct, error.getError(), Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onGetToken(OAuthToken token){
		    getUserInfo(mAct,token.getAccessToken());
		}
		});
	}
	
	private static void getUserInfo(final BaseLoginActivity mAct,final String token){
		ReqInternet.in().doGet(url + token, new InternetCallback(mAct) {
			
			@Override
			public void loaded(int flag, String url, Object msg) {
				if(flag >= UtilInternet.REQ_OK_STRING){
					String value = UtilString.getListMapByJson(msg).get(0).get("value");
					Map<String,String> map = UtilString.getListMapByJson(value).get(0);
					String devCode = XGPushServer.getXGToken(mAct);
					String param = "type=thirdLogin&devCode=" + devCode +
							"&p1=" + token + 
							"&p2="+ map.get("openId") + 
							"&p3=" + "魅族" + 
							"&p4=" + map.get("nickname")  + 
							"&p5="+ map.get("icon") + 
							"&p6=1"; 
//					Tools.showToast(mAct, "授权完成");
					LoginManager.userLogin(mAct, param);
				}else{
					Tools.showToast(mAct, "登录失败");
				}
			}
		});
	}
}
