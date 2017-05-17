package acore.widget.multifunction;

import android.view.View;

import java.util.Map;

import acore.tools.StringManager;
import acore.widget.multifunction.base.StyleConfig;
import acore.widget.multifunction.base.StyleConfigBuilder;

/**
 * PackageName : acore.widget.multifunction.base
 * Created by MrTrying on 2016/7/27 18:19.
 * E_mail : ztanzeyu@gmail.com
 */
public class ReplyStyleBuilder extends StyleConfigBuilder {
	public static final String landlordTag = "(楼主)";
	public static final String replyText = "回复 ";
	public String textColor = "#26BC89";
	String realText = "";
	String userCode = "";

	public ReplyStyleBuilder(String subjectOwnerCode, String floorOwnerCode,
	                         String customerJson, String replyJson, final ReplyClickCallback callback) {
		super();
		StringBuffer stringBuffer = new StringBuffer();
		Map<String, String> customerMap = StringManager.getFirstMap(customerJson);
		Map<String, String> replyMap = StringManager.getFirstMap(replyJson);
		//添加回复人
		String name = "";
		if (customerIsEmpty(customerMap)) {
			name = customerMap.get("nickName");
			userCode = customerMap.get("code");
			stringBuffer.append(name);
			//是否是楼主
			if (userCode.equals(subjectOwnerCode)) {
				stringBuffer.append(landlordTag);
				name += landlordTag;
			}
		}
		//添加被回复的人
		if (customerIsEmpty(replyMap)) {
			String replyCode = replyMap.get("code");
			//是否是层主，如果是不显示
			if (!replyCode.equals(floorOwnerCode)) {
				stringBuffer.append(replyText)
						.append(replyMap.get("nickName"));
				//是否是楼主
				if (replyCode.equals(subjectOwnerCode)) {
					stringBuffer.append(landlordTag);
				}
			}
		}
		//添加 ：
		stringBuffer.append("：");
		realText = stringBuffer.toString();
		StyleConfig config = new StyleConfig(name);
		config.setText(realText)
				.setTextColor(textColor)
				.setStart(realText.indexOf(name))
				.setEnd(realText.indexOf(name) + name.length())
				.setClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (callback != null) {
							callback.onReplyClick(v, userCode);
						}
					}
				});
		configs.add(config);
	}

	/**
	 * 判断用户的map是否为null
	 *
	 * @param customerMap
	 *
	 * @return
	 */
	private static boolean customerIsEmpty(Map<String, String> customerMap) {
		return customerMap != null
				&& customerMap.containsKey("nickName")
				&& customerMap.containsKey("code");
	}

	public String getContent() {
		return realText;
	}

	public interface ReplyClickCallback {
		public void onReplyClick(View v, String userCode);
	}
}
