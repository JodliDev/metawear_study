package at.jodlidev.metawear.study;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class Fragment_ConfigurationRoot extends FragmentBase {
	public static class OwnPagerAdapter extends FragmentPagerAdapter {
		private WeakReference<Resources> res_reference;
		private WeakReference<Board_logic> logic;
		
		OwnPagerAdapter(Board_logic logic, Resources res, FragmentManager fm) {
			super(fm);
			res_reference = new WeakReference<>(res);
			this.logic = new WeakReference<>(logic);
		}
		
		@Override
		public Fragment getItem(int i) {
			FragmentBase fragment;
			switch(i) {
				case SITE_BING:
					fragment = new Fragment_Bing();
					break;
				case SITE_SWITCH:
					fragment = new Fragment_Switch();
					break;
				case SITE_MACRO:
					fragment = new Fragment_BatteryOptions();
					break;
				default:
					fragment = new FragmentBase();
			}
			return fragment;
		}
		
		@Override
		public int getCount() {
			return 3;
		}
		
		@Override
		public CharSequence getPageTitle(int i) {
			Resources res = res_reference.get();
			if(res == null)
				return "ERROR";
			
			switch(i) {
				case SITE_BING:
					return res.getString(R.string.bing);
				case SITE_SWITCH:
					return res.getString(R.string.switch_feedback);
				case SITE_MACRO:
					return res.getString(R.string.battery_options);
				default:
					return "ERROR";
			}
		}
	}
	private final static int SITE_BING = 0;
	private final static int SITE_SWITCH = 1;
	private final static int SITE_MACRO = 2;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		init();
		return inflater.inflate(R.layout.fragment_configuration_root, container, false);
	}
	
	
	@Override
	public void onViewCreated(@NonNull View rootView, Bundle savedInstanceState) {
		ViewPager viewPager = rootView.findViewById(R.id.view_pager);
		
		viewPager.setAdapter(new OwnPagerAdapter(logic, getResources(), getChildFragmentManager()));
	}
	
	void update_ui() {
		Fragment_Switch f_switch = (Fragment_Switch) get_fragment(SITE_SWITCH);
		if(f_switch != null)
			f_switch.update_ui();
		Fragment_Bing f_bing = (Fragment_Bing) get_fragment(SITE_BING);
		if(f_bing != null)
			f_bing.update_ui();
		Fragment_BatteryOptions f_macro = (Fragment_BatteryOptions) get_fragment(SITE_MACRO);
		if(f_macro != null)
			f_macro.update_ui();
	}
	
	private Fragment get_fragment(int pos) {
		return getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + pos);
	}
}
