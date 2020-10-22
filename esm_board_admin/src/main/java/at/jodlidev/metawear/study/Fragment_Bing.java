package at.jodlidev.metawear.study;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mbientlab.metawear.Route;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import at.jodlidev.metawear.study.data.Bing;
import at.jodlidev.metawear.study.data.RandomTimer;
import at.jodlidev.metawear.study.data.Repeat;
import bolts.Task;

/**
 * Created by JodliDev on 21.03.18.
 */

public class Fragment_Bing extends FragmentBase implements View.OnClickListener {
	
	private List<Bing> bing_dates = new ArrayList<>();
	
	private LinearLayout bing_collection;
	private View repeat_box;
	private FrameLayout repeat_disable_overlay;
	private View random_box;
	private FrameLayout random_disable_overlay;
	private Element_State_icons repeat_data;
	private Element_State_icons random_data;
	
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		init();
		return inflater.inflate(R.layout.fragment_bing, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View rootView, Bundle savedInstanceState) {
		bing_collection = rootView.findViewById(R.id.bing_list);
		repeat_box = rootView.findViewById(R.id.repeat_box);
		repeat_disable_overlay = rootView.findViewById(R.id.repeat_disable_overlay);
		random_box = rootView.findViewById(R.id.random_box);
		random_disable_overlay = rootView.findViewById(R.id.random_disable_overlay);
		repeat_data = rootView.findViewById(R.id.repeat_data);
		random_data = rootView.findViewById(R.id.random_data);
		
		repeat_box.setOnClickListener(this);
		random_box.setOnClickListener(this);
		rootView.findViewById(R.id.btn_add_bing).setOnClickListener(this);
		rootView.findViewById(R.id.btn_add_random_bing).setOnClickListener(this);
		
		
		update_ui();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.repeat_box:
				
				new AlertDialog.Builder(getActivity())
						.setTitle(R.string.delete)
						.setMessage(R.string.confirm_delete_repeat)
						.setIcon(R.drawable.ic_warning_black)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(R.string.delete, (DialogInterface dialog, int which) -> {
							logic.remove_repeat();
						}).show();
				break;
			case R.id.random_box:
				new AlertDialog.Builder(getActivity())
						.setTitle(R.string.delete)
						.setMessage(R.string.confirm_delete_random)
						.setIcon(R.drawable.ic_warning_black)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(R.string.delete, (DialogInterface dialog, int which) -> {
							logic.remove_random();
						}).show();
				break;
			case R.id.btn_add_bing:
				(new Dialog_newBing(false, getActivity(), logic)).show();
				break;
			case R.id.btn_add_random_bing:
				(new Dialog_newBing(true, getActivity(), logic)).show();
				break;
		}
	}
	
	
	void update_ui() {
		if(getActivity() == null)
			return;
		
		getActivity().runOnUiThread(() -> {
			bing_collection.removeAllViews();
			List<Bing> bings = logic.get_bings();
			if(bings != null) {
				for(Bing r : bings) {
					show_bing(r);
				}
			}
			
			
			Repeat repeat = logic.get_repeat();
			if(repeat == null) {
				repeat_box.setVisibility(View.GONE);
				repeat_disable_overlay.setVisibility(View.GONE);
			}
			else {
				repeat_box.setVisibility(View.VISIBLE);
				repeat_data.show_data(repeat.min, false, repeat.battery_logging, repeat.led, repeat.color, repeat.vibration);
				if(repeat.repeat_num != 0) //0 stands for infinity
					repeat_data.multiply_time_with(repeat.repeat_num);
				
				if(!repeat.exists_on_board)
					repeat_disable_overlay.setVisibility(View.VISIBLE);
				else
					repeat_disable_overlay.setVisibility(View.GONE);
			}
			
			
			RandomTimer randomTimer = logic.get_randomTimer();
			if(randomTimer == null) {
				random_box.setVisibility(View.GONE);
				random_disable_overlay.setVisibility(View.GONE);
			}
			else {
				random_box.setVisibility(View.VISIBLE);
				random_data.show_data(randomTimer.min, randomTimer.repeat, randomTimer.battery_logging, randomTimer.led, randomTimer.color, randomTimer.vibration);
				random_data.divide_time_by(randomTimer.timeframes_num);
				
				if(!randomTimer.exists_on_board)
					random_disable_overlay.setVisibility(View.VISIBLE);
				else
					random_disable_overlay.setVisibility(View.GONE);
			}
		});
	}
	
	//*****
	//Bing
	//*****
	void show_bing(Bing bing) {
		//TODO: This code is outdated! Use a RecyclerView instead!
		
		LayoutInflater inflater = LayoutInflater.from(getContext());
		FrameLayout view = (FrameLayout) inflater.inflate(R.layout.item_bing_list, null);
		bing.view = view;
		
		if(!bing.exists_on_board)
			view.setForeground(getResources().getDrawable(R.drawable.disabled_overlay));
		
		Element_State_icons state = view.findViewById(R.id.bing_data);
		
		int random_min = 0;
		if(bing.random) {
			RandomTimer randomTimer = logic.get_randomTimer();
			random_min = randomTimer.min;
		}
		state.show_data(bing.hour, bing.min, random_min, bing.repeat, bing.battery_logging, bing.led, bing.color, bing.vibration);
		
		TextView state_text = view.findViewById(R.id.state_text);
		ImageView state_icon = view.findViewById(R.id.state_icon);
		
		if(bing.id_wait == -1) {
			state_text.setText(R.string.state_started);
			state_text.setTextColor(getResources().getColor(R.color.green));
			state_icon.setImageResource(R.drawable.ic_play_black);
			state_icon.setColorFilter(getResources().getColor(R.color.green));
		}
		else {
			state_text.setText(R.string.state_waiting);
			state_text.setTextColor(getResources().getColor(R.color.orange));
			state_icon.setImageResource(R.drawable.ic_schedule_black);
			state_icon.setColorFilter(getResources().getColor(R.color.orange));
			
			if(bing.timer == null) {
				bing.timer = new Handler();
				bing.timer.postDelayed(() -> logic.remove_waitingTimer(bing), bing.countdown + 10000); //add 10 seconds just to be safe
			}
		}
		
		view.findViewById(R.id.btn_delete).setOnClickListener((View v) -> {
			logic.remove_bing(bing);
		});
		
		getActivity().runOnUiThread(() -> bing_collection.addView(view, 0));
		
		bing_dates.add(bing);
	}
	
	void unshow_bing(Bing bing) {
		bing.removeTimer();
		if(bing.view != null) {
			bing_collection.removeView(bing.view);
			bing.view = null;
		}
		bing_dates.remove(bing);
	}
	
	private void empty_bing_list() {
		for(int i = bing_dates.size() - 1; i >= 0; --i) { //it is possible that entries will get removed - so we need to iterate from the end
			unshow_bing(bing_dates.get(i));
		}
//		for(Bing r : bing_dates) {
//			unshow_bing(r);
//		}
	}
	
	void remove_waitingTimer(Bing bing) {
		TextView state_text = bing.view.findViewById(R.id.state_text);
		ImageView state_icon = bing.view.findViewById(R.id.state_icon);
		
		state_text.setText(R.string.state_started);
		state_text.setTextColor(getResources().getColor(R.color.green));
		state_icon.setImageResource(R.drawable.ic_play_black);
		state_icon.setColorFilter(getResources().getColor(R.color.green));
	}
	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		empty_bing_list();
		
		bing_collection = null;
		repeat_box = null;
		repeat_disable_overlay = null;
		random_box = null;
		random_disable_overlay = null;
		repeat_data = null;
		random_data = null;
	}
}
