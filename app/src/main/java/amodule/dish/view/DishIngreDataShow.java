package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.SetDataView;
import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;
import acore.tools.Tools;
import amodule.health.activity.DetailIngre;
import third.mall.tool.ToolView;
import third.mall.widget.ListViewForScrollView;
import xh.basic.tool.UtilString;

import static amodule.dish.activity.DetailDish.tongjiId;

/**
 * 主辅料
 */
public class DishIngreDataShow extends ItemBaseView implements View.OnClickListener {
    private LinearLayout recommendAd_linear;
    private ArrayList<Map<String, String>> listAll;
    private ArrayList<Map<String, String>> lists = new ArrayList<>();
    private ArrayList<Map<String, String>> listNoAll = new ArrayList<>();
    private AdapterSimple adapter;
    private String isSpread = "";//1--未展开状态，2--展开状态
    private boolean isSupport = false;
    private TextView ingre_all_tv;

    public DishIngreDataShow(Context context) {
        super(context, R.layout.view_dish_data_show);
    }

    public DishIngreDataShow(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_data_show);
    }

    public DishIngreDataShow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_data_show);
    }

    @Override
    public void init() {
        super.init();
        recommendAd_linear = (LinearLayout) findViewById(R.id.recommendAd_linear);
        findViewById(R.id.ingre_all).setOnClickListener(this);
        ingre_all_tv= (TextView) findViewById(R.id.ingre_all_tv);
        isSupport = false;
    }

    /**
     * 设置数据
     */
    public void setData(final ArrayList<Map<String, String>> listmap) {
        if (listmap == null || listmap.size() <= 0) return;
        this.listAll = listmap;
        for (Map<String, String> map : listAll) {
            map.put("goIngre", map.get("url").length() > 0 ? "ignore" : "hide");
        }
        if (listAll.size() > 4) {
            isSupport = true;
            for (int i = 0; i < 4; i++) {
                listNoAll.add(listAll.get(i));
            }
        }
        lists.addAll( isSupport ? listNoAll : listAll);
        isSpread = isSupport ? "1" : "2";
        findViewById(R.id.ingre_all).setVisibility(isSupport?View.VISIBLE:View.GONE);
        ListViewForScrollView listview_scroll = (ListViewForScrollView) findViewById(R.id.listview_scroll);
        if (lists.size() > 0) {
            listview_scroll.setVisibility(View.VISIBLE);
            adapter = new AdapterSimple(listview_scroll, lists,
                    R.layout.table_cell_burden,
                    new String[]{"name", "goIngre", "content"},
                    new int[]{R.id.itemText1, R.id.itemImg1, R.id.itemText2});
            listview_scroll.setAdapter(adapter);
            listview_scroll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Map<String, String> ingre = lists.get(position);
                    if (ingre.get("url").length() > 0) {
                        XHClick.mapStat(context, tongjiId, "菜谱区域的点击", "用料部分的点击量");
                        XHClick.mapStat(context, tongjiId, "食材部分的点击量", "");
                        AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), ingre.get("url"), false);
                    }
                }
            });
            adapter.notifyDataSetChanged();
        } else listview_scroll.setVisibility(View.GONE);
    }


    private void handleRecommendAD(ArrayList<Map<String, String>> lists) {
        //处理广告
//        if (lists.get(0).containsKey("recommendAD") && !TextUtils.isEmpty(lists.get(0).get("recommendAD")) && !"[]".equals((lists.get(0).get("recommendAD")))) {
//            recommendAd_linear.setVisibility(View.VISIBLE);
//            final ArrayList<Map<String, String>> list_recommend = UtilString.getListMapByJson(lists.get(0).get("recommendAD"));
//            String name = list_recommend.get(0).get("name");
//            String desc = list_recommend.get(0).get("desc");
        String name = "电商不错哦";
        String desc = "心态决定人生，细节决定成败";
            if (TextUtils.isEmpty(name)) name = "推荐";

            if (TextUtils.isEmpty(name) && TextUtils.isEmpty(desc)) {
                recommendAd_linear.setVisibility(View.GONE);
                return;
            } else {
                recommendAd_linear.setVisibility(View.VISIBLE);
            }
            int tv_distance = (int) this.getResources().getDimension(R.dimen.dp_17);
            int dp_13 = (int) this.getResources().getDimension(R.dimen.dp_13);
            int tv_pad = ToolView.dip2px(context, 1.0f);
            int distance_commend = name.length() * dp_13 + (name.length() - 1) * tv_pad + Tools.getDimen(context, R.dimen.dp_6);
            int num_text = setTextViewNum(distance_commend);
            TextView quan_title_1 = (TextView) findViewById(R.id.quan_title_1);
            TextView quan_title_2 = (TextView) findViewById(R.id.quan_title_2);
            TextView tv_recommend = (TextView) findViewById(R.id.tv_recommend);

            if (!TextUtils.isEmpty(name)) {
                tv_recommend.setVisibility(View.VISIBLE);
                tv_recommend.setText(name);
                tv_recommend.setBackgroundResource(R.drawable.round_red2);
            } else tv_recommend.setVisibility(View.GONE);

            int dp_28 = (int) this.getResources().getDimension(R.dimen.dp_28);
            quan_title_1.setPadding(distance_commend, 0, 0, 0);
            if (num_text >= desc.length()) {
                quan_title_1.setText(desc);
                quan_title_2.setVisibility(View.GONE);
                quan_title_1.setVisibility(View.VISIBLE);
            } else {
                quan_title_1.setText(desc.substring(0, num_text));
                quan_title_2.setText(desc.substring(num_text, desc.length()));
                quan_title_1.setVisibility(View.VISIBLE);
                quan_title_2.setVisibility(View.VISIBLE);
            }
            //点击跳转页面
            recommendAd_linear.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), list_recommend.get(0).get("link"), true);
                    XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), tongjiId, "广告/运营", "用料下方广告位");
                }
            });
//        } else {
//            recommendAd_linear.setVisibility(View.GONE);
//        }
    }

    /**
     * 获取值得买每行的字数
     *
     * @return
     */
    private int setTextViewNum(int distance_commend) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int tv_distance = (int) this.getResources().getDimension(R.dimen.dp_17);
        int distance = (int) this.getResources().getDimension(R.dimen.dp_15);

        int waith = wm.getDefaultDisplay().getWidth();
        int tv_waith = waith - distance * 2 - distance_commend;
        int tv_pad = ToolView.dip2px(context, 1.0f);
        int num = (tv_waith + tv_pad) / (tv_distance + tv_pad);
        return num;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ingre_all:
                lists.clear();
                setIngreState();
                break;
        }
    }
    private void setIngreState(){
        if("1".equals(isSpread)){
            lists.addAll(listAll);
            isSpread="2";
            ingre_all_tv.setText("收起");
            adapter.notifyDataSetChanged();
        }else if("2".equals(isSpread)){
            lists.addAll(listNoAll);
            isSpread="1";
            ingre_all_tv.setText("展开全部");
            adapter.notifyDataSetChanged();
        }
    }
}
