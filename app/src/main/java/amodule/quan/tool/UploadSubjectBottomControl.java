package amodule.quan.tool;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.tools.AgreementManager;
import acore.tools.Tools;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.BrowseHistory;
import third.location.LocationSys;

/**
 * Created by Fang Ruijiao on 2017/7/19.
 */
public class UploadSubjectBottomControl implements View.OnClickListener{
    private Activity mAct;
    //--------- 协议 ------------
    private AgreementManager mAgreementManager;
    //----------定位------------
    private UploadSubjectLocationControl mLocationControl;
    public LocationSys mLocationSys;

    private TextView followDishTv;
    private LinearLayout scoreLayout;
    private ImageView imgGo,imgClear;

    private String dishCode,dishName;
    private int scoreNum = 0;

    private boolean isFollowDish = true;

    public UploadSubjectBottomControl(UploadSubjectNew act, final String atreementUrl){
        mAct = act;
        mAgreementManager = new AgreementManager(act,atreementUrl);
        mLocationSys = new LocationSys(act);
        mLocationControl = new UploadSubjectLocationControl(act,mLocationSys);
        act.findViewById(R.id.follow_dish_parentlayout).setOnClickListener(this);
        followDishTv = (TextView) act.findViewById(R.id.follow_dish_tv);
        scoreLayout = (LinearLayout) act.findViewById(R.id.score_dish_linearLayout);
        imgGo = (ImageView) act.findViewById(R.id.follow_dish_go);
        imgClear = (ImageView) act.findViewById(R.id.follow_dish_clear);
        imgClear.setOnClickListener(this);
        initScoreLayout();
    }

    private void initScoreLayout(){
        ImageView scoreImg;
        int dimen22 = Tools.getDimen(mAct,R.dimen.dp_22);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dimen22,dimen22);
        layoutParams.setMargins(0,0,Tools.getDimen(mAct,R.dimen.dp_10),0);
        for(int i = 1; i < 6; i++){
            scoreImg = new ImageView(mAct);
            scoreImg.setLayoutParams(layoutParams);
            scoreImg.setImageResource(R.drawable.i_score);
            scoreImg.setTag(i);
            scoreImg.setOnClickListener(this);
            scoreLayout.addView(scoreImg);
        }
    }

    public void setScoreLayoutVisible(boolean isVisible){
        if(isVisible){
            mAct.findViewById(R.id.follow_dish_parentlayout).setVisibility(View.VISIBLE);
            mAct.findViewById(R.id.score_dish_line).setVisibility(View.VISIBLE);
        }else{
            mAct.findViewById(R.id.follow_dish_parentlayout).setVisibility(View.GONE);
            mAct.findViewById(R.id.score_dish_parentlayout).setVisibility(View.GONE);
            mAct.findViewById(R.id.score_dish_line).setVisibility(View.GONE);
        }
    }

    public boolean getIsChecked(){
        return mAgreementManager.getIsChecked();
    }

    public void stopLocation(){
        mLocationSys.mLocationClient.stop();
    }

    public void onLocationClick(){
        mLocationControl.onLocationClick();
    }

    public boolean getIsLocation(){
        return mLocationControl.getIsLocation();
    }

    public String getLocationJson(){
        return mLocationControl.getLocationJson();
    }

    public String getDishCode(){
        return dishCode;
    }

    public void setDishInfo(String dishCode,String dishName) {
        this.dishCode = dishCode;
        this.dishName = dishName;
        followDishTv.setText(dishName);
        mAct.findViewById(R.id.score_dish_parentlayout).setVisibility(View.VISIBLE);
    }

    public int getScoreNum(){
        return scoreNum;
    }

    public void setIsFollowDish(boolean isFollowDish){
        this.isFollowDish = isFollowDish;
    }

    public void onActivityResult(Intent data) {
        dishCode = data.getStringExtra("dishCode");
        dishName = data.getStringExtra("dishName");
        followDishTv.setText(dishName);
        mAct.findViewById(R.id.score_dish_parentlayout).setVisibility(View.VISIBLE);
        imgGo.setVisibility(View.GONE);
        imgClear.setVisibility(View.VISIBLE);
        if(mOnBottomListener != null) mOnBottomListener.onChoseFollowDish();
    }

    @Override
    public void onClick(View v) {
        if(v.getTag() != null){
            int tag = Integer.parseInt(String.valueOf(v.getTag()));
            clickScore(tag);
        }else {
            switch (v.getId()) {
                case R.id.follow_dish_parentlayout:
                    if(isFollowDish) {
                        Intent followDishIntent = new Intent(mAct, BrowseHistory.class);
                        followDishIntent.putExtra("isChoose", true);
                        mAct.startActivityForResult(followDishIntent, UploadSubjectNew.CHOOSE_DISH);
                    }
                    break;
                case R.id.follow_dish_clear:
                    dishCode = null;
                    dishName = null;
                    followDishTv.setText("");
                    mAct.findViewById(R.id.score_dish_parentlayout).setVisibility(View.GONE);
                    imgGo.setVisibility(View.VISIBLE);
                    imgClear.setVisibility(View.GONE);
                    if(mOnBottomListener != null) mOnBottomListener.onClearFollowDish();
                    break;
            }
        }
    }

    private void clickScore(int tag){
        scoreNum = tag;
        for(int i = 0; i < tag; i++){
            ImageView scoreImg = (ImageView) scoreLayout.getChildAt(i);
            scoreImg.setImageResource(R.drawable.i_score_activity);
        }
        for(int i = tag; i < scoreLayout.getChildCount(); i ++){
            ImageView scoreImg = (ImageView) scoreLayout.getChildAt(i);
            scoreImg.setImageResource(R.drawable.i_score);
        }
    }

    private OnBottomListener mOnBottomListener;
    public void setOnBottomListener(OnBottomListener listener){
        mOnBottomListener = listener;
    }

    public interface OnBottomListener{
        public void onChoseFollowDish();
        public void onClearFollowDish();
    }
}
