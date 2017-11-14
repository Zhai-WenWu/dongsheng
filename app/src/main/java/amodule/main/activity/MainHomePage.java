package amodule.main.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;

import com.xiangha.R;

import java.util.Map;

import acore.logic.SpecialWebControl;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.StringManager;
import amodule.home.HomeDataControler;
import amodule.home.HomeViewControler;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Description :
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 13:53.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class MainHomePage extends MainBaseActivity {
    public static final String KEY = "MainIndex";

    HomeDataControler mDataControler;
    HomeViewControler mViewContrloer;

    boolean LoadOver =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_home_page);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        Main.allMain.allTab.put(KEY, this);//这个Key值不变

        mViewContrloer = new HomeViewControler(this);
        mDataControler = new HomeDataControler(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDataControler.loadCacheHomeData(getHeaderCallback(true));
        mDataControler.loadServiceHomeData(getHeaderCallback(false));
        mDataControler.loadServiceTopData(new InternetCallback(this) {
            @Override
            public void loaded(int i, String s, Object o) {
                if(i>=ReqEncyptInternet.REQ_OK_STRING)
                    mViewContrloer.setTopData(StringManager.getListMapByJson(o));
            }
        });
        //TODO

//        loadManager.setLoading(mViewContrloer.getRvListView(),
//                mViewContrloer.getAdapter(),
//                true,
//                v -> loadServiceFeedData()
//        );
    }

    public InternetCallback getHeaderCallback(boolean isCache){
        return new InternetCallback(this) {
            @Override
            public void loaded(int i, String s, Object o) {
                if(i >= ReqEncyptInternet.REQ_OK_STRING){
                    if(!isCache){
                        mDataControler.saveCacheHomeData((String) o);
                    }
                    Map<String,String> map = StringManager.getFirstMap(o);
                    if(map.containsKey("list")){
                        mViewContrloer.setHeaderData(StringManager.getListMapByJson(map.get("list")),isCache);
                    }
                }
            }
        };
    }

    private void loadServiceFeedData(){
        mDataControler.loadServiceFeedData(false,new InternetCallback(this) {
            @Override
            public void loaded(int i, String s, Object o) {

            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    private int resumeCount = 0;

    public void onResumeFake() {
        if (resumeCount != 0)
            SpecialWebControl.initSpecialWeb(this, rl, "index", "", "");
        resumeCount++;
    }

    public void refreshContentView(boolean b) {

    }
}
