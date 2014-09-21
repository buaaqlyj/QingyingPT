package cn.edu.hit.pt;

import java.util.ArrayList;
import java.util.concurrent.FutureTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.http.URLImageParser;
import cn.edu.hit.pt.http.UrlTagHandler;
import cn.edu.hit.pt.http.UserAvatarTask;
import cn.edu.hit.pt.impl.TagFormatter;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.model.Post;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

public class TorrentComments extends Fragment implements OnTouchListener{
	private String Tag = "TorrentComments";
	private LayoutInflater inflater;
	private LinearLayout mcontainer;
	private EditText etReply;
	private Button btnReply;
	private RelativeLayout footer_view = null;
	
	public int commentCount;
	public long torrentid;
	private String body;
	private long commentid = 0;
	private boolean nomore = false;
	
	private HttpAsyncExecutor asyncExecutor;
	private ArrayList<FutureTask<Response>> httpTasks = new ArrayList<FutureTask<Response>>();
	
	public void setArgs(long torrentid, int commentCount) {
		this.torrentid = torrentid;
		this.commentCount = commentCount;
	}
	
	public boolean canLoadComments() {
		if(commentCount > 0 && nomore == false && footer_view == null)
			return true;
		else
			return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View mView = inflater.inflate(R.layout.torrent_comments, null);
		etReply = (EditText)mView.findViewById(R.id.etReply);
		btnReply = (Button)mView.findViewById(R.id.btnReply);
		mcontainer = (LinearLayout)mView.findViewById(R.id.mcontainer);
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(getActivity()));
		etReply.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View view, boolean focused) {
				if(focused){
					btnReply.setVisibility(View.VISIBLE);
					etReply.setGravity(Gravity.LEFT|Gravity.TOP);
					etReply.setBackgroundResource(R.drawable.edittext);
					etReply.setHeight(getResources().getDimensionPixelOffset(R.dimen.edittext_height_large));
				}else{
					btnReply.setVisibility(View.GONE);
					etReply.setGravity(Gravity.LEFT|Gravity.CENTER);
					etReply.setBackgroundResource(R.drawable.edittext_blurred);
					etReply.setHeight(getResources().getDimensionPixelOffset(R.dimen.edittext_height_small));
				}
			}
		});
		
		btnReply.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 0);
				param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
				body = etReply.getText().toString();
				if(!body.equals("")){
					btnReply.setEnabled(false);
					postComment();
				}
			}
		});
		
		getView().setOnTouchListener(this);
		loadComments();
	}
	
	public void loadComments(){
		if(footer_view == null){
			footer_view = (RelativeLayout)inflater.inflate(R.layout.refresh_view, null);
			ImageView iv = (ImageView)footer_view.findViewById(R.id.iv);
			iv.setImageResource(R.drawable.loading);
			AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
			animationLoading.start();
			mcontainer.addView(footer_view);
		}
		ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new NameValuePair("action", "torrent_comment"));
		param.add(new NameValuePair("id", torrentid + ""));
		param.add(new NameValuePair("commentid", commentid+""));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getTorrent(param));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				if(commentid == 0){
					mcontainer.removeAllViews();
					nomore = false;
				}
				try{
					JSONArray jsonArray = new JSONArray(res.getString());
					for (int i = 0; i < jsonArray.length(); i++) {
						Gson gson = new GsonBuilder().create();
						Post post = gson.fromJson(jsonArray.get(i).toString(), Post.class);
						addCommentRow(post, false);
					}
					if(jsonArray.length() < 20){
						nomore = true;
					}
				} catch (JSONException e) {
					Log.e(Tag, e.toString());
				}
				if(footer_view != null){
					mcontainer.removeView(footer_view);
					footer_view = null;
				}
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(getActivity()).handleException(e);
				if(footer_view != null){
					mcontainer.removeView(footer_view);
					footer_view = null;
				}
			}
		}));
	}
	
	public void setCommentRow(final Post post, boolean local, LinearLayout rlList) {
		if(post == null) return;
		TextView tvUsername = (TextView)rlList.findViewById(R.id.tvUsername);
		TextView tvAdded = (TextView)rlList.findViewById(R.id.tvAdded);
		TextView tvBody = (TextView)rlList.findViewById(R.id.tvBody);
		ImageView ivUserAvatar = (ImageView)rlList.findViewById(R.id.ivUserAvatar);
		if(post.username.equals("")){
			tvUsername.setText(getString(R.string.invalid_user));
			tvUsername.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
		}
		else
			tvUsername.setText(post.username);
		tvAdded.setText(post.added);

		new UserAvatarTask(getActivity(), post.userid, ivUserAvatar, true);
	    
		if(local == true){
			TagFormatter formatter = new TagFormatter(getActivity(), post.body);
	    	tvBody.setText(formatter.format());
		}else{
			URLImageParser p = new URLImageParser(tvBody, getActivity());
			UrlTagHandler t = new UrlTagHandler(tvBody, getActivity());
			Spanned sp = Html.fromHtml(post.body, p, t);
			tvBody.setText(sp);
			
			rlList.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					int start = etReply.getSelectionStart();
					String toAppend = getString(R.string.reply) + " " + post.id + " " + getString(R.string.floor) + "[@"+ post.username +"]:";
					StringBuilder str = new StringBuilder(etReply.getText().toString());
					str.insert(start, toAppend);
					TagFormatter tagFormatter = new TagFormatter(getActivity());
					tagFormatter.setText(str.toString());
					etReply.setText(tagFormatter.format());
					etReply.setSelection(start + toAppend.length());
				}
			});
		}
		
	}
	
	public LinearLayout addCommentRow(final Post post, boolean local) {
		if(post == null) return null;
	    LayoutInflater inflater =  LayoutInflater.from(getActivity());
		LinearLayout rlList = (LinearLayout)inflater.inflate(R.layout.comment_row, null);
		commentid = post.id;
		setCommentRow(post, local, rlList);
		if(local){
			mcontainer.addView(rlList, 0);
		}else{
			mcontainer.addView(rlList);
		}
		return rlList;
	}
	
	public void postComment(){
		Post post = new Post(Params.CURUSER.id, Params.CURUSER.name, getString(R.string.posting), body);
		final LinearLayout rlList = addCommentRow(post, true);
		final TextView tvAdded = (TextView)rlList.findViewById(R.id.tvAdded);
		rlList.setTag(body);
		MyHttpRequest request = new MyHttpRequest(URLContainer.postComment(torrentid, body));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				Post post = res.getObject(Post.class);
				if(post != null && Util.checkPostResult(getActivity(), post.result) == true){
					setCommentRow(post, false, rlList);
				}else if(post == null){
					Toast.makeText(getActivity(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
					tvAdded.setText(getString(R.string.text_click_resend));
					rlList.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							body = rlList.getTag().toString();
							mcontainer.removeView(rlList);
							postComment();
						}
					});
				}
				btnReply.setEnabled(true);
				etReply.setText("");
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(getActivity()).handleException(e);
				btnReply.setEnabled(true);
				tvAdded.setText(getString(R.string.text_click_resend));
				rlList.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						body = rlList.getTag().toString();
						mcontainer.removeView(rlList);
						postComment();
					}
				});
			}
		}));
	}

	@Override
	public void onDestroy() {
		for(FutureTask<Response> task : httpTasks){
			task.cancel(true);
		}
		super.onDestroy();
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		View mView = getView();
		mView.setFocusable(true);
		mView.setFocusableInTouchMode(true);
		mView.requestFocus();
		return false;
	}
}
