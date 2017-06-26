package acore.dialogManager;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import acore.override.XHApplication;
import acore.tools.FileManager;

/**
 * Created by Fang Ruijiao on 2017/5/4.
 */
public class DialogControler {

    private VersionOp versionOp;
    private boolean isNeedUpdata = false;

    public void showDialog(){
        toVersionUpdata();
    }

    private void toVersionUpdata(){
        versionOp = VersionOp.getInstance();
        versionOp.isShow(new DialogManagerParent.OnDialogManagerCallback() {
            @Override
            public void onShow() {
//                Tools.showToast(XHApplication.in(),"toVersionUpdata onShow() versionOp.isMustUpdata:" + versionOp.isMustUpdata);
                isNeedUpdata = true;
                if(versionOp.isMustUpdata){
                    versionOp.show();
                }else{
                    toGetGuidData();
                }
            }

            @Override
            public void onGone() {
//                Tools.showToast(XHApplication.in(),"toVersionUpdata onGone()");
                isNeedUpdata = false;
                toGetGuidData();
            }
        });
    }

    private void toGetGuidData(){
//        Tools.showToast(XHApplication.in(),"toGetGuidData()");
        final GuideManager guideManager = new GuideManager();
        guideManager.isShow(new DialogManagerParent.OnDialogManagerCallback() {
            @Override
            public void onShow() {
//                Tools.showToast(XHApplication.in(),"toGetGuidData onShow()");
                //导流弹框
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        guideManager.show();
                    }
                },5 * 1000);
            }

            @Override
            public void onGone() {
//                Tools.showToast(XHApplication.in(),"toGetGuidData onGone()");
                if(isNeedUpdata){
                    versionOp.show();
                }else{
                    toADPopwindowData();
                }
            }
        });
    }

    private void toADPopwindowData(){
        ADPopwindiwManager adPopwindiwManager = new ADPopwindiwManager();
        adPopwindiwManager.isShow(new DialogManagerParent.OnDialogManagerCallback() {
            @Override
            public void onShow() {
                //什么都不用做，只是回调
            }

            @Override
            public void onGone() {
                toGetGoodData();
            }
        });
    }

    private void toGetGoodData(){
        final GoodCommentManager goodCommentManager = new GoodCommentManager();
        goodCommentManager.isShow(new DialogManagerParent.OnDialogManagerCallback() {
            @Override
            public void onShow() {
//                Tools.showToast(XHApplication.in(),"toGetGoodData onShow()");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        goodCommentManager.show();
                    }
                },9 * 1000);
            }

            @Override
            public void onGone() {
//                Tools.showToast(XHApplication.in(),"toGetGoodData onGone()");
                String show_time = (String)FileManager.loadShared(XHApplication.in(),FileManager.GOODCOMMENT_SHOW_TIME,FileManager.GOODCOMMENT_SHOW_TIME);
                String show_num_all = (String) FileManager.loadShared(XHApplication.in(), FileManager.GOODCOMMENT_SHOW_NUM_ALL, FileManager.GOODCOMMENT_SHOW_NUM_ALL);
                int all_num = 0;
                if(!TextUtils.isEmpty(show_num_all)){
                    all_num = Integer.parseInt(show_num_all);
                }
                //当距离上次的好评弹框72小时才弹推送弹框
                if(all_num > 0 && !TextUtils.isEmpty(show_time) && System.currentTimeMillis() - Long.parseLong(show_time) < 72 * 60 * 60 * 1000){
                    return;
                }
                toGetPushData();
            }
        });

    }

    private void toGetPushData(){
        final PushManager dialogManagerParent = new PushManager();
        dialogManagerParent.isShow(new DialogManagerParent.OnDialogManagerCallback(){

            @Override
            public void onShow() {
//                Tools.showToast(XHApplication.in(),"toGetPushData onShow()");
                dialogManagerParent.show();
            }

            @Override
            public void onGone() {
//                Tools.showToast(XHApplication.in(),"toGetPushData onGone()");
            }
        });
    }
}
