package acore.logic.stat;

import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Description :
 * PackageName : acore.logic.stat
 * Created by tanzeyu on 2018/10/16 18:27.
 * e_mail : ztanzeyu@gmail.com
 */
public class StatModel implements Serializable {
    private static final long serialVersionUID = -2758994198769493918L;
    public static final String EMPTY = "";
    public static final String EVENT_STAY = "stay";
    public static final String EVENT_LIST_SHOW = "show";
    public static final String EVENT_BTN_CLICK = "btnC";
    public static final String EVENT_LIST_CLICK = "listC";
    public static final String EVENT_VIDEO_VIEW = "vv";
    public static final String EVENT_SPECIAL_ACTION = "act";

    final public String e;
    final public String p;
    final public String m;
    final public String statJson;
    final public String pos;
    final public String btn;
    final public String s1;
    final public String s2;
    final public String s3;
    final public String n1;
    final public String n2;

    public static StatModel createPageStayModel(String p, String stayTime, String s1) {
        return new StatModel(EVENT_STAY, p, EMPTY, EMPTY, EMPTY, EMPTY, s1, EMPTY, EMPTY, stayTime, EMPTY);
    }

    public static StatModel createPageStayModel(String p, String stayTime) {
        return createPageStayModel(p, stayTime, EMPTY);
    }

    public static StatModel createListShowModel(String p, String m, String pos, String s1, String statJson) {
        return new StatModel(EVENT_LIST_SHOW, p, m, statJson, pos, EMPTY, s1, EMPTY, EMPTY, EMPTY, EMPTY);
    }

    public static StatModel createBtnClickModel(String p, String m, String btn) {
        return createBtnClickModel(p, m, "0", btn, EMPTY);
    }

    public static StatModel createBtnClickModel(String p, String m, String pos, String btn, String s1) {
        return new StatModel(EVENT_BTN_CLICK, p, m, EMPTY, pos, btn, s1, EMPTY, EMPTY, EMPTY, EMPTY);
    }

    public static StatModel createListClickModel(String p, String m, String pos, String s1, String statJson) {
        return new StatModel(EVENT_LIST_CLICK, p, m, statJson, pos, EMPTY, s1, EMPTY, EMPTY, EMPTY, EMPTY);
    }

    public static StatModel createVideoViewModel(String p, String m, String pos, String n1, String n2, String statJson) {
        return new StatModel(EVENT_VIDEO_VIEW, p, m, statJson, pos, EMPTY, EMPTY, EMPTY, EMPTY, n1, n2);
    }

    public static StatModel createSpecialActionModel(String p, String m, String pos, String btn, String s1, String n1, String statJson) {
        return new StatModel(EVENT_SPECIAL_ACTION, p, m, statJson, pos, btn, s1, EMPTY, EMPTY, n1, EMPTY);
    }

    public static StatModel createADShowModel(String p,String m,String statJson,String s2,String s3,String n1,String n2){
        return new StatModel(EVENT_LIST_SHOW, p, m, statJson, EMPTY, EMPTY, EMPTY, s2, s3, n1, n2);
    }

    public static StatModel createADClickModel(String p,String m,String statJson,String s2,String s3,String n1,String n2){
        return new StatModel(EVENT_LIST_CLICK, p, m, statJson, EMPTY, EMPTY, EMPTY, s2, s3, n1, n2);
    }

    public StatModel(String e, String p, String m, String statJson, String pos, String btn,
                     String s1, String s2, String s3, String n1, String n2) {
        this.e = e;
        this.p = p;
        this.m = m;
        this.statJson = statJson;
        this.pos = pos;
        this.btn = btn;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
        this.n1 = n1;
        this.n2 = n2;
    }
}
