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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super .onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN );
        overridePendingTransition(R.anim.null_anim, R.anim.null_anim);
        setContentView(R.layout.xh_welcome);
        new LoadDexTask().execute();
    }

    class LoadDexTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                MultiDex.install(getApplication());
                MultiDexTools.installFinish(getApplication());
            } catch (Exception e) {
            }
            return null;
        }
        @Override
        protected void onPostExecute(Object o) {
            finish();
            System.exit( 0);
        }
    }
    @Override
    public void onBackPressed() {
        //cannot backpress
    }

}
