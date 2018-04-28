package amodule.dish.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.GifRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;

/**
 * adapter菜谱详情页
 */
public class DishStepView extends ItemBaseView {
    public static String DISH_STYLE_STEP="dish_style_step";
    public static final  int DISH_STYLE_STEP_INDEX=1;
    private TextView itemText1;
    private Map<String,String> map;
    private ImageView loadProgress;
    private ImageView itemImg1,itemGif,itemGifHint;
    private StepViewCallBack callback;
    private boolean isHasVideo = false;
    private int position;

    public DishStepView(Context context) {
        super(context, R.layout.a_dish_step_item);
    }

    public DishStepView(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs, R.layout.a_dish_step_item);
    }

    public DishStepView(Context context, AttributeSet attrs, int defStyleAttr, int layoutId) {
        super(context, attrs, defStyleAttr, R.layout.a_dish_step_item);
    }

    @Override
    public void init() {
        super.init();
        itemText1 = (TextView) findViewById(R.id.itemText1);
        loadProgress = (ImageView) findViewById(R.id.load_progress);
        itemImg1 = (ImageView) findViewById(R.id.itemImg1);
        itemGif = (ImageView) findViewById(R.id.a_dish_stem_item_gif);
        itemGifHint = (ImageView) findViewById(R.id.dish_step_gif_hint);
    }

    /**
     * 隐藏边距
     */
    public void isDistance(boolean isShow){
        findViewById(R.id.step_distance).setVisibility(View.GONE);
        itemText1.setVisibility(isShow?View.VISIBLE:View.GONE);
    }
    public void hideStepDistance(boolean isShow){
        findViewById(R.id.step_distance).setVisibility(isShow?View.GONE:View.VISIBLE);
    }
    public void setData(Map<String, String> maps, StepViewCallBack stepViewCallBack, int position) {
        this.map = maps;
        imgWidth=Tools.getPhoneWidth()-Tools.getDimen(context,R.dimen.dp_40);
        this.position= position;
        this.callback = stepViewCallBack;
        if(!TextUtils.isEmpty(map.get("info").trim())) {
            String text = "<b><tt>" + map.get("num") + ".</tt></b>";
            int size_num = text.length();
            text += map.get("info").trim();
            text = text.replace("\n", "").replace("\r", "");
            itemText1.setText(Html.fromHtml(text));
            itemText1.setVisibility(View.VISIBLE);
        }else if(!TextUtils.isEmpty(map.get("num").trim())){
            String text = "<b><tt>" + map.get("num") + "</tt></b>";
            itemText1.setText(Html.fromHtml(text));
            itemText1.setVisibility(View.VISIBLE);
        }else itemText1.setVisibility(View.GONE);
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClick();
            }
        });
        //数据
        itemGifHint.setVisibility(View.GONE);
        itemImg1.setVisibility(View.VISIBLE);
        loadProgress.clearAnimation();
        loadProgress.setVisibility(View.GONE);
        itemImg1.setImageResource(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon);
        itemGifHint.setImageResource(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon);

        isHasVideo = false;
        //视频信息
        if(map.containsKey("video")&&!TextUtils.isEmpty(map.get("video"))){
            findViewById(R.id.view_linear).setVisibility(View.VISIBLE);
            ArrayList<Map<String,String>> videoArray = StringManager.getListMapByJson(map.get("video"));
            if(videoArray.size() > 0){
                Map<String,String> videoMap = videoArray.get(0);
                final String gifUrl = videoMap.get("gif");
                final String imgUrl = videoMap.get("img");
                if(!TextUtils.isEmpty(gifUrl)){
                    findViewById(R.id.img_view).setVisibility(View.GONE);
                    isHasVideo = true;
                    loadImg(imgUrl);
                    handlerImgHeight(imgUrl);
                    itemImg1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadGif(gifUrl);
                        }
                    });
                    itemGif.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            itemGifHint.setVisibility(View.VISIBLE);
                            itemImg1.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }
         if(!isHasVideo){
             if( map.containsKey("img")&&!TextUtils.isEmpty(map.get("img"))){
                 findViewById(R.id.view_linear).setVisibility(View.VISIBLE);
                 itemImg1.setVisibility(View.VISIBLE);
                 String img = map.get("img");
                //YLKLog.i("xianghaTag","img:存在::"+img);
                 handlerImgHeight(img);
                 setViewImage(itemImg1, map.get("img"));
//                 if (map.containsKey("height") && Integer.parseInt(map.get("height")) > 0) {
//                     setImageWH(itemImg1, Integer.parseInt(map.get("height")));
//                 }
             }else{
                 findViewById(R.id.view_linear).setVisibility(View.GONE);
                 itemImg1.setVisibility(View.GONE);
             }

        }
    }
    private void handlerImgHeight(String img ){
        if(img.contains("?")&&!map.containsKey("height")){
            String temp = img.substring(img.indexOf("?")+1,img.length());
            if(temp.contains("_")){
                String img_waith= temp.substring(0,temp.indexOf("_"));
                String img_height= temp.substring(temp.indexOf("_")+1,temp.length());
                int viewWaith=Tools.getPhoneWidth()-Tools.getDimen(context,R.dimen.dp_40);
                if(!TextUtils.isEmpty(img_waith)&&!TextUtils.isEmpty(img_height)&&stringIsNum(img_height)&&stringIsNum(img_waith)&&viewWaith>0){
                    double intTemp=Double.parseDouble(img_height)/Double.parseDouble(img_waith);
                    int viewHeight= (int) (intTemp*viewWaith);
                    map.put("height",String.valueOf(viewHeight));
                    RelativeLayout.LayoutParams layoutParams= new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,viewHeight);
                    itemImg1.setLayoutParams(layoutParams);
                }
            }
        }else if(map.containsKey("height")&&!TextUtils.isEmpty(map.get("height"))){
           //YLKLog.i("xianghaTag","height存在：：：");
            RelativeLayout.LayoutParams layoutParams= new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,Integer.parseInt(map.get("height")));
            itemImg1.setLayoutParams(layoutParams);
        }
    }

    /**
     * 字符串是否数字
     * @param str
     * @return
     */
    private boolean  stringIsNum(String str){
        if(TextUtils.isEmpty(str))return false;
        return str.matches("[0-9]+");
    }
    /**
     * gif不进行显示
     */
    public void stopGif(){
        itemGifHint.setVisibility(View.VISIBLE);
        itemImg1.setVisibility(View.VISIBLE);
        loadProgress.setVisibility(GONE);
    }

    private void loadImg(String imgUrl){
        if(!TextUtils.isEmpty(imgUrl)) {
            itemImg1.setImageResource(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon);
            itemImg1.setScaleType(ImageView.ScaleType.CENTER_CROP);
            itemImg1.setTag(TAG_ID, imgUrl);
            BitmapRequestBuilder<GlideUrl, Bitmap> requestBuilder = LoadImage.with(context)
                    .load(imgUrl)
                    .setImageRound(roundImgPixels)
                    .setPlaceholderId(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                    .setErrorId(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                    .setSaveType(imgLevel)
                    .build();
            if(requestBuilder != null){
                itemGifHint.setVisibility(View.VISIBLE);
                itemGifHint.setImageResource(R.drawable.i_dish_detail_gif_hint);
                requestBuilder.into(getTarget(itemImg1, imgUrl));
            }
        }
    }

    /**
     * 设置GIF
     * @param gifUrl
     */
    private void loadGif(final String gifUrl){
        if(!TextUtils.isEmpty(gifUrl)){
            if(itemGif.getTag() == null)
                itemGif.setTag(TAG_ID, gifUrl);
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.loading_anim);
            loadProgress.startAnimation(animation);
            loadProgress.setVisibility(VISIBLE);
            itemImg1.setVisibility(VISIBLE);
            itemGifHint.setVisibility(View.GONE);

            GifRequestBuilder requestBuilder =  Glide.with(getContext())
                    .load(gifUrl)
                    .asGif()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(new RequestListener<String, GifDrawable>() {
                        @Override
                        public boolean onException(Exception e, String s, Target<GifDrawable> target, boolean b) {
                           //YLKLog.i("xianghaTag","e"+(e == null ? "Message is null." : e.getMessage()));
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(GifDrawable gifDrawable, String s, Target<GifDrawable> target, boolean b, boolean b1) {
                            if (itemGif.getTag(TAG_ID).equals(gifUrl)) {
                                loadProgress.clearAnimation();
                                loadProgress.setVisibility(GONE);
                                itemImg1.setVisibility(View.GONE);
                                itemGifHint.setVisibility(View.GONE);
//                                setImageWH(itemGif, itemImg1.getHeight());
                            }
                            return false;
                        }
                    });

            if(itemGif != null){
                if (itemGif.getTag(TAG_ID).equals(gifUrl)){
                   requestBuilder.into(itemGif);
                    itemGif.setVisibility(VISIBLE);
                }
            }
            if(callback!=null)callback.onGifPlayClick();
        }
    }

    private void setImageWH(ImageView imgView, int imgHeight){
        imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int dp_290= Tools.getDimen(context, R.dimen.dp_290);
        RelativeLayout.LayoutParams layoutParams;
        if(isHasVideo){
            imgView.setMinimumHeight(0);
            layoutParams = new RelativeLayout.LayoutParams((int) (imgHeight / 9.0 * 16),imgHeight);
        }else {
            layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, imgHeight > dp_290 ? dp_290 : imgHeight);
        }int dp_12= Tools.getDimen(context, R.dimen.dp_12);
        int dp_8= Tools.getDimen(context, R.dimen.dp_8);
        layoutParams.setMargins(0,dp_12,0,0);
        imgView.setLayoutParams(layoutParams);
    }

    public interface StepViewCallBack{
        public void getHeight(String height);
        public void onClick();
        public void onGifPlayClick();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(this!=null){
            context=null;
        }
    }
}
