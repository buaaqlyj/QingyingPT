package cn.edu.hit.pt;

import java.util.ArrayList;
import java.util.concurrent.FutureTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
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

public class SearchUserFragment extends Fragment implements OnScrollListener{
	private CustomScrollView svRefresh;
	private LinearLayout mcontainer;
	private RelativeLayout footer_view = null;
	private RelativeLayout nomore_view = null;

	private long last_userid = 0;
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
		if((scrollY > rangeY -  svRefresh.getHeight() - 10)&&(footer_view == null)&&(nomore_view == null)&&(last_userid != 0)){
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
		last_userid = 0;
		mcontainer.removeAllViews();
		footer_view = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.refresh_view, null);
		ImageView iv = (ImageView)footer_view.findViewById(R.id.iv);
		iv.setImageResource(R.drawable.loading);
		AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
		animationLoading.start();
		mcontainer.addView(footer_view);
		loadSearch(keyword);
	}
	
	public void loadSearch(final String keyword){
		if(keyword == null || keyword.equals("")) return;
		this.keyword = keyword;		
		HttpAsyncExecutor asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(getActivity()));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getSearchUsersURL(keyword, last_userid));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				if(last_userid == 0){
					svRefresh.scrollTo(0, 0);
					mcontainer.removeAllViews();
					nomore_view = null;
					footer_view = null;
				}
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
			}
			
		}));
	}

	public void setContactList(ArrayList<Contact> list){
		if(list == null || list.size() == 0){
			LinearLayout rlList = (LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.no_friend_tip, null);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(svRefresh.getWidth(), svRefresh.getHeight()-70);
			rlList.setLayoutParams(lp);
			mcontainer.addView(rlList);
			return;
		}
		for(final User user : list){
			final LinearLayout rlList = (LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.contact_row, null);
			TextView tvUsername = (TextView)rlList.findViewById(R.id.tvUsername);
			TextView tvTitle = (TextView)rlList.findViewById(R.id.tvTitle);
			final ImageView ivUserAvatar = (ImageView)rlList.findViewById(R.id.ivUserAvatar);
			tvUsername.setText(user.name);
			tvTitle.setText(user.title);

		    rlList.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent();
					intent.putExtra("sender_name", user.name);
					intent.putExtra("sender", user.id);
					intent.setClass(getActivity(), ViewMail.class);
					startActivity(intent);
				}
			});

		    avatarTasks.add(new UserAvatarTask(getActivity(), user.id, ivUserAvatar, true));
			
		    mcontainer.addView(rlList);
		}
		
		if(list.size() < 20){
			nomore_view = (RelativeLayout)getActivity().getLayoutInflater().inflate(R.layout.footer_nomore_row, null);
			mcontainer.addView(nomore_view);
		}else{
			nomore_view = null;
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