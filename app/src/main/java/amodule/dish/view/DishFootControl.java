package amodule.dish.view;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.tools.Tools;
import amodule.dish.activity.RelevantDishList;
import amodule.quan.activity.upload.UploadSubjectNew;

/**
 * Created by Fang Ruijiao on 2017/7/13.
 */

public class DishFootControl implements View.OnClickListener{

    private Activity mAct;
    private LinearLayout mAdLayout;
    private RelativeLayout mRecomentLayout;
    private TextView mRecommentNum,mRelevantTv,mHoverNum,mQuizTv;

    private String code;

    public DishFootControl(Activity act){
        mAct = act;
    }

    public void init(String recomenNum,String hoverNum){
        mAdLayout = (LinearLayout) mAct.findViewById(R.id.a_dish_detail_new_tieshi_ad);
        mRecomentLayout = (RelativeLayout) mAct.findViewById(R.id.a_dish_detail_new_xiangguan);

        mRecommentNum = (TextView) mAct.findViewById(R.id.a_dish_detail_new_tv_num);
        mRelevantTv = (TextView) mAct.findViewById(R.id.a_dish_detail_new_relevantTv);
        mHoverNum = (TextView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_number);
        mQuizTv = (TextView) mAct.findViewById(R.id.a_dish_detail_new_footer_hover_tv);

        DishAdDataView dishAdDataView = new DishAdDataView(mAct);
        dishAdDataView.getRequest(mAct, mAdLayout);
        mRecomentLayout.setOnClickListener(this);
        mRelevantTv.setOnClickListener(this);
        mQuizTv.setOnClickListener(this);
        mRecommentNum.setText(recomenNum + "道");
        mHoverNum.setText(hoverNum);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.a_dish_detail_new_xiangguan: //点击相关作品
                Intent intent = new Intent(mAct, RelevantDishList.class);
                intent.putExtra("code",code);
                mAct.startActivity(intent);
                break;
            case R.id.a_dish_detail_new_relevantTv: //晒我做的这道菜
                Intent showIntent = new Intent(mAct, UploadSubjectNew.class);
                showIntent.putExtra("dishCode",code);
                mAct.startActivity(showIntent);
                break;
            case R.id.a_dish_detail_new_footer_hover_tv: //提问作者
                Tools.showToast(mAct,"提问作者");
                break;
        }
    }
}
