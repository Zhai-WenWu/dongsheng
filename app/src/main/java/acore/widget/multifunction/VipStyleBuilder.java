package acore.widget.multifunction;

import android.content.Context;

import com.xiangha.R;

import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.multifunction.base.StyleConfigBuilder;

/**
 * Description : //TODO
 * PackageName : acore.widget.multifunction
 * Created by tanzeyu on 2018/4/3 16:59.
 * e_mail : ztanzeyu@gmail.com
 */
public class VipStyleBuilder extends StyleConfigBuilder {

    public VipStyleBuilder(Context context, String text, int drawableResID){
        super(text);
        config.setDrawable(drawableResID, Tools.getDimen(context, R.dimen.dp_28),Tools.getDimen(context, R.dimen.dp_16))
                .setTextSize(ToolsDevice.sp2px(context, Tools.getDimenSp(context, R.dimen.sp_9)))
                .setStart(0)
                .setEnd(text.length());
    }
}
