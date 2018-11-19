package amodule.search.data;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.search.bean.WordBean;
import amodule.search.db.WordsSqlite;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import xh.basic.tool.UtilFile;

/**
 * Created by ：airfly on 2016/10/10 16:12.
 */

public class SearchDataImp implements SearchData {


    //考虑老版本中，保存在本地是数据中包含"\u3000",需要10条记录
    @Override
    public ArrayList<Map<String, String>> getHistoryWords() {

        ArrayList<Map<String, String>> listSearchHistory = new ArrayList<Map<String, String>>();
        String[] searchWords = UtilFile.readFile(UtilFile.getDataDir() + FileManager.file_searchHis).split("\r\n");
        Assert.assertEquals(true, searchWords != null);
        int j = 0;
        for (int i = 0; i < searchWords.length && j < 10; i++) {
            String hisSstr = searchWords[i];
            if (!TextUtils.isEmpty(hisSstr)) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("search", searchWords[i]);
                listSearchHistory.add(map);
                j++;
            }
        }

        return listSearchHistory;
    }

    @Override
    public ArrayList<Map<String, String>> getMatchWords(Context context, String key) {
        ArrayList<Map<String, String>> matchwordList = new ArrayList<Map<String, String>>();
        List<WordBean> wordBeanList = WordsSqlite.getInstance(context).querryByName(key);
        if (wordBeanList != null && wordBeanList.size() > 0) {
            HashMap<String, String> map;
            for (WordBean word : wordBeanList) {
                map = new HashMap<>();
                map.put("matchword", word.getValue());
                matchwordList.add(map);
            }
        }
        return matchwordList;
    }


    //获取到热词后，考虑保持到本地
    @Override
    public void getHotWords(Context context, final InternetCallback callBack) {

        if (callBack == null) return;
        ReqEncyptInternet.in().doGetEncypt(StringManager.api_getHotWords, callBack);
    }

    public void getRandomHotWord(final InternetCallback callBack){
        getHotWords(null, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                String word = "";
                if(i >= ReqInternet.REQ_OK_STRING){
                    List<Map<String,String>> words = StringManager.getListMapByJson(o);
                    if(!words.isEmpty()){
                        int random = Tools.getRandom(0,words.size());
                        String wordTemp =  words.get(random).get("");
                        word = wordTemp != null ? wordTemp : "";
                    }
                }
                if(callBack != null){
                    callBack.loaded(ReqInternet.REQ_OK_STRING,s,word);
                }
            }
        });
    }

    @Override
    public void getCaipuAndShicaiResult(Context context, String key, int currentPage, InternetCallback callBack) {
        if (callBack == null) return;
        String getUrl = StringManager.api_getCaipu + "?keywords=" + key + "&page=" + currentPage;
        ReqEncyptInternet.in().doGetEncypt(getUrl, callBack);
    }

    @Override
    public void getCaidanResult(Context context, String key, int currentPage, InternetCallback callBack) {
        if (callBack == null) return;
        //接口不使用
//        String getUrl = StringManager.api_getCaipu + "?type=caidan&keywords=" + key + "&page=" + currentPage;
//        ReqInternet.in().doGet(getUrl, callBack);
    }

    @Override
    public void getZhishiResult(Context context, String key, int currentPage, InternetCallback callBack) {
        if (callBack == null) return;
        //接口不使用
//        String getUrl = StringManager.api_getCaipu + "?type=zhishi&keywords=" + key + "&page=" + currentPage;
//        ReqInternet.in().doGet(getUrl, callBack);
    }

    @Override
    public void getTieziResult(Context context, String key, int currentPage, final InternetCallback callBack) {
        if (callBack == null) return;
        String getUrl = StringManager.api_getTiezi + "?keywords=" + key + "&page=" + currentPage;
        ReqInternet.in().doGet(getUrl, callBack);
    }

    @Override
    public void getHayouResult(Context context, String key, int currentPage, final InternetCallback callBack) {
        if (callBack == null) return;
        String url = StringManager.api_soList + "?type=customer&s=" + key + "&page=" + currentPage;
        ReqInternet.in().doGet(url, callBack);
    }
}
