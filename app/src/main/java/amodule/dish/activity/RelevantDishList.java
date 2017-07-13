package amodule.dish.activity;

import android.os.Bundle;

import com.xiangha.R;

import acore.override.activity.base.BaseActivity;

/**
 * Created by Fang Ruijiao on 2017/7/13.
 */

public class RelevantDishList extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("",3,0, R.layout.a_dish_detail_bar_title,R.layout.a_relevandish);
    }
}
