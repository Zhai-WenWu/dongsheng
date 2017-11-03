package amodule.user.view.module;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;

/**
 * 大图样式--B1
 */
public class ModuleBigImgView extends ModuleBaseView{
    private TextView big_title;
    private ImageView big_img;
    private String url="";
    public ModuleBigImgView(Context context ) {
        super(context, R.layout.module_big_view);
    }

    public ModuleBigImgView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.module_big_view);
    }

    public ModuleBigImgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.module_big_view);
    }

    @Override
    public void initUI() {
        setMODULE_TAG("B1");
//        Log.i("xianghaTag","ModuleBigImgView:::initUI");
        big_title= (TextView) findViewById(R.id.big_title);
        big_img= (ImageView) findViewById(R.id.big_img);
    }

    @Override
    public void initData(Map<String, String> map) {
//        Log.i("xianghaTag","ModuleBigImgView::initData");
        //标题
        if(map.containsKey("text1")&& !TextUtils.isEmpty(map.get("text1"))){
            big_title.setVisibility(View.VISIBLE);
            big_title.setText(map.get("text1"));
        }
        //url点击跳转
        if(map.containsKey("url")&& !TextUtils.isEmpty(map.get("url")))url= map.get("url");
        if(!map.containsKey("styleData")||TextUtils.isEmpty(map.get("styleData"))){
            findViewById(R.id.big_container_rela).setVisibility(GONE);
            return;
        }
        //显示数据判断
        Map<String,String> mapStyle= StringManager.getFirstMap(map.get("styleData"));
        if(mapStyle.containsKey("img")&&!TextUtils.isEmpty(mapStyle.get("img"))){
            findViewById(R.id.big_container_rela).setVisibility(VISIBLE);
            setViewImage(big_img,mapStyle.get("img"));
            //type 类型判断//1:图，2视频，3gif
            findViewById(R.id.big_layer_view).setVisibility(mapStyle.containsKey("type")&&"2".equals(mapStyle.get("type"))?VISIBLE:GONE);
            findViewById(R.id.big_play_img).setVisibility(mapStyle.containsKey("type")&&"2".equals(mapStyle.get("type"))?VISIBLE:GONE);
        }else findViewById(R.id.big_container_rela).setVisibility(GONE);
        setListener();
    }

    @Override
    public void setListener() {
        findViewById(R.id.big_container_rela).setOnClickListener(UrlOnClickListener);
        big_title.setOnClickListener(UrlOnClickListener);
    }
    private OnClickListener UrlOnClickListener= new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i("xianghaTag","OnClickListener");
            if(!TextUtils.isEmpty(url)) {
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), url, false);
                if(TextUtils.isEmpty(getStatisticId())&&mContext!=null) XHClick.mapStat(mContext,getStatisticId(),"点击内容","");
            }
        }
    };
}
