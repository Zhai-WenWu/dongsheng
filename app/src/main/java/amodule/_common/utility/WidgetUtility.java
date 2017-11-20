package amodule._common.utility;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
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

    public static boolean handlerClickEvent(String mTransferUrl,String moduleType,String dataType,int position){
        if(TextUtils.isEmpty(mTransferUrl))
            return false;
        if (!TextUtils.isEmpty(mTransferUrl)) {
                if (!mTransferUrl.contains("data_type=") && !mTransferUrl.contains("module_type=")) {
                    if (!mTransferUrl.startsWith("http")) {
                        if (mTransferUrl.contains("?"))
                            mTransferUrl += "&data_type=" + dataType;
                        else
                            mTransferUrl += "?data_type=" + dataType;
                        mTransferUrl += "&module_type=" + moduleType;
                    }
                    //TODO
//                Log.i("zhangyujian", "点击：" + mDataMap.get("code") + ":::" + mTransferUrl);
//                XHClick.saveStatictisFile("home", moduleType, dataType, mDataMap.get("code"), "", "click", "", "", String.valueOf(position + 1), "", "");
                XHClick.saveStatictisFile("home", moduleType, dataType, "", "", "click", "", "", String.valueOf(position + 1), "", "");
            }
            if (mTransferUrl.contains("dishInfo.app")
                    && !TextUtils.isEmpty(dataType)
                    && !"2".equals(dataType)) {
                //TODO
//                mTransferUrl += "&img=" + mDataMap.get("img");
            }
            String params = mTransferUrl.substring(mTransferUrl.indexOf("?") + 1, mTransferUrl.length());
            Log.i("zhangyujian", "mTransferUrl:::" + params);
            Map<String, String> map = StringManager.getMapByString(params, "&", "=");
            Class c = null;
            Intent intent = new Intent();
            if (mTransferUrl.startsWith("http")) {//我的香豆、我的会员页面
                if (mTransferUrl.contains("fullScreen=2")) {
                    c = FullScreenWeb.class;
                    intent.putExtra("url", mTransferUrl);
                    intent.putExtra("code", map.containsKey("code") ? map.get("code") : "");
                } else {
                    c = ShowWeb.class;
                    intent.putExtra("url", mTransferUrl);
                    intent.putExtra("code", map.get("code"));
                }
            } else if (mTransferUrl.contains("xhds.product.info.app?")) {//商品详情页，原生
                c = CommodDetailActivity.class;
                for(String key : map.keySet()){//取全部参数。
                    intent.putExtra(key, map.get(key));
                }
            } else if (mTransferUrl.contains("nousInfo.app")) {
                c = ShowWeb.class;
                intent.putExtra("url", StringManager.api_nouseInfo);
                intent.putExtra("code", map.get("code"));
            }
            if (c != null) {
                intent.putExtra("data_type", dataType);
                intent.putExtra("module_type", moduleType);
                intent.setClass(XHActivityManager.getInstance().getCurrentActivity(), c);
                XHActivityManager.getInstance().getCurrentActivity().startActivity(intent);
                return true;
            }
        }
        return false;
    }
}
