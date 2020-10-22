package at.jodlidev.metawear.study;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tooltip.Tooltip;

import java.io.Serializable;

import androidx.annotation.NonNull;
import at.jodlidev.metawear.study.data.Battery;
import at.jodlidev.metawear.study.data.Switch_route;


/**
 * Created by JodliDev on 25.03.18.
 */

public class Fragment_Switch extends FragmentBase implements Serializable {
	
	private FrameLayout states_box;
	
	private TextView acc_configured_time;
	private TextView switch_configured_time;
	
	private View log_acceleration_state;
	private View log_length_of_click_state;
	private View log_number_of_clicks_state;
	private View vibration_state;
	private ImageView led_state;
	
	private View number_timeout_box;
	private ImageView number_timeout_led_state;
	private View number_timeout_vibration_state;
	
	private View battery_feedback_box;
	private TextView battery_threshold;
	private ImageView battery_led_state;
	private View battery_vibration_state;
	
	private View macro_already_saved_info;
	private Button btn_edit;
	
	
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		init();
		return inflater.inflate(R.layout.fragment_switch, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View rootView, Bundle savedInstanceState) {
		states_box = rootView.findViewById(R.id.states_box);
		
		acc_configured_time = rootView.findViewById(R.id.acc_configured_time);
		switch_configured_time = rootView.findViewById(R.id.switch_configured_time);
		
		log_acceleration_state = rootView.findViewById(R.id.log_acceleration_state);
		log_length_of_click_state = rootView.findViewById(R.id.log_length_of_click_state);
		log_number_of_clicks_state = rootView.findViewById(R.id.log_number_of_clicks_state);
		vibration_state = rootView.findViewById(R.id.vibration_state);
		led_state = rootView.findViewById(R.id.led_state);
		
		number_timeout_box = rootView.findViewById(R.id.number_timeout_box);
		number_timeout_led_state = rootView.findViewById(R.id.number_timeout_led_state);
		number_timeout_vibration_state = rootView.findViewById(R.id.number_timeout_vibration_state);
		
		battery_feedback_box = rootView.findViewById(R.id.battery_feedback_box);
		battery_threshold = rootView.findViewById(R.id.battery_threshold);
		battery_led_state = rootView.findViewById(R.id.battery_led_state);
		battery_vibration_state = rootView.findViewById(R.id.battery_vibration_state);
		
		macro_already_saved_info = rootView.findViewById(R.id.macro_already_saved_info);
		btn_edit = rootView.findViewById(R.id.btn_edit);
		
		
		btn_edit.setOnClickListener((View v) -> {
			(new Dialog_setSwitch(getContext(), logic)).show();
		});
		
		
		switch_configured_time.setOnClickListener((View v) -> {
			open_timeTooltip(switch_configured_time, logic.get_switch_route().switch_log_timestamp);
		});
		acc_configured_time.setOnClickListener((View v) -> {
			open_timeTooltip(acc_configured_time, logic.get_switch_route().acc_log_timestamp);
		});
		
		
		update_ui();
	}
	
	private String getDateTimeString(long timestamp) {
		java.text.DateFormat date_format = android.text.format.DateFormat.getDateFormat(getContext());
		java.text.DateFormat time_format = java.text.DateFormat.getTimeInstance();
		
		return date_format.format(timestamp) + " - " + time_format.format(timestamp);
	}
	
	private void open_timeTooltip(View view, long timestamp) {
		new Tooltip.Builder(view)
				.setText(getString(R.string.colon_timestamp) + " " + timestamp)
				.setCornerRadius(25f)
				.setCancelable(true)
				.setDismissOnClick(true)
				.setTextColor(Color.WHITE)
				.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
				.show();
	}
	
	void update_ui() {
		if(log_acceleration_state == null) //if one is null, then all are...
			return;
		
		Switch_route route = logic.get_switch_route();
		Battery battery = logic.board_data.battery;
		
		if(route == null) {
			route = new Switch_route();
			switch_configured_time.setVisibility(View.GONE);
			acc_configured_time.setVisibility(View.GONE);
		}
		else {
			if(route.restore_on_boot) {
				macro_already_saved_info.setVisibility(View.VISIBLE);
				btn_edit.setEnabled(false);
			}
			
			if(route.exists_on_board)
				states_box.setForeground(null);
			else
				states_box.setForeground(getResources().getDrawable(R.drawable.disabled_overlay));
			
			if(route.log_switch_data == Switch_route.LOG_SWITCH_NONE)
				switch_configured_time.setVisibility(View.GONE);
			else {
				switch_configured_time.setVisibility(View.VISIBLE);
				switch_configured_time.setText(Html.fromHtml(getString(R.string.brackets_switch_configured_time, getDateTimeString(route.switch_log_timestamp))));
			}
			
			if(!route.log_acc_data)
				acc_configured_time.setVisibility(View.GONE);
			else {
				acc_configured_time.setVisibility(View.VISIBLE);
				acc_configured_time.setText(Html.fromHtml(getString(R.string.brackets_acc_configured_time, getDateTimeString(route.acc_log_timestamp))));
			}
		}
		
		
		log_acceleration_state.setVisibility(route.log_acc_data ? View.VISIBLE : View.GONE);
		log_length_of_click_state.setVisibility(route.log_switch_data == Switch_route.LOG_LENGTH_OF_CLICK ? View.VISIBLE : View.GONE);
		log_number_of_clicks_state.setVisibility(
				(route.log_switch_data == Switch_route.LOG_NUMBER_OF_CLICKS)
						? View.VISIBLE
						: View.GONE
		);
		vibration_state.setVisibility(route.vibration ? View.VISIBLE : View.GONE);
		led_state.setVisibility(route.led_behaviour != Switch_route.LED_NONE ? View.VISIBLE : View.GONE);
		
		led_state.setColorFilter(route.led_behaviour == Switch_route.LED_BLINKING ? Color.parseColor(route.color) : Color.BLACK);
		
		number_timeout_box.setVisibility(route.log_switch_data==Switch_route.LOG_NUMBER_OF_CLICKS ? View.VISIBLE : View.GONE);
		number_timeout_vibration_state.setVisibility(route.log_number_vibration ? View.VISIBLE : View.GONE);
		number_timeout_led_state.setColorFilter(Color.parseColor(route.log_number_color));
		number_timeout_led_state.setVisibility(route.log_number_led ? View.VISIBLE : View.GONE);
		
		battery_feedback_box.setVisibility(battery.battery_for_switch ? View.VISIBLE : View.GONE);
		battery_threshold.setText(Integer.toString(battery.battery_low_threshold));
		battery_led_state.setVisibility(battery.battery_low_led ? View.VISIBLE : View.GONE);
		battery_led_state.setColorFilter(Color.parseColor(battery.battery_low_color));
		battery_vibration_state.setVisibility(battery.battery_low_vibration ? View.VISIBLE : View.GONE);
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		if(logic.is_ready)
			update_ui();
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		states_box = null;
		log_acceleration_state = null;
		log_length_of_click_state = null;
		log_number_of_clicks_state = null;
		vibration_state = null;
		led_state = null;
		
		number_timeout_box = null;
		number_timeout_led_state = null;
		number_timeout_vibration_state = null;
		
		battery_feedback_box = null;
		battery_threshold = null;
		battery_led_state = null;
		battery_vibration_state = null;
		
		btn_edit = null;
		macro_already_saved_info = null;
	}
}
