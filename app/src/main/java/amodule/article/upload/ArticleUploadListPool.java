package amodule.article.upload;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.UploadFailPopWindowDialog;
import amodule.article.activity.ArticleUploadListActivity;
import amodule.article.activity.edit.EditParentActivity;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadArticleSQLite;
import amodule.article.db.UploadParentSQLite;
import amodule.article.db.UploadVideoSQLite;
import amodule.dish.activity.upload.UploadDishListActivity;
import amodule.dish.db.UploadDishData;
import amodule.main.Main;
import amodule.upload.UploadListPool;
import amodule.upload.bean.UploadItemData;
import amodule.upload.bean.UploadPoolData;
import amodule.upload.callback.UploadListUICallBack;
import amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver;
import amodule.user.activity.FriendHome;

/**
 * Created by XiangHa on 2017/5/23.
 */

public class ArticleUploadListPool extends UploadListPool {

    private AtomicBoolean hasDoUploadLastInfo = new AtomicBoolean(false);
    private boolean flag;

    private UploadParentSQLite sqLite;

    private String tongjiId = "a_videodish_uploadlist";

    private int dataType;

    //是否是二次编辑上传的文章
    private String isSecondEdit;//1:否 2:是

    /**
     * 设置是否二次编辑上传的文章
     * @param isSecondEdit
     */
    public void setIsSecondEdit(boolean isSecondEdit) {
        this.isSecondEdit = isSecondEdit ? "2" : "1";
    }

    @Override
    protected void initData(int dataType,int draftId, String coverPath, String finalVideoPath, String timestamp, UploadListUICallBack callback) {
        super.initData(draftId, coverPath, finalVideoPath, timestamp, callback);
        this.dataType = dataType;
        if(dataType == EditParentActivity.DATA_TYPE_ARTICLE) {
            sqLite = new UploadArticleSQLite(XHApplication.in().getApplicationContext());
        }else if(dataType == EditParentActivity.DATA_TYPE_VIDEO) {
            sqLite = new UploadVideoSQLite(XHApplication.in().getApplicationContext());
        }
        Log.i("articleUpload","ArticleUploadListPool coverPath:" + coverPath);
        uploadPoolData.setDraftId(draftId);
        UploadArticleData uploadArticleData = modifyUploadListPoolData();
        uploadPoolData.setUploadArticleData(uploadArticleData);
        uploadPoolData.setTitle(uploadArticleData.getTitle());
        super.setUploadProcess();
    }

    @Override
    public void allStartOrStop(int flag) {
        super.allStartOrStop(flag);
        if (!isCancel) {
            UploadArticleData uploadArticleData = modifyUploadArticleData(false);
            if (uploadArticleData == null)
                return;
            if (flag == UploadListPool.TYPE_START) {
                if(isSecondEditor()){
                    uploadArticleData.setUploadType(UploadDishData.UPLOAD_ING_BACK);
                }else{
                    uploadArticleData.setUploadType(UploadDishData.UPLOAD_ING);
                }
                XHClick.mapStat(XHApplication.in(), tongjiId, "全部开始", "");
            } else if (flag == UploadListPool.TYPE_PAUSE) {
                uploadArticleData.setUploadType(UploadDishData.UPLOAD_PAUSE);
                XHClick.mapStat(XHApplication.in(), tongjiId, "全部暂停", "");
            }
            saveDataToSqlit(uploadArticleData);
        }
    }

    /**
     * 上传结束回调
     * @param flag true 成功  false 失败
     */
    @Override
    public void uploadOver(final boolean flag, final String response) {
        Log.i("articleUpload","uploadOver flag:" + flag + "   response:" + response);
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
                                uploadPoolData.setUploadArticleData(null);
                                XHClick.mapStat(XHApplication.in(), tongjiId, "上传状态", "成功");
                            } else {
                                hasDoUploadLastInfo.set(false);
                                itemData.setState(UploadItemData.STATE_FAILD);
                                UploadArticleData uploadArticleData = modifyUploadArticleData();
                                uploadArticleData.setUploadType(UploadDishData.UPLOAD_FAIL);
                                saveDataToSqlit(uploadArticleData);
                                XHClick.mapStat(XHApplication.in(), tongjiId, "上传状态", "上传失败");
                            }
                            return true;
                        }
                        return false;
                    }
                });

        super.uploadOver(flag, response);
        Activity act = XHActivityManager.getInstance().getCurrentActivity();
        if (FriendHome.isAlive) {
            Intent broadIntent = new Intent();
            broadIntent.setAction(UploadStateChangeBroadcasterReceiver.ACTION);
            String type = "";
            if (this.dataType == EditParentActivity.DATA_TYPE_ARTICLE)
                type = "2";
            else if (this.dataType == EditParentActivity.DATA_TYPE_VIDEO)
                type = "1";
            if (!TextUtils.isEmpty(type))
                broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.DATA_TYPE, type);
            broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.STATE_KEY,
                    flag ? UploadStateChangeBroadcasterReceiver.STATE_SUCCESS : UploadStateChangeBroadcasterReceiver.STATE_FAIL);
            if (flag && !TextUtils.isEmpty(isSecondEdit)) {
                broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.SECONDE_EDIT, isSecondEdit);
            }
            Main.allMain.sendBroadcast(broadIntent);
        } else if (Tools.isForward(XHApplication.in())) {
            Intent intent = new Intent();
            intent.putExtra("code", LoginManager.userInfo.get("code"));
            if(dataType == EditParentActivity.DATA_TYPE_ARTICLE)
                intent.putExtra("index", 3);
            else if(dataType == EditParentActivity.DATA_TYPE_VIDEO)
                intent.putExtra("index", 2);
            intent.setClass(act, FriendHome.class);
            act.startActivity(intent);

//            showUploadOverDialog(flag);
//            if(flag){
//                Activity act = XHActivityManager.getInstance().getCurrentActivity();
//                Intent it = new Intent(act, ArticleDetailActivity.class);
//                if(dataType == EditParentActivity.DATA_TYPE_ARTICLE) {
//                    it.setClass(act, ArticleDetailActivity.class);
//                }else if(dataType == EditParentActivity.DATA_TYPE_VIDEO) {
//                    it.setClass(act, VideoDetailActivity.class);
//                }
//                it.putExtra("code",response);
//                act.startActivity(it);
//            }else{
//                Activity act = XHActivityManager.getInstance().getCurrentActivity();
//                Intent it = new Intent(act, FriendHome.class);
//                it.putExtra("index",fridendHomeIndex);
//                act.startActivity(it);
//            }
        }
        if(act.getClass() == ArticleUploadListActivity.class){
            act.finish();
        }
    }

    /**
     * 上传文字信息
     * 判断物料是否上传完毕，如果是，组装文字信息，上传文字信息
     */
    @Override
    public void uploadLast() {
        Log.i("articleUpload","uploadLast()");

        //排除物料上传成功后，重复回调物料上传成功，触发上传最后一步
        if(hasDoUploadLastInfo.get())
            return;
        Log.i("articleUpload","uploadLast() checkStuffUploadOver:" + checkStuffUploadOver());
        if (checkStuffUploadOver()) {
            hasDoUploadLastInfo.set(true);
            final LinkedHashMap<String, String> params = combineParameter();
            List<UploadItemData> tailItemDatas = uploadPoolData.getTailDataList();
            uploadPoolData.loopPoolData(tailItemDatas, new UploadPoolData.LoopCallback() {
                @Override
                public boolean onLoop(UploadItemData itemData) {
                    if (itemData.getType() == UploadItemData.TYPE_LAST_TEXT) {
                        if(dataType == EditParentActivity.DATA_TYPE_ARTICLE)
                            itemData.setUploadUrl(StringManager.api_articleAdd);
                        else if(dataType == EditParentActivity.DATA_TYPE_VIDEO)
                            itemData.setUploadUrl(StringManager.api_videoAdd);
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
        Log.i("articleUpload","uploadThingOver() isPause:" + isPause);
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
                                UploadArticleData uploadArticleData = modifyUploadArticleData();
                                if(uploadArticleData != null) {
                                    uploadArticleData.setUploadType(UploadDishData.UPLOAD_ING);
                                    saveDataToSqlit(uploadArticleData);
                                }
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
     * 将上传池中的线上路径存入草稿数据库
     */
    private void saveDataToSqlit(UploadArticleData uploadDishData) {
        sqLite.update(uploadDishData.getId(),uploadDishData);
    }

    /**
     * 删除草稿箱数据库，上传成功后调用
     */
    private void deleteData() {
        sqLite.deleteById(uploadPoolData.getDraftId());
    }

    private UploadArticleData modifyUploadArticleData() {
        return modifyUploadArticleData(false);
    }

    private boolean isSecondEditor(){
        boolean flag = false;
        UploadArticleData articleData = uploadPoolData.getUploadArticleData();
        if(articleData!=null){
            String code = articleData.getCode();
            if(!TextUtils.isEmpty(code)&&Integer.valueOf(code)>0){
                flag = true;
            }
        }
        return  flag;
    }

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
                Log.i("articleUpload","checkStuffUploadOver() ---HeadData--- itemData.getRecMsg():" + itemData.getRecMsg());
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
                Log.i("articleUpload","checkStuffUploadOver()   ---BodyData--- itemData.getRecMsg():" + itemData.getRecMsg());
                if (TextUtils.isEmpty(itemData.getRecMsg())) {
                    flag = false;
                    return true;
                }
                return false;
            }
        });
        return flag;
    }

    private UploadArticleData modifyUploadArticleData(final boolean isReset) {
        final UploadArticleData uploadDishData = uploadPoolData.getUploadArticleData();
        if (uploadDishData == null){
            Log.e("articleUpload","UploadDishData 空");
            return null;
        }

        //视频首图
        List<UploadItemData> headDataList = uploadPoolData.getHeadDataList();
        if(headDataList != null && headDataList.size() > 0){
            final ArrayList<Map<String, String>> makesList = uploadDishData.getVideoArray();
            uploadPoolData.loopPoolData(headDataList, new UploadPoolData.LoopCallback() {
                @Override
                public boolean onLoop(UploadItemData itemData) {
                    if (itemData.getType() == UploadItemData.TYPE_BREAKPOINT_IMG) {
                        for (int i = 0; i < makesList.size(); i++) {
                            Map<String, String> map = makesList.get(i);
                            Log.i("articleUpload","视频图片信息 map.path:" + map.get("image") + "  url:" + itemData.getRecMsg());
                            if (map.get("image").equals(itemData.getPath())) {
                                map.put("imageUrl",isReset ? "" : itemData.getRecMsg());
                                break;
                            }
                        }
                    }
                    return false;
                }
            });
            uploadDishData.setVideoArray(makesList);
        }
        final List<UploadItemData> bodyItemDatas = uploadPoolData.getBodyDataList();
        //视频信息
        if(bodyItemDatas != null && bodyItemDatas.size() > 0) {
            final ArrayList<Map<String, String>> makesList = uploadDishData.getVideoArray();
            uploadPoolData.loopPoolData(bodyItemDatas, new UploadPoolData.LoopCallback() {
                @Override
                public boolean onLoop(UploadItemData itemData) {
                    if (itemData.getType() == UploadItemData.TYPE_VIDEO) {
                        for (int i = 0; i < makesList.size(); i++) {
                            Map<String, String> map = makesList.get(i);
                            Log.i("articleUpload","视频信息 map.path:" + map.get("video") + "  url:" + itemData.getRecMsg());
                            if (map.get("video").equals(itemData.getPath())) {
                                map.put("videoUrl",isReset ? "" : itemData.getRecMsg());
                                break;
                            }
                        }
                    }
                    return false;
                }
            });
            uploadDishData.setVideoArray(makesList);
        }
        //图片信息
        if(bodyItemDatas != null && bodyItemDatas.size() > 0) {
            final ArrayList<Map<String, String>> makesList = uploadDishData.getImgArray();
            uploadPoolData.loopPoolData(bodyItemDatas, new UploadPoolData.LoopCallback() {
                @Override
                public boolean onLoop(UploadItemData itemData) {
                    if (itemData.getType() == UploadItemData.TYPE_BREAKPOINT_IMG) {
                        Log.i("articleUpload","图片信息 path:" + itemData.getPath() + "  type:" + itemData.getType() + "  size:" + makesList.size());
                        for (int i = 0; i < makesList.size(); i++) {
                            Map<String, String> map = makesList.get(i);
                            Log.i("articleUpload","图片信息 map.path:" + map.get("path") + "  url:" + itemData.getRecMsg());
                            if (map.get("path").equals(itemData.getPath())) {
                                map.put("url",isReset ? "" : itemData.getRecMsg());
                                break;
                            }
                        }
                    }
                    return false;
                }
            });
            uploadDishData.setImgArray(makesList);
        }
        return uploadDishData;
    }

    /**
     * 修改上传池数据 从草稿箱数据库读取数据，修改后，添加到上传池
     */
    public UploadArticleData modifyUploadListPoolData() {
        UploadArticleData uploadArticleData = sqLite.selectById(uploadPoolData.getDraftId());
        Log.i("articleUpload","修改上传池数据 draftId:" + uploadPoolData.getDraftId());
        if (uploadArticleData != null) {

            ArrayList<UploadItemData> bodyItemDatas = new ArrayList<>();
            int bodyIndex = 0;
            String imgsDataStr = uploadArticleData.getImgs();
            Log.i("articleUpload","修改上传池数据 imgsDataStr:" + imgsDataStr);
            if (!TextUtils.isEmpty(imgsDataStr)) {
                UploadItemData bodyItemData;
                ArrayList<Map<String, String>> makesList = StringManager.getListMapByJson(imgsDataStr);
                if (makesList != null && makesList.size() > 0) {
                    for (int i = 0; i < makesList.size(); i++) {
                        Map<String, String> map = makesList.get(i);
                        String imgPath = map.get("path");
                        String imgUrl = map.get("url");
                        if (imgPath.indexOf("http") != 0 && !Tools.isFileExists(imgPath)) {
                            Toast.makeText(Main.allMain, "获取不到文章图片路径 " + i, Toast.LENGTH_SHORT).show();
                            return null;
                        }

                        bodyItemData = new UploadItemData();
                        bodyItemData.setPath(imgPath);
                        bodyItemData.setRecMsg(imgUrl);
                        bodyItemData.setIndex(bodyIndex++);
                        bodyItemData.setMakeStep("图片" + (i+1));
                        bodyItemData.setType(UploadItemData.TYPE_BREAKPOINT_IMG);
                        bodyItemData.setPos(UploadItemData.POS_BODY);
                        bodyItemDatas.add(bodyItemData);
                    }
                }
            }

            ArrayList<UploadItemData> headItemDatas = new ArrayList<>();
            int headIndex = 0;
            String videosDataStr = uploadArticleData.getVideos();
            if (!TextUtils.isEmpty(videosDataStr)) {
                UploadItemData captureVideoData;
                ArrayList<Map<String, String>> videosList = StringManager.getListMapByJson(videosDataStr);
                if (videosList != null && videosList.size() > 0) {
                    for (int i = 0; i < videosList.size(); i++) {
                        Map<String, String> map = videosList.get(i);
                        String videoPath = map.get("video");
                        String videoUrl = map.get("videoUrl");
                        String videoImage = map.get("image");
                        String imageUrl = map.get("imageUrl");

                        Log.e("articleUpload", "文章上传 videoPath: " + videoPath + "  videoUrl:" + videoUrl);
                        Log.e("articleUpload", "文章上传 videoImage: " + videoImage + "  imageUrl:" + imageUrl);
                        if (videoPath.indexOf("http") != 0 && !Tools.isFileExists(videoPath)) {
                            Toast.makeText(Main.allMain, "获取不到文章视频路径 " + i, Toast.LENGTH_SHORT).show();
                            return null;
                        }
                        if (videoImage.indexOf("http") != 0 && !Tools.isFileExists(videoImage)) {
                            Toast.makeText(Main.allMain, "获取不到文章视频图片路径", Toast.LENGTH_SHORT).show();
                            return null;
                        }

                        UploadItemData videoImgItemData = new UploadItemData();
                        videoImgItemData.setPath(videoImage);
                        videoImgItemData.setRecMsg(imageUrl);
                        videoImgItemData.setIndex(headIndex++);
                        videoImgItemData.setMakeStep("视频封面" + (i+1));
                        videoImgItemData.setType(UploadItemData.TYPE_BREAKPOINT_IMG);
                        videoImgItemData.setPos(UploadItemData.POS_HEAD);
                        headItemDatas.add(videoImgItemData);

                        captureVideoData = new UploadItemData();
                        captureVideoData.setPath(videoPath);
                        captureVideoData.setVideoImage(videoImage);
                        captureVideoData.setRecMsg(videoUrl);
                        captureVideoData.setIndex(bodyIndex++);
                        captureVideoData.setMakeStep("视频" + (i+1));
                        captureVideoData.setPos(UploadItemData.POS_BODY);
                        captureVideoData.setType(UploadItemData.TYPE_VIDEO);
                        bodyItemDatas.add(captureVideoData);
                    }
                }
            }
            uploadPoolData.setHeadDataList(headItemDatas);
            uploadPoolData.setBodyDataList(bodyItemDatas);


            ArrayList<UploadItemData> tailItemDatas = new ArrayList<>();
            UploadItemData tailItemData = new UploadItemData();
            tailItemData.setType(UploadItemData.TYPE_LAST_TEXT);
            tailItemData.setMakeStep("文字等信息");
            tailItemData.setPos(UploadItemData.POS_TAIL);
            tailItemData.setIndex(0);
            tailItemDatas.add(tailItemData);
            uploadPoolData.setTailDataList(tailItemDatas);
        } else {
            Toast.makeText(XHApplication.in(), "uploadArticleData 从数据库中没有获取到数据", Toast.LENGTH_SHORT).show();
        }

        return uploadArticleData;
    }

    public LinkedHashMap<String, String> combineParameter() {
        UploadArticleData uploadArticleData = uploadPoolData.getUploadArticleData();
        LinkedHashMap<String, String> uploadTextData = new LinkedHashMap<>();

        if (uploadArticleData == null) {
            return uploadTextData;
        }
        String content = uploadArticleData.getContent();
        Log.i("articleUpload","combineParameter() content:" + content);
        ArrayList<Map<String,String>> arrayList = uploadArticleData.getImgArray();
        content = replaceUrl(content,arrayList,"path","url");
        Log.i("articleUpload","combineParameter() 替换图片 content:" + content);
        ArrayList<Map<String,String>> videoArrayList = uploadArticleData.getVideoArray();
        content = replaceUrl(content,videoArrayList,"video","videoUrl");
        Log.i("articleUpload","combineParameter() 替换视频 content:" + content);
        content = replaceUrl(content,videoArrayList,"image","imageUrl");
        Log.i("articleUpload","combineParameter() 2222content:" + content);
        try {
            ArrayList<Map<String, String>> imgArray = uploadArticleData.getImgArray();
            Log.i("articleUpload", "combineParameter() uploadArticleData.getImgUrl():" + uploadArticleData.getImgUrl());
            Log.i("articleUpload", "combineParameter() uploadArticleData.getVideos():" + uploadArticleData.getVideos());
            uploadTextData.put("title", Uri.encode(uploadArticleData.getTitle(), "utf-8"));
            uploadTextData.put("classCode", uploadArticleData.getClassCode());
            uploadTextData.put("content", new String(Uri.encode(content, "utf-8")));
            uploadTextData.put("isOriginal", String.valueOf(uploadArticleData.getIsOriginal()));
            uploadTextData.put("repAddress", uploadArticleData.getRepAddress());
            uploadTextData.put("img", imgArray.size() > 0 ? imgArray.get(0).get("url") : "");
            if(videoArrayList.size() > 0) {
                Map<String,String> map = videoArrayList.get(0);
                uploadTextData.put("video", map.get("videoUrl"));
                uploadTextData.put("videoImg", map.get("imageUrl"));
            }
            uploadTextData.put("code", uploadArticleData.getCode());
        }catch (Exception e){
            e.printStackTrace();
        }
        return uploadTextData;
    }

    private String replaceUrl(String content,ArrayList<Map<String,String>> arrayList,String pathKey,String urlKey){
        for(int i = 0; i < arrayList.size(); i++){
            Map<String,String> map = arrayList.get(i);
            String path = map.get(pathKey);
            String url = map.get(urlKey);
            String newPath = path.replace("/","\\/");
            String newUrl = url.replace("/","\\/");
            Log.i("articleUpload","combineParameter() path:" + path + "   url:" + url);
            content = content.replace(newPath,newUrl);
            Log.i("articleUpload","combineParameter() newPath:" + newPath + "   newUrl:" + newUrl);
        }
        return content;
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

        if (flag) {
//            broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.STATE_KEY,
//                    UploadStateChangeBroadcasterReceiver.STATE_SUCCESS);
//            String recMsg = uploadPoolData.getTailDataList().get(0).getRecMsg();
//            String clickUrl = StringManager.wwwUrl + "caipu/" + recMsg + ".html";
//
//            Log.e("VideoDishUploadListPool", "imgUrl  "+imgUrl);
//            UploadSuccessPopWindowDialog dialog = new UploadSuccessPopWindowDialog(Main.allMain,
//                    uploadPoolData.getTitle(), imgUrl, new UploadSuccessPopWindowDialog.UploadSuccessDialogCallback() {
//                @Override
//                public void onClick() {
//                }
//            });
//            dialog.show(BarShare.IMG_TYPE_LOC, "我做了[" + uploadPoolData.getTitle() + "]，超好吃哦~",
//                    clickUrl, "独门秘籍都在这里，你也试试吧！",
//                    imgUrl, "视频菜谱发布成功后", "强化分享");
//            BaseActivity.mUploadDishVideoSuccess = dialog;
        } else {
            broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.STATE_KEY,
                    UploadStateChangeBroadcasterReceiver.STATE_FAIL);
            final UploadFailPopWindowDialog failPopWindowDialog =
                    new UploadFailPopWindowDialog(Main.allMain, uploadPoolData.getTitle(),
                            imgUrl, uploadPoolData.getDraftId(),
                            new UploadFailPopWindowDialog.UploadFailDialogCallback() {
                                @Override
                                public void callback(int draftId) {
                                    Intent intent = new Intent(XHApplication.in().getApplicationContext(), UploadDishListActivity.class);
                                    intent.putExtra("draftId", draftId);
                                    XHApplication.in().getApplicationContext().startActivity(intent);
                                }
                            });
            failPopWindowDialog.show();
            BaseActivity.mUploadDishVideoFail = failPopWindowDialog;
        }
        Main.allMain.sendBroadcast(broadIntent);
    }
}
