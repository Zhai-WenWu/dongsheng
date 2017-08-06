package amodule.answer.model;

import java.io.Serializable;

/**
 * Created by sll on 2017/7/28.
 */

public class QAMsgModel implements Serializable {

    private String mType;
    private String mTitle;
    private String mMsgNum;
    private boolean mIsSelect;
    private int mPosition;

    public int getmPosition() {
        return mPosition;
    }

    public void setmPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmMsgNum() {
        return mMsgNum;
    }

    public void setmMsgNum(String mMsgNum) {
        this.mMsgNum = mMsgNum;
    }

    public boolean ismIsSelect() {
        return mIsSelect;
    }

    public void setmIsSelect(boolean mIsSelect) {
        this.mIsSelect = mIsSelect;
    }
}
