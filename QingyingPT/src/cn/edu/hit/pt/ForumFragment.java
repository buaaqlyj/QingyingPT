package cn.edu.hit.pt;

import java.util.ArrayList;
import java.util.concurrent.FutureTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.http.URLImageParser;
import cn.edu.hit.pt.http.UrlTagHandler;
import cn.edu.hit.pt.http.UserAvatarTask;
import cn.edu.hit.pt.impl.DatabaseUtil;
import cn.edu.hit.pt.impl.ScaleUtil;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.model.News;
import cn.edu.hit.pt.model.Topic;
import cn.edu.hit.pt.widget.CustomScrollView;
import cn.edu.hit.pt.widget.CustomScrollView.OnScrollListener;
import cn.edu.hit.pt.widget.PullToRefreshView;
import cn.edu.hit.pt.widget.PullToRefreshView.OnHeaderRefreshListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.litesuits.android.async.AsyncExecutor;
import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

@SuppressLint("InflateParams") public class ForumFragment extends Fragment implements OnHeaderRefreshListener, OnScrollListener{
	//private String TAG = "ForumFragment";
	private Button showLeft;
	private LinearLayout mcontainer;
	private LinearLayout rlTitle;
	private PullToRefreshView mPullToRefreshView;
	private CustomScrollView svRefresh;
	private View mView;
	private Button btnAddPost;
	private TextView tvTitle;
	private RelativeLayout head_layout;
	private RelativeLayout footer_view = null;
	private RelativeLayout nomore_view = null;
	private LayoutInflater inflater;
	
	private long last_post = 0;
	public static int minclasscreate = 255;
	public static int minclasswrite = 255;
	public boolean news_folded = true;
	public static boolean if_load = false;
	public static String forumid = "latest";
	public static String forumname = "";

	private MyBroadcastReceiver buttonRefreshBroadcastReceiver;
	public ArrayList<FutureTask<Response>> httpTasks = new ArrayList<FutureTask<Response>>();
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		mView = inflater.inflate(R.layout.forumfragment, null);
		head_layout = (RelativeLayout)mView.findViewById(R.id.head_layout);
		mPullToRefreshView = (PullToRefreshView)mView.findViewById(R.id.pull_refresh_view);
		showLeft = (Button) mView.findViewById(R.id.showLeft);
		btnAddPost = (Button)mView.findViewById(R.id.btnAddPost);
		mcontainer = (LinearLayout)mView.findViewById(R.id.topic_list);
		svRefresh = (CustomScrollView)mView.findViewById(R.id.svRefresh);
		rlTitle = (LinearLayout)mView.findViewById(R.id.rlTitle);
		tvTitle = (TextView)mView.findViewById(R.id.tvTitle);
		
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

		Util.setShowLeftButton(showLeft);
		showLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).showLeft();
			}
		});

		btnAddPost.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.putExtra("forumid", forumid);
				intent.putExtra("forumname", forumname);
				intent.setClass(getActivity(), AddPost.class);
				startActivity(intent);
			}
		});
		
		head_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				svRefresh.scrollTo(0, 0);
			}
		});
		
		rlTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ForumsClick sWindow = new ForumsClick(getActivity());
				sWindow.showAtLocation(mView, Gravity.CENTER_HORIZONTAL|Gravity.TOP, 0, ScaleUtil.Dp2Px(getActivity(), 60));
				sWindow.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss() {
						if(if_load == true){
							tvTitle.setText(forumname);
							mPullToRefreshView.setHeaderRefreshing();
							if((minclasscreate <= Params.CURUSER.uclass)||(forumid.equals("latest")))
								btnAddPost.setVisibility(View.VISIBLE);
							else
								btnAddPost.setVisibility(View.GONE);
							if_load = false;
						}
					}
				});
			}
		});
		
		ArrayList<Topic> topics = DatabaseUtil.userDatabase(getActivity()).queryAll(Topic.class);
		setTopics(topics);
		mPullToRefreshView.setHeaderRefreshing();

	}

	@Override
	public void onHeaderRefresh(PullToRefreshView view) {
		last_post = 0;
		loadTopics();
	}

	@Override
	public void onScroll(int scrollY) {
		int rangeY = mcontainer.getMeasuredHeight();
		if((scrollY > rangeY -  svRefresh.getHeight() - 10)&&(footer_view == null)&&(nomore_view == null)&&(last_post != 0)){
			footer_view = (RelativeLayout) inflater.inflate(R.layout.refresh_view, null);
			ImageView iv = (ImageView)footer_view.findViewById(R.id.iv);
			iv.setImageResource(R.drawable.loading);
			AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
			animationLoading.start();
			mcontainer.addView(footer_view);
			loadTopics();
		}
	}

	public void loadTopics(){
		HttpAsyncExecutor asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(getActivity()));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getTopicList(forumid, last_post));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {
	
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				if((last_post == 0)&&(forumid.equals("latest"))){
					final LiteHttpClient client = LiteHttpClient.newApacheHttpClient(getActivity());
					HttpAsyncExecutor asyncExcutor = HttpAsyncExecutor.newInstance(client);
					httpTasks.add(asyncExcutor.execute(new AsyncExecutor.Worker<Response>() {

						@Override
						protected Response doInBackground() {
							MyHttpRequest request = new MyHttpRequest(URLContainer.getNews());
							return client.execute(request);
						}

						@Override
						protected void onPostExecute(Response data) {
							final News news = data.getObject(News.class);
							if(news == null) return;
							News newsCache = DatabaseUtil.userDatabase(getActivity()).queryById(1, News.class);
							if(newsCache == null) newsCache = new News();
							if(news.id > newsCache.id){
							    LayoutInflater inflater =  LayoutInflater.from(getActivity());
								RelativeLayout rlList = (RelativeLayout)inflater.inflate(R.layout.news_row, null);
								TextView tvName = (TextView)rlList.findViewById(R.id.tvTopicName);
								final TextView tvBody = (TextView)rlList.findViewById(R.id.tvTopicBody);
								tvName.setText(news.title);
								tvBody.setText(news.description);
								rlList.setOnClickListener(new OnClickListener() {
									
									@Override
									public void onClick(View arg0) {
										if(news_folded == true){
											URLImageParser p = new URLImageParser(tvBody, getActivity());
											UrlTagHandler t = new UrlTagHandler(tvBody, getActivity());
											Spanned sp = Html.fromHtml(news.body, p, t);
											tvBody.setText(sp);
											tvBody.setMaxLines(200);
											tvBody.setTextColor(getResources().getColor(R.color.text_color_dark));
											news_folded = false;
										}else{
											tvBody.setText(news.description);
											tvBody.setMaxLines(1);
											tvBody.setTextColor(getResources().getColor(R.color.text_color_grey));
											news_folded = true;
										}
									}
								});
								mcontainer.addView(rlList, 0);
							}
							DatabaseUtil.userDatabase(getActivity()).save(news);
							super.onPostExecute(data);
						}
					}));
				}

				try{
					ArrayList<Topic> topics = new ArrayList<Topic>();
					JSONArray jsonArray = new JSONArray(res.getString());
					for (int i = 0; i < jsonArray.length(); i++) {
						Gson gson = new GsonBuilder().create();
						Topic topic = gson.fromJson(jsonArray.get(i).toString(), Topic.class);
						topics.add(topic);
					}
					if(forumid.equals("latest") && last_post == 0){
						DatabaseUtil.userDatabase(getActivity()).deleteAll(Topic.class);
						DatabaseUtil.userDatabase(getActivity()).save(topics);
					}
					setTopics(topics);
					
				}catch(JSONException e){
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
					}else if(last_post != 0){
						nomore_view = (RelativeLayout) inflater.inflate(R.layout.footer_nomore_row, null);
						mcontainer.addView(nomore_view);
					}
					e.printStackTrace();
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
			
		}));
	}
	
	private void setTopics(ArrayList<Topic> list) {
		mPullToRefreshView.onHeaderRefreshComplete();
		if(list == null || list.size() == 0) return;
		if(last_post == 0){
			svRefresh.scrollTo(0, 0);
			mcontainer.removeAllViews();
			nomore_view = null;
		}
		for(final Topic topic : list){
		    final LayoutInflater inflater =  LayoutInflater.from(getActivity());
			final RelativeLayout rlList = (RelativeLayout)inflater.inflate( R.layout.topic_row, null);
			final TextView tvTopicName = (TextView)rlList.findViewById(R.id.tvTopicName);
			TextView tvTopicBody = (TextView)rlList.findViewById(R.id.tvTopicBody);
			TextView tvViews = (TextView)rlList.findViewById(R.id.tvViews);
			TextView tvReplies = (TextView)rlList.findViewById(R.id.tvReplies);
			TextView tvLastPostTime = (TextView)rlList.findViewById(R.id.tvLastPostTime);
			TextView tvLastPostUser = (TextView)rlList.findViewById(R.id.tvLastPostUser);
			ImageView ivUserAvatar = (ImageView)rlList.findViewById(R.id.ivUserAvatar);
			
			tvTopicName.setText(topic.subject);
			tvTopicBody.setText(topic.body);
			tvViews.setText(topic.views + "");
			tvReplies.setText(topic.reply_num + "");
			tvLastPostTime.setText(topic.added);
			if(topic.lastpostuser.equals("")){
				tvLastPostTime.setText(getString(R.string.invalid_user));
				tvLastPostTime.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
			}
			else
				tvLastPostUser.setText(topic.lastpostuser);
		    
			rlList.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent();
					intent.putExtra("topicid", topic.id);
					intent.putExtra("subject", topic.subject);
					intent.putExtra("locked", topic.locked);
					intent.putExtra("minclasswrite", minclasswrite);
					intent.putExtra("reply_num", topic.reply_num);
					intent.setClass(getActivity(), TopicPosts.class);
					startActivity(intent);
					tvTopicName.setTextColor(getResources().getColor(R.color.text_color_grey));
				}
			});
			
			new UserAvatarTask(getActivity(), topic.userid, ivUserAvatar, true);
		    
			ImageView ivSticky = (ImageView)rlList.findViewById(R.id.ivSticky);
			if(topic.sticky.equals("yes"))
				ivSticky.setVisibility(View.VISIBLE);
			else
				ivSticky.setVisibility(View.INVISIBLE);
		    
			ImageView ivLocked = (ImageView)rlList.findViewById(R.id.ivLocked);
			if(topic.locked.equals("yes"))
				ivLocked.setVisibility(View.VISIBLE);
			else
				ivLocked.setVisibility(View.INVISIBLE);
	
			if(topic.read.equals("unread"))
				tvTopicName.setTextColor(getResources().getColor(R.color.text_color_dark));
			else
				tvTopicName.setTextColor(getResources().getColor(R.color.text_color_grey));
				
		    mcontainer.addView(rlList);
		    last_post = topic.lastpost;
		}
		if(list.size() < 20){
			nomore_view = (RelativeLayout)inflater.inflate(R.layout.footer_nomore_row, null);
			mcontainer.addView(nomore_view);
		}
		if(footer_view != null){
			mcontainer.removeView(footer_view);
			footer_view = null;
		}
	}
	
	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		//Log.e("view", String.valueOf(getView()));
		if (this.getView() != null){
			this.getView().setVisibility(menuVisible ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public void onDestroy() {
		for(FutureTask<Response> task : httpTasks){
			task.cancel(true);
		}
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