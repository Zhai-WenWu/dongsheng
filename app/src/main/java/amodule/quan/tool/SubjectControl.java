package amodule.quan.tool;

import android.app.Activity;
import android.view.View;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.MessageView;
import com.xh.view.TitleView;

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
        final DialogManager dialogManager = new DialogManager(mAct);
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleView(mAct).setText("确认删除"))
                .setView(new MessageView(mAct).setText("您确认要删除" +content+ "吗？"))
                .setView(new HButtonView(mAct)
                        .setNegativeText("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogManager.cancel();
                            }
                        })
                        .setPositiveText("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogManager.cancel();
                                ReqInternet.in().doPost(StringManager.api_quanSetSubject, params, new InternetCallback(mAct) {
                                    @Override
                                    public void loaded(int flag, String url, Object returnObj) {
                                        if (flag >= UtilInternet.REQ_OK_STRING) {
                                            callback.onDeleteSuccess(flag,url,returnObj);
                                        }
                                    }
                                });
                            }
                        }))).show();
    }

    public interface OnDeleteSuccessCallback{
        public void onDeleteSuccess(int flag, String url, Object returnObj);
    }
}
