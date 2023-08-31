package at.jodlidev.metawear.study.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Battery extends DataBox {
	public static final String KEY_LOG_IDENTIFIER = "battery_log_identifier";
	public static final String KEY_ROUTE_ID = "battery_route_id";
	public static final String KEY_REACT_ROUTE_ID = "battery_react_route_id";
	public static final String KEY_BATTERY_FOR_SWITCH = "battery_for_swtich";
	public static final String KEY_BATTERY_FOR_PING = "battery_for_bing";
	public static final String KEY_LOW_THRESHOLD = "battery_low_threshold";
	public static final String KEY_LOW_LED = "battery_low_led";
	public static final String KEY_LOW_COLOR = "battery_low_color";
	public static final String KEY_LOW_VIBRATION = "battery_low_vibration";
	public static final String KEY_LOW_VIBRATION_STRENGTH = "battery_low_vibration_strength";
	public static final String KEY_LOW_VIBRATION_MS = "battery_low_vibration_ms";
	
	
	public String battery_log_identifier;
	public int battery_route_id = -1;
	public int battery_react_route_id = -1;
	public boolean battery_for_switch = false;
	public boolean battery_for_ping = false;
	public int battery_low_threshold = 50;
	public boolean battery_low_led = false;
	public String battery_low_color = "RED";
	public boolean battery_low_vibration = false;
	public float battery_low_vibration_strength = 100;
	public short battery_low_vibration_ms = 5000;
	
	public Battery() {
	
	}
	public Battery(Cursor c) {
		battery_log_identifier = c.getString(3);
		battery_route_id = c.getInt(4);
		battery_react_route_id = c.getInt(5);
		battery_for_switch = c.getInt(6) == 1;
		battery_for_ping = c.getInt(7) == 1;
		battery_low_threshold = c.getInt(8);
		battery_low_led = c.getInt(9) == 1;
		battery_low_color = c.getString(10);
		battery_low_vibration = c.getInt(11) == 1;
		battery_low_vibration_strength = c.getFloat(12);
		battery_low_vibration_ms = c.getShort(13);
	}
	
	public Battery(boolean _battery_for_switch,
				   boolean _battery_for_ping,
				   int _battery_low_threshold,
				   boolean _battery_low_led,
				   String _battery_low_color,
				   boolean _battery_low_vibration,
				   float _battery_low_vibration_strength,
				   short _battery_low_vibration_ms) {
		
		battery_for_switch = _battery_for_switch;
		battery_for_ping = _battery_for_ping;
		battery_low_threshold = _battery_low_threshold;
		battery_low_led = _battery_low_led;
		battery_low_color = _battery_low_color;
		battery_low_vibration = _battery_low_vibration;
		battery_low_vibration_strength = _battery_low_vibration_strength;
		battery_low_vibration_ms = _battery_low_vibration_ms;
	}
	
	public Battery(JsonReader reader) throws IOException {
		reader.beginObject();
		while(reader.hasNext()) {
			switch(reader.nextName()) {
				case KEY_BATTERY_FOR_SWITCH:
					battery_for_switch = reader.nextBoolean();
					break;
				case KEY_BATTERY_FOR_PING:
					battery_for_ping = reader.nextBoolean();
					break;
				case KEY_LOW_THRESHOLD:
					battery_low_threshold = reader.nextInt();
					break;
				case KEY_LOW_LED:
					battery_low_led = reader.nextBoolean();
					break;
				case KEY_LOW_COLOR:
					battery_low_color = reader.nextString();
					break;
				case KEY_LOW_VIBRATION:
					battery_low_vibration = reader.nextBoolean();
					break;
				case KEY_LOW_VIBRATION_STRENGTH:
					battery_low_vibration_strength = (float) reader.nextDouble();
					break;
				case KEY_LOW_VIBRATION_MS:
					battery_low_vibration_ms = (short) reader.nextInt();
					break;
			}
		}
		reader.endObject();
	}
	void save_to_board(SQLiteDatabase db, Board_data board_data) {
		
		ContentValues values = new ContentValues();
		values.put(KEY_LOG_IDENTIFIER, battery_log_identifier);
		values.put(KEY_ROUTE_ID, battery_route_id);
		values.put(KEY_REACT_ROUTE_ID, battery_react_route_id);
		values.put(KEY_BATTERY_FOR_SWITCH, battery_for_switch);
		values.put(KEY_BATTERY_FOR_PING, battery_for_ping);
		values.put(KEY_LOW_THRESHOLD, battery_low_threshold);
		values.put(KEY_LOW_LED, battery_low_led);
		values.put(KEY_LOW_COLOR, battery_low_color);
		values.put(KEY_LOW_VIBRATION, battery_low_vibration);
		values.put(KEY_LOW_VIBRATION_STRENGTH, battery_low_vibration_strength);
		values.put(KEY_LOW_VIBRATION_MS, battery_low_vibration_ms);
		
		db.update(Board_data.TABLE, values, "id = ?", new String[] {Long.toString(board_data.sql_id)});
	}
	
	public JSONObject export() throws JSONException {
		JSONObject data = new JSONObject();
		data.put(KEY_BATTERY_FOR_SWITCH, battery_for_switch);
		data.put(KEY_BATTERY_FOR_PING, battery_for_ping);
		data.put(KEY_LOW_THRESHOLD, battery_low_threshold);
		data.put(KEY_LOW_LED, battery_low_led);
		data.put(KEY_LOW_COLOR, battery_low_color);
		data.put(KEY_LOW_VIBRATION, battery_low_vibration);
		data.put(KEY_LOW_VIBRATION_STRENGTH, battery_low_vibration_strength);
		data.put(KEY_LOW_VIBRATION_MS, battery_low_vibration_ms);
		return data;
	}
	
	@Override
	public int get_type() {
		return TYPE_BATTERY;
	}
}
