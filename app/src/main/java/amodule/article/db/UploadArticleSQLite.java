package amodule.article.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Fang Ruijiao on 2017/5/22.
 */

public class UploadArticleSQLite extends SQLiteOpenHelper {
    
    public UploadArticleSQLite(Context context) {
        super(context, TB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public int insert(UploadArticleData upData){
        return insertData(upData);
    }

    public UploadArticleData getDraftData(){
        Cursor cur = null;
        SQLiteDatabase readableDatabase = null;
        try {
            readableDatabase = getReadableDatabase();
            UploadArticleData upData = new UploadArticleData();
            cur = readableDatabase.query(TB_NAME, null,"", null, null, null, null);// 查询并获得游标

            int count = cur.getCount();
            if (cur.moveToLast()) {// 判断游标是否为空
                upData.setId(cur.getInt(cur.getColumnIndex(UploadArticleData.article_id)));
                upData.setTitle(cur.getString(cur.getColumnIndex(UploadArticleData.article_title)));
                upData.setClassCode(cur.getString(cur.getColumnIndex(UploadArticleData.article_classCode)));
                upData.setContent(cur.getString(cur.getColumnIndex(UploadArticleData.article_content)));
                upData.setIsOriginal(cur.getInt(cur.getColumnIndex(UploadArticleData.article_isOriginal)));
                upData.setRepAddress(cur.getString(cur.getColumnIndex(UploadArticleData.article_repAddress)));
                upData.setImg(cur.getString(cur.getColumnIndex(UploadArticleData.article_img)));
                upData.setVideo(cur.getString(cur.getColumnIndex(UploadArticleData.article_video)));
                upData.setVideoImg(cur.getString(cur.getColumnIndex(UploadArticleData.article_videoImg)));
            }
            return upData;
        } finally {
            close(cur, readableDatabase);
        }
    }


    /**
     * 插入一条数据;
     */
    private int insertData(UploadArticleData upData) {
        ContentValues cv = new ContentValues();
        cv.put(UploadArticleData.article_title, upData.getTitle());
        cv.put(UploadArticleData.article_classCode, upData.getClassCode());
        cv.put(UploadArticleData.article_content, upData.getContent());
        cv.put(UploadArticleData.article_isOriginal, upData.getIsOriginal());
        cv.put(UploadArticleData.article_repAddress, upData.getRepAddress());
        cv.put(UploadArticleData.article_img, upData.getImg());
        cv.put(UploadArticleData.article_video, upData.getVideo());
        cv.put(UploadArticleData.article_videoImg, upData.getVideoImg());

        long id = -1;
        try {
            id = this.getWritableDatabase().insert(TB_NAME, null, cv);
            this.getWritableDatabase().close();
        } catch (Exception e) {
        }
        return (int) id;
    }

    /**
     * 修改一条数据;
     */
    public synchronized int update(int id, UploadArticleData upData) {
        int row = -1;
        if(upData == null) return row;
        SQLiteDatabase writableDatabase = null;
        ContentValues cv = new ContentValues();
        cv.put(UploadArticleData.article_title, upData.getTitle());
        cv.put(UploadArticleData.article_classCode, upData.getClassCode());
        cv.put(UploadArticleData.article_content, upData.getContent());
        cv.put(UploadArticleData.article_isOriginal, upData.getIsOriginal());
        cv.put(UploadArticleData.article_repAddress, upData.getRepAddress());
        cv.put(UploadArticleData.article_img, upData.getImg());
        cv.put(UploadArticleData.article_video, upData.getVideo());
        cv.put(UploadArticleData.article_videoImg, upData.getVideoImg());
        try {
            writableDatabase = getWritableDatabase();
            row = writableDatabase.update(TB_NAME, cv, UploadArticleData.article_id + "=?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            close(null, writableDatabase);
        }
        return row;
    }
    
    private synchronized UploadArticleData selectById(int id) {
        Cursor cur = null;
        SQLiteDatabase readableDatabase = null;
        try {
            readableDatabase = getReadableDatabase();
            UploadArticleData upData = new UploadArticleData();
            cur = readableDatabase.query(TB_NAME, null,
                    UploadArticleData.article_id + "=?", new String[]{String.valueOf(id)}, null, null, null);// 查询并获得游标
            if (cur.moveToFirst()) {// 判断游标是否为空
                upData.setId(cur.getInt(cur.getColumnIndex(UploadArticleData.article_id)));
                upData.setTitle(cur.getString(cur.getColumnIndex(UploadArticleData.article_title)));
                upData.setClassCode(cur.getString(cur.getColumnIndex(UploadArticleData.article_classCode)));
                upData.setContent(cur.getString(cur.getColumnIndex(UploadArticleData.article_content)));
                upData.setIsOriginal(cur.getInt(cur.getColumnIndex(UploadArticleData.article_isOriginal)));
                upData.setRepAddress(cur.getString(cur.getColumnIndex(UploadArticleData.article_repAddress)));
                upData.setImg(cur.getString(cur.getColumnIndex(UploadArticleData.article_img)));
                upData.setVideo(cur.getString(cur.getColumnIndex(UploadArticleData.article_video)));
                upData.setVideoImg(cur.getString(cur.getColumnIndex(UploadArticleData.article_videoImg)));
            }
            return upData;
        } finally {
            close(cur, readableDatabase);
        }
    }

    public boolean deleteById(int id) {
        SQLiteDatabase readableDatabase = null;
        int i = -1;
        try {
            readableDatabase = getWritableDatabase();
            i = readableDatabase.delete(TB_NAME, UploadArticleData.article_id + "=" + Integer.valueOf(id) + "",null);
            this.getWritableDatabase().close();
        } catch (Exception e) {
        } finally {
            close(null, readableDatabase);
        }
        return i > 0;
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
//			db=null;
        }
    }

    private static final String TB_NAME = "tb_uploadAriticle";
    private static final String CREATE_TABLE_SQL = "create table " + TB_NAME + "("
            + UploadArticleData.article_id + " integer primary key autoincrement,"
            + UploadArticleData.article_title + " text,"
            + UploadArticleData.article_classCode + " text,"
            + UploadArticleData.article_content + "  text,"
            + UploadArticleData.article_isOriginal + " integer,"
            + UploadArticleData.article_repAddress + " text,"
            + UploadArticleData.article_img + " text,"
            + UploadArticleData.article_video + " text,"
            + UploadArticleData.article_videoImg + " text)";
}
