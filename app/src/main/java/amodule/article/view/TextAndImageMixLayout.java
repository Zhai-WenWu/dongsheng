package amodule.article.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
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

    private boolean isSecondEdit = false;

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
        setPadding(0, 0, 0, Tools.getDimen(getContext(), R.dimen.dp_80));
        addRichText(-1, "");
    }

    /** 设置上传需要数据 */
    public void setXHServiceData(String content) {
        if (TextUtils.isEmpty(content)) return;
        content = Uri.decode(content);
        if (!"2".equals(type))
            removeAllViews();

        List<Map<String, String>> dataArray = StringManager.getListMapByJson(content);
        Log.i("tzy", "dataArray = " + dataArray.toString());
        for (int index = 0; index < dataArray.size(); index++) {
            Map<String, String> map = dataArray.get(index);
            Log.i("tzy", "map = " + map.toString());
            if (!map.containsKey("type")
                    || TextUtils.isEmpty(map.get("type"))) {
                continue;
            }
            boolean isLast = index == dataArray.size() - 1;
            if (index + 1 < dataArray.size()) {
                Map<String, String> nextMap = dataArray.get(index + 1);
                if (!BaseView.TEXT.equals(nextMap.get("type"))) {
                    isLast = true;
                }
            }
            switch (map.get("type")) {
                case BaseView.TEXT:
                    handlerTextData(map.get("html"));
                    break;
                case BaseView.IMAGE:
                    addImage(map.get("imageurl"),map.get("id"), isLast, "");
                    break;
                case BaseView.IMAGE_GIF:
                    addImage(map.get("gifurl"),map.get("id"), isLast, "");
                    break;
                case BaseView.VIDEO:
                    addVideo(map.get("videosimageurl"), map.get("videourl"),map.get("id"), isLast, "");
                    break;
                case BaseView.URLS:
                    //do nothing
                    handlerUrls(map.get("urls"));
                    break;
                default:
                    break;
            }
        }
        if (getChildCount() == 0)
            addRichText(-1, "");
        else if (isSingleVideo) {
            for (int index = getChildCount() - 1; index > 1; index--) {
                removeViewAt(index);
            }
        }
    }

    /**
     * [{"title":"hjjbjjjjj","url":"http:\/\/www.baidu.com"}]}
     * @param urls
     */
    private void handlerUrls(String urls) {
        List<Map<String,String>> urlsArray = StringManager.getListMapByJson(urls);
        for(int index = 0; index < getChildCount() ; index ++){
            BaseView view = (BaseView) getChildAt(index);
            if(view instanceof EditTextView){
                EditTextView editTextView = ((EditTextView)view);
                String text = editTextView.getText().toString();
                for(Map<String,String> map:urlsArray){
                    if(text.contains(map.get("title"))){
                        editTextView.addLinkToData(map.get("url"),map.get("title"));
                    }
                }
            }
        }
    }

    /**
     * 处理 text 数据
     *
     * @param html
     */
    private void handlerTextData(String html) {
        EditTextView editTextView = null;
        if (getChildCount() - 1 >= 0) {
            View view = getChildAt(getChildCount() - 1);
            if (view instanceof EditTextView) {
                editTextView = (EditTextView) view;
            }
        }
        if (editTextView == null)
            editTextView = addRichText(-1, "");
        boolean isCenter = false;
        StringBuilder htmlTmep = new StringBuilder(html);
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

        if (htmlTmep.indexOf(">") + 1 >= 0)
            html = htmlTmep.substring(htmlTmep.indexOf(">") + 1, htmlTmep.length());
        Log.i("tzy","html = " + html);
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
                Log.i("tzy", "htmlTmep = " + htmlTmep);
                Log.i("tzy", "title = " + title);
                Log.i("tzy", "url = " + url);
                if (!TextUtils.isEmpty(title)
                        && !TextUtils.isEmpty(url))
                    editTextView.addLinkToData(url, title);
                htmlTmep = htmlTmep.replace(htmlTmep.indexOf(aTagData), htmlTmep.indexOf(aTagData) + aTagData.length(), "");
            }
        }
        //删除<p></p>
        html = new String(html.replace(propertyStr, "").replace("<br></p>", ""));
        if("<br>".equals(html)){
            html = "";
        }
        Log.i("tzy","html = " + html);
        editTextView.setCenterHorizontal(isCenter);
        editTextView.setTextFrormHtml(html);
        editTextView.setSelection(editTextView.getRichText().getText().length());
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
            if (jsonObject != null)
                jsonArray.put(jsonObject);
            Log.i("tzy", jsonArray.toString());
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
                if (onFocusChangeCallback != null) {
                    onFocusChangeCallback.onFocusChange(v, hasFocus);
                }
                if (hasFocus) {
                    currentEditText = v;
                    if (indexOfChild(currentEditText) == getChildCount() - 1) {
                        if (onScorllEndCallback != null)
                            onScorllEndCallback.onScorllEnd();
                    }
                }
            }
        });
        currentEditText.setOnAfterTextChanged(new EditTextView.OnAfterTextChanged() {
            @Override
            public void afterTextChanged(Editable s) {
                int value = getTextCount() - maxTextCount;
                if (value > 0) {
                    currentEditText.setText(s.subSequence(0, s.length() - value));
                    currentEditText.setSelection(currentEditText.getText().length());
                    Tools.showToast(getContext(), "内容最多" + maxTextCount + "字");
                    ToolsDevice.keyboardControl(false, getContext(), currentEditText);
                }
            }
        });
        currentEditText.setOnSelectBoldCallback(onSelectBoldCallback);
        currentEditText.setOnSelectUnderlineCallback(onSelectUnderlineCallback);
        currentEditText.setOnSelectCenterCallback(onSelectCenterCallback);
        if (!TextUtils.isEmpty(content)) {
            view.setText(content);
//            view.setSelection(view.getRichText().getText().length());
        }
        view.getRichText().setHint(indexOfChild(view) == 0 ? "添加内容" : "");
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
    private void uploadImage(final String imagePath) {
        //为空或者为服务器链接则忽略
        if (TextUtils.isEmpty(imagePath)
                || imagePath.startsWith("http://"))
            return;
        //上传图片
        new BreakPointControl(getContext(), "", imagePath, TYPE_IMG).start(new UploadListNetCallBack() {
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
     * @param imageUrl
     * @param ifAddText
     * @param content
     */
    public void addImage(String imageUrl, boolean ifAddText, CharSequence content) {
        addImage(imageUrl, "",ifAddText, content);
    }

    /**
     * 添加图片
     * @param imageUrl
     * @param idStr
     * @param ifAddText
     * @param content
     */
    public void addImage(String imageUrl,String idStr, boolean ifAddText, CharSequence content) {
        if (TextUtils.isEmpty(imageUrl))
            return;
        final int insertIndex = getFoucsIndex() + 1;

        ImageShowView view = new ImageShowView(getContext());
        view.setEnableEdit(true);
        view.setSelected(isSecondEdit);
        view.setImageUrl(imageUrl);
        view.setIdStr(idStr);
        view.setmOnRemoveCallback(this);

        if (insertIndex >= getChildCount()) {
            if (getChildCount() == 0) {
                addRichText(-1, "");
            } else {
                View preView = getChildAt(getChildCount() - 1);
                if (!(preView instanceof EditTextView)) {
                    addRichText(-1, "");
                }
            }
            addView(view, getChildLayoutParams());
        } else
            addView(view, insertIndex, getChildLayoutParams());

        imageMap.put(imageUrl, "");
//        uploadImage(imageUrl);
        //如果网络图片则不上传，直接替换map数据
        if (imageUrl.startsWith("http")) {
            imageMap.put(imageUrl, imageUrl);
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
                Log.i("tzy", "getImageMapArray() path:" + path + "    url:" + imageMap.get(path));
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
     * @param coverImageUrl
     * @param videoUrl
     * @param ifAddText
     * @param content
     */
    public void addVideo(String coverImageUrl, String videoUrl, boolean ifAddText, CharSequence content) {
        addVideo(coverImageUrl, videoUrl,"", ifAddText, content);;
    }

    /**
     * 添加视频
     * @param coverImageUrl
     * @param videoUrl
     * @param idStr
     * @param ifAddText
     * @param content
     */
    public void addVideo(String coverImageUrl, String videoUrl, String idStr, boolean ifAddText, CharSequence content) {
        if(TextUtils.isEmpty(coverImageUrl) || TextUtils.isEmpty(videoUrl)){
            Tools.showToast(getContext(),"文件已损坏");
            return;
        }
        int videoCount = 0;
        for (int index = 0; index < getChildCount(); index++) {
            View baseView = getChildAt(index);
            if (baseView instanceof VideoShowView)
                videoCount++;
        }
        if (videoCount >= maxVideoCount && !isSingleVideo) {
            Tools.showToast(getContext(), "最多可选择" + maxVideoCount + "视频");
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
            if (insertIndex >= getChildCount()) {
                if (getChildCount() == 0) {
                    addRichText(-1, "");
                } else {
                    View preView = getChildAt(getChildCount() - 1);
                    if (!(preView instanceof EditTextView)) {
                        addRichText(-1, "");
                    }
                }
                addView(view, getChildLayoutParams());
            } else
                addView(view, insertIndex, getChildLayoutParams());

            //默认插入edit
            if (ifAddText)
                addRichText(insertIndex + 1, content);
        }
        view.setEnableEdit(true);
        view.setSecondEdit(isSecondEdit);
        view.setVideoData(coverImageUrl, videoUrl);
        view.setIdStr(idStr);
        view.setmOnRemoveCallback(this);
        view.setmOnClickImageListener(this);
    }

    public void setVideo(VideoShowView.VideoDefaultClickCallback callback) {
        VideoShowView view = new VideoShowView(getContext());
        LinearLayout.LayoutParams layoutparams = getChildLayoutParams();
        int dp_5 = Tools.getDimen(getContext(), R.dimen.dp_5);
        int dp_15 = Tools.getDimen(getContext(), R.dimen.dp_15);
//        view.setPadding(dp_20,dp_20,dp_20,0);
        layoutparams.setMargins(0, dp_5, 0, 0);
        addView(view, 0, layoutparams);
        view.setEnableEdit(true);
        view.setmOnRemoveCallback(this);
        view.setmOnClickImageListener(this);
        view.setWrapContent(false);
        if (callback != null)
            view.setVideoDefaultClickCallback(callback);
        currentEditText.getRichText().setHint("添加视频介绍");
        layoutparams = (LayoutParams) currentEditText.getLayoutParams();
        layoutparams.setMargins(0, dp_15, 0, 0);
        currentEditText.setLayoutParams(layoutparams);
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
    private LinearLayout.LayoutParams getChildLayoutParams() {
        return new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onRemove(final BaseView view) {
//        if (isSecondEdit) {
            removeBaseView(view);
//        } else {
//            final XhDialog dialog = new XhDialog(getContext());
//            dialog.setTitle("确定删除？");
//            dialog.setCanselButton("取消", new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dialog.cancel();
//                }
//            });
//            dialog.setSureButton("确定", new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    removeBaseView(view);
//                    dialog.cancel();
//                }
//            });
//            dialog.show();
//        }
    }

    /**
     * 执行remove操作
     *
     * @param view
     */
    private void removeBaseView(BaseView view) {
        if ("2".equals(type)
                && (view instanceof VideoShowView)) {
            ((VideoShowView) view).resetData();
            return;
        }
        final int index = indexOfChild(view);
        String text = "";
        if (index + 1 < getChildCount()) {
            View removeView = getChildAt(index + 1);
            if (removeView instanceof EditTextView) {
                text = ((EditTextView) removeView).getTextHtml();
                removeViewAt(index + 1);
            }
        }
        //同时维护图片集合
        removeImagePath(view);
        removeView(view);

        for (int i = index; i >= 0; i--) {
            View lastView = getChildAt(i);
            if (lastView instanceof EditTextView) {
                EditTextView editTextView = ((EditTextView) lastView);
                if (!TextUtils.isEmpty(text)) {
                    editTextView.appendText((Editable) RichParser.fromHtml(text));
                }
                currentEditText = editTextView;
                currentEditText.requestFocus();
                currentEditText.setSelection(currentEditText.getRichText().getText().length());
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
        boolean hasVideo = view != null
                && !TextUtils.isEmpty(view.getCoverImageUrl())
                && !TextUtils.isEmpty(view.getVideoUrl());
        return hasVideo;
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
                String text = ((EditTextView) view).getText().toString().trim();
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

    public int getTextCount() {
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

    public int getURLCount() {
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

    public int getImageCount() {
        int imageCount = 0;
        for (int index = 0; index < getChildCount(); index++) {
            View view = getChildAt(index);
            if (view instanceof ImageShowView)
                imageCount++;
        }
        return imageCount;
    }

    public int getVideoCount() {
        int imageCount = 0;
        for (int index = 0; index < getChildCount(); index++) {
            View view = getChildAt(index);
            if (view instanceof VideoShowView)
                imageCount++;
        }
        return imageCount;
    }

    /**
     * 获取视频数据
     *
     * @return
     */
    public ArrayList<Map<String, String>> getVideoArrayMap() {
        ArrayList<Map<String, String>> dataArray = new ArrayList<>();
        for (int index = 0; index < getChildCount(); index++) {
            View view = getChildAt(index);
            if (view instanceof VideoShowView) {
                Map<String, String> map = new HashMap<>();
                map.put("video", ((VideoShowView) view).getVideoUrl());
                map.put("image", ((VideoShowView) view).getCoverImageUrl());
                dataArray.add(map);
            }
        }
        return dataArray;
    }

    public ArrayList<String> getVideoArrayList() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (int index = 0; index < getChildCount(); index++) {
            View view = getChildAt(index);
            if (view instanceof VideoShowView)
                arrayList.add(((VideoShowView) view).getVideoUrl());
        }
        return arrayList;
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

    public boolean isSecondEdit() {
        return isSecondEdit;
    }

    public void setSecondEdit(boolean secondEdit) {
        isSecondEdit = secondEdit;
    }

    private String type;

    public void setType(String type) {
        this.type = type;
    }

    private OnScorllEndCallback onScorllEndCallback;

    public interface OnScorllEndCallback {
        public void onScorllEnd();
    }

    private EditTextView.OnFocusChangeCallback onFocusChangeCallback;

    public void setOnFocusChangeCallback(EditTextView.OnFocusChangeCallback onFocusChangeCallback) {
        this.onFocusChangeCallback = onFocusChangeCallback;
    }

    public void setOnScorllEndCallback(OnScorllEndCallback onScorllEndCallback) {
        this.onScorllEndCallback = onScorllEndCallback;
    }

    private EditTextView.OnSelectBoldCallback onSelectBoldCallback;
    private EditTextView.OnSelectUnderlineCallback onSelectUnderlineCallback;
    private EditTextView.OnSelectCenterCallback onSelectCenterCallback;

    public void setOnSelectBoldCallback(EditTextView.OnSelectBoldCallback onSelectBoldCallback) {
        this.onSelectBoldCallback = onSelectBoldCallback;
    }

    public void setOnSelectUnderlineCallback(EditTextView.OnSelectUnderlineCallback onSelectUnderlineCallback) {
        this.onSelectUnderlineCallback = onSelectUnderlineCallback;
    }

    public void setOnSelectCenterCallback(EditTextView.OnSelectCenterCallback onSelectCenterCallback) {
        this.onSelectCenterCallback = onSelectCenterCallback;
    }
}