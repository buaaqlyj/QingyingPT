package cn.edu.hit.pt;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.edu.hit.pt.MainActivity.MainFragmentPagerAdapter;
import cn.edu.hit.pt.http.UserAvatarTask;

@SuppressLint("InflateParams") public class LeftMenu extends Fragment {
	private LinearLayout rlCurrentUser;
	private LinearLayout btn_forum;
	private LinearLayout btn_torrent;
	private LinearLayout btn_search;
	private LinearLayout btn_apps;
	private LinearLayout btn_mail;
	private LinearLayout btn_settings;
	private TextView tvSettingsCount;
	private TextView tvMessageCount;
	private ImageView ivAvatar;
	private TextView tvName;
	private TextView tvClass;

	private MainFragmentPagerAdapter mFragmentPagerAdapter;
	private MyBroadcastReceiver counterRefreshBroadcastReceiver;
	
	@Override
	public void onResume() {
		new UserAvatarTask(getActivity(), Params.CURUSER.id, ivAvatar, true);
		tvClass.setText(Params.CURUSER.ucname);
		tvName.setText(Params.CURUSER.name);
		super.onResume();
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.left, null);
		rlCurrentUser = (LinearLayout)view.findViewById(R.id.rlCurrentUser);
		ivAvatar=(ImageView)view.findViewById(R.id.ivAvatar);
		tvName = (TextView)view.findViewById(R.id.tvName);
		tvClass = (TextView)view.findViewById(R.id.tvClass);
		btn_torrent=(LinearLayout)view.findViewById(R.id.btn_torrent);
		btn_forum=(LinearLayout)view.findViewById(R.id.btn_forum);
		btn_search=(LinearLayout)view.findViewById(R.id.btn_search);
		btn_apps=(LinearLayout)view.findViewById(R.id.btn_apps);
		btn_mail=(LinearLayout)view.findViewById(R.id.btn_mail);
		btn_settings=(LinearLayout)view.findViewById(R.id.btn_settings);
		tvSettingsCount = (TextView)view.findViewById(R.id.tvSettingsCount);
		tvMessageCount = (TextView)view.findViewById(R.id.tvMessageCount);
		return view;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mFragmentPagerAdapter = ((MainActivity)getActivity()).mainFragmentPagerAdapter;
		
		rlCurrentUser.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getActivity(), UserInformation.class);
				getActivity().startActivity(intent);
			}
		});
		
		btn_settings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).showLeft();
				mFragmentPagerAdapter.setCurrentItem(6);
			}
		});

		btn_mail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).showLeft();
				mFragmentPagerAdapter.setCurrentItem(5);
			}
		});
		
		btn_apps.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				((MainActivity) getActivity()).showLeft();
				mFragmentPagerAdapter.setCurrentItem(4);
			}
		});

		btn_search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).showLeft();
				mFragmentPagerAdapter.setCurrentItem(3);
			}
		});

		btn_torrent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).showLeft();
				mFragmentPagerAdapter.setCurrentItem(2);
			}
		});

		btn_forum.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).showLeft();
				mFragmentPagerAdapter.setCurrentItem(1);
			}
		});
		counterRefreshBroadcastReceiver = new MyBroadcastReceiver();
		getActivity().registerReceiver(counterRefreshBroadcastReceiver, new IntentFilter(MainActivity.ACTION_REFRESH_COUNTER));
	}
	
	@Override
	public void onDestroy() {
		try {
			getActivity().unregisterReceiver(counterRefreshBroadcastReceiver);
		} catch (Exception e) {
			
		}
		super.onDestroy();
	}

	public class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(MainActivity.ACTION_REFRESH_COUNTER)){
				if(Params.message != null){
					if(Params.message.version > Params.version){
						tvSettingsCount.setText("1");
						tvSettingsCount.setVisibility(View.VISIBLE);
					}else
						tvSettingsCount.setVisibility(View.GONE);
				}
				
				if(Params.unread_count > 0){
					tvMessageCount.setText(String.valueOf(Params.unread_count));
					tvMessageCount.setVisibility(View.VISIBLE);
				}else {
					tvMessageCount.setVisibility(View.GONE);
				}
			}	
		}
	}
}
