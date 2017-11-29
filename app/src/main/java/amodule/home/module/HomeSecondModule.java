package amodule.home.module;

import java.io.Serializable;

/**
 * Created by sll on 2017/11/15.
 */

public class HomeSecondModule implements Serializable {
    private String mTitle;
    private String mType;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }
}
