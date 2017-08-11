package amodule.answer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import amodule.answer.model.AskAnswerModel;

/**
 * Created by sll on 2017/7/20.
 */

public class AskAnswerSQLite extends SQLiteOpenHelper {

    private static final String mTabName = "tb_askanswer";
    private static final int mVersion = 1;

    private String mColumnId = "id";
    private String mColumnDishCode = "dishCode";
    private String mColumnQACode = "qaCode";
    private String mColumnAnswerCode = "answerCode";
    private String mColumnType = "type";
    private String mColumnTitle = "title";
    private String mColumnPrice = "price";
    private String mColumnImgs = "imgs";
    private String mColumnVideos = "videos";
    private String mColumnText = "text";
    private String mColumnAnonymity = "anonymity";
    private String mColumnAuthorCode = "authorCode";

    public AskAnswerSQLite(Context context) {
        super(context, mTabName, null, mVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists "
                + mTabName + "("
                + mColumnId + " integer primary key autoincrement,"
                + mColumnDishCode + " text,"
                + mColumnQACode + " text,"
                + mColumnAnswerCode + " text,"
                + mColumnType + " text,"
                + mColumnTitle + " text,"
                + mColumnPrice + " text,"
                + mColumnImgs + " text,"
                + mColumnVideos + " text,"
                + mColumnText + " text,"
                + mColumnAnonymity + " text,"
                + mColumnAuthorCode + " text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertData(AskAnswerModel model) {
        long row = -1;
        if (model == null) {
            return row;
        }

        Log.e("SLL", "insertData = " + model.toString());

        SQLiteDatabase database = null;
        ContentValues cv = new ContentValues();
        cv.put(mColumnAnswerCode, model.getmAnswerCode());
        cv.put(mColumnQACode, model.getmQACode());
        cv.put(mColumnDishCode, model.getmDishCode());
        cv.put(mColumnType, model.getmType());
        cv.put(mColumnTitle, model.getmTitle());
        cv.put(mColumnPrice, model.getmPrice());
        cv.put(mColumnImgs, model.getmImgs());
        cv.put(mColumnVideos, model.getmVideos());
        cv.put(mColumnText, model.getmText());
        cv.put(mColumnAnonymity, model.getmAnonymity());
        cv.put(mColumnAuthorCode, model.getmAuthorCode());
        try {
            database = getWritableDatabase();
            row = database.insert(mTabName, null, cv);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null)
                database.close();
            return row;
        }
    }

    public int updateData(int id, AskAnswerModel model) {
        int row = -1;
        if (id < 0 || model == null)
            return row;
        SQLiteDatabase database = null;
        ContentValues cv = new ContentValues();
        cv.put(mColumnAnswerCode, model.getmAnswerCode());
        cv.put(mColumnQACode, model.getmQACode());
        cv.put(mColumnDishCode, model.getmDishCode());
        cv.put(mColumnType, model.getmType());
        cv.put(mColumnTitle, model.getmTitle());
        cv.put(mColumnPrice, model.getmPrice());
        cv.put(mColumnImgs, model.getmImgs());
        cv.put(mColumnVideos, model.getmVideos());
        cv.put(mColumnText, model.getmText());
        cv.put(mColumnAnonymity, model.getmAnonymity());
        cv.put(mColumnAuthorCode, model.getmAuthorCode());
        try {
            database = getWritableDatabase();
            row = database.update(mTabName, cv, mColumnId + "=?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
        } finally {
            if (database != null)
                database.close();
            return row;
        }
    }

    public boolean deleteData(int id) {
        if (id < 0)
            return false;
        int num = -1;
        SQLiteDatabase database = null;
        try {
            database = getWritableDatabase();
            num = database.delete(mTabName, mColumnId + "=?", new String[]{String.valueOf(id)});
        } finally {
            if (database != null)
                database.close();
            return num > 0;
        }
    }

    public AskAnswerModel queryData(String dishCode, String qaType) {
        AskAnswerModel model = null;
        if (TextUtils.isEmpty(dishCode) || TextUtils.isEmpty(qaType))
            return model;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = getReadableDatabase();
            cursor = database.query(mTabName, null, mColumnDishCode + "=? and " + mColumnType + "=?", new String[]{dishCode, qaType}, null, null, mColumnId + " desc");
            if (cursor.moveToFirst()) {
                do {
                    model = new AskAnswerModel();
                    model.setmId(cursor.getInt(cursor.getColumnIndex(mColumnId)));
                    model.setmType(cursor.getString(cursor.getColumnIndex(mColumnType)));
                    model.setmAnswerCode(cursor.getString(cursor.getColumnIndex(mColumnAnswerCode)));
                    model.setmQACode(cursor.getString(cursor.getColumnIndex(mColumnQACode)));
                    model.setmDishCode(cursor.getString(cursor.getColumnIndex(mColumnDishCode)));
                    model.setmImgs(cursor.getString(cursor.getColumnIndex(mColumnImgs)));
                    model.setmText(cursor.getString(cursor.getColumnIndex(mColumnText)));
                    model.setmAnonymity(cursor.getString(cursor.getColumnIndex(mColumnAnonymity)));
                    model.setmAuthorCode(cursor.getString(cursor.getColumnIndex(mColumnAuthorCode)));
                    model.setmTitle(cursor.getString(cursor.getColumnIndex(mColumnTitle)));
                    model.setmPrice(cursor.getString(cursor.getColumnIndex(mColumnPrice)));
                    model.setmVideos(cursor.getString(cursor.getColumnIndex(mColumnVideos)));
                    break;
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (cursor != null)
                cursor.close();
            if (database != null) {
                database.close();
            }
            return model;
        }
    }

    public AskAnswerModel queryData(int id) {
        AskAnswerModel model = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = getReadableDatabase();
            cursor = database.query(mTabName, null, mColumnId + "=? ", new String[]{String.valueOf(id)}, null, null, mColumnId + " desc");
            if (cursor.moveToFirst()) {
                do {
                    model = new AskAnswerModel();
                    model.setmId(cursor.getInt(cursor.getColumnIndex(mColumnId)));
                    model.setmType(cursor.getString(cursor.getColumnIndex(mColumnType)));
                    model.setmAnswerCode(cursor.getString(cursor.getColumnIndex(mColumnAnswerCode)));
                    model.setmQACode(cursor.getString(cursor.getColumnIndex(mColumnQACode)));
                    model.setmDishCode(cursor.getString(cursor.getColumnIndex(mColumnDishCode)));
                    model.setmImgs(cursor.getString(cursor.getColumnIndex(mColumnImgs)));
                    model.setmText(cursor.getString(cursor.getColumnIndex(mColumnText)));
                    model.setmAnonymity(cursor.getString(cursor.getColumnIndex(mColumnAnonymity)));
                    model.setmAuthorCode(cursor.getString(cursor.getColumnIndex(mColumnAuthorCode)));
                    model.setmTitle(cursor.getString(cursor.getColumnIndex(mColumnTitle)));
                    model.setmPrice(cursor.getString(cursor.getColumnIndex(mColumnPrice)));
                    model.setmVideos(cursor.getString(cursor.getColumnIndex(mColumnVideos)));
                    break;
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (cursor != null)
                cursor.close();
            if (database != null) {
                database.close();
            }
            return model;
        }
    }

    public AskAnswerModel queryData(String qaType) {
        AskAnswerModel model = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = getReadableDatabase();
            cursor = database.query(mTabName, null, mColumnType + "=? ", new String[]{qaType}, null, null, mColumnId + " desc");
            if (cursor.moveToFirst()) {
                do {
                    model = new AskAnswerModel();
                    model.setmId(cursor.getInt(cursor.getColumnIndex(mColumnId)));
                    model.setmType(cursor.getString(cursor.getColumnIndex(mColumnType)));
                    model.setmAnswerCode(cursor.getString(cursor.getColumnIndex(mColumnAnswerCode)));
                    model.setmQACode(cursor.getString(cursor.getColumnIndex(mColumnQACode)));
                    model.setmDishCode(cursor.getString(cursor.getColumnIndex(mColumnDishCode)));
                    model.setmImgs(cursor.getString(cursor.getColumnIndex(mColumnImgs)));
                    model.setmText(cursor.getString(cursor.getColumnIndex(mColumnText)));
                    model.setmAnonymity(cursor.getString(cursor.getColumnIndex(mColumnAnonymity)));
                    model.setmAuthorCode(cursor.getString(cursor.getColumnIndex(mColumnAuthorCode)));
                    model.setmTitle(cursor.getString(cursor.getColumnIndex(mColumnTitle)));
                    model.setmPrice(cursor.getString(cursor.getColumnIndex(mColumnPrice)));
                    model.setmVideos(cursor.getString(cursor.getColumnIndex(mColumnVideos)));
                    break;
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (cursor != null)
                cursor.close();
            if (database != null) {
                database.close();
            }
            return model;
        }
    }
}
