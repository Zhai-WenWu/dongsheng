package acore.widget.multifunction;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.StringManager;
import acore.widget.multifunction.base.StyleConfig;

/**
 * PackageName : acore.widget.multifunction
 * Created by MrTrying on 2016/7/27 16:05.
 * E_mail : ztanzeyu@gmail.com
 */
public class MentionStyleBulider extends EmojiStyleBuilder {
	public static final String mentionTag = "@";
	public String textColor = "#26BC89";
	private String realContent;

	public MentionStyleBulider(Context context, String text, MentionClickCallback callback) {
		super(context,text);
		this.callback = callback;
		parse(content);
	}

	public void parse(String contentJson) {
		ArrayList<Map<String, String>> contentList = StringManager.getListMapByJson(contentJson);
		if (contentList.size() > 0) {
			realContent = contentList.get(0).get("");
			if (contentList.size() > 1) {
				//@用户的相关数据
				String mentionCustomersJson = contentList.get(1).get("");
				ArrayList<Map<String, String>> mentionCustomersList = StringManager.getListMapByJson(mentionCustomersJson);
				for (Map<String, String> cusMap : mentionCustomersList) {
					String name = cusMap.get("nickName");
					final String cusCode = cusMap.get("code");
					String text = mentionTag + name;
					StyleConfig config = new StyleConfig(text);
					config.setTextColor(textColor);
					config.setStart(realContent.indexOf(text));
					config.setEnd(realContent.indexOf(text) + text.length());
					config.setClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(callback != null){
								callback.onMentionClick(v,cusCode);
							}
						}
					});
					configs.add(config);
				}
			}
		}
	}

	public String getContent(){
		return realContent;
	}

	private MentionClickCallback callback = null;

	public interface MentionClickCallback {
		public void onMentionClick(View v, String userCode);
	}
}
