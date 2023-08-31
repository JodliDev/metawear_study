package at.jodlidev.metawear.study.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Board_data {
	public static final String TABLE = "boards";
	public static final String KEY_ID = "id";
	public static final String KEY_MAC = "mac";
	public static final String KEY_USER_NOTES = "user_notes";
	
	public static final String[] COLUMNS = {
			KEY_ID,
			KEY_MAC,
			KEY_USER_NOTES,
			Battery.KEY_LOG_IDENTIFIER,
			Battery.KEY_ROUTE_ID,
			Battery.KEY_REACT_ROUTE_ID,
			Battery.KEY_BATTERY_FOR_SWITCH,
			Battery.KEY_BATTERY_FOR_PING,
			Battery.KEY_LOW_THRESHOLD,
			Battery.KEY_LOW_LED,
			Battery.KEY_LOW_COLOR,
			Battery.KEY_LOW_VIBRATION,
			Battery.KEY_LOW_VIBRATION_STRENGTH,
			Battery.KEY_LOW_VIBRATION_MS
	};
	
	public long sql_id;
	public String mac;
	public String userNotes;
	
	public Battery battery;
	
	public Board_data(Cursor c) {
		sql_id = c.getLong(0);
		mac = c.getString(1);
		userNotes = c.getString(2);
		battery = new Battery(c);
	}
	public Board_data(SQLiteDatabase db, String mac) {
		ContentValues values = new ContentValues();
		values.put(KEY_MAC, mac);
	
		this.sql_id = db.insert(TABLE, null, values);
		this.mac = mac;
		this.userNotes = "";
		this.battery = new Battery();
	}
	
	public void set_userNotes(SQLiteDatabase db, String notes) {
		userNotes = notes;
		
		ContentValues values = new ContentValues();
		values.put(Board_data.KEY_USER_NOTES, notes);
		
		db.update(Board_data.TABLE, values, Board_data.KEY_ID + " = ?", new String[]{Long.toString(sql_id)});
	}
	
	public void save_battery(SQLiteDatabase db, Battery battery) {
		this.battery = battery;
		battery.save_to_board(db, this);
	}
}
