package amodule.quan.tool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import acore.logic.XHClick;
import acore.tools.StringManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;

/**
 * PackageName : amodule.quan.tool
 * Created by MrTrying on 2016/9/27 18:49.
 * E_mail : ztanzeyu@gmail.com
 */

public class SubjectControl {
    private static volatile SubjectControl mInstance = null;

    public static SubjectControl getInstance(){
        if(mInstance == null){
            synchronized (SubjectControl.class){
                if(mInstance == null){
                    mInstance = new SubjectControl();
                }
            }
        }
        return mInstance;
    }

    /**
     *
     * @param mAct
     * @param params
     * @param callback
     */
    public void createDeleteDilog(final Activity mAct,final String params,String content,final OnDeleteSuccessCallback callback) {
        AlertDialog dialog = new AlertDialog.Builder(mAct)
                .setTitle("确认删除？")
                .setMessage("您确认要删除"+content+"吗？")
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ReqInternet.in().doPost(StringManager.api_quanSetSubject, params, new InternetCallback(mAct) {
                                    @Override
                                    public void loaded(int flag, String url, Object returnObj) {
                                        if (flag >= UtilInternet.REQ_OK_STRING) {
                                            callback.onDeleteSuccess(flag,url,returnObj);
                                        } else {
                                            toastFaildRes(flag, true, returnObj);
                                        }
                                    }
                                });
                            }
                        })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int which) {
                                dialog.cancel();
                            }
                        })
                .create();
        dialog.show();
    }

    public interface OnDeleteSuccessCallback{
        public void onDeleteSuccess(int flag, String url, Object returnObj);
    }
}
