package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.github.chrisbanes.photoview.PhotoView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.Tools;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;

import static amodule.dish.activity.DetailDish.tongjiId;

/**
 * 图片预览单个view
 * Created by XiangHa on 2016/8/10.
 */
public class ImageMoreCommenView extends ImageMoreView implements View.OnClickListener {

    private Activity mAct;
    private Map<String, String> mData;
    private View mParentView;

    private PhotoView imageTouchView;

    private RelativeLayout contentParentRl;
    private ScrollView contentParentScr;
    private TextView num,numHe, num_hint,num_he_hint, content;
    private Animation inAnimation, outAnimation;

    private boolean isShowContent = true;

    public ImageMoreCommenView(Activity con, Map<String, String> data) {
        mAct = con;
        mData = data;
        LayoutInflater inflater = LayoutInflater.from(con);
        mParentView = inflater.inflate(R.layout.a_dish_more_imageview_item, null);
        init();
    }

    private void init() {
        imageTouchView = (PhotoView) mParentView.findViewById(R.id.view_ad_img);
        ImageView close = (ImageView) mParentView.findViewById(R.id.a_dish_more_img_item_close);
        if(mAct!=null) {
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mAct)
                    .load(mData.get("img"))
                    .setSaveType(FileManager.save_cache)
                    .build();
            if (bitmapRequest != null) {
                bitmapRequest.into(new SubBitmapTarget() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
//                    imageTouchView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageTouchView.setImageBitmap(bitmap);
                    }
                });
            }
        }

        imageTouchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(mAct, tongjiId, "菜谱区域的点击", "步骤图大图-单击");
                changeContentVisibility();
            }
        });
        close.setOnClickListener(this);
        initContentView();
        setContent();
    }

    private void initContentView() {
        List<Map<String, String>> list = new ArrayList<>();
        list.add(mData);
        contentParentRl = (RelativeLayout) mParentView.findViewById(R.id.a_dish_more_img_item_content_parent_rl);
        contentParentScr = (ScrollView) mParentView.findViewById(R.id.a_dish_more_img_item_content_parent_sc);
        num = (TextView) mParentView.findViewById(R.id.a_dish_more_img_item_num);
        numHe = (TextView) mParentView.findViewById(R.id.a_dish_more_img_item_num_he);
        num_hint = (TextView) mParentView.findViewById(R.id.a_dish_more_img_item_num_hint);
        num_he_hint = (TextView) mParentView.findViewById(R.id.a_dish_more_img_item_num__he_hint);
        content = (TextView) mParentView.findViewById(R.id.view_ad_text);

        contentParentRl.setOnClickListener(this);
        num_hint.setOnClickListener(this);
        inAnimation = AnimationUtils.loadAnimation(mAct, R.anim.in_from_botoom);
        outAnimation = AnimationUtils.loadAnimation(mAct, R.anim.out_to_bottom);
        inAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                num_hint.setVisibility(View.GONE);
                num_he_hint.setVisibility(View.GONE);
                contentParentScr.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        outAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                contentParentScr.setVisibility(View.GONE);
                if(isShowContent){
                    num_hint.setVisibility(View.VISIBLE);
                    num_he_hint.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void setContent() {
        if(TextUtils.isEmpty(mData.get("info"))){
            isShowContent = false;
            contentParentRl.setVisibility(View.GONE);
        }
        content.setText(mData.get("info"));
        setImageViewHeight(mAct,contentParentScr,content);
        if (TextUtils.isEmpty(mData.get("img"))) {
            contentParentRl.setOnClickListener(null);
            setContentViewHeight();
        }
        num.setText(mData.get("num"));
        numHe.setText(mData.get("numHe"));
        num_hint.setText(mData.get("num"));
        num_he_hint.setText(mData.get("numHe"));
    }

    /**
     * 设置内容layout的高度
     */
    private void setContentViewHeight() {
        WindowManager wm = (WindowManager) mAct.getSystemService(Context.WINDOW_SERVICE);
        int screenH = wm.getDefaultDisplay().getHeight();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) contentParentScr.getLayoutParams();
        params.height = screenH;
        params.setMargins(0, Tools.getDimen(mAct, R.dimen.dp_100), 0, 0);
    }

    /**
     * 点击控制区域，改变内容显示是否
     */
    private void changeContentVisibility() {
        if (IS_SHOW) {
            contentParentScr.clearAnimation();
            contentParentScr.startAnimation(outAnimation);
        } else {
            contentParentScr.clearAnimation();
            contentParentScr.startAnimation(inAnimation);
        }
        IS_SHOW = !IS_SHOW;
    }

    @Override
    public void setOnClick() {
//        imageTouchView.setOnClickListener(new PhotoViewAttacher.OnClickListener() {
//            @Override
//            public void onClick() {
//                changeContentVisibility();
//            }
//        });
        imageTouchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeContentVisibility();
            }
        });
    }

    @Override
    public View getImageMoreView() {
        return mParentView;
    }

    @Override
    public void switchNextPage() {
        if (imageTouchView.isZoomEnabled()) imageTouchView.setScale(1.0f);
    }

    @Override
    public void onPageChange() {
        if (!TextUtils.isEmpty(mData.get("img"))) {
            if (IS_SHOW) {
                contentParentScr.setVisibility(View.VISIBLE);
                num_hint.setVisibility(View.GONE);
                num_he_hint.setVisibility(View.GONE);
            } else {
                contentParentScr.setVisibility(View.GONE);
                num_hint.setVisibility(View.VISIBLE);
                num_he_hint.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.a_dish_more_img_item_close:
                mAct.finish();
                break;
            case R.id.view_ad_img:
                changeContentVisibility();
                break;
            case R.id.a_dish_more_img_item_content_parent_rl:
            case R.id.a_dish_more_img_item_num_hint:
                if(TextUtils.isEmpty(tongjiId))
                XHClick.mapStat(mAct, tongjiId, "菜谱区域的点击", "步骤图大图-单击");
                changeContentVisibility();
                break;
        }
    }
}