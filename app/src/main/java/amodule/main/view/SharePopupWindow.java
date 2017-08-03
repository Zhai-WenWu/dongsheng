package amodule.main.view;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.tools.ToolsDevice;
import third.share.ShareTools;

/**
 * Created by sll on 2017/4/28.
 */

public class SharePopupWindow extends PopupWindow {

    private Activity mContext;
    private View mContentView;
    private GridView mGridView;

    private ArrayList<Map<String, String>> mDatas = new ArrayList<Map<String, String>>();
    private String[] mNames ;
    private int[] mLogos ;
    private String[] mSharePlatforms ;

    public SharePopupWindow(Activity context, Map<String, String > dataMap) {
        super(context);
        if (context == null)
            return;
        mContext = context;
        initData();
        initContentView(dataMap);
    }

    private void initContentView(final Map<String, String > dataMap) {
        mContentView = LayoutInflater.from(mContext).inflate(R.layout.share, null);
        mGridView = (GridView) mContentView.findViewById(R.id.share_gridview);
        setContentView(mContentView);
        setWidth(ViewGroup.MarginLayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.MarginLayoutParams.MATCH_PARENT);
        setAnimationStyle(R.style.BottomInOutPopupAnim);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(R.color.c_trans_parent)));
        mGridView.setAdapter(new SimpleAdapter(mContext, mDatas, R.layout.share_item, new String[]{"img","name"}, new int[]{R.id.share_logo,R.id.share_name}));
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String platfrom = mSharePlatforms[position];
                ShareTools barShare = ShareTools.getBarShare(mContext);
                if (dataMap != null)
                    barShare.showSharePlatform(dataMap.get("title"), dataMap.get("content"), dataMap.get("type"), dataMap.get("imgUrl"),
                        dataMap.get("clickUrl"), platfrom, dataMap.get("from"), dataMap.get("parent"));
            }
        });
        mContentView.findViewById(R.id.share_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissShare();
            }
        });
    }

    private void initData() {
        final String[] sharePlatfroms;
        if(ToolsDevice.isAppInPhone(mContext, "com.tencent.mm") == 0){
            mNames = new String[]{"QQ空间","QQ","新浪微博","信息","复制链接"};
            mLogos = new int[]{
                    R.drawable.logo_qzone,R.drawable.logo_qq,
                    R.drawable.logo_sina_weibo,R.drawable.logo_short_message,
                    R.drawable.logo_copy};
            mSharePlatforms = new String[]{
                    ShareTools.QQ_ZONE,ShareTools.QQ_NAME,
                    ShareTools.SINA_NAME,ShareTools.SHORT_MESSAGE,
                    ShareTools.LINK_COPY};
        }else{
            mNames = new String[]{"微信好友","微信朋友圈","QQ空间","QQ","新浪微博","信息","复制链接"};
            mLogos = new int[]{R.drawable.logo_wechat,R.drawable.logo_wechat_moments,
                    R.drawable.logo_qzone,R.drawable.logo_qq,
                    R.drawable.logo_sina_weibo,R.drawable.logo_short_message,
                    R.drawable.logo_copy};
            mSharePlatforms = new String[]{
                    ShareTools.WEI_XIN,ShareTools.WEI_QUAN,
                    ShareTools.QQ_ZONE,ShareTools.QQ_NAME,
                    ShareTools.SINA_NAME,ShareTools.SHORT_MESSAGE,
                    ShareTools.LINK_COPY};
        }
        for(int i = 0; i < mNames.length; i ++){
            Map<String,String> map = new HashMap<String,String>();
            map.put("name", mNames[i]);
            map.put("img", "" + mLogos[i]);
            mDatas.add(map);
        }

    }

    public void showShare() {
        if (this.isShowing()) {
            dismissShare();
            return;
        }
        if (mContext != null) {
            showAtLocation(mContext.findViewById(android.R.id.content), Gravity.FILL, 0, 0);
        }
    }

    public void dismissShare() {
        if (!this.isShowing())
            return;
        setAnimationStyle(R.anim.up_to_down_translate);
        dismiss();
    }
}
