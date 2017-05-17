package amodule.dish.tools;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.text.DecimalFormat;

import acore.tools.Tools;
import amodule.dish.business.DishVideoDownloaderManager;
import amodule.dish.view.CommonDialog;

/**
 * 视频：从网络获取片头片尾，bmg
 * 1、请求网路请求是否有新的数据需要下载。
 * 2、把下载的文件保存到指定的路径下，并保证指定的md5值。
 * 注意:
 * 1、后台下载功能和失败后的三次重试机制。
 * 2、跟服务端校验是否有新数据，要跟服务端对接时确定。
 */
public class MediaReqDataContorl {
    private Activity mAct;
    private Context context;
    private int state = 0;//当前状态 0:未获取到更新数据, 1:有更新数据,但未进行下载. 2:正在进行更新. 3:已经更新完成. 4:当前无更新.
    private DishVideoDownloaderManager downloaderManager;
    private CommonDialog updateDialog;
    private CommonDialog downloadDialog;
    private int reTryLimit = 3;
    private int currentTry;

    public MediaReqDataContorl(final Activity activity) {
        this.mAct = activity;
        this.context = activity.getApplicationContext();

        downloaderManager = DishVideoDownloaderManager.getDishVideoManagerInstance();

    }

    /**
     * 外部只调用该方法，根据不同状态进行不同操作
     *
     * @return 只有3，4 状态时返回true
     */
    public synchronized boolean start() {
        boolean flag = false;
        if (state == 0 || state == 1 || state == 2) {
            flag = false;
        } else if (state == 3 || state == 4) {
            flag = true;
        }
        if (state == 0) {
            downloaderManager.setCallback(new DishVideoDownloaderManager.DownloadCallback() {
                @Override
                public void onGetTotalLength(long total) {
                    state = 0;
                    updateTipDialog(total);
                }

                @Override
                public void onProgress(long current, long total) {
                    state = 2;
                    downloadDataDialog(current, total);
                }

                @Override
                public void onFail() {

                    if (currentTry < reTryLimit) {
                        currentTry++;
                        downloaderManager.downloadDishVideo(context.getApplicationContext());
                    } else {
                        state = 0;
                        Tools.showToast(context.getApplicationContext(), "下载失败");
                        closeUi();
                    }
                }

                @Override
                public void onCancel() {
                    state = 0;
                    closeUi();
                }

                @Override
                public void onSuccess() {
                    state = 3;
                    Tools.showToast(context.getApplicationContext(), "下载完成");
                    closeUi();
                }

                @Override
                public void onAllHasUpdate() {
                    state = 4;
                    closeUi();
                }
            });
            downloaderManager.getUpdateInfo(context);
        }
        return flag;
//        return true;
    }

    /**
     * 提示更新，片头片尾数据
     *
     * @param fileSize
     */
    public void updateTipDialog(final long fileSize) {


        String showInfo = "需下载" + Tools.getPrintSize(fileSize) + "的片头片尾文件才能合成视频~";
        String btnMsg1 = "取消";
        String btnMsg2 = "下载";


        updateDialog = new CommonDialog(mAct);
        updateDialog.setCancelable(false);
        updateDialog.setMessage(showInfo).setSureButton(btnMsg2, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloaderManager.downloadDishVideo(context);
                updateDialog.cancel();
                downloadDataDialog(0, fileSize);
            }
        });

        updateDialog.setCanselButton(btnMsg1, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeUi();
            }
        });
        updateDialog.show();
    }

    private void closeUi() {
        if (updateDialog != null) {
            updateDialog.cancel();
        }

        if (downloadDialog != null) {
            downloadDialog.cancel();
        }
        downloaderManager.releaseCallback();
    }


    /**
     * 下载数据
     *
     * @param current
     * @param total
     */
    public void downloadDataDialog(final long current, final long total) {


        final float totalSize = ((int) (total / 1024 / 1024f * 100)) / 100f;
        final float currentSize =  (float)(Math.round(current / 1024f / 1024f * 100))/100;


        DecimalFormat decimalFormat=new DecimalFormat("0.0");
        String totalString= decimalFormat.format(totalSize);
        String currentString= decimalFormat.format(currentSize);
        final String showInfo = "正在下载(" + currentString + "MB/" + totalString + "MB)";
        String btnMsg1 = "取消";
        String btnMsg2 = "后台下载";

        if (downloadDialog == null) {
            downloadDialog = new CommonDialog(mAct);
            downloadDialog.setCancelable(false);
            downloadDialog.setMessage(showInfo).setSureButton(btnMsg2, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadDialog.cancel();
                }
            });

            downloadDialog.setCanselButton(btnMsg1, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloaderManager.cancelDownload();
                    downloadDialog.cancel();
                    closeUi();
                }
            });
            downloadDialog.show();
        }

        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadDialog.setMessage(showInfo);
                downloadDialog.setProgress((int) (currentSize * 100.0f / totalSize));
            }
        });
    }


}