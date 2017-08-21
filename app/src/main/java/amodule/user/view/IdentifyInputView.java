package amodule.user.view;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.xianghatest.R;

import static com.baidu.location.b.k.ca;

/**
 * Created by dao on 2017/2/19.
 */

public class IdentifyInputView extends RelativeLayout implements View.OnClickListener {

    private final EditText et_identify;
    private final Button btn_identify_request;
    private final CountDownTimer countDownTimer;
    private IdentifyInputViewCallback callback;

    /**
     * 等待验证码时间：秒
     */
    private int waitTime = 60;

    public IdentifyInputView(Context context) {
        this(context, null);
    }

    public IdentifyInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IdentifyInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.a_login_identify, this, true);
        et_identify = (EditText) findViewById(R.id.et_identify);
        et_identify.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        btn_identify_request = (Button) findViewById(R.id.btn_identify_request);

        btn_identify_request.setOnClickListener(this);

        et_identify.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                callback.onInputDataChanged();
            }
        });

        countDownTimer = new CountDownTimer(waitTime * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
//                Log.i("FRJ","millisUntilFinished:" + millisUntilFinished);
                btn_identify_request.setBackgroundResource(R.drawable.bg_round_gray_identify);
                btn_identify_request.setTextColor(Color.parseColor("#999999"));
                btn_identify_request.setClickable(false);
                btn_identify_request.setText("重新获取(" + (int)(millisUntilFinished / 1000) + ")");
                callback.onTick(waitTime * 1000 - millisUntilFinished);
            }

            @Override
            public void onFinish() {
                btn_identify_request.setText("获取验证码");
                btn_identify_request.setTextColor(Color.parseColor("#ff533c"));
                btn_identify_request.setClickable(true);
                btn_identify_request.setBackgroundResource(R.drawable.bg_round_red_identify);
                callback.onCountDownEnd();
            }
        };

    }


    public void init(String defaultHint, IdentifyInputViewCallback callback) {

        this.callback = callback;
        if (!TextUtils.isEmpty(defaultHint))
            et_identify.setHint(defaultHint);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_identify_request:
                btn_identify_request.setClickable(false);
                callback.onCliclSendIdentify();
                break;
            default:
                break;
        }
    }

    public void startCountDown(){
        countDownTimer.start();
    }

    /*** 设置获取验证码可点击*/
    public void setOnBtnClickState(boolean state){
        btn_identify_request.setClickable(state);
    }

    public String getIdentify() {
        return et_identify.getText().toString();
    }

    public boolean isIdentifyCodeEmpty() {
        return TextUtils.isEmpty(et_identify.getText().toString());
    }

    public interface IdentifyInputViewCallback {

        /**
         * 每隔countDownInterval秒会回调一次onTick()方法
         * @param millisUntilFinished
         */
        void onTick(long millisUntilFinished);

        /**
         * 倒计时完成后回调
         */
        void onCountDownEnd();

        /**
         * 验证码内容变化后回调
         */
        void onInputDataChanged();

        /**
         * 点击获取验证码后回调
         */
        void onCliclSendIdentify();
    }

}
