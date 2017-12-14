package amodule.home.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import java.util.Map;

import acore.tools.StringManager;
import third.ad.tools.AdConfigTools;
import third.ad.tools.AdPlayIdConfig;

/**
 * Description :
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 15:17.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeActivityIconView extends AppCompatImageView {

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
        initData();
        //请求数据
        postDelayed(() -> {
            loadData();
        },2000);
    }

    private void initData(){
        setImageResource(R.drawable.home_default_icon);
    }

    //获取数据
    private void loadData() {
        Map<String, String> dataMap = AdConfigTools.getInstance().getAdConfigData(AdPlayIdConfig.HOME_TOPLEFT);
        if (dataMap.isEmpty()) {
            return;
        }
        Map<String,String> adConfigMap = StringManager.getFirstMap(dataMap.get("adConfig"));
        final String[] keys = {"1","2","3","4",};
        for(String key:keys){
            Map<String,String> tempMap = StringManager.getFirstMap(adConfigMap.get(key));
            if("personal".equals(tempMap.get("type"))
                    && "2".equals(tempMap.get("open"))){
                handlerData(StringManager.getFirstMap(dataMap.get("banner")));
                break;
            }
        }
    }

    private void handlerData(Map<String,String> dataMap){
        if(dataMap.isEmpty()){
            return;
        }
        Map<String,String> imgsMap = StringManager.getFirstMap(dataMap.get("imgs"));
        Log.i("tzy","imgsMap = " + imgsMap.toString());
        String imgUrl = imgsMap.get("topbarImg");
        if(TextUtils.isEmpty(imgUrl)){
            return;
        }
        this.mUrl = dataMap.get("url");
        Glide.with(getContext())
                .load(imgUrl)
                .placeholder(R.drawable.home_default_icon)
                .error(R.drawable.home_default_icon)
                .into(this);
    }

    @Nullable
    public String getUrl() {
        return mUrl;
    }
}
