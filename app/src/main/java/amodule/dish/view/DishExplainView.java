package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.override.view.ItemBaseView;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.activity.DetailDish;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.quan.db.CircleData;
import amodule.quan.db.CircleSqlite;
import amodule.user.activity.login.LoginByAccout;
import aplug.feedback.activity.Feedback;


/**
 * 小贴士
 */
public class DishExplainView extends ItemBaseView {
    private LinearLayout mAdLayout;
    private DishAdDataViewNew dishAdDataView;
    public DishExplainView(Context context) {
        super(context, R.layout.view_dish_explain);
    }

    public DishExplainView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_explain);
    }
    public DishExplainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_explain);
    }
    @Override
    public void init() {
        super.init();
        mAdLayout = (LinearLayout)findViewById(R.id.a_dish_detail_ad);
        dishAdDataView = new DishAdDataViewNew(context,R.layout.view_dish_tips_ad_layout_distance);
        dishAdDataView.getRequest(XHActivityManager.getInstance().getCurrentActivity(), mAdLayout);
    }

    public void setData(final Map<String,String> maps){
        TextView explain_content_tv= (TextView) findViewById(R.id.explain_content_tv);
        if(maps.containsKey("remark")&& !TextUtils.isEmpty(maps.get("remark"))){
            explain_content_tv.setText(maps.get("remark"));
            findViewById(R.id.tv_explain).setVisibility(View.VISIBLE);
            explain_content_tv.setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.tv_explain).setVisibility(View.GONE);
            explain_content_tv.setVisibility(View.GONE);
        }
    }
}
