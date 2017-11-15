package amodule.home.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.xiangha.R;

import amodule.search.avtivity.HomeSearch;

/**
 * Description :
 * PackageName : amodule.home.view
 * Created by MrTrying on 2017/11/13 18:19.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeTitleLayout extends RelativeLayout implements View.OnClickListener{

    HomeActivityIconView mIconView;
    HomePushIconView mPulishView;

    OnClickActivityIconListener mOnClickActivityIconListener;

    public HomeTitleLayout(Context context) {
        this(context,null);
    }

    public HomeTitleLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HomeTitleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(R.layout.a_home_title,this,true);
        mPulishView = (HomePushIconView) findViewById(R.id.home_publish_btn);
        mIconView = (HomeActivityIconView) findViewById(R.id.home_act_btn);

        mPulishView.setOnClickListener(this);
        mIconView.setOnClickListener(this);
        findViewById(R.id.home_search_layout).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v == null) return;
        switch (v.getId()){
            case R.id.home_act_btn:
                if(mOnClickActivityIconListener != null && mIconView != null){
                    mOnClickActivityIconListener.onCLick(mIconView,mIconView.getUrl());
                }
                break;
            case R.id.home_publish_btn:
                mPulishView.showPulishMenu();
                break;
            case R.id.home_search_layout:
                getContext().startActivity(new Intent(getContext(), HomeSearch.class));
                break;
        }
    }

    public interface OnClickActivityIconListener{
        void onCLick(View v,String url);
    }

    public void setOnClickActivityIconListener(OnClickActivityIconListener onClickActivityIconListener) {
        mOnClickActivityIconListener = onClickActivityIconListener;
    }
}
