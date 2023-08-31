package at.jodlidev.metawear.study;

import android.content.Context;

import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.module.Settings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by JodliDev on 18.03.2019.
 */
public class Download_formatter {
	private static final String DELIMITER = "; ";
	static final int SWITCH_NUM = 0;
	static final int SWITCH_LENGTH = 1;
	static final int ACC = 2;
	static final int PING_BATTERY = 3;
	static final int UNKNOWN = 4;
	static final int SWITCH_NUM_ACC_COMBINED = 5;
	static final int SWITCH_LENGTH_ACC_COMBINED = 6;
	
	static final String FILENAME_SWITCH_NUM = "btn_count";
	static final String FILENAME_SWITCH_LENGTH = "btn_length";
	static final String FILENAME_ACC = "acceleration";
	static final String FILENAME_PING_BATTERY = "ping_battery";
	static final String FILENAME_UNKNOWN = "unknown";
	static final String FILENAME_SWITCH_NUM_ACC_COMBINED = "btn_count_acceleration";
	static final String FILENAME_SWITCH_LENGTH_ACC_COMBINED = "btn_length_acceleration";
	
	static final String[] FILENAMES = {FILENAME_SWITCH_NUM, FILENAME_SWITCH_LENGTH, FILENAME_ACC, FILENAME_PING_BATTERY, FILENAME_UNKNOWN, FILENAME_SWITCH_NUM_ACC_COMBINED, FILENAME_SWITCH_LENGTH_ACC_COMBINED};
	
	private Context context;
	private int[] count = new int[FILENAMES.length];
	
	private Data last_pressed_data = null;
	private boolean switch_acc_combined = false; //controls if data should be written into the cache
	private List<List<Data>> cache = new ArrayList<>();
	
	Download_formatter(Context context) {
		this.context = context.getApplicationContext();
	}
	
	void enable_switch_acc_combination() {
		switch_acc_combined = true;
		cache.add(SWITCH_NUM, new ArrayList<>());
		cache.add(SWITCH_LENGTH, new ArrayList<>());
		cache.add(ACC, new ArrayList<>());
	}
	String export_switch_acc_combination(String mac, int type) {
		StringBuilder sb = new StringBuilder();
		
		
		List<Data> cache_switch_num = cache.get(SWITCH_NUM);
		List<Data> cache_switch_length = cache.get(SWITCH_LENGTH);
		List<Data> cache_acc = cache.get(ACC);
		
		//There were some cases where data was not ordered (I think when the board lost power). It would break a lot so to be sure:
		Comparator<Data> comparator = (Data o1, Data o2) -> (int) (o1.timestamp().getTimeInMillis() - o2.timestamp().getTimeInMillis());
		Collections.sort(cache_switch_num, comparator);
		Collections.sort(cache_switch_length, comparator);
		Collections.sort(cache_acc, comparator);
		
		switch_acc_combined = false; //the cache is already filled so lets disable it now because we will reuse the format-functions inside the loop
		
		switch(type) {
			case SWITCH_NUM_ACC_COMBINED:
				if(cache_switch_num.isEmpty())
					return null;
				else {
					for(int i_switch = 0, max_switch = cache_switch_num.size(), i_acc = 0, max_acc = cache_acc.size(); i_switch < max_switch; ++i_switch) {
						Data data_switch = cache_switch_num.get(i_switch);
						
						//switch-timestamp will be lost because it is not saved before the timeout expired
						long timestamp = data_switch.timestamp().getTimeInMillis();
						
						long first_acc_timestamp = 0;
						float x_min = Integer.MAX_VALUE;
						float x_max = Integer.MIN_VALUE;
						float x_mean = 0;
						double x_std = 0;
						
						float y_min = Integer.MAX_VALUE;
						float y_max = Integer.MIN_VALUE;
						float y_mean = 0;
						double y_std = 0;
						
						float z_min = Integer.MAX_VALUE;
						float z_max = Integer.MIN_VALUE;
						float z_mean = 0;
						double z_std = 0;
						
						List<Acceleration> list_data_acc = new ArrayList<>();
						
						for(; i_acc < max_acc; ++i_acc) {
							Data data_acc = cache_acc.get(i_acc);
							if(data_acc.timestamp().getTimeInMillis() > timestamp)
								break;
							if(first_acc_timestamp == 0)
								first_acc_timestamp = data_acc.timestamp().getTimeInMillis();
							
							Acceleration acc = data_acc.value(Acceleration.class);
							
							
							list_data_acc.add(acc);
							
							if(acc.x() < x_min)
								x_min = acc.x();
							if(acc.y() < y_min)
								y_min = acc.y();
							if(acc.z() < z_min)
								z_min = acc.z();
							
							if(acc.x() > x_max)
								x_max = acc.x();
							if(acc.y() > y_max)
								y_max = acc.y();
							if(acc.z() > z_max)
								z_max = acc.z();
							
							x_mean += acc.x();
							y_mean += acc.y();
							z_mean += acc.z();
						}
						
						int acc_size = list_data_acc.size();
						
						if(acc_size > 0) {
							x_mean /= acc_size;
							y_mean /= acc_size;
							z_mean /= acc_size;
							
							for(Acceleration acc : list_data_acc) {
								x_std += Math.pow(acc.x() - x_mean, 2);
								y_std += Math.pow(acc.y() - y_mean, 2);
								z_std += Math.pow(acc.z() - z_mean, 2);
							}
							
							x_std = Math.sqrt(x_std / acc_size);
							y_std = Math.sqrt(y_std / acc_size);
							z_std = Math.sqrt(z_std / acc_size);
						}
						else {
							x_mean = 0;
							y_mean = 0;
							z_mean = 0;
							x_min = 0;
							y_min = 0;
							z_min = 0;
							x_max = 0;
							y_max = 0;
							z_max = 0;
						}
						sb.append(format_time(mac, data_switch));
						sb.append(DELIMITER);
						sb.append(format_switch_num_data(data_switch));
						sb.append(DELIMITER);
						sb.append(acc_size);
						sb.append(DELIMITER);
						sb.append(first_acc_timestamp);
						sb.append(DELIMITER);
						
						sb.append(x_mean);
						sb.append(DELIMITER);
						sb.append(x_std);
						sb.append(DELIMITER);
						sb.append(x_min);
						sb.append(DELIMITER);
						sb.append(x_max);
						sb.append(DELIMITER);
						sb.append(calc_angle(x_mean, y_mean, z_mean));
						sb.append(DELIMITER);
						
						sb.append(y_mean);
						sb.append(DELIMITER);
						sb.append(y_std);
						sb.append(DELIMITER);
						sb.append(y_min);
						sb.append(DELIMITER);
						sb.append(y_max);
						sb.append(DELIMITER);
						sb.append(calc_angle(y_mean, x_mean, z_mean));
						sb.append(DELIMITER);
						
						sb.append(z_mean);
						sb.append(DELIMITER);
						sb.append(z_std);
						sb.append(DELIMITER);
						sb.append(z_min);
						sb.append(DELIMITER);
						sb.append(z_max);
						sb.append(DELIMITER);
						sb.append(calc_angle(z_mean, x_mean, y_mean));
						
						sb.append("\n");
					}
				}
				break;
			case SWITCH_LENGTH_ACC_COMBINED:
				if(cache_switch_length.isEmpty())
					return null;
				else {
					boolean release_data_discovered = false;
					
					int i_acc = 0, max_acc = cache_acc.size();
					
					//Flow:
					//acc-data needs the timestamp of the next switch_pressed-data.
					//So we first start the line with switch-data
					//then search for the next switch_pressed-data
					//then add acc-data for the last line,
					//start the next line with the new switch-data and so forth
					
					for(int i_switch = 0, max_switch = cache_switch_length.size(); i_switch < max_switch; ++i_switch) {
						Data data_switch = cache_switch_length.get(i_switch);
						String formatted_release_data = format_switch_length_data(data_switch);
						if(formatted_release_data == null) //format_switch_length_data returns null if we provide it with press data (and caches it to last_pressed_data)
							continue;
						
						if(release_data_discovered) { //add acc-data for the next line
							if(i_acc < max_acc) {
								//Assumption:
								// Acc-timestamp is never greater than the next switch_pressed-timestamp
								long current_switch_timestamp = last_pressed_data.timestamp().getTimeInMillis();
								Data previous_data_acc = cache_acc.get(i_acc);
								long previous_acc_timestamp = previous_data_acc.timestamp().getTimeInMillis();
								
								if(previous_acc_timestamp > current_switch_timestamp)
									sb.append(format_acc_data(null));
								else {
									sb.append(format_acc_data(previous_data_acc));
									++i_acc;
								}
							}
							else
								sb.append(format_acc_data(null));
							
							sb.append("\n"); //complete the last line
							
//							release_data_discovered = false; //wait until the next formatted switch-data was added to sp
						}
						
						
						
						sb.append(format_time(mac, last_pressed_data)); //last_pressed_data will be set by format_switch_length_data
						sb.append(DELIMITER);
						sb.append(formatted_release_data);
						sb.append(DELIMITER);
						
						release_data_discovered = true;
					}
					
					if(release_data_discovered) {
						//completing the last line of the loop:
						if(i_acc >= max_acc)
							sb.append(format_acc_data(null));
						else
							sb.append(format_acc_data(cache_acc.get(i_acc)));
					}
					
					
					
					
					
					
//					for(int i_switch = 0, max_switch = cache_switch_length.size(), i_acc = 0, max_acc = cache_acc.size(); i_switch < max_switch; ++i_switch) {
//						Data data_switch = cache_switch_length.get(i_switch);
//						String format_data = format_switch_length_data(data_switch);
//						if(format_data == null) //format_switch_length_data returns null if we provide it with press data (and caches it to last_pressed_data)
//							continue;
//						//Beware: format_data is the entry for the release-data
//
//						sb.append(format_time(mac, last_pressed_data)); //last_pressed_data will be set by format_switch_length_data
//						sb.append(DELIMITER);
//						sb.append(format_data);
//						sb.append(DELIMITER);
//
//
//						if(i_acc >= max_acc) { //this should never happen!
//							sb.append(format_acc_data(null));
//							sb.append("\n");
//							continue;
//						}
//
//						//Assumption:
//						// Acc-timestamp will always be greater than switch-timestamp. But never be greater than the next switch-timestamp
//						long next_switch_timestamp = i_switch+1 < max_switch ? cache_switch_length.get(i_switch+1).timestamp().getTimeInMillis() : Long.MAX_VALUE;
//						Data data_acc = cache_acc.get(i_acc);
//						long acc_timestamp = data_acc.timestamp().getTimeInMillis();
//						if(acc_timestamp > next_switch_timestamp)
//							sb.append(format_acc_data(null));
//						else
//							sb.append(format_acc_data(data_acc));
//
//						sb.append("\n");
//						++i_acc;
//
////						long timestamp = data_switch.timestamp().getTimeInMillis();
////						for(; i_acc < max_acc; ++i_acc) { //there should be only one acc per switch - but we like to be safe, so we use a loop
////							Data data_acc = cache_acc.get(i_acc);
////							if(data_acc.timestamp().getTimeInMillis() >= timestamp)
////								break;
////
////							sb.append(format_acc_data(data_acc)); //this will break the table if there is more than one entry - which is good because then something went wrong and data should be checked manually anyways...
////						}
////						sb.append("\n");
//					}
				}
				break;
		}
		return sb.toString();
	}
	
	String format(String mac, Data data, int type) {
		
		String s;
		Data time_data = data;
		switch(type) {
			case SWITCH_NUM:
				s = format_switch_num_data(data);
				break;
			case SWITCH_LENGTH:
				s = format_switch_length_data(data);
				time_data = last_pressed_data;
			break;
			case ACC:
				s = format_acc_data(data);
			break;
			case PING_BATTERY:
				s = format_battery_data(data);
			break;
			default:
				s = data.toString();
		}
		if(s != null) {
			++count[type];
			return format_time(mac, time_data) + DELIMITER + s;
		}
		else
			return null;
		
	}
	String success_msg() {
		StringBuilder sb = new StringBuilder();
		for(int i=count.length-1; i>=0; --i) {
			if(count[i] == 0)
				continue;
			
			if(sb.length() != 0)
				sb.append(", ");
			sb.append(FILENAMES[i]);
			sb.append("(");
			sb.append(count[i]);
			sb.append(")");
		}
		return (sb.length() == 0) ? context.getString(R.string.info_download_success_empty) : context.getString(R.string.info_download_success, sb.toString());
	}
	
	String header_line(int type) {
		String default_start = "mac"+DELIMITER+"date_formatted"+DELIMITER+"time_formatted"+DELIMITER+"timestamp"+DELIMITER;
		
		switch(type) {
			case SWITCH_NUM:
				return default_start + "press_count";
			case SWITCH_LENGTH:
				return default_start + "press_ms";
			case ACC:
				return default_start + "x_axis"+DELIMITER+"y_axis"+DELIMITER+"z_axis"+DELIMITER+"x_angle"+DELIMITER+"y_angle"+DELIMITER+"z_angle";
			case PING_BATTERY:
				return default_start + "battery_percent"+DELIMITER+"battery_voltage";
			case SWITCH_NUM_ACC_COMBINED:
				return default_start
					   +"press_count"+DELIMITER
					   +"acc_data_count"+DELIMITER
					   +"first_acc_timestamp"+DELIMITER
					   +"x_mean"+DELIMITER
					   +"x_std"+DELIMITER
					   +"x_min"+DELIMITER
					   +"x_max"+DELIMITER
					   +"x_mean_angle"+DELIMITER
					   
					   +"y_mean"+DELIMITER
					   +"y_std"+DELIMITER
					   +"y_min"+DELIMITER
					   +"y_max"+DELIMITER
					   +"y_mean_angle"+DELIMITER
					   
					   +"z_mean"+DELIMITER
					   +"z_std"+DELIMITER
					   +"z_min"+DELIMITER
					   +"z_max"+DELIMITER
						+"z_mean_angle";
			case SWITCH_LENGTH_ACC_COMBINED:
				return default_start + "press_ms"+DELIMITER+"x_axis"+DELIMITER+"y_axis"+DELIMITER+"z_axis"+DELIMITER+"x_angle"+DELIMITER+"y_angle"+DELIMITER+"z_angle";
			default:
			case UNKNOWN:
				return default_start + "hex_array";
		}
	}
	
	private double calc_angle(float x, float y, float z) {
		return Math.atan(x / Math.sqrt(Math.pow(y,2) + Math.pow(z,2)) ) * 180 / Math.PI;
	}
	
	//*****
	//format-functions
	//*****
	private String format_time(String mac, Data data) {
		long timestamp = data.timestamp().getTimeInMillis();
		java.text.DateFormat date_format = android.text.format.DateFormat.getDateFormat(context);
		
		java.text.DateFormat time_format= java.text.DateFormat.getTimeInstance();
		
		return mac + DELIMITER + date_format.format(timestamp) + DELIMITER + time_format.format(timestamp) + DELIMITER + timestamp;
	}
	
	private String format_switch_num_data(Data data) {
		int state = data.value(Integer.class);//this needs to be first in case it fails and throws an exception
		if(switch_acc_combined)
			cache.get(SWITCH_NUM).add(data);
		
		return Integer.toString(state);
	}
	private String format_switch_length_data(Data data) {
		int state = data.value(Integer.class);//this needs to be first in case it fails and throws an exception
		if(switch_acc_combined)
			cache.get(SWITCH_LENGTH).add(data);
		
		if(state == 1) {
			last_pressed_data = data;
			return null; //lets wait for the button-released data and combine them next time
		}
		else {
			Calendar timestamp = data.timestamp();
			return Long.toString(timestamp.getTimeInMillis() - last_pressed_data.timestamp().getTimeInMillis());
		}
	}
	
	private String format_acc_data(Data data) {
		if(data == null)
			return DELIMITER+DELIMITER+DELIMITER+DELIMITER+DELIMITER+DELIMITER;
		Acceleration acc_data = data.value(Acceleration.class); //this needs to be first in case it fails and throws an exception
		
		if(switch_acc_combined)
			cache.get(ACC).add(data);
		
//		return acc_data.x() + DELIMITER + acc_data.y() + DELIMITER + acc_data.z();
		return acc_data.x() + DELIMITER + acc_data.y() + DELIMITER + acc_data.z() + DELIMITER + calc_angle(acc_data.x(), acc_data.y(), acc_data.z()) + DELIMITER + calc_angle(acc_data.y(), acc_data.x(), acc_data.z()) + DELIMITER + calc_angle(acc_data.z(), acc_data.x(), acc_data.y());
	}
	private String format_battery_data(Data data) {
		Settings.BatteryState batteryState = data.value(Settings.BatteryState.class);
		return batteryState.charge + DELIMITER + batteryState.voltage;
	}
	
}
