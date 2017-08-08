package amodule.quan.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianghatest.R;

/**
 * PackageName : amodule.quan.view
 * Created by MrTrying on 2016/9/28 16:23.
 * E_mail : ztanzeyu@gmail.com
 */

public class SubjectHeaderAddress extends LinearLayout {
    private TextView addressTextView;

    public SubjectHeaderAddress(Context context) {
        this(context,null);
    }

    public SubjectHeaderAddress(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SubjectHeaderAddress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.subject_header_address_layout,this);

        initView();
    }

    private void initView() {
        addressTextView = (TextView) findViewById(R.id.address_tv);
    }

    public void setData(String address){
        if(TextUtils.isEmpty(address)){
            setVisibility(GONE);
        }else{
            addressTextView.setText(address);
            setVisibility(VISIBLE);
        }
    }
}
