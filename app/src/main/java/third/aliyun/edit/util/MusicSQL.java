package third.aliyun.edit.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

import acore.override.XHApplication;

/**
 * 音乐到数据库
 */

public class MusicSQL extends SQLiteOpenHelper {
    private static final String sqltite_name="aliyun_video";//数据库名称
    public static final int VERSION = 1;
    public volatile static MusicSQL mInstance = null;

    public static MusicSQL getInstance(){
        if(mInstance == null){
            synchronized (MusicSQL.class){
                if(mInstance == null){
                    mInstance = new MusicSQL(XHApplication.in());
                }
            }
        }
        return mInstance;
    }
    /** 现在都使用此构造器实例化 */
    private MusicSQL(Context context){
        this(context, sqltite_name, null, VERSION);
    }
    public MusicSQL(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, sqltite_name, factory, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 插入数据
     * @param bean
     */
    public int insertMusic(MusicBean bean){
        if(getCodeState(bean.getCode())==null) {
            SQLiteDatabase db = null;
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MusicDB.db_code, bean.getCode());
                contentValues.put(MusicDB.db_name, bean.getName());
                contentValues.put(MusicDB.db_url, bean.getUrl());
                contentValues.put(MusicDB.db_status, bean.getStatus());
                long id = -1;
                db = getWritableDatabase();
                id = db.insert(MusicDB.TB_NAME, null, contentValues);
                return (int) id;
            } finally {
                close(null, db);
            }
        }
        return -1;
    }
    /**
     * 插入数据
     * @param mapList
     */
    public ArrayList<MusicBean> insertMusic(ArrayList<Map<String,String>> mapList){
        int size=mapList.size();
        ArrayList<MusicBean> musicBeans = new ArrayList<>();
        for(int i=0;i<size;i++){
            MusicBean bean =getCodeState(mapList.get(i).get("musicCode"));
            MusicBean musicBeanTemp = new MusicBean();
            musicBeanTemp.setMap(mapList.get(i));
            if(bean!=null){//当前已经存在
                musicBeans.add(bean);
                updateMusicBean(musicBeanTemp,false);

            }else{//当前不存在
                insertMusic(musicBeanTemp);
                musicBeans.add(musicBeanTemp);
            }
        }
        return musicBeans;
    }

    /**
     * 更新数据
     * @param musicBean
     * @param isDownLoad
     * @return
     */
    public int updateMusicBean(MusicBean musicBean, boolean isDownLoad){
        SQLiteDatabase db = null;
        ContentValues contentValues = new ContentValues();
        contentValues.put(MusicDB.db_code, musicBean.getCode());
        contentValues.put(MusicDB.db_name, musicBean.getName());
        contentValues.put(MusicDB.db_url, musicBean.getUrl());
        if(isDownLoad) {
            contentValues.put(MusicDB.db_url_location, musicBean.getLocationUrl());
            contentValues.put(MusicDB.db_is_download, musicBean.getIsDownLoad());
        }
        contentValues.put(MusicDB.db_status, musicBean.getStatus());
        int row = -1;
        try{
            db = getWritableDatabase();
            row= db.update(MusicDB.TB_NAME, contentValues, MusicDB.db_code+"=?",new String[]{String.valueOf(musicBean.getCode())});
        }catch(Exception e){

        }finally{
            close(null,db);
        }
        return row;
    }

    /**
     * 更新全部数据
     */
    public void updateAllData(){
        SQLiteDatabase db = null;
        ContentValues contentValues = new ContentValues();
        contentValues.put(MusicDB.db_status, "30");
        int row=-1;
        try{
            db= getWritableDatabase();
            row= db.update(MusicDB.TB_NAME, contentValues,null,null);
        }finally {
            close(null,db);
        }
    }
    /**
     * 获取音乐是否存在
     * @param code  false--不存在，true--已经存在
     */
    public MusicBean getCodeState(String code){
        Cursor cursor = null;
        try{
            MusicBean bean = new MusicBean();
            String selection = MusicDB.db_code + "=" + code;
            cursor = getReadableDatabase().query(MusicDB.TB_NAME, null, selection, null, null, null, null);
            // 查询并获得游标
            if(cursor.moveToFirst()==false){
                return null;
            }else{
                bean.setId(cursor.getInt(cursor.getColumnIndex(MusicDB.db_id)));
                bean.setCode(cursor.getString(cursor.getColumnIndex(MusicDB.db_code)));
                bean.setName(cursor.getString(cursor.getColumnIndex(MusicDB.db_name)));
                bean.setUrl(cursor.getString(cursor.getColumnIndex(MusicDB.db_url)));
                bean.setLocationUrl(cursor.getString(cursor.getColumnIndex(MusicDB.db_url_location)));
                bean.setIsDownLoad(cursor.getString(cursor.getColumnIndex(MusicDB.db_is_download)));
                bean.setStatus(cursor.getString(cursor.getColumnIndex(MusicDB.db_status)));
                return bean;
            }
        }finally{
            close(cursor, getReadableDatabase());
        }
    }

    /**
     * 查询全部数据
     * @return
     */
    public ArrayList<MusicBean> queryAllData(){
        Cursor cursor = null;
        ArrayList<MusicBean> musicBeans= new ArrayList<>();
        try{
            cursor = getReadableDatabase().query(MusicDB.TB_NAME, null, null, null, null, null, null);
                while (cursor.moveToNext()){
                    MusicBean bean = new MusicBean();
                    bean.setId(cursor.getInt(cursor.getColumnIndex(MusicDB.db_id)));
                    bean.setCode(cursor.getString(cursor.getColumnIndex(MusicDB.db_code)));
                    bean.setName(cursor.getString(cursor.getColumnIndex(MusicDB.db_name)));
                    bean.setUrl(cursor.getString(cursor.getColumnIndex(MusicDB.db_url)));
                    bean.setLocationUrl(cursor.getString(cursor.getColumnIndex(MusicDB.db_url_location)));
                    bean.setIsDownLoad(cursor.getString(cursor.getColumnIndex(MusicDB.db_is_download)));
                    bean.setStatus(cursor.getString(cursor.getColumnIndex(MusicDB.db_status)));
                    musicBeans.add(bean);
                    Log.i("xianghaTag","musicBeans::::"+bean.toMap().toString());
                }

        }finally {
            close(cursor, getReadableDatabase());
        }

        return musicBeans;
    }

    /**
     * 查询可展示的数据
     * @return
     */
    public ArrayList<MusicBean> queryAllShowData(){
        Cursor cursor = null;
        ArrayList<MusicBean> musicBeans= new ArrayList<>();
        try{
            String selection = MusicDB.db_status+"=10";
            cursor = getReadableDatabase().query(MusicDB.TB_NAME, null, selection, null, null, null, null);
            while (cursor.moveToNext()){
                MusicBean bean = new MusicBean();
                bean.setId(cursor.getInt(cursor.getColumnIndex(MusicDB.db_id)));
                bean.setCode(cursor.getString(cursor.getColumnIndex(MusicDB.db_code)));
                bean.setName(cursor.getString(cursor.getColumnIndex(MusicDB.db_name)));
                bean.setUrl(cursor.getString(cursor.getColumnIndex(MusicDB.db_url)));
                bean.setLocationUrl(cursor.getString(cursor.getColumnIndex(MusicDB.db_url_location)));
                bean.setIsDownLoad(cursor.getString(cursor.getColumnIndex(MusicDB.db_is_download)));
                bean.setStatus(cursor.getString(cursor.getColumnIndex(MusicDB.db_status)));

                musicBeans.add(bean);
            }

        }finally {
            close(cursor, getReadableDatabase());
        }
        return musicBeans;
    }

    /**
     * 删除全部数据
     */
    public void deleteAllData(){
        SQLiteDatabase db = null;
        try{
            db= getWritableDatabase();
            int num=db.delete(MusicDB.TB_NAME,null,null);
        }finally {
            close(null, db);
        }
    }
    /**
     * 关闭数据库游标
     */
    private void close(Cursor c, SQLiteDatabase db){
        if(null!=c){
            c.close();
            c=null;
        }
        if(null!=db){
            db.close();
        }
    }

    private static final String CREATE_TABLE_SQL = "create table if not exists " + MusicDB.TB_NAME + "("
            + MusicDB.db_id + " integer primary key autoincrement,"
            + MusicDB.db_code + " varchar,"
            + MusicDB.db_name + " varchar,"
            + MusicDB.db_url + " varchar,"
            + MusicDB.db_url_location + " varchar,"
            + MusicDB.db_is_download + " varchar,"
            + MusicDB.db_status + " varchar)";
    public class MusicDB {
        private static final String TB_NAME = "tb_music";//表名称

        public static final String db_id = "id";//自增长id
        public static final String db_code = "code";//code
        public static final String db_name = "name";//名称
        public static final String db_url = "url";//网络地址
        public static final String db_url_location = "location_url";//本地地址
        public static final String db_is_download = "isDownload";//是否下载 2是下载.1下载中
        public static final String db_status = "status";//是否下载 2是下载
    }
}
