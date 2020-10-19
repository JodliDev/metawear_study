package at.jodlidev.metawear.study;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by JodliDev on 07.11.17.
 */

public class Element_Time_input extends FrameLayout {
	private TextView timeInfo;
	
	public Calendar date;
	
	interface OnChangeListener {
		void onTimeChanged(Calendar date);
	}
	OnChangeListener listener = null;
	public Element_Time_input(Context _context) {
		super(_context);
		inflate(_context, R.layout.element_time_input, this);
	}
	public Element_Time_input(Context _context, AttributeSet attrs) {
		super(_context, attrs);
		inflate(_context, R.layout.element_time_input, this);
	}
	
	
	public void init(Activity activity) {
		init(activity, date == null ? Calendar.getInstance() : date);
	}
	public void init(final Activity activity, Calendar _date) {
		//*****
		//create Time
		timeInfo = findViewById(R.id.timeInfo);
		timeInfo.setOnClickListener((View v) -> {
			TimePickerDialog timeDialog = new TimePickerDialog(activity, (TimePicker timePicker, int hour, int min) -> {
				date.set(Calendar.HOUR_OF_DAY, hour);
				date.set(Calendar.MINUTE, min);
				show_time(date);
				if(listener != null)
					listener.onTimeChanged(date);
			}, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), DateFormat.is24HourFormat(getContext()));
			timeDialog.show();
		});
		
		set_calendar(_date);
	}
	
	public void set_listener(OnChangeListener listener) {
		this.listener = listener;
	}
	public void set_calendar(Calendar cal) {
		date = (Calendar) cal.clone();
		show_time(date);
	}
	
	public void show_time(Calendar cal) {
		timeInfo.setText(CONSTANTS.return_time(getContext(), cal));
	}
	
	
	@Override
	public Parcelable onSaveInstanceState() {
		OwnSavedState s = new OwnSavedState(super.onSaveInstanceState());
		if(date != null)
			s.state = date.getTimeInMillis();
		return s;
		
	}
	@Override
	public void onRestoreInstanceState(Parcelable state) {
		OwnSavedState s = (OwnSavedState) state;
		super.onRestoreInstanceState(s.getSuperState());
		
		date = Calendar.getInstance();
		if(s.state != 0)
			date.setTimeInMillis(s.state);
//		set_calendar(date);
	}
	
	
	static class OwnSavedState extends BaseSavedState {
		long state=0;
		
		OwnSavedState(Parcelable superState) {
			super(superState);
		}
		
		private OwnSavedState(Parcel in) {
			super(in);
			state = in.readLong();
		}
		
		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeLong(state);
		}
		
		public static final Creator<OwnSavedState> CREATOR = new Creator<OwnSavedState>() {
			public OwnSavedState createFromParcel(Parcel in) {
				return new OwnSavedState(in);
			}
			
			public OwnSavedState[] newArray(int size) {
				return new OwnSavedState[size];
			}
		};
	}
}
