package at.jodlidev.metawear.study;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.mbientlab.metawear.MetaWearBoard;

import java.util.UUID;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import bolts.Task;

public class Activity_SelectDevice extends AppCompatActivity implements com.mbientlab.bletoolbox.scanner.BleScannerFragment.ScannerCommunicationBus {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_device);
		
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
			toolbar.setNavigationOnClickListener((View view) -> {
				onBackPressed();
			});
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public UUID[] getFilterServiceUuids() {
		return new UUID[] {MetaWearBoard.METAWEAR_GATT_SERVICE};
	}
	
	@Override
	public long getScanDuration() {
		return 10000L;
	}
	
	@Override
	public void onDeviceSelected(final BluetoothDevice device) {
		Intent intent = new Intent(Activity_SelectDevice.this, Activity_SingleDevice.class);
		intent.putExtra(Activity_SingleDevice.EXTRA_DEVICE, device);
		
		setResult(RESULT_OK, intent);
		overridePendingTransition(R.anim.slide_to_left, R.anim.slide_from_right);
		finish();
	}
	
	
	public static Task<Void> reconnect(final MetaWearBoard board) {
		return board.connectAsync().continueWithTask(task -> task.isFaulted() ? reconnect(board) : task);
	}
	public static Task<Void> connect(final MetaWearBoard board) {
		return board.connectAsync().continueWithTask(task -> task.isCancelled() || !task.isFaulted() ? task : Activity_SelectDevice.reconnect(board));
	}
	public static Task<Void> connect(final MetaWearBoard board, int timeout) {
		return board.connectAsync(timeout).continueWithTask(task -> task.isCancelled() || !task.isFaulted() ? task : Activity_SelectDevice.reconnect(board));
	}
	
	
	public void onResume() {
		super.onResume();
		BleScannerFragment scanner = ((BleScannerFragment) getFragmentManager().findFragmentById(R.id.scanner_fragment));
		if(scanner != null) {
			try {
				scanner.startBleScan();
			}
			catch(NullPointerException e) { //when bluetooth is disabled, BleScannerFragment.btAdapter is null - but we cant check that from the outside...
				//do nothing
			}
		}
	}
}
