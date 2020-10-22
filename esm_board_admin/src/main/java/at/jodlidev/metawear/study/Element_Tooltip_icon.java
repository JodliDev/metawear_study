package at.jodlidev.metawear.study;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.tooltip.Tooltip;

/**
 * Created by david on 12.07.18.
 */

public class Element_Tooltip_icon extends AppCompatImageView {
	private int tooltip_gravity = Gravity.BOTTOM;
	private String tooltip_msg = null;
	private int backgroundColor;
	
	public Element_Tooltip_icon(Context context) {
		super(context);
		init(context, null);
	}
	
	public Element_Tooltip_icon(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	
	public Element_Tooltip_icon(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
		
	}
	
	private void init(Context _context, AttributeSet attrs) {
		//load custom attributes
		TypedArray a = _context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.Element_Tooltip_icon,
				0,
				0);
		
		try {
			tooltip_msg = a.getString(R.styleable.Element_Tooltip_icon_tooltip_msg);
			tooltip_gravity = a.getInt(R.styleable.Element_Tooltip_icon_tooltip_gravity, 0) == 0 ? Gravity.BOTTOM : Gravity.TOP;
		}
		finally {
			a.recycle();
		}
		
		backgroundColor = _context.getResources().getColor(R.color.colorPrimaryDark);
		this.setImageDrawable(_context.getResources().getDrawable(R.drawable.ic_info_red));
		
		if(tooltip_msg != null)
			createTooltip();
	}
	
	
	public void createTooltip() {
		this.setOnClickListener((View v) -> {
			new Tooltip.Builder(this)
					.setText(tooltip_msg)
					.setCornerRadius(25f)
					.setGravity(tooltip_gravity)
					.setCancelable(true)
					.setDismissOnClick(true)
					.setTextColor(Color.WHITE)
					.setBackgroundColor(backgroundColor)
					.show();
		});
	}
}
