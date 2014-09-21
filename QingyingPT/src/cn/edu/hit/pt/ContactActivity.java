package cn.edu.hit.pt;

import java.util.ArrayList;
import java.util.concurrent.FutureTask;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.http.UserAvatarTask;
import cn.edu.hit.pt.impl.DatabaseUtil;
import cn.edu.hit.pt.impl.TagFormatter;
import cn.edu.hit.pt.model.Contact;
import cn.edu.hit.pt.model.User;
import cn.edu.hit.pt.widget.CustomScrollView;
import cn.edu.hit.pt.widget.CustomScrollView.OnScrollListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

public class ContactActivity extends SwipeBackActivity implements OnScrollListener{
	//private String TAG = "Contacts";
	public final static int ACTION_SELECT = 1;
	private LinearLayout mcontainer;
	private CustomScrollView svRefresh;
	private Button btnReturn;
	private Button btnSearch;
	private EditText etUsername;
	private LinearLayout rlSelection;
	private RelativeLayout footer_view = null;
	private RelativeLayout nomore_view = null;
	
	private int action;
	private long last_userid;
	private String searchName = "";
	private FutureTask<Response> mainTask;
	private ArrayList<UserAvatarTask> avatarTasks = new ArrayList<UserAvatarTask>();

	@Override
	protected void onResume() {
		if(Params.refresh_friends == true){
			getContactList();
		}
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);
		
		SwipeBackLayout mSwipeBackLayout;
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
		
		Intent intent = getIntent();
		action = intent.getIntExtra("action", 0);
		
		mcontainer = (LinearLayout)findViewById(R.id.container);
		svRefresh = (CustomScrollView)findViewById(R.id.svFriendslist);
		etUsername = (EditText)findViewById(R.id.etUsername);
		btnSearch = (Button)findViewById(R.id.btnSearch);
		btnReturn = (Button)findViewById(R.id.btnReturn);
		rlSelection = (LinearLayout)findViewById(R.id.rlSelection);
		
		btnSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				startSearch();
			}
		});
		
		btnReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		etUsername.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
				if(keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN){
					startSearch();
					return true;
				}
				return false;
			}
		});
		
		ArrayList<Contact> contacts = DatabaseUtil.userDatabase(ContactActivity.this).queryAll(Contact.class);
		setContactList(contacts);
		
		svRefresh.setOnScrollListener(this);
	}
	
	public void startSearch(){
		last_userid = 0;
		searchName = etUsername.getText().toString();
		if(!searchName.equals("")){
			mcontainer.removeAllViews();
			getSearchList();
		}
	}
	
	public void getContactList() {
		footer_view = (RelativeLayout)getLayoutInflater().inflate(R.layout.refresh_view, null);
		ImageView iv = (ImageView)footer_view.findViewById(R.id.iv);
		iv.setImageResource(R.drawable.loading);
		AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
		animationLoading.start();
		mcontainer.addView(footer_view, 0);
		
		HttpAsyncExecutor asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(this));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getContactsURL());
		mainTask = asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				try {
					ArrayList<Contact> contacts = new ArrayList<Contact>();
					JSONArray jsonArray = new JSONArray(res.getString());
					mcontainer.removeAllViews();
					for (int i = 0; i < jsonArray.length(); i++) {
						Gson gson = new GsonBuilder().create();
						Contact contact = gson.fromJson(jsonArray.get(i).toString(), Contact.class);
						contacts.add(contact);
					}
					setContactList(contacts);
					DatabaseUtil.userDatabase(ContactActivity.this).deleteAll(Contact.class);
					DatabaseUtil.userDatabase(ContactActivity.this).save(contacts);
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				Params.refresh_friends = false;
				if(footer_view != null)
					mcontainer.removeView(footer_view);
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(ContactActivity.this).handleException(e);
				footer_view = null;
				mcontainer.removeAllViews();
				LayoutInflater inflater =  LayoutInflater.from(ContactActivity.this);
				LinearLayout rlList = (LinearLayout)inflater.inflate(R.layout.no_friend_tip, null);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(svRefresh.getWidth(), svRefresh.getHeight()-70);
				rlList.setLayoutParams(lp);
				mcontainer.addView(rlList);
			}
			
		});
	}
	
	public void getSearchList(){
		if(searchName.equals("")) return;
		if(last_userid == 0)
			mcontainer.removeAllViews();

		footer_view = (RelativeLayout)getLayoutInflater().inflate(R.layout.refresh_view, null);
		ImageView iv = (ImageView)footer_view.findViewById(R.id.iv);
		iv.setImageResource(R.drawable.loading);
		AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
		animationLoading.start();
		mcontainer.addView(footer_view);
		
		HttpAsyncExecutor asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(this));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getSearchUsersURL(searchName, last_userid));
		mainTask = asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				try {
					ArrayList<Contact> contacts = new ArrayList<Contact>();
					JSONArray jsonArray = new JSONArray(res.getString());
					for (int i = 0; i < jsonArray.length(); i++) {
						Gson gson = new GsonBuilder().create();
						Contact contact = gson.fromJson(jsonArray.get(i).toString(), Contact.class);
						contacts.add(contact);
						if(contact != null) last_userid = contact.id;
					}
					setContactList(contacts);
				} catch (JSONException e) {
					if(mcontainer.getChildCount() == 0 || last_userid == 0){
						mcontainer.removeAllViews();
						LayoutInflater inflater =  LayoutInflater.from(ContactActivity.this);
						LinearLayout rlList = (LinearLayout)inflater.inflate(R.layout.no_search_tip, null);
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(svRefresh.getWidth(), svRefresh.getHeight()-70);
						rlList.setLayoutParams(lp);
						mcontainer.addView(rlList);
					}else{
						nomore_view = (RelativeLayout)getLayoutInflater().inflate(R.layout.footer_nomore_row, null);
						mcontainer.addView(nomore_view);
					}
				}
				Params.refresh_friends = false;
				if(footer_view != null){
					mcontainer.removeView(footer_view);
					footer_view = null;
				}
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(ContactActivity.this).handleException(e);
			}
			
		});
	}

	public void setContactList(ArrayList<Contact> list){
		if(list == null || list.size() == 0){
			LayoutInflater inflater =  LayoutInflater.from(ContactActivity.this);
			LinearLayout rlList = (LinearLayout)inflater.inflate(R.layout.no_friend_tip, null);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(svRefresh.getWidth(), svRefresh.getHeight()-70);
			rlList.setLayoutParams(lp);
			mcontainer.addView(rlList);
			return;
		}
		for(final User user : list){
		    final LayoutInflater inflater =  LayoutInflater.from(ContactActivity.this);
			final LinearLayout rlList = (LinearLayout)inflater.inflate(R.layout.contact_row, null);
			TextView tvUsername = (TextView)rlList.findViewById(R.id.tvUsername);
			TextView tvTitle = (TextView)rlList.findViewById(R.id.tvTitle);
			LinearLayout arrow = (LinearLayout)rlList.findViewById(R.id.arrow);
			final ImageView ivUserAvatar = (ImageView)rlList.findViewById(R.id.ivUserAvatar);
			if(user.name.equals("")){
				tvUsername.setText(getString(R.string.invalid_user));
				tvUsername.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
			}
			else
				tvUsername.setText(user.name);
			tvTitle.setText(user.title);
		    
			if(action == ACTION_SELECT){
				arrow.setVisibility(View.GONE);
			    rlList.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						rlList.setVisibility(View.GONE);
						if(rlSelection.getVisibility() == View.GONE)
							rlSelection.setVisibility(View.VISIBLE);
						final ImageView iv = new ImageView(ContactActivity.this);
						iv.setLayoutParams(new LinearLayout.LayoutParams(
								getResources().getDimensionPixelSize(R.dimen.avatar_with_padding),
								getResources().getDimensionPixelSize(R.dimen.avatar_with_padding))
						);
						iv.setPadding(
								getResources().getDimensionPixelSize(R.dimen.padding_medium), 
								getResources().getDimensionPixelSize(R.dimen.padding_medium), 
								0, 
								getResources().getDimensionPixelSize(R.dimen.padding_medium)
						);
						iv.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								if(rlList != null)
									rlList.setVisibility(View.VISIBLE);
								rlSelection.removeView(iv);
								if(rlSelection.getChildCount() == 0){
									rlSelection.setVisibility(View.GONE);
								}
							}
						});
						iv.setTag(user.name);
						rlSelection.addView(iv);
					    avatarTasks.add(new UserAvatarTask(ContactActivity.this, user.id, iv, false));
					}
				});
			}else{
			    rlList.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent();
						intent.putExtra("sender_name", user.name);
						intent.putExtra("sender", user.id);
						intent.setClass(ContactActivity.this, ViewMail.class);
						startActivity(intent);
						finish();
					}
				});
				
			}

		    avatarTasks.add(new UserAvatarTask(this, user.id, ivUserAvatar, true));
			
		    mcontainer.addView(rlList);
		}
		if(searchName.equals("")){
			nomore_view = (RelativeLayout)getLayoutInflater().inflate(R.layout.footer_nomore_row, null);
			mcontainer.addView(nomore_view);
		}else{
			if(list.size() < 20){
				nomore_view = (RelativeLayout)getLayoutInflater().inflate(R.layout.footer_nomore_row, null);
				mcontainer.addView(nomore_view);
			}else{
				nomore_view = null;
			}
		}
	}

	@Override
	public void finish() {
		if(mainTask != null)
			mainTask.cancel(true);
		for(UserAvatarTask avatarTask : avatarTasks){
			avatarTask.cancel();
		}
		if(action == ACTION_SELECT && Params.tempTarget != null){
			StringBuilder sBuilder = new StringBuilder();
			for(int i = 0; i < rlSelection.getChildCount(); i++){
				View view = rlSelection.getChildAt(i);
				sBuilder.append("[@" + view.getTag().toString() + "]");
			}
			int start = Params.tempTarget.getSelectionStart();
			StringBuilder str = new StringBuilder(Params.tempTarget.getText().toString());
			str.insert(start, sBuilder);
			TagFormatter tagFormatter = new TagFormatter(this);
			tagFormatter.setText(str.toString());
			Params.tempTarget.setText(tagFormatter.format());
			Params.tempTarget.setSelection(start + sBuilder.length());
		}
		super.finish();
	}

	@Override
	public void onScroll(int scrollY) {
		int rangeY = mcontainer.getMeasuredHeight();
		if((scrollY > rangeY -  svRefresh.getHeight() - 10)&&(footer_view == null)&&(nomore_view == null)&&(!searchName.equals(""))){
			getSearchList();
		}
	}
	
}
