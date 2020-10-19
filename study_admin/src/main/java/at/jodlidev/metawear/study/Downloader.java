package at.jodlidev.metawear.study;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.module.Logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bolts.Task;

/**
 * Created by JodliDev on 12.11.2019.
 * This class is extremely ugly and hacky...
 * But it needs to be serializable without saving data (like the download locations or the formatter object) into state but creating them when they are actually needed.
 * Maybe I just dont know better, but in my opinion the API for RouteComponent.log() is really messy...
 */
public class Downloader {
	interface OnCompleteDownloadListener {
		void onComplete();
	}
	static Subscriber subscriber = (Data data, Object... env) -> {
		int identifier;
		try {
			identifier = (int) env[0];
		}
		catch(Exception e) {
			add_to_unknown(data, Download_formatter.UNKNOWN);
			return;
		}
		try {
			appendToFile(data, identifier);
		}
		catch(ClassCastException e) {
			add_to_unknown(data, identifier);
		}
	};
	
	private static Map<Integer, List<Data>> unknown_data;
	static File[] files;
	private static WeakReference<FragmentBase> fragment;
	private static Download_formatter formatter;
	private static FileOutputStream[] outputStream;
	private static String macAddress;
	private static String downloadFolder;
	private static int unknown_dialog_count;
	
	public static  int uploading_count = 0;
	
	static boolean init(FragmentBase _fragment, Download_formatter _formatter, Board_logic logic) {
		downloadFolder = _fragment.getString(R.string.download_folder);
		
		File file = new File(return_directory(), "test.txt");
		try {
			FileOutputStream test_output = new FileOutputStream(file, false);
			test_output.write(("test").getBytes());
			file.delete();
			
		}
		catch(Exception e) {
			return false;
		}
		
		fragment = new WeakReference<>(_fragment);
		macAddress = logic.board.getMacAddress().replace(':', '.');
		formatter = _formatter;
		outputStream = new FileOutputStream[Download_formatter.FILENAMES.length];
		files = new File[Download_formatter.FILENAMES.length];
		
		unknown_data = new HashMap<>();
		Arrays.fill(outputStream, null);
		Arrays.fill(files, null);
		
		
		return true;
	}
	
	static Subscriber create_subscriber(final int identifier) {
		return null;
//		return (Data data, Object... env) -> {
//			try {
//				appendToFile(data, identifier);
//			}
//			catch(ClassCastException e) {
//				add_to_unknown(data, identifier);
//			}
//		};
	}
	
	static Subscriber create_anon_subscriber(final int identifier) {
		return (Data data, Object... env) -> {
			try {
				appendToFile(data, identifier);
			}
			catch(ClassCastException e) {
				add_to_unknown(data, identifier);
			}
		};
	}
	
	static Task<Void> start_download(Logging loggingModule, Logging.LogDownloadUpdateHandler updateHandler) {
		return loggingModule.downloadAsync(10, updateHandler);
	}
	
	static boolean save_unknown(Activity activity, final OnCompleteDownloadListener onCompleteDownloadListener) {
		if(activity == null || unknown_data.isEmpty())
			return false;
		unknown_dialog_count = unknown_data.size();
		activity.runOnUiThread(() -> {
			
			for(Map.Entry<Integer, List<Data>> entry : unknown_data.entrySet()) {
				List<Data> list = entry.getValue();
				(new Dialog_choose_unknown_download_type(activity, Download_formatter.FILENAMES[entry.getKey()], new Dialog_choose_unknown_download_type.OnFinishListener() {
					@Override
					public void onFinish(int type) {
						for(Data data : list) {
							try {
								Downloader.appendToFile(data, type);
							}
							catch(ClassCastException e) {
								appendToFile(data, Download_formatter.UNKNOWN);
							}
						}
						if(--unknown_dialog_count == 0)
							onCompleteDownloadListener.onComplete();
					}
					
					@Override
					public void onCancel() {
						if(--unknown_dialog_count == 0)
							onCompleteDownloadListener.onComplete();
					}
				}, list.get(0))).show();
			}
		});
		
		return true;
	}
	
	static void upload_files(Activity activity) {
		uploading_count = 0;
		if(activity != null) {
			for(File f : Downloader.files) {
				if(f != null && f.exists()) {
					if(Service_Upload.fire(activity, Activity_SingleDevice.REQUEST_UPLOAD, f.getParentFile().getName(), f))
						++uploading_count;
				}
			}
			
		}
	}
	
	static void complete(boolean switch_acc_combined) {
		if(switch_acc_combined) {
			if(fragment != null && fragment.get() != null) {
				FragmentBase f = fragment.get();
				f.update_progress(f.getString(R.string.info_download_creating_switch_acc_combined));
			}
			
			String export = formatter.export_switch_acc_combination(macAddress, Download_formatter.SWITCH_NUM_ACC_COMBINED);
			if(export != null)
				writeToFile(export, Download_formatter.SWITCH_NUM_ACC_COMBINED);
			
			export = formatter.export_switch_acc_combination(macAddress, Download_formatter.SWITCH_LENGTH_ACC_COMBINED);
			if(export != null)
				writeToFile(export, Download_formatter.SWITCH_LENGTH_ACC_COMBINED);
		}
	}
	static void close() {
		try {
			for(FileOutputStream s: outputStream) {
				if(s != null) {
					s.flush();
					s.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		unknown_data = null;
		fragment = null;
		formatter = null;
		outputStream = null;
		files = null;
		macAddress = null;
		downloadFolder = null;
	}
	
	
	
	private static String return_filename(int f_index, int i) {
		String end = i==0 ? ".csv" : "_"+i+".csv";
		return Download_formatter.FILENAMES[f_index] + "_" + macAddress + end;
	}
	private static File return_directory() {
		File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), downloadFolder);
		folder.mkdir();
		
		return folder;
	}
	
	private static boolean createFile(int f_index) {
		try {
			File folder = return_directory();
			folder = new File(folder, Download_formatter.FILENAMES[f_index]);
			folder.mkdir();
			String filename;
			File file;
			int i = 0;
			do {
				file = new File(folder, filename = return_filename(f_index, i));
				++i;
			} while(file.exists());
			if(fragment.get() != null) {
				FragmentBase f = fragment.get();
				f.log(f.getString(R.string.creating_file_format, filename, downloadFolder));
			}
			outputStream[f_index] = new FileOutputStream(file, true);
			files[f_index] = file;
			writeToFile(formatter.header_line(f_index), f_index);
		}
		catch (IOException e) {
			if(fragment.get() != null) {
				FragmentBase f = fragment.get();
				f.error(new Exception(f.getString(R.string.error_create_file)));
			}
			return false;
		}
		
		
		return true;
	}
	
	private static void appendToFile(Data data, int i) {
		String data_formatted = formatter.format(macAddress, data, i);
		if(data_formatted == null) //no data for output this time. Formatter needs to do some magic
			return;
		Log.d("log", "(" + Integer.toString(i) + ")" + data_formatted);
		
		writeToFile(data_formatted, i);
	}
	private static void writeToFile(String s, int i) {
		if(outputStream[i] == null)
			createFile(i);
		
		try {
			outputStream[i].write((s + "\n").getBytes());
		}
		catch (IOException e) {
//			log(return_error(new Exception(getString(R.string.error_save_data, s))));
			
			if(fragment.get() != null) {
				FragmentBase f = fragment.get();
				f.error(new Exception(f.getString(R.string.error_save_data, s)));
			}
		}
	}
	
	
	private static void add_to_unknown(Data data, int identifier) {
		if(!unknown_data.containsKey(identifier)) {
			if(fragment.get() != null) {
				FragmentBase f = fragment.get();
				f.log(f.getString(R.string.error_download_unknown));
			}
			unknown_data.put(identifier, new ArrayList<>());
		}
		
		List<Data> list = unknown_data.get(identifier);
		list.add(data);
	}
}
