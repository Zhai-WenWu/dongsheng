package third.ad.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Map;

import acore.tools.StringManager;
import third.ad.db.bean.AdBean;

import static third.ad.db.bean.AdBean.AdEntry;
import static third.ad.tools.AdPlayIdConfig.FULL_SRCEEN_ACTIVITY;

/**
 * Description :
 * PackageName : third.ad.db
 * Created by mrtrying on 2018/2/6 13:36:20.
 * e_mail : ztanzeyu@gmail.com
 */
public class XHAdSqlite extends SQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String NAME = "ad.db";

    public static final String TABLE_ADCONFIG = "tb_ad_config";

    private volatile static XHAdSqlite mInstance = null;

    public static XHAdSqlite newInstance(Context context){
        if(null == mInstance){
            synchronized (XHAdSqlite.class){
                if(null == mInstance){
                    mInstance = new XHAdSqlite(context);
                }
            }
        }
        return mInstance;
    }

    private XHAdSqlite(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAdConfigTable(db);
    }

    private void createAdConfigTable(SQLiteDatabase db) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("create table if not exists ").append(TABLE_ADCONFIG).append(" (").append("_id").append(" integer primary key autoincrement,")
                .append(AdEntry.COLUMN_ISBAIDU).append(" text,")
                .append(AdEntry.COLUMN_ISBANNER).append(" text,")
                .append(AdEntry.COLUMN_ISGDT).append(" text,")
                .append(AdEntry.COLUMN_ISJD).append(" text,")
                .append(AdEntry.COLUMN_ADID).append(" text,")
                .append(AdEntry.COLUMN_BANNER).append(" text,")
                .append(AdEntry.COLUMN_ADCONFIG).append(" text,")
                .append(AdEntry.COLUMN_UPDATETIME).append(" long")
                .append(")");
        db.execSQL(buffer.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void updateConfig(String jsonValue){
        SQLiteDatabase database = null;
        try{
            database = getWritableDatabase();
            ContentValues values = null;
            Map<String,String> map = StringManager.getFirstMap(jsonValue);
            for(Map.Entry<String,String> entry:map.entrySet()){
                if(!FULL_SRCEEN_ACTIVITY.equals(entry.getKey())){
                    values = new ContentValues();
                    values.put(AdEntry.COLUMN_ADID,entry.getKey());
                    Map<String,String> configData = StringManager.getFirstMap(entry.getValue());
                    values.put(AdEntry.COLUMN_ISBAIDU,configData.get(AdEntry.COLUMN_ISBAIDU));
                    values.put(AdEntry.COLUMN_ISBANNER,configData.get(AdEntry.COLUMN_ISBANNER));
                    values.put(AdEntry.COLUMN_ISGDT,configData.get(AdEntry.COLUMN_ISGDT));
                    values.put(AdEntry.COLUMN_ISJD,configData.get(AdEntry.COLUMN_ISJD));
                    values.put(AdEntry.COLUMN_BANNER,configData.get(AdEntry.COLUMN_BANNER));
                    values.put(AdEntry.COLUMN_ADCONFIG,configData.get(AdEntry.COLUMN_ADCONFIG));
                    values.put(AdEntry.COLUMN_UPDATETIME,System.currentTimeMillis());
                    update(database, TABLE_ADCONFIG,values);
                }else{

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase(database);
        }
    }

    private void update(SQLiteDatabase database, String tableName,ContentValues values) throws Exception{
        if(database.isOpen() && !database.isReadOnly()
                && !TextUtils.isEmpty(tableName)
                && values != null && values.size() > 0){
            if(TextUtils.isEmpty(values.getAsString(AdEntry.COLUMN_ADID))){
                return;
            }
            final String whereClause = AdEntry.COLUMN_ADID;
            final String[] whereArgs = new String[]{values.getAsString(AdEntry.COLUMN_ADID)};
            Cursor cursor = database.rawQuery("select * from " + tableName + " where " + whereClause + "=?",whereArgs);
            if (cursor.moveToFirst()){
                database.update(tableName,values,whereClause,whereArgs);
            }else{
                database.insert(tableName,null,values);
            }
            closeCursor(cursor);
        }
    }

    public AdBean getAdConfig(String adid){
        return getAdByADId(TABLE_ADCONFIG,adid);
    }

    @Nullable
    private AdBean getAdByADId(String tableName,String adid){
        if(TextUtils.isEmpty(tableName) || TextUtils.isEmpty(adid)){
            return null;
        }
        SQLiteDatabase database = null;
        AdBean adBean = null;
        Cursor cursor = null;
        try{
            database = getReadableDatabase();
            cursor = database.rawQuery("select * from " + tableName + " where " + AdEntry.COLUMN_ADID + "=?",new String[]{adid});
            if(cursor.moveToFirst()){
                adBean = cursorToBean(cursor);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeCursor(cursor);
            closeDatabase(database);
        }
        return adBean;
    }

    private AdBean cursorToBean(Cursor cursor) {
        AdBean adBean = new AdBean();
        adBean._id = cursor.getInt(cursor.getColumnIndexOrThrow(AdEntry._ID));
        adBean.isBaidu = cursor.getString(cursor.getColumnIndexOrThrow(AdEntry.COLUMN_ISBAIDU));
        adBean.isBanner = cursor.getString(cursor.getColumnIndexOrThrow(AdEntry.COLUMN_ISBANNER));
        adBean.isGdt = cursor.getString(cursor.getColumnIndexOrThrow(AdEntry.COLUMN_ISGDT));
        adBean.isJD = cursor.getString(cursor.getColumnIndexOrThrow(AdEntry.COLUMN_ISJD));
        adBean.adId = cursor.getString(cursor.getColumnIndexOrThrow(AdEntry.COLUMN_ADID));
        adBean.adConfig = cursor.getString(cursor.getColumnIndexOrThrow(AdEntry.COLUMN_ADCONFIG));
        adBean.banner = cursor.getString(cursor.getColumnIndexOrThrow(AdEntry.COLUMN_BANNER));
        return adBean;
    }

    public void deleteOverdueConfig(){
        SQLiteDatabase database = null;
        try{
            database = getWritableDatabase();
            final long OverdueTime = System.currentTimeMillis() - (24*60*60*1000L);
            database.execSQL("delete from tb_ad_config where updateTime<="+OverdueTime+";");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeDatabase(database);
        }
    }

    private void closeDatabase(SQLiteDatabase database) {
        try{
            if(database != null){
                database.close();
            }
        }catch (Exception ignored){

        }
    }

    private void closeCursor(Cursor cursor) {
        try{
            if(cursor != null){
                cursor.close();
            }
        }catch (Exception ignored){

        }
    }

}