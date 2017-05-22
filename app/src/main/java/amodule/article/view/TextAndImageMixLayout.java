package amodule.article.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;

import acore.tools.Tools;
import amodule.article.view.richtext.RichText;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:34.
 * E_mail : ztanzeyu@gmail.com
 */

public class TextAndImageMixLayout extends LinearLayout
        implements BaseView.OnRemoveCallback {

    private boolean isSingleVideo = true;
    private EditTextView currentEditText = null;

    public TextAndImageMixLayout(Context context) {
        this(context, null);
    }

    public TextAndImageMixLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextAndImageMixLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        addRichText(0);
    }

    public void setupTextBold(){
        if(currentEditText != null){
            currentEditText.setupTextBold();
        }
    }

    public void setupUnderline(){
        if(currentEditText != null){
            currentEditText.setupUnderline();
        }
    }

    public void setupTextCenter(){
        if(currentEditText != null){
            currentEditText.setupTextCenter();
        }
    }

    public String getData(){
        StringBuilder builder = new StringBuilder();
        final int length = getChildCount();
        for(int index = 0; index < length ; index ++){
            BaseView baseView = (BaseView) getChildAt(index);
            builder.append(baseView.getOutputData());
        }
        return builder.toString();
    }

    private void addRichText(int insertIndex) {
        EditTextView view = new EditTextView(getContext());
        addView(view, insertIndex,getChildLayoutParams());
        currentEditText = view;
        currentEditText.setEditTextFocus(true);
        view.setOnFocusChangeCallback(new EditTextView.OnFocusChangeCallback() {
            @Override
            public void onFocusChange(EditTextView v, boolean hasFocus) {
                if(hasFocus){
                    currentEditText = v;
                }
            }
        });
    }

    public void addImageArray(List<String> imageUrlArray) {
        if (imageUrlArray == null) {
            return;
        }
        for (String imageUrl : imageUrlArray) {
            addImage(imageUrl);
        }
    }

    /**
     * 添加图片
     * @param imageUrl
     */
    public void addImage(String imageUrl) {
        final int insertIndex = getFoucsIndex() + 1;

        ImageShowView view = new ImageShowView(getContext());
        view.setEnableEdit(true);
        view.setImageUrl(imageUrl);
        view.setmOnRemoveCallback(this);


        addView(view, insertIndex,getChildLayoutParams());
        //默认插入edit
        addRichText(insertIndex + 1);
    }


    /**
     * 添加视频
     * @param coverImageUrl
     * @param videoUrl
     */
    public void addVideo(String coverImageUrl, String videoUrl) {
        final int insertIndex = getFoucsIndex() + 1;

        VideoShowView view = null;
        //如果单个视频，则先遍历parent中是否存在
        if (isSingleVideo) {
            for (int index = 0; index < getChildCount(); index++) {
                View baseView = getChildAt(index);
                if (baseView instanceof VideoShowView) {
                    view = (VideoShowView) baseView;
                    break;
                }
            }
        }
        if (null == view) {
            view = new VideoShowView(getContext());
            addView(view, insertIndex,getChildLayoutParams());
            //默认插入edit
            addRichText(insertIndex + 1);
        }
        view.setEnabled(true);
        view.setVideoData(coverImageUrl, videoUrl);
        view.setmOnRemoveCallback(this);
    }

    public void addLink(String url,String desc){
        currentEditText.setupTextLink(url, desc);
    }

    /**
     * 获取当前聚焦的view位置
     * @return
     */
    public int getFoucsIndex() {
        int index = 0;
        final int length = getChildCount();
        for (int i = 0; i < length; i++) {
            if (currentEditText != null
                    && getChildAt(i).equals(currentEditText))
                return i;
        }
        return index;
    }

    /**
     * 获取childview的layoutParams
     * @return
     */
    private LayoutParams getChildLayoutParams(){
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        return params;
    }

    @Override
    public void onRemove(BaseView view) {
        final int index = indexOfChild(view);
        if (index + 1 < getChildCount()) {
            removeViewAt(index + 1);
        }
        removeView(view);
    }

    public boolean isSingleVideo() {
        return isSingleVideo;
    }

    public void setSingleVideo(boolean singleVideo) {
        isSingleVideo = singleVideo;
    }
}
