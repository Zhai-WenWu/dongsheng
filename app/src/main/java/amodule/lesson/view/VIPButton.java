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

/**
 * Description :
 * PackageName : amodule.lesson.view
 * Created by mrtrying on 2017/12/19 15:43:13.
 * e_mail : ztanzeyu@gmail.com
 */
public class VIPButton extends CardView {

    TextView mTextView;

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

        //TODO
        mTextView.setText("开通会员");
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

    @Override
    public void setBackgroundColor(int color) {
        super.setCardBackgroundColor(color);
    }

    public interface HandlerDataAction{
        void handerData(VIPButton button);
    }

    //click、textColor、bgColor ----set
    // 文本显示逻辑、自身显示罗、行为逻辑

}
