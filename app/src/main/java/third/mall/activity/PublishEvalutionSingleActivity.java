package third.mall.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xianghatest.R;

import java.util.ArrayList;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ProperRatingBar;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;
import third.mall.upload.EvalutionUploadControl;
import third.mall.view.EvalutionImageLayout;
import xh.windowview.XhDialog;

public class PublishEvalutionSingleActivity extends BaseActivity implements View.OnClickListener {
    public static final String TAG = "tzy";
    public static final String EXTRAS_CODE = "code";
    public static final String EXTRAS_SCORE = "score";
    public static final String EXTRAS_IMAGE = "image";
    public static final int DEFAULT_SCORE = 5;

    private static final int SELECT_IMAE_REQUEST_CODE = 0x1;
    private static final int maxTextCount = 500;

    private ImageView commodityImage;
    private ImageView selectImage;
    private ImageView shareToCircleImage;
    private TextView scoreDescText;
    private TextView contentLengthText;
    private TextView publishButton;
    private ProperRatingBar ratingBar;
    private EditText contentEdit;

    private EvalutionImageLayout imagesLayout;
    private RelativeLayout shareLayout;
    private LinearLayout contentLayout;

    EvalutionUploadControl uploadControl;

    String code = "";
    String image = "";
    int score = DEFAULT_SCORE;

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
        code = intent.getStringExtra(EXTRAS_CODE);
        image = intent.getStringExtra(EXTRAS_IMAGE);
        if (TextUtils.isEmpty(code))
            this.finish();
        score = intent.getIntExtra(EXTRAS_SCORE, DEFAULT_SCORE);

        uploadControl = new EvalutionUploadControl(this);
        uploadControl.setCode(code);
        uploadControl.setOnPublishCallback(new EvalutionUploadControl.OnPublishCallback() {
            @Override
            public void onStratPublish() {
                showUploadingDialog();
            }

            @Override
            public void onSuccess() {
                cancelUploadingDialog();
            }

            @Override
            public void onFailed() {
                cancelUploadingDialog();
            }
        });
    }

    private void initView() {
        publishButton = (TextView) findViewById(R.id.rightText);
        publishButton.setText("发布");
        publishButton.setVisibility(View.VISIBLE);
        commodityImage = (ImageView) findViewById(R.id.commodity_image);
        selectImage = (ImageView) findViewById(R.id.select_image);
        shareToCircleImage = (ImageView) findViewById(R.id.share_image);
        scoreDescText = (TextView) findViewById(R.id.evalution_desc);
        contentLengthText = (TextView) findViewById(R.id.content_length_text);
        contentEdit = (EditText) findViewById(R.id.content_edit);
        contentEdit.setHint(getResources().getString(R.string.publish_evalution_desc_hint));
        ratingBar = (ProperRatingBar) findViewById(R.id.rating_bar);
        imagesLayout = (EvalutionImageLayout) findViewById(R.id.images);
        shareLayout = (RelativeLayout) findViewById(R.id.share_to_circle);
        contentLayout = (LinearLayout) findViewById(R.id.content_layout);

        int itemIwdth = (ToolsDevice.getWindowPx(this).widthPixels - Tools.getDimen(this,R.dimen.dp_100)) / 3;
        int imageWidth = itemIwdth - Tools.getDimen(this,R.dimen.dp_12_5);
        if(imageWidth < Tools.getDimen(this,R.dimen.dp_75)){
            imagesLayout.setViewSize(itemIwdth);
            selectImage.setLayoutParams(new LinearLayout.LayoutParams(imageWidth,imageWidth));
        }
        int starItemWidth = (ToolsDevice.getWindowPx(this).widthPixels - Tools.getDimen(this,R.dimen.dp_198)) / 5 - 10;
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
                uploadControl.setScore(ratingBar.getRating());
                scoreDescText.setText(starEvalutionDesc[ratingBar.getRating() - 1]);
                updateShareLayoutVisibility();
            }
        });
        //设置数据
        ratingBar.setRating(score);

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
            }
        });

        imagesLayout.setOnHierarchyChangeCallback(new EvalutionImageLayout.OnHierarchyChangeCallback() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                Log.i("tzy","onChildViewAdded");
                updateShareLayoutVisibility();
                updateSelectImageVisibility();
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                Log.i("tzy","onChildViewRemoved");
                updateShareLayoutVisibility();
                updateSelectImageVisibility();
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
                selectImage.setSelected(true);
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
     * @return
     */
    private boolean canShareToCircle() {
        return ratingBar.getRating() >= 4
                && contentEdit.getText().length() > 0
                && imagesLayout.getChildCount() > 0;
    }

    /** 更新图片选择显示状态 */
    private void updateSelectImageVisibility() {
        selectImage.setVisibility(imagesLayout.getChildCount() == 3 ? View.GONE : View.VISIBLE);
    }

    /** 更新文字长度提示 */
    private void updateContentLengthText() {
        int contentLength = contentEdit.getText().length();
        contentLengthText.setText(contentLength + "/" + maxTextCount);
        contentLengthText.setTextColor(getResources().getColor(contentLength == maxTextCount ? R.color.comment_color : R.color.psts_tab_text));
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
            default:
                break;
        }
    }

    /**
     * 更新上传数据
     *
     * @param images
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
                publishEvalution();
                break;
            case R.id.select_image:
                openSelectImages();
                break;
        }
    }

    /** 发布评论 */
    private void publishEvalution() {
        uploadControl.setScore(ratingBar.getRating())
                .setContent(contentEdit.getText().toString())
                .setCanShare(canShareToCircle())
                .publishEvalution();
    }

    /** 选择图片 */
    private void openSelectImages() {
        Intent intent = new Intent(this, ImageSelectorActivity.class);
        intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT, 3);
        intent.putExtra(ImageSelectorConstant.EXTRA_DEFAULT_SELECTED_LIST, imagesLayout.getImageArray());
        startActivityForResult(intent, SELECT_IMAE_REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {
        if (isSureBack) {
            super.onBackPressed();
        } else
            showSureBackDialog();
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
                        isSureBack = true;
                        dialog.cancel();
                        PublishEvalutionSingleActivity.this.onBackPressed();
                    }
                })
                .setSureButtonTextColor("#333333")
                .setSureButton("否", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
            mUploadingDialog.setCancelable(false);
        }
        mUploadingDialog.show();
    }

    private void cancelUploadingDialog() {
        if (mUploadingDialog == null || !mUploadingDialog.isShowing())
            return;
        mUploadingDialog.cancel();
    }
}
