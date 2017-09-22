package com.popdialog.util;

import android.content.Context;

import com.popdialog.FullSrceenDialogControl;

import java.util.Map;

/**
 * PackageName : com.popdialog.util
 * Created by MrTrying on 2017/9/20 13:45.
 * E_mail : ztanzeyu@gmail.com
 */

public class FullScreenManager {

    // 保存welcome页数据
    public static void saveWelcomeInfo(Context context, String json, FullSrceenDialogControl.OnLoadImageCallback onLoadImageCallback) {
        Map<String, String> map = getWelcomeInfo(context);
        if (json == null || json.length() < 10) {
            delWelcomeInfo(context, map);
        } else {
            String file_content = FileManager.readFile(FileManager.getDataDir(context) + FileManager.file_welcome);
            file_content = file_content.trim();
            if (!file_content.equals(json)) {
                delWelcomeInfo(context, map);
                FileManager.saveShared(context, FileManager.xmlFile_popdialog, FileManager.xmlKey_fullSrceenShowNum, "0");
                FileManager.saveFileToCompletePath(FileManager.getDataDir(context) + FileManager.file_welcome, json, false);
                if (map != null && map.containsKey("img")) {
                    if (onLoadImageCallback != null) {
                        onLoadImageCallback.onLoadImage(map.get("img"), null);
                    }
                }
            }
        }
    }

    // 删除welcome页数据
    private static void delWelcomeInfo(Context context, Map<String, String> map) {
        if (map != null && map.get("img") != null)
            ImageManager.delImg(map.get("img"));
        FileManager.delDirectoryOrFile(FileManager.getDataDir(context) + FileManager.file_welcome);
    }

    // 从文件获取welcome页数据
    public static Map<String, String> getWelcomeInfo(Context context) {
        String file_content = FileManager.readFile(FileManager.getDataDir(context) + FileManager.file_welcome);
        if (file_content.length() > 10)
            return StringManager.getListMapByJson(file_content).get(0);
        return null;
    }
}
