package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;

import static third.ad.scrollerAd.XHScrollerAdParent.ADKEY_BAIDU;
import static third.ad.scrollerAd.XHScrollerAdParent.ADKEY_GDT;
import static third.ad.scrollerAd.XHScrollerAdParent.ID_AD_ICON_GDT;

/**
 * 处理数据的小贴士下的广告
 */
public class DishGgDataViewNew extends ItemBaseView {
    private XHAllAdControl xhAllAdControl;

    private Map<String, String> adDataMap = new HashMap<>();
    RelativeLayout root_layout;

    public DishGgDataViewNew(Context context, int layoutId) {
        super(context, layoutId);
    }

    public DishGgDataViewNew(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs, layoutId);
    }

    public DishGgDataViewNew(Context context, AttributeSet attrs, int defStyleAttr, int layoutId) {
        super(context, attrs, defStyleAttr, layoutId);
    }

    @Override
    public void init() {
        root_layout = (RelativeLayout) findViewById(R.id.root_layout);
        DishGgDataViewNew.this.setVisibility(View.GONE);
    }

    /**
     * 去请求数据
     *
     * @param activity
     */
    public void getRequest(final Activity activity, final ViewGroup parentView) {
        DishGgDataViewNew.this.setVisibility(View.GONE);
        ArrayList<String> list = new ArrayList<>();
        list.add(AdPlayIdConfig.DISH_TIESHI);
        xhAllAdControl = new XHAllAdControl(list, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(boolean isRefresh, Map<String, String> map) {
                //对数据进行处理
                String data = map.get(AdPlayIdConfig.DISH_TIESHI);
                Log.i("xianghaTag", AdPlayIdConfig.DISH_TIESHI + " : " + data);
                adDataMap = StringManager.getFirstMap(data);
                DishGgDataViewNew.this.setVisibility(View.VISIBLE);
                if (adDataMap.containsKey("type")) {
                    switch (adDataMap.get("type")) {
                        case AdPlayIdConfig.ADTYPE_GDT:
                        case AdPlayIdConfig.ADTYPE_BAIDU:
                        case AdPlayIdConfig.ADTYPE_BANNER:
                            DishGgDataViewNew.this.setVisibility(View.VISIBLE);
                            setBigPicADData(adDataMap, parentView);
                            break;
                        default:
                            DishGgDataViewNew.this.setVisibility(View.GONE);
                            break;
                    }
                } else DishGgDataViewNew.this.setVisibility(View.GONE);
            }
        }, activity, "result_tip");
        xhAllAdControl.registerRefreshCallback();
    }

    /**
     * 设置大图
     *
     * @param map        广告数据
     * @param parentView 父容器
     */
    private void setBigPicADData(final Map<String, String> map, ViewGroup parentView) {
        //设置图片
        new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        ImageView bigImage = (ImageView) findViewById(R.id.ad_big_pic);
                        imgWidth = ToolsDevice.getWindowPx(getContext()).widthPixels - Tools.getDimen(getContext(), R.dimen.dp_40);
                        imgHeight = imgWidth / 2;
                        setViewImage(bigImage, map.get("imgUrl"));
                    }
                });
        Log.i("zyj", "展示广告");
        //设置文字
//        TextView title = (TextView) findViewById(R.id.ad_name);
//        title.setText(map.get("title"));
        TextView text = (TextView) findViewById(R.id.ad_big_pic_text);
        text.setText(handlerAdText(map));
        //曝光数据
        exposureAdData();
        //广告小标
//        setAdHintClick(R.id.ad_big_pic_flag, !"1".equals(map.get("adType")));
        //添加到parent
        addViewToPraent(R.id.ad_big_pic_layout, parentView);
        setIconVisibility(ID_AD_ICON_GDT, map.get("type"));
    }

    private void setIconVisibility(int id, String adType) {
        View view = findViewById(id);
        if (view != null) {
            if (adType == null) {
                view.setVisibility(View.GONE);
                return;
            }
            switch (adType) {
                case ADKEY_BAIDU:
                    if (view instanceof ImageView) {
                        ((ImageView)view).setImageResource(R.drawable.icon_video_ad);
                    }
                    break;
                case ADKEY_GDT:
                    if (view instanceof ImageView) {
                        ((ImageView)view).setImageResource(R.drawable.icon_ad_gdt_rct);
                    }
                    break;
                default:
                    view.setVisibility(View.GONE);
                    return;
            }
            view.setVisibility(ADKEY_GDT.equals(adType) || ADKEY_BAIDU.equals(adType) ? VISIBLE : GONE);
        }
    }

    /**
     * 设置一小图
     *
     * @param adDataMap  广告数据
     * @param parentView 父容器
     */
    private void setSmallPicADData(final Map<String, String> adDataMap, ViewGroup parentView) {
        final ImageView imageView = (ImageView) findViewById(R.id.ad_small_pic);
        new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        setViewImage(imageView, adDataMap.get("imgUrl"));
                    }
                });
        TextView title = (TextView) findViewById(R.id.ad_small_pic_title);
        title.setText(adDataMap.get("title"));
        TextView desc = (TextView) findViewById(R.id.ad_small_pic_desc);
        desc.setText(adDataMap.get("desc"));

        //曝光数据
        exposureAdData();
        //添加到parent
        addViewToPraent(R.id.ad_small_pic_layout, parentView);
    }

    /**
     * 处理关高描述
     *
     * @param map
     *
     * @return
     */
    private String handlerAdText(Map<String, String> map) {
//        StringBuffer buffer = new StringBuffer();
//        String title = map.get("title");
//        String desc = map.get("desc");
//        if (!TextUtils.isEmpty(title)
//                && !TextUtils.isEmpty(desc)
//                && !title.equals(desc)) {
//            buffer.append(title).append(" | ").append(desc);
//        } else if (!TextUtils.isEmpty(title)) {
//            buffer.append(title);
//        } else if (!TextUtils.isEmpty(desc)) {
//            buffer.append(desc);
//        }
//        return buffer.toString();

        return map.get("desc");
    }

    /**
     * 设置广告小标点击事件
     *
     * @param id 广告小标id
     */
    private void setAdHintClick(int id, boolean show) {
        View view = findViewById(id);
        view.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show)
            return;
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.setAdHintClick((Activity) DishGgDataViewNew.this.getContext(), v, xhAllAdControl, 0, "");
            }
        });
    }

    /**
     * 设置广告点击事件
     */
    private void exposureAdData() {
        DishGgDataViewNew.this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                xhAllAdControl.onAdClick(DishGgDataViewNew.this, 0, "");
            }
        });
    }

    /**
     * 添加到parent
     *
     * @param showId
     * @param parentView
     */
    private void addViewToPraent(int showId, ViewGroup parentView) {
        if (showId <= 0) {
            return;
        }
        int[] layoutIds = {R.id.ad_big_pic_layout, R.id.ad_small_pic_layout, R.id.ad_three_pic_layout};
        int count = 0;
        for (int id : layoutIds) {
            Log.i("xianghaTag", "id :" + count++);
            findViewById(id).setVisibility(id == showId ? View.VISIBLE : View.GONE);
        }
        Log.i("xianghaTag", "addView");
        if (DishGgDataViewNew.this != null && context != null)
            parentView.removeAllViews();
        parentView.addView(DishGgDataViewNew.this);
    }

    boolean canAdBind = true;

    public void onListScroll() {
        if (Tools.inScreenAdView(this)) {
            if (xhAllAdControl != null && adDataMap.size() > 0 && canAdBind) {
                xhAllAdControl.onAdBind(0, root_layout, "");
            }
            canAdBind = false;
        } else {
            canAdBind = true;
        }
    }

    public void onDestroy() {
        if (xhAllAdControl != null) {
            xhAllAdControl = null;
        }
    }

}
