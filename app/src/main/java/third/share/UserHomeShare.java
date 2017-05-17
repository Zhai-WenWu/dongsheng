package third.share;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import amodule.quan.activity.QuanReport;

import static third.share.ShareTools.mFrom;
import static third.share.ShareTools.mParent;

/**
 * Created by Fang Ruijiao on 2016/9/21.
 */
public class UserHomeShare extends Activity implements View.OnClickListener{

    private ArrayList<Map<String,String>> mData = new ArrayList<>();
    private String[] mSharePlatforms;
    private String mTitle,mContent,mType,mImgUrl,mClickUrl;
    private Boolean isHasReport;

    private String nickName,userCode;

    private String tongjiId = "a_user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_user_home_share);
        tongjiId = getIntent().getStringExtra("tongjiId");
        isHasReport = getIntent().getBooleanExtra("isHasReport",false);
        userCode = getIntent().getStringExtra("code");
        nickName = getIntent().getStringExtra("nickName");
        mClickUrl = getIntent().getStringExtra("clickUrl");
        mTitle = "【香哈菜谱】“" + nickName + "”的个人主页";
        mContent = "“" + nickName + "”入驻香哈菜谱啦，快来关注与Ta一起学习做菜吧！";
        mImgUrl = getIntent().getStringExtra("imgUrl");
        mType = ShareTools.IMG_TYPE_WEB;

        init();
    }

    private void init(){
        initData();

        findViewById(R.id.a_user_home_share_layout).setOnClickListener(this);
        findViewById(R.id.a_user_home_share_close).setOnClickListener(this);

        GridView mGridView = (GridView)findViewById(R.id.d_popwindow_share_gridview);
        SimpleAdapter adapter = new SimpleAdapter(this, mData,R.layout.a_user_home_share_item,
                new String[]{"img","name"},
                new int[]{R.id.share_logo,R.id.share_name});
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String platfrom = mSharePlatforms[position];
                XHClick.mapStat(UserHomeShare.this, tongjiId, "分享和举报", mData.get(position).get("name"));
                if("report".equals(platfrom)){ //举报
                    Intent intent = new Intent(UserHomeShare.this, QuanReport.class);
                    intent.putExtra("isQuan", "0");
                    intent.putExtra("nickName", "举报 " + nickName);
                    intent.putExtra("code", userCode);
                    intent.putExtra("repType", "4");
                    intent.putExtra("subjectCode", "0");
                    UserHomeShare.this.startActivity(intent);
                }else{
                    ShareTools barShare = ShareTools.getBarShare(UserHomeShare.this);
                    barShare.showSharePlatform(mTitle,mContent,mType,mImgUrl,mClickUrl,platfrom,mFrom,mParent);
                }
                UserHomeShare.this.finish();
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

    @Override
    public void onClick(View v) {
        UserHomeShare.this.finish();
    }
}
