package amodule.quan.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import org.w3c.dom.Text;

/**
 * 上传美食贴数据操作
 * @author Eva
 *
 */
public class SubjectSqlite extends SQLiteOpenHelper{
	public static final int VERSION = 3;

	public volatile static SubjectSqlite mInstance = null;

	public static SubjectSqlite getInstance(Context context){
		if(mInstance == null){
			synchronized (SubjectSqlite.class){
				if(mInstance == null){
					mInstance = new SubjectSqlite(context);
				}
			}
		}
		return mInstance;
	}

	/** 现在都使用此构造器实例化 */
	private SubjectSqlite(Context context){
		this(context, SubjectDB.TB_NAME, null, VERSION);
	}

	/** 防止外部调用 */
	private SubjectSqlite(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion < newVersion){
			switch(oldVersion){
			case 1: //需要添加timeCode列
				db.execSQL("ALTER TABLE " + SubjectDB.TB_NAME + " ADD COLUMN " + SubjectDB.db_timeCode);
				break;
			case 2: //需要添加timeCode列
				db.execSQL("ALTER TABLE " + SubjectDB.TB_NAME + " ADD COLUMN " + SubjectDB.db_video);
				db.execSQL("ALTER TABLE " + SubjectDB.TB_NAME + " ADD COLUMN " + SubjectDB.db_videoLocalPath);
				db.execSQL("ALTER TABLE " + SubjectDB.TB_NAME + " ADD COLUMN " + SubjectDB.db_videoSImg);
				db.execSQL("ALTER TABLE " + SubjectDB.TB_NAME + " ADD COLUMN " + SubjectDB.db_videoSImgLocal);
				db.execSQL("ALTER TABLE " + SubjectDB.TB_NAME + " ADD COLUMN " + SubjectDB.db_videoType);
				break;
			}
		}
	}

	/**
	 * 插入数据
	 * 如果需要插入的数据已经存在于数据库中，则更新数据库
	 * @param subjectData
	 * @return
	 */
	public int inser(SubjectData subjectData){
		int id = subjectData.getId();
		if(id >= 0){
			return update(id, subjectData);
		}else{
			return insert(subjectData);
		}
	}
	
	/**
	 * 插入数据
	 * @param subjectData
	 * @return
	 */
	private int insert(SubjectData subjectData) {
		SQLiteDatabase db = null;
		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(SubjectDB.db_title, subjectData.getTitle());
			contentValues.put(SubjectDB.db_code, subjectData.getCode());
			contentValues.put(SubjectDB.db_contentJson, subjectData.getContentJson());
			contentValues.put(SubjectDB.db_cid, subjectData.getCid());
			contentValues.put(SubjectDB.db_mid, subjectData.getMid());
			contentValues.put(SubjectDB.db_uploadState, subjectData.getUploadState());
			contentValues.put(SubjectDB.db_addTime, subjectData.getAddTime());
			contentValues.put(SubjectDB.db_titleCanModify, subjectData.getTitleCanModifyInteger());
			contentValues.put(SubjectDB.db_type, subjectData.getType());
			contentValues.put(SubjectDB.db_timeCode, subjectData.getUploadTimeCode());
			contentValues.put(SubjectDB.db_video, subjectData.getVideo());
			contentValues.put(SubjectDB.db_videoLocalPath, subjectData.getVideoLocalPath());
			contentValues.put(SubjectDB.db_videoSImg, subjectData.getVideoSImg());
			contentValues.put(SubjectDB.db_videoSImgLocal, subjectData.getVideoSImgLocal());
			contentValues.put(SubjectDB.db_videoType, subjectData.getVideoType());
			long id = -1;
			db = getWritableDatabase();
			id = db.insert(SubjectDB.TB_NAME, null, contentValues);
			subjectData.setId((int) id);
			return (int) id;
		} finally {
			close(null,db);
		}
	}
	
	/**
	 * 获取数据
	 * @param id
	 * @return
	 */
	public SubjectData selectById(int id){
		Cursor cursor = null;
		try{
			SubjectData data = new SubjectData();
			String selection = SubjectDB.db_id + "=" + id;
			cursor = getReadableDatabase().query(SubjectDB.TB_NAME, null, selection, null, null, null, null);
			// 查询并获得游标
			if(cursor.moveToFirst()){
				data.setId(cursor.getInt(cursor.getColumnIndex(SubjectDB.db_id)));
				data.setTitle(cursor.getString(cursor.getColumnIndex(SubjectDB.db_title)));
				data.setCode(cursor.getString(cursor.getColumnIndex(SubjectDB.db_code)));
				data.setContentJson(cursor.getString(cursor.getColumnIndex(SubjectDB.db_contentJson)));
				data.setCid(cursor.getString(cursor.getColumnIndex(SubjectDB.db_cid)));
				data.setMid(cursor.getString(cursor.getColumnIndex(SubjectDB.db_mid)));
				data.setUploadState(cursor.getInt(cursor.getColumnIndex(SubjectDB.db_uploadState)));
				data.setAddTime(cursor.getLong(cursor.getColumnIndex(SubjectDB.db_addTime)));
				data.setTitleCanModify(cursor.getInt(cursor.getColumnIndex(SubjectDB.db_titleCanModify)));
				data.setType(cursor.getString(cursor.getColumnIndex(SubjectDB.db_type)));
				data.setUploadTimeCode(cursor.getLong(cursor.getColumnIndex(SubjectDB.db_timeCode)));
				data.setVideo(cursor.getString(cursor.getColumnIndex(SubjectDB.db_video)));
				data.setVideoLocalPath(cursor.getString(cursor.getColumnIndex(SubjectDB.db_videoLocalPath)));
				data.setVideoSImg(cursor.getString(cursor.getColumnIndex(SubjectDB.db_videoSImg)));
				data.setVideoSImgLocal(cursor.getString(cursor.getColumnIndex(SubjectDB.db_videoSImgLocal)));
				data.setVideoType(cursor.getString(cursor.getColumnIndex(SubjectDB.db_videoType)));
			}
			return data;
		}finally{
			close(cursor, getReadableDatabase());
		}
	}
	
	public ArrayList<SubjectData> selectByCidState(String cid , int state){
		ArrayList<SubjectData> dataArray = new ArrayList<>();
		Cursor cursor = null;
		try{
			String selection = SubjectDB.db_cid + "=? and " + SubjectDB.db_uploadState + "=?";
			String[] selectionArgs = {cid , state + ""};
			cursor = getReadableDatabase().query(SubjectDB.TB_NAME, null, selection, selectionArgs, null, null, null);
			// 查询并获得游标
			if(cursor.moveToFirst()){
				do{
					SubjectData data = new SubjectData();
					data.setId(cursor.getInt(cursor.getColumnIndex(SubjectDB.db_id)));
					data.setTitle(cursor.getString(cursor.getColumnIndex(SubjectDB.db_title)));
					data.setCode(cursor.getString(cursor.getColumnIndex(SubjectDB.db_code)));
					data.setContentJson(cursor.getString(cursor.getColumnIndex(SubjectDB.db_contentJson)));
					data.setCid(cursor.getString(cursor.getColumnIndex(SubjectDB.db_cid)));
					data.setMid(cursor.getString(cursor.getColumnIndex(SubjectDB.db_mid)));
					data.setUploadState(cursor.getInt(cursor.getColumnIndex(SubjectDB.db_uploadState)));
					data.setAddTime(cursor.getLong(cursor.getColumnIndex(SubjectDB.db_addTime)));
					data.setTitleCanModify(cursor.getInt(cursor.getColumnIndex(SubjectDB.db_titleCanModify)));
					data.setType(cursor.getString(cursor.getColumnIndex(SubjectDB.db_type)));
					data.setUploadTimeCode(cursor.getLong(cursor.getColumnIndex(SubjectDB.db_timeCode)));
					data.setVideo(cursor.getString(cursor.getColumnIndex(SubjectDB.db_video)));
					data.setVideoLocalPath(cursor.getString(cursor.getColumnIndex(SubjectDB.db_videoLocalPath)));
					data.setVideoSImg(cursor.getString(cursor.getColumnIndex(SubjectDB.db_videoSImg)));
					data.setVideoSImgLocal(cursor.getString(cursor.getColumnIndex(SubjectDB.db_videoSImgLocal)));
					data.setVideoType(cursor.getString(cursor.getColumnIndex(SubjectDB.db_videoType)));
					dataArray.add(data);
				}while(cursor.moveToNext());
			}
			return dataArray;
		}finally{
			close(cursor, getReadableDatabase());
		}
	}
	
	/** 
	 * 获取最近一个草稿 
	 * 没有则返回 null
	 * @return
	 */
	public SubjectData getLastDraft(String cid,boolean isVideo){
		SubjectData subjectData = null;
		ArrayList<SubjectData> array = null;
		if(TextUtils.isEmpty(cid)){
			array = selectByState(SubjectData.UPLOAD_DRAF);
		}else{
			array = selectByCidState(cid , SubjectData.UPLOAD_DRAF);
		}
		if(array.size() > 0){
			for(int index = array.size() - 1 ; index >= 0 ; index --){
				SubjectData data = array.get(index);
				if(SubjectData.TYPE_UPLOAD.equals(data.getType())){
					if((isVideo && !TextUtils.isEmpty(data.getVideo()))
							|| (!isVideo && TextUtils.isEmpty(data.getVideo()))){
						subjectData = array.get(index);
						break;
					}
				}
			}
		}
		return subjectData;
	}
	
	public ArrayList<SubjectData> selectByState(int state){
		Cursor cursor = null;
		try{
			ArrayList<SubjectData> dataArray = new ArrayList<>();
			String selection = SubjectDB.db_uploadState + "=" + state;
			cursor = getReadableDatabase().query(SubjectDB.TB_NAME, null, selection, null, null, null, null);
			// 查询并获得游标
			if(cursor.moveToFirst()){
				do{
					SubjectData data = new SubjectData();
					data.setId(cursor.getInt(cursor.getColumnIndex(SubjectDB.db_id)));
					data.setTitle(cursor.getString(cursor.getColumnIndex(SubjectDB.db_title)));
					data.setCode(cursor.getString(cursor.getColumnIndex(SubjectDB.db_code)));
					data.setContentJson(cursor.getString(cursor.getColumnIndex(SubjectDB.db_contentJson)));
					data.setCid(cursor.getString(cursor.getColumnIndex(SubjectDB.db_cid)));
					data.setMid(cursor.getString(cursor.getColumnIndex(SubjectDB.db_mid)));
					data.setUploadState(cursor.getInt(cursor.getColumnIndex(SubjectDB.db_uploadState)));
					data.setAddTime(cursor.getLong(cursor.getColumnIndex(SubjectDB.db_addTime)));
					data.setTitleCanModify(cursor.getInt(cursor.getColumnIndex(SubjectDB.db_titleCanModify)));
					data.setType(cursor.getString(cursor.getColumnIndex(SubjectDB.db_type)));
					data.setUploadTimeCode(cursor.getLong(cursor.getColumnIndex(SubjectDB.db_timeCode)));
					data.setVideo(cursor.getString(cursor.getColumnIndex(SubjectDB.db_video)));
					data.setVideoLocalPath(cursor.getString(cursor.getColumnIndex(SubjectDB.db_videoLocalPath)));
					data.setVideoSImg(cursor.getString(cursor.getColumnIndex(SubjectDB.db_videoSImg)));
					data.setVideoSImgLocal(cursor.getString(cursor.getColumnIndex(SubjectDB.db_videoSImgLocal)));
					data.setVideoType(cursor.getString(cursor.getColumnIndex(SubjectDB.db_videoType)));
					dataArray.add(data);
				}while(cursor.moveToNext());
			}
			return dataArray;
		}finally{
			close(cursor, getReadableDatabase());
		}
	}
	
	/**
	 * 更新
	 * @param id
	 * @param state
	 * @return
	 */
	public boolean updateById(int id, int state) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(SubjectDB.db_uploadState, state);
		String selection = SubjectDB.db_id + "=" + id;
		int result = this.getReadableDatabase().update(SubjectDB.TB_NAME, contentValues,selection, null);
		this.getReadableDatabase().close();
		return result > 0;
	}
	
	public int update(int id,SubjectData subjectData){
		SQLiteDatabase db = null;
		ContentValues contentValues = new ContentValues();
		contentValues.put(SubjectDB.db_title, subjectData.getTitle());
		contentValues.put(SubjectDB.db_code, subjectData.getCode());
		contentValues.put(SubjectDB.db_contentJson, subjectData.getContentJson());
		contentValues.put(SubjectDB.db_cid, subjectData.getCid());
		contentValues.put(SubjectDB.db_mid, subjectData.getMid());
		contentValues.put(SubjectDB.db_uploadState, subjectData.getUploadState());
		contentValues.put(SubjectDB.db_addTime, subjectData.getAddTime());
		contentValues.put(SubjectDB.db_titleCanModify, subjectData.getTitleCanModifyInteger());
		contentValues.put(SubjectDB.db_type, subjectData.getType());
		contentValues.put(SubjectDB.db_timeCode,subjectData.getUploadTimeCode());
		contentValues.put(SubjectDB.db_video,subjectData.getVideo());
		contentValues.put(SubjectDB.db_videoLocalPath,subjectData.getVideo());
		contentValues.put(SubjectDB.db_videoSImg,subjectData.getVideoSImg());
		contentValues.put(SubjectDB.db_videoSImgLocal,subjectData.getVideoSImgLocal());
		contentValues.put(SubjectDB.db_videoType,subjectData.getVideoType());
		int row = -1;
		try{
			db = getWritableDatabase();
			row= db.update(SubjectDB.TB_NAME, contentValues,SubjectDB.db_id+"=?",new String[]{String.valueOf(id)});
		}catch(Exception e){
			
		}finally{
			close(null,db);
		}
		return row;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public boolean deleteById(int id) {
		String selection = SubjectDB.db_id + "=" + id;
		int i = this.getReadableDatabase().delete(SubjectDB.TB_NAME, selection,null);
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
		try{
			if(null!=db){
				db.endTransaction();
				db.close();
			}
		}catch (Exception e){

		}
	}
	
	public class SubjectDB{
		private static final String TB_NAME = "tb_subject";
		
		public static final String db_id = "id";
		public static final String db_title = "title";
		public static final String db_code = "code";
		public static final String db_contentJson = "contentJson";
		public static final String db_cid = "cid";
		public static final String db_mid = "mid";
		public static final String db_uploadState = "uploadState";
		public static final String db_addTime = "addTime";
		public static final String db_titleCanModify = "titleCanModify";
		public static final String db_type = "type";
		public static final String db_timeCode = "timeCode";
		public static final String db_video = "video";
		public static final String db_videoLocalPath = "videoLocalPath";
		public static final String db_videoSImg = "videoSImg";
		public static final String db_videoSImgLocal = "videoSImgLocal";
		public static final String db_videoType = "videoType";
	}
	
	private static final String CREATE_TABLE_SQL = "create table " + SubjectDB.TB_NAME + "("
			+ SubjectDB.db_id + " integer primary key autoincrement,"
			+ SubjectDB.db_code + " text,"
			+ SubjectDB.db_title + " text,"
			+ SubjectDB.db_contentJson + " text,"
			+ SubjectDB.db_uploadState + " integer,"
			+ SubjectDB.db_cid + " text,"
			+ SubjectDB.db_mid + " text,"
			+ SubjectDB.db_addTime + " long,"
			+ SubjectDB.db_titleCanModify + " integer,"
			+ SubjectDB.db_timeCode + "  long,"
			+ SubjectDB.db_video + " text,"
			+ SubjectDB.db_videoLocalPath + " text,"
			+ SubjectDB.db_videoSImg + " text,"
			+ SubjectDB.db_videoSImgLocal + " text,"
			+ SubjectDB.db_videoType + " text,"
			+ SubjectDB.db_type + " text)";
}
