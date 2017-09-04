package com.xiangha;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import acore.tools.LogManager;
import amodule.main.Main;

/**
 * welcome 临时停留页面
 */

public class WelcomeStart extends Activity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.printStartTime("zhangyujian","WelcomeStart::oncreate:start:");

        Intent intent = new Intent(this,Main.class);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        if(this.getIntent().getData() != null){
            intent.setData(Uri.parse(this.getIntent().getData().toString()));
        }
        this.startActivity(intent);
        LogManager.printStartTime("zhangyujian","WelcomeStart::oncreate:");
        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }
}
