package acore.tools;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.logging.Handler;

import acore.logic.VersionOp;
import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;

/**
 * Created by sll on 2018/1/8.
 */

public class SettingUtils {

    public void showNotificationPermissionSetView(){
        if(XHActivityManager.getInstance().getCurrentActivity()!=null){
            Activity activity = XHActivityManager.getInstance().getCurrentActivity();
//            if(!TextUtils.isEmpty((CharSequence) FileManager.loadShared(activity,FileManager.app_notification, VersionOp.getVerName(activity)))){
//                return;
//            }
            Log.i("xianghaTag","activity：：："+activity.getComponentName().getClassName());
            if(activity.findViewById(R.id.activityLayout)==null)return;
            showPermissionSetView(activity, (RelativeLayout) activity.findViewById(R.id.activityLayout));
        }
    }
    private void showPermissionSetView(Context context, RelativeLayout rl){
        if(context==null||rl==null)return;
        View view=LayoutInflater.from(context).inflate(R.layout.view_notification_set,null);
        RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,Tools.getDimen(context,R.dimen.dp_39));//两个参数分别是layout_width,layout_height
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        view.setLayoutParams(lp);
        view.findViewById(R.id.view_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {if(view!=null&&rl!=null)rl.removeView(view);}});
        rl.addView(view);
        FileManager.saveShared(context,FileManager.app_notification, VersionOp.getVerName(context),"2");
    }
    public static class NotificationSetting {

//        public static void requestApp

    }

    public static class ApplicationSetting {

        public static void openApplicationDetailSettings() {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, getPackageUri());
            XHApplication.in().getApplicationContext().startActivity(intent);
        }

    }

    public static void openSystemSettings() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        XHApplication.in().getApplicationContext().startActivity(intent);
    }

    private static Uri getPackageUri() {
        return Uri.parse("package:" + XHApplication.in().getPackageName());
    }
}
