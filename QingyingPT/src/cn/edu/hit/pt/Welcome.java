package cn.edu.hit.pt;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class Welcome extends FragmentActivity {
	WelcomePage p1 = WelcomePage.newInstance(1);
	WelcomePage p2 = WelcomePage.newInstance(2);
	WelcomePage p3 = WelcomePage.newInstance(3);
	WelcomePage p4 = WelcomePage.newInstance(4);
	private ViewPager mPager;
	private MyAdapter mAdapter;
	private ArrayList<Fragment> pagerItemList = new ArrayList<Fragment>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		mPager = (ViewPager)findViewById(R.id.pager);
		pagerItemList.add(p1);
		pagerItemList.add(p2);
		pagerItemList.add(p3);
		pagerItemList.add(p4);
		mAdapter = new MyAdapter(getSupportFragmentManager());
		mPager.setAdapter(mAdapter);
	}
	
	@Override
	public void finish() {
		startActivity(new Intent(this, LoginActivity.class));
		super.finish();
	}

	public class MyAdapter extends FragmentPagerAdapter {
		public MyAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public int getCount() {
			return pagerItemList.size();
		}

		@Override
		public Fragment getItem(int position) {

			Fragment fragment = null;
			fragment = pagerItemList.get(position);

			return fragment;

		}
	}
}