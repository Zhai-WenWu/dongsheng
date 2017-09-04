package acore.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import third.share.tools.ShareTools;

public class PopWindowDialog {
	private Context mContext;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLayoutParams;
	private View mView;
	private ArrayList<Map<String,String>> mData = new ArrayList<>();
	private String[] mSharePlatforms;
	private String mType,mTitle,mClickUrl,mContent,mImgUrl,mFrom,mParent;
	private String mHintTitle,mShareHint,mMessage;
	
	public PopWindowDialog(Context context,String hintTitle,String shareHint,String message){
		mContext = context;
		mHintTitle = hintTitle;
		mShareHint= shareHint;
		mMessage = message;
		initData();
		init();
	}
	
	private void init(){
		LayoutInflater inflater = LayoutInflater.from(mContext);
		// Context.getSystemService(Context.WINDOW_SERVICE); 这个Context要用Application的context
		mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		mView = inflater.inflate(R.layout.d_popwindow, null);
		initShareView();
		TextView hintTitle = (TextView)mView.findViewById(R.id.d_popwindow_title);
		TextView message = (TextView)mView.findViewById(R.id.d_popwindow_message);
//		TextView shareHint = (TextView)mView.findViewById(R.id.d_popwindow_share_hint);

		hintTitle.setText(mHintTitle);
		if(TextUtils.isEmpty(mMessage)){
			message.setVisibility(View.GONE);
		}else{
			message.setText(mMessage);
			message.setVisibility(View.VISIBLE);
		}
//		shareHint.setText(mShareHint);

		mView.setOnClickListener(onCloseListener);
		mView.findViewById(R.id.d_popwindow_close).setOnClickListener(onCloseListener);
		mLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
//        //设置window的type
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //位置
        mLayoutParams.gravity = Gravity.CENTER;
//        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        layoutParams.height = Tools.getDimen(mContext, R.dimen.dp_100);
	}
	
	private void initShareView(){
		GridView mGridView = (GridView)mView.findViewById(R.id.d_popwindow_share_gridview);
		
		SimpleAdapter adapter = new SimpleAdapter(mContext, mData,R.layout.d_popwindow_share_item,
				new String[]{"img","name"},
				new int[]{R.id.share_logo,R.id.share_name});
		mGridView.setAdapter(adapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override 
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				String platfrom = mSharePlatforms[position];
				ShareTools barShare = ShareTools.getBarShare(mContext);
				barShare.showSharePlatform(mTitle,mContent,mType,mImgUrl,mClickUrl,platfrom,mFrom,mParent);
				closePopWindowDialog();
			}
		});
	}
	
	private void initData(){
		String[] mNames;
		int[] mLogos;
		if(ToolsDevice.isAppInPhone(mContext, "com.tencent.mm") == 0){
			mNames = new String[]{"QQ空间","QQ","新浪微博","信息","复制链接"};
			mLogos = new int[]{
					R.drawable.logo_qzone,R.drawable.logo_qq,
					R.drawable.logo_sina_weibo,R.drawable.logo_short_message,
					R.drawable.logo_copy
					};
			mSharePlatforms = new String[]{
					ShareTools.QQ_ZONE,ShareTools.QQ_NAME,
					ShareTools.SINA_NAME,ShareTools.SHORT_MESSAGE,
					ShareTools.LINK_COPY};
		}else{
			mNames = new String[]{"微信好友","微信朋友圈","QQ空间","QQ","新浪微博","信息","复制链接"};
			mLogos = new int[]{R.drawable.logo_wechat,R.drawable.logo_wechat_moments,
					R.drawable.logo_qzone,R.drawable.logo_qq,
					R.drawable.logo_sina_weibo,R.drawable.logo_short_message,
					R.drawable.logo_copy
					};
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
	
	public void show(String type,String title,String clickUrl,String content,String imgUrl,String from,String parent) {
		mType = type;mTitle = title;mClickUrl = clickUrl;mContent = content;mImgUrl = imgUrl;mFrom = from;mParent = parent;
		mWindowManager.addView(mView, mLayoutParams);
    }
	
//	public void show() {
//		mWindowManager.addView(mView, mLayoutParams);
//	}
	
	private OnClickListener onCloseListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			closePopWindowDialog();
		}
	};
	
	public void closePopWindowDialog(){
		if(mWindowManager != null){
			if(mView!=null)
				mWindowManager.removeView(mView);
			mWindowManager = null;
		}
	}
	
	/**
	 * 获取当前分享Dialog是否还在显示
	 * @return true:显示 fase：不显示
	 */
	public boolean isHasShow(){
		return mWindowManager != null;
	}
	
	public void onPause(){
		mView.setVisibility(View.GONE);
	}
	
	public void onResume(){
		mView.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 当分享成功或者发布菜谱成功后判断是否已满足每天显示两次，还需不需要显示
	 * @param dataKey ：用于保存日期的key
	 * @param numKey ： 用于保存次数的key
	 * @return
	 */
	public static boolean isShowPop(String dataKey,String numKey){
		String data = String.valueOf(FileManager.loadShared(XHApplication.in(), FileManager.xmlFile_shareShowPop, dataKey));
		String today = Tools.getAssignTime("yyyyMMdd", 0);
		boolean isShow = false;
		if(!TextUtils.isEmpty(data)){
			int dataNum = Integer.parseInt(data);
			int todayNum = Integer.parseInt(today);
			if(todayNum > dataNum){
				FileManager.saveShared(XHApplication.in(), FileManager.xmlFile_shareShowPop, dataKey,today);
				FileManager.saveShared(XHApplication.in(), FileManager.xmlFile_shareShowPop, numKey,"1");
				isShow = true;
			}else{
				String numStr = String.valueOf(FileManager.loadShared(XHApplication.in(), FileManager.xmlFile_shareShowPop, numKey));
				if(!TextUtils.isEmpty(numStr)){
					int num = Integer.parseInt(numStr);
					if(num > 1){
						isShow = false;
					}else{
						FileManager.saveShared(XHApplication.in(), FileManager.xmlFile_shareShowPop, numKey, String.valueOf(num + 1));
						isShow = true;
					}
				}else{
					FileManager.saveShared(XHApplication.in(), FileManager.xmlFile_shareShowPop, numKey,"1");
					isShow = true;
				}
			}
		}else{
			FileManager.saveShared(XHApplication.in(), FileManager.xmlFile_shareShowPop, dataKey,today);
			FileManager.saveShared(XHApplication.in(), FileManager.xmlFile_shareShowPop, numKey,"1");
			isShow = true;
		}
		return isShow;
	}
}
