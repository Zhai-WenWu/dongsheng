package amodule.dish.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.LoginManager;
import amodule.user.activity.login.LoginByAccout;

/**
 * PackageName : amodule.dish.view
 * Created by MrTrying on 2017/6/27 15:31.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoDredgeVipView extends RelativeLayout {
    private TextView tipMessage,dredgeVip;
    private LinearLayout logingLayout;
    public VideoDredgeVipView(Context context) {
        super(context);
        initView();
    }

    public VideoDredgeVipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public VideoDredgeVipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView(){
        LayoutInflater.from(getContext()).inflate(R.layout.v_video_vip_layout,this);
        logingLayout = (LinearLayout) findViewById(R.id.login_layout);
        tipMessage = (TextView) findViewById(R.id.tip_message);
        dredgeVip = (TextView) findViewById(R.id.dredge_vip_text);

        setLogin();
    }

    public void setTipMessaText(String text){
        if(!TextUtils.isEmpty(text) && tipMessage != null)
            tipMessage.setText(text);
    }

    public void setDredgeVipText(String text){
        if(!TextUtils.isEmpty(text) && dredgeVip != null)
            dredgeVip.setText(text);
    }

    public void setDredgeVipClick(OnClickListener clickListener){
        if(clickListener != null)
            dredgeVip.setOnClickListener(clickListener);
    }

    public void setLogin(){
        if(LoginManager.isLogin())
            logingLayout.setVisibility(GONE);
        else{
            logingLayout.setVisibility(VISIBLE);
            logingLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContext().startActivity(new Intent(getContext(), LoginByAccout.class));
                }
            });
        }
    }

}
