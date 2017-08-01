package amodule.answer.model;

import java.io.Serializable;

/**
 * Created by sll on 2017/7/28.
 */

public class QAMsgModel implements Serializable {

    private String mTitle;
    private String mMsgNum;
    private boolean mIsSelect;

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
