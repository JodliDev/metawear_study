package at.jodlidev.metawear.study;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.module.Settings;

/**
 * Created by david on 24.03.18.
 */

public class Dialog_choose_unknown_download_type extends Dialog implements View.OnClickListener {
	private Data data;
	private String identifier;
	
	private RadioGroup route_type;
	private OnFinishListener listener;
	private RadioButton acceleration;
	private RadioButton length_of_click;
	private RadioButton number_of_clicks;
	private RadioButton bing_battery;
	private RadioButton unknown;
	
	Dialog_choose_unknown_download_type(Activity _activity, String identifier, OnFinishListener listener, Data _data) {
		super(_activity);
		this.identifier = identifier;
		this.data = _data;
		this.listener = listener;
		
		setCancelable(false);
	}
	
	
	public interface OnFinishListener {
		void onFinish(int type);
		void onCancel();
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.dialog_choose_unknown_download_type);
		
		route_type = findViewById(R.id.route_type);
		
		acceleration = findViewById(R.id.acceleration);
		length_of_click = findViewById(R.id.length_of_click);
		number_of_clicks = findViewById(R.id.number_of_clicks);
		bing_battery = findViewById(R.id.bing_battery);
		unknown = findViewById(R.id.unknown);
		
		((TextView) findViewById(R.id.identifier_text)).setText(identifier);
		
		if(data != null) {
			findViewById(R.id.not_started).setVisibility(View.GONE);
			Class<?>[] types = data.types();
			
			acceleration.setEnabled(false);
			length_of_click.setEnabled(false);
			number_of_clicks.setEnabled(false);
			bing_battery.setEnabled(false);
			
			for(Class<?> type : types) {
				if(type.equals(Acceleration.class))
					acceleration.setEnabled(true);
				else if(type.equals(Long.class)) {
					length_of_click.setEnabled(true);
					number_of_clicks.setEnabled(true);
				}
				else if(type.equals(Settings.BatteryState.class))
					bing_battery.setEnabled(true);
			}
		}
		
		findViewById(R.id.btn_cancel).setOnClickListener(this);
		findViewById(R.id.btn_ok).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case  R.id.btn_ok:
				int type;
				switch(route_type.getCheckedRadioButtonId()) {
					case R.id.number_of_clicks:
						type = Download_formatter.SWITCH_NUM;
						break;
					case R.id.length_of_click:
						type = Download_formatter.SWITCH_LENGTH;
						break;
					case R.id.acceleration:
						type = Download_formatter.ACC;
						break;
					case R.id.bing_battery:
						type = Download_formatter.BING_BATTERY;
						break;
					case R.id.unknown:
						type = Download_formatter.UNKNOWN;
						break;
					default: //no selection
						return;
				}
				listener.onFinish(type);
				break;
			case R.id.btn_cancel:
				listener.onCancel();
		}
		dismiss();
	}
}
