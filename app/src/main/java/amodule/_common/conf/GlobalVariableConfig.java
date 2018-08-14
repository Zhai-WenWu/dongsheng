package amodule._common.conf;

public class GlobalVariableConfig {
    public static boolean shortVideoDetail_netStateTip_dialogEnable = true;//是否可以提示网络状态

    public static void restoreConf() {
        shortVideoDetail_netStateTip_dialogEnable = true;
    }
}
