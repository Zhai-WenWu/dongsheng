package amodule.article.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import acore.tools.Tools;
import amodule.article.view.richtext.RichText;
import aplug.shortvideo.activity.VideoFullScreenActivity;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:34.
 * E_mail : ztanzeyu@gmail.com
 */

public class TextAndImageMixLayout extends LinearLayout
        implements BaseView.OnRemoveCallback, BaseView.OnClickImageListener {

    private EditTextView currentEditText = null;

    private boolean isSingleVideo = true;

    /**图片集合*/
    //path:url
    private Map<String,String> imageMap = new HashMap<>();

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

    public void setupTextBold() {
        if (currentEditText != null) {
            currentEditText.setupTextBold();
        }
    }

    public void setupUnderline() {
        if (currentEditText != null) {
            currentEditText.setupUnderline();
        }
    }

    public void setupTextCenter() {
        if (currentEditText != null) {
            currentEditText.setupTextCenter();
        }
    }

    public String getData() {
        StringBuilder builder = new StringBuilder();
        final int length = getChildCount();
        for (int index = 0; index < length; index++) {
            BaseView baseView = (BaseView) getChildAt(index);
            builder.append(baseView.getOutputData());
        }
        return builder.toString();
    }

    private void addRichText(int insertIndex) {
        EditTextView view = new EditTextView(getContext());
        addView(view, insertIndex, getChildLayoutParams());
        currentEditText = view;
        currentEditText.setEditTextFocus(true);
        view.setOnFocusChangeCallback(new EditTextView.OnFocusChangeCallback() {
            @Override
            public void onFocusChange(EditTextView v, boolean hasFocus) {
                if (hasFocus) {
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
     *
     * @param imageUrl
     */
    public void addImage(String imageUrl) {
        final int insertIndex = getFoucsIndex() + 1;

        ImageShowView view = new ImageShowView(getContext());
        view.setEnableEdit(true);
        view.setImageUrl(imageUrl);
        view.setmOnRemoveCallback(this);

        addView(view, insertIndex, getChildLayoutParams());

        addImagePath(imageUrl);
        //默认插入edit
        addRichText(insertIndex + 1);
    }

    private void addImagePath(String imageUrl){
        setupImagePath(imageUrl,"");
    }

    public void setupImagePath(String imagePath,String imageUrl){
        imageMap.put(imagePath,imageUrl);
    }

    /**
     * 添加视频
     *
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
            addView(view, insertIndex, getChildLayoutParams());
            addImagePath(coverImageUrl);
            //默认插入edit
            addRichText(insertIndex + 1);
        }else{
            addImagePath(coverImageUrl);
        }
        view.setEnabled(true);
        view.setVideoData(coverImageUrl, videoUrl);
        view.setmOnRemoveCallback(this);
        view.setmOnClickImageListener(this);
    }

    public void addLink(String url, String desc, int start, int end) {
        currentEditText.setupTextLink(url, desc, start, end);
    }

    public String getSelectionText() {
        return currentEditText.getSelectionText();
    }

    public int getSelectionStart() {
        return currentEditText.getSelectionStart();
    }

    public int getSelectionEnd() {
        return currentEditText.getSelectionEnd();
    }

    /**
     * 获取当前聚焦的view位置
     *
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
     *
     * @return
     */
    private LayoutParams getChildLayoutParams() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        return params;
    }

    @Override
    public void onRemove(BaseView view) {
        final int index = indexOfChild(view);
        if (index + 1 < getChildCount()) {
            removeViewAt(index + 1);
        }
        //同时维护图片集合
        removeImagePath(view);
        removeView(view);
    }

    /**
     *
     * @param view
     */
    private void removeImagePath(BaseView view){
        if(view instanceof ImageShowView){
            imageMap.remove(((ImageShowView)view).getImageUrl());
        }else if(view instanceof VideoShowView){
            imageMap.remove(((VideoShowView)view).getCoverImageUrl());
        }
    }

    /**
     * 获取第一个张图片
     * @return
     */
    public String getFirstImage() {
        String imageUrl = "";
        ImageShowView view = getFirstImageView();
        if(view != null){
            imageUrl = view.getImageUrl();
        }
        return imageUrl;
    }


    /**
     * 获取第一个Video封面图
     * @return
     */
    public String getFirstCoverImage() {
        String coverImageUrl = "";
        VideoShowView view = getFirstVideoView();
        if(view != null){
            coverImageUrl = view.getCoverImageUrl();
        }
        return coverImageUrl;
    }

    /**
     * 获取第一个视频url
     * @return
     */
    public String getFirstVideoUrl() {
        String videoUrl = "";
        VideoShowView view = getFirstVideoView();
        if(view != null){
            videoUrl = view.getVideoUrl();
        }
        return videoUrl;
    }

    /**
     * 是否有图片
     * @return
     */
    public boolean hasImage() {
        ImageShowView view = getFirstImageView();
        return view != null;
    }

    /**
     * 是否有视频
     * @return
     */
    public boolean hasVideo() {
        VideoShowView view = getFirstVideoView();
        return view != null;
    }

    /**
     * 是否有文本内容
     * @return
     */
    public boolean hasText() {
        boolean hasText = false;
        for (int index = 0; index < getChildCount(); index++) {
            View view = getChildAt(index);
            if (view instanceof EditTextView) {
                String text = ((EditTextView) view).getOutputData();
                if (!TextUtils.isEmpty(text)) {
                    hasText = true;
                    return hasText;
                }
            }
        }
        return hasText;
    }

    /**
     * 获取第一个图片组件
     * @return
     */
    private ImageShowView getFirstImageView() {
        for (int index = 0; index < getChildCount(); index++) {
            View view = getChildAt(index);
            if (view instanceof ImageShowView) {
                return (ImageShowView) view;
            }
        }
        return null;
    }

    /**
     * 获取第一个Video组件
     * @return
     */
    private VideoShowView getFirstVideoView() {
        for (int index = 0; index < getChildCount(); index++) {
            View view = getChildAt(index);
            if (view instanceof VideoShowView) {
                return (VideoShowView) view;
            }
        }
        return null;
    }

    public boolean isSingleVideo() {
        return isSingleVideo;
    }

    public void setSingleVideo(boolean singleVideo) {
        isSingleVideo = singleVideo;
    }

    public ArrayList<String> getImageArray(){
        ArrayList<String> imageUrlArray = new ArrayList<>();
        VideoShowView videoShowView = getFirstVideoView();
        String coverImage = "";
        if(videoShowView != null){
            coverImage = videoShowView.getCoverImageUrl();
        }
        Set<Map.Entry<String,String>> entries = imageMap.entrySet();
        for(Map.Entry entry:entries){
            imageUrlArray.add(entry.getKey().toString());
        }
        imageUrlArray.remove(coverImage);
        return imageUrlArray;
    }

    @Override
    public void onClick(View v, String url) {
        Intent intent = new Intent(getContext(), VideoFullScreenActivity.class);
        intent.putExtra(VideoFullScreenActivity.EXTRA_VIDEO_TYPE, VideoFullScreenActivity.LOCAL_VIDEO);
        intent.putExtra(VideoFullScreenActivity.EXTRA_VIDEO_URL, url);
        getContext().startActivity(intent);
    }


}
