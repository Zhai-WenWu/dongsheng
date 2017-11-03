package amodule.user.view.module;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
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
 * 蒙版--B2
 */
public class ModuleMaskImgView extends ModuleBaseView{
    private ImageView mask_img;
    private TextView mask_title_album,mask_num;
    private String url="";
    public ModuleMaskImgView(Context context) {
        super(context, R.layout.module_mask_view);
    }

    public ModuleMaskImgView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.module_mask_view);
    }

    public ModuleMaskImgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.module_mask_view);
    }

    @Override
    public void initUI() {
        setMODULE_TAG("B2");
        mask_img= (ImageView) findViewById(R.id.mask_img);
        mask_title_album= (TextView) findViewById(R.id.mask_title_album);
        mask_num= (TextView) findViewById(R.id.mask_num);

    }

    @Override
    public void initData(Map<String, String> map) {
        if(!map.containsKey("styleData")|| TextUtils.isEmpty(map.get("styleData"))){
            findViewById(R.id.mask_album_container).setVisibility(GONE);
            return;
        }
        //url点击跳转
        if(map.containsKey("url")&& !TextUtils.isEmpty(map.get("url")))url= map.get("url");
        Map<String,String> mapStyle= StringManager.getFirstMap(map.get("styleData"));
        if(mapStyle.containsKey("img")&&!TextUtils.isEmpty(mapStyle.get("img"))){
            setViewImage(mask_img,mapStyle.get("url"));
            findViewById(R.id.mask_album_container).setVisibility(VISIBLE);
            findViewById(R.id.mask_layer_view).setVisibility(VISIBLE);
            setKeyContent(map,mask_title_album,"text1");//标题
            if(map.containsKey("dishNum")&&TextUtils.isEmpty(map.get("text2"))){
                mask_num.setVisibility(View.VISIBLE);
                mask_num.setText(map.get("text2"));
            }else mask_num.setVisibility(View.GONE);
        }else{
            findViewById(R.id.mask_album_container).setVisibility(GONE);
        }
        setListener();
    }

    @Override
    public void setListener() {
        mask_img.setOnClickListener(UrlOnClickListener);
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
