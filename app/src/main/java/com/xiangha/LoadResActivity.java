package com.xiangha;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import acore.override.activity.base.BaseActivity;
import acore.tools.MultiDexTools;

/**
 * Created by xiangha on 2016/8/23.
 */

public class LoadResActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.i("FRJ", "onCreate" );
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super .onCreate(savedInstanceState);
        //Log.i("FRJ", "setFlags" );
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN );
        overridePendingTransition(R.anim.null_anim, R.anim.null_anim);
        setContentView(R.layout.xh_welcome);
        //Log.i("FRJ", "setContentView" );
        new LoadDexTask().execute();
    }

    class LoadDexTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                //Log.i("FRJ", "install star" );
                MultiDex.install(getApplication());
                //Log.i("FRJ", "install finish" );
                MultiDexTools.installFinish(getApplication());
            } catch (Exception e) {
                //Log.i("FRJ" , "Exception:" + e.getLocalizedMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(Object o) {
            //Log.i("FRJ","get install finish");
            finish();
            System.exit( 0);
        }
    }
    @Override
    public void onBackPressed() {
        //cannot backpress
    }

}
