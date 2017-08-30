
package third.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.ToolsDevice;

import static amodule.dish.activity.DetailDish.tongjiId;

/**
 * Created by Administrator on 2016/8/5.
 */
public class ShareImageActivity extends Activity{
    public static final String EXTRA_CONTENT = "content";
    public static final String EXTRA_IMAGE= "img";
    private ArrayList<Map<String,String>> mData = new ArrayList<>();
    private String[] mNames ;
    private int[] mLogos ;
    private String[] mSharePlatforms ;
    private String mContent,mImgUrl;

    public static void openShareImageActivity(Context context,String content,@NonNull String imageUrl){
        Intent intent = new Intent(context,ShareImageActivity.class);
        if(!TextUtils.isEmpty(content)){
            intent.putExtra(EXTRA_CONTENT,content);
        }
        if(!TextUtils.isEmpty(imageUrl)){
            intent.putExtra(EXTRA_IMAGE,imageUrl);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_share_dialog);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            try{
                mContent = bundle.getString(EXTRA_CONTENT);
                mImgUrl = bundle.getString(EXTRA_IMAGE);
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
                XHClick.mapStat(ShareImageActivity.this, tongjiId, "分享", mNames[position]);
                new ShareImage(ShareImageActivity.this).share(mSharePlatforms[position],mImgUrl,mContent);
                ShareImageActivity.this.finish();
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
            mNames = new String[]{"QQ好友","QQ空间","新浪微博","短信"};
            mLogos = new int[]{
                    R.drawable.logo_qq_new,R.drawable.logo_space_new,
                    R.drawable.logo_sina_new,R.drawable.logo_message_new};
            mSharePlatforms = new String[]{
                    ShareImage.QQ_NAME,ShareImage.QQ_ZONE,
                    ShareImage.SINA_NAME,ShareImage.SHORT_MESSAGE};
        }else{
            mNames = new String[]{"微信好友","微信朋友圈","QQ好友","QQ空间","新浪微博","短信"};
            mLogos = new int[]{R.drawable.logo_weixin_new,R.drawable.logo_friends_new,
                    R.drawable.logo_qq_new,R.drawable.logo_space_new,
                    R.drawable.logo_sina_new,R.drawable.logo_message_new};
            mSharePlatforms = new String[]{
                    ShareImage.WEI_XIN,ShareImage.WEI_QUAN,
                    ShareImage.QQ_NAME,ShareImage.QQ_ZONE,
                    ShareTools.SINA_NAME,ShareImage.SHORT_MESSAGE};
        }

        int mapSize = mNames.length;
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
