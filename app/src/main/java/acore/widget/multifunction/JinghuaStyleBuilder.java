package acore.widget.multifunction;

import android.content.Context;

import com.xiangha.R;

import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.multifunction.base.StyleConfigBuilder;

/**
 * PackageName : acore.widget.multifunction
 * Created by MrTrying on 2016/7/28 14:16.
 * E_mail : ztanzeyu@gmail.com
 */
public class JinghuaStyleBuilder extends StyleConfigBuilder {

	public JinghuaStyleBuilder(Context context, String text, int drawableResID){
		super(text);
		config.setDrawable(drawableResID, Tools.getDimen(context, R.dimen.dp_30),Tools.getDimen(context, R.dimen.dp_17))
				.setTextSize(ToolsDevice.sp2px(context, Tools.getDimenSp(context, R.dimen.sp_10)))
				.setStart(0)
				.setEnd(text.length());
	}
}
