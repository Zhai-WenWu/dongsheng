package acore.dialogManager;

import android.os.Handler;
import android.util.Log;

/**
 * Created by XiangHa on 2017/5/4.
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
                Log.i("tzy","toVersionUpdata onShow() versionOp.isMustUpdata:" + versionOp.isMustUpdata);
                isNeedUpdata = true;
                if(versionOp.isMustUpdata){
                    versionOp.show();
                }else{
                    toGetGuidData();
                }
            }

            @Override
            public void onGone() {
                Log.i("tzy","toVersionUpdata onGone()");
                isNeedUpdata = false;
                toGetGuidData();
            }
        });
    }

    private void toGetGuidData(){
        Log.i("tzy","toGetGuidData()");
        final GuideManager guideManager = new GuideManager();
        guideManager.isShow(new DialogManagerParent.OnDialogManagerCallback() {
            @Override
            public void onShow() {
                Log.i("tzy","toGetGuidData onShow()");
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
                Log.i("tzy","toGetGuidData onGone()");
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
                Log.i("tzy","toADPopwindowData onShow()");
            }

            @Override
            public void onGone() {
                Log.i("tzy","toADPopwindowData onShow()");
                toGetGoodData();
            }
        });
    }

    private void toGetGoodData(){
        final GoodCommentManager goodCommentManager = new GoodCommentManager();
        goodCommentManager.isShow(new DialogManagerParent.OnDialogManagerCallback() {
            @Override
            public void onShow() {
                Log.i("tzy","toGetGoodData onShow()");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        goodCommentManager.show();
                    }
                },9 * 1000);
            }

            @Override
            public void onGone() {
                Log.i("tzy","toGetGoodData onGone()");
                toGetPushData();
            }
        });

    }

    private void toGetPushData(){
        final PushManager dialogManagerParent = new PushManager();
        dialogManagerParent.isShow(new DialogManagerParent.OnDialogManagerCallback(){

            @Override
            public void onShow() {
                Log.i("tzy","toGetPushData onShow()");
                dialogManagerParent.show();
            }

            @Override
            public void onGone() {
                Log.i("tzy","toGetPushData onGone()");
            }
        });
    }
}
