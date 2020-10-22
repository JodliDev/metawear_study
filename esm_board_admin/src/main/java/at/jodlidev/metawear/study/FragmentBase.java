package at.jodlidev.metawear.study;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

/**
 * Created by david on 21.03.18.
 */

public class FragmentBase extends Fragment{
	public Board_logic logic;
	public Progress_Info progress;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	
	public void init() {
		logic = ((Communication) getActivity()).get_logic();
		progress = ((Communication) getActivity()).get_progressInfo();
	}
	
	public void log(String msg) {
		if(logic == null)
			progress.log(msg);
		else
			logic.log(msg);
	}
	public void error(Exception e) {
		progress.error(e);
	}
	public void error(int res, Exception e) {
		progress.error(res, e);
	}
//	public String return_error(Exception e) {
//		return progress.return_error(e);
//	}
	
	public void show_progress(int res) {
		progress.show_progress(res);
//		Communication f = (Communication) getActivity();
//		if(f != null)
//			f.show_progress(res);
	}
	public void update_progress(String s) {
		progress.update_progress(s);
//		Communication f = (Communication) getActivity();
//		if(f != null)
//			f.update_progress(s);
	}
	public void end_progress() {
		progress.end_progress();
//		Communication f = (Communication) getActivity();
//		if(f != null)
//			f.end_progress();
	}
}
