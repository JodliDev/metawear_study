package at.jodlidev.metawear.study;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import at.jodlidev.metawear.study.data.Battery;
import at.jodlidev.metawear.study.data.Switch_route;

/**
 * Created by JodliDev on 14.05.2019.
 */
public class Dialog_setSwitch extends Dialog {
	public static class OwnPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
		View.OnClickListener uncheck_radioButton_listener = (View view) -> {
			RadioGroup group = (RadioGroup) view.getParent();
			int checkedId = group.getCheckedRadioButtonId();
			if(group.getTag() != null && (int) group.getTag() == checkedId) {
				group.clearCheck();
				group.setTag(0);
			}
			else
				group.setTag(checkedId);
		};
		
		Switch_route route;
		Battery battery;
		Board_logic logic;
		
		private Button btn_save;
		private Button btn_back;
		
		private CheckBox log_acc_checkbox;
		private RadioButton log_number_of_clicks;
		private RadioButton log_length_of_click;
		
		private Element_Vibration_checkbox vibration_checkbox;
		private RadioButton led_type_blinking;
		private RadioButton led_type_number_of_clicks;
		private RadioButton led_type_length_of_click;
		private Spinner led_color;
		
		private TextView threshold_label;
		private CheckBox battery_feedback_checkbox;
		private EditText battery_threshold;
		private Element_Vibration_checkbox vibration_battery_checkbox;
		private Element_Led_checkbox led_battery_checkbox;
		
		private View number_timeout_header;
		private Element_Led_checkbox number_timeout_led_checkbox;
		private Element_Vibration_checkbox number_timeout_vibration_checkbox;
		
		LayoutInflater inflater;
		OwnPagerAdapter(Context context, Board_logic _logic, Button _btn_back, Button _btn_save) {
			inflater = LayoutInflater.from(context);
			logic = _logic;
			route = logic.get_switch_route();
			if(route == null)
				route = new Switch_route();
			battery = logic.get_battery();
			btn_back = _btn_back;
			btn_save = _btn_save;
		}
		
		@Override
		@NonNull
		public Object instantiateItem(@NonNull ViewGroup container, int position) {
			View view;
			switch(position) {
				default:
				case PAGE_LOGGING:
					view = inflater.inflate(R.layout.item_set_switch_logging, container, false);
					
					log_acc_checkbox = view.findViewById(R.id.log_acc_checkbox);
					log_number_of_clicks = view.findViewById(R.id.log_number_of_clicks);
					log_length_of_click = view.findViewById(R.id.log_length_of_click);
					
					//
					//listener
					//
					log_number_of_clicks.setOnClickListener(uncheck_radioButton_listener);
					log_length_of_click.setOnClickListener(uncheck_radioButton_listener);
					
					//
					//preload:
					//
					if(route != null) {
						log_acc_checkbox.setChecked(route.log_acc_data);
						log_length_of_click.setChecked(route.log_switch_data == Switch_route.LOG_LENGTH_OF_CLICK);
						log_number_of_clicks.setChecked(route.log_switch_data == Switch_route.LOG_NUMBER_OF_CLICKS);
					}
					
					init_radioGroup_for_uncheck(log_number_of_clicks);
					break;
				case PAGE_FEEDBACK:
					view = inflater.inflate(R.layout.item_set_switch_feedback, container, false);
					
					vibration_checkbox = view.findViewById(R.id.vibration_checkbox);
					led_type_blinking = view.findViewById(R.id.led_type_blinking);
					led_type_number_of_clicks = view.findViewById(R.id.led_type_number_of_clicks);
					led_type_length_of_click = view.findViewById(R.id.led_type_length_of_click);
					led_color = view.findViewById(R.id.led_color);
					number_timeout_header = view.findViewById(R.id.number_timeout_header);
					number_timeout_led_checkbox = view.findViewById(R.id.number_timeout_led_checkbox);
					number_timeout_vibration_checkbox = view.findViewById(R.id.number_timeout_vibration_checkbox);
					
					//
					//listener
					//
					led_type_blinking.setOnClickListener(uncheck_radioButton_listener);
					led_type_number_of_clicks.setOnClickListener(uncheck_radioButton_listener);
					led_type_length_of_click.setOnClickListener(uncheck_radioButton_listener);
					
					CompoundButton.OnCheckedChangeListener led_color_enabler = (CompoundButton v, boolean checked) -> {
						led_color.setEnabled(!led_type_number_of_clicks.isChecked());
					};
					
					led_type_blinking.setOnCheckedChangeListener(led_color_enabler);
					led_type_number_of_clicks.setOnCheckedChangeListener(led_color_enabler);
					led_type_length_of_click.setOnCheckedChangeListener(led_color_enabler);
					
					//
					//preload:
					//
					if(route != null) {
						vibration_checkbox.setChecked(route.vibration);
						vibration_checkbox.set_strength(route.vibration_strength);
						vibration_checkbox.set_ms(route.vibration_ms);
						
						switch(route.led_behaviour) {
							case Switch_route.LED_BLINKING:
								led_type_blinking.setChecked(true);
								break;
							case Switch_route.LED_VISUALIZE_BUTTON:
								if(route.log_switch_data == Switch_route.LOG_LENGTH_OF_CLICK)
									led_type_length_of_click.setChecked(true);
								else
									led_type_number_of_clicks.setChecked(true);
						}
						
						if(route.color != null) {
							for(int i = 0, max = led_color.getCount(); i < max; ++i) {
								if(led_color.getItemAtPosition(i).toString().equals(route.color)) {
									led_color.setSelection(i);
									break;
								}
							}
						}
						
						number_timeout_led_checkbox.setChecked(route.log_number_led);
						number_timeout_led_checkbox.setColor(route.log_number_color);
						number_timeout_vibration_checkbox.setChecked(route.log_number_vibration);
						number_timeout_vibration_checkbox.set_ms(route.log_number_vibration_ms);
						number_timeout_vibration_checkbox.set_strength(route.log_number_vibration_strength);
					}
					
					init_radioGroup_for_uncheck(led_type_blinking);
					break;
				case PAGE_BATTERY:
					view = inflater.inflate(R.layout.item_set_switch_battery, container, false);
					
					battery_feedback_checkbox = view.findViewById(R.id.battery_feedback_checkbox);
					threshold_label = view.findViewById(R.id.threshold_label);
					battery_threshold = view.findViewById(R.id.battery_threshold);
					vibration_battery_checkbox = view.findViewById(R.id.vibration_battery_checkbox);
					led_battery_checkbox = view.findViewById(R.id.led_battery_checkbox);
					
					battery_feedback_checkbox.setOnCheckedChangeListener((CompoundButton v, boolean checked) -> {
						battery_threshold.setEnabled(checked);
						vibration_battery_checkbox.setEnabled(checked);
						led_battery_checkbox.setEnabled(checked);
						threshold_label.setTextColor(threshold_label.getContext().getResources().getColor(checked ? android.R.color.black : android.R.color.darker_gray));
						
					});
					
					battery_feedback_checkbox.setChecked(battery.battery_for_switch);
					battery_threshold.setText(Integer.toString(battery.battery_low_threshold));
					vibration_battery_checkbox.setChecked(battery.battery_low_vibration);
					vibration_battery_checkbox.set_ms(battery.battery_low_vibration_ms);
					vibration_battery_checkbox.set_strength(battery.battery_low_vibration_strength);
					led_battery_checkbox.setChecked(battery.battery_low_led);
					led_battery_checkbox.setColor(battery.battery_low_color);
					break;
			}
			
			container.addView(view);
			return view;
		}
		
		int last_page = 0;
		@Override
		public void onPageSelected(int position) {
			//
			//save data from last page
			//
			switch(last_page) {
				case PAGE_LOGGING:
					route.log_switch_data = log_length_of_click.isChecked()
							? Switch_route.LOG_LENGTH_OF_CLICK
							: (log_number_of_clicks.isChecked() ? Switch_route.LOG_NUMBER_OF_CLICKS : Switch_route.LOG_SWITCH_NONE);
					route.log_acc_data = log_acc_checkbox.isChecked();
					
					if(route.log_switch_data == Switch_route.LOG_NUMBER_OF_CLICKS) {
						number_timeout_header.setVisibility(View.VISIBLE);
						number_timeout_led_checkbox.setVisibility(View.VISIBLE);
						number_timeout_vibration_checkbox.setVisibility(View.VISIBLE);
					}
					else {
						number_timeout_header.setVisibility(View.GONE);
						number_timeout_led_checkbox.setVisibility(View.GONE);
						number_timeout_vibration_checkbox.setVisibility(View.GONE);
					}
					
					break;
				case PAGE_FEEDBACK:
					route.vibration = vibration_checkbox.isChecked();
					route.vibration_ms = vibration_checkbox.get_ms();
					route.vibration_strength = vibration_checkbox.get_strength();
					route.led_behaviour = led_type_blinking.isChecked()
							? Switch_route.LED_BLINKING
							: (led_type_number_of_clicks.isChecked() || led_type_length_of_click.isChecked() ? Switch_route.LED_VISUALIZE_BUTTON : Switch_route.LED_NONE);
					route.color = led_color.getSelectedItem().toString();
					
					route.log_number_led = number_timeout_led_checkbox.isChecked();
					route.log_number_color = number_timeout_led_checkbox.getColor();
					route.log_number_vibration = number_timeout_vibration_checkbox.isChecked();
					route.log_number_vibration_ms = number_timeout_vibration_checkbox.get_ms();
					route.log_number_vibration_strength = number_timeout_vibration_checkbox.get_strength();
					
					break;
				case PAGE_BATTERY:
					battery.battery_for_switch = battery_feedback_checkbox.isChecked();
					battery.battery_low_threshold = Integer.parseInt(battery_threshold.getText().toString());
					battery.battery_low_vibration = vibration_battery_checkbox.isChecked();
					battery.battery_low_vibration_ms = vibration_battery_checkbox.get_ms();
					battery.battery_low_vibration_strength = vibration_battery_checkbox.get_strength();
					battery.battery_low_led = led_battery_checkbox.isChecked();
					battery.battery_low_color = led_battery_checkbox.getColor();
					break;
			}
			
			//
			//prepare this page
			//
			switch(position) {
				case PAGE_FEEDBACK:
					led_type_length_of_click.setVisibility(route.log_switch_data == Switch_route.LOG_LENGTH_OF_CLICK ? View.VISIBLE : View.GONE);
					if(route.log_switch_data == Switch_route.LOG_NUMBER_OF_CLICKS) {
						led_type_number_of_clicks.setVisibility(View.VISIBLE);
					}
					else {
						led_type_number_of_clicks.setVisibility(View.GONE);
						led_type_number_of_clicks.setChecked(false);
					}
					break;
			}
			
			if(position == PAGES_NUM-1) {
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
			
			last_page = position;
		}
		
		@Override
		public void onPageScrollStateChanged(int state) {}
		
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
		
		private void init_radioGroup_for_uncheck(RadioButton v) {
			RadioGroup parent = (RadioGroup) v.getParent();
			parent.setTag(parent.getCheckedRadioButtonId());
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
	}
	
	private static final int PAGE_LOGGING = 0;
	private static final int PAGE_FEEDBACK = 1;
	private static final int PAGE_BATTERY = 2;
	private static final int PAGES_NUM = 3;
	
	
	Board_logic logic;
	Dialog_setSwitch(Context context, Board_logic _logic) {
		super(context);
		logic = _logic;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		setTitle(R.string.dialog_title_add_Category);
		setContentView(R.layout.dialog_set_switch);
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		
		Button btn_save = findViewById(R.id.btn_save);
		Button btn_back = findViewById(R.id.btn_back);
		final CheckBox restore_switch_config = findViewById(R.id.restore_switch_config);
		
		ViewPager viewPager = findViewById(R.id.content_box);
		viewPager.setPageMargin(10);
		OwnPagerAdapter adapter = new OwnPagerAdapter(getContext(), logic, btn_back, btn_save);
		viewPager.setAdapter(adapter);
		viewPager.addOnPageChangeListener(adapter);
		
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
				logic.set_up_switch_route(adapter.route, adapter.battery, restore_switch_config.isChecked());
				dismiss();
			}
		});
		
		if(logic != null) {
			Switch_route switchRoute = logic.get_switch_route();
			
			if(switchRoute != null && switchRoute.restore_on_boot) {
				restore_switch_config.setChecked(false);
				restore_switch_config.setEnabled(false);
				findViewById(R.id.restore_switch_config_info).setVisibility(View.VISIBLE);
			}
		}
	}
}
