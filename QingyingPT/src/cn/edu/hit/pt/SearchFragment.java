package cn.edu.hit.pt;

import java.util.ArrayList;
import java.util.concurrent.FutureTask;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.widget.FlowLayout;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

@SuppressLint("InflateParams") public class SearchFragment extends Fragment{
	//private String TAG = "Search";
	private EditText etKey;
	private Button showLeft;
	private Button btnSearch;
	private LinearLayout rlHotSearch;
	private FlowLayout rlKeywords;
	private LinearLayout rlTabHeader;
	private ViewPager viewPager;
	
	private SearchTopicFragment searchTopicFragment;
	private SearchTorrentFragment searchTorrentFragment;
	private SearchUserFragment searchUserFragment;
	private HttpAsyncExecutor asyncExecutor;
	private MyBroadcastReceiver buttonRefreshBroadcastReceiver;
	private ArrayList<FutureTask<Response>> httpTasks = new ArrayList<FutureTask<Response>>();

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View mView = inflater.inflate(R.layout.searchfragment, null);
		etKey = (EditText)mView.findViewById(R.id.etKey);
		showLeft = (Button)mView.findViewById(R.id.showLeft);
		btnSearch = (Button)mView.findViewById(R.id.btnSearch);
		rlKeywords = (FlowLayout)mView.findViewById(R.id.rlKeywords);
		rlHotSearch = (LinearLayout)mView.findViewById(R.id.rlHotSearch);
		rlTabHeader = (LinearLayout)mView.findViewById(R.id.rlTabHeader);
		viewPager = (ViewPager)mView.findViewById(R.id.viewPager);
		
		return mView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		((MainActivity) getActivity()).mainFragmentPagerAdapter.restoreState(this);
		
		buttonRefreshBroadcastReceiver = new MyBroadcastReceiver();
		getActivity().registerReceiver(buttonRefreshBroadcastReceiver, new IntentFilter(MainActivity.ACTION_REFRESH_BUTTON));
		
		etKey.setHint(getString(R.string.input_keyword));

		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(getActivity()));
		loadHotSearch();
		
		btnSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				search();
			}
		});
		
		etKey.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View arg0, int keyCode, KeyEvent event) {
				if (KeyEvent.KEYCODE_ENTER == keyCode && event.getAction() == KeyEvent.ACTION_DOWN) {
					search();
					return true;
				}
				return false;
			}
		});
		
		etKey.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
			
			@Override
			public void afterTextChanged(Editable text) {
				if(text == null || text.length() == 0){
					rlHotSearch.setVisibility(View.VISIBLE);
					rlTabHeader.setVisibility(View.GONE);
					viewPager.setVisibility(View.GONE);
				}
			}
			
		});

		if(Params.unread_count != 0)
			showLeft.setBackgroundResource(R.drawable.button_menu_dark_new);
		else
			showLeft.setBackgroundResource(R.drawable.button_menu_dark);
		
		showLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).showLeft();
			}
		});
		
		initViewPager();
	}
	
	public void initViewPager() {
		ArrayList<Fragment> fragments = new ArrayList<Fragment>();
		searchTopicFragment = new SearchTopicFragment();
		searchTorrentFragment = new SearchTorrentFragment();
		searchUserFragment = new SearchUserFragment();
		fragments.add((Fragment)searchTopicFragment);
		fragments.add((Fragment)searchTorrentFragment);
		fragments.add((Fragment)searchUserFragment);
		viewPager.setAdapter(new myFragmentPagerAdapter(getChildFragmentManager(), fragments));
		viewPager.setOnPageChangeListener(new myOnPageChangerListener());
		viewPager.setOffscreenPageLimit(3);
		viewPager.setCurrentItem(0);
		for (int i = 0; i < rlTabHeader.getChildCount(); i++) {
			View tab = rlTabHeader.getChildAt(i);
			if(tab != null){
				tab.setTag(i);
				tab.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View tab) {
						int position = Integer.parseInt(tab.getTag().toString());
						viewPager.setCurrentItem(position);
					}
				});
			}
		}
	}
	
	public void loadHotSearch(){
		MyHttpRequest request = new MyHttpRequest(URLContainer.getHotSearchURL());
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {
			
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				try {
					@SuppressWarnings("unchecked")
					ArrayList<String> list = (ArrayList<String>)res.getObject(ArrayList.class);
					if(list != null){
						for(final String item : list){
							LinearLayout layout = (LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.hotsearch_row, null);
							TextView tv = (TextView) layout.findViewById(R.id.tv);
							tv.setText(item);
							layout.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View arg0) {
									etKey.setText(item);
									search();
								}
								
							});
							rlKeywords.addView(layout);
						}
						rlHotSearch.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
			}
			
		}));
	}
	
	public void search(){
		rlHotSearch.setVisibility(View.GONE);
		rlTabHeader.setVisibility(View.VISIBLE);
		viewPager.setVisibility(View.VISIBLE);
		String keyword = etKey.getText().toString();
		if(keyword == null || keyword.equals(""))
			return;
		searchTopicFragment.initSearch(keyword);
		searchTorrentFragment.initSearch(keyword);
		searchUserFragment.initSearch(keyword);
	}
	
	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(buttonRefreshBroadcastReceiver);
		super.onDestroy();
	}

	public class myFragmentPagerAdapter extends FragmentPagerAdapter{
		public ArrayList<Fragment> fragments;

		public myFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
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
	
	public class myOnPageChangerListener implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int arg0) {}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {}

		@Override
		public void onPageSelected(int position) {
			for (int i = 0; i < rlTabHeader.getChildCount(); i++) {
				View tab = rlTabHeader.getChildAt(i);
				if(tab != null){
					if(i == position)
						tab.setBackgroundResource(R.color.bg_color_light_grey);
					else
						tab.setBackgroundResource(R.drawable.list_item);
				}
			}
			if(position == 0)
				MainActivity.mSlidingMenu.setCanSliding(true, false);
			else
				MainActivity.mSlidingMenu.setCanSliding(false, false);
		}
		
	}
	
	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		if (this.getView() != null){
			if(menuVisible){
				if(viewPager.getCurrentItem() == 0)
					MainActivity.mSlidingMenu.setCanSliding(true, false);
				else
					MainActivity.mSlidingMenu.setCanSliding(false, false);
			}
			this.getView().setVisibility(menuVisible ? View.VISIBLE : View.GONE);
			if(Params.unread_count != 0)
				showLeft.setBackgroundResource(R.drawable.button_menu_dark_new);
			else
				showLeft.setBackgroundResource(R.drawable.button_menu_dark);
		}
	}
	
	public class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(MainActivity.ACTION_REFRESH_BUTTON)){
				if(showLeft != null){
					if(Params.unread_count != 0)
						showLeft.setBackgroundResource(R.drawable.button_menu_dark_new);
					else
						showLeft.setBackgroundResource(R.drawable.button_menu_dark);
				}
			}	
		}
	}
}
