package amodule.answer.model;

import org.json.JSONArray;

import java.util.List;
import java.util.Map;

import acore.tools.Tools;

/**
 * Created by sll on 2017/7/19.
 */

public class AskAnswerModel {
    public static final String TYPE_ASK = "1";
    public static final String TYPE_ANSWER = "2";
    public static final String TYPE_ASK_AGAIN = "3";
    public static final String TYPE_ANSWER_AGAIN = "4";

    private String mSaveTime;
    private String mAuthorCode;
    private String mDishCode;
    private String mQACode;
    private String mAnswerCode;
    private String mType;
    private String mText;
    private String mPrice;

    private String mImgs;
    private String mVideos;
    private String mTitle;
    private String mAnonymity;//是否匿名 "1":否  "2":是
    private long mId;

    public String getmSaveTime() {
        return mSaveTime;
    }

    public void setmSaveTime(String mSaveTime) {
        this.mSaveTime = mSaveTime;
    }

    public String getmPrice() {
        return mPrice;
    }

    public void setmPrice(String mPrice) {
        this.mPrice = mPrice;
    }

    public String getmAuthorCode() {
        return mAuthorCode;
    }

    public void setmAuthorCode(String mAuthorCode) {
        this.mAuthorCode = mAuthorCode;
    }

    public String getmAnonymity() {
        return mAnonymity;
    }

    public void setmAnonymity(String mAnonymity) {
        this.mAnonymity = mAnonymity;
    }

    public long getmId() {
        return mId;
    }

    public void setmId(long mId) {
        this.mId = mId;
    }

    public String getmDishCode() {
        return mDishCode;
    }

    public void setmDishCode(String mDishCode) {
        this.mDishCode = mDishCode;
    }

    public String getmQACode() {
        return mQACode;
    }

    public void setmQACode(String mQACode) {
        this.mQACode = mQACode;
    }

    public String getmAnswerCode() {
        return mAnswerCode;
    }

    public void setmAnswerCode(String mAnswerCode) {
        this.mAnswerCode = mAnswerCode;
    }

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }

    public String getmText() {
        return mText;
    }

    public void setmText(String mText) {
        this.mText = mText;
    }



    public String getmImgs() {
        return mImgs;
    }

    public void setmImgs(String mImgs) {
        this.mImgs = mImgs;
    }

    public String getmVideos() {
        return mVideos;
    }

    public void setmVideos(String mVideos) {
        this.mVideos = mVideos;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setmImgs(List<Map<String,String>> imgMaps) {
        JSONArray jsonArray = Tools.list2JsonArray(imgMaps);
        mImgs = jsonArray.toString();
    }

    public void setmVideos(List<Map<String,String>> videoMaps){
        JSONArray jsonArray = Tools.list2JsonArray(videoMaps);
        mVideos = jsonArray.toString();
    }

    public boolean isValid() {
        boolean ret = false;
        try {
            long saveTime = Long.parseLong(mSaveTime);
            long off = System.currentTimeMillis() - saveTime;
            ret = off <= 24 * 60 * 60 * 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
