package at.jodlidev.metawear.study.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by JodliDev on 04.12.2019.
 */
public class ChargeFeedback extends DataBox_forFeedback {
	public static final String TABLE = "chargeFeedback";
	public static final String KEY_ID = "_id";
	public static final String KEY_ROUTE_ID = "route_id";
	public static final String KEY_LED = "led";
	public static final String KEY_COLOR = "color";
	public static final String KEY_VIBRATION = "vibration";
	public static final String KEY_VIBRATION_STRENGTH = "vibration_strength";
	public static final String KEY_VIBRATION_MS = "vibration_ms";
	public static final String KEY_EXISTS_ON_BOARD = "exists_on_board";
	
	
	public static final String[] COLUMNS = {
			KEY_ROUTE_ID,
			KEY_LED,
			KEY_COLOR,
			KEY_VIBRATION,
			KEY_VIBRATION_STRENGTH,
			KEY_VIBRATION_MS,
			KEY_EXISTS_ON_BOARD
	};
	
	
	public int route_id;
	public boolean exists_on_board = true;
	
	
	public ChargeFeedback(
			boolean _led,
			String _color,
			boolean _vibration,
			float _vibration_strength,
			short _vibration_ms) {
		led = _led;
		color = _color;
		vibration = _vibration;
		vibration_strength = _vibration_strength;
		vibration_ms = _vibration_ms;
	}
	
	public ChargeFeedback(Cursor c) {
		route_id = c.getInt(0);
		led = c.getInt(1) == 1;
		color = c.getString(2);
		vibration = c.getInt(3) == 1;
		vibration_strength = c.getFloat(4);
		vibration_ms = c.getShort(5);
		exists_on_board = c.getInt(6) == 1;
	}
	
	public ChargeFeedback(JsonReader reader) throws IOException {
		reader.beginObject();
		while(reader.hasNext()) {
			switch(reader.nextName()) {
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
		values.put(KEY_ROUTE_ID, route_id);
		values.put(KEY_LED, led ? 1 : 0);
		values.put(KEY_COLOR, color);
		values.put(KEY_VIBRATION, vibration ? 1 : 0);
		values.put(KEY_VIBRATION_STRENGTH, vibration_strength);
		values.put(KEY_VIBRATION_MS, vibration_ms);
		
		db.insert(TABLE, null, values);
	}
	public void set_to_not_exist(Board_data board_data, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(KEY_EXISTS_ON_BOARD, exists_on_board = false);
		db.update(TABLE, values, KEY_BOARD+ " = ?", new String[] {Long.toString(board_data.sql_id)});
	}
	public JSONObject export() throws JSONException {
		JSONObject data = new JSONObject();
		data.put(KEY_LED, led);
		data.put(KEY_COLOR, color);
		data.put(KEY_VIBRATION, vibration);
		data.put(KEY_VIBRATION_STRENGTH, vibration_strength);
		data.put(KEY_VIBRATION_MS, vibration_ms);
		return data;
	}
	
	
	@Override
	public int get_type() {
		return TYPE_CHARGE_FEEDBACK;
	}
}
