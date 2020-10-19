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

public abstract class DataBox_forFeedback extends DataBox {
	public static final String KEY_ID = "_id";
	public static final String KEY_BOARD = "board";
	public static final String KEY_HOUR = "hour";
	public static final String KEY_MIN = "min";
	public static final String KEY_RANDOM = "random";
	public static final String KEY_REPEAT = "repeat";
	public static final String KEY_BATTERY_LOGGING = "battery_logging";
	public static final String KEY_LED = "led";
	public static final String KEY_COLOR = "color";
	public static final String KEY_VIBRATION = "vibration";
	public static final String KEY_VIBRATION_STRENGTH = "vibration_strength";
	public static final String KEY_VIBRATION_MS = "vibration_ms";
	
	
	public long sqlId = -1;
	
	public int hour;
	public int min;
	public boolean random;
	public boolean repeat;
	public boolean battery_logging;
	public boolean led;
	public String color;
	public boolean vibration;
	public float vibration_strength;
	public short vibration_ms;
}
