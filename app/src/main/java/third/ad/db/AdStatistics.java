package third.ad.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import acore.override.XHApplication;

/**
 * Description :
 * PackageName : third.ad.db
 * Created by mrtrying on 2018/4/26 11:28.
 * e_mail : ztanzeyu@gmail.com
 */
public class AdStatistics extends SQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String DB_NAME = "AdStatistics.db";
    public static final String tableName = "data";
    public static final String CULOMN_JSON = "json";

    private static volatile AdStatistics instance = null;

    public static AdStatistics getInstance() {
        if (instance == null) {
            synchronized (AdStatistics.class) {
                if (instance == null) {
                    instance = new AdStatistics(XHApplication.in());
                }
            }
        }
        return instance;
    }

    private AdStatistics(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + tableName +
                " ( _id  integer primary key autoincrement," +
                CULOMN_JSON + " text" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(String jsonStr) {
        synchronized (AdStatistics.class) {
            if (TextUtils.isEmpty(jsonStr)) {
                return;
            }
            SQLiteDatabase database = null;
            try {
                database = getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(CULOMN_JSON, jsonStr);
                database.insertOrThrow(tableName, null, contentValues);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeDB(database);
            }
        }
    }

    public int getSize(){
        synchronized (AdStatistics.class) {
            int size = 0;
            SQLiteDatabase database = null;
            Cursor cursor = null;
            try {
                database = getWritableDatabase();
                cursor = database.rawQuery("select * from " + tableName,null);
                size = cursor.getCount();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeCursor(cursor);
                closeDB(database);
            }
            return size;
        }
    }

    public List<JSONObject> getAllData(){
        synchronized (AdStatistics.class) {
            List<JSONObject> result = new ArrayList<>();
            SQLiteDatabase database = null;
            Cursor cursor = null;
            try {
                database = getWritableDatabase();
                cursor = database.rawQuery("select * from " + tableName,null);
                if(cursor.moveToFirst()){
                    do{
                        String jsonValue = cursor.getString(cursor.getColumnIndexOrThrow(CULOMN_JSON));
                        if(!TextUtils.isEmpty(jsonValue)){
                            result.add(new JSONObject(jsonValue));
                        }
                    }while(cursor.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeCursor(cursor);
                closeDB(database);
            }
            return result;
        }
    }



    /**
     * 删除所有数据
     */
    public void deleteAll() {
        synchronized (AdStatistics.class) {
            SQLiteDatabase database = null;
            try {
                database = getWritableDatabase();
                database.execSQL("delete from " + tableName);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            } finally {
                closeDB(database);
            }
        }
    }

    /**
     * 关闭cursor
     *
     * @param cursor
     */
    private void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * 关闭数据库
     *
     * @param db
     */
    private void closeDB(SQLiteDatabase db) {
        if (db != null) {
            db.close();
        }
    }
}
