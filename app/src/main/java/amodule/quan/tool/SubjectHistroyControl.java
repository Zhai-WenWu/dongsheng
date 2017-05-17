package amodule.quan.tool;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import amodule.user.db.BrowseHistorySqlite;
import amodule.user.db.HistoryData;

/**
 * PackageName : amodule.quan.tool
 * Created by MrTrying on 2016/9/27 18:36.
 * E_mail : ztanzeyu@gmail.com
 */

public class SubjectHistroyControl {
    private static volatile SubjectHistroyControl mInstance = null;

    public static SubjectHistroyControl getInance(){
        if(mInstance == null){
            synchronized (SubjectHistroyControl.class){
                if(mInstance == null){
                    mInstance = new SubjectHistroyControl();
                }
            }
        }
        return mInstance;
    }

    /** 保存浏览记录 */
    public void saveHistoryToDB(final Context context, final Map<String, String> subject_map, final Map<String, String> floors_0_map) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject = handlerJSONData(subject_map, floors_0_map);
                HistoryData data = new HistoryData();
                data.setCode(subject_map.get("code"));
                data.setBrowseTime(System.currentTimeMillis());
                data.setDataJson(jsonObject.toString());
                BrowseHistorySqlite sqlite = new BrowseHistorySqlite(context);
                int id = sqlite.insertSubject(BrowseHistorySqlite.TB_SUBJECT_NAME, data);
                Log.w("BrowseHistorySqlite","id=" + id);
            }
        }).start();
    }

    /** 处理json数据 */
    private JSONObject handlerJSONData(final Map<String, String> subject_map, final Map<String, String> floors_0_map) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", subject_map.get("code"));
            jsonObject.put("commentNum", subject_map.get("commentNum"));
            jsonObject.put("likeNum", subject_map.get("likeNum"));
            jsonObject.put("title", subject_map.get("title"));
            jsonObject.put("hasVideo", "2".equals(floors_0_map.get("hasVideo")) ? 2 : 1);

            ArrayList<Map<String, String>> contents = StringManager.getListMapByJson(floors_0_map.get("content"));
            if (contents.size() > 0) {
                //初始化标志位
                boolean hasImage = false;
                boolean hasText = false;
                //循环获取数据，以免数据为null
                for(int index = 0 ; index < contents.size() ; index ++){
                    Map<String, String> map = contents.get(index);
                    if(!hasImage && !TextUtils.isEmpty(map.get("img"))){
                        jsonObject.put("img", map.get("img"));
                        hasImage = true;
                    }
                    List<Map<String,String>> texts = StringManager.getListMapByJson(map.get("text"));
                    if(!hasText){
                        if (contents.size() > 0) {
                            map = texts.get(0);
                            if(!TextUtils.isEmpty(map.get(""))){
                                jsonObject.put("content", map.get(""));
                                hasText = true;
                            }
                        } else {
                            jsonObject.put("content", "");
                            hasText = true;
                        }
                    }

                    if(hasImage && hasText){
                        break;
                    }
                }
            } else {
                jsonObject.put("content", "");
                jsonObject.put("img", "");
            }
            //处理小视频数据
            if(floors_0_map.containsKey("selfVideo")){
                ArrayList<Map<String, String>> video = StringManager.getListMapByJson(floors_0_map.get("selfVideo"));
                if(video.get(0).containsKey("sImgUrl")&&!TextUtils.isEmpty((video.get(0).get("sImgUrl")))){
                    jsonObject.put("img", video.get(0).get("sImgUrl"));
                }
            }
            ArrayList<Map<String, String>> customers = StringManager.getListMapByJson(floors_0_map.get("customer"));
            if (customers.size() > 0) {
                jsonObject.put("nickName", customers.get(0).get("nickName") + "");
            } else {
                jsonObject.put("nickName", "");
            }
        } catch (Exception e) {

        }
        return jsonObject;
    }
}
