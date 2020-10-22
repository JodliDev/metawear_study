package at.jodlidev.metawear.study;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.android.BtleService;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import at.jodlidev.metawear.study.data.DataBox;
import bolts.Continuation;

public class Activity_MultipleDevices extends AppCompatActivity implements ServiceConnection {
	public static final int INTENT_ADD_DEVICE = 10;
	private static final int INTENT_IMPORT_CONFIG = 11;
	public static final int REQUEST_UPLOAD_DB = 103;
	
	private BtleService.LocalBinder binder = null;
	
	private LogAdapter logAdapter;
	private BoardAdapter boardAdapter;
	private MenuItem action_add;
	
	private Activity_SingleDevice.Progress_Info_Single main_progress;
	private boolean force_disconnect = false;
	
	
	private static class BoardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private final static int TYPE_BOARD = 1;
		private final static int TYPE_BTN_ADD = 2;
		
		private SQLite sql;
		private List<Board_logic> data = new ArrayList<>();
		private LogAdapter logAdapter;
		private BtleService.LocalBinder binder;
		private List<DataBox> instructions;
		private WeakReference<Activity_MultipleDevices> activity;
		
		static class ViewHolder extends RecyclerView.ViewHolder {
			TextView state;
			TextView mac_address;
			ProgressBar progress_bar;
			
			
			ViewHolder(ViewGroup vG) {
				super(vG);
				state = vG.findViewById(R.id.state);
				mac_address = vG.findViewById(R.id.mac_address);
				progress_bar = vG.findViewById(R.id.progress_bar);
			}
		}
		
		static class FooterHolder extends RecyclerView.ViewHolder {
			Button btn;
			FooterHolder(View vG, final Activity_MultipleDevices activity) {
				super(vG);
				btn = vG.findViewById(R.id.btn_add);
				btn.setOnClickListener((View v) -> {
					if(activity != null)
						activity.add_device();
				});
			}
		}
		
		BoardAdapter(Activity_MultipleDevices _activity, BtleService.LocalBinder _binder, LogAdapter _logAdapter, List<DataBox> _instructions) {
			activity = new WeakReference<>(_activity);
			logAdapter = _logAdapter;
			binder = _binder;
			instructions = _instructions;
			sql = new SQLite(_activity.getApplicationContext());
		}
		
		@Override
		@NonNull
		public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			switch(viewType) {
				default:
				case TYPE_BOARD:
					ViewGroup vG = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board, parent, false);
					vG.setOnClickListener((View v) -> {
					int position = (int) v.getTag();
					Board_logic logic = data.get(position);
					
					if(!((Progress_Info_Multiple) logic.progress).is_loading)
						logic.import_config(instructions);
				});
					return new ViewHolder(vG);
				case TYPE_BTN_ADD:
					return new FooterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_device, parent, false), activity.get());
			}
		}
		
		@Override
		public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
			if(holder instanceof ViewHolder) {
				ViewHolder viewHolder = (ViewHolder) holder;
				Board_logic logic = data.get(position);
				Progress_Info_Multiple progress = (Progress_Info_Multiple) logic.progress;
				Resources res = viewHolder.progress_bar.getContext().getResources();
				
				viewHolder.itemView.setTag(position);
				
				viewHolder.mac_address.setText(progress.mac);
				viewHolder.state.setText(progress.state);
				viewHolder.progress_bar.setMax(1);
				
				if(progress.error) {
					viewHolder.progress_bar.setIndeterminate(false);
					viewHolder.progress_bar.setProgressDrawable(res.getDrawable(R.drawable.ic_error_red));
					if(logic.is_ready)
						logic.enableLed("RED");
				}
				else if(progress.is_loading) {
					viewHolder.progress_bar.setIndeterminate(true);
					if(progress.connected)
						logic.disableLed();
				}
				else {
					viewHolder.progress_bar.setIndeterminate(false);
					viewHolder.progress_bar.setProgressDrawable(res.getDrawable(R.drawable.ic_success_dark_green));
					if(logic.is_ready)
						logic.enableLed("GREEN");
				}
			}
		}
		
		@Override
		public int getItemViewType(int position) {
			return position == data.size() ? TYPE_BTN_ADD : TYPE_BOARD;
		}
		
		@Override
		public int getItemCount() {
			return data.size() + 1;
		}
		
		void add(BluetoothDevice btDevice) {
			Activity a = activity.get();
			final int position = data.size();
			final Progress_Info_Multiple progress = new Progress_Info_Multiple(a, logAdapter, this, position, btDevice.getAddress());
			Board_logic logic = new Board_logic(
					a.getApplicationContext(),
					sql,
					progress,
					binder,
					btDevice);
			
			progress.logic = logic;
			
			data.add(logic);
			notifyItemInserted(position);
			
			MetaWearBoard board = logic.board;
			
			logic.connect(() -> {
						board.onUnexpectedDisconnect(status -> {
							progress.show_progress(R.string.state_connecting);
							
							Activity_SelectDevice.connect(board)
									.continueWith((Continuation<Void, Void>) task -> {
										if(!task.isCancelled())
											progress.end_progress();
										else {
											progress.log_error(a.getString(R.string.error_connection_failed));
											notifyItemChanged(position);
										}
										
										return null;
									});
						});
						
						logic.import_config(instructions);
					}, () -> {
						progress.log_error(a.getString(R.string.error_wrong_password));
					},
					null);
		}
		
//		void add(Activity activity, MetaWearBoard newBoard) {
//			final int position = data.size();
//			final Progress_Info_Multiple progress = new Progress_Info_Multiple(activity, logAdapter, this, position, newBoard.getMacAddress());
//			final Board_logic logic = new Board_logic(activity.getApplicationContext(), sql, progress);
//			progress.logic = logic;
//
//			data.add(logic);
//			notifyItemInserted(position);
//
//			progress.show_progress(R.string.state_connecting);
//			Activity_SelectDevice.connect(newBoard)
//					.continueWith(connect_task -> {
//						progress.end_progress();
//						if(connect_task.isCancelled()) {
//							progress.log_error(activity.getString(R.string.error_connection_failed));
//							notifyItemChanged(position);
//							return null;
//						}
//						logic.bind_board(newBoard);
//						progress.connected = true;
//
//						logic.check_password(() -> {
//							logic.import_config(instructions);
//						}, () -> {
//							progress.log_error(activity.getString(R.string.error_wrong_password));
//						},
//								null);
//
//
//
//						newBoard.onUnexpectedDisconnect(status -> {
//							progress.show_progress(R.string.state_connecting);
//
//							newBoard.connectAsync().continueWithTask(reconnect_task -> reconnect_task.isCancelled() || !reconnect_task.isFaulted() ? reconnect_task : Activity_SelectDevice.reconnect(newBoard))
//									.continueWith((Continuation<Void, Void>) task -> {
//										if(!task.isCancelled())
//											progress.end_progress();
//										else {
//											progress.log_error(activity.getString(R.string.error_connection_failed));
//											notifyItemChanged(position);
//										}
//
//										return null;
//									});
//						});
//						return null;
//					});
//		}
		
		void disconnect() {
			for(Board_logic logic : data) {
				logic.disableLed();
				logic.board.disconnectAsync();
				logic.destroy();
			}
		}
	}
	
	private static class Progress_Info_Multiple extends Progress_Info {
		private Context context;
		private Activity activity;
		
		private LogAdapter logAdapter;
		private BoardAdapter boardAdapter;
		private Board_logic logic;
		
		
		private int position;
		private String mac;
		
		String state;
		boolean connected = false;
		boolean is_loading = true;
		
		Progress_Info_Multiple(Activity _activity, LogAdapter _logAdapter, BoardAdapter _boardAdapter, int _position, String _mac) {
			super(_activity);
			activity = _activity;
			context = activity.getApplicationContext();
			logAdapter = _logAdapter;
			boardAdapter = _boardAdapter;
			position = _position;
			mac = _mac;
		}
		@Override
		public void update_ui() {}
		
		@Override
		public void log(String s) {
//			if(error)
			activity.runOnUiThread(() -> {
				logAdapter.add(mac+":\n"+s, false);
			});
			save_log(s);
		}
		
		@Override
		public void log_error(String s) {
			is_loading = false;
			error = true;
			state = s;
			
			activity.runOnUiThread(() -> {
				if(logAdapter != null)
					logAdapter.add(mac+":\n"+s, true);
			});
			save_log(s);
		}
		
		
		private void save_log(String s) {
			logic.save_log(s);
		}
		
		@Override
		public void show_loader(int res) {
			state = context.getString(res);
			is_loading = true;
			boardAdapter.notifyItemChanged(position);
		}
		@Override
		public void update_loader(String s) {
			state = s;
			boardAdapter.notifyItemChanged(position);
		}
		@Override
		public void end_loader() {
			state = "";
			is_loading = false;
			boardAdapter.notifyItemChanged(position);
		}
		@Override
		public void new_loader_block() {}
		@Override
		public void end_loader_block() {}
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_multiple_devices);
		
		//
		//Logger
		//
		RecyclerView log_box = findViewById(R.id.log_box);
		log_box.setHasFixedSize(true);
		
		logAdapter = new LogAdapter(log_box);
		log_box.setAdapter(logAdapter);
		
		
		final TextView log_header = findViewById(R.id.log_header);
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
		
		
		
		main_progress = new Activity_SingleDevice.Progress_Info_Single(this, logAdapter, log_header, findViewById(R.id.loading_box), findViewById(R.id.state_el));
		
		//
		//Toolbar
		//
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
			toolbar.setNavigationOnClickListener((View view) -> {
				if(!force_disconnect && Service_Upload.upload_database(this, REQUEST_UPLOAD_DB)) {
					main_progress.show_loader(R.string.state_uploading);
					force_disconnect = true;
				}
				else
					onBackPressed();
			});
		}
		
		connect();
		
		CONSTANTS.open_file(this, "*/*", INTENT_IMPORT_CONFIG);
	}
	
	
	private void add_device() {
		Intent intent_multiple = new Intent(this, Activity_SelectDevice.class);
		startActivityForResult(intent_multiple, INTENT_ADD_DEVICE);
		overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top);
		
	}
	
	private void connect() {
		getApplicationContext().bindService(new Intent(this, BtleService.class), this, BIND_AUTO_CREATE);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch(requestCode) {
			case INTENT_ADD_DEVICE:
				if(resultCode != Activity.RESULT_OK)
					return;
				BluetoothDevice device = data.getParcelableExtra(Activity_SingleDevice.EXTRA_DEVICE);
				
				if(device != null) {
//					boardAdapter.add(this, binder.getMetaWearBoard(device));
					boardAdapter.add(device);
				}
				break;
			case INTENT_IMPORT_CONFIG:
				if(resultCode != Activity.RESULT_OK) {
					force_disconnect = true;
					finish();
				}
				List<DataBox> instructions;
				if(binder == null)
					connect();
				try {
					instructions = Board_logic.read_config_from_JSON(getApplicationContext(), data);
				}
				catch(IOException e) {
					if(main_progress != null)
						main_progress.log_error(getString(R.string.error_load_config_failed, e.toString()));
					return;
				}
				
				
				//
				//Board-List
				//
				RecyclerView connected_devices_list = findViewById(R.id.connected_devices_list);
				connected_devices_list.setHasFixedSize(true);
				boardAdapter = new BoardAdapter(this, binder, logAdapter, instructions);
				connected_devices_list.setAdapter(boardAdapter);
				
				break;
			case REQUEST_UPLOAD_DB:
				main_progress.end_progress();
				if(resultCode == Service_Upload.CODE_OK) {
					onBackPressed();
				}
				else {
					String m = data.getStringExtra(Service_Upload.EXTRA_ANSWER);
					main_progress.log_error(m);
				}
				break;
		}
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		binder = (BtleService.LocalBinder) service;
		if(action_add != null)
			action_add.setEnabled(true);
		
//		View add_board = findViewById(R.id.add_board);
//		add_board.setVisibility(View.VISIBLE);
//		add_board.setOnClickListener((View v) -> {
//			Intent intent_multiple = new Intent(this, Activity_SelectDevice.class);
//			startActivityForResult(intent_multiple, INTENT_ADD_DEVICE);
//			overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top);
//		});
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		connect();
	}
	
	@Override
	public void finish() {
		if(!force_disconnect && Service_Upload.upload_database(this, REQUEST_UPLOAD_DB)) {
			main_progress.show_loader(R.string.state_uploading);
			force_disconnect = true;
			return;
		}
		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
		super.finish();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(boardAdapter != null)
			boardAdapter.disconnect();
		
		boardAdapter = null;
		logAdapter = null;
		action_add = null;
		
		getApplicationContext().unbindService(this);
	}
}
