package third.mall.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ProperRatingBar;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;
import aplug.web.ShowTemplateWeb;
import aplug.web.tools.XHTemplateManager;
import third.mall.upload.EvalutionUploadControl;
import third.mall.view.EvalutionImageLayout;
import xh.windowview.XhDialog;

public class PublishEvalutionSingleActivity extends BaseActivity implements View.OnClickListener {
    /** 统计id */
    public static final String STATISTICS_ID = "a_publish_commerce";
    public static final String STATISTICS_RETURN_ID = "a_comcoment_return";
    public static final String STATISTICS_PUBLISH_ID = "a_comcoment_result";
    /** 传参 key */
    public static final String EXTRAS_ORDER_ID = "order_id";
    public static final String EXTRAS_PRODUCT_CODE = "product_code";
    public static final String EXTRAS_PRODUCT_IMAGE = "product_img";
    public static final String EXTRAS_SCORE = "score";
    public static final String EXTRAS_POSITION = "position";
    public static final String EXTRAS_ID = "id";
    /** 默认值 */
    public static final int DEFAULT_SCORE = 5;
    public static final int MAX_IMAGE = 3;
    /**  */
    private static final int SELECT_IMAE_REQUEST_CODE = 0x1;
    private static final int maxTextCount = 500;

    private LinearLayout selectImage;
    private ImageView shareToCircleImage;
    private TextView scoreDescText;
    private TextView selectImageText;
    private TextView contentLengthText;
    private TextView publishButton;
    private ProperRatingBar ratingBar;
    private EditText contentEdit;

    private EvalutionImageLayout imagesLayout;
    private RelativeLayout shareLayout;

    EvalutionUploadControl uploadControl;

    String orderID = "";
    String name = "";
    String productID = "";
    String image = "";
    int position = -1;
    int id = -1;
    int score = DEFAULT_SCORE;
    int status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("发布评价", 6, 0, R.layout.c_view_bar_title, R.layout.activity_publish_evalution_single);
        initData();
        initView();
        setListener();
    }

    private void initData() {
        Intent intent = getIntent();
        orderID = intent.getStringExtra(EXTRAS_ORDER_ID);
        productID = intent.getStringExtra(EXTRAS_PRODUCT_CODE);
        if (TextUtils.isEmpty(productID) || TextUtils.isEmpty(orderID))
            this.finish();
        image = intent.getStringExtra(EXTRAS_PRODUCT_IMAGE);
        //url规则中只能使用String类型，这里处理类型转换异常
        try{
            score = Integer.parseInt(intent.getStringExtra(EXTRAS_SCORE));
        }catch (Exception ignore){
            score = DEFAULT_SCORE;
        }
        id = intent.getIntExtra(EXTRAS_ID,id);
        position = intent.getIntExtra(EXTRAS_POSITION,position);

        uploadControl = new EvalutionUploadControl(this);
        uploadControl.setOrderId(orderID);
        uploadControl.setProductId(productID);
        uploadControl.setOnPublishCallback(new EvalutionUploadControl.OnPublishCallback() {
            @Override
            public void onStratPublish() {
                showUploadingDialog();
            }

            @Override
            public void onSuccess(Object msg) {
                XHClick.mapStat(PublishEvalutionSingleActivity.this,STATISTICS_PUBLISH_ID,"成功提交","");
                cancelUploadingDialog();
                Map<String,String> data = StringManager.getFirstMap(msg);
                if(data.containsKey("is_has") && "1".equals(data.get("is_has"))){
                    //TODO
                    Intent intent = new Intent(PublishEvalutionSingleActivity.this,EvalutionSuccessActivity.class);
                    intent.putExtra(ShowTemplateWeb.REQUEST_METHOD, XHTemplateManager.XHDISH);
                    intent.putExtra(ShowTemplateWeb.NOW_DATA_ARR,new String[]{"94888485"});
                    intent.putExtra(EvalutionSuccessActivity.EXTRAS_ID,id);
                    intent.putExtra(EvalutionSuccessActivity.EXTRAS_POSITION,position);
                    intent.putExtra("url","http://m.xiangha.com");
                    startActivityForResult(intent,OrderStateActivity.request_order);
                    startActivityForResult(
                            new Intent(PublishEvalutionSingleActivity.this, EvalutionSuccessActivity.class)
                                    .putExtra(EvalutionSuccessActivity.REQUEST_METHOD, XHTemplateManager.DSSUCCESSCOMMENT)
                                    .putExtra(EvalutionSuccessActivity.EXTRAS_ID,id)
                                    .putExtra(EvalutionSuccessActivity.EXTRAS_POSITION,position),
                            OrderStateActivity.request_order
                    );
                }else{
                    setResult(RESULT_OK);
                    PublishEvalutionSingleActivity.this.finish();
                }
            }

            @Override
            public void onFailed(String msg) {
                Tools.showToast(PublishEvalutionSingleActivity.this,msg);
                XHClick.mapStat(PublishEvalutionSingleActivity.this,STATISTICS_PUBLISH_ID,"提交失败","");
                cancelUploadingDialog();
            }
        });
    }

    private void initView() {
        publishButton = (TextView) findViewById(R.id.rightText);
        publishButton.setText("发布");
        publishButton.setVisibility(View.VISIBLE);
        ImageView commodityImage = (ImageView) findViewById(R.id.commodity_image);
        selectImage = (LinearLayout) findViewById(R.id.select_image);
        shareToCircleImage = (ImageView) findViewById(R.id.share_image);
        scoreDescText = (TextView) findViewById(R.id.evalution_desc);
        selectImageText = (TextView) findViewById(R.id.select_image_text);
        contentLengthText = (TextView) findViewById(R.id.content_length_text);
        contentEdit = (EditText) findViewById(R.id.content_edit);
        contentEdit.setHint(getResources().getString(R.string.publish_evalution_desc_hint));
        ratingBar = (ProperRatingBar) findViewById(R.id.rating_bar);
        imagesLayout = (EvalutionImageLayout) findViewById(R.id.images);
        shareLayout = (RelativeLayout) findViewById(R.id.share_to_circle);

        int itemIwdth = (ToolsDevice.getWindowPx(this).widthPixels - Tools.getDimen(this,R.dimen.dp_100)) / 3;
        int imageWidth = itemIwdth - Tools.getDimen(this,R.dimen.dp_12_5);
        if(imageWidth < Tools.getDimen(this,R.dimen.dp_75)){
            imagesLayout.setViewSize(itemIwdth);
            selectImage.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageWidth));
        }
        int starItemWidth = (ToolsDevice.getWindowPx(this).widthPixels - Tools.getDimen(this,R.dimen.dp_198)) / 5;
        int defaultWidth = Tools.getDimen(this,R.dimen.dp_32);
        int defaultStarWidth = Tools.getDimen(this,R.dimen.dp_18);
        if(starItemWidth < defaultWidth){
            int starWidth = defaultStarWidth * starItemWidth / Tools.getDimen(this,R.dimen.dp_32);
            int difference = starWidth - Tools.getDimen(this,R.dimen.dp_16);
            ratingBar.setStarWidth(starItemWidth,difference > 0?difference:ratingBar.getTickSpacing());
        }

        Glide.with(this).load(image)
                .placeholder(R.drawable.i_nopic)
                .error(R.drawable.i_nopic)
                .into(commodityImage);
    }

    private void setListener() {
        final String[] starEvalutionDesc = getResources().getStringArray(R.array.evalution_star_descriptions);
        ratingBar.setListener(new ProperRatingBar.RatingListener() {
            @Override
            public void onRatePicked(ProperRatingBar ratingBar) {
                XHClick.mapStat(PublishEvalutionSingleActivity.this,STATISTICS_ID,"点击星星","");
                uploadControl.setScore(ratingBar.getRating());
                scoreDescText.setText(starEvalutionDesc[ratingBar.getRating() - 1]);
                updateShareLayoutVisibility();
            }
        });
        //设置数据
        ratingBar.setRating(score);
        scoreDescText.setText(starEvalutionDesc[score - 1]);

        contentEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int currentLength = s.toString().length();
                int value = currentLength - maxTextCount;
                if (value > 0) {
                    contentEdit.setText(s.subSequence(0, s.length() - value));
                    contentEdit.setSelection(contentEdit.getText().length());
                    Tools.showToast(PublishEvalutionSingleActivity.this, "内容最多" + maxTextCount + "字");
                    ToolsDevice.keyboardControl(false, PublishEvalutionSingleActivity.this, contentEdit);
                }
                updateShareLayoutVisibility();
                updateContentLengthText();
            }
        });

        shareToCircleImage.setSelected(true);
        shareToCircleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shareToCircleImage.isSelected()) {
                    shareToCircleImage.setSelected(false);
                    shareToCircleImage.setBackgroundResource(R.drawable.evalution_can_share);
                } else {
                    shareToCircleImage.setSelected(true);
                    shareToCircleImage.setBackgroundResource(R.drawable.evalution_can_share_selected);
                }
                XHClick.mapStat(PublishEvalutionSingleActivity.this,STATISTICS_ID,"点击分享美食圈","");
            }
        });

        imagesLayout.setOnHierarchyChangeCallback(new EvalutionImageLayout.OnHierarchyChangeCallback() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                updateShareLayoutVisibility();
                updateSelectImageStatus();
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                updateShareLayoutVisibility();
                updateSelectImageStatus();
                //移除上传图片
                if (child != null && child.getTag(R.id.image_path) != null) {
                    uploadControl.delUploadImage(child.getTag(R.id.image_path).toString());
                }
            }
        });

        selectImage.setOnClickListener(this);
        publishButton.setOnClickListener(this);
    }

    /** 更新分享layout显示状态 */
    private void updateShareLayoutVisibility() {
        if (canShareToCircle()) {
            if(shareLayout.getVisibility() == View.GONE){
                shareToCircleImage.setSelected(true);
                shareToCircleImage.setBackgroundResource(R.drawable.evalution_can_share_selected);
            }
            shareLayout.setVisibility(View.VISIBLE);
        }else{
            shareLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 是否可以分享到美食圈
     *
     * @return 是否显示分享到美食圈
     */
    private boolean canShareToCircle() {
        /*4星以上 && 去头尾空格内容长度 > 0 && 图片 1 张以上*/
        return ratingBar.getRating() >= 4
                && contentEdit.getText().toString().trim().length() > 0
                && imagesLayout.getChildCount() > 0;
    }

    /** 更新图片选择显示状态 */
    private void updateSelectImageStatus() {
        int imagesCount =  imagesLayout.getChildCount();
        selectImage.setVisibility(imagesCount == MAX_IMAGE ? View.GONE : View.VISIBLE);
        if(imagesCount > 0 && imagesCount <= 3){
            StringBuffer stringBuilder = new StringBuffer().append(imagesCount).append("/3");
            selectImageText.setText(stringBuilder.toString());
        }else{
            selectImageText.setText("添加图片");
        }
    }

    /** 更新文字长度提示 */
    private void updateContentLengthText() {
        int contentLength = contentEdit.getText().length();
        contentLengthText.setText(contentLength + "/" + maxTextCount);
        contentLengthText.setTextColor(getResources().getColor(contentLength == maxTextCount ? R.color.comment_color : R.color.common_super_tint_text));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_IMAE_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> images = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);
                    //更新上传数据，必须先于UI更新
                    updateUploadImages(images);
                    //UI更新同时更新数据
                    imagesLayout.updateImage(images);
                }
                break;
            case OrderStateActivity.request_order:
                if (resultCode == OrderStateActivity.result_comment_success) {
                    status = resultCode;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 更新上传数据
     *
     * @param images 新选取的数组
     */
    private void updateUploadImages(ArrayList<String> images) {
        //对比新增数据
        for (String imagePath : images) {
            if (!imagesLayout.getImageArray().contains(imagePath))
                uploadControl.uploadImage(imagePath);//上传
        }
        //对比旧的移除数据
        for (String imagePath : imagesLayout.getImageArray()) {
            if (!images.contains(imagePath))
                uploadControl.delUploadImage(imagePath);//移除上传
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rightText:
                XHClick.mapStat(PublishEvalutionSingleActivity.this,STATISTICS_ID,"点击发布按钮","");
                publishEvalution();
                break;
            case R.id.select_image:
                XHClick.mapStat(PublishEvalutionSingleActivity.this,STATISTICS_ID,"点击添加图片","");
                openSelectImages();
                break;
        }
    }

    /** 发布评论 */
    private void publishEvalution() {
        if(!ToolsDevice.isNetworkAvailable(this)){
            Tools.showToast(this,"网络异常，请检查网络");
            return;
        }
        uploadControl.setScore(ratingBar.getRating())
                .setContent(contentEdit.getText().toString().trim())
                .setCanShare(canShareToCircle())
                .publishEvalution();
    }

    /** 选择图片 */
    private void openSelectImages() {
        Intent intent = new Intent(this, ImageSelectorActivity.class);
        intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT, MAX_IMAGE - imagesLayout.getImageArray().size());
        intent.putExtra(ImageSelectorConstant.EXTRA_NOT_SELECTED_LIST, imagesLayout.getImageArray());
        startActivityForResult(intent, SELECT_IMAE_REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {
        if (isSureBack) {
            super.onBackPressed();
        } else
            showSureBackDialog();
    }

    @Override
    public void finish() {
        if(id != -1 && position != -1){
            Intent intent = new Intent();
            intent.putExtra("code", String.valueOf(id));
            intent.putExtra("position", String.valueOf(position));
            intent.putExtra("order_id",orderID);
            setResult(status, intent);
        }
        super.finish();
    }

    /** 确认返回 */
    private boolean isSureBack = false;

    /** 显示确认返回dialog */
    private void showSureBackDialog() {
        final XhDialog dialog = new XhDialog(this);
        dialog.setMessage("是否取消发布")
                .setCanselButton("是", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        XHClick.mapStat(PublishEvalutionSingleActivity.this, STATISTICS_RETURN_ID,"是否取消发布","是");
                        isSureBack = true;
                        dialog.cancel();
                        PublishEvalutionSingleActivity.this.onBackPressed();
                    }
                })
                .setSureButtonTextColor("#333333")
                .setSureButton("否", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        XHClick.mapStat(PublishEvalutionSingleActivity.this, STATISTICS_RETURN_ID,"是否取消发布","否");
                        isSureBack = false;
                        dialog.cancel();
                    }
                })
                .show();
    }

    private Dialog mUploadingDialog;

    private void showUploadingDialog() {
        if (mUploadingDialog != null && mUploadingDialog.isShowing())
            return;
        if (mUploadingDialog == null) {
            mUploadingDialog = new Dialog(this, R.style.dialog);
            mUploadingDialog.setContentView(R.layout.ask_upload_dialoglayout);
//            mUploadingDialog.setCancelable(false);
            mUploadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    uploadControl.cancelUpload();
                }
            });
        }

        mUploadingDialog.show();
    }

    private boolean cancelUploadingDialog() {
        if (mUploadingDialog == null || !mUploadingDialog.isShowing())
            return false;
        mUploadingDialog.cancel();
        return true;
    }
}
