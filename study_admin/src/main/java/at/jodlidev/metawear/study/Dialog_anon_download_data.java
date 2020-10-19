package at.jodlidev.metawear.study;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * Created by JodliDev on 24.03.18.
 */

public class Dialog_anon_download_data extends Dialog_download_data implements View.OnClickListener {
	private List<Integer> found_routes;
	
	Dialog_anon_download_data(Activity _activity, Board_logic _logic, Dialog_download_data.OnFinishListener _listener, List<Integer> _found_routes) {
		super(_activity, _logic, _listener);
		found_routes = _found_routes;
	}
	
	@Override
	int get_contentView() {
		return R.layout.dialog_anon_download_data;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TextView found_routes_text = findViewById(R.id.found_routes_text);
		
		if(found_routes.size() == 0) {
			found_routes_text.setText(R.string.none);
			findViewById(R.id.btn_ok).setEnabled(false);
		}
		else {
			
			findViewById(R.id.btn_ok).setEnabled(true);
			found_routes_text.setText("");
			int i = 0;
			boolean has_accRoute = false;
			boolean has_switchRoute = false;
			for(int route : found_routes) {
				found_routes_text.append((++i)+".) "+Download_formatter.FILENAMES[route] + "\n");
				switch(route) {
					case Download_formatter.ACC:
						has_accRoute = true;
						break;
					case Download_formatter.SWITCH_NUM:
					case Download_formatter.SWITCH_LENGTH:
						has_switchRoute = true;
				}
			}
			if(has_accRoute && has_switchRoute) {
				switch_acc_combined.setVisibility(View.VISIBLE);
				switch_acc_combined.setChecked(true);
			}
		}
	}
}
