package amodule.user.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import amodule.dish.db.ShowBuyData;

/**
 * PackageName : amodule.user.db
 * Created by MrTrying on 2016/8/17 11:53.
 * E_mail : ztanzeyu@gmail.com
 */
public class BrowseHistorySqlite extends SQLiteOpenHelper {
	/** 分页时，每页的数据总数 */
	public static final int PageSize = 10;
	/** 数据库版本 */
	public final static int VERSION = 1;
	/** 数据库名 */
	public final static String DB_NAME = "db_history";

	public final static String TB_DISH_NAME = "tb_dish";
	public final static String TB_SUBJECT_NAME = "tb_subject";
	public final static String TB_NOUS_NAME = "tb_nous";

	public BrowseHistorySqlite(final Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(getCreateTableName(TB_DISH_NAME));
		db.execSQL(getCreateTableName(TB_SUBJECT_NAME));
		db.execSQL(getCreateTableName(TB_NOUS_NAME));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public static final int SUBJECT_COUNT_MAX = 60;

	public synchronized int insertSubject(String tb_name, HistoryData data) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		long id = 0;
		try {
			ContentValues cv = new ContentValues();
			cv.put(HistoryData._browseTime, data.getBrowseTime());
			cv.put(HistoryData._code, data.getCode());
			cv.put(HistoryData._dataJson, data.getDataJson());

			db = getWritableDatabase();
			String selectSQL = "select count(*) from " + tb_name + " where " + HistoryData._code + "=" + data.getCode();
			cursor = db.rawQuery(selectSQL, null);
			if (cursor.moveToFirst()) {
				int count = cursor.getInt(cursor.getColumnIndex("count(*)"));
				if (count != 0) {
					id = db.update(tb_name, cv, HistoryData._code+"=?",new String[]{data.getCode()});
					return (int) id;
				}
			}
			//插入语句
			id = db.insert(tb_name, null, cv);
			//
			selectSQL = "select count(*) from " + tb_name;
			cursor = db.rawQuery(selectSQL, null);
			if (cursor.moveToFirst()) {
				int count = cursor.getInt(cursor.getColumnIndex("count(*)"));
				if (count > SUBJECT_COUNT_MAX) {
					int deleteCount = count - SUBJECT_COUNT_MAX;
					String deleteSQL = "delete from " + tb_name
							+ " where id in (select id from " + tb_name + " order by " + HistoryData._browseTime + " asc limit " + deleteCount + ")";
					db.execSQL(deleteSQL);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(cursor, db);
		}
		return (int) id;
	}

	public List<Map<String, String>> loadByPage(String tb_name, int currentPage) {
		List<Map<String, String>> datas = new ArrayList<>();
		Cursor cursor = null;
		SQLiteDatabase writableDatabase = null;
		try {
			writableDatabase = getWritableDatabase();
			String sql = "select * from " + tb_name + " Order By " + HistoryData._browseTime
					+ " Desc Limit " + String.valueOf(PageSize) + " Offset " + String.valueOf((currentPage - 1) * PageSize);
			cursor = writableDatabase.rawQuery(sql, null);
			if (cursor.moveToFirst()) {
				do {
					String dataJson = cursor.getString(cursor.getColumnIndex(HistoryData._dataJson));
					List<Map<String, String>> dataArray = StringManager.getListMapByJson(dataJson);
					datas.addAll(dataArray);
				} while (cursor.moveToNext());
			}
		} finally {
			close(cursor, writableDatabase);
		}
		return datas;
	}

	public boolean deleteByCode(String tb_name, String code) {
		SQLiteDatabase writableDatabase = null;
		try {
			writableDatabase = getWritableDatabase();
			if (TextUtils.isEmpty(code)) {
				int i = writableDatabase.delete(tb_name, null, null);
				return i > 0;
			}
			int i = writableDatabase.delete(tb_name, HistoryData._code + "=" + code,null);
			return i > 0;
		} finally {
			close(writableDatabase);
		}
	}

	private void close(SQLiteDatabase db) {
		close(null, db);
	}

	/** 关闭数据库游标 */
	private void close(Cursor c, SQLiteDatabase db) {
		if (null != c) {
			c.close();
			c = null;
		}
		if (null != db) {
			db.close();
		}
	}

	private final String getCreateTableName(String tb_name) {
		return "create table " + tb_name + "("
				+ HistoryData._id + " integer primary key autoincrement,"
				+ HistoryData._browseTime + " long,"
				+ HistoryData._code + " char unique,"
				+ HistoryData._dataJson + " text)";
	}

}
