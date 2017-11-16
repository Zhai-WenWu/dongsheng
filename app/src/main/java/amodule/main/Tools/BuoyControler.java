package amodule.main.Tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
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
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;

/**
 * Description : 浮动按钮控制
 * PackageName : amodule.main.Tools
 * Created by MrTrying on 2017/11/16 10:59.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class BuoyControler {
    public static final String TYPE_HOME = "XHFloat";
    public static final String TYPE_DS = "DSFloat";

    public static boolean loadOver = false;

    public static Map<String, String> map = new HashMap<>();
    //获取活动数据
    public static void getBuoyDataFromService(boolean isRefresh,Context context,final LoadDataCallback callback) {
        if(loadOver && !isRefresh){
            if(callback != null)
                callback.onLoad(map);
            return;
        }
        //请求数据
        ReqInternet.in().doGet(StringManager.API_GET_ACTIVITY_BOUY, new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    loadOver = true;
                    map =StringManager.getFirstMap(returnObj);
                    if(callback != null)
                        callback.onLoad(map);
                }
            }
        });
    }

    public static Map<String, String> getBuoyData(){
        return map;
    }

    public interface LoadDataCallback{
        void onLoad(Map<String, String> map);
    }

    public static class Buoy {
        private Activity mAct;
        // 活动浮标所有东西
        private Handler mainFloatHandler;
        private boolean isMove = false;// 活动图标是否全部滑出
        private boolean isClosed = false;
        private Animation close;// 关闭动画
        private Animation open;// 打开动画
        private ImageView imageButton;
        private String mType;

        public Buoy(Activity act, @NonNull final String type){
            Log.i("tzy","create Buoy");
            this.mAct = act;
            this.mType = type;
            // 浮动按钮
            isMove = true;
            initBuoy();
            initAnimation();
            initHandler();
            refresh(false);
        }

        public void refresh(boolean isRefresh){
            // 加载数据(悬浮按钮)
            getBuoyDataFromService(isRefresh,mAct, map -> {
                if(TextUtils.isEmpty(mType) || !map.containsKey(mType))
                    return;
                Map<String,String> buoyData = StringManager.getFirstMap(map.get(mType));
                if(buoyData.isEmpty()) return;
                bindClick(buoyData.get("url"));
                setBuoyImage(buoyData.get("img"));
                setFloatMenuData();
                mainFloatHandler.sendEmptyMessageDelayed(CLOSE,3*1000);
            });
        }

        public void setFloatMenuData() {
            show();
            if (isClosed) {
                imageButton.startAnimation(isMove ? open :close);
                isClosed = false;
            }
        }

        private void initBuoy(){
            if(mAct == null)
                return;
            imageButton = new ImageView(mAct);
            int width = Tools.getDimen(mAct, R.dimen.dp_45);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width);
            DisplayMetrics dm = ToolsDevice.getWindowPx(mAct);
            params.setMargins(params.leftMargin, dm.heightPixels / 5 * 2,params.rightMargin, params.bottomMargin);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            RelativeLayout rootLayout = (RelativeLayout) mAct.findViewById(R.id.activityLayout);
            if(rootLayout == null){
                return;
            }
            rootLayout.addView(imageButton,params);
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
            open.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mainFloatHandler.sendEmptyMessageDelayed(CLOSE,5*1000);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        public final int CLOSE = 1;
        public final int OPEN = 2;
        private void initHandler() {
            mainFloatHandler = new Handler(Looper.getMainLooper(),msg -> {
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
            if(imageButton == null){
                return;
            }
            imageButton.setOnClickListener(v -> {
                if (isMove) {
                    AppCommon.openUrl(mAct,floatUrl, true);
                    executeCloseAnim();
                } else {
                    executeOpenAnim();
                }
            });
        }

        /**
         * 处理图片
         * @param imgUrl 图片链接
         */
        private void setBuoyImage(String imgUrl) {
            if(imageButton == null) return;
            imageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mAct)
                    .load(imgUrl)
                    .setPlaceholderId(R.drawable.z_quan_float_activity)
                    .setErrorId(R.drawable.z_quan_float_activity)
                    .build();
            if(bitmapRequest != null)
                bitmapRequest.into(imageButton);
        }

        public void executeOpenAnim() {
            if(isMove) return;
            imageButton.startAnimation(open);
            isMove = true;
        }

        public void executeCloseAnim() {
            if(!isMove) return;
            imageButton.startAnimation(close);
            isMove = false;
        }

        public void clearAnimation(){
            if(imageButton != null){
                imageButton.clearAnimation();
            }
        }

        public void show(){
            if(imageButton != null){
                imageButton.setVisibility(View.VISIBLE);
            }
        }

        public void hide(){
            if(imageButton != null){
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
    }
}
