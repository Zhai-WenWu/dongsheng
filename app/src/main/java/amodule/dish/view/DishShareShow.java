package amodule.dish.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.ToolsDevice;
import third.share.ShareNewActivity;
import third.share.ShareTools;

import static amodule.dish.activity.DetailDish.tongjiId;


/**
 * 分享标签
 */

public class DishShareShow extends DishBaseView{
    public static String DISH_STYLE_SHARE="dish_style_share";
    public static int DISH_STYLE_SHARE_INDEX=3;
    private ArrayList<Map<String,String>> mData ;
    private String[] mNames ;
    private int[] mLogos ;
    private String[] mSharePlatforms ;
    private Map<String,String> map;
    public DishShareShow(Context context) {
        super(context, R.layout.view_dish_header_share);
    }

    public DishShareShow(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_header_share);
    }

    public DishShareShow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_header_share);
    }

    @Override
    public void init() {
        super.init();
        initData();
    }

    /**
     * 设置数据
     */
    public void setData(Map<String,String> maps){
        this.map=maps;
        //因gridView带来onclick事件无法回到主线程问题，故直接布局写成
        ((ImageView)findViewById(R.id.share_logo_1)).setBackgroundResource(mLogos[0]);
        ((ImageView)findViewById(R.id.share_logo_2)).setBackgroundResource(mLogos[1]);
        ((ImageView)findViewById(R.id.share_logo_3)).setBackgroundResource(mLogos[2]);
        ((ImageView)findViewById(R.id.share_logo_4)).setBackgroundResource(mLogos[3]);
        ((TextView)findViewById(R.id.share_name_1)).setText(mNames[0]);
        ((TextView)findViewById(R.id.share_name_2)).setText(mNames[1]);
        ((TextView)findViewById(R.id.share_name_3)).setText(mNames[2]);
        ((TextView)findViewById(R.id.share_name_4)).setText(mNames[3]);
        findViewById(R.id.share_linear_1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goShare(0);
            }
        });
        findViewById(R.id.share_linear_2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goShare(1);
            }
        });
        findViewById(R.id.share_linear_3).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goShare(2);
            }
        });
        findViewById(R.id.share_linear_4).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShareNewActivity.class);
                intent.putExtra("type", map.get("type"));
                intent.putExtra("title", map.get("title"));
                intent.putExtra("clickUrl", map.get("clickUrl"));
                intent.putExtra("content", map.get("content"));
                intent.putExtra("imgUrl", map.get("imgUrl"));
                intent.putExtra("from", map.get("from"));
                intent.putExtra("parent", map.get("parent"));
                context.startActivity(intent);
            }
        });
    }

    /**
     * 去分享
     * @param position
     */
    private void goShare(int position){
        XHClick.mapStat(context, tongjiId, "分享", mNames[position]);
        String platfrom = mSharePlatforms[position];
        ShareTools barShare = ShareTools.getBarShare(context);
        barShare.showSharePlatform(map.get("title"),map.get("content"),map.get("type"),map.get("imgUrl"),map.get("clickUrl"),platfrom,map.get("from"),map.get("parent"));

    }
    private void initData(){
        mData = new ArrayList<Map<String,String>>();
        if(ToolsDevice.isAppInPhone(context, "com.tencent.mm") == 0){
            mNames = new String[]{"QQ空间","QQ好友","新浪微博","更多"};
            mLogos = new int[]{
                    R.drawable.logo_space_new,R.drawable.logo_qq_new,
                    R.drawable.logo_sina_new,R.drawable.logo_more_new};
            mSharePlatforms = new String[]{
                    ShareTools.QQ_ZONE,ShareTools.QQ_NAME,
                    ShareTools.SINA_NAME,"more"};
        }else{
            mNames = new String[]{"微信好友","微信朋友圈","QQ好友","更多"};
            mLogos = new int[]{R.drawable.logo_weixin_new,R.drawable.logo_friends_new,
                    R.drawable.logo_qq_new,R.drawable.logo_more_new};
            mSharePlatforms = new String[]{
                    ShareTools.WEI_XIN,ShareTools.WEI_QUAN,
                    ShareTools.QQ_NAME,"more"};
        }
        for(int i = 0; i < mNames.length; i ++){
            Map<String,String> map = new HashMap<String,String>();
            map.put("name", mNames[i]);
            map.put("img", "" + mLogos[i]);
            mData.add(map);
        }
    }
}
