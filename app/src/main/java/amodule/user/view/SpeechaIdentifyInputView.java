package amodule.user.view;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

/**
 * Created by Fang Ruijiao on 2017/6/22.
 */

public class SpeechaIdentifyInputView extends RelativeLayout implements View.OnClickListener {

    private TextView speechaTv;

    public SpeechaIdentifyInputView(Context context) {
        this(context, null);
    }

    public SpeechaIdentifyInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeechaIdentifyInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundResource(R.color.transparent);
        LayoutInflater.from(context).inflate(R.layout.a_login_speecha_identify, this, true);
        speechaTv = (TextView) findViewById(R.id.user_login_speeach_tv);
        speechaTv.setText(Html.fromHtml("<u>语音验证码</u>"));
        speechaTv.setOnClickListener(this);
    }

    /**
     * 设置获取语言验证码状态
     * @param state ：true 可以点击重新获取    false:置灰，不可点击
     */
    public void setState(boolean state){
        speechaTv.setClickable(state);
        if(state){
            speechaTv.setTextColor(Color.parseColor("#ff533c"));
        }else{
            speechaTv.setTextColor(Color.parseColor("#999999"));
        }
    }

    private OnClickListener mListener;
    public void setOnSpeechaClickListener(OnClickListener listener){
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
//        Tools.showToast(mContext,"获取语音验证码");
        if(mListener != null) mListener.onClick(this);
    }
}
