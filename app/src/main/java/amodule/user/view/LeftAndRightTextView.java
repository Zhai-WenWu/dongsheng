package amodule.user.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

/**
 * Created by ：fei_teng on 2017/2/21 18:40.
 */

public class LeftAndRightTextView extends RelativeLayout implements View.OnClickListener {


    private final Context context;
    private TextView tv_left;
    private TextView tv_right;
    private TextView tv_hint_newInfo;
    private ImageView iv_right,iv_switch;
    private View line_bottom;
    private RelativeLayout rl_outer;
    private LeftAndRightTextViewCallback callback;

    public LeftAndRightTextView(Context context) {
        this(context, null);
    }

    public LeftAndRightTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeftAndRightTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.left_and_right_text_view, this, true);
        this.context = context;
        initView();
    }

    private void initView() {
        tv_left = (TextView) findViewById(R.id.tv_left);
        tv_right = (TextView) findViewById(R.id.tv_right);
        tv_hint_newInfo = (TextView) findViewById(R.id.hint_new_info);
        iv_right = (ImageView) findViewById(R.id.iv_right);
        iv_switch = (ImageView) findViewById(R.id.iv_switch);
        line_bottom = (View) findViewById(R.id.line_bottom);
        rl_outer = (RelativeLayout) findViewById(R.id.rl_outer);


        tv_right.setVisibility(VISIBLE);
        iv_right.setVisibility(GONE);


    }

    public void init(String leftStr, String rightStr, boolean showRightIcon,
                     final LeftAndRightTextViewCallback callback) {
        init(leftStr, rightStr, true, showRightIcon, callback);
    }

    public void init(String leftStr, String rightStr, boolean showBelowLine,boolean showRightIcon,final LeftAndRightTextViewCallback callback) {

        if (callback != null) {
            rl_outer.setOnClickListener(this);
            this.callback = callback;
        }
        tv_left.setText(leftStr);
        if (!TextUtils.isEmpty(rightStr)) {
            tv_right.setText(rightStr);
            tv_right.setVisibility(VISIBLE);
        }

        if (showRightIcon) {
            iv_right.setVisibility(VISIBLE);
        } else {
            iv_right.setVisibility(GONE);
        }

        if (showBelowLine) {
            line_bottom.setVisibility(VISIBLE);
        } else {
            line_bottom.setVisibility(INVISIBLE);
        }
    }

    public void setSwitch(boolean isShow,OnClickListener listener){
        if (isShow) {
            iv_switch.setVisibility(VISIBLE);
            iv_switch.setOnClickListener(listener);
        } else {
            iv_switch.setVisibility(GONE);
        }
    }

    public void switchState(boolean isChose){
        if(isChose)iv_switch.setImageResource(R.drawable.i_switch_on);
        else iv_switch.setImageResource(R.drawable.i_switch_off);
    }

    public void setRightText(String rightStr) {
        if (!TextUtils.isEmpty(rightStr)) {
            tv_right.setVisibility(VISIBLE);
            tv_right.setText(rightStr);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_outer:
                callback.onClick();
                break;
            default:
                break;
        }
    }

    public void setNewHintVisibility(int visibility) {
        tv_hint_newInfo.setVisibility(visibility);
    }

    public void setArrowRightVisibility (int visibility) {
        iv_right.setVisibility(visibility);
    }

    public void setSwitchBtnVisibility (int visibility) {
        iv_switch.setVisibility(visibility);
    }

    public interface LeftAndRightTextViewCallback {

        void onClick();
    }
}
