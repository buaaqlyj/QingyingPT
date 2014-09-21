package cn.edu.hit.pt;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.widget.RemoteViews;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.impl.ApplicationInfo;
import cn.edu.hit.pt.impl.DatabaseUtil;
import cn.edu.hit.pt.impl.PreferenceUtil;
import cn.edu.hit.pt.model.PollMessage;
import cn.edu.hit.pt.model.SystemSettings;
import cn.edu.hit.pt.model.Tips;
import cn.edu.hit.pt.model.UserSettings;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

public class PollService extends Service{
	//private static final String TAG = "PollService";
	public final static String TIMER_RAPID = "RAPID";
	public final static String TIMER_SLOW = "SLOW";
	public final static String SYSTEM_EXIT = "EXIT";
	public static int PERIOD_RAPID = 30000;
	public static int PERIOD_SLOW = 60000;
	
	private boolean networkState = true;
	private boolean canNotify = false;
	
	private Timer mTimer;
	private TimerTask task;
	private Notification notif;
	private NotificationManager manager;
	private InternetListener internetListener;
	private ExitBroadcastReceiver exitReceiver;
	private TimerBroadcastReceiver slowTimerReceiver;
	private TimerBroadcastReceiver rapidTimerReceiver;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_STICKY;
		resetParams();
		notif = new Notification();
		notif.contentView = new RemoteViews(getPackageName(), R.layout.notify_message);
		manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

		mTimer = new Timer(true);
		task = new TimerTask(){
			
			@Override
			public void run() {
				poll();
				if(canNotify == true){
					PreferenceUtil preferenceUtil = new PreferenceUtil(PollService.this);
					preferenceUtil.loadKeyPreference();
				}
			}
		};
		mTimer.schedule(task, 0, PERIOD_RAPID);

		registerReceiver(exitReceiver = new ExitBroadcastReceiver(), new IntentFilter(SYSTEM_EXIT));
		registerReceiver(internetListener = new InternetListener(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		registerReceiver(rapidTimerReceiver = new TimerBroadcastReceiver(), new IntentFilter(TIMER_RAPID));
		registerReceiver(slowTimerReceiver = new TimerBroadcastReceiver(), new IntentFilter(TIMER_SLOW));
		
		return super.onStartCommand(intent, flags, startId);
	}

	public void poll() {
		HttpAsyncExecutor asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(this));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getPollURL());
		asyncExecutor.execute(request, new HttpResponseHandler() {
			
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				PollMessage message = res.getObject(PollMessage.class);
				if(message == null) return;
				sendBroadcast(new Intent(MainActivity.ACTION_REFRESH_COUNTER));
				sendBroadcast(new Intent(MainActivity.ACTION_REFRESH_BUTTON));
				sendBroadcast(new Intent(MainActivity.ACTION_REFRESH_UPDATE));
				if(Params.unread_count != message.unread){
					if(canNotify && Params.unread_count < message.unread ){
						notif.icon = R.drawable.logo_notification;
						notif.contentView.setImageViewResource(R.id.content_view_image, R.drawable.ic_message);
						notif.contentView.setTextViewText(R.id.content_view_title, getString(R.string.app_name));
						notif.contentView.setTextViewText(R.id.content_view_text, getString(R.string.you_have) + message.unread + getString(R.string.new_unread_message));
						notif.flags = Notification.FLAG_NO_CLEAR|Notification.FLAG_AUTO_CANCEL;
						Intent intent = new Intent(PollService.this, LauncherActivity.class);  
						PendingIntent pendingIntent = PendingIntent.getActivity(PollService.this, 0, intent, 0); 
						notif.contentIntent = pendingIntent;
						manager.notify(Params.MESSAGE_NOTIFICATION_ID, notif);
					}
					Params.unread_count = message.unread;
					Params.refresh_mail = true;
				}
				Params.message = message;
			
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {}
			
		});
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(exitReceiver);
		unregisterReceiver(internetListener);
		unregisterReceiver(slowTimerReceiver);
		unregisterReceiver(rapidTimerReceiver);
		super.onDestroy();
	}

	public void resetParams() {
		try {
			PreferenceUtil preferenceUtil = new PreferenceUtil(this);
			preferenceUtil.loadKeyPreference();
			ApplicationInfo info = new ApplicationInfo(this);
			Params.version = info.getVersionCode();
			Params.systemSettings = DatabaseUtil.systemDatabase(this).queryById(1, SystemSettings.class);
			Params.userSettings = DatabaseUtil.userDatabase(this).queryById(1, UserSettings.class);
			Params.tips =  DatabaseUtil.userDatabase(this).queryById(1, Tips.class);
			if(Params.userSettings == null) Params.userSettings = new UserSettings();
			if(Params.tips == null) Params.tips = new Tips();
		} catch (Exception e) {
			System.exit(0);
		}
		
	}

	private class TimerBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			mTimer.cancel();
			task.cancel();
			mTimer = new Timer(true);
			task = new TimerTask(){
				
				@Override
				public void run() {
					if(networkState == true)
						poll();
				}
			};
			if(intent.getAction().equals(TIMER_RAPID)){
				mTimer.schedule(task, 0, PERIOD_RAPID);
				canNotify = false;
			}else if(intent.getAction().equals(TIMER_SLOW)){
				mTimer.schedule(task, 0, PERIOD_SLOW);
				canNotify = true;
			}
		}
		
	}
	
	public class ExitBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(SYSTEM_EXIT)){
				System.exit(0);
			}
		}
		
	}
	
	private class InternetListener extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			//网络状态已经改变
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo info = connectivityManager.getActiveNetworkInfo();  
				if(info != null && info.isAvailable()) {
					//String name = info.getTypeName();
					networkState = true;
				} else {
					networkState = false;
				}
			}
		}
	};
}
