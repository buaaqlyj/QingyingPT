package cn.edu.hit.pt;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
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
import cn.edu.hit.pt.model.MailItem;
import cn.edu.hit.pt.model.Message;
import cn.edu.hit.pt.widget.CustomDialog;
import cn.edu.hit.pt.widget.CustomScrollView;
import cn.edu.hit.pt.widget.CustomScrollView.OnScrollListener;
import cn.edu.hit.pt.widget.PullToRefreshView;
import cn.edu.hit.pt.widget.PullToRefreshView.OnHeaderRefreshListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

@SuppressLint("InflateParams") public class MailFragment extends Fragment implements OnHeaderRefreshListener, OnScrollListener{
	private String TAG = "MailFragment";
	static Button showLeft;
	private Button btnContacts;
	private LinearLayout mcontainer;
	private PullToRefreshView mPullToRefreshView;
	private CustomScrollView svRefresh;
	private RelativeLayout footer_view = null;
	private RelativeLayout nomore_view = null;
	private LayoutInflater inflater;
	private int mailPage = 0;
	
	private HttpAsyncExecutor asyncExecutor;
	private MyBroadcastReceiver buttonRefreshBroadcastReceiver;

	@Override
	public void onResume() {
		if(Params.refresh_mail == true){
			mailPage = 0;
			mPullToRefreshView.setHeaderRefreshing();
		}
		Params.refresh_mail = false;
		super.onResume();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View mView = inflater.inflate(R.layout.mailfragment, null);
		mPullToRefreshView = (PullToRefreshView)mView.findViewById(R.id.pull_refresh_view);
		mcontainer = (LinearLayout)mView.findViewById(R.id.container);
		svRefresh = (CustomScrollView)mView.findViewById(R.id.svRefresh);
		showLeft = (Button) mView.findViewById(R.id.showLeft);
		btnContacts = (Button)mView.findViewById(R.id.btnContacts);
		
		return mView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((MainActivity) getActivity()).mainFragmentPagerAdapter.restoreState(this);

		buttonRefreshBroadcastReceiver = new MyBroadcastReceiver();
		getActivity().registerReceiver(buttonRefreshBroadcastReceiver, new IntentFilter(MainActivity.ACTION_REFRESH_BUTTON));
		
		svRefresh.setOnScrollListener(this);
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		mPullToRefreshView.deleteFooter();
		
		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(getActivity()));

		Util.setShowLeftButton(showLeft);
		showLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).showLeft();
			}
		});

		btnContacts.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(getActivity(), ContactActivity.class);
				startActivity(intent);
			}
		});
		
		ArrayList<MailItem> mails = DatabaseUtil.userDatabase(getActivity()).queryAll(MailItem.class);
		setMails(mails);
	}
	
	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		if (this.getView() != null){
			this.getView().setVisibility(menuVisible ? View.VISIBLE : View.GONE);
			if(Params.refresh_mail == true){
				mailPage = 0;
				mPullToRefreshView.setHeaderRefreshing();
			}
			Params.refresh_mail = false;
		}
	}

	@Override
	public void onScroll(int scrollY) {
		int rangeY = mcontainer.getMeasuredHeight();
		if((scrollY > rangeY -  svRefresh.getHeight() - 10)&&(footer_view == null)&&(nomore_view == null)&&(mailPage != 0)){
			footer_view = (RelativeLayout) inflater.inflate(R.layout.refresh_view, null);
			ImageView iv = (ImageView)footer_view.findViewById(R.id.iv);
			iv.setImageResource(R.drawable.loading);
			AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
			animationLoading.start();
			mcontainer.addView(footer_view);
			getMails();
		}
	}

	@Override
	public void onHeaderRefresh(PullToRefreshView view) {
		mailPage = 0;
		getMails();
	}
	
	public void getMails() {
		MyHttpRequest request = new MyHttpRequest(URLContainer.getMailURL(mailPage));
		asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				try {
					ArrayList<MailItem> mails = new ArrayList<MailItem>();
					JSONArray jsonArray = new JSONArray(res.getString());
					for (int i = 0; i < jsonArray.length(); i++) {
						Gson gson = new GsonBuilder().create();
						MailItem mail = gson.fromJson(jsonArray.get(i).toString(), MailItem.class);
						mails.add(mail);
					}
					if(mailPage == 0){
						DatabaseUtil.userDatabase(getActivity()).deleteAll(MailItem.class);
						DatabaseUtil.userDatabase(getActivity()).save(mails);
					}
					setMails(mails);
					getActivity().sendBroadcast(new Intent(MainActivity.ACTION_REFRESH_COUNTER));
					getActivity().sendBroadcast(new Intent(MainActivity.ACTION_REFRESH_BUTTON));
					Params.refresh_mail = false;
				} catch (JSONException e) {
					mPullToRefreshView.onHeaderRefreshComplete();
					if(footer_view != null){
						mcontainer.removeView(footer_view);
						footer_view = null;
					}
					if(mailPage == 0){
						LayoutInflater inflater =  LayoutInflater.from(getActivity());
						LinearLayout rlList = (LinearLayout)inflater.inflate(R.layout.no_message_tip, null);
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(svRefresh.getWidth(), svRefresh.getHeight());
						rlList.setLayoutParams(lp);
						mcontainer.addView(rlList);
					}else if(mailPage != 0){
						nomore_view = (RelativeLayout) inflater.inflate(R.layout.footer_nomore_row, null);
						mcontainer.addView(nomore_view);
					}
				}
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(getActivity()).handleException(e);
				mPullToRefreshView.onHeaderRefreshComplete();
				if(footer_view != null){
					mcontainer.removeView(footer_view);
					footer_view = null;
				}
				if(mcontainer.getChildCount() == 0){
					LayoutInflater inflater =  LayoutInflater.from(getActivity());
					LinearLayout rlList = (LinearLayout)inflater.inflate(R.layout.unknown_error_tip, null);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(svRefresh.getWidth(), svRefresh.getHeight()-70);
					rlList.setLayoutParams(lp);
					mcontainer.addView(rlList);
				}
			}
			
		});
	}
	
	public void setMails(ArrayList<MailItem> list) {
		mPullToRefreshView.onHeaderRefreshComplete();
		if(list == null || list.size() == 0) return;
		if(mailPage == 0){
			svRefresh.scrollTo(0, 0);
			mcontainer.removeAllViews();
		}
		for(final MailItem mail : list){
			LayoutInflater inflater =  LayoutInflater.from(getActivity());
			final RelativeLayout rlList = (RelativeLayout)inflater.inflate(R.layout.mail_row, null);
			TextView tvUsername = (TextView)rlList.findViewById(R.id.tvUserName);
			final TextView tvTime = (TextView)rlList.findViewById(R.id.tvTime);
			TextView tvText = (TextView)rlList.findViewById(R.id.tvText);
			final TextView tvCount = (TextView)rlList.findViewById(R.id.tvCount);
			ImageView ivUserAvatar = (ImageView)rlList.findViewById(R.id.ivUserAvatar);

			if(mail.sender_name.equals("")){
				tvUsername.setText(getString(R.string.invalid_user));
				tvUsername.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
			}
			else
				tvUsername.setText(mail.sender_name);
			tvTime.setText(mail.added);
			tvText.setText(mail.msg);
			if(mail.unread_count != 0){
				tvCount.setText(String.valueOf(mail.unread_count));
				tvCount.setVisibility(View.VISIBLE);
			}

			rlList.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View arg0) {
					CustomDialog.Builder customBuilder = new CustomDialog.Builder(getActivity());
		            customBuilder.setTitle(R.string.text_delete)
		            .setMessage(getString(R.string.text_delete_comfirm_1) + mail.sender_name + getString(R.string.text_delete_comfirm_2))
	                .setPositiveButton(R.string.text_delete, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {
							deleteMail(rlList, mail.sender, mail.unread_count);
							dialog.dismiss();
	                    }
	                })
	                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
	                    }
	                }).create().show();
					return true;
				}
			});
		    
		    rlList.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
			    	tvCount.setVisibility(View.GONE);
			    	Params.unread_count -= mail.unread_count;
			    	mail.unread_count = 0;
					getActivity().sendBroadcast(new Intent(MainActivity.ACTION_REFRESH_COUNTER));
					getActivity().sendBroadcast(new Intent(MainActivity.ACTION_REFRESH_BUTTON));
					Intent intent = new Intent();
					intent.putExtra("sender_name", mail.sender_name);
					intent.putExtra("sender", mail.sender);
					intent.setClass(getActivity(), ViewMail.class);
					startActivity(intent);
				}
			});
		    
			if(mail.sender!=0){
				new UserAvatarTask(getActivity(), mail.sender, ivUserAvatar, true);
				mcontainer.addView(rlList);
			}else{
				ivUserAvatar.setImageResource(R.drawable.ic_logo);
				mcontainer.addView(rlList, 0);
			}
		}
		if(list.size() < 20){
			nomore_view = (RelativeLayout)inflater.inflate(R.layout.footer_nomore_row, null);
			mcontainer.addView(nomore_view);
		}
		mailPage++;

		if(footer_view != null){
			mcontainer.removeView(footer_view);
			footer_view = null;
		}
	}
	
	public void deleteMail(final RelativeLayout rlList, long sender, final int unread_count) {
		MyHttpRequest request = new MyHttpRequest(URLContainer.getDelMailURL(sender));
		asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				Message message = res.getObject(Message.class);
				if(message.status == 0){
					TranslateAnimation anim = new TranslateAnimation(0, rlList.getWidth(), 0, 0);
					anim.setDuration(200);
					anim.setFillAfter(true);
					anim.setAnimationListener(new AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							try {
								mcontainer.removeView(rlList);
								if(mcontainer.getChildCount()==1 && nomore_view != null){
									mcontainer.removeAllViews();
									LayoutInflater inflater =  LayoutInflater.from(getActivity());
									LinearLayout rlLayout = (LinearLayout)inflater.inflate(R.layout.no_message_tip, null);
									LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(svRefresh.getWidth(), svRefresh.getHeight());
									rlLayout.setLayoutParams(lp);
									mcontainer.addView(rlLayout);
								}
								Params.unread_count -= unread_count;
								getActivity().sendBroadcast(new Intent(MainActivity.ACTION_REFRESH_COUNTER));
								getActivity().sendBroadcast(new Intent(MainActivity.ACTION_REFRESH_BUTTON));
							} catch (Exception e) {
								Log.e(TAG, "Main container removeView failed!");
							}
						}
					});
					rlList.startAnimation(anim);
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
		getActivity().unregisterReceiver(buttonRefreshBroadcastReceiver);
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
			}	
		}
	}
}