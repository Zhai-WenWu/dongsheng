package amodule.dish.BrocastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.UploadNetChangeWindowDialog;
import amodule.dish.db.UploadDishSqlite;
import amodule.main.Main;
import amodule.upload.UploadListControl;
import amodule.upload.UploadListPool;

/**
 * Created by ：fei_teng on 2016/11/16 16:43.
 */

public class NetChangeBrocastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (ToolsDevice.isNetworkAvailable(context)
                && !"wifi".equals(ToolsDevice.getNetWorkType(context))) {
            UploadDishSqlite sqlite = new UploadDishSqlite(context);
            if (sqlite.getUploadingId() > 0)
                if (Tools.isForward(context))
                    showNetChangeDialog();
        }
    }


    private void showNetChangeDialog() {

        if (BaseActivity.mUploadNetChangeWindowDialog != null) {
            BaseActivity.mUploadNetChangeWindowDialog.onResume();
        } else {
            UploadNetChangeWindowDialog dialog = new UploadNetChangeWindowDialog(Main.allMain,
                    new UploadNetChangeWindowDialog.NetChangeCallback() {
                        @Override
                        public void onClickSure() {
                            UploadListControl.getUploadListControlInstance().allStartOrStop(UploadListPool.TYPE_PAUSE);
                        }

                        @Override
                        public void onClickNegative() {
//                        UploadListControl.getUploadListControlInstance().allStartOrStop(UploadListPool.TYPE_START);
                        }
                    });
            dialog.show();
            BaseActivity.mUploadNetChangeWindowDialog = dialog;
        }
    }
}
