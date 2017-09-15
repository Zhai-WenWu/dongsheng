package amodule.dish.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import amodule.user.adapter.AdapterMyDish;
import xh.basic.tool.UtilLog;

public class UploadDishSqlite extends SQLiteOpenHelper {

    public UploadDishSqlite(Context context) {
        //第四个参数为此版数据库的版本号:2->添加了一个food列 3->修补以前版本没有subjectType列和ds_story列
        //4->添加了了一个线上菜谱code列  5->添加了活动id，7-》添加了准备时间，难度，口味，烹饪时间
        //8-》添加了避免重复上传的uploadTimeCode  9->>添加了菜谱类型1-普通菜谱，2-视频菜谱
        super(context, TB_NAME, null, 10);
//		//Log.i("FRJ", "UploadDishSqlite--UploadDishSqlite():");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
//		//Log.i("FRJ", "UploadDishSqlite--onCreate():");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // db.execSQL("ALTER TABLE tb_student ADD COLUMN other STRING");
//		//Log.i("FRJ", "UploadDishSqlite--onUpgrade():"+oldVersion + "   ;" + newVersion);
//		需要升级
        if (oldVersion < newVersion) {
            try {
                switch (oldVersion) {
                    case 1:
                        db.execSQL("ALTER TABLE " + TB_NAME + " ADD COLUMN " + UploadDishData.ds_food);
                    case 2:
                        if (!checkColumnExists(db, TB_NAME, UploadDishData.ds_dishType)) {
                            db.execSQL("ALTER TABLE " + TB_NAME + " ADD COLUMN " + UploadDishData.ds_dishType);
                        }
                        if (!checkColumnExists(db, TB_NAME, UploadDishData.ds_story)) {
                            db.execSQL("ALTER TABLE " + TB_NAME + " ADD COLUMN " + UploadDishData.ds_story);
                        }
                    case 3:
                        if (!checkColumnExists(db, TB_NAME, UploadDishData.ds_code)) {
                            db.execSQL("ALTER TABLE " + TB_NAME + " ADD COLUMN " + UploadDishData.ds_code);
                        }
                    case 4:
                        if (!checkColumnExists(db, TB_NAME, UploadDishData.ds_activityId)) {
                            db.execSQL("ALTER TABLE " + TB_NAME + " ADD COLUMN " + UploadDishData.ds_activityId);
                        }
                    case 5:
                    case 6:
                        if (!checkColumnExists(db, TB_NAME, UploadDishData.ds_readyTime)) {
                            db.execSQL("ALTER TABLE " + TB_NAME + " ADD COLUMN " + UploadDishData.ds_readyTime);
                            db.execSQL("ALTER TABLE " + TB_NAME + " ADD COLUMN " + UploadDishData.ds_cookTime);
                            db.execSQL("ALTER TABLE " + TB_NAME + " ADD COLUMN " + UploadDishData.ds_taste);
                            db.execSQL("ALTER TABLE " + TB_NAME + " ADD COLUMN " + UploadDishData.ds_diff);
                        }
                    case 7:
                        if (!checkColumnExists(db, TB_NAME, UploadDishData.ds_uploadTimeCode)) {
                            db.execSQL("ALTER TABLE " + TB_NAME + " ADD COLUMN " + UploadDishData.ds_uploadTimeCode);
                        }
                    case 8:
                        if (!checkColumnExists(db, TB_NAME, UploadDishData.ds_exclusive)) {
                            db.execSQL("ALTER TABLE " + TB_NAME + " ADD COLUMN " + UploadDishData.ds_exclusive);
                        }
                    case 9:
                        if (!checkColumnExists(db, TB_NAME, UploadDishData.ds_videoType)) {
                            db.execSQL("ALTER TABLE " + TB_NAME + " ADD COLUMN " + UploadDishData.ds_videoType);
                            db.execSQL("ALTER TABLE " + TB_NAME + " ADD COLUMN " + UploadDishData.ds_capture);
                        }
                }

            } catch (Exception e) {
                UtilLog.reportError("上传菜谱数据添加列出错;oldVersion:" +
                        oldVersion + ";newVersion:" + newVersion, e);
            }
        }
    }

    /**
     * 方法2：检查表中某列是否存在
     *
     * @param db
     * @param tableName  表名
     * @param columnName 列名
     * @return
     */
    private boolean checkColumnExists(SQLiteDatabase db, String tableName, String columnName) {
        boolean result = false;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("select * from sqlite_master where name = ? and sql like ?"
                    , new String[]{tableName, "%" + columnName + "%"});
            result = null != cursor && cursor.moveToFirst();
        } catch (Exception e) {
            UtilLog.reportError("检查表中某列是否存在", e);
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return result;
    }


    public void executeSql(String sql) {
        try {
            this.getWritableDatabase().execSQL(sql);
            this.getWritableDatabase().close();
        } catch (Exception e) {
        }
    }


    public synchronized ArrayList<Map<String, String>> getAllDataInDB() {
        Cursor cur = null;
        SQLiteDatabase writableDatabase = null;
        try {
            ArrayList<Map<String, String>> listmap = new ArrayList<>();
            writableDatabase = getWritableDatabase();
            cur = writableDatabase.query(TB_NAME, null, null, null,
                    null, null, UploadDishData.ds_addTime + " desc");// 查询并获得游标
            if (cur.moveToFirst()) {// 判断游标是否为空
                do {
                    Map<String, String> map = new HashMap<>();
                    int id = cur.getInt(cur.getColumnIndex(UploadDishData.ds_id));
                    String name = cur.getString(cur.getColumnIndex(UploadDishData.ds_name));
                    String code = cur.getString(cur.getColumnIndex(UploadDishData.ds_code));
                    String dishCover = cur.getString(cur.getColumnIndex(UploadDishData.ds_cover));
                    String dishTime = cur.getString(cur.getColumnIndex(UploadDishData.ds_addTime));
                    String makes = cur.getString(cur.getColumnIndex(UploadDishData.ds_makes));
                    String capture = cur.getString(cur.getColumnIndex(UploadDishData.ds_capture));
                    String dishType = cur.getString(cur.getColumnIndex(UploadDishData.ds_dishType));
                    String dishReadyTime = cur.getString(cur.getColumnIndex(UploadDishData.ds_readyTime));
                    String dishCookTime = cur.getString(cur.getColumnIndex(UploadDishData.ds_cookTime));
                    String dishTaste = cur.getString(cur.getColumnIndex(UploadDishData.ds_taste));
                    String dishDiff = cur.getString(cur.getColumnIndex(UploadDishData.ds_diff));
                    String dishExclusive = cur.getString(cur.getColumnIndex(UploadDishData.ds_exclusive));
                    long ds_uploadTimeCode = cur.getLong(cur.getColumnIndex(UploadDishData.ds_uploadTimeCode));
                    String activityId = cur.getString(cur.getColumnIndex(UploadDishData.ds_activityId));
                    String videoType = cur.getString(cur.getColumnIndex(UploadDishData.ds_videoType));

                    map.put("id", id + "");
                    map.put("code", code);
                    map.put("style", AdapterMyDish.styleNormal + "");
                    map.put("name", name);
                    map.put("img", dishCover);
                    map.put("favNum", "");
                    map.put("allClick", "");
                    map.put("draft", dishType);
                    map.put("addTime", dishTime);
                    map.put("makes", makes);
                    map.put("capture", capture);
                    map.put("readyTime", dishReadyTime);
                    map.put("dishCookTime", dishCookTime);
                    map.put("dishTaste", dishTaste);
                    map.put("dishDiff", dishDiff);
                    map.put("dishExclusive", dishExclusive);
                    map.put("uploadTimeCode", String.valueOf(ds_uploadTimeCode));
                    map.put("activityId", activityId);
                    map.put("videoType", videoType);
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
            cur = writableDatabase.query(TB_NAME, null, UploadDishData.ds_dishType + "=? or " + UploadDishData.ds_dishType + "=?",
                    new String[]{UploadDishData.UPLOAD_ING,UploadDishData.UPLOAD_ING_BACK},
                    null, null, UploadDishData.ds_addTime + " desc");// 查询并获得游标
            if (cur.moveToFirst()) {// 判断游标是否为空
                do {
                    Map<String, String> map = new HashMap<>();
                    int id = cur.getInt(cur.getColumnIndex(UploadDishData.ds_id));
                    String name = cur.getString(cur.getColumnIndex(UploadDishData.ds_name));
                    String code = cur.getString(cur.getColumnIndex(UploadDishData.ds_code));
                    String dishCover = cur.getString(cur.getColumnIndex(UploadDishData.ds_cover));
                    String makes = cur.getString(cur.getColumnIndex(UploadDishData.ds_makes));
                    String capture = cur.getString(cur.getColumnIndex(UploadDishData.ds_capture));
                    String dishTime = cur.getString(cur.getColumnIndex(UploadDishData.ds_addTime));
                    String dishType = cur.getString(cur.getColumnIndex(UploadDishData.ds_dishType));
                    String dishReadyTime = cur.getString(cur.getColumnIndex(UploadDishData.ds_readyTime));
                    String dishCookTime = cur.getString(cur.getColumnIndex(UploadDishData.ds_cookTime));
                    String dishTaste = cur.getString(cur.getColumnIndex(UploadDishData.ds_taste));
                    String dishDiff = cur.getString(cur.getColumnIndex(UploadDishData.ds_diff));
                    String dishExclusive = cur.getString(cur.getColumnIndex(UploadDishData.ds_exclusive));
                    long ds_uploadTimeCode = cur.getLong(cur.getColumnIndex(UploadDishData.ds_uploadTimeCode));
                    String activityId = cur.getString(cur.getColumnIndex(UploadDishData.ds_activityId));
                    String videoType = cur.getString(cur.getColumnIndex(UploadDishData.ds_videoType));

                    map.put("id", id + "");
                    map.put("code", code);
                    map.put("style", AdapterMyDish.styleNormal + "");
                    map.put("name", name);
                    map.put("img", dishCover);
                    map.put("favNum", "");
                    map.put("makes", makes);
                    map.put("capture", capture);
                    map.put("allClick", "");
                    map.put("draft", dishType);
                    map.put("addTime", dishTime);
                    map.put("readyTime", dishReadyTime);
                    map.put("dishCookTime", dishCookTime);
                    map.put("dishTaste", dishTaste);
                    map.put("dishDiff", dishDiff);
                    map.put("dishExclusive", dishExclusive);
                    map.put("uploadTimeCode", String.valueOf(ds_uploadTimeCode));
                    map.put("activityId", activityId);
                    map.put("videoType", videoType);
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

    public int getFailNeedHintId() {
//        Cursor cur = null;
//        SQLiteDatabase writableDatabase = null;
//        int numer = -1;
//        try {
//            writableDatabase = getWritableDatabase();
//            cur = writableDatabase.query(TB_NAME, null,
//                    UploadDishData.ds_videoType + "=? and " + UploadDishData.ds_dishType + "=?",
//                    new String[]{"2", UploadDishData.UPLOAD_PAUSE},
//                    null, null, null);// 查询并获得游标
//            if(cur.moveToLast())
//                numer = cur.getInt(cur.getColumnIndex(UploadDishData.ds_id));
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (writableDatabase != null) close(cur, writableDatabase);
//        }
//        return numer;
        return -1;
    }


    //获取正在上传的菜谱视频草稿箱id,
    public int getUploadingId() {
        Cursor cur = null;
        SQLiteDatabase writableDatabase = null;
        int numer = -1;
        try {
            writableDatabase = getWritableDatabase();
            cur = writableDatabase.query(TB_NAME, null,
                    UploadDishData.ds_videoType + "=? and " + UploadDishData.ds_dishType + "=? or"
                            + UploadDishData.ds_dishType + "=?",
                    new String[]{"2", UploadDishData.UPLOAD_ING, UploadDishData.UPLOAD_ING_BACK},
                    null, null, null);// 查询并获得游标
            if (cur.moveToLast()) {
                numer = cur.getInt(cur.getColumnIndex(UploadDishData.ds_id));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writableDatabase != null) close(cur, writableDatabase);
        }
        return numer;
    }

    public Map<String, String> getUploadingBack(){
        Cursor cur = null;
        SQLiteDatabase writableDatabase = null;
        Map<String, String> map = null;
        try {
            writableDatabase = getWritableDatabase();
            cur = writableDatabase.query(TB_NAME, null,
                    UploadDishData.ds_videoType + "=? and " + UploadDishData.ds_dishType + "=?",
                    new String[]{"2", UploadDishData.UPLOAD_ING_BACK},
                    null, null, null);// 查询并获得游标
            if(cur.moveToLast()){
                map = new HashMap<>();
                map.put("code",cur.getString(cur.getColumnIndex(UploadDishData.ds_code)));
                map.put("draft",cur.getString(cur.getColumnIndex(UploadDishData.ds_dishType)));
                map.put("id",String.valueOf(cur.getInt(cur.getColumnIndex(UploadDishData.ds_id))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writableDatabase != null) close(cur, writableDatabase);
        }
        return map;
    }




    public List<Integer> getUploadintIds(){
        Cursor cur = null;
        SQLiteDatabase writableDatabase = null;
        List<Integer> idList = new ArrayList<>();
        try {
            writableDatabase = getWritableDatabase();
            cur = writableDatabase.query(TB_NAME, null,
                    UploadDishData.ds_videoType + "=? and " + UploadDishData.ds_dishType + "=? and " + UploadDishData.ds_dishType + "=? ",
                    new String[]{"2", UploadDishData.UPLOAD_PAUSE,UploadDishData.UPLOAD_ING},
                    null, null, null);// 查询并获得游标
            if (cur.moveToFirst()) {// 判断游标是否为空
                do {
                    int numer = cur.getInt(cur.getColumnIndex(UploadDishData.ds_id));
                    idList.add(numer);
                } while (cur.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writableDatabase != null) close(cur, writableDatabase);
        }
        return idList;
    }


    public int getAllDraftSize() {
        Cursor cur = null;
        int num = 0;
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            cur = writableDatabase.query(TB_NAME, null,
                    UploadDishData.ds_dishType + "=?", new String[]{UploadDishData.UPLOAD_DRAF}, null, null, null);// 查询并获得游标
            num = cur.getCount();

            return num;
        } catch (Exception e) {
            e.printStackTrace();
            return num;
        } finally {
            if (writableDatabase != null) close(cur, writableDatabase);
        }
    }

    public synchronized UploadDishData selectById(int id) {
        Cursor cur = null;
        SQLiteDatabase readableDatabase = null;
        try {
            readableDatabase = getReadableDatabase();
            UploadDishData upData = new UploadDishData();
            cur = readableDatabase.query(TB_NAME, null,
                    UploadDishData.ds_id + "=?", new String[]{String.valueOf(id)}, null, null, null);// 查询并获得游标
            if (cur.moveToFirst()) {// 判断游标是否为空
                upData.setId(cur.getInt(cur.getColumnIndex(UploadDishData.ds_id)));
                upData.setCode(cur.getString(cur.getColumnIndex(UploadDishData.ds_code)));
                upData.setName(cur.getString(cur.getColumnIndex(UploadDishData.ds_name)));
                upData.setCover(cur.getString(cur.getColumnIndex(UploadDishData.ds_cover)));
                upData.setBurden(cur.getString(cur.getColumnIndex(UploadDishData.ds_burden)));
                try {
                    upData.setFood(cur.getString(cur.getColumnIndex(UploadDishData.ds_food)));
                } catch (Exception e) {
                    upData.setFood("");
                    UtilLog.reportError("数据库查找新添加的食材列", e);
                }
                upData.setMakes(cur.getString(cur.getColumnIndex(UploadDishData.ds_makes)));
                upData.setCaptureVideoInfo(cur.getString(cur.getColumnIndex(UploadDishData.ds_capture)));
                upData.setTips(cur.getString(cur.getColumnIndex(UploadDishData.ds_tips)));
                upData.setStory(cur.getString(cur.getColumnIndex(UploadDishData.ds_story)));
                upData.setDishType(cur.getString(cur.getColumnIndex(UploadDishData.ds_dishType)));
                upData.setAddTime(cur.getString(cur.getColumnIndex(UploadDishData.ds_addTime)));
                upData.setReadyTime(cur.getString(cur.getColumnIndex(UploadDishData.ds_readyTime)));
                upData.setCookTime(cur.getString(cur.getColumnIndex(UploadDishData.ds_cookTime)));
                upData.setTaste(cur.getString(cur.getColumnIndex(UploadDishData.ds_taste)));
                upData.setDiff(cur.getString(cur.getColumnIndex(UploadDishData.ds_diff)));
                upData.setExclusive(cur.getString(cur.getColumnIndex(UploadDishData.ds_exclusive)));
                upData.setUploadTimeCode(cur.getLong(cur.getColumnIndex(UploadDishData.ds_uploadTimeCode)));
                upData.setActivityId(cur.getString(cur.getColumnIndex(UploadDishData.ds_activityId)));
                upData.setVideType(cur.getString(cur.getColumnIndex(UploadDishData.ds_videoType)));
            }
            return upData;
        } finally {
            close(cur, readableDatabase);
        }
    }

    public int selectIdByCode(String code) {
        if(TextUtils.isEmpty(code)) return -1;
        Cursor cur = null;
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            int id = -1;
            cur = writableDatabase.query(TB_NAME, null,
                    UploadDishData.ds_code + "=?", new String[]{code}, null, null, null);// 查询并获得游标
            if (cur.moveToFirst()) {// 判断游标是否为空
                id = cur.getInt(cur.getColumnIndex(UploadDishData.ds_id));
            }
            return id;
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            close(cur, writableDatabase);
        }
        return -1;
    }

    public boolean isExist(int recoverId) {
        Cursor cur = null;
        SQLiteDatabase writableDatabase = null;
        try {
            writableDatabase = getWritableDatabase();
            boolean isRecover = false;
            cur = writableDatabase.query(TB_NAME, null, null, null,
                    null, null, null);// 查询并获得游标
            if (cur.moveToFirst()) {// 判断游标是否为空
                do {
                    int id = cur.getInt(cur.getColumnIndex(UploadDishData.ds_id));
                    if (id == recoverId) {
                        isRecover = true;
                        break;
                    }
                } while (cur.moveToNext());
            }
            return isRecover;
        } finally {
            close(cur, writableDatabase);
        }
    }

    /**
     * 插入一条数据;
     */
    public int insert(UploadDishData upData) {
        ContentValues cv = new ContentValues();
        cv.put(UploadDishData.ds_code, upData.getCode());
        cv.put(UploadDishData.ds_name, upData.getName());
        cv.put(UploadDishData.ds_cover, upData.getCover());
        cv.put(UploadDishData.ds_burden, upData.getBurden());
        cv.put(UploadDishData.ds_food, upData.getFood());
        cv.put(UploadDishData.ds_makes, upData.getMakes());
        cv.put(UploadDishData.ds_capture, upData.getCaptureVideoInfo());
        cv.put(UploadDishData.ds_tips, upData.getTips());
        cv.put(UploadDishData.ds_story, upData.getStory());
        cv.put(UploadDishData.ds_dishType, upData.getDishType());
        cv.put(UploadDishData.ds_addTime, upData.getAddTime());
        cv.put(UploadDishData.ds_readyTime, upData.getReadyTime());
        cv.put(UploadDishData.ds_cookTime, upData.getCookTime());
        cv.put(UploadDishData.ds_taste, upData.getTaste());
        cv.put(UploadDishData.ds_diff, upData.getDiff());
        cv.put(UploadDishData.ds_exclusive, upData.getExclusive());
        cv.put(UploadDishData.ds_uploadTimeCode, upData.getUploadTimeCode());
        cv.put(UploadDishData.ds_activityId, upData.getActivityId());
        cv.put(UploadDishData.ds_videoType, upData.getVideType());

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
    public synchronized int update(int id, UploadDishData upData) {
        int row = -1;
        if(upData == null) return row;
        SQLiteDatabase writableDatabase = null;
        ContentValues cv = new ContentValues();
        cv.put(UploadDishData.ds_code, upData.getCode());
        cv.put(UploadDishData.ds_name, upData.getName());
        cv.put(UploadDishData.ds_cover, upData.getCover());
        cv.put(UploadDishData.ds_burden, upData.getBurden());
        cv.put(UploadDishData.ds_food, upData.getFood());
        cv.put(UploadDishData.ds_makes, upData.getMakes());
        cv.put(UploadDishData.ds_capture, upData.getCaptureVideoInfo());
        cv.put(UploadDishData.ds_tips, upData.getTips());
        cv.put(UploadDishData.ds_story, upData.getStory());
        cv.put(UploadDishData.ds_dishType, upData.getDishType());
        cv.put(UploadDishData.ds_addTime, upData.getAddTime());
        cv.put(UploadDishData.ds_readyTime, upData.getReadyTime());
        cv.put(UploadDishData.ds_cookTime, upData.getCookTime());
        cv.put(UploadDishData.ds_taste, upData.getTaste());
        cv.put(UploadDishData.ds_diff, upData.getDiff());
        cv.put(UploadDishData.ds_exclusive, upData.getExclusive());
        cv.put(UploadDishData.ds_uploadTimeCode, upData.getUploadTimeCode());
        cv.put(UploadDishData.ds_activityId, upData.getActivityId());
        cv.put(UploadDishData.ds_videoType, upData.getVideType());
        try {
            writableDatabase = getWritableDatabase();
            row = writableDatabase.update(TB_NAME, cv, UploadDishData.ds_id + "=?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            close(null, writableDatabase);
        }
        return row;
    }

    public synchronized int update(int id, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(key, value);
        int row = -1;
        try {
            row = this.getWritableDatabase().update(TB_NAME, cv, UploadDishData.ds_id + "=?", new String[]{String.valueOf(id)});
            this.getWritableDatabase().close();
        } catch (Exception e) {
        }
        return row;
    }

    public boolean deleteById(int id) {
        int i = -1;
        try {
            i = this.getWritableDatabase().delete(TB_NAME, UploadDishData.ds_id + "=" + Integer.valueOf(id) + "",
                    null);
            this.getWritableDatabase().close();
        } catch (Exception e) {
        }
        return i > 0;
    }

    public boolean deleteDatabase(Context context) {
        return context.deleteDatabase(TB_NAME);
    }

    private static final String TB_NAME = "tb_uploadDish";
    private static final String CREATE_TABLE_SQL = "create table if not exists " + TB_NAME + "("
            + UploadDishData.ds_id + " integer primary key autoincrement,"
            + UploadDishData.ds_code + " text,"
            + UploadDishData.ds_name + " text,"
            + UploadDishData.ds_cover + "  text,"
            + UploadDishData.ds_burden + " text,"
            + UploadDishData.ds_food + " text,"
            + UploadDishData.ds_makes + " text,"
            + UploadDishData.ds_capture + " text,"
            + UploadDishData.ds_tips + " text,"
            + UploadDishData.ds_story + " text,"
            + UploadDishData.ds_dishType + " text,"
            + UploadDishData.ds_activityId + " text,"
            + UploadDishData.ds_readyTime + " text,"
            + UploadDishData.ds_cookTime + " text,"
            + UploadDishData.ds_taste + " text,"
            + UploadDishData.ds_diff + " text,"
            + UploadDishData.ds_exclusive + " text,"
            + UploadDishData.ds_videoType + " text,"
            + UploadDishData.ds_uploadTimeCode + " long,"
            + UploadDishData.ds_addTime + " text)";

    /**
     * 查询SubjectType的数据
     *
     * @param SubjectType
     * @return
     */
    public ArrayList<String> querySubjectType(String SubjectType) {
        StringBuffer sb = new StringBuffer();
        ArrayList<String> type = new ArrayList<String>();
        sb.append("SELECT ");
        sb.append(UploadDishData.ds_id);
        sb.append("	FROM " + TB_NAME + " ");
        sb.append("	WHERE " + UploadDishData.ds_dishType + " LIKE '% " + SubjectType + "  %'");

//		sb.append(" order by draftId desc");
        Cursor c = this.getReadableDatabase().rawQuery(sb.toString(), null);
        while (c.moveToNext()) {
            type.add(c.getString(c.getColumnIndex(UploadDishData.ds_id)));
        }
        c.close();
        this.getReadableDatabase().close();
        return type;
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
}

