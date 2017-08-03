package amodule.upload.bean;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import amodule.answer.model.AskAnswerModel;
import amodule.article.db.UploadArticleData;
import amodule.dish.db.UploadDishData;
import amodule.upload.callback.UploadListNetCallBack;
import amodule.upload.callback.UploadListUICallBack;

/**
 * Created by ：fei_teng on 2016/10/29 22:42.
 */

public class UploadPoolData {


    public static final int POSITION_HEAD = 1;
    public static final int POSITION_BODY = 2;
    public static final int POSITION_TAIL = 3;


    private int draftId;
    private String title;
    private List<UploadItemData> headDataList;
    private List<UploadItemData> bodyDataList;
    private List<UploadItemData> tailDataList;
    private List<UploadItemData> totalDataList;
    private UploadDishData uploadDishData;
    private UploadArticleData uploadArticleData;
    private AskAnswerModel uploadAskAnswerData;
    private UploadListNetCallBack netCallback;
    private UploadListUICallBack uiCallback;

    public UploadPoolData() {
        totalDataList = new ArrayList<UploadItemData>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public void setHeadDataList(List<UploadItemData> dataList) {
        headDataList = dataList;
        totalDataList.addAll(headDataList);
    }


    public void setBodyDataList(List<UploadItemData> dataList) {
        bodyDataList = dataList;
        totalDataList.addAll(bodyDataList);
    }


    public void setTailDataList(List<UploadItemData> dataList) {
        tailDataList = dataList;
        totalDataList.addAll(tailDataList);
    }


    public List<UploadItemData> getTailDataList() {
        return tailDataList;
    }

    public List<UploadItemData> getHeadDataList() {
        return headDataList;
    }

    public List<UploadItemData> getBodyDataList() {
        return bodyDataList;
    }


    public UploadListUICallBack getUiCallback() {
        return uiCallback;
    }

    public void setUiCallback(UploadListUICallBack uiCallback) {
        this.uiCallback = uiCallback;
    }

    public UploadListNetCallBack getNetCallback() {
        return netCallback;
    }

    public void setNetCallback(UploadListNetCallBack netCallback) {
        this.netCallback = netCallback;
    }

    /**
     * 根据唯一标示，找到对应的单项数据
     *
     * @param uniqueId 唯一标示
     * @return speciaItemData
     */
    public UploadItemData getSpeciaItem(String uniqueId) {

        UploadItemData speciaItemData = null;
        if (!TextUtils.isEmpty(uniqueId)) {
            for (UploadItemData itemData : totalDataList) {
                if (uniqueId.equals(itemData.getUniqueId())) {
                    speciaItemData = itemData;
                }
            }
        }
        return speciaItemData;
    }

    public int getDraftId() {
        return draftId;
    }

    public void setDraftId(int draftId) {
        this.draftId = draftId;
    }

    public UploadDishData getUploadDishData() {
        return uploadDishData;
    }

    public void setUploadDishData(UploadDishData uploadDishData) {
        this.uploadDishData = uploadDishData;
    }

    public UploadArticleData getUploadArticleData() {
        return uploadArticleData;
    }

    public void setUploadArticleData(UploadArticleData uploadArticleData) {
        this.uploadArticleData = uploadArticleData;
    }

    public AskAnswerModel getUploadAskAnswerData() {
        return uploadAskAnswerData;
    }

    public void setUploadAskAnswerData(AskAnswerModel model) {
        this.uploadAskAnswerData = model;
    }

    public List<UploadItemData> getTotalDataList() {
        return totalDataList;
    }

    public List<UploadItemData> getUploadItemDataList(int pos) {

        List<UploadItemData> uploadItemDataList = null;
        switch (pos) {
            case POSITION_HEAD:
                uploadItemDataList = headDataList;
                break;
            case POSITION_BODY:
                uploadItemDataList = bodyDataList;
                break;
            case POSITION_TAIL:
                uploadItemDataList = tailDataList;
                break;
        }
        return uploadItemDataList;
    }


    public void loopPoolData(List<UploadItemData> list, LoopCallback callback) {
        if(list == null) return;
        boolean needStop = false;
        for (UploadItemData itemData : list) {
            if(needStop)
                break;
           needStop =  callback.onLoop(itemData);
        }
    }


    public interface LoopCallback {
        boolean onLoop(UploadItemData itemData);
    }

    public ArrayList<Map<String, String>> getListData() {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        for (UploadItemData itemData : totalDataList) {
            list.add(itemData.translateToMap());
        }
        return list;
    }

}
