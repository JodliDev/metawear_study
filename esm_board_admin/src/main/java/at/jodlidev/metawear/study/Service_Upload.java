package at.jodlidev.metawear.study;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import at.jodlidev.metawear.study.data.Password;

/**
 * Created by JodliDev on 27.09.18.
 */
public class Service_Upload extends IntentService {
	public static final String PENDING_RESULT_EXTRA = "pending_result";
	public static final String EXTRA_URL = "url";
	public static final String EXTRA_DATA = "data";
	public static final String EXTRA_FOLDER = "folder";
	public static final String EXTRA_FILENAME = "filename";
	
	public static final String EXTRA_ANSWER = "answer";
	
	public static final String FOLDER_BACKUPS = "app_backups";
	
	public static final int CODE_ERROR = Activity.RESULT_CANCELED;
	public static final int CODE_OK = Activity.RESULT_OK;
	
	public Service_Upload() {
		super("Request_Service");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		PendingIntent reply = intent.getParcelableExtra(PENDING_RESULT_EXTRA);
		
		Bundle extras = intent.getExtras();
		if(extras == null)
			return;
		File data = (File) extras.get(EXTRA_DATA);
		
		String answer;
		int code = CODE_OK;
		String filename = null;
		
		
		try {
			URL url = new URL(extras.getString(EXTRA_URL));
			
			HttpURLConnection urlConnection;
			if(BuildConfig.DEBUG)
				urlConnection = (HttpURLConnection) url.openConnection();
			else
				urlConnection = (HttpsURLConnection) url.openConnection();
			
			urlConnection.setDoInput(true);
			
			
			
			if(data == null) {
			
			}
			else {
				filename = extras.containsKey(EXTRA_FILENAME) ? extras.getString(EXTRA_FILENAME) : data.getName();
				
				String lineEnd = "\r\n";
				String twoHyphens = "--";
				String boundary = "*****";
				int maxBufferSize = 1024 * 1024;
				
				urlConnection.setRequestMethod("POST");
				urlConnection.setDoOutput(true);
				urlConnection.setUseCaches(false);
				urlConnection.setRequestProperty("Connection", "Keep-Alive");
				urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
				urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				urlConnection.setRequestProperty("uploaded_file", filename);
				
				DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
				
				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + filename + "\"" + lineEnd);
				
				dos.writeBytes(lineEnd);
				
				FileInputStream fileInputStream = new FileInputStream(data);
				// create a buffer of maximum size
				int bytesAvailable = fileInputStream.available();
				
				int bufferSize = Math.min(bytesAvailable, maxBufferSize);
				byte[] buffer = new byte[bufferSize];
				
				// read file and write it into form...
				int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				
				
				while(bytesRead > 0) {
					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
					
				}
				
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
				
				fileInputStream.close();
				dos.flush();
				dos.close();
			}
			
			
			if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				code = CODE_ERROR;
				if(filename == null)
					answer = getString(R.string.error_server_error, urlConnection.getResponseMessage());
				else
					answer = getString(R.string.error_server_error_with_file, filename, urlConnection.getResponseMessage());
			}
			else {
				InputStream in = urlConnection.getInputStream();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
				StringBuilder response = new StringBuilder();
				String line;
				while((line = bufferedReader.readLine()) != null) {
					response.append(line).append('\n');
				}
				
				if(response.toString().trim().equals("ok")) {
					answer = getString(R.string.info_upload_successful, filename);
				}
				else {
					code = CODE_ERROR;
					answer = getString(R.string.error_server_error_with_file, filename, response.toString());
				}
			}
		}
		catch(IOException e) {
			code = CODE_ERROR;
			if(filename == null)
				answer = getString(R.string.error_connection_failed);
			else
				answer = getString(R.string.error_upload_failed, filename, e.getMessage());
		}
		
		
		try {
			Intent result = new Intent();
			result.putExtra(EXTRA_ANSWER, answer);
			reply.send(this, code, result);
		}
		catch(PendingIntent.CanceledException exc) {
			Log.d("Request_to_String", "Request was canceled");
		}
	}
	private static String create_url(Activity activity) {
		SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(activity);
		if(!preferenceManager.contains(EXTRA_URL))
			return null;
		
		String url = preferenceManager.getString(EXTRA_URL, "").trim();
		
		if(url.length() < 5)
			return null;
		
		if(url.startsWith("http://")) {
			if(!BuildConfig.DEBUG)
				url = "https" + url.substring(4);
		}
		else if(!url.startsWith("https://"))
			url = "https://" + url;
		
		if(!url.endsWith(".php")) {
			if(!url.endsWith("/"))
				url += "/";
			url += "wearable_sync.php";
		}
		
		return url;
	}
	private static Intent create_intent(Activity activity, int requestCode, String url) {
		PendingIntent pendingResult = activity.createPendingResult(requestCode, new Intent(), 0);
		Intent intent = new Intent(activity.getApplicationContext(), Service_Upload.class);
		
		intent.putExtra(Service_Upload.PENDING_RESULT_EXTRA, pendingResult);
		intent.putExtra(Service_Upload.EXTRA_URL, url);
		
		return intent;
	}
	
	
	static boolean test(Activity activity, int requestCode) {
		return fire(activity, requestCode, null, null, "test", "1");
	}
	static boolean fire(Activity activity, int requestCode, String folder, File data) {
		return fire(activity, requestCode, data, null, "folder", folder);
	}
	static boolean fire(Activity activity, int requestCode, File data, String filename, String key, String value) {
		String url = create_url(activity);
		if(url == null)
			return false;
		
		if(key != null)
			url += "?"+key+"="+value;
		
		Log.d("qwe", url);
		Intent intent = create_intent(activity, requestCode, url);
		if(intent == null)
			return false;
		
		if(filename != null)
			intent.putExtra(Service_Upload.EXTRA_FILENAME, filename);
		
		if(data != null)
			intent.putExtra(Service_Upload.EXTRA_DATA, data);
		
		activity.startService(intent);
		return true;
	}
	
	static String get_appBackupName() {
		return Build.MODEL + '_' + Password.get();
	}
	
	static boolean upload_database(Activity activity, int requestCode) {
		return fire(activity, requestCode, activity.getDatabasePath(SQLite.DATABASE_NAME), get_appBackupName(), "folder", FOLDER_BACKUPS);
	}
}
