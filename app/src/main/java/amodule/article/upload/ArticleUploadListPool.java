package amodule.article.upload;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.UploadFailPopWindowDialog;
import amodule.article.activity.ArticleDetailActivity;
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
 * Created by Fang Ruijiao on 2017/5/23.
 */

public class ArticleUploadListPool extends UploadListPool {

    private AtomicBoolean hasDoUploadLastInfo = new AtomicBoolean(false);
    private boolean flag;

    private UploadParentSQLite sqLite;

    private String tongjiId = "a_videodish_uploadlist";

    private int dataType;
    private int fridendHomeIndex; //失败后，跳转到个人主页的index页面

    @Override
    protected void initData(int dataType,int draftId, String coverPath, String finalVideoPath, String timestamp, UploadListUICallBack callback) {
        super.initData(draftId, coverPath, finalVideoPath, timestamp, callback);
        this.dataType = dataType;
        if(dataType == EditParentActivity.TYPE_ARTICLE) {
            fridendHomeIndex = 2;
            sqLite = new UploadArticleSQLite(XHApplication.in().getApplicationContext());
        }else if(dataType == EditParentActivity.TYPE_VIDEO) {
            fridendHomeIndex = 3;
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
        if (Tools.isForward(XHApplication.in())) {
//            showUploadOverDialog(flag);
            if(flag){
                Activity act = XHActivityManager.getInstance().getCurrentActivity();
                Intent it = new Intent(act, ArticleDetailActivity.class);
                it.putExtra("code",response);
                act.startActivity(it);
            }else{
                Activity act = XHActivityManager.getInstance().getCurrentActivity();
                Intent it = new Intent(act, FriendHome.class);
                it.putExtra("index",fridendHomeIndex);
                act.startActivity(it);
            }
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
                        if(dataType == EditParentActivity.TYPE_ARTICLE)
                            itemData.setUploadUrl(StringManager.api_articleAdd);
                        else if(dataType == EditParentActivity.TYPE_VIDEO)
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
                                uploadArticleData.setUploadType(UploadDishData.UPLOAD_ING);
                                saveDataToSqlit(uploadArticleData);
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
            Log.e("VideoDishUploadListPool","UploadDishData 空");
            return null;
        }

        //视频首图
        List<UploadItemData> headDataList = uploadPoolData.getHeadDataList();
        if(headDataList != null && headDataList.size() > 0){
            Log.i("articleUpload","视频首图 path:" + headDataList.get(0).getPath() + "   url:" + headDataList.get(0).getRecMsg());
            uploadDishData.setVideoImgUrl(isReset ? "" : headDataList.get(0).getRecMsg());
        }
        //视频信息
        final List<UploadItemData> bodyItemDatas = uploadPoolData.getBodyDataList();
        if(bodyItemDatas != null && bodyItemDatas.size() > 0) {
            UploadItemData captureItem = bodyItemDatas.get(bodyItemDatas.size() - 1);
            Log.i("articleUpload","视频信息 path:" + captureItem.getPath() + "  type:" + captureItem.getType() + "   url:" + captureItem.getRecMsg());
            if(UploadItemData.TYPE_VIDEO == captureItem.getType()){
                Log.i("articleUpload","视频信息 setVideoUrl:" + captureItem.getRecMsg());
                uploadDishData.setVideoUrl(isReset ? "" : captureItem.getRecMsg());
            }
        }
        //图片信息
        if(bodyItemDatas != null && bodyItemDatas.size() > 0) {
            final ArrayList<Map<String, String>> makesList = uploadDishData.getImgArray();
            uploadPoolData.loopPoolData(bodyItemDatas, new UploadPoolData.LoopCallback() {
                @Override
                public boolean onLoop(UploadItemData itemData) {
                    if (itemData.getType() == UploadItemData.TYPE_IMG) {
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

            ArrayList<UploadItemData> headItemDatas = new ArrayList<>();
            String videoImgPath = uploadArticleData.getVideoImg();
            String videoImgUrl = uploadArticleData.getVideoImgUrl();
            Log.i("articleUpload","从草稿箱添加到上传池  videoImgPath:" + videoImgPath + "   videoImgUrl:" + videoImgUrl);
            if (!TextUtils.isEmpty(videoImgPath)) {
                if (videoImgPath.startsWith("http")) {
                    videoImgUrl = videoImgPath;
                }
                UploadItemData headItemData = new UploadItemData();
                headItemData.setPath(videoImgPath);
                headItemData.setRecMsg(videoImgUrl);
                headItemData.setType(UploadItemData.TYPE_BREAKPOINT_IMG);
                headItemData.setPos(UploadItemData.POS_BODY);
                headItemData.setIndex(0);
                headItemData.setMakeStep("视频首图");
                headItemDatas.add(headItemData);
                uploadPoolData.setHeadDataList(headItemDatas);
            }

            int imgIndex = 0;
            ArrayList<UploadItemData> bodyItemDatas = new ArrayList<>();
            String imgsDataStr = uploadArticleData.getImgs();
            if (!TextUtils.isEmpty(imgsDataStr)) {
                UploadItemData headItemData;
                ArrayList<Map<String, String>> makesList = StringManager.getListMapByJson(imgsDataStr);
                if (makesList != null && makesList.size() > 0) {
                    for (int i = 0; i < makesList.size(); i++) {
                        Map<String, String> map = makesList.get(i);
                        String imgPath = map.get("path");
                        String imgUrl = map.get("url");

                        if (!TextUtils.isEmpty(imgUrl)) {
                            continue;
                        }
                        Log.e("articleUpload", "文章上传 imgPath: " + imgPath + ", " + true);
                        if (!Tools.isFileExists(imgPath)) {
                            Toast.makeText(Main.allMain, "获取不到文章图片路径 " + i, Toast.LENGTH_SHORT).show();
                            return null;
                        }

                        headItemData = new UploadItemData();
                        headItemData.setPath(imgPath);
                        headItemData.setRecMsg(imgUrl);
                        headItemData.setIndex(imgIndex++);
                        headItemData.setMakeStep(String.valueOf(i));
                        headItemData.setType(UploadItemData.TYPE_BREAKPOINT_IMG);
                        headItemData.setPos(UploadItemData.POS_BODY);
                        bodyItemDatas.add(headItemData);
                    }
                }
            }

            String videoPath = uploadArticleData.getVideo();
            String videoUrl = uploadArticleData.getVideoUrl();
            if (!TextUtils.isEmpty(videoPath)) {
                if (!Tools.isFileExists(videoPath)) {
                    Toast.makeText(Main.allMain, "获取不到文章视频路径", Toast.LENGTH_SHORT).show();
                    return null;
                }
                UploadItemData captureVideoData = new UploadItemData();
                captureVideoData.setPath(videoPath);
                captureVideoData.setRecMsg(videoUrl);
                captureVideoData.setIndex(imgIndex++);
                captureVideoData.setMakeStep("文章视频");
                captureVideoData.setPos(UploadItemData.POS_BODY);
                captureVideoData.setType(UploadItemData.TYPE_VIDEO);
                bodyItemDatas.add(captureVideoData);
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
        for(int i = 0; i < arrayList.size(); i++){
            Map<String,String> map = arrayList.get(i);
            String path = map.get("path");
            String url = map.get("url");
            String newPath = path.replace("/","\\/");
            String newUrl = url.replace("/","\\/");
            Log.i("articleUpload","combineParameter() path:" + path + "   url:" + url);
            content = content.replace(newPath,newUrl);
            Log.i("articleUpload","combineParameter() newPath:" + newPath + "   newUrl:" + newUrl);
        }
        String videoImgUrl = uploadArticleData.getVideoImg();
        if(!TextUtils.isEmpty(videoImgUrl)) {
            videoImgUrl = videoImgUrl.replace("/", "\\/");
            content = content.replace(videoImgUrl, uploadArticleData.getVideoImgUrl());
            String videoUrl = uploadArticleData.getVideo();
            videoUrl = videoUrl.replace("/", "\\/");
            content = content.replace(videoUrl, uploadArticleData.getVideoUrl());
        }

        Log.i("articleUpload","combineParameter() 2222content:" + content);
        try {
            ArrayList<Map<String, String>> imgArray = uploadArticleData.getImgArray();
            Log.i("articleUpload", "combineParameter() uploadArticleData.getImgUrl():" + uploadArticleData.getImgUrl());
            Log.i("articleUpload", "combineParameter() uploadArticleData.getVideoUrl():" + uploadArticleData.getVideoUrl());
            uploadTextData.put("title", uploadArticleData.getTitle());
            uploadTextData.put("classCode", uploadArticleData.getClassCode());
            uploadTextData.put("content", URLEncoder.encode(content, HTTP.UTF_8));
            uploadTextData.put("isOriginal", String.valueOf(uploadArticleData.getIsOriginal()));
            uploadTextData.put("repAddress", uploadArticleData.getRepAddress());
            uploadTextData.put("img", imgArray.size() > 0 ? imgArray.get(0).get("url") : "");
            uploadTextData.put("video", uploadArticleData.getVideoUrl());
            uploadTextData.put("videoImg", uploadArticleData.getVideoImgUrl());
            uploadTextData.put("code", uploadArticleData.getCode());
        }catch (Exception e){
            e.printStackTrace();
        }
        return uploadTextData;
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
