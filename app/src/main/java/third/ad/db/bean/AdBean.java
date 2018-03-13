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
    public int index;
    public String adId;
    public String adConfig;
    public long updateTime;

    public abstract static class AdEntry implements BaseColumns {
        public static final String COLUMN_ADINDEX_INLIST = "indexInList";
        public static final String COLUMN_ADID = "adId";
        public static final String COLUMN_ADCONFIG = "adConfig";
        public static final String COLUMN_UPDATETIME = "updateTime";
    }
}
