package third.mall.override;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import acore.override.activity.base.BaseActivity;
import acore.tools.PageStatisticsUtils;
import third.mall.aplug.MallCommon;

/**
 * 基础类：用于统计
 */

public class MallBaseActivity extends BaseActivity{

    public static final String PAGE_FROM = "ds_from";//上一个页面来源的key
    public static final String PAGE_LOGO = "_";//key数据之间的分隔符
    public String dsFrom="";
    public String nowFrom="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dsFrom = getIntent().getStringExtra(PAGE_FROM);
        if(!TextUtils.isEmpty(dsFrom)){
            nowFrom=dsFrom;
            MallCommon.statictisFrom+=TextUtils.isEmpty(MallCommon.statictisFrom)?nowFrom:PAGE_LOGO+nowFrom;
        }
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
