package cn.edu.hit.pt;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.impl.DatabaseUtil;
import cn.edu.hit.pt.impl.DirectoryUtil;
import cn.edu.hit.pt.impl.PreferenceUtil;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.model.SystemSettings;
import cn.edu.hit.pt.model.Tips;
import cn.edu.hit.pt.model.User;
import cn.edu.hit.pt.model.UserSettings;
import cn.edu.hit.pt.widget.CustomToast;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

@SuppressLint("InflateParams")
public class MainActivity extends FragmentActivity{
//	private static final String TAG = "MainActivity";
	public static SlidingMenu mSlidingMenu;
	
	public final static String ACTION_REFRESH_COUNTER = "REFRESH_COUNTER";
	public final static String ACTION_REFRESH_BUTTON = "REFRESH_BUTTON";
	public final static String ACTION_REFRESH_UPDATE = "REFRESH_UPDATE";
	
	private FrameLayout mContainer;
	private ArrayList<Fragment> fragments;
	private MyBroadcastReceiver myBroadcastReceiver;
	public MainFragmentPagerAdapter mainFragmentPagerAdapter;
	
	public boolean logoutUser(int error) {
		switch (error) {
		case 1:
			new CustomToast(this, CustomToast.TYPE_WARNING).setTitle(getString(R.string.user_login_failed))
			.setText(getString(R.string.user_login_expire)).create().show();
			break;
		case 2:
			new CustomToast(this, CustomToast.TYPE_WARNING).setTitle(getString(R.string.user_login_failed))
			.setText(getString(R.string.user_parked)).create().show();
			break;
		case 3:
			new CustomToast(this, CustomToast.TYPE_WARNING).setTitle(getString(R.string.user_login_failed))
			.setText(getString(R.string.user_disabled)).create().show();
			break;
		default:
			return true;
		}
		PreferenceUtil preferenceUtil = new PreferenceUtil(this);
		preferenceUtil.savePreference("cookie", "");
		preferenceUtil.savePreference("CURUSER_class", 0);
		preferenceUtil.savePreference("CURUSER_class_name", "");
		stopService(new Intent(this, PollService.class));
		startActivity(new Intent(this, LoginActivity.class));
		finish();
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Params.userSettings = DatabaseUtil.userDatabase(this).queryById(1, UserSettings.class);
		Params.tips =  DatabaseUtil.userDatabase(this).queryById(1, Tips.class);
		if(Params.userSettings == null) Params.userSettings = new UserSettings();
		if(Params.tips == null) Params.tips = new Tips();
		
		if(DirectoryUtil.checkdir(this)==false){
			Params.userSettings.nopic = true;
		}

		if(Params.CURUSER != null && Params.CURUSER.id != 0)
			Params.systemSettings = DatabaseUtil.systemDatabase(this).queryById(1, SystemSettings.class);
		startService(new Intent(this, PollService.class));

		MyHttpRequest request = new MyHttpRequest(URLContainer.getUserInforUrl(0, true));
		HttpAsyncExecutor asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(this));
		asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				User user = res.getObject(User.class);
				if(user == null) return;
				if(logoutUser(user.error) == false) return;
				if((user.id != 0)&&(user.uclass > 0)){
					Params.CURUSER = user;
					DatabaseUtil.userDatabase(MainActivity.this).save(user);
					PreferenceUtil preferenceUtil = new PreferenceUtil(MainActivity.this);
					preferenceUtil.saveKeyPreference();
				}else{
					if(logoutUser(1) == false) return;
				}
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(MainActivity.this).handleException(e);
			}
		});
		
		mSlidingMenu = (SlidingMenu)findViewById(R.id.slidingMenu);
		mSlidingMenu.setLeftView(getLayoutInflater().inflate(R.layout.left_frame, null));
		mSlidingMenu.setCenterView(getLayoutInflater().inflate(R.layout.center_frame, null));

		mContainer = (FrameLayout)findViewById(R.id.center_frame);
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		t.replace(R.id.left_frame, new LeftMenu());
		t.commit();
		
		fragments = new ArrayList<Fragment>();
		fragments.add(new ForumFragment());
		fragments.add(new TorrentFragment());
		fragments.add(new SearchFragment());
		fragments.add(new AppsFragment());
		fragments.add(new MailFragment());
		fragments.add(new SettingsFragment());
		mainFragmentPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(), mContainer, fragments);
		mainFragmentPagerAdapter.setCurrentItem(1);
		
		myBroadcastReceiver = new MyBroadcastReceiver();
		registerReceiver(myBroadcastReceiver, new IntentFilter(MainActivity.ACTION_REFRESH_UPDATE));
	}

	@Override
	protected void onResume() {
		sendBroadcast(new Intent(PollService.TIMER_RAPID));
		super.onResume();
	}

	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(mSlidingMenu.SlidingState() == false)
				showLeft();
			else{
	    		moveTaskToBack(true);
	    		if(Params.userSettings == null || !Params.userSettings.receive_offline_message){
	    			sendBroadcast(new Intent(PollService.SYSTEM_EXIT));
	    		}
			}
	    }
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			showLeft();
		}
        return true;
    }

	public void showLeft() {
		MainActivity.mSlidingMenu.setCanSliding(true, false);
		mSlidingMenu.showLeftView();
	}

	@Override
	protected void onStop() {
		sendBroadcast(new Intent(PollService.TIMER_SLOW));
		super.onStop();
	}

	@Override
	public void onLowMemory() {
		finish();
		super.onLowMemory();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(myBroadcastReceiver);
		sendBroadcast(new Intent(PollService.SYSTEM_EXIT));
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	public class MainFragmentPagerAdapter extends FragmentPagerAdapter{
		public ViewGroup container;
		public ArrayList<Fragment> fragments;
		public Fragment mCurrentPrimaryItem;
		
		public int position;
		
		public MainFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public MainFragmentPagerAdapter(FragmentManager fm, ViewGroup container, ArrayList<Fragment> fragments){
			super(fm);
			this.container = container;
			this.fragments = fragments;
		}
		
		public void setCurrentItem(int position){
			setPrimaryItem(container, 0, instantiateItem(mContainer, position));
			finishUpdate(container);
		}
		
		public int getCurrentItem() {
			return position;
		}
		
		public void restoreState(Fragment fragment) {
			mCurrentPrimaryItem = fragment;
		}
		
		@Override
	    public void setPrimaryItem(ViewGroup container, int position, Object object) {
	        Fragment fragment = (Fragment)object;
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            mCurrentPrimaryItem = fragment;
            this.position = position;
	    }

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position - 1);
		}

		@Override
		public int getCount() {
			return 6;
		}

	}

	public class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(MainActivity.ACTION_REFRESH_UPDATE)){
				Util.showUpdateDialog(MainActivity.this, false);
			}
		}
	}

}
