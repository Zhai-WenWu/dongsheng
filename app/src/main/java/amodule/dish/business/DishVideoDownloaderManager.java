package amodule.dish.business;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.video.control.MediaControl;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.FileDownloadCallback;
import xh.basic.internet.UtilInternet;
import xh.basic.internet.progress.UtilInternetFile;
import xh.basic.tool.UtilString;

/**
 * Created by ：fei_teng on 2016/10/26 10:57.
 */
public class DishVideoDownloaderManager {

    private volatile static DishVideoDownloaderManager downloaderManager;
    private  DownloadCallback callback = null;
    List<Map<String, String>> readyDownloadList = new CopyOnWriteArrayList<Map<String, String>>();
    private int totalLeng;
    private int hasDownCount;
    private String currentDownloadUrl;
    private volatile boolean isRunning;

    private final String VIDEO_HEAN_PATH = MediaControl.path_voide+"/xiangha_start.mp4";
    private final String VIDEO_TAIL_PATH = MediaControl.path_voide+"/xiangha_end.mp4";
    private final String VIDEO_MUSIC_PATH =  MediaControl.path_voide+"/xiangha_bgm.aac";
    private final String VIDEO_FONT_PATH =  MediaControl.path_voide+"/fonts/font.ttf";


    private DishVideoDownloaderManager() {
    }

    public static DishVideoDownloaderManager getDishVideoManagerInstance() {

        if (downloaderManager == null) {
            synchronized (DishVideoDownloaderManager.class) {
                if (downloaderManager == null) {
                    downloaderManager = new DishVideoDownloaderManager();
                }
            }
        }
        return downloaderManager;
    }


    public void setCallback(DownloadCallback downloadCallback) {
        callback = downloadCallback;
    }


    public void releaseCallback(){
        callback = new DownloadCallback() {
            @Override
            public void onGetTotalLength(long total) {

            }

            @Override
            public void onProgress(long current, long total) {

            }

            @Override
            public void onFail() {
                Tools.showToast(XHApplication.in().getApplicationContext(), "下载失败");
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onSuccess() {
                Tools.showToast(XHApplication.in().getApplicationContext(), "升级成功");
            }

            @Override
            public void onAllHasUpdate() {

            }
        };
    }

    public void getUpdateInfo(Context context) {

        if (isRunning)
            return;
        isRunning = true;

        checkFileExist(context);
        String getUrl = StringManager.api_getConf;
        ReqInternet.in().doGet(getUrl, new InternetCallback(context) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {

                if (flag >= UtilInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> listReturn = UtilString.getListMapByJson(returnObj);
                    Map<String, String> map = null;
                    if (listReturn.size() > 0) {
                        map = listReturn.get(0);
                        if (map != null) {
                            Map<String, String> dataMap = new HashMap<String, String>();

                            String resources = map.get("dishVideoResources");
                            ArrayList<Map<String, String>> resList = UtilString.getListMapByJson(resources);
                            if (resList != null && resList.size() > 0) {
                                dataMap.putAll(resList.get(0));
                            }

                            String fonts = map.get("fonts");

                            if (!TextUtils.isEmpty(fonts)) {
                                dataMap.put("fonts", fonts);
                            }

                            if (dataMap.size() > 0) {
                                getReadyDownList(context, dataMap);
                            } else {
                                isRunning = false;
                            }
                        }
                    }
                } else {
                    isRunning = false;
                }
            }
        });

    }

    private void getReadyDownList(Context context, Map<String, String> map) {
        ArrayList<Map<String, String>> headList = new ArrayList<Map<String, String>>();
        ArrayList<Map<String, String>> tailList = new ArrayList<Map<String, String>>();
        ArrayList<Map<String, String>> musicList = new ArrayList<Map<String, String>>();
        ArrayList<Map<String, String>> fontsList = new ArrayList<Map<String, String>>();
        readyDownloadList.clear();

        if (map != null && map.size() > 0) {
            if (map.containsKey("head")) {
                String headStr = map.get("head");
                if (!TextUtils.isEmpty(headStr)) {
                    headList = StringManager.getListMapByJson(headStr);
                }
            }
            if (map.containsKey("tail")) {
                String tailStr = map.get("tail");
                if (!TextUtils.isEmpty(tailStr)) {
                    tailList = StringManager.getListMapByJson(tailStr);
                }
            }
            if (map.containsKey("bacMusic")) {
                String musicStr = map.get("bacMusic");
                if (!TextUtils.isEmpty(musicStr)) {
                    musicList = StringManager.getListMapByJson(musicStr);
                }
            }

            if (map.containsKey("fonts")) {
                String fontsStr = map.get("fonts");
                if (!TextUtils.isEmpty(fontsStr)) {
                    fontsList = StringManager.getListMapByJson(fontsStr);
                }
            }
        }

        String remoteHeadVideoMd5 = "";
        String remoteTailVideoMd5 = "";
        String remoteMusicMd5 = "";
        String remoteFontsMd5 = "";


        Map<String, String> headMap = null;
        if (headList != null && headList.size() > 0) {
            headMap = headList.get(0);
            if (headMap != null && headMap.size() > 0) {
                remoteHeadVideoMd5 = headMap.get("md5");
            }
        }
        Map<String, String> tailMap = null;
        if (tailList != null && tailList.size() > 0) {
            tailMap = tailList.get(0);
            if (tailMap != null && tailMap.size() > 0) {
                remoteTailVideoMd5 = tailMap.get("md5");
            }
        }
        Map<String, String> musicMap = null;
        if (musicList != null && musicList.size() > 0) {
            musicMap = musicList.get(0);
            if (musicMap != null && musicMap.size() > 0) {
                remoteMusicMd5 = musicMap.get("md5");
            }
        }

        Map<String, String> fontsMap = null;
        if (fontsList != null && fontsList.size() > 0) {
            fontsMap = fontsList.get(0);
            if (fontsMap != null && fontsMap.size() > 0) {
                remoteFontsMd5 = fontsMap.get("md5");
            }
        }

        totalLeng = 0;
        String localVideoHeadMd5 = (String) FileManager.loadShared(context,
                FileManager.DISH_VIDEO, FileManager.HEAD_VIDEO_MD5);
        if (!TextUtils.isEmpty(remoteHeadVideoMd5)
                && !remoteHeadVideoMd5.trim().equals(localVideoHeadMd5)) {
            readyDownloadList.add(headMap);
            headMap.put("md5Key", FileManager.HEAD_VIDEO_MD5);
            headMap.put("path", VIDEO_HEAN_PATH);
            String sizeStr = headMap.get("size");
            if (!TextUtils.isEmpty(sizeStr)) {
                totalLeng += Integer.valueOf(sizeStr);
            }
        }
        String localVideoTailMd5 = (String) FileManager.loadShared(context,
                FileManager.DISH_VIDEO, FileManager.TAIL_VIDEO_MD5);
        if (!TextUtils.isEmpty(remoteTailVideoMd5)
                && !remoteTailVideoMd5.trim().equals(localVideoTailMd5)) {
            readyDownloadList.add(tailMap);
            tailMap.put("md5Key", FileManager.TAIL_VIDEO_MD5);
            tailMap.put("path", VIDEO_TAIL_PATH);
            String sizeStr = tailMap.get("size");
            if (!TextUtils.isEmpty(sizeStr)) {
                totalLeng += Integer.valueOf(sizeStr);
            }
        }
        String localVideoMusicMd5 = (String) FileManager.loadShared(context,
                FileManager.DISH_VIDEO, FileManager.MUSIC_VIDEO_MD5);
        if (!TextUtils.isEmpty(remoteMusicMd5)
                && !remoteMusicMd5.trim().equals(localVideoMusicMd5)) {
            readyDownloadList.add(musicMap);
            musicMap.put("md5Key", FileManager.MUSIC_VIDEO_MD5);
            musicMap.put("path", VIDEO_MUSIC_PATH);
            String sizeStr = musicMap.get("size");
            if (!TextUtils.isEmpty(sizeStr)) {
                totalLeng += Integer.valueOf(sizeStr);
            }
        }

        String localVideoFontsMd5 = (String) FileManager.loadShared(context,
                FileManager.DISH_VIDEO, FileManager.FONT_VIDEO_MD5);
        if (!TextUtils.isEmpty(remoteFontsMd5)
                &&!remoteFontsMd5.trim().equals(localVideoFontsMd5)) {
            readyDownloadList.add(fontsMap);
            fontsMap.put("md5Key", FileManager.FONT_VIDEO_MD5);
            fontsMap.put("path", VIDEO_FONT_PATH);
            String sizeStr = fontsMap.get("size");
            if (!TextUtils.isEmpty(sizeStr)) {
                totalLeng += Integer.valueOf(sizeStr);
            }
        }

        isRunning = false;
        if (readyDownloadList.size() > 0) {
            hasDownCount = 0;
            callback.onGetTotalLength(totalLeng);
        } else {
            callback.onAllHasUpdate();
        }
    }


    public void downloadDishVideo(Context context) {

        if (isRunning)
            return;
        isRunning = true;
        if (readyDownloadList != null && readyDownloadList.size() > 0) {
            Map<String, String> firstMap = readyDownloadList.get(0);
            downItem(context, firstMap);
        }
    }

    private void downItem(Context context, final Map<String, String> map) {
        if (map != null) {
            String url = map.get("url");
            if (!TextUtils.isEmpty(url)) {
                UtilInternetFile.in().downloadFileProgress(url, map.get("path"),
                        new InternetCallback(context) {
                            @Override
                            public void loaded(int flag, String url, Object msg) {



                                if (flag >= ReqInternet.REQ_OK_FI) {

                                    FileManager.saveShared(context, FileManager.DISH_VIDEO,
                                            map.get("md5Key"), map.get("md5"));
                                    if(readyDownloadList.size() > 0){
                                        readyDownloadList.remove(0);
                                    }
                                    if (readyDownloadList.size() > 0) {
                                        hasDownCount += Integer.valueOf(map.get("size"));
                                        downloadDishVideo(context);
                                    } else {
                                        isRunning = false;
                                        callback.onSuccess();
                                    }


                                } else if (flag == ReqInternet.REQ_CANCEL_FI) {
                                    isRunning = false;
                                    callback.onCancel();
                                } else {
                                    isRunning = false;
                                    callback.onFail();
                                }
                            }
                        }, new FileDownloadCallback() {
                            @Override
                            public void onProgress(long current, long total, boolean isDone) {
                                callback.onProgress(hasDownCount + current, totalLeng);

                            }
                        });
                currentDownloadUrl = url;

            } else {
                isRunning = false;

            }
        }
        isRunning = false;
    }

    public void cancelDownload() {
        if (!TextUtils.isEmpty(currentDownloadUrl)) {
            UtilInternetFile.in().cancelDownload(currentDownloadUrl);
        }
        readyDownloadList.clear();
    }


    public boolean checkFileExist(Context context) {
        boolean flag = true;
        File headFile = new File(VIDEO_HEAN_PATH);
        if (!headFile.exists() || !headFile.isFile()) {
            flag = false;
            FileManager.saveShared(context,
                    FileManager.DISH_VIDEO, FileManager.HEAD_VIDEO_MD5, "");
        }

        File tailFile = new File(VIDEO_TAIL_PATH);
        if (!tailFile.exists() || !tailFile.isFile()) {
            flag = false;
            FileManager.saveShared(context,
                    FileManager.DISH_VIDEO, FileManager.TAIL_VIDEO_MD5, "");
        }
        File msicFile = new File(VIDEO_MUSIC_PATH);
        if (!msicFile.exists() || !msicFile.isFile()) {
            flag = false;
            FileManager.saveShared(context,
                    FileManager.DISH_VIDEO, FileManager.MUSIC_VIDEO_MD5, "");
        }


        File fontFile = new File(VIDEO_FONT_PATH);
        if (!fontFile.exists() || !fontFile.isFile()) {
            flag = false;
            FileManager.saveShared(context,
                    FileManager.DISH_VIDEO, FileManager.FONT_VIDEO_MD5, "");
        }
        return flag;
    }

    public interface DownloadCallback {

        void onGetTotalLength(long total);

        void onProgress(long current, long total);

        void onFail();

        void onCancel();

        void onSuccess();

        void onAllHasUpdate();
    }
}
