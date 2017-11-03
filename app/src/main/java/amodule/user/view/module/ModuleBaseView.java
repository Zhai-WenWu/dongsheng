package amodule.user.view.module;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;
import java.util.Map;
import acore.tools.FileManager;
import aplug.basic.LoadImage;
import aplug.basic.SubAnimTarget;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

/**
 * 模块View的基础类
 * 1、处理基础加载逻辑-逻辑方法
 * 2、处理图片加载。
 */
public abstract class ModuleBaseView extends RelativeLayout{
    public String MODULE_TAG="";//当前模块名称
    protected final int TAG_ID = R.string.tag;
    protected int mImgResource = R.drawable.i_nopic;
    public int roundImgPixels = 0, imgWidth = 0, imgHeight = 0,// 以像素为单位
            roundType = 1; // 1为全圆角，2上半部分圆角
    public boolean imgZoom = false; // 是否允许图片拉伸来适应设置的宽或高
    public ImageView.ScaleType scaleType = ImageView.ScaleType.CENTER_CROP;
    protected String mImgLevel = FileManager.save_cache; // 图片保存等级
    protected ImageView.ScaleType mScaleType = ImageView.ScaleType.CENTER_CROP;
    public Context mContext;
    public ModuleBaseView(Context context,int layoutId) {
        super(context);
        mContext= context;
        initLayout(layoutId);
    }
    public ModuleBaseView(Context context, AttributeSet attrs,int layoutId) {
        super(context, attrs);
        mContext= context;
        initLayout(layoutId);
    }
    public ModuleBaseView(Context context, AttributeSet attrs, int defStyleAttr,int layoutId) {
        super(context, attrs, defStyleAttr);
        mContext= context;
        initLayout(layoutId);
    }

    /**
     * layoutId 进行处理。子View不进行处理
     * @param layoutId
     */
    private void initLayout(int layoutId){
        LayoutInflater.from(mContext).inflate(layoutId,this,true);
        initUI();
        setListener();
    }

    /**
     * 初始化Ui必须在这里实现(子类不需要手动调用)
     */
    public abstract void initUI();
    /**
     * 设置数据
     * @param map 数据必须是map（必须调用）
     */
    public abstract void initData(Map<String,String> map);

    /**
     * 设置View点击监听(子类不需要手动调用)
     */
    public abstract void setListener();


    /**
     * 对图片加载的处理
     * @param v
     * @param value
     */
    protected void setViewImage(final ImageView v, String value) {
        v.setVisibility(View.VISIBLE);
        if(TextUtils.isEmpty(value)||v==null)return;

        if (value.indexOf("http") == 0) {// 异步请求网络图片
            if (v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(value))
                return;
            v.setImageResource(mImgResource);
            v.setScaleType(mScaleType);
            if (value.length() < 10)
                return;
            v.setTag(TAG_ID, value);
            if (v.getContext() == null) return;
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(v.getContext())
                    .load(value)
                    .setSaveType(mImgLevel)
                    .build();
            if (bitmapRequest != null){
                AlphaAnimation animation = new AlphaAnimation(0f,1f);
                animation.setDuration(300);
                bitmapRequest.animate(animation);
                bitmapRequest.into(getSubAnimTarget(v, value));
            }
        }
    }

    protected SubAnimTarget getSubAnimTarget(final ImageView v, final String url){
        return new SubAnimTarget(v) {
            @Override
            protected void setResource(Bitmap bitmap) {
                if (bitmap != null && v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(url)) {
                    if (v.getId() == R.id.iv_userImg || v.getId() == R.id.auther_userImg) {
                        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        v.setImageBitmap(UtilImage.toRoundCorner(v.getResources(),bitmap,1,500));
                    }else {
                        v.setScaleType(scaleType);
                        v.setImageBitmap(bitmap);
                    }
                }
            }
        };
    }

    /**
     * 设置key字段中的内容
     * @param map
     * @param textView
     * @param key
     */
      public void setKeyContent(Map<String,String> map, TextView textView, String key){
        if(map.containsKey(key)&& !TextUtils.isEmpty(map.get(key))){
            textView.setText(map.get(key));
            textView.setVisibility(VISIBLE);
        }else textView.setVisibility(GONE);
    }

    public String getMODULE_TAG() {
        return MODULE_TAG;
    }
    public void setMODULE_TAG(String MODULE_TAG) {
        this.MODULE_TAG = MODULE_TAG;
    }
}
