package amodule.dish.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.view.ImageMoreAdView;
import amodule.dish.view.ImageMoreCommenView;
import amodule.dish.view.ImageMoreView;
import amodule.dish.view.MoreImageViewPager;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;

/**
 * 图片预览页面
 * Created by XiangHa on 2016/8/10.
 */
public class MoreImageShow extends BaseActivity{
    public static final String[] AD_IDS = new String[]{AdPlayIdConfig.DETAIL_DISH_MAKE};
    private MoreImageViewPager viewPager;
    private ArrayList<ImageMoreView> classContainter = new ArrayList<>();
    private List<Map<String,String>> mList = new ArrayList<>();
    private MyAdapter myAdapter;
    private int pageNum = 0;
    private XHAllAdControl xhAllAdControl;

    private String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // 声明使用自定义标题
        super.onCreate(savedInstanceState);
        initActivity("",3,0,0,R.layout.a_moreimge_show);
        Tools.setStatusBarColor(this, Color.parseColor("#000000"));
        Bundle bundleObject = getIntent().getExtras();
        mList = (List<Map<String,String>>)bundleObject.getSerializable("data");
        pageNum = bundleObject.getInt("index");
        from = bundleObject.getString("from");
        if(mList == null || mList.size() == 0){
            finish();
        }
        int size = mList.size();
        for(int i=0;i<size;i++){
            Map<String,String> map= mList.get(i);
            map.put("num",String.valueOf(i+1));
            map.put("numHe","/" + size);
        }
        init();
    }

    private void init() {
        viewPager = (MoreImageViewPager) findViewById(R.id.viewpager);
        ImageMoreView.IS_SHOW = true;
        ImageMoreView imageMoreView;
        for(int i = 0; i < mList.size(); i ++){
            imageMoreView = new ImageMoreCommenView(this,mList.get(i));
            classContainter.add(imageMoreView);
        }
        myAdapter = new MyAdapter();
        viewPager.setAdapter(myAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position != pageNum){
                    classContainter.get(pageNum).switchNextPage();
                }
                classContainter.get(position).onPageChange();
                classContainter.get(position).onShow();
                pageNum = position;
                if("dish".equals(from))
                    XHClick.mapStat(MoreImageShow.this, DetailDish.tongjiId_detail, "菜谱区域的点击", "步骤图大图-滑动");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setCurrentItem(pageNum);

        //外部控制是否显示广告，默认显示
        if(getIntent().getBooleanExtra("isShowAd",true))
            getAdData();
    }

    private ImageMoreAdView mImageMoreAdView;

    private void getAdData(){

        ArrayList<String> adPosList = new ArrayList<>();
        for (String posStr : AD_IDS) {
            adPosList.add(posStr);
        }
        xhAllAdControl = new XHAllAdControl(adPosList, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(boolean isRefresh,Map<String, String> map) {
                if (map != null && map.size() > 0) {
                    for (String adKey : AD_IDS) {
                        String adStr = map.get(adKey);
                        Log.i("zyj","adStr::"+adStr);
                        if (!TextUtils.isEmpty(adStr)) {
                            ArrayList<Map<String, String>> adList = StringManager.getListMapByJson(adStr);
                            if (adList != null && adList.size() > 0) {
                                if(mImageMoreAdView == null){
                                    mImageMoreAdView = new ImageMoreAdView(xhAllAdControl,MoreImageShow.this,adList,AdPlayIdConfig.DETAIL_DISH_MAKE,"ad");
                                    classContainter.add(mImageMoreAdView);
                                }else{
                                    classContainter.remove(mImageMoreAdView);
                                    mImageMoreAdView = new ImageMoreAdView(xhAllAdControl,MoreImageShow.this,adList,AdPlayIdConfig.DETAIL_DISH_MAKE,"ad");
                                    classContainter.add(mImageMoreAdView);
                                }
                                myAdapter.notifyDataSetChanged();
                            }
                        }else if(mImageMoreAdView != null){
                            viewPager.setCurrentItem(classContainter.size() - 2);
                            classContainter.remove(mImageMoreAdView);
                            myAdapter.notifyDataSetChanged();
                            mImageMoreAdView = null;
                        }
                    }
                }
            }
        },MoreImageShow.this, "result_step");
        xhAllAdControl.registerRefreshCallback();
    }



    public class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return classContainter.size();
        }
        //滑动切换的时候销毁当前的组件
        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            ((ViewPager) container).removeView(classContainter.get(position).getImageMoreView());
        }
        //每次滑动的时候生成的组件
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ((ViewPager) container).addView(classContainter.get(position).getImageMoreView());
            classContainter.get(position).setOnClick();
            classContainter.get(position).onPageChange();
            return classContainter.get(position).getImageMoreView();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }


    }

}
