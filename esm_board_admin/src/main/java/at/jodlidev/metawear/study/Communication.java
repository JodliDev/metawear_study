package at.jodlidev.metawear.study;

import com.mbientlab.metawear.MetaWearBoard;

/**
 * Created by JodliDev on 25.03.18.
 */

public interface Communication {
	final static int SITE_CONFIGURATION = 0;
	final static int SITE_MAIN_SETUP = 1;
	
	Progress_Info get_progressInfo();
	Board_logic get_logic();
	
	
	void update_ui();
	
	void goto_site(int site);
}
