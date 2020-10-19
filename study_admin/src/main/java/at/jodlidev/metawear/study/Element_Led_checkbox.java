package at.jodlidev.metawear.study;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

/**
 * Created by david on 03.04.18.
 */

public class Element_Led_checkbox extends LinearLayout {
	
	private CheckBox led_checkbox;
	private Spinner spinner_color;
	private ArrayAdapter<CharSequence> adapter;
	
	public Element_Led_checkbox(Context context) {
		super(context);
		init(context, null);
	}
	public Element_Led_checkbox(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	public Element_Led_checkbox(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}
	
	private void init(Context _context, AttributeSet attrs) {
		//get layout
		inflate(_context, R.layout.element_led_checkbox, this);
		
		led_checkbox = findViewById(R.id.item_led_checkbox);
		spinner_color = findViewById(R.id.spinner_color);
		
		adapter = ArrayAdapter.createFromResource(_context, R.array.led_colors, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_color.setAdapter(adapter);
		spinner_color.setSelection(0);
		
		
		//load custom attributes
		TypedArray a = _context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.Element_Led_checkbox,
				0,
				0);
		
		try {
			led_checkbox.setChecked(a.getBoolean(R.styleable.Element_Led_checkbox_led_checked, false));
			spinner_color.setSelection(a.getInt(R.styleable.Element_Led_checkbox_led_color, 0));
			String text = a.getString(R.styleable.Element_Led_checkbox_led_title);
			if(text == null)
				text = getContext().getString(R.string.enable_led);
			led_checkbox.setText(text);
			if(!a.getBoolean(R.styleable.Element_Led_checkbox_led_enabled, true))
				setEnabled(false);
		}
		finally {
			a.recycle();
		}
	}
	
	public String getColor() {
		return spinner_color.getSelectedItem().toString();
	}
	public boolean isChecked() {
		return led_checkbox.isChecked();
	}
	
	public void setChecked(boolean b) {
		led_checkbox.setChecked(b);
	}
	public void setColor(String color) {
		for(int i=0, max=adapter.getCount(); i<max; ++i) {
			if(adapter.getItem(i).toString().equals(color)) {
				spinner_color.setSelection(i);
				break;
			}
		}
	}
	
	public void setEnabled(boolean b) {
		led_checkbox.setEnabled(b);
		spinner_color.setEnabled(b);
	}
	
	public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
		led_checkbox.setOnCheckedChangeListener(listener);
	}
}
