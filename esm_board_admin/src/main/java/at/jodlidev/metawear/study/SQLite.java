package at.jodlidev.metawear.study;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import at.jodlidev.metawear.study.data.Battery;
import at.jodlidev.metawear.study.data.Bing;
import at.jodlidev.metawear.study.data.Board_data;
import at.jodlidev.metawear.study.data.BootMacro;
import at.jodlidev.metawear.study.data.ChargeFeedback;
import at.jodlidev.metawear.study.data.FeedbackMacro;
import at.jodlidev.metawear.study.data.LogMessages;
import at.jodlidev.metawear.study.data.Password;
import at.jodlidev.metawear.study.data.RandomTimer;
import at.jodlidev.metawear.study.data.Repeat;
import at.jodlidev.metawear.study.data.Switch_route;

/**
 * Created by JodliDev on 02.04.18.
 */

public class SQLite extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 26;
	static final String DATABASE_NAME = "board_data";
	
	public static class Serialized_Boards {
		public static final String TABLE = "serialized_boards";
		public static final String KEY_BOARD = "board";
		public static final String KEY_SERIALIZED = "serialized";
	}
	
	public SQLite(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Password.init(getWritableDatabase());
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Password.TABLE + " (" +
				   Password.KEY_PASSWORD + " INTEGER)");
		
		db.execSQL("CREATE TABLE "+Serialized_Boards.TABLE+" (" +
				   Serialized_Boards.KEY_BOARD+" INTEGER PRIMARY KEY, " +
				   Serialized_Boards.KEY_SERIALIZED+" BLOB)");
		
		db.execSQL("CREATE TABLE " + LogMessages.TABLE + " (" +
				   LogMessages.KEY_ID + " INTEGER PRIMARY KEY, " +
				   LogMessages.KEY_BOARD + " INTEGER, " +
				   LogMessages.KEY_TEXT + " TEXT, " +
				   LogMessages.KEY_TIMESTAMP + " INTEGER, " +
				   "FOREIGN KEY("+LogMessages.KEY_BOARD+") REFERENCES boards("+Board_data.KEY_ID+"))");
		
		db.execSQL("CREATE TABLE " + Board_data.TABLE + " (" +
				   Board_data.KEY_ID + " INTEGER PRIMARY KEY, " +
				   Board_data.KEY_MAC + " TEXT, " +
				   Board_data.KEY_USER_NOTES + " TEXT, " +
				   Battery.KEY_LOG_IDENTIFIER + " TEXT, " +
				   Battery.KEY_BATTERY_FOR_SWITCH + " INTEGER, " +
				   Battery.KEY_ROUTE_ID + " INTEGER DEFAULT -1, " +
				   Battery.KEY_REACT_ROUTE_ID + " INTEGER DEFAULT -1, " +
				   Battery.KEY_BATTERY_FOR_BING + " INTEGER, " +
				   Battery.KEY_LOW_THRESHOLD + " INTEGER, " +
				   Battery.KEY_LOW_LED + " INTEGER, " +
				   Battery.KEY_LOW_COLOR + " TEXT, " +
				   Battery.KEY_LOW_VIBRATION + " INTEGER, " +
				   Battery.KEY_LOW_VIBRATION_STRENGTH + " INTEGER, " +
				   Battery.KEY_LOW_VIBRATION_MS + " INTEGER)");
		
		db.execSQL("CREATE TABLE "+Switch_route.TABLE+" (" +
				   Switch_route.KEY_BOARD+" INTEGER PRIMARY KEY, " +
				   Switch_route.KEY_SWITCH_PRE_COUNT_ID+" INTEGER, " +
				   Switch_route.KEY_SWITCH_PRE_STATE_ID+" INTEGER, " +
				   Switch_route.KEY_SWITCH_POST_ID+" INTEGER, " +
				   Switch_route.KEY_ACC_ID+" INTEGER, " +
				   Switch_route.KEY_TIMER_ID+" INTEGER, " +
				   Switch_route.KEY_BUFFER_ID+" INTEGER, " +
				   Switch_route.KEY_SWITCH_IDENTIFIER+" TEXT, " +
				   Switch_route.KEY_SWITCH_LOG_TIMESTAMP+" INTEGER, " +
				   Switch_route.KEY_ACC_IDENTIFIER+" TEXT, " +
				   Switch_route.KEY_ACC_LOG_TIMESTAMP+" INTEGER, " +
				   Switch_route.KEY_LOG_SWITCH_DATA+" INTEGER, " +
				   Switch_route.KEY_LOG_ACC_DATA+" INTEGER, " +
				   Switch_route.KEY_LED_BEHAVIOR+" INTEGER, " +
				   Switch_route.KEY_ACC_AXIS_VISUALISED+" INTEGER, " +
				   Switch_route.KEY_COLOR+" TEXT, " +
				   Switch_route.KEY_VIBRATION+" INTEGER, " +
				   Switch_route.KEY_VIBRATION_STRENGTH+" INTEGER, " +
				   Switch_route.KEY_VIBRATION_MS+" INTEGER, " +
				   Switch_route.KEY_LOG_NUMBER_LED+" INTEGER, " +
				   Switch_route.KEY_LOG_NUMBER_COLOR+" TEXT, " +
				   Switch_route.KEY_LOG_NUMBER_VIBRATION+" INTEGER, " +
				   Switch_route.KEY_LOG_NUMBER_VIBRATION_STRENGTH+" INTEGER, " +
				   Switch_route.KEY_LOG_NUMBER_VIBRATION_MS+" INTEGER, " +
				   Switch_route.KEY_RESTORE_ON_BOOT+" INTEGER, " +
				   Switch_route.KEY_EXISTS_ON_BOARD+" INTEGER DEFAULT 1, " +
				   "FOREIGN KEY("+Switch_route.KEY_BOARD+") REFERENCES boards("+Board_data.KEY_ID+"))");
		
		db.execSQL("CREATE TABLE " + Bing.TABLE + " (" +
				   Bing.KEY_ID+" INTEGER PRIMARY KEY, " +
				   Bing.KEY_BOARD+" INTEGER, " +
				   Bing.KEY_HOUR+" INTEGER, " +
				   Bing.KEY_MIN+" INTEGER, " +
				   Bing.KEY_REMOVE_WAIT_TIMESTAMP+" INTEGER, " +
				   Bing.KEY_RANDOM+" INTEGER, " +
				   Bing.KEY_REPEAT+" INTEGER, " +
				   Bing.KEY_BATTERY_LOGGING+" INTEGER, " +
				   Bing.KEY_LED+" INTEGER, " +
				   Bing.KEY_COLOR+" TEXT, " +
				   Bing.KEY_VIBRATION+" INTEGER, " +
				   Bing.KEY_VIBRATION_STRENGTH+" INTEGER, " +
				   Bing.KEY_VIBRATION_MS+" INTEGER, " +
				   Bing.KEY_ID_WAIT+" INTEGER, " +
				   Bing.KEY_ID_LOOP+" INTEGER, " +
				   Bing.KEY_EXISTS_ON_BOARD+" INTEGER DEFAULT 1, " +
				   "FOREIGN KEY("+Bing.KEY_BOARD+") REFERENCES boards("+Board_data.KEY_ID+"))");
		
		db.execSQL("CREATE TABLE "+Repeat.TABLE+" (" +
				   Repeat.KEY_BOARD+" INTEGER PRIMARY KEY, " +
				   Repeat.KEY_MIN+" INTEGER, " +
				   Repeat.KEY_REPEAT_NUM+" INTEGER, " +
				   Repeat.KEY_BATTERY_LOGGING+" INTEGER, " +
				   Repeat.KEY_VIBRATION+" INTEGER, " +
				   Repeat.KEY_VIBRATION_STRENGTH+" INTEGER, " +
				   Repeat.KEY_VIBRATION_MS+" INTEGER, " +
				   Repeat.KEY_LED+" INTEGER, " +
				   Repeat.KEY_COLOR+" TEXT, " +
				   Repeat.KEY_ID_TIMER+" INTEGER, " +
				   Repeat.KEY_ID_SWITCH+" INTEGER, " +
				   Repeat.KEY_EXISTS_ON_BOARD+" INTEGER DEFAULT 1, " +
				   "FOREIGN KEY("+Repeat.KEY_BOARD+") REFERENCES boards("+Board_data.KEY_ID+"))");
		
		db.execSQL("CREATE TABLE "+RandomTimer.TABLE+" (" +
				   RandomTimer.KEY_ID+" INTEGER PRIMARY KEY, " +
				   RandomTimer.KEY_BOARD+" INTEGER, " +
				   RandomTimer.KEY_MIN+" INTEGER, " +
				   RandomTimer.KEY_TIMEFRAMES_NUM+" INTEGER, " +
				   RandomTimer.KEY_VIBRATION+" INTEGER, " +
				   RandomTimer.KEY_VIBRATION_STRENGTH+" INTEGER, " +
				   RandomTimer.KEY_VIBRATION_MS+" INTEGER, " +
				   RandomTimer.KEY_REPEAT+" INTEGER, " +
				   RandomTimer.KEY_BATTERY_LOGGING+" INTEGER, " +
				   RandomTimer.KEY_LED+" INTEGER, " +
				   RandomTimer.KEY_COLOR+" TEXT, " +
				   RandomTimer.KEY_ID_TIMER+" INTEGER, " +
				   RandomTimer.KEY_ID_GYRO+" INTEGER, " +
				   RandomTimer.KEY_EXISTS_ON_BOARD+" INTEGER DEFAULT 1, " +
				   "FOREIGN KEY("+RandomTimer.KEY_BOARD+") REFERENCES boards("+Board_data.KEY_ID+"))");
		
		db.execSQL("CREATE TABLE " + BootMacro.TABLE + " (" +
				   BootMacro.KEY_ID + " INTEGER PRIMARY KEY, " +
				   BootMacro.KEY_BOARD + " INTEGER, " +
				   BootMacro.KEY_REPEAT_FEEDBACK + " INTEGER, " +
				   BootMacro.KEY_HOUR + " INTEGER, " +
				   BootMacro.KEY_MIN + " INTEGER, " +
				   BootMacro.KEY_LED + " INTEGER, " +
				   BootMacro.KEY_LED_TYPE + " INTEGER, " +
				   BootMacro.KEY_COLOR + " TEXT, " +
				   BootMacro.KEY_VIBRATION + " INTEGER, " +
				   BootMacro.KEY_VIBRATION_STRENGTH + " INTEGER, " +
				   BootMacro.KEY_VIBRATION_MS + " INTEGER, " +
				   "FOREIGN KEY(" + BootMacro.KEY_BOARD + ") REFERENCES boards(" + Board_data.KEY_ID + "))");
		
		
		db.execSQL("CREATE TABLE " + FeedbackMacro.TABLE + " (" +
				   FeedbackMacro.KEY_ID + " INTEGER PRIMARY KEY, " +
				   FeedbackMacro.KEY_BOARD + " INTEGER, " +
				   FeedbackMacro.KEY_MACRO_ID + " INTEGER, " +
				   FeedbackMacro.KEY_IDENTIFIER + " TEXT, " +
				   "FOREIGN KEY(" + FeedbackMacro.KEY_BOARD + ") REFERENCES boards(" + Board_data.KEY_ID + "))");
		
		db.execSQL("CREATE TABLE " + ChargeFeedback.TABLE + " (" +
				   ChargeFeedback.KEY_ID + " INTEGER, " +
				   ChargeFeedback.KEY_BOARD + " INTEGER, " +
				   ChargeFeedback.KEY_ROUTE_ID + " INTEGER, " +
				   ChargeFeedback.KEY_LED + " INTEGER, " +
				   ChargeFeedback.KEY_COLOR + " TEXT, " +
				   ChargeFeedback.KEY_VIBRATION + " INTEGER, " +
				   ChargeFeedback.KEY_VIBRATION_MS + " INTEGER, " +
				   ChargeFeedback.KEY_VIBRATION_STRENGTH + " INTEGER, " +
				   ChargeFeedback.KEY_EXISTS_ON_BOARD+" INTEGER DEFAULT 1, " +
				   "FOREIGN KEY(" + ChargeFeedback.KEY_BOARD + ") REFERENCES boards(" + Board_data.KEY_ID + "))");
		
		db.execSQL("CREATE TRIGGER limit_error_rows AFTER INSERT ON "+LogMessages.TABLE+
				   " WHEN (SELECT COUNT(*) FROM "+LogMessages.TABLE+") > "+LogMessages.MAX_SAVED_ERRORS+
				   " BEGIN "+
				   " DELETE FROM "+LogMessages.TABLE+ " WHERE "+LogMessages.KEY_ID+
				   " = (SELECT MIN("+LogMessages.KEY_ID+") FROM "+LogMessages.TABLE+");"+
				   " END");
		
//		Password.get_or_create(db);
	}
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("sql", "downgrade from "+oldVersion+" to "+newVersion);
//		reset(db);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("sql", "upgrading from "+oldVersion+" to "+newVersion);
		switch(oldVersion) {
			default:
				reset(db);
				break;
			case 20:
				db.execSQL("CREATE TABLE "+Password.TABLE+" (" +
						   Password.KEY_PASSWORD+" INTEGER)");
				Password.init(db);
			case 21:
				db.execSQL("CREATE TABLE " + LogMessages.TABLE + " (" +
						   LogMessages.KEY_ID + " INTEGER PRIMARY KEY, " +
						   LogMessages.KEY_BOARD + " INTEGER, " +
						   LogMessages.KEY_TEXT + " TEXT, " +
						   LogMessages.KEY_TIMESTAMP + " INTEGER, " +
						   "FOREIGN KEY("+LogMessages.KEY_BOARD+") REFERENCES boards("+Board_data.KEY_ID+"))");
				
				db.execSQL("ALTER TABLE " + Switch_route.TABLE + " ADD COLUMN " + Switch_route.KEY_RESTORE_ON_BOOT + " INTEGER;");
				
			case 22:
				db.execSQL("CREATE TRIGGER limit_error_rows AFTER INSERT ON "+LogMessages.TABLE+
						   " WHEN (SELECT COUNT(*) FROM "+LogMessages.TABLE+") > "+LogMessages.MAX_SAVED_ERRORS+
						   " BEGIN "+
						   " DELETE FROM "+LogMessages.TABLE+ " WHERE "+LogMessages.KEY_ID+
						   " = (SELECT MIN("+LogMessages.KEY_ID+") FROM "+LogMessages.TABLE+");"+
						   " END");
				
				db.execSQL("ALTER TABLE " + Repeat.TABLE + " ADD COLUMN " + Repeat.KEY_ID_SWITCH + " INTEGER;");
				db.execSQL("ALTER TABLE " + Board_data.TABLE + " ADD COLUMN " + Battery.KEY_REACT_ROUTE_ID + " INTEGER;");
			case 23:
				db.execSQL("CREATE TABLE " + ChargeFeedback.TABLE + " (" +
						   ChargeFeedback.KEY_ID + " INTEGER, " +
						   ChargeFeedback.KEY_BOARD + " INTEGER, " +
						   ChargeFeedback.KEY_ROUTE_ID + " INTEGER, " +
						   ChargeFeedback.KEY_LED + " INTEGER, " +
						   ChargeFeedback.KEY_COLOR + " TEXT, " +
						   ChargeFeedback.KEY_VIBRATION + " INTEGER, " +
						   ChargeFeedback.KEY_VIBRATION_MS + " INTEGER, " +
						   ChargeFeedback.KEY_VIBRATION_STRENGTH + " INTEGER, " +
						   "FOREIGN KEY(" + ChargeFeedback.KEY_BOARD + ") REFERENCES boards(" + Board_data.KEY_ID + "))");
			case 24:
				db.execSQL("ALTER TABLE " + Switch_route.TABLE + " ADD COLUMN " + Switch_route.KEY_SWITCH_LOG_TIMESTAMP + " INTEGER;");
				db.execSQL("ALTER TABLE " + Switch_route.TABLE + " ADD COLUMN " + Switch_route.KEY_ACC_LOG_TIMESTAMP + " INTEGER;");
				db.execSQL("ALTER TABLE " + Switch_route.TABLE + " ADD COLUMN " + Switch_route.KEY_EXISTS_ON_BOARD + " INTEGER DEFAULT 1;");
				db.execSQL("ALTER TABLE " + Bing.TABLE + " ADD COLUMN " + Bing.KEY_EXISTS_ON_BOARD + " INTEGER DEFAULT 1;");
				db.execSQL("ALTER TABLE " + Repeat.TABLE + " ADD COLUMN " + Repeat.KEY_EXISTS_ON_BOARD + " INTEGER DEFAULT 1;");
				db.execSQL("ALTER TABLE " + RandomTimer.TABLE + " ADD COLUMN " + RandomTimer.KEY_EXISTS_ON_BOARD + " INTEGER DEFAULT 1;");
				db.execSQL("ALTER TABLE " + ChargeFeedback.TABLE + " ADD COLUMN " + ChargeFeedback.KEY_EXISTS_ON_BOARD + " INTEGER DEFAULT 1;");
			case 25:
				db.execSQL("ALTER TABLE " + Board_data.TABLE + " ADD COLUMN " + Board_data.KEY_USER_NOTES + " TEXT;");
				//RandomTimer.KEY_HOUR and Repeat.KEY_HOUR were also removed. But we can just leave it
		}
	}
	private void reset(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS "+Serialized_Boards.TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+Board_data.TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+Switch_route.TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+Bing.TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+Repeat.TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+RandomTimer.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + BootMacro.TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + FeedbackMacro.TABLE);
		this.onCreate(db);
	}
	
	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}
	
	//*****
	//Log
	//*****
	
	void save_log(long board_id, String m) {
		LogMessages.save(getWritableDatabase(), board_id, m);
	}
	List<LogMessages> get_logs(long board_id) {
		Cursor c = getWritableDatabase().query(LogMessages.TABLE,
				LogMessages.COLUMNS,
				LogMessages.KEY_BOARD+" = ?",
				new String[] {Long.toString(board_id)},
				null,
				null,
				LogMessages.KEY_TIMESTAMP,
				null);
		
		List<LogMessages> r = new ArrayList<>();
		if(c.moveToFirst()) {
			do {
				r.add(new LogMessages(c));
			}
			while(c.moveToNext());
		}
		c.close();
		return r;
	}
	
	//*****
	//Board
	//*****
	void save_battery(Board_data board_data, Battery battery) {
		board_data.save_battery(getWritableDatabase(), battery);
	}
	
	Board_data get_board_data(String mac) {
		SQLiteDatabase db = getWritableDatabase();

		Cursor c = db.query(Board_data.TABLE,
				Board_data.COLUMNS,
				Board_data.KEY_MAC+" = ?",
				new String[] {mac},
				null,
				null,
				null,
				"1");

		Board_data r = c.moveToFirst() ? new Board_data(c) : new Board_data(db, mac);

		c.close();
		return r;
	}
	
	void reset_board(Board_data board_data) {
		String board_id = Long.toString(board_data.sql_id);
		SQLiteDatabase db = getWritableDatabase();
		db.delete(Switch_route.TABLE, Switch_route.KEY_BOARD+" = ?", new String[] {board_id});
		db.delete(Bing.TABLE, Bing.KEY_BOARD+" = ?", new String[] {board_id});
		db.delete(Repeat.TABLE, Repeat.KEY_BOARD+" = ?", new String[] {board_id});
		db.delete(RandomTimer.TABLE, RandomTimer.KEY_BOARD+" = ?", new String[] {board_id});
		db.delete(Serialized_Boards.TABLE, RandomTimer.KEY_BOARD+" = ?", new String[] {board_id});
		db.delete(BootMacro.TABLE, BootMacro.KEY_BOARD+" = ?", new String[] {board_id});
		db.delete(FeedbackMacro.TABLE, FeedbackMacro.KEY_BOARD+" = ?", new String[] {board_id});
		db.delete(ChargeFeedback.TABLE, ChargeFeedback.KEY_BOARD+" = ?", new String[] {board_id});
		
		board_data.save_battery(db, new Battery());
//		remove_serialized(board_data);
	}
	
	void insert_serialized(Board_data board_data, byte[] data) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(Serialized_Boards.TABLE, RandomTimer.KEY_BOARD+" = ?", new String[] {Long.toString(board_data.sql_id)});
		
		ContentValues values = new ContentValues();
		values.put(Serialized_Boards.KEY_BOARD, board_data.sql_id);
		values.put(Serialized_Boards.KEY_SERIALIZED, data);
		db.insert(Serialized_Boards.TABLE, null, values);
	}
	byte[] load_serialized(Board_data board_data) {
		Cursor c = getWritableDatabase().query(Serialized_Boards.TABLE,
				new String[] {Serialized_Boards.KEY_SERIALIZED},
				Serialized_Boards.KEY_BOARD+" = ?",
				new String[] {Long.toString(board_data.sql_id)},
				null,
				null,
				null,
				"1");
		
		byte[] r = null;
		if(c.moveToFirst()) {
			r = c.getBlob(0);
		}
		c.close();
		
		return r;
	}
	
	void remove_serialized(Board_data board_data) {
		getWritableDatabase().delete(Serialized_Boards.TABLE, Serialized_Boards.KEY_BOARD+" = ?", new String[] {Long.toString(board_data.sql_id)});
	}
	
	String export_config(Board_data board_data) {
		JSONObject r = new JSONObject();
		try {
			//Order:
			//Battery first because it is needed everywhere
			//repeat before switch and random
			//bings after repeat and random
			//Macro after switch and battery
			
			//battery
			r.put("battery", board_data.battery.export());
			
			//repeat
			Repeat repeat = get_repeat(board_data);
			r.put(Repeat.TABLE, repeat != null ? repeat.export() : null);
			
			//random
			RandomTimer random = get_randomTimer(board_data);
			r.put(RandomTimer.TABLE, random != null ? random.export() : null);
			
			//Switch
			Switch_route switch_route = get_switch_route(board_data);
			r.put(Switch_route.TABLE, switch_route != null ? switch_route.export() : null);
			
			//bings
			List<Bing> bings = get_bings(board_data);
			
			JSONArray bing_array = new JSONArray();
			for(Bing bing : bings) {
				bing_array.put(bing.export());
			}
			r.put(Bing.TABLE, bing_array);
			
			//bootMacro
			BootMacro bootMacro = get_bootMacro(board_data);
			r.put(BootMacro.TABLE, bootMacro != null ? bootMacro.export() : null);
			
			//chargeFeedback
			ChargeFeedback chargeFeedback = get_chargeFeedback(board_data);
			r.put(ChargeFeedback.TABLE, chargeFeedback != null ? chargeFeedback.export() : null);
			
		}
		catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return r.toString();
	}
	
	void set_board_to_rebooted(Board_data board_data) {
		SQLiteDatabase db = getWritableDatabase();
		
		Switch_route switch_route = get_switch_route(board_data);
		if(switch_route != null && !switch_route.restore_on_boot) {
			switch_route.set_to_not_exist(board_data, db);
		}
		
		List<Bing> bings = get_bings(board_data);
		if(bings.size() != 0) {
			for(Bing bing : bings) {
				bing.set_to_not_exist(board_data, db);
				}
		}
		
		Repeat repeat = get_repeat(board_data);
		if(repeat != null)
			repeat.set_to_not_exist(board_data, db);
		
		RandomTimer randomTimer = get_randomTimer(board_data);
		if(randomTimer != null)
			randomTimer.set_to_not_exist(board_data, db);
		
		ChargeFeedback chargeFeedback = get_chargeFeedback(board_data);
		if(chargeFeedback != null)
			chargeFeedback.set_to_not_exist(board_data, db);
	}
	
	void set_userNotes(Board_data board_data, String notes) {
		board_data.set_userNotes(getWritableDatabase(), notes);
	}
	
	//*****
	//Charge status
	//*****
	ChargeFeedback get_chargeFeedback(Board_data board_data) {
		Cursor c = getReadableDatabase().query(ChargeFeedback.TABLE,
				ChargeFeedback.COLUMNS,
				ChargeFeedback.KEY_BOARD+" = ?",
				new String[] {Long.toString(board_data.sql_id)},
				null,
				null,
				null,
				"1");
		
		ChargeFeedback r = c.moveToFirst() ? new ChargeFeedback(c) : null;
		c.close();
		return r;
	}
	
	void remove_chargeFeedback(Board_data board_data) {
		getWritableDatabase().delete(ChargeFeedback.TABLE, ChargeFeedback.KEY_BOARD+" = ?", new String[] {Long.toString(board_data.sql_id)});
	}
	
	//*****
	//Feedback-Macro
	//*****
	byte get_feedback_macro_id(FeedbackMacro f) {
		return f.get_macro_id(getReadableDatabase());
	}
	void save_feedback_macro(FeedbackMacro f) {
		f.save(getWritableDatabase());
	}
	boolean has_feedback_macro(Board_data board_data) {
		Cursor c = getWritableDatabase().query(FeedbackMacro.TABLE,
				new String[] {FeedbackMacro.KEY_ID},
				FeedbackMacro.KEY_BOARD + " = ?",
				new String[] {Long.toString(board_data.sql_id)},
				null,
				null,
				null,
				"1");
		
		boolean r = c.moveToFirst();
		c.close();
		return r;
	}
	void remove_feedbackMacros(Board_data board_data) {
		getWritableDatabase().delete(FeedbackMacro.TABLE, FeedbackMacro.KEY_BOARD + " = ?", new String[] {Long.toString(board_data.sql_id)});
	}
	
	//*****
	//Boot-Macro
	//*****
	void save_bootMacro(Board_data board_data, BootMacro bootMacro) {
		bootMacro.save(board_data, getWritableDatabase());
	}
	BootMacro get_bootMacro(Board_data board_data) {
		
		Cursor c = getWritableDatabase().query(BootMacro.TABLE,
				BootMacro.COLUMNS,
				BootMacro.KEY_BOARD + " = ?",
				new String[] {Long.toString(board_data.sql_id)},
				null,
				null,
				null,
				null);
		
		BootMacro r = c.moveToFirst() ? new BootMacro(c) : null;
		c.close();
		return r;
	}
	boolean has_bootMacro(Board_data board_data) {
		Cursor c = getWritableDatabase().query(BootMacro.TABLE,
				new String[] {BootMacro.KEY_ID},
				BootMacro.KEY_BOARD + " = ?",
				new String[] {Long.toString(board_data.sql_id)},
				null,
				null,
				null,
				"1");
		
		boolean r = c.moveToFirst();
		c.close();
		return r;
	}
	void remove_bootMacros(Board_data board_data) {
		getWritableDatabase().delete(BootMacro.TABLE, BootMacro.KEY_BOARD + " = ?", new String[] {Long.toString(board_data.sql_id)});
	}
	
	//*****
	//Switch Routes
	//*****
	void remove_switch_routes(Board_data board_data) {
		getWritableDatabase().delete(Switch_route.TABLE, Switch_route.KEY_BOARD+" = ?", new String[] {Long.toString(board_data.sql_id)});
	}
	
	boolean has_switch_routes(Board_data board_data) {
		Cursor c = getWritableDatabase().query(Switch_route.TABLE,
				new String[] {Switch_route.KEY_BOARD},
				Switch_route.KEY_BOARD+" = ?",
				new String[] {Long.toString(board_data.sql_id)},
				null,
				null,
				null,
				"1");
		
		boolean r = c.moveToFirst();
		c.close();
		return r;
	}
	
	void save_switch_route(Board_data board_data, Switch_route route) {
		route.save(board_data, getWritableDatabase());
	}
	Switch_route get_switch_route(Board_data board_data) {
		Cursor c = getReadableDatabase().query(Switch_route.TABLE,
				Switch_route.COLUMNS,
				Switch_route.KEY_BOARD+" = ?",
				new String[] {Long.toString(board_data.sql_id)},
				null,
				null,
				null,
				"1");
		
		Switch_route r = c.moveToFirst() ? new Switch_route(c) : null;
		c.close();
		return r;
	}
	
	
	
	//*****
	//Bing
	//*****
	public void add_bing(Board_data board_data, Bing bing) {
		bing.save(board_data, getWritableDatabase());
	}
	void update_wait(Bing bing) {
		ContentValues values = new ContentValues();
		values.put(Bing.KEY_ID_WAIT, bing.id_wait);
		bing.sqlId = getWritableDatabase().update(Bing.TABLE, values, Bing.KEY_ID+" = ?", new String[] {Long.toString(bing.sqlId)});
	}
	
	List<Bing> get_bings(Board_data board_data) {
		Cursor c = getWritableDatabase().query(Bing.TABLE,
				Bing.COLUMNS,
				Bing.KEY_BOARD+" = ?",
				new String[] {Long.toString(board_data.sql_id)},
				null,
				null,
				null,
				null);
		
		List<Bing> r = new ArrayList<>();
		if(c.moveToFirst()) {
			do {
				r.add(new Bing(c));
			}
			while(c.moveToNext());
		}
		c.close();
		return r;
	}
	List<Bing> get_bings_with(Board_data board_data, String column, boolean value) {
		Cursor c = getWritableDatabase().query(Bing.TABLE,
				Bing.COLUMNS,
				Bing.KEY_BOARD+" = ? AND "+column+" = ?",
				new String[] {Long.toString(board_data.sql_id), value ? "1" : "0"},
				null,
				null,
				null,
				null);
		
		List<Bing> r = new ArrayList<>();
		if(c.moveToFirst()) {
			do {
				r.add(new Bing(c));
			}
			while(c.moveToNext());
		}
		return r;
	}
	
	void remove_bing(Bing bing) {
		getWritableDatabase().delete(Bing.TABLE, Bing.KEY_ID+" = ?", new String[] {Long.toString(bing.sqlId)});
	}
	
	
	//*****
	//Repeat
	//*****
	void save_repeat(Board_data board_data, Repeat repeat) {
		repeat.save(board_data, getWritableDatabase());
	}
	
	Repeat get_repeat(Board_data board_data) {
		Cursor c = getWritableDatabase().query(Repeat.TABLE,
				Repeat.COLUMNS,
				Repeat.KEY_BOARD+" = ?",
				new String[] {Long.toString(board_data.sql_id)},
				null,
				null,
				null,
				null);
		
		Repeat r = c.moveToFirst() ? new Repeat(c) : null;
		c.close();
		return r;
	}
	
	void remove_repeat(Board_data board_data) {
		getWritableDatabase().delete(Repeat.TABLE, Repeat.KEY_BOARD+" = ?", new String[] {Long.toString(board_data.sql_id)});
	}
	
	
	//*****
	//Random
	//*****
	void add_random(Board_data board_data, RandomTimer random) {
		random.save(board_data, getWritableDatabase());
	}
	
	RandomTimer get_randomTimer(Board_data board_data) {
		Cursor c = getWritableDatabase().query(RandomTimer.TABLE,
				RandomTimer.COLUMN,
				RandomTimer.KEY_BOARD+" = ?",
				new String[] {Long.toString(board_data.sql_id)},
				null,
				null,
				null,
				null);
		
		RandomTimer r = c.moveToFirst() ? new RandomTimer(c) : null;
		c.close();
		return r;
	}
	
	void remove_random(Board_data board_data) {
		getWritableDatabase().delete(RandomTimer.TABLE, RandomTimer.KEY_BOARD+" = ?", new String[] {Long.toString(board_data.sql_id)});
	}
}
