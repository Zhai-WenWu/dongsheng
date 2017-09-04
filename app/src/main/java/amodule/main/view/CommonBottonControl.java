package amodule.main.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.FileManager;
import amodule.main.Main;
import amodule.main.activity.MainCircle;
import xh.basic.tool.UtilString;

public class CommonBottonControl {

	public CommonBottomView mCommonBottomView;
	private CommonBottomView.BottomViewBuilder builder;
	private ArrayList<Map<String,String>> list;
	/**
	 * 设置底部导航view
	 * @param context
	 */
	public View setCommonBottonView(String className,Activity context,int contentXml){
//		if(jubgeCommonBottonShow(context,className))
//			return addCommonBottonView(className,context, contentXml);
//		else
			return LayoutInflater.from(context).inflate(contentXml, null);
	}
	/**
	 * 判断底部view显示
	 * @return
	 */
	private boolean jubgeCommonBottonShow(Context context,String className){
		String msg= FileManager.getFromAssets(context, "showCommonBottom");
		list=UtilString.getListMapByJson(UtilString.getListMapByJson(msg).get(0).get("native"));
		return list.get(0).containsKey(className);
	}
	/**
	 * 刷新底部view
	 * @param context
	 * @param bottomView
	 */
	public void refreshBottonView(Context context,CommonBottomView bottomView){
		CommonBottomView.BottomViewBuilder.getInstance().refresh(bottomView);
	}

	/**
	 * 	添加底部view
	 * @param className
	 * @param context
	 * @param contentXml
     * @return
     */
	public View addCommonBottonView(String className,final Activity context,int contentXml){
		View view_bottom=LayoutInflater.from(context).inflate(R.layout.base_bottom_view, null);
		CommonBottomView mCommonBottomView=createBottomView(className, context);
		FrameLayout  view_normal=(FrameLayout) view_bottom.findViewById(R.id.bottom_normal);
		FrameLayout  view_frame=(FrameLayout) view_bottom.findViewById(R.id.bottom_frame);
		view_normal.addView(LayoutInflater.from(context).inflate(contentXml, null));
		if(mCommonBottomView!=null)
			view_frame.addView(mCommonBottomView);
		return view_bottom;
	}
	/**
	 * 创建view,可以在这里添加业务逻辑
	 * @param className
	 * @param context
	 * @return
	 */
	public CommonBottomView createBottomView(String className,final Activity context){
		builder= CommonBottomView.BottomViewBuilder.getInstance();
		String style=UtilString.getListMapByJson(list.get(0).get(className)).get(0).get("style");
		if(list!=null&&style.equals("1")){//正常---圈子外面---设置view为社区
			builder.setIconOnClickListener(CommonBottomView.BOTTOM_CENTER, new OnClickListener() {
				@Override
				public void onClick(View v) {
					Main.colse_level=2;
					context.finish();
					if(Main.allMain!=null)
						Main.allMain.setCurrentTabByClass(MainCircle.class);
					XHClick.mapStat(context, "a_down420", "美食圈", "");
				}
			});
			builder.setIconDrawableAndText(CommonBottomView.BOTTOM_CENTER, R.drawable.tab_found, "社区");
		}else if(list!=null&&style.equals("2")){//圈子内部----显示otherview--不传文字和后面背景色就ok了
//			builder.setIconDrawableAndText(CommonBottomView.BOTTOM_CENTER, R.drawable.z_home_menu_fatie, "发贴");
//			builder.setNoShowIndex(Integer.parseInt(CommonBottomView.BOTTOM_CENTER));
//			String color = Tools.getColorStr(context,R.color.comment_color);
//			builder.setIndexBackgroup(CommonBottomView.BOTTOM_CENTER, color, "#FFFFFF");
		}
		return mCommonBottomView=builder.create(context);
	}
	/**
	 * 刷新view的红点，和显示数量
	 * @param type
	 * @param num
	 * @param state
	 */
	public void refreshIconShow(String type, int num, boolean state){
		builder.setIconShow(type, num, state);
		builder.refresh(mCommonBottomView);
	}
	/**
	 * 刷新view的点击事件
	 * @param type
	 * @param onClickListener
	 */
	public void refreshIconOnClickListener(String type, OnClickListener onClickListener){
		builder.setIconOnClickListener(type, onClickListener);
		builder.refresh(mCommonBottomView);
	}
	/**
	 * 刷新view的icon和text
	 * @param type
	 * @param drawableId
	 * @param showText
	 */
	public void refreshIconDrawableAndText(String type,int drawableId,String showText){
		builder.setIconDrawableAndText(type, drawableId, showText);
		builder.refresh(mCommonBottomView);
	}
}
	
	
	
	
	