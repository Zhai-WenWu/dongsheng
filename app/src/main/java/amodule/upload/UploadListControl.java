package amodule.upload;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import acore.override.XHApplication;
import acore.tools.UploadImg;
import amodule.upload.bean.UploadItemData;
import amodule.upload.bean.UploadPoolData;
import amodule.upload.callback.UploadListNetCallBack;
import amodule.upload.callback.UploadListUICallBack;
import aplug.basic.BreakPointControl;
import aplug.basic.BreakPointUploadManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;

/**
 * Created by ：fei_teng on 2016/10/27 20:43.
 */

public class UploadListControl {

    private volatile static UploadListControl control;
    private CopyOnWriteArrayList<Map<String, UploadListPool>> uploadPoolList;
    private ConcurrentHashMap<String, BreakPointControl> uploaderMap;
    private final int MAX_UPLOADING_NUM = 3;
    private final int MAX_RETRY_NUM = 3;//最后一步上传失败，可重试最大次数
    private int retryNum; // 最后一步上传失败，重试次数


    private UploadListControl() {
        uploadPoolList = new CopyOnWriteArrayList<Map<String, UploadListPool>>();
        uploaderMap = new ConcurrentHashMap<String, BreakPointControl>();

    }

    public static UploadListControl getUploadListControlInstance() {

        if (control == null) {
            synchronized (UploadListControl.class) {
                if (control == null) {
                    control = new UploadListControl();
                }
            }
        }
        return control;
    }

//    /**
//     * 上传池类型，草稿id，上传池UI回调,获取上传池，
//     * 上传池已存在，无需创建，否则创建
//     *
//     * @param poolType 上传池类型
//     * @param draftId
//     * @param callback
//     * @return UploadListPool 上传池
//     */
//    public UploadListPool add(Class<? extends UploadListPool> poolType, int draftId, UploadListUICallBack callback) {
//        UploadListPool pool;
//        String poolKey = poolType.getSimpleName() + draftId;
//        pool = getPool(poolKey);
//        if (pool == null) {
//            try {
//                pool = poolType.newInstance();
//                pool.initData(draftId, callback);
//                HashMap<String, UploadListPool> poolHashMap = new HashMap<>();
//                poolHashMap.put(poolKey, pool);
//                uploadPoolList.add(poolHashMap);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            pool.setUiCallback(callback);
//        }
//
//        return pool;
//    }


    /**
     * 临时方法，处理步骤视频，最终视频，大图路径丢失
     * 处理完数据库数据丢失问题后应该删除
     * <p>
     * <p>
     * 上传池类型，草稿id，上传池UI回调,获取上传池，
     * 上传池已存在，无需创建，否则创建
     *
     * @param poolType       上传池类型
     * @param draftId
     * @param coverPath      大图路径
     * @param finalVideoPath 最终视频路径
     * @param timestamp      时间戳
     * @param callback
     * @return UploadListPool 上传池
     */
    public UploadListPool add(Class<? extends UploadListPool> poolType, int draftId,
                              String coverPath, String finalVideoPath, String timestamp,
                              UploadListUICallBack callback) {
        UploadListPool pool;
        String poolKey = poolType.getSimpleName() + draftId;
        pool = getPool(poolKey);
        if (pool == null) {
            try {
                pool = poolType.newInstance();
                pool.initData(draftId, coverPath, finalVideoPath, timestamp, callback);
                HashMap<String, UploadListPool> poolHashMap = new HashMap<>();
                poolHashMap.put(poolKey, pool);
                uploadPoolList.add(poolHashMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            pool.setUiCallback(callback);
        }

        return pool;
    }

    /**
     * 初始化网络上传,获取上传进度，
     *
     * @param data 单项数据
     * @return progress   上传进度
     */
    public int initUpload(UploadItemData data) {

        if (!TextUtils.isEmpty(data.getRecMsg())) {
            return 100;
        }

        int progress;
        switch (data.getType()) {
            case UploadItemData.TYPE_VIDEO:
                progress = initUploadVideo(data);
                break;
            default:
                progress = 0;
                break;
        }
        return progress;
    }

    private int initUploadVideo(UploadItemData data) {
        BreakPointControl breakPointContorl
                = new BreakPointControl(XHApplication.in().getApplicationContext(),
                data.getUniqueId(),
                data.getPath(), BreakPointUploadManager.TYPE_VIDEO);
        uploaderMap.put(data.getUniqueId(), breakPointContorl);
        return (int) (breakPointContorl.getProgress() * 100);
    }


    //考虑由回调，返回状态

    /**
     * 开启网络上传,根据不同的数据类型，开启不同网络
     *
     * @param data     单项数据
     * @param callback 网络回调
     * @return state 上传状态
     */
    public int startUpload(UploadItemData data, UploadListNetCallBack callback) {
        int state = 0;
        if (!TextUtils.isEmpty(data.getRecMsg())) {
            state = UploadItemData.STATE_SUCCESS;
        } else {
            switch (data.getType()) {
                case UploadItemData.TYPE_IMG:
                    state = startUploadImg(data, callback);
                    break;
                case UploadItemData.TYPE_BREAKPOINT_IMG:
                    state = startUploadBreakPointImg(data, callback);
                    break;
                case UploadItemData.TYPE_VIDEO:
                    state = startUploadVideo(data, callback);
                    break;
                case UploadItemData.TYPE_TEXT:
                    state = startUploadText(data, callback);
                    break;
                default:
                    break;

            }
        }
        return state;
    }


    private int startUploadText(UploadItemData data, UploadListNetCallBack callback) {
        int state = UploadItemData.STATE_WAITING;
        return state;

    }

    /**
     * 上传视频，以断点续传方式上传
     *
     * @param data
     * @param callback
     * @return
     */
    private int startUploadVideo(UploadItemData data, UploadListNetCallBack callback) {

        int state = 0;
        if (isUploadingOutLimit()) {
            state = UploadItemData.STATE_WAITING;
        } else {
            BreakPointControl uploader = uploaderMap.get(data.getUniqueId());
            if (uploader != null) {
                //------断点开始上传
                uploader.start(callback);
                state = UploadItemData.STATE_RUNNING;
            }
        }
        return state;
    }

    /**
     * 上传图片，以断点续传方式上传
     *
     * @param data
     * @param callback
     * @return
     */
    private int startUploadBreakPointImg(UploadItemData data, UploadListNetCallBack callback) {
        int state = UploadItemData.STATE_RUNNING;
        return state;
    }

    /**
     * 上传图片，使用普通方式上传
     *
     * @param itemData
     * @param callback
     * @return
     */
    private int startUploadImg(final UploadItemData itemData, final UploadListNetCallBack callback) {
        Log.i("articleUpload", "startUploadImg() path:" + itemData.getPath());
        new UploadImg("", itemData.getPath(), new InternetCallback(XHApplication.in().getApplicationContext()) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    Log.i("articleUpload", "startUploadImg() onSuccess()" + url);
                    callback.onSuccess((String) msg, itemData.getUniqueId(), null);
                } else {
                    callback.onFaild((String) msg, itemData.getUniqueId());
                    Log.i("articleUpload", "startUploadImg() onFaild()" + url);
                }
            }
        }).uploadImg();

        return UploadItemData.STATE_RUNNING;
    }


    /**
     * 停止上传
     *
     * @param uploadItemDataId 唯一标示
     * @return state 上传状态
     */
    public int stopUpload(String uploadItemDataId) {
        int state = 0;
        BreakPointControl uploader = uploaderMap.get(uploadItemDataId);
        if (uploader != null) {
            uploader.stop();
            state = UploadItemData.STATE_PAUSE;
        }
        return state;
    }


    /**
     * 判断当前正在上传的单项数据是否达到限制
     *
     * @return true 达到，false 没有
     */
    private boolean isUploadingOutLimit() {
        boolean flag = false;
        int upLoadingNum = 0;

        for (BreakPointControl breakPointControl : uploaderMap.values()) {
            if ("3".equals(breakPointControl.getReqState())
                    || "2".equals(breakPointControl.getReqState())
                    || "1".equals(breakPointControl.getReqState())) {
                if (++upLoadingNum == MAX_UPLOADING_NUM) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * 开启等待中的上传
     */
    public void startWaitingUpload() {
        for (final Map<String, UploadListPool> poolMap : uploadPoolList) {
            for (final UploadListPool pool : poolMap.values()) {
                UploadPoolData uploadPoolData = pool.getUploadPoolData();
                uploadPoolData.loopPoolData(uploadPoolData.getTotalDataList(),
                        new UploadPoolData.LoopCallback() {
                            @Override
                            public boolean onLoop(UploadItemData itemData) {
                                if (itemData.getState() == UploadItemData.STATE_WAITING) {
                                    int uploadState = startUpload(itemData, pool.getUploadPoolData().getNetCallback());
                                    itemData.setState(uploadState);
                                }
                                return false;
                            }
                        });
            }
        }

    }


    /**
     * 物料上传成功后，上传文字信息
     *
     * @param poolType 上传池类型
     * @param draftId  草稿id
     * @return
     */


    public int startUploadLast(final Class<? extends UploadListPool> poolType, final int draftId) {
        retryNum = 0;
        uploadLastInfo(poolType, draftId);
        return UploadItemData.STATE_RUNNING;
    }


    private void uploadLastInfo(final Class<? extends UploadListPool> poolType, final int draftId) {
        Log.i("articleUpload", "uploadLastInfo() draftId:" + draftId);
        final UploadListPool pool = getPool(poolType.getSimpleName() + draftId);
        if (pool == null) {
            Log.e("uploadLastInfo", "数据丢失");
            Toast.makeText(XHApplication.in(), "上传最后一步，数据丢失", Toast.LENGTH_SHORT).show();
            return;
        }

        UploadPoolData uploadPoolData = pool.getUploadPoolData();
        uploadPoolData.loopPoolData(uploadPoolData.getTailDataList(), new UploadPoolData.LoopCallback() {
            @Override
            public boolean onLoop(UploadItemData itemData) {
                if (itemData.getType() == UploadItemData.TYPE_LAST_TEXT) {
                    if (TextUtils.isEmpty(itemData.getUploadUrl())) {
                        Log.e("articleUpload", "上传url为空");
                        Toast.makeText(XHApplication.in(), "上传最后一步，上传url为空", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    String path = itemData.getUploadUrl();
                    Log.i("articleUpload", "uploadLastInfo() getUploadUrl:" + path);
                    InternetCallback internetCallback = new InternetCallback(XHApplication.in()) {
                        @Override
                        public void loaded(int flag, String url, Object msg) {
                            if (flag >= UtilInternet.REQ_OK_STRING) {
                                retryNum = 0;
                                pool.getUploadPoolData().getNetCallback().onLastUploadOver(true, (String) msg);
                            } else {
                                if (retryNum < MAX_RETRY_NUM) {
                                    retryNum++;
                                    uploadLastInfo(poolType, draftId);
                                } else {
                                    pool.getUploadPoolData().getNetCallback().onLastUploadOver(false, (String) msg);
                                }
                            }
                        }
                    };
                    Log.i("articleUpload", "uploadLastInfo() params:" + MapToString(itemData.getUploadMsg(), "&", "="));
                    if (path.contains("Main7")) {
                        ReqEncyptInternet.in().doEncypt(path, MapToString(itemData.getUploadMsg(), "&", "="), internetCallback);
                    } else {
                        ReqInternet.in().doPost(path, itemData.getUploadMsg(), internetCallback);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public String MapToString(Map<String, String> map, String line1, String line2) {
        StringBuffer str = new StringBuffer();
        int index = 0;
        for (String key : map.keySet()) {
            if (index != 0) {
                str.append(line1);
            }
            str.append(key);
            str.append(line2);
            str.append(map.get(key));
            index++;
        }
        return str.toString();
    }


    /**
     * 解除上传池
     * 删除对应的断点续传控制器
     * 从数组中删除上传池
     *
     * @param clazz   上传池字节码
     * @param draftId 草稿id
     * @return
     */
    public void destroyUploadPool(Class<? extends UploadListPool> clazz, int draftId) {

        UploadListPool uploadListPool = getPool(clazz.getSimpleName() + draftId);
        UploadPoolData uploadPoolData = uploadListPool.getUploadPoolData();
        uploadPoolData.loopPoolData(uploadPoolData.getTotalDataList(),
                new UploadPoolData.LoopCallback() {
                    @Override
                    public boolean onLoop(UploadItemData itemData) {
                        uploaderMap.remove(itemData.getUniqueId());
                        return false;
                    }
                });
        removeSpecPool(clazz.getSimpleName() + draftId);
    }


    private UploadListPool getPool(String poolKey) {
        UploadListPool pool = null;
        for (Map<String, UploadListPool> map : uploadPoolList) {
            if (map.containsKey(poolKey)) {
                pool = map.get(poolKey);
                break;
            }
        }
        return pool;
    }

    //从数组中删除上传池
    private void removeSpecPool(String poolKey) {
        Map<String, UploadListPool> poolMap = null;
        for (Map<String, UploadListPool> map : uploadPoolList) {
            if (map.containsKey(poolKey)) {
                poolMap = map;
                break;
            }
        }
        uploadPoolList.remove(poolMap);
    }

    public void allStartOrStop(int flag) {
        for (Map<String, UploadListPool> map : uploadPoolList) {
            for (UploadListPool pool : map.values()) {
                pool.allStartOrStop(flag);
            }
        }
    }

}
