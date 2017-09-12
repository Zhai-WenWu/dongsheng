package amodule.main.view.circle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.Tools;
import aplug.basic.LoadImage;

/**
 * PackageName : amodule.main.view.circle
 * Created by MrTrying on 2016/8/26 18:42.
 * E_mail : ztanzeyu@gmail.com
 */
public class CircleHeaderRecUser extends LinearLayout {
	private String stiaticID = "";

	public CircleHeaderRecUser(Context context) {
		this(context, null, 0);
	}

	public CircleHeaderRecUser(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleHeaderRecUser(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setOrientation(VERTICAL);
		setBackgroundColor(Color.parseColor(getResources().getString(R.color.common_bg)));
	}

	public void setData(ArrayList<Map<String, String>> list) {
		if (list == null || list.size() == 0) {
			setVisibility(GONE);
			return;
		}
		for (int index = 0; index < list.size(); index++) {
			Map<String, String> userData = list.get(index);
			View view = LayoutInflater.from(getContext()).inflate(R.layout.a_circle_header_rec_user, null);
			setUserData(view, userData, index);
		}
		if(getChildCount() == 0){
			setVisibility(GONE);
			return;
		}
		setPadding(0, 0, 0, Tools.getDimen(getContext(), R.dimen.dp_10));
	}

	private void setUserData(final View view, final Map<String, String> userData, final int index) {
		if (userData.containsKey("folState") && ! "2".equals(userData.get("folState"))) {

			ImageView userImage = (ImageView) view.findViewById(R.id.user_img);

			TextView userName = (TextView) view.findViewById(R.id.user_name);
			TextView userDesc = (TextView) view.findViewById(R.id.user_desc);

			final RelativeLayout follow_rela = (RelativeLayout) view.findViewById(R.id.follow_rela);
			final TextView follow_tv = (TextView) view.findViewById(R.id.follow_tv);
			final ImageView follow_img = (ImageView) view.findViewById(R.id.follow_img);

			userName.setText(userData.get("nickName"));
			try {
				if (!TextUtils.isEmpty(userData.get("color"))) {
					userName.setTextColor(Color.parseColor(userData.get("color")));
				}
			} catch (Exception e) {

			}
			userDesc.setText(userData.get("desc"));

			view.findViewById(R.id.user_icon).setVisibility("2".equals(userData.get("isGourmet")) ? VISIBLE : GONE);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mOnItemClickListener != null) {
						mOnItemClickListener.onClick(v, index);
					}
				}
			});
			follow_rela.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					XHClick.mapStat(getContext(),stiaticID,"顶部推荐关注","关注");
					AppCommon.onAttentionClick(userData.get("code"), "follow");
					postDelayed(new Runnable() {
						@Override
						public void run() {
							followMethod();
						}

						private void followMethod(){
							Tools.showToast(getContext(), "已关注");
							follow_rela.setVisibility(View.VISIBLE);
							int dp_12 = Tools.getDimen(follow_rela.getContext(), R.dimen.dp_12);
							int dp_9 = Tools.getDimen(follow_rela.getContext(), R.dimen.dp_9);
							LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp_12, dp_9);
							layoutParams.gravity = Gravity.CENTER_VERTICAL;
							layoutParams.setMargins(0, Tools.getDimen(follow_rela.getContext(), R.dimen.dp_1), 0, 0);
							follow_img.setLayoutParams(layoutParams);
							follow_img.setBackgroundResource(R.drawable.circle_follow_user_right);
							follow_tv.setText("已关注");
							follow_tv.setTextColor(Color.parseColor("#999999"));
							findViewById(R.id.follow_rela).setBackgroundColor(Color.parseColor("#fffffe"));
							post(new Runnable() {
								@Override
								public void run() {
									final TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
									animation.setDuration(350);//设置动画持续时间
									animation.setAnimationListener(new Animation.AnimationListener() {
										@Override
										public void onAnimationStart(Animation animation) {
										}

										@Override
										public void onAnimationEnd(Animation animation) {
											post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    removeView(view);
                                                    if (getChildCount() == 0) {
                                                        setVisibility(GONE);
                                                    }
                                                }
                                            });
										}

										@Override
										public void onAnimationRepeat(Animation animation) {
										}
									});
									view.startAnimation(animation);
								}
							});
						}
					},500);

				}
			});

			BitmapRequestBuilder<GlideUrl, Bitmap> requestBuilder = LoadImage.with(getContext()).load(userData.get("img")).setImageRound(200).build();
			if(requestBuilder != null){
				requestBuilder.into(userImage);
			}

			addView(view, ViewGroup.LayoutParams.MATCH_PARENT, Tools.getDimen(getContext(), R.dimen.dp_71));
		}
	}

	private OnItemClickListener mOnItemClickListener;

	public interface OnItemClickListener {
		public void onClick(View view, int position);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mOnItemClickListener = listener;
	}

	public String getStiaticID() {
		return stiaticID;
	}

	public void setStiaticID(String stiaticID) {
		this.stiaticID = stiaticID;
	}
}
