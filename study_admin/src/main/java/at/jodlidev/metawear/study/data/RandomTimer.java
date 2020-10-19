package at.jodlidev.metawear.study.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by JodliDev on 03.04.18.
 */

public class RandomTimer extends DataBox_forFeedback {
	public static final String TABLE = "random";
	public static final String KEY_ID_TIMER = "id_timer";
	public static final String KEY_ID_GYRO = "id_gyro";
	public static final String KEY_TIMEFRAMES_NUM = "timeframes_num";
	public static final String KEY_EXISTS_ON_BOARD = "exists_on_board";
	
	public static final String[] COLUMN = {
			KEY_ID,
			KEY_ID_TIMER,
			KEY_ID_GYRO,
			KEY_MIN,
			KEY_TIMEFRAMES_NUM,
			KEY_REPEAT,
			KEY_BATTERY_LOGGING,
			KEY_LED,
			KEY_COLOR,
			KEY_VIBRATION,
			KEY_VIBRATION_STRENGTH,
			KEY_VIBRATION_MS,
			KEY_EXISTS_ON_BOARD
	};
	
	public int timeframes_num;
	public int id_timer = -1;
	public int id_gyro = -1;
	public boolean exists_on_board = true;
	
	
	public RandomTimer(int _min, int _timeframes_num, boolean _repeat, boolean _battery_logging, boolean _led, String _color, boolean _vibration, float _vibration_strength, short _vibration_ms) {
		min = _min;
		timeframes_num = _timeframes_num;
		repeat = _repeat;
		battery_logging = _battery_logging;
		led = _led;
		color = _color;
		vibration = _vibration;
		vibration_strength = _vibration_strength;
		vibration_ms = _vibration_ms;
	}
	public RandomTimer(Cursor c) {
		sqlId = c.getInt(0);
		id_timer = c.getInt(1);
		id_gyro = c.getInt(2);
		min = c.getInt(3);
		timeframes_num = c.getInt(4);
		repeat = c.getInt(5) == 1;
		battery_logging = c.getInt(6) == 1;
		led = c.getInt(7) == 1;
		color = c.getString(8);
		vibration = c.getInt(9) == 1;
		vibration_strength = c.getFloat(10);
		vibration_ms = c.getShort(11);
		exists_on_board = c.getInt(12) == 1;
	}
	
	public RandomTimer(JsonReader reader) throws IOException {
		reader.beginObject();
		while(reader.hasNext()) {
			switch(reader.nextName()) {
				case KEY_MIN:
					min = reader.nextInt();
					break;
				case KEY_TIMEFRAMES_NUM:
					timeframes_num = reader.nextInt();
					break;
				case KEY_REPEAT:
					repeat = reader.nextBoolean();
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
		values.put(KEY_TIMEFRAMES_NUM, timeframes_num);
		values.put(KEY_REPEAT, repeat ? 1 : 0);
		values.put(KEY_BATTERY_LOGGING, battery_logging ? 1 : 0);
		values.put(KEY_LED, led ? 1 : 0);
		values.put(KEY_COLOR, color);
		values.put(KEY_VIBRATION, vibration ? 1 : 0);
		values.put(KEY_VIBRATION_STRENGTH, vibration_strength);
		values.put(KEY_VIBRATION_MS, vibration_ms);
		values.put(KEY_ID_TIMER, id_timer);
		values.put(KEY_ID_GYRO, id_gyro);
		values.put(KEY_EXISTS_ON_BOARD, exists_on_board ? 1 : 0);
		
		db.insert(RandomTimer.TABLE, null, values);
	}
	public void set_to_not_exist(Board_data board_data, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(KEY_EXISTS_ON_BOARD, exists_on_board = false);
		db.update(TABLE, values, KEY_BOARD+ " = ?", new String[] {Long.toString(board_data.sql_id)});
	}
	
	
	public JSONObject export() throws JSONException {
		JSONObject data = new JSONObject();
		data.put(KEY_MIN, min);
		data.put(KEY_TIMEFRAMES_NUM, timeframes_num);
		data.put(KEY_REPEAT, repeat);
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
		return TYPE_RANDOM;
	}
}
