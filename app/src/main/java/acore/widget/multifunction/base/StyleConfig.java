package acore.widget.multifunction.base;

import android.view.View;

/**
 * PackageName : acore.widget.multifunction
 * Created by MrTrying on 2016/7/27 15:37.
 * E_mail : ztanzeyu@gmail.com
 */
public class StyleConfig {
	/** 文本 */
	String text = "";
	/** 文本背景颜色 */
	String backgroudColor = "";
	/** 文本背景颜色 */
	int chooseBackgroudColor = 0;
	/** 文本颜色 */
	String textColor = "";
	/** 文本大小 */
	int textSize = -1;
	/** 是否有下划线 */
	boolean isUnderline = false;
	/** drawable资源id */
	int drawableResID = -1;
	/** drawable显示宽 */
	int drawableWidth = -1;
	/** drawable显示高 */
	int drawableHeight = -1;
	/** 文本添加的前缀 */
	String prefix = "";
	/** 文本添加的后缀 */
	String suffix = "";
	/** 开始index */
	int start = -1;
	/** 结束index */
	int end = -1;
	/** 点击回调 */
	View.OnClickListener clickListener;
	private int chooseColor;

	public StyleConfig(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public StyleConfig setText(String text) {
		this.text = text;
		return this;
	}

	public String getBackgroudColor() {
		return backgroudColor;
	}

	public StyleConfig setBackgroudColor(String backgroudColor) {
		this.backgroudColor = backgroudColor;
		return this;
	}

	public void setChooseBackgroudColor(int chooseBackgroudColor) {
		this.chooseBackgroudColor = chooseBackgroudColor;
	}

	public int getChooseBackgroudColor() {
		return chooseBackgroudColor;
	}

	public String getTextColor() {
		return textColor;
	}

	public StyleConfig setTextColor(String textColor) {
		this.textColor = textColor;
		return this;
	}

	public int getTextSize() {
		return textSize;
	}

	public StyleConfig setTextSize(int textSize) {
		this.textSize = textSize;
		return this;
	}

	public StyleConfig setDrawable(int drawableResID, int drawableWidth, int drawableHeight) {
		this.drawableResID = drawableResID;
		this.drawableWidth = drawableWidth;
		this.drawableHeight = drawableHeight;
		return this;
	}

	public int getDrawableResID() {
		return drawableResID;
	}

	public StyleConfig setDrawableResID(int drawableResID) {
		this.drawableResID = drawableResID;
		return this;
	}

	public int getDrawableWidth() {
		return drawableWidth;
	}

	public StyleConfig setDrawableWidth(int drawableWidth) {
		this.drawableWidth = drawableWidth;
		return this;
	}

	public int getDrawableHeight() {
		return drawableHeight;
	}

	public StyleConfig setDrawableHeight(int drawableHeight) {
		this.drawableHeight = drawableHeight;
		return this;
	}

	public String getPrefix() {
		return prefix;
	}

	public StyleConfig setPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

	public String getSuffix() {
		return suffix;
	}

	public StyleConfig setSuffix(String suffix) {
		this.suffix = suffix;
		return this;
	}

	public int getStart() {
		return start;
	}

	public StyleConfig setStart(int start) {
		this.start = start;
		return this;
	}

	public int getEnd() {
		return end;
	}

	public StyleConfig setEnd(int end) {
		this.end = end;
		return this;
	}

	public boolean isUnderline() {
		return isUnderline;
	}

	public StyleConfig setUnderline(boolean underline) {
		isUnderline = underline;
		return this;
	}

	public View.OnClickListener getClickListener() {
		return clickListener;
	}

	public int getChooseColor() {
		return chooseColor;
	}

	public StyleConfig setClickListener(View.OnClickListener clickListener) {
		this.clickListener = clickListener;
		return this;
	}

	public StyleConfig setChooseColor(int color){
		chooseColor = color;
		return this;
	}
}
