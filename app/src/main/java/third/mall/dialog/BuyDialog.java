package third.mall.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xianghatest.R;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.Tools;
import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;
import aplug.basic.SubBitmapTarget;
import third.mall.activity.ShoppingActivity;
import third.mall.activity.ShoppingOrderActivity;
import third.mall.adapter.AdapterFavorable;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilString;

/**
 * 选择优惠券的dialog
 *
 * @author yujian
 */
public class BuyDialog extends SimpleDialog {
    public int imgResource = R.drawable.i_nopic;
    public int roundImgPixels = 0, imgWidth = 0, imgHeight = 0,// 以像素为单位
            roundType = 1; // 1为全圆角，2上半部分圆角
    public boolean imgZoom = false; // 是否允许图片拉伸来适应设置的宽或高
    public String imgLevel = FileManager.save_cache; // 图片保存等级
    public int viewWidth = 0; // viewWidth的最小宽度
    public int viewHeight = 0; // viewHeight的最小宽度
    public boolean isAnimate = false;//控制图片渐渐显示

    public static final int TAG_ID = R.string.tag;

    private Context context;
    private ImageView item_commod_iv;
    private TextView item_commod_texts,item_commod_price,item_commod_num;
    private RelativeLayout item_commod_cut;
    private Map<String,String> mapData;
    private int productNum=1;
    private int saleableNum,maxSaleNum;

    public BuyDialog(Activity activity, Map<String,String> mapData) {
        super(activity, R.layout.dialog_mall_buy);
        this.context = activity;
        this.mapData= mapData;
        setLatyoutHeight();
        initView();
    }

    @Override
    public void setLatyoutHeight() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        RelativeLayout ll_view=(RelativeLayout) findViewById(R.id.ll_view);
        RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ll_view.setLayoutParams(params);
        findViewById(R.id.dialog_close).setOnClickListener(this);
    }

    private  void initView(){
        item_commod_iv= (ImageView) findViewById(R.id.item_commod_iv);
        item_commod_texts= (TextView) findViewById(R.id.item_commod_texts);
        item_commod_price= (TextView) findViewById(R.id.item_commod_price);
        item_commod_num= (TextView) findViewById(R.id.item_commod_num);
        setViewImage(item_commod_iv,mapData.get("buy_img"));
        item_commod_texts.setText(mapData.get("title"));
        item_commod_price.setText("¥" + mapData.get("discount_price"));
        //减少数量
        findViewById(R.id.item_commod_cut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(productNum>1){
                    --productNum;
                    item_commod_num.setText(String.valueOf(productNum));

                }else{
                    Tools.showToast(context,"最少购买一个");
                }
            }
        });
        //增加数量
        findViewById(R.id.item_commod_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("zyj","maxSaleNum::"+maxSaleNum+":::"+saleableNum);
                if(saleableNum>0&&saleableNum<=productNum){
                    Tools.showToast(context,"最多购买"+productNum+"个");
                    return;
                }
                if(maxSaleNum>0&&maxSaleNum<=productNum){
                    Tools.showToast(context,"最多购买"+productNum+"个");
                    return;
                }
                ++productNum;
                item_commod_num.setText(String.valueOf(productNum));
            }
        });
        //点击下一步
        findViewById(R.id.next_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRequestOrder(getOrderInfo());
            }
        });
        if(mapData.containsKey("saleable_num")&&!TextUtils.isEmpty(mapData.get("saleable_num"))) {
            saleableNum=Integer.parseInt(mapData.get("saleable_num"));
        }
        if(mapData.containsKey("max_sale_num")&&!TextUtils.isEmpty(mapData.get("max_sale_num"))) {
            maxSaleNum=Integer.parseInt(mapData.get("max_sale_num"));
        }
    }

    /**
     *
     * @param productNum
     */
    public void initProductNum(int productNum) {
        this.productNum = productNum;
        item_commod_num.setText(String.valueOf(productNum));
    }

    private String getOrderInfo(){
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("product_code",mapData.get("product_code"));
            jsonObject.put("product_num",String.valueOf(productNum));
            return  jsonObject.toString();
        }catch (Exception e){
        }
        return "";
    }
    /**
     * 生产订单
     */
    private void setRequestOrder(final String orderInfo) {
        if(TextUtils.isEmpty(orderInfo)){
            return;
        }
        String param="order_info="+orderInfo;
        MallReqInternet.in().doPost(MallStringManager.mall_checkoutOrder_v2, param, new MallInternetCallback(context) {

            @Override
            public void loadstat(int flag, String url, Object msg, Object... stat) {
                if(flag>= UtilInternet.REQ_OK_STRING){
                    ArrayList<Map<String,String>> list_data=UtilString.getListMapByJson(UtilString.getListMapByJson(msg).get(0).get("sub_order"));
                    if(list_data.size()>0){
                        Intent intent = new Intent(context,ShoppingOrderActivity.class);
                        intent.putExtra("msg_order", msg.toString());
                        intent.putExtra("order_info", orderInfo);
                        intent.putExtra("url", MallStringManager.mall_checkoutOrder_v2);
                        if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
                            intent.putExtra("stat", (String) stat[0]);
                        }
                    }else{
                        setRequestOrder(orderInfo);
                    }
                }else if(flag==UtilInternet.REQ_CODE_ERROR && msg instanceof Map){
                    @SuppressWarnings("unchecked")
                    Map<String,String> map= (Map<String, String>) msg;
                    if("6000008".equals(map.get("code"))){
                        setRequestOrder(orderInfo);
                    }else{
                        Tools.showToast(context, map.get("msg")+"");
                    }
                }
            }
        });
    }

    public void setViewImage(final ImageView v, String value) {
        v.setVisibility(View.VISIBLE);
        if(TextUtils.isEmpty(value)){
            v.setVisibility(View.GONE);
            return;
        }
        // 异步请求网络图片
        if (value.indexOf("http") == 0) {
            if (value.length() < 10)
                return;
            v.setImageResource(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon);
            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            v.setTag(TAG_ID, value);
            if(context==null)return;
            BitmapRequestBuilder<GlideUrl, Bitmap> requestBuilder = LoadImage.with(context)
                    .load(value)
                    .setImageRound(roundImgPixels)
                    .setPlaceholderId(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                    .setErrorId(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                    .setSaveType(imgLevel)
                    .build();
            if(requestBuilder != null){
                requestBuilder.into(getTarget(v, value));
            }
        }
        // 直接设置为内部图片
        else if (value.indexOf("ico") == 0) {
            InputStream is = v.getResources().openRawResource(Integer.parseInt(value.replace("ico", "")));
            Bitmap bitmap = UtilImage.inputStreamTobitmap(is);
            bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, roundType, roundImgPixels);
            UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
        }
        // 隐藏
        else if (value.equals("hide") || value.length() == 0)
            v.setVisibility(View.GONE);
            // 直接加载本地图片
        else if (!value.equals("ignore")) {
            if (v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(value))
                return;
            v.setTag(TAG_ID, value);
            BitmapRequestBuilder<GlideUrl, Bitmap> requestBuilder = LoadImage.with(context)
                    .load(value)
                    .setImageRound(roundImgPixels)
                    .setSaveType(imgLevel)
                    .build();
            if(requestBuilder != null){
                requestBuilder.placeholder(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                        .error(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                        .into(getTarget(v, value));
            }
        }
    }

    public SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                ImageView img = null;
                if (v.getTag(TAG_ID).equals(url))
                    img = v;
                if (img != null && bitmap != null) {
                    // 图片圆角和宽高适应auther_userImg
                    v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
                }
            }
        };
    }/**
     * 关闭dialog
     */
    public void closeDialog() {

        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, height);
        animation.setDuration(500);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                myDismiss();
                if (buyDialogCallBack != null)
                    buyDialogCallBack.dialogDismiss(productNum);
            }
        });
        view.startAnimation(animation);
    }

    public interface BuyDialogCallBack{
        public void dialogDismiss(int productNum);
    }
    public  BuyDialogCallBack buyDialogCallBack;
    public void setBuyDialogCallBack(BuyDialogCallBack buyDialogCallBack){
        this.buyDialogCallBack= buyDialogCallBack;
    }
}
