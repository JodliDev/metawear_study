package at.jodlidev.metawear.study;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by JodliDev on 14.03.2019.
 */
public class LogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private final static int TYPE_ITEM = 0;
	private final static int TYPE_LIMITER = 1;
	private List<Data> data = new ArrayList<>();
	private RecyclerView recyclerView;
	
	private boolean block_is_open = false;
	
	static class Data {
		String time;
		String text;
		boolean old;
		boolean error;
		
		Data(String _time, String _text, boolean is_error) {
			time = _time;
			text = _text;
			old = false;
			error = is_error;
		}
	}
	
	static class ViewHolder_item extends RecyclerView.ViewHolder {
		TextView timeView;
		TextView textView;
		
		ViewHolder_item(ViewGroup v) {
			super(v);
			timeView = v.findViewById(R.id.time_textview);
			textView = v.findViewById(R.id.text1);
		}
	}
	static class ViewHolder_limiter extends RecyclerView.ViewHolder {
		ViewHolder_limiter(View v) {
			super(v);
		}
	}
	
	LogAdapter(RecyclerView _recyclerView) {
		recyclerView = _recyclerView;
	}
	
	@Override
	@NonNull
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		switch(viewType) {
			case TYPE_ITEM:
				return new ViewHolder_item((ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_text, parent, false));
			case TYPE_LIMITER:
				return new ViewHolder_limiter(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_limiter, parent, false));
		}
		
		ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_text, parent, false);
		return new ViewHolder_item(v);
	}
	
	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if(holder.getItemViewType() == TYPE_ITEM) {
			ViewHolder_item holder_i = (ViewHolder_item) holder;
			Data current_data = data.get(position);
			
			holder_i.timeView.setText(current_data.time);
			holder_i.textView.setText(current_data.text);
			Resources res = recyclerView.getContext().getResources();
			
			if(current_data.error)
				holder_i.textView.setTextColor(res.getColor(android.R.color.holo_red_dark));
			else if(current_data.old)
				holder_i.textView.setTextColor(res.getColor(android.R.color.darker_gray));
			else
				holder_i.textView.setTextColor(res.getColor(android.R.color.black));
		}
	}
	
	@Override
	public int getItemViewType(int position) {
		return data.get(position) == null ? TYPE_LIMITER : TYPE_ITEM;
	}
	
	@Override
	public int getItemCount() {
		return data.size();
	}
	
	void new_block(boolean add_limiter) {
		block_is_open = true;
		for(int i=data.size()-1; i>=0; --i) {
			Data current_data = data.get(i);
			if(current_data == null) //this is a limiter, others are already gray so no reason to continue
				break;
			else {
				current_data.old = true;
			}
			
//			RecyclerView.ViewHolder holder = recyclerView.findViewHolderForLayoutPosition(i);
//			if(holder != null) {
//				if(holder.getItemViewType() == TYPE_ITEM) {
//					data.get(i).old = true;
////					TextView textView = ((ViewHolder_item) holder).textView;
////					textView.setTextColor(textView.getContext().getResources().getColor(android.R.color.darker_gray));
//				}
//				else
//					break; //others are already gray so noe reason to continue
//			}
		}
		if(add_limiter) {
			if(data.size() == 0 || data.get(data.size()-1) != null)
			data.add(null);
		}
		notifyDataSetChanged();
	}
	void end_block() {
		block_is_open = false;
	}
	void add(String s, boolean is_error) {
		add(new Data(CONSTANTS.return_time(recyclerView.getContext(), Calendar.getInstance()), s, is_error));
	}
	void add(String s, long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		
		add(new Data(CONSTANTS.return_time(recyclerView.getContext(), cal), s, false));
	}
	
	private void add(Data d) {
		if(!block_is_open) {
			new_block(true);
			end_block();
		}
		
		data.add(d);
		int new_size = data.size()-1;
		notifyItemInserted(new_size);
		recyclerView.scrollToPosition(new_size);
	}
	
	void clear() {
		data = new ArrayList<>();
		notifyDataSetChanged();
	}
}