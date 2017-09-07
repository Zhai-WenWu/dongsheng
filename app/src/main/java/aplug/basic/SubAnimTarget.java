/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package aplug.basic;

import android.widget.ImageView;

import acore.logic.XHClick;
import acore.override.XHApplication;
import xh.basic.internet.img.BitmapAnimableTarget;

/**
 * PackageName : aplug.basic
 * Created by MrTrying on 2017/9/6 23:44.
 * E_mail : ztanzeyu@gmail.com
 */

public abstract class SubAnimTarget extends BitmapAnimableTarget {
    public SubAnimTarget(ImageView imageView) {
        super(imageView);
    }

    @Override
    public void switchDNS() {
        XHClick.statisticsSwitchDNS(XHApplication.in());
    }
}
