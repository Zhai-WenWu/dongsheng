package amodule.home.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.Map;

import acore.tools.StringManager;
import third.ad.db.bean.AdBean;
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
        AdBean adBean = AdConfigTools.getInstance().getAdConfig(AdPlayIdConfig.HOME_TOPLEFT);
        if(adBean == null){
            return;
        }
        //TODO
//        Map<String,String> adConfigMap = StringManager.getFirstMap(adBean.adConfig);
//        final String[] keys = {"1","2","3","4",};
//        for(String key:keys){
//            Map<String,String> tempMap = StringManager.getFirstMap(adConfigMap.get(key));
//            if(TAG_BANNER.equals(tempMap.get("type"))
//                    && "2".equals(tempMap.get("open"))){
//                handlerData(StringManager.getFirstMap(adBean.banner));
//                break;
//            }
//        }

    }

    private void handlerData(Map<String,String> dataMap){
        if(dataMap.isEmpty()){
            return;
        }
        Map<String,String> imgsMap = StringManager.getFirstMap(dataMap.get("imgs"));
//        Log.i("tzy","imgsMap = " + imgsMap.toString());
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
