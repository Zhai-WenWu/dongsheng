package third.ad.db.bean;

import android.provider.BaseColumns;

/**
 * Description :
 * PackageName : third.ad.db.bean
 * Created by mrtrying on 2018/2/6 13:42:58.
 * e_mail : ztanzeyu@gmail.com
 */
public class AdBean {
    public int _id;
    public String isBaidu;
    public String isJD;
    public String isBanner;
    public String isGdt;
    public String adId;
    public String banner;
    public String adConfig;
    public long updateTime;

    public abstract static class AdEntry implements BaseColumns {
        public static final String COLUMN_ISBAIDU = "isBaidu";
        public static final String COLUMN_ISJD = "isJD";
        public static final String COLUMN_ISBANNER = "isBanner";
        public static final String COLUMN_ISGDT = "isGdt";
        public static final String COLUMN_ADID = "adId";
        public static final String COLUMN_BANNER = "banner";
        public static final String COLUMN_ADCONFIG = "adConfig";
        public static final String COLUMN_UPDATETIME = "updateTime";
    }
}
