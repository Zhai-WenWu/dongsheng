package acore.logic.login;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.FileManager;

/**
 * Created by ：fei_teng on 2017/2/15 16:45.
 */

public class LoginCheck {

    private final static String LAST_ACCOUT_INFO = "last_accout_info";
    private final static String ACCOUT_TYPE = "accout_type";
    private final static String TYPE_PHONE = "type_phone";
    private final static String TYPE_MAILBOX = "type_mailbox";
    private final static String AREA_CODE = "area_code";
    private final static String PHONE_NUMBER = "phone_number";
    private final static String MAIL_BOX = "mail_box";

    //手机号码错误类型
    public final static String WELL_TYPE = "well_type";
    public final static String NOT_11_NUM = "not_11_num";
    public final static String MISS_ZONECODE = "miss_zonecode";
    public final static String MISS_PHONENUM = "miss_phonenum";
    public final static String ERROR_FORMAT = "error_format";


    public static void saveLastLoginAccoutInfo(Context context, String type, String zoneCode,
                                               String phoneNum, String mailbox) {
        Map<String, String> infoMap = new HashMap<>();
        infoMap.put(ACCOUT_TYPE, type);
        infoMap.put(AREA_CODE, zoneCode);
        infoMap.put(PHONE_NUMBER, phoneNum);
        infoMap.put(MAIL_BOX, mailbox);
        FileManager.saveShared(context, LAST_ACCOUT_INFO, infoMap);
    }

    public static AccountInfoBean getLastLoginAccout(Context context) {

        AccountInfoBean infoBean = new AccountInfoBean();
        infoBean.setAccoutType((String) FileManager.loadShared(context, LAST_ACCOUT_INFO, ACCOUT_TYPE));
        infoBean.setAreaCode((String) FileManager.loadShared(context, LAST_ACCOUT_INFO, AREA_CODE));
        infoBean.setPhoneNum((String) FileManager.loadShared(context, LAST_ACCOUT_INFO, PHONE_NUMBER));
        infoBean.setMailBox((String) FileManager.loadShared(context, LAST_ACCOUT_INFO, MAIL_BOX));

        return infoBean;
    }

    public static String checkPhoneFormatWell(Context context, String areaCode, String phoneStr) {

        if (areaCode == null || TextUtils.isEmpty(areaCode.trim())){
            return MISS_ZONECODE;
        }
        if ( phoneStr == null || TextUtils.isEmpty(phoneStr.trim()))
            return MISS_PHONENUM;
        areaCode = areaCode.trim();
        phoneStr = phoneStr.trim();

        if (areaCode.contains("^\\d") || phoneStr.contains("^\\d")) {
            return ERROR_FORMAT;
        }

        String errorType = "";
        if ("86".equals(areaCode)) {
            if (phoneStr.length() == 11) {
                errorType = WELL_TYPE;
            } else {
                errorType = NOT_11_NUM;
                Toast.makeText(context, "手机号有误，应该是11位数字", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (phoneStr.length() <= 20) {
                errorType = WELL_TYPE;
            } else {
                errorType = ERROR_FORMAT;
                Toast.makeText(context, "手机号格式错误", Toast.LENGTH_SHORT).show();
            }
        }

        return errorType;
    }

    public static boolean isSecretFormated(String secret) {

        boolean flag = !(TextUtils.isEmpty(secret) || secret.length() < 6 || secret.length() > 20);

        if (!flag) {
            Toast.makeText(XHApplication.in(), "密码为6-20位字母、数字或字符", Toast.LENGTH_SHORT).show();
        }
        return flag;
    }

    public static boolean checkMailboxValid(Context context, String mailbox) {

        String mailboxExg = "^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+)+)$";
        boolean isMatches = mailbox.matches(mailboxExg);
        if (!isMatches) {
            Toast.makeText(context, "邮箱格式不正确", Toast.LENGTH_SHORT).show();
        }
        return isMatches;
    }
}
