package amodule.quan.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.GifRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.xianghatest.R;

import java.util.Map;

import acore.tools.Tools;
import acore.widget.ImageViewVideo;

/**
 * PackageName : amodule.quan.view
 * Created by MrTrying on 2016/9/26 17:59.
 * E_mail : ztanzeyu@gmail.com
 */

public class CircleVideoView extends RelativeLayout implements View.OnClickListener {
    /** 短视频 */
    public static final String VIDEO_SHORT = "1";
    /** 长视频 */
    public static final String VIDEO_LONG = "2";

    /** 显示GIF */
    private ImageView imageGif;
    /** 假的image */
    private ImageViewVideo imageViewVideo,gifImg;
    /** 播放次数 */
    private TextView playCount;
    /** 时长 */
    private TextView playTime;
    /** 假的覆盖层 */
    private RelativeLayout coverlayout;

    private ImageView loadProgress;
    /**GIF图的点击事件*/
    private OnClickListener mGIFOnClickListener = null;

    private OnClickListener mSpareClickListener = null;

    private boolean isPlaying = false;
    /** 视频类型 */
    private String videoType = "";

    private String gifUrl = "";
    private String videoUrl = "";

    public CircleVideoView(Context context) {
        this(context, null);
    }

    public CircleVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.circle_item_video_view, this);
        //初始化
        init();
    }

    /**初始化*/
    private void init() {
        coverlayout = (RelativeLayout) findViewById(R.id.cover_layout);
        imageGif = (ImageView) findViewById(R.id.image_for_gif);
        gifImg = (ImageViewVideo) findViewById(R.id.image_for_gif_img);
        imageViewVideo = (ImageViewVideo) findViewById(R.id.image_video);
        playCount = (TextView) findViewById(R.id.play_count);
        playTime = (TextView) findViewById(R.id.play_time);
        loadProgress = (ImageView) findViewById(R.id.load_progress);

        imageViewVideo.playImgWH = Tools.getDimen(getContext(),R.dimen.dp_41);

        imageViewVideo.setOnClickListener(this);
        imageGif.setOnClickListener(this);
    }

    /**
     * 设置数据
     *
     * @param map
     */
    public void setData(Map<String, String> map) {
        //字段暂时未定
        if(map != null){
            this.videoType = map.get("type");
            this.gifUrl =map.get("gImgUrl");
            this.videoUrl =map.get("videoUrl");

            gifImg.parseItemImg(map.get("sImgUrl"),"1",true);
            imageViewVideo.parseItemImg(map.get("sImgUrl"),"2",true);
//            loadGif(gifUrl,null);
            setViewText(playTime,null);
//            setViewText(playTime,map.get("videoTime"));
            stop();
        }
    }

    public void setImageViewSpareClick(OnClickListener listener){
        this.mSpareClickListener = listener;
    }

    /** 开始 */
    public void start() {
        if (TextUtils.isEmpty(videoType)) {
            return;
        }
        if(isPlaying){
            return;
        }
        switch (videoType) {
            case VIDEO_SHORT:
                Animation animation = AnimationUtils.loadAnimation(getContext(),R.anim.loading_anim);
                loadProgress.startAnimation(animation);
                loadProgress.setVisibility(VISIBLE);
                loadGif(gifUrl,imageGif);
                coverlayout.setVisibility(GONE);
                break;
            case VIDEO_LONG:

                coverlayout.setVisibility(GONE);
                break;
            default:
                break;
        }
        isPlaying = true;
    }

    /** 停止 */
    public void stop() {
        if (TextUtils.isEmpty(videoType)) {
            return;
        }
        if(!isPlaying){
            return;
        }
        switch (videoType) {
            case VIDEO_SHORT:
                loadProgress.clearAnimation();
                loadProgress.setVisibility(GONE);
                imageGif.setImageResource(R.drawable.i_nopic);
                coverlayout.setVisibility(VISIBLE);
                gifImg.setVisibility(VISIBLE);
                break;
            case VIDEO_LONG:
                coverlayout.setVisibility(VISIBLE);
                break;
            default:
                break;
        }
        isPlaying = false;
    }

    /**
     * 设置GIF
     * @param gifUrl
     * @param imageView
     */
    private void loadGif(String gifUrl,ImageView imageView){
        if(!TextUtils.isEmpty(gifUrl)){
            GifRequestBuilder requestBuilder = Glide.with(getContext())
                    .load(gifUrl)
                    .asGif()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(new RequestListener<String, GifDrawable>() {
                        @Override
                        public boolean onException(Exception e, String s, Target<GifDrawable> target, boolean b) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable gifDrawable, String s, Target<GifDrawable> target, boolean b, boolean b1) {
                            loadProgress.clearAnimation();
                            loadProgress.setVisibility(GONE);
                            gifImg.setVisibility(GONE);
                            return false;
                        }
                    });
            if(imageView != null){
                requestBuilder.into(imageView);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(!isClickable()){
            return;
        }
        final int id = v.getId();
        switch (id) {
            case R.id.image_video:
                if(VIDEO_SHORT.equals(videoType) && TextUtils.isEmpty(gifUrl)){
                    if(mSpareClickListener != null){
                        mSpareClickListener.onClick(v);
                        break;
                    }
                }else if(VIDEO_LONG.equals(videoType) && TextUtils.isEmpty(videoUrl)){
                    if(mSpareClickListener != null){
                        mSpareClickListener.onClick(v);
                        break;
                    }
                }
                start();
                break;
            case R.id.image_for_gif:
                if(mGIFOnClickListener != null){
                    mGIFOnClickListener.onClick(v);
                }
                break;
        }
    }

    public void setViewText(TextView textView,String text){
        if(textView != null){
            if(TextUtils.isEmpty(text)){
                textView.setVisibility(GONE);
            }else{
                textView.setText(text);
                textView.setVisibility(VISIBLE);
            }
        }
    }

    public OnClickListener getGIFOnClickListener() {
        return mGIFOnClickListener;
    }

    public void setGIFOnClickListener(OnClickListener mGIFOnClickListener) {
        this.mGIFOnClickListener = mGIFOnClickListener;
    }

}
