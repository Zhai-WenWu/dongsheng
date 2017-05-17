package com.lansosdk.videoeditor;

import android.content.Context;
import android.content.res.AssetManager;


public class LanSoEditor {

		  public static void initSo(Context context,String keyfile)
		  {
		    	    nativeInit(context,context.getAssets(),keyfile);
		  }
	    public static void unInitSo()
	    {
	    		nativeUninit();
	    }
	    public static native void nativeInit(Context ctx,AssetManager ass,String filename);
	    public static native void nativeUninit();
	    
}
