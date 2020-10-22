package at.jodlidev.metawear.study;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

public class Element_Vibration_checkbox extends LinearLayout {
	private CheckBox vibration_checkbox;
	private EditText strength_editText;
	private EditText ms_editText;
	
	public Element_Vibration_checkbox(Context context) {
		super(context);
		init(context, null);
	}
	public Element_Vibration_checkbox(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	public Element_Vibration_checkbox(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}
	private void init(Context _context, AttributeSet attrs) {
		//get layout
		inflate(_context, R.layout.element_vibration_checkbox, this);
		
		vibration_checkbox = findViewById(R.id.vib_checkbox);
		strength_editText = findViewById(R.id.vib_strength);
		ms_editText = findViewById(R.id.vib_ms);
		
		
		//load custom attributes
		TypedArray a = _context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.Element_Vibration_checkbox,
				0,
				0);
		
		try {
			vibration_checkbox.setChecked(a.getBoolean(R.styleable.Element_Vibration_checkbox_vibration_checked, false));
			strength_editText.setText(Integer.toString((int) a.getFloat(R.styleable.Element_Vibration_checkbox_vibration_strength, 50)));
			ms_editText.setText(Integer.toString(a.getInt(R.styleable.Element_Vibration_checkbox_vibration_ms, 1000)));
			if(!a.getBoolean(R.styleable.Element_Vibration_checkbox_vibration_enabled, true))
				setEnabled(false);
		}
		finally {
			a.recycle();
		}
	}
	
	public boolean isChecked() {
		return vibration_checkbox.isChecked();
	}
	public void setChecked(boolean b) {
		vibration_checkbox.setChecked(b);
	}
	
	
	public void setEnabled(boolean b) {
		vibration_checkbox.setEnabled(b);
		strength_editText.setEnabled(b);
		ms_editText.setEnabled(b);
	}
	
	public float get_strength() {
		return Float.parseFloat(strength_editText.getText().toString());
	}
	public void set_strength(float i) {
		strength_editText.setText(Integer.toString((int) i));
	}
	public short get_ms() {
		return Short.parseShort(ms_editText.getText().toString());
	}
	public void set_ms(short i) {
		ms_editText.setText(Short.toString(i));
	}
}
