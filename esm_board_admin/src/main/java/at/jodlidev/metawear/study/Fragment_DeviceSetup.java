package at.jodlidev.metawear.study;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import android.os.Bundle;

import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mbientlab.metawear.AnonymousRoute;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.DeviceInformation;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.module.Logging;
import com.mbientlab.metawear.module.Settings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.core.app.ActivityCompat;
import at.jodlidev.metawear.study.data.DataBox;
import at.jodlidev.metawear.study.data.Switch_route;
import bolts.Task;

public class Fragment_DeviceSetup extends FragmentBase implements Serializable {
	private final static int INTENT_EXPORT_CONFIG = 10;
	private final static int INTENT_IMPORT_CONFIG = 11;
	private static final int REQUEST_PERMISSION = 101;
	
	private Dialog_download_data.OnFinishListener download_dialog_listener = new Dialog_download_data.OnFinishListener() {
		@Override
		public void onFinish(boolean switch_acc_combined) {
			if(switch_acc_combined)
				formatter.enable_switch_acc_combination();
			
			Downloader.start_download(logic.board.getModule(Logging.class), (long nEntriesLeft, long totalEntries) -> {
				update_progress(getString(R.string.info_download_update, totalEntries - nEntriesLeft, totalEntries));
			}).continueWith((Task<Void> download_task) -> {
				
				//categorizing and saving data that were not recognized properly:
				if(Downloader.save_unknown(getActivity(), () -> complete_download(switch_acc_combined)))
					update_progress(getString(R.string.info_download_identying_unknown));
				else
					complete_download(switch_acc_combined);
				return null;
			});
		}
		
		@Override
		public void onCancel() {
			finish_anon_download();
		}
	};
	
	private Download_formatter formatter;
	private Map<String, Integer> anon_identifier_types;
	
	
	private Route battery_route = null;
	
	
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		init();
		return inflater.inflate(R.layout.fragment_device_setup, container, false);
	}
	
	private boolean firmwareWarning_was_issued = false;
	
	
	@Override
	public void onViewCreated(@NonNull View rootView, Bundle savedInstanceState) {
		TextView device_name_info = rootView.findViewById(R.id.device_name_info);
		TextView model_info = rootView.findViewById(R.id.model_info);
		TextView firmware_info = rootView.findViewById(R.id.firmware_info);
		TextView battery_info = rootView.findViewById(R.id.battery_info);
		TextView user_notes = rootView.findViewById(R.id.user_notes);
		
		
		device_name_info.setOnClickListener((View v) -> {
			final EditText e = new EditText(getContext());
			e.setFilters(new InputFilter[] {new InputFilter.LengthFilter(8)});
			e.setText(device_name_info.getText());
			
			(new AlertDialog.Builder(getContext()).setView(e)
					.setTitle(R.string.colon_device_name)
					.setCancelable(true)
					.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int whichButton) -> {
						String new_name = e.getText().toString();
						int l = new_name.length();
						if(l < 3 || l > 8)
							Toast.makeText(getContext(), R.string.error_invalid_name_length, Toast.LENGTH_SHORT).show();
						else {
							logic.settingsModule.editBleAdConfig()
									.deviceName(new_name)
									.commit();
							log(getString(R.string.info_device_name_changed, new_name));
							device_name_info.setText(new_name);
						}
					})
					.setNegativeButton(R.string.cancel, null)
			).show();
		});
		
		user_notes.setText(logic.board_data.userNotes);
		user_notes.setOnClickListener((View v) -> {
			final EditText e = new EditText(getContext());
			e.setText(user_notes.getText());
			
			(new AlertDialog.Builder(getContext()).setView(e)
					.setTitle(R.string.colon_user_notes)
					.setCancelable(true)
					.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int whichButton) -> {
						String notes = e.getText().toString();
						logic.set_userNotes(notes);
						user_notes.setText(notes);
					})
					.setNegativeButton(R.string.cancel, null)
			).show();
		});
		
		
		//
		//Buttons
		//
		
		//new
		rootView.findViewById(R.id.btn_new).setOnClickListener((View view) ->
			new AlertDialog.Builder(getActivity())
					.setTitle(R.string.confirm)
					.setMessage(R.string.confirm_reset)
					.setIcon(R.drawable.ic_warning_black)
					.setPositiveButton(android.R.string.yes, (DialogInterface dialog, int whichButton) -> {
						logic.reset_board(true);
						((Communication) Objects.requireNonNull(getActivity())).goto_site(Communication.SITE_CONFIGURATION);
					})
					.setNegativeButton(android.R.string.no, null).show()
		);
		
		//continue
		rootView.findViewById(R.id.btn_continue).setOnClickListener((View view) ->
				((Communication) Objects.requireNonNull(getActivity())).goto_site(Communication.SITE_CONFIGURATION)
		);
		
		//empty log
		rootView.findViewById(R.id.btn_empty_board_log).setOnClickListener((View view) ->
				new AlertDialog.Builder(getActivity())
						.setTitle(R.string.confirm)
						.setMessage(R.string.confirm_clear_log)
						.setIcon(R.drawable.ic_warning_black)
						.setPositiveButton(android.R.string.yes, (DialogInterface dialog, int whichButton) -> {
							logic.clear_board_log();
						})
						.setNegativeButton(android.R.string.no, null).show()
		);
		
		//download
		rootView.findViewById(R.id.btn_download).setOnClickListener((View view) -> {
			Context context = getContext();
			if(context == null)
				return;
			if(ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{
								Manifest.permission.READ_EXTERNAL_STORAGE,
								Manifest.permission.WRITE_EXTERNAL_STORAGE
						},
						REQUEST_PERMISSION);
				return;
			}
			
			anon_download();
		});
		
		//load
		rootView.findViewById(R.id.btn_load).setOnClickListener((View view) ->
				CONSTANTS.open_file(this, "*/*", INTENT_IMPORT_CONFIG)
		);
		
		//save
		rootView.findViewById(R.id.btn_save).setOnClickListener((View view) ->
				CONSTANTS.create_file(this, INTENT_EXPORT_CONFIG, "application/json", logic.board_data.mac+".json")
		);
		
		
		//
		//reading board infos
		//
		
		//Model name
		model_info.setText(logic.board.getModelString());
		
		//firmware
		logic.board.readDeviceInformationAsync().continueWith((Task<DeviceInformation> task) -> {
			Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
				Activity activity = getActivity();
				if(activity == null)
					return;
				
				if(firmware_info == null) //can happen when user left fragment before call was finished
					return;
				if(task.isFaulted())
					firmware_info.setText(getString(R.string.error));
				else {
					String fw_string = task.getResult().firmwareRevision;
					firmware_info.setText(fw_string);
					
					if(!firmwareWarning_was_issued && !fw_string.equals(CONSTANTS.FIRMWARE_OPTIMAL)) {
						new AlertDialog.Builder(getActivity())
								.setMessage(getString(R.string.info_firmware_not_optimal, fw_string, CONSTANTS.FIRMWARE_OPTIMAL))
								.setIcon(R.drawable.ic_warning_black)
								.setPositiveButton(android.R.string.ok, null).show();
						firmwareWarning_was_issued = true;
					}
				}
			});
			return null;
		});

		//device name
		logic.settingsModule.readBleAdConfigAsync().continueWith((Task<Settings.BleAdvertisementConfig> task) -> {
			getActivity().runOnUiThread(() -> {
				Activity activity = getActivity();
				if(activity == null)
					return;
				
				if(device_name_info == null) //can happen when user left fragment before call was finished
					return;
				if(task.isFaulted())
					device_name_info.setText(getString(R.string.error));
				else
					device_name_info.setText(task.getResult().deviceName);
			});
			return null;
		});
		
		//battery
		logic.settingsModule.battery().addRouteAsync((RouteComponent source) -> {
			source.stream((Data data, Object... env) -> {
				Activity activity = getActivity();
				if(activity == null)
					return;

				Settings.BatteryState battery = data.value(Settings.BatteryState.class);
				activity.runOnUiThread(() -> {
					if(battery_info == null) //can happen when user left fragment before call was finished
						return;
					battery_info.setText(battery.charge + "%, " + battery.voltage + " " + getString(R.string.volt));

					if(battery_route != null) {
						battery_route.remove();
						battery_route = null;
					}
				});
			});
		}).continueWith((Task<Route> task) -> {
			if(task.isFaulted()) {
				if(getActivity() == null)
					return null;
				getActivity().runOnUiThread(() -> {
					battery_info.setText(getString(R.string.error));
				});
			}
			else {
				battery_route = task.getResult();
				logic.settingsModule.battery().read();
			}
			return null;
		});
		
//		battery_info.setOnClickListener((View view) -> {
////			logic.log_battery();
//			bug3(logic.board);
//		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Context context = getContext();
		
		if(context == null)
			return;
		
		switch(requestCode) {
			case INTENT_EXPORT_CONFIG:
				if(resultCode != Activity.RESULT_OK)
					return;
				try {
					if(!CONSTANTS.write_to_file(context, new ByteArrayInputStream(logic.export_config().getBytes()), data.getData()))
						log(getString(R.string.error_save_config_failed, getString(R.string.error_unknown)));
				}
				catch(IOException e) {
//					log(getString(R.string.error_save_config_failed, return_error(e)));
					error(R.string.error_save_config_failed, e);
				}
				break;
				
			case INTENT_IMPORT_CONFIG:
				if(resultCode != Activity.RESULT_OK)
					return;
				try {
					List<DataBox> instructions = Board_logic.read_config_from_JSON(getContext(), data);
					logic.import_config(instructions);
				}
				catch(IOException e) {
//					log(getString(R.string.error_load_config_failed, return_error(e)));
					error(R.string.error_load_config_failed, e);
				}
		}
	}
	
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if(requestCode == REQUEST_PERMISSION) {
			if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
				anon_download();
		}
	}
	
	
	//*****
	//download
	//*****
	
//static void download(final MetaWearBoard MetaWear) {
//	MetaWear.getModule(Logging.class).downloadAsync().continueWith(task -> {
//		Log.i("log", "Done");
//		return null;
//	});
//}
	
	void download() {
		Context context = getContext();
		if(context == null || getActivity() == null)
			return;
		
		formatter = new Download_formatter(context);
		
		if(!Downloader.init(this, formatter, logic)) {
			error(new Exception(getString(R.string.error_create_file)));
			return;
		}
		
		show_progress(R.string.state_preparing);
		
		
		//
		//start download
		//
		getActivity().runOnUiThread(() -> (new Dialog_download_data(getActivity(), logic, download_dialog_listener)).show());
	}
	private void complete_download(boolean switch_acc_combined) {
		if(download_canceled) //this will be true when an error occurred or when a dialog was opened and anon_download() will be called again as soon as the user answers
			return;
		
		Downloader.complete(switch_acc_combined);
		Downloader.upload_files(getActivity());
		
		log(formatter.success_msg());
		
		if(Downloader.uploading_count == 0) {
			Downloader.close();
			finish_anon_download();
		}
		else
			update_progress(getString(R.string.state_uploading_with_count, Downloader.uploading_count));
	}
	
	
	
	
	private boolean download_canceled = false;
	void anon_download() {
		Context context = getContext();
		if(context == null)
			return;
		
		anon_identifier_types = new HashMap<>();
		formatter = new Download_formatter(context);
		if(!Downloader.init(this, formatter, logic)) {
			error(new Exception(getString(R.string.error_create_file)));
			return;
		}
		
		
		show_progress(R.string.state_preparing);
		anon_download_without_progress();
	}
	
	private void anon_download_without_progress() {
		download_canceled = false;
		
		logic.board.createAnonymousRoutesAsync().onSuccessTask((Task<AnonymousRoute[]> task) -> {
			Switch_route route = logic.get_switch_route();
			
			//
			//loading all known route-identifiers
			//
			
			if(route != null) {
				if(route.log_acc_data)
					anon_identifier_types.put(route.acc_identifier, Download_formatter.ACC);
				
				if(route.log_switch_data == Switch_route.LOG_LENGTH_OF_CLICK) {
					anon_identifier_types.put(route.switch_identifier, Download_formatter.SWITCH_LENGTH);
					//if(switch_acc_combined && !createFile(Download_formatter.SWITCH_LENGTH_ACC_COMBINED))
					//return null;
				}
				else if(route.log_switch_data != Switch_route.LOG_SWITCH_NONE) {
					anon_identifier_types.put(route.switch_identifier, Download_formatter.SWITCH_NUM);
					//if(switch_acc_combined && !createFile(Download_formatter.SWITCH_NUM_ACC_COMBINED))
					//return null;
				}
			}
			
			if(logic.board_data.battery.battery_log_identifier == null)
				anon_identifier_types.put("battery", Download_formatter.BING_BATTERY);
			else
				anon_identifier_types.put(logic.board_data.battery.battery_log_identifier, Download_formatter.BING_BATTERY);
			
			//
			//iterating all download-routes on the board
			//
			List<Integer> found_routes = new ArrayList<>();
			
			for(final AnonymousRoute it: task.getResult()) {
				String identifier = it.identifier();
				
				//
				//Identifying download-route or Failsafe
				//
				if(!anon_identifier_types.containsKey(identifier)) {
					log(getString(R.string.info_unknown_data));
					download_canceled = true;
					Activity activity = getActivity();
					
					if(activity != null) {
						activity.runOnUiThread(() -> {
							(new Dialog_choose_unknown_download_type(activity, identifier, new Dialog_choose_unknown_download_type.OnFinishListener() {
								@Override
								public void onFinish(int type)  {
									anon_identifier_types.put(identifier, type); //anon_identifier_types will not be reset
									anon_download_without_progress();
								}
								
								@Override
								public void onCancel() {
									finish_anon_download();
								}
							}, null)).show();
						});
					}
					else
						end_progress();
					return null;
				}
				found_routes.add(anon_identifier_types.get(identifier));
				
				
				//
				//Subscribing to downloader
				//
				
				if(anon_identifier_types.containsKey(identifier))
					it.subscribe(Downloader.create_anon_subscriber(anon_identifier_types.get(identifier)));
				else//this should never happen because we check the identifiers before downloaders are subscribed
					return null;
			}
			
			//
			//start download
			//
			
			Activity activity = getActivity();
			if(activity == null)
				return null;
			
			activity.runOnUiThread(() -> {
				(new Dialog_anon_download_data(activity, logic, download_dialog_listener, found_routes)).show();
			});
			
			return null;
		}).continueWith((Task<Object> task) -> {
			if(task.isFaulted()) {
//				log(return_error(task.getError()));
				error(task.getError());
				end_progress();
			}
			return null;
		});
	}
	
	private void finish_anon_download() {
		logic.load_state(); //createAnonymousRoutesAsync() messes up the saved board state. So we just reload the state when we are done.
		end_progress();
	}
}
