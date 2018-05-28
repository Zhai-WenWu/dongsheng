package amodule.dish.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import amodule.dish.activity.DetailDish;
import amodule.dish.activity.VideoDish;
import amodule.user.activity.FriendHome;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;
import xh.basic.tool.UtilImage;

import static third.ad.scrollerAd.XHScrollerAdParent.ADKEY_BAIDU;
import static third.ad.scrollerAd.XHScrollerAdParent.ADKEY_GDT;
import static third.ad.scrollerAd.XHScrollerAdParent.ID_AD_ICON_GDT;

/**
 * Title:AdapterListDish.java Copyright: Copyright (c) 2014~2017
 *
 * @author FangRuijiao
 * @date 2016年07月07日
 */
public class AdapterTimeDish extends AdapterSimple {
    private List<? extends Map<String, ?>> data;
    private BaseActivity mAct;
    public int viewHeight = 0;
    private XHAllAdControl adControl;

    public AdapterTimeDish(BaseActivity act, View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(parent, data, resource, from, to);
        this.data = data;
        this.mAct = act;
    }

    @Override
    public void setViewImage(ImageView v, String value) {
        if (v.getId() != R.id.iv_userType)
            super.setViewImage(v, value);
        if(v.getId() == R.id.ad_hint_imv){
            if(value.equals("hide") || value.length() == 0)
                v.setVisibility(View.GONE);
            else {
                v.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                layoutParams.width = Tools.getDimen(mAct,R.dimen.dp_40);
                layoutParams.height = Tools.getDimen(mAct,R.dimen.dp_18);
            }
        }else{
            super.setViewImage(v,value);
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        final Map<String, String> map = (Map<String, String>) data.get(position);

        View iv_itemIsSolo = view.findViewById(R.id.iv_itemIsSolo);
        if ("2".equals(map.get("isPromotion"))) {
            view.findViewById(R.id.dish_recom_item_today_layout).setVisibility(View.GONE);
            view.findViewById(R.id.iv_userType).setVisibility(View.GONE);
            iv_itemIsSolo.setVisibility(View.INVISIBLE);

            adControl.onAdBind(Integer.valueOf(map.get("indexOnData")), view, "");

            AppCommon.setAdHintClick(mAct,view.findViewById(R.id.ad_hint_imv),adControl,Integer.valueOf(map.get("indexOnData")), "");
            setAdItemListener(view, map);
            setAdItemListener(view.findViewById(R.id.iv_userImg), map);
            setAdItemListener(view.findViewById(R.id.user_name), map);
            View gdtIcon = view.findViewById(ID_AD_ICON_GDT);
            if(gdtIcon != null){
                gdtIcon.setVisibility(ADKEY_GDT.equals(map.get("type"))?View.VISIBLE:View.GONE);
            }
        } else {
            TextView today = (TextView) view.findViewById(R.id.dish_recom_item_today);
            if (today.getVisibility() == View.VISIBLE) {
                view.findViewById(R.id.dish_recom_item_today_layout).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.dish_recom_item_today_layout).setVisibility(View.GONE);
            }

            ImageView iv_userType = (ImageView) view.findViewById(R.id.iv_userType);
            if (TextUtils.isEmpty(map.get("isGourment"))) {
                AppCommon.setUserTypeImage(Integer.parseInt(map.get("isGourmet")), iv_userType);
            }

            if (map.get("isExclusive") != null && map.get("isExclusive").equals("2")) {
                iv_itemIsSolo.setVisibility(View.VISIBLE);
            } else {
                iv_itemIsSolo.setVisibility(View.INVISIBLE);
            }

            userClick(view.findViewById(R.id.iv_userImg), map.get("userCode"));
            userClick(view.findViewById(R.id.user_name), map.get("userCode"));
            userClick(iv_userType, map.get("userCode"));
            dishItemClick(view,map.get("code"),map.get("name"));
        }
        return view;
    }


    private void setAdItemListener(View view, final Map<String, String> map) {

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adControl.onAdClick(Integer.valueOf(map.get("indexOnData")), "");
            }
        });


    }

    private void userClick(View v, final String userCode) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(mAct, VideoDish.STATISTICS_ID, "用户头像点击 ", "");
                Intent intent = new Intent(mAct, FriendHome.class);
                intent.putExtra("code", userCode);
                mAct.startActivity(intent);
            }
        });
    }

    private void dishItemClick(View v, final String userCode, final String userName){
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mAct, DetailDish.class);
                intent.putExtra("code", userCode);
                intent.putExtra("name", userName);
                mAct.startActivity(intent);
            }
        });
    }

    @Override
    public SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                ImageView img = null;
                if (v.getTag(TAG_ID).equals(url))
                    img = v;
                if (img != null && bitmap != null) {
                    // 图片圆角和宽高适应auther_userImg
                    if (v.getId() == R.id.iv_userImg || v.getId() == R.id.auther_userImg) {
                        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                        bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, 1, ToolsDevice.dp2px(mParent.getContext(), 500));
//                        v.setImageBitmap(bitmap);

                        v.setImageBitmap(circleBitmapByShader(bitmap,bitmap.getWidth(),bitmap.getWidth()/2));
                    } else {
                        v.setScaleType(scaleType);
                        UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
                        if (isAnimate) {
//							AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
//							alphaAnimation.setDuration(300);
//							v.setAnimation(alphaAnimation);
                        }
                    }
                }
            }
        };
    }

    public void setAdControl(@NonNull XHAllAdControl adControl) {
        this.adControl = adControl;
    }


    private Bitmap circleBitmapByShader(Bitmap bitmap, int edgeWidth, int radius) {
        if(bitmap == null) {
            throw new NullPointerException("Bitmap can't be null");
        }
        float btWidth = bitmap.getWidth();
        float btHeight = bitmap.getHeight();
        // 水平方向开始裁剪的位置
        float btWidthCutSite = 0;
        // 竖直方向开始裁剪的位置
        float btHeightCutSite = 0;
        // 裁剪成正方形图片的边长，未拉伸缩放
        float squareWidth = 0f;
        if(btWidth > btHeight) { // 如果矩形宽度大于高度
            btWidthCutSite = (btWidth - btHeight) / 2f;
            squareWidth = btHeight;
        } else { // 如果矩形宽度不大于高度
            btHeightCutSite = (btHeight - btWidth) / 2f;
            squareWidth = btWidth;
        }

        // 设置拉伸缩放比
        float scale = edgeWidth * 1f / squareWidth;
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);

        // 将矩形图片裁剪成正方形并拉伸缩放到控件大小
        Bitmap squareBt = Bitmap.createBitmap(bitmap, (int)btWidthCutSite, (int)btHeightCutSite, (int)squareWidth, (int)squareWidth, matrix, true);

        // 初始化绘制纹理图
        BitmapShader bitmapShader = new BitmapShader(squareBt, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        // 初始化目标bitmap
        Bitmap targetBitmap = Bitmap.createBitmap(edgeWidth, edgeWidth, Bitmap.Config.ARGB_8888);

        // 初始化目标画布
        Canvas targetCanvas = new Canvas(targetBitmap);

        // 初始化画笔
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(bitmapShader);

        // 利用画笔绘制圆形图
        targetCanvas.drawRoundRect(new RectF(0, 0, edgeWidth, edgeWidth), radius, radius, paint);

        return targetBitmap;
    }
}

