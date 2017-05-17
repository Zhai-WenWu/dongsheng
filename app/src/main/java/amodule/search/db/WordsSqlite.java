package amodule.search.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import amodule.search.bean.WordBean;

/**
 * Created by dao on 2016/10/7.
 */

public class WordsSqlite extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DB_VALUE = "value";
    private static final String TB_NAME = "wordsdirectory";
    private static final String CREATE_TABLE_SQL = "create table if not exists " + TB_NAME + "(" + DB_VALUE + ")";
    private static AtomicBoolean isOperationSqlite = new AtomicBoolean(false);
    private static final int MAX_RECORDS_NUM = 5;

    private static volatile WordsSqlite mInstance = null;

    public static synchronized WordsSqlite getInstance(Context context){
        if( null == mInstance){
            synchronized (WordsSqlite.class){
                if(null == mInstance){
                    mInstance = new WordsSqlite(context);
                }
            }
        }
        return mInstance;
    }

    private WordsSqlite(Context context) {
        this(context, TB_NAME, null, VERSION);
    }

    private WordsSqlite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public long getRecordNum() {

        //得到操作数据库的实例
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select count(*)from " + TB_NAME, null);
        cursor.moveToFirst();
        Long count = cursor.getLong(0);
        cursor.close();
        return count.longValue();
    }

    /**
     * 插入数据
     *
     * @param list
     * @return
     */
    public boolean insert(List<WordBean> list) {
        if (isOperationSqlite.get())
            return false;
        isOperationSqlite.set(true);
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            String sql = "insert into " + TB_NAME + "("
                    + DB_VALUE + ") " + "values(?)";
            SQLiteStatement stat = db.compileStatement(sql);
            db.beginTransaction();
            for (WordBean bean : list) {
                stat.bindString(1, bean.getValue());
                long result = stat.executeInsert();
//                if (result < 0) {
//                    return false;
//                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (null != db) {
                    db.endTransaction();
                    db.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            isOperationSqlite.set(false);
        }
        return true;
    }


    /**
     * 根据输入词，查询匹配词
     *
     * @param name
     * @return
     */
    public List<WordBean> querryByName(String name) {
        if (isOperationSqlite.get())
            return null;
        isOperationSqlite.set(true);
        Cursor cursor = null;
        ArrayList<WordBean> wordsDatas = new ArrayList<>();
        try {
            WordBean data;
            String selection = DB_VALUE + " like ?" + " limit " + MAX_RECORDS_NUM;
            String[] selectionArgs = {"%" + name + "%"};
            cursor = getReadableDatabase().query(TB_NAME, null, selection, selectionArgs, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    data = new WordBean();
                    data.setValue(cursor.getString(cursor.getColumnIndex(DB_VALUE)));
                    wordsDatas.add(data);
                } while (cursor.moveToNext());
            }

        } finally {
            close(cursor, getReadableDatabase());
            isOperationSqlite.set(false);
            return wordsDatas;
        }
    }


    /**
     * 删除全部数据
     *
     * @param
     * @return
     */
    public void deleteAllRecord() {
        if (isOperationSqlite.get())
            return;
        isOperationSqlite.set(true);
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        database.execSQL("delete from " + TB_NAME);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
        isOperationSqlite.set(false);
    }


    /**
     * 关闭数据库游标
     */
    private void close(Cursor c, SQLiteDatabase db) {
        if (null != c) {
            c.close();
            c = null;
        }
        if (null != db) {
            db.close();
        }
    }
}
