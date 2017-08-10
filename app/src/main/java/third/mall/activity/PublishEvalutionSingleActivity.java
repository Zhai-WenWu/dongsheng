package third.mall.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ProperRatingBar;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;
import third.mall.upload.EvalutionUploadControl;
import third.mall.view.CommodEvalutionImageItem;

public class PublishEvalutionSingleActivity extends BaseActivity implements View.OnClickListener {
    public static final String TAG = "tzy";
    public static final String EXTRAS_CODE = "code";
    public static final String EXTRAS_SCORE = "score";
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

    private LinearLayout imagesLayout;
    private RelativeLayout shareLayout;

    EvalutionUploadControl uploadControl;

    String code = "";
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
        ratingBar = (ProperRatingBar) findViewById(R.id.rating_bar);
        imagesLayout = (LinearLayout) findViewById(R.id.images);
        shareLayout = (RelativeLayout) findViewById(R.id.share_to_circle);
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

        imagesLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                updateShareLayoutVisibility();
            }
        });

        selectImage.setOnClickListener(this);
        publishButton.setOnClickListener(this);
    }

    /**
     * 是否可以分享到美食圈
     *
     * @return
     */
    private boolean canShareToCircle() {
        return ratingBar.getRating() >= 4
                && contentEdit.getText().length() > 0
                && imageArray.size() > 0;
    }

    /** 更新分享layout显示状态 */
    private void updateShareLayoutVisibility() {
        if (canShareToCircle()) {
            if (shareLayout.getVisibility() == View.GONE) {
                selectImage.setSelected(true);
            }
        }
        shareLayout.setVisibility(canShareToCircle() ? View.VISIBLE : View.GONE);
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
                    Log.i(TAG,"images = " + images.toString());
                    //更新上传数据，必须先于UI更新
                    updateUploadImages(images);
                    //UI更新同时更新数据
                    updateImage(images);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 更新上传数据
     * @param images
     */
    private void updateUploadImages(ArrayList<String> images) {
        //对比新增数据
        for(String imagePath:images){
            if(!imageArray.contains(imagePath))
                uploadControl.uploadImage(imagePath);//上传
        }
        //对比旧的移除数据
        for(String imagePath:imageArray){
            if(!images.contains(imagePath))
                uploadControl.delUploadImage(imagePath);//移除上传
        }
    }

    ArrayList<String> imageArray = new ArrayList<>();

    /**
     * 更新UI以及数据集合
     * @param images
     */
    private void updateImage(ArrayList<String> images) {
        imageArray.clear();
        //替换数据
        for (int index = 0, length = images.size(); index < length; index++) {
            CommodEvalutionImageItem item = null;
            String imagePath = images.get(index);
            Log.i(TAG,"imagePath = " + imagePath);
            if(index < imagesLayout.getChildCount()){
                //有image了
                Log.i(TAG,"复用ImageView");
                item = (CommodEvalutionImageItem) imagesLayout.getChildAt(index);
            }else {
                //没有image
                item = new CommodEvalutionImageItem(this);
                imagesLayout.addView(item);
                Log.i(TAG,"新建ImageView");
            }
            item.setTag(R.id.image_path, imagePath);
            imageArray.add(imagePath);
            setImage(imagePath, item);
        }
        //移除多余view
        for (int index = imagesLayout.getChildCount() - 1; index >= images.size(); index--) {
            imagesLayout.removeViewAt(index);
        }
        selectImage.setVisibility(imagesLayout.getChildCount() == 3 ? View.GONE : View.VISIBLE);
    }

    private void setImage(final String imagePath, final CommodEvalutionImageItem item) {
        item.setImage(imagePath, new CommodEvalutionImageItem.OnLoadImageFailed() {
            @Override
            public void onLoadFailed() {
                imageArray.remove(imagePath);
                imagesLayout.removeView(item);
                uploadControl.delUploadImage(imagePath);
            }
        });
        item.setRemoveClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagesLayout.removeView(item);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rightText:
                uploadControl.publishEvalution();
                break;
            case R.id.select_image:
                openSelectImages();
                break;
        }
    }

    /**
     * 选择图片
     */
    private void openSelectImages() {
        Intent intent = new Intent(this, ImageSelectorActivity.class);
        intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT, 3);
        intent.putExtra(ImageSelectorConstant.EXTRA_DEFAULT_SELECTED_LIST, imageArray);
        startActivityForResult(intent, SELECT_IMAE_REQUEST_CODE);
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
