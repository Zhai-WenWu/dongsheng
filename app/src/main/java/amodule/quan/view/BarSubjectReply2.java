/**
 * 贴子回复栏.
 * @author intBird 20140213.
 *
 */
package amodule.quan.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.xianghatest.R;
import com.xiangha.emojiutil.EmojiUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.quan.activity.FriendQuan;
import amodule.quan.activity.ShowSubject;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import core.xiangha.emj.tools.EmjParseMsgUtil;
import core.xiangha.emj.view.EditTextShow;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilLog;
import xh.basic.tool.UtilString;

import static amodule.quan.activity.FriendQuan.REQUEST_CODE_QUAN_FRIEND;
import static android.app.Activity.RESULT_OK;

public class BarSubjectReply2 extends RelativeLayout implements OnClickListener{

	private ImageButton ib_addEmoji,ib_addFren;
	private EditTextShow et_reply;
	private Button btn_reply;
	private RelativeLayout rl_emoji;
	private Activity mAct;
	private Handler quanHandler;
	private String mSubjectCode,mFoorId,mFloorNum,userCode,lcCode;
	private View mView;
	private String mParam;
	private int what;
	private ProgressBar progressBar;
	private RelativeLayout activityLayout;

	private boolean isEmoji = false;


	public BarSubjectReply2(Context context, AttributeSet attrs) {
		super(context, attrs);
		mView = LayoutInflater.from(context).inflate(R.layout.c_view_bar_subject_reply_lc, this);
		ib_addEmoji = (ImageButton) mView.findViewById(R.id.ib_addEmoji);
		ib_addFren = (ImageButton) mView.findViewById(R.id.ib_addFren);
		et_reply = (EditTextShow) mView.findViewById(R.id.et_reply);
		btn_reply = (Button)mView.findViewById(R.id.btn_reply);
		rl_emoji = (RelativeLayout) mView.findViewById(R.id.rl_emoji);
		progressBar = (ProgressBar) mView.findViewById(R.id.send_progressBar);

		hide();
	}

	/**
	 * 初始化
	 * @param act
	 * @param handler
	 * @param subjectCode
	 */
	@SuppressLint("HandlerLeak")
	public void initView(Activity act,Handler handler,String subjectCode,RelativeLayout activity) {
		mAct = act;
		quanHandler = handler;
		mSubjectCode = subjectCode;
		ib_addEmoji.setOnClickListener(this);
		ib_addFren.setOnClickListener(this);
		btn_reply.setOnClickListener(this);
		this.activityLayout = activity;
		et_reply.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setEmoji(false);
			}
		});
		EmojiUtil mEmojiUtil = new EmojiUtil(mAct);
		mEmojiUtil.setEditTextShow(et_reply);
		rl_emoji.addView(mEmojiUtil.getEmojiView());
		//处理状态栏引发的问题
		if (Tools.isShowTitle()) {
			activityLayout.getViewTreeObserver().addOnGlobalLayoutListener(
					new ViewTreeObserver.OnGlobalLayoutListener() {
						public void onGlobalLayout() {
							int heightDiff = activityLayout.getRootView().getHeight() - activityLayout.getHeight();
							Rect r = new Rect();
							activityLayout.getWindowVisibleDisplayFrame(r);
							int screenHeight = activityLayout.getRootView().getHeight();
							int heightDifference = screenHeight - (r.bottom - r.top);
							if (heightDifference > 200) {
								heightDifference = heightDifference - heightDiff;

							} else {
								heightDifference = 0;
							}
							BarSubjectReply2.this.setPadding(0, 0, 0, heightDifference);

						}
					});
		}
	}
	/**
	 * 显示控件,回复楼层<包括楼中楼>
	 */
	public void show(String floorId,String floorNum,String code,String nickName,String louCengCode) {
		mFoorId = floorId;
		mFloorNum = floorNum;
		userCode = code;
		lcCode = louCengCode;
		et_reply.setHint("回复" + floorNum + "楼 " + nickName + ":");
		mParam = "type=comment&subjectCode=" + mSubjectCode + "&customerCode=" + userCode + "&floorId=" + mFoorId;
		what = ShowSubject.REPLY_LOU_OVER;
		mView.setVisibility(View.VISIBLE);
		keybroadShow(true);
	}
	/**
	 * 显示控件,回复楼主
	 */
	public void show() {
		mFloorNum = "0";
		mFoorId = "0";
		et_reply.setHint("回复楼主");
		mParam = "type=floor&subjectCode=" + mSubjectCode;
		what = ShowSubject.REPLY_LZ_OVER;
		mView.setVisibility(View.VISIBLE);
		keybroadShow(true);
	}

	public void hide(){
		mView.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.ib_addEmoji:
				addEmoji();
				break;
			case R.id.ib_addFren:
				// 没有登录,去登录
				if (!LoginManager.isLogin()) {
					//添加登录
					Intent intent = new Intent(mAct,LoginByAccout.class);
					mAct.startActivity(intent);
					return;
				}
				Intent intent = new Intent(mAct, FriendQuan.class);
				intent.putExtra("Activity", "quanReplay");
				intent.putExtra("value", et_reply.getText().toString());
				mAct.startActivityForResult(intent, REQUEST_CODE_QUAN_FRIEND);
				break;
			case R.id.btn_reply:
				replay();
				break;
		}
	}
	/**
	 * @完后,调用此方法解析,添加上去
	 * @param data
	 */
	private void editAdd(String data) {
		ArrayList<Map<String, String>> list = UtilString
				.getListMapByJson(data);
		int index = et_reply.getSelectionStart();
		et_reply.setFriends(index, list);
		keybroadShow(true);
	}

	private void replay(){
//		keybroadShow(false);

		// 没有登录,去登录
		if (!LoginManager.isLogin()) {
			//添加登录
			Intent intent = new Intent(mAct,LoginByAccout.class);
			mAct.startActivity(intent);
			return;
		}
		// 没有回复文字,输入文字
		if (et_reply.getText().toString().trim().length() == 0) {
			Tools.showToast(mAct, "请输入回复内容");
			return;
		}
		// 如果上一条回复还在发布,等待
		if (!btn_reply.isEnabled()) {
			Tools.showToast(mAct, "上一条回复还在发布中哦~");
			return;
		}
		// 开始回复
		btn_reply.setText("");
		progressBar.setVisibility(View.VISIBLE);

//		Toast.makeText(mAct, "正在回复,请稍后~", Toast.LENGTH_SHORT).show();
		et_reply.setFocusable(false);
		btn_reply.setEnabled(false);
		final String myFloorNum = mFloorNum;
		final String myFoorId = mFoorId;
		final String myLcCode = lcCode;
		String url = null;
		LinkedHashMap<String, String> params = null;
		if(what==ShowSubject.REPLY_LOU_OVER){//楼层
			url=StringManager.api_quanSetSubject;
//			params=mParam+ "&comment="+ et_reply.getURLEncoder();
			params= getLOULinkedMapParams();
		}else if(what==ShowSubject.REPLY_LZ_OVER){//楼主
			url=StringManager.api_uploadFloor;
			params= getLZLinkedMapParams();

		}
		try {
			ReqInternet.in().doPost(url, params, new InternetCallback(mAct){

				@Override
				public void loaded(int flag, String url, Object returnObj) {
					et_reply.setFocusableInTouchMode(true);
					et_reply.requestFocus();
					if (flag >= UtilInternet.REQ_OK_STRING) {
						if(StringManager.api_uploadFloor.equals(url)){
							XHClick.track(mAct,"美食贴评论");
						}
						keybroadShow(false);
						et_reply.setText("");
						btn_reply.setEnabled(true);
//						Tools.showToast(mAct,"回复成功啦~");
						Map<String, String> map = new HashMap<String, String>();
						map.put("returnObj", returnObj.toString());
						map.put("floorNum", myFloorNum);
						map.put("foorId", myFoorId);
						map.put("lcCode", myLcCode);
//						Log.i("bar:222::", what+"");
						Message msg = quanHandler.obtainMessage(what, map);
						quanHandler.sendMessage(msg);
						progressBar.setVisibility(View.GONE);
						btn_reply.setText("发送");
					} else {
						btn_reply.setEnabled(true);
						progressBar.setVisibility(View.GONE);
						btn_reply.setText("发送");
					}
				}

			});
		} catch (Exception e) {
			UtilLog.reportError("回复出错", e);
			et_reply.setFocusableInTouchMode(true);
			et_reply.requestFocus();
			btn_reply.setEnabled(true);
			Tools.showToast(mAct.getApplicationContext(), "回复失败");
			progressBar.setVisibility(View.GONE);
			btn_reply.setText("发送");
		}
	}

	// 弹出或隐藏键盘,外部可调用隐藏键盘;
	public void keybroadShow(boolean isShow) {
		if (isShow) {
			InputMethodManager inputMethodManager1 = (InputMethodManager) mAct.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager1.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
			et_reply.setFocusableInTouchMode(true);
			et_reply.requestFocus();
			setEmoji(false);
//			Tools.keyboardControl(true, mAct, et_reply);
		} else {
			ToolsDevice.keyboardControl(false, mAct, et_reply);
			setEmoji(false);
		}
	}

	private void addEmoji() {
		if (!isEmoji) {
			et_reply.setFocusableInTouchMode(true);
			et_reply.requestFocus();
			keybroadShow(false);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					setEmoji(true);
				}
			}, 300);
		} else {
			setEmoji(false);
			keybroadShow(true);
		}
	}

	// 设置Emoji表情显示或隐藏
	public void setEmoji(boolean isVisible) {
		if (isVisible) {
			ib_addEmoji.setImageResource(R.drawable.z_quan_tie_menu_ico_tab);
			rl_emoji.setVisibility(View.VISIBLE);
			isEmoji = true;
		} else {
			ib_addEmoji.setImageResource(R.drawable.z_quan_tie_menu_ico_face);
			rl_emoji.setVisibility(View.GONE);
			isEmoji = false;
		}
	}

	// 替换所有<img>标签，转换成unicode编码,供发送到服务器
	public String getUnicodeText() {
		String senStr = EmjParseMsgUtil.convertToMsg(mAct,et_reply.getEditableText());
		return senStr;
	}

	/**
	 * 获取参数
	 * @return
	 */
	private String getparams(){
		String params="location="+"&code="+mSubjectCode+"&text[0]="+et_reply.getURLEncoder()
				+"&img[0]="+"&isLocation=";
		return params;
	}
	private LinkedHashMap<String, String> getLZLinkedMapParams(){
		LinkedHashMap<String, String> linkhashmap= new LinkedHashMap<String, String>();
		linkhashmap.put("location", "");
		linkhashmap.put("code", mSubjectCode);
		linkhashmap.put("text[0]", getUnicodeText(et_reply));
		linkhashmap.put("img[0]", "");
		linkhashmap.put("isLocation", "");
		return linkhashmap;
	}

	private LinkedHashMap<String, String> getLOULinkedMapParams(){
		LinkedHashMap<String, String> linkhashmap= new LinkedHashMap<String, String>();
		linkhashmap.put("type", "comment");
		linkhashmap.put("subjectCode", mSubjectCode);
		linkhashmap.put("customerCode", userCode);
		linkhashmap.put("floorId", mFoorId);
		linkhashmap.put("comment", et_reply.getURLEncoder());
		return linkhashmap;
	}

	/**
	 * 替换所有<img>标签，转换成unicode编码,供发送到服务器
	 *
	 * @return
	 */
	private String getUnicodeText(EditTextShow edit) {
		String senStr = EmjParseMsgUtil.convertToMsg(mAct, edit.getEditableText());
		return senStr;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK && data != null) {
			switch (requestCode) {
				case REQUEST_CODE_QUAN_FRIEND:
						String listData = data.getStringExtra(FriendQuan.FRIENDS_LIST_RESULT);
						editAdd(listData);
					break;
			}
		}
	}
}
