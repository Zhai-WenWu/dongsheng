package amodule.user.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

/**
 * Created by ：fei_teng on 2017/2/17 17:49.
 */

public class NextStepView extends RelativeLayout implements View.OnClickListener {

    private final Context context;
    private final Button user_next_step_btn;
    private final TextView tv_left;
    private final TextView tv_right;
    private NextStepViewCallback callBack;

    public NextStepView(Context context) {
        this(context, null);
    }

    public NextStepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NextStepView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.a_login_next_step_view, this, true);
        this.context = context;

        user_next_step_btn = (Button) findViewById(R.id.user_next_step_btn);
        tv_left = (TextView) findViewById(R.id.tv_left);
        tv_right = (TextView) findViewById(R.id.tv_right);

        user_next_step_btn.setOnClickListener(this);
        tv_left.setOnClickListener(this);
        tv_right.setOnClickListener(this);

        setClickCenterable(false);

    }


    public void init(String title, String leftStr, String rightStr, NextStepViewCallback callBack) {

        this.callBack = callBack;
        user_next_step_btn.setText(title);

        if (!TextUtils.isEmpty(leftStr)) {
            tv_left.setText(leftStr);
            tv_left.setVisibility(VISIBLE);
        } else {
            tv_left.setVisibility(GONE);
        }

        if (!TextUtils.isEmpty(rightStr)) {
            tv_right.setText(rightStr);
            tv_right.setVisibility(VISIBLE);
        } else {
            tv_right.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.user_next_step_btn:
                callBack.onClickCenterBtn();
                break;
            case R.id.tv_left:
                callBack.onClickLeftView();
                break;
            case R.id.tv_right:
                callBack.onClickRightView();
                break;
            default:
                break;
        }
    }


    public interface NextStepViewCallback {

        void onClickCenterBtn();

        void onClickLeftView();

        void onClickRightView();
    }

    public void setClickCenterable(boolean useable) {

        if (useable) {
            user_next_step_btn.setBackgroundResource(R.drawable.bg_next_step_read);
            user_next_step_btn.setClickable(true);
        } else {
            user_next_step_btn.setBackgroundResource(R.drawable.bg_next_step_gray);
            user_next_step_btn.setClickable(false);
        }
    }

}
