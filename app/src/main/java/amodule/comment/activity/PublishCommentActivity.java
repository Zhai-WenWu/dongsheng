package amodule.comment.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import amodule.comment.helper.PublishCommentControler;
import aplug.imageselector.constant.ImageSelectorConstant;

public class PublishCommentActivity extends BaseActivity{
    public static final int REQUEST_SELECT_IMAGE = 0x11;
    public static final String EXTRAS_CODE = "code";
    public static final String EXTRAS_MESSAGE = "message";
    public static final String EXTRAS_TYPE = "type";

    private PublishCommentControler mPublishContrler;

    private String type;
    private String code;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("评论",2,0,0,R.layout.activity_publish_comment);
        initData();
        initView();
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        if(null != bundle){
            type = bundle.getString(EXTRAS_TYPE);
            code = bundle.getString(EXTRAS_CODE);
            message = bundle.getString(EXTRAS_MESSAGE);
        }
    }

    private void initView() {
        mPublishContrler = new PublishCommentControler(this);

        Tools.setStatusBarColor(this, getResources().getColor(R.color.common_top_bg));

        TextView title = (TextView) findViewById(R.id.title);
        title.setText("评论");
        TextView rightText = (TextView) findViewById(R.id.nextStep);
        rightText.setText("发送");
        rightText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPublishContrler.publish(type,"");
            }
        });
        rightText.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_SELECT_IMAGE:
                    ArrayList<String> images = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);
                    mPublishContrler.setImages(images);
                    break;
                default:
                    break;
            }
        }
    }

}
