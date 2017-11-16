package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
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
import xh.basic.tool.UtilString;

import static amodule.dish.activity.DetailDish.tongjiId;

/**
 * 主辅料
 */
public class DishIngreDataShow extends ItemBaseView {
    private LinearLayout linear_data;
    private LinearLayout recommendAd_linear;

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
        linear_data = (LinearLayout) findViewById(R.id.linear_data);
        recommendAd_linear = (LinearLayout) findViewById(R.id.recommendAd_linear);
    }
    /**
     * 设置数据
     *
     * @param lists
     */
    public void setData(final ArrayList<Map<String, String>> lists) {
        if (lists == null) return;
        //主料
        View view = LayoutInflater.from(context).inflate(R.layout.view_dish_header_data, null);
        TableLayout zhuLiaoTab = (TableLayout) view.findViewById(R.id.zhu_liao_tab);
        TextView zhu_tv = (TextView) view.findViewById(R.id.zhu_tv);
        zhu_tv.setText("用料");

        if (lists.size() > 0) {
            zhuLiaoTab.setVisibility(View.VISIBLE);
            for (Map<String, String> map : lists) {
                map.put("goIngre", map.get("url").length() > 0 ? "ignore" : "hide");
            }
            AdapterSimple adapter = new AdapterSimple(zhuLiaoTab, lists,
                    R.layout.table_cell_burden,
                    new String[]{"name", "goIngre", "content"},
                    new int[]{R.id.itemText1, R.id.itemImg1, R.id.itemText2});
            SetDataView.view(zhuLiaoTab, 1, adapter, null,
                    new SetDataView.ClickFunc[]{new SetDataView.ClickFunc() {
                        @Override
                        public void click(int index, View v) {
                            Map<String, String> ingre = lists.get(index);
                            if (ingre.get("url").length() > 0) {
                                XHClick.mapStat(context, tongjiId, "菜谱区域的点击", "用料部分的点击量");
                                XHClick.mapStat(context, tongjiId, "食材部分的点击量", "");
                                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), ingre.get("url"), false);
                            }
                        }
                    }});
            linear_data.addView(view);
        } else zhuLiaoTab.setVisibility(View.GONE);


    }

    private void handleRecommendAD(ArrayList<Map<String, String>> lists) {
        //处理广告
        if (lists.get(0).containsKey("recommendAD") && !TextUtils.isEmpty(lists.get(0).get("recommendAD")) && !"[]".equals((lists.get(0).get("recommendAD")))) {
            recommendAd_linear.setVisibility(View.VISIBLE);
            final ArrayList<Map<String, String>> list_recommend = UtilString.getListMapByJson(lists.get(0).get("recommendAD"));
            String name = list_recommend.get(0).get("name");
            String desc = list_recommend.get(0).get("desc");
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
//            LayoutParams layoutParams= new LayoutParams(LayoutParams.WRAP_CONTENT,dp_28);
//            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//            layoutParams.s
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
            recommendAd_linear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), list_recommend.get(0).get("link"), true);
                    XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), tongjiId, "广告/运营", "用料下方广告位");
                }
            });
        } else {
            recommendAd_linear.setVisibility(View.GONE);
        }
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
}
