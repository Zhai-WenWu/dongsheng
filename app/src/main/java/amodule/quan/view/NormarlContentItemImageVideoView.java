package amodule.quan.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import acore.widget.TextViewShow;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilString;

/**
 * NormarlContentView---中间部分view
 * 因考虑到以前会把标题和内容跟img强关联关系，所以放在一起，
 * 正常情况还是要进行拆分.
 */
public class NormarlContentItemImageVideoView extends NormarlContentItemView {
    private View view;
    private LinearLayout ll_imgs;
    private ImageView image_one;
    private ImageViewVideo video_image;
    private TextViewShow tv_content;
    private Map<String, String> map;
    private int subjectImgWidth;
    private RelativeLayout root_layout;
    private boolean needRefresh = false;
    private RelativeLayout video_rela;

    public NormarlContentItemImageVideoView(Activity context, View view) {
        super(context);
        this.view = view;
        subjectImgWidth = ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_30);// 左右两边的pinding值
    }

    @Override
    protected void initView() {
        root_layout = (RelativeLayout) view.findViewById(R.id.root_layout);
        // 内容
        tv_content = (TextViewShow) view.findViewById(R.id.tv_content);
        tv_content.setHaveCopyFunction(false);

        // 多图模式
        ll_imgs = (LinearLayout) view.findViewById(R.id.ll_imgs);
        image_one = (ImageView) view.findViewById(R.id.image_one);
        video_rela = (RelativeLayout) view.findViewById(R.id.video_rela);
        video_image= (ImageViewVideo) view.findViewById(R.id.video_image);
    }

    @Override
    public void setViewData(Map<String, String> maps,int position) {
        this.map = maps;
        showImageViewVideoIndex(position);
    }

    @Override
    public void setShowUpload(boolean state) {
        if (state) {
            setListener(root_layout, typeSubject, "");
            setListener(ll_imgs, typeSubject, "贴子图片");
            setListener(image_one, typeSubject, "贴子图片");
            setListener(tv_content, typeSubject, "贴子内容");
        } else {
        }
    }

    @Override
    public void onClickCallback(int type, String statisValue) {
        if (normarlViewOnClickCallBack != null)
            normarlViewOnClickCallBack.onClickViewIndex(type, statisValue);
    }

    @Override
    public void onAdClickCallback(View view,String eventId) {
        if (mAdHintClickCallback != null)
            mAdHintClickCallback.onAdHintListener(view,eventId);
    }

    /**
     * 重新开始videoView
     */
    public void startVideoView() {
        Log.i("zhangyujian","startVideoView");
//        if (videoView != null) {
//            video_rela.setVisibility(View.VISIBLE);
//            ViewGroup viewParent = (ViewGroup) videoView.getParent();
//            if (viewParent != null)
//                viewParent.removeAllViews();
//            video_layout.removeAllViews();
//            video_layout.addView(videoView);
//            videoView.onResume();
//            video_image.setVisibility(View.GONE);
//            Log.i("zhangyujian","播放");
//        }else{
//
//        }
    }

    /**
     * 暂停videoView
     */
    public void stopVideoView() {
        Log.i("zhangyujian","stopVideoView");
//        if (videoView != null) {
//            video_layout.removeAllViews();
//            videoView.stop();
//            videoView=null;
//            video_rela.setVisibility(View.VISIBLE);
//        }
    }

    public void setNeedRefresh(boolean needRefresh) {
        this.needRefresh = needRefresh;
    }


    /**
     * 对Video进行处理
     */
    private void showImageViewVideoIndex(final int position) {
        Log.i("zhangyujian","showImageViewVideoIndex");
        if (map.containsKey("selfVideo") && !TextUtils.isEmpty(map.get("selfVideo"))) {//有数据进行控制
//            RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.quan_middle);
            video_rela.setVisibility(View.VISIBLE);
            //处理视频：
//            if (videoView != null) {
//                if (!needRefresh) {
//                    return;
//                }
//                video_layout.removeAllViews();
//            }

            final Map<String, String> videoData = StringManager.getFirstMap(map.get("selfVideo"));
//            videoView = new CircleVideoView(getContext());
            int height = (ToolsDevice.getWindowPx(getContext()).widthPixels - Tools.getDimen(getContext(), R.dimen.dp_30)) * 3 / 4;
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);

            video_image.setLayoutParams(layoutParams);
//            Glide.with(context).load(videoData.get("sImgUrl")).into(video_image);
            //点击开始播放
            video_image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(videoClickCallBack!=null)
                        videoClickCallBack.videoImageOnClick(position);
                }
            });
            video_image.playImgWH=Tools.getDimen(context,R.dimen.dp_41);
            video_image.parseItemImg(videoData.get("sImgUrl"),"2",true);
            video_image.setVisibility(View.VISIBLE);
            view.findViewById(R.id.ll_imgs).setVisibility(GONE);
            view.findViewById(R.id.quan_item_model_video).setVisibility(GONE);
            image_one.setVisibility(GONE);
        } else {
            Log.d(this.getClass().getSimpleName(), "no video");

            video_rela.setVisibility(View.GONE);
            setImageView();
        }
    }

    /**
     * 对Imgs[]进行处理
     * 对图片数量处理
     */
    private void setImageView() {
        Log.i("zhangyujian","setImageView");
        ImageViewVideo quan_item_model_video = (ImageViewVideo) view.findViewById(R.id.quan_item_model_video);
        ImageView quan_iv_img_1 = (ImageView) view.findViewById(R.id.quan_iv_img_1);
        ImageView quan_iv_img_2 = (ImageView) view.findViewById(R.id.quan_iv_img_2);
        ImageView quan_iv_img_3 = (ImageView) view.findViewById(R.id.quan_iv_img_3);
        // *****************************对图片数量处理start*******************************
        ArrayList<Map<String, String>> imgurl = UtilString.getListMapByJson(map.get("imgs"));
        RelativeLayout.LayoutParams viewIamgeParams = new RelativeLayout.LayoutParams(subjectImgWidth, subjectImgWidth * 2 / 3);

        RelativeLayout.LayoutParams _1PicLayoutParams = new RelativeLayout.LayoutParams(subjectImgWidth, subjectImgWidth * 2 / 3);
        LinearLayout.LayoutParams _1PicImageParams = new LinearLayout.LayoutParams(subjectImgWidth, subjectImgWidth * 2 / 3);

        RelativeLayout.LayoutParams _2PicLayoutParams = new RelativeLayout.LayoutParams(subjectImgWidth, subjectImgWidth / 2);
        LinearLayout.LayoutParams _2PicImageParams = new LinearLayout.LayoutParams(subjectImgWidth / 2, subjectImgWidth / 2);

        RelativeLayout.LayoutParams _3PicLayoutParams = new RelativeLayout.LayoutParams(subjectImgWidth, subjectImgWidth / 3);
        LinearLayout.LayoutParams _3PicImageParams = new LinearLayout.LayoutParams(subjectImgWidth / 3, subjectImgWidth / 3);
        view.findViewById(R.id.line_1).setVisibility(View.GONE);
        view.findViewById(R.id.line_2).setVisibility(View.GONE);
        switch (imgurl.size()) {
            case 0:// 无图显示
                ll_imgs.setVisibility(View.GONE);
                image_one.setVisibility(View.GONE);
                quan_item_model_video.setVisibility(View.GONE);
                break;
            case 1:// 单图显示
                // 单图模式下：有可能为视频
                if (map.containsKey("type") && "5".equals(map.get("type"))) {
                    ll_imgs.setVisibility(View.GONE);
                    quan_iv_img_1.setVisibility(View.GONE);
                    image_one.setVisibility(View.GONE);
                    quan_item_model_video.setVisibility(View.VISIBLE);

                    quan_item_model_video.setLayoutParams(viewIamgeParams);
                    String video_img = imgurl.get(0).get("");
                    quan_item_model_video.parseItemImg(ImageView.ScaleType.CENTER_CROP, video_img, "2", false, R.drawable.i_nopic, FileManager.save_cache);
                    quan_item_model_video.playImgWH = Tools.getDimen(context, R.dimen.dp_25);
                } else {
                    ll_imgs.setVisibility(View.GONE);
                    quan_iv_img_1.setVisibility(View.GONE);
                    quan_item_model_video.setVisibility(View.GONE);

                    setViewImage(image_one, imgurl.get(0).get(""), 1);
                    ll_imgs.setLayoutParams(_1PicLayoutParams);
                }
                break;
            case 2:// 两张图显示
                view.findViewById(R.id.line_1).setVisibility(View.VISIBLE);
                view.findViewById(R.id.line_2).setVisibility(View.GONE);
                image_one.setVisibility(View.GONE);
                ll_imgs.setVisibility(View.VISIBLE);
                ll_imgs.setLayoutParams(_2PicLayoutParams);
                quan_iv_img_1.setLayoutParams(_2PicImageParams);
                quan_iv_img_2.setLayoutParams(_2PicImageParams);
                quan_iv_img_1.setVisibility(View.VISIBLE);
                quan_iv_img_2.setVisibility(View.VISIBLE);
                quan_iv_img_3.setVisibility(View.GONE);
                quan_item_model_video.setVisibility(View.GONE);
                setViewImage(quan_iv_img_1, imgurl.get(0).get(""), 1);
                setViewImage(quan_iv_img_2, imgurl.get(1).get(""), 1);
                break;
            default:
                quan_item_model_video.setVisibility(View.GONE);
                image_one.setVisibility(View.GONE);
                view.findViewById(R.id.line_1).setVisibility(View.VISIBLE);
                view.findViewById(R.id.line_2).setVisibility(View.VISIBLE);
                ll_imgs.setVisibility(View.VISIBLE);
                ll_imgs.setLayoutParams(_3PicLayoutParams);
                quan_iv_img_1.setLayoutParams(_3PicImageParams);
                quan_iv_img_2.setLayoutParams(_3PicImageParams);
                quan_iv_img_3.setLayoutParams(_3PicImageParams);
                quan_iv_img_1.setVisibility(View.VISIBLE);
                quan_iv_img_2.setVisibility(View.VISIBLE);
                quan_iv_img_3.setVisibility(View.VISIBLE);
                setViewImage(quan_iv_img_1, imgurl.get(0).get(""), 1);
                setViewImage(quan_iv_img_2, imgurl.get(1).get(""), 1);
                setViewImage(quan_iv_img_3, imgurl.get(2).get(""), 1);
                break;
        }
        if (imgurl.size() <= 0) {
            ll_imgs.setVisibility(View.GONE);
        }
        // *****************************对图片数量处理end****************************

    }

    /**
     * 处理图片显示
     *
     * @param v
     * @param value
     * @param number
     */
    private void setViewImage(ImageView v, String value, float number) {
        setViewImage(v, value);
        if (value.indexOf(Environment.getExternalStorageDirectory().toString()) == 0) {
            v.setImageBitmap(UtilImage.imgPathToBitmap(value, (int) (subjectImgWidth / number), subjectImgWidth, true, null));
        }
    }

    @Override
    public SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                try {
                    imgWidth = 0;
                    imgHeight = 0;
                    ImageView img = null;
                    imgZoom = false;
                    if (v.getTag(TAG_ID).equals(url))
                        img = v;
                    if (img != null && bitmap != null) {
                        // 图片圆角和宽高适应auther_userImg
                        if (v.getId() == R.id.iv_userImg || v.getId() == R.id.auther_userImg) {
                            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            v.setImageBitmap(UtilImage.toRoundCorner(v.getResources(),bitmap,1,500));
                        } else if (map.get("isPromotion") != null && map.get("isPromotion").equals("1") && UtilString.getListMapByJson(map.get("imgs")).size()==1) {
                            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            int waith = context.getWindowManager().getDefaultDisplay().getWidth();
                            int dp_30 = Tools.getDimen(context, R.dimen.dp_30);
                            imgWidth = waith - dp_30;
                            imgHeight = (waith - dp_30) * 720 / 1280;
                            UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
                        } else if (v.getId() == R.id.image_one) {
                            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            imgZoom = true;
                            final float maxW = ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_30);
                            final float maxH = maxW * 4 / 5;
                            final float minW = maxW / 4;
                            final float minH = minW;

                            final int originalW = bitmap.getWidth();
                            final int originalH = bitmap.getHeight();

                            final float tmpW = maxW;
                            final float tmpH = originalH * maxW / originalW;

                            if (tmpH <= minH) {
                                imgHeight = (int) minH;
                                imgWidth = (int) maxW;
                            } else if (minH < tmpH && tmpH <= maxH) {
                                imgWidth = (int) maxW;
                                imgHeight = (int) tmpH;
                            } else if (tmpH == maxW) {
                                imgHeight = (int) (tmpH / 2);
                                imgWidth = imgHeight;
                            } else {
                                imgHeight = (int) maxH;
                                imgWidth = (int) (tmpW * maxH / tmpH);
                                if (imgWidth < minW) {
                                    imgWidth = (int) minW;
                                }
                            }
                            UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
                        } else {
                            v.setScaleType(scaleType);
                            UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (Error e) {
                    e.printStackTrace();
                }
            }
        };
    }
    public interface VideoClickCallBack{
        public void videoImageOnClick(int position);
    }
    private VideoClickCallBack videoClickCallBack;
    public void setVideoClicCallBack(VideoClickCallBack videoClickCallBack){
        this.videoClickCallBack= videoClickCallBack;
    }
}
