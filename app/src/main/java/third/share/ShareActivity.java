package third.share;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.tools.ToolsDevice;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.xiangha.R;

public class ShareActivity extends Activity{
	private GridView mGridView;
	private ArrayList<Map<String,String>> mData = new ArrayList<Map<String,String>>();
	private String mType,mTitle,mClickUrl,mContent,mImgUrl,mFrom,mParent;
	
	public static String IMG_TYPE_WEB="web";
	public static String IMG_TYPE_RES="res";
	public static String IMG_TYPE_LOC="loc";
	private String[] mNames ;
	private int[] mLogos ;
	private String[] mSharePlatforms ;

	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		Intent it = getIntent();
		Bundle bundle = it.getExtras();
		if(bundle != null){
			try{
				mType = bundle.getString("type");
				mTitle = bundle.getString("title");
				mClickUrl = bundle.getString("clickUrl");
				mContent = bundle.getString("content");
				mImgUrl = bundle.getString("imgUrl");
				mFrom = bundle.getString("from");
				mParent = bundle.getString("parent");
			}catch(Exception e){}
		}
		
		mTitle = mTitle.subSequence(0, mTitle.length() > 30 ? 30 : mTitle.length()).toString();
		mContent = mContent.subSequence(0, mContent.length() > 30 ? 30 : mContent.length()).toString();
		
		setContentView(R.layout.share); 
		
		initData();
		initView();
	}
	
	private void initView(){
		findViewById(R.id.share_container).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ShareActivity.this.finish();
			}
		});

		mGridView = (GridView)findViewById(R.id.share_gridview);
		
		SimpleAdapter adapter = new SimpleAdapter(this, mData, 
				R.layout.share_item,
				new String[]{"img","name"},
				new int[]{R.id.share_logo,R.id.share_name});
		mGridView.setAdapter(adapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override 
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				String platfrom = mSharePlatforms[position];
				ShareTools barShare = ShareTools.getBarShare(ShareActivity.this);
				barShare.showSharePlatform(mTitle,mContent,mType,mImgUrl,mClickUrl,platfrom,mFrom,mParent);
				ShareActivity.this.finish();
			}
		});
	}
	
	private void initData(){
		if(ToolsDevice.isAppInPhone(this, "com.tencent.mm") == 0){
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
			mData.add(map);
		}
	}
	
}
