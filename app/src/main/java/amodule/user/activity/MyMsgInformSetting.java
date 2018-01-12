package amodule.user.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.popdialog.util.PushManager;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.MessageView;
import com.xh.view.TitleView;
import com.xiangha.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.notification.controller.NotificationSettingController;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.widget.SwitchView;
import amodule.user.datacontroller.MsgSettingDataController;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.push.xg.XGTagManager;

/**
 * 消息通知设置
 * @author luomin
 */
public class MyMsgInformSetting extends BaseActivity implements View.OnClickListener, SwitchView.OnSwitchChangeListener{
	private View msgNew;//接收全部消息通知
	private TextView msgNewDesc;
	private SwitchView msg_comments;//有人给我评论 tag:tag_comments
	private SwitchView msg_good;//有人给我点赞  tag:tag_good
	private SwitchView msg_feedback;//香哈小秘书通知  tag:tag_feedback
	private SwitchView msg_qa;//有问答消息  tag:tag_qa
	private SwitchView msg_official;//你可能感兴趣的内容  tag:tag_official

	private LinearLayout msgInform_ll;//消息开关列表的布局
	private LinearLayout msgTipStart;//提示开启消息的布局
	private Button start_btn;

	private final String tag_comments = "tag_comments";
	private final String tag_good = "tag_good";
	private final String tag_feedback = "tag_feedback";
	private final String tag_qa = "tag_qa";
	private final String tag_official = "tag_official";

	private Map<String, String> mData;

	private boolean mNeedCheckStatus;
	private boolean mNewMsgOpen;
	private boolean mResumeFromPermission;
	private boolean mGotoClosePermission;
	private boolean mResumeFromClickBtn;

	private MsgSettingDataController mDataController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("通知设置", 2, 0, R.layout.c_view_bar_title, R.layout.a_my_msginform);
		initView();
		initMsgNewStatus();
		setListener();
		initData();
		getData();
	}

	private void initData() {
		mDataController = new MsgSettingDataController();
		mData = new HashMap<>();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mNeedCheckStatus) {
			mNeedCheckStatus = false;
			checkStatusChanged();
		}

		if (mResumeFromPermission && mGotoClosePermission) {
			mResumeFromPermission = false;
			mGotoClosePermission = false;
			XHClick.mapStat(this, "a_set_push", "去关闭跳转到设置", mNewMsgOpen ? "未关闭" : "关闭成功");
		}
		if (mResumeFromClickBtn) {
			XHClick.mapStat(this, "a_set_push", "关闭状态下，点击“设置”去开启", mNewMsgOpen ? "开启成功" : "开启失败");
		}
	}

	private void checkStatusChanged() {
		boolean isEnabled = PushManager.isNotificationEnabled(this);
		if (mNewMsgOpen == isEnabled) {
			return;
		}
		mNewMsgOpen = isEnabled;
		setMsgViewStatus(isEnabled);

		setViewStatus(msg_comments, isEnabled);
		setViewStatus(msg_good, isEnabled);
		setViewStatus(msg_feedback, isEnabled);
		setViewStatus(msg_qa, isEnabled);
		setViewStatus(msg_official, isEnabled);

		setDataMap("comments", isEnabled);
		setDataMap("good", isEnabled);
		setDataMap("feedback", isEnabled);
		setDataMap("qa", isEnabled);
		setDataMap("official", isEnabled);

		if (isEnabled) {
			showStartTip(false);
			showInfoList(true);
		} else {
			showInfoList(false);
			showStartTip(true);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		saveMsgSettingData();
		pushDataToService();
	}

	/**
	 * 开关状态改变监听
	 */
	private void setListener() {
		msgNew.setOnClickListener(this);
		msg_comments.setOnChangeListener(this);
		msg_good.setOnChangeListener(this);
		msg_feedback.setOnChangeListener(this);
		msg_qa.setOnChangeListener(this);
		msg_official.setOnChangeListener(this);
		start_btn.setOnClickListener(this);
	}

	private void initView() {
		msgNew = findViewById(R.id.msg_new);
		TextView title1 = (TextView) msgNew.findViewById(R.id.title);
		title1.setText("接收新消息通知");
		msgNewDesc = (TextView) msgNew.findViewById(R.id.desc);
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
		View msgInteresting = findViewById(R.id.msg_official);
		TextView title6 = (TextView) msgInteresting.findViewById(R.id.title);
		title6.setText("官方提醒消息");
		msg_official = (SwitchView) msgInteresting.findViewById(R.id.switch_btn);
		msg_official.setTag(tag_official);
		msg_official.setState(true);
		msgInteresting.findViewById(R.id.line).setVisibility(View.GONE);
		msgInform_ll = (LinearLayout) findViewById(R.id.msgInform_ll);
		msgTipStart = (LinearLayout) findViewById(R.id.tip_start_layout);
		start_btn = (Button) findViewById(R.id.start_btn);
	}

	private void initMsgNewStatus() {
		mNewMsgOpen = PushManager.isNotificationEnabled(XHApplication.in());
		msgNewDesc.setText(mNewMsgOpen ? "已开启" : "已关闭");
	}

	private void saveMsgSettingData() {
		mDataController.saveAllData(mData);
	}

	private void getData() {
		if (!mNewMsgOpen) {
			setLocalData();
			onDataReady();
			return;
		}
		if (loadManager != null)
			loadManager.showProgressBar();
		ReqInternet.in().doPost(StringManager.API_GETINFOSWITCHLIST, "", new InternetCallback() {
			@Override
			public void loaded(int i, String s, Object o) {
				if (i >= ReqInternet.REQ_OK_STRING) {
					Map<String, String> data = StringManager.getFirstMap(o);
					String comments = data.get("comments");
					setDataMap("comments", TextUtils.isEmpty(comments) || TextUtils.equals(comments, "1"));
					String good = data.get("good");
					setDataMap("good", TextUtils.isEmpty(good) || TextUtils.equals(good, "1"));
					String feedback = data.get("feedback");
					setDataMap("feedback", TextUtils.isEmpty(feedback) || TextUtils.equals(feedback, "1"));
					String qa = data.get("qa");
					setDataMap("qa", TextUtils.isEmpty(qa) || TextUtils.equals(qa, "1"));
					String official = data.get("official");
					setDataMap("official", TextUtils.isEmpty(official) || TextUtils.equals(official, "1"));
				} else {
					setLocalData();
				}
				onDataReady();
			}
		});
	}

	private void onDataReady() {
		if (loadManager != null)
			loadManager.hideProgressBar();
		if (mData == null || mData.isEmpty()) {
			showStartTip(true);
		} else {
			setViewStatus(msg_comments, TextUtils.equals("1", mData.get("comments")));
			setViewStatus(msg_good, TextUtils.equals("1", mData.get("good")));
			setViewStatus(msg_feedback, TextUtils.equals("1", mData.get("feedback")));
			setViewStatus(msg_qa, TextUtils.equals("1", mData.get("qa")));
			setViewStatus(msg_official, TextUtils.equals("1", mData.get("official")));
			if (mNewMsgOpen)
				showInfoList(true);
			else
				showStartTip(true);
		}

	}

	private void pushDataToService() {
		ReqInternet.in().doPost(StringManager.API_SETINFOSWITCH, "list=" + combineData(), new InternetCallback() {
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
		try {
			object.put("comments", mData.get("comments"));
			object.put("good", mData.get("good"));
			object.put("feedback", mData.get("feedback"));
			object.put("qa", mData.get("qa"));
			object.put("official", mData.get("official"));
			ret = object.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private void showInfoList(boolean show) {
		msgInform_ll.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	private void showStartTip(boolean show) {
		msgTipStart.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	private void setMsgViewStatus(boolean open) {
		msgNewDesc.setText(open ? "已开启" : "已关闭");
	}

	private void setViewStatus(SwitchView view, boolean open) {
		view.setState(open);
	}

	private void setDataMap(String key, boolean open) {
		if (mData == null)
			return;
		mData.put(key, open ? "1" : "2");
	}

	private void setLocalData() {
		setDataMap("comments", mDataController.checkOpenByKey("comments"));
		setDataMap("good", mDataController.checkOpenByKey("good"));
		setDataMap("feedback", mDataController.checkOpenByKey("feedback"));
		setDataMap("qa", mDataController.checkOpenByKey("qa"));
		setDataMap("official", mDataController.checkOpenByKey("official"));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.msg_new:
				mNeedCheckStatus = true;
				if (!mNewMsgOpen)
					startOpenNotify();
				else {
					showDialog();
					mGotoClosePermission = true;
					XHClick.mapStat(this, "a_set_push", "开启状态下点击去关闭推送", "");
				}
				break;
			case R.id.start_btn:
				mNeedCheckStatus = true;
				mResumeFromClickBtn = true;
				startOpenNotify();
				break;
		}
	}

	private void startOpenNotify() {
		mResumeFromPermission = true;
		NotificationSettingController.openNotificationSettings();
	}

	@Override
	public void onChange(View v, boolean state) {
		String staticStr = "";
		String tag = (String) v.getTag();
		String key = null;
		switch (tag) {
			case tag_comments:
				key = "comments";
				staticStr = "点击关闭有人给我评论";
				break;
			case tag_good:
				key = "good";
				staticStr = "点击关闭有人给我点赞";
				break;
			case tag_feedback:
				key = "feedback";
				staticStr = "点击关闭香哈小秘书通知";
				break;
			case tag_qa:
				key = "qa";
				staticStr = "点击关闭有问答消息";
				break;
			case tag_official:
				key = "official";
				staticStr = "点击关闭官方提醒消息";
				if (state)
					new XGTagManager().addXGTag(XGTagManager.OFFICIAL);
				else
					new XGTagManager().removeXGTag(XGTagManager.OFFICIAL);
				break;
		}
		if (key != null)
			setDataMap(key, state);
		if (!state)
			XHClick.mapStat(this, "a_set_push", staticStr, "");
	}

	private void showDialog() {
		DialogManager manager = new DialogManager(this);
		ViewManager viewManager = new ViewManager(manager);
		manager.createDialog(viewManager.setView(new TitleView(this).setText("确认关闭推送？"))
		.setView(new MessageView(this).setText("关闭后，您将无法收到香哈为您精选的内容通知以及哈友的互动消息等。"))
		.setView(new HButtonView(this)
				.setNegativeText("取消", v -> {
					manager.cancel();
					XHClick.mapStat(this, "a_set_push", "确认关闭推送弹框", "取消");
				})
				.setPositiveText("确定", v -> {
					manager.cancel();
					startOpenNotify();
					XHClick.mapStat(this, "a_set_push", "确认关闭推送弹框", "去关闭");
				}))).show();
	}
}
