package at.jodlidev.metawear.study;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.mbientlab.metawear.AsyncDataProducer;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.DataToken;
import com.mbientlab.metawear.ForcedDataProducer;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.builder.filter.Comparison;
import com.mbientlab.metawear.builder.filter.Passthrough;
import com.mbientlab.metawear.builder.function.Function2;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.DataProcessor;
import com.mbientlab.metawear.module.Debug;
import com.mbientlab.metawear.module.Gpio;
import com.mbientlab.metawear.module.GyroBmi160;
import com.mbientlab.metawear.module.Haptic;
import com.mbientlab.metawear.module.Led;
import com.mbientlab.metawear.module.Logging;
import com.mbientlab.metawear.module.Macro;
import com.mbientlab.metawear.module.Settings;
import com.mbientlab.metawear.module.Switch;
import com.mbientlab.metawear.module.Timer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import at.jodlidev.metawear.study.data.Battery;
import at.jodlidev.metawear.study.data.Bing;
import at.jodlidev.metawear.study.data.Board_data;
import at.jodlidev.metawear.study.data.BootMacro;
import at.jodlidev.metawear.study.data.ChargeFeedback;
import at.jodlidev.metawear.study.data.DataBox;
import at.jodlidev.metawear.study.data.DataBox_forFeedback;
import at.jodlidev.metawear.study.data.FeedbackMacro;
import at.jodlidev.metawear.study.data.LogMessages;
import at.jodlidev.metawear.study.data.Password;
import at.jodlidev.metawear.study.data.RandomTimer;
import at.jodlidev.metawear.study.data.Repeat;
import at.jodlidev.metawear.study.data.Switch_route;
import bolts.Task;

/**
 * Created by JodliDev on 07.03.2019.
 */
public class Board_logic {
	private final static String ROUTE_BATTERY_REACT = "battery_react";
	private final static String ROUTE_BATTERY_LOG = "battery_log";
	
	private SQLite sql;
	public MetaWearBoard board;
	
	public Board_data board_data;
	
	
	private Debug debugModule;
	private Led ledModule;
	private Haptic vibrationModule;
	private Switch switchModule;
	private Accelerometer accelerometerModule;
	private Logging logModule;
	Timer timerModule;
	private Macro macroModule;
	DataProcessor dataprocessorModule;
	Settings settingsModule;
	GyroBmi160 gyroModule; //its existence needs to be checked in Fragment_Bing
	
	private Context context;
	private BtleService.LocalBinder binder;
	BluetoothDevice btDevice;
	Progress_Info progress;
	
	private boolean is_in_macro_mode = false;
	
	boolean is_ready = false;
	boolean loaded_from_serialized_state = false;
	
	Board_logic(
			Context _context,
			SQLite _sql,
			Progress_Info _progress,
			BtleService.LocalBinder _binder,
			BluetoothDevice _btDevice) {
		context = _context.getApplicationContext();
		sql = _sql;
		progress = _progress;
		binder = _binder;
		btDevice = _btDevice;
		
		board = create_board();
	}
	
	
	
	//*****
	//Global
	//*****
	private MetaWearBoard create_board() {
		binder.clearSerializedState(btDevice);
		binder.removeMetaWearBoard(btDevice);
		return binder.getMetaWearBoard(btDevice);
	}
	void connect(Runnable success, Runnable fail, Runnable no_password) {
		progress.show_progress(R.string.state_connecting);
		Activity_SelectDevice.connect(board)
				.continueWith(task -> {
					if(!task.isCancelled()) {
						board_data = sql.get_board_data(board.getMacAddress());
						
						load_state();
						is_ready = true;
						check_password(success, fail, no_password);
//						Gpio gpio = board.getModule(Gpio.class);
//						gpio.pin((byte) 1).setPullMode(Gpio.PullMode.NO_PULL);
//
//
//						ForcedDataProducer analogAdc = gpio.pin((byte) 1).analogAdc();
//						analogAdc.addRouteAsync(source -> source.stream(new Subscriber() {
//							String output = "";
//							@Override
//							public void apply(Data data, Object... env) {
//								Short data_o = data.value(Short.class);
//								long o = Math.round(Math.pow(data_o,2)/100%10);
//								Log.i("MainActivity", "analogAdc = " + o);
//								output += o+"\n";
//							}
//						}));
//						ForcedDataProducer analogAbsRef = gpio.pin((byte) 1).analogAbsRef();
//						analogAbsRef.addRouteAsync(source -> source.stream(new Subscriber() {
//							String output = "";
//							@Override
//							public void apply(Data data, Object... env) {
//								Float data_o = data.value(Float.class);
//								long o = Math.round(Math.pow(data_o*1000,2)/100%10);
//								Log.i("MainActivity", "analogAbsRef = " + o);
//								output += o+"\n";
//							}
//						}));
//
//						for(int i=0; i<1000; ++i) {
//							analogAdc.read();
//						}
//						for(int i=0; i<1000; ++i) {
//							analogAbsRef.read();
//						}
//
//						switchModule.state().addRouteAsync((RouteComponent source) -> {
//							source.stream((Subscriber) (data, env) -> {
//								analogAdc.read();
//								analogAbsRef.read();
//							});
//						});
					}
					progress.end_progress();
					
					return null;
				});
	}
	
	private void show_progress(int res) {
		progress.show_progress(res);
	}
	private void update_progress(final int res) {
		progress.update_progress(context.getString(res));
	}
	private void end_progress() {
		progress.end_progress();
	}
	
	private void error(int res, Exception e) {
//		log(context.getString(res, progress.return_error(e)));
		progress.error(res, e);
	}
	private void error(Exception e) {
		progress.error(e);
	}
	void log(int res) {
		log(context.getString(res));
	}
	void log(String msg) {
//		sql.save_log(board_data.sql_id, msg);
		progress.log(msg);
	}
	
	void save_log(String s) {
		if(sql != null && board_data != null)
			sql.save_log(board_data.sql_id, s);
	}
	List<LogMessages> get_logs() {
		return sql.get_logs(board_data.sql_id);
	}
	
	private void loadModules() {
		debugModule = board.getModule(Debug.class);
		logModule = board.getModule(Logging.class);
		ledModule = board.getModule(Led.class);
		vibrationModule = board.getModule(Haptic.class);
		switchModule = board.getModule(Switch.class);
		accelerometerModule = board.getModule(Accelerometer.class);
		timerModule = board.getModule(Timer.class);
		macroModule = board.getModule(Macro.class);
		dataprocessorModule = board.getModule(DataProcessor.class);
		settingsModule = board.getModule(Settings.class);
		gyroModule = board.getModule(GyroBmi160.class);
	}
	
	void signal_error() {
		vibrationModule.startBuzzer((short) 1000);
		singlePulseLed("RED");
	}
	private void pulseLed(String color) {
		ledModule.stop(true);
		ledModule.editPattern(Led.Color.valueOf(color), Led.PatternPreset.PULSE).highTime((short) 1000).pulseDuration((short) 2500).repeatCount((byte) 3).commit();
		ledModule.play();
	}
	void singlePulseLed(String color) {
		ledModule.stop(true);
		ledModule.editPattern(Led.Color.valueOf(color), Led.PatternPreset.PULSE).highTime((short) 1000).pulseDuration((short) 2500).repeatCount((byte) 1).commit();
		ledModule.play();
	}
	void enableLed(String color) {
		ledModule.stop(true);
		ledModule.editPattern(Led.Color.valueOf(color), Led.PatternPreset.SOLID).commit();
		ledModule.play();
	}
	void disableLed() {
		ledModule.stop(true);
	}
	private void blinkLed(String color) {
		ledModule.stop(true);
		ledModule.editPattern(Led.Color.valueOf(color), Led.PatternPreset.BLINK).highTime((short) 500).pulseDuration((short) 1000).repeatCount((byte) 1).commit();
		ledModule.play();
	}
	
	private void save_state() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		try {
			board.serialize(stream);
			if(!is_in_macro_mode) {
				if(progress.error) {
					signal_error();
				}
				else
					singlePulseLed("GREEN");
			}
		}
		catch(IOException e) {
			error(R.string.error_serialization_failed, e);
		}

		sql.insert_serialized(board_data, stream.toByteArray());
	}
	void load_state() {
		try {
			byte[] data = sql.load_serialized(board_data);
			board.deserialize(new ByteArrayInputStream(data));
			loaded_from_serialized_state = true;
		}
		catch(NullPointerException | ClassNotFoundException e) {
			log(context.getString(R.string.info_no_deserialization));
			board.getModule(Logging.class).start(false); //logModule is not defined yet
		}
		catch(Exception e) {
			error(R.string.error_deserialization_failed, e);
			board.getModule(Logging.class).start(false); //logModule is not defined yet
		}
		
		loadModules();
	}
	
	Task<?> clear_board_log() {
		logModule.clearEntries();
		log(R.string.info_data_cleared);
		return reconnect(false);
	}
	Task<?> reset_board(boolean create_new_board) {
		board.tearDown();
		macroModule.eraseAll();
		debugModule.resetAfterGc();
		
		sql.remove_bootMacros(board_data);
		sql.remove_feedbackMacros(board_data);
		sql.reset_board(board_data);
		
		progress.update_ui();
		
		
		
		
//		show_progress(R.string.state_resetting
//		return debugModule.resetAsync().continueWithTask(task -> remove_all_macros()).continueWith(task -> {
		return clear_board_log().continueWithTask(task ->
				reconnect(create_new_board)
		).continueWith(task -> {
			loadModules();
			logModule.start(false);
			save_state();
			
			log(R.string.info_reset_done);
			set_password();
			return null;
		});
		
	}
	
	void set_userNotes(String notes) {
		sql.set_userNotes(board_data, notes);
	}
	
	Task<?> reconnect(boolean new_board) {
//		board.disconnectAsync().continueWith((Task<Void> battery_react_task) -> {
//			board.connectAsync(2000).continueWithTask(task -> task.isFaulted() ? reconnect(board) : task);
//
//			return null;
//		});
		
//		board.getModule(Debug.class).resetAfterGc();
		show_progress(R.string.state_reconnecting);
		return board.disconnectAsync()
				.continueWithTask(task -> Activity_SelectDevice.connect(board = new_board ? create_board() : board, 1000))
				.continueWith(task -> {
					end_progress();
					
					return null;
				});
		
		
		
		
//		show_progress(R.string.state_reconnecting);
//		return board.disconnectAsync()
//				.continueWithTask((Task<Void> task) -> board.connectAsync(2000))
//				.continueWithTask(task -> {
//					if(task.isFaulted()) {
//						Activity_SelectDevice.reconnect(board);
//					}
//					end_progress();
//					return null;
//				})
//				.continueWith(task -> {
//					end_progress();
//					return null;
//				});
	}
	void destroy() {
		end_progress();
		sql.close();
		
		if(board != null && board.isConnected())
			board.disconnectAsync();
		board = null;
		sql = null;
		
		ledModule = null;
		vibrationModule = null;
		switchModule = null;
		logModule = null;
		accelerometerModule = null;
		timerModule = null;
		macroModule = null;
		dataprocessorModule = null;
		settingsModule = null;
		
		progress = null;
		is_ready = false;
	}
	
	void set_board_to_rebooted() {
		sql.set_board_to_rebooted(board_data);
	}
	
	//*****
	//Password
	//*****
	void check_password(Runnable success, Runnable fail, Runnable no_password) {
		Task<Integer> pass_task = debugModule.readTmpValueAsync();
		
		show_progress(R.string.state_checking_password);
		pass_task.continueWith((Task<Integer> task) -> {
			if(task.isFaulted()) {
				error(task.getError());
				fail.run();
				return null;
			}
			int pass = task.getResult();
			
			
			Log.d("password", "db:" + Password.get() + " == board:" + pass);
			end_progress();
			if(pass == 0) {
				set_password();
				if(no_password != null)
					no_password.run();
				success.run();
			}
			else if(Password.is_same(pass))
				success.run();
			else
				fail.run();
			
			return null;
		});
	}
	void set_password() {
		debugModule.writeTmpValue(Password.get());
	}
	
	//*****
	//Config
	//*****
	String export_config() {
		return sql.export_config(board_data);
	}
	public static List<DataBox> read_config_from_JSON(Context context, Intent data) throws IOException {
		Uri uri;
		JsonReader reader;
		
		if(data == null || (uri = data.getData()) == null)
			throw new IOException(context.getString(R.string.error));
		
		InputStream input = context.getContentResolver().openInputStream(uri);
		
		if(input == null)
			throw new IOException(context.getString(R.string.error));
		
		reader = new JsonReader(new InputStreamReader(input));
		List<DataBox> instructions = new ArrayList<>();
		
		reader.beginObject();
		while(reader.hasNext()) {
			switch(reader.nextName()) {
				case "battery":
					instructions.add(new Battery(reader));
					break;
				case Repeat.TABLE:
					if(reader.peek() == JsonToken.NULL)
						reader.skipValue();
					else
						instructions.add(new Repeat(reader));
					break;
				case RandomTimer.TABLE:
					if(reader.peek() == JsonToken.NULL)
						reader.skipValue();
					else
						instructions.add(new RandomTimer(reader));
					break;
				case Switch_route.TABLE:
					if(reader.peek() == JsonToken.NULL)
						reader.skipValue();
					else
						instructions.add(new Switch_route(reader));
					break;
				case Bing.TABLE:
					if(reader.peek() == JsonToken.NULL)
						reader.skipValue();
					else {
						reader.beginArray();
						while(reader.hasNext()) {
							instructions.add(new Bing(reader));
						}
						reader.endArray();
					}
					break;
				case BootMacro.TABLE:
					if(reader.peek() == JsonToken.NULL)
						reader.skipValue();
					else
						instructions.add(new BootMacro(reader));
					break;
				case ChargeFeedback.TABLE:
					if(reader.peek() == JsonToken.NULL)
						reader.skipValue();
					else
						instructions.add(new ChargeFeedback(reader));
					break;
				default:
					reader.skipValue();
			}
		}
		reader.endObject();
		reader.close();
		input.close();
		return instructions;
	}
	
	void import_config(final List<DataBox> instructions) {
		//TODO: use continueWith again - but font forget to update the task-obj!
		
//		reset_board();
//		Task<?> task = Task.forResult(null);
//		for(DataBox obj : instructions) {
//			switch(obj.get_type()) {
//				case DataBox.TYPE_BATTERY:
//					task.continueWithTask((newtask) -> {
//						Log.d("progress", "battery" + Boolean.toString(newtask.isCompleted()));
//						return init_battery((Battery) obj, null);
//					});
//					break;
//				case DataBox.TYPE_REPEAT:
//					task.continueWithTask((newtask) -> {
//						Log.d("progress", "repeat" + Boolean.toString(newtask.isCompleted()));
//						return add_repeat((Repeat) obj);
//					});
//					break;
//				case DataBox.TYPE_RANDOM:
//					task.continueWithTask((newtask) -> {
//						Log.d("progress", "random" + Boolean.toString(newtask.isCompleted()));
//						return add_random((RandomTimer) obj);
//					});
//					break;
//				case DataBox.TYPE_SWITCH:
//					task.continueWithTask((newtask) -> {
//						Log.d("progress", "switch" + Boolean.toString(newtask.isCompleted()));
//						return set_up_switch_route((Switch_route) obj, get_battery());
//					});
//					break;
//				case DataBox.TYPE_BING:
//					task.continueWithTask((newtask) -> {
//						Log.d("progress", "bing" + Boolean.toString(newtask.isCompleted()));
//						return add_bing((Bing) obj);
//					});
//					break;
//				case DataBox.TYPE_MACRO:
//					task.continueWithTask((newtask) -> {
//						Log.d("progress", "macro" + Boolean.toString(newtask.isCompleted()));
//						return add_bootMacro((BootMacro) obj);
//					});
//					break;
//			}
//		}
		show_progress(R.string.state_loading);
		reset_board(true).continueWith(reset_task -> {
			//we dont want to run in the ui-Thread because of waitForCompletion
			Task.callInBackground(() -> {
				Battery battery = null;
				for(DataBox obj : instructions) {
					Task<?> task;
					switch(obj.get_type()) {
						case DataBox.TYPE_BATTERY:
							battery = (Battery) obj;
							task = Task.forResult(null);
							break;
						case DataBox.TYPE_SWITCH: //has to be before repeat, random and bing because battery has not be saved to db yet
							Switch_route route = (Switch_route) obj;
							task = set_up_switch_route(route, battery, route.restore_on_boot); //this will save battery to db - it cannot be saved before that or nothing will change
							break;
						case DataBox.TYPE_REPEAT:
							task = add_repeat((Repeat) obj);
							break;
						case DataBox.TYPE_RANDOM:
							task = add_random((RandomTimer) obj);
							break;
						case DataBox.TYPE_BING:
							task = add_bing((Bing) obj);
							break;
						case DataBox.TYPE_MACRO:
							task = add_bootMacro((BootMacro) obj);
							break;
						case DataBox.TYPE_CHARGE_FEEDBACK:
							task = add_chargeFeedback((ChargeFeedback) obj);
							break;
						default:
							task = Task.forResult(null);
					}
					try {
						task.waitForCompletion();
					}
					catch(InterruptedException e) {
						log(e.getMessage());
						e.printStackTrace();
						end_progress();
						return null;
					}
				};
				end_progress();
				log(R.string.info_import_finished);
				clear_board_log();
				return null;
			});
			
			return null;
		});
		
		
		
		
		
		
		
		
		
//		reset_board();
//		Task<?> task = Task.forResult(null);
//		Continuation continuation = new Continuation() {
//			private int i = 0;
//
//			@Override
//			public Object then(Task oldTask) throws Exception {
//				if(oldTask.isFaulted()) {
//					end_progress();
//					return null;
//				}
//
//
////				wait(2000);
//				DataBox obj = instructions.get(i++);
//
//				Task<?> newTask = exec_instruction(obj);
//				newTask.continueWithTask(this);
//				return newTask;
//			}
//		};
//		task.continueWithTask(continuation);
		

		
		
		
//		reset_board();
//		Progress_Info.ContinueListener listener = new Progress_Info.ContinueListener() {
//			private Battery battery = null;
//			private int i = 0;
//
//			@Override
//			public boolean end_block() {
//				if(i < instructions.size()) {
//					DataBox obj = instructions.get(i++);
//					switch(obj.get_type()) {
//						case DataBox.TYPE_BATTERY:
//							init_battery(battery = (Battery) obj, null);
//							break;
//						case DataBox.TYPE_REPEAT:
//							add_repeat((Repeat) obj);
//							break;
//						case DataBox.TYPE_RANDOM:
//							add_random((RandomTimer) obj);
//							break;
//						case DataBox.TYPE_SWITCH:
//							set_up_switch_route((Switch_route) obj, battery);
//							break;
//						case DataBox.TYPE_BING:
//							add_bing((Bing) obj);
//							break;
//						case DataBox.TYPE_MACRO:
//							add_bootMacro((BootMacro) obj);
//							break;
//					}
//					return false;
//				}
//				else
//					return true;
//			}
//		};
//		progress.set_listener(listener);
//		listener.end_block();
	}
	
	//*****
	//Boot-Macro
	//*****
	Task<?> add_bootMacro(final BootMacro macro) {
//		show_progress(R.string.state_saving_macro);
//		is_in_macro_mode = true;
//
//		Task<?> switch_task;
//		if(macro.remember_switch) { //TODO!!!
//			Switch_route route = get_switch_route();
//			Battery battery = get_battery();
//
//			//remove routes so they can be recreated for the macro
//			remove_battery_logic();
//			remove_switch_route();
//
//			macroModule.startRecord(true); //start recording after everything was removed
//
//			switch_task = set_up_switch_route(route, battery);
//		}
//		else {
//			macroModule.startRecord(true);
//			switch_task = Task.forResult(null);
//		}
//
//
//
//		return switch_task.continueWith((task) -> {
//			if(macro.repeat_feedback) {
//				int countdown = (macro.hour * 60*60 + macro.min*60)*1000;
//
//				return timerModule.scheduleAsync(countdown, false, () -> {
//					bootMacro_feedback(macro);
//				}).continueWithTask(timer_task -> {
//					if(timer_task.isFaulted())
//						throw timer_task.getError();
//
//					Timer.ScheduledTask scheduledTask = timer_task.getResult();
//					scheduledTask.start();
//
//					return bootMacro_end(macro, scheduledTask);
//				});
//			}
//			else {
//				bootMacro_feedback(macro);
//				return bootMacro_end(macro, null);
//			}
//		});
		
		
		
		show_progress(R.string.state_saving_macro);
		is_in_macro_mode = true;
		macroModule.startRecord(true);
		
		if(macro.repeat_feedback) {
			int countdown = (macro.hour * 60*60 + macro.min*60)*1000;
			
			return timerModule.scheduleAsync(countdown, false, () -> {
				bootMacro_feedback(macro);
			}).continueWithTask(timer_task -> {
				if(timer_task.isFaulted())
					throw timer_task.getError();
				
				Timer.ScheduledTask scheduledTask = timer_task.getResult();
				scheduledTask.start();
				
				return bootMacro_end(macro, scheduledTask);
			});
		}
		else {
			bootMacro_feedback(macro); //TODO: we can also use init_generic_feedbackMacro() instead, but this function must be slightly changed in order to do so
			return bootMacro_end(macro, null);
		}
	}
	
	private void bootMacro_feedback(BootMacro macro) {
		if(macro.led) {
			if(macro.led_type == BootMacro.LED_TYPE_BLINKING)
				blinkLed(macro.color);
			else if(macro.led_type == BootMacro.LED_TYPE_SOLID)
				enableLed(macro.color);
		}
		if(macro.vibration)
			vibrationModule.startMotor(macro.vibration_strength, macro.vibration_ms);
	}
	
	private Task<Byte> bootMacro_end(final BootMacro macro, final Timer.ScheduledTask scheduledTask) {
		update_progress(R.string.state_saving_macro);
		return macroModule.endRecordAsync().continueWith((Task<Byte> task) -> {
			if(scheduledTask != null)
				scheduledTask.remove();
			if(task.isFaulted()) {
				error(R.string.error_saving_macro, task.getError());
				end_progress();
				throw  task.getError();
			}
			is_in_macro_mode = false;
			save_state();
			sql.save_bootMacro(board_data, macro);
			log(R.string.info_macro_saved);
			end_progress();
			progress.update_ui();
			return null;
		});
	}
	
	BootMacro get_bootMacro() {
		return sql.get_bootMacro(board_data);
	}
	
	//*****
	//Charge status
	//*****
	Task<?> add_chargeFeedback(ChargeFeedback chargeFeedback) {
		show_progress(R.string.state_creating_route);
		remove_chargeFeedback();
		return init_generic_feedbackMacro(chargeFeedback, null).continueWithTask(task -> {
			if(task.isFaulted())
				throw task.getError();
			
			final byte macro_id = task.getResult();
			
			return settingsModule.chargeStatus().addRouteAsync(source -> {
				source.filter(Comparison.EQ, 1).react(token -> {
					macroModule.execute(macro_id);
				});
			});
		}).continueWithTask(task -> {
			if(task.isFaulted()) {
				error(R.string.error_chargeFeedback_failed, task.getError());
				end_progress();
				throw task.getError();
			}
			else {
				chargeFeedback.route_id = task.getResult().id();
				chargeFeedback.save(board_data, sql.getWritableDatabase());
				
				log(R.string.info_chargeFeedback_created);
				progress.update_ui();
				save_state();
			}
			end_progress();
			return null;
		});
	}
	
	ChargeFeedback get_chargeFeedback() {
		return sql.get_chargeFeedback(board_data);
	}
	
	void remove_chargeFeedback() {
		ChargeFeedback chargeFeedback = get_chargeFeedback();
		
		if(chargeFeedback != null) {
			Route route = board.lookupRoute(chargeFeedback.route_id);
			if(route != null)
				route.remove();
			
			sql.remove_chargeFeedback(board_data);
			progress.update_ui();
			log(R.string.info_removed_chargeFeedback);
		}
	}
	
	//*****
	//Feedback-Macro
	//*****
	private Task<Byte> init_generic_feedbackMacro(DataBox_forFeedback dataBox, Repeat repeat) {
		String identifier = dataBox.led + dataBox.color
							+ dataBox.vibration + dataBox.vibration_strength + dataBox.vibration_ms
							+ dataBox.repeat + dataBox.random
							+ (dataBox.battery_logging && board_data.battery.battery_for_bing) + board_data.battery.battery_for_switch;
		
		FeedbackMacro f = new FeedbackMacro(board_data, identifier);
		
		byte id = sql.get_feedback_macro_id(f);
		if(id == FeedbackMacro.NO_MACRO) {
			show_progress(R.string.state_saving_macro);
			
			is_in_macro_mode = true;
			macroModule.startRecord(false);
			
			boolean _had_error = false;
			try {
				if(dataBox.led)
					singlePulseLed(dataBox.color);
				
				if(dataBox.vibration)
					vibrationModule.startMotor(dataBox.vibration_strength, dataBox.vibration_ms);
				
				if(dataBox.repeat && repeat != null) {
					Timer.ScheduledTask repeat_task = timerModule.lookupScheduledTask(((byte) repeat.id_timer));
					repeat_task.start();
				}
				
				if(dataBox.random) {
					gyroModule.angularVelocity().start();
					gyroModule.start();
				}
				
				if(dataBox.battery_logging && board_data.battery.battery_for_bing) {
					log_battery();
				}
			}
			catch(Exception e) {
				_had_error = true;
				error(R.string.error_saving_macro, e);
			}
			
			final boolean had_error = _had_error;
			
			return macroModule.endRecordAsync().continueWith((Task<Byte> task) -> {
				is_in_macro_mode = false;
				//undo timers that have been started for the macro:
				if(dataBox.repeat && repeat != null) {
					Timer.ScheduledTask repeat_task = timerModule.lookupScheduledTask(((byte) repeat.id_timer));
					repeat_task.stop();
				}
				if(dataBox.random)
					reset_random();
				
				if(had_error || task.isFaulted()) {
					end_progress();
					throw task.getError();
				}
				
				f.macro_id = task.getResult();
				sql.save_feedback_macro(f);
				
				save_state();
				
				end_progress();
				return f.macro_id;
			});
		}
		else
			return Task.forResult(id);
	}
	
	private Task<Byte> create_unique_feedbackMacro(String identifier, Runnable runnable) {
		FeedbackMacro feedbackMacro = new FeedbackMacro(board_data, identifier);
		byte id = sql.get_feedback_macro_id(feedbackMacro);
		
		if(id == FeedbackMacro.NO_MACRO) {
			is_in_macro_mode = true;
			show_progress(R.string.state_saving_macro);
			macroModule.startRecord(false);
			
			try {
				runnable.run();
			}
			catch(Exception e) {
				is_in_macro_mode = false;
				end_progress();
				throw e;
			}
			
			return macroModule.endRecordAsync().continueWith((Task<Byte> task) -> {
				if(task.isFaulted()) {
					is_in_macro_mode = false;
					end_progress();
					throw task.getError();
				}
				
				feedbackMacro.macro_id = task.getResult();
				sql.save_feedback_macro(feedbackMacro);
				
				is_in_macro_mode = false;
				save_state();
				end_progress();
				return feedbackMacro.macro_id;
			});
		}
		else
			return Task.forResult(id);
	}
	
	
	//*****
	//Battery
	//*****
	private Task<?> init_battery_forSwitch(final Battery battery, final Switch_route switch_route, final boolean createBootMacro) {
		Battery old_battery = sql.get_board_data(board_data.mac).battery; //we need to reload the battery from sql in case the battery-variable was altered and sent as a new battery
		
		boolean remove = old_battery.battery_for_switch && (!battery.battery_for_switch
				|| (!battery.battery_low_color.equals(old_battery.battery_low_color)
					 || battery.battery_low_threshold != old_battery.battery_low_threshold
					 || battery.battery_low_led != old_battery.battery_low_led
					 || battery.battery_low_vibration != old_battery.battery_low_vibration
					 || battery.battery_low_vibration_ms != old_battery.battery_low_vibration_ms
					 || battery.battery_low_vibration_strength != old_battery.battery_low_vibration_strength));
		boolean add = battery.battery_for_switch && (remove || !old_battery.battery_for_switch);
		
		if(remove)
			remove_battery_forSwitch();
		
		if(add) {
			show_progress(R.string.state_adding_battery);
			
			if(createBootMacro)
				macroModule.startRecord(true);
			
			return settingsModule.battery().addRouteAsync(source ->
					source.split().index(0).limit(Passthrough.COUNT, (short) 0).name(ROUTE_BATTERY_REACT)
							.multicast()
							.to().filter(Comparison.LTE, battery.battery_low_threshold).react(token -> {
								if(battery.battery_low_led)
									singlePulseLed(battery.battery_low_color);
								if(battery.battery_low_vibration)
									vibrationModule.startMotor(battery.battery_low_vibration_strength, battery.battery_low_vibration_ms);
					})
							.to().filter(Comparison.GT, battery.battery_low_threshold).react(token -> {
								if(switch_route != null)
									basic_switch_feedback(switch_route);
							})
			)
					.continueWithTask((Task<Route> task) -> {
						if(task.isFaulted())
							throw new Exception(context.getString(R.string.error_battery_route_failed, task.getError()));
						
						battery.battery_react_route_id = task.getResult().id();
						
						return createBootMacro ? macroModule.endRecordAsync() : null;
					})
					.continueWith((Task<Byte> task) -> {
						progress.update_ui();
						
						if(task.isFaulted()) {
							end_progress();
							throw new Exception(context.getString(R.string.error_battery_route_failed, task.getError()));
						}
						
						sql.save_battery(board_data, battery);
						save_state();
						
						log(R.string.info_battery_finished);
						end_progress();
						
						return false;
					});
		}
		else
			return Task.forResult(null);
	}
	
	Task<?> init_battery_forBing(boolean new_battery_for_bing) {
		if(new_battery_for_bing && !board_data.battery.battery_for_bing) {
			show_progress(R.string.state_adding_battery);
			Battery battery = get_battery();
			battery.battery_for_bing = true;
			
			return settingsModule.battery().addRouteAsync(source -> source.limit(Passthrough.COUNT, (short) 0).name(ROUTE_BATTERY_LOG).log(Downloader.create_subscriber(Download_formatter.BING_BATTERY)))
					.continueWith((Task<Route> task) -> {
						progress.update_ui();
						
						if(task.isFaulted()) {
							end_progress();
							throw new Exception(context.getString(R.string.error_battery_route_failed, task.getError()));
						}
						else {
							Route main_result = task.getResult();
//							main_result.setEnvironment(0, Download_formatter.BING_BATTERY);
							
							battery.battery_log_identifier = main_result.generateIdentifier(0);
							battery.battery_route_id = main_result.id();
							
							sql.save_battery(board_data, battery);
							save_state();
							
							
							log(R.string.info_battery_finished);
							end_progress();
						}
						
						return false;
					});
		}
		else
			return Task.forResult(null);
	}
	
	
	Battery get_battery() {//creates a new instance of battery
		Board_data old_board = sql.get_board_data(board_data.mac);
		return old_board.battery;
	}
	
	private void remove_battery_forSwitch() {
		Route route = board.lookupRoute(board_data.battery.battery_react_route_id);
		if(route != null)
			route.remove();
		
		sql.save_battery(board_data, new Battery());
		log(R.string.info_battery_removed);
	}
	private void remove_battery_for_bing_if_needed() {
		if(!has_bings_with_battery_logging()) {
			board_data.battery.battery_for_bing = false; //no bings have battery anymore - so we remove
			
			Route route = board.lookupRoute(board_data.battery.battery_route_id);
			if(route != null)
				route.remove();
			
			sql.save_battery(board_data, new Battery());
			log(R.string.info_battery_removed);
		}
	}
	
	private void remove_battery_logic() {
		if(board_data.battery.battery_for_switch || board_data.battery.battery_for_bing) {
			Route route = board.lookupRoute(board_data.battery.battery_route_id);
			if(route != null)
				route.remove();
			
			sql.save_battery(board_data, new Battery());
			log(R.string.info_battery_removed);
		}
	}
	
	void log_battery() {
		if(board_data.battery.battery_for_bing) {
//			if(board_data.battery.battery_for_switch) //this limiter only exists when switch also reacts to battery - OLD
				dataprocessorModule.edit(ROUTE_BATTERY_LOG, DataProcessor.PassthroughEditor.class).set((short) 1);
			settingsModule.battery().read();
		}
	}
	void check_battery() {
//		if(board_data.battery.battery_for_bing) //this limiter only exists when battery also logs the bing - OLD
			dataprocessorModule.edit(ROUTE_BATTERY_REACT, DataProcessor.PassthroughEditor.class).set((short) 1);
		settingsModule.battery().read();
	}
	
	//*****
	//Switch_route
	//*****
	boolean has_switch_routes() {
		return sql.has_switch_routes(board_data);
	}
	Switch_route get_switch_route() {
		return sql.get_switch_route(board_data);
	}
	
	
	Task<?> set_up_switch_route(Switch_route route, Battery battery, final boolean createBootMacro) {
		if(route == null)
			return Task.forResult(null);
		if(battery == null)
			battery = get_battery();
		
		show_progress(R.string.state_switch);
		
		if(createBootMacro)
			route.restore_on_boot = true;
		final boolean will_remove = has_switch_routes();
		final boolean will_add = route.vibration || route.led_behaviour != Switch_route.LED_NONE || route.log_acc_data || route.log_switch_data != Switch_route.LOG_SWITCH_NONE;
		
		
		return init_battery_forSwitch(battery, route, createBootMacro).continueWithTask(task -> {//switch macro
			if(task.isFaulted())
				throw task.getError();
			
			if(!will_remove && !will_add)
				return Task.forResult(FeedbackMacro.NO_MACRO);
			
			
			//remove existing route
			if(will_remove)
				remove_switch_route();
			
			//only add a new route if needed
			if(will_add) {
				update_progress(R.string.state_switch);
				
				//Record macro:
				
				String identifier = "btn_" + route.led_behaviour + route.color + route.vibration + route.vibration_strength + route.vibration_ms
									+ route.log_switch_data + route.log_acc_data
									+ board_data.battery.battery_for_switch + board_data.battery.battery_for_bing;
				
				return create_unique_feedbackMacro(identifier, () -> {
					
					//Visualize length of click:
					
					if(route.led_behaviour == Switch_route.LED_VISUALIZE_BUTTON && route.log_switch_data == Switch_route.LOG_LENGTH_OF_CLICK) {
						//start LED until button was released
						ledModule.stop(true); //this will stop LED as long as button is pressed
						ledModule.editPattern(Led.Color.valueOf(route.color), Led.PatternPreset.SOLID).commit();
						ledModule.play();
					}
					
					//Number of clicks:
					
					else if(route.log_switch_data == Switch_route.LOG_NUMBER_OF_CLICKS) {
						if(route.led_behaviour == Switch_route.LED_VISUALIZE_BUTTON) {
							//color-instructions have changed. Restart LED:
							ledModule.stop(true);
							ledModule.play();
						}
					}
					
					
					//Log acceleration (needs to happen on press):
					
					if(route.log_acc_data) {
						accelerometerModule.start();
						accelerometerModule.acceleration().start();
					}
					
					
					
					
					
					
					//stop repeater of current bing:
					
//					if(repeat != null) {
//						Timer.ScheduledTask repeat_task = timerModule.lookupScheduledTask(((byte) repeat.id_timer));
//						if(repeat_task != null)
//							repeat_task.stop();
//					}
				});
			}
			else
				return Task.forResult(FeedbackMacro.NO_MACRO);
		
		})
				.continueWithTask((Task<Byte> task) -> {
					if(task.isFaulted())
						throw task.getError();
					
					if(route.log_acc_data) { //macro may have activated the accelerometer
						accelerometerModule.stop();
						accelerometerModule.acceleration().stop();
					}
					final byte macro_id = task.getResult();
					
					
					if(createBootMacro) {
						is_in_macro_mode = true;
						macroModule.startRecord(true);
					}
					
					return switchModule.state().addRouteAsync((RouteComponent source) -> { //visualization
						if(route.log_switch_data == Switch_route.LOG_LENGTH_OF_CLICK)
							source.log(Downloader.create_subscriber(Download_formatter.SWITCH_LENGTH));
						
						//on switch press
						
						if(macro_id != FeedbackMacro.NO_MACRO) {
							source.filter(Comparison.EQ, 1).react((DataToken token) -> {
								macroModule.execute(macro_id);
							});
						}
						
						if(route.led_behaviour == Switch_route.LED_VISUALIZE_BUTTON) {
							if(route.log_switch_data == Switch_route.LOG_NUMBER_OF_CLICKS) {
								
								RouteComponent count = source.filter(Comparison.EQ, 0).count().name("switch_count");
								count.buffer().name("switch_state");
								count.map(Function2.MODULUS, 3).multicast()
										.to().filter(Comparison.EQ, 0).react((DataToken token) -> {
									ledModule.editPattern(Led.Color.valueOf("RED"), Led.PatternPreset.SOLID).commit();
								})
										.to().filter(Comparison.EQ, 1).react((DataToken token) -> {
									ledModule.editPattern(Led.Color.valueOf("GREEN"), Led.PatternPreset.SOLID).commit();
								})
										.to().filter(Comparison.EQ, 2).react((DataToken token) -> {
									ledModule.editPattern(Led.Color.valueOf("BLUE"), Led.PatternPreset.SOLID).commit();
								});
								
//								source.filter(Comparison.EQ, 0).count().name("switch_count").map(Function2.MODULUS, 3).multicast()
//										.to().buffer().name("switch_state")
//										.to().filter(Comparison.EQ, 0).react((DataToken token) -> {
//									ledModule.editPattern(Led.Color.valueOf("RED"), Led.PatternPreset.SOLID).commit();
//								})
//										.to().filter(Comparison.EQ, 1).react((DataToken token) -> {
//									ledModule.editPattern(Led.Color.valueOf("GREEN"), Led.PatternPreset.SOLID).commit();
//								})
//										.to().filter(Comparison.EQ, 2).react((DataToken token) -> {
//									ledModule.editPattern(Led.Color.valueOf("BLUE"), Led.PatternPreset.SOLID).commit();
//								});
								
							}
							else if(route.log_switch_data == Switch_route.LOG_LENGTH_OF_CLICK) {
								source.filter(Comparison.EQ, 0).react((DataToken token) -> {
									ledModule.stop(true);
								});
							}
						}
						else if(route.log_switch_data == Switch_route.LOG_NUMBER_OF_CLICKS) {
							source.filter(Comparison.EQ, 0).count().name("switch_count").buffer().name("switch_state");
						}
					});
				})
				
				.continueWithTask((Task<Route> task) -> {
					if(task.isFaulted())
						throw task.getError();
					
					Route result = task.getResult();
					
					if(result != null) {
//						if(route.log_switch_data == Switch_route.LOG_LENGTH_OF_CLICK)
//							result.setEnvironment(0, Download_formatter.SWITCH_LENGTH);
						
						route.switch_post_id = result.id();
						
						if(route.log_switch_data == Switch_route.LOG_LENGTH_OF_CLICK) {
							route.switch_identifier = result.generateIdentifier(0);
							route.switch_log_timestamp = Calendar.getInstance().getTimeInMillis();
						}
					}
					
					return createBootMacro ? macroModule.endRecordAsync() : null;
				})
				.continueWithTask((Task<Byte> task) -> {
					if(task.isFaulted())
						throw task.getError();
					if(route.log_switch_data == Switch_route.LOG_NUMBER_OF_CLICKS) {
						//macro needs to be recreated every time because
						//when button-logic is changed, presumably, the internal state-IDs change and the already saved macro can not create button-logs anymore
						String identifier = "btn_num_" + System.currentTimeMillis();

//						String identifier = "btn_num_" + route.led_behaviour + route.log_number_led + route.log_number_color
//								+ route.log_number_vibration + route.log_number_vibration_strength + route.log_number_vibration_ms;
						
						return create_unique_feedbackMacro(identifier, () -> {
							if(route.log_number_vibration)
								vibrationModule.startMotor(route.log_number_vibration_strength, route.log_number_vibration_ms);
							
							if(route.log_number_led)
								singlePulseLed(route.log_number_color); //this will also stop the LED when led_behaviour is set to LED_VISUALIZE_BUTTON
							else if(route.led_behaviour == Switch_route.LED_VISUALIZE_BUTTON)
								ledModule.stop(true);
							
							//when button-logic is changed, presumably, the internal state-IDs change and the already saved macro can not create button-logs anymore:
							dataprocessorModule.state("switch_state").read();
							dataprocessorModule.edit("switch_count", DataProcessor.CounterEditor.class).reset();
						});
					}
					else
						return Task.forResult(FeedbackMacro.NO_MACRO);
				})
				.continueWithTask((Task<Byte> task) -> { //acceleration-logic, number-of-clicks-logic, or skip
					if(task.isFaulted())
						throw task.getError();
					
					byte macro_id = task.getResult();
					
					
					if(createBootMacro) {
						is_in_macro_mode = true;
						macroModule.startRecord(true);
					}
					
					if(route.log_switch_data == Switch_route.LOG_NUMBER_OF_CLICKS)
						return create_route_for_num_of_clicks(route, macro_id);
					else if(route.log_acc_data)
						return create_acc_route(route);
					else
						return null;
					
				})
				.continueWithTask((Task<Route> task) -> {
					if(task.isFaulted())
						throw task.getError();
					if(createBootMacro) {
						logModule.start(false);
						return macroModule.endRecordAsync();
					}
					else
						return null;
				})
				.continueWith((Task<Byte> task) -> {
					is_in_macro_mode = false;
					if(task.isFaulted()) {
						remove_switch_route();
						error(task.getError());
						
						end_progress();
						throw task.getError();
					}
					else {
						sql.save_switch_route(board_data, route);
						progress.update_ui();
						
						save_state();
						
						//TODO: create a message that asks the user if data should be deleted
//						if(route.log_switch_data != Switch_route.LOG_SWITCH_NONE || route.log_acc_data)  //TODO: disabled
//							clear_board_log(); //it is likely that we just created a log-entry by recording the macro
						
						if(will_add)
							log(R.string.info_switchRoute_set_up);
					}
					end_progress();
					
					return null;
				});
	}
	
	
	private Task<Route> create_route_for_num_of_clicks(Switch_route route, final byte macro_id) {
		update_progress(R.string.state_switch);
		
		return dataprocessorModule.state("switch_state").addRouteAsync((RouteComponent source) -> {
			source.log(Downloader.create_subscriber(Download_formatter.SWITCH_NUM));
		})
				.continueWithTask((Task<Route> task) -> {
					if(task.isFaulted())
						throw task.getError();
					
					Route result = task.getResult();
//					result.setEnvironment(0, Download_formatter.SWITCH_NUM);
					route.switch_pre_state_id = result.id();
					route.switch_identifier = result.generateIdentifier(0);
					route.switch_log_timestamp = Calendar.getInstance().getTimeInMillis();
					
					update_progress(R.string.state_timer);
					
					
					return timerModule.scheduleAsync(3000, (short) 1, true, () -> {
						macroModule.execute(macro_id);
					});
				})
				.continueWithTask((Task<Timer.ScheduledTask> timer_task) -> { //continue
					if(timer_task.isFaulted())
						throw timer_task.getError();
					
					route.timer_id = timer_task.getResult().id();
					
					if(route.log_acc_data) //we need to start the timer. We can either do that indirectly by using the existing
						return create_acc_route(route);
					else {
						return switchModule.state().addRouteAsync((RouteComponent source) -> {
							source.react((DataToken token) -> {
								start_switchCountTimer(route);
							});
						});
					}
				});
	}
	
	private void start_switchCountTimer(Switch_route route) {
		Timer.ScheduledTask timer = timerModule.lookupScheduledTask(((byte) route.timer_id));
		if(timer != null) {
			timer.stop();
			timer.start();
		}
	}
	
	private Task<Route> create_acc_route(Switch_route route) {
		update_progress(R.string.state_acc);
		
		accelerometerModule.configure()
				.odr(4f)		// 4 datapoints per second
				.range(4f)		// Set data range to +/-4g, or closet valid range
				.commit();

		return accelerometerModule.acceleration().addRouteAsync((RouteComponent source) -> {
			source.log(Downloader.create_subscriber(Download_formatter.ACC));
			source.react((DataToken token) -> {
				accelerometerModule.stop();
				accelerometerModule.acceleration().stop();
				if(route.log_switch_data == Switch_route.LOG_NUMBER_OF_CLICKS) {
					start_switchCountTimer(route);
				}

				//basic feedback or warn if battery is too low:
				if(board_data.battery.battery_for_switch)
					check_battery();
				else
					basic_switch_feedback(route);
			});
		})
		
		
		
//		accelerometerModule.configure()
//				.odr(40f) //datapoints per second
//				.range(4f)
//				.commit();
//
//		return accelerometerModule.acceleration().addRouteAsync((RouteComponent source) -> {
//			RouteComponent average = source.lowpass((byte)10);
//			average.name("accAverage");
//			average.log(Downloader.create_subscriber(Download_formatter.ACC));
//			average.react((DataToken token) -> {
//				accelerometerModule.stop();
//				accelerometerModule.acceleration().stop();
//				if(route.log_switch_data == Switch_route.LOG_NUMBER_OF_CLICKS) {
//					start_switchCountTimer(route);
//				}
//
//				//basic feedback or warn if battery is too low:
//				if(board_data.battery.battery_for_switch)
//					check_battery();
//				else
//					basic_switch_feedback(route);
//				dataprocessorModule.edit("accAverage", DataProcessor.AverageEditor.class).reset();
//			});
//		})
				.continueWithTask((Task<Route> acc_task) -> {
			if(acc_task.isFaulted())
				throw acc_task.getError();
			else {
				Route result = acc_task.getResult();
//				result.setEnvironment(0, Download_formatter.ACC);
				route.acc_id = result.id();
				route.acc_identifier = result.generateIdentifier(0);
				route.acc_log_timestamp = Calendar.getInstance().getTimeInMillis();
				
				return null;
			}
		});
	}
	
	private void basic_switch_feedback(Switch_route route) {
		if(route.led_behaviour == Switch_route.LED_BLINKING)
			singlePulseLed(route.color);
		if(route.vibration)
			vibrationModule.startMotor(route.vibration_strength, route.vibration_ms);
	}
	
	private void remove_switch_route() {
//		logModule.stop();
		Switch_route button_route_data = sql.get_switch_route(board_data);
		
		if(button_route_data != null) {
			Route route = board.lookupRoute(button_route_data.switch_pre_count_id);
			if(route != null)
				route.remove();
			
			route = board.lookupRoute(button_route_data.switch_pre_state_id);
			if(route != null)
				route.remove();
			
			route = board.lookupRoute(button_route_data.switch_post_id);
			if(route != null)
				route.remove();
			
			route = board.lookupRoute(button_route_data.acc_id);
			if(route != null)
				route.remove();
			
			Timer.ScheduledTask timer = timerModule.lookupScheduledTask(((byte) button_route_data.timer_id));
			if(timer != null)
				timer.remove();
			
			sql.remove_switch_routes(board_data);
		}
		
		progress.update_ui();
		log(R.string.info_removed_switchRoute);
	}
	
	
	//*****
	//Bing
	//*****
	private boolean has_bings_with_battery_logging() {
		List<Bing> bings = sql.get_bings_with(board_data, "battery_logging", true);
		if(bings == null) {
			RandomTimer random = sql.get_randomTimer(board_data);
			Repeat repeat = sql.get_repeat(board_data);
			return (random != null && random.battery_logging) && (repeat != null && repeat.battery_logging);
		}
		else
			return false;
	}
	private void remove_all_with_battery_logging() {
		List<Bing> bings = sql.get_bings_with(board_data, "battery_logging", true);
		
		for(int i = bings.size() - 1; i >= 0; --i) {
			remove_bing( bings.get(i));
		}
		
		RandomTimer random = get_randomTimer();
		if(random != null && random.battery_logging)
			remove_random();
		
		Repeat repeat = get_repeat();
		if(random != null && repeat.battery_logging)
			remove_repeat();
	}
	
	Task<Route> add_bing(Bing bing) {
		show_progress(R.string.state_waiting);
		
		return init_battery_forBing(bing.battery_logging).continueWithTask(task -> {
			return init_generic_feedbackMacro(bing, get_repeat());
		}).continueWithTask((Task<Byte> task) -> {//24h loop timer
			if(task.isFaulted())
				throw task.getError();
			
			update_progress(R.string.state_bing);
			
			final byte macro_id = task.getResult();
			
			return timerModule.scheduleAsync(86400000, false, () -> {
				macroModule.execute(macro_id);
				
//				if(bing.led) {
//					pulseLed(bing.color);
//				}
//
//				if(bing.vibration)
//					vibrationModule.startMotor(bing.vibration_strength, bing.vibration_ms);
//
//				if(bing.repeat && repeat != null) {
//					Timer.ScheduledTask repeat_task = timerModule.lookupScheduledTask(((byte) repeat.id_timer));
//					repeat_task.start();
//				}
//
//				if(bing.random) {
//					gyroModule.angularVelocity().start();
//					gyroModule.start();
//				}
//
//				log_battery();
				
			});
		}).continueWithTask(loopTask -> {// wait timer which "positions" the 24h loop to the right time
			if(loopTask.isFaulted())
				throw loopTask.getError();
			else {
				final Timer.ScheduledTask scheduledTask_loop = loopTask.getResult();
				bing.id_loop = scheduledTask_loop.id();
				
				
				//
				//creating wait timer (can be removed after loop-timer has started)
				//
				return timerModule.scheduleAsync(bing.countdown, (short) 1, true, scheduledTask_loop::start);
			}
		}).continueWith(wait_Task -> { //concluding configurations
			if(wait_Task.isFaulted()) {
				error(R.string.error_timer_failed, wait_Task.getError());
				remove_bing(bing);
				end_progress();
				throw wait_Task.getError();
			}
			else {
				final Timer.ScheduledTask scheduledTask_wait = wait_Task.getResult();
				scheduledTask_wait.start();
				
				bing.id_wait = scheduledTask_wait.id();
				sql.add_bing(board_data, bing);
				save_state();
				
				progress.update_ui();
				log(context.getString(R.string.info_new_bing_format, bing.countdown));
			}
			end_progress();
			return null;
		});
	}
	
	void remove_bing(Bing bing) {
		Timer.ScheduledTask waiter = timerModule.lookupScheduledTask(((byte) bing.id_wait));
		Timer.ScheduledTask loop = timerModule.lookupScheduledTask(((byte) bing.id_loop));
		
		if(waiter != null)
			waiter.remove();
		if(loop != null)
			loop.remove();
		
		String hour = bing.hour < 10 ? "0"+bing.hour : Integer.toString(bing.hour);
		String min = bing.min < 10 ? "0"+bing.min : Integer.toString(bing.min);
		log(context.getString(R.string.info_bing_removed, hour, min));
		
		sql.remove_bing(bing);
		
		if(bing.battery_logging)
			remove_battery_for_bing_if_needed();
		
		save_state();
		
		progress.update_ui();
	}
	
	
	
	List<Bing> get_bings() {
		return sql.get_bings(board_data);
	}
	
	
	//*****
	//Bing-waiter
	//*****
	void remove_waitingTimer(Bing bing) {
		Timer.ScheduledTask waiter = timerModule.lookupScheduledTask(((byte) bing.id_wait));
		
		if(waiter != null && waiter.isActive()) {
			log(context.getString(R.string.info_timer_removed, context.getString(R.string.wait)));
			bing.timer.removeCallbacksAndMessages(null);
			bing.timer = null;
			bing.id_wait = -1;
			sql.update_wait(bing);
			waiter.remove();
			save_state();
			load_state(); //Workaround: for some reason the state is now faulty (downloading data will fail until next reconnect) - but the saved state seems to be ok
		}
		
		progress.update_ui();
	}
	
	
	
	//*****
	//Random
	//*****
	RouteComponent create_random_route(RouteComponent source, byte id_timer, int part_num) {
		source.react((DataToken token) -> {
			gyroModule.stop();
			gyroModule.angularVelocity().stop();
		});
		RouteComponent timed_value = source.split().index(0);
		timed_value.limit(Passthrough.COUNT, (short) 1).name("random_value"); //this value is set once in the beginning and decides how many "counts" are needed until bing fires
//		RouteComponent stripped_value = timed_value.count().name("random_counter").map(Function2.ADD, "random_value").map(Function2.MULTIPLY, 1000).map(Function1.ABS_VALUE).map(Function2.MODULUS, part_num);
		RouteComponent stripped_value = timed_value.count().name("random_counter").map(Function2.ADD, "random_value").map(Function2.MODULUS, part_num);
		
		stripped_value.filter(Comparison.NEQ, 0).react((DataToken token) -> {
			Timer.ScheduledTask random_task = timerModule.lookupScheduledTask(id_timer);
			if(random_task != null)
				random_task.start();
		});
		
		return stripped_value;
	}
	void new_random_circle() {
		dataprocessorModule.edit("random_value", DataProcessor.PassthroughEditor.class).set((short) 1);
		dataprocessorModule.edit("random_counter", DataProcessor.CounterEditor.class).reset();
	}
	
	Task<Route> add_random(RandomTimer random) {
		if(gyroModule == null) {
			progress.error(R.string.error_no_gyroModule);
			return Task.forResult(null);
		}
		
		final int countdown = (random.min * 60 * 1000) / random.timeframes_num;
		
		show_progress(R.string.state_waiting);
		remove_random();
		return init_battery_forBing(random.battery_logging).continueWithTask(task -> { //timer for timeframe (which starts the gyroModule)
			
			return timerModule.scheduleAsync(countdown, (short) 1, true, () -> {
				gyroModule.angularVelocity().start();
				gyroModule.start();
			});
		}).continueWithTask(task -> { //main randomization: check gyro-output and decide if timer should keep running or feedback is triggered
			if(task.isFaulted())
				throw task.getError();
			
			final Timer.ScheduledTask scheduledTask = task.getResult();
			random.id_timer = scheduledTask.id();
				
			return init_generic_feedbackMacro(random, get_repeat());
		}).continueWithTask((Task<Byte> task) -> {
			if(task.isFaulted())
				throw task.getError();
			
			final byte macro_id = task.getResult();
			
			update_progress(R.string.state_random_timer);
			gyroModule.configure()
					.odr(GyroBmi160.OutputDataRate.ODR_25_HZ)
					.range(GyroBmi160.Range.FSR_125)
					.commit();
			
			return gyroModule.angularVelocity().addRouteAsync((RouteComponent source) -> {
				
				
				RouteComponent finish_source = create_random_route(source, (byte) random.id_timer, random.timeframes_num);
				finish_source.filter(Comparison.EQ, 0).react((DataToken token) -> {
					new_random_circle();
					macroModule.execute(macro_id);
				});
				
				
//				RouteComponent fired = finish_source.filter(Comparison.EQ, 0).count().name("random_fire_count");
//				fired.react((DataToken token) -> {
//					macroModule.execute(macro_id);
//					new_random_circle();
//				});
//				fired.filter(Comparison.EQ, 5).react((DataToken token) -> {
//					dataprocessorModule.edit("random_fire_count", DataProcessor.CounterEditor.class).reset();
//				});
//				fired.filter(Comparison.NEQ, 5).react((DataToken token) -> {
//					macroModule.execute(macro_id);
//
//					Timer.ScheduledTask random_task = timerModule.lookupScheduledTask((byte) random.id_timer);
//					if(random_task != null)
//						random_task.start();
//				});
				
				
//				source.multicast()
//						.to().react((DataToken token) -> {
//							gyroModule.stop();
//							gyroModule.angularVelocity().stop();
//						})
//						.to().split().index(0).multicast()
//
//						.to().limit(Passthrough.COUNT, (short) 1).name("random_value") //this value is set in the beginning and decides how many "counts" are needed until bing fires
//						.to().count().name("random_counter")
//						.map(Function2.ADD, "random_value").map(Function1.ABS_VALUE).map(Function2.MODULUS, 10).multicast()
//
//						.to().filter(Comparison.LT, 9).react((DataToken token) -> {
//							Timer.ScheduledTask random_task = timerModule.lookupScheduledTask(((byte) random.id_timer));
//							if(random_task != null)
//								random_task.start();
//						})
//						.to().filter(Comparison.GTE, 9).react((DataToken token) -> {
//							dataprocessorModule.edit("random_value", DataProcessor.PassthroughEditor.class).set((short) 1);
//							dataprocessorModule.edit("random_counter", DataProcessor.CounterEditor.class).reset();
//
//							macroModule.execute(macro_id);
//				});
				
			});
		}).continueWith((Task<Route> task) -> { //concluding configurations
			if(task.isFaulted()) {
				error(R.string.error_random_failed, task.getError());
				remove_random();
				end_progress();
				throw task.getError();
			}
			else {
				random.id_gyro = task.getResult().id();
				save_state();
				sql.add_random(board_data, random);
				
				progress.update_ui();
				
				log(context.getString(R.string.info_random_created, countdown));
			}
			end_progress();
			return null;
		});
	}
	
	private void reset_random() {
		RandomTimer random = get_randomTimer();
		Timer.ScheduledTask random_task = timerModule.lookupScheduledTask(((byte) random.id_timer));
		if(random_task != null)
			random_task.stop();
		
		new_random_circle();
		
	}
	
	void remove_random() {
		RandomTimer randomTimer = sql.get_randomTimer(board_data);
		
		if(randomTimer != null) {
			Timer.ScheduledTask timer = timerModule.lookupScheduledTask(((byte) randomTimer.id_timer));
			
			if(timer != null)
				timer.remove();
			
			
			Route route = board.lookupRoute(randomTimer.id_gyro);
			if(route != null)
				route.remove();
			
			sql.remove_random(board_data);
			
			List<Bing> bings = sql.get_bings_with(board_data, "random", true);
			for(int i = bings.size() - 1; i >= 0; --i) {
				remove_bing(bings.get(i));
			}
			
			
			log(context.getString(R.string.info_timer_removed, context.getString(R.string.random)));
			
			
			if(randomTimer.battery_logging)
				remove_battery_for_bing_if_needed();
			
			save_state();
			progress.update_ui();
		}
	}
	
	RandomTimer get_randomTimer() {
		return sql.get_randomTimer(board_data);
	}
	
	
	//*****
	//Repeat
	//*****
	Task<Route> add_repeat(Repeat repeat) {
		show_progress(R.string.state_waiting);
		remove_repeat();
		
		final int countdown = repeat.min*60*1000;
		
		return init_battery_forBing(repeat.battery_logging).continueWithTask(task -> {
			if(task.isFaulted())
				throw task.getError();
			return init_generic_feedbackMacro(repeat, get_repeat());
		})
				.continueWithTask((Task<Byte> task) -> {
					if(task.isFaulted())
						throw task.getError();
					
					update_progress(R.string.state_reminder);
					
					final byte macro_id = task.getResult();
					
					return repeat.repeat_num == 0 ?
							timerModule.scheduleAsync(countdown, true, () -> {
								macroModule.execute(macro_id);
		//						if(repeat.led)
		//							pulseLed(repeat.color);
		//						if(repeat.vibration)
		//							vibrationModule.startMotor(repeat.vibration_strength, repeat.vibration_ms);
		//
		//						log_battery();
							})
							: timerModule.scheduleAsync(countdown, repeat.repeat_num, true, () -> {
								macroModule.execute(macro_id);
		//						if(repeat.led)
		//							pulseLed(repeat.color);
		//						if(repeat.vibration)
		//							vibrationModule.startMotor(repeat.vibration_strength, repeat.vibration_ms);
		//
		//						log_battery();
							});
					
				})
				.continueWithTask(task -> {
					if(task.isFaulted())
						throw task.getError();
					
					update_progress(R.string.btn_program_switch);
					final Timer.ScheduledTask scheduledTask = task.getResult();
					repeat.id_timer = scheduledTask.id();
					
					return switchModule.state().addRouteAsync((RouteComponent source) -> {
						source.filter(Comparison.EQ, 1).react((DataToken token) -> {
							scheduledTask.stop();
						});
					});
				})
				.continueWith(task -> {
					if(task.isFaulted()) {
						error(R.string.error_repeater_failed, task.getError());
						remove_repeat();
						end_progress();
						throw task.getError();
					}
					else {
						repeat.id_switch = task.getResult().id();
						
						sql.save_repeat(board_data, repeat);
						progress.update_ui();
						
						save_state();
						log(R.string.info_repeat_created);
						end_progress();
					}
					return null;
				});
	}
	
	void remove_repeat() {
		Repeat repeat = sql.get_repeat(board_data);
		if(repeat != null) {
			
			Timer.ScheduledTask timer = timerModule.lookupScheduledTask(((byte) repeat.id_timer));
			if(timer != null) {
				timer.remove();
				log(context.getString(R.string.info_timer_removed, context.getString(R.string.repeat)));
			}
			
			Route switch_route = board.lookupRoute(repeat.id_switch);
			if(switch_route != null)
				switch_route.remove();
			
			sql.remove_repeat(board_data);
			progress.update_ui();
			
			List<Bing> bings = sql.get_bings_with(board_data, "repeat", true);
			for(int i = bings.size() - 1; i >= 0; --i) {
				remove_bing(bings.get(i));
			}
			RandomTimer random = sql.get_randomTimer(board_data);
			if(random != null && random.repeat)
				remove_random();
			
			if(repeat.battery_logging)
				remove_battery_for_bing_if_needed();
			
			save_state();
			progress.update_ui();
		}
	}
	
	Repeat get_repeat() {
		return sql.get_repeat(board_data);
	}
}
