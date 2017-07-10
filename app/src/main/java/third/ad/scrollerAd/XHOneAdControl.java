package third.ad.scrollerAd;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Map;

/**
 * 广告单条管理类
 */
public class XHOneAdControl {
    private ArrayList<XHScrollerAdParent> listAdParent;//当前全部的广告实体类
    private String backId;//当前广告位置id
    private int index_controls;//当前的广告存在的位置---针对于全部广告位集合
    private int index_ad=-1;//当前广告类的进行的位置\
    private XHAllAdControl.XHAdControlCallBack xhAdControlCallBack;
    public XHOneAdControl(ArrayList<XHScrollerAdParent> listAdParent,String backId, int Num){
        this.listAdParent= listAdParent;
        this.backId= backId;
        this.index_controls= Num;
    }

    /**
     * 设置广告数据回调
     * @param adDataCallBack
     */
    public void setAdDataCallBack(XHAllAdControl.XHAdControlCallBack adDataCallBack){
        this.xhAdControlCallBack=adDataCallBack;
        getCurrentAd(0);
    }

    /**
     * 获取当前广告
     */
    public void getCurrentAd(final int index){
        if(index <listAdParent.size()){
            //广点通单独进行处理
            if(listAdParent.get(index) instanceof XHScrollerGdt){
                ((XHScrollerGdt)listAdParent.get(index)).setGdtData(xhAdControlCallBack.onGdtNativeData());
            }
            if(listAdParent.get(index) instanceof XHScrollerBaidu){
                ((XHScrollerBaidu)listAdParent.get(index)).setNativeResponse(xhAdControlCallBack.onBaiduNativeData());
            }
            listAdParent.get(index).setIndexControl(index_controls);
            listAdParent.get(index).getAdDataWithBackAdId(new XHScrollerAdParent.XHAdDataCallBack() {
                @Override
                public void onSuccees(String type, Map<String, String> map) {
//                    Log.i("tzy", "GDT NactiveAD XHAdDataCallBack onSuccees");
                    index_ad=index;
                    xhAdControlCallBack.onSuccess(type,map,index_controls);
                }

                @Override
                public void onFail(String type) {
//                    Log.i("tzy", "GDT NactiveAD XHAdDataCallBack onFail");
                    if(index==listAdParent.size()-1){
                        xhAdControlCallBack.onFail(type,index_controls);
                    }else{
                        getCurrentAd(index+1);
                    }

                }
            });
        }
    }

    /**
     * 广告的点击事件
     */
    public void onAdClick(String oneLevel,String twoLevel){
        Log.i("tzy","onAdClick::::"+index_ad);
        if (index_ad>-1)listAdParent.get(index_ad).onThirdClick(oneLevel,twoLevel);
    }

    /**
     * 广告曝光
     */
    public void onAdBind(View view,String oneLevel,String twoLevel){
        Log.i("tzy","onAdBind::::"+index_ad+"::::"+view==null?"view为null":"正常");
        if (index_ad>-1){
            if(view!=null)listAdParent.get(index_ad).setShowView(view);
            listAdParent.get(index_ad).onResumeAd(oneLevel,twoLevel);
        }
    }

    /**
     * 获取当前view的状态
     * @return
     */
    public boolean getAdViewState(){
        if(index_ad>-1)
            return listAdParent.get(index_ad).getViewState();
        return true;
    }

    /**
     * 设置view状态
     * @param view
     */
    public void setView(View view){
        if (index_ad>-1) {
            if (view != null) listAdParent.get(index_ad).setShowView(view);
        }
    }

    //退出activity时，释放view
    public void releaseView(){
        if(listAdParent!=null&&listAdParent.size()>0){
            for(int i=0;i<listAdParent.size();i++){
                if(listAdParent.get(i)!=null)
                    listAdParent.get(i).realseView();
            }
        }
    }
}
