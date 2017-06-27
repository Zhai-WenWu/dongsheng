package amodule.dish.upload;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.UploadFailPopWindowDialog;
import acore.widget.UploadSuccessPopWindowDialog;
import amodule.dish.activity.upload.UploadDishListActivity;
import amodule.dish.business.StepVideoPosCompute;
import amodule.dish.db.UploadDishData;
import amodule.dish.db.UploadDishSqlite;
import amodule.dish.video.control.MediaControl;
import amodule.dish.video.control.MediaHandleControl;
import amodule.main.Main;
import amodule.upload.UploadListPool;
import amodule.upload.bean.UploadItemData;
import amodule.upload.bean.UploadPoolData;
import amodule.upload.callback.UploadListUICallBack;
import amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver;
import third.share.BarShare;
import xh.basic.tool.UtilString;

/**
 * Created by ：fei_teng on 2016/10/27 21:05.
 */
public class VideoDishUploadListPool extends UploadListPool {

    private String coverPath;
    private String timesStamp;
    private String finalVideoPath;

    public VideoDishUploadListPool() {
        super();
    }

//    /**
//     * 初始化数据
//     * @param draftId  草稿箱id
//     * @param callback ui回调
//     */
//    public void initData(int draftId, final UploadListUICallBack callback) {
//        super.initData(draftId, callback);
//        uploadPoolData.setDraftId(draftId);
//        UploadDishData uploadDishData = modifyUploadListPoolData();
//        uploadPoolData.setUploadDishData(uploadDishData);
//        uploadPoolData.setTitle(uploadDishData.getName());
//        super.setUploadProcess();
//    }

    /**
     * 初始化数据
     * @param draftId  草稿箱id
     * @param callback ui回调
     */
    public void initData(int draftId,String coverPath,String finalVideoPath,String timestamp,
                         final UploadListUICallBack callback) {
        super.initData(draftId, coverPath,finalVideoPath,timestamp,callback);
        this.coverPath = coverPath;
        this.timesStamp = timestamp;
        this.finalVideoPath = finalVideoPath;
        uploadPoolData.setDraftId(draftId);
        UploadDishData uploadDishData = modifyUploadListPoolData();
        uploadPoolData.setUploadDishData(uploadDishData);
        uploadPoolData.setTitle(uploadDishData.getName());
        super.setUploadProcess();
    }



    @Override
    public void allStartOrStop(int flag) {
        super.allStartOrStop(flag);
        if (!isCancel) {
            UploadDishData uploadDishData = modifyUploadDishData(false);
            if (flag == UploadListPool.TYPE_START) {
                if(isSecondEditor()){
                    uploadDishData.setDishType(UploadDishData.UPLOAD_ING_BACK);
                }else{
                    uploadDishData.setDishType(UploadDishData.UPLOAD_ING);
                }
                XHClick.mapStat(XHApplication.in(), "a_videodish_uploadlist", "全部开始", "");
            } else if (flag == UploadListPool.TYPE_PAUSE) {
                uploadDishData.setDishType(UploadDishData.UPLOAD_PAUSE);
                XHClick.mapStat(XHApplication.in(), "a_videodish_uploadlist", "全部暂停", "");
            }
            saveDataToSqlit(uploadDishData);
        }
    }


    /**
     * 上传文字信息
     * 判断物料是否上传完毕，如果是，组装文字信息，上传文字信息
     */
    public void uploadLast() {
        //排除物料上传成功后，重复回调物料上传成功，触发上传最后一步
        if(hasDoUploadLastInfo.get())
            return;
        if (checkStuffUploadOver()) {
            hasDoUploadLastInfo.set(true);
            final LinkedHashMap<String, String> params = combineParameter();
            List<UploadItemData> tailItemDatas =
                    uploadPoolData.getTailDataList();
            uploadPoolData.loopPoolData(tailItemDatas, new UploadPoolData.LoopCallback() {
                @Override
                public boolean onLoop(UploadItemData itemData) {
                    if (itemData.getType() == UploadItemData.TYPE_LAST_TEXT) {
                        itemData.setUploadUrl(StringManager.api_uploadDish);
                        itemData.setUploadMsg(params);
                        return true;
                    }
                    return false;
                }
            });
            super.uploadLast();

        }
    }

    /**
     * 上传物料结束
     *
     * @param flag        成功  false 失败
     * @param uniquId     唯一标示
     * @param responseStr
     * @param jsonObject
     */
    @Override
    protected void uploadThingOver(final boolean flag, final String uniquId, final String responseStr, final JSONObject jsonObject) {

        if (isPause)
            return;
        uploadPoolData.loopPoolData(uploadPoolData.getTotalDataList(),
                new UploadPoolData.LoopCallback() {
                    @Override
                    public boolean onLoop(UploadItemData itemData) {
                        if (itemData.getUniqueId().equals(uniquId)) {
                            if (flag) {
                                if (jsonObject != null)
                                    Log.e("itemData ", responseStr+","+itemData.getType() + "," + jsonObject.optString("hash"));

                                if (UploadItemData.TYPE_VIDEO == itemData.getType()){
                                    //处理url,has值丢失
                                    if(TextUtils.isEmpty(responseStr)||(TextUtils.isEmpty(jsonObject.optString("hash")))){
                                        Toast.makeText(XHApplication.in(),"hash为空",Toast.LENGTH_SHORT).show();
                                        itemData.setHashCode("");
                                        itemData.setRecMsg("");
                                        itemData.setState(UploadItemData.STATE_FAILD);
                                        return true;
                                    }else{
                                        itemData.setHashCode(jsonObject.optString("hash"));
                                        itemData.setRecMsg(responseStr);
                                        itemData.setState(UploadItemData.STATE_SUCCESS);
                                    }
                                }else{
                                    itemData.setState(UploadItemData.STATE_SUCCESS);
                                    itemData.setRecMsg(responseStr);
                                }
                                saveDataToSqlit(modifyUploadDishData());
                            } else {
                                itemData.setState(UploadItemData.STATE_FAILD);
                            }
                            return true;
                        }
                        return false;
                    }
                });

        uploadLast();
        super.uploadThingOver(flag, uniquId, responseStr, jsonObject);
    }


    /**
     * 取消上传
     */
    public void cancelUpload() {
        super.cancelUpload();
        MediaHandleControl.delAllMediaHandlerData(uploadPoolData.getDraftId());
        resetDbData();
        XHClick.mapStat(XHApplication.in(), "a_videodish_uploadlist", "取消上传", "");
    }

    /**
     * 取消上传需要，重置数据库，删除文件对应的url
     */
    private void resetDbData() {
        UploadDishData uploadDishData = modifyUploadDishData(true);
        uploadDishData.setDishType(UploadDishData.UPLOAD_DRAF);
        saveDataToSqlit(uploadDishData);
    }


    /**
     * 上传结束回调
     *
     * @param flag true 成功  false 失败
     */
    public void uploadOver(final boolean flag, final String response) {

        if (isPause)
            return;
        uploadPoolData.loopPoolData(uploadPoolData.getTailDataList(),
                new UploadPoolData.LoopCallback() {
                    @Override
                    public boolean onLoop(UploadItemData itemData) {
                        if (itemData.getType() == UploadItemData.TYPE_LAST_TEXT) {
                            itemData.setRecMsg(response);
                            if (flag) {
                                itemData.setState(UploadItemData.STATE_SUCCESS);
                                deleteData();
                                uploadPoolData.setUploadDishData(null);
                                XHClick.mapStat(XHApplication.in(), "a_videodish_uploadlist", "上传状态", "成功");
                            } else {
                                hasDoUploadLastInfo.set(false);
                                itemData.setState(UploadItemData.STATE_FAILD);
                                UploadDishData uploadDishData = modifyUploadDishData();
                                uploadDishData.setDishType(UploadDishData.UPLOAD_FAIL);
                                saveDataToSqlit(uploadDishData);
                                XHClick.mapStat(XHApplication.in(), "a_videodish_uploadlist", "上传状态", "上传失败");
                            }
                            return true;
                        }
                        return false;
                    }
                });

        super.uploadOver(flag, response);

        if (Tools.isForward(XHApplication.in())) {
            showUploadOverDialog(flag);
        }
    }


    /**
     * 解除ui绑定，如果正在上传，将草稿箱数据修改为后台上传
     */
    public void unBindUI() {
        super.unBindUI();
    }

    /**
     * @param flag true， false
     *             上传结束后，成功 弹出分享框，失败 提示框
     */
    private void showUploadOverDialog(final boolean flag) {
        String imgUrl = uploadPoolData.getHeadDataList().get(0).getPath();
        if(TextUtils.isEmpty(imgUrl)){
            imgUrl = uploadPoolData.getHeadDataList().get(0).getRecMsg();
        }
        Intent broadIntent = new Intent();
        broadIntent.setAction(UploadStateChangeBroadcasterReceiver.ACTION);
        broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.DATA_TYPE, "0");
        if (flag) {
            broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.STATE_KEY,
                    UploadStateChangeBroadcasterReceiver.STATE_SUCCESS);
            String recMsg = uploadPoolData.getTailDataList().get(0).getRecMsg();
            String clickUrl = StringManager.wwwUrl + "caipu/" + recMsg + ".html";

            Log.e("VideoDishUploadListPool", "imgUrl  "+imgUrl);
            UploadSuccessPopWindowDialog dialog = new UploadSuccessPopWindowDialog(Main.allMain,
                    uploadPoolData.getTitle(), imgUrl, new UploadSuccessPopWindowDialog.UploadSuccessDialogCallback() {
                @Override
                public void onClick() {
                }
            });
            dialog.show(BarShare.IMG_TYPE_LOC, "我做了[" + uploadPoolData.getTitle() + "]，超好吃哦~",
                    clickUrl, "独门秘籍都在这里，你也试试吧！",
                    imgUrl, "视频菜谱发布成功后", "强化分享");
            BaseActivity.mUploadDishVideoSuccess = dialog;
        } else {
            broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.STATE_KEY,
                    UploadStateChangeBroadcasterReceiver.STATE_FAIL);
            final UploadFailPopWindowDialog failPopWindowDialog =
                    new UploadFailPopWindowDialog(Main.allMain, uploadPoolData.getTitle(),
                            imgUrl, uploadPoolData.getDraftId(),
                            new UploadFailPopWindowDialog.UploadFailDialogCallback() {
                                @Override
                                public void callback(int draftId) {
                                    Intent intent = new Intent();
                                    intent.setClass(XHApplication.in().getApplicationContext(), UploadDishListActivity.class);
                                    intent.putExtra("draftId", draftId);
                                }
                            });
            failPopWindowDialog.show();
            BaseActivity.mUploadDishVideoFail = failPopWindowDialog;
        }
        Main.allMain.sendBroadcast(broadIntent);
    }


    /**
     * 将上传池中的线上路径存入草稿数据库
     */
    private void saveDataToSqlit(UploadDishData uploadDishData) {
        UploadDishSqlite dishSqlite = new UploadDishSqlite(XHApplication.in().getApplicationContext());
        dishSqlite.update(uploadPoolData.getDraftId(), uploadDishData);
    }

    /**
     * 删除草稿箱数据库，上传成功后调用
     */
    private void deleteData() {
        UploadDishSqlite dishSqlite = new UploadDishSqlite(XHApplication.in().getApplicationContext());
        dishSqlite.deleteById(uploadPoolData.getDraftId());
        MediaHandleControl.delAllMediaHandlerData(uploadPoolData.getDraftId());
    }


    private AtomicBoolean hasDoUploadLastInfo = new AtomicBoolean(false);
    private boolean flag;

    /**
     * 检查物料是否上传完毕
     *
     * @return allTingUploadSucess true 是，false 否
     */
    private boolean checkStuffUploadOver() {
        flag = true;
        uploadPoolData.loopPoolData(uploadPoolData.getHeadDataList(), new UploadPoolData.LoopCallback() {
            @Override
            public boolean onLoop(UploadItemData itemData) {
                if (TextUtils.isEmpty(itemData.getRecMsg())) {
                    flag = false;
                    return true;
                }
                return false;
            }
        });

        if (!flag) {
            return false;
        }
        uploadPoolData.loopPoolData(uploadPoolData.getBodyDataList(), new UploadPoolData.LoopCallback() {
            @Override
            public boolean onLoop(UploadItemData itemData) {
                if (TextUtils.isEmpty(itemData.getRecMsg())
                        || TextUtils.isEmpty(itemData.getHashCode())) {
                    flag = false;
                    return true;
                }
                return false;
            }
        });
        return flag;
    }


    private UploadDishData modifyUploadDishData() {
        return modifyUploadDishData(false);
    }

    private JSONArray jsonArray;

    private UploadDishData modifyUploadDishData(final boolean isReset) {

        final UploadDishData uploadDishData = uploadPoolData.getUploadDishData();
        if (uploadDishData == null){
            Log.e("VideoDishUploadListPool","UploadDishData 空");
            return null;
        }

        uploadDishData.setCoverUrl(isReset ? "" : uploadPoolData.getHeadDataList().get(0).getRecMsg());
        final List<UploadItemData> bodyItemDatas = uploadPoolData.getBodyDataList();
        UploadItemData captureItem = bodyItemDatas.get(bodyItemDatas.size() - 1);
        JSONObject object = new JSONObject();
        try {
            object.putOpt("videoPath", captureItem.getPath());
            object.putOpt("videoUrl", isReset ? "" : captureItem.getRecMsg());
            object.putOpt("videoMd5", captureItem.getHashCode());
            uploadDishData.setCaptureVideoInfo(object.toString());
        } catch (Exception e) {
            Log.e("VideoDishUploadListPool","UploadDishData 异常");
            return null;
        }

        final String videoDataStr = uploadDishData.getMakes();
        final ArrayList<Map<String, String>> makesList = StringManager.getListMapByJson(videoDataStr);
        jsonArray = new JSONArray();

        uploadPoolData.loopPoolData(bodyItemDatas, new UploadPoolData.LoopCallback() {
            JSONObject jsonObj;

            @Override
            public boolean onLoop(UploadItemData itemData) {
                if (itemData.getType() == UploadItemData.TYPE_VIDEO) {
                    jsonObj = new JSONObject();
                    {
                        for (int i = 0; i < makesList.size(); i++) {
                            try {
                                Map<String, String> map = makesList.get(i);
                                if (map.get("makesStep").equals(itemData.getMakeStep())) {
                                    jsonObj.put("videoUrl", isReset ? "" : itemData.getRecMsg());
                                    jsonObj.put("md5", itemData.getHashCode());
                                    jsonObj.put("makesStep", itemData.getMakeStep());
                                    jsonObj.put("makesInfo", itemData.getMakesInfo());
                                    jsonObj.put("videoInfo", itemData.getVideoInfo());
                                    jsonArray.put(jsonObj);
                                    break;
                                }
                            } catch (Exception e) {
                                break;
                            }
                        }
                    }
                }
                return false;
            }
        });

        uploadDishData.setMakes(jsonArray.toString());
        return uploadDishData;
    }


    /**
     * 修改上传池数据 从草稿箱数据库读取数据，修改后，添加到上传池
     */
    public UploadDishData modifyUploadListPoolData() {
        UploadDishSqlite dishSqlite = new UploadDishSqlite(XHApplication.in().getApplicationContext());
        UploadDishData uploadDishData = dishSqlite.selectById(uploadPoolData.getDraftId());

        if (uploadDishData != null) {
            String cover = uploadDishData.getCover();
            String coverUrl = uploadDishData.getCoverUrl();
            if (TextUtils.isEmpty(cover)&& TextUtils.isEmpty(coverUrl)) {
                Log.e("cover   ",cover);
                Log.e("coverUrl   ",coverUrl);

                if(!Tools.isFileExists(coverPath)){
                    Toast.makeText(Main.allMain,"获取不到视频菜谱大图路径",Toast.LENGTH_SHORT).show();
                    return null;
                }else{
                    cover = coverPath;
                }
//                Toast.makeText(Main.allMain,"获取不到视频菜谱大图路径",Toast.LENGTH_SHORT).show();
//                return null;
            }
            if (!TextUtils.isEmpty(cover)) {
                if (cover.startsWith("http")) {
                    coverUrl = cover;
                }
                ArrayList<UploadItemData> headItemDatas = new ArrayList<>();
                UploadItemData headItemData = new UploadItemData();
                headItemData.setPath(cover);
                headItemData.setRecMsg(coverUrl);
                headItemData.setType(UploadItemData.TYPE_IMG);
                headItemData.setPos(UploadItemData.POS_HEAD);
                headItemData.setIndex(0);
                headItemData.setMakeStep("菜谱封面图");
                headItemDatas.add(headItemData);
                uploadPoolData.setHeadDataList(headItemDatas);
            }
            ArrayList<UploadItemData> bodyItemDatas = new ArrayList<>();
            String videoDataStr = uploadDishData.getMakes();
            if (!TextUtils.isEmpty(videoDataStr)) {
                UploadItemData bodyItemData;
                ArrayList<Map<String, String>> makesList = StringManager.getListMapByJson(videoDataStr);
                if (makesList != null && makesList.size() > 0) {
                    for (int i = 0; i < makesList.size(); i++) {
                        Map<String, String> map = makesList.get(i);
                        String makesInfo = map.get("makesInfo");
                        String makesStep = map.get("makesStep");
                        String videoInfo = map.get("videoInfo");
                        String videoUrl = map.get("videoUrl");
                        String md5 = map.get("md5");
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

                        //处理数据库数据丢失

                        if(TextUtils.isEmpty(md5)||TextUtils.isEmpty(videoUrl)){
                            md5 = "";
                            videoUrl = "";
                        }

//                        path = "";
                        if(!Tools.isFileExists(path)){
                            String assemblePath = MediaControl.viewPath_cancel + uploadPoolData.getDraftId() + "/" +
                                    MediaControl.path_paper + "/" + timesStamp + "_" + i + ".mp4";

                            if(!Tools.isFileExists(assemblePath)){
                                Toast.makeText(Main.allMain,"获取不到步骤视频路径 "+i,Toast.LENGTH_SHORT).show();
                                Log.e("菜谱视频上传","cutPath: "+path+", " + false);
                                return null;
                            }

                            path = assemblePath;

                        }
                        Log.e("菜谱视频上传", "cutPath: " + path + ", " + true);

                        bodyItemData = new UploadItemData();
                        bodyItemData.setType(UploadItemData.TYPE_VIDEO);
                        bodyItemData.setPath(path);
                        bodyItemData.setPos(UploadItemData.POS_BODY);
                        bodyItemData.setIndex(i);
                        bodyItemData.setMakeStep(makesStep);
                        bodyItemData.setHashCode(md5);
                        bodyItemData.setRecMsg(videoUrl);
                        bodyItemData.setMakesInfo(makesInfo);
                        bodyItemData.setVideoInfo(videoInfo);
                        bodyItemDatas.add(bodyItemData);
                    }
                }
            }

            String captureVideoInfo = uploadDishData.getCaptureVideoInfo();
            if (!TextUtils.isEmpty(captureVideoInfo)) {
                ArrayList<Map<String, String>> captureInfoList = StringManager.getListMapByJson(captureVideoInfo);
                if (captureInfoList != null && captureInfoList.size() > 0) {
                    Map<String, String> captureMap = captureInfoList.get(0);
                    String videoPath = captureMap.get("videoPath");
                    String videoUrl = captureMap.get("videoUrl");
                    String md5 = captureMap.get("videoMd5");

                    if(!Tools.isFileExists(videoPath)){

                        if(!Tools.isFileExists(finalVideoPath)){
                            Toast.makeText(Main.allMain,"获取不到合成视频路径",Toast.LENGTH_SHORT).show();
                            return null;
                        }
                        videoPath = finalVideoPath;
                    }

                    //处理数据库数据丢失
                    if(TextUtils.isEmpty(md5) || TextUtils.isEmpty(videoUrl)){
                        md5 = "";
                        videoUrl = "";
                    }

                    UploadItemData captureVideoData = new UploadItemData();
                    captureVideoData.setRecMsg(videoUrl);
                    captureVideoData.setPath(videoPath);
                    captureVideoData.setPos(UploadItemData.POS_BODY);
                    captureVideoData.setIndex(bodyItemDatas.size());
                    captureVideoData.setMakeStep("高清菜谱视频");
                    captureVideoData.setType(UploadItemData.TYPE_VIDEO);
                    captureVideoData.setHashCode(md5);
                    bodyItemDatas.add(captureVideoData);
                }
            }
            uploadPoolData.setBodyDataList(bodyItemDatas);

            ArrayList<UploadItemData> tailItemDatas = new ArrayList<>();
            UploadItemData tailItemData = new UploadItemData();
            tailItemData.setType(UploadItemData.TYPE_LAST_TEXT);
            tailItemData.setMakeStep("其他信息");
            tailItemData.setPos(UploadItemData.POS_TAIL);
            tailItemData.setIndex(0);
            tailItemDatas.add(tailItemData);
            uploadPoolData.setTailDataList(tailItemDatas);
        }else{
            Toast.makeText(XHApplication.in(), "uploadDishData 从数据库中没有获取到数据", Toast.LENGTH_SHORT).show();
        }

        return uploadDishData;
    }


    private boolean isSecondEditor(){
        boolean flag = false;
        UploadDishData dishData = uploadPoolData.getUploadDishData();
        if(dishData!=null){
            String code = dishData.getCode();
            if(!TextUtils.isEmpty(code)&&Integer.valueOf(code)>0){
                flag = true;
            }
        }
        return  flag;
    }

    public LinkedHashMap<String, String> combineParameter() {
        UploadDishData dishData = uploadPoolData.getUploadDishData();
        LinkedHashMap<String, String> uploadTextData = new LinkedHashMap<>();

        if (dishData == null) {
            return uploadTextData;
        }
        uploadTextData.put("code", TextUtils.isEmpty(dishData.getCode()) ? "0" : dishData.getCode());
        uploadTextData.put("sign", String.valueOf(dishData.getUploadTimeCode()));
        uploadTextData.put("name", dishData.getName());
        uploadTextData.put("img[0]", dishData.getCoverUrl());
        uploadTextData.put("remark", dishData.getTips());
        uploadTextData.put("readyTime", dishData.getReadyTime());
        uploadTextData.put("cookTime", dishData.getCookTime());
        uploadTextData.put("taste", dishData.getTaste());
        uploadTextData.put("diff", dishData.getDiff());
        uploadTextData.put("exclusive", dishData.getExclusive());
        uploadTextData.put("info", dishData.getStory());

        String captureVideoInfo = dishData.getCaptureVideoInfo();
        if (!TextUtils.isEmpty(captureVideoInfo)) {
            ArrayList<Map<String, String>> captureList = StringManager.getListMapByJson(captureVideoInfo);
            if (captureList != null && captureList.size() > 0) {
                Map<String, String> captureMap = captureList.get(0);
                if (captureMap != null && captureMap.size() > 0) {
                    uploadTextData.put("video", captureMap.get("videoUrl"));
                    uploadTextData.put("videoMd5", captureMap.get("videoMd5"));
                }
            }
        }


        ArrayList<Map<String, String>> food = UtilString.getListMapByJson(dishData.getFood());
        for (int i = 0; i < food.size(); i++) {
            uploadTextData.put("ingredients[" + i + "]", food.get(i).get("name"));
            uploadTextData.put("content[" + i + "]", food.get(i).get("number"));
        }


        ArrayList<Map<String, String>> burden = UtilString.getListMapByJson(dishData.getBurden());
        for (int i = 0; i < burden.size(); i++) {
            uploadTextData.put("seasoning[" + i + "]", burden.get(i).get("name"));
            uploadTextData.put("content2[" + i + "]", burden.get(i).get("number"));
        }
        ArrayList<Map<String, String>> makes = UtilString.getListMapByJson(dishData.getMakes());
        for (int i = 0; i < makes.size(); i++) {
            Map<String, String> makeMap = makes.get(i);
            uploadTextData.put("makeId[" + i + "]", makeMap.get("makesStep"));
            uploadTextData.put("makeInfo[" + i + "]", makeMap.get("makesInfo"));
            uploadTextData.put("makeVideo[" + i + "]", makeMap.get("videoUrl"));
            uploadTextData.put("makeVideoMd5[" + i + "]", makeMap.get("md5"));
        }

        Map<String, ArrayList> posPointMap = new StepVideoPosCompute().computeStepPos(dishData);

        if (posPointMap != null && posPointMap.size() > 0) {
            ArrayList<List<Float>> stepPosList = posPointMap.get("stepPos");
            for (int j = 0; j < stepPosList.size(); j++) {
                List<Float> posList = stepPosList.get(j);
                uploadTextData.put("makeVideoSTime[" + j + "]", posList.get(0) + "");
            }

            ArrayList<Map<String, Float>> otherInfoPosList = posPointMap.get("otherInfoPos");
            Map<String, Float> otherInfoPosMap = otherInfoPosList.get(0);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt("title", otherInfoPosMap.get("title"));
                jsonObject.putOpt("ingredients", otherInfoPosMap.get("ingredients"));
                jsonObject.putOpt("practice", otherInfoPosMap.get("practice"));
                uploadTextData.put("otherVideoSTime", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        uploadTextData.put("addition", dishData.getMakes());
        return uploadTextData;
    }

}
