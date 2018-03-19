package amodule.home.view;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.StringManager;
import acore.tools.Tools;
import third.ad.db.bean.AdBean;
import third.ad.db.bean.XHSelfNativeData;
import third.ad.tools.AdConfigTools;
import third.ad.tools.AdPlayIdConfig;
import third.ad.tools.XHSelfAdTools;
import xh.basic.tool.UtilString;

import static third.ad.scrollerAd.XHScrollerAdParent.TAG_BANNER;
import static third.ad.scrollerAd.XHScrollerSelf.showSureDownload;

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
    private Map<String, String> configMap = new HashMap<>();
    private XHSelfNativeData mNativeData;

    public HomeBuoy(Activity act) {
        this.mAct = act;
        // 浮动按钮
        isMove = true;
        isClosed = true;
        handlerData();
    }

    //处理数据
    private void handlerData() {
        AdBean adBean = AdConfigTools.getInstance().getAdConfig(AdPlayIdConfig.HOME_FLOAT);
        if (adBean == null) {
            return;
        }
        ArrayList<Map<String, String>> configArray = StringManager.getListMapByJson(adBean.adConfig);
        Stream.of(configArray)
                .filter(value -> "2".equals(value.get("open"))
                        && TAG_BANNER.equals(value.get("type"))
                        && configMap.isEmpty()
                ).forEach(value -> configMap.putAll(value));
        String adid = analysData(configMap.get("data"));
        if(TextUtils.isEmpty(adid)){
            return;
        }
        XHSelfAdTools.getInstance().loadNativeData(Collections.singletonList(adid), new XHSelfAdTools.XHSelfCallback() {
            @Override
            public void onNativeLoad(List<XHSelfNativeData> list) {
                if(list == null || list.isEmpty()){
                    return;
                }
                mNativeData = list.get(0);
                if(mNativeData != null){
                    //初始化浮标
                    initBuoy();
                    //初始化动画
                    initAnimation();
                    //初始化hanlder
                    initHandler();
                    //绑定点击
                    bindClick(mNativeData.getUrl());
                    //设置图片
                    setBuoyImage(mNativeData.getLittleImage());
                    //显示
                    setFloatMenuData();
                }
            }

            @Override
            public void onNativeFail() {

            }
        });

    }

    private String analysData(String data) {
        LinkedHashMap<String, String> map_link = UtilString.getMapByString(data, "&", "=");
        String adid = "";
        if (map_link.containsKey("adid"))
            adid = map_link.get("adid");
        return adid;
    }

    public void setFloatMenuData() {
        if (null == imageButton) return;
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
        params.setMargins(params.leftMargin, params.topMargin, Tools.getDimen(mAct, R.dimen.dp_11), Tools.getDimen(mAct, R.dimen.dp_34));
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

    private final int CLOSE = 1;
    private final int OPEN = 2;

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
                if(mNativeData != null){
                    if("1".equals(mNativeData.getDbType())){
                        showSureDownload(mNativeData,AdPlayIdConfig.HOME_FLOAT,"xh",mNativeData != null ? mNativeData.getId() : "");
                    }else{
                        AppCommon.openUrl(mAct, floatUrl, true);
                        AdConfigTools.getInstance().postStatistics("click", AdPlayIdConfig.HOME_FLOAT, "xh", mNativeData != null ? mNativeData.getId() : "");
                    }
                }
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
        if (TextUtils.isEmpty(imgUrl)) {
            imageButton.setVisibility(View.GONE);
            return;
        }
        imageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageButton.setVisibility(View.VISIBLE);

        Glide.with(mAct).load(imgUrl).into(imageButton);
        AdConfigTools.getInstance().postStatistics("show", AdPlayIdConfig.HOME_FLOAT, "xh", mNativeData != null ? mNativeData.getId() : "");
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
