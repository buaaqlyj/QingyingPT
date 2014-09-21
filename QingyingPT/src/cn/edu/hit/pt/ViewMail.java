package cn.edu.hit.pt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.http.URLImageParser;
import cn.edu.hit.pt.http.UploadAsyncTask;
import cn.edu.hit.pt.http.UrlTagHandler;
import cn.edu.hit.pt.http.UserAvatarTask;
import cn.edu.hit.pt.http.UploadAsyncTask.onUploadFinishedListener;
import cn.edu.hit.pt.impl.TagFormatter;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.model.Attachment;
import cn.edu.hit.pt.model.Mail;
import cn.edu.hit.pt.model.Message;
import cn.edu.hit.pt.widget.CustomScrollView;
import cn.edu.hit.pt.widget.CustomScrollView.OnScrollListener;
import cn.edu.hit.pt.widget.ImplementsBar;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

public class ViewMail extends SwipeBackActivity implements OnScrollListener{
	//private String TAG = "ViewMail";
	private Button btnReturn;
	private Button postMail;
	private Button addSmile;
	private Button addOther;
	private EditText etMail;
	private TextView tvTitle;
	private RelativeLayout header_view;
	private CustomScrollView svRefresh;
	private LinearLayout mcontainer;
	private RelativeLayout bottom_layout;
	private ImplementsBar implementsBar;
	
	private int page = 0;
	private int last_Height = 0;
	private int now_height = 0;
	private long id = 0;
	private boolean ifTheEnd = false;
	private String last_added = "";
	private String body = "";
	
	private HttpAsyncExecutor asyncExecutor;
	private ArrayList<FutureTask<Response>> httpTasks = new ArrayList<FutureTask<Response>>();
	private ArrayList<UserAvatarTask> avatarTasks = new ArrayList<UserAvatarTask>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_mail);
		
		SwipeBackLayout mSwipeBackLayout;
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
		
		addSmile = (Button)findViewById(R.id.addSmile);
		etMail = (EditText)findViewById(R.id.etMail);
		tvTitle = (TextView)findViewById(R.id.tvTitle);
		mcontainer = (LinearLayout)findViewById(R.id.container);
		svRefresh = (CustomScrollView)findViewById(R.id.svRefresh);
		bottom_layout = (RelativeLayout)findViewById(R.id.bottom_layout);
		addOther = (Button)findViewById(R.id.addOther);
		btnReturn = (Button)findViewById(R.id.btnReturn);
		
		svRefresh.setOnScrollListener(this);
		
		implementsBar = (ImplementsBar)findViewById(R.id.implementsBar);
		implementsBar.setFragmentTransaction(getSupportFragmentManager());
		implementsBar.setTargetView(etMail);

		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(this));
		
		Intent intent = getIntent();
		String sender_name = intent.getStringExtra("sender_name");
		tvTitle.setText(sender_name);
		
		id = intent.getLongExtra("sender", 0);

		if(id == 0)
			bottom_layout.setVisibility(View.GONE);

		addOther.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				implementsBar.fold(ImplementsBar.ITEM_ATTACHMENT);
			}
		});

		addSmile.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				implementsBar.fold(ImplementsBar.ITEM_SMILIES);
			}
		});
		
		btnReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		postMail = (Button)findViewById(R.id.postMail);
		postMail.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				implementsBar.hide();
				body = etMail.getText().toString();
				etMail.setText("");
				sendMail();
			}
		});
		
		set_loading(0);
		
	}

	private void retrunBottom(){
		Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
            public void run() {
			 	LinearLayout mcontainer = (LinearLayout)findViewById(R.id.container);
			 	now_height = mcontainer.getMeasuredHeight();
				ScrollView svRefresh = (ScrollView)findViewById(R.id.svRefresh);
				if(last_Height == 0)
					svRefresh.scrollBy(0, now_height);
				else
					svRefresh.scrollBy(0, now_height - last_Height);
			 	last_Height = mcontainer.getMeasuredHeight();
            }
        });
	}
	
	public void setMailRow(final Message message, RelativeLayout rlList, boolean local) {
		TextView tvText = (TextView)rlList.findViewById(R.id.tvText);
		TextView tvSubject = (TextView)rlList.findViewById(R.id.tvSubject);
		ImageView ivUserAvatar = (ImageView)rlList.findViewById(R.id.ivUserAvatar);
		if(id == 0){
			URLImageParser p_title = new URLImageParser(tvSubject, ViewMail.this);
			UrlTagHandler t_title = new UrlTagHandler(tvSubject, ViewMail.this);
			Spanned sp_title = Html.fromHtml(message.subject, p_title, t_title);
			tvSubject.setText(sp_title);
		}else
			tvSubject.setVisibility(View.GONE);
		
		if(message.sender != 0){
			avatarTasks.add(new UserAvatarTask(ViewMail.this, message.sender, ivUserAvatar, true));
		}else{
			ivUserAvatar.setImageResource(R.drawable.ic_logo);
		}

		if(local){
		    TagFormatter tagFormatter = new TagFormatter(this, message.msg);
		    tvText.setText(tagFormatter.format());			
		}else{
			URLImageParser p_text = new URLImageParser(tvText, ViewMail.this);
			UrlTagHandler t_text = new UrlTagHandler(tvText, ViewMail.this);
			Spanned sp_text = Html.fromHtml(message.msg, p_text, t_text);
		    tvText.setText(sp_text);
		}
	}
	
	public RelativeLayout addMailRow(final Message message, int i, int size, boolean local) {
	    if(message == null) return null;
	    final LayoutInflater inflater =  LayoutInflater.from(getBaseContext());
	    RelativeLayout rlList = null;
	    if(message.type.equals("receive"))
	    	rlList = (RelativeLayout)inflater.inflate( R.layout.view_mail_row_left, null);
	    if(message.type.equals("send"))
	    	rlList = (RelativeLayout)inflater.inflate( R.layout.view_mail_row_right, null);
	    setMailRow(message, rlList, local);
	    if(local){
	    	mcontainer.addView(rlList);
	    }else{
			if((i == 0)&&(i != size - 1)){
				TextView tvTime_head = (TextView)findViewById(R.id.tvTime_head);
				if((tvTime_head != null)&&(tvTime_head.getText().equals(message.added)))
					mcontainer.removeView((RelativeLayout)findViewById(R.id.timestamp_head));
			    mcontainer.addView(rlList, 0);
			}
			else if(i == size - 1){
				if((last_added=="")&&(i==0))
					last_added = message.added;
				
		    	RelativeLayout rlTimestamp = (RelativeLayout)inflater.inflate( R.layout.timestamp_row, null);
				TextView tvTime = (TextView)rlTimestamp.findViewById(R.id.tvTime);
				if(!message.added.equals(last_added)){
					tvTime.setText(last_added);
				    mcontainer.addView(rlTimestamp, 0);
				}
			    mcontainer.addView(rlList, 0);
			    
		    	RelativeLayout rlTimestamp_head = (RelativeLayout)inflater.inflate( R.layout.timestamp_head, null);
				TextView tvTime_head_new = (TextView)rlTimestamp_head.findViewById(R.id.tvTime_head);
				tvTime_head_new.setText(message.added);
			    mcontainer.addView(rlTimestamp_head, 0);
				
			}else{
				if(i > 0){
			    	RelativeLayout rlTimestamp = (RelativeLayout)inflater.inflate( R.layout.timestamp_row, null);
					TextView tvTime = (TextView)rlTimestamp.findViewById(R.id.tvTime);
					if(!message.added.equals(last_added)){
						tvTime.setText(last_added);
					    mcontainer.addView(rlTimestamp, 0);
					}
				}
			    mcontainer.addView(rlList, 0);
			}
	    }
		return rlList;
	}
	
	public void loadMailList() {
		MyHttpRequest request = new MyHttpRequest(URLContainer.getPeerMailURL(id, page));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				if(page == 0)
					mcontainer.removeAllViews();
				
				Mail mail = res.getObject(Mail.class);
				if(mail != null){
					tvTitle.setText(mail.peer_name);
					if(mail.mails != null){
						int i = 0;
						for(final Message message : mail.mails){
						    addMailRow(message, i, mail.mails.size(), false);
							last_added = message.added;
							i++;
						}
						page++;
						retrunBottom();
					}
				}
				if(mail == null || mail.mails == null || mail.mails.size() < 20){
					ifTheEnd = true;
				}
				
				if(header_view != null){
					mcontainer.removeView(header_view);
					header_view = null;
				}
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(ViewMail.this).handleException(e);
				if(header_view != null){
					mcontainer.removeView(header_view);
					header_view = null;
				}
			}
		}));
	}
	
	public void sendMail(){
		postMail.setEnabled(false);
		File file = (File) etMail.getTag();
		if(file != null){
			Map<String, String> params = new HashMap<String, String>();
			params.put("type", "attachment");
			UploadAsyncTask uploadTask = new UploadAsyncTask(this, URLContainer.getUploadURL(), params, file);
			uploadTask.setOnUploadFinishedListener(new onUploadFinishedListener() {
				
				@Override
				public void onFinished(Attachment result) {
					if(result.error == 0){
						etMail.setTag(null);
						implementsBar.clearCounter();
						body = body + "\n[attach]" + result.dlkey + "[/attach]";
						postMail();
					}else{
						postMail.setEnabled(true);
					}
				}
			});
			uploadTask.execute();
		}else if(!body.equals("")){
			postMail();
		}else{
			postMail.setEnabled(true);
		}
	}
	
	public void postMail() {
		Message message = new Message(Params.CURUSER.id, body, "send");
		final RelativeLayout rlList = addMailRow(message, 0, 1, true);
		final ProgressBar pbPosting = (ProgressBar)rlList.findViewById(R.id.pbPosting);
		final ImageView btnError = (ImageView)rlList.findViewById(R.id.btnError);
		rlList.setTag(body);
		pbPosting.setVisibility(View.VISIBLE);
		btnError.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				body = rlList.getTag().toString();
				mcontainer.removeView(rlList);
				postMail();
			}
		});
		pbPosting.setVisibility(View.VISIBLE);
		retrunBottom();
		
		MyHttpRequest request = new MyHttpRequest(URLContainer.getAddMailURL(id, body));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				Message message = res.getObject(Message.class);
				if(message != null){
					if(Util.checkMailResult(ViewMail.this, message.status)){
						setMailRow(message, rlList, false);
						retrunBottom();
						Params.refresh_mail = true;
					}
				}else{
					Toast.makeText(ViewMail.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
					btnError.setVisibility(View.VISIBLE);
				}
				postMail.setEnabled(true);
				pbPosting.setVisibility(View.GONE);
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(ViewMail.this).handleException(e);
				postMail.setEnabled(true);
				pbPosting.setVisibility(View.GONE);
				btnError.setVisibility(View.VISIBLE);
			}
			
		}));
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

	@Override
	public void onScroll(int scrollY) {
		set_loading(scrollY);
	}
	
	private void set_loading(int scrollY) {
		if(scrollY < 10 && header_view == null && ifTheEnd == false){
			header_view = (RelativeLayout) getLayoutInflater().inflate(R.layout.refresh_view, null);
			ImageView iv = (ImageView)header_view.findViewById(R.id.iv);
			iv.setImageResource(R.drawable.loading);
			AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
			animationLoading.start();
			mcontainer.addView(header_view, 0);
			loadMailList();
		}
	}
}
