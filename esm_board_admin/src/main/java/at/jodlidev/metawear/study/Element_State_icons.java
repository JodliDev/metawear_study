package at.jodlidev.metawear.study;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by JodliDev on 03.04.18.
 */

public class Element_State_icons extends LinearLayout {
	
	private TextView time;
	private TextView time_separator;
	private TextView time_end;
	private TextView time_extra;
	private ImageView vibration_state;
	private ImageView led_state;
	private ImageView random_state;
	private ImageView repeat_state;
	private ImageView battery_state;
	
	public Element_State_icons(Context context) {
		super(context);
		init(context);
	}
	
	public Element_State_icons(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public Element_State_icons(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
	}
	private void init(Context _context) {
		inflate(_context, R.layout.element_state_icons, this);
		time = findViewById(R.id.time);
		time_separator = findViewById(R.id.time_separator);
		time_end = findViewById(R.id.time_end);
		time_extra = findViewById(R.id.time_extra);
		led_state = findViewById(R.id.led_state);
		vibration_state = findViewById(R.id.vibration_state);
		random_state = findViewById(R.id.random_state);
		repeat_state = findViewById(R.id.repeat_state);
		battery_state = findViewById(R.id.battery_state);
	}
	
	private void set_absoluteTime(TextView v, int hour, int min) {
		v.setTextSize(30);
		String hour_s = hour > 9 ? Integer.toString(hour) : "0"+hour;
		String min_s = min > 9 ? Integer.toString(min) : "0"+min;
		v.setText(getResources().getString(R.string.time_format, hour_s, min_s));
	}
	
	public void show_data(int min, boolean repeat, boolean battery, boolean led, String color, boolean vibration) {
		time.setTextSize(20);
		time.setText(getResources().getString(R.string.relative_time_format, Integer.toString(min/60), Integer.toString(min%60)));
		
		show_states(false, repeat, battery, led, color, vibration);
	}
	
	
	public void show_data(int hour, int min, int random_min, boolean repeat, boolean battery, boolean led, String color, boolean vibration) {
		boolean random = random_min != 0;
		set_absoluteTime(time, hour, min);
		
		if(random) {
			int added_min = min + random_min;
			min = added_min % 60;
			hour += added_min / 60;
			
			
			
			time_separator.setVisibility(VISIBLE);
			time_end.setVisibility(VISIBLE);
			set_absoluteTime(time_end, hour, min);
		}
		
		show_states(random, repeat, battery, led, color, vibration);
	}
	private void show_states(boolean random, boolean repeat, boolean battery, boolean led, String color, boolean vibration) {
		
		
		if(led) {
			led_state.setVisibility(View.VISIBLE);
			led_state.setColorFilter(Color.parseColor(color));
		}
		else
			led_state.setVisibility(View.GONE);
		
		vibration_state.setVisibility(vibration ? View.VISIBLE : View.GONE);
		random_state.setVisibility(random ? View.VISIBLE : View.GONE);
		repeat_state.setVisibility(repeat ? View.VISIBLE : View.GONE);
		battery_state.setVisibility(battery ? View.VISIBLE : View.GONE);
	}
	
	public void divide_time_by(int num) {
		time_extra.setVisibility(VISIBLE);
		time_extra.setText(getResources().getString(R.string.divide_by, num));
	}
	public void multiply_time_with(int num) {
		time_extra.setVisibility(VISIBLE);
		time_extra.setText(getResources().getString(R.string.multiply_with, num));
	}
}
