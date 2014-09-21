package cn.edu.hit.pt;

import java.util.ArrayList;
import java.util.concurrent.FutureTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.http.UserAvatarTask;
import cn.edu.hit.pt.model.Topic;
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

public class SearchTopicFragment extends Fragment implements OnScrollListener{
	private CustomScrollView svRefresh;
	private LinearLayout mcontainer;
	private RelativeLayout footer_view = null;
	private RelativeLayout nomore_view = null;

	private long last_post = 0;
	private int minclasswrite = 255;
	private String keyword;
	private ArrayList<FutureTask<Response>> httpTasks = new ArrayList<FutureTask<Response>>();
	private ArrayList<UserAvatarTask> avatarTasks = new ArrayList<UserAvatarTask>();
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		svRefresh.setOnScrollListener(this);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mView = (View)inflater.inflate(R.layout.search_child_layout, null);
		svRefresh = (CustomScrollView)mView.findViewById(R.id.svRefresh);
		mcontainer = (LinearLayout)mView.findViewById(R.id.mcontainer);
		return mView;
	}
	
	@Override
	public void onScroll(int scrollY) {
		int rangeY = mcontainer.getMeasuredHeight();
		if((scrollY > rangeY -  svRefresh.getHeight() - 10)&&(footer_view == null)&&(nomore_view == null)&&(last_post != 0)){
			footer_view = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.refresh_view, null);
			ImageView iv = (ImageView)footer_view.findViewById(R.id.iv);
			iv.setImageResource(R.drawable.loading);
			AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
			animationLoading.start();
			mcontainer.addView(footer_view);
			loadSearch(keyword);
		}
	}
	
	public void initSearch(String keyword){
		last_post = 0;
		mcontainer.removeAllViews();
		footer_view = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.refresh_view, null);
		ImageView iv = (ImageView)footer_view.findViewById(R.id.iv);
		iv.setImageResource(R.drawable.loading);
		AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
		animationLoading.start();
		mcontainer.addView(footer_view);
		loadSearch(keyword);
	}
	
	public void loadSearch(String keyword){
		if(keyword == null || keyword.equals("")) return;
		this.keyword = keyword;
		HttpAsyncExecutor asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(getActivity()));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getTopicSearchList(keyword, last_post));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {
	
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				if(last_post == 0){
					svRefresh.scrollTo(0, 0);
					mcontainer.removeAllViews();
					nomore_view = null;
					footer_view = null;
				}
				try{
					ArrayList<Topic> topics = new ArrayList<Topic>();
					JSONArray jsonArray = new JSONArray(res.getString());
					for (int i = 0; i < jsonArray.length(); i++) {
						Gson gson = new GsonBuilder().create();
						Topic topic = gson.fromJson(jsonArray.get(i).toString(), Topic.class);
						topics.add(topic);
					}
					setTopics(topics);
					
				}catch(JSONException e){
					if(mcontainer.getChildCount() == 0 || last_post == 0){
						mcontainer.removeAllViews();
						LayoutInflater inflater =  LayoutInflater.from(getActivity());
						LinearLayout rlList = (LinearLayout)inflater.inflate(R.layout.no_search_tip, null);
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(svRefresh.getWidth(), svRefresh.getHeight()-70);
						rlList.setLayoutParams(lp);
						mcontainer.addView(rlList);
					}else{
						nomore_view = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.footer_nomore_row, null);
						mcontainer.addView(nomore_view);
					}
				}
			}
	
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(getActivity()).handleException(e);
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
		if(list == null || list.size() == 0) return;
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
			
			avatarTasks.add(new UserAvatarTask(getActivity(), topic.userid, ivUserAvatar, true));
		    
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
			nomore_view = (RelativeLayout)getActivity().getLayoutInflater().inflate(R.layout.footer_nomore_row, null);
			mcontainer.addView(nomore_view);
		}
		if(footer_view != null){
			mcontainer.removeView(footer_view);
			footer_view = null;
		}
	}

	@Override
	public void onDestroy() {
		for (FutureTask<Response> task : httpTasks) {
			task.cancel(true);
		}
		for (UserAvatarTask task : avatarTasks) {
			task.cancel();
		}
		super.onDestroy();
	}
	
}
