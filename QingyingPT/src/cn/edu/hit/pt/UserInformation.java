package cn.edu.hit.pt;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.FutureTask;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.TorrentImage;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.http.URLImageParser;
import cn.edu.hit.pt.http.UrlTagHandler;
import cn.edu.hit.pt.http.UserAvatarTask;
import cn.edu.hit.pt.impl.DatabaseUtil;
import cn.edu.hit.pt.impl.ScaleUtil;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.model.Torrent;
import cn.edu.hit.pt.model.User;
import cn.edu.hit.pt.widget.CustomScrollView;
import cn.edu.hit.pt.widget.HeaderScrollLayout;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

@SuppressLint({ "ClickableViewAccessibility", "InflateParams", "RtlHardcoded" })
public class UserInformation extends SwipeBackActivity implements OnTouchListener{
	private String TAG = "UserInformation";
	private LinearLayout mcontainer;
	private CustomScrollView svRefresh;
	private HeaderScrollLayout rlHeader;
	private ImageView ivUserAvatar;
	private ImageView ivDonor;
	private TextView tvUsername;
	private TextView tvUserTitle;
	private TextView tvUploaded;
	private TextView tvDownloaded;
	private TextView tvUserClass;
	private TextView tvRatio;
	private LinearLayout rlUserDetail;
	private LinearLayout rlPrivacyWarning;
	private LinearLayout torrent_list;
	private RelativeLayout rlTorrentNum;
	private LinearLayout rlTorrents;
	private TextView tvTorrentNum;
	private ImageView ivTorrentBig;
	private TextView tvDescription;
	private TextView tvGender;
	private TextView tvCountry;
	private TextView tvSchool;
	private ImageView ivCountry;
	private Button btnReturn;
	private Button btnMore;
	private LinearLayout btnUserSettings;
	private LinearLayout btnAddfriend;
	private LinearLayout btnSendPM;
	private TextView tvAddFriend;
	private ImageView ivAddFriend;

	private RelativeLayout footer_view = null;
	private RelativeLayout first_tip;

	private long id;
	private String name;
    private int len = 50;
    private float startY;
    private int bottom;
	private int window_width;
    private boolean canScroll = true;
	private UserAvatarTask avatarTask;
	private FutureTask<Response> mainTask;
	private FutureTask<Response> friendTask;
	private ArrayList<TorrentImage> imageTasks = new ArrayList<TorrentImage>();
	private HttpAsyncExecutor asyncExecutor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_information);
		
		SwipeBackLayout mSwipeBackLayout;
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

		mcontainer = (LinearLayout)findViewById(R.id.mcontainer);
		svRefresh = (CustomScrollView)findViewById(R.id.svRefresh);
		rlHeader = (HeaderScrollLayout)findViewById(R.id.rlHeader);
		ivUserAvatar = (ImageView)findViewById(R.id.ivAvatar);
		ivDonor = (ImageView)findViewById(R.id.ivDonor);
		tvUsername = (TextView)findViewById(R.id.tvUsername);
		tvUserTitle = (TextView)findViewById(R.id.tvTitle);
		tvUploaded = (TextView)findViewById(R.id.tvUpload);
		tvDownloaded = (TextView)findViewById(R.id.tvDownload);
		tvRatio = (TextView)findViewById(R.id.tvRatio);
		tvUserClass = (TextView)findViewById(R.id.tvUserClass);
		rlUserDetail = (LinearLayout)findViewById(R.id.rlUserDetail);
		rlPrivacyWarning = (LinearLayout)findViewById(R.id.rlPrivacyWarning);
		torrent_list = (LinearLayout)findViewById(R.id.torrent_list);
		rlTorrentNum = (RelativeLayout)findViewById(R.id.rlTorrentNum);
		rlTorrents = (LinearLayout)findViewById(R.id.rlTorrents);
		tvTorrentNum = (TextView)findViewById(R.id.tvTorrentNum);
		ivTorrentBig = (ImageView)findViewById(R.id.ivTorrentBig);
		tvDescription = (TextView)findViewById(R.id.tvDescription);
		tvGender = (TextView)findViewById(R.id.tvGender);
		tvCountry = (TextView)findViewById(R.id.tvCountry);
		tvSchool = (TextView)findViewById(R.id.tvSchool);
		ivCountry = (ImageView)findViewById(R.id.ivCountry);
		btnReturn = (Button)findViewById(R.id.btnReturn);
		btnMore = (Button)findViewById(R.id.btnMore);
		btnUserSettings = (LinearLayout)findViewById(R.id.btnUserSettings);
		btnAddfriend = (LinearLayout)findViewById(R.id.btnAddfriend);
		btnSendPM = (LinearLayout)findViewById(R.id.btnSendPM);
		tvAddFriend = (TextView)findViewById(R.id.tvAddFriend);
		ivAddFriend = (ImageView)findViewById(R.id.ivAddFriend);
		first_tip = (RelativeLayout)findViewById(R.id.first_tip);

		window_width = ScaleUtil.widthPixels(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(ScaleUtil.heightPixels(this) / 2.5));
		rlHeader.setLayoutParams(lp);
		
		if(footer_view == null){
			footer_view = (RelativeLayout) getLayoutInflater().inflate(R.layout.refresh_view, null);
			ImageView iv = (ImageView)footer_view.findViewById(R.id.iv);
			iv.setImageResource(R.drawable.loading);
			AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
			animationLoading.start();
			mcontainer.addView(footer_view);
		}
		
		Intent intent = getIntent();
		id = intent.getLongExtra("userid", 0);
		if((id == 0)||(id == Params.CURUSER.id)){
			btnUserSettings.setVisibility(View.VISIBLE);
			btnAddfriend.setVisibility(View.GONE);
			loadUserInfo(Params.CURUSER, false);
		}
		
		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(this));
		mainTask = asyncExecutor.execute(new MyHttpRequest(URLContainer.getUserInforUrl(id, false)), new HttpResponseHandler() {
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				loadUserInfo(res.getObject(User.class), true);
				DatabaseUtil.userDatabase(UserInformation.this).save(res.getObject(User.class));
				if(footer_view != null){
					mcontainer.removeView(footer_view);
					footer_view = null;
				}

			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(UserInformation.this).handleException(e);
				if(footer_view != null){
					mcontainer.removeView(footer_view);
					footer_view = null;
				}

			}
        });
		
		btnReturn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		btnSendPM.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.putExtra("sender_name", name);
				intent.putExtra("sender", id);
				intent.setClass(UserInformation.this, ViewMail.class);
				startActivity(intent);
			}
		});
		
		btnUserSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(UserInformation.this, ProfileSettings.class);
				startActivity(intent);
			}
		});
		
		btnMore.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				UserInfoMenu sWindow = new UserInfoMenu(UserInformation.this, id);
				sWindow.showAtLocation(btnMore, Gravity.RIGHT|Gravity.TOP, 0, ScaleUtil.Dp2Px(UserInformation.this, 75));
			}
		});
		
		if(Params.tips.swipeback == false){
			first_tip.setVisibility(View.VISIBLE);
			Params.tips.swipeback = true;
			DatabaseUtil.userDatabase(UserInformation.this).save(Params.tips);
		}
		first_tip.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				first_tip.setVisibility(View.GONE);
			}
		});
	}

	public void setFriends(long id) {
		friendTask = asyncExecutor.execute(new MyHttpRequest(URLContainer.getAddContactURL(id)), new HttpResponseHandler() {
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				JSONObject json;
				try {
					json = new JSONObject(res.getString());
					int result = json.getInt("result");
					if(result == 0){
						tvAddFriend.setText(getString(R.string.add_friend));
						ivAddFriend.setImageResource(R.drawable.button_addfriend);
					}else{
						tvAddFriend.setText(getString(R.string.remove_friend));
						ivAddFriend.setImageResource(R.drawable.button_removefriend);
					}
				} catch (JSONException e) {
					Log.e(TAG, e.toString());
				}
				btnAddfriend.setEnabled(true);
				Params.refresh_friends = true;
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(UserInformation.this).handleException(e);
			}
        });
		
	}
	
	private void loadUserInfo(final User user, boolean refreshUser){
		if(user == null) return;
		avatarTask = new UserAvatarTask(this, user.id, ivUserAvatar, false);
		if(user.id == Params.CURUSER.id){
			btnMore.setVisibility(View.VISIBLE);
			if(refreshUser)
				Params.CURUSER = user;
		}
		id = user.id;
		name = user.name;
		if(user.donor.equals("yes"))
			ivDonor.setVisibility(View.VISIBLE);
		if(user.name.equals("")){
			tvUsername.setText(getString(R.string.invalid_user));
			tvUsername.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
		}
		else
			tvUsername.setText(user.name);
		tvUserTitle.setText(user.title);
		tvUploaded.setText(Util.format_size(user.uploaded));
		tvDownloaded.setText(Util.format_size(user.downloaded));
		tvRatio.setText(Util.format_ratio(this, user.uploaded, user.downloaded));
		tvUserClass.setText(user.ucname);
		tvUserClass.setVisibility(View.VISIBLE);
		if(user.gender.equals("Male"))
			tvGender.setText(getString(R.string.male));
		else if (user.gender.equals("Female")) 
			tvGender.setText(getString(R.string.female));
		else
			tvGender.setText(getString(R.string.unknown));
		if(user.country.equals(""))
			tvCountry.setText(getString(R.string.none));
		else
			tvCountry.setText(user.country);
		if(!user.school.equals(""))
			tvSchool.setText(user.school);
		if(!user.flagpic.equals("")){
			try {
				InputStream is = getAssets().open("flag/" + user.flagpic);
				Drawable d = Drawable.createFromResourceStream(getResources(), null, is, "");
				int scaleWidth = ScaleUtil.Dp2Px(UserInformation.this, d.getIntrinsicWidth());
				int scaleHeight = ScaleUtil.Dp2Px(UserInformation.this, d.getIntrinsicHeight());
				d.setBounds(0, 0, scaleWidth, scaleHeight);
	            is.close();
				ivCountry.setImageDrawable(d);
				ivCountry.setVisibility(View.VISIBLE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(user.info.equals(""))
			tvDescription.setText(getString(R.string.no_description));
		else{
			URLImageParser p_text = new URLImageParser(tvDescription, UserInformation.this);
			UrlTagHandler t_text = new UrlTagHandler(tvDescription, UserInformation.this);
			Spanned sp_text = Html.fromHtml(user.info, p_text, t_text);
			tvDescription.setText(sp_text);
		}
		if(user.privacy.equals("strong")){
			rlPrivacyWarning.setVisibility(View.VISIBLE);
		}else{
			rlUserDetail.setVisibility(View.VISIBLE);
			if(user.torrentNum > 0)
				tvTorrentNum.setText("(" + user.torrentNum + ")");
			if((user.torrents != null)&&(user.torrents.size() > 0)){
				LinearLayout.LayoutParams lpLarge = new LinearLayout.LayoutParams(window_width/3, window_width/3);
				rlTorrentNum.setLayoutParams(lpLarge);
				LinearLayout.LayoutParams lpSmall = new LinearLayout.LayoutParams(window_width/6, window_width/6);
				LinearLayout ll = null;
				rlTorrents.removeAllViews();
				int i = 0;
				for (final Torrent torrent : user.torrents) {
					if((i-1)%4==0){
						ll = new LinearLayout(getBaseContext());
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
						ll.setLayoutParams(lp);
						rlTorrents.addView(ll);
					}
					OnClickListener torrentClickListener = new OnClickListener() {
						
						@Override
						public void onClick(View view) {
							Intent intent = new Intent();
							intent.putExtra("id", torrent.torrentid);
							intent.setClass(UserInformation.this, TorrentActivity.class);
							startActivity(intent);
						}
					};
					TorrentImage imageTask;
					if(i>0){
						ImageView iv = new ImageView(getBaseContext());
						iv.setLayoutParams(lpSmall);
						imageTask = new TorrentImage(this, iv, torrent.torrentid, TorrentImage.TYPE_SMALL);
						iv.setOnClickListener(torrentClickListener);
						ll.addView(iv);
					}else{
						imageTask = new TorrentImage(this, ivTorrentBig, torrent.torrentid, TorrentImage.TYPE_SMALL);
						ivTorrentBig.setOnClickListener(torrentClickListener);
					}
					imageTasks.add(imageTask);
					i++;
				}
			}else{
				torrent_list.removeAllViews();
				TextView tv = new TextView(getBaseContext());
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 100);
				tv.setLayoutParams(lp);
				tv.setText(getString(R.string.no_torrent));
				tv.setGravity(Gravity.CENTER);
				torrent_list.addView(tv);
			}
			torrent_list.setVisibility(View.VISIBLE);
		}
		if(user.friend.equals("yes")){
			tvAddFriend.setText(getString(R.string.remove_friend));
			ivAddFriend.setImageResource(R.drawable.button_removefriend);
		}
		btnAddfriend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				btnAddfriend.setEnabled(false);
				setFriends(id);
			}
		});

	}

	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		return super.onTouchEvent(ev);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev){
	    float currentY = ev.getY();
	    switch (ev.getAction()) {
	    case MotionEvent.ACTION_DOWN:
	        startY = currentY;
	        bottom = rlHeader.getBottom();
	        rlHeader.setViewBottom(bottom);
	        break;
	    case MotionEvent.ACTION_MOVE:
	        if (canScroll && startY > bottom && rlHeader.isShown() && rlHeader.getTop() >= 0) {
	            int y = (int) (bottom + (currentY - startY) / 2.5f);
	            if (y < rlHeader.getBottom() + len && y >= bottom) {
	            	rlHeader.setLayoutParams(new LinearLayout.LayoutParams(rlHeader.getWidth(), y));
	            	svRefresh.setEnabled(false);
	    	    	if(svRefresh.getScrollY() > 0){
	    	    		canScroll = false;
	    	    	}
	            }
	            rlHeader.setCanScroll(false);
	        }
	        break;
	    case MotionEvent.ACTION_UP:
	    case MotionEvent.ACTION_CANCEL:
	    	if(svRefresh.getScrollY() <= 0)
	    		canScroll = true;
	    	else
	    		canScroll = false;
	    	svRefresh.setEnabled(true);
	    	rlHeader.startScroll(0, rlHeader.getBottom(), 0, bottom - rlHeader.getBottom());
	        break;
	    }
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void finish() {
		mainTask.cancel(true);
		if(avatarTask != null)
			avatarTask.cancel();
		if(friendTask!=null)
			friendTask.cancel(true);
		if(imageTasks.size()>0){
			for (int i = 0; i < imageTasks.size(); i++) {
				if(imageTasks.get(i) != null)
					imageTasks.get(i).cancel();
			}
		}
		super.finish();
	}
}
