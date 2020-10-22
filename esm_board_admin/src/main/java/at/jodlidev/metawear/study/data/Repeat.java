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

public class Repeat extends DataBox_forFeedback {
	public static final String TABLE = "repeats";
	public static final String KEY_ID_TIMER = "id_timer";
	public static final String KEY_ID_SWITCH = "id_switch";
	public static final String KEY_REPEAT_NUM = "repeat_num";
	public static final String KEY_EXISTS_ON_BOARD = "exists_on_board";
	
	public static final String[] COLUMNS = {
			KEY_ID_TIMER,
			KEY_ID_SWITCH,
			KEY_MIN,
			KEY_REPEAT_NUM,
			KEY_BATTERY_LOGGING,
			KEY_LED,
			KEY_COLOR,
			KEY_VIBRATION,
			KEY_VIBRATION_STRENGTH,
			KEY_VIBRATION_MS,
			KEY_EXISTS_ON_BOARD
	};
	
	public short repeat_num;
	public int id_timer = -1;
	public int id_switch = -1;
	public boolean exists_on_board = true;
	
	
	public Repeat(int _min, short _repeat_num, boolean _battery_logging, boolean _led, String _color, boolean _vibration, float _vibration_strength, short _vibration_ms) {
		min = _min;
		repeat_num = _repeat_num;
		battery_logging = _battery_logging;
		led = _led;
		color = _color;
		vibration = _vibration;
		vibration_strength = _vibration_strength;
		vibration_ms = _vibration_ms;
	}
	public Repeat(Cursor c) {
		id_timer = c.getInt(0);
		id_switch = c.getInt(1);
		min = c.getInt(2);
		repeat_num = c.getShort(3);
		battery_logging = c.getInt(4) == 1;
		led = c.getInt(5) == 1;
		color = c.getString(6);
		vibration = c.getInt(7) == 1;
		vibration_strength = c.getFloat(8);
		vibration_ms = c.getShort(9);
		exists_on_board = c.getInt(10) == 1;
	}
	
	public Repeat(JsonReader reader) throws IOException {
		reader.beginObject();
		while(reader.hasNext()) {
			switch(reader.nextName()) {
				case KEY_MIN:
					min = reader.nextInt();
					break;
				case KEY_REPEAT_NUM:
					repeat_num = (short) reader.nextInt();
					break;
				case KEY_BATTERY_LOGGING:
					battery_logging = reader.nextBoolean();
					break;
				case KEY_LED:
					led = reader.nextBoolean();
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
			}
		}
		reader.endObject();
	}
	public void save(Board_data board_data, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(KEY_BOARD, board_data.sql_id);
		values.put(KEY_MIN, min);
		values.put(KEY_REPEAT_NUM, repeat_num);
		values.put(KEY_BATTERY_LOGGING, battery_logging ? 1 : 0);
		values.put(KEY_LED, led ? 1 : 0);
		values.put(KEY_COLOR, color);
		values.put(KEY_VIBRATION, vibration ? 1 : 0);
		values.put(KEY_VIBRATION_STRENGTH, vibration_strength);
		values.put(KEY_VIBRATION_MS, vibration_ms);
		values.put(KEY_ID_TIMER, id_timer);
		values.put(KEY_ID_SWITCH, id_switch);
		
		db.insert(Repeat.TABLE, null, values);
	}
	public void set_to_not_exist(Board_data board_data, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(KEY_EXISTS_ON_BOARD, exists_on_board = false);
		db.update(TABLE, values, KEY_BOARD+ " = ?", new String[] {Long.toString(board_data.sql_id)});
	}
	public JSONObject export() throws JSONException {
		JSONObject data = new JSONObject();
		data.put(KEY_MIN, min);
		data.put(KEY_REPEAT_NUM, repeat_num);
		data.put(KEY_BATTERY_LOGGING, battery_logging);
		data.put(KEY_LED, led);
		data.put(KEY_COLOR, color);
		data.put(KEY_VIBRATION, vibration);
		data.put(KEY_VIBRATION_STRENGTH, vibration_strength);
		data.put(KEY_VIBRATION_MS, vibration_ms);
		return data;
	}
	
	@Override
	public int get_type() {
		return TYPE_REPEAT;
	}
}
