package amodule.main.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.XHClick;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import amodule.main.activity.MainChangeSend;
import amodule.main.activity.MainCircle;
import amodule.main.activity.MainHome;
import amodule.main.activity.MainMyself;
import amodule.quan.tool.MyQuanDataControl;
import amodule.user.activity.MyMessage;
import third.mall.MainMall;

public class CommonBottomView extends RelativeLayout {

	// icon五种类型
	public static String BOTTOM_HOME = "0";
	public static String BOTTOM_TWO = "1";
	public static String BOTTOM_CENTER = "2";
	public static String BOTTOM_FOUR = "3";
	public static String BOTTOM_FIVE = "4";

	private String[] tabTitle = { "首页", "商城", "社区", "消息", "我的" };
	private LinearLayout linear_item;
	public Activity context;
	public CommonBottomParams viewParams;
	public BottomViewBuilder buidler;
	
	public CommonBottomView(Activity context) {
		super(context);
		this.context = context;
		viewParams= new CommonBottomParams();
		viewParams.index=-1;
		initOnClickListener();
		initView();
	}
	/**
	 * 初始化view
	 */
	private void initView() {
		int[] tabImgs = new int[] { R.drawable.tab_index, R.drawable.tab_mall, R.drawable.tab_found, R.drawable.tab_four, R.drawable.tab_myself };
		LayoutInflater.from(context).inflate(R.layout.view_commonbottomview, this, true);
		linear_item = (LinearLayout) findViewById(R.id.linear_item);
		for (int i = 0; i < tabTitle.length; i++) {
			LinearLayout layout = (LinearLayout) linear_item.getChildAt(i).findViewById(R.id.tab_linearLayout);
			layout.setTag(String.valueOf(i));
			TextView tv = ((TextView) linear_item.getChildAt(i).findViewById(R.id.textView1));
			tv.setText(tabTitle[i]);

			ImageView imgView = (ImageView) linear_item.getChildAt(i).findViewById(R.id.iv_itemIsFine);
			imgView.setImageResource(tabImgs[i]);

			if (i == 2) {
//				String color = Tools.getColorStr(context,R.color.comment_color);
//				layout.setBackgroundColor(Color.parseColor(color));
//				tv.setTextColor(Color.parseColor("#FFFFFF"));
			}
		}
		// 处理布局margin
		int margin = (ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_5) * 2 - Tools.getDimen(context, R.dimen.dp_70) * 5) / 4 / 2;
		int length = linear_item.getChildCount();
		for (int i = 0; i < length; i++) {
			setTabItemMargins(linear_item, i, margin, margin);
		}
		ImageView btn_changeSend = (ImageView) findViewById(R.id.btn_changeSend);
		RelativeLayout changeSendLayout = (RelativeLayout) findViewById (R.id.btn_changeSend_layout);
		changeSendLayout.setVisibility(View.GONE);
		int btn_width = ToolsDevice.getWindowPx(context).widthPixels / 5;
		int padding = (btn_width - Tools.getDimen(context, R.dimen.dp_55)) / 2;
		int cha = padding / 4;
		changeSendLayout.getLayoutParams().width = btn_width;
		btn_changeSend.getLayoutParams().width = btn_width;
		btn_changeSend.setPadding(padding + cha, 0, padding - cha, 0);

		setTabItemMargins(linear_item, 0, 0, margin);
		setTabItemMargins(linear_item, length - 1, margin, 0);
		setIconOnClickListener();
		setCurrentText(viewParams.index);
	}
	
	

	private void setTabItemMargins(ViewGroup viewGroup, int index, int leftMargin, int rightMargin) {
		RelativeLayout child = (RelativeLayout) viewGroup.getChildAt(index);
		LinearLayout.LayoutParams params_child = (LinearLayout.LayoutParams) child.getLayoutParams();
		params_child.setMargins(leftMargin, 0, leftMargin, 0);
	}

	/**
	 * 初始化onclickListener
	 */
	private void initOnClickListener() {
		/*** 通用点击事件 */
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Main.colse_level=2;
				context.finish();
				gotoNext((String)v.getTag());
			}
		};
		viewParams.ONCLICK_INDEX = onClickListener;
		viewParams.ONCLICK_TWO = onClickListener;
		viewParams.ONCLICK_THREE = onClickListener;
		viewParams.ONCLICK_FOUR = onClickListener;
		viewParams.ONCLICK_FIVE = onClickListener;
		/*** 中间点击事件 */
		OnClickListener onClickCenterListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
//				onChangeSend();
				ChangeSendDialog dialog= new ChangeSendDialog(context);
				dialog.show();
				
			}
		};
//		viewParams.ONCLICK_THREE = onClickCenterListener;
	}

	private void onChangeSend() {
		MyQuanDataControl.getNewMyQuanData(context, null);
		Intent intent = new Intent(context, MainChangeSend.class);
		context.startActivity(intent);
	}


	private OnClickListener getOnClickChangeLayout(){
		OnClickListener onClickCenterListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent(context, MainChangeSend.class);
//				context.startActivity(intent);
				ChangeSendDialog dialog= new ChangeSendDialog(context);
				dialog.show();
			}
		};
		return onClickCenterListener;
	}
	private OnClickListener getOnclickListener(String type) {
		if (type.equals(BOTTOM_HOME)) {
			return viewParams.ONCLICK_INDEX;
		} else if (type.equals(BOTTOM_TWO)) {
			return viewParams.ONCLICK_TWO;
		} else if (type.equals(BOTTOM_CENTER)) {
			return viewParams.ONCLICK_THREE;
		} else if (type.equals(BOTTOM_FOUR)) {
			return viewParams.ONCLICK_FOUR;
		} else if (type.equals(BOTTOM_FIVE)) {
			return viewParams.ONCLICK_FIVE;
		}
		return viewParams.ONCLICK_INDEX;
	}

	public void Show() {
		this.setVisibility(View.VISIBLE);
	}

	/**
	 * 刷新view
	 */
	public void refreshBottonView() {
		refershViewData();
		setIconOnClickListener();
		setCurrentText(viewParams.index);
	}
	/**
	 * 刷新数据
	 */
	public void refershViewData(){
		setViewData(0, viewParams.showIndexNum, viewParams.Indexstate,viewParams.index_drawableId,viewParams.index_showText,viewParams.index_layout_backgroup,viewParams.index_textColor);
		setViewData(1, viewParams.showTwoNum, viewParams.Twostate,viewParams.two_drawableId,viewParams.two_showText,viewParams.two_layout_backgroup,viewParams.two_textColor);
		setViewData(2, viewParams.showThreeNum, viewParams.Threestate,viewParams.three_drawableId,viewParams.three_showText,viewParams.three_layout_backgroup,viewParams.three_textColor);
		setViewData(3, viewParams.showFourNum, viewParams.Fourstate,viewParams.four_drawableId,viewParams.four_showText,viewParams.four_layout_backgroup,viewParams.four_textColor);
		setViewData(4, viewParams.showFiveNum, viewParams.Fivestate,viewParams.five_drawableId,viewParams.five_showText,viewParams.five_layout_backgroup,viewParams.five_textColor);
	}
	/**
	 * 刷新Icon的view
	 * @param index
	 * @param num
	 * @param state
	 * @param drawableId
	 * @param showText
	 */
	private void setViewData(int index,int num,boolean state,int drawableId,String showText,String backgroupColor,String textColor){
		TextView msg_num=(TextView) linear_item.getChildAt(index).findViewById(R.id.tv_tab_msg_num);
		TextView msg_two_num=(TextView) linear_item.getChildAt(index).findViewById(R.id.tv_tab_msg_tow_num);
		if(num>0){
			linear_item.getChildAt(index).findViewById(R.id.activity_tabhost_redhot).setVisibility(View.GONE);
			if(num>0&&num<10){
				msg_num.setVisibility(View.VISIBLE);
				msg_two_num.setVisibility(View.GONE);
				msg_num.setText(String.valueOf(num));
			}else if(num>=10){
				msg_num.setVisibility(View.GONE);
				msg_two_num.setVisibility(View.VISIBLE);
				msg_two_num.setText(num>99?"99+":String.valueOf(num));
			}
		}else{
			msg_num.setVisibility(View.GONE);
			msg_two_num.setVisibility(View.GONE);
			if(state)
				linear_item.getChildAt(index).findViewById(R.id.activity_tabhost_redhot).setVisibility(View.VISIBLE);
			else
				linear_item.getChildAt(index).findViewById(R.id.activity_tabhost_redhot).setVisibility(View.GONE);
		}
		//设置图片
		if(drawableId!=0)
			((ImageView)linear_item.getChildAt(index).findViewById(R.id.iv_itemIsFine)).setImageResource(drawableId);
		//设置文字
		if(!TextUtils.isEmpty(showText))
			((TextView) linear_item.getChildAt(index).findViewById(R.id.textView1)).setText(showText);
		
		//对设置index=2并设置了数据
		if(index==2&&drawableId!=0&&!TextUtils.isEmpty(showText)){
			linear_item.getChildAt(index).findViewById(R.id.iv_itemIsFine).setVisibility(View.VISIBLE);
			linear_item.getChildAt(index).findViewById(R.id.textView1).setVisibility(View.VISIBLE);
		}
//		findViewById(R.id.btn_changeSend_layout).setVisibility(View.VISIBLE);
//		findViewById(R.id.btn_changeSend).setVisibility(View.VISIBLE);
		//对layout背景颜色
//		if(!TextUtils.isEmpty(backgroupColor)&&View.GONE==findViewById(R.id.btn_changeSend_layout).getVisibility()) {
//			if(String.valueOf(index).equals(CommonBottomView.BOTTOM_CENTER)){
//
//				linear_item.getChildAt(index).findViewById(R.id.Index_backgroup).setVisibility(View.VISIBLE);
//				linear_item.getChildAt(index).findViewById(R.id.tab_linearLayout).setBackgroundColor(Color.parseColor("#00ffffff"));
//				linear_item.getChildAt(index).findViewById(R.id.Index_backgroup).setBackgroundColor(Color.parseColor(backgroupColor));
//			}else{
//				linear_item.getChildAt(index).findViewById(R.id.Index_backgroup).setVisibility(View.GONE);
//				linear_item.getChildAt(index).findViewById(R.id.tab_linearLayout).setBackgroundColor(Color.parseColor(backgroupColor));
//			}
//
//		}
		//对text的文字意思
		if(!TextUtils.isEmpty(textColor))
			((TextView)linear_item.getChildAt(index).findViewById(R.id.textView1)).setTextColor(Color.parseColor(textColor));
	}
	/**
	 * 设置icon点击事件
	 */
	private void setIconOnClickListener(){
		for (int i = 0; i < tabTitle.length; i++) {
			LinearLayout layout = (LinearLayout) linear_item.getChildAt(i).findViewById(R.id.tab_linearLayout);
			layout.setOnClickListener(getOnclickListener(String.valueOf(i)));
		}
		//设置中间view的点击事件
		findViewById(R.id.btn_changeSend_layout).setOnClickListener(getOnClickChangeLayout());
		findViewById(R.id.btn_changeSend).setOnClickListener(getOnClickChangeLayout());
	}

	/**
	 * 处理页面切换按钮图片文字的变化
	 * @param index
	 */
	public void setCurrentText(int index) {
		if(viewParams.showColorIndex>-1&&index==viewParams.showColorIndex){
			return;
		}
		for (int j = 0; j < tabTitle.length; j++)
			if (j == index) {
				((TextView) linear_item.getChildAt(j).findViewById(R.id.textView1)).setTextColor(Color.parseColor("#f84445"));
				linear_item.getChildAt(j).findViewById(R.id.iv_itemIsFine).setSelected(true);
				linear_item.getChildAt(j).findViewById(R.id.iv_itemIsFine).setPressed(false);
			} else {
				((TextView) linear_item.getChildAt(j).findViewById(R.id.textView1)).setTextColor(Color.parseColor("#929292"));
				linear_item.getChildAt(j).findViewById(R.id.iv_itemIsFine).setSelected(false);
				linear_item.getChildAt(j).findViewById(R.id.iv_itemIsFine).setPressed(true);
			}
		if(index==2){//特殊美食圈的逻辑
			findViewById(R.id.btn_changeSend_layout).setVisibility(View.VISIBLE);
		}else{
			findViewById(R.id.btn_changeSend_layout).setVisibility(View.GONE);
		}
	}
	
	private void gotoNext(String type) {
		if (type.equals(BOTTOM_HOME)) {
			XHClick.mapStat(context, "a_down420", tabTitle[0] + "", "");
			if(Main.allMain!=null)
				Main.allMain.setCurrentTabByClass(MainHome.class);
		} else if (type.equals(BOTTOM_TWO)) {
			XHClick.mapStat(context, "a_down420", tabTitle[1] + "", "");
			if(Main.allMain!=null)
				Main.allMain.setCurrentTabByClass(MainMall.class);
		} else if (type.equals(BOTTOM_CENTER)) {
			XHClick.mapStat(context, "a_down420", tabTitle[2] + "", "");
			if(Main.allMain!=null)
				Main.allMain.setCurrentTabByClass(MainCircle.class);
		} else if (type.equals(BOTTOM_FOUR)) {
			XHClick.mapStat(context, "a_down420", tabTitle[3] + "", "");
			if(Main.allMain!=null)
				Main.allMain.setCurrentTabByClass(MyMessage.class);
		} else if (type.equals(BOTTOM_FIVE)) {
			XHClick.mapStat(context, "a_down420", tabTitle[4] + "", "");
			if(Main.allMain!=null)
				Main.allMain.setCurrentTabByClass(MainMyself.class);
		}
	}
	/**
	 * 底部View的构建
	 * 
	 * @author yujian
	 *
	 */
	public static class BottomViewBuilder {
		private static CommonBottomParams ViewParams;
		
		private static BottomViewBuilder instance=null;
		public static BottomViewBuilder getInstance() {
			if (instance == null||ViewParams==null) {
				synchronized (BottomViewBuilder.class) {
					if (instance == null) {
						instance = new BottomViewBuilder();
					}
					if(ViewParams==null){
						ViewParams= new CommonBottomParams();
					}
				}
			}
			ViewParams.clearData();
			return instance;
		}
		/**
		 * 设置icon按钮数据
		 * 
		 * @param type
		 * @param num -1时不设置数据
		 * @param state
		 */
		public void setIconShow(String type, int num, boolean state) {
			if (type.equals(BOTTOM_HOME)) {
				ViewParams.Indexstate=state;
				if(num>-1)ViewParams.showIndexNum= num;
			} else if (type.equals(BOTTOM_TWO)) {
				ViewParams.Twostate=state;
				if(num>-1)ViewParams.showTwoNum= num;
			} else if (type.equals(BOTTOM_CENTER)) {
				ViewParams.Threestate= state;
				if(num>-1)ViewParams.showThreeNum= num;
			} else if (type.equals(BOTTOM_FOUR)) {
				ViewParams.Fourstate=state;
				if(num>-1)ViewParams.showFourNum= num;
			} else if (type.equals(BOTTOM_FIVE)) {
				ViewParams.Fivestate=state;
				if(num>-1)ViewParams.showFiveNum= num;
			}
			
		}

		/**
		 * 设置icon按钮的点击事件
		 * 
		 * @param type
		 * @param onClickListener
		 */
		public void setIconOnClickListener(String type, OnClickListener onClickListener) {
			if (type.equals(BOTTOM_HOME)) {
				ViewParams.ONCLICK_INDEX=onClickListener;
			} else if (type.equals(BOTTOM_TWO)) {
				ViewParams.ONCLICK_TWO=onClickListener;
			} else if (type.equals(BOTTOM_CENTER)) {
				ViewParams.ONCLICK_THREE=onClickListener;
			} else if (type.equals(BOTTOM_FOUR)) {
				ViewParams.ONCLICK_FOUR=onClickListener;
			} else if (type.equals(BOTTOM_FIVE)) {
				ViewParams.ONCLICK_FIVE=onClickListener;
			}
		}

		/**
		 * 设置icon图片和文字
		 * @param type
		 * @param drawableId
		 * @param showText
		 */
		public void setIconDrawableAndText(String type,int drawableId,String showText){
			if (type.equals(BOTTOM_HOME)) {
				ViewParams.index_drawableId=drawableId;
				ViewParams.index_showText=showText;
			} else if (type.equals(BOTTOM_TWO)) {
				ViewParams.two_drawableId=drawableId;
				ViewParams.two_showText=showText;
			} else if (type.equals(BOTTOM_CENTER)) {
				ViewParams.three_drawableId=drawableId;
				ViewParams.three_showText=showText;
			} else if (type.equals(BOTTOM_FOUR)) {
				ViewParams.four_drawableId=drawableId;
				ViewParams.four_showText=showText;
			} else if (type.equals(BOTTOM_FIVE)) {
				ViewParams.five_drawableId=drawableId;
				ViewParams.five_showText=showText;
			}
		}
		/**
		 * 设置位置的背景颜色和文字意思
		 * @param type
		 * @param backgroupColor
		 * @param textColor
		 */
		public void setIndexBackgroup(String type,String backgroupColor,String textColor){
			if (type.equals(BOTTOM_HOME)) {
				ViewParams.index_layout_backgroup=backgroupColor;
				ViewParams.index_textColor=textColor;
			} else if (type.equals(BOTTOM_TWO)) {
				ViewParams.two_layout_backgroup=backgroupColor;
				ViewParams.two_textColor=textColor;
			} else if (type.equals(BOTTOM_CENTER)) {
				ViewParams.three_layout_backgroup=backgroupColor;
				ViewParams.three_textColor=textColor;
			} else if (type.equals(BOTTOM_FOUR)) {
				ViewParams.four_layout_backgroup=backgroupColor;
				ViewParams.four_textColor=textColor;
			} else if (type.equals(BOTTOM_FIVE)) {
				ViewParams.five_layout_backgroup=backgroupColor;
				ViewParams.five_textColor=textColor;
			}
		}
		
		public void setNoShowIndex(int index){
			ViewParams.showColorIndex=index;
		}
		/**
		 * 添加view到指定位置
		 * 
		 * @param index
		 */
		public void setMainIndex(int index) {
			ViewParams.index= index;
		}

		/**
		 * 创建view
		 * 
		 * @return
		 */
		public CommonBottomView create(Activity context) {
			CommonBottomView bottomView=new CommonBottomView(context);
			return refresh(bottomView);
		}

		/**
		 * 刷新
		 */
		public CommonBottomView refresh(CommonBottomView bottomView ) {
			if(bottomView==null)return null;
			bottomView.viewParams.Indexstate= ViewParams.Indexstate;
			bottomView.viewParams.Twostate= ViewParams.Twostate;
			bottomView.viewParams.Threestate= ViewParams.Threestate;
			bottomView.viewParams.Fourstate=ViewParams.Fourstate;
			bottomView.viewParams.Fivestate=ViewParams.Fivestate;
			if(ViewParams.ONCLICK_INDEX!=null)bottomView.viewParams.ONCLICK_INDEX=ViewParams.ONCLICK_INDEX;
			if(ViewParams.ONCLICK_TWO!=null)bottomView.viewParams.ONCLICK_TWO=ViewParams.ONCLICK_TWO;
			if(ViewParams.ONCLICK_THREE!=null)bottomView.viewParams.ONCLICK_THREE=ViewParams.ONCLICK_THREE;
			if(ViewParams.ONCLICK_FOUR!=null)bottomView.viewParams.ONCLICK_FOUR=ViewParams.ONCLICK_FOUR;
			if(ViewParams.ONCLICK_FIVE!=null)bottomView.viewParams.ONCLICK_FIVE=ViewParams.ONCLICK_FIVE;
			if(ViewParams.index>-1)bottomView.viewParams.index= ViewParams.index;
			if(ViewParams.showIndexNum>-1)bottomView.viewParams.showIndexNum=ViewParams.showIndexNum;
			if(ViewParams.showTwoNum>-1)bottomView.viewParams.showTwoNum=ViewParams.showTwoNum;
			if(ViewParams.showThreeNum>-1)bottomView.viewParams.showThreeNum=ViewParams.showThreeNum;
			if(ViewParams.showFourNum>-1)bottomView.viewParams.showFourNum=ViewParams.showFourNum;
			if(ViewParams.showFiveNum>-1)bottomView.viewParams.showFiveNum=ViewParams.showFiveNum;
			if(ViewParams.index_drawableId!=0)bottomView.viewParams.index_drawableId= ViewParams.index_drawableId;
			if(ViewParams.two_drawableId!=0)bottomView.viewParams.two_drawableId= ViewParams.two_drawableId;
			if(ViewParams.three_drawableId!=0)bottomView.viewParams.three_drawableId= ViewParams.three_drawableId;
			if(ViewParams.four_drawableId!=0)bottomView.viewParams.four_drawableId= ViewParams.four_drawableId;
			if(ViewParams.five_drawableId!=0)bottomView.viewParams.five_drawableId= ViewParams.five_drawableId;
			if(!TextUtils.isEmpty(ViewParams.index_showText))bottomView.viewParams.index_showText=ViewParams.index_showText;
			if(!TextUtils.isEmpty(ViewParams.two_showText))bottomView.viewParams.two_showText=ViewParams.two_showText;
			if(!TextUtils.isEmpty(ViewParams.three_showText))bottomView.viewParams.three_showText=ViewParams.three_showText;
			if(!TextUtils.isEmpty(ViewParams.four_showText))bottomView.viewParams.four_showText=ViewParams.four_showText;
			if(!TextUtils.isEmpty(ViewParams.five_showText))bottomView.viewParams.five_showText=ViewParams.five_showText;
			
			if(!TextUtils.isEmpty(ViewParams.index_layout_backgroup))bottomView.viewParams.index_layout_backgroup=ViewParams.index_layout_backgroup;
			if(!TextUtils.isEmpty(ViewParams.two_layout_backgroup))bottomView.viewParams.two_layout_backgroup=ViewParams.two_layout_backgroup;
			if(!TextUtils.isEmpty(ViewParams.three_layout_backgroup))bottomView.viewParams.three_layout_backgroup=ViewParams.three_layout_backgroup;
			if(!TextUtils.isEmpty(ViewParams.four_layout_backgroup))bottomView.viewParams.four_layout_backgroup=ViewParams.four_layout_backgroup;
			if(!TextUtils.isEmpty(ViewParams.five_layout_backgroup))bottomView.viewParams.five_layout_backgroup=ViewParams.five_layout_backgroup;
			if(!TextUtils.isEmpty(ViewParams.index_textColor))bottomView.viewParams.index_textColor=ViewParams.index_textColor;
			if(!TextUtils.isEmpty(ViewParams.two_textColor))bottomView.viewParams.two_textColor=ViewParams.two_textColor;
			if(!TextUtils.isEmpty(ViewParams.three_textColor))bottomView.viewParams.three_textColor=ViewParams.three_textColor;
			if(!TextUtils.isEmpty(ViewParams.four_textColor))bottomView.viewParams.four_textColor=ViewParams.four_textColor;
			if(!TextUtils.isEmpty(ViewParams.five_textColor))bottomView.viewParams.five_textColor=ViewParams.five_textColor;
			if(ViewParams.showColorIndex>-1)bottomView.viewParams.showColorIndex=ViewParams.showColorIndex;
			bottomView.buidler=this;
			bottomView.refreshBottonView();
			return bottomView;
		}
	}
}
