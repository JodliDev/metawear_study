package at.jodlidev.metawear.study.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by JodliDev on 13.05.2019.
 */
public class FeedbackMacro {
	public final static byte NO_MACRO = -1;
	
	public static final String TABLE = "feedback_macro";
	public static final String KEY_ID = "_id";
	public static final String KEY_BOARD = "board_id";
	public static final String KEY_MACRO_ID = "macro_id";
	public static final String KEY_IDENTIFIER = "identifier";
	
	
	private  long board_id;
	public byte macro_id;
	public String identifier;
	
	public FeedbackMacro(Board_data board_data) {
		board_id = board_data.sql_id;
	}
	
	public FeedbackMacro(Board_data board_data, String _identifier) {
		board_id = board_data.sql_id;
		identifier = _identifier;
	}
	
	public void save(SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(KEY_BOARD, board_id);
		values.put(KEY_MACRO_ID, macro_id);
		values.put(KEY_IDENTIFIER, identifier);
		
		db.insert(TABLE, null, values);
	}
	
	public byte get_macro_id(SQLiteDatabase db) {
		Cursor c = db.query(TABLE,
				new String[] {KEY_MACRO_ID},
				KEY_BOARD + "= ? AND "+ KEY_IDENTIFIER + "=? ",
				new String[] {Long.toString(board_id), identifier},
				null,
				null,
				null,
				"1");
		
		byte r = c.moveToFirst() ? (byte) c.getInt(0) : NO_MACRO;
		
		c.close();
		return r;
	}
}
