package at.jodlidev.metawear.study.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by JodliDev on 27.03.2019.
 */
public class BootMacro extends DataBox {
	public static final String TABLE = "macro";
	public static final String KEY_ID = "_id";
	public static final String KEY_BOARD = "board";
	public static final String KEY_REPEAT_FEEDBACK = "repeat_feedback";
	public static final String KEY_HOUR = "hour";
	public static final String KEY_MIN = "min";
	public static final String KEY_LED = "led";
	public static final String KEY_LED_TYPE = "led_type";
	public static final String KEY_COLOR = "color";
	public static final String KEY_VIBRATION = "vibration";
	public static final String KEY_VIBRATION_STRENGTH = "vibration_strength";
	public static final String KEY_VIBRATION_MS = "vibration_ms";
//	public static final String KEY_REMEMBER_SWITCH = "remember_switch";
	
	public static final int LED_TYPE_BLINKING = 1;
	public static final int LED_TYPE_SOLID = 2;
	
	
	public static final String[] COLUMNS = {
			KEY_REPEAT_FEEDBACK,
			KEY_HOUR,
			KEY_MIN,
			KEY_LED,
			KEY_LED_TYPE,
			KEY_COLOR,
			KEY_VIBRATION,
			KEY_VIBRATION_STRENGTH,
			KEY_VIBRATION_MS
//			KEY_REMEMBER_SWITCH
	};
	
	public boolean repeat_feedback;
	public int hour;
	public int min;
	public boolean led;
	public int led_type;
	public String color;
	public boolean vibration;
	public float vibration_strength;
	public short vibration_ms;
//	public boolean remember_switch;
	
	public BootMacro(
			boolean _repeat_feedback,
			int _hour,
			int _min,
			boolean _led,
			int _led_type,
			String _color,
			boolean _vibration,
			float _vibration_strength,
			short _vibration_ms) {
		repeat_feedback = _repeat_feedback;
		hour = _hour;
		min = _min;
		led = _led;
		led_type = _led_type;
		color = _color;
		vibration = _vibration;
		vibration_strength = _vibration_strength;
		vibration_ms = _vibration_ms;
//		remember_switch = _remember_switch;
	}
	public BootMacro(Cursor c) {
		repeat_feedback = c.getInt(0) == 1;
		hour = c.getInt(1);
		min = c.getInt(2);
		led = c.getInt(3) == 1;
		led_type = c.getInt(4);
		color = c.getString(5);
		vibration = c.getInt(6) == 1;
		vibration_strength = c.getFloat(7);
		vibration_ms = c.getShort(8);
//		remember_switch = c.getInt(9) == 1;
	}
	
	public BootMacro(JsonReader reader) throws IOException {
		reader.beginObject();
		while(reader.hasNext()) {
			switch(reader.nextName()) {
				case KEY_REPEAT_FEEDBACK:
					repeat_feedback = reader.nextBoolean();
					break;
				case KEY_HOUR:
					hour = reader.nextInt();
					break;
				case KEY_MIN:
					min = reader.nextInt();
					break;
				case KEY_LED:
					led = reader.nextBoolean();
					break;
				case KEY_LED_TYPE:
					led_type = reader.nextInt();
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
//				case KEY_REMEMBER_SWITCH:
//					remember_switch = reader.nextBoolean();
//					break;
			}
		}
		reader.endObject();
	}
	public void save(Board_data board_data, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(KEY_BOARD, board_data.sql_id);
		values.put(KEY_REPEAT_FEEDBACK, repeat_feedback);
		values.put(KEY_HOUR, hour);
		values.put(KEY_MIN, min);
		values.put(KEY_LED, led ? 1 : 0);
		values.put(KEY_LED_TYPE, led_type);
		values.put(KEY_COLOR, color);
		values.put(KEY_VIBRATION, vibration ? 1 : 0);
		values.put(KEY_VIBRATION_STRENGTH, vibration_strength);
		values.put(KEY_VIBRATION_MS, vibration_ms);
//		values.put(KEY_REMEMBER_SWITCH, remember_switch);
		db.insert(BootMacro.TABLE, null, values);
	}
	public JSONObject export() throws JSONException {
		JSONObject data = new JSONObject();
		data.put(KEY_REPEAT_FEEDBACK, repeat_feedback);
		data.put(KEY_HOUR, hour);
		data.put(KEY_MIN, min);
		data.put(KEY_LED, led);
		data.put(KEY_LED_TYPE, led_type);
		data.put(KEY_COLOR, color);
		data.put(KEY_VIBRATION, vibration);
		data.put(KEY_VIBRATION_STRENGTH, vibration_strength);
		data.put(KEY_VIBRATION_MS, vibration_ms);
//		data.put(KEY_REMEMBER_SWITCH, remember_switch);
		return data;
	}
	
	
	
	@Override
	public int get_type() {
		return TYPE_MACRO;
	}
}
