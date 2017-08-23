package third.mall.override;

import android.os.Bundle;
import android.text.TextUtils;

import acore.override.activity.base.BaseActivity;
import acore.tools.PageStatisticsUtils;

/**
 * Created by Fang Ruijiao on 2017/8/23.
 */

public class MallBaseActivity extends BaseActivity{

    public static final String PAGE_FROM = "from";
    public static final String PAGE_FROM_TWO = "two";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String from = getIntent().getStringExtra(PAGE_FROM);
        String two = getIntent().getStringExtra(PAGE_FROM_TWO);
        if(!TextUtils.isEmpty(from)){
            PageStatisticsUtils.onPageChange(TextUtils.isEmpty(two) ? from : from + "-" + two,this);
        }
    }
}
