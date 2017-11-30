package amodule.home.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.xiangha.R;

import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.LoadImage;
import third.ad.tools.AdConfigTools;
import third.ad.tools.AdPlayIdConfig;

/**
 * Description :
 * PackageName : amodule.home.view
 * Created on 2017/11/25 13:57.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeBuoy {
    private Activity mAct;
    // 活动浮标所有东西
    private Handler mainFloatHandler;
    private boolean isMove = false;// 活动图标是否全部滑出
    private boolean isClosed = false;
    private Animation close;// 关闭动画
    private Animation open;// 打开动画
    private ImageView imageButton;
    private OnClickCallback mClickCallback;

    public HomeBuoy(Activity act) {
        this.mAct = act;
        // 浮动按钮
        isMove = true;
        isClosed = true;
        Map<String, String> dataMap = AdConfigTools.getInstance().getAdConfigData(AdPlayIdConfig.HOME_FLOAT);
        if(dataMap.isEmpty()){
            return;
        }
        handlerData(dataMap);
    }

    //处理数据
    private void handlerData(Map<String, String> buoyData){
        Map<String,String> adConfigMap = StringManager.getFirstMap(buoyData.get("adConfig"));
        final String[] keys = {"1","2","3","4",};
        Map<String,String> bannerMap = new HashMap<>();
        for(String key:keys){
            Map<String,String> tempMap = StringManager.getFirstMap(adConfigMap.get(key));
            if("personal".equals(tempMap.get("type"))
                    && "2".equals(tempMap.get("open"))){
                bannerMap = StringManager.getFirstMap(buoyData.get("banner"));
                break;
            }
        }
        //储存数据
        String countStr = FileManager.loadShared(mAct, FileManager.xmlFile_appInfo, AdPlayIdConfig.HOME_FLOAT).toString();
        final String path = FileManager.getDataDir() + AdPlayIdConfig.HOME_FLOAT;
        String originalData = FileManager.readFile(path).trim();
        String currentData = Tools.map2Json(buoyData);
        //如果不一样，重新存储数据
        if (!currentData.equals(originalData)) {
            FileManager.saveFileToCompletePath(FileManager.getSDCacheDir() + AdPlayIdConfig.HOME_FLOAT, currentData, false);
            FileManager.saveShared(mAct, FileManager.xmlFile_appInfo, AdPlayIdConfig.HOME_FLOAT, "0");
        }
        int count = TextUtils.isEmpty(countStr) ? 0 : Integer.parseInt(countStr);
        int defaultMaxCount = 3;
        final String showNumValue = bannerMap.get("showNum");
        if (!TextUtils.isEmpty(showNumValue)
                && !"null".equals(showNumValue)) {
            defaultMaxCount = Integer.parseInt(showNumValue);
        }
        if (count < defaultMaxCount) {
            //初始化浮标
            initBuoy();
            //初始化动画
            initAnimation();
            //初始化hanlder
            initHandler();
            //绑定点击
            bindClick(bannerMap.get("url"));
            //设置图片
            setBuoyImage(StringManager.getFirstMap(bannerMap.get("imgs")).get("rightFloatImg"));
            //显示
            setFloatMenuData();
            FileManager.saveShared(mAct, FileManager.xmlFile_appInfo, AdPlayIdConfig.HOME_FLOAT, String.valueOf(++count));
        } else {
            hide();
        }
    }

    public void setFloatMenuData() {
        if(null == imageButton) return;
        show();
        if (isClosed) {
            imageButton.startAnimation(isMove ? open : close);
            isClosed = false;
        }
    }

    private void initBuoy() {
        if (mAct == null)
            return;
        imageButton = new ImageView(mAct);
        int width = Tools.getDimen(mAct, R.dimen.dp_45);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width);
        params.setMargins(params.leftMargin, params.topMargin, Tools.getDimen(mAct,R.dimen.dp_11), Tools.getDimen(mAct,R.dimen.dp_34));
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        RelativeLayout rootLayout = (RelativeLayout) mAct.findViewById(R.id.activityLayout);
        if (rootLayout == null) {
            return;
        }
        rootLayout.addView(imageButton, params);
        hide();//初始化完成后hide浮标
    }

    private void initAnimation() {
        float floatAnimation = Tools.getDimen(mAct, R.dimen.dp_35);
        close = new TranslateAnimation(0, 0 + floatAnimation, 0, 0);
        close.setFillEnabled(true);
        close.setFillAfter(true);
        close.setDuration(300);

        open = new TranslateAnimation(0 + floatAnimation, 0, 0, 0);
        open.setFillEnabled(true);
        open.setFillAfter(true);
        open.setDuration(300);
    }

    public final int CLOSE = 1;
    public final int OPEN = 2;

    private void initHandler() {
        mainFloatHandler = new Handler(Looper.getMainLooper(), msg -> {
            switch (msg.what) {
                case CLOSE:
                    if (isMove)
                        executeCloseAnim();
                    break;
                case OPEN:
                    if (!isMove)
                        executeOpenAnim();
                    break;
            }
            return false;
        });
    }

    private void bindClick(final String floatUrl) {
        if (imageButton == null) {
            return;
        }
        imageButton.setOnClickListener(v -> {
            if (isMove) {
                AppCommon.openUrl(mAct, floatUrl, true);
//                    executeCloseAnim();
                if (mClickCallback != null) {
                    mClickCallback.onClick();
                }
            } else {
                executeOpenAnim();
            }
        });
    }

    /**
     * 处理图片
     *
     * @param imgUrl 图片链接
     */
    private void setBuoyImage(String imgUrl) {
        if (imageButton == null) return;
        imageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mAct)
                .load(imgUrl)
                .setPlaceholderId(R.drawable.z_quan_float_activity)
                .setErrorId(R.drawable.z_quan_float_activity)
                .build();
        if (bitmapRequest != null)
            bitmapRequest.into(imageButton);
    }

    public void executeOpenAnim() {
        if (imageButton == null) return;
        if (isMove) return;
        imageButton.startAnimation(open);
        isMove = true;
    }

    public void executeCloseAnim() {
        if (imageButton == null) return;
        if (!isMove) return;
        imageButton.startAnimation(close);
        isMove = false;
    }

    public void clearAnimation() {
        if (imageButton != null) {
            imageButton.clearAnimation();
        }
    }

    public void show() {
        if (imageButton != null) {
            imageButton.setVisibility(View.VISIBLE);
        }
    }

    public void hide() {
        if (imageButton != null) {
            imageButton.setVisibility(View.GONE);
        }
    }

    public void setMove(boolean isMove) {
        this.isMove = isMove;
    }

    public void setClosed(boolean isClosed) {
        this.isClosed = isClosed;
    }

    public boolean isMove() {
        return isMove;
    }

    public void setClickCallback(OnClickCallback clickCallback) {
        mClickCallback = clickCallback;
    }

    public interface OnClickCallback {
        void onClick();
    }
}
