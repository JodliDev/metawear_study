package at.jodlidev.metawear.study;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.JsonReader;
import android.util.JsonToken;



import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.fragment.app.Fragment;
import at.jodlidev.metawear.study.data.Battery;
import at.jodlidev.metawear.study.data.Bing;
import at.jodlidev.metawear.study.data.BootMacro;
import at.jodlidev.metawear.study.data.DataBox;
import at.jodlidev.metawear.study.data.RandomTimer;
import at.jodlidev.metawear.study.data.Repeat;
import at.jodlidev.metawear.study.data.Switch_route;

/**
 * Created by JodliDev on 15.03.2019.
 */
public class CONSTANTS {
	public static String FIRMWARE_OPTIMAL = "1.4.5";
	public static String return_time(Context context, Calendar cal) {
		java.text.DateFormat format = android.text.format.DateFormat.getTimeFormat(context);
		return format.format(cal.getTime());
	}
	
	
	private static Intent create_open_file_intent(String type, String name) {
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType(type);
		intent.putExtra(Intent.EXTRA_TITLE, name);
		
		return intent;
	}
	public static void create_file(Fragment fragment, int response_code, String type, String name) {
		fragment.startActivityForResult(create_open_file_intent(type, name), response_code);
	}
	public static void create_file(Activity activity, int response_code, String type, String name) {
		activity.startActivityForResult(create_open_file_intent(type, name), response_code);
	}
	
	public static boolean write_to_file(Context context, InputStream input, Uri uri) throws IOException{
		if(uri == null)
			return false;
		try {
			OutputStream output = context.getContentResolver().openOutputStream(uri);
			
			if(output == null)
				throw new IOException("OutputStream  was null");
			
			byte[] buffer = new byte[1024];
			int length;
			while((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
			input.close();
			output.flush();
			output.close();
		}
		catch(IOException e) {
			throw e;
		}
		
		return true;
	}
	
	public static void open_file(Activity activity, String type, int response_code) {
		Intent intent_restore = new Intent(Intent.ACTION_GET_CONTENT);
		intent_restore.setType(type);
		activity.startActivityForResult(intent_restore, response_code);
	}
	public static void open_file(Fragment fragment, String type, int response_code) {
		Intent intent_restore = new Intent(Intent.ACTION_GET_CONTENT);
		intent_restore.setType(type);
		fragment.startActivityForResult(intent_restore, response_code);
	}
}
