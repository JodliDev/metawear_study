package at.jodlidev.metawear.study;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import at.jodlidev.metawear.study.data.Bing;
import at.jodlidev.metawear.study.data.RandomTimer;
import at.jodlidev.metawear.study.data.Repeat;
import bolts.Task;

/**
 * Created by JodliDev on 24.03.18.
 */

public class Dialog_newBing extends Dialog {
	public static class OwnPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
		
		private final static int PAGE_BING = 0;
		private final static int PAGE_REPEAT = 1;
		
		private int PAGES_NUM = 2;
		
		private Activity activity;
		private Board_logic logic;
		private Bing bing;
		private Repeat repeat;
		private RandomTimer randomTimer;
		
		private Button btn_save;
		private Button btn_back;
		
		private boolean isRandom;
		private boolean random_already_created;
		
		private Element_Time_input bing_time_picker;
		private Element_Led_checkbox bing_led_alarm_checkbox;
		private Element_Vibration_checkbox bing_vibration_alarm_checkbox;
		private CheckBox bing_log_battery_checkbox;
		
		private EditText random_input_min;
		private EditText random_input_timeframes;
		
		
		private CheckBox repeat_enable;
		private Element_Led_checkbox repeat_led_alarm_checkbox;
		private Element_Vibration_checkbox repeat_vibration_alarm_checkbox;
		private CheckBox repeat_log_battery_checkbox;
		private EditText repeat_input_min;
		private EditText repeat_input_repeat_num;
		private TextView repeat_time_header;
		private TextView repeat_count_header;
		private TextView repeat_count_label;
		
		LayoutInflater inflater;
		OwnPagerAdapter(Activity _activity, boolean _isRandom, Board_logic _logic, Button _btn_back, Button _btn_save) {
			inflater = LayoutInflater.from(_activity.getApplicationContext());
			activity = _activity;
			isRandom = _isRandom;
			logic = _logic;
			btn_back = _btn_back;
			btn_save = _btn_save;
			
			random_already_created = logic.get_randomTimer() != null;
			
			if(isRandom && random_already_created) //since the Repeat is part of Random, it can not be changed
				PAGES_NUM = 1;
		}
		
		@Override
		@NonNull
		public Object instantiateItem(@NonNull ViewGroup container, int position) {
			View view;
			switch(position) {
				default:
				case PAGE_BING:
					if(isRandom) {
						view = inflater.inflate(R.layout.item_bing_dialog_set_random, container, false);
						
						bing_time_picker = view.findViewById(R.id.time_picker);
						bing_time_picker.init(activity);
						bing_led_alarm_checkbox = view.findViewById(R.id.led_alarm_checkbox);
						bing_vibration_alarm_checkbox = view.findViewById(R.id.vibration_alarm_checkbox);
						bing_log_battery_checkbox = view.findViewById(R.id.log_battery_checkbox);
						
						random_input_min = view.findViewById(R.id.input_min);
						random_input_timeframes = view.findViewById(R.id.input_timeframes);
						
						
						RandomTimer randomTimer = logic.get_randomTimer();
						if(randomTimer != null) {
							view.findViewById(R.id.only_one_info).setVisibility(View.GONE);
							view.findViewById(R.id.already_exists_info).setVisibility(View.VISIBLE);
							bing_led_alarm_checkbox.setEnabled(false);
							bing_led_alarm_checkbox.setColor(randomTimer.color);
							bing_vibration_alarm_checkbox.setEnabled(false);
							bing_vibration_alarm_checkbox.set_ms(randomTimer.vibration_ms);
							bing_vibration_alarm_checkbox.set_strength(randomTimer.vibration_strength);
							bing_log_battery_checkbox.setEnabled(false);
							bing_log_battery_checkbox.setChecked(randomTimer.battery_logging);
							random_input_min.setEnabled(false);
							random_input_min.setText(Integer.toString(randomTimer.min));
							random_input_timeframes.setEnabled(false);
							random_input_timeframes.setText(Integer.toString(randomTimer.timeframes_num));
						}
						
					}
					else {
						view = inflater.inflate(R.layout.item_bing_dialog_new, container, false);
						
						bing_led_alarm_checkbox = view.findViewById(R.id.led_alarm_checkbox);
						bing_vibration_alarm_checkbox = view.findViewById(R.id.vibration_alarm_checkbox);
						bing_log_battery_checkbox = view.findViewById(R.id.log_battery_checkbox);
						bing_time_picker = view.findViewById(R.id.time_picker);
						bing_time_picker.init(activity);
						
						if(isRandom) {
							bing_led_alarm_checkbox.setVisibility(View.GONE);
							bing_vibration_alarm_checkbox.setVisibility(View.GONE);
							bing_log_battery_checkbox.setVisibility(View.GONE);
						}
					}
					break;
				case PAGE_REPEAT:
					
					view = inflater.inflate(R.layout.item_bing_dialog_set_repeat, container, false);
					
					repeat_enable = view.findViewById(R.id.enable);
					repeat_led_alarm_checkbox = view.findViewById(R.id.led_alarm_checkbox);
					repeat_vibration_alarm_checkbox = view.findViewById(R.id.vibration_alarm_checkbox);
					repeat_log_battery_checkbox = view.findViewById(R.id.log_battery_checkbox);
					repeat_input_min = view.findViewById(R.id.input_min);
					repeat_input_repeat_num = view.findViewById(R.id.input_repeat_num);
					
					repeat_time_header = view.findViewById(R.id.repeat_time_header);
					repeat_count_header = view.findViewById(R.id.repeat_count_header);
					repeat_count_label = view.findViewById(R.id.repeat_count_label);
					
					repeat_setEnabled(false);
					
					Repeat repeat = logic.get_repeat();
					if(repeat == null) {
						repeat_enable.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
							repeat_setEnabled(isChecked);
						});
					}
					else {
						
						view.findViewById(R.id.already_exists_info).setVisibility(View.VISIBLE);
						view.findViewById(R.id.only_one_info).setVisibility(View.GONE);
						repeat_led_alarm_checkbox.setChecked(repeat.led);
						repeat_led_alarm_checkbox.setColor(repeat.color);
						repeat_vibration_alarm_checkbox.setChecked(repeat.vibration);
						repeat_vibration_alarm_checkbox.set_strength(repeat.vibration_strength);
						repeat_vibration_alarm_checkbox.set_ms(repeat.vibration_ms);
						repeat_log_battery_checkbox.setChecked(repeat.battery_logging);
						repeat_input_min.setText(Integer.toString(repeat.min));
						repeat_input_repeat_num.setText(Integer.toString(repeat.repeat_num));
					}
					break;
			}
			
			
			container.addView(view);
			return view;
		}
		private void repeat_setEnabled(boolean show) {
			
			int color = activity.getResources().getColor(show ? android.R.color.black : android.R.color.darker_gray);
			
			repeat_time_header.setTextColor(color);
			repeat_count_header.setTextColor(color);
			repeat_count_label.setTextColor(color);
			
			repeat_led_alarm_checkbox.setEnabled(show);
			repeat_vibration_alarm_checkbox.setEnabled(show);
			repeat_log_battery_checkbox.setEnabled(show);
			repeat_input_min.setEnabled(show);
			repeat_input_repeat_num.setEnabled(show);
		}
		
		@Override
		public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
			collection.removeView((View) view);
		}
		
		@Override
		public int getCount() {
			return PAGES_NUM;
		}
		
		@Override
		public CharSequence getPageTitle(int i) {
			return "none";
		}
		
		@Override
		public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
			return view == object;
		}
		
		
		int last_page = 0;
		@Override
		public void onPageSelected(int position) {
			if(last_page == PAGE_BING) {
				if(isRandom) {
					Calendar cal = bing_time_picker.date;
					bing = new Bing(
							cal.get(Calendar.HOUR_OF_DAY),
							cal.get(Calendar.MINUTE),
							true,
							false,
							false,
							false,
							bing_led_alarm_checkbox.getColor(),
							false,
							bing_vibration_alarm_checkbox.get_strength(),
							bing_vibration_alarm_checkbox.get_ms()
					);
					
					randomTimer = create_randomTimer();
				}
				else
					bing = create_bing();
			}
			else if(last_page == PAGE_REPEAT) {
				repeat = logic.get_repeat();
				if(repeat == null)
					repeat = create_repeat();
			}
			else {
				bing = create_bing();
			}
			
			update_buttons(position);
			
			last_page = position;
		}
		
		@Override
		public void onPageScrollStateChanged(int state) {}
		
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
		
		void update_buttons(int position) {
			if(position == getCount()-1) {
				btn_save.setText(R.string.btn_save);
				btn_save.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_program_board_black, 0, 0, 0);
			}
			else {
				btn_save.setText(R.string.btn_forward);
				btn_save.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_forward_black, 0);
			}
			
			
			if(position == 0) {
				btn_back.setText(android.R.string.cancel);
				btn_back.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}
			else {
				btn_back.setText(R.string.btn_back);
				btn_back.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_back_black, 0, 0, 0);
			}
		}
		
		RandomTimer create_randomTimer() {
			int min;
			int timeframes_num;
			try {
				min = Integer.parseInt(random_input_min.getText().toString());
				timeframes_num = Integer.parseInt(random_input_timeframes.getText().toString());
			}
			catch(NumberFormatException e) {
				Toast.makeText(activity.getApplicationContext(), R.string.error_number_format, Toast.LENGTH_SHORT).show();
				return null;
			}
			
			if(min <= 0) {
				Toast.makeText(activity.getApplicationContext(), R.string.error_invalid_time, Toast.LENGTH_SHORT).show();
				return null;
			}
			else if(timeframes_num <= 1) {
				Toast.makeText(activity.getApplicationContext(), R.string.error_number_format, Toast.LENGTH_SHORT).show();
				return null;
			}
			
			return new RandomTimer(
					min,
					timeframes_num,
					false,
					bing_log_battery_checkbox.isChecked(),
					bing_led_alarm_checkbox.isChecked(),
					bing_led_alarm_checkbox.getColor(),
					bing_vibration_alarm_checkbox.isChecked(),
					bing_vibration_alarm_checkbox.get_strength(),
					bing_vibration_alarm_checkbox.get_ms()
			);
		}
		Repeat create_repeat() {
			int min = Integer.parseInt(repeat_input_min.getText().toString());
			short repeat_num = Short.parseShort(repeat_input_repeat_num.getText().toString());
			
			if(min < 0) {
				Toast.makeText(activity.getApplicationContext(), R.string.error_invalid_time, Toast.LENGTH_SHORT).show();
				return null;
			}
			return new Repeat(
					min,
					repeat_num,
					repeat_log_battery_checkbox.isChecked(),
					repeat_led_alarm_checkbox.isChecked(),
					repeat_led_alarm_checkbox.getColor(),
					repeat_vibration_alarm_checkbox.isChecked(),
					repeat_vibration_alarm_checkbox.get_strength(),
					repeat_vibration_alarm_checkbox.get_ms()
			);
		}
		Bing create_bing() {
			Calendar cal = bing_time_picker.date;
			return new Bing(
					cal.get(Calendar.HOUR_OF_DAY),
					cal.get(Calendar.MINUTE),
					isRandom,
					false,
					bing_log_battery_checkbox.isChecked(),
					bing_led_alarm_checkbox.isChecked(),
					bing_led_alarm_checkbox.getColor(),
					bing_vibration_alarm_checkbox.isChecked(),
					bing_vibration_alarm_checkbox.get_strength(),
					bing_vibration_alarm_checkbox.get_ms()
			);
		}
		
		
		public RandomTimer get_randomTimer() {
			if(repeat_enable != null && repeat_enable.isChecked())
				randomTimer.repeat = true;
			return randomTimer;
		}
		public Repeat get_repeat() {
			return repeat;
		}
		public Bing get_bing() {
			if(!isRandom && repeat_enable != null && repeat_enable.isChecked())
				bing.repeat = true;
			return bing;
		}
	}
	
	private Activity activity;
	private Board_logic logic;
	
	private boolean isRandom;
	
	Dialog_newBing(boolean _isRandom, Activity _activity, Board_logic _logic) {
		super(_activity);
		activity = _activity;
		logic = _logic;
		isRandom = _isRandom;
	}
	
	public interface OnFinishListener {
		void onFinish(Bing bing);
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.dialog_new_bing);
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		
		Button btn_save = findViewById(R.id.btn_save);
		Button btn_back = findViewById(R.id.btn_back);
		
		ViewPager viewPager = findViewById(R.id.content_box);
		viewPager.setPageMargin(10);
		OwnPagerAdapter adapter = new OwnPagerAdapter(activity, isRandom, logic, btn_back, btn_save);
		viewPager.setAdapter(adapter);
		viewPager.addOnPageChangeListener(adapter);
		adapter.update_buttons(0);
		
		btn_back.setOnClickListener((View view) -> {
			int pos = viewPager.getCurrentItem();
			if(pos > 0)
				viewPager.setCurrentItem(pos - 1);
			else
				dismiss();
		});
		btn_save.setOnClickListener((View view) -> {
			int pos = viewPager.getCurrentItem();
			int count = adapter.getCount();
			
			if(pos < count-1)
				viewPager.setCurrentItem(pos + 1);
			else if(pos == count-1) {
				adapter.onPageSelected(adapter.last_page); //to make sure that changes to the current page are saved
				
				Bing bing = adapter.get_bing();
				Task<?> task_progress = (logic.get_repeat() == null && bing.repeat) ? logic.add_repeat(adapter.get_repeat()) : Task.forResult(null);
				
				if(isRandom && logic.get_randomTimer() == null)
					task_progress = task_progress.continueWithTask(task -> {
						if(task.isFaulted())
							throw task.getError();
						
						return logic.add_random(adapter.get_randomTimer());
					});
				
				task_progress.continueWithTask(task -> {
					if(task.isFaulted()) {
						logic.log(R.string.error_cancel_bing);
						return null;
					}
					
					return logic.add_bing(bing);
				});
				
				dismiss();
			}
		});
	}
}
