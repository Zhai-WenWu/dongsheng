package amodule._common.utility;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.ColorUtil;
import acore.tools.StringManager;
import aplug.web.FullScreenWeb;
import aplug.web.ShowWeb;
import third.mall.activity.CommodDetailActivity;

/**
 * Created by sll on 2017/11/17.
 */

public class WidgetUtility {
    /**
     *
     * @param textView
     * @param text 文本
     */
    public static void setTextToView(TextView textView, @Nullable CharSequence text){
        setTextToView(textView,text,true);
    }

    /**
     *
     * @param textView
     * @param text 文本
     * @param canHide 文本为空是否GONE
     */
    public static void setTextToView(TextView textView, @Nullable CharSequence text, boolean canHide){
        if(null == textView){
            return;
        }
        if(TextUtils.isEmpty(text)){
            textView.setVisibility(canHide? View.GONE:View.INVISIBLE);
        }else{
            textView.setText(text);
            textView.setVisibility(View.VISIBLE);
        }
    }

    /**
     *
     * @param image
     * @param resIcon 资源文件id
     */
    public static void setResToImage(ImageView image, int resIcon){
        setResToImage(image,resIcon,true);
    }

    /**
     *
     * @param image
     * @param resIcon 资源文件id
     * @param canHide 文本为空是否GONE
     */
    public static void setResToImage(ImageView image,int resIcon,boolean canHide){
        if(null == image){
            return;
        }
        if(resIcon <= 0){
            image.setVisibility(canHide? View.GONE:View.INVISIBLE);
        }else{
            image.setImageResource(resIcon);
            image.setVisibility(View.VISIBLE);
        }
    }

}
