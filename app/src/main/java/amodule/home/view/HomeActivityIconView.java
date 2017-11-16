package amodule.home.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.main.Tools.BuoyControler;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Description :
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 15:17.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeActivityIconView extends AppCompatImageView {

    private final String CACHE_PATH = FileManager.getSDCacheDir() + "actIconCache";

    String mUrl = "";

    public HomeActivityIconView(Context context) {
        this(context, null);
    }

    public HomeActivityIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeActivityIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        setScaleType(ScaleType.CENTER_INSIDE);
        setImageResource(R.drawable.home_default_icon);
        int padding = Tools.getDimen(getContext(), R.dimen.dp_10);
        setPadding(padding,0,padding,0);
        initData();
        loadData();
    }

    //初始化数据
    private void initData() {
        String cacheData = FileManager.readFile(CACHE_PATH).trim();
        handlerData(true, cacheData);
    }

    //获取数据
    private void loadData() {
        BuoyControler.getBuoyDataFromService(false, getContext(), data -> {
            for(int index = 0;index <data.size();index++){
                Map<String,String> buoyData = data.get(index);
                String typeStr = buoyData.get("text");
                if(!buoyData.isEmpty() && BuoyControler.TYPE_LEFT_TOP.equals(typeStr)){
                    handlerData(false,Tools.map2Json(buoyData));
                }
            }
        });
    }

    private void handlerData(boolean isCache, String dataStr) {
        if (TextUtils.isEmpty(dataStr)) {
            return;
        }
        Map<String, String> dataMap = StringManager.getFirstMap(dataStr);
        if (dataMap.isEmpty()) {
            return;
        }
        int padding = Tools.getDimen(getContext(), R.dimen.dp_20);
        setPadding(padding,0,padding,0);
        this.mUrl = dataMap.get("url");
        Glide.with(getContext())
                .load(dataMap.get("img"))
                .into(this);
        if (isCache) {
            return;
        }
        //保存数据
        FileManager.saveFileToCompletePath(CACHE_PATH, dataStr, false);
    }

    @Nullable
    public String getUrl() {
        return mUrl;
    }
}
