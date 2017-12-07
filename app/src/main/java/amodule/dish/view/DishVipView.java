package amodule.dish.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;

/**
 * vip权限按钮
 */
public class DishVipView extends ItemBaseView {
    private TextView text_vip;

    public DishVipView(Context context) {
        super(context, R.layout.view_dish_vip);
    }

    public DishVipView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_vip);
    }

    public DishVipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_vip);
    }

    @Override
    public void init() {
        super.init();
        text_vip = (TextView) findViewById(R.id.text_vip);
    }

    public void setData(final Map<String, String> maps) {
//        int strokeWidth = 5; // 3dp 边框宽度
        int roundRadius = 15; // 8dp 圆角半径
//        int strokeColor = Color.parseColor("#2E3135");//边框颜色
        String bgColor = handlerColorValue(maps.get("backColor"));
        int fillColor = Color.parseColor(TextUtils.isEmpty(bgColor) ? "#f23030" : bgColor);//内部填充颜色

        GradientDrawable gd = new GradientDrawable();//创建drawable
        gd.setColor(fillColor);
        gd.setCornerRadius(roundRadius);
//        gd.setStroke(strokeWidth, strokeColor);

        text_vip.setBackgroundDrawable(gd);
        text_vip.setText(maps.get("text"));
        String textColorValue = handlerColorValue(maps.get("textColor"));
        int textColor = Color.parseColor(TextUtils.isEmpty(textColorValue) ? "#FFFFFE" : textColorValue);
        text_vip.setTextColor(textColor);
        text_vip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), maps.get("clickUrl"), false);
            }
        });
    }

    private String handlerColorValue(String colorValue){
        if(!TextUtils.isEmpty(colorValue)){
            if(!colorValue.startsWith("#")){
                colorValue = "#" + colorValue;
            }
            if(colorValue.length() != 7 && colorValue.length() != 9){
                colorValue = "";
            }
        }
        return colorValue;
    }
}
