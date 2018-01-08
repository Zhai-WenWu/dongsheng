package amodule.user.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.popdialog.util.PushManager;
import com.xiangha.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.widget.SwitchView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.tool.UtilFile;

/**
 * 消息通知设置
 * @author luomin
 */
public class MyMsgInformSetting extends BaseActivity implements View.OnClickListener, SwitchView.OnSwitchChangeListener{
	private SwitchView msg_new;//接收全部消息通知 tag:tag_new
	private SwitchView msg_comments;//有人给我评论 tag:tag_comments
	private SwitchView msg_good;//有人给我点赞  tag:tag_good
	private SwitchView msg_feedback;//香哈小秘书通知  tag:tag_feedback
	private SwitchView msg_qa;//有问答消息  tag:tag_qa
	private SwitchView msg_dianshang;//电商客服消息  tag:tag_kefu
	private SwitchView msg_interesting;//你可能感兴趣的内容  tag:tag_interesting

	private LinearLayout msgInform_ll;//消息开关列表的布局
	private LinearLayout msgTipStart;//提示开启消息的布局
	private Button start_btn;

	private final String tag_new = "tag_new";
	private final String tag_comments = "tag_comments";
	private final String tag_good = "tag_good";
	private final String tag_feedback = "tag_feedback";
	private final String tag_qa = "tag_qa";
	private final String tag_kefu = "tag_kefu";
	private final String tag_interesting = "tag_interesting";

	private Map<String, String> mData;

	private boolean mNeedCheckStatus;
	private boolean mNewMsgOpen;
	private boolean mNewMsgOpenTemp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("通知设置", 2, 0, R.layout.c_view_bar_title, R.layout.a_my_msginform);
		initView();
		initMsgNewStatus();
		setListener();
		getData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mNeedCheckStatus) {
			mNeedCheckStatus = false;
			checkStatusChanged();
		}
	}

	private void checkStatusChanged() {
		boolean isEnabled = PushManager.isNotificationEnabled(this);
		setViewStatus(msg_new, isEnabled);
		if (mNewMsgOpenTemp == isEnabled) {
			return;
		}
		mNewMsgOpenTemp = isEnabled;
		setViewStatus(msg_comments, isEnabled);
		setViewStatus(msg_good, isEnabled);
		setViewStatus(msg_feedback, isEnabled);
		setViewStatus(msg_qa, isEnabled);
		setViewStatus(msg_dianshang, isEnabled);
		setViewStatus(msg_interesting, isEnabled);

		setDataMap("comments", isEnabled);
		setDataMap("good", isEnabled);
		setDataMap("feedback", isEnabled);
		setDataMap("qa", isEnabled);
		setDataMap("kefu", isEnabled);
		setDataMap("interesting", isEnabled);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void finish() {
		super.finish();
		saveNewMsgStatus();
		pushDataToService();
	}
	/**
	 * 开关状态改变监听
	 */
	private void setListener() {
		msg_new.setOnChangeListener(this);
		msg_comments.setOnChangeListener(this);
		msg_good.setOnChangeListener(this);
		msg_feedback.setOnChangeListener(this);
		msg_qa.setOnChangeListener(this);
		msg_dianshang.setOnChangeListener(this);
		msg_interesting.setOnChangeListener(this);
		start_btn.setOnClickListener(this);
	}

	private void initView() {
		View msgNew = findViewById(R.id.msg_new);
		TextView title1 = (TextView) msgNew.findViewById(R.id.title);
		title1.setText("接收新消息通知");
		msg_new = (SwitchView) msgNew.findViewById(R.id.switch_btn);
		msg_new.setTag(tag_new);
		View msgComments = findViewById(R.id.msg_comments);
		TextView title2 = (TextView) msgComments.findViewById(R.id.title);
		title2.setText("有人给我评论");
		msg_comments = (SwitchView) msgComments.findViewById(R.id.switch_btn);
		msg_comments.setTag(tag_comments);
		msg_comments.setState(true);
		View msgGood = findViewById(R.id.msg_good);
		TextView title3 = (TextView) msgGood.findViewById(R.id.title);
		title3.setText("有人给我点赞");
		msg_good = (SwitchView) msgGood.findViewById(R.id.switch_btn);
		msg_good.setTag(tag_good);
		msg_good.setState(true);
		View msgFeedback = findViewById(R.id.msg_feedback);
		TextView title4 = (TextView) msgFeedback.findViewById(R.id.title);
		title4.setText("香哈小秘书通知");
		msg_feedback = (SwitchView) msgFeedback.findViewById(R.id.switch_btn);
		msg_feedback.setTag(tag_feedback);
		msg_feedback.setState(true);
		View msgQa = findViewById(R.id.msg_qa);
		TextView title5 = (TextView) msgQa.findViewById(R.id.title);
		title5.setText("有问答消息");
		msg_qa = (SwitchView) msgQa.findViewById(R.id.switch_btn);
		msg_qa.setTag(tag_qa);
		msg_qa.setState(true);
		View msgDianshang = findViewById(R.id.msg_dianshang);
		TextView title6 = (TextView) msgDianshang.findViewById(R.id.title);
		title6.setText("电商客服消息");
		msg_dianshang = (SwitchView) msgDianshang.findViewById(R.id.switch_btn);
		msg_dianshang.setTag(tag_kefu);
		msg_dianshang.setState(true);
		View msgInteresting = findViewById(R.id.msg_interesting);
		TextView title7 = (TextView) msgInteresting.findViewById(R.id.title);
		title7.setText("你可能感兴趣的内容");
		msg_interesting = (SwitchView) msgInteresting.findViewById(R.id.switch_btn);
		msg_interesting.setTag(tag_interesting);
		msg_interesting.setState(true);
		msgInform_ll = (LinearLayout) findViewById(R.id.msgInform_ll);
		msgTipStart = (LinearLayout) findViewById(R.id.tip_start_layout);
		start_btn = (Button) findViewById(R.id.start_btn);
	}

	private void initMsgNewStatus() {
		String status = (String) UtilFile.loadShared(getApplicationContext(), FileManager.msgInform, FileManager.newMSG);
		mNewMsgOpen = TextUtils.equals("1", status);
		msg_new.setState(mNewMsgOpen);
	}

	private void saveNewMsgStatus() {
		UtilFile.saveShared(getApplicationContext(), FileManager.msgInform, FileManager.newMSG, msg_new.mSwitchOn ? "1" : "2");
	}

	private void getData() {
		if (!msg_new.mSwitchOn) {
			onDataReady();
			return;
		}
		if (loadManager != null)
			loadManager.showProgressBar();
		ReqInternet.in().doPost(StringManager.API_GETINFOSWITCHLIST, "", new InternetCallback() {
			@Override
			public void loaded(int i, String s, Object o) {
				if (i >= ReqInternet.REQ_OK_STRING) {
					mData = StringManager.getFirstMap(o);
				}
				onDataReady();
			}
		});
	}

	private void onDataReady() {
		if (loadManager != null)
			loadManager.hideProgressBar();
		if (mData == null || mData.isEmpty()) {
			showStarTip(true);
		} else {
			setViewStatus(msg_comments, TextUtils.equals("1", mData.get("comments")));
			setViewStatus(msg_good, TextUtils.equals("1", mData.get("good")));
			setViewStatus(msg_feedback, TextUtils.equals("1", mData.get("feedback")));
			setViewStatus(msg_qa, TextUtils.equals("1", mData.get("qa")));
			setViewStatus(msg_dianshang, TextUtils.equals("1", mData.get("kefu")));
			setViewStatus(msg_interesting, TextUtils.equals("1", mData.get("interesting")));
			showInfoList(true);
		}
	}

	private void pushDataToService() {
		ReqInternet.in().doPost(StringManager.API_SETINFOSWITCH, combineData(), new InternetCallback() {
			@Override
			public void loaded(int i, String s, Object o) {

			}
		});
	}

	private String combineData() {
		String ret = "";
		if (mData == null || mData.isEmpty())
			return ret;
		JSONObject object = new JSONObject();
		JSONObject objectList = new JSONObject();
		try {
			objectList.put("comments", mData.get("comments"));
			objectList.put("good", mData.get("good"));
			objectList.put("feedback", mData.get("feedback"));
			objectList.put("qa", mData.get("qa"));
			objectList.put("kefu", mData.get("kefu"));
			objectList.put("interesting", mData.get("interesting"));
			object.put("list", objectList);
			ret = object.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private void showInfoList(boolean show) {
		msgInform_ll.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	private void showStarTip(boolean show) {
		msgTipStart.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	private void setViewStatus(SwitchView view, boolean open) {
		view.setState(open);
	}

	private void setDataMap(String key, boolean open) {
		if (mData == null)
			return;
		mData.put(key, open ? "1" : "2");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.start_btn:
				mNeedCheckStatus = true;
				startOpenNotify();
				break;
		}
	}

	private void startOpenNotify() {
		PushManager.requestPermission(MyMsgInformSetting.this);
	}

	@Override
	public void onChange(View v, boolean state) {
		String tag = (String) v.getTag();
		String key = null;
		switch (tag) {
			case tag_new:
				mNeedCheckStatus = true;
				mNewMsgOpenTemp = !state;
				startOpenNotify();
				break;
			case tag_comments:
				key = "comments";
				break;
			case tag_good:
				key = "good";
				break;
			case tag_feedback:
				key = "feedback";
				break;
			case tag_qa:
				key = "qa";
				break;
			case tag_kefu:
				key = "kefu";
				break;
			case tag_interesting:
				key = "interesting";
				break;
		}
		if (key != null)
			setDataMap(key, state);
	}
}
