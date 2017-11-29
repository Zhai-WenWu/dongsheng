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
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;

/**
 * 处理数据的小贴士下的广告
 */
public class DishAdDataViewNew extends ItemBaseView {
    private XHAllAdControl xhAllAdControl;

    private Map<String, String> adDataMap = new HashMap<>();
    RelativeLayout root_layout;

    public DishAdDataViewNew(Context context,int layoutId) {
        super(context, layoutId);
    }

    public DishAdDataViewNew(Context context, AttributeSet attrs,int layoutId) {
        super(context, attrs, layoutId);
    }

    public DishAdDataViewNew(Context context, AttributeSet attrs, int defStyleAttr,int layoutId) {
        super(context, attrs, defStyleAttr, layoutId);
    }

    @Override
    public void init() {
        root_layout = (RelativeLayout) findViewById(R.id.root_layout);
    }

    /**
     * 去请求数据
     *
     * @param activity
     */
    public void getRequest(final Activity activity, final ViewGroup parentView) {
        ArrayList<String> list = new ArrayList<>();
        list.add(AdPlayIdConfig.DISH_TIESHI);
        xhAllAdControl = new XHAllAdControl(list, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(Map<String, String> map) {
                //对数据进行处理
                String data = map.get(AdPlayIdConfig.DISH_TIESHI);
                Log.i("tzy", AdPlayIdConfig.DISH_TIESHI + " : " + data);
                adDataMap = StringManager.getFirstMap(data);
                if (adDataMap.containsKey("type")) {
                    switch (adDataMap.get("type")) {
                        case AdPlayIdConfig.ADTYPE_API:
                            setApiADData(adDataMap, parentView);
                            break;
                        case AdPlayIdConfig.ADTYPE_GDT:
                        case AdPlayIdConfig.ADTYPE_BAIDU:
                        case AdPlayIdConfig.ADTYPE_BANNER:
                            setBigPicADData(adDataMap, parentView);
                            break;
                        default:

                            break;
                    }
                }
            }
        }, activity, "result_tip");
    }

    /**
     * 处理Tencent广告数据
     *
     * @param map        广告数据
     * @param parentView 父容器
     */
    private void setApiADData(Map<String, String> map, ViewGroup parentView) {
        if (map.containsKey("stype")) {
            switch (map.get("stype")) {
                //一张大图
                case "202":
                    setBigPicADData(map, parentView);
                    break;
                //一张小图
                case "101":
                    setSmallPicADData(map, parentView);
                    break;
                //三张大图
                case "301":
                    setThreePicADData(map, parentView);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 设置大图
     *
     * @param map        广告数据
     * @param parentView 父容器
     */
    private void setBigPicADData(final Map<String, String> map, ViewGroup parentView) {
        //设置图片
        final ImageView bigImage = (ImageView) findViewById(R.id.ad_big_pic);
        imgWidth = ToolsDevice.getWindowPx(getContext()).widthPixels - Tools.getDimen(getContext(), R.dimen.dp_20);
        imgHeight = imgWidth / 2;
        new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        setViewImage(bigImage, map.get("imgUrl"));
                    }
                });
        Log.i("zyj","展示广告");
        //设置文字
        TextView title = (TextView) findViewById(R.id.ad_name);
        title.setText(map.get("title"));
        TextView text = (TextView) findViewById(R.id.ad_big_pic_text);
        text.setText(handlerAdText(map));
        //曝光数据
        exposureAdData();
        //广告小标
        setAdHintClick(R.id.ad_big_pic_flag);
        //添加到parent
        addViewToPraent(R.id.ad_big_pic_layout, parentView);
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
     * 设置三小图
     *
     * @param adDataMap  广告数据
     * @param parentView 父容器
     */
    private void setThreePicADData(Map<String, String> adDataMap, ViewGroup parentView) {
        imgWidth = (int) ((ToolsDevice.getWindowPx(getContext()).widthPixels - Tools.getDimen(getContext(), R.dimen.dp_61)) / 3f);
        imgHeight = imgWidth * 2 / 3;
        final List<Map<String, String>> imgsArray = StringManager.getListMapByJson(adDataMap.get("imgs"));
        Log.i("tzy", "imgsArray : " + imgsArray.toString());
        int[] ids = {R.id.ad_three_pic_1, R.id.ad_three_pic_2, R.id.ad_three_pic_3};
        if (imgsArray.size() >= ids.length) {
            for (int i = 0; i < ids.length; i++) {
                final int index = i;
                final ImageView imageView = (ImageView) findViewById(ids[index]);
                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                setViewImage(imageView, imgsArray.get(index).get(""));
                            }
                        });
            }
        }

        //设置文字
        TextView text = (TextView) findViewById(R.id.ad_three_pic_desc);
        text.setText(handlerAdText(adDataMap));

        //广告小标
        setAdHintClick(R.id.ad_three_pic_flag);
        //曝光数据
        exposureAdData();
        //添加到parent
        Log.i("tzy", "id = " + R.id.ad_three_pic_layout);
        addViewToPraent(R.id.ad_three_pic_layout, parentView);
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
    private void setAdHintClick(int id) {
        View view = findViewById(id);
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.setAdHintClick((Activity) DishAdDataViewNew.this.getContext(), v, xhAllAdControl, 0, "");
            }
        });
    }

    /**
     * 设置广告点击事件
     */
    private void exposureAdData() {
        DishAdDataViewNew.this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                xhAllAdControl.onAdClick(DishAdDataViewNew.this, 0, "");
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
            Log.i("tzy", "id :" + count++);
            findViewById(id).setVisibility(id == showId ? View.VISIBLE : View.GONE);
        }
        Log.i("tzy", "addView");
        if(DishAdDataViewNew.this!=null&&context!=null)
            parentView.addView(DishAdDataViewNew.this);
    }

    public void onListScroll() {
        if (adDataMap != null && !"2".equals(adDataMap.get("isShow"))) {
            xhAllAdControl.onAdBind(0, root_layout, "");
            adDataMap.put("isShow", "2");
        }
    }

    public void onDestroy(){
        if(xhAllAdControl!=null){
            xhAllAdControl=null;
        }
    }

}
