package amodule.quan.tool;

import java.util.HashMap;
import java.util.Map;

import third.share.ShareTools;
import acore.tools.StringManager;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.xianghatest.R;

public class UploadSubjectShareControl{
	private Context mCon;
	private View mViewParent;
	private Map<String,Boolean> shareToFlag = null;
	
	public UploadSubjectShareControl(Context context,View viewParent){
		mCon = context;
		mViewParent = viewParent;
		shareToFlag = new HashMap<String, Boolean>();
		shareToFlag.put("kj", false);
		shareToFlag.put("pyq", false);
		shareToFlag.put("bk", false);
		mViewParent.findViewById(R.id.shareToKJ).setOnClickListener(onClickListener);
		mViewParent.findViewById(R.id.shareToPYQ).setOnClickListener(onClickListener);
		mViewParent.findViewById(R.id.shareToBK).setOnClickListener(onClickListener);
	}
	
	private OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.shareToKJ:
				shareToFlag.put("kj",!shareToFlag.get("kj"));
				((ImageView)v.findViewById(R.id.shareToKJ)).setBackgroundResource(shareToFlag.get("kj")?
						R.drawable.z_quan_send_share_ico_qq_active:R.drawable.z_quan_send_share_ico_qq);
//				}
				break;
			case R.id.shareToPYQ:
				shareToFlag.put("pyq",!shareToFlag.get("pyq"));
				((ImageView)v.findViewById(R.id.shareToPYQ)).setBackgroundResource(shareToFlag.get("pyq")?
						R.drawable.z_quan_send_share_ico_weixin_active:R.drawable.z_quan_send_share_ico_weixin);
//				}
				break;
			case R.id.shareToBK:
				shareToFlag.put("bk",!shareToFlag.get("bk"));
				((ImageView)v.findViewById(R.id.shareToBK)).setBackgroundResource(shareToFlag.get("bk")?
						R.drawable.z_quan_send_share_ico_weibo_active:R.drawable.z_quan_send_share_ico_weibo);
			}
		}
	};
	
	public void starShare(String subCode,String title,String content,String shareImg,String imgType){
		String clickUrl = StringManager.wwwUrl + "quan/" + subCode;
//		String clickUrl = "http://www.xiangha.com/quan/1052050.html";
		boolean isHavaPYQ = false;
		boolean isShowBeginToast = true;
		if(shareToFlag.get("bk")){
			ShareTools barShare = ShareTools.getBarShare(mCon);
			barShare.showSharePlatform(title,content,imgType,shareImg,clickUrl,ShareTools.SINA_NAME,"发贴","",isShowBeginToast);
			isShowBeginToast = false;
		}
		if(shareToFlag.get("pyq")){
			isHavaPYQ = true;
			ShareTools barShare = ShareTools.getBarShare(mCon);
			barShare.showSharePlatform(title,content,imgType,shareImg,clickUrl,ShareTools.WEI_QUAN,"发贴","",isShowBeginToast);
			isShowBeginToast = false;
		}
		if(shareToFlag.get("kj")){
			if(!isHavaPYQ){
				ShareTools barShare = ShareTools.getBarShare(mCon);
				barShare.showSharePlatform(title,content,imgType,shareImg,clickUrl,ShareTools.QQ_ZONE,"发贴","",isShowBeginToast);
			}
		}
//		LogManager.print("d", "toastStr:" + toastStr);
//		if(toastStr != ""){
////			Intent it = new Intent(this,DialogUploadOk.class);
////			startActivity(it);
//			Toast.makeText(this, "贴子已发布成功啦，正在为您同步到" + toastStr, 1000).show();
//		}
	}
}
