package amodule.quan.tool;

import xh.basic.tool.UtilLog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "database.db";// 数据库名称
	public static final int VERSION = 3;
	
	public static final String TABLE_CHANNEL = "channel";//数据表 

	public static final String ID = "id";//id
	public static final String NAME = "name";//对应名称
	public static final String SELECTED = "selected";//是否被用户选中
	public static final String FIX = "fix";//是否固定
	public static final String DES = "des";//数据描述
	public static final String IMG = "img";//数据url
	public static final String TYPE="type";//模块类型
	public static final String HASIMG="hasImg";//美食贴是否有图片
	/***********************VERSION==2添加的字段***************************/
	public static final String DISPLAYFLAG="displayFlag";//模块是否显示
	public static final String STYLE="style";//模块数据展示类型
	public static final String CODE="code";//模块code
	public static final String GROUPPOSITION="groupPosition";//该模块下群组数据
	
	private Context context;
	public SQLHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
		this.context = context;
	}

	public Context getContext(){
		return context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO  创建数据库后，对数据库的操作
		String sql = "create table if not exists "+TABLE_CHANNEL +
				"(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				ID + " INTEGER , " +
				NAME + " TEXT , " +
				TYPE + " INTEGER , " +
				HASIMG + " INTEGER , " +
				DES + " TEXT , " +
				IMG + " TEXT , " +
				DISPLAYFLAG + " INTEGER , " +
				STYLE + " INTEGER , " +
				CODE + " TEXT , " +
				GROUPPOSITION + " TEXT)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO更改数据库版本的操作
		if(oldVersion < newVersion){
			try{
				switch(oldVersion){
				case 1:
					db.execSQL("ALTER TABLE " + TABLE_CHANNEL + " ADD " + DISPLAYFLAG+" INTEGER");
					db.execSQL("ALTER TABLE " + TABLE_CHANNEL + " ADD " + STYLE+" INTEGER");
					db.execSQL("ALTER TABLE " + TABLE_CHANNEL + " ADD " + CODE+" TEXT");
					break;
				case 2:
					db.execSQL("ALTER TABLE " + TABLE_CHANNEL + " ADD " + GROUPPOSITION+" TEXT");
					break;
				}
			}catch(Exception e){
				UtilLog.reportError("上传菜谱数据添加列出错;oldVersion:" + 
									oldVersion + ";newVersion:" + newVersion, e);	
			}
		}
	}

}
