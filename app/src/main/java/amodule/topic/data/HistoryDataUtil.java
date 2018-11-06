package amodule.topic.data;

import android.text.TextUtils;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.tools.FileManager;
import xh.basic.tool.UtilFile;

public class HistoryDataUtil {

    /**
     * 保存搜索字
     * @param searchWord
     */
    public static void saveSearchWord(String searchWord) {
        String NWELINE = "\r\n";
        if (searchWord != null && searchWord.trim().length() > 0){
            String his = UtilFile.readFile(UtilFile.getDataDir() + FileManager.file_topicSearchHis);
            if (his.length() == 0) {
                his = NWELINE;
            }
            // 兼容老文件
            else if (his.indexOf(NWELINE) != 0) {
                his = NWELINE + his + NWELINE;
            }
            // 已存在放到首位
            if (his.indexOf(NWELINE + searchWord + NWELINE) >= 0) {
                his = NWELINE + searchWord + his.replace(NWELINE + searchWord + NWELINE, NWELINE);
            } else {
                String[] hiss = his.split(NWELINE);
                if (hiss.length < 51) {
                    his = NWELINE + searchWord + his;
                } else {
                    his = NWELINE + searchWord;
                    for (int i = 1; i < hiss.length - 1; i++) {
                        his += NWELINE + hiss[i];
                    }
                    his += NWELINE;
                }
            }
            UtilFile.saveFileToCompletePath(UtilFile.getDataDir() + FileManager.file_topicSearchHis, his, false);
        }
    }


    public static  ArrayList<Map<String, String>> getHistoryWords() {

        ArrayList<Map<String, String>> listSearchHistory = new ArrayList<Map<String, String>>();
        String[] searchWords = UtilFile.readFile(UtilFile.getDataDir() + FileManager.file_topicSearchHis).split("\r\n");
        Assert.assertEquals(true, searchWords != null);
        int j = 0;
        for (int i = 0; i < searchWords.length && j < 10; i++) {
            String hisSstr = searchWords[i];
            if (!TextUtils.isEmpty(hisSstr)) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("hot", searchWords[i]);
                listSearchHistory.add(map);
                j++;
            }
        }

        return listSearchHistory;
    }

    public static void deleteHistoryWord(){
        FileManager.delDirectoryOrFile(UtilFile.getDataDir() + FileManager.file_topicSearchHis);
    }
}
