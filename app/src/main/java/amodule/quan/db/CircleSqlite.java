package amodule.quan.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CircleSqlite extends SQLiteOpenHelper {
	public static final int VERSION = 1;

	public CircleSqlite(Context context){
		this(context, CircleDB.TB_NAME, null, VERSION);
	}

	/** 防止外部调用 */
	private CircleSqlite(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	/**
	 * 处理数据是更新还是插入
	 * @param circleData
	 * @return
	 */
	public synchronized int insertOrUpdate(CircleData circleData){
		String cid = circleData.getCid();
		CircleData data = select(CircleDB.db_cid , cid);
		if(data != null && cid.equals(data.getCid())){
			return update(circleData);
		}else{
			return insert(circleData); 
		}
	}
	
	public ArrayList<CircleData> getAllCircleData(){
		ArrayList<CircleData> circleArray = new ArrayList<>();
		Cursor cursor = null;
		try{
			cursor = getWritableDatabase().query(CircleDB.TB_NAME, null, null, null, null, null, null);
			if(cursor.moveToFirst()){
				do{			
					CircleData data = new CircleData();
					data.setCid(cursor.getString(cursor.getColumnIndex(CircleDB.db_cid)));
					data.setName(cursor.getString(cursor.getColumnIndex(CircleDB.db_name)));
					data.setRule(cursor.getString(cursor.getColumnIndex(CircleDB.db_rule)));
					data.setSkip(cursor.getString(cursor.getColumnIndex(CircleDB.db_skip)));
					data.setImg(cursor.getString(cursor.getColumnIndex(CircleDB.db_img)));
					data.setInfo(cursor.getString(cursor.getColumnIndex(CircleDB.db_info)));
					data.setCustomerNum(cursor.getString(cursor.getColumnIndex(CircleDB.db_customerNum)));
					data.setDayHotNum(cursor.getString(cursor.getColumnIndex(CircleDB.db_dayHotNum)));
					circleArray.add(data);
				}while(cursor.moveToNext());
			}
			return circleArray;
		}finally{
			close(cursor, getReadableDatabase());
		}
	}
	
	public CircleData select(String key , String cid){
		Cursor cursor = null;
		try{
			CircleData data = null;
			String selection =  key + "=?";
			String[] selectionArgs = { cid };
			cursor = getReadableDatabase().query(CircleDB.TB_NAME, null, selection, selectionArgs, null, null, null);
			if(cursor.moveToFirst()){
				data = new CircleData();
				data.setCid(cursor.getString(cursor.getColumnIndex(CircleDB.db_cid)));
				data.setName(cursor.getString(cursor.getColumnIndex(CircleDB.db_name)));
				data.setRule(cursor.getString(cursor.getColumnIndex(CircleDB.db_rule)));
				data.setSkip(cursor.getString(cursor.getColumnIndex(CircleDB.db_skip)));
				data.setImg(cursor.getString(cursor.getColumnIndex(CircleDB.db_img)));
				data.setInfo(cursor.getString(cursor.getColumnIndex(CircleDB.db_info)));
				data.setCustomerNum(cursor.getString(cursor.getColumnIndex(CircleDB.db_customerNum)));
				data.setDayHotNum(cursor.getString(cursor.getColumnIndex(CircleDB.db_dayHotNum)));
			}
			return data;
		}finally{
			close(cursor, getReadableDatabase());
		}
	}
	
	/**
	 * 插入数据
	 * @param circleData
	 * @return
	 */
	public int insert(CircleData circleData){
		SQLiteDatabase db = null;
		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(CircleDB.db_cid, circleData.getCid());
			contentValues.put(CircleDB.db_name, circleData.getName());
			contentValues.put(CircleDB.db_rule, circleData.getRule());
			contentValues.put(CircleDB.db_skip, circleData.getSkip());
			contentValues.put(CircleDB.db_img, circleData.getImg());
			contentValues.put(CircleDB.db_info, circleData.getInfo());
			contentValues.put(CircleDB.db_customerNum, circleData.getCustomerNum());
			contentValues.put(CircleDB.db_dayHotNum, circleData.getDayHotNum());
			long id = -1;
			db = getWritableDatabase();
			id = db.insert(CircleDB.TB_NAME, null, contentValues);
			return (int) id;
		} finally {
			close(null,db);
		}
	}
	
	public int update(CircleData circleData){
		SQLiteDatabase db = null;
		ContentValues contentValues = new ContentValues();
		contentValues.put(CircleDB.db_cid, circleData.getCid());
		contentValues.put(CircleDB.db_name, circleData.getName());
		contentValues.put(CircleDB.db_rule, circleData.getRule());
		contentValues.put(CircleDB.db_skip, circleData.getSkip());
		contentValues.put(CircleDB.db_img, circleData.getImg());
		contentValues.put(CircleDB.db_info, circleData.getInfo());
		contentValues.put(CircleDB.db_customerNum, circleData.getCustomerNum());
		contentValues.put(CircleDB.db_dayHotNum, circleData.getDayHotNum());
		int row = -1;
		try{
			db = getWritableDatabase();
			row= db.update(CircleDB.TB_NAME, contentValues,CircleDB.db_cid+"=?",new String[]{String.valueOf(circleData.getCid())});
		}catch(Exception e){
			
		}finally{
			close(null,db);
		}
		return row;
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	public boolean deleteById(int cid) {
		String selection = CircleDB.db_cid + "=" + cid;
		int i = this.getReadableDatabase().delete(CircleDB.TB_NAME, selection,null);
		this.getReadableDatabase().close();
		return i > 0;
	}
	
	public boolean deleteAll(){
		int i = this.getReadableDatabase().delete(CircleDB.TB_NAME, null,null);
		this.getReadableDatabase().close();
		return i > 0;
	}
	
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
		}
	}

	private static final String CREATE_TABLE_SQL = "create table if not exists " + CircleDB.TB_NAME + "("
			+ CircleDB.db_cid + " text primary key,"
			+ CircleDB.db_name + " text,"
			+ CircleDB.db_rule + " text,"
			+ CircleDB.db_skip + " text,"
			+ CircleDB.db_info + " text,"
			+ CircleDB.db_img + " text,"
			+ CircleDB.db_customerNum + " text,"
			+ CircleDB.db_dayHotNum + " text)";
	
	public class CircleDB {
		private static final String TB_NAME = "tb_circle";

		public static final String db_id = "id";
		public static final String db_cid = "cid";
		public static final String db_name = "name";
		public static final String db_rule = "rule";
		public static final String db_skip = "skip";
		public static final String db_info = "info";
		public static final String db_img = "img";
		public static final String db_customerNum = "customerNum";
		public static final String db_dayHotNum = "dayHotNum";
	}
	
}
