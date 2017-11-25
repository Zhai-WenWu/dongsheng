package amodule.home.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule._common.utility.WidgetUtility;

/**
 * Description :
 * PackageName : amodule._common.widget
 * Created by MrTrying on 2017/11/13 15:33.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeFuncNavView2 extends LinearLayout {

    protected View mLeftView,mRightView;

    public HomeFuncNavView2(Context context) {
        this(context,null);
    }

    public HomeFuncNavView2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HomeFuncNavView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
        initData();
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(R.layout.widget_func_nav_2_layout,this,true);
        mLeftView = findViewById(R.id.left_nav);
        mRightView = findViewById(R.id.right_nav);

        int height = (int) (((ToolsDevice.getWindowPx(getContext()).widthPixels - Tools.getDimen(getContext(),R.dimen.dp_50)) / 2) * 144 / 325f);
        mLeftView.getLayoutParams().height = height;
        mRightView.getLayoutParams().height = height;
    }

    protected void initData() {
        WidgetUtility.setTextToView(getTextView(R.id.text_left_1),"今日三餐");
        WidgetUtility.setTextToView(getTextView(R.id.text_left_2),"早.中.晚餐食谱");
        //10之前早上，10~14中午，14~23晚上
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0:00"));
        String currentHourStr = dateFormat.format(System.currentTimeMillis());
        int currentHour = Integer.parseInt(currentHourStr);
        Log.i("tzy","currentHour = " + currentHour);
        int resId = R.drawable.home_nav_dish_2;
        if(currentHour <= 10 || currentHour > 23){
            resId = R.drawable.home_nav_dish_1;
        }else if(currentHour > 10 && currentHour <= 14){
            resId = R.drawable.home_nav_dish_2;
        }else if(currentHour > 14 && currentHour <= 23){
            resId = R.drawable.home_nav_dish_3;
        }
        WidgetUtility.setResToImage(getImageView(R.id.icon_left_1),resId);

        WidgetUtility.setTextToView(getTextView(R.id.text_right_1),"关注/社区");
        WidgetUtility.setTextToView(getTextView(R.id.text_right_2),"晒美食,关注美食家");
        WidgetUtility.setResToImage(getImageView(R.id.icon_right_1),R.drawable.home_nav_circle);
    }

    protected TextView getTextView(int id){
        return (TextView)findViewById(id);
    }

    protected ImageView getImageView(int id){
        return (ImageView)findViewById(id);
    }
}
