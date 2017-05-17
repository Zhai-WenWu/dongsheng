package acore.widget.multifunction;

import android.content.Context;

import acore.widget.multifunction.base.StyleConfigBuilder;

/**
 * PackageName : acore.widget.multifunction
 * Created by MrTrying on 2016/7/29 15:51.
 * E_mail : ztanzeyu@gmail.com
 */
public class EmojiStyleBuilder extends StyleConfigBuilder {
	protected Context  context;
	protected String content = "";

	public EmojiStyleBuilder(Context context,String text){
		super(text);
		this.context = context;
		this.content = core.xiangha.emj.tools.EmjParser.getInstance(context).parseEmoji(text);
	}
}
