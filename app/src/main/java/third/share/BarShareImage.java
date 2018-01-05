/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package third.share;

import android.content.Context;
import android.support.annotation.NonNull;

import acore.tools.IObserver;
import acore.tools.ObserverManager;
import third.share.activity.ShareImageActivity;

/**
 * PackageName : third.share
 * Created by MrTrying on 2017/8/31 17:31.
 * E_mail : ztanzeyu@gmail.com
 */

public class BarShareImage {

    Context mContext;
    String imageUrl;

    public BarShareImage(Context context, @NonNull String imageUrl){
        this.mContext = context;
        this.imageUrl = imageUrl;
        if(context instanceof IObserver){
            ObserverManager.getInstance().registerObserver((IObserver) context,ObserverManager.NOTIFY_SHARE);
        }
    }

    public void openShareImage(){
        ShareImageActivity.openShareImageActivity(mContext, imageUrl);
    }
}
