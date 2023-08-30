package at.jodlidev.metawear.study;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.module.Haptic;
import com.mbientlab.metawear.module.Led;
import com.tooltip.Tooltip;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import at.jodlidev.metawear.study.data.LogMessages;
import bolts.Continuation;

public class Activity_SingleDevice extends AppCompatActivity implements ServiceConnection, Communication {
	public final static String EXTRA_DEVICE = "extra_device";
	private final String FRAGMENT_TAG = "main_fragment";
	public static final int REQUEST_UPLOAD = 102;
	public static final int REQUEST_UPLOAD_DB = 103;
	
	private BluetoothDevice btDevice;
	private Board_logic logic;
	
	private int current_site;
	private boolean force_disconnect = false;
	
	private ActionBar actionBar;
	
	private Progress_Info_Single progress;
	private LogAdapter logAdapter;
	
	
	public static class Progress_Info_Single extends Progress_Info {
//		private ProgressDialog currentDialog = null;
		
		private LogAdapter logAdapter;
		Board_logic logic = null;
		
		
		private TextView log_title;
		private View loading_box;
		private TextView state_el;
		
		private Tooltip.Builder log_tooltip;
//		private final Handler waiter = new Handler();
		
		Progress_Info_Single(Activity _activity, LogAdapter _logAdapter, TextView _log_title, View _loading_box, TextView _state_el) {
			super(_activity);
			logAdapter = _logAdapter;
			
			log_title = _log_title;
			loading_box = _loading_box;
			state_el = _state_el;
			create_tooltip("");
		}
		
		private Activity_SingleDevice get_activity() {
			return (Activity_SingleDevice) activity;
		}
		
		@Override
		public void update_ui() {
			if(activity instanceof Activity_SingleDevice)
				get_activity().update_ui();
		}
		@Override
		public void log(final String s) {
			if(!error) {
				create_tooltip(s);
				
				//Problem:
				//(old code)
				//UI-stuff is moved to the UI-thread and takes time. While it is processed, it is possible that another log comes in
				//Since the first Tooltip is not finished, Tooltip.dismiss() does nothing yet. But Tooltip.Builder.setText() also does not work anymore because the builder is already interpreted.
				//This leads to multiple Tooltips at once with different text.
				//Best solution I came up with:
				//We start the UI-stuff after this thread is done working but do Tooltip.Builder.setText on the same Object immediately.
				//Tooltip.show() will still be called multiple times, and will still lead to multiple Tooltips, but they will have the same text, so the user will only see one
//				waiter.post(() -> {
//					activity.runOnUiThread(() -> {
//						log_tooltip.show();
//					});
//				});
				
				activity.runOnUiThread(() -> {
					log_tooltip.show();
				});
			}
			
			activity.runOnUiThread(() -> {
				if(logAdapter != null)
					logAdapter.add(s, false);
			});
			
			save_log(s);
			Log.d("progress", s);
		}
		
		@Override
		public void log_error(String s) {
			log_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error_red, 0, 0, 0);
			
			activity.runOnUiThread(() -> {
				if(logAdapter != null)
					logAdapter.add(s, true);
				
				final ObjectAnimator backgroundColorAnimator = ObjectAnimator.ofObject(log_title,
						"backgroundColor",
						new ArgbEvaluator(),
						0xFFFF0000,
						activity.getResources().getColor(R.color.colorPrimary));
				backgroundColorAnimator.setDuration(1500);
				backgroundColorAnimator.start();
			});
			if(logic != null)
				logic.signal_error();
			save_log(s);
			Log.d("progress", s);
		}
		
		private void save_log(String s) {
			if(logic != null)
				logic.save_log(s);
		}
		
		synchronized private void create_tooltip(String text) {
			if(log_tooltip == null) {
				log_tooltip = new Tooltip.Builder(log_title)
						.setText(text)
						.setCornerRadius(25f)
						.setGravity(Gravity.TOP)
						.setCancelable(true)
						.setDismissOnClick(true)
						.setTextColor(Color.WHITE)
						.setBackgroundColor(activity.getColor(R.color.colorAccent));
			}
			else {
				log_tooltip.setText(text);
			}
		}
		
		@Override
		public void show_loader(int res) {
			loading_box.setVisibility(View.VISIBLE);
			state_el.setText(res);
			
			Log.d("progress", activity.getString(res));
		}
		@Override
		public void update_loader(String s) {
//			if(currentDialog != null)
//				currentDialog.setMessage(s);
			
			
			loading_box.setVisibility(View.VISIBLE);
			state_el.setText(s);
			Log.d("progress", s);
		}
		@Override
		public void end_loader() {
//			if(currentDialog != null) {
//				currentDialog.dismiss();
//				currentDialog = null;
//			}
			loading_box.setVisibility(View.GONE);
		}
		
		@Override
		void new_loader_block() {
			logAdapter.new_block(true);
		}
		@Override
		void end_loader_block() {
			logAdapter.end_block();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//
		//Prepare Ui
		//
		setContentView(R.layout.activity_single_device);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		actionBar = getSupportActionBar();
		if(actionBar != null) {
//			actionBar.setDisplayHomeAsUpEnabled(true);
//			actionBar.setDisplayShowHomeEnabled(true);
			toolbar.setNavigationOnClickListener((View view) -> {
				onBackPressed();
			});
		}
		
		
		
		//
		//Logging stuff
		//
		
		RecyclerView log_box = findViewById(R.id.log_box);
		log_box.setHasFixedSize(true);
		
		logAdapter = new LogAdapter(log_box);
		log_box.setAdapter(logAdapter);
		
		final TextView log_header = findViewById(R.id.log_header);
		progress = new Progress_Info_Single(this, logAdapter, log_header, findViewById(R.id.loading_box), findViewById(R.id.state_el));

		final BottomSheetBehavior bottomSheetBehavior =  BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
		bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View view, int i) {
				log_header.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(i == BottomSheetBehavior.STATE_COLLAPSED ? R.drawable.ic_arrow_up_white : R.drawable.ic_arrow_down_white), null, null, null);
			}

			@Override
			public void onSlide(@NonNull View view, float v) {}
		});
		
		log_header.setOnClickListener(
				(View v) -> bottomSheetBehavior.setState(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED ? BottomSheetBehavior.STATE_COLLAPSED : BottomSheetBehavior.STATE_EXPANDED)
		);
		
		bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); //this seems to be important so the position of log_header is set correctly (and tooltips would be placed outside the screen)
		
		
		btDevice = getIntent().getParcelableExtra(EXTRA_DEVICE);
		getApplicationContext().bindService(new Intent(this, BtleService.class), this, BIND_AUTO_CREATE);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_device_setup, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_disconnect:
				finish();
				return true;
			case R.id.action_debug_random:
				Dialog_test_random.open(this, logic);
				return true;
//			case R.id.action_different_download:
//				Fragment f = get_fragment();
//
//				if(f instanceof Fragment_DeviceSetup) {
//					((Fragment_DeviceSetup) f).download();
//				}
//				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void goto_site(int site) {
		current_site = site;
		FragmentBase f;
		switch(site) {
			case Communication.SITE_CONFIGURATION:
				f = new Fragment_ConfigurationRoot();
				
				actionBar.setDisplayHomeAsUpEnabled(true);
				actionBar.setDisplayShowHomeEnabled(true);
				break;
			case Communication.SITE_MAIN_SETUP:
				f = new Fragment_DeviceSetup();
				actionBar.setDisplayHomeAsUpEnabled(false);
				actionBar.setDisplayShowHomeEnabled(false);
				break;
			default:
				f = new FragmentBase();
		}
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
//		transaction.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right);
		transaction.setCustomAnimations(R.anim.appear_from_0, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.vanish_to_0);
		transaction.replace(R.id.device_setup_fragment, f, FRAGMENT_TAG).commit();
		transaction.addToBackStack(null);
	}
	
	@Override
	public Progress_Info_Single get_progressInfo() {
		return progress;
	}
	@Override
	public Board_logic get_logic() {
		return logic;
	}
	
	@Override
	public void update_ui() {
		runOnUiThread(() -> {
			Fragment f = get_fragment();
			
			if(f instanceof Fragment_ConfigurationRoot)
				((Fragment_ConfigurationRoot) f).update_ui();
		});
	}
	
	private Fragment get_fragment() {
		return getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
	}
	
	
	
	//
	//Connection
	//
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		logic = new Board_logic(
				getApplicationContext(),
				new SQLite(getApplicationContext()),
				progress,
				(BtleService.LocalBinder) service,
				btDevice);
		progress.logic = logic;
		
		MetaWearBoard board = logic.board;
		
		logic.connect(() -> {
					runOnUiThread(() -> {
						progress.show_progress(R.string.state_loading); //this is just here so that all old logs will be shown in one block
						for(LogMessages log : logic.get_logs()) {
							logAdapter.add(log.text, log.timestamp);
						}
						progress.end_progress();
						progress.log(getString(R.string.info_connected));
					});
					
					
					Led ledModule = board.getModule(Led.class);
					Haptic vibrationModule = board.getModule(Haptic.class);
					
					
					ledModule.stop(true);
					ledModule.editPattern(Led.Color.GREEN, Led.PatternPreset.PULSE).highTime((short) 1000).pulseDuration((short) 2500).repeatCount((byte) 2).commit();
					ledModule.play();
					if(!BuildConfig.DEBUG)
						vibrationModule.startMotor(50.f, (short) 500);
					
					goto_site(Communication.SITE_MAIN_SETUP);
					
					board.onUnexpectedDisconnect(status -> {
						progress.show_progress(R.string.state_connecting);
						Activity_SelectDevice.connect(board)
								.continueWith((Continuation<Void, Void>) task -> {
									if(!task.isCancelled()) {
										runOnUiThread(() -> {
											progress.log(getString(R.string.info_connected));
											progress.end_progress();
											progress.end_loader();
										});
									}
									else
										finish();
									
									return null;
								});
					});
				},
				() -> {
					runOnUiThread(() ->
							new AlertDialog.Builder(Activity_SingleDevice.this)
									.setTitle(R.string.confirm)
									.setMessage(R.string.confirm_wrong_password)
									.setIcon(R.drawable.ic_warning_black)
									.setPositiveButton(R.string.btn_delete_all_data, (DialogInterface dialog, int whichButton) -> {
										new AlertDialog.Builder(Activity_SingleDevice.this)
												.setTitle(R.string.confirm)
												.setMessage(R.string.confirm_reset)
												.setIcon(R.drawable.ic_warning_black)
												.setPositiveButton(android.R.string.yes, (DialogInterface dialog_confirm, int whichButton_confirm) -> {
													logic.reset_board(true).continueWith(task -> {
														logic.set_password();
														goto_site(Communication.SITE_MAIN_SETUP);
														
														return null;
													});
												})
												.setNegativeButton(android.R.string.no, (DialogInterface dialog_confirm, int whichButton_confirm) -> {
													finish();
												}).show();
									})
									.setNegativeButton(android.R.string.no, (DialogInterface dialog, int whichButton) -> {
										finish();
									}).show()
					);
				},
				() -> {
					if(logic.loaded_from_serialized_state) {
						logic.set_board_to_rebooted();
						runOnUiThread(() ->
								new AlertDialog.Builder(Activity_SingleDevice.this)
										.setMessage(R.string.info_no_password)
										.setPositiveButton(android.R.string.ok, null).show()
						);
					}
				});
		
		
		runOnUiThread(() -> {
			ActionBar actionBar = getSupportActionBar();
			if(actionBar != null)
				actionBar.setTitle(board.getMacAddress());
		});
		
		
		
		
//		metawear = ((BtleService.LocalBinder) service).getMetaWearBoard(btDevice);
//
//		progress.show_progress(R.string.state_connecting);
//		runOnUiThread(() -> {
//			ActionBar actionBar = getSupportActionBar();
//			if(actionBar != null)
//				actionBar.setTitle(metawear.getMacAddress());
//		});
//		Activity_SelectDevice.connect(metawear)
//			.continueWith(task -> {
//				if(!task.isCancelled())
//					onBoardConnected();
//				progress.end_progress();
//
//				return null;
//			});
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {

	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode) {
			case REQUEST_UPLOAD:
				String m = data.getStringExtra(Service_Upload.EXTRA_ANSWER);
				
				if(resultCode == Service_Upload.CODE_OK)
					progress.log(m);
				else
					progress.log_error(m);
				
				if(--Downloader.uploading_count <= 0) {
					Downloader.close();
					progress.end_progress();
				}
				else
					progress.update_progress(getString(R.string.state_uploading_with_count, Downloader.uploading_count));
				
				break;
			case REQUEST_UPLOAD_DB:
				progress.end_progress();
				if(resultCode == Service_Upload.CODE_OK) {
					finish();
				}
				else {
					m = data.getStringExtra(Service_Upload.EXTRA_ANSWER);
//					progress.log(m);
					progress.log_error(m);
				}
				break;
		}
	}
	
	@Override
	public void onBackPressed() {
		if(current_site == Communication.SITE_CONFIGURATION) {
			getSupportFragmentManager().popBackStack(); //mostly the same as goto_site(Communication.SITE_MAIN_SETUP) - but we can make use of the backstack
			current_site = Communication.SITE_MAIN_SETUP;
			actionBar.setDisplayHomeAsUpEnabled(false);
			actionBar.setDisplayShowHomeEnabled(false);
//			goto_site(Communication.SITE_MAIN_SETUP);
		}
		else
			finish();
	}
	
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		if(logic != null)
//			logic.destroy();
//
//		finish();
//	}
	
	
	@Override
	public void finish() {
		if(logic != null) {
			logic.destroy();
			logic = null;
			if(!force_disconnect && Service_Upload.upload_database(this, REQUEST_UPLOAD_DB)) {
				progress.show_loader(R.string.state_uploading);
				force_disconnect = true;
				return;
			}
		}
		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
		super.finish();
		
		
//		if(logic != null && logic.board.isConnected()) {
//			logic.board.disconnectAsync();
//			if(!force_disconnect && Service_Upload.upload_database(this, REQUEST_UPLOAD_DB)) {
//				progress.show_loader(R.string.state_uploading);
//				force_disconnect = true;
//				return;
//			}
//		}
//		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
//		super.finish();
	}
}
