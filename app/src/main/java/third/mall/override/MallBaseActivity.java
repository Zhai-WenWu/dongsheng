package third.mall.override;

import android.os.Bundle;
import android.text.TextUtils;

import acore.override.activity.base.BaseActivity;
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
            MallCommon.setStatictisFrom(dsFrom);
        }
    }
}
