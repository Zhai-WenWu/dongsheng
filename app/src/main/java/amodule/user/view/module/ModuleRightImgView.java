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
 *右图--B3
 */
public class ModuleRightImgView extends ModuleBaseView{
    private TextView module_title_txt,module_desc;
    private ImageView module_img;
    private String url="";

    public ModuleRightImgView(Context context) {
        super(context, R.layout.module_right_view);
    }

    public ModuleRightImgView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.module_right_view);
    }

    public ModuleRightImgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.module_right_view);
    }

    @Override
    public void initUI() {
        setMODULE_TAG("B3");
        module_title_txt= (TextView) findViewById(R.id.module_title_txt);
        module_desc= (TextView) findViewById(R.id.module_desc);
        module_img= (ImageView) findViewById(R.id.module_img);
    }

    @Override
    public void initData(Map<String, String> map) {
        Log.i("xianghaTag","ModuleRightImgView::");
        setKeyContent(map,module_title_txt,"text1");//标题
        setKeyContent(map,module_desc,"text2");//内容
        //url点击跳转
        if(map.containsKey("url")&& !TextUtils.isEmpty(map.get("url")))url= map.get("url");
        if(!map.containsKey("styleData")||TextUtils.isEmpty(map.get("styleData"))){
            findViewById(R.id.module_imgs_rela).setVisibility(GONE);
            return;
        }
        Map<String,String> mapStyle= StringManager.getFirstMap(map.get("styleData"));
        if(mapStyle.containsKey("img")&&!TextUtils.isEmpty(mapStyle.get("img"))){
            findViewById(R.id.module_imgs_rela).setVisibility(VISIBLE);
            setViewImage(module_img,mapStyle.get("url"));
            findViewById(R.id.module_layer_view).setVisibility(mapStyle.containsKey("type")&&"2".equals(mapStyle.get("type"))?VISIBLE:GONE);
            findViewById(R.id.module_play_img).setVisibility(mapStyle.containsKey("type")&&"2".equals(mapStyle.get("type"))?VISIBLE:GONE);
        }else findViewById(R.id.module_imgs_rela).setVisibility(GONE);

    }

    @Override
    public void setListener() {
        module_title_txt.setOnClickListener(UrlOnClickListener);
        module_desc.setOnClickListener(UrlOnClickListener);
        module_img.setOnClickListener(UrlOnClickListener);
    }
    private OnClickListener UrlOnClickListener= new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!TextUtils.isEmpty(url)) {
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), url, false);
                if(TextUtils.isEmpty(getStatisticId())&&mContext!=null) XHClick.mapStat(mContext,getStatisticId(),"点击内容","");
            }
        }
    };
}
