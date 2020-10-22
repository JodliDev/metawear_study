package at.jodlidev.metawear.study;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.mbientlab.bletoolbox.scanner.ScannedDeviceInfo;
import com.mbientlab.bletoolbox.scanner.ScannedDeviceInfoAdapter;
import com.mbientlab.bletoolbox.scanner.BleScannerFragment.ScannerCommunicationBus;


/**
 * Created by JodliDev on 17.09.2019.
 */

public class Fragment_Touch_BleScanner extends BleScannerFragment {
	private static final int AUTO_CONNECT_THRESHOLD = -35;
	
	public static class Own_ScannedDeviceInfoAdapter extends ScannedDeviceInfoAdapter {
		ScannerCommunicationBus commBus;
		CheckBox connect_nearby;
		View empty_list_state;
		
		Own_ScannedDeviceInfoAdapter(Activity activity, int resource, CheckBox _connect_nearby, View _empty_list_state) {
			super(activity, resource);
			commBus = (ScannerCommunicationBus) activity;
			connect_nearby = _connect_nearby;
			empty_list_state = _empty_list_state;
		}
		
		@Override
		public void update(ScannedDeviceInfo newInfo) {
			empty_list_state.setVisibility(View.GONE);
			if(newInfo.rssi > AUTO_CONNECT_THRESHOLD && connect_nearby.isChecked()) {
				commBus.onDeviceSelected(newInfo.btDevice);
				return;
			}
			super.update(newInfo);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root_view = inflater.inflate(R.layout.fragment_own_blescan_device_list, container);
		
		this.scannedDevicesAdapter = new Own_ScannedDeviceInfoAdapter(this.getActivity(), R.id.blescan_entry_layout, root_view.findViewById(R.id.connect_nearby), root_view.findViewById(R.id.empty_list_state));
		this.scannedDevicesAdapter.setNotifyOnChange(true);
		this.mHandler = new Handler();
		
		return root_view;
	}
}
