package amodule.quan.view;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xianghatest.R;
import com.xiangha.emojiutil.EmojiUtil;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.ToolsDevice;
import amodule.quan.activity.FriendQuan;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.login.LoginByAccout;
import core.xiangha.emj.tools.EmjParseMsgUtil;
import core.xiangha.emj.tools.EmjParser;
import core.xiangha.emj.view.EditTextShow;

import static amodule.quan.activity.FriendQuan.REQUEST_CODE_QUAN_FRIEND;

public class BarUploadSubjectNew {
	private BaseActivity mAct;
	private BarUploadSubInterface mBarInterface;
	private View mParentView;
	private ImageButton ib_seting;
	private EditTextShow mEdContent;

	private EmojiUtil mEmojiUtil;
	private ImageView switchButton;
	private RelativeLayout rl_emoji = null,rl_seting = null;
	private boolean isEmoji = false,isSeting = false,setingIsClick = false,isChange = true;;

	public BarUploadSubjectNew(BaseActivity act,BarUploadSubInterface barInterface) {
		mAct = act;
		mBarInterface = barInterface;
		mParentView = mAct.findViewById(R.id.quan_bar_subject_reply);
		init();
	}

	private void init(){
		mAct.findViewById(R.id.quan_bar_ll_emojiBar).setOnClickListener(onClickListener);
		ib_seting = (ImageButton) mAct.findViewById(R.id.ib_seting);
		rl_emoji = (RelativeLayout) mAct.findViewById(R.id.quan_bar_rl_emoji);
		rl_seting = (RelativeLayout) mAct.findViewById(R.id.quan_bar_rl_seting);
		switchButton = (ImageView)mAct.findViewById(R.id.quan_bar_switch_button_setting);
		// 添加Emoji表情
		mAct.findViewById(R.id.quan_bar_ib_addEmoji).setOnClickListener(onClickListener);
		ib_seting.setOnClickListener(onClickListener);
		// 添加@人
		ImageButton addFrends = (ImageButton) mAct.findViewById(R.id.quan_bar_ib_addFrend);
		addFrends.setOnClickListener(onClickListener);
		mAct.findViewById(R.id.ib_addImg).setOnClickListener(onClickListener);
		
		mEmojiUtil = new EmojiUtil(mAct);
		rl_emoji.addView(mEmojiUtil.getEmojiView());
		switchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isChange = !isChange;
				if(isChange && !LoginManager.isLogin()){
					isChange = !isChange;
					Intent intent = new Intent(mAct, LoginByAccout.class);
					mAct.startActivity(intent);
				}else{
					setSwitchButton(isChange);
					FileManager.setSharedPreference(mAct, FileManager.xmlKey_uploadDishSetingIsShow,isChange?"true":"false");
				}
			}
		});
		boolean isShowSeting = true;
		String[] localIsShow = FileManager.getSharedPreference(mAct, FileManager.xmlKey_uploadDishSetingIsShow);
		if(localIsShow != null && localIsShow.length > 1){
			if("false".equals(localIsShow[1])){
				isShowSeting = false;
			}
		}
		if(!isShowSeting || !LoginManager.isLogin()){
			setSwitchButton(false);
			isChange = false;
		}
	}
	
	
	
	public void showBar(EditTextShow ed_content){
		mEdContent = ed_content;
		mParentView.setVisibility(View.VISIBLE);
		setEmoji(true);
		mEmojiUtil.setEditTextShow(mEdContent);
		mEdContent.addTextChangedListener(new TextWatcher() {

			private boolean isNew = true;
			private int index = -1, beginL = 0;

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (isNew) {
					String str = EmjParser.getInstance(mAct).parseEmoji(
							s.toString());
					// 有表情
					if (s.toString().length() != str.length()) {
						CharSequence c2 = EmjParseMsgUtil.convetToHtml(mAct,str);
						// 添加字则光标向后移动，删除则不动
						index = start
								+ (c2.length() > beginL ? c2.length() - beginL
										: 0);
						isNew = false;
						mEdContent.setText(c2);
					} else
						index = -1;
				} else {
					isNew = true;
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
				beginL = s.length();
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (isNew && index > -1 && index < s.length())
					mEdContent.setSelection(index);
			}
		});
		setEmoji(false);
	}
	public void hindBar(){
		mParentView.setVisibility(View.GONE);
		setEmoji(false);
		setSeting(false);
	}
	
	/**
	 *  替换所有<img>标签，转换成unicode编码,供发送到服务器
	 * @return
	 */
	public String getUnicodeText(EditTextShow edit) {
		String senStr = EmjParseMsgUtil.convertToMsg(mAct,edit.getEditableText());
		return senStr;
	}
	
	
	// 设置Emoji表情显示或隐藏
	public void setEmoji(boolean isVisible) {
		if (isVisible) {
			rl_emoji.setVisibility(View.VISIBLE);
			isEmoji = true;
			mAct.findViewById(R.id.a_emoji_pop).setVisibility(View.VISIBLE);
		} else {
			mAct.findViewById(R.id.a_emoji_pop).setVisibility(View.GONE);
			rl_emoji.setVisibility(View.GONE);
			isEmoji = false;
		}
	}
	
	public void setSeting(boolean isVisible){
		if (isVisible) {
			rl_seting.setVisibility(View.VISIBLE);
			isSeting = true;
			mAct.findViewById(R.id.a_seting_pop).setVisibility(View.VISIBLE);
		} else {
			mAct.findViewById(R.id.a_seting_pop).setVisibility(View.GONE);
			rl_seting.setVisibility(View.GONE);
			isSeting = false;
		}
	}
	
	private void setSwitchButton(boolean isVisible){
		mBarInterface.onWatermarkClick(isVisible);
		if(isVisible){
			switchButton.setImageResource(R.drawable.z_quan_tie_menu_ico_camera_switch_ok);
		}else{
			switchButton.setImageResource(R.drawable.z_quan_tie_menu_ico_camera_switch_cancle);
		}
	}
	
	// 弹出或隐藏键盘,外部可调用隐藏键盘;
	private void keybroadShow(boolean isShow) {
		if (isShow) {
			ToolsDevice.keyboardControl(true, mAct, mEdContent);
		} else {
			setEmoji(false);
			setSeting(false);
			ToolsDevice.keyboardControl(false, mAct, mEdContent);
		}
	}

	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:
				setEmoji(true);
				break;
			case 1:
				setSeting(true);
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	private OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.quan_bar_ib_addEmoji:
				XHClick.mapStat(mAct, UploadSubjectNew.mTongjiId, "底部点击", "表情");
				addEmoji();
				break;
			case R.id.quan_bar_ib_addFrend:
				XHClick.mapStat(mAct, UploadSubjectNew.mTongjiId, "底部点击", "@点击");
				Intent intent = new Intent(mAct, FriendQuan.class);
				intent.putExtra("Activity", "UploadS");
				intent.putExtra("value", mEdContent.getText().toString());
				mAct.startActivityForResult(intent, REQUEST_CODE_QUAN_FRIEND);
				break;
			case R.id.ib_addImg:
				XHClick.mapStat(mAct, UploadSubjectNew.mTongjiId, "底部点击", "图片");
				mBarInterface.onAddImgClick();
				break;
			case R.id.ib_seting:
				XHClick.mapStat(mAct, UploadSubjectNew.mTongjiId, "底部点击", "设置");
				if(setingIsClick){
					setingIsClick = false;
					ib_seting.setImageResource(R.drawable.z_quan_tie_menu_ico_camera_seting);
				}else{
					setingIsClick = true;
					ib_seting.setImageResource(R.drawable.z_quan_tie_menu_ico_camera_seting_cancel);
				}
				addSeting();
				break;
			/*case R.id.quan_bar_ll_emojiBar:
				hindBar();
				break;*/
			}
		}
	};
	
	private void addEmoji(){
		if (!isEmoji) {
			mEdContent.setFocusableInTouchMode(true);
			mEdContent.requestFocus();
			keybroadShow(false);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					myHandler.sendEmptyMessage(0);
				}
			}, 300);
		} else {
			setEmoji(false);
			keybroadShow(true);
		}
	}
	
	private void addSeting(){
		if(!isSeting){
			mEdContent.setFocusableInTouchMode(true);
			mEdContent.requestFocus();
			keybroadShow(false);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					myHandler.sendEmptyMessage(1);
				}
			}, 300);
		}else {
			setSeting(false);
			keybroadShow(true);
		}
	}
	
	public interface BarUploadSubInterface{
		public void onAddImgClick();
		public void onWatermarkClick(boolean state);
	}
	
	public boolean onBackPressed(){
		if(rl_emoji.getVisibility() == View.VISIBLE){
			setEmoji(false);
			return true;
		}else if(rl_seting.getVisibility() == View.VISIBLE){
			setSeting(false);
			return true;
		}
		return false;
	}
}
