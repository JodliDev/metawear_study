package at.jodlidev.metawear.study;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.Serializable;

import androidx.annotation.NonNull;
import at.jodlidev.metawear.study.data.BootMacro;
import at.jodlidev.metawear.study.data.ChargeFeedback;

//import java.text.DateFormat;

/**
 * Created by JodliDev on 25.03.18.
 */

public class Fragment_BatteryOptions extends FragmentBase implements Serializable {
	
	private Element_Vibration_checkbox boot_vibration_checkbox;
	private Element_Led_checkbox boot_led_checkbox;
	private RadioButton boot_led_type_blinking;
	private RadioButton boot_led_type_solid;
	private CheckBox boot_repeat_feedback;
	private EditText boot_input_hour;
	private EditText boot_input_min;
	private Button btn_boot_save;
	
	private FrameLayout charge_feedback_box;
	private CheckBox charge_enabled;
	private Element_Vibration_checkbox charge_vibration_checkbox;
	private Element_Led_checkbox charge_led_checkbox;
	
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		init();
		return inflater.inflate(R.layout.fragment_battery_options, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View rootView, Bundle savedInstanceState) {
		boot_repeat_feedback = rootView.findViewById(R.id.repeat_feedback);
		boot_input_hour = rootView.findViewById(R.id.input_hour);
		boot_input_min = rootView.findViewById(R.id.input_min);
		boot_led_checkbox = rootView.findViewById(R.id.boot_led_checkbox);
		RadioGroup boot_led_type = rootView.findViewById(R.id.led_type);
		boot_led_type_blinking = rootView.findViewById(R.id.led_type_blinking);
		boot_led_type_solid = rootView.findViewById(R.id.led_type_solid);
		boot_vibration_checkbox = rootView.findViewById(R.id.boot_vibration_checkbox);
		btn_boot_save = rootView.findViewById(R.id.btn_boot_save);
		
		charge_feedback_box = rootView.findViewById(R.id.charge_feedback_box);
		charge_enabled = rootView.findViewById(R.id.charge_enabled);
		charge_led_checkbox = rootView.findViewById(R.id.charge_led_checkbox);
		charge_vibration_checkbox = rootView.findViewById(R.id.charge_vibration_checkbox);
		
		
		btn_boot_save.setOnClickListener((View v) -> {
			if(logic.get_bootMacro() == null) {
				logic.add_bootMacro(new BootMacro(
						boot_repeat_feedback.isChecked(),
						Integer.parseInt(boot_input_hour.getText().toString()),
						Integer.parseInt(boot_input_min.getText().toString()),
						boot_led_checkbox.isChecked(),
						boot_led_type.getCheckedRadioButtonId() == R.id.led_type_blinking ? BootMacro.LED_TYPE_BLINKING : BootMacro.LED_TYPE_SOLID,
						boot_led_checkbox.getColor(),
						boot_vibration_checkbox.isChecked(),
						boot_vibration_checkbox.get_strength(),
						boot_vibration_checkbox.get_ms()));
			}
		});
		
		View btn_charge_save = rootView.findViewById(R.id.btn_charge_save);
		
		btn_charge_save.setOnClickListener((View v) -> {
			if(charge_enabled.isChecked())
				logic.add_chargeFeedback(new ChargeFeedback(
						charge_led_checkbox.isChecked(),
						charge_led_checkbox.getColor(),
						charge_vibration_checkbox.isChecked(),
						charge_vibration_checkbox.get_strength(),
						charge_vibration_checkbox.get_ms()));
			else
				logic.remove_chargeFeedback();
		});
		
		
		
		update_ui();
		
		//must happen after update_ui():
		
		boot_led_checkbox.setOnCheckedChangeListener((CompoundButton v, boolean isChecked) -> {
			switch(v.getId()) {
				case R.id.item_led_checkbox: //this is the id from the checkbox inside Item_led_checkbox
					boot_led_type.setVisibility(boot_led_checkbox.isChecked() ? View.VISIBLE : View.GONE);
					break;
			}
		});
		charge_enabled.setOnCheckedChangeListener((CompoundButton v, boolean isChecked) -> {
			charge_led_checkbox.setEnabled(isChecked);
			charge_vibration_checkbox.setEnabled(isChecked);
		});
	}
	
	void update_ui() {
		if(boot_repeat_feedback == null)
			return;
		
		BootMacro macro = logic.get_bootMacro();
		boolean b;
		if(macro != null) {
			b = false;
			boot_repeat_feedback.setChecked(macro.repeat_feedback);
			boot_input_hour.setText(Integer.toString(macro.hour));
			boot_input_min.setText(Integer.toString(macro.min));
			boot_led_checkbox.setChecked(macro.led);
			switch(macro.led_type) {
				case BootMacro.LED_TYPE_BLINKING:
					boot_led_type_blinking.setChecked(true);
					break;
				case BootMacro.LED_TYPE_SOLID:
					boot_led_type_solid.setChecked(true);
					break;
			}
			boot_led_checkbox.setColor(macro.color);
			boot_vibration_checkbox.setChecked(macro.vibration);
			boot_vibration_checkbox.set_strength(macro.vibration_strength);
			boot_vibration_checkbox.set_ms(macro.vibration_ms);
			
			btn_boot_save.setEnabled(false);
		}
		else {
			b = true;
			boot_repeat_feedback.setChecked(false);
			boot_led_checkbox.setChecked(false);
			boot_vibration_checkbox.setChecked(false);
			btn_boot_save.setText(R.string.btn_save_to_board);
			btn_boot_save.setEnabled(true);
		}
		
		boot_repeat_feedback.setEnabled(b);
		boot_input_hour.setEnabled(b);
		boot_input_min.setEnabled(b);
		boot_led_checkbox.setEnabled(b);
		boot_vibration_checkbox.setEnabled(b);
		boot_led_type_blinking.setEnabled(b);
		boot_led_type_solid.setEnabled(b);
		
		
		
		
		ChargeFeedback chargeFeedback = logic.get_chargeFeedback();
		
		if(chargeFeedback == null) {
			charge_feedback_box.setForeground(null);
			b = false;
		}
		else {
			b = true;
			
			charge_led_checkbox.setChecked(chargeFeedback.led);
			charge_vibration_checkbox.setChecked(chargeFeedback.vibration);
			
			charge_led_checkbox.setColor(chargeFeedback.color);
			charge_vibration_checkbox.setChecked(chargeFeedback.vibration);
			charge_vibration_checkbox.set_strength(chargeFeedback.vibration_strength);
			charge_vibration_checkbox.set_ms(chargeFeedback.vibration_ms);
			
		if(chargeFeedback.exists_on_board)
			charge_feedback_box.setForeground(null);
		else
			charge_feedback_box.setForeground(getResources().getDrawable(R.drawable.disabled_overlay));
		}
		charge_enabled.setChecked(b);
		charge_led_checkbox.setEnabled(b);
		charge_vibration_checkbox.setEnabled(b);
	}
	
	
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		boot_vibration_checkbox = null;
		boot_led_checkbox = null;
		boot_led_type_blinking = null;
		boot_led_type_solid = null;
		boot_repeat_feedback = null;
		boot_input_hour = null;
		boot_input_min = null;
		btn_boot_save = null;
		
		charge_feedback_box = null;
		charge_enabled = null;
		charge_vibration_checkbox = null;
		charge_led_checkbox = null;
	}
}
