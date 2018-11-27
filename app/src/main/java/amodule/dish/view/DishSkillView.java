package amodule.dish.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;
import acore.tools.Tools;
import amodule.dish.activity.DetailDish;
import amodule.vip.DeviceVipManager;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

/**
 * 模块化；横滑
 */
public class DishSkillView extends ItemBaseView{
    private ImageView img_skill;
    private TextView text1,text2;
    public DishSkillView(Context context) {
        super(context, R.layout.dish_module_skill);
    }

    public DishSkillView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.dish_module_skill);
    }

    public DishSkillView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.dish_module_skill);
    }

    @Override
    public void init() {
        super.init();
        img_skill= (ImageView) findViewById(R.id.img_skill);
        text1= (TextView) findViewById(R.id.text1);
        text2= (TextView) findViewById(R.id.text2);
    }
    public void setData(Map<String,String> data,int position){
        findViewById(R.id.skill_line).setVisibility(position>0?View.VISIBLE:View.GONE);
        findViewById(R.id.skill_vip).setVisibility("2".equals(data.get("iconType"))?VISIBLE:GONE);
        findViewById(R.id.skill_shikan).setVisibility("1".equals(data.get("iconType"))?VISIBLE:GONE);
        setViewImage(img_skill,data,"img");
        if("2".equals(data.get("isVideo")))text1.setText(data.get("videoTime"));
        text2.setText(data.get("text"));
        final String url = data.get("url");
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!LoginManager.isLogin()&&!DeviceVipManager.isDeviceVip()){
                    if(data.containsKey("iconType")&&"2".equals(data.get("iconType"))){
                        String url="xiangha://welcome?VipWebView.app?url=https%3A%2F%2Fappweb.xiangha.com%2Fvip%2Fmyvip%3Fpayset%3D2%26fullScreen%3D2%26vipFrom%3D%E9%A6%99%E5%93%88%E8%AF%BE%E7%A8%8B%E8%AF%A6%E6%83%85%E9%A1%B5%E7%AB%8B%E5%88%BB%E6%8B%A5%E6%9C%89%E7%89%B9%E6%9D%83%E6%8C%89%E9%92%AE";
                        if(XHActivityManager.getInstance().getCurrentActivity() != null)
                            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),url,false);
                        return;
                    }
                }
                if(!TextUtils.isEmpty(moduleType)&&"2".equals(moduleType)) {
                    if(callBack!=null)callBack.getDataUrl(url);
                }else {
                    XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), DetailDish.tongjiId_detail, "食材小技巧", "食材小技巧点击量");
                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), url, false);
                }
            }
        });
    }

    public SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                ImageView img = null;
                if (v.getTag(TAG_ID).equals(url))
                    img = v;
                if (img != null && bitmap != null) {
                    // 图片圆角和宽高适应auther_userImg
                    v.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                    v.setImageBitmap(bitmap);
                    v.setImageBitmap(UtilImage.toRoundCorner(v.getResources(),bitmap,1, Tools.getDimen(context,R.dimen.dp_4)));
                }
            }
        };
    }
    private DishModuleScrollView.onDishModuleClickCallBack callBack;
    private String moduleType = "";
    public void setDishModuleClickCallBack(DishModuleScrollView.onDishModuleClickCallBack callBack,String type){
        this.callBack= callBack;if(!TextUtils.isEmpty(type))moduleType=type;

    }
}
