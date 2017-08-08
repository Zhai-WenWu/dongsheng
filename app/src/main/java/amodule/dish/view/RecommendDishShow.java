package amodule.dish.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.override.view.ItemBaseView;
import acore.override.adapter.AdapterSimple;
import third.mall.view.HorizontalListView;
import xh.basic.tool.UtilString;

/**
 * 推荐标签
 */

public class RecommendDishShow extends ItemBaseView {
    private HorizontalListView horizontalListView;
    private AdapterSimple adapterSimple;
    private ListView listview;

    public RecommendDishShow(Context context,int style,  ArrayList<Map<String, String>> lists) {
        super(context, R.layout.view_dish_foot_recommed);
        setStyle(style,lists);
//        setData();
    }

    public RecommendDishShow(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_foot_recommed);
    }

    public RecommendDishShow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_foot_recommed);
    }

    @Override
    public void init() {
        super.init();
        horizontalListView = (HorizontalListView) findViewById(R.id.horizontalListView);
        findViewById(R.id.textview_rela).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    /**
     * 设置数据
     *
     * @param lists
     */
    public void setData() {
        ArrayList<Map<String, String>> lists = new ArrayList<Map<String, String>>();
        for (int i = 0; i < 10; i++) {
            Map<String, String> map = new HashMap<>();
            map.put("name", "name" + i);
            map.put("price", "pricr" + i);
            lists.add(map);
        }
        AdapterSimple adapter = new AdapterSimple(horizontalListView, lists, R.layout.a_mall_advert_item_view, new String[]{"name", "price"}, new int[]{R.id.itemText1, R.id.text_rela_price});
        horizontalListView.setAdapter(adapter);
        horizontalListView.setVisibility(View.VISIBLE);
    }

    /**
     * 设置数据样式
     *
     * @param style
     * @param lists
     */
    public void setStyle(int style, final ArrayList<Map<String, String>> lists) {
        switch (style) {
            case 1:
                adapterSimple = new AdapterSimple(horizontalListView, lists, R.layout.a_mall_advert_item_view, new String[]{}, new int[]{R.id.iv_img, R.id.itemText1, R.id.text_rela_price});
                break;
            case 2:
                for (Map<String, String> content : lists) {
                    ArrayList<Map<String, String>> cusInfo = UtilString.getListMapByJson(content.get("customer"));
                    content.remove("customer");
                    if (cusInfo.size() > 0) {
                        content.put("nickName", cusInfo.get(0).get("nickName"));
                        content.put("nickImg", cusInfo.get(0).get("img"));
                    } else {
                        content.put("nickName", "");
                    }
                    if (!content.containsKey("hasVideo")) content.put("hasVideo", "1");
                }
                adapterSimple = new AdapterSimple(horizontalListView, lists,
                        R.layout.a_common_item_index_quan,
                        new String[]{"title", "nickName", "nickImg"},
                        new int[]{R.id.itemText1, R.id.itemText2, R.id.a_common_item_index_quan_user_img});
//                adapterSimple.scaleType = ImageView.ScaleType.CENTER_CROP;
//                adapterSimple.roundType = 1;
//                adapterSimple.roundImgPixels = ToolsDevice.dp2px(context, 400);
                break;
        }
        horizontalListView.setAdapter(adapterSimple);
        horizontalListView.setVisibility(View.VISIBLE);


    }
}
