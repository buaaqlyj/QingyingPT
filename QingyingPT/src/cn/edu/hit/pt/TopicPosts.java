package cn.edu.hit.pt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.FutureTask;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
import cn.edu.hit.pt.http.UploadAsyncTask;
import cn.edu.hit.pt.http.UploadAsyncTask.onUploadFinishedListener;
import cn.edu.hit.pt.http.UrlTagHandler;
import cn.edu.hit.pt.http.UserAvatarTask;
import cn.edu.hit.pt.impl.ScaleUtil;
import cn.edu.hit.pt.impl.TagFormatter;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.model.Attachment;
import cn.edu.hit.pt.model.Post;
import cn.edu.hit.pt.model.Topic;
import cn.edu.hit.pt.widget.CustomScrollView;
import cn.edu.hit.pt.widget.CustomScrollView.OnScrollListener;
import cn.edu.hit.pt.widget.ImplementsBar;
import cn.edu.hit.pt.widget.PullToRefreshView;
import cn.edu.hit.pt.widget.PullToRefreshView.OnHeaderRefreshListener;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.request.content.UrlEncodedFormBody;
import com.litesuits.http.request.param.HttpMethod;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

@SuppressLint({ "InflateParams", "RtlHardcoded" })
public class TopicPosts extends SwipeBackActivity implements OnHeaderRefreshListener, OnScrollListener{
	//private String TAG = "TopicPost";
	public PullToRefreshView mPullToRefreshView;
	private LinearLayout mcontainer;
	private CustomScrollView svRefresh;
	private Button addSmile;
	private Button addOther;
	private RelativeLayout bottom_layout;
	private Button btnReturn;
	private Button addReply;
	private Button btnMenu;
	private EditText etPost;
	private TextView tvSubject;
	private ImplementsBar implementsBar;
	private RelativeLayout footer_view = null;
	private RelativeLayout nomore_view = null;
	
	private int minclasswrite;
	private int reply_num = 0;
	private int page_num = 0;
	private String locked = "";
	private long topicid;
	private String subject = "";
	private String body = "";

	public String order = "";
	public String author = "";
	public int floor_total = 0;
	public long last_postid = 0;
	public boolean if_last_post = false;
	
	private HttpAsyncExecutor asyncExecutor;
	private ArrayList<FutureTask<Response>> httpTasks = new ArrayList<FutureTask<Response>>();
	private ArrayList<UserAvatarTask> avatarTasks = new ArrayList<UserAvatarTask>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.topic_posts);
		
		SwipeBackLayout mSwipeBackLayout;
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
		
		addSmile = (Button)findViewById(R.id.addSmile);
		addReply = (Button)findViewById(R.id.addReply);
		btnMenu = (Button)findViewById(R.id.btnMenu);
		etPost = (EditText)findViewById(R.id.et_post);
	 	mcontainer = (LinearLayout)findViewById(R.id.container);
		svRefresh=(CustomScrollView)findViewById(R.id.svRefresh);
		mPullToRefreshView = (PullToRefreshView)findViewById(R.id.pull_refresh_view);
		bottom_layout = (RelativeLayout)findViewById(R.id.bottom_layout);
		tvSubject = (TextView)findViewById(R.id.tvSubject);
		addOther = (Button)findViewById(R.id.addOther);
		
		implementsBar = (ImplementsBar)findViewById(R.id.implementsBar);
		implementsBar.setFragmentTransaction(getSupportFragmentManager());
		implementsBar.setTargetView(etPost);
		
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		mPullToRefreshView.deleteFooter();
		svRefresh.setOnScrollListener(this);
		
		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(this));
		
		Intent intent = getIntent();
		topicid = intent.getLongExtra("topicid", 0);
		subject = intent.getStringExtra("subject");
		locked = intent.getStringExtra("locked");
		minclasswrite = intent.getIntExtra("minclasswrite", 255);
		
		if(intent.getStringExtra("reply_num") != null)
			reply_num = Integer.parseInt(intent.getStringExtra("reply_num"));
		
		initInterface();
		loadPostList();
		
		btnReturn = (Button)findViewById(R.id.btnReturn);
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
		
		btnMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				TopicMenu sWindow = new TopicMenu(TopicPosts.this, topicid, subject);
				sWindow.showAtLocation(btnMenu, Gravity.RIGHT|Gravity.TOP, 0, ScaleUtil.Dp2Px(TopicPosts.this, 75));
			}
		});
		
//		LinearLayout rlPage = (LinearLayout)findViewById(R.id.rlPage);
//		rlPage.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if(reply_num > 0){
//					page_num = reply_num/20 + 1;
//					String[] pages = new String[page_num];
//					for(int i=1; i<=page_num; i++){
//						pages[i-1] = (getString(R.string.num)+i+getString(R.string.page));
//					}
//					new AlertDialog.Builder(new ContextThemeWrapper(TopicPosts.this, R.style.AlertDialogListCustom))
//					.setAdapter(new ArrayAdapter<String>(TopicPosts.this, R.layout.alertdialog_list, pages), new DialogInterface.OnClickListener() {
//
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							page_num = which + 1;
//							last_postid = 0;
//							order = "";
//							ivOnlyAuthor.setImageDrawable(getResources().getDrawable(R.drawable.button_only_author));
//							tvOnlyAuthor.setText(R.string.only_author);
//							author = "";
//							ivOnlyAuthor.setImageDrawable(getResources().getDrawable(R.drawable.button_only_author));
//							tvOnlyAuthor.setText(R.string.only_author);
//							ivReverseOrder.setImageDrawable(getResources().getDrawable(R.drawable.button_order));
//							tvReverseOrder.setText(R.string.reverse_order);
//							mPullToRefreshView.setHeaderRefreshing();
//						}
//					}).show();
//					
//			        setAnimation();
//				}
//			}
//		});
		
		addReply.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				implementsBar.hide();
				body = etPost.getText().toString();
				etPost.setText("");
				sendPost();
			}
		});
	}

	public void onHeaderRefresh(PullToRefreshView view) {
		last_postid = 0;
		loadPostList();
	}
	
	public void loadPostList() {
		MyHttpRequest request = new MyHttpRequest(URLContainer.getPostList(topicid, last_postid, order, author, page_num));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {
			
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				if(last_postid == 0){
					svRefresh.scrollTo(0, 0);
					floor_total = 0;
					if_last_post = true;
					mcontainer.removeAllViews();
					nomore_view = null;
					mPullToRefreshView.onHeaderRefreshComplete();
				}
				Topic topic = res.getObject(Topic.class);
				if(topic != null){
					subject = topic.subject;
					locked = topic.locked;
					minclasswrite = topic.minclasswrite;
					reply_num = topic.reply_num;
					
					if(topic.posts!=null && topic.posts.size() > 0){
						for(Post post : topic.posts){
							addPostRow(post, false);
						}
					}
					if(topic == null || topic.posts == null || topic.posts.size() < 20){
						nomore_view = (RelativeLayout)getLayoutInflater().inflate(R.layout.footer_nomore_row, null);
						mcontainer.addView(nomore_view);
					}
					initInterface();
				}else{
					if(footer_view != null){
						mcontainer.removeView(footer_view);
						footer_view = null;
					}
					if(mcontainer.getChildCount() == 0){
						LinearLayout rlList = (LinearLayout)getLayoutInflater().inflate(R.layout.unknown_error_tip, null);
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(svRefresh.getWidth(), svRefresh.getHeight()-70);
						rlList.setLayoutParams(lp);
						mcontainer.addView(rlList);
					}else if(last_postid != 0){
						nomore_view = (RelativeLayout)getLayoutInflater().inflate(R.layout.footer_nomore_row, null);
						mcontainer.addView(nomore_view);
					}
				}
				if(footer_view != null){
					mcontainer.removeView(footer_view);
					footer_view = null;
				}
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(TopicPosts.this).handleException(e);
				mPullToRefreshView.onHeaderRefreshComplete();
			}
		}));
	}
	
	public void setPostRow(final Post post, boolean local, RelativeLayout rlList){
		if(post == null || rlList == null) return;
		TextView tvUsername = (TextView)rlList.findViewById(R.id.tvUsername);
		TextView tvAdded = (TextView)rlList.findViewById(R.id.tvAdded);
		TextView tvFloor = (TextView)rlList.findViewById(R.id.tvFloor);
		TextView tvBody = (TextView)rlList.findViewById(R.id.tvBody);
		TextView tvLike = (TextView)rlList.findViewById(R.id.tvLike);
		ImageView ivUserAvatar = (ImageView)rlList.findViewById(R.id.ivUserAvatar);
		ImageView ivLike = (ImageView)rlList.findViewById(R.id.ivLike);
		LinearLayout rlReply = (LinearLayout)rlList.findViewById(R.id.rlReply);
		final LinearLayout rlLike = (LinearLayout)rlList.findViewById(R.id.rlLike);
		if(post.username.equals("")){
			tvUsername.setText(getString(R.string.invalid_user));
			tvUsername.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
		}
		else
			tvUsername.setText(post.username);
		tvAdded.setText(post.added);

		tvLike.setTag(post.likes);
		if(post.likes > 0){
			tvLike.setText((post.liked == 0?getString(R.string.like):getString(R.string.liked)) + "(" + post.likes + ")");
		}
		if(post.liked == 1){
			tvLike.setTextColor(getResources().getColor(R.color.bg_color_red));
			ivLike.setImageDrawable(getResources().getDrawable(R.drawable.button_liked));
		}

		rlLike.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				likePost(rlLike, post.id);
				rlLike.setEnabled(false);
			}
		});
		
		if((order.equals("reverse"))&&(reply_num > 0)&&(floor_total == 0)&&(if_last_post==true)){
			floor_total = reply_num;
			if_last_post = false;
		}
		if(page_num > 0){
			floor_total = (page_num - 1)*20;
			page_num = 0;
		}
	    switch (floor_total) {
			case 0:
				tvFloor.setText(R.string.floor_lz);
				break;
			case 1:
				tvFloor.setText(R.string.floor_sf);
				break;
			case 2:
				tvFloor.setText(R.string.floor_bd);
				break;
			case 3:
				tvFloor.setText(R.string.floor_db);
				break;
			case 4:
				tvFloor.setText(R.string.floor_xsd);
				break;

			default:
				tvFloor.setText((floor_total + 1) + getString(R.string.floor));
				break;
		}

		rlReply.setOnClickListener(new OnClickListener() {
			int floor = floor_total + 1;
			
			@Override
			public void onClick(View arg0) {
				int start = etPost.getSelectionStart();
				String toAppend = getString(R.string.reply) + " " + floor + " " + getString(R.string.floor) + "[@"+ post.username +"]:";
				StringBuilder str = new StringBuilder(etPost.getText().toString());
				str.insert(start, toAppend);
				TagFormatter tagFormatter = new TagFormatter(TopicPosts.this);
				tagFormatter.setText(str.toString());
				etPost.setText(tagFormatter.format());
				etPost.setSelection(start + toAppend.length());
			}
		});

	    avatarTasks.add(new UserAvatarTask(TopicPosts.this, post.userid, ivUserAvatar, true));
	    
	    if(local == true){
	    	TagFormatter formatter = new TagFormatter(this, post.body);
	    	tvBody.setText(formatter.format());
	    }else{
		    if(order.equals(""))
		    	floor_total++;
		    else
		    	floor_total--;
			URLImageParser p_text = new URLImageParser(tvBody, this);
			UrlTagHandler t_text = new UrlTagHandler(tvBody, this);
			Spanned sp_text = Html.fromHtml(post.body, p_text, t_text);
			tvBody.setText(sp_text);
	    }
	}
	
	public RelativeLayout addPostRow(final Post post, boolean local) {
		if(post == null) return null;
	    LayoutInflater inflater =  LayoutInflater.from(getBaseContext());
		RelativeLayout rlList = (RelativeLayout)inflater.inflate( R.layout.post_row, null);
	    setPostRow(post, local, rlList);
	    if(local == true){
			mcontainer.addView(rlList, mcontainer.getChildCount()-1);
	    }else{
	    	mcontainer.addView(rlList);
		    last_postid = post.id;
	    }
	    rlList.setOnClickListener(null);
	    return rlList;
	}
	
	public void sendPost(){
		addReply.setEnabled(false);
		File file = (File) etPost.getTag();
		if(file != null){
			Map<String, String> params = new HashMap<String, String>();
			params.put("type", "attachment");
			UploadAsyncTask uploadTask = new UploadAsyncTask(this, URLContainer.getUploadURL(), params, file);
			uploadTask.setOnUploadFinishedListener(new onUploadFinishedListener() {
				
				@Override
				public void onFinished(Attachment result) {
					if(result.error == 0){
						etPost.setTag(null);
						implementsBar.clearCounter();
						body = body + "\n[attach]" + result.dlkey + "[/attach]";
						replyPost();
					}else{
						addReply.setEnabled(true);
					}
				}
			});
			uploadTask.execute();
		}else if(!body.equals("")){
			replyPost();
		}else{
			addReply.setEnabled(true);
		}
	}
	
	public void replyPost() {
		Post post = new Post(Params.CURUSER.id, Params.CURUSER.name, getString(R.string.posting), body);
		final RelativeLayout rlList = addPostRow(post, true);
		final TextView tvAdded = (TextView)rlList.findViewById(R.id.tvAdded);
		rlList.setTag(body);
		MyHttpRequest request = new MyHttpRequest(URLContainer.getPostURL());
		request.setMethod(HttpMethod.Post);
        LinkedList<NameValuePair> pList = new LinkedList<NameValuePair>();
        pList.add(new NameValuePair("id", topicid + ""));
        pList.add(new NameValuePair("type", "reply"));
        pList.add(new NameValuePair("body", body));
        request.setHttpBody(new UrlEncodedFormBody(pList, "UTF-8"));
        httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {
			
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				Post post = res.getObject(Post.class);
				if(post != null && Util.checkPostResult(TopicPosts.this, post.result) == true){
				    if(nomore_view != null){
						setPostRow(post, false, rlList);
				    }
				}else if(post == null){
					Toast.makeText(TopicPosts.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
					tvAdded.setText(getString(R.string.text_click_resend));
					rlList.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							body = rlList.getTag().toString();
							mcontainer.removeView(rlList);
							replyPost();
						}
					});
				}
				addReply.setEnabled(true);
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(TopicPosts.this).handleException(e);
				addReply.setEnabled(true);
				tvAdded.setText(getString(R.string.text_click_resend));
				rlList.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						body = rlList.getTag().toString();
						mcontainer.removeView(rlList);
						replyPost();
					}
				});
			}
		}));
	}
	
	public void likePost(final LinearLayout rlLike, long postid) {
		MyHttpRequest request = new MyHttpRequest(URLContainer.likePost(postid));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				try {
					JSONObject json = new JSONObject(res.getString());
					int liked = json.getInt("liked");
					TextView tvLike = (TextView)rlLike.findViewById(R.id.tvLike);
					ImageView ivLike = (ImageView)rlLike.findViewById(R.id.ivLike);
					int likes = Integer.parseInt(tvLike.getTag().toString());
					if(liked == 0){
						likes = likes - 1;
						tvLike.setTextColor(getResources().getColor(R.color.button_post_text));
						ivLike.setImageDrawable(getResources().getDrawable(R.drawable.button_like));
					}else if(liked == 1){
						likes = likes + 1;
						tvLike.setTextColor(getResources().getColor(R.color.bg_color_red));
						ivLike.setImageDrawable(getResources().getDrawable(R.drawable.button_liked));
					}
					if(likes > 0){
						tvLike.setText((liked == 0?getString(R.string.like):getString(R.string.liked)) + "(" + likes + ")");
					}else{
						tvLike.setText(getString(R.string.like));
					}
					tvLike.setTag(likes);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				rlLike.setEnabled(true);
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(TopicPosts.this).handleException(e);
			}
			
		}));
	}
	 
	private void initInterface(){
		tvSubject.setText(subject);
		
		if((Params.CURUSER.uclass >= minclasswrite)&&(!locked.equals("yes"))){
			bottom_layout.setVisibility(View.VISIBLE);
		}else{
			bottom_layout.setVisibility(View.GONE);
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

	@Override
	public void onScroll(int scrollY) {
		int rangeY = mcontainer.getMeasuredHeight();
		if((scrollY > rangeY -  svRefresh.getHeight() - 10)&&(footer_view == null)&&(nomore_view == null)&&(last_postid != 0)){
			footer_view = (RelativeLayout) getLayoutInflater().inflate(R.layout.refresh_view, null);
			ImageView iv = (ImageView)footer_view.findViewById(R.id.iv);
			iv.setImageResource(R.drawable.loading);
			AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
			animationLoading.start();
			mcontainer.addView(footer_view);
			loadPostList();
		}
	}
}
