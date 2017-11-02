package amodule.user.view.module;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import acore.widget.TextViewShow;
import aplug.basic.SubAnimTarget;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilString;

/**
 * 多图---美食贴多图样式
 */
public class ModuleMoreImgView extends ModuleBaseView{
    private TextViewShow module_title,tv_content;
    private LinearLayout ll_imgs;
    private ImageView image_one;
    private ImageViewVideo video_image;
    private RelativeLayout video_rela;
    private int subjectImgWidth;
    private Map<String,String> map;
    private String url="";
    public ModuleMoreImgView(Context context, int layoutId) {
        super(context, R.layout.module_more_view);
    }

    public ModuleMoreImgView(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs, R.layout.module_more_view);
    }

    public ModuleMoreImgView(Context context, AttributeSet attrs, int defStyleAttr, int layoutId) {
        super(context, attrs, defStyleAttr, R.layout.module_more_view);
    }

    @Override
    public void initUI() {
        module_title= (TextViewShow) findViewById(R.id.module_title);
        tv_content= (TextViewShow) findViewById(R.id.tv_content);
        // 多图模式
        ll_imgs = (LinearLayout) findViewById(R.id.ll_imgs);
        image_one = (ImageView) findViewById(R.id.image_one);
        video_rela = (RelativeLayout)findViewById(R.id.video_rela);
        video_image= (ImageViewVideo)findViewById(R.id.video_image);
        subjectImgWidth = ToolsDevice.getWindowPx(mContext).widthPixels - Tools.getDimen(mContext, R.dimen.dp_30);// 左右两边的pinding值
    }

    @Override
    public void initData(Map<String, String> map) {
        this.map= map;
        setKeyContent(map,module_title,"title");
        setKeyContent(map,tv_content,"content");
        //url点击跳转
        if(map.containsKey("url")&& !TextUtils.isEmpty(map.get("url")))url= map.get("url");
        if(!map.containsKey("styleData")|| TextUtils.isEmpty(map.get("styleData"))){
            findViewById(R.id.module_middle).setVisibility(GONE);
            return;
        }
        setImageView();
    }

    @Override
    public void setListener() {
        module_title.setOnClickListener(UrlOnClickListener);
        tv_content.setOnClickListener(UrlOnClickListener);
        ll_imgs.setOnClickListener(UrlOnClickListener);
        image_one.setOnClickListener(UrlOnClickListener);
        video_rela.setOnClickListener(UrlOnClickListener);
        video_image.setOnClickListener(UrlOnClickListener);
    }

    private OnClickListener UrlOnClickListener= new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!TextUtils.isEmpty(url)) AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),url,false);
        }
    };
    /**
     * 对Imgs[]进行处理
     * 对图片数量处理
     */
    private void setImageView() {
        Log.i("zhangyujian", "setImageView");
        ImageViewVideo quan_item_model_video = (ImageViewVideo)findViewById(R.id.quan_item_model_video);
        ImageView quan_iv_img_1 = (ImageView)findViewById(R.id.quan_iv_img_1);
        ImageView quan_iv_img_2 = (ImageView)findViewById(R.id.quan_iv_img_2);
        ImageView quan_iv_img_3 = (ImageView)findViewById(R.id.quan_iv_img_3);
        // *****************************对图片数量处理start*******************************
        ArrayList<Map<String, String>> imgurl = UtilString.getListMapByJson(map.get("styleData"));
        RelativeLayout.LayoutParams viewIamgeParams = new RelativeLayout.LayoutParams(subjectImgWidth, subjectImgWidth * 2 / 3);

        RelativeLayout.LayoutParams _1PicLayoutParams = new RelativeLayout.LayoutParams(subjectImgWidth, subjectImgWidth * 2 / 3);
        LinearLayout.LayoutParams _1PicImageParams = new LinearLayout.LayoutParams(subjectImgWidth, subjectImgWidth * 2 / 3);

        RelativeLayout.LayoutParams _2PicLayoutParams = new RelativeLayout.LayoutParams(subjectImgWidth, subjectImgWidth / 2);
        LinearLayout.LayoutParams _2PicImageParams = new LinearLayout.LayoutParams(subjectImgWidth / 2, subjectImgWidth / 2);

        RelativeLayout.LayoutParams _3PicLayoutParams = new RelativeLayout.LayoutParams(subjectImgWidth, subjectImgWidth / 3);
        LinearLayout.LayoutParams _3PicImageParams = new LinearLayout.LayoutParams(subjectImgWidth / 3, subjectImgWidth / 3);
        findViewById(R.id.line_1).setVisibility(View.GONE);
        findViewById(R.id.line_2).setVisibility(View.GONE);
        switch (imgurl.size()) {
            case 0:// 无图显示
                ll_imgs.setVisibility(View.GONE);
                image_one.setVisibility(View.GONE);
                quan_item_model_video.setVisibility(View.GONE);
                break;
            case 1:// 单图显示
                // 单图模式下：有可能为视频
//                if (map.containsKey("type") && "2".equals(map.get("type"))) {
//                    ll_imgs.setVisibility(View.GONE);
//                    quan_iv_img_1.setVisibility(View.GONE);
//                    image_one.setVisibility(View.GONE);
//                    quan_item_model_video.setVisibility(View.VISIBLE);
//
//                    quan_item_model_video.setLayoutParams(viewIamgeParams);
//                    String video_img = imgurl.get(0).get("");
//                    quan_item_model_video.parseItemImg(ImageView.ScaleType.CENTER_CROP, video_img, "2", false, R.drawable.i_nopic, FileManager.save_cache);
//                    quan_item_model_video.playImgWH = Tools.getDimen(mContext, R.dimen.dp_25);
//                } else {
                    ll_imgs.setVisibility(View.GONE);
                    quan_iv_img_1.setVisibility(View.GONE);
                    quan_item_model_video.setVisibility(View.GONE);

                    setViewImage(image_one, imgurl.get(0).get("url"), 1);
                    ll_imgs.setLayoutParams(_1PicLayoutParams);
//                }
                break;
            case 2:// 两张图显示
                findViewById(R.id.line_1).setVisibility(View.VISIBLE);
                findViewById(R.id.line_2).setVisibility(View.GONE);
                image_one.setVisibility(View.GONE);
                ll_imgs.setVisibility(View.VISIBLE);
                ll_imgs.setLayoutParams(_2PicLayoutParams);
                quan_iv_img_1.setLayoutParams(_2PicImageParams);
                quan_iv_img_2.setLayoutParams(_2PicImageParams);
                quan_iv_img_1.setVisibility(View.VISIBLE);
                quan_iv_img_2.setVisibility(View.VISIBLE);
                quan_iv_img_3.setVisibility(View.GONE);
                quan_item_model_video.setVisibility(View.GONE);
                setViewImage(quan_iv_img_1, imgurl.get(0).get("url"), 1);
                setViewImage(quan_iv_img_2, imgurl.get(1).get("url"), 1);
                break;
            default:
                quan_item_model_video.setVisibility(View.GONE);
                image_one.setVisibility(View.GONE);
               findViewById(R.id.line_1).setVisibility(View.VISIBLE);
               findViewById(R.id.line_2).setVisibility(View.VISIBLE);
                ll_imgs.setVisibility(View.VISIBLE);
                ll_imgs.setLayoutParams(_3PicLayoutParams);
                quan_iv_img_1.setLayoutParams(_3PicImageParams);
                quan_iv_img_2.setLayoutParams(_3PicImageParams);
                quan_iv_img_3.setLayoutParams(_3PicImageParams);
                quan_iv_img_1.setVisibility(View.VISIBLE);
                quan_iv_img_2.setVisibility(View.VISIBLE);
                quan_iv_img_3.setVisibility(View.VISIBLE);
                setViewImage(quan_iv_img_1, imgurl.get(0).get("url"), 1);
                setViewImage(quan_iv_img_2, imgurl.get(1).get("url"), 1);
                setViewImage(quan_iv_img_3, imgurl.get(2).get("url"), 1);
                break;
        }
        if (imgurl.size() <= 0) {
            ll_imgs.setVisibility(View.GONE);
        }
    }

    @Override
    protected SubAnimTarget getSubAnimTarget(final ImageView v, final String url) {
        return new SubAnimTarget(v) {
            @Override
            protected void setResource(Bitmap bitmap) {
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
                            int waith = ToolsDevice.getWindowPx(mContext).widthPixels;
                            int dp_30 = Tools.getDimen(mContext, R.dimen.dp_30);
                            imgWidth = waith - dp_30;
                            imgHeight = (waith - dp_30) * 720 / 1280;
                            UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
                        } else if (v.getId() == R.id.image_one) {
                            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            imgZoom = true;
                            final float maxW = ToolsDevice.getWindowPx(mContext).widthPixels - Tools.getDimen(mContext, R.dimen.dp_30);
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
}
