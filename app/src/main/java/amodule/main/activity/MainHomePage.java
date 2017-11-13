package amodule.main.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;

import com.xiangha.R;

import acore.logic.SpecialWebControl;
import acore.override.activity.mian.MainBaseActivity;
import amodule.main.Main;

/**
 * Description :
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 13:53.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class MainHomePage extends MainBaseActivity {

    public static final String KEY = "MainIndex";

    private int resumeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_home_page);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        Main.allMain.allTab.put(KEY, this);//这个Key值不变
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onResumeFake(){
        if(resumeCount != 0)
            SpecialWebControl.initSpecialWeb(this,rl,"index","","");
        resumeCount++;
    }

    public void refreshContentView(boolean b) {
    }
}
