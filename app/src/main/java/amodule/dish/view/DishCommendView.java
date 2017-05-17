package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;
import java.util.Random;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.Tools;
import amodule.dish.activity.DetailDish;
import third.ad.AdParent;
import third.ad.GdtAdNew;
import third.ad.TencenApiAd;
import third.ad.tools.GdtAdTools;
import third.mall.tool.ToolView;

import static com.xiangha.R.id.explian_1;
import static com.xiangha.R.id.explian_2;
import static com.xiangha.R.id.item_browser_1;
import static com.xiangha.R.id.item_browser_2;
import static third.ad.tools.TencenApiAdTools.TX_ID_DISH_DETAIL_SCOMMEND_1;

/**
 * 推荐item
 */
public class DishCommendView extends DishBaseView {

    public static String DISH_STYLE_COMMEND = "dish_style_commend";
    public static final int DISH_STYLE_COMMEND_INDEX = 5;
    public Activity activity;
    private Map<String, String> map;
    private boolean isADshow = false;
    private int down_x, down_y;

    public DishCommendView(Context context) {
        super(context, R.layout.view_dish_commend);
    }

    public DishCommendView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_commend);
    }

    public DishCommendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_commend);
    }

    @Override
    public void init() {
        super.init();
    }

    public void setData(final Map<String, String> map, final Activity activitys) {
        this.activity = activitys;
        this.map = map;

        //是否是第一个位置
        if (map.containsKey("position") && map.get("position").equals("0")) {
            findViewById(R.id.commend_line).setVisibility(View.VISIBLE);
            findViewById(R.id.dish_commend_tv).setVisibility(View.VISIBLE);
            findViewById(R.id.line_huaise).setVisibility(View.GONE);
        } else {
            findViewById(R.id.dish_commend_tv).setVisibility(View.GONE);
            findViewById(R.id.commend_line).setVisibility(View.GONE);
            findViewById(R.id.line_huaise).setVisibility(View.VISIBLE);
        }

        findViewById(R.id.a_home_main_ad_gdt_nouse_bottom).setVisibility(View.VISIBLE);
        findViewById(R.id.a_home_main_ad_gdt_nouse_bottom).setVisibility(View.VISIBLE);
        ImageView img = (ImageView) findViewById(R.id.img);
        int num = setTextViewNum(Tools.getDimen(context, R.dimen.dp_128));
        String title_map = map.get("title");
        TextView title;
        TextView explian;
        TextView item_browser;
        if (num >= title_map.length()) {
            findViewById(R.id.style_1).setVisibility(View.GONE);
            findViewById(R.id.style_2).setVisibility(View.VISIBLE);
            title = (TextView) findViewById(R.id.title_2);
            explian = (TextView) findViewById(explian_2);
            item_browser = (TextView) findViewById(item_browser_2);
        } else {
            findViewById(R.id.style_1).setVisibility(View.VISIBLE);
            findViewById(R.id.style_2).setVisibility(View.GONE);
            title = (TextView) findViewById(R.id.title_1);
            explian = (TextView) findViewById(explian_1);
            item_browser = (TextView) findViewById(item_browser_1);
        }

        setViewImage(img, map.get("img"));
        title.setText(map.get("title"));
        explian.setText(map.get("source"));
        item_browser.setText(map.get("allClick") + "浏览");
        setDishCommmedAd();
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                map.get("link")
//                Tools.showToast(activity, map.get("link"));


            }
        });
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        down_x = (int) event.getX();
                        down_y = (int) event.getY();
//                        Tools.showToast(activity, "down");
                        break;
                    case MotionEvent.ACTION_MOVE:
//                        Tools.showToast(activity, "move");
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
//                        Tools.showToast(activity, "up");
                        int up_x = (int) event.getX();
                        int up_y = (int) event.getY();
                        int dp_3 = Tools.getDimen(context, R.dimen.dp_3);
                         if (Math.abs(down_x - up_x) < dp_3 && Math.abs(up_y - down_y) < dp_3) {
                            XHClick.mapStat(activity, DetailDish.tongjiId, "底部推荐数据", "相关推荐");
                            AppCommon.openUrl(activity, map.get("link"), true);
                        }
                        break;
                }
                return false;
            }
        });
//        setAdData();
    }

    /**
     * 获取值得买每行的字数
     *
     * @return
     */
    private int setTextViewNum(int distance_commend) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int tv_distance = (int) this.getResources().getDimension(R.dimen.dp_18);
        int distance = (int) this.getResources().getDimension(R.dimen.dp_15);

        int waith = wm.getDefaultDisplay().getWidth();
        int tv_waith = waith - distance * 2 - distance_commend;
        int tv_pad = ToolView.dip2px(context, 1.0f);
        int num = (tv_waith + tv_pad) / (tv_distance + tv_pad);
        return num;
    }

    /**
     *加载广告
     */
    public void setDishCommmedAd() {
        if (isADshow) return;
        isADshow = true;
        if (map.containsKey("position") && map.get("position").equals("0")) {
            findViewById(R.id.a_home_main_ad_gdt_nouse_bottom).setVisibility(View.VISIBLE);
            findViewById(R.id.a_home_main_ad_gdt_nouse_bottom).setVisibility(View.VISIBLE);
            final RelativeLayout adTipLayout = (RelativeLayout) findViewById(R.id.a_home_main_ad_layout_nouse_bottom);
            final RelativeLayout adGdtLayout = (RelativeLayout) findViewById(R.id.a_home_main_ad_gdt_nouse_bottom);
            GdtAdNew gdtAdNew = new GdtAdNew(activity,"菜谱详情页相关推荐1",  adGdtLayout, GdtAdTools.ID_DISH_SCOMMEND_ONE, R.layout.view_dish_commend_item_ad_one, GdtAdNew.CREATE_AD, new GdtAdNew.AdListener() {
                @Override
                public void onAdCreate() {
                    TextView tv = (TextView) adGdtLayout.findViewById(R.id.view_ad_comment);
                    Random random = new Random();
                    int v = random.nextInt(4000) + 6000;
                    tv.setText(v + "浏览");
                    TextView tv_ping = (TextView) adGdtLayout.findViewById(R.id.view_ad_ping);
                    tv_ping.setVisibility(View.GONE);
                }
            });

            adTipLayout.setVisibility(View.GONE);
            adGdtLayout.setVisibility(View.GONE);


            final RelativeLayout adTencentLayout = (RelativeLayout) findViewById(R.id.a_home_main_ad_tencent_nouse_bottom);
            TencenApiAd tencenApiAd = new TencenApiAd(activity, "菜谱详情页相关推荐1", TX_ID_DISH_DETAIL_SCOMMEND_1,"101",
                    adTencentLayout,
                    R.layout.view_dish_commend_item_ad_one, new AdParent.AdListener() {

                @Override
                public void onAdCreate() {
                    super.onAdCreate();
                    TextView tv = (TextView) adTencentLayout.findViewById(R.id.view_ad_comment);
                    Random random = new Random();
                    int v = random.nextInt(10000 - 6000) + 6000;
                    tv.setText(v + "浏览");
                    TextView tv_ping = (TextView) adTencentLayout.findViewById(R.id.view_ad_ping);
                    tv_ping.setVisibility(View.GONE);
                }
            });

            adTipLayout.setVisibility(View.GONE);
            adGdtLayout.setVisibility(View.GONE);
            adTencentLayout.setVisibility(View.GONE);

            AdParent[] adsNouBottom = {tencenApiAd,gdtAdNew};
//            AdsShow adNouBottom = new AdsShow(adsNouBottom, AdPlayIdConfig.DISH_COMMEND_ONE);
//            adNouBottom.onResumeAd();
        }
    }
}
