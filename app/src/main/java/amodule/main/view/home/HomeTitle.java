package amodule.main.view.home;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianghatest.R;

import acore.logic.XHClick;
import acore.widget.ScrollLinearListLayout;
import amodule.dish.activity.GoodDish;
import amodule.dish.activity.MenuDish;
import amodule.main.Main;
import amodule.main.activity.MainCircle;
import amodule.main.activity.MainHomePageNew;
import amodule.nous.activity.HomeNous;
import amodule.user.activity.GourmetList;

/**
 * 内容标题
 * @author Administrator
 */
public class HomeTitle extends LinearLayout {

	private String title;
	private TextView title_tv;
	private Context contexts;
	/**点击事件是否有效*/
	private boolean canClick = true;

	public HomeTitle(Context context) {
		super(context);
		init(context, null);
	}

	public HomeTitle(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public HomeTitle(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		this.contexts = context;
		LayoutInflater.from(context).inflate(R.layout.a_home_title, this);
		title_tv = (TextView) findViewById(R.id.title_tv);
		setClickable(true);
		findViewById(R.id.title_rela).setOnClickListener(ScrollLinearListLayout.getOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity();
			}
		}));
		if(attrs != null){
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HomeTitle);
			setTitle(a.getString(R.styleable.HomeTitle_htTitle));
			a.recycle();
		}
	}

	public void setTitle(String title) {
		this.title = title;
		title_tv.setText(title);
	}

	/**
	 * 跳转页面
	 */
	private void startActivity() {
		if (!TextUtils.isEmpty(title) && canClick) {
			XHClick.mapStat(contexts, MainHomePageNew.STATISTICS_ID, title,"更多" );
			if ("精彩生活圈".equals(title)){ // 进入生活圈的首页
				Main.allMain.setCurrentTabByClass(MainCircle.class);
			}else {
				Intent intent = new Intent();
				if ("人气推荐".equals(title)) { // 进入更多美食家{
					XHClick.track(getContext(), "点击首页的人气推荐");
					intent.setClass(contexts, GourmetList.class);
				}else if ("精选专题".equals(title)){ // 精选菜单列表页
					intent.setClass(contexts, MenuDish.class);
				}else if("最新佳作".equals(title)) {//今日佳作----暂时写成这样
					intent.setClass(contexts, GoodDish.class);
				}else if("香哈头条".equals(title) && Main.allMain != null) { // 香哈头条页面
					XHClick.track(getContext(), "点击首页的香哈头条");
					intent.setClass(contexts, HomeNous.class);
				}
				contexts.startActivity(intent);
			}
		}
	}

	public boolean isCanClick() {
		return canClick;
	}

	public void setCanClick(boolean canClick) {
		this.canClick = canClick;
	}
}
