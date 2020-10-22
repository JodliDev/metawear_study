package at.jodlidev.metawear.study;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.module.GyroBmi160;
import com.mbientlab.metawear.module.Timer;

import java.util.Arrays;

/**
 * Created by david on 03.04.18.
 */

public class Dialog_test_random extends Dialog implements android.view.View.OnClickListener{
	private Board_logic logic;
	private Activity activity;
	
	
	private EditText input_timeframes;
	private TextView show_data_text;
	private Button btn_start;
	private boolean started = false;
	
	private int route_id;
	private byte timer_id;
	
	private int part_num;
	
	private Integer[] nums;
	private int current;
//	private Accelerometer accModule;
	
	
	private Dialog_test_random(Activity _activity, Board_logic _logic) {
		super(_activity);
		activity = _activity;
		logic = _logic;
	}
	
	private void show_data() {
		StringBuilder r = new StringBuilder();
		int i = 1;
		for(int num : nums) {
			r.append(i++);
			r.append(": ");
			r.append(num);
			r.append("\n");
		}
//		Log.d("qwe", r.toString());
		activity.runOnUiThread(() -> {
			show_data_text.setText(r.toString());
		});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.dialog_test_random);
		
		btn_start = findViewById(R.id.btn_start);
		show_data_text = findViewById(R.id.show_data_text);
		input_timeframes = findViewById(R.id.input_timeframes);
		
		btn_start.setOnClickListener(this);
		findViewById(R.id.btn_close).setOnClickListener(this);
		
		if (logic.gyroModule == null) {
			Toast.makeText(activity, R.string.error_no_gyroModule, Toast.LENGTH_SHORT).show();
			dismiss();
		}
		
		logic.gyroModule.configure()
				.odr(GyroBmi160.OutputDataRate.ODR_25_HZ)
				.range(GyroBmi160.Range.FSR_125)
				.commit();
	}
	
	private void reset() {
		Route route2 = logic.board.lookupRoute(route_id);
		if(route2 != null)
			route2.remove();
		
		Timer.ScheduledTask timer = logic.timerModule.lookupScheduledTask(timer_id);
		if(timer != null)
			timer.remove();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_start:
				if(started) {
					started = false;
					btn_start.setText(R.string.start);
					
					reset();
				}
				else {
					started = true;
					btn_start.setText(R.string.stop);
					
					part_num = Integer.parseInt(input_timeframes.getText().toString());
					
					nums = new Integer[part_num];
					Arrays.fill(nums, 0);
					current = 0;
					show_data();
					
					logic.timerModule.scheduleAsync(100, (short) 1, true, () -> {
						logic.gyroModule.angularVelocity().start();
						logic.gyroModule.start();
					}).continueWithTask(task -> {
						if(task.isFaulted())
							throw task.getError();
						
						Timer.ScheduledTask scheduledTask = task.getResult();
						timer_id = scheduledTask.id();
						
						return logic.gyroModule.angularVelocity().addRouteAsync((RouteComponent source) -> {
							
							RouteComponent finish_source = logic.create_random_route(source, timer_id, part_num);
							finish_source.stream((Data data, Object... env) -> {
								int value = data.value(Integer.class);
								Log.d("qwe", "value: "+value);
								if(current == -1){
									current = value;
								}
								
								if(value == 0) {
									++nums[current];
									current = -1;
									logic.singlePulseLed("RED");
									show_data();
									logic.new_random_circle();
									logic.gyroModule.angularVelocity().start();
									logic.gyroModule.start();
								}
								else
									logic.singlePulseLed("GREEN");
							});
							
						});
					}).continueWithTask(task -> {
						if (task.isFaulted()) {
							Toast.makeText(activity.getApplicationContext(), task.getError().toString(), Toast.LENGTH_LONG).show();
						}
						else {
							route_id = task.getResult().id();
							
							logic.gyroModule.angularVelocity().start();
							logic.gyroModule.start();
						}
						return null;
					});
						
				}
				break;
			default:
				dismiss();
		}
	}
	
	@Override
	public void dismiss() {
		reset();
		super.dismiss();
	}
	
	
	public static void open(Activity activity, Board_logic logic) {
		new AlertDialog.Builder(activity)
				.setTitle(R.string.confirm)
				.setMessage(R.string.confirm_reset)
				.setIcon(R.drawable.ic_warning_black)
				.setPositiveButton(android.R.string.yes, (DialogInterface dialog_confirm, int whichButton_confirm) -> {
					logic.reset_board(false).continueWith(task -> {
						activity.runOnUiThread(() -> (new Dialog_test_random(activity, logic)).show());
						return null;
					});
				})
				.setNegativeButton(android.R.string.no, null).show();
		
		
		
	}
}
