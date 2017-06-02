package acore.widget.multifunction.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import acore.widget.multifunction.linstener.TextViewTagLongClick;
import acore.widget.multifunction.base.StyleConfig;

/**
 * PackageName : acore.widget
 * Created by MrTrying on 2016/7/22 11:37.
 * E_mail : ztanzeyu@gmail.com
 */
public class MultifunctionTextView extends TextView {

	private boolean isLongClick = false;

	/** 长按事件 */
	protected TextViewTagLongClick mTextViewTagLongClick;

	protected MultifunctionText mMultifunctionText = null;

	public MultifunctionTextView(Context context) {
		this(context, null, 0);
	}

	public MultifunctionTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MultifunctionTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		//设置链接可点
		setMovementMethod(LinkMovementMethod.getInstance());

		//初始化长按监听
		mTextViewTagLongClick = new TextViewTagLongClick(context, this);
		mTextViewTagLongClick.setOnLongClickListener(new TextViewTagLongClick.OnLongClickListener() {
			@Override
			public void onLongClick() {
				isLongClick = true;
			}
		});
	}

	public void setText(SpannableStringBuilder style) {
		if (mMultifunctionText != null) {
			style = parseTextStyle(style, mMultifunctionText.getConfigs());
		}
		super.setText(style,BufferType.NORMAL);
	}

	public void setText(MultifunctionText multifunctionText) {
		this.mMultifunctionText = multifunctionText;
		if (mMultifunctionText == null) {
			return;
		}
		String text = multifunctionText.getText();
		if (text != null) {
			//解析Emoji
			SpannableStringBuilder style = new SpannableStringBuilder(text);
			style = parseTextStyle(style, mMultifunctionText.getConfigs());
			super.setText(style, BufferType.NORMAL);
		}
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		if(isLongClick){
			isLongClick = false;
			return;
		}
		super.setOnClickListener(l);
	}

	/** 解析 */
	private SpannableStringBuilder parseTextStyle(SpannableStringBuilder style, ArrayList<StyleConfig> configsArray) {
		for (final StyleConfig config : configsArray) {
			if(style.length() <= 0
					|| style.length() < config.getEnd()
					|| config.getStart() < 0){
				continue;
			}
			//背景色
			if (!TextUtils.isEmpty(config.getBackgroudColor())) {
				style.setSpan(new BackgroundColorSpan(Color.parseColor(config.getBackgroudColor())),
						config.getStart(), config.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			//文字颜色
			if (!TextUtils.isEmpty(config.getTextColor())) {
				style.setSpan(new ForegroundColorSpan(Color.parseColor(config.getTextColor())),
						config.getStart(), config.getEnd(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			}
			//文字大小
			if (config.getTextSize() != -1) {
				style.setSpan(new AbsoluteSizeSpan(config.getTextSize()),
						config.getStart(), config.getEnd(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
			}
			if (config.getDrawableResID() > 0) {
				Drawable drawable = getContext().getResources().getDrawable(config.getDrawableResID());
				drawable.setBounds(0, 0, config.getDrawableWidth(), config.getDrawableHeight());
				style.setSpan(new DrawableTagSpan(drawable, config.getText(), config.getTextColor(), config.getTextSize()),
						config.getStart(), config.getEnd(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			}
			if (config.getClickListener() != null) {
				style.setSpan(new ClickableSpan() {
					//  在onClick方法中可以编写单击链接时要执行的动作
					@Override
					public void onClick(View widget) {
						config.getClickListener().onClick(widget);
					}

					@Override
					public void updateDrawState(TextPaint ds) {
						super.updateDrawState(ds);
						if (!TextUtils.isEmpty(config.getTextColor())) {
							ds.setColor(Color.parseColor(config.getTextColor()));
						}
						// 设置下划线
						ds.setUnderlineText(config.isUnderline());
					}
				}, config.getStart(), config.getEnd(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return style;
	}

	/** 特殊ImageSpan */
	public class DrawableTagSpan extends ImageSpan {
		public static final String replace = "replace";
		String realText = null;
		String textColor = "";
		int textSize = 0;
		int drawableWidth = 0;
		int drawableHeight = 0;

		public DrawableTagSpan(Drawable d, String realText, String textColor, int textSize) {
			super(d);
			this.realText = realText;
			this.textColor = textColor;
			this.textSize = textSize;
		}

		@Override
		public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
			Rect bounds2 = new Rect();
			paint.getTextBounds("测试：ijk", 0, 6, bounds2);

			// image to draw
			Drawable drawable = getDrawable();
			Rect rect = drawable.getBounds();
			drawableWidth = rect.width();
			drawableHeight = rect.height();
			// font metrics of text to be replaced
			Paint.FontMetricsInt fm = paint.getFontMetricsInt();
			int transY = Math.abs((Math.abs(fm.ascent - fm.descent) - drawableHeight) / 2) + Math.abs(fm.top - bounds2.top);
			canvas.save();
			canvas.translate(x, transY);
			drawable.draw(canvas);
			canvas.restore();

			if (!TextUtils.isEmpty(realText) && !replace.equals(realText)) {
				paint.setTextSize(textSize);
				if (!TextUtils.isEmpty(textColor)) {
					paint.setColor(Color.parseColor(textColor));
				}
				paint.setTextAlign(Paint.Align.CENTER);
				Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
				//计算文字高度
				float fontHeight = Math.abs(fontMetrics.descent - fontMetrics.ascent);
				//计算文字baseline
				float textBaseY = transY + Math.abs(drawableHeight - fontHeight) / 2 + Math.abs(fontMetrics.ascent);
				canvas.drawText(realText, drawableWidth / 2, textBaseY, paint);
			}
		}
	}

	/** 多功能text */
	public static class MultifunctionText {
		int offset = 0;
		StringBuffer stringBuffer = new StringBuffer();
		ArrayList<StyleConfig> configsArray = new ArrayList<>();
		Context context;

		public MultifunctionText() {
		}

		public MultifunctionText addStyle(String text, ArrayList<StyleConfig> configs) {
			if (configs != null) {
				for (StyleConfig config : configs) {
					config.setStart(config.getStart() + offset);
					config.setEnd(config.getEnd() + offset);
					configsArray.add(config);
				}
			}
			stringBuffer.append(text);
			offset = stringBuffer.length();
			return this;
		}

		public String getText() {
			return stringBuffer.toString();
		}

		public ArrayList<StyleConfig> getConfigs() {
			return configsArray;
		}
	}

	/**
	 * 设置copy的内容
	 *
	 * @param copyText
	 */
	public void setCopyText(String copyText) {
		mTextViewTagLongClick.setCopyText(copyText);
	}

	public void setNormBackColor(int color){
		mTextViewTagLongClick.setNormBackColor(color);
	}

	public void setChoseBackColor(int color) {
		mTextViewTagLongClick.setChoseBackColor(color);
	}

	/**
	 * 设置
	 *
	 * @param listener
	 */
	public void setRightClicker(OnClickListener listener) {
		mTextViewTagLongClick.setRightClicker(listener);
	}

	public void setRightClicker(String text, OnClickListener listener) {
		mTextViewTagLongClick.setmRightBtnName(text);
		mTextViewTagLongClick.setRightClicker(listener);
	}

	public void setUserClicker(OnClickListener listener) {
		mTextViewTagLongClick.setUserClicker(listener);
	}

	public void setTypeOwer(int typeOwer) {
		mTextViewTagLongClick.setTypeOwer(typeOwer);
	}

	public void setHaveCopyFunction(boolean isHaveCopy) {
		mTextViewTagLongClick.setHaveCopyFunction(isHaveCopy);
	}

	public static ArrayList<String> getStringList(String text, char star, char end) {
		ArrayList<String> indexList = new ArrayList<>();
		int index = 0;
		int begin = 0;
		int over = 0;
		if (TextUtils.isEmpty(text)) {
			return indexList;
		}
		while (index < text.length()) {
			if (text.charAt(index) == star) {
				begin = index;
				index++;
				while (index < text.length() && text.charAt(index) != end) {
					if (text.charAt(index) == star) begin = index;
					index++;
				}
				if (index < text.length()) {
					over = index;
					indexList.add(text.substring(begin + 1, over));
				}
			}
			index++;
		}
		return indexList;
	}
}
