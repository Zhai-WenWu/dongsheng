package amodule.lesson.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import amodule._common.helper.WidgetDataHelper;
import amodule._common.utility.WidgetUtility;
import amodule.vip.VipDataController;

import static amodule.vip.VipDataController.KEY_BTN_DATA;
import static amodule.vip.VipDataController.KEY_BTN_SHOW;

/**
 * Description :
 * PackageName : amodule.lesson.view
 * Created by mrtrying on 2017/12/19 15:43:13.
 * e_mail : ztanzeyu@gmail.com
 */
public class VIPButton extends CardView {

    TextView mTextView;

    VipDataController mVipDataController;

    public VIPButton(Context context) {
        super(context);
        initialze();
    }

    public VIPButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialze();
    }

    public VIPButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialze();
    }

    private void initialze(){
        mTextView = new TextView(getContext());
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setTextColor(Color.WHITE);
        mTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f);
        addView(mTextView, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        setCardElevation(0);

        mVipDataController = new VipDataController();
        loadData();
    }

    private void loadData(){
        if(null == mVipDataController) return;
        mVipDataController.loadVIPButtonData(obj -> setData(StringManager.getFirstMap(obj)));
    }

    public void setText(String text){
        if(TextUtils.isEmpty(text)) return;
        mTextView.setText(text);
    }

    public void setTextColor(String colorValue){
        if(!TextUtils.isEmpty(colorValue)
                && colorValue.startsWith("#")
                && (colorValue.length() == 7 || colorValue.length() == 9)
                ){
            mTextView.setTextColor(Color.parseColor(colorValue));
        }
    }

    public void setTextColor(int color){
        mTextView.setTextColor(color);
    }

    public void setBackgroundColor(String colorValue){
        if(!TextUtils.isEmpty(colorValue)
                && colorValue.startsWith("#")
                && (colorValue.length() == 7 || colorValue.length() == 9)
                ){
            setBackgroundColor(Color.parseColor(colorValue));
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setCardBackgroundColor(color);
    }

    public void setData(Map<String,String> data){
        if("2".equals(data.get(KEY_BTN_SHOW))){
            Map<String,String> buttonData = StringManager.getFirstMap(data.get(KEY_BTN_DATA));
            WidgetUtility.setTextToView(mTextView,buttonData.get("title"));
            setTextColor(buttonData.get("color"));
            setBackgroundColor(buttonData.get("bgColor"));
            setOnClickListener(v -> AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),buttonData.get("url"),false));
            setVisibility(VISIBLE);
        }else{
            setVisibility(GONE);
        }
    }

    public void refresh(){
        loadData();
    }

    // 文本显示逻辑、自身显示罗、行为逻辑

}
