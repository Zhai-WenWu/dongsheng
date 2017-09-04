package amodule.user.view;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

/**
 * Created by ：fei_teng on 2017/2/16 16:50.
 */

public class PhoneNumInputView extends RelativeLayout implements View.OnClickListener {

    private final LinearLayout ll_country_id;
    private final ImageView iv_del;
    private TextView user_contryId;
    private EditText user_phone_number;
    private PhoneNumInputViewCallback callback;

    public PhoneNumInputView(Context context) {
        this(context, null);
    }

    public PhoneNumInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhoneNumInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.a_login_phone_info, this, true);
        user_contryId = (TextView) findViewById(R.id.user_contryId);
        user_phone_number = (EditText) findViewById(R.id.user_phone_number);
        ll_country_id = (LinearLayout) findViewById(R.id.ll_country_id);
        iv_del = (ImageView) findViewById(R.id.iv_del);

        iv_del.setVisibility(GONE);

        ll_country_id.setOnClickListener(this);
        iv_del.setOnClickListener(this);
        user_phone_number.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        user_phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isChina = "86".equals(getZoneCode());
                if (isChina && count == 1) {
                    int length = s.toString().length();
                    if (length == 3 || length == 8) {
                        user_phone_number.setText(s + " ");
                        user_phone_number.setSelection(user_phone_number.getText().toString().length());
                    }
                }
                InputFilter inputFilter = new InputFilter.LengthFilter(isChina ? 13 : 20);
                user_phone_number.setFilters(new InputFilter[]{inputFilter});
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isVisibility = user_phone_number.getText().length() > 0;
                iv_del.setVisibility(isVisibility ? VISIBLE : GONE);
                callback.onPhoneInfoChanged();
            }
        });

        user_phone_number.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                boolean isVisibility = hasFocus && user_phone_number.getText().length() > 0;
                iv_del.setVisibility(isVisibility ? VISIBLE : GONE);
            }
        });
    }

    public void init(String hint, String zoneCode, String phoneNum, PhoneNumInputViewCallback callback) {
        this.callback = callback;

        if (!TextUtils.isEmpty(zoneCode))
            user_contryId.setText("+" + zoneCode);

        if (!TextUtils.isEmpty(phoneNum)) {
            user_phone_number.setText("86".equals(zoneCode)
                    ? phoneNum.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1 $2 $3") : phoneNum);
        } else if (!TextUtils.isEmpty(hint)) {
            user_phone_number.setHint(hint);
        }
    }

    public void setInfo(String zoneCode, String phoneNum){
        if (!TextUtils.isEmpty(zoneCode))
            user_contryId.setText("+" + zoneCode);

        if (!TextUtils.isEmpty(phoneNum)) {
            user_phone_number.setText("86".equals(zoneCode)
                    ? phoneNum.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1 $2 $3") : phoneNum);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_country_id:
                callback.onZoneCodeClick();
                break;
            case R.id.iv_del:
                user_phone_number.setText("");
                iv_del.setVisibility(GONE);
                break;
            default:
                break;
        }
    }


    public void setZoneCode(String code) {
        if (!TextUtils.isEmpty(code))
            user_contryId.setText(code);

        boolean isChina = "86".equals(code);
        InputFilter inputFilter = new InputFilter.LengthFilter(isChina ? 13 : 20);
        user_phone_number.setFilters(new InputFilter[]{inputFilter});
    }

    public String getZoneCode() {
        return user_contryId.getText().toString().replace("+", "");
    }

    public String getPhoneNum() {
        return user_phone_number.getText().toString().replace(" ", "");
    }

    public boolean isDataAbsence() {
        return TextUtils.isEmpty(getZoneCode()) || TextUtils.isEmpty(getPhoneNum());
    }

    public interface PhoneNumInputViewCallback {

        void onZoneCodeClick();

        void onPhoneInfoChanged();
    }
}
