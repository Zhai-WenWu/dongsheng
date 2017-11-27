package amodule.dish.tools;

/**
 * Created by ：airfly on 2016/10/27 18:06.
 */

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.db.UploadDishData;
import amodule.dish.db.UploadDishSqlite;

/**
 * 合成视频前，判断内存大小，
 * 内存不足，对话框提示
 */

public class MediaStorageControl {


    private Context context;

    public MediaStorageControl(Context context) {
        this.context = context;

    }


    /**
     * 判断合成视频空间是否足够
     * @param id 数据库id
     * @return
     */
    public boolean checkStorage(int id) {
        boolean flag = false;
        float mediaStorage = getEvaluateStorage(id);
        long availableSize = Tools.getSDCardAvailableSize();
        if(availableSize<mediaStorage){
            flag = true;
            showDialog();
        }else{
            flag = false;
        }
        return flag;
    }

    private void showDialog() {
        /**
         * 展示dialog
         */
        String showInfo = "您的手机存储空间不足，无法合成视频！需要至少1GB的存储空间~";
        String btnMsg1 = "我知道了";

        final DialogManager dialogManager = new DialogManager(context);
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleMessageView(context).setText(showInfo))
                .setView(new HButtonView(context)
                        .setNegativeText(btnMsg1, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogManager.cancel();
                            }
                        }))).setCancelable(false).show();
    }


    public float getEvaluateStorage(int id) {

        float evaluateSize = 0;
        float size = 0;
        float totalDuration = 0;
        float cutTime = 0;
        UploadDishSqlite dishSqlite = new UploadDishSqlite(XHApplication.in().getApplicationContext());
        UploadDishData uploadDishData = dishSqlite.selectById(Integer.valueOf(id));

        if (uploadDishData != null) {
            String videoDataStr = uploadDishData.getMakes();
            if (!TextUtils.isEmpty(videoDataStr)) {
                ArrayList<Map<String, String>> makesList = StringManager.getListMapByJson(videoDataStr);
                if (makesList != null && makesList.size() > 0) {
                    for (int i = 0; i < makesList.size(); i++) {
                        Map<String, String> map = makesList.get(i);
                        String videoInfo = map.get("videoInfo");
                        if (!TextUtils.isEmpty(videoInfo)) {
                            ArrayList<Map<String, String>> videoInfoList = StringManager.getListMapByJson(videoInfo);
                            if (videoInfoList != null && videoInfoList.size() > 0) {
                                Map<String, String> videoInfoMap = videoInfoList.get(0);
                                if (videoInfoMap != null && videoInfoMap.size() > 0) {
                                    size += getFileSize(videoInfoMap.get("path"));
                                    totalDuration += getFileDurtion(videoInfoMap.get("allTime"));
                                    cutTime += getFileDurtion(videoInfoMap.get("cutTime"));
                                }
                            }
                        }

                    }
                }
            }
        }

        if (size != 0 && totalDuration != 0) {
            evaluateSize = size / totalDuration * (cutTime + 20);
        }

        evaluateSize = evaluateSize/1024f/1024f;
        return evaluateSize;
    }

    private float getFileDurtion(String durString) {

        float dur = 0;
        if (!TextUtils.isEmpty(durString)) {
            dur = Float.valueOf(durString);
        }
        return dur;
    }


    private float getFileSize(String path) {
        long size = 0;
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.isFile() && file.exists()) {
                size = file.length();
            }
        }
        return size;
    }


}


