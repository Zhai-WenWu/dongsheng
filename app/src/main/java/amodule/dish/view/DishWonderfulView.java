package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.override.view.ItemBaseView;
import acore.logic.AppCommon;
import amodule.dish.tools.ADDishContorl;
import amodule.quan.view.NormalContentView;
import amodule.quan.view.NormalContentView_New;
import third.ad.tools.AdConfigTools;

import static amodule.dish.activity.DetailDish.tongjiId;

/**
 * 精彩推荐
 */

public class DishWonderfulView extends ItemBaseView {

    public static String DISH_STYLE_WONDERFUL = "DISH_STYLE_WONDERFUL";
    private Map<String, String> map;
    private Activity activity;
    private ADDishContorl adDishContorls;
    private NormalContentView_New normalContentView;

    public DishWonderfulView(Context context) {
        super(context, R.layout.view_dish_wonderful);
    }

    public DishWonderfulView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_wonderful);
    }

    public DishWonderfulView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_wonderful);
    }

    @Override
    public void init() {
        super.init();

    }

    public void setData(final Map<String, String> maps, final Activity activitys, ADDishContorl adDishContorl) {
        this.map = maps;
        this.activity = activitys;
        this.adDishContorls=adDishContorl;
        findViewById(R.id.wonferful_linear_flag).setVisibility(View.GONE);
        if (map.containsKey("position") && map.get("position").equals("0")) {
            findViewById(R.id.wonferful_linear_flag).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.wonferful_linear_flag).setVisibility(View.GONE);
        }
        //头
        RelativeLayout wonferful_rela_content = (RelativeLayout) findViewById(R.id.wonferful_rela_content);
        normalContentView = new NormalContentView_New(activity);
        normalContentView.setStatisIDNew(tongjiId,"底部推荐数据");
        //设置点击统计回调
        normalContentView.setmOnItemClickStatictis(new NormalContentView_New.OnItemClickStatictis() {
            @Override
            public void onStatictis(String index) {
                if (map.containsKey("showMid") && map.containsKey("showCid")) {
                    AdConfigTools.getInstance().postTongjiQuan(context, map, index,"click");
                }
            }
        });
        //设置广告回调
        normalContentView.setOnAdCallback(new NormalContentView_New.OnAdCallback() {
            @Override
            public void onAdShow(View view) {
                if (map.containsKey("isPromotion")) {
                    if (!"2".equals(map.get("isShow"))) {
                        int index = Integer.parseInt(map.get("promotionIndex"));
                        if(adDishContorls.xhAllAdControl!=null)
                        adDishContorls.xhAllAdControl.onAdBind(index,normalContentView,map.get("indexAd"));
                        map.put("isShow", "2");
                    }

                }
            }

            @Override
            public void onAdClick(View view) {
                int index = Integer.parseInt(map.get("promotionIndex"));
                if(adDishContorls.xhAllAdControl!=null)
                    adDishContorls.xhAllAdControl.onAdClick(index,map.get("indexAd"));
            }
        });
        normalContentView.setAdHintCallback(new NormalContentView.OnAdHintListener() {
            @Override
            public void onAdHintListener(View view, String eventID) {
                int index = Integer.parseInt(map.get("promotionIndex"));
                if(adDishContorls.xhAllAdControl!=null)
                    AppCommon.onAdHintClick(activity,adDishContorls.xhAllAdControl,index,map.get("indexAd"));
            }
        });
        normalContentView.initView(map, "",0);
        findViewById(R.id.wonferful_rela_header).setVisibility(View.GONE);
        wonferful_rela_content.addView(normalContentView);
    }

    /**
     * 处理文字是否显示
     *
     * @param v
     * @param text
     */
    public void setViewText(TextView v, String text) {
        if (text == null || text.length() == 0 || text.equals("hide") || " ".equals(text))
            v.setVisibility(View.GONE);
        else {
            v.setVisibility(View.VISIBLE);
            v.setText(text);
        }
    }

    public void onListScroll() {
        normalContentView.setShowIndex();
    }

}
