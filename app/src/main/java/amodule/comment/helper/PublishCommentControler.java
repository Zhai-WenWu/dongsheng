package amodule.comment.helper;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import acore.tools.Tools;
import acore.tools.UploadImg;
import amodule.article.upload.CommentUpload;
import amodule.comment.activity.PublishCommentActivity;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;

/**
 * PackageName : amodule.comment.helper
 * Created by MrTrying on 2017/5/26 09:39.
 * E_mail : ztanzeyu@gmail.com
 */

public class PublishCommentControler {
    /** 文章 */
    public static final String TYPE_ARTICLE = "1";
    /** 视频 */
    public static final String TYPE_VIDEO = "2";
    /** 问答 */
    public static final String TYPE_QA = "3";
    /** 电商 */
    public static final String TYPE_DS = "4";

    private Activity mAct;

    private EditText contentEdit;
    private LinearLayout imageLayout;
    private TextView aboutMessageText;

    private ArrayList<String> imagePathArray = new ArrayList<>();
    private final int DEFAULT_IMAGE_COUNT = 3;
    private final int imageCount = DEFAULT_IMAGE_COUNT;
    private boolean needImage = true;

    private Map<String, String> imagePath = new HashMap<>();

    public PublishCommentControler(Activity mAct) {
        this.mAct = mAct;
        initView();
    }

    private void initView() {
        contentEdit = (EditText) mAct.findViewById(R.id.content);
        imageLayout = (LinearLayout) mAct.findViewById(R.id.images_layout);
        aboutMessageText = (TextView) mAct.findViewById(R.id.about_message);

        mAct.findViewById(R.id.add_image).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mAct, ImageSelectorActivity.class);
                        intent.putExtra(ImageSelectorConstant.EXTRA_DEFAULT_SELECTED_LIST, imagePathArray);
                        intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT, imageCount);
                        mAct.startActivityForResult(intent, PublishCommentActivity.REQUEST_SELECT_IMAGE);
                    }
                });
    }

    public void setImages(ArrayList<String> imageArray) {
        this.imagePathArray = imageArray;
        if (imagePathArray.size() > imageCount) {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("最多可选择").append(imageCount).append("张图片");
            Toast.makeText(mAct, strBuilder.toString(), Toast.LENGTH_SHORT).show();
            return;
        }
        //removeAllView
        for (int index = 0; index < imageLayout.getChildCount() - 1 ;) {
            imageLayout.removeViewAt(index);
        }
        //添加
        for (final String path : imagePathArray) {
            View view = LayoutInflater.from(mAct).inflate(R.layout.select_image_item, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.image);
            Glide.with(mAct).load(path).into(imageView);
            ImageView imageDelete = (ImageView) view.findViewById(R.id.delete_image);
            imageDelete.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteImage(path);
                        }
                    });
            view.setTag(path);
            imagePath.put(path, "");
            new UploadImg("", path, new InternetCallback(mAct) {
                @Override
                public void loaded(int i, String s, Object o) {
                    if (i < ReqInternet.REQ_OK_STRING) {
                        imagePath.put(path, o.toString());
                    }
                }
            });
            imageLayout.addView(view, imageLayout.getChildCount() - 1);
        }
    }

    private void deleteImage(String path) {
        imagePathArray.remove(path);
        //移除view
        int imageCount = imageLayout.getChildCount();
        for (int index = 0; index < imageCount; index++) {
            View view = imageLayout.getChildAt(index);
            if (path.equals(view.getTag())) {
                imageLayout.removeView(view);
                break;
            }
        }
    }

    /**
     * 发布
     *
     * @param type
     * @param code
     */
    public void publish(String type, String code) {
        //数据验证
        String tip = checkData();
        if (!TextUtils.isEmpty(tip)) {
            Tools.showToast(mAct, tip);
            return;
        }
        //发请求
        CommentUpload.getInstance().uploadComment(mAct, type, code, getCotnent().toString());
    }

    /**
     * 检查数据
     *
     * @return 提示信息
     */
    public String checkData() {
        String text = contentEdit.getText().toString();
        if (TextUtils.isEmpty(text)) {
            return "内容不能为空";
        }
        if (text.length() < 10) {
            return "发送内容不可以小于10字哦";
        }
        if (text.length() > 2000) {
            return "发送内容不可以大于2000字哦";
        }
        if (needImage) {
            Set<Map.Entry<String, String>> set = imagePath.entrySet();
            for (Map.Entry<String, String> entry : set) {
                if (TextUtils.isEmpty(entry.getValue())) {
                    return "图片上传中";
                }
            }
        }
        return "";
    }

    /**
     * 获取content字段内容
     *
     * @return content数组
     */
    public ArrayList<Map<String, String>> getCotnent() {
        ArrayList<Map<String, String>> contentArray = new ArrayList<>();
        Map<String, String> content = new HashMap<>();
        content.put("text", contentEdit.getText().toString());
        content.put("imgs", imagePathArray.toString());
        contentArray.add(content);
        return contentArray;
    }
}
