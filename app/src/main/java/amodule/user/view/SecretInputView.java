package amodule.user.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xianghatest.R;

/**
 * Created by ：fei_teng on 2017/2/17 16:04.
 */

public class SecretInputView extends RelativeLayout implements View.OnClickListener {

    private final EditText user_secret;
    private final ImageView iv_state_secret;
    private final ImageView iv_del;
    private boolean isShowSecret;
    private Context context;
    private SecretInputViewCallback callback;

    public SecretInputView(Context context) {
        this(context, null);
    }

    public SecretInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SecretInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.a_login_secret, this, true);
        this.context = context;
        user_secret = (EditText) findViewById(R.id.et_user_secret);
        iv_del = (ImageView) findViewById(R.id.iv_del);
        iv_state_secret = (ImageView) findViewById(R.id.iv_state_secret);

        iv_del.setVisibility(GONE);

        iv_del.setOnClickListener(this);
        iv_state_secret.setOnClickListener(this);
        hideSecret();

        user_secret.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() > 0) {
                    iv_del.setVisibility(VISIBLE);
                } else {
                    iv_del.setVisibility(GONE);
                }
                callback.onInputSecretChanged();

            }
        });

        user_secret.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    iv_del.setVisibility(GONE);
                } else {
                    if(user_secret.getText().length()>0){
                        iv_del.setVisibility(VISIBLE);
                    }else{
                        iv_del.setVisibility(GONE);
                    }
                }
            }
        });

    }

    public void init(String hint, SecretInputViewCallback callback) {

        user_secret.setHint(hint);
        this.callback = callback;
    }


    public void showSecret() {
        isShowSecret = true;
        iv_state_secret.setBackgroundResource(R.drawable.a_login_button_password2);
        user_secret.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        user_secret.setSelection(user_secret.length());
    }

    private void hideSecret() {
        isShowSecret = false;
        iv_state_secret.setBackgroundResource(R.drawable.a_login_button_password);
        user_secret.setTransformationMethod(PasswordTransformationMethod.getInstance());
        user_secret.setSelection(user_secret.length());
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_state_secret:
                if (isShowSecret) {
                    hideSecret();
                } else {
                    showSecret();
                }
                callback.OnClicksecret();
                break;

            case R.id.iv_del:
                user_secret.setText("");
                callback.onInputSecretChanged();
            default:
                break;
        }

    }

    public String getPassword() {
        return user_secret.getText().toString();
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(getPassword());
    }

    public interface SecretInputViewCallback {
        void onInputSecretChanged();
        void OnClicksecret();
    }
}
