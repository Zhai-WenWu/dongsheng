package third.ad;

/**
 * inMobi处理业务层
 */
public class InMobiAd extends AdParent{
    private int mAdWhat=1;//广告类型区分

    @Override
    public boolean isShowAd(String adPlayId, AdIsShowListener listener) {
        boolean isShow= super.isShowAd(adPlayId, listener);
        if(isShow){
            if(mAdWhat==1){
                listener.onIsShowAdCallback(this,isShow);
            }else{
                listener.onIsShowAdCallback(this,isShow);
            }
        }else{
            listener.onIsShowAdCallback(this,isShow);
        }
        return isShow;
    }


    @Override
    public void onResumeAd() {

    }

    @Override
    public void onPsuseAd() {

    }

    @Override
    public void onDestroyAd() {

    }
}
