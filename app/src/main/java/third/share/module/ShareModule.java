package third.share.module;

/**
 * Created by sll on 2018/1/24.
 */

public class ShareModule {

    private int mResId;
    private String mTitle;
    private boolean mIntegralTipShow;

    public ShareModule() {
    }

    public int getResId() {
        return mResId;
    }

    public void setResId(int resId) {
        mResId = resId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public boolean isIntegralTipShow() {
        return mIntegralTipShow;
    }

    public void setIntegralTipShow(boolean integralTipShow) {
        mIntegralTipShow = integralTipShow;
    }
}
