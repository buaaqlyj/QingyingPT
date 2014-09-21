package cn.edu.hit.pt;

import java.util.ArrayList;
import java.util.concurrent.FutureTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

public class TorrentOtherCopy extends Fragment{
	private String TAG = "TorrentCatSrc";
	private LayoutInflater inflater;
	private LinearLayout mcontainer;
	private RelativeLayout footer_view = null;

	public long torrentid;
	public long imdbid;
	public int copyCount;
	private long last_torrent = 0;
	private boolean nomore = false;
	
	private HttpAsyncExecutor asyncExecutor;
	private FutureTask<Response> httpTask;
	
	public void setArgs(long torrentid, long imdbid, int copyCount) {
		this.torrentid = torrentid;
		this.imdbid = imdbid;
		this.copyCount = copyCount;
	}
	
	public boolean canLoadCopies() {
		if(copyCount > 0 && nomore == false && footer_view == null)
			return true;
		else
			return false;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.inflater = inflater;
		View mView = inflater.inflate(R.layout.torrent_othercopy, null);
		mcontainer = (LinearLayout)mView.findViewById(R.id.mcontainer);
		return mView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(getActivity()));
		if(copyCount == 0){
			mcontainer.removeAllViews();
			TextView tv = new TextView(getActivity());
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 100);
			tv.setLayoutParams(lp);
			tv.setText(getActivity().getString(R.string.no_othercopy));
			tv.setGravity(Gravity.CENTER);
			mcontainer.addView(tv);
		}
	}
	
	public void loadTorrentList() {
		if(footer_view == null){
			footer_view = (RelativeLayout)inflater.inflate(R.layout.refresh_view, null);
			ImageView iv = (ImageView)footer_view.findViewById(R.id.iv);
			iv.setImageResource(R.drawable.loading);
			AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
			animationLoading.start();
			mcontainer.addView(footer_view);
		}
		ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new NameValuePair("action", "other_copy"));
		param.add(new NameValuePair("id", torrentid + ""));
		param.add(new NameValuePair("imdb_id", imdbid+""));
		param.add(new NameValuePair("last_torrent", last_torrent+""));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getTorrent(param));
		httpTask = asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				if(last_torrent == 0){
					mcontainer.removeAllViews();
					nomore = false;
				}
				try{
					JSONArray arr = new JSONArray(res.getString());
					for (int i = 0; i < arr.length(); i++) {
						Gson gson = new GsonBuilder().create();
						final Torrent torrent = gson.fromJson(arr.get(i).toString(), Torrent.class);
					    LayoutInflater inflater =  LayoutInflater.from(getActivity());
						RelativeLayout rlList = (RelativeLayout)inflater.inflate(R.layout.othercopy_row, null);
						TextView tvDescription = (TextView)rlList.findViewById(R.id.tvDescription);
						TextView tvInfo = (TextView)rlList.findViewById(R.id.tvInfo);
						ImageView ivThumb = (ImageView)rlList.findViewById(R.id.ivThumb);
						
						LinearLayout rlSPstate = (LinearLayout)rlList.findViewById(R.id.sp_state);
						Util.set_sp_layout(rlSPstate, torrent.sp_state);
						
						tvDescription.setText(torrent.small_descr);
						tvInfo.setText(Util.format_size(torrent.size));

						new TorrentImage(getActivity(), ivThumb, torrent.torrentid, TorrentImage.TYPE_SMALL);
						
						rlList.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								Intent intent = new Intent();
								intent.putExtra("id", torrent.torrentid);
								intent.putExtra("title", torrent.small_descr);
								intent.setClass(getActivity(), TorrentActivity.class);
								getActivity().startActivity(intent);
							}
						});
						
					    mcontainer.addView(rlList);
						last_torrent = torrent.torrentid;
					}
					if(arr.length() < 20){
						nomore = true;
					}
				} catch (JSONException e) {
					Log.e(TAG, e.toString());
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
		});
	}

	@Override
	public void onDestroy() {
		if(httpTask != null) httpTask.cancel(true);
		super.onDestroy();
	}
}
