package amodule.quan.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.TextViewShow;
import amodule.quan.activity.ShowSubject;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;

public class BarSubjectReply1 extends LinearLayout{
	private final ImageView subject_camera;
	private LinearLayout subject_zan_ll;
	private LinearLayout subject_pinglun;
	private TextView subject_zan;
	public TextViewShow pinglun;
	private View view;
	private String isLikeString;
	public BarSubjectReply1(Context context, AttributeSet attrs) {
		super(context, attrs);
		view = LayoutInflater.from(context).inflate(R.layout.c_view_bar_subject_reply_lz, this);
		hide();
		subject_camera = (ImageView) view.findViewById(R.id.subject_camera);
		subject_zan_ll = (LinearLayout) view.findViewById(R.id.subject_zan_ll);
		subject_pinglun = (LinearLayout) view.findViewById(R.id.subject_ping_ll);
		subject_zan = (TextView) view.findViewById(R.id.subject_zan);
		pinglun = (TextViewShow) view.findViewById(R.id.subject_pinglun);
		pinglun.setHaveCopyFunction(false);
	}
	/**
	 * 初始化
	 * @param act
	 * @param handler
	 * @param subjectCode
	 * @param subjectName
	 * @param isLike
	 * @param zanNum
	 */
	@SuppressLint("ResourceAsColor")
	public void initView(final Activity act,final Handler handler,final String subjectCode
			,final String subjectName,final String isLike,final int zanNum,String commentNum,final String cid) {
		
		
		//界面显示
		if ("2".equals(isLike)){
			subject_zan_ll.setBackgroundResource(R.drawable.bg_round_grey20);
		}
		else{
			subject_zan_ll.setBackgroundResource(R.drawable.bg_round_red20);
		}
		subject_zan.setText(zanNum+"赞");
		if ("0".equals(commentNum)) {
			pinglun.setText("抢沙发");
		}else {
			pinglun.setText(commentNum+"评论...");
		}
		//绑定点击事件
		subject_camera.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!LoginManager.isLogin()) {
					// 登录 罗明
					Intent intent = new Intent();
					intent.setClass(act,LoginByAccout.class);
					act.startActivity(intent);
					return;
				}
				//统计发美食贴(计算事件)
				XHClick.onEventValue(act, "uploadQuan", "uploadQuan", "从帖子发", 1);
				
				Intent intent = new Intent();
				intent.putExtra("title", subjectName);
				intent.putExtra("cid", cid);
				intent.putExtra("subjectCode", subjectCode);
				intent.putExtra("skip", false);
				intent.setClass(act, UploadSubjectNew.class);
				act.startActivity(intent);
			}
		});
		pinglun.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Message msg=handler.obtainMessage(ShowSubject.REPLY_LZ_CLICK, null);
				handler.sendMessage(msg);
			}
		});
		subject_pinglun.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Message msg=handler.obtainMessage(ShowSubject.REPLY_LZ_CLICK, null);
				handler.sendMessage(msg);
			}
		});
		isLikeString = isLike;
		subject_zan_ll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				Message message = handler.obtainMessage(ShowSubject.ZAN_LZ_CLICK, null);
//				handler.sendMessage(message);
				
				
				if (!LoginManager.isLogin()) {
					// 跳转到登录
					Intent intent = new Intent(act,LoginByAccout.class);
					act.startActivity(intent);
					Tools.showToast(act, "请先登录");
					return;
				}
				XHClick.track(getContext(), "美食贴点赞");
				/**
				 * 点赞事件处理
				 * 
				 * @param map
				 */
				
					if (!isLikeString.equals("2")) {
						//赞直接+1并变红
						subject_zan.setText(zanNum+1+"赞");
//						subject_zan.setTextColor(R.color.c_gray_999999);
						subject_zan_ll.setBackgroundResource(R.drawable.bg_round_grey20);
						isLikeString = "2";
						
						String params = "type=likeList&subjectCode=" + subjectCode + "&floorId=0";
						ReqInternet.in().doPost(StringManager.api_quanSetSubject, params,new InternetCallback(act) {
							@Override
							public void loaded(int flag, String url,Object returnObj) {
								if (flag >= UtilInternet.REQ_OK_STRING) {
									Message msg=handler.obtainMessage(ShowSubject.ZAN_LZ_OVER, returnObj);
									handler.sendMessage(msg);
								}
								
							}
						});
					} else {
						Tools.showToast(act, "您已经赞过了，谢谢！");
					}
			}
		});
	}
	
	/** 显示控件 */
	public void show() {
		view.setVisibility(View.VISIBLE);
	}
	/** 隐藏控件 */
	public void hide() {
		view.setVisibility(View.GONE);
	}
	
	public void setPinglun(CharSequence content){
		pinglun.setText(content);
	}
}
