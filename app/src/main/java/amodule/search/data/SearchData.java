package amodule.search.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.Map;

import aplug.basic.InternetCallback;

/**
 * Created by ：airfly on 2016/10/10 16:26.
 */

public interface SearchData {

    ArrayList<Map<String, String>> getHistoryWords();

    ArrayList<Map<String, String>> getMatchWords(Context context, String key);

    void getHotWords(Context conntext, InternetCallback callBack);

    void getCaipuAndShicaiResult(Context context,String key,int currentPage, InternetCallback callBack);

    void getCaidanResult(Context context,String key,int currentPage, InternetCallback callBack);

    void getZhishiResult(Context context,String key,int currentPage, InternetCallback callBack);

    void getTieziResult(Context context, String key, int currentPage, InternetCallback callBack);

    void getHayouResult(Context context, String key, int currentPage, InternetCallback callBack);
}
