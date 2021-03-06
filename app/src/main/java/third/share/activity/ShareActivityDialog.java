/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package third.share.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.ToolsDevice;
import amodule.article.activity.ReportActivity;
import amodule.quan.activity.QuanReport;
import third.share.adapter.ShareAdapter;
import third.share.module.ShareModule;
import third.share.tools.ShareTools;

import static third.share.tools.ShareTools.mParent;

/**
 * 分享弹框：
 */
public class ShareActivityDialog extends Activity implements View.OnClickListener{

    private ArrayList<ShareModule> mData = new ArrayList<>();
    private String[] mSharePlatforms;
    private String mTitle,mContent,mType,mImgUrl,mClickUrl, mShareFrom,mShareTwoData,reportUrl;
    private Boolean isHasReport;

    private String nickName,userCode;

    private String tongjiId = "a_user";

    private String mShareParams;
    private String mExtraParams;

    private String mNewReportType;//1:主题，2:评论（默认），3:回复
    private String mNewReportContent;
    private String mNewType;//文章1；视频2；问答3；电商4

    private boolean mShowIntegralTip;
    private boolean mUseNewReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_user_home_share);
        tongjiId = getIntent().getStringExtra("tongjiId");
        isHasReport = getIntent().getBooleanExtra("isHasReport",false);
        userCode = getIntent().getStringExtra("code");
        nickName = getIntent().getStringExtra("nickName");

        //分享必须
        mClickUrl = getIntent().getStringExtra("clickUrl");
        mTitle = getIntent().getStringExtra("title");
        if (TextUtils.isEmpty(mTitle))//默认是个人主页的
            mTitle = "【香哈菜谱】“" + nickName + "”的个人主页";
        mContent = getIntent().getStringExtra("content");
        if (TextUtils.isEmpty(mContent))//默认是个人主页的
            mContent = "“" + nickName + "”入驻香哈菜谱啦，快来关注与Ta一起学习做菜吧！";
        mImgUrl = getIntent().getStringExtra("imgUrl");
        mType = getIntent().getStringExtra("type");
        if (TextUtils.isEmpty(mType))//默认是来自网络的
            mType = ShareTools.IMG_TYPE_WEB;

        mShareFrom = getIntent().getStringExtra("shareFrom");
        if (TextUtils.isEmpty(mShareFrom))
            mShareFrom = "个人主页";
        //分享的二级内容
        mShareTwoData= getIntent().getStringExtra("shareTwoContent");
        if(TextUtils.isEmpty(mShareTwoData)){
            mShareTwoData="分享和举报";
        }
        //举报跳转页面
        reportUrl=getIntent().getStringExtra("reportUrl");

        //分享的配置数据
        mShareParams = getIntent().getStringExtra("shareParams");

        mShowIntegralTip = getIntent().getBooleanExtra("showIntegralTip", false);
        mExtraParams = getIntent().getStringExtra("extraParams");

        mUseNewReport = getIntent().getBooleanExtra("new_report", false);
        mNewType = getIntent().getStringExtra("new_type");
        mNewReportType = getIntent().getStringExtra("new_reportType");
        mNewReportContent = getIntent().getStringExtra("new_reportContent");

        init();
    }

    private void init(){
        initData();

        findViewById(R.id.a_user_home_share_layout).setOnClickListener(this);
        findViewById(R.id.a_user_home_share_close).setOnClickListener(this);

        GridView mGridView = (GridView)findViewById(R.id.d_popwindow_share_gridview);
        ShareAdapter adapter = new ShareAdapter();
        adapter.setData(mData);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String platfrom = mSharePlatforms[position];
                XHClick.mapStat(ShareActivityDialog.this, tongjiId, mShareTwoData, mData.get(position).getTitle());
                if ("report_new".equals(platfrom)) {
                    Intent intent = new Intent(ShareActivityDialog.this, ReportActivity.class);
                    intent.putExtra("code", userCode);
                    intent.putExtra("type", mNewType);
                    intent.putExtra("userCode", userCode);
                    intent.putExtra("reportName", nickName);
                    intent.putExtra("reportContent", mNewReportContent);
                    intent.putExtra("reportType", mNewReportType);
                    ShareActivityDialog.this.startActivity(intent);
                } else if("report".equals(platfrom)){ //举报
                    if(TextUtils.isEmpty(reportUrl)) {
                        Intent intent = new Intent(ShareActivityDialog.this, QuanReport.class);
                        intent.putExtra("isQuan", "0");
                        intent.putExtra("nickName", "举报 " + nickName);
                        intent.putExtra("code", userCode);
                        intent.putExtra("repType", "4");
                        intent.putExtra("subjectCode", "0");
                        ShareActivityDialog.this.startActivity(intent);
                    }else{
                        AppCommon.openUrl(ShareActivityDialog.this,reportUrl,true);
                    }
                }else{
                    ShareTools barShare = ShareTools.getBarShare(ShareActivityDialog.this);
                    Map<String, String> dataMap = new HashMap<>();
                    dataMap.put("type", mType);
                    dataMap.put("title", mTitle);
                    dataMap.put("url", mClickUrl);
                    dataMap.put("content", mContent);
                    dataMap.put("img", mImgUrl);
                    dataMap.put("from", mShareFrom);
                    dataMap.put("parent", mParent);
                    dataMap.put("platform", platfrom);
                    dataMap.put("extraParams", mExtraParams);
                    if (!TextUtils.isEmpty(mShareParams))
                        dataMap.put("shareParams", mShareParams);
                    barShare.showSharePlatform(dataMap);
                }
                ShareActivityDialog.this.finish();
            }
        });
    }

    private void initData(){
        String[] mNames;
        int[] mLogos;
        //自己不举报自己
        if(ToolsDevice.isAppInPhone(this, "com.tencent.mm") == 0){
            mNames = new String[]{"QQ好友","QQ空间","新浪微博","短信","复制链接","举报"};
            mLogos = new int[]{
                    R.drawable.logo_qq_new,R.drawable.logo_space_new,
                    R.drawable.logo_sina_new,R.drawable.logo_message_new,
                    R.drawable.logo_copy_new,R.drawable.logo_copy_report};
            mSharePlatforms = new String[]{
                    ShareTools.QQ_NAME,ShareTools.QQ_ZONE,
                    ShareTools.SINA_NAME,ShareTools.SHORT_MESSAGE,
                    ShareTools.LINK_COPY,mUseNewReport ? "report_new" : "report"};
        }else{
            mNames = new String[]{"微信好友","微信朋友圈","QQ好友","QQ空间","新浪微博","短信","复制链接","举报"};
            mLogos = new int[]{R.drawable.logo_weixin_new,R.drawable.logo_friends_new,
                    R.drawable.logo_qq_new,R.drawable.logo_space_new,
                    R.drawable.logo_sina_new,R.drawable.logo_message_new,
                    R.drawable.logo_copy_new,R.drawable.logo_copy_report};
            mSharePlatforms = new String[]{
                    ShareTools.WEI_XIN,ShareTools.WEI_QUAN,
                    ShareTools.QQ_NAME,ShareTools.QQ_ZONE,
                    ShareTools.SINA_NAME,ShareTools.SHORT_MESSAGE,
                    ShareTools.LINK_COPY,mUseNewReport ? "report_new" : "report"};
        }

        int mapSize = mNames.length;
        if(!isHasReport){
            mapSize --;
        }
        for(int i = 0; i < mapSize; i ++){
            String platform = mSharePlatforms[i];
            ShareModule module = new ShareModule();
            module.setResId(mLogos[i]);
            module.setTitle(mNames[i]);
            module.setIntegralTipShow(mShowIntegralTip && (TextUtils.equals(platform, ShareTools.WEI_XIN) || TextUtils.equals(platform, ShareTools.WEI_QUAN)));
            mData.add(module);
        }
    }

    @Override
    public void onClick(View v) {
        ShareActivityDialog.this.finish();
    }
}
