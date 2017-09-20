package com.popdialog.util;

import android.content.Context;

/**
 * PackageName : com.popdialog.util
 * Created by MrTrying on 2017/9/19 16:28.
 * E_mail : ztanzeyu@gmail.com
 */

public class Tools {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        int res = 0;
        final float scale = context.getResources().getDisplayMetrics().density;
        if (dpValue < 0)
            res = -(int) (-dpValue * scale + 0.5f);
        else
            res = (int) (dpValue * scale + 0.5f);
        return res;
    }

    /**
     * @param context
     * @param id      dimens文件中的id(仅适用于dp)
     * @return dimen 对应分辨率的dp或者sp值
     */
    public static int getDimen(Context context, int id) {
        float dimen = 0;
        String string = context.getResources().getString(id).replace("dip", "");
        dimen = Float.parseFloat(string);
        return dip2px(context, dimen);
    }

    /**
     *
     * @param context
     * @param tvWidth : textView的宽
     * @param tvSize
     * @return
     */
    public static int getTextNumbers (Context context, int tvWidth, int tvSize) {
        int tv_pad = dip2px(context, 1.0f);
        /* 判断是否等于0 */
        return tvSize + tv_pad > 0 ? (tvWidth + tv_pad) / (tvSize + tv_pad) : 0;
    }
}
