package third.video;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.sina.sinavideo.coreplayer.splayer.ContextUtils;
import com.sina.sinavideo.coreplayer.splayer.IOUtils;
import com.sina.sinavideo.coreplayer.splayer.SPlayer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.FileManager;
import acore.tools.LogManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

public class VideoApplication {
	private static VideoApplication mVideoApp = null;
//	static String libsplayerUrl = "http://www.huher.com:9810/test/zip/";
	static String suffixName = ".7z";
	public static boolean initSuccess = false;
	static boolean isIniting = false;

	private VideoApplication(){}
	
	public static VideoApplication getInstence(){
		if(mVideoApp == null){
			mVideoApp = new VideoApplication();
		}
		return mVideoApp;
	}
	
	public void initialize(Context context){
		initSuccess = SPlayer.isInitialized(context);
//		Log.i("tzy","initialize ed");
//		Log.i("tzy","initSuccess = " + initSuccess);
		if(!initSuccess){
			getLibs(context);
		}
	}

	private void getLibs(final Context context){
		if(isIniting){
			return;
		}
		isIniting = true;
		String libsplayerUrl = AppCommon.getAppData(context, "videoDecUrl");
//		Log.i("tzy","libsplayerUrl = " + libsplayerUrl);
		if(TextUtils.isEmpty(libsplayerUrl)){
			isIniting = false;
			initSuccess = true;
			return;
		}
		String url = libsplayerUrl + SPlayer.getVitamioType() + suffixName;
//		Log.i("tzy","url = " + url);
		final String libPath = SPlayer.getLibraryPath() + SPlayer.COMPRESS_LIBS_NAME;
		if(FileManager.ifFileModifyByCompletePath(libPath, -1) == null){
			ReqInternet.in().getInputStream(url, new InternetCallback(context) {
				@Override
				public void loaded(int flag, String url, final Object msg) {
					if(flag >= ReqInternet.REQ_OK_IS){
						new Thread(new Runnable() {
							@Override
							public void run() {
								FileManager.saveFileToCompletePath(libPath , (InputStream) msg, false);
								extractLibs(context);
							}
						}).start();
					}else{
						isIniting = false;
						initSuccess = true;
					}
				}
			});
		}else{
			extractLibs(context);
		}
	}
	
	private void extractLibs(Context context) {
		final int version = ContextUtils.getVersionCode(context);
		File lock = new File(SPlayer.getLibraryPath() + SPlayer.LIBS_LOCK);
		if (lock.exists()) {
			if (!lock.delete()) {}
		}
		// /加上一个逻辑，解压前应将原有的so删掉，防止解压出错
		for (String L : SPlayer.getRequiredLibs()) {
			File tmpFile = new File(SPlayer.getLibraryPath() + L);
			if (tmpFile != null && tmpFile.exists() && tmpFile.isFile()) {
				tmpFile.delete();
			}
		}
		String libPath = SPlayer.getLibraryPath() + SPlayer.COMPRESS_LIBS_NAME;
		//inited的意义为   true ? 失败 : 成功
		boolean inited = SPlayer.initializeLibs(libPath, SPlayer.getLibraryPath(),
				String.valueOf(SPlayer.getVitamioType()));
		LogManager.print("d", "---------------" + inited + "---------------");
		if(inited){
			initSuccess = !inited;
			isIniting = false;
			return;
		}
		FileWriter fw = null;
		try {
			if (!lock.createNewFile()) {
			} else {
			}
			fw = new FileWriter(lock);
			fw.write(String.valueOf(version));
		} catch (IOException e) {
			LogManager.print("d", "---------------" + Arrays.toString(e.getStackTrace()) + "---------------");
		} finally {
			IOUtils.closeSilently(fw);
			initSuccess = !inited;
			isIniting = false;
		}
	}

	public boolean isInitSuccess(){
		return initSuccess;
	}
	
}
