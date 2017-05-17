package acore.widget;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;

import acore.widget.multifunction.view.MultifunctionTextView;
import core.xiangha.emj.tools.EmjParseMsgUtil;

/**
 * PackageName : acore.widget
 * Created by MrTrying on 2016/7/29 15:40.
 * E_mail : ztanzeyu@gmail.com
 */
public class TextViewShow extends MultifunctionTextView {

	public int faceWH = 0;

	public TextViewShow(Context context) {
		super(context);
	}

	public TextViewShow(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TextViewShow(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		if (text != null) {
			//解析Emoji
			String str = core.xiangha.emj.tools.EmjParser.getInstance(getContext()).parseEmoji(text.toString());
			SpannableStringBuilder style;
			if(faceWH != 0){
				style = EmjParseMsgUtil.convetToHtml(getContext(), str,faceWH);
			}else {
				style = EmjParseMsgUtil.convetToHtml(getContext(), str);
			}
			super.setText(style);
		}
	}

	public void setText(MultifunctionText multifunctionText) {
		this.mMultifunctionText = multifunctionText;
		if (mMultifunctionText == null) {
			return;
		}
		String text = multifunctionText.getText();
		if (text != null) {
			//解析Emoji
			SpannableStringBuilder style;
			if(faceWH != 0){
				style = EmjParseMsgUtil.convetToHtml(getContext(), text.toString(),faceWH);
			}else {
				style = EmjParseMsgUtil.convetToHtml(getContext(), text.toString());
			}
			super.setText(style);
		}
	}

	public int getFaceWH() {
		return faceWH;
	}

	public void setFaceWH(int faceWH) {
		this.faceWH = faceWH;
	}
}
