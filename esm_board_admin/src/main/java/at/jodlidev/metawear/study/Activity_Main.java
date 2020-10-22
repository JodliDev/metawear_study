package at.jodlidev.metawear.study;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import at.jodlidev.metawear.study.data.Password;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Activity_Main extends AppCompatActivity implements View.OnClickListener {
	private final static int INTENT_CREATE_BACKUP = 10;
	private final static int INTENT_LOAD_BACKUP = 11;
	private final static int INTENT_SINGLE_DEVICE = 12;
	public static final int REQUEST_TEST = 42;
	
	private TextView btn_advanced;
	private View btn_backup;
	private View btn_restore;
	private View field_server_backup;
	private TextView btn_test;
	
	private boolean advanced_shown = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btn_advanced = findViewById(R.id.btn_advanced);
		btn_backup = findViewById(R.id.btn_backup);
		btn_restore = findViewById(R.id.btn_restore);
		field_server_backup = findViewById(R.id.field_server_backup);
		btn_test = findViewById(R.id.btn_test);
		
		findViewById(R.id.btn_single_device).setOnClickListener(this);
		findViewById(R.id.btn_multiple_devices).setOnClickListener(this);
		btn_advanced.setOnClickListener(this);
		btn_backup.setOnClickListener(this);
		btn_restore.setOnClickListener(this);
		btn_test.setOnClickListener(this);
		
		EditText server_url_el = findViewById(R.id.server_url);
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		server_url_el.setText(sharedPref.getString(Service_Upload.EXTRA_URL, ""));
		
		server_url_el.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				SharedPreferences.Editor edit = sharedPref.edit();
				edit.putString(Service_Upload.EXTRA_URL, s.toString());
				edit.apply();
			}
		});
		
		
		new SQLite(getApplicationContext()); //we need that so password gets initialized (which is part of get_appBackupName())
		((TextView) findViewById(R.id.appBackup_text)).setText(Service_Upload.get_appBackupName());
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btn_single_device:
				Intent intent_single = new Intent(this, Activity_SelectDevice.class);
				startActivityForResult(intent_single, INTENT_SINGLE_DEVICE);
				overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top);
				break;
			case R.id.btn_multiple_devices:
				startActivity(new Intent(this, Activity_MultipleDevices.class));
				overridePendingTransition(R.anim.emerge_zoom_in, R.anim.vanish_zoom_out);
				break;
			case R.id.btn_backup:
				CONSTANTS.create_file(this, INTENT_CREATE_BACKUP, "application/x-sqlite3", getString(R.string.app_name)+".db");
				break;
			case R.id.btn_restore:
				CONSTANTS.open_file(this, "*/*", INTENT_LOAD_BACKUP);
				break;
			case R.id.btn_advanced:
				if(advanced_shown) {
					advanced_shown = false;
					btn_advanced.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_down_black, 0, 0, 0);
					btn_backup.setVisibility(View.GONE);
					btn_restore.setVisibility(View.GONE);
					field_server_backup.setVisibility(View.GONE);
				}
				else {
					advanced_shown = true;
					btn_advanced.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_up_black, 0, 0, 0);
					btn_backup.setVisibility(View.VISIBLE);
					btn_restore.setVisibility(View.VISIBLE);
					field_server_backup.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.btn_test:
				btn_test.setEnabled(false);
				Service_Upload.test(this, REQUEST_TEST);
				break;
		}
	}
	
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		Context context = getApplicationContext();
		
		
		if(context == null)
			return;
		
		switch(requestCode) {
			case INTENT_SINGLE_DEVICE:
				if(resultCode != Activity.RESULT_OK)
					return;
				Intent intent = new Intent(this, Activity_SingleDevice.class);
				
				BluetoothDevice device = data.getParcelableExtra(Activity_SingleDevice.EXTRA_DEVICE);
				intent.putExtra(Activity_SingleDevice.EXTRA_DEVICE, device);
				startActivity(intent);
				overridePendingTransition(R.anim.emerge_zoom_in, R.anim.vanish_zoom_out);
				break;
			case INTENT_CREATE_BACKUP:
				if(resultCode != Activity.RESULT_OK)
					return;
				try {
					if(!CONSTANTS.write_to_file(context, new FileInputStream(context.getDatabasePath(SQLite.DATABASE_NAME)), data.getData()))
						log(getString(R.string.error_save_backup_failed, getString(R.string.error_unknown)));
				}
				catch(IOException e) {
					log(getString(R.string.error_save_backup_failed, e));
				}
				
				break;
			case INTENT_LOAD_BACKUP:
				if(resultCode != Activity.RESULT_OK)
					return;
				Uri selectedDB;
				
				if(data == null || (selectedDB = data.getData()) == null)
					return;
				
				try {
					final File target = context.getDatabasePath(SQLite.DATABASE_NAME);
					
					InputStream input = context.getContentResolver().openInputStream(selectedDB);
					FileOutputStream output = new FileOutputStream(target);
					
					if(input == null)
						throw new IOException();
					
					byte[] buffer = new byte[1024];
					int length;
					while ((length = input.read(buffer)) > 0) {
						output.write(buffer, 0, length);
					}
					input.close();
					output.close();
				}
				catch(IOException e) {
					log(getString(R.string.error_load_backup_failed, e.toString()));
				}
				break;
			case REQUEST_TEST:
				String m = data.getStringExtra(Service_Upload.EXTRA_ANSWER);
				
				btn_test.setEnabled(true);
				if(resultCode == Service_Upload.CODE_OK) {
					btn_test.setBackgroundColor(Color.GREEN);
					Toast.makeText(getApplicationContext(), R.string.info_server_connected_successfully, Toast.LENGTH_LONG).show();
				}
				else {
					Toast.makeText(getApplicationContext(), m, Toast.LENGTH_LONG).show();
					btn_test.setBackgroundColor(Color.RED);
				}
				break;
		}
	}
	
	
	private void log(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
	}
}
