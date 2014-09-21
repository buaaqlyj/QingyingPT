package cn.edu.hit.pt.widget;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import cn.edu.hit.pt.R;

public class SmiliesFragment extends Fragment{
	public View mView;
	public ViewPager viewPager;
	public EditText target;
	public ImageView btnSmilies1;
	public ImageView btnSmilies2;
	public ArrayList<SmiliesArray> list;
	public ArrayList<Fragment> fragments;
	
	public class SmiliesArray{
		public int[] IDArray;
		public int pixel;
		
		public SmiliesArray(int[] IDArray, int pixel){
			this.IDArray = IDArray;
			this.pixel = pixel;
		}
	}
	
	public void setTargetView(EditText target){
		this.target = target;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		list = new ArrayList<SmiliesArray>();
		list.add(new SmiliesArray(initiateID(192, 215), 36));
		list.add(new SmiliesArray(initiateID(216, 239), 36));
		list.add(new SmiliesArray(initiateID(240, 241), 36));
		list.add(new SmiliesArray(initiateID(1, 22, new int[]{24, 25}), 36));
		list.add(new SmiliesArray(initiateID(26, 49), 36));
		list.add(new SmiliesArray(initiateID(50, 60, initiateID(62, 71, new int[]{77, 88, 101})), 36));
		list.add(new SmiliesArray(initiateID(136, 144, initiateID(131, 134, new int[]{105,109,110,117,119,120,121,122,124,125,128})), 36));
		list.add(new SmiliesArray(initiateID(145, 149, initiateID(152, 162, new int[]{164,165,167,169,170,171,172,174})), 36));
		list.add(new SmiliesArray(new int[]{176,183,184,23,61,74,75,76,91,93,99,103,106,118,123,126,130,135,150,151}, 40));
		list.add(new SmiliesArray(new int[]{72,73,78,79,80,81,82,84,86,87,100,102,108,111,112,113,114,127,129,163}, 40));
		list.add(new SmiliesArray(new int[]{166,168,178,180,182,185,187,188,189,190,97,173,175,83,85,104,96,115,116,179}, 40));
		list.add(new SmiliesArray(new int[]{98,89,92,177,181,186,191,90,94,95,107}, 60));
		
		fragments = new ArrayList<Fragment>();
		for (SmiliesArray item : list) {
			SmiliesPage page = new SmiliesPage();
			page.setArgs(target, item.IDArray, item.pixel);
			fragments.add(page);
		}
		
		if(viewPager.getAdapter() == null)
			viewPager.setAdapter(new myFragmentPagerAdapter(getChildFragmentManager(), fragments));
		viewPager.setOnPageChangeListener(new myOnPageChangerListener());
		viewPager.setCurrentItem(0);
		
		btnSmilies1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(viewPager.getCurrentItem() >= 3){
					viewPager.setCurrentItem(0);
				}
			}
		});
		
		btnSmilies2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(viewPager.getCurrentItem() < 3){
					viewPager.setCurrentItem(3);
				}
			}
		});
	}

	public int[] initiateID(int start, int end){
		int[] IDArray = new int[end - start + 1];
		for(int i = 0; i < IDArray.length; i++){
			IDArray[i] = i + start;
		}
		return IDArray;
	}
	
	public int[] initiateID(int start, int end, int[] bond){
		int[] IDArray = new int[end - start + bond.length + 1];
		for(int i = 0; i < IDArray.length; i++){
			if(i < end - start + 1)
				IDArray[i] = i + start;
			else
				IDArray[i] = bond[i - end + start - 1];
		}
		return IDArray;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(mView != null){
			ViewGroup parent = (ViewGroup) mView.getParent();
			if(parent != null)
				parent.removeView(mView);
		}else{
			mView = inflater.inflate(R.layout.smilies_fragment, null);
			viewPager = (ViewPager)mView.findViewById(R.id.viewPager);
			btnSmilies1 = (ImageView)mView.findViewById(R.id.btnSmilies1);
			btnSmilies2 = (ImageView)mView.findViewById(R.id.btnSmilies2);
		}
		return mView;
	}
	
	public class myOnPageChangerListener implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int arg0) {}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {}

		@Override
		public void onPageSelected(int position) {
			if(position < 3){
				btnSmilies1.setBackgroundResource(R.color.bg_color_white);
				btnSmilies2.setBackgroundResource(R.color.bg_color_light_grey);
			}else{
				btnSmilies2.setBackgroundResource(R.color.bg_color_white);
				btnSmilies1.setBackgroundResource(R.color.bg_color_light_grey);
			}
		}
		
	}
	
	public class myFragmentPagerAdapter extends FragmentStatePagerAdapter{
		public ArrayList<Fragment> fragments;
		
		public myFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
			super(fm);
			this.fragments = fragments;
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}
		
	}

}
