/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package third.share.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.ToolsDevice;
import third.share.tools.ShareTools;

import static amodule.dish.activity.DetailDishWeb.tongjiId;

/**
 * Created by Administrator on 2016/8/5.
 */
public class ShareNewActivity extends Activity{
    private ArrayList<Map<String,String>> mData = new ArrayList<>();
    private String[] mNames ;
    private int[] mLogos ;
    private String[] mSharePlatforms ;
    private String mType,mTitle,mClickUrl,mContent,mImgUrl,mFrom,mParent;

    private Boolean isHasReport;

    private String mShareParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_share_dialog);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            try{
                isHasReport = getIntent().getBooleanExtra("isHasReport",false);
                mType = bundle.getString("type");
                mTitle = bundle.getString("title");
                mClickUrl = bundle.getString("clickUrl");
                mContent = bundle.getString("content");
                mImgUrl = bundle.getString("imgUrl");
                mFrom = bundle.getString("from");
                mParent = bundle.getString("parent");

                //分享的配置数据
                mShareParams = getIntent().getStringExtra("shareParams");
            }catch(Exception e){}
        }
        init();
    }

    private void init() {
        initData();
        GridView sahre_gridview= (GridView) findViewById(R.id.sahre_gridview);
        SimpleAdapter adapter = new SimpleAdapter(this, mData,
                R.layout.share_item_new,
                new String[]{"img","name"},
                new int[]{R.id.share_logo,R.id.share_name});
        sahre_gridview.setAdapter(adapter);
        sahre_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                XHClick.mapStat(ShareNewActivity.this, tongjiId, "分享", mNames[position]);
                String platfrom = mSharePlatforms[position];
                ShareTools barShare = ShareTools.getBarShare(ShareNewActivity.this);
                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("type", mType);
                dataMap.put("title", mTitle);
                dataMap.put("url", mClickUrl);
                dataMap.put("content", mContent);
                dataMap.put("img", mImgUrl);
                dataMap.put("from", mFrom);
                dataMap.put("parent", mParent);
                dataMap.put("platform", platfrom);
                if (!TextUtils.isEmpty(mShareParams))
                    dataMap.put("shareParams", mShareParams);
                barShare.showSharePlatform(dataMap);
                ShareNewActivity.this.finish();
            }
        });
        findViewById(R.id.activity_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCloseThis(v);
            }
        });
        findViewById(R.id.share_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCloseThis(v);
            }
        });
    }

    private void initData(){
        if(ToolsDevice.isAppInPhone(this, "com.tencent.mm") == 0){
            mNames = new String[]{"QQ好友","QQ空间","新浪微博","短信","复制链接","举报"};
            mLogos = new int[]{
                    R.drawable.logo_qq_new,R.drawable.logo_space_new,
                    R.drawable.logo_sina_new,R.drawable.logo_message_new,
                    R.drawable.logo_copy_new,R.drawable.logo_copy_report};
            mSharePlatforms = new String[]{
                    ShareTools.QQ_NAME,ShareTools.QQ_ZONE,
                    ShareTools.SINA_NAME,ShareTools.SHORT_MESSAGE,
                    ShareTools.LINK_COPY,"report"};
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
                    ShareTools.LINK_COPY,"report"};
        }

        int mapSize = mNames.length;
        if(!isHasReport){
            mapSize --;
        }
        for(int i = 0; i < mapSize; i ++){
            Map<String,String> map = new HashMap<String,String>();
            map.put("name", mNames[i]);
            map.put("img", "" + mLogos[i]);
            mData.add(map);
        }
    }

    public void onCloseThis(View v){
        this.finish();
    }

}
