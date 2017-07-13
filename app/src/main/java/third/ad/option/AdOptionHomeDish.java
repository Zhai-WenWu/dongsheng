package third.ad.option;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import acore.tools.Tools;

/**
 *拼装数据
 */
public class AdOptionHomeDish extends AdOptionList {

    private int index = 0;

    public AdOptionHomeDish(String[] adPlayIds) {
        super(adPlayIds);
    }

    /**
     * 获取到广告对把数据加到集合中
     * @param title
     * @param desc
     * @param iconUrl
     * @param imageUrl
     * @param adTag----广告是百度或广点通
     */
    @Override
    public Map<String, String> getAdListItemData(final String title, final String desc, final String iconUrl,
                                                 String imageUrl, String adTag,String isDownloadApp) {
        Map<String, String> map = new HashMap<>();
        map.put("name", title );
        map.put("img", imageUrl);
        map.put("content", desc);
        map.put("allClick", String.valueOf(Tools.getRandom(6000, 20000)));
        map.put("commentNum", String.valueOf(Tools.getRandom(5, 20)));
        map.put("adClass", adTag);
        if(!TextUtils.isEmpty(isDownloadApp))
            map.put("isDownloadApp", isDownloadApp);
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("nickName", title);
            jsonArray.put(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        map.put("customer", jsonArray.toString());

        return map;
    }

}
