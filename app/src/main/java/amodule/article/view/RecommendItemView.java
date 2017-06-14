package amodule.article.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.Map;

import java.util.Map;

import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import amodule.article.activity.ArticleDetailActivity;
import amodule.article.activity.VideoDetailActivity;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/25 14:38.
 * E_mail : ztanzeyu@gmail.com
 */

public class RecommendItemView extends ItemBaseView {

    private ImageView recImage;
    private TextView recTitle;
    private TextView recCustomerName;
    private TextView recBrowse;
    private TextView recComment;

    public RecommendItemView(Context context) {
        super(context, R.layout.a_article_recommend_item);
    }

    public RecommendItemView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.a_article_recommend_item);
    }

    public RecommendItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.a_article_recommend_item);
    }

    @Override
    public void init() {
        recImage = (ImageView) findViewById(R.id.rec_image);
        recTitle = (TextView) findViewById(R.id.rec_title);
        recCustomerName = (TextView) findViewById(R.id.rec_customer_name);
        recBrowse = (TextView) findViewById(R.id.rec_browse);
        recComment = (TextView) findViewById(R.id.rec_comment);
    }

    public void setData(final Map<String, String> map) {
        findViewById(R.id.hander).setVisibility(map.containsKey("showheader") ? View.VISIBLE : View.GONE);
        if(map != null && !TextUtils.isEmpty(map.get("img"))){
            setViewImage(recImage, map.get("img"));
            recImage.setVisibility(VISIBLE);
        }else
            recImage.setVisibility(GONE);
        setViewText(recTitle, map, "title", View.INVISIBLE);
        setViewText(recBrowse, map, "clickAll");
        setViewText(recComment, map, "commentNumber");
        if (map.containsKey("customer")) {
            Map<String, String> customer = StringManager.getFirstMap(map.get("customer"));
            setViewText(recCustomerName, customer, "nickName");
            findViewById(R.id.gourmet_icon)
                    .setVisibility(customer.containsKey("isGourmet") && "2".equals(customer.get("isGourmet")) ? View.VISIBLE : View.GONE);
        }
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if("2".equals(map.get("isAd"))){
                    if(onAdClickCallback != null){
                        onAdClickCallback.onAdClick(RecommendItemView.this);
                    }
                    return;
                }
                //开启文章
                if(map.containsKey("code") && !TextUtils.isEmpty(map.get("code"))
                        && map.containsKey("type") && !TextUtils.isEmpty(map.get("type"))){
                    String type = map.get("type");
                    Intent intent = new Intent();
                    switch (type){
                        case "1":
                            intent.setClass(getContext(),ArticleDetailActivity.class);
                            break;
                        case "2":
                            intent.setClass(getContext(),VideoDetailActivity.class);
                            break;
                    }
                    intent.putExtra("code",map.get("code"));
                    getContext().startActivity(intent);
                }
            }
        });


    }

    @Override
    public SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                ImageView img = null;
                if (v.getTag(TAG_ID).equals(url))
                    img = v;
                if (img != null && bitmap != null) {
                    // 图片圆角和宽高适应auther_userImg
                    if (v.getId() == R.id.iv_userImg || v.getId() == R.id.auther_userImg) {
                        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
//						bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, 1, ToolsDevice.dp2px(context, 500));
                        v.setImageBitmap(UtilImage.makeRoundCorner(bitmap));
                    } else {
                        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
                        if (isAnimate) {
//							AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
//							alphaAnimation.setDuration(300);
//							v.setAnimation(alphaAnimation);
                        }
                    }
                    if(v.getId() == R.id.rec_image){
                        findViewById(R.id.imgs).setVisibility(v.getVisibility());
                    }
                }
            }
        };
    }

    private OnAdClickCallback onAdClickCallback;
    public interface OnAdClickCallback{
        public void onAdClick(View view);
    }

    public OnAdClickCallback getOnAdClickCallback() {
        return onAdClickCallback;
    }

    public void setOnAdClickCallback(OnAdClickCallback onAdClickCallback) {
        this.onAdClickCallback = onAdClickCallback;
    }
}
