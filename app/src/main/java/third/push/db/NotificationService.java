package third.push.db;

import java.util.ArrayList;
import java.util.List;

import third.push.model.XGNotification;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NotificationService {
	private DBOpenHelper dbOpenHelper;
	private static NotificationService instance = null;

	public NotificationService(Context context) {
		this.dbOpenHelper = new DBOpenHelper(context);
	}

	public synchronized static NotificationService getInstance(Context ctx) {
		if (null == instance) {
			instance = new NotificationService(ctx);
		}
		return instance;
	}

	public void save(XGNotification notification) {
		SQLiteDatabase db = null;
		try{
			db = dbOpenHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("msg_id", notification.getMsg_id());
			values.put("title", notification.getTitle());
			values.put("content", notification.getContent());
			values.put("activity", notification.getActivity());
			values.put("notificationActionType", notification.getNotificationActionType());
			values.put("update_time", notification.getUpdate_time());
			db.insert("notification", null, values);
		}catch(Exception e){
			if(db != null) db.close();
		}
	}

	public void delete(Integer id) {
		SQLiteDatabase db = null;
		try{
			db = dbOpenHelper.getWritableDatabase();
			db.delete("notification", "id=?", new String[] { id.toString() });
		}catch(Exception e){
			if(db != null) db.close();
		}
	}

	public void deleteAll() {
		SQLiteDatabase db = null;
		try{
			db = dbOpenHelper.getWritableDatabase();
			db.delete("notification", "", null);
		}catch(Exception e){
			if(db != null) db.close();
		}
	}

	public void update(XGNotification notification) {
		SQLiteDatabase db = null;
		try{
			db = dbOpenHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("msg_id", notification.getMsg_id());
			values.put("title", notification.getTitle());
			values.put("content", notification.getContent());
			values.put("activity", notification.getActivity());
			values.put("notificationActionType", notification.getNotificationActionType());
			values.put("update_time", notification.getUpdate_time());
			db.update("notification", values, "id=?", new String[] { notification
				.getId().toString() });
		}catch(Exception e){
			if(db != null) db.close();
		}	
	}

	public XGNotification find(Integer id) {
		Cursor cursor = null;
		SQLiteDatabase db = null;
		try{
			db = dbOpenHelper.getReadableDatabase();
			cursor = db
					.query("notification",
							new String[] { "id,msg_id,title,content,activity,notificationActionType,update_time" },
							"id=?", new String[] { id.toString() }, null, null,
							null, "1");
		}catch(Exception e){
			if(cursor != null )cursor.close();
			if(db != null )db.close();
			
		}
		try {
			if (cursor.moveToFirst()) {
				return new XGNotification(cursor.getInt(cursor
						.getColumnIndex("id")), cursor.getLong(cursor
						.getColumnIndex("msg_id")), cursor.getString(cursor
						.getColumnIndex("title")), cursor.getString(cursor
						.getColumnIndex("content")), cursor.getString(cursor
						.getColumnIndex("activity")), cursor.getInt(cursor
						.getColumnIndex("notificationActionType")), cursor.getString(cursor
						.getColumnIndex("update_time")));
			}
			return null;
		} finally {
			if(cursor != null )cursor.close();
		}
	}

	public List<XGNotification> getScrollData(int currentPage, int lineSize,
			String msg_id) {
		String firstResult = String.valueOf((currentPage - 1) * lineSize);
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = dbOpenHelper.getReadableDatabase();
			if (msg_id == null || "".equals(msg_id)) {
				cursor = db
						.query("notification",
								new String[] { "id,msg_id,title,content,activity,notificationActionType,update_time" },
								null, null, null, null, "update_time DESC",
								firstResult + "," + lineSize);
			} else {
				cursor = db
						.query("notification",
								new String[] { "id,msg_id,title,content,activity,notificationActionType,update_time" },
								"msg_id like ?", new String[] { msg_id + "%" },
								null, null, "update_time DESC", firstResult
										+ "," + lineSize);
			}
			List<XGNotification> notifications = new ArrayList<XGNotification>();
			while (cursor.moveToNext()) {
				notifications.add(new XGNotification(cursor.getInt(cursor
						.getColumnIndex("id")), cursor.getLong(cursor
						.getColumnIndex("msg_id")), cursor.getString(cursor
						.getColumnIndex("title")), cursor.getString(cursor
						.getColumnIndex("content")), cursor.getString(cursor
						.getColumnIndex("activity")), cursor.getInt(cursor
						.getColumnIndex("notificationActionType")), cursor.getString(cursor
						.getColumnIndex("update_time"))));
			}
			return notifications;
		} finally {
			if(cursor != null)cursor.close();
			if(db != null )db.close();
		}
	}

	public int getCount() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = dbOpenHelper.getReadableDatabase();
			cursor = db.rawQuery("select count(*) from notification", null);
			cursor.moveToFirst();
			return cursor.getInt(0);
		} finally {
			if(cursor != null)cursor.close();
			if(db != null)db.close();
		}
	}
}
