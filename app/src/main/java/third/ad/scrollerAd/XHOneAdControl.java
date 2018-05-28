package third.ad.scrollerAd;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Map;

import third.ad.tools.XHSelfAdTools;

/**
 * 广告单条管理类
 */
public class XHOneAdControl {
    private ArrayList<XHScrollerAdParent> listAdParent;//当前全部的广告实体类
    private String backId;//当前广告位id
    private int index_controls;//当前的广告存在的位置---针对于全部广告位集合
    private int index_ad = -1;//当前广告类的进行的位置\
    private XHAllAdControl.XHAdControlCallBack xhAdControlCallBack;
    private boolean isDisplayed = false;

    XHOneAdControl(ArrayList<XHScrollerAdParent> listAdParent, String backId, int Num) {
        resetData(listAdParent, backId, Num);
    }

    public void resetData(ArrayList<XHScrollerAdParent> listAdParent, String backId, int Num) {
        this.listAdParent = listAdParent;
        this.backId = backId;
        this.index_controls = Num;
    }

    public void resetDispaly(){
        isDisplayed = false;
    }

    public void displayed(){
        isDisplayed = true;
    }

    /**
     * 设置广告数据回调
     *
     * @param adDataCallBack
     */
    public void setAdDataCallBack(XHAllAdControl.XHAdControlCallBack adDataCallBack) {
        this.xhAdControlCallBack = adDataCallBack;
        getCurrentAd(0);
    }

    /**
     * 获取当前广告
     */
    public void getCurrentAd(final int index) {
        if (index < listAdParent.size()) {
            XHScrollerAdParent adParentTemp = listAdParent.get(index);
            //广点通单独进行处理
            if (adParentTemp instanceof XHScrollerGdt) {
                ((XHScrollerGdt) adParentTemp).setGdtData(xhAdControlCallBack.onGdtNativeData());
            }
            if (adParentTemp instanceof XHScrollerBaidu) {
                ((XHScrollerBaidu) adParentTemp).setNativeResponse(xhAdControlCallBack.onBaiduNativeData());
            }
            if(adParentTemp instanceof XHScrollerSelf){
                ((XHScrollerSelf) adParentTemp).setNativeData(xhAdControlCallBack.onXHNativeData(index_controls));
            }
            adParentTemp.setIndexControl(index_controls);
            adParentTemp.getAdDataWithBackAdId(new XHScrollerAdParent.XHAdDataCallBack() {
                @Override
                public void onSuccees(String type, Map<String, String> map) {
                    index_ad = index;
                    listAdParent.get(index_ad).setExecuteStatisticCallback(mCallback);
                    xhAdControlCallBack.onSuccess(type, map, index_controls);
                }

                @Override
                public void onFail(String type) {
                    if (index >= listAdParent.size() - 1) {
                        xhAdControlCallBack.onFail(type, index_controls);
                    } else {
                        getCurrentAd(index + 1);
                    }
                }
            });
        }
    }

    /**
     * 广告的点击事件
     */
    public void onAdClick(String oneLevel, String twoLevel) {
        Log.i("tzy", "onAdClick::::" + index_ad);
        if (index_ad > -1 && index_ad < listAdParent.size())
            listAdParent.get(index_ad).onThirdClick(oneLevel, twoLevel);
    }

    /**
     * 广告曝光
     */
    public void onAdBind(View view, String oneLevel, String twoLevel) {
        Log.i("tzy", "onAdBind::::" + index_ad + "::::" + (view == null ? "view为null" : "正常"));
        if (index_ad > -1 && index_ad < listAdParent.size()) {
            XHScrollerAdParent scrollerAdParent = listAdParent.get(index_ad);
            if (view != null)
                scrollerAdParent.setShowView(view);
            if (scrollerAdParent instanceof XHScrollerSelf || !isDisplayed) {
                scrollerAdParent.onResumeAd(oneLevel, twoLevel);
            }
        }
    }

    /**
     * 获取当前view的状态
     *
     * @return 当前view的状态
     */
    boolean getAdViewState() {
        if (index_ad > -1 && index_ad < listAdParent.size()){
            return listAdParent.get(index_ad).getViewState();
        }
        return true;
    }

    /**
     * 设置view状态
     *
     * @param view 广告view
     */
    public void setView(View view) {
        if (index_ad > -1 && index_ad < listAdParent.size() && view != null) {
            listAdParent.get(index_ad).setShowView(view);
        }
    }

    private XHScrollerAdParent.ExecuteStatisticCallback mCallback = new XHScrollerAdParent.ExecuteStatisticCallback() {
        @Override
        public void execute() {
            displayed();
        }
    };

}
