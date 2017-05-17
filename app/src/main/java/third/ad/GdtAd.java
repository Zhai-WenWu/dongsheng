//package third.ad;
//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.Field;
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.Method;
//import java.lang.reflect.Proxy;
//import java.util.ArrayList;
//import java.util.Map;
//
//import xh.basic.tool.UtilString;
//import android.app.Activity;
//import android.content.Context;
//import android.text.TextUtils;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.RelativeLayout;
//
//import com.umeng.analytics.MobclickAgent;
//
//public class GdtAd {
//
//	private static GdtAd gdtAd = null;
//
//	private final static String appId = "1150004142";
//	private String bannerId = "9079537216558868103";
//	private String interstitialId = "6010803614672710";
//
//	public boolean welcomeAdIsShow = false;
//	public boolean bannerAdIsShow = false;
//	public boolean classHavFound = true;
//
//	public static GdtAd in(){
//		if(gdtAd == null){
//			gdtAd = new GdtAd();
//		}
//		return gdtAd;
//	}
//
//	/**
//	 * @param act
//	 * @param RelativeLayout
//	 * @param RefreshTime 设置广告轮播时间，为0或30~120之间的数字，单位为s,0标识不自动轮播
//	 * @param adListener 广告状态的监听
//	 * @return
//	 */
//	public void addBannerAd(Activity act, RelativeLayout rl, int RefreshTime){
//		// 创建Banner广告AdView对象
//		// appId : 在 http://e.qq.com/dev/ 能看到的app唯一字符串
//		// posId : 在 http://e.qq.com/dev/ 生成的数字串，并非 appid 或者 appkey
//		try {
//			Class c = Class.forName("com.qq.e.ads.banner.BannerView");
//			Class aDSize = Class.forName("com.qq.e.ads.banner.ADSize");
//			Field bannerField = aDSize.getField("BANNER");
//			Object banner = bannerField.get(aDSize);
//			Constructor constructor = c.getConstructor(new Class[]{Activity.class,aDSize,String.class,String.class});
//			Object object = constructor.newInstance(new Object[]{act, banner, appId, bannerId});
//			rl.addView((View) object);
//			Method method = c.getMethod("setRefresh", new Class[]{int.class});
//			method.invoke(object, new Object[]{RefreshTime});
//			Method loadAD = c.getMethod("loadAD");
//			loadAD.invoke(object, null);
//		}catch(ClassNotFoundException classNotFound){
//			classHavFound = false;
//			classNotFound.printStackTrace();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
////		com.qq.e.ads.banner.BannerView banner = new com.qq.e.ads.banner.BannerView(act, com.qq.e.ads.banner.ADSize.BANNER, appId, bannerId);
////		rl.addView(banner);
////		//设置广告轮播时间，为0或30~120之间的数字，单位为s,0标识不自动轮播
////		banner.setRefresh(RefreshTime);
////		// 发起广告请求，收到广告数据后会展示数据
////		banner.loadAD();
//	}
//	/**
//	 *
//	 * @param act
//	 * @param container
//	 * @param splashADListener
//	 */
//	public void addSplashAD(Activity act,final FrameLayout container, GdtSplashAdListener splashADListener){
//
//		//# NOTE
//		try {
//			Class splashAd = Class.forName("com.qq.e.ads.splash.SplashAD");
//			Class listenerClass = Class.forName("com.qq.e.ads.splash.SplashADListener");
//			Object listenerMeth = methon(act,splashADListener);
//
//			Constructor cons = splashAd.getConstructor(new Class[]{Activity.class,ViewGroup.class,String.class,String.class,listenerClass});
//			cons.newInstance(new Object[]{act, container, appId, interstitialId,listenerMeth});
//		}catch(ClassNotFoundException classNotFound){
//			classHavFound = false;
//			classNotFound.printStackTrace();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//
//
////		new com.qq.e.ads.splash.SplashAD(act, container, appId, interstitialId,splashADListener);
//		//# NOTE
//
//	}
//
//	private Object methon(Activity act,final GdtSplashAdListener splashADListener){
//		Object clazzInstance = null;
//		try {
//			ClassLoader loader = act.getClassLoader();
//			Class interfazz = loader.loadClass("com.qq.e.ads.splash.SplashADListener");
//			clazzInstance = Proxy.newProxyInstance(loader, new Class[] { interfazz }, new InvocationHandler() {
//				@Override
//				public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
//					if (method.getName().equals("onADDismissed")) {
//						splashADListener.onADDismissed();
//					}else if (method.getName().equals("onADPresent")) {
//						splashADListener.onADPresent();
//					}else if (method.getName().equals("onNoAD")) {
//						if(args[0] != null) splashADListener.onNoAD(Integer.parseInt(args[0].toString()));
//						else splashADListener.onNoAD(-1);
//					}else{
//						return method.invoke(obj, args);
//					}
//					return null;
//				}
//			});
//		}catch(ClassNotFoundException classNotFound){
//			classHavFound = false;
//			classNotFound.printStackTrace();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		return clazzInstance;
//	}
//
//	public void setAdConf(final Context context){
//		new Thread(){
//
//			@Override
//			public void run() {
//				super.run();
//				String jsonStr = MobclickAgent.getConfigParams(context, "xianghaAd");
//				if(jsonStr.length()>1) {
//					try{
//						ArrayList<Map<String, String>> array = UtilString.getListMapByJson(jsonStr);
//						Map<String,String> map = array.get(0);
//						if(map.containsKey("welcomeAd")){
//							Map<String,String> mapWelcomeAd = UtilString.getListMapByJson(map.get("welcomeAd")).get(0);
//							if("2".equals(mapWelcomeAd.get("show"))){
//								welcomeAdIsShow = true;
//							}else{
//								welcomeAdIsShow = false;
//							}
//							String adId = mapWelcomeAd.get("adId");
//							if(!TextUtils.isEmpty(adId) && adId.length() > 3){
//								interstitialId = adId;
//							}
//						}
//						if(map.containsKey("bannerAd")){
//							Map<String,String> mapBannerAd = UtilString.getListMapByJson(map.get("bannerAd")).get(0);
//							if("2".equals(mapBannerAd.get("show"))){
//								bannerAdIsShow = true;
//							}else{
//								bannerAdIsShow = false;
//							}
//							String adId = mapBannerAd.get("adId");
//							if(!TextUtils.isEmpty(adId) && adId.length() > 3){
//								bannerId = adId;
//							}
//						}
//					}catch(Exception e){
//					}
//				}
//			}
//
//		}.start();
//	}
//
//
//	public interface GdtSplashAdListener
//	{
//		public abstract void onADDismissed();
//		public abstract void onADPresent();
//		public void onNoAD(int arg0);
//	}
//}
