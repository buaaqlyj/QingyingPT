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
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.TorrentImage;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.model.Torrent;
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

public class SearchTorrentFragment extends Fragment implements OnScrollListener{
	private CustomScrollView svRefresh;
	private LinearLayout mcontainer;
	private RelativeLayout footer_view = null;
	private RelativeLayout nomore_view = null;
	private View last_divider = null;

	private int margin;
	private long last_id;
	private String keyword;
	private String last_torrent_added = "";
	private ArrayList<FutureTask<Response>> httpTasks = new ArrayList<FutureTask<Response>>();
	private ArrayList<TorrentImage> torrentImageTasks = new ArrayList<TorrentImage>();
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		svRefresh.setOnScrollListener(this);
		margin = getResources().getDimensionPixelSize(R.dimen.padding_medium);
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
		if((scrollY > rangeY -  svRefresh.getHeight() - 10)&&(footer_view == null)&&(nomore_view == null)&&(last_id != 0)){
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
		last_id = 0;
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
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new NameValuePair("search", keyword));
		HttpAsyncExecutor asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(getActivity()));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getTorrent(params));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				if(last_id == 0){
					svRefresh.scrollTo(0, 0);
					mcontainer.removeAllViews();
					last_torrent_added = "";
					nomore_view = null;
					footer_view = null;
				}
				try{
					ArrayList<Torrent> torrents = new ArrayList<Torrent>();
					JSONArray jsonArray = new JSONArray(res.getString());
					for (int i = 0; i < jsonArray.length(); i++) {
						Gson gson = new GsonBuilder().create();
						Torrent torrent = gson.fromJson(jsonArray.get(i).toString(), Torrent.class);
						torrents.add(torrent);
					}
					setTorrents(torrents);					
				}catch(JSONException e){
					if(mcontainer.getChildCount() == 0 || last_id == 0){
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
	
	public void setTorrents(ArrayList<Torrent> list) {
		if(list == null || list.size() == 0) return;
		if(last_divider != null)
			mcontainer.removeView(last_divider);
		
		for(final Torrent torrent : list){
		    LayoutInflater inflater =  LayoutInflater.from(getActivity());
	    	String added = Util.TimeStamp2Date(torrent.added, "yyyy-MM-dd");
		    if(!added.equals(last_torrent_added)){
		    	if(last_id != 0){
		    		View divider = new View(getActivity());
		    		LinearLayout.LayoutParams lp_divider = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 2);
		    		divider.setBackgroundResource(R.color.border_grey);
		    		lp_divider.setMargins(margin, 0, margin, 0);
		    		divider.setLayoutParams(lp_divider);
		    		mcontainer.addView(divider);
		    	}
				LinearLayout rlTime = (LinearLayout)inflater.inflate(R.layout.torrent_time_row, null);
				TextView tvTime = (TextView)rlTime.findViewById(R.id.tvTime);
				tvTime.setText(added);
				mcontainer.addView(rlTime);
		    	last_torrent_added = added;
		    }
			final RelativeLayout rlList = (RelativeLayout)inflater.inflate(R.layout.torrent_row, null);
			TextView tvDescription = (TextView)rlList.findViewById(R.id.tvDescription);
			TextView tvInfo = (TextView)rlList.findViewById(R.id.tvInfo);
			ImageView ivThumb = (ImageView)rlList.findViewById(R.id.ivThumb);
			LinearLayout rlSPstate = (LinearLayout)rlList.findViewById(R.id.sp_state);
			Util.set_sp_layout(rlSPstate, torrent.sp_state);
			tvDescription.setText(torrent.small_descr);
		    String size = Util.format_size(torrent.size);
			tvInfo.setText(size);

			torrentImageTasks.add(new TorrentImage(getActivity(), ivThumb, torrent.torrentid, TorrentImage.TYPE_SMALL));
			
			rlList.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent();
					intent.putExtra("id", torrent.torrentid);
					intent.putExtra("title", torrent.small_descr);
					intent.setClass(getActivity(), TorrentActivity.class);
					startActivity(intent);
				}
			});
			
			rlList.setTag(torrent.torrentid);
			
			mcontainer.addView(rlList);
			last_id = torrent.torrentid;
		}
		last_divider = new View(getActivity());
		LinearLayout.LayoutParams lp_divider = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 2);
		last_divider.setBackgroundResource(R.color.border_grey);
		lp_divider.setMargins(margin, 0, margin, 0);
		last_divider.setLayoutParams(lp_divider);
		mcontainer.addView(last_divider);
		
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
		for (TorrentImage task : torrentImageTasks) {
			task.cancel();
		}
		super.onDestroy();
	}
}
