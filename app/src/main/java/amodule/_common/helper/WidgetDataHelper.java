package amodule._common.helper;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * PackageName : amodule._common
 * Created by MrTrying on 2017/11/13 10:19.
 * E_mail : ztanzeyu@gmail.com
 */

public class WidgetDataHelper {

    /**控件类型字段*/
    public static final String KEY_WIDGET_TYPE = "widgetType";
    /**数据类型字段*/
    public static final String KEY_WIDGET_DATA = "widgetData";

    /**公共数据字段*/
    public static final String KEY_WIDGET_PARAMETER = "widgetParameter";
    /**额外数据字段*/
    public static final String KEY_WIDGET_EXTRA = "widgetExtra";

    public static final String KEY_DATA = "data";

    public static final String KEY_PARAMETER = "parameter";
    /**widget start方向插入数据*/
    public static final String KEY_TOP = "top";
    /**widget end方向插入数据*/
    public static final String KEY_BOTTOM = "bottom";

    /**--------------------------------------------------- 内部数据key ---------------------------------------------------*/
    public static final String KEY_STYLE = "style";

    /**
     *
     * @param textView
     * @param text 文本
     */
    public static void setTextToView(TextView textView,String text){
        setTextToView(textView,text,true);
    }

    /**
     *
     * @param textView
     * @param text 文本
     * @param canHide 文本为空是否GONE
     */
    public static void setTextToView(TextView textView,String text,boolean canHide){
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
    public static void setResToImage(ImageView image,int resIcon){
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
