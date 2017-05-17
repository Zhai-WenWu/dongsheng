package acore.logic.login;

/**
 * Created by ：fei_teng on 2017/2/17 12:03.
 */

public class AccountInfoBean {

    public static final String ACCOUT_PHONE = "accout_phone";
    public static final String ACCOUT_MAILBOX = "accout_mailbox";

    private String accoutType;
    private String mailBox;
    private String areaCode;
    private String phoneNum;

    public String getAccoutType() {
        return accoutType;
    }

    public void setAccoutType(String accoutType) {
        this.accoutType = accoutType;
    }

    public String getMailBox() {
        return mailBox;
    }

    public void setMailBox(String mailBox) {
        this.mailBox = mailBox;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
}
