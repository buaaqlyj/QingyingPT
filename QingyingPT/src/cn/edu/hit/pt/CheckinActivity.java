package cn.edu.hit.pt;

import java.util.ArrayList;
import java.util.concurrent.FutureTask;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import org.json.JSONArray;
import org.json.JSONException;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.http.UserAvatarTask;
import cn.edu.hit.pt.impl.DatabaseUtil;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.model.Checkin;
import cn.edu.hit.pt.model.CheckinList;
import cn.edu.hit.pt.widget.CustomToast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

public class CheckinActivity extends SwipeBackActivity {
	//private String TAG = "Checkin";
	private LinearLayout mcontainer;
	private Button btnCheckin;
	private TextView tvInfor;
	private LinearLayout rlInfor;
	private RelativeLayout loading;
	private HttpAsyncExecutor asyncExecutor;
	private ArrayList<FutureTask<Response>> httpTasks = new ArrayList<FutureTask<Response>>();
	private ArrayList<UserAvatarTask> avatarTasks = new ArrayList<UserAvatarTask>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkin);
		
		SwipeBackLayout mSwipeBackLayout;
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
		
		mcontainer = (LinearLayout)findViewById(R.id.mcontainer);
		btnCheckin = (Button)findViewById(R.id.btnCheckin);
		tvInfor = (TextView)findViewById(R.id.tvInfor);
		rlInfor = (LinearLayout)findViewById(R.id.rlInfor);

		Checkin checkin = DatabaseUtil.userDatabase(CheckinActivity.this).queryById(1, Checkin.class);
		if(checkin == null) checkin = new Checkin();
		if(Util.get_today().equals(checkin.lastCheckedin)){
			tvInfor.setText(getString(R.string.already_checkin) + checkin.totalDays + getString(R.string.day) + getString(R.string.comma) + getString(R.string.text_get) + checkin.bonusAdded + getString(R.string.text_get_mana_point));
			btnCheckin.setVisibility(View.GONE);
			rlInfor.setVisibility(View.VISIBLE);
		}
		
		//Check Check-in state
		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(this));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getCheckin(""));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, com.litesuits.http.data.NameValuePair[] headers) {
				Checkin checkin = res.getObject(Checkin.class);
				if(checkin.result != 0){
					tvInfor.setText(getString(R.string.already_checkin) + checkin.totalDays + getString(R.string.day) + getString(R.string.comma) + getString(R.string.text_get) + checkin.bonusAdded + getString(R.string.text_get_mana_point));
					btnCheckin.setVisibility(View.GONE);
					rlInfor.setVisibility(View.VISIBLE);
				}else{
					btnCheckin.setVisibility(View.VISIBLE);
					rlInfor.setVisibility(View.GONE);
				}
				DatabaseUtil.userDatabase(CheckinActivity.this).save(checkin);
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(CheckinActivity.this).handleException(e);
			}

		}));

		ArrayList<CheckinList> checkinList = DatabaseUtil.userDatabase(CheckinActivity.this).queryAll(CheckinList.class);
		if(checkinList == null || checkinList.size() < 10){
			loadCheckinList();
		}else{
			loadCheckinList(checkinList);
		}
		
		btnCheckin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setCheckin();
				loadCheckinList();
			}
		});
	}
	
	private void setCheckin(){
		MyHttpRequest request = new MyHttpRequest(URLContainer.getCheckin("checkin"));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {
	
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				Checkin checkin = res.getObject(Checkin.class);
				if(checkin == null) checkin = new Checkin();
				if(checkin.redate == 1){
					new CustomToast(CheckinActivity.this, CustomToast.TYPE_INFORMATION).setTitle(getString(R.string.checkin_disturbed))
					.setText(getString(R.string.checkin_please_buy_card)).create().show();
				}else{
					tvInfor.setText(getString(R.string.already_checkin) + checkin.totalDays + getString(R.string.day) + getString(R.string.comma) + getString(R.string.text_get) + checkin.bonusAdded + getString(R.string.text_get_mana_point));
					btnCheckin.setVisibility(View.GONE);
					rlInfor.setVisibility(View.VISIBLE);
				}
				DatabaseUtil.userDatabase(CheckinActivity.this).save(checkin);
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(CheckinActivity.this).handleException(e);
			}
	
		}));
	}
	
	private void loadCheckinList(){
		loading = (RelativeLayout) getLayoutInflater().inflate(R.layout.refresh_view, null);
		ImageView iv = (ImageView)loading.findViewById(R.id.iv);
		iv.setImageResource(R.drawable.loading);
		AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
		animationLoading.start();
		mcontainer.addView(loading, 0);
		MyHttpRequest request = new MyHttpRequest(URLContainer.getCheckin("list"));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				try {
					ArrayList<CheckinList> checkinList = new ArrayList<CheckinList>();
					JSONArray jsonArray = new JSONArray(res.getString());
					for (int i = 0; i < jsonArray.length(); i++) {
						Gson gson = new GsonBuilder().create();
						CheckinList item = gson.fromJson(jsonArray.get(i).toString(), CheckinList.class);
						checkinList.add(item);
					}
					loadCheckinList(checkinList);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(CheckinActivity.this).handleException(e);
			}
	
		}));
	}
	
	private void loadCheckinList(ArrayList<CheckinList> list) {
		mcontainer.removeAllViews();
		if(list == null || list.size() == 0) return;
		DatabaseUtil.userDatabase(CheckinActivity.this).deleteAll(CheckinList.class);
		for(CheckinList item : list){
		    final LayoutInflater inflater =  LayoutInflater.from(CheckinActivity.this);
			final LinearLayout rlList = (LinearLayout)inflater.inflate(R.layout.checkin_row, null);
			ImageView ivUserAvatar = (ImageView)rlList.findViewById(R.id.ivUserAvatar);
			TextView tvUname = (TextView)rlList.findViewById(R.id.tvUname);
			TextView tvTime = (TextView)rlList.findViewById(R.id.tvTime);
			avatarTasks.add(new UserAvatarTask(this, item.id, ivUserAvatar, true));
			tvUname.setText(item.name);
			tvTime.setText(item.lastCheckedin);
		    mcontainer.addView(rlList);
			DatabaseUtil.userDatabase(CheckinActivity.this).save(item);
		}
	}

	@Override
	public void finish() {
		for (FutureTask<Response> httpTask : httpTasks) {
			httpTask.cancel(true);
		}
		for(UserAvatarTask avatarTask : avatarTasks){
			avatarTask.cancel();
		}
		super.finish();
	}
}
