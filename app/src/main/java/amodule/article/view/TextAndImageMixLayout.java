package amodule.article.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.article.activity.edit.ArticleEidtActiivty;
import amodule.article.activity.edit.VideoEditActivity;
import amodule.article.view.richtext.RichParser;
import amodule.upload.callback.UploadListNetCallBack;
import aplug.basic.BreakPointControl;
import aplug.shortvideo.activity.VideoFullScreenActivity;
import xh.windowview.XhDialog;

import static aplug.basic.BreakPointUploadManager.TYPE_IMG;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:34.
 * E_mail : ztanzeyu@gmail.com
 */
public class TextAndImageMixLayout extends LinearLayout
        implements BaseView.OnRemoveCallback, BaseView.OnClickImageListener {

    private int maxTextCount = 1000;

    private EditTextView currentEditText = null;

    private boolean isSingleVideo = false;

    /** 图片集合 */
    //path:url
    private Map<String, String> imageMap = new HashMap<>();

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
        addRichText(-1, "");
    }

    /** 设置上传需要数据 */
    public void setXHServiceData(String content) {
        if(TextUtils.isEmpty(content))return;
        removeAllViews();
        List<Map<String, String>> dataArray = StringManager.getListMapByJson(content);
        for (Map<String, String> map : dataArray) {
            switch (map.get("type")) {
                case BaseView.TEXT:
                    handlerTextData(map.get("html"));
                    break;
                case BaseView.IMAGE:
                    addImage(map.get("imageurl"), false, "");
                    break;
                case BaseView.IMAGE_GIF:
                    addImage(map.get("gifurl"), false, "");
                    break;
                case BaseView.VIDEO:
                    addVideo(map.get("videosimageurl"), map.get("videourl"), false, "");
                    break;
                case BaseView.URLS:
                    //do nothing
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 处理 text 数据
     *
     * @param html
     */
    private void handlerTextData(String html) {
        EditTextView editTextView = addRichText(-1, "");
        boolean isCenter = false;
        String htmlTmep = new String(html);
        //解析段落是否居中
        String propertyStr = htmlTmep.substring(htmlTmep.indexOf("<p") + 2, htmlTmep.indexOf(">"));
        String[] properties = propertyStr.split(" ");
        for (String property : properties) {
            if (property.contains("align")) {
                String propValue = property.substring(property.indexOf("\""), property.lastIndexOf("\""));
                isCenter = "center".equals(propValue);
                break;
            }
        }
        //处理<a></a>
        while (htmlTmep.indexOf("<a") >= 0) {
            int startIndex = htmlTmep.indexOf("<a");
            int endIndex = htmlTmep.indexOf("</a>") + 4;
            if (startIndex >= 0 && startIndex < htmlTmep.length()
                    && endIndex >= 0 && endIndex < htmlTmep.length()) {
                String aTagData = htmlTmep.substring(startIndex, endIndex);
                String title = aTagData.substring(aTagData.indexOf(">") + 1, aTagData.indexOf("</a>"));
                String url = "";
                String apropertyStr = aTagData.substring(aTagData.indexOf("<a") + 2, aTagData.indexOf(">"));
                String[] aproperties = apropertyStr.split(" ");
                for (String property : aproperties) {
                    if (property.contains("href")) {
                        url = property.substring(property.indexOf("\"") + 1, property.lastIndexOf("\""));
                        break;
                    }
                }
                if (!TextUtils.isEmpty(title)
                        && !TextUtils.isEmpty(url))
                    editTextView.addLinkToData(url, title);
                htmlTmep = htmlTmep.replaceFirst(aTagData, "");
            }
        }

        editTextView.setCenterHorizontal(isCenter);
        //删除<p></p>
        htmlTmep = htmlTmep.replace("</p>","");
        if(htmlTmep.indexOf(">") + 1 >= 0)
            html = htmlTmep.substring(htmlTmep.indexOf(">") + 1,htmlTmep.length());
        editTextView.setTextFrormHtml(html);
    }


    /**
     * 获取上传需要的数据
     *
     * @return
     */
    public String getXHServiceData() {
        JSONArray jsonArray = new JSONArray();
        final int length = getChildCount();
        for (int index = 0; index < length; index++) {
            BaseView baseView = (BaseView) getChildAt(index);
            JSONObject jsonObject = baseView.getOutputData();
            if(jsonObject != null)
                jsonArray.put(jsonObject);
            Log.i("tzy",jsonArray.toString());
        }
        jsonArray.put(getUrlsJsonObj());
        return jsonArray.toString();
    }

    /**
     * 获取Url链接jsonObj
     *
     * @return
     */
    private JSONObject getUrlsJsonObj() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", BaseView.URLS);
            List<Map<String, String>> arrayList = new ArrayList<>();
            final int length = getChildCount();
            for (int index = 0; index < length; index++) {
                View view = getChildAt(index);
                if (view instanceof EditTextView)
                    arrayList.addAll(((EditTextView) view).getLinkMapArray());
            }
            JSONArray jsonArray = Tools.list2JsonArray(arrayList);
            jsonObject.put("urls", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * @param insertIndex
     */
    private EditTextView addRichText(int insertIndex, CharSequence content) {
        EditTextView view = new EditTextView(getContext());
        if (insertIndex == -1)
            addView(view, getChildLayoutParams());
        else
            addView(view, insertIndex, getChildLayoutParams());
        currentEditText = view;
        currentEditText.setEditTextFocus(true);
        view.setOnFocusChangeCallback(new EditTextView.OnFocusChangeCallback() {
            @Override
            public void onFocusChange(EditTextView v, boolean hasFocus) {
                if (hasFocus){
                    currentEditText = v;
                }
            }
        });
        currentEditText.setOnAfterTextChanged(new EditTextView.OnAfterTextChanged() {
            @Override
            public void afterTextChanged(Editable s) {
                int value = getTextCount() - maxTextCount;
                if(value > 0){
                    currentEditText.setText(s.subSequence(0,s.length() - value));
                    currentEditText.setSelection(currentEditText.getText().length());
                    Tools.showToast(getContext(),"内容不能超过" + maxTextCount + "字");
                }
            }
        });
        if (!TextUtils.isEmpty(content))
            view.setText(content);
        return view;
    }

    /**
     * @param imageUrlArray
     */
    public void addImageArray(List<String> imageUrlArray) {
        if (imageUrlArray == null)
            return;
        CharSequence content = currentEditText.getSelectionEndContent();
        for (int index = 0; index < imageUrlArray.size(); index++) {
            String imageUrl = imageUrlArray.get(index);
            addImage(imageUrl, true, index == imageUrlArray.size() - 1 ? content : "");
        }
    }

    /**
     * 上传图片
     *
     * @param imagePath
     */
    public void uploadImage(final String imagePath) {
        //为空或者为服务器链接则忽略
        if (TextUtils.isEmpty(imagePath)
                || imagePath.startsWith("http://"))
            return;
        //上传图片
        new BreakPointControl(getContext(),"",imagePath,TYPE_IMG).start(new UploadListNetCallBack() {
            @Override
            public void onProgress(double progress, String uniqueId) {

            }

            @Override
            public void onSuccess(String url, String uniqueId, JSONObject jsonObject) {
                imageMap.put(imagePath, url);
            }

            @Override
            public void onFaild(String faild, String uniqueId) {

            }

            @Override
            public void onLastUploadOver(boolean flag, String responseStr) {

            }

            @Override
            public void onProgressSpeed(String uniqueId, long speed) {

            }
        });
    }

    /**
     * 添加图片
     *
     * @param imageUrl
     */
    public void addImage(String imageUrl, boolean ifAddText, CharSequence content) {
        if (TextUtils.isEmpty(imageUrl))
            return;
        final int insertIndex = getFoucsIndex() + 1;

        ImageShowView view = new ImageShowView(getContext());
        view.setEnableEdit(true);
        view.setImageUrl(imageUrl);
        view.setmOnRemoveCallback(this);

        addView(view, insertIndex, getChildLayoutParams());

        imageMap.put(imageUrl, "");
        uploadImage(imageUrl);
        //如果网络图片则不上传，直接替换map数据
        if(imageUrl.startsWith("http")){
            imageMap.put(imageUrl,imageUrl);
        }

        //默认插入edit
        if (ifAddText)
            addRichText(insertIndex + 1, content);
    }

    /**
     * 有序输出图片array
     *
     * @return
     */
    public ArrayList<Map<String, String>> getImageMapArray() {
        ArrayList<Map<String, String>> arrayList = new ArrayList<>();
        for (int index = 0; index < getChildCount(); index++) {
            View view = getChildAt(index);
            if (view instanceof ImageShowView) {
                Map<String, String> map = new HashMap<>();
                String path = ((ImageShowView) view).getImageUrl();
                Log.i("articleUpload","getImageMapArray() path:" + path + "    url:" + imageMap.get(path));
                map.put("path", path);
                map.put("url", imageMap.get(path));
                arrayList.add(map);
            }
        }
        return arrayList;
    }

    private int maxVideoCount = 1;

    /**
     * 添加视频
     *
     * @param coverImageUrl
     * @param videoUrl
     */
    public void addVideo(String coverImageUrl, String videoUrl, boolean ifAddText, CharSequence content) {
        int videoCount = 0;
        for (int index = 0; index < getChildCount(); index++) {
            View baseView = getChildAt(index);
            if (baseView instanceof VideoShowView)
                videoCount++;
        }
        if(videoCount >= maxVideoCount){
            Tools.showToast(getContext(),"最多可选择" + maxVideoCount + "视频");
            return;
        }
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
            //默认插入edit
            if (ifAddText)
                addRichText(insertIndex + 1, content);
        }
        view.setEnableEdit(true);
        view.setVideoData(coverImageUrl, videoUrl);
        view.setmOnRemoveCallback(this);
        view.setmOnClickImageListener(this);
    }

    //=============文本操作=====================================

    /**
     * 添加link
     *
     * @param url
     * @param desc
     * @param start
     * @param end
     */
    public void addLink(String url, String desc, int start, int end) {
        if (currentEditText != null)
            currentEditText.setupTextLink(url, desc, start, end);
    }

    /** 加粗 */
    public void setupTextBold() {
        if (currentEditText != null)
            currentEditText.setupTextBold();
    }

    public void setupUnderline() {
        if (currentEditText != null)
            currentEditText.setupUnderline();
    }

    public void setupTextCenter() {
        if (currentEditText != null)
            currentEditText.setupTextCenter();
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
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onRemove(final BaseView view) {
        final XhDialog dialog = new XhDialog(getContext());
        dialog.setTitle("确定删除？");
        dialog.setCanselButton("取消", new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        dialog.setSureButton("确定", new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeBaseView(view);
                dialog.cancel();
            }
        });
        dialog.show();
    }

    /**
     * 执行remove操作
     *
     * @param view
     */
    private void removeBaseView(BaseView view) {
        final int index = indexOfChild(view);
        String text = null;
        if (index + 1 < getChildCount()) {
            View removeView = getChildAt(index + 1);
            if (removeView instanceof EditTextView)
                text = ((EditTextView) removeView).getTextHtml();
            removeViewAt(index + 1);
        }
        //同时维护图片集合
        removeImagePath(view);
        removeView(view);

        for (int i = index; i >= 0; i--) {
            View lastView = getChildAt(i);
            if (lastView instanceof EditTextView) {
                ((EditTextView) lastView).appendText((Editable) RichParser.fromHtml(text));
                break;
            }
        }

        if (getContext() instanceof ArticleEidtActiivty) {
            if (view instanceof ImageShowView)
                XHClick.mapStat(getContext(), "a_ArticleEdit", "编辑文章内容", "删除图片");
            else if (view instanceof VideoShowView)
                XHClick.mapStat(getContext(), "a_ArticleEdit", "编辑文章内容", "删除视频");
        } else if (getContext() instanceof VideoEditActivity) {
            if (view instanceof VideoShowView)
                XHClick.mapStat(getContext(), "a_ShortVideoEdit", "编辑视频内容", "删除视频");
        }
    }

    /**
     * @param view
     */
    private void removeImagePath(BaseView view) {
        if (view instanceof ImageShowView)
            imageMap.remove(((ImageShowView) view).getImageUrl());
    }

    /**
     * 获取第一个Video封面图
     *
     * @return
     */
    public String getFirstCoverImage() {
        String coverImageUrl = "";
        VideoShowView view = getFirstVideoView();
        if (view != null)
            coverImageUrl = view.getCoverImageUrl();
        return coverImageUrl;
    }

    /**
     * 获取第一个视频url
     *
     * @return
     */
    public String getFirstVideoUrl() {
        String videoUrl = "";
        VideoShowView view = getFirstVideoView();
        if (view != null)
            videoUrl = view.getVideoUrl();
        return videoUrl;
    }

    /**
     * 是否有图片
     *
     * @return
     */
    public boolean hasImage() {
        return !imageMap.isEmpty();
    }

    /**
     * 是否有视频
     *
     * @return
     */
    public boolean hasVideo() {
        VideoShowView view = getFirstVideoView();
        return view != null;
    }

    /**
     * 是否有文本内容
     *
     * @return
     */
    public boolean hasText() {
        boolean hasText = false;
        for (int index = 0; index < getChildCount(); index++) {
            View view = getChildAt(index);
            if (view instanceof EditTextView) {
                String text = ((EditTextView) view).getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    hasText = true;
                    return hasText;
                }
            }
        }
        return hasText;
    }

    /**
     * 获取第一个Video组件
     *
     * @return
     */
    private VideoShowView getFirstVideoView() {
        for (int index = 0; index < getChildCount(); index++) {
            View view = getChildAt(index);
            if (view instanceof VideoShowView)
                return (VideoShowView) view;
        }
        return null;
    }

    public int getTextCount(){
        StringBuilder sbuilder = new StringBuilder();
        for (int index = 0; index < getChildCount(); index++) {
            View view = getChildAt(index);
            if (view instanceof EditTextView) {
                String text = ((EditTextView) view).getText().toString();
                sbuilder.append(text);
            }
        }
        return sbuilder.length();
    }

    public int getURLCount(){
        int count = 0;
        for (int index = 0; index < getChildCount(); index++) {
            View view = getChildAt(index);
            if (view instanceof EditTextView)
                count += ((EditTextView) view).getLinkMapArray().size();
        }
        return count;
    }

    /** 设置显示单个视频 */
    public void setSingleVideo(boolean singleVideo) {
        isSingleVideo = singleVideo;
    }

    public ArrayList<String> getImageArray() {
        ArrayList<String> imageUrlArray = new ArrayList<>();
        VideoShowView videoShowView = getFirstVideoView();
        String coverImage = "";
        if (videoShowView != null)
            coverImage = videoShowView.getCoverImageUrl();
        Set<Map.Entry<String, String>> entries = imageMap.entrySet();
        for (Map.Entry entry : entries)
            imageUrlArray.add(entry.getKey().toString());
        imageUrlArray.remove(coverImage);
        return imageUrlArray;
    }

    public int getImageCount(){
        int imageCount = 0;
        for(int index = 0;index< getChildCount();index++){
            View view = getChildAt(index);
            if(view instanceof ImageShowView)
                imageCount++;
        }
        return imageCount;
    }

    public int getVideoCount(){
        int imageCount = 0;
        for(int index = 0;index< getChildCount();index++){
            View view = getChildAt(index);
            if(view instanceof VideoShowView)
                imageCount++;
        }
        return imageCount;
    }

    /**
     * 获取视频数据
     * @return
     */
    public ArrayList<Map<String,String>> getVideoArray(){
        ArrayList<Map<String,String>> dataArray = new ArrayList<>();
        for(int index = 0 ; index < getChildCount() ;index ++){
            View view = getChildAt(index);
            if(view instanceof VideoShowView){
                Map<String,String> map = new HashMap<>();
                map.put("video",((VideoShowView)view).getVideoUrl());
                map.put("image",((VideoShowView)view).getCoverImageUrl());
                dataArray.add(map);
            }
        }
        return dataArray;
    }

    public EditTextView getCurrentEditText() {
        return currentEditText;
    }

    @Override
    public void onClick(View v, String url) {
        Intent intent = new Intent(getContext(), VideoFullScreenActivity.class);
        intent.putExtra(VideoFullScreenActivity.EXTRA_VIDEO_TYPE, VideoFullScreenActivity.LOCAL_VIDEO);
        intent.putExtra(VideoFullScreenActivity.EXTRA_VIDEO_URL, url);
        getContext().startActivity(intent);
        XHClick.mapStat(getContext(), "a_ArticleEdit", "编辑文章内容", "播放视频");
    }

    public int getMaxVideoCount() {
        return maxVideoCount;
    }

    public void setMaxVideoCount(int maxVideoCount) {
        this.maxVideoCount = maxVideoCount;
    }

    public void setMaxTextCount(int maxTextCount) {
        this.maxTextCount = maxTextCount;
    }
}