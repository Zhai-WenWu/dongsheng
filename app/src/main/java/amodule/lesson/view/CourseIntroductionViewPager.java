package amodule.lesson.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.tools.ToolsDevice;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.mall.tool.ToolView;
import third.video.VideoPlayerController;

/**
 * Description :
 * PackageName : amodule.lesson.view
 * Created by mrtrying on 2018/12/4 17:22.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseIntroductionViewPager extends RelativeLayout {
    final int LAYOUT_ID = R.layout.view_course_introdce_view_pager;
    public static final String TYPE_KEY = "type";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_VIDEO = "video";

    private ViewPager mViewPager;
    private LinearLayout mPointLayout;

    private VideoPlayerController mVideoPlayerController;

    private int mInitPlayState = -2;
    private boolean mHasInitPlayStateOnPagerSelected;
    private int mVideoPosition = -1;
    private String mVideoUrl;

    public CourseIntroductionViewPager(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public CourseIntroductionViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public CourseIntroductionViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(LAYOUT_ID,this);
        mViewPager = findViewById(R.id.viewpager);
        mPointLayout = findViewById(R.id.point_linear);
    }

    public void setData(List<Map<String,String>> images){
        List<View> views = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            View topView = LayoutInflater.from(getContext()).inflate(R.layout.v_product_top_view, null);
            Map<String, String> itemMap = images.get(i);
            String type = itemMap.get("type");

            if (TYPE_VIDEO.equals(type)) {
                mVideoPosition = i;
                ViewGroup videoLayout = topView.findViewById(R.id.video_layout);
                videoLayout.setVisibility(View.VISIBLE);
                String imgUrl = itemMap.get("img");
                mVideoPlayerController = new VideoPlayerController((Activity) getContext(), videoLayout, imgUrl);
                mVideoPlayerController.setOnSeekbarVisibilityListener(visibility -> mPointLayout.setVisibility(View.VISIBLE == visibility ? View.GONE : View.VISIBLE));
                mVideoUrl = itemMap.get("url");
                mVideoPlayerController.setVideoUrl(mVideoUrl);
            } else {
                topView.findViewById(R.id.image).setVisibility(View.VISIBLE);
                ImageView iv = topView.findViewById(R.id.image);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                setImageView(iv, images.get(i).get("img"), false);
            }
            views.add(topView);
        }
        for(int i=0;i<views.size();i++){
            ImageView point = new ImageView(getContext());
            point.setImageResource(R.drawable.bg_course_introduction_point);
            mPointLayout.addView(point);
        }
        mViewPager.setAdapter(new Adapter(views));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                selectPoint(position);
                if (mVideoPlayerController != null) {
                    if (mVideoPosition != position) {
                        if (Math.abs(position - mVideoPosition) == 1 && !mHasInitPlayStateOnPagerSelected) {
                            mInitPlayState = mVideoPlayerController.getPlayState();
                            mHasInitPlayStateOnPagerSelected = true;
                            if (videoCanPause()) {
                                mVideoPlayerController.onPause();
                            }
                        }
                        mPointLayout.setVisibility(View.VISIBLE);
                    } else {
                        mHasInitPlayStateOnPagerSelected = false;
                        if (videoCanResume()) {
                            mVideoPlayerController.onResume();
                        }
                        mPointLayout.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        if (mVideoPlayerController != null && "wifi".equals(ToolsDevice.getNetWorkSimpleType(getContext()))) {
            mVideoPlayerController.setOnClick();
        }
    }

    private void selectPoint(int position){
        if(position >=0 && mPointLayout != null && mPointLayout.getChildCount() > position){
            for(int i=0;i<mPointLayout.getChildCount();i++){
                mPointLayout.getChildAt(i).setSelected(false);
            }
            mPointLayout.getChildAt(position).setSelected(true);
        }
    }

    private boolean videoCanPause() {
        boolean ret = false;
        if (mVideoPlayerController != null &&
                mVideoPlayerController.getPlayState() == GSYVideoPlayer.CURRENT_STATE_PAUSE)
            return ret;
        switch (mInitPlayState) {
            case GSYVideoPlayer.CURRENT_STATE_PAUSE:
            case GSYVideoPlayer.CURRENT_STATE_AUTO_COMPLETE:
            case GSYVideoPlayer.CURRENT_STATE_ERROR:
                break;
            default:
                ret = true;
                break;
        }
        return ret;
    }

    private boolean videoCanResume() {
        boolean ret = false;
        if (null != mVideoPlayerController
                && mVideoPlayerController.getPlayState() == GSYVideoPlayer.CURRENT_STATE_PLAYING)
            return ret;
        switch (mInitPlayState) {
            case GSYVideoPlayer.CURRENT_STATE_PAUSE:
            case GSYVideoPlayer.CURRENT_STATE_AUTO_COMPLETE:
            case GSYVideoPlayer.CURRENT_STATE_ERROR:
                break;
            default:
                ret = true;
                break;
        }
        return ret;
    }

    /**
     * 加载图片
     *
     * @param iv
     * @param imageUrl
     * @param state    true 圆形，false正常
     */
    private void setImageView(final ImageView iv, String imageUrl, final boolean state) {
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(getContext())
                .load(imageUrl)
                .build();
        if (bitmapRequest != null)
            bitmapRequest.into(new SubBitmapTarget() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                    if (state)
                        iv.setImageBitmap(ToolView.toRoundBitmap(bitmap));
                    else
                        iv.setImageBitmap(bitmap);
                }
            });
    }

    class Adapter extends PagerAdapter{

        List<View> views;

        Adapter(List<View> views){
            super();
            this.views = views;
        }

        @Override
        public int getCount() {
            return views == null ? 0 : views.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View pager = views.get(position);
            container.addView(pager, 0);
            return pager;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}
