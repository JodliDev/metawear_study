package at.jodlidev.metawear.study.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by david on 03.04.18.
 */

public class Switch_route extends DataBox {
	public static final String TABLE = "switch_routes";
	public static final String KEY_BOARD = "board";
	public static final String KEY_SWITCH_PRE_COUNT_ID = "switch_pre_count_id";
	public static final String KEY_SWITCH_PRE_STATE_ID = "switch_pre_state_id";
	public static final String KEY_SWITCH_POST_ID = "switch_post_id";
	public static final String KEY_ACC_ID = "acc_id";
	public static final String KEY_TIMER_ID = "timer_id";
	public static final String KEY_BUFFER_ID = "buffer_id";
	public static final String KEY_SWITCH_IDENTIFIER = "switch_identifier";
	public static final String KEY_SWITCH_LOG_TIMESTAMP = "switch_log_timestamp";
	public static final String KEY_ACC_IDENTIFIER = "acc_identifier";
	public static final String KEY_ACC_LOG_TIMESTAMP = "acc_log_timestamp";
	public static final String KEY_LOG_SWITCH_DATA = "log_switch_data";
	public static final String KEY_LOG_ACC_DATA = "log_acc_data";
	public static final String KEY_LED_BEHAVIOR = "led_behaviour";
	public static final String KEY_ACC_AXIS_VISUALISED = "acc_axis_visualised";
	public static final String KEY_COLOR = "color";
	public static final String KEY_VIBRATION = "vibration";
	public static final String KEY_VIBRATION_STRENGTH = "vibration_strength";
	public static final String KEY_VIBRATION_MS = "vibration_ms";
	public static final String KEY_LOG_NUMBER_LED = "log_number_led";
	public static final String KEY_LOG_NUMBER_COLOR = "log_number_color";
	public static final String KEY_LOG_NUMBER_VIBRATION = "log_number_vibration";
	public static final String KEY_LOG_NUMBER_VIBRATION_STRENGTH = "log_number_vibration_strength";
	public static final String KEY_LOG_NUMBER_VIBRATION_MS = "log_number_vibration_ms";
	public static final String KEY_RESTORE_ON_BOOT = "restore_on_boot";
	public static final String KEY_EXISTS_ON_BOARD = "exists_on_board";
	
	public static final String[] COLUMNS = {
			KEY_SWITCH_PRE_COUNT_ID,
			KEY_SWITCH_PRE_STATE_ID,
			KEY_SWITCH_POST_ID,
			KEY_ACC_ID,
			KEY_TIMER_ID,
			KEY_BUFFER_ID,
			KEY_SWITCH_IDENTIFIER,
			KEY_SWITCH_LOG_TIMESTAMP,
			KEY_ACC_IDENTIFIER,
			KEY_ACC_LOG_TIMESTAMP,
			KEY_LOG_SWITCH_DATA,
			KEY_LOG_ACC_DATA,
			KEY_LED_BEHAVIOR,
			KEY_ACC_AXIS_VISUALISED,
			KEY_COLOR,
			KEY_VIBRATION,
			KEY_VIBRATION_STRENGTH,
			KEY_VIBRATION_MS,
			KEY_LOG_NUMBER_LED,
			KEY_LOG_NUMBER_COLOR,
			KEY_LOG_NUMBER_VIBRATION,
			KEY_LOG_NUMBER_VIBRATION_STRENGTH,
			KEY_LOG_NUMBER_VIBRATION_MS,
			KEY_RESTORE_ON_BOOT,
			KEY_EXISTS_ON_BOARD
	};
	
	public static final short LOG_SWITCH_NONE = 0;
	public static final short LOG_LENGTH_OF_CLICK = 1;
	public static final short LOG_NUMBER_OF_CLICKS = 2;
	
	public static final short LED_NONE = 0;
	public static final short LED_BLINKING = 1;
	public static final short LED_VISUALIZE_BUTTON = 2;
//	public static final short LED_VISUALIZE_ACC3 = 3;
//	public static final short LED_VISUALIZE_ACC6 = 4;
	
	
	public int switch_pre_count_id = -1;
	public int switch_pre_state_id = -1;
	public int switch_post_id = -1;
	public int timer_id = -1;
	public int buffer_id = -1;
	public int acc_id = -1;
	
	public String switch_identifier;
	public long switch_log_timestamp;
	public String acc_identifier;
	public long acc_log_timestamp;
	
	public short log_switch_data = LOG_SWITCH_NONE;
	public boolean log_acc_data = false;
	public short led_behaviour = LED_NONE;
	public short acc_axis_visualised = 0; //not used anymore - but we may want to use it again in the future
	public String color = "GREEN";
	public boolean vibration = false;
	public float vibration_strength = 50;
	public short vibration_ms = 200;
	public boolean log_number_led = false;
	public String log_number_color = "GREEN";
	public boolean log_number_vibration = false;
	public float log_number_vibration_strength = 50;
	public short log_number_vibration_ms = 1000;
	
	public boolean restore_on_boot = false;
	public boolean exists_on_board = true;
	
	public Switch_route() {
	
	}
	
//	public Switch_route(short _log_switch_data,
//						boolean _log_acc_data,
//						short _led_behaviour,
//						String _color,
//						boolean _vibration,
//						float _vibration_strength,
//						short _vibration_ms,
//						boolean _log_number_led,
//						String _log_number_color,
//						boolean _log_number_vibration,
//						float _log_number_vibration_strength,
//						short _log_number_vibration_ms) {
//		log_switch_data = _log_switch_data;
//		log_acc_data = _log_acc_data;
//		led_behaviour = _led_behaviour;
//		color = _color;
//		vibration = _vibration;
//		vibration_strength = _vibration_strength;
//		vibration_ms = _vibration_ms;
//		log_number_led = _log_number_led;
//		log_number_color = _log_number_color;
//		log_number_vibration = _log_number_vibration;
//		log_number_vibration_strength = _log_number_vibration_strength;
//		log_number_vibration_ms = _log_number_vibration_ms;
//	}
	
	public Switch_route(Cursor c) {
		switch_pre_count_id = c.getInt(0);
		switch_pre_state_id = c.getInt(1);
		switch_post_id = c.getInt(2);
		acc_id = c.getInt(3);
		timer_id = c.getInt(4);
		buffer_id = c.getInt(5);
		switch_identifier = c.getString(6);
		switch_log_timestamp = c.getLong(7);
		acc_identifier = c.getString(8);
		acc_log_timestamp = c.getLong(9);
		log_switch_data = c.getShort(10);
		log_acc_data = c.getInt(11) == 1;
		led_behaviour = c.getShort(12);
		acc_axis_visualised = c.getShort(13);
		color = c.getString(14);
		vibration = c.getInt(15) == 1;
		vibration_strength = c.getFloat(16);
		vibration_ms = c.getShort(17);
		log_number_led = c.getInt(18) == 1;
		log_number_color = c.getString(19);
		log_number_vibration = c.getInt(20) == 1;
		log_number_vibration_strength = c.getFloat(21);
		log_number_vibration_ms = c.getShort(22);
		restore_on_boot = c.getInt(23) == 1;
		exists_on_board = c.getInt(24) == 1;
	}
	
	public Switch_route(JsonReader reader) throws IOException {
		reader.beginObject();
		while(reader.hasNext()) {
			switch(reader.nextName()) {
				case KEY_LOG_SWITCH_DATA:
					log_switch_data = (short) reader.nextInt();
					break;
				case KEY_LOG_ACC_DATA:
					log_acc_data = reader.nextBoolean();
					break;
				case KEY_LED_BEHAVIOR:
					led_behaviour = (short) reader.nextInt();
					break;
				case KEY_ACC_AXIS_VISUALISED:
					acc_axis_visualised = (short) reader.nextInt();
					break;
				case KEY_COLOR:
					color = reader.nextString();
					break;
				case KEY_VIBRATION:
					vibration = reader.nextBoolean();
					break;
				case KEY_VIBRATION_STRENGTH:
					vibration_strength = (float) reader.nextInt();
					break;
				case KEY_VIBRATION_MS:
					vibration_ms = (short) reader.nextInt();
					break;
				case KEY_LOG_NUMBER_LED:
					log_number_led = reader.nextBoolean();
					break;
				case KEY_LOG_NUMBER_COLOR:
					log_number_color = reader.nextString();
					break;
				case KEY_LOG_NUMBER_VIBRATION:
					log_number_vibration = reader.nextBoolean();
					break;
				case KEY_LOG_NUMBER_VIBRATION_STRENGTH:
					log_number_vibration_strength = (float) reader.nextInt();
					break;
				case KEY_LOG_NUMBER_VIBRATION_MS:
					log_number_vibration_ms = (short) reader.nextInt();
					break;
				case KEY_RESTORE_ON_BOOT:
					restore_on_boot = reader.nextBoolean();
					break;
			}
		}
		reader.endObject();
	}
	public void save(Board_data board_data, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(KEY_BOARD, board_data.sql_id);
		values.put(KEY_SWITCH_PRE_COUNT_ID, switch_pre_count_id);
		values.put(KEY_SWITCH_PRE_STATE_ID, switch_pre_state_id);
		values.put(KEY_SWITCH_POST_ID, switch_post_id);
		values.put(KEY_ACC_ID, acc_id);
		values.put(KEY_TIMER_ID, timer_id);
		values.put(KEY_BUFFER_ID, buffer_id);
		values.put(KEY_SWITCH_IDENTIFIER, switch_identifier);
		values.put(KEY_SWITCH_LOG_TIMESTAMP, switch_log_timestamp);
		values.put(KEY_ACC_IDENTIFIER, acc_identifier);
		values.put(KEY_ACC_LOG_TIMESTAMP, acc_log_timestamp);
		values.put(KEY_LOG_SWITCH_DATA, log_switch_data);
		values.put(KEY_LOG_ACC_DATA, log_acc_data);
		values.put(KEY_LED_BEHAVIOR, led_behaviour);
		values.put(KEY_ACC_AXIS_VISUALISED, acc_axis_visualised);
		values.put(KEY_COLOR, color);
		values.put(KEY_VIBRATION, vibration ? 1 : 0);
		values.put(KEY_VIBRATION_STRENGTH, vibration_strength);
		values.put(KEY_VIBRATION_MS, vibration_ms);
		values.put(KEY_LOG_NUMBER_LED, log_number_led);
		values.put(KEY_LOG_NUMBER_COLOR, log_number_color);
		values.put(KEY_LOG_NUMBER_VIBRATION, log_number_vibration ? 1 : 0);
		values.put(KEY_LOG_NUMBER_VIBRATION_STRENGTH, log_number_vibration_strength);
		values.put(KEY_LOG_NUMBER_VIBRATION_MS, log_number_vibration_ms);
		values.put(KEY_RESTORE_ON_BOOT, restore_on_boot);
		values.put(KEY_EXISTS_ON_BOARD, true); //in case an existing object is reused
		
		db.insert(TABLE, null, values);
	}
	public void set_to_not_exist(Board_data board_data, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(KEY_EXISTS_ON_BOARD, exists_on_board = false);
		db.update(TABLE, values, KEY_BOARD+ " = ?", new String[] {Long.toString(board_data.sql_id)});
	}
	
	
	public JSONObject export() throws JSONException {
		JSONObject data = new JSONObject();
		data.put(KEY_LOG_SWITCH_DATA, log_switch_data);
		data.put(KEY_LOG_ACC_DATA, log_acc_data);
		data.put(KEY_LED_BEHAVIOR, led_behaviour);
		data.put(KEY_ACC_AXIS_VISUALISED, acc_axis_visualised);
		data.put(KEY_COLOR, color);
		data.put(KEY_VIBRATION, vibration);
		data.put(KEY_VIBRATION_STRENGTH, vibration_strength);
		data.put(KEY_VIBRATION_MS, vibration_ms);
		data.put(KEY_LOG_NUMBER_LED, log_number_led);
		data.put(KEY_LOG_NUMBER_COLOR, log_number_color);
		data.put(KEY_LOG_NUMBER_VIBRATION, log_number_vibration);
		data.put(KEY_LOG_NUMBER_VIBRATION_STRENGTH, log_number_vibration_strength);
		data.put(KEY_LOG_NUMBER_VIBRATION_MS, log_number_vibration_ms);
		data.put(KEY_RESTORE_ON_BOOT, restore_on_boot);
		return data;
	}
	
	@Override
	public int get_type() {
		return TYPE_SWITCH;
	}
}
