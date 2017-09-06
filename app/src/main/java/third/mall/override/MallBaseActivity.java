package third.mall.override;

import android.os.Bundle;
import android.text.TextUtils;

import acore.override.activity.base.BaseActivity;
import acore.tools.PageStatisticsUtils;

/**
 * 基础类：用于统计
 */

public class MallBaseActivity extends BaseActivity{

    public static final String PAGE_FROM = "dsFrom";//上一个页面来源的key
    public static final String PAGE_FROM_TWO = "two";
    public static final String PAGE_LOGO = "#";//key数据之间的分隔符
    public String dsFrom="";
    public String nowFrom="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dsFrom = getIntent().getStringExtra(PAGE_FROM);
        String two = getIntent().getStringExtra(PAGE_FROM_TWO);
        if(!TextUtils.isEmpty(dsFrom)){
            PageStatisticsUtils.getInstance().onPageChange(TextUtils.isEmpty(two) ? dsFrom : dsFrom + "-" + two,this);
        }
        handlerFrom();
    }

    /**
     *处理当前from
     */
    public void handlerFrom(){
        String pageName= PageStatisticsUtils.getInstance().getPageName(this);
        nowFrom=TextUtils.isEmpty(dsFrom)?pageName:dsFrom+PAGE_LOGO+pageName;
    }
    public String getNowFrom(){
        return nowFrom;
    }
}
