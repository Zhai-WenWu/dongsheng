package amodule.quan.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import acore.widget.TextViewShow;
import acore.widget.multifunction.MentionStyleBulider;
import acore.widget.multifunction.ReplyStyleBuilder;
import acore.widget.multifunction.base.StyleConfigBuilder;
import acore.widget.multifunction.view.MultifunctionTextView;
import amodule.quan.activity.ShowSubject;
import amodule.quan.tool.SubjectControl;
import amodule.quan.view.ImgTextCombineLayout;
import amodule.quan.view.ImgTextCombineLayout.ImgTextCallBack;
import amodule.user.activity.FriendHome;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import aplug.imageselector.ImgWallActivity;
import core.xiangha.emj.view.EditTextShow;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilString;

@SuppressLint("InflateParams")
public class AdapterQuanShowSubject extends AdapterSimple {
	public String subjectOwer = "";// 从外部设置楼主code;
	/**
	 * 用map做评论的缓存，每一页加载时要生成所有回复框在缓存中 如果要刷新回复数据，要删除commentLayouts中对应楼的回复框以便重新生成。
	 */
	public Map<String, LinearLayout> commentLayouts = new HashMap<>();
	final int viewUser = 0;// 访问用户
	final int viewReport = 1;// 举报楼层;
	final int viewPing = 2;// 点评操作;
	final int viewLouzhu = 3; // 点击楼主回复
	final int viewSub = 4;// 访问内容;
	ArrayList<Map<String, String>> floorData;
	DownRefreshList mParent;
	Activity mAct;
	Handler mHandler;
	String commentId;// 从消息点过来的时候会有楼中楼的id,否则为null
	float sp_15;

	@SuppressWarnings("unchecked")
	public AdapterQuanShowSubject(Activity act, Handler handler, DownRefreshList parent, List<? extends Map<String, ?>> data,
								  int resource, String[] from, int[] to, String comentId) {
		super(parent, data, resource, from, to);
		mAct = act;
		mHandler = handler;
		this.mParent = parent;
		floorData = (ArrayList<Map<String, String>>) data;
		this.commentId = comentId;
		sp_15 = Tools.getDimenSp(mAct, R.dimen.sp_15);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		// 数据集合
		final Map<String, String> floorMap = floorData.get(position);
		final Map<String, String> user = UtilString.getListMapByJson(floorMap.get("customer")).get(0);
		// 楼层显示
		TextView tv_sub_user_name = (TextView) view.findViewById(R.id.tv_sub_user_name);
		this.setViewText(tv_sub_user_name, user.get("nickName"));
//		user.put("color","#000000");
		if(user.containsKey("color"))
			tv_sub_user_name.setTextColor(Color.parseColor(user.get("color")));

		ImageView iv_sub_user_heard = (ImageView) view.findViewById(R.id.iv_sub_user_heard);
		this.setViewImage(iv_sub_user_heard, user.get("imgShow"));

		TextView tv_sub_num = (TextView) view.findViewById(R.id.tv_sub_num);
		this.setViewText(tv_sub_num, floorMap.get("num"));

		ImageView i_user_lv = (ImageView) view.findViewById(R.id.i_user_lv);
		// 等级显示
		if (user.containsKey("lv")) {
			AppCommon.setLvImage(Integer.parseInt(user.get("lv")), i_user_lv);
		}
		ImageView i_user_vip = (ImageView) view.findViewById(R.id.i_user_vip);
		AppCommon.setVip(mAct,i_user_vip,user.get("vip"), AppCommon.VipFrom.COMMENT);
		ImageView iv_userType = (ImageView) view.findViewById(R.id.iv_userType);
		// 加v显示
		if (user.containsKey("isGourmet") && user.get("isGourmet") != null) {
			AppCommon.setUserTypeImage(Integer.parseInt(user.get("isGourmet")), iv_userType);
		}
		// 时间显示
		TextView tv_sub_timeShow = (TextView) view.findViewById(R.id.tv_sub_timeShow);
		this.setViewText(tv_sub_timeShow, floorMap.get("timeShow"));
		// 评论次数
		TextView tv_sub_ping = (TextView) view.findViewById(R.id.tv_sub_ping);
		this.setViewText(tv_sub_ping, floorMap.get("commentNum").equals("0") ? " " : floorMap.get("commentNum"));
		// 评论内容
		TextViewShow tv_sub_content = (TextViewShow) view.findViewById(R.id.tv_sub_content);
//		tv_sub_content.setIsStackOverflowError(AppCommon.isStackOverflowError);
		ArrayList<Map<String, String>> contentList = UtilString.getListMapByJson(floorMap.get("content"));
		final ArrayList<String> img_urls = new ArrayList<>();
		final LinearLayout linear_body_imgs = (LinearLayout) view.findViewById(R.id.linear_body_imgs);
		tv_sub_content.setVisibility(View.GONE);
		if (contentList.size() > 0) {
			if (linear_body_imgs.getTag() != floorMap.get("id")) {
				if (linear_body_imgs.getChildCount() > 0)
					linear_body_imgs.removeAllViews();
				for (int i = 0; i < contentList.size(); i++) {
					ImgTextCombineLayout layout = new ImgTextCombineLayout(mAct);
					layout.setSubjectStyle();
					layout.setViewWaith();
					Map<String, String> map = contentList.get(i);
					layout.setImgText(map.get("text"), map.get("img"), false, sp_15);
					if (!TextUtils.isEmpty(map.get("img"))) {
						img_urls.add(map.get("img"));
					}

					layout.setImgTextCallBack(new ImgTextCallBack() {
						@Override
						public void onImageClick(ImgTextCombineLayout layout) {
							int index = 0;
							int index_now = 0;
							if (layout != null)
								for (int i = 0, length = linear_body_imgs.getChildCount(); i < length; i++) {
									if (layout == linear_body_imgs.getChildAt(i)) index = i;
								}
							index_now = index;
							for (int i = 0, length = linear_body_imgs.getChildCount(); i < length; i++) {
								if (index >= i) {
									if (TextUtils.isEmpty(((ImgTextCombineLayout) linear_body_imgs.getChildAt(i)).getImgText().get(ImgTextCombineLayout.IMGEURL)))
										index_now--;
								}
							}

							showImgWall(index_now, img_urls);
						}

						@Override
						public int getWidth() {
							return 0;
						}
						@Override public void onFocusChange(EditTextShow editTextshow, boolean hasFocus, ImgTextCombineLayout layout) {}
						@Override public void onDelete(ImgTextCombineLayout layout) {}
						@Override public void onClick(ImgTextCombineLayout layout) {}
						@Override public void initImgNull(ImgTextCombineLayout layout) {}
					});
					setTv_content(layout.textview, floorMap, user, map.get("text"), position);
					linear_body_imgs.addView(layout);
				}
				linear_body_imgs.setTag(floorMap.get("id"));
			}
		}

		// 楼层删除的判断
		// ImageView iv_sub_zan = (ImageView)
		// view.findViewById(R.id.iv_sub_zan);
		// 当别人@你,点击后颜色变化
		LinearLayout rela_heard = (LinearLayout) view.findViewById(R.id.rela_heard);

		if (floorMap.containsKey("bgColor") && commentId == null) {
			int resid = Integer.parseInt(floorMap.get("bgColor"));
			rela_heard.setBackgroundResource(resid);
			linear_body_imgs.setBackgroundResource(resid);
		} else {
			rela_heard.setBackgroundResource(R.color.c_white_bg_title);
			linear_body_imgs.setBackgroundResource(R.color.c_white_bg_title);
		}
		// 设置楼主标示是否可见
		TextView item_subject_linear_item_type = (TextView) view.findViewById(R.id.item_subject_linear_item_type);
		if (user.get("code").equals(subjectOwer)) {
			item_subject_linear_item_type.setVisibility(View.VISIBLE);
		} else {
			item_subject_linear_item_type.setVisibility(View.GONE);
		}
		// 赞和评论的布局
		LinearLayout item_subject_linear_zan = (LinearLayout) view.findViewById(R.id.item_subject_linear_zan);
		LinearLayout item_subject_linear_ping = (LinearLayout) view.findViewById(R.id.item_subject_linear_ping);
		// 点击事件
		setClickEvent(floorMap, user, tv_sub_user_name, viewUser, position);
		setClickEvent(floorMap, user, iv_sub_user_heard, viewUser, position);
		setClickEvent(floorMap, user, item_subject_linear_zan, viewReport, position);
		setClickEvent(floorMap, user, item_subject_linear_ping, viewPing, position);
//		setClickEvent(floorMap, user, tv_sub_content, viewPing, position);

		// 是否有楼中楼
		final LinearLayout linear_foot_bg = (LinearLayout) view.findViewById(R.id.linear_foot_bg);
		// 设置评论布局，重用已有布局
		// long start=System.currentTimeMillis();
		final LinearLayout commentView = (LinearLayout) view.findViewById(R.id.item_subject_linear_foot_replay);

		LinearLayout commView = getComments(floorMap, user.get("code"), true);
		if (commView != null) {
			if (commView.getParent() != null) {
				((LinearLayout) commView.getParent()).removeAllViews();
			}
			commentView.removeAllViews();
			commentView.addView(commView);
			linear_foot_bg.setVisibility(View.VISIBLE);
		} else {
			linear_foot_bg.setVisibility(View.GONE);
		}
		// LogManager.print("i", "评论用时："+(System.currentTimeMillis()-start));
		return view;
	}

	/**
	 * 设置内容
	 *
	 * @param textshow
	 * @param floorMap
	 * @param user
	 * @param content
	 * @param position
	 */
	private void setTv_content(TextViewShow textshow, final Map<String, String> floorMap, final Map<String, String> user, String content, int position) {
		MentionStyleBulider builder = new MentionStyleBulider(mAct, content,
				new MentionStyleBulider.MentionClickCallback() {
					@Override
					public void onMentionClick(View v, String userCode) {
						Intent intent = new Intent(mAct, FriendHome.class);
						intent.putExtra("code", userCode);
						mAct.startActivity(intent);
					}
				});
		MultifunctionTextView.MultifunctionText multifunctionText = new MultifunctionTextView.MultifunctionText();
		multifunctionText.addStyle(builder.getContent(), builder.build());
		textshow.setText(multifunctionText);
		textshow.setCopyText(builder.getContent());
		if(LoginManager.isLogin()){
			String comeCode = user.get("code");
			String currentUserCode = LoginManager.userInfo.get("code");
			if(currentUserCode.equals(subjectOwer) || currentUserCode.equals(comeCode)){
				if(currentUserCode.equals(subjectOwer)){
					textshow.setTypeOwer(1);
				}
				//删除
				textshow.setRightClicker("删除", new OnClickListener() {// 删除
					@Override
					public void onClick(View v) {
						deletFloor(floorMap);
					}
				});
			}else{
				//举报
				textshow.setRightClicker("举报", new OnClickListener() {// 删除
					@Override
					public void onClick(View v) {
						Map<String, String> data = new HashMap<>();
						data.put("id", floorMap.get("id"));
						data.put("num", floorMap.get("num"));
						data.put("nickName", user.get("nickName"));
						Message msg = mHandler.obtainMessage(ShowSubject.REPORT_LOU_CLICK, data);
						mHandler.sendMessage(msg);
					}
				});
			}
		}

		setClickEvent(floorMap, user, textshow, viewPing, position);
	}

	/**
	 * 删除楼层
	 *
	 * @param floorMap
	 */
	public void deletFloor(final Map<String, String> floorMap) {
		String params = "type=delFloor&floorId=" + floorMap.get("id");
		SubjectControl.getInstance().createDeleteDilog(mAct, params, "该楼层",
				new SubjectControl.OnDeleteSuccessCallback() {
					@Override
					public void onDeleteSuccess(int flag, String url, Object returnObj) {
						ArrayList<Map<String, String>> list = StringManager.getListMapByJson(returnObj);
						if (list.size() > 0) {
							if ("2".equals(list.get(0).get("type"))) {
								floorData.remove(floorMap);
								notifyDataSetChanged();
							}
						}
						// 统计
						XHClick.onEventValue(mAct, "quanOperate", "quanOperate", "删除楼", 1);
					}
				});
	}

	/**
	 * 楼层图片点击后跳转到图片墙
	 */
	private void showImgWall(int index, ArrayList<String> img_urls) {
		Intent intent = new Intent(mAct, ImgWallActivity.class);
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("images", img_urls);
		bundle.putInt("index", index);
		intent.putExtras(bundle);
		mAct.startActivity(intent);
	}

	/**
	 * 点击事件的方法
	 *
	 * @param floorMap 楼层的集合
	 * @param user     楼层用户的code和name
	 * @param view     点击的view
	 * @param type     事件类型
	 */
	private void setClickEvent(final Map<String, String> floorMap, final Map<String, String> user, View view, final int type, final int position) {
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (type) {
					case viewUser:
						Intent intent = new Intent(mParent.getContext(), FriendHome.class);
						Bundle bundle = new Bundle();
						bundle.putString("code", user.get("code"));
						intent.putExtras(bundle);
						mAct.startActivity(intent);
						break;
					case viewReport:
						Map<String, String> data = new HashMap<>();
						data.put("id", floorMap.get("id"));
						data.put("num", floorMap.get("num"));
						data.put("nickName", user.get("nickName"));
						Message report = mHandler.obtainMessage(ShowSubject.REPORT_LOU_CLICK, data);
						mHandler.sendMessage(report);
						break;
					case viewPing:
						Map<String, String> map = new HashMap<>();
						map.put("floorId", floorMap.get("id"));
						map.put("floorNum", floorMap.get("num"));
						map.put("code", user.get("code"));
						map.put("nickName", user.get("nickName"));
						// map.put("nickCode", subjectOwer);
						map.put("louCode", user.get("code"));
						Message pingLun = mHandler.obtainMessage(ShowSubject.REPLY_LOU_CLICK, map);
						mHandler.sendMessage(pingLun);
						break;
					default:
						break;
				}
			}
		});
	}

	// 获取回复布局
	public LinearLayout getComments(Map<String, String> floorMap, String floorOwner, boolean readCache) {
		// 获取楼层的ID和楼层num
		String floorId = floorMap.get("id");
		String floorNum = floorMap.get("num");
		// 获取楼层中的楼中楼回复数据
		ArrayList<Map<String, String>> comments = UtilString.getListMapByJson(floorMap.get("comments"));

		if (comments.size() == 0)
			return null;
		// 已存在map则直接返回
		if (readCache && commentLayouts.containsKey(floorId)) {
//			UtilLog.print("d", floorNum + "楼，重用map");
			return commentLayouts.get(floorId);
		}
		// 新生成commentLinear
		LinearLayout commentLinear = new LinearLayout(mParent.getContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		commentLinear.setOrientation(LinearLayout.VERTICAL);
		commentLinear.setLayoutParams(params);
		int maxNum = commentLinear.getChildCount() > comments.size() ? commentLinear.getChildCount() : comments.size();
		for (int i = 0; i < maxNum; i++) {
			// 之前的commentLinear太多了，开始隐藏
			if (i == comments.size()) {
				for (int j = i; j < commentLinear.getChildCount(); j++) {
					commentLinear.getChildAt(j).setVisibility(View.GONE);
				}
				break;
			}
			// 修改已有的条目，或者添加新有项
			View itemView = null;
			if (floorMap.containsKey("bgColor") && commentId != null) {

				int resid = Integer.parseInt(floorMap.get("bgColor"));
				for (int j = 0; j < comments.size(); j++) {
					if (commentId.equals(comments.get(j).get("id"))) {
						itemView = getItemView(comments, commentLinear.getChildAt(j), comments.get(j), floorId, floorNum, floorOwner, resid);
					} else {
						itemView = getItemView(comments, commentLinear.getChildAt(j), comments.get(j), floorId, floorNum, floorOwner, 0);
					}
				}
			} else {
				itemView = getItemView(comments, commentLinear.getChildAt(i), comments.get(i), floorId, floorNum, floorOwner, 0);
			}
			if (itemView != null)
				commentLinear.addView(itemView);
		}
		commentLayouts.put(floorId, commentLinear);
		return commentLinear;
	}

	/**
	 * 获取楼层回复的一条，优先修改itemView，成功就返回null，新增就返回view
	 *
	 * @param comments
	 * @param itemView
	 * @param replayData 数据
	 * @param floorId    楼层Id
	 * @param floorNum   楼层数
	 * @param floorOwner 层主code
	 * @param resid
	 *
	 * @return
	 */
	private View getItemView(final ArrayList<Map<String, String>> comments, View itemView, final Map<String, String> replayData,
	                         final String floorId, final String floorNum, final String floorOwner, int resid) {
		View view = itemView;
		if (view == null)
			view = LayoutInflater.from(mParent.getContext()).inflate(R.layout.a_quan_item_subject_comment, null);
		view.setVisibility(View.VISIBLE);

		// 设置单调回复的信息
		TextViewShow tvContent = (TextViewShow) view.findViewById(R.id.tv_content_bg);

		RelativeLayout rela_replay = (RelativeLayout) view.findViewById(R.id.rela_replay);
		// tvReplay.setVisibility(View.GONE);

		if (resid != 0) {
			rela_replay.setBackgroundResource(resid);
			rela_replay.setBackgroundResource(resid);
		} else {
			rela_replay.setBackgroundResource(R.color.quan_green_bg_bartitle_select);
			rela_replay.setBackgroundResource(R.color.quan_green_bg_bartitle_select);
		}
		ReplyStyleBuilder replyBuilder = new ReplyStyleBuilder(subjectOwer, floorOwner,
				replayData.get("customer"), replayData.get("reply"),
				new ReplyStyleBuilder.ReplyClickCallback() {
					@Override
					public void onReplyClick(View v, String userCode) {
						Intent intent = new Intent(mAct, FriendHome.class);
						intent.putExtra("code", userCode);
						mAct.startActivity(intent);
					}
				});
		MentionStyleBulider mentionBuilder = new MentionStyleBulider(mAct, replayData.get("content"),
				new MentionStyleBulider.MentionClickCallback() {
					@Override
					public void onMentionClick(View v, String userCode) {
						Intent intent = new Intent(mAct, FriendHome.class);
						intent.putExtra("code", userCode);
						mAct.startActivity(intent);
					}
				});
		StyleConfigBuilder timeBuilder = new StyleConfigBuilder(" " + replayData.get("time"));
		timeBuilder.setTextColor("#CCCCCC")
				.setTextSize(ToolsDevice.sp2px(mAct, Tools.getDimenSp(mAct, R.dimen.sp_12)));
		MultifunctionTextView.MultifunctionText multifunctionText = new MultifunctionTextView.MultifunctionText();
		multifunctionText.addStyle(replyBuilder.getContent(), replyBuilder.build())
				.addStyle(mentionBuilder.getContent(), mentionBuilder.build())
				.addStyle(timeBuilder.getText(), timeBuilder.build());
		tvContent.setText(multifunctionText);

		tvContent.setCopyText(mentionBuilder.getContent());

		// 回复人信息
		Map<String, String> comeNameMap = UtilString.getListMapByJson(replayData.get("customer")).get(0);
		final String comeCode = comeNameMap.get("code");
		final String name = comeNameMap.get("nickName");

		if(LoginManager.isLogin()){
			String currentUserCode = LoginManager.userInfo.get("code");
			if(currentUserCode.equals(subjectOwer) || currentUserCode.equals(comeCode)){
				//删除
				tvContent.setRightClicker("删除", new OnClickListener() {// 删除
					@Override
					public void onClick(View v) {
						showDeleteDialog(comments, floorId, replayData);
					}
				});
			}else{
				//举报
				tvContent.setRightClicker("举报",new OnClickListener() {
					@Override
					public void onClick(View v) {
						Map<String, String> data = new HashMap<>();
						data.put("id", replayData.get("id"));
						data.put("num", floorNum);
						data.put("nickName", name);
						Message msg = mHandler.obtainMessage(ShowSubject.REPORT_LOUZHONGLOU_CLICK, data);
						mHandler.sendMessage(msg);
					}
				});
			}
		}

		// 点击回复该层
		setReplayClick(view, floorId, floorNum, name, comeCode, floorOwner);
		setReplayClick(tvContent, floorId, floorNum, name, comeCode, floorOwner);
		setReplayClick(view.findViewById(R.id.quan_tie), floorId, floorNum, name, comeCode, floorOwner);
		// LogManager.print("d",
		// "设置点击用时："+(System.currentTimeMillis()-start));
		return itemView == null ? view : null;
	}

	/**
	 * 绑定层中回复的点击事件
	 *
	 * @param view
	 * @param view        : 整个层的view
	 * @param floorId     : 楼层id
	 * @param floorNum    :楼层数
	 * @param comeName    : 回复人名字
	 * @param comeCode    : 回复人code
	 * @param louCengCode :层主code
	 */
	private void setReplayClick(View view, final String floorId, final String floorNum, final String comeName, final String comeCode, final String louCengCode) {
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Map<String, String> data = getHandlerDataMap(floorId, floorNum, comeName, comeCode, louCengCode);
				Message msg = mHandler.obtainMessage(ShowSubject.REPLY_LOU_CLICK, data);
				mHandler.sendMessage(msg);
			}
		});
	}

	// 获取一个map
	public Map<String, String> getHandlerDataMap(final String floorId, final String floorNum, final String comeName, final String comeCode, String louCengCode) {
		Map<String, String> map = new HashMap<>();
		map.put("floorId", floorId);
		map.put("floorNum", floorNum);
		map.put("code", comeCode);
		map.put("nickName", comeName);
		// map.put("nickCode", subjectOwer);
		map.put("louCode", louCengCode);
		return map;
	}

	private ArrayList<Map<String, String>> getReplayDataArray(String comeName, String comeCode) {
		ArrayList<Map<String, String>> array = new ArrayList<>();
		Map<String, String> map1 = new HashMap<>();
		Map<String, String> map2 = new HashMap<>();
		JSONArray array1 = new JSONArray();
		JSONObject stoneObject = new JSONObject();
		try {
			stoneObject.put("code", comeCode);
			stoneObject.put("nickName", comeName);
			array1.put(stoneObject);
			map2.put("", array1.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		array.add(map1);
		array.add(map2);
		return array;
	}

	@Override
	public void setViewImage(final ImageView v, String value) {
		v.setVisibility(View.VISIBLE);
		// 异步请求网络图片
		InputStream iStream = v.getResources().openRawResource(R.drawable.i_nopic);
		Bitmap bitmapImg = UtilImage.inputStreamTobitmap(iStream);
		if (value.indexOf("http") == 0) {
			if (v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(value))
				return;
			if (v.getId() == R.id.iv_sub_user_heard) {
				bitmapImg = UtilImage.toRoundCorner(v.getResources(), bitmapImg, roundType, ToolsDevice.dp2px(mAct, 500));
				UtilImage.setImgViewByWH(v, bitmapImg, imgWidth, imgHeight, imgZoom);
			}

			if (value.length() < 10)
				return;

			v.setTag(TAG_ID, value);
			BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mAct)
					.load(value)
					.setSaveType(imgLevel)
					.build();
			if (bitmapRequest != null)
				bitmapRequest.into(getTarget(v, value));
		}
		// 直接设置为内部图片
		else if (value.indexOf("ico") == 0) {
			InputStream is = v.getResources().openRawResource(Integer.parseInt(value.replace("ico", "")));
			Bitmap bitmap = UtilImage.inputStreamTobitmap(is);
			bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, roundType, roundImgPixels);
			UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
		}
		// 隐藏
		else if (value.equals("hide") || value.length() == 0)
			v.setVisibility(View.GONE);
			// 直接加载本地图片
		else if (!value.equals("ignore")) {
			Bitmap bmp = UtilImage.imgPathToBitmap(value, imgWidth, imgHeight, false, null);
			v.setScaleType(scaleType);
			v.setImageBitmap(bmp);
		}
		// 如果为ignore,则忽略图片
	}

	@Override
	public SubBitmapTarget getTarget(final ImageView imgView, final String url) {
		return new SubBitmapTarget() {
			@Override
			public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
				ImageView img = null;
				if (imgView.getTag(TAG_ID).equals(url))
					img = imgView;
				if (img != null && bitmap != null) {
					boolean isContentImg = false;
					// 头像图片圆角设置;
					if (!isContentImg && imgView.getId() == R.id.iv_sub_user_heard) {
						imgView.setScaleType(ScaleType.CENTER_CROP);
						bitmap = UtilImage.toRoundCorner(imgView.getResources(), bitmap, 1, ToolsDevice.dp2px(mAct, 500));
						imgView.setImageBitmap(bitmap);
					}
				}
			}
		};
	}

	/**
	 * 删除楼中楼回复
	 *
	 * @param comments
	 * @param floorId
	 * @param replayData
	 */
	private void showDeleteDialog(final ArrayList<Map<String, String>> comments, final String floorId, final Map<String, String> replayData) {
		String params = "type=delFloor&floorId=" + floorId + "&commentId=" + replayData.get("id");
		SubjectControl.getInstance().createDeleteDilog(mAct, params, "该回复",
				new SubjectControl.OnDeleteSuccessCallback() {
					@Override
					public void onDeleteSuccess(int flag, String url, Object returnObj) {
						comments.remove(replayData);
						// 找到数据源中要删除的那个楼中楼所在的楼层
						for (int i = 0; i < floorData.size(); i++) {
							if (floorData.get(i).get("id").equals(floorId)) {
								JSONArray array1 = new JSONArray();
								// 将改变后的楼中楼集合转成json字符串.
								for (int j = 0; j < comments.size(); j++) {
									Map<String, String> map = comments.get(j);
									JSONObject jsonObject = new JSONObject();
									Iterator<Entry<String, String>> it = map.entrySet().iterator();
									while (it.hasNext()) {
										Entry<String, String> entry = it.next();
										try {
											jsonObject.put(entry.getKey(), entry.getValue());
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}
									array1.put(jsonObject);
								}
								// 更改源数据,并刷新adapter.
								floorData.get(i).put("comments", array1.toString());
								// 清除标记,从新更新楼层及回复
								commentLayouts.remove(floorId);
								// 刷新
								notifyDataSetChanged();
								// 统计
								XHClick.onEventValue(mAct, "quanOperate", "quanOperate", "删除楼中回复", 1);
							}
						}
					}
				});
	}
}
