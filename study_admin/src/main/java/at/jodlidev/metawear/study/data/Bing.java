package at.jodlidev.metawear.study.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.JsonReader;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by david on 03.04.18.
 */

public class Bing extends DataBox_forFeedback {
	public static final String TABLE = "bings";
	public static final String KEY_REMOVE_WAIT_TIMESTAMP = "remove_wait_timestamp";
	public static final String KEY_ID_WAIT = "id_wait";
	public static final String KEY_ID_LOOP = "id_loop";
	public static final String KEY_EXISTS_ON_BOARD = "exists_on_board";
	
	public static final String[] COLUMNS = {
			KEY_ID,
			KEY_REMOVE_WAIT_TIMESTAMP,
			KEY_HOUR,
			KEY_MIN,
			KEY_RANDOM,
			KEY_REPEAT,
			KEY_BATTERY_LOGGING,
			KEY_LED,
			KEY_COLOR,
			KEY_VIBRATION,
			KEY_VIBRATION_STRENGTH,
			KEY_VIBRATION_MS,
			KEY_ID_WAIT,
			KEY_ID_LOOP,
			KEY_EXISTS_ON_BOARD
	};
	
	
	public long remove_wait_timestamp = -1;
	public int id_wait = -1;
	public int id_loop = -1;
	
	public Handler timer = null;
	public View view = null;
	public int countdown;
	
	public boolean exists_on_board = true;
	
	public Bing(int _hour, int _min, boolean _random, boolean _repeat, boolean _battery_logging, boolean _led, String _color, boolean _vibration, float _vibration_strength, short _vibration_ms) {
		hour = _hour;
		min = _min;
		random = _random;
		battery_logging = _battery_logging;
		repeat = _repeat;
		led = _led;
		color = _color;
		vibration = _vibration;
		vibration_strength = _vibration_strength;
		vibration_ms = _vibration_ms;
		
		calc_wait_countdown();
	}
	public Bing(Cursor c) {
		sqlId = c.getLong(0);
		remove_wait_timestamp = c.getLong(1);
		hour = c.getInt(2);
		min = c.getInt(3);
		random = c.getInt(4) == 1;
		repeat = c.getInt(5) == 1;
		battery_logging = c.getInt(6) == 1;
		led = c.getInt(7) == 1;
		color =  c.getString(8);
		vibration = c.getInt(9) == 1;
		vibration_strength = c.getFloat(10);
		vibration_ms = c.getShort(11);
		id_wait = c.getInt(12);
		id_loop = c.getInt(13);
		exists_on_board = c.getInt(14) == 1;
		
		calc_wait_countdown();
	}
	
	public Bing(JsonReader reader) throws IOException {
		reader.beginObject();
		while(reader.hasNext()) {
			switch(reader.nextName()) {
				case KEY_HOUR:
					hour = reader.nextInt();
					break;
				case KEY_MIN:
					min = reader.nextInt();
					break;
				case KEY_RANDOM:
					random = reader.nextBoolean();
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
		
		calc_wait_countdown();
	}
	public void save(Board_data board_data, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(KEY_BOARD, board_data.sql_id);
		values.put(KEY_REMOVE_WAIT_TIMESTAMP, remove_wait_timestamp);
		values.put(KEY_HOUR, hour);
		values.put(KEY_MIN, min);
		values.put(KEY_RANDOM, random ? 1 : 0);
		values.put(KEY_REPEAT, repeat ? 1 : 0);
		values.put(KEY_BATTERY_LOGGING, battery_logging ? 1 : 0);
		values.put(KEY_LED, led ? 1 : 0);
		values.put(KEY_COLOR, color);
		values.put(KEY_VIBRATION, vibration ? 1 : 0);
		values.put(KEY_VIBRATION_STRENGTH, vibration_strength);
		values.put(KEY_VIBRATION_MS, vibration_ms);
		values.put(KEY_ID_WAIT, id_wait);
		values.put(KEY_ID_LOOP, id_loop);
		
		sqlId = db.insert(Bing.TABLE, null, values);
	}
	public void set_to_not_exist(Board_data board_data, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(KEY_EXISTS_ON_BOARD, exists_on_board = false);
		db.update(TABLE, values, KEY_BOARD+ " = ?", new String[] {Long.toString(board_data.sql_id)});
	}
	
	public void removeTimer() {
		if(timer != null)
			timer.removeCallbacksAndMessages(null);
	}
	
	private void calc_wait_countdown() {
		Calendar now = Calendar.getInstance();
		
		if(remove_wait_timestamp == -1) {
			Calendar later = Calendar.getInstance();
			later.set(Calendar.HOUR_OF_DAY, hour);
			later.set(Calendar.MINUTE, min);
			later.set(Calendar.SECOND, 0);
			
			if (later.getTimeInMillis() < now.getTimeInMillis()) {
				later.add(Calendar.DAY_OF_MONTH, 1);
			}
			
			countdown = (int) (later.getTimeInMillis() - now.getTimeInMillis());
			remove_wait_timestamp = later.getTimeInMillis();
		}
		else {
			countdown = (int) (remove_wait_timestamp - now.getTimeInMillis());
		}
	}
	
	public JSONObject export() throws JSONException {
		JSONObject data = new JSONObject();
		data.put(KEY_HOUR, hour);
		data.put(KEY_MIN, min);
		data.put(KEY_RANDOM, random);
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
		return TYPE_BING;
	}
}
