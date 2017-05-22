package amodule.article.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import java.util.List;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import amodule.article.view.EditBottomControler;
import amodule.article.view.InputUrlDialog;
import amodule.article.view.TextAndImageMixLayout;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;

/**
 * PackageName : amodule.article.activity
 * Created by MrTrying on 2017/5/19 09:19.
 * E_mail : ztanzeyu@gmail.com
 */

public class ArticleEidtActiivty extends BaseActivity implements View.OnClickListener {

    private final int REQUEST_SELECT_IMAGE = 0x01;
    private final int REQUEST_SELECT_VIDEO = 0x02;

    private EditBottomControler editBottomControler;
    private TextAndImageMixLayout mixLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sufureView页面闪烁
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        initActivity("发文章", 2, 0, 0, R.layout.a_article_edit_activity);

        initView();
    }

    private void initView() {
        initTopBar();
        //初始化底部编辑控制
        initEditBottomControler();

        initMixLayout();
    }

    private void initTopBar() {
        String color = Tools.getColorStr(this, R.color.common_top_bg);
        Tools.setStatusBarColor(this, Color.parseColor(color));
        findViewById(R.id.nextStep).setVisibility(View.VISIBLE);
        findViewById(R.id.nextStep).setOnClickListener(this);
        TextView title= (TextView)findViewById(R.id.title);
        title.setText("写文章");
        ImageView close = (ImageView) findViewById(R.id.leftImgBtn);
//        close.setImageResource();
        SpannableString ss = new SpannableString("标题（64字以内）");
        int titleSize = Tools.getDimen(this,R.dimen.dp_25);
        int hintSize = Tools.getDimen(this,R.dimen.dp_14);
        ss.setSpan(new AbsoluteSizeSpan(titleSize), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new AbsoluteSizeSpan(hintSize), 2, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        EditText editTitle = (EditText) findViewById(R.id.edit_title);
        editTitle.setHint(ss);
    }

    private void initMixLayout() {
        mixLayout = (TextAndImageMixLayout) findViewById(R.id.text_image_mix_ayout);
    }

    /**
     * 初始化底部编辑控制
     */
    private void initEditBottomControler() {
        editBottomControler = (EditBottomControler) findViewById(R.id.edit_controler);
        editBottomControler.setOnSelectImageCallback(
                new EditBottomControler.OnSelectImageCallback() {
                    @Override
                    public void onSelectImage() {
                        Intent intent = new Intent(ArticleEidtActiivty.this, ImageSelectorActivity.class);
                        intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_MODE, ImageSelectorConstant.MODE_MULTI);
                        intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT, 8);
                        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
                    }
                });
        editBottomControler.setOnSelectVideoCallback(
                new EditBottomControler.OnSelectVideoCallback() {
                    @Override
                    public void onSelectVideo() {
                        Toast.makeText(ArticleEidtActiivty.this, "选择视频", Toast.LENGTH_SHORT).show();
                    }
                });
        editBottomControler.setOnAddLinkCallback(
                new EditBottomControler.OnAddLinkCallback() {
                    @Override
                    public void onAddLink() {
                        InputUrlDialog dialog = new InputUrlDialog(ArticleEidtActiivty.this);
                        dialog.setOnReturnResultCallback(
                                new InputUrlDialog.OnReturnResultCallback() {
                                    @Override
                                    public void onSure(String url, String desc) {
                                        mixLayout.addLink(url, desc);
                                    }

                                    @Override
                                    public void onCannel() {

                                    }
                                });
                        dialog.show();
                    }
                });
        editBottomControler.setOnKeyboardControlCallback(
                new EditBottomControler.OnKeyboardControlCallback() {
                    @Override
                    public void onKeyboardControlSwitch() {
                        Toast.makeText(ArticleEidtActiivty.this, "onKeyboardControlSwitch", Toast.LENGTH_SHORT).show();
                    }
                });
        editBottomControler.setOnTextEidtCallback(
                new EditBottomControler.OnTextEidtCallback() {
                    @Override
                    public void onTextBold() {
                        mixLayout.setupTextBold();
                    }

                    @Override
                    public void onTextUnderLine() {
                        mixLayout.setupUnderline();
                    }

                    @Override
                    public void onTextCenter() {
                        mixLayout.setupTextCenter();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_SELECT_IMAGE:
                    List<String> imagePathArray = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);
                    mixLayout.addImageArray(imagePathArray);
                    break;
                case REQUEST_SELECT_VIDEO:

                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nextStep:
                String data = mixLayout.getData();
                Toast.makeText(this, TextUtils.isEmpty(data) ? "下一步" : data, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
