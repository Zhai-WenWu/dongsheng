package amodule.dish.business;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.db.UploadDishData;
import amodule.dish.db.UploadDishSqlite;
import amodule.dish.video.control.MediaControl;
import amodule.main.Main;

/**
 * Created by ：fei_teng on 2016/12/30 19:00.
 */

public class StepVideoPosCompute {

    private ArrayList<Float> list = new ArrayList<>();
    private ArrayList<List<Float>> stepPosList = new ArrayList<List<Float>>();
    private ArrayList<Map<String,Float>> otherInfoPosList = new ArrayList<Map<String,Float>>();
    private Map<String, ArrayList> posMap = new HashMap<String, ArrayList>();
    private float startVideoTime = 0f;
    private int draftId;

    public  Map<String, ArrayList> computeStepPos(int id) {
        draftId = id;
        startVideoTime = getStartVideoTime();
        if (startVideoTime == 0)
            return null;
        if (!getStepTime(id))
            return null;
        compute();
        return posMap;
    }

    public  Map<String, ArrayList>  computeStepPos(UploadDishData uploadDishData) {
        draftId = uploadDishData.getId();
        startVideoTime = getStartVideoTime();
        if (startVideoTime == 0)
            return null;
        if (!getStepTime(uploadDishData))
            return null;
        compute();
        return posMap;
    }


    private void compute() {
        if (list == null || list.size() < 1)
            return;

        Log.i("StepVideoPosCompute","startVideoTime:"+startVideoTime);
        float titleDur = getTitleDur();
        float dishDur = getDishDur();
        Float stepStarTime = startVideoTime + titleDur + dishDur;

        HashMap<String, Float> otherInfoPosMap = new HashMap<>();
        otherInfoPosMap.put("title",startVideoTime);
        otherInfoPosMap.put("ingredients",startVideoTime+titleDur);
        otherInfoPosMap.put("practice",stepStarTime);
        otherInfoPosList.add(otherInfoPosMap);
        posMap.put("otherInfoPos",otherInfoPosList);

        for (int i = 0; i < list.size(); i++) {
            float stepDur = list.get(i);
            ArrayList<Float> pointList = new ArrayList<>();
            pointList.add(stepStarTime);
            Log.i("StepVideoPosCompute","stepPosList:"+stepStarTime);
            pointList.add(stepStarTime + stepDur);
            stepStarTime = stepStarTime + stepDur;
            stepPosList.add(pointList);
        }
        posMap.put("stepPos",stepPosList);
        Log.i("StepVideoPosCompute","lastPos:"+stepStarTime);
    }

    private float getStartVideoTime() {
       return getVideoDuraton(MediaControl.path_voide+"/xiangha_start.mp4");
    }

    private float getTitleDur() {
        float titleDur;
        String titlePath = MediaControl.viewPath_cancel + draftId + "/" + MediaControl.path_paper + "/one.mp4";
        File file = new File(titlePath);
        if(file.exists()&&file.isFile()){
            titleDur =getVideoDuraton(titlePath);
        }else{
            titleDur = 3f;
        }
        Log.i("StepVideoPosCompute","titleDur:"+titleDur);
        return titleDur;
    }

    private float getDishDur() {
        float stuffDur;
        String disPath = MediaControl.viewPath_cancel + draftId + "/" + MediaControl.path_paper + "/dish.mp4";
        File file = new File(disPath);
        if (file.exists() && file.isFile()) {
            stuffDur = getVideoDuraton(disPath);
        } else {
            stuffDur = 5f;
        }
        Log.i("StepVideoPosCompute","stuffDur:"+stuffDur);
        return stuffDur;
    }



    public boolean getStepTime(int id) {

        UploadDishSqlite dishSqlite = new UploadDishSqlite(XHApplication.in().getApplicationContext());
        UploadDishData uploadDishData = dishSqlite.selectById(id);
        return getStepTime(uploadDishData);
    }

    public boolean getStepTime(UploadDishData uploadDishData) {

        boolean flag = false;
        if (uploadDishData != null) {
            String videoDataStr = uploadDishData.getMakes();
            if (!TextUtils.isEmpty(videoDataStr)) {
                ArrayList<Map<String, String>> makesList = StringManager.getListMapByJson(videoDataStr);
                if (makesList != null && makesList.size() > 0) {
                    for (int i = 0; i < makesList.size(); i++) {
                        Map<String, String> map = makesList.get(i);
                        String videoInfo = map.get("videoInfo");
                        String path = "";
                        if (!TextUtils.isEmpty(videoInfo)) {
                            ArrayList<Map<String, String>> videoInfoList = StringManager.getListMapByJson(videoInfo);
                            if (videoInfoList != null && videoInfoList.size() > 0) {
                                Map<String, String> videoInfoMap = videoInfoList.get(0);
                                if (videoInfoMap != null && videoInfoMap.size() > 0) {
                                    path = videoInfoMap.get("cutPath");
                                }
                            }
                        }
                        if (!Tools.isFileExists(path)) {
                            Toast.makeText(Main.allMain, "获取不到步骤视频大小", Toast.LENGTH_SHORT).show();
                            Log.i("菜谱视频预览", "cutPath: " + path + ", " + false);
                        } else {
                            Log.i("菜谱视频预览", "cutPath: " + path + ", " + true);
                            ArrayList<Map<String, String>> videoList = StringManager.getListMapByJson(videoInfo);
                            if (videoList != null && videoList.size() > 0) {
                                Map<String, String> videoMap = videoList.get(0);
                                Float cutTime = Float.valueOf(videoMap.get("cutTime"));
                                list.add(cutTime);
                            }
                        }
                    }
                }
            }
            flag = true;
        } else {
            Toast.makeText(XHApplication.in(), "菜谱视频预览 从数据库中没有获取到数据", Toast.LENGTH_SHORT).show();
            flag = false;
        }
        return flag;
    }


    private float getVideoDuraton(String path) {
        float time = 0f;
//        if(Tools.isFileExists(path)){
//            MediaInfo mediaInfo = new MediaInfo(path);
//            if(mediaInfo!=null){
//                mediaInfo.prepare();
//            }
//            if(mediaInfo!=null){
//                time = mediaInfo.vDuration;
//            }
//        }
        return time;
    }
}
