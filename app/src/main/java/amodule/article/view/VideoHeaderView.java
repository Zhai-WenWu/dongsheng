package amodule.article.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.view.DishHeaderView;
import amodule.dish.view.DishVideoImageView;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;
import third.video.VideoPlayerController;

import static amodule.dish.activity.DetailDish.tongjiId;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/6/20 11:55.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoHeaderView extends RelativeLayout {
    private Activity activity;

    private RelativeLayout dishVidioLayout;
    private FrameLayout adLayout;

    private VideoPlayerController mVideoPlayerController = null;//视频控制器
    private DishHeaderView.DishHeaderVideoCallBack callBack;

    private Map<String, String> mapAd;//广告数据
    private boolean isAutoPaly = false;//默认自动播放
    private boolean isOnResuming = false;//默认自动播放
    private XHAllAdControl xhAllAdControl;
    private int num = 4;

    public VideoHeaderView(Context context) {
        super(context);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_video_header_oneimage, null);
        addView(view);
    }

    public VideoHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_video_header_oneimage, null);
        addView(view);
    }

    public VideoHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_video_header_oneimage, null);
        addView(view);
    }

    public void initView(Activity activity) {
        this.activity = activity;
        isAutoPaly = "wifi".equals(ToolsDevice.getNetWorkSimpleType(activity));
        //大图处理
        dishVidioLayout = (RelativeLayout) findViewById(R.id.video_layout);
    }

    public void setData(Map<String, String> data, DishHeaderView.DishHeaderVideoCallBack callBack) {
        if (callBack == null) {
            this.callBack = new DishHeaderView.DishHeaderVideoCallBack() {
                @Override
                public void videoImageOnClick() {
                }

                @Override
                public void getVideoControl(VideoPlayerController mVideoPlayerController, RelativeLayout dishVidioLayout, View view_oneImage) {
                }
            };
        } else this.callBack = callBack;

        try {
            Map<String, String> videoData = StringManager.getFirstMap(data.get("video"));
            videoData.put("title", data.get("title"));
            Map<String, String> videoUrlData = StringManager.getFirstMap(videoData.get("videoUrl"));
            String url = "";
            if (videoUrlData.isEmpty()) {
                Toast.makeText(getContext(), "视频播放失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if (videoUrlData.containsKey("D1080p") && !TextUtils.isEmpty(videoUrlData.get("D1080p"))) {
                url = videoUrlData.get("D1080p");
            } else if (videoUrlData.containsKey("D720p") && !TextUtils.isEmpty(videoUrlData.get("D720p"))) {
                url = videoUrlData.get("D720p");
            } else if (videoUrlData.containsKey("D480p") && !TextUtils.isEmpty(videoUrlData.get("D480p"))) {
                url = videoUrlData.get("D480p");
            }
            videoData.put("url", url);
            setSelfVideo(videoData);
        } catch (Exception e) {
            Toast.makeText(getContext(), "视频播放失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void initVideoAd() {
        adLayout = (FrameLayout) findViewById(R.id.video_ad_layout);
//        int distance = Tools.getDimen(activity, R.dimen.dp_45);
//        adLayout.setPadding(0, distance, 0, 0);

        ArrayList<String> list = new ArrayList<>();
        String key = AdPlayIdConfig.DISH_MEDIA;
        list.add(key);

        xhAllAdControl = new XHAllAdControl(list, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(Map<String, String> maps) {
                String temp = maps.get(AdPlayIdConfig.DISH_MEDIA);
                mapAd = StringManager.getFirstMap(temp);
                Log.i("tzy", "needVideoControl time = " + System.currentTimeMillis());
                if (mapAd != null && mapAd.size() > 0
                        && mVideoPlayerController != null) {
                    mVideoPlayerController.setShowAd(true);
                }
                if (isAutoPaly && mVideoPlayerController != null)
                    mVideoPlayerController.setOnClick();
            }
        }, activity, "result_media");

    }

    /**
     * 处理广告展示
     *
     * @param map
     * @param view
     */
    private void setVideoAdData(final Map<String, String> map, final View view) {
        xhAllAdControl.onAdBind(0, view, "");
        final TextView mNum = (TextView) view.findViewById(R.id.ad_gdt_video_num);
        final ImageView mImageView = (ImageView) view.findViewById(R.id.ad_video_img);
        String imgUrl = null;
        if (map.containsKey("imgUrl")) imgUrl = map.get("imgUrl");
        if (TextUtils.isEmpty(imgUrl)) return;

        view.findViewById(R.id.ad_gdt_video_hint_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                view.setVisibility(View.GONE);
                mVideoPlayerController.setShowAd(false);
                mVideoPlayerController.setOnClick();
            }
        });
        view.findViewById(R.id.ad_vip_lead).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.openUrl(activity, StringManager.api_openVip, true);
            }
        });

        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                xhAllAdControl.onAdClick(0, "");
            }
        });
        //初始化倒计时
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mNum.setText("" + msg.what);
                if (msg.what == 0) {
                    view.setVisibility(View.GONE);
                    if (!mVideoPlayerController.isPlaying()) {
                        mVideoPlayerController.setShowAd(false);
                        if (isOnResuming) mVideoPlayerController.setOnClick();
                    }
                }
            }
        };
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
                .load(imgUrl)
                .setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
                    @Override
                    public boolean onResourceReady(Bitmap arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3, boolean arg4) {
                        return false;
                    }

                    @Override
                    public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
                        mImageView.setVisibility(View.GONE);
                        return false;
                    }
                })
                .build();
        if (bitmapRequest != null)
            bitmapRequest.into(new SubBitmapTarget() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                    view.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.VISIBLE);
                    bitmap.getHeight();
                    mImageView.setImageBitmap(bitmap);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (; num > 0; num--) {
                                handler.sendEmptyMessage(num);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            handler.sendEmptyMessage(0);
                        }
                    }).start();
                }
            });
    }

    private boolean setSelfVideo(final Map<String, String> selfVideoMap) {
        initVideoAd();
        boolean isUrlVaild = false;
        tongjiId = "a_menu_detail_video430";
        String videoUrl = selfVideoMap.get("url");
        String img = selfVideoMap.get("videoImg");
        if (!TextUtils.isEmpty(videoUrl)
                && videoUrl.startsWith("http")) {
            LinearLayout dishvideo_img = (LinearLayout) findViewById(R.id.video_img_layout);
            int distance = Tools.getDimen(activity, R.dimen.dp_45);
//                if (Tools.isShowTitle()) {
//                    distance += Tools.getStatusBarHeight(activity);
//                }
//            dishVidioLayout.setPadding(0, distance, 0, 0);
//                dishvideo_img.addView(new DishVideoImageView(activity).setData(img, selfVideoMap.get("duration")));
            mVideoPlayerController = new VideoPlayerController(activity, dishVidioLayout, img);
            DishVideoImageView dishVideoImageView = new DishVideoImageView(activity);
            dishVideoImageView.setData(img, selfVideoMap.get("duration"));
            mVideoPlayerController.setNewView(dishVideoImageView);
            mVideoPlayerController.initVideoView2(videoUrl, selfVideoMap.get("title"), dishVideoImageView);
            mVideoPlayerController.setStatisticsPlayCountCallback(new VideoPlayerController.StatisticsPlayCountCallback() {
                @Override
                public void onStatistics() {
                    XHClick.mapStat(activity, tongjiId, "菜谱区域的点击", "视频播放按钮点击");
                    callBack.videoImageOnClick();
                }
            });
            //被点击回调
            mVideoPlayerController.setMediaViewCallBack(new VideoPlayerController.MediaViewCallBack() {
                @Override
                public void onclick() {
                    setVideoAdData(mapAd, adLayout);
                }
            });
            dishvideo_img.setVisibility(View.GONE);
            callBack.getVideoControl(mVideoPlayerController, dishVidioLayout, this);
            callBack.videoImageOnClick();
//                mVideoPlayerController.setOnClick();
            isUrlVaild = true;
        }
        return isUrlVaild;
    }

    public void onResume() {
        isOnResuming = true;
    }

    public void onPause() {
        isOnResuming = false;
    }
}
