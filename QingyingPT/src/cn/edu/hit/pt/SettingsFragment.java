package cn.edu.hit.pt;

import java.util.concurrent.FutureTask;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.impl.CacheCleaner;
import cn.edu.hit.pt.impl.DatabaseUtil;
import cn.edu.hit.pt.impl.DirectoryUtil;
import cn.edu.hit.pt.impl.PreferenceUtil;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.model.PollMessage;
import cn.edu.hit.pt.widget.CustomDialog;
import cn.edu.hit.pt.widget.CustomToast;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

@SuppressLint("InflateParams") public class SettingsFragment extends Fragment{
	private Button showLeft;
	private Button logout;
	private LinearLayout rlUserSettings;
	private LinearLayout rlReceiveOfflineMessage;
	private LinearLayout rlNoPic;
	private LinearLayout rlCache;
	private LinearLayout rlCheckUpdate;
	private LinearLayout rlAbout;
	private TextView tvCache;
	private TextView tvUpdateCount;
	private ToggleButton tbNoPic;
	private ToggleButton tbReceiveOfflineMessage;

	private MyBroadcastReceiver buttonRefreshBroadcastReceiver;
	private MyBroadcastReceiver updateRefreshBroadcastReceiver;
	private FutureTask<Response> httpTask;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View mView = inflater.inflate(R.layout.settingsfragment, null);
		showLeft = (Button) mView.findViewById(R.id.showLeft);
		logout = (Button) mView.findViewById(R.id.logout);
		rlUserSettings = (LinearLayout)mView.findViewById(R.id.rlUserSettings);
		rlCache = (LinearLayout) mView.findViewById(R.id.rlCache);
		rlReceiveOfflineMessage = (LinearLayout)mView.findViewById(R.id.rlReceiveOfflineMessage);
		rlNoPic = (LinearLayout) mView.findViewById(R.id.rlNoPic);
		rlCheckUpdate = (LinearLayout) mView.findViewById(R.id.rlCheckUpdate);
		tvCache = (TextView) mView.findViewById(R.id.tvCache);
		tvUpdateCount = (TextView) mView.findViewById(R.id.tvUpdateCount);
		tbReceiveOfflineMessage = (ToggleButton) mView.findViewById(R.id.tbReceiveOfflineMessage);
		tbNoPic = (ToggleButton) mView.findViewById(R.id.tbNoPic);
		rlAbout = (LinearLayout)mView.findViewById(R.id.rlAbout);
		
		return mView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((MainActivity) getActivity()).mainFragmentPagerAdapter.restoreState(this);

		buttonRefreshBroadcastReceiver = new MyBroadcastReceiver();
		updateRefreshBroadcastReceiver = new MyBroadcastReceiver();
		getActivity().registerReceiver(buttonRefreshBroadcastReceiver, new IntentFilter(MainActivity.ACTION_REFRESH_BUTTON));
		getActivity().registerReceiver(updateRefreshBroadcastReceiver, new IntentFilter(MainActivity.ACTION_REFRESH_UPDATE));

		Util.setShowLeftButton(showLeft);
		showLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).showLeft();
			}
		});
		
		if(Params.message!= null && Params.message.version > Params.version)
			tvUpdateCount.setVisibility(View.VISIBLE);
		
		logout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				PreferenceUtil preferenceUtil = new PreferenceUtil(getActivity());
				preferenceUtil.savePreference("cookie", "");
				preferenceUtil.savePreference("CURUSER_class", 0);
				preferenceUtil.savePreference("CURUSER_class_name", "");
				startActivity(new Intent(getActivity(), LoginActivity.class));
				getActivity().stopService(new Intent(getActivity(), PollService.class));
				getActivity().finish();
			}
		});
		
		rlUserSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getActivity(), ProfileSettings.class);
				startActivity(intent);
			}
		});
		
		rlCache.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Dialog dialog = null;
				CustomDialog.Builder customBuilder = new CustomDialog.Builder(getActivity());
	            customBuilder.setTitle(R.string.clear_cache).setMessage(R.string.clear_cache_query)
                .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
    					new CustomToast(getActivity(), CustomToast.TYPE_INFORMATION).setTitle(getString(R.string.please_wait))
    					.setText(getString(R.string.removing_cache)).create().show();
                    	CacheCleaner cleanCache = new CacheCleaner(getActivity());
                    	cleanCache.execute(tvCache);
						dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
                    }
                });
	            dialog = customBuilder.create();
	            dialog.show();
			}
		});
		
		rlCheckUpdate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tvUpdateCount.setVisibility(View.GONE);
				new CustomToast(getActivity(), CustomToast.TYPE_INFORMATION).setTitle(getString(R.string.checking))
				.setText(getString(R.string.checking_new_version)).create().show();
				queryUpdate();
			}
		});

		tbReceiveOfflineMessage.setChecked(Params.userSettings.receive_offline_message);

		rlReceiveOfflineMessage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(tbReceiveOfflineMessage.isChecked())
					tbReceiveOfflineMessage.setChecked(false);
				else
					tbReceiveOfflineMessage.setChecked(true);
			}
		});

		tbNoPic.setChecked(Params.userSettings.nopic);

		rlNoPic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(tbNoPic.isChecked())
					tbNoPic.setChecked(false);
				else
					tbNoPic.setChecked(true);
			}
		});
		
		tbReceiveOfflineMessage.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					Params.userSettings.receive_offline_message = true;
					DatabaseUtil.userDatabase(getActivity()).save(Params.userSettings);
				}else{
					Params.userSettings.receive_offline_message = false;
					DatabaseUtil.userDatabase(getActivity()).save(Params.userSettings);
				}
			}
		});
		
		tbNoPic.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					Params.userSettings.nopic = true;
					DatabaseUtil.userDatabase(getActivity()).save(Params.userSettings);
				}else{
					Params.userSettings.nopic = false;
					DatabaseUtil.userDatabase(getActivity()).save(Params.userSettings);
				}
			}
		});
		
		rlAbout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getActivity(), About.class);
				startActivity(intent);
			}
		});
		
		setCacheSize();
	}
	
	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		if (this.getView() != null){
			this.getView().setVisibility(menuVisible ? View.VISIBLE : View.GONE);
			if(menuVisible){
				setCacheSize();
			}
		}
	}
	
	public void setCacheSize() {
		if(tvCache != null){
			String cache_size = DirectoryUtil.getCacheSize();
			if(cache_size.equals(""))
				tvCache.setText("");
			else
				tvCache.setText("("+ cache_size +")");
		}
	}
	
	public void queryUpdate(){
		HttpAsyncExecutor asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(getActivity()));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getPollURL());
		httpTask = asyncExecutor.execute(request, new HttpResponseHandler() {
			
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				final PollMessage message = res.getObject(PollMessage.class);
				if(message == null){
					new CustomToast(getActivity(), CustomToast.TYPE_WARNING).setTitle(getString(R.string.query_update_failed))
					.setText(getString(R.string.unknown_error));
				}else{
					Params.message = message;
					Params.if_update = 1;
					Util.showUpdateDialog(getActivity(), true);
				}
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(getActivity()).handleException(e);
			}
			
		});
	}

	@Override
	public void onDestroy() {
		if(httpTask != null) httpTask.cancel(true);
		getActivity().unregisterReceiver(buttonRefreshBroadcastReceiver);
		getActivity().unregisterReceiver(updateRefreshBroadcastReceiver);
		super.onDestroy();
	}
	
	public class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(MainActivity.ACTION_REFRESH_BUTTON)){
				if(showLeft != null){
					if(Params.unread_count != 0)
						showLeft.setBackgroundResource(R.drawable.button_menu_new);
					else
						showLeft.setBackgroundResource(R.drawable.button_menu);
				}
			}else if(intent.getAction().equals(MainActivity.ACTION_REFRESH_UPDATE)){
				if(Params.message!= null && Params.message.version > Params.version)
					tvUpdateCount.setVisibility(View.VISIBLE);
			}
		}
	}
	
}