package acore.logic;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import acore.tools.FileManager;
import acore.tools.Tools;
import amodule.dish.activity.MenuDish;

/**
 * Description : 特殊指令处理类
 * PackageName : acore.logic
 * Created by MrTrying on 2017/11/7 11:06.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class SpecialOrder {
    private static final String ORDER_PREFIX = "//";
    private static final String GrowingIOOrder = "//growingioopen";
    private static final String START_MENU = "//startmenu";

    public static SpecialOrder of(){
        return new SpecialOrder();
    }

    /**
     * 执行指令
     * @param order 指令
     * @return 是否为有效指令
     */
    public boolean handlerOrder(Context context, String order){
        if (null == context || TextUtils.isEmpty(order)){
            return false;
        }
        switch(order){
            case GrowingIOOrder:
                String isInputOrder = FileManager.loadShared(context, FileManager.file_appData, FileManager.xmlKey_growingioopen).toString();
                boolean isOpen = "true".equals(isInputOrder);
                FileManager.saveShared(context, FileManager.file_appData, FileManager.xmlKey_growingioopen,  isOpen ? "false" : "true");
                Tools.showToast(context,isOpen?"GrowingIO随即模式":"GrowingIO强制开启模式");
                return true;
            case START_MENU:
                context.startActivity(new Intent(context, MenuDish.class));
                return true;
            default:
                return order.startsWith(ORDER_PREFIX);
        }
    }
}
