package com.xianghatest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import acore.override.XHApplication;
import amodule.main.Main;

/**
 * welcome 临时停留页面
 */

public class WelcomeStart extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long endTime= System.currentTimeMillis();
        Log.i("zhangyujian","WelcomeStart::oncreate:start:"+(endTime- XHApplication.in().startTime));

        Intent intent = new Intent(this,Main.class);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        if(this.getIntent().getData() != null){
            intent.setData(Uri.parse(this.getIntent().getData().toString()));
        }
        this.startActivity(intent);
        long endTime1= System.currentTimeMillis();
        Log.i("zhangyujian","WelcomeStart::oncreate::"+(endTime1- XHApplication.in().startTime));
        finish();
    }

    @Override
    public void finish() {
        long endTime= System.currentTimeMillis();
        super.finish();
    }
}
