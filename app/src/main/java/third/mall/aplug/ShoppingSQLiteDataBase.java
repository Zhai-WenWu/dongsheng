package third.mall.aplug;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 购物车数据库
 * @author yu
 *
 */
public class ShoppingSQLiteDataBase extends SQLiteOpenHelper{
	
	public static final int VERSION = 1;
	public static final String DB_NAME = "shoppsqlite.db";// 数据库名称
	public static final String TABLE = "shoppingcat";// 数据库名称
	public static String COMMOD_ID ="commod_id";//商品id
	public static String COMMOD_NUM ="commod_num";//商品数量
	
	private Context context;
	private static ShoppingSQLiteDataBase sqlite=null;
	private ShoppingSQLiteDataBase(Context context){
		super(context, DB_NAME, null, VERSION);
		this.context= context;
	}
	/**
	 * 单例模式
	 * @param context
	 * @return
	 */
	public synchronized static ShoppingSQLiteDataBase getInstance(Context context){
		if(sqlite==null){
			sqlite=new ShoppingSQLiteDataBase(context);
		}
		return sqlite;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//  创建数据库后，对数据库的操作
		String sql = "create table if not exists "+TABLE +
				"(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				COMMOD_ID + " VARCHAR , " +
				COMMOD_NUM + " INTEGER)";
		db.execSQL(sql);
		
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	/**
	 * 插入数据
	 * @param map
	 * @return
	 */
	public synchronized long insterData(String  id){
		SQLiteDatabase db=null;
		
		try {
			ContentValues values= new ContentValues();
			values.put(COMMOD_ID, id);
			values.put(COMMOD_NUM, 1);
			db=getWritableDatabase();
			return db.insert(TABLE, null, values);
		} finally{
			close(db);
		}
	}
	
	/**
	 * 获取商品一个状态的数据
	 * @param state--为空为无差别查询，0-无货，1—100-有货
	 * @return
	 */
	public synchronized long queryData(String state){
		SQLiteDatabase db=null;
		Cursor cursor = null;
		int num=0;
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT ");
			sb.append("	commod_id ");
			sb.append("FROM ");
			sb.append("	"+TABLE+" ");
			
			
			db = getReadableDatabase();
			cursor=db.rawQuery(sb.toString(), null);
			while (cursor.moveToNext()) {
				num++;
			}
			return num;
		}finally{
			close(cursor, db);
		}
	}
	/**
	 * 是否存在该商品
	 * @param id
	 * @return
	 */
	public synchronized long queryNoData(String id){
		SQLiteDatabase db=null;
		Cursor cursor = null;
		int num=0;
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT * ");
			sb.append("FROM ");
			sb.append("	"+TABLE+" ");
			sb.append("WHERE ");
			sb.append("	commod_id = " + id);
			
			db = getReadableDatabase();
			cursor=db.rawQuery(sb.toString(), null);
			while (cursor.moveToNext()) {
				num=cursor.getInt(cursor.getColumnIndex("commod_num"));
			}
			return num;
		}finally{
			close(cursor, db);
		}
	}
	
	/**
	 * 更新商品对应库存状态
	 * @param id
	 * @param state
	 * @return
	 */
	public synchronized long updateSate(String id,int num){
		SQLiteDatabase db=null;
		try {
			ContentValues values = new ContentValues();
			values.put(COMMOD_NUM, num);
			db = getWritableDatabase();
			return db.update(TABLE, values, "commod_id = ?", new String[]{id});
		} finally{
			close(db);
		}
	}
	
	/**
	 * 删除指定商品
	 * @param id id为null 全部删除
	 * @return
	 */
	public synchronized long deleteData(String id){
		SQLiteDatabase db=null;
		try {
			db = getWritableDatabase();
			if (id == null)
				return db.delete(TABLE, null, null);
			else
				return db.delete(TABLE, "commod_id = ?", new String[] { id });
		} finally{
			close(db);
		}
		
	}
	
	
	
	private void close(SQLiteDatabase db) {
		close(null, db);
	}

	private void close(Cursor c, SQLiteDatabase db) {
		if (c != null) {
			c.close();
			c = null;
		}
		 if (db != null) {
			 db.close();
			 db = null;
		 }
	}
}
