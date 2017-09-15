package aplug.recordervideo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import amodule.main.Main;

/**
 * Created by XiangHa on 2016/11/15.
 */
public class RecorderVideoSqlite extends SQLiteOpenHelper {

    private volatile static RecorderVideoSqlite recorderVideoSqlite;

    private RecorderVideoSqlite(Context context){
        super(context, TB_NAME, null, 1);
    }


    public static RecorderVideoSqlite getInstans(){
        if(recorderVideoSqlite == null){
            synchronized (RecorderVideoSqlite.class) {
                if(recorderVideoSqlite == null){
                    recorderVideoSqlite = new RecorderVideoSqlite(Main.allMain);
                }
            }
        }
        return recorderVideoSqlite;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void resetAll(ArrayList<Map<String, String>> arrayList){
        if(arrayList == null){
            return;
        }
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            writableDatabase.execSQL("DELETE FROM " + TB_NAME);
            ContentValues cv;
            Map<String, String> map;
            for(int i = 0; i < arrayList.size(); i++ ){
                map = arrayList.get(i);
                cv = new ContentValues();
                cv.put(RecorderVideoData.video_add_time,map.get(RecorderVideoData.video_add_time));
                cv.put(RecorderVideoData.video_long_time, map.get(RecorderVideoData.video_long_time));
                cv.put(RecorderVideoData.video_show_time, map.get(RecorderVideoData.video_show_time));
                cv.put(RecorderVideoData.video_path, map.get(RecorderVideoData.video_path));
                cv.put(RecorderVideoData.video_img_path, map.get(RecorderVideoData.video_img_path));
                writableDatabase.insert(TB_NAME, null, cv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writableDatabase != null) close(null, writableDatabase);
        }
    }

    /**
     * 插入一条数据;
     */
    public int insert(RecorderVideoData upData) {
        ContentValues cv = new ContentValues();
        cv.put(RecorderVideoData.video_add_time, upData.getVideoAddTime());
        cv.put(RecorderVideoData.video_long_time, upData.getVideoLongTime());
        cv.put(RecorderVideoData.video_show_time, upData.getVideoShowTime());
        cv.put(RecorderVideoData.video_path, upData.getVideoPath());
        cv.put(RecorderVideoData.video_img_path, upData.getVideoImgPath());
        SQLiteDatabase writableDatabase = null;
        long id = -1;
        try {
            writableDatabase = this.getWritableDatabase();
            id = writableDatabase.insert(TB_NAME, null, cv);
            this.getWritableDatabase().close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writableDatabase != null) close(null, writableDatabase);
        }
        return (int) id;
    }

    public int insert(Map<String, String> map) {
        ContentValues cv = new ContentValues();
        cv.put(RecorderVideoData.video_add_time,map.get(RecorderVideoData.video_add_time));
        cv.put(RecorderVideoData.video_long_time, map.get(RecorderVideoData.video_long_time));
        cv.put(RecorderVideoData.video_show_time, map.get(RecorderVideoData.video_show_time));
        cv.put(RecorderVideoData.video_path, map.get(RecorderVideoData.video_path));
        cv.put(RecorderVideoData.video_img_path, map.get(RecorderVideoData.video_img_path));
        SQLiteDatabase writableDatabase = null;
        long id = -1;
        try {
            writableDatabase = this.getWritableDatabase();
            id = writableDatabase.insert(TB_NAME, null, cv);
            this.getWritableDatabase().close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writableDatabase != null) close(null, writableDatabase);
        }
        return (int) id;
    }

    public RecorderVideoData selectLastTimeData(){
        RecorderVideoData data = new RecorderVideoData();
        Cursor cur = null;
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            cur = writableDatabase.query(TB_NAME, null, null, null, null, null, RecorderVideoData.video_add_time + " desc");// 查询并获得游标
            if (cur.moveToFirst()) {// 判断游标是否为空
                Map<String, String> map = new HashMap<>();
                int id = cur.getInt(cur.getColumnIndex(RecorderVideoData.video_id));
                Long addTime = cur.getLong(cur.getColumnIndex(RecorderVideoData.video_add_time));
                float longTime = cur.getFloat(cur.getColumnIndex(RecorderVideoData.video_long_time));
                String showTime = cur.getString(cur.getColumnIndex(RecorderVideoData.video_show_time));
                String videoPath = cur.getString(cur.getColumnIndex(RecorderVideoData.video_path));
                String imgPath = cur.getString(cur.getColumnIndex(RecorderVideoData.video_img_path));
                data.setVideoId(id);
                data.setVideoAddTime(addTime);
                data.setVideoLongTime(longTime);
                data.setVideoShowTime(showTime);
                data.setVideoPath(videoPath);
                data.setVideoImgPath(imgPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writableDatabase != null) close(cur, writableDatabase);
        }
        return data;
    }

    public Map<String, String> selectIdByPath(String path) {
        Map<String, String> map = new HashMap<>();
        if(TextUtils.isEmpty(path)) return map;
        Cursor cur = null;
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            cur = writableDatabase.query(TB_NAME, null,
                    RecorderVideoData.video_path + "=?", new String[]{path}, null, null, null);// 查询并获得游标
            if (cur.moveToFirst()) {// 判断游标是否为空
                int id = cur.getInt(cur.getColumnIndex(RecorderVideoData.video_id));
                long addTime = cur.getLong(cur.getColumnIndex(RecorderVideoData.video_add_time));
                float longTime = cur.getFloat(cur.getColumnIndex(RecorderVideoData.video_long_time));
                String showTime = cur.getString(cur.getColumnIndex(RecorderVideoData.video_show_time));
                String videoPath = cur.getString(cur.getColumnIndex(RecorderVideoData.video_path));
                String imgPath = cur.getString(cur.getColumnIndex(RecorderVideoData.video_img_path));
                map.put(RecorderVideoData.video_id, id + "");
                map.put(RecorderVideoData.video_add_time, String.valueOf(addTime));
                map.put(RecorderVideoData.video_show_time, showTime);
                map.put(RecorderVideoData.video_long_time, String.valueOf(longTime));
                map.put(RecorderVideoData.video_path, videoPath);
                map.put(RecorderVideoData.video_img_path, imgPath);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            close(cur, writableDatabase);
        }
        return map;
    }

    /**
     * 获取所有上传中的数据
     * @return
     */
    public ArrayList<Map<String, String>> getAllIngDataInDB() {
        Cursor cur = null;
        SQLiteDatabase writableDatabase = null;
        try {
            ArrayList<Map<String, String>> listmap = new ArrayList<>();
            writableDatabase = getWritableDatabase();
            cur = writableDatabase.query(TB_NAME, null, null, null, null, null, RecorderVideoData.video_add_time + " desc");// 查询并获得游标
            if (cur.moveToFirst()) {// 判断游标是否为空
                do {
                    Map<String, String> map = new HashMap<>();
                    int id = cur.getInt(cur.getColumnIndex(RecorderVideoData.video_id));
                    long addTime = cur.getLong(cur.getColumnIndex(RecorderVideoData.video_add_time));
                    float longTime = cur.getFloat(cur.getColumnIndex(RecorderVideoData.video_long_time));
                    String showTime = cur.getString(cur.getColumnIndex(RecorderVideoData.video_show_time));
                    String videoPath = cur.getString(cur.getColumnIndex(RecorderVideoData.video_path));
                    String imgPath = cur.getString(cur.getColumnIndex(RecorderVideoData.video_img_path));

                    map.put(RecorderVideoData.video_id, id + "");
                    map.put(RecorderVideoData.video_add_time, String.valueOf(addTime));
                    map.put(RecorderVideoData.video_show_time, showTime);
                    map.put(RecorderVideoData.video_long_time, String.valueOf(longTime));
                    map.put(RecorderVideoData.video_path, videoPath);
                    map.put(RecorderVideoData.video_img_path, imgPath);
                    listmap.add(map);
                } while (cur.moveToNext());
            }
            return listmap;
        } catch (Exception e) {
            return new ArrayList<>();
        } finally {
            if (writableDatabase != null) close(cur, writableDatabase);
        }
    }

    public int getDataSize(){
        Cursor cur = null;
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            cur = writableDatabase.query(TB_NAME, null, null, null, null, null, null);// 查询并获得游标
            return cur.getCount();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (writableDatabase != null) close(cur, writableDatabase);
        }
        return 0;
    }

    public boolean deleteById(String id) {
        int i = -1;
        try {
            i = this.getWritableDatabase().delete(TB_NAME, RecorderVideoData.video_id + "=" + id,null);
            this.getWritableDatabase().close();
        } catch (Exception e) {
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

    private static final String TB_NAME = "tb_recorderVideo";
    private static final String CREATE_TABLE_SQL = "create table if not exists " + TB_NAME + "("
            + RecorderVideoData.video_id + " integer primary key autoincrement,"
            + RecorderVideoData.video_add_time + " long,"
            + RecorderVideoData.video_show_time + " text,"
            + RecorderVideoData.video_long_time + " float,"
            + RecorderVideoData.video_img_path + " text,"
            + RecorderVideoData.video_path + " text)";

}
