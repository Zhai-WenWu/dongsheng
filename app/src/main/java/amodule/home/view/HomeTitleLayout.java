package amodule.home.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.xiangha.R;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import amodule._common.delegate.IStatictusData;
import amodule.main.view.MessageTipIcon;
import amodule.search.avtivity.HomeSearch;
import aplug.web.ShowWeb;

import static amodule.main.activity.MainHomePage.STATICTUS_ID_PULISH;

/**
 * Description :
 * PackageName : amodule.home.view
 * Created by MrTrying on 2017/11/13 18:19.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeTitleLayout extends RelativeLayout implements View.OnClickListener,IStatictusData {

    HomePushIconView mPulishView;
    MessageTipIcon mMessageTipIcon;

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
        mPulishView.setStatictusID(STATICTUS_ID_PULISH);
        mMessageTipIcon = (MessageTipIcon) findViewById(R.id.message_tip);

        mPulishView.setOnClickListener(this);
        findViewById(R.id.home_search_layout).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v == null) return;
        switch (v.getId()){
            case R.id.home_publish_btn:
                //统计
                XHClick.mapStat(getContext(),id,twoLevel,"发布按钮");
                mPulishView.showPulishMenu();
                break;
            case R.id.home_search_layout:
                //统计
                XHClick.mapStat(getContext(),id,twoLevel,"搜索框");
                getContext().startActivity(new Intent(getContext(), HomeSearch.class));
                break;
        }
    }

    String id,twoLevel;
    @Override
    public void setStatictusData(String id, String twoLevel, String threeLevel) {
        this.id = id;
        this.twoLevel = twoLevel;
    }

    public void setMessage(int messageTipCount){
        if(mMessageTipIcon != null){
            mMessageTipIcon.setMessageTip(messageTipCount);
        }
    }

    public interface OnClickActivityIconListener{
        void onCLick(View v,String url);
    }

    public void setOnClickActivityIconListener(OnClickActivityIconListener onClickActivityIconListener) {
        mOnClickActivityIconListener = onClickActivityIconListener;
    }
}
