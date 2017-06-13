package amodule.upload;

import android.util.Log;

import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import amodule.upload.bean.UploadItemData;
import amodule.upload.bean.UploadPoolData;
import amodule.upload.callback.UploadListNetCallBack;
import amodule.upload.callback.UploadListUICallBack;

import static amodule.main.Main.timer;

/**
 * Created by ：fei_teng on 2016/10/27 20:52.
 */
public class UploadListPool {


    //操作标志
    public static final int TYPE_START = 1;
    public static final int TYPE_PAUSE = 2;
    protected UploadPoolData uploadPoolData;

    protected boolean isPause = false;
    protected boolean isCancel = false;


    public UploadListPool() {
        init();
    }


    /**
     * 创建上传池数据
     */
    public void init() {
        uploadPoolData = new UploadPoolData();
        uploadPoolData.setNetCallback(getUploadListNetCallBack());
    }

//    /**
//     * 初始化，设置定时刷新
//     *
//     * @param draftId
//     * @param callback
//     */
//    protected void initData(int draftId, final UploadListUICallBack callback) {
//        setUiCallback(callback);
//    }

    /**
     *  * 临时方法，处理步骤视频，最终视频路径，大图路径丢失
     * 处理完数据库数据丢失问题后应该删除该方法
     * 初始化，设置定时刷新
     * @param draftId
     * @param callback
     */
    protected void initData(int draftId,String coverPath,String finalVideoPath,String timestamp,
                            final UploadListUICallBack callback) {
        setUiCallback(callback);
    }

    /**
     *  * 临时方法，处理步骤视频，最终视频路径，大图路径丢失
     * 处理完数据库数据丢失问题后应该删除该方法
     * 初始化，设置定时刷新
     * @param draftId
     * @param callback
     */
    protected void initData(int dataType,int draftId,String coverPath,String finalVideoPath,String timestamp,
                            final UploadListUICallBack callback) {
        setUiCallback(callback);
    }


    private void refreshUi() {

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                uploadPoolData.getUiCallback().changeState();
            }
        }, 0, 1000);

    }


    /**
     * 全部暂停或开始
     *
     * @param operation TYPE_START 开始，TYPE_PAUSE 暂停
     */
    public void allStartOrStop(final int operation) {
        uploadPoolData.loopPoolData(uploadPoolData.getTotalDataList(), new UploadPoolData.LoopCallback() {
            @Override
            public boolean onLoop(UploadItemData itemData) {
                if (itemData.getState() != UploadItemData.STATE_SUCCESS) {
                    Log.i("articleUpload","allStartOrStop " + itemData.getPos() + "   " + itemData.getPath());
                    startOrStop(itemData.getPos(), itemData.getIndex(), operation);
                }
                return false;
            }
        });

        if (operation == TYPE_PAUSE) {
            isPause = true;
            //暂时无用，暂停为全局暂停,暂停后不可能存在等待上传的itemData
//            UploadListControl.getUploadListControlInstance().startWaitingUpload();
        } else {
            isPause = false;
        }
    }


    /**
     * 单独暂停或开始一个
     *
     * @param pos       位置常量
     * @param index     序号
     * @param operation TYPE_START 开始，TYPE_PAUSE 停止
     */
    public void oneStartOrStop(int pos, int index, int operation) {
        startOrStop(pos, index, operation);
        if (operation == TYPE_PAUSE) {
            UploadListControl.getUploadListControlInstance().startWaitingUpload();
        }
    }


    /**
     * 单个上传
     *
     * @param pos       位置常量
     * @param index     序号
     * @param operation TYPE_START 开始，TYPE_PAUSE 停止
     */
    public void startOrStop(int pos, int index, int operation) {
        List<UploadItemData> itemDatas = uploadPoolData.getUploadItemDataList(pos);
        UploadItemData itemData = itemDatas.get(index);
        Log.i("articleUpload","startOrStop() pos:" + pos + "   path:" + itemData.getPath());
        Log.i("articleUpload","startOrStop() operation:" + operation + "   itemData.getType():" + itemData.getType());
        if (operation == TYPE_START) {
            if (itemData.getType() == UploadItemData.TYPE_LAST_TEXT) {
                uploadLast();
            } else if (itemData.getType() == UploadItemData.TYPE_IMG || itemData.getType() == UploadItemData.TYPE_BREAKPOINT_IMG || itemData.getType() == UploadItemData.TYPE_VIDEO) {
                int state = UploadListControl.getUploadListControlInstance().startUpload(itemData, uploadPoolData.getNetCallback());
                itemData.setState(state);
            }
        } else {
            int state = UploadListControl.getUploadListControlInstance().stopUpload(itemData.getUniqueId());
            itemData.setState(state);
        }
    }


    public void setUploadProcess() {

        uploadPoolData.loopPoolData(uploadPoolData.getTotalDataList(), new UploadPoolData.LoopCallback() {
            @Override
            public boolean onLoop(UploadItemData itemData) {
                int progress = UploadListControl.getUploadListControlInstance().initUpload(itemData);
                itemData.setProgress(progress);
                return false;
            }
        });
    }


    /**
     * 上传物料结束
     *
     * @param flag        true 成功 false 失败
     * @param uniquId     唯一标示
     * @param responseStr
     * @param jsonObject
     */
    protected void uploadThingOver(boolean flag, final String uniquId, String responseStr, JSONObject jsonObject) {
        UploadListControl.getUploadListControlInstance().startWaitingUpload();
    }


    /**
     * 上传最终信息
     */
    public void uploadLast() {
        UploadListControl.getUploadListControlInstance()
                .startUploadLast(this.getClass(), uploadPoolData.getDraftId());

        uploadPoolData.loopPoolData(uploadPoolData.getTailDataList(), new UploadPoolData.LoopCallback() {
            @Override
            public boolean onLoop(UploadItemData itemData) {
                if (itemData.getType() == UploadItemData.TYPE_LAST_TEXT) {
                    itemData.setState(UploadItemData.STATE_RUNNING);
                    return true;
                }
                return false;
            }
        });

    }

    /**
     * 上传结束回调
     *
     * @param flag true 成功  false 失败
     */
    public void uploadOver(boolean flag, String response) {
        uploadPoolData.getUiCallback().uploadOver(flag, response);
        if (flag) {
            UploadListControl.getUploadListControlInstance()
                    .destroyUploadPool(this.getClass(), uploadPoolData.getDraftId());
        }

    }

    /**
     * 取消上传
     */
    public void cancelUpload() {
        isCancel = true;
        allStartOrStop(UploadListPool.TYPE_PAUSE);
        UploadListControl.getUploadListControlInstance()
                .destroyUploadPool(this.getClass(), uploadPoolData.getDraftId());

    }


    /**
     * 解除UI绑定
     */
    public void unBindUI() {
        if(timer!=null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        if(uploadPoolData!=null){
        uploadPoolData.setUiCallback(new UploadListUICallBack() {

            @Override
            public void changeState() {

            }

            @Override
            public void changeState(int pos, int index, UploadItemData data) {

            }

            @Override
            public void uploadOver(boolean flag, String responseStr) {

            }
        });}
    }

    public void setUiCallback(UploadListUICallBack callback) {
        uploadPoolData.setUiCallback(callback);
        refreshUi();
    }

    private UploadListNetCallBack getUploadListNetCallBack() {
        return new UploadListNetCallBack() {

            @Override
            public void onProgress(double progress, String uniquId) {
                UploadItemData speciaItem = uploadPoolData.getSpeciaItem(uniquId);
                if (speciaItem != null) {
                    speciaItem.setState(UploadItemData.STATE_RUNNING);
                    speciaItem.setProgress((int) (progress * 100));
                }
            }

            @Override
            public void onSuccess(String responseStr, String uniquId, JSONObject jsonObject) {
//                Log.i("articleUpload","getUploadListNetCallBack() onSuccess()" + responseStr);
//                UploadItemData speciaItem = uploadPoolData.getSpeciaItem(uniquId);
//                if (speciaItem != null) {
//                    speciaItem.setState(UploadItemData.STATE_SUCCESS);
//                }
                uploadThingOver(true, uniquId, responseStr, jsonObject);
            }


            @Override
            public void onFaild(String faild, String uniqueId) {
//                Log.i("articleUpload","getUploadListNetCallBack() onFaild()");
//                UploadItemData speciaItem = uploadPoolData.getSpeciaItem(uniqueId);
//                if (speciaItem != null) {
//                    speciaItem.setState(UploadItemData.STATE_FAILD);
//                }
                uploadThingOver(false, uniqueId, faild, null);
            }

            @Override
            public void onLastUploadOver(boolean flag, String responseStr) {
                uploadOver(flag, responseStr);
            }

            @Override
            public void onProgressSpeed(final String uniqueId, final long speed) {
                uploadPoolData.loopPoolData(uploadPoolData.getBodyDataList(), new UploadPoolData.LoopCallback() {
                    @Override
                    public boolean onLoop(UploadItemData itemData) {
                        if (itemData.getUniqueId().equals(uniqueId)) {
                            itemData.setSpeed(speed);
                            return true;
                        }
                        return false;
                    }
                });
            }
        };
    }

    public UploadPoolData getUploadPoolData() {
        return uploadPoolData;
    }

}
