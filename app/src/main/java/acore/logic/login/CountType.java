package acore.logic.login;

/**
 * Created by ：fei_teng on 2017/2/15 16:33.
 */

public enum CountType {

    TYPE_SMS("type_sms"), TYPE_SECRET("type_secret");

    private String value;
    CountType(String str) {
        value = str;
    }

    @Override
    public String toString() {
        return value;
    }
}
