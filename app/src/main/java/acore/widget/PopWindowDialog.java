package acore.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;

import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import third.share.adapter.ShareAdapter;
import third.share.module.ShareModule;
import third.share.tools.ShareTools;

public class PopWindowDialog extends Dialog {
	private Context mContext;
	private View mView;
	private ArrayList<ShareModule> mData = new ArrayList<>();
	private String[] mSharePlatforms;
	private String mType,mTitle,mClickUrl,mContent,mImgUrl,mFrom,mParent;
	private String mHintTitle,mShareHint,mMessage;

	private boolean mShowing;
	private View.OnClickListener onCloseListener;

	private boolean mShowIntegralTip;

	public PopWindowDialog(@NonNull Context context,String hintTitle,String shareHint,String message) {
		this(context, R.style.dialog);
		mContext = context;
		mHintTitle = hintTitle;
		mShareHint= shareHint;
		mMessage = message;
		initDefault();
	}

	public PopWindowDialog(@NonNull Context context,String hintTitle,String shareHint,String message, boolean showIntegralTip) {
		this(context, R.style.dialog);
		mContext = context;
		mHintTitle = hintTitle;
		mShareHint= shareHint;
		mMessage = message;
		mShowIntegralTip = showIntegralTip;
		initDefault();
	}

	private void initDefault() {
		initData();
		init();
		addListener();
	}

	private void addListener() {
		onCloseListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closePopWindowDialog();
			}
		};
		mView.setOnClickListener(onCloseListener);
		mView.findViewById(R.id.d_popwindow_close).setOnClickListener(onCloseListener);
	}

	public PopWindowDialog(@NonNull Context context, int themeResId) {
		super(context, themeResId);
	}

	protected PopWindowDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	private void init(){
		LayoutInflater inflater = LayoutInflater.from(mContext);
		mView = inflater.inflate(R.layout.d_popwindow, null);
		initShareView();
		TextView hintTitle = (TextView)mView.findViewById(R.id.d_popwindow_title);
		TextView message = (TextView)mView.findViewById(R.id.d_popwindow_message);
		hintTitle.setText(mHintTitle);
		if(TextUtils.isEmpty(mMessage)){
			message.setVisibility(View.GONE);
		}else{
			message.setText(mMessage);
			message.setVisibility(View.VISIBLE);
		}
		setContentView(mView);
	}

	private void initShareView(){
		GridView mGridView = (GridView)mView.findViewById(R.id.d_popwindow_share_gridview);

		ShareAdapter adapter = new ShareAdapter();
		adapter.setData(mData);
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
			String platform = mSharePlatforms[i];
			ShareModule module = new ShareModule();
			module.setResId(mLogos[i]);
			module.setTitle(mNames[i]);
			module.setIntegralTipShow(mShowIntegralTip && (TextUtils.equals(platform, ShareTools.WEI_XIN) || TextUtils.equals(platform, ShareTools.WEI_QUAN)));
			mData.add(module);
		}
	}

	public void show(String type,String title,String clickUrl,String content,String imgUrl,String from,String parent) {
		mType = type;mTitle = title;mClickUrl = clickUrl;mContent = content;mImgUrl = imgUrl;mFrom = from;mParent = parent;
		this.show();
		mShowing = true;
	}

	public void closePopWindowDialog(){
		this.dismiss();
		mShowing = false;
	}

	/**
	 * 获取当前分享Dialog是否还在显示
	 * @return true:显示 fase：不显示
	 */
	public boolean isHasShow(){
		return mShowing;
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
