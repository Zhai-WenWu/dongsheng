package amodule.dish.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import acore.logic.AppCommon;
import acore.tools.ImgManager;

/**
 * 菜谱详情页离线菜谱数据库
 */
public class DishOffSqlite  extends SQLiteOpenHelper {
    public static final int pageSize = 10;// 分页时，每页的数据总数

    public DishOffSqlite(final Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MAIN_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ArrayList<String> getAllNedUpdataMoulderCodes(String moudleVersion){
        ArrayList<String> codes = new ArrayList<>();
        if(TextUtils.isEmpty(moudleVersion)) return codes;
        Cursor cur=null;
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            cur = writableDatabase.query(TB_MAIN_ENAME, null, DishOffData.bd_moudleVersion+"!='" + moudleVersion + "'", null,
                    null, null, null);// 查询并获得游标
//            cur = writableDatabase.query(TB_MAIN_ENAME, null, null, null,
//                    null, null, null);// 查询并获得游标
            if(cur.moveToFirst()){
                do{
                    String code =cur.getString(cur.getColumnIndex(DishOffData.bd_code));
                    String bd_moudleVersion =cur.getString(cur.getColumnIndex(DishOffData.bd_moudleVersion));
                    codes.add(code);
                }while(cur.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            close(cur, writableDatabase);
        }
        return codes;
    }

    /**
     * 查询数据库,得到所有的数据
     */
    public String getAllDataFromDB(){
        Cursor cur=null;
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            cur = writableDatabase.query(TB_MAIN_ENAME, null, null, null,
                    null, null, DishOffData.bd_addTime + " desc");// 查询并获得游标
            String json="";
            if(cur.moveToFirst()){
                do{
                    String statedb = cur.getString(cur.getColumnIndex(DishOffData.bd_json));
                    if(json==""){
                        json="[" + statedb;
                    }else{
                        json +="," + statedb;
                    }
                }while(cur.moveToNext());
                json = json + "]";
            }
            return json;
        }finally{
            close(cur, writableDatabase);
        }
    }

    /*
     * 读取指定ID的分页数据
     * SQL:Select * From TABLE_NAME Limit 9 Offset 10;
     * 表示从TABLE_NAME表获取数据，跳过10行，取9行
     */
    public String LoadPage(int pageID) {
        Cursor cur=null;
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            String sql = "select * from " + TB_MAIN_ENAME + " Order By ID Desc Limit " + String.valueOf(pageSize) + " Offset "
                    + String.valueOf((pageID-1) * pageSize);
            cur = writableDatabase.rawQuery(sql, null);

            String json="";
            if(cur.moveToFirst()){
                do{
                    String statedb=cur.getString(cur.getColumnIndex(DishOffData.bd_json));
                    if(json==""){
                        json="[" + statedb;
                    }else{
                        json +="," + statedb;
                    }
                }while(cur.moveToNext());
                json = json + "]";
            }
            return json;
        }finally{
            close(cur, writableDatabase);
        }
    }

    /**
     * 查询数据库
     * @param code
     * @return
     */
    public String selectByCode(String code) {
        Cursor cur=null;
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            cur = writableDatabase.query(TB_MAIN_ENAME, null,
                    DishOffData.bd_code+"=" + code , null, null, null, null);// 查询并获得游标
            String json="";
            if (cur.moveToFirst()) {// 判断游标是否为空
                json = cur.getString(cur.getColumnIndex(DishOffData.bd_json));
            }
            if(TextUtils.isEmpty(json)) json = "";
            return json;
        }
        finally{
            close(cur, writableDatabase);
        }
    }

    public int selectCount(){
        Cursor cur=null;
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            cur = writableDatabase.query(TB_MAIN_ENAME, null, null, null,
                    null, null, null);// 查询并获得游标
            int count = 0;
            if(cur.moveToFirst()){
                do{
                    count ++;
                }while(cur.moveToNext());
            }
            return count;
        }finally{
            close(cur, writableDatabase);
        }
    }

    /**
     * 插入一条数据;
     */
    public int insert(DishOffData buyData) {
        SQLiteDatabase writableDatabase = null;
        ContentValues cv = new ContentValues();
        long id = -1;
        cv.put(DishOffData.bd_code, buyData.getCode());
        cv.put(DishOffData.bd_name, buyData.getName());
        cv.put(DishOffData.bd_addTime, buyData.getAddTime());
        cv.put(DishOffData.bd_json, buyData.getJson());
        cv.put(DishOffData.bd_moudleVersion, buyData.getMoudleVersion());
        try {
            writableDatabase = getWritableDatabase();
            id = writableDatabase.insert(TB_MAIN_ENAME, null, cv);
            writableDatabase.close();
        }catch(Exception e){
        }
        return (int)id;
    }
    /**
     * 修改收藏数据;
     */
    public int updateIsFav(String code,String dishJson) {
        int row = -1;
        ContentValues cv = new ContentValues();
        cv.put(DishOffData.bd_json, dishJson);
        try{
            row= this.getWritableDatabase().update(TB_MAIN_ENAME, cv, DishOffData.bd_code+"=?",new String[]{code});
            this.getWritableDatabase().close();
        }catch(Exception e){

        }
        return row;
    }
    /**
     * 删除指定code的记录
     * @param code 为空删除buyBurden的所有内容，其他则删除指定code的菜谱数据，
     * @return
     */
    public boolean deleteByCode(String code) {
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            if(code ==""){
                int i = writableDatabase.delete(TB_MAIN_ENAME, null,
                        null);
                return i>0;
            }
            int i = writableDatabase.delete(TB_MAIN_ENAME, DishOffData.bd_code+"=" +  code,
                    null);
            return i > 0;
        } finally{
            close(writableDatabase);
        }
    }

    public void deleteByTime(int count){
        Cursor cur=null;
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            cur = writableDatabase.query(TB_MAIN_ENAME, null, null, null,
                    null, null, DishOffData.bd_addTime + " asc");// 查询并获得游标
            int num = 0;
            if (cur.moveToFirst()){
                do{
                    if(num < count){
                        String code = cur.getString(cur.getColumnIndex(DishOffData.bd_code));
                        deleteByCode(code);
                        num ++;
                    }
                }while(cur.moveToNext());
                AppCommon.buyBurdenNum -= num;
            }
        }finally{
            close(cur, writableDatabase);
        }
    }

    private void deleteImgByCode(String code){
        JSONObject theDish;
        try {
            theDish = new JSONObject(selectByCode(code));
            //删除大图
            ImgManager.delImg(theDish.getString("img"));
            //删除步骤图
            JSONArray dishMake=theDish.getJSONArray("makes");
            for(int j=0;j<dishMake.length();j++){
                ImgManager.delImg(dishMake.getJSONObject(j).getString("img"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除存储在本地的图片
     * @param code 为空删除buyBurden的所有内容，其他则删除指定code的图片数据，
     */
    public void deleteImg(String code){
        Cursor cur=null;
        SQLiteDatabase writableDatabase = null;
        try {
            if(code == ""){
                writableDatabase = getWritableDatabase();
                cur = writableDatabase.query(TB_MAIN_ENAME, null, null, null,
                        null, null, null);// 查询并获得游标
                if(cur.moveToFirst()){
                    do{
                        String newCode = cur.getString(cur.getColumnIndex(DishOffData.bd_code));
                        deleteImgByCode(newCode);
                    }while(cur.moveToNext());
                }
            }else{
                deleteImgByCode(code);
            }
        }finally{
            close(cur, writableDatabase);
        }
    }
    private final static String DB_NAME = "tb_dishData";
    public static final String TB_MAIN_ENAME ="tb_dishData";

    private final String CREATE_MAIN_TABLE_SQL = "create table if not exists "+TB_MAIN_ENAME+"("
            + DishOffData.bd_id+" integer primary key autoincrement,"
            + DishOffData.bd_code+" text,"
            + DishOffData.bd_name+" ntext,"
            + DishOffData.bd_addTime +" text,"
            + DishOffData.bd_moudleVersion +" text,"
            + DishOffData.bd_json + " text)" ;

    /**
     * 关闭数据库游标
     */
    private void close(Cursor c,SQLiteDatabase db){
        if(null!=c){
            c.close();
            c=null;
        }
        if(null!=db){
            db.close();
//			db=null;
        }
    }
    private void close(SQLiteDatabase db) {
        close(null, db);
    }
}