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
     *
     * @param searchWord
     */
    public static void saveSearchWord(String searchWord, String searchCode) {
        String NWELINE = "\r\n";
        if (searchWord != null && searchWord.trim().length() > 0 && searchCode != null && searchCode.trim().length() > 0) {
            String wordHis = UtilFile.readFile(UtilFile.getDataDir() + FileManager.file_topicSearchHis);
            if (wordHis.length() == 0) {
                wordHis = NWELINE;
            }
            // 兼容老文件
            else if (wordHis.indexOf(NWELINE) != 0) {
                wordHis = NWELINE + wordHis + NWELINE;
            }
            // 已存在放到首位
            if (wordHis.indexOf(NWELINE + searchWord + NWELINE) >= 0) {
                wordHis = NWELINE + searchWord + wordHis.replace(NWELINE + searchWord + NWELINE, NWELINE);
            } else {
                String[] wordHiss = wordHis.split(NWELINE);
                if (wordHiss.length < 51) {
                    wordHis = NWELINE + searchWord + wordHis;
                } else {
                    wordHis = NWELINE + searchWord;
                    for (int i = 1; i < wordHiss.length - 1; i++) {
                        wordHis += NWELINE + wordHiss[i];
                    }
                    wordHis += NWELINE;
                }
            }
            UtilFile.saveFileToCompletePath(UtilFile.getDataDir() + FileManager.file_topicSearchHis, wordHis, false);

            String codeHis = UtilFile.readFile(UtilFile.getDataDir() + FileManager.file_topicSearchCode);
            if (codeHis.length() == 0) {
                codeHis = NWELINE;
            }
            // 兼容老文件
            else if (codeHis.indexOf(NWELINE) != 0) {
                codeHis = NWELINE + codeHis + NWELINE;
            }
            // 已存在放到首位
            if (codeHis.indexOf(NWELINE + searchCode + NWELINE) >= 0) {
                codeHis = NWELINE + searchCode + codeHis.replace(NWELINE + searchCode + NWELINE, NWELINE);
            } else {
                String[] codeHiss = codeHis.split(NWELINE);
                if (codeHiss.length < 51) {
                    codeHis = NWELINE + searchCode + codeHis;
                } else {
                    codeHis = NWELINE + searchCode;
                    for (int i = 1; i < codeHiss.length - 1; i++) {
                        codeHis += NWELINE + codeHiss[i];
                    }
                    codeHis += NWELINE;
                }
            }
            UtilFile.saveFileToCompletePath(UtilFile.getDataDir() + FileManager.file_topicSearchCode, wordHis, false);
        }
    }


    public static ArrayList<ArrayList<Map<String, String>>> getHistoryWords() {

        ArrayList<ArrayList<Map<String, String>>> listSearchHistory = new ArrayList<ArrayList<Map<String, String>>>();

        ArrayList<Map<String, String>> searchWordMap = new ArrayList<>();
        String[] searchWords = UtilFile.readFile(UtilFile.getDataDir() + FileManager.file_topicSearchHis).split("\r\n");
        Assert.assertEquals(true, searchWords != null);
        int j = 0;
        for (int i = 0; i < searchWords.length && j < 10; i++) {
            String hisSstr = searchWords[i];
            if (!TextUtils.isEmpty(hisSstr)) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("hot", searchWords[i]);
                searchWordMap.add(map);
                j++;
            }
        }
        listSearchHistory.add(searchWordMap);


        ArrayList<Map<String, String>> searchCodeMap = new ArrayList<>();
        String[] searchCodes = UtilFile.readFile(UtilFile.getDataDir() + FileManager.file_topicSearchCode).split("\r\n");
        Assert.assertEquals(true, searchCodes != null);
        int k = 0;
        for (int i = 0; i < searchCodes.length && k < 10; i++) {
            String hisSstr = searchCodes[i];
            if (!TextUtils.isEmpty(hisSstr)) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("hot", searchCodes[i]);
                searchCodeMap.add(map);
                k++;
            }
        }
        listSearchHistory.add(searchCodeMap);


        return listSearchHistory;
    }

    public static void deleteHistoryWord() {
        FileManager.delDirectoryOrFile(UtilFile.getDataDir() + FileManager.file_topicSearchHis);
        FileManager.delDirectoryOrFile(UtilFile.getDataDir() + FileManager.file_topicSearchCode);
    }
}
