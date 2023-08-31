package at.jodlidev.metawear.study.data;

/**
 * Created by JodliDev on 26.03.2019.
 */
public abstract class DataBox {
	public static final int TYPE_BATTERY = 1;
	public static final int TYPE_REPEAT = 2;
	public static final int TYPE_RANDOM = 3;
	public static final int TYPE_SWITCH = 4;
	public static final int TYPE_PING = 5;
	public static final int TYPE_MACRO = 6;
	public static final int TYPE_CHARGE_FEEDBACK = 7;
	
	public abstract int get_type();
}
