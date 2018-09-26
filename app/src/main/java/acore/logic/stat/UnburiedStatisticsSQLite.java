package acore.logic.stat;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import acore.override.XHApplication;


/**
 * Description :
 * PackageName : acore.logic.statistics
 * Created by mrtrying on 2018/8/1 16:25.
 * e_mail : ztanzeyu@gmail.com
 */
public class UnburiedStatisticsSQLite extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DB_NAME = "unburiedStatistics.db";// 数据库名称
    private static final String TB_NAME = "commonData";// 数据库名称
    private static final String statistics_data = "statistics_data";
    private static final String data_type = "data_type";
    public static final String Normal = "normal";
    public static final String GXHTJ = "#GXHTJ#";

    private static volatile UnburiedStatisticsSQLite instance = null;

    private UnburiedStatisticsSQLite() {
        super(XHApplication.in(), DB_NAME, null, VERSION);
    }

    public static UnburiedStatisticsSQLite instance() {
        if (instance == null) {
            synchronized (UnburiedStatisticsSQLite.class) {
                if (instance == null) {
                    instance = new UnburiedStatisticsSQLite();
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TB_NAME +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + statistics_data + " VARCHAR," +
                data_type + " VARCHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            default:
                break;
        }
    }

    /**
     * home插入数据
     *
     * @return
     */
    public synchronized long insterData(String data,String type) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(statistics_data, data);
            values.put(data_type, type);
            return db.insert(TB_NAME, null, values);
        } finally {
            closeDB(db);
        }
    }

    /**
     * 查询home全部数据
     *
     * @return
     */
    public ArrayList<String> selectAllData() {
        ArrayList<String> arrayList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.rawQuery("select * from " + TB_NAME, null);
            while (cursor.moveToNext()) {
                arrayList.add(cursor.getString(cursor.getColumnIndex(statistics_data)));
            }

        } finally {
            close(db, cursor);
        }
        return arrayList;
    }

    public ArrayList<String> selectAllDataByType(String type) {
        ArrayList<String> arrayList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.rawQuery("select * from " + TB_NAME + " where data_type=?", new String[]{type});
            while (cursor.moveToNext()) {
                arrayList.add(cursor.getString(cursor.getColumnIndex(statistics_data)));
            }

        } finally {
            close(db, cursor);
        }
        return arrayList;
    }

    public long getDataCount() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            String sql = "select count(*) from " + TB_NAME;
            cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            return cursor.getLong(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(db, cursor);
        }
        return 0;
    }

    /** 删除表home中的数据 */
    public void deleteAllData() {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.delete(TB_NAME, null, null);
        } finally {
            closeDB(db);
        }
    }

    private void close(SQLiteDatabase db, Cursor cursor) {
        closeDB(db);
        closeCursor(cursor);
    }

    private void closeDB(SQLiteDatabase db) {
        if (db != null) {
            db.close();
        }
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }


}
