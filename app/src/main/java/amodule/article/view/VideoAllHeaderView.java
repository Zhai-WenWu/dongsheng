package amodule.article.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xianghatest.R;

import java.util.Map;

import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.view.DishHeaderViewNew;
import third.video.VideoPlayerController;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/6/19 15:29.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoAllHeaderView extends LinearLayout {
    private Activity activity;
    private VideoHeaderView videoHeaderView;
    private CustomerView customerView;
    private VideoInfoView videoInfoView;

    public VideoAllHeaderView(Context context) {
        super(context);
        if(context instanceof Activity)
            this.activity = (Activity) context;
        this.setOrientation(LinearLayout.VERTICAL);
        initView();
    }

    private void initView(){
        videoHeaderView = new VideoHeaderView(getContext());
        videoHeaderView.initView(activity);
        addView(videoHeaderView);

        videoInfoView = new VideoInfoView(getContext());
        addView(videoInfoView);

        customerView = new CustomerView(getContext());
        customerView.setType(mCurrType);
        LinearLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, Tools.getDimen(getContext(), R.dimen.dp_5),0,0);
        addView(customerView,layoutParams);

    }

    public void setData(boolean isOnlyUser, Map<String, String> mapVideo, Map<String, String> detailPermissionMap){
        if(mapVideo == null){
            setVisibility(GONE);
            return;
        }

        videoInfoView.setType(mCurrType);
        setVisibility(VISIBLE);
        if(!isOnlyUser){
            videoHeaderView.setData(mapVideo, null,detailPermissionMap);
            //设置videoinfo数据
            videoInfoView.setData(mapVideo);
            setUserData(mapVideo);
        }
    }

    public void setupCommentNum(String commentNumStr){
        if(videoInfoView != null && !TextUtils.isEmpty(commentNumStr)){
            videoInfoView.setupConmentNum(commentNumStr);
        }
    }

    public void setUserData(Map<String, String> mapVideo){
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

    public void setLoginStatus(){
        if(videoHeaderView != null)
            videoHeaderView.setLoginStatus();
    }

    public void onResume() {
        if(videoHeaderView!=null)
            videoHeaderView.onResume();
    }
    public void onPause() {
        if(videoHeaderView!=null)
            videoHeaderView.onPause();
    }

    public boolean onBackPressed() {
        return null != videoHeaderView && videoHeaderView.onBackPressed();
    }

    public void onDestroy() {
        if(videoHeaderView != null)
            videoHeaderView.onDestroy();
    }

    public VideoHeaderView getVideoHeaderView() {
        return videoHeaderView;
    }
}
