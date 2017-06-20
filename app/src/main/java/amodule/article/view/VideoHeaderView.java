package amodule.article.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Map;

import acore.tools.StringManager;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/6/19 15:29.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoHeaderView extends LinearLayout {
    private CustomerView customerView;
    private VideoInfoView videoInfoView;


    public VideoHeaderView(Context context) {
        super(context);
        this.setOrientation(LinearLayout.VERTICAL);
        initView();
    }

    public VideoHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setOrientation(LinearLayout.VERTICAL);
        initView();
    }

    public VideoHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOrientation(LinearLayout.VERTICAL);
        initView();
    }

    private void initView(){
        videoInfoView = new VideoInfoView(getContext());
        videoInfoView.setType(mCurrType);
        addView(videoInfoView);

        customerView = new CustomerView(getContext());
        customerView.setType(mCurrType);
        addView(customerView);

    }

    public void setData(Map<String, String> mapVideo){
        if(mapVideo == null){
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);
        //设置videoinfo数据
        videoInfoView.setData(mapVideo);
        //设置用户数据
        if (mapVideo.containsKey("customer") && !TextUtils.isEmpty(mapVideo.get("customer"))) {
            Map<String, String> mapUser = StringManager.getFirstMap(mapVideo.get("customer"));
            customerView.setType(mCurrType);
            customerView.setData(mapUser);
        }
    }

    private String mCurrType;
    public void setType(String type) {
        mCurrType = type;
    }

}
