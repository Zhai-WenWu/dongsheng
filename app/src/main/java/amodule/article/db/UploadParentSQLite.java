package amodule.article.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import amodule.dish.db.UploadDishData;


/**
 * Created by Fang Ruijiao on 2017/5/22.
 */
public class UploadParentSQLite extends SQLiteOpenHelper {

    public UploadParentSQLite(Context context,String tabName,int tabVersion) {
        super(context, tabName, null, tabVersion);
        TB_NAME = tabName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getCreateTableSql());
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public int insert(UploadArticleData upData){
        return insertData(upData);
    }

    public UploadArticleData getDraftData(){
        Log.i("articleUpload","获取草稿数据()");
        Cursor cur = null;
        SQLiteDatabase readableDatabase = null;
        try {
            readableDatabase = getReadableDatabase();
            UploadArticleData upData = new UploadArticleData();
            cur = readableDatabase.query(TB_NAME, null,"", null, null, null, UploadArticleData.article_id + " desc");// 查询并获得游标
            Log.i("articleUpload","获取草稿数据() size:" + cur.getCount());
            if (cur.moveToFirst()) {// 判断游标是否为空
                do {
                    String uploadType = cur.getString(cur.getColumnIndex(UploadArticleData.article_uploadType));
                    if(UploadDishData.UPLOAD_DRAF.equals(uploadType)) {
                        Log.i("articleUpload","获取草稿数据() 是草稿");
                        upData.setId(cur.getInt(cur.getColumnIndex(UploadArticleData.article_id)));
                        upData.setTitle(cur.getString(cur.getColumnIndex(UploadArticleData.article_title)));
                        upData.setClassCode(cur.getString(cur.getColumnIndex(UploadArticleData.article_classCode)));
                        upData.setContent(cur.getString(cur.getColumnIndex(UploadArticleData.article_content)));
                        upData.setIsOriginal(cur.getInt(cur.getColumnIndex(UploadArticleData.article_isOriginal)));
                        upData.setRepAddress(cur.getString(cur.getColumnIndex(UploadArticleData.article_repAddress)));
                        upData.setImg(cur.getString(cur.getColumnIndex(UploadArticleData.article_img)));
                        upData.setImgs(cur.getString(cur.getColumnIndex(UploadArticleData.article_imgs)));
                        upData.setCode(cur.getString(cur.getColumnIndex(UploadArticleData.article_code)));
                        upData.setImgUrl(cur.getString(cur.getColumnIndex(UploadArticleData.article_imgUrl)));
                        upData.setVideoUrl(cur.getString(cur.getColumnIndex(UploadArticleData.article_videoUrl)));
                        upData.setVideoImgUrl(cur.getString(cur.getColumnIndex(UploadArticleData.article_videoImgUrl)));
                        upData.setUploadType(uploadType);
                        upData.setVideo(cur.getString(cur.getColumnIndex(UploadArticleData.article_video)));
                        upData.setVideoImg(cur.getString(cur.getColumnIndex(UploadArticleData.article_videoImg)));
                        break;
                    }
                }while (cur.moveToNext());
            }
            return upData;
        } finally {
            close(cur, readableDatabase);
        }
    }

    public UploadArticleData getUploadIngData(){
        Cursor cur = null;
        SQLiteDatabase readableDatabase = null;
        try {
            readableDatabase = getReadableDatabase();
            UploadArticleData upData = null;
            cur = readableDatabase.query(TB_NAME, null,"", null, null, null, UploadArticleData.article_id + " desc");// 查询并获得游标
            Log.i("articleUpload","获取上传中数据() size:" + cur.getCount());
            if (cur.moveToFirst()) {// 判断游标是否为空
                do {
                    String uploadType = cur.getString(cur.getColumnIndex(UploadArticleData.article_uploadType));
                    Log.i("articleUpload","获取上传中数据() uploadType:" + uploadType);
                    if(!UploadDishData.UPLOAD_DRAF.equals(uploadType)) {
                        Log.i("articleUpload","获取上传中数据() 不是草稿");
                        upData = new UploadArticleData();
                        upData.setId(cur.getInt(cur.getColumnIndex(UploadArticleData.article_id)));
                        upData.setTitle(cur.getString(cur.getColumnIndex(UploadArticleData.article_title)));
                        upData.setClassCode(cur.getString(cur.getColumnIndex(UploadArticleData.article_classCode)));
                        upData.setContent(cur.getString(cur.getColumnIndex(UploadArticleData.article_content)));
                        upData.setIsOriginal(cur.getInt(cur.getColumnIndex(UploadArticleData.article_isOriginal)));
                        upData.setRepAddress(cur.getString(cur.getColumnIndex(UploadArticleData.article_repAddress)));
                        upData.setImg(cur.getString(cur.getColumnIndex(UploadArticleData.article_img)));
                        upData.setImgs(cur.getString(cur.getColumnIndex(UploadArticleData.article_imgs)));
                        upData.setCode(cur.getString(cur.getColumnIndex(UploadArticleData.article_code)));
                        upData.setImgUrl(cur.getString(cur.getColumnIndex(UploadArticleData.article_imgUrl)));
                        upData.setVideoUrl(cur.getString(cur.getColumnIndex(UploadArticleData.article_videoUrl)));
                        upData.setVideoImgUrl(cur.getString(cur.getColumnIndex(UploadArticleData.article_videoImgUrl)));
                        upData.setUploadType(uploadType);
                        upData.setVideo(cur.getString(cur.getColumnIndex(UploadArticleData.article_video)));
                        upData.setVideoImg(cur.getString(cur.getColumnIndex(UploadArticleData.article_videoImg)));
                        break;
                    }
                }while (cur.moveToNext());
            }
            return upData;
        } finally {
            close(cur, readableDatabase);
        }
    }

    public ArrayList<UploadArticleData> getAllUploadIngData(){
        Cursor cur = null;
        SQLiteDatabase readableDatabase = null;
        try {
            readableDatabase = getReadableDatabase();
            ArrayList<UploadArticleData> articleDatas = new ArrayList<>();
            UploadArticleData upData;
            cur = readableDatabase.query(TB_NAME, null,"", null, null, null, UploadArticleData.article_id + " desc");// 查询并获得游标
            Log.i("articleUpload","获取上传中数据() size:" + cur.getCount());
            if (cur.moveToFirst()) {// 判断游标是否为空
                do {
                    String uploadType = cur.getString(cur.getColumnIndex(UploadArticleData.article_uploadType));
                    Log.i("articleUpload","获取上传中数据() uploadType:" + uploadType);
                    if(!UploadDishData.UPLOAD_DRAF.equals(uploadType)) {
                        Log.i("articleUpload","获取上传中数据() 不是草稿");
                        upData = new UploadArticleData();
                        upData.setId(cur.getInt(cur.getColumnIndex(UploadArticleData.article_id)));
                        upData.setTitle(cur.getString(cur.getColumnIndex(UploadArticleData.article_title)));
                        upData.setClassCode(cur.getString(cur.getColumnIndex(UploadArticleData.article_classCode)));
                        upData.setContent(cur.getString(cur.getColumnIndex(UploadArticleData.article_content)));
                        upData.setIsOriginal(cur.getInt(cur.getColumnIndex(UploadArticleData.article_isOriginal)));
                        upData.setRepAddress(cur.getString(cur.getColumnIndex(UploadArticleData.article_repAddress)));
                        upData.setImg(cur.getString(cur.getColumnIndex(UploadArticleData.article_img)));
                        upData.setImgs(cur.getString(cur.getColumnIndex(UploadArticleData.article_imgs)));
                        upData.setCode(cur.getString(cur.getColumnIndex(UploadArticleData.article_code)));
                        upData.setImgUrl(cur.getString(cur.getColumnIndex(UploadArticleData.article_imgUrl)));
                        upData.setVideoUrl(cur.getString(cur.getColumnIndex(UploadArticleData.article_videoUrl)));
                        upData.setVideoImgUrl(cur.getString(cur.getColumnIndex(UploadArticleData.article_videoImgUrl)));
                        upData.setUploadType(uploadType);
                        upData.setVideo(cur.getString(cur.getColumnIndex(UploadArticleData.article_video)));
                        upData.setVideoImg(cur.getString(cur.getColumnIndex(UploadArticleData.article_videoImg)));
                        articleDatas.add(upData);
                    }
                }while (cur.moveToNext());
            }
            return articleDatas;
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
        cv.put(UploadArticleData.article_imgs, upData.getImgs());
        cv.put(UploadArticleData.article_code, upData.getCode());
        cv.put(UploadArticleData.article_imgUrl, upData.getImgUrl());
        cv.put(UploadArticleData.article_videoUrl, upData.getVideoUrl());
        cv.put(UploadArticleData.article_videoImgUrl, upData.getVideoImgUrl());
        cv.put(UploadArticleData.article_uploadType, upData.getUploadType());
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
        cv.put(UploadArticleData.article_imgs, upData.getImgs());
        cv.put(UploadArticleData.article_code, upData.getCode());
        cv.put(UploadArticleData.article_imgUrl, upData.getImgUrl());
        cv.put(UploadArticleData.article_videoUrl, upData.getVideoUrl());
        cv.put(UploadArticleData.article_videoImgUrl, upData.getVideoImgUrl());
        cv.put(UploadArticleData.article_uploadType, upData.getUploadType());
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
//        Tools.showToast(XHApplication.in().getApplicationContext(),"更新数据");
        return row;
    }

    public synchronized UploadArticleData selectById(int id) {
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
                upData.setImgs(cur.getString(cur.getColumnIndex(UploadArticleData.article_imgs)));
                upData.setCode(cur.getString(cur.getColumnIndex(UploadArticleData.article_code)));
                upData.setImgUrl(cur.getString(cur.getColumnIndex(UploadArticleData.article_imgUrl)));
                upData.setVideoUrl(cur.getString(cur.getColumnIndex(UploadArticleData.article_videoUrl)));
                upData.setVideoImgUrl(cur.getString(cur.getColumnIndex(UploadArticleData.article_videoImgUrl)));
                upData.setUploadType(cur.getString(cur.getColumnIndex(UploadArticleData.article_uploadType)));
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

    public boolean checkHasMidea(int id){
        Cursor cur = null;
        SQLiteDatabase readableDatabase = null;
        try {
            readableDatabase = getReadableDatabase();
            String content = null;
            cur = readableDatabase.query(TB_NAME, null,
                    UploadArticleData.article_id + "=?", new String[]{String.valueOf(id)}, null, null, null);// 查询并获得游标
            if (cur.moveToFirst()) {// 判断游标是否为空
                content = cur.getString(cur.getColumnIndex(UploadArticleData.article_content));
            }
            return !TextUtils.isEmpty(content) && (content.contains("image") || content.contains("video"));
        } finally {
            close(cur, readableDatabase);
        }
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

    private String TB_NAME;
    private String getCreateTableSql(){
        return "create table " + TB_NAME + "("
                + UploadArticleData.article_id + " integer primary key autoincrement,"
                + UploadArticleData.article_title + " text,"
                + UploadArticleData.article_classCode + " text,"
                + UploadArticleData.article_content + "  text,"
                + UploadArticleData.article_isOriginal + " integer,"
                + UploadArticleData.article_repAddress + " text,"
                + UploadArticleData.article_img + " text,"
                + UploadArticleData.article_imgs + " text,"
                + UploadArticleData.article_code + " text,"
                + UploadArticleData.article_imgUrl + " text,"
                + UploadArticleData.article_videoUrl + " text,"
                + UploadArticleData.article_videoImgUrl + " text,"
                + UploadArticleData.article_uploadType + " text,"
                + UploadArticleData.article_video + " text,"
                + UploadArticleData.article_videoImg + " text)";
    }

}
