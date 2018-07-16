package third.aliyun.edit.effects.coverImg;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.aliyun.common.media.ShareableBitmap;
import com.aliyun.qupai.editor.AliyunIThumbnailFetcher;
import com.aliyun.qupai.editor.AliyunThumbnailFetcherFactory;
import com.aliyun.struct.common.ScaleMode;
import com.xiangha.R;

import third.aliyun.edit.effects.control.BaseChooser;
import third.aliyun.edit.effects.control.VideoCallBack;

/**
 *选择封面
 */
public class CoverImgMediator extends BaseChooser {
    private String videoPath = "";
    private long durtion;
    private LinearLayout mThumbnailList;
    private AliyunIThumbnailFetcher mThumbnailFetcher;
    private AliyunIThumbnailFetcher mTimeThumbnailFetcher;
    private Activity mAct;
    private SeekBar seekBar;
    private int progress;
    private int itemWidth;
    private ImageView backgroup_img;
    private boolean isOnResume = false; //是否获取焦点
    public static CoverImgMediator newInstance() {
        CoverImgMediator dialog = new CoverImgMediator();
        Bundle args = new Bundle();
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAct= (Activity) context;
    }

    public void setVideoPath(String videoPath){
        this.videoPath= videoPath;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.work_aliyun_svideo_cover_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mThumbnailList= (LinearLayout) view.findViewById(R.id.cover_thumbnail_list);
        seekBar= (SeekBar) view.findViewById(R.id.seek_bar);
        backgroup_img= (ImageView) view.findViewById(R.id.backgroup_img);
        init();
    }
    private void init(){
        mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mThumbnailFetcher.addVideoSource(videoPath, 0, Integer.MAX_VALUE);
        mTimeThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mTimeThumbnailFetcher.addVideoSource(videoPath, 0, Integer.MAX_VALUE);
        mThumbnailList.post(mInitThumbnails);
        seekBar.setMax((int) (durtion));
        seekBar.setProgress(0);
        seekBar.post(mTimeThumbnails);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                CoverImgMediator.this.progress=progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(videoCallBack!=null){
                    videoCallBack.getSeekPostion(progress);
                }
                timeThumbnailImage(progress);

            }
        });
    }

    private final Runnable mInitThumbnails = new Runnable() {
        @Override
        public void run() {
            initThumbnails();
        }
    };
    private final Runnable mTimeThumbnails = new Runnable() {
        @Override
        public void run() {
            timeThumbnailImage(0);
        }
    };
    private void initThumbnails() {
        int width = mThumbnailList.getWidth();
        itemWidth = width / 7;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,itemWidth);
        seekBar.setLayoutParams(layoutParams);
        backgroup_img.setLayoutParams(layoutParams);
        seekBar.setPaddingRelative(itemWidth/2,0,itemWidth/2,0);
        mThumbnailFetcher.setParameters(itemWidth, itemWidth,
                AliyunIThumbnailFetcher.CropMode.Mediate, ScaleMode.LB, 6);
        mTimeThumbnailFetcher.setParameters(itemWidth, itemWidth,
                AliyunIThumbnailFetcher.CropMode.Mediate, ScaleMode.LB, 2);
        long duration = mThumbnailFetcher.getTotalDuration();
        long itemTime = duration / 6;
        for (int i = 0; i < 6; i++) {
            long time = itemTime * i;
            mThumbnailFetcher.requestThumbnailImage(new long[]{time},
                    new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
                        @Override
                        public void onThumbnailReady(ShareableBitmap frameBitmap, long time) {
                            initThumbnails(frameBitmap.getData());
                        }
                        @Override
                        public void onError(int errorCode) {}
                    });
        }
    }
    private void initThumbnails(Bitmap thumbnail){
        ImageView image = new ImageView(mAct);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        image.setImageBitmap(thumbnail);
        mThumbnailList.addView(image);
    }
    public void setVideoDurtion(long durtion){
        this.durtion= durtion;
    }
    private VideoCallBack videoCallBack;
    public void setVideoCallBack(VideoCallBack callBack){
        this.videoCallBack = callBack;
    }

    private void timeThumbnailImage(long time){
        mTimeThumbnailFetcher.requestThumbnailImage(new long[]{time/1000},
                new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
                    @Override
                    public void onThumbnailReady(ShareableBitmap frameBitmap, long time) {
                        setDrawable(frameBitmap.getData());
                    }
                    @Override
                    public void onError(int errorCode) {
                    }
                });
    }
    private void setDrawable(Bitmap bitmap){
        if(bitmap!=null&&isOnResume) {
            Drawable drawable = new BitmapDrawable(getResources(),bitmap);
            drawable.setBounds(0,0,itemWidth,itemWidth);
            seekBar.setThumb(drawable);
        }
    }
    private void recycleBitmap(Bitmap bitmap){
        if(bitmap==null){
            return;
        }
        if(!bitmap.isRecycled()){
            bitmap.recycle();
            bitmap=null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isOnResume=true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isOnResume=false;
    }
}
