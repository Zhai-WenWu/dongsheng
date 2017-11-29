package amodule.main.view;

import android.view.View.OnClickListener;
/**
 * 底部导航参数
 * @author yujian
 *
 */
public class CommonBottomParams {
	public  OnClickListener ONCLICK_INDEX = null;
	public  OnClickListener ONCLICK_TWO = null;
	public  OnClickListener ONCLICK_THREE = null;
	public  OnClickListener ONCLICK_FOUR = null;
	public boolean Indexstate = false;// 首页显示红点
	public boolean Twostate = false;//电商显示红点
	public boolean Threestate= false;//中间显示红点
	public boolean Fourstate = false;// 生活圈显示红点
	public int showIndexNum = 0;// 首页显示数量
	public int showTwoNum = 0;// 电商显示数量
	public int showThreeNum = 0;// 中间显示数量
	public int showFourNum = 0;// 美食圈显示数量
	public int index=-1;//view所处的位置
	//按钮的图片
	public  int index_drawableId=0;
	public  int two_drawableId=0;
	public  int three_drawableId=0;
	public  int four_drawableId=0;
	//按钮显示文字
	public  String index_showText;
	public  String two_showText;
	public  String three_showText;
	public  String four_showText;

	//位置文字的颜色
	public  String index_textColor;
	public  String two_textColor;
	public  String three_textColor;
	public  String four_textColor;
	//位置的背景颜色
	public String index_layout_backgroup;
	public String two_layout_backgroup;
	public String three_layout_backgroup;
	public String four_layout_backgroup;
	public int showColorIndex=-1;//位置不支持文字点击颜色变化

	public void clearData(){
		//初始化数据为null
		ONCLICK_INDEX=null;
		ONCLICK_TWO=null;
		ONCLICK_THREE=null;
		ONCLICK_FOUR=null;
		index_drawableId=0;
		two_drawableId=0;
		three_drawableId=0;
		four_drawableId=0;
		//按钮显示文字
		index_showText=null;
		two_showText=null;
		three_showText=null;
		four_showText=null;

		index_textColor=null;
		two_textColor=null;
		three_textColor=null;
		four_textColor=null;

		index_layout_backgroup=null;
		two_layout_backgroup=null;
		three_layout_backgroup=null;
		four_layout_backgroup=null;
		showColorIndex=-1;
	}
}
