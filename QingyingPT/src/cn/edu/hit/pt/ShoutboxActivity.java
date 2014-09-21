package cn.edu.hit.pt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import org.json.JSONArray;
import org.json.JSONException;

import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
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
import cn.edu.hit.pt.http.UploadAsyncTask.onUploadFinishedListener;
import cn.edu.hit.pt.http.UrlTagHandler;
import cn.edu.hit.pt.http.UserAvatarTask;
import cn.edu.hit.pt.impl.TagFormatter;
import cn.edu.hit.pt.model.Attachment;
import cn.edu.hit.pt.model.ShoutRow;
import cn.edu.hit.pt.widget.CustomScrollView;
import cn.edu.hit.pt.widget.CustomScrollView.OnScrollListener;
import cn.edu.hit.pt.widget.ImplementsBar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

public class ShoutboxActivity extends SwipeBackActivity implements OnScrollListener{
	private String TAG = "Shoutbox";
	private Button btnReturn;
	private Button addShout;
	private Button addSmile;
	private Button addOther;
	private CustomScrollView svRefresh;
	private RelativeLayout header_view;
	private LinearLayout mcontainer;
	private EditText etContent;
	private ImplementsBar implementsBar;

	private String text = "";
	private long shoutbox_id = 0;
	private int last_Height=0;
	private int now_height=0;
	private String last_added = "";
	
	private HttpAsyncExecutor asyncExecutor;
	private ArrayList<FutureTask<Response>> httpTasks = new ArrayList<FutureTask<Response>>();
	private ArrayList<UserAvatarTask> avatarTasks = new ArrayList<UserAvatarTask>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shoutbox);
		SwipeBackLayout mSwipeBackLayout;
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
		
		btnReturn = (Button)findViewById(R.id.btnReturn);
		addShout = (Button)findViewById(R.id.addShout);
		addSmile = (Button)findViewById(R.id.addSmile);
		addOther = (Button)findViewById(R.id.addOther);
		mcontainer = (LinearLayout)findViewById(R.id.container);
		svRefresh = (CustomScrollView)findViewById(R.id.svRefresh);
		etContent = (EditText)findViewById(R.id.et_shoutbox);
		
		implementsBar = (ImplementsBar)findViewById(R.id.implementsBar);
		implementsBar.setFragmentTransaction(getSupportFragmentManager());
		implementsBar.setTargetView(etContent);
		implementsBar.disableUploadPhoto();

		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(this));
		
		svRefresh.setOnScrollListener(this);
		
		btnReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
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

		addShout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				implementsBar.hide();
				text = etContent.getText().toString();
				etContent.setText("");
				sendShout();
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
	
	public void setShoutRow(final ShoutRow shoutRow, RelativeLayout rlList, boolean local){
		TextView tvText = (TextView)rlList.findViewById(R.id.tvText);
		ImageView ivUserAvatar = (ImageView)rlList.findViewById(R.id.ivUserAvatar);

	    avatarTasks.add(new UserAvatarTask(ShoutboxActivity.this, shoutRow.userid, ivUserAvatar, true));
		
	    if(local){
		    TagFormatter tagFormatter = new TagFormatter(this, shoutRow.text);
		    tvText.setText(tagFormatter.format());
	    }else{
			URLImageParser p = new URLImageParser(tvText, ShoutboxActivity.this);
			UrlTagHandler t = new UrlTagHandler(tvText, ShoutboxActivity.this);
			Spanned sp = Html.fromHtml(shoutRow.text, p, t);
		    tvText.setText(sp);
		    tvText.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int start = etContent.getSelectionStart();
					String toAppend = "[@"+ shoutRow.username +"]";
					StringBuilder str = new StringBuilder(etContent.getText().toString());
					str.insert(start, toAppend);
					TagFormatter tagFormatter = new TagFormatter(ShoutboxActivity.this);
					tagFormatter.setText(str.toString());
					etContent.setText(tagFormatter.format());
					etContent.setSelection(start + toAppend.length());
				}
			});
			shoutbox_id = shoutRow.id;
	    }
		
		

	}
	
	public RelativeLayout addShoutRow(ShoutRow shoutRow, int i, int size, boolean local) {
	    if(shoutRow == null) return null;
	    LayoutInflater inflater =  LayoutInflater.from(getBaseContext());
	    RelativeLayout rlList = null;
	    if(shoutRow.userid != Params.CURUSER.id){
			rlList = (RelativeLayout)inflater.inflate( R.layout.shoutbox_row_left, null);
			TextView tvUsername = (TextView)rlList.findViewById(R.id.tvUserName);
			if(shoutRow.username.equals("")){
				tvUsername.setText(getString(R.string.invalid_user));
				tvUsername.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
			}
			else
				tvUsername.setText(shoutRow.username);
	    }else
	    	rlList = (RelativeLayout)inflater.inflate( R.layout.shoutbox_row_right, null);

	    setShoutRow(shoutRow, rlList, local);
	    if(local){
	    	mcontainer.addView(rlList);
	    }else{
			if((i == 0)&&(i != size - 1)){
				TextView tvTime_head = (TextView)findViewById(R.id.tvTime_head);
				if((tvTime_head != null)&&(tvTime_head.getText().equals(shoutRow.date)))
					mcontainer.removeView((RelativeLayout)findViewById(R.id.timestamp_head));
			    mcontainer.addView(rlList, 0);
			}
			else if(i == size - 1){
				if((last_added=="")&&(i==0))
					last_added = shoutRow.date;
				
		    	RelativeLayout rlTimestamp = (RelativeLayout)inflater.inflate( R.layout.timestamp_row, null);
				TextView tvTime = (TextView)rlTimestamp.findViewById(R.id.tvTime);
				if(!shoutRow.date.equals(last_added)){
					tvTime.setText(last_added);
				    mcontainer.addView(rlTimestamp, 0);
				}
				
			    mcontainer.addView(rlList, 0);
			    
		    	RelativeLayout rlTimestamp_head = (RelativeLayout)inflater.inflate( R.layout.timestamp_head, null);
				TextView tvTime_head_new = (TextView)rlTimestamp_head.findViewById(R.id.tvTime_head);
				tvTime_head_new.setText(shoutRow.date);
			    mcontainer.addView(rlTimestamp_head, 0);
				
			}
			else{
				if(i > 0){
			    	RelativeLayout rlTimestamp = (RelativeLayout)inflater.inflate( R.layout.timestamp_row, null);
					TextView tvTime = (TextView)rlTimestamp.findViewById(R.id.tvTime);
					if(!shoutRow.date.equals(last_added)){
						tvTime.setText(last_added);
					    mcontainer.addView(rlTimestamp, 0);
					}
				}
			    mcontainer.addView(rlList, 0);
			}
	    }
	    return rlList;
	}
	
	public void getShoutBox(){
		MyHttpRequest request = new MyHttpRequest(URLContainer.getShoutboxUrl(shoutbox_id));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {
			
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
	    		if(shoutbox_id == 0){
	    			mcontainer.removeAllViews();
	    			last_Height = 0;
	    		}
				try{
					JSONArray jsonArray = new JSONArray(res.getString());
					for (int i = 0; i < jsonArray.length(); i++) {
						Gson gson = new GsonBuilder().create();
						ShoutRow shoutRow = gson.fromJson(jsonArray.get(i).toString(), ShoutRow.class);
						if(shoutRow != null){
							addShoutRow(shoutRow, i, jsonArray.length(), false);
							last_added = shoutRow.date;
						}
					}
					retrunBottom();
				} catch (JSONException e) {
					if(header_view != null){
						mcontainer.removeView(header_view);
						header_view = null;
					}
					Log.e(TAG, e.toString());
				}
				if(header_view != null){
					mcontainer.removeView(header_view);
					header_view = null;
				}
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(ShoutboxActivity.this).handleException(e);
				if(header_view != null){
					mcontainer.removeView(header_view);
					header_view = null;
				}
			}
		}));
	}
	
	public void sendShout(){
		addShout.setEnabled(false);
		File file = (File) etContent.getTag();
		if(file != null){
			Map<String, String> params = new HashMap<String, String>();
			params.put("type", "attachment");
			UploadAsyncTask uploadTask = new UploadAsyncTask(this, URLContainer.getUploadURL(), params, file);
			uploadTask.setOnUploadFinishedListener(new onUploadFinishedListener() {
				
				@Override
				public void onFinished(Attachment result) {
					if(result.error == 0){
						etContent.setTag(null);
						implementsBar.clearCounter();
						text = text + "\n[attach]" + result.dlkey + "[/attach]";
						postShout();
					}else{
						addShout.setEnabled(true);
					}
				}
			});
			uploadTask.execute();
		}else if(!text.equals("")){
			postShout();
		}else{
			addShout.setEnabled(true);
		}
	}
	
	public void postShout() {
		ShoutRow shoutRow = new ShoutRow(Params.CURUSER.id, Params.CURUSER.name, text);
		final RelativeLayout rlList = addShoutRow(shoutRow, 0, 1, true);
		final ProgressBar pbPosting = (ProgressBar)rlList.findViewById(R.id.pbPosting);
		final ImageView btnError = (ImageView)rlList.findViewById(R.id.btnError);
		rlList.setTag(text);
		pbPosting.setVisibility(View.VISIBLE);
		btnError.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				text = rlList.getTag().toString();
				mcontainer.removeView(rlList);
				postShout();
			}
		});
		retrunBottom();
		
		MyHttpRequest request = new MyHttpRequest(URLContainer.getPostShoutboxUrl(text));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {
			
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				ShoutRow shoutRow = res.getObject(ShoutRow.class);
				if(shoutRow != null){
					setShoutRow(shoutRow, rlList, false);
				}else{
					Toast.makeText(ShoutboxActivity.this, R.string.post_failed, Toast.LENGTH_SHORT).show();
					btnError.setVisibility(View.VISIBLE);
				}
				pbPosting.setVisibility(View.GONE);
				addShout.setEnabled(true);
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(ShoutboxActivity.this).handleException(e);
				btnError.setVisibility(View.VISIBLE);
				pbPosting.setVisibility(View.GONE);
				addShout.setEnabled(true);
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
		if((scrollY < 10)&&(header_view == null)){
			header_view = (RelativeLayout) getLayoutInflater().inflate(R.layout.refresh_view, null);
			ImageView iv = (ImageView)header_view.findViewById(R.id.iv);
			iv.setImageResource(R.drawable.loading);
			AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
			animationLoading.start();
			mcontainer.addView(header_view, 0);
			getShoutBox();
		}
	}
}
