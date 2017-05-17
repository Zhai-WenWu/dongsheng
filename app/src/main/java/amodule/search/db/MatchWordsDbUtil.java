package amodule.search.db;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import amodule.search.bean.WordBean;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;

/**
 * Created by ：airfly on 2016/10/13 18:15.
 */

public class MatchWordsDbUtil {

    /**
     * 检查更新
     * @param context
     */
    public void checkUpdateMatchWordsDb(Context context) {
        String createTime = "-1";
        String createTimeStr = (String) FileManager.loadShared(context,
                FileManager.MATCH_WORDS, FileManager.MATCH_WORDS_CREATE_TIME);
        if (!TextUtils.isEmpty(createTimeStr)) {
            createTime = createTimeStr;
        }
        String url = StringManager.api_matchWords + "?createTime=" + createTime;
        ReqInternet.in().doGet(url, new InternetCallback(context) {
            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= UtilInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> listMapByJson = StringManager.getListMapByJson(o);
                    if (listMapByJson != null && listMapByJson.size() > 0) {
                        Map<String, String> stringMap = listMapByJson.get(0);
                        if (stringMap != null && stringMap.size() > 0) {

                            final String createTime = stringMap.get("createTime");
                            final String matchWordsUrl = stringMap.get("url");
                            String isUpdate = stringMap.get("isUpdate");

                            if (Integer.valueOf(isUpdate) == 2) {
                                if (!TextUtils.isEmpty(matchWordsUrl)) {
                                    Handler handler = new Handler(Looper.getMainLooper());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            downloadMatchWords(matchWordsUrl, createTime);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 下载匹配词文件，并保存到数据库
     * @param url
     * @param createTime
     */
    private void downloadMatchWords(final String url, final String createTime) {
        ReqInternet.in().getInputStream(url, new InternetCallback(XHApplication.in()) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if (flag >= ReqInternet.REQ_OK_IS) {
                    if(insertStreamToDb(XHApplication.in(), (InputStream) msg)){
                        FileManager.saveShared(XHApplication.in(),
                                FileManager.MATCH_WORDS,  FileManager.MATCH_WORDS_CREATE_TIME, createTime);
                    }
                }
            }
        });
    }

    /**
     * 存入数据库
     * @param context
     * @param in
     * @return
     */
    private boolean insertStreamToDb(Context context, InputStream in) {
        boolean flag = false;
        BufferedReader buf = null;
        List<WordBean> data = new ArrayList<WordBean>();
        try {
            buf = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = buf.readLine()) != null) {
                WordBean r1 = new WordBean();
                r1.setValue(line);
                data.add(r1);
            }
            flag = WordsSqlite.getInstance(context).insert(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (buf != null)
                    buf.close();
            } catch (IOException e) {
                if (buf != null)
                    buf = null;
            }
            return flag;
        }
    }
}
