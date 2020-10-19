package at.jodlidev.metawear.study.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;

/**
 * Created by JodliDev on 18.09.2019.
 */
public class LogMessages {
	public static final String TABLE = "logs";
	public static final String KEY_ID = "_id";
	public static final String KEY_BOARD = "board";
	public static final String KEY_TEXT = "log_text";
	public static final String KEY_TIMESTAMP = "log_timestamp";
	
	public static final String[] COLUMNS = {
			KEY_ID,
			KEY_BOARD,
			KEY_TEXT,
			KEY_TIMESTAMP
	};
	
	public static final int MAX_SAVED_ERRORS = 500;
	
	long id;
	public String text;
	long board;
	public long timestamp;
	
	public LogMessages(Cursor c) {
		id = c.getLong(0);
		board = c.getLong(1);
		text = c.getString(2);
		timestamp = c.getLong(3);
	}
	
	
	public static void save(SQLiteDatabase db, long board_id, String m) {
		ContentValues values = new ContentValues();
		values.put(KEY_BOARD, board_id);
		values.put(KEY_TEXT, m);
		values.put(KEY_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
		db.insert(TABLE, null, values);
	}
}
