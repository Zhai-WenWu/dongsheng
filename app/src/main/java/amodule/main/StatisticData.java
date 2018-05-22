package amodule.main;

import android.text.TextUtils;

public class StatisticData {
    private String mId;
    private String mContentTwo;
    private String mContentThree;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getContentTwo() {
        return mContentTwo;
    }

    public void setContentTwo(String contentTwo) {
        mContentTwo = contentTwo;
    }

    public String getContentThree() {
        return mContentThree;
    }

    public void setContentThree(String contentThree) {
        mContentThree = contentThree;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(mId) && TextUtils.isEmpty(mContentTwo) && TextUtils.isEmpty(mContentThree);
    }
}
