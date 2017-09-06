package acore.logic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import acore.override.XHApplication;
import amodule.quan.db.SubjectSqlite;
import third.mall.aplug.ShoppingSQLiteDataBase;

/**
 * Created by xiangha-zhangyujian on 2017/7/7.
 */

public class StatictisSQLiteDataBase extends SQLiteOpenHelper{
    public static final int VERSION = 2;
    public static final String DB_NAME = "statictis.db";// 数据库名称
    public static final String TABLE = "home";// 数据库表单名称
    public static final String TABLE_PAGE = "page";// 数据库表单名称
    public static String statictis_data ="statictis_data";//统计数据
    public static String statictis_type ="statictis_type";//统计类型
    public static String home_type ="recom";//类型--目前只有一个
    public static String page_type = "time";//page类型数据。

    private static StatictisSQLiteDataBase sqlite=null;
    private StatictisSQLiteDataBase(){
        super(XHApplication.in().getApplicationContext(), DB_NAME, null, VERSION);
    }
    /**
     * 单例模式
     * @return
     */
    public synchronized static StatictisSQLiteDataBase getInstance(){
        if(sqlite==null){
            sqlite=new StatictisSQLiteDataBase();
        }
        return sqlite;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //  创建数据库后，对数据库的操作
        String sql = "create table if not exists "+TABLE +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                statictis_type + " VARCHAR , " +
                statictis_data + " VARCHAR)";
        String sql_page = "create table if not exists "+TABLE_PAGE +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                statictis_type + " VARCHAR , " +
                statictis_data + " VARCHAR)";
        db.execSQL(sql);
        db.execSQL(sql_page);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion){
            case 1:
                String sql_page = "create table if not exists "+TABLE_PAGE +
                        "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        statictis_type + " VARCHAR , " +
                        statictis_data + " VARCHAR)";
                db.execSQL(sql_page);
                break;
        }
    }
    /**
     * home插入数据
     * @return
     */
    public synchronized long insterData(String  data){
        SQLiteDatabase db=null;
        try {
            ContentValues values= new ContentValues();
            values.put(statictis_type, home_type);
            values.put(statictis_data, data);
            db=getWritableDatabase();
            return db.insert(TABLE, null, values);
        } finally{
            close(db);
        }
    }

    /**
     * 查询数据库中home的总条数.
     * @return
     */
    public long getDataNum(){
        SQLiteDatabase db=null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            String sql = "select count(*) from "+TABLE;
            cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            long count = cursor.getLong(0);
            cursor.close();
            return count;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            close(cursor,db);
        }
        return 0;
    }

    /**
     * 查询home全部数据
     * @return
     */
    public ArrayList<String> selectAllData(){
        ArrayList<String> arrayList = new ArrayList<>();
        SQLiteDatabase db=null;
        Cursor cursor = null;
        try{
            db= getReadableDatabase();
            cursor = db.rawQuery("select * from "+TABLE, null);
            while (cursor.moveToNext()){
                arrayList.add(cursor.getString(cursor.getColumnIndex(statictis_data)));
            }

        }finally {
            close(cursor,db);
        }
        return arrayList;
    }

    /**
     * 删除表home中的数据
     */
    public void deleteAllData(){
        SQLiteDatabase db=null;
        try{
            db= getWritableDatabase();
            db.delete(TABLE,null,null);
        }finally {
            close(db);
        }
    }
    /**
     * page插入数据
     * @return
     */
    public synchronized long insterPageData(String  data){
        SQLiteDatabase db=null;
        try {
            ContentValues values= new ContentValues();
            values.put(statictis_type, page_type);
            values.put(statictis_data, data);
            db=getWritableDatabase();
            return db.insert(TABLE_PAGE, null, values);
        } finally{
            close(db);
        }
    }

    /**
     * 查询数据库中page的总条数.
     * @return
     */
    public long getPageDataNum(){
        SQLiteDatabase db=null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            String sql = "select count(*) from "+TABLE_PAGE;
            cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            long count = cursor.getLong(0);
            cursor.close();
            return count;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            close(cursor,db);
        }
        return 0;
    }

    /**
     * 查询page全部数据
     * @return
     */
    public ArrayList<String> selectPageAllData(){
        ArrayList<String> arrayList = new ArrayList<>();
        SQLiteDatabase db=null;
        Cursor cursor = null;
        try{
            db= getReadableDatabase();
            cursor = db.rawQuery("select * from "+TABLE_PAGE, null);
            while (cursor.moveToNext()){
                arrayList.add(cursor.getString(cursor.getColumnIndex(statictis_data)));
            }

        }finally {
            close(cursor,db);
        }
        return arrayList;
    }

    /**
     * 删除表page中的数据
     */
    public void deletePageAllData(){
        SQLiteDatabase db=null;
        try{
            db= getWritableDatabase();
            db.delete(TABLE_PAGE,null,null);
        }finally {
            close(db);
        }
    }
    private void close(SQLiteDatabase db) {
        close(null, db);
    }

    private void close(Cursor c, SQLiteDatabase db) {
        if (c != null) {
            c.close();
            c = null;
        }
        if (db != null) {
            db.close();
            db = null;
        }
    }
}
