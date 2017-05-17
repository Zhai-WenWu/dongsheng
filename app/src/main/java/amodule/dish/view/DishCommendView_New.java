package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.SyntaxTools;
import acore.tools.Tools;
import acore.widget.ImageViewVideo;
import amodule.dish.activity.DetailDish;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.scrollerAd.XHScrollerAdParent;
import third.ad.tools.AdPlayIdConfig;

/**
 * Created by ：fei_teng on 2017/1/12 17:47.
 */

public class DishCommendView_New extends DishBaseView {

    public static String DISH_STYLE_COMMEND = "dish_style_commend";
    public Activity activity;
    private Map<String, String> map;
    private boolean isADshow = false;
    private int down_x, down_y;
    private int adPosition = -1;
    private ADCallback mADCallback;

    public DishCommendView_New(Context context) {
        this(context, null);
    }

    public DishCommendView_New(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DishCommendView_New(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.a_home_xgtj_item);
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

        ImageView img = (ImageView) findViewById(R.id.iv_icon);
        TextView title = (TextView) findViewById(R.id.tv_name);
        TextView explian = (TextView) findViewById(R.id.tv_desc);
        TextView item_browser = (TextView) findViewById(R.id.tv_observed);
        TextView collected = (TextView) findViewById(R.id.tv_collected);
        TextView tv_origin = (TextView) findViewById(R.id.tv_origin);

        setViewImage(img, map.get("img"));
        title.setText(map.get("title"));
        explian.setText(map.get("burdens"));
        tv_origin.setText(map.get("source"));
        item_browser.setText(map.get("allClick") + "浏览");
        if("hide".equals(map.get("favorites"))){
            collected.setVisibility(View.GONE);
        }else{
            collected.setText(map.get("favorites") + "收藏");
            collected.setVisibility(View.VISIBLE);
        }
        if(!TextUtils.isEmpty(map.get("adPosition"))){
            adPosition = Integer.parseInt(map.get("adPosition"));
        }
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(isADshow()){
                if(mADCallback != null){
                    mADCallback.onAdClick(adPosition == -1 ? 0 : adPosition, "");
                }
            }
            }
        });

        this.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        down_x = (int) event.getX();
                        down_y = (int) event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        int up_x = (int) event.getX();
                        int up_y = (int) event.getY();
                        int dp_3 = Tools.getDimen(context, R.dimen.dp_3);
                        if (Math.abs(down_x - up_x) < dp_3 && Math.abs(up_y - down_y) < dp_3
                                && !"2".equals(map.get("isAD"))) {
                            XHClick.mapStat(activity, DetailDish.tongjiId, "底部推荐数据", "相关推荐");
                            AppCommon.openUrl(activity, map.get("link"), true);
                        }
                        break;
                }
                return false;
            }
        });
    }

    private boolean isADshow(){
        return "2".equals(map.get("isAD"));
    }

    public void onListScroll() {
        if (isADshow()) {
            if (isADshow)
                return;
            isADshow = true;
            if(mADCallback != null){
                mADCallback.onAdBind(adPosition == -1 ? 0 : adPosition, this, "");
            }
        }
    }

    public interface ADCallback{
        public void onAdBind(int index, View view, String listIndex);
        public void onAdClick(int index, String listIndex);
    }

    public ADCallback getmADCallback() {
        return mADCallback;
    }

    public void setmADCallback(ADCallback mADCallback) {
        this.mADCallback = mADCallback;
    }
}
