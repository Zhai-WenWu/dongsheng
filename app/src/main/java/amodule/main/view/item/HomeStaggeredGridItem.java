package amodule.main.view.item;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.common.utils.MD5Util;
import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.download.tools.FileUtils;
import com.xiangha.R;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import acore.logic.ActivityMethodManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.IconTextSpan;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import third.mall.wx.MD5;

public class HomeStaggeredGridItem extends HomeItem {

//    private ConstraintLayout mRootLayout;
    private ConstraintLayout mContentLayout;
    private ImageView mImg;
    private GifImageView gifImageView;
    private TextView mTitle,num_tv;
    private ImageView auther_userImg,img_fav;
    private int mImgMinHeight, mImgMaxHeight,fixedWidth;

    public HomeStaggeredGridItem(Context context) {
        this(context, null);
    }

    public HomeStaggeredGridItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeStaggeredGridItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_staggered_grid_item);
    }
    @SuppressLint("WrongViewCast")
    @Override
    protected void initView() {
        super.initView();
//        mRootLayout = findViewById(R.id.staggered_root);
        mContentLayout = findViewById(R.id.staggered_container);
        mImg = findViewById(R.id.img);
        gifImageView=findViewById(R.id.gif_img);
        mTitle = findViewById(R.id.title);
        auther_userImg = findViewById(R.id.user_header_img);
        img_fav = findViewById(R.id.img_fav);
        num_tv= findViewById(R.id.num_tv);

        mImgMinHeight = (Tools.getPhoneWidth() - getResources().getDimensionPixelSize(R.dimen.dp_51)) / 2 * 4 / 5;
        mImgMaxHeight = getResources().getDimensionPixelSize(R.dimen.dp_260);
        fixedWidth = (Tools.getPhoneWidth() - getResources().getDimensionPixelSize(R.dimen.dp_51)) / 2;
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if(mDataMap == null)
            return;
        int imgWidth = 0, imgHeight = 0;
        String parseResourceDataWidth = mDataMap.get("parseResourceData_width");
        if (TextUtils.isEmpty(parseResourceDataWidth)) {
            Map<String, String> resourceData = StringManager.getFirstMap(mDataMap.get("resourceData"));
            if (!resourceData.isEmpty()) {
                String widthStr = resourceData.get("width");
                imgWidth = Integer.parseInt(widthStr);
                String heightStr = resourceData.get("height");
                imgHeight = Integer.parseInt(heightStr);
                mDataMap.put("parseResourceData_width", widthStr);
                mDataMap.put("parseResourceData_height", heightStr);
                mDataMap.put("parseResourceData_gif", resourceData.get("gif"));
                mDataMap.put("parseResourceData_img", resourceData.get("img"));
                mDataMap.put("parseResourceData_videoImg", resourceData.get("videoImg"));
            } else {
                Map<String, String> styleData = StringManager.getFirstMap(mDataMap.get("styleData"));
                String type = styleData.get("type");
                if (type != null) {
                    String imgUrl = styleData.get("url");
                    if (!TextUtils.isEmpty(imgUrl)) {
                        int index = imgUrl.lastIndexOf("?");
                        if (index != -1) {
                            String w_h = imgUrl.substring(index + 1);
                            if (!TextUtils.isEmpty(w_h) && w_h.contains("_")) {
                                String[] whs = w_h.split("_");
                                imgWidth = Integer.parseInt(whs[0]);
                                imgHeight = Integer.parseInt(whs[1]);
                                mDataMap.put("parseResourceData_width", whs[0]);
                                mDataMap.put("parseResourceData_height", whs[1]);
                            }
                        }
                    }
                    switch (type) {
                        case "1"://图片
                        case "2"://视频
                            mDataMap.put("parseResourceData_img", imgUrl);
                            break;
                        case "3"://GIF
                            mDataMap.put("parseResourceData_gif", imgUrl);
                            break;
                    }
                }
            }
        } else {
            imgWidth = Integer.parseInt(mDataMap.get("parseResourceData_width"));
            imgHeight = Integer.parseInt(mDataMap.get("parseResourceData_height"));
        }

        int realImgHeight = fixedWidth * imgHeight / (imgWidth < 1 ? 1 : imgWidth);
        if (realImgHeight < mImgMinHeight) {
            realImgHeight = mImgMinHeight;
        } else if (realImgHeight > mImgMaxHeight) {
            realImgHeight = mImgMaxHeight;
        }

        applyViewToLayout(mImg, realImgHeight, mContentLayout);
        applyViewToLayout(gifImageView, realImgHeight, mContentLayout);
        gifImageView.setVisibility(View.GONE);
        mImg.setImageResource(R.drawable.i_nopic);
        if (!TextUtils.isEmpty(mDataMap.get("parseResourceData_gif"))) {
            gifImageView.setTag(TAG_ID, mDataMap.get("parseResourceData_gif"));
            if(ActivityMethodManager.isAppShow) {
                handleGif(mDataMap.get("parseResourceData_gif"));
            }else {
                if(gifImageView.getDrawable()!=null){
                    Drawable drawable=gifImageView.getDrawable();
                    if(drawable instanceof GifDrawable){
                        ((GifDrawable)drawable).stop();
                        ((GifDrawable)drawable).recycle();
                    }
                }
                gifImageView.setImageDrawable(null);
                gifImageView.setVisibility(View.GONE);
                handleImg();
            }
//            Glide.with(getContext()).load(mDataMap.get("parseResourceData_gif")).asGif().centerCrop().placeholder(R.drawable.i_nopic).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mImg);
        } else {
            handleImg();
        }
        mTitle.setText("");
        String title = mDataMap.get("name");
        if (!TextUtils.isEmpty(title)) {
            mTitle.setVisibility(View.VISIBLE);
            if (TextUtils.equals(mDataMap.get("isEssence"), "2")) {
                IconTextSpan.Builder ib = new IconTextSpan.Builder();
                ib.setBgColorInt(getResources().getColor(R.color.icon_text_bg));
                ib.setTextColorInt(getResources().getColor(R.color.c_white_text));
                ib.setText("精选");
                ib.setRadius(2f);
                ib.setRightMargin(3);
                ib.setBgHeight(14f);
                ib.setTextSize(10f);
                StringBuffer sb = new StringBuffer(" ");
                sb.append(title);
                SpannableStringBuilder ssb = new SpannableStringBuilder(sb.toString());
                ssb.setSpan(ib.build(getContext()), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mTitle.setText(ssb);
            } else {
                mTitle.setText(title);
            }
        } else {
            mTitle.setVisibility(View.GONE);
        }
        setImgFav();
        if(mDataMap.containsKey("likeNum")){
            num_tv.setText(mDataMap.get("likeNum"));
        }
        if (TextUtils.isEmpty(mDataMap.get("parseResourceData_customer_img"))) {
            Map<String, String> map = StringManager.getFirstMap(mDataMap.get("customer"));
            mDataMap.put("parseResourceData_customer_img", map.get("img"));
        }
        String userImage = mDataMap.get("parseResourceData_customer_img");
        auther_userImg.setTag(TAG_ID, userImage);
        auther_userImg.setImageResource(R.drawable.i_nopic);
        BitmapRequestBuilder bitmapRequestBuilder = LoadImage.with(getContext()).load(userImage).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build();
        if (bitmapRequestBuilder != null) {
            bitmapRequestBuilder.into(auther_userImg);
        }
        String videoImg = mDataMap.get("parseResourceData_videoImg");
        if (!TextUtils.isEmpty(videoImg)) {
            Glide.with(getContext()).load(videoImg).diskCacheStrategy(DiskCacheStrategy.SOURCE).preload();
        }
    }
    private void setImgFav(){
        img_fav.setImageResource("2".equals(mDataMap.get("isFavorites"))?R.drawable.icon_home_good_selected:R.drawable.icon_home_good_def);
    }
    private void handleImg(){
        String img = mDataMap.get("parseResourceData_img");
        mImg.setTag(TAG_ID, img);
        mImg.setImageResource(R.drawable.i_nopic);
        mImg.setVisibility(VISIBLE);
        BitmapRequestBuilder builder = LoadImage.with(getContext()).load(img).setSaveType(FileManager.save_cache).setPlaceholderId(R.drawable.i_nopic).setErrorId(R.drawable.i_nopic).build();
        if (builder != null) {
            builder.into(mImg);
        }
    }

    private void applyViewToLayout(View targetView, int targetViewHeight, ConstraintLayout targetLayout) {
        ConstraintSet cs = new ConstraintSet();
        cs.constrainWidth(targetView.getId(), ConstraintSet.MATCH_CONSTRAINT);
        cs.constrainHeight(targetView.getId(), targetViewHeight);
        cs.constrainMinHeight(targetView.getId(), mImgMinHeight);
        cs.connect(targetView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        cs.connect(targetView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        cs.connect(targetView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        cs.connect(targetView.getId(), ConstraintSet.BOTTOM, R.id.title, ConstraintSet.TOP);
        cs.applyTo(mContentLayout);
    }

    public ConstraintLayout getContentLayout() {
        return mContentLayout;
    }
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 100:
                    showGitImage(msg.obj.toString());
                    break;
            }
        }
    };
    private void handleGif(String url){
        String md5url= MD5Util.getMD5(url);
        String path= FileManager.getSDCacheDir()+md5url;
        File cacheFile= new File(path);
        if(cacheFile.exists()){//文件已经存在
            showGitImage(path);
            return;
        }
        ReqEncyptInternet.in().getInputStream(url, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                if(flag>= ReqInternet.REQ_OK_IS){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FileUtils.saveSDFile(path,(InputStream) o,false);
                            Message message = handler.obtainMessage(100,path);
                            handler.sendMessage(message);
                        }
                    }).start();
                }
            }
        });
    }
    private void showGitImage(String path){
        if(!TextUtils.isEmpty(path)){
            try {
                gifImageView.setDrawingCacheEnabled(true);
                gifImageView.setVisibility(View.VISIBLE);
                GifDrawable gifDrawable = new GifDrawable(path);
                gifImageView.setImageDrawable(gifDrawable);
            }catch (Exception e){
                e.printStackTrace();
                gifImageView.setVisibility(View.GONE);
            }
        }else {
            gifImageView.setVisibility(View.GONE);
        }
    }
}
