package acore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xiangha.R;

/**
 * Created by xiangha-zhangyujian on 2017/6/14
 */

public class switchView extends RelativeLayout{
    private  Context context;
    private ImageView iv_switch;
    private OnSwitchChangeListener mListener = null;
    public boolean mSwitchOn = true;//开关默认是开着的

    public switchView(Context context) {
        super(context);
        this.context=context;
        initView();
    }

    public switchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        initView();
    }

    public switchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        initView();
    }
    private void initView(){
        LayoutInflater.from(context).inflate(R.layout.view_switch,this,true);
        iv_switch= (ImageView) findViewById(R.id.iv_switch);
        iv_switch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener!=null){
                    mSwitchOn=!mSwitchOn;
                    mListener.onChange(mSwitchOn);
                }
            }
        });
        iv_switch.setVisibility(VISIBLE);

    }

    public void setOnChangeListener(OnSwitchChangeListener listener) {
        mListener = listener;
    }
    public interface OnSwitchChangeListener {
        public void onChange( boolean state);
    }
    public void setState(boolean state){
        mSwitchOn=state;
        setViewState();
    }
    private void setViewState(){
        if(mSwitchOn)iv_switch.setImageResource(R.drawable.i_switch_on);
        else iv_switch.setImageResource(R.drawable.i_switch_off);
    }
}
