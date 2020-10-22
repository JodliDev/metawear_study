package at.jodlidev.metawear.study;

import android.app.Activity;
import android.util.Log;

import java.util.concurrent.TimeoutException;

/**
 * Created by JodliDev on 22.03.2019.
 */
public abstract class Progress_Info {
	private int nested_progress_count = 0;
	
	private boolean is_block_open = false;
	boolean error = false;
	
	Activity activity;
	
	private ContinueListener listener = null;
	
	interface ContinueListener {
		boolean end_block();
	}
	Progress_Info(Activity _activity) {
		activity = _activity;
	}
	
	void set_listener(ContinueListener _listener) {
		listener = _listener;
	}
	
	
	private String return_error(Exception exception) {
		error = true;
		String r;
		try {
			throw exception;
		}
		catch(TimeoutException e) {
			r = activity.getString(R.string.error_TimeoutException);
		}
		catch(Exception e) {
			r = exception.toString();
		}
//		activity.runOnUiThread(() -> {
//			log_error(r);
//		});
		exception.printStackTrace();
		
		return r;
	}
	
	
	public void error(Exception e) {
		error = true;
		activity.runOnUiThread(() -> {
			log_error(return_error(e));
		});
	}
	
	public void error(int res) {
		error = true;
		activity.runOnUiThread(() -> {
			log_error(activity.getString(res));
		});
	}
	
	public void error(int res, Exception e) {
		error = true;
		activity.runOnUiThread(() -> {
			log_error(activity.getString(res, return_error(e)));
		});
	}
	
	public abstract void log(String t);
	public abstract void log_error(String t);
	
	abstract void update_ui();
	abstract void show_loader(int res);
	abstract void update_loader(String s);
	abstract void end_loader();
	abstract void new_loader_block();
	abstract void end_loader_block();
	
	
	synchronized public void show_progress(int res) {
		Log.d("board", "show_progress: "+nested_progress_count);
		++nested_progress_count;
		
		activity.runOnUiThread(() -> {
			show_loader(res);
			if(!is_block_open && nested_progress_count == 1) {
				is_block_open = true;
				new_loader_block();
			}
		});
	}
	public void update_progress(String s) {
		activity.runOnUiThread(() -> {
			update_loader(s);
		});
	}
	synchronized public void end_progress() {
		Log.d("board", "end_progress: "+nested_progress_count);
		--nested_progress_count;
		
		if(nested_progress_count <= 0) {
			Log.d("board", "closing block");
			nested_progress_count = 0;
			
			if(listener == null || listener.end_block()) {
				error = false;
				is_block_open = false;
				listener = null;
			}
			
			activity.runOnUiThread(() -> {
				end_loader();
				if(listener == null || listener.end_block())
					end_loader_block();
			});
		}
		else
			Log.d("board", "block is still open: " + nested_progress_count);
	}
}
