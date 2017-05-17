package com.lansosdk.videoeditor;

import android.content.Context;
import android.util.Log;

public class LoadLanSongSdk {
	private static boolean isLoaded=false;
	  
	public static synchronized void loadLibraries() {
        if (isLoaded)
            return;
        Log.d("lansoeditor","load libraries......");
    	System.loadLibrary("ffmpegeditor");
    	System.loadLibrary("lsdisplay");
    	System.loadLibrary("lsplayer");
    	
	    isLoaded=true;
	}

	/**
	 * 初始化so库和key值
	 * @param context context为Application的
     */
	public static void initVideoSdk(Context context){
		loadLibraries();
		LanSoEditor.initSo(context,"xiangha_veditor.key");
	}
}
