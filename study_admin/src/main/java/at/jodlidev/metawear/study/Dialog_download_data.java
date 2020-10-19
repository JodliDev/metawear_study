package at.jodlidev.metawear.study;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import at.jodlidev.metawear.study.data.Battery;
import at.jodlidev.metawear.study.data.Bing;
import at.jodlidev.metawear.study.data.Switch_route;

/**
 * Created by JodliDev on 24.03.18.
 */

public class Dialog_download_data extends Dialog implements View.OnClickListener {
	CheckBox switch_acc_combined; //needed in Dialog_anon_download_data
	private OnFinishListener listener;
	private Board_logic logic;
	
	Dialog_download_data(Activity _activity, Board_logic _logic, OnFinishListener _listener) {
		super(_activity);
		listener = _listener;
		logic = _logic;
	}
	
	
	public interface OnFinishListener {
		void onFinish(boolean switch_acc_combined);
		void onCancel();
	}
	
	
	int get_contentView() {
		return R.layout.dialog_download_data;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(get_contentView());
		
		switch_acc_combined = findViewById(R.id.switch_acc_combined);
		
		Switch_route switch_route = logic.get_switch_route();
		Battery battery = logic.get_battery();
		if(switch_route != null && battery != null && (switch_route.log_acc_data || switch_route.log_switch_data != Switch_route.LOG_SWITCH_NONE || battery.battery_for_bing)) {
			if(switch_route.log_acc_data &&  switch_route.log_switch_data != Switch_route.LOG_SWITCH_NONE) {
				switch_acc_combined.setVisibility(View.VISIBLE);
				switch_acc_combined.setChecked(true);
			}
		}
		else
			findViewById(R.id.btn_ok).setEnabled(false);
			
		
		findViewById(R.id.btn_ok).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
		
		setOnCancelListener((DialogInterface dialog) -> {
			listener.onCancel();
		});
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case  R.id.btn_ok:
				listener.onFinish(switch_acc_combined.isChecked());
				break;
			case R.id.btn_cancel:
				listener.onCancel();
		}
		dismiss();
	}
}
