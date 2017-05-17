package acore.widget.multifunction.base;

import android.view.View;

import java.util.ArrayList;

/**
 * PackageName : acore.widget.multifunction.base
 * Created by MrTrying on 2016/7/27 15:50.
 * E_mail : ztanzeyu@gmail.com
 */
public class StyleConfigBuilder {
	protected StyleConfig config = null;
	protected ArrayList<StyleConfig> configs = null;

	public StyleConfigBuilder(){
		super();
		configs = new ArrayList();
	}

	public StyleConfigBuilder(String text){
		super();
		configs = new ArrayList();
		config = new StyleConfig(text);
		if(text != null){
			config.setStart(0).setEnd(text.length());
		}
	}

	public ArrayList<StyleConfig> build(){
		if(configs.size() == 0 && config != null){
			configs.add(config);
		}
		return configs;
	}

	public String getText() {
		return config.text;
	}

	public StyleConfigBuilder setText(String text) {
		config.text = text;
		return this;
	}

	public String getBackgroudColor() {
		return config.backgroudColor;
	}

	public StyleConfigBuilder setBackgroudColor(String backgroudColor) {
		config.backgroudColor = backgroudColor;
		return this;
	}

	public String getTextColor() {
		return config.textColor;
	}

	public StyleConfigBuilder setTextColor(String textColor) {
		config.textColor = textColor;
		return this;
	}

	public int getTextSize() {
		return config.textSize;
	}

	public StyleConfigBuilder setTextSize(int textSize) {
		config.textSize = textSize;
		return this;
	}

	public int getDrawableResID() {
		return config.drawableResID;
	}

	public StyleConfigBuilder setDrawableResID(int drawableResID) {
		config.drawableResID = drawableResID;
		return this;
	}

	public int getDrawableWidth() {
		return config.drawableWidth;
	}

	public StyleConfigBuilder setDrawableWidth(int drawableWidth) {
		config.drawableWidth = drawableWidth;
		return this;
	}

	public int getDrawableHeight() {
		return config.drawableHeight;
	}

	public StyleConfigBuilder setDrawableHeight(int drawableHeight) {
		config.drawableHeight = drawableHeight;
		return this;
	}

	public String getPrefix() {
		return config.prefix;
	}

	public StyleConfigBuilder setPrefix(String prefix) {
		config.prefix = prefix;
		return this;
	}

	public String getSuffix() {
		return config.suffix;
	}

	public StyleConfigBuilder setSuffix(String suffix) {
		config.suffix = suffix;
		return this;
	}

	public int getStart() {
		return config.start;
	}

	public StyleConfigBuilder setStart(int start) {
		config.start = start;
		return this;
	}

	public int getEnd() {
		return config.end;
	}

	public StyleConfigBuilder setEnd(int end) {
		config.end = end;
		return this;
	}

	public View.OnClickListener getClickListener() {
		return config.clickListener;
	}

	public StyleConfigBuilder setClickListener(View.OnClickListener clickListener) {
		config.clickListener = clickListener;
		return this;
	}
}
