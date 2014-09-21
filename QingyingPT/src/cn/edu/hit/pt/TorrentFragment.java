package cn.edu.hit.pt;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.TorrentImage;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.impl.CustomAlert;
import cn.edu.hit.pt.impl.DatabaseUtil;
import cn.edu.hit.pt.impl.ScaleUtil;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.model.Torrent;
import cn.edu.hit.pt.model.TorrentExtended;
import cn.edu.hit.pt.widget.CustomDialog;
import cn.edu.hit.pt.widget.CustomScrollView;
import cn.edu.hit.pt.widget.CustomScrollView.OnScrollListener;
import cn.edu.hit.pt.widget.PointerTip;
import cn.edu.hit.pt.widget.PullToRefreshView;
import cn.edu.hit.pt.widget.PullToRefreshView.OnHeaderRefreshListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

@SuppressLint("InflateParams") public class TorrentFragment extends Fragment implements OnHeaderRefreshListener, OnScrollListener{
	private String TAG = "TorrentFragment";
	private LayoutInflater inflater;
	private View mView;
	private Button showLeft;
	private TextView tvTitle;
	private LinearLayout rlTitle;
	private RelativeLayout head_layout;
	private PullToRefreshView mPullToRefreshView;
	private LinearLayout mcontainer;
	private LinearLayout outContainer;
	private CustomScrollView svRefresh;
	private View last_divider = null;
	private RelativeLayout footer_view = null;
	private RelativeLayout nomore_view = null;
	private LinearLayout customAlert = null;
	
	private long last_id = 0;
	private String last_torrent_added = "";
	public static boolean if_load = false;
	public static String action = "new";
	public static int categoryid = 0;
	public static String categoryname = "";

	public int margin;
	public LayoutParams lParams;
	private HttpAsyncExecutor asyncExecutor;
	private MyBroadcastReceiver buttonRefreshBroadcastReceiver;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.torrentfragment, null);
		this.inflater = inflater;
		showLeft = (Button) mView.findViewById(R.id.showLeft);
		tvTitle = (TextView)mView.findViewById(R.id.tvTitle);
		rlTitle = (LinearLayout)mView.findViewById(R.id.rlTitle);
		mcontainer = (LinearLayout)mView.findViewById(R.id.torrent_list);
		outContainer = (LinearLayout)mView.findViewById(R.id.outContainer);
		svRefresh = (CustomScrollView)mView.findViewById(R.id.svRefresh);
		mPullToRefreshView = (PullToRefreshView)mView.findViewById(R.id.pull_refresh_view);
		head_layout = (RelativeLayout)mView.findViewById(R.id.head_layout);
		return mView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		((MainActivity) getActivity()).mainFragmentPagerAdapter.restoreState(this);
		
		buttonRefreshBroadcastReceiver = new MyBroadcastReceiver();
		getActivity().registerReceiver(buttonRefreshBroadcastReceiver, new IntentFilter(MainActivity.ACTION_REFRESH_BUTTON));
		
		margin = getResources().getDimensionPixelSize(R.dimen.padding_medium);
		lParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(getActivity()));
		
		svRefresh.setOnScrollListener(this);
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		mPullToRefreshView.deleteFooter();
		
		ArrayList<Torrent> torrents = DatabaseUtil.userDatabase(getActivity()).queryAll(Torrent.class);
		setTorrents(torrents);

		Util.setShowLeftButton(showLeft);
		showLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).showLeft();
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
				TorrentCats sWindow = new TorrentCats(getActivity());
				sWindow.showAtLocation(mView, Gravity.CENTER_HORIZONTAL|Gravity.TOP, 0, ScaleUtil.Dp2Px(getActivity(), 60));
				sWindow.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss() {
						if(if_load == true){
							tvTitle.setText(categoryname);
							mPullToRefreshView.setHeaderRefreshing();
							if_load = false;
						}
					}
				});
			}
		});
		
		if(!Params.tips.remotedownload_here){
			PointerTip sWindow = new PointerTip(getActivity());
			sWindow.setText(getString(R.string.remote_download_here));
			sWindow.showAtLocation(mView, Gravity.CENTER_HORIZONTAL|Gravity.TOP, 0, ScaleUtil.Dp2Px(getActivity(), 80));
			Params.tips.remotedownload_here = true;
			DatabaseUtil.userDatabase(getActivity()).save(Params.tips);
		}
		mPullToRefreshView.setHeaderRefreshing();

	}
	
	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		if (this.getView() != null){
			this.getView().setVisibility(menuVisible ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public void onScroll(int scrollY) {
		int rangeY = mcontainer.getMeasuredHeight();
		if((scrollY > rangeY -  svRefresh.getHeight() - 10)&&(footer_view == null)&&(nomore_view == null)){
			footer_view = (RelativeLayout) inflater.inflate(R.layout.refresh_view, null);
			ImageView iv = (ImageView)footer_view.findViewById(R.id.iv);
			iv.setImageResource(R.drawable.loading);
			AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
			animationLoading.start();
			mcontainer.addView(footer_view);
			loadTorrent();
		}
	}

	@Override
	public void onHeaderRefresh(PullToRefreshView view) {
		last_id = 0;
		loadTorrent();
	}
	
	public void loadTorrent(){
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new NameValuePair("action", action));
		params.add(new NameValuePair("id", last_id+""));
		if(action.equals(""))
			params.add(new NameValuePair("category", categoryid+""));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getTorrent(params));
		asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				mPullToRefreshView.onHeaderRefreshComplete();
				try{
					if((action.equals("remote"))&&(customAlert == null || customAlert.getVisibility() == View.GONE)&&(last_id == 0)){
						if(Params.tips.remotedownload == false){
							final CustomAlert alert = new CustomAlert(getActivity());
							customAlert = alert.init(CustomAlert.TYPE_INFO, getString(R.string.howtoRemoteDownload));
							customAlert.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View arg0) {
									alert.close();
									Intent intent = new Intent();
									intent.putExtra("title", getString(R.string.remoteDownload));
									intent.putExtra("doc", "remoteDownload");
									intent.setClass(getActivity(), DocViewer.class);
									startActivity(intent);
									Params.tips.remotedownload = true;
									DatabaseUtil.userDatabase(getActivity()).save(Params.tips);
								}
							});
							outContainer.addView(customAlert, 0);
						}
					}else if (!action.equals("remote") && customAlert != null) {
						customAlert.setVisibility(View.GONE);
					}
					if(action.equals("remote")||action.equals("seeding")||action.equals("leeching")){
						ArrayList<TorrentExtended> torrents = new ArrayList<TorrentExtended>();
						JSONArray jsonArray = new JSONArray(res.getString());
						for (int i = 0; i < jsonArray.length(); i++) {
							Gson gson = new GsonBuilder().create();
							TorrentExtended torrent = gson.fromJson(jsonArray.get(i).toString(), TorrentExtended.class);
							torrents.add(torrent);
						}
						setExTorrents(torrents);
					}else{
						ArrayList<Torrent> torrents = new ArrayList<Torrent>();
						JSONArray jsonArray = new JSONArray(res.getString());
						for (int i = 0; i < jsonArray.length(); i++) {
							Gson gson = new GsonBuilder().create();
							Torrent torrent = gson.fromJson(jsonArray.get(i).toString(), Torrent.class);
							torrents.add(torrent);
						}
						if(action.equals("new") && last_id == 0){
							DatabaseUtil.userDatabase(getActivity()).deleteAll(Torrent.class);
							DatabaseUtil.userDatabase(getActivity()).save(torrents);
						}
						setTorrents(torrents);
					}
					
				}catch(JSONException e){
					if(footer_view != null){
						mcontainer.removeView(footer_view);
						footer_view = null;
					}
					if(mcontainer.getChildCount() == 0 || last_id == 0){
						mcontainer.removeAllViews();
						LayoutInflater inflater =  LayoutInflater.from(getActivity());
						LinearLayout rlList = null;
						if(res.getString().equals("null\n")||res.getString().equals("null")){
							if(action.equals("bookmark"))
								rlList = (LinearLayout)inflater.inflate(R.layout.no_bookmark_tip, null);
							else
								rlList = (LinearLayout)inflater.inflate(R.layout.no_task_tip, null);
						}
						if(rlList!=null){
							LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(svRefresh.getWidth(), svRefresh.getHeight()-70);
							rlList.setLayoutParams(lp);
							mcontainer.addView(rlList);
						}
					}else{
						nomore_view = (RelativeLayout) inflater.inflate(R.layout.footer_nomore_row, null);
						mcontainer.addView(nomore_view);
					}
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

		});
	}
	
	public void setExTorrents(ArrayList<TorrentExtended> list) {
		if(list == null || list.size() == 0) return;
		if(last_id == 0){
			svRefresh.scrollTo(0, 0);
			mcontainer.removeAllViews();
			last_torrent_added = "";
			nomore_view = null;
		}
		lParams.setMargins(0, margin, 0, 0);
		mcontainer.setLayoutParams(lParams);
		if(last_divider != null)
			mcontainer.removeView(last_divider);
		
		for(final TorrentExtended torrent : list){
		    LayoutInflater inflater =  LayoutInflater.from(getActivity());		    
			final RelativeLayout rlList = (RelativeLayout)inflater.inflate(R.layout.task_row, null);
			TextView tvDescription = (TextView)rlList.findViewById(R.id.tvDescription);
			TextView tvInfo = (TextView)rlList.findViewById(R.id.tvInfo);
			ImageView ivThumb = (ImageView)rlList.findViewById(R.id.ivThumb);
			LinearLayout rlSPstate = (LinearLayout)rlList.findViewById(R.id.sp_state);
			ProgressBar pbDownloaded = (ProgressBar)rlList.findViewById(R.id.pbDownloaded);
			Util.set_sp_layout(rlSPstate, torrent.sp_state);
			tvDescription.setText(torrent.small_descr);
			
			if(!torrent.startdat.equals("") && torrent.if_seeder != null){
				if(torrent.if_seeder.equals("yes")){
					tvInfo.setText(getActivity().getString(R.string.seeding) + " | " + getActivity().getString(R.string.already_uploaded) + Util.format_size(torrent.uploaded));
				}else{
					if((torrent.to_go > 0)&&(torrent.to_go <= torrent.size)){
						int percent = (int)((double)(torrent.size - torrent.to_go)/torrent.size * 100);
						tvInfo.setText(getActivity().getString(R.string.already_downloaded) + " " + percent + "%");
						pbDownloaded.setProgress(percent);
						pbDownloaded.setVisibility(View.VISIBLE);
					}else{
						tvInfo.setText(getActivity().getString(R.string.already_finished));
					}
				}
			}else{
				tvInfo.setText(getActivity().getString(R.string.unstarted));
			}

			new TorrentImage(getActivity(), ivThumb, torrent.torrentid, TorrentImage.TYPE_SMALL);
			
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
			
			if(action.equals("remote")){
				rlList.setOnLongClickListener(new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View arg0) {
						CustomDialog.Builder customBuilder = new CustomDialog.Builder(getActivity());
			            customBuilder.setTitle(R.string.text_delete)
			            .setMessage(getActivity().getString(R.string.deleteRemoteDownload))
		                .setPositiveButton(R.string.text_delete, new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int which) {
		                    	setReservation(rlList);
								dialog.dismiss();
		                    }
		                })
		                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
		                    }
		                }).create().show();
						return true;
					}
				});

			}
			
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
			nomore_view = (RelativeLayout)inflater.inflate(R.layout.footer_nomore_row, null);
			mcontainer.addView(nomore_view);
		}
		if(footer_view != null){
			mcontainer.removeView(footer_view);
			footer_view = null;
		}
	}
	
	public void setTorrents(ArrayList<Torrent> list) {
		if(list == null || list.size() == 0) return;
		if(last_id == 0){
			svRefresh.scrollTo(0, 0);
			mcontainer.removeAllViews();
			last_torrent_added = "";
			nomore_view = null;
		}
		if(action.equals("bookmark"))
			lParams.setMargins(0, margin, 0, 0);
		else
			lParams.setMargins(0, 0, 0, 0);
		mcontainer.setLayoutParams(lParams);
		if(last_divider != null)
			mcontainer.removeView(last_divider);
		
		for(final Torrent torrent : list){
		    LayoutInflater inflater =  LayoutInflater.from(getActivity());
		    if(action.equals("")||action.equals("new")){
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

			new TorrentImage(getActivity(), ivThumb, torrent.torrentid, TorrentImage.TYPE_SMALL);
			
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
			if(action.equals("bookmark")){
				rlList.setOnLongClickListener(new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View arg0) {
						CustomDialog.Builder customBuilder = new CustomDialog.Builder(getActivity());
			            customBuilder.setTitle(R.string.text_delete)
			            .setMessage(getActivity().getString(R.string.message_delete_favorite))
		                .setPositiveButton(R.string.text_delete, new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int which) {
		                    	setBookmark(rlList);
								dialog.dismiss();
		                    }
		                })
		                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
		                    }
		                }).create().show();
						return true;
					}
				});
			}
			
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
			nomore_view = (RelativeLayout)inflater.inflate(R.layout.footer_nomore_row, null);
			mcontainer.addView(nomore_view);
		}
		if(footer_view != null){
			mcontainer.removeView(footer_view);
			footer_view = null;
		}
	}
	
	public void setReservation(final RelativeLayout rlList){
		ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new NameValuePair("action", "make_reservation"));
		param.add(new NameValuePair("id", rlList.getTag().toString()));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getTorrent(param));
		asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status,
					com.litesuits.http.data.NameValuePair[] headers) {
				try {
					JSONObject json_result = new JSONObject(res.getString());
					String response = json_result.getString("result");
					if(response.equals("0")){
						TranslateAnimation anim = new TranslateAnimation(0, rlList.getWidth(), 0, 0);
						anim.setDuration(200);
						anim.setFillAfter(true);
						anim.setAnimationListener(new AnimationListener() {
							
							@Override
							public void onAnimationStart(Animation animation) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void onAnimationRepeat(Animation animation) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void onAnimationEnd(Animation animation) {
								try {
									mcontainer.removeView(rlList);
									if(mcontainer.getChildCount()==2 && nomore_view != null && last_divider != null){
										mcontainer.removeAllViews();
										LayoutInflater inflater =  LayoutInflater.from(getActivity());
										LinearLayout rlLayout = (LinearLayout)inflater.inflate(R.layout.no_task_tip, null);
										LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(svRefresh.getWidth(), svRefresh.getHeight());
										rlLayout.setLayoutParams(lp);
										mcontainer.addView(rlLayout);
									}
								} catch (Exception e) {
									Log.e(TAG, "Main container removeView failed!");
								}
							}
						});
						rlList.startAnimation(anim);

					}else{
						Toast.makeText(getActivity(), getActivity().getString(R.string.message_delete_failed), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(getActivity(), getActivity().getString(R.string.message_delete_failed), Toast.LENGTH_LONG).show();
					//e.printStackTrace();
				}
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(getActivity()).handleException(e);
			}

		});
	}

	public void setBookmark(final RelativeLayout rlList){
		ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new NameValuePair("action", "make_bookmark"));
		param.add(new NameValuePair("id", rlList.getTag().toString()));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getTorrent(param));
		asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status,
					com.litesuits.http.data.NameValuePair[] headers) {
				try {
					JSONObject json_result = new JSONObject(res.getString());
					String response = json_result.getString("result");
					if(response.equals("0")){
						TranslateAnimation anim = new TranslateAnimation(0, rlList.getWidth(), 0, 0);
						anim.setDuration(200);
						anim.setFillAfter(true);
						anim.setAnimationListener(new AnimationListener() {
							
							@Override
							public void onAnimationStart(Animation animation) {
							}
							
							@Override
							public void onAnimationRepeat(Animation animation) {
							}
							
							@Override
							public void onAnimationEnd(Animation animation) {
								try {
									mcontainer.removeView(rlList);
									if(mcontainer.getChildCount()==2 && nomore_view != null && last_divider != null){
										mcontainer.removeAllViews();
										LayoutInflater inflater =  LayoutInflater.from(getActivity());
										LinearLayout rlLayout = (LinearLayout)inflater.inflate(R.layout.no_bookmark_tip, null);
										LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(svRefresh.getWidth(), svRefresh.getHeight());
										rlLayout.setLayoutParams(lp);
										mcontainer.addView(rlLayout);
									}
								} catch (Exception e) {
									Log.e(TAG, "Main container removeView failed!");
								}
							}
						});
						rlList.startAnimation(anim);

					}else{
						Toast.makeText(getActivity(), getActivity().getString(R.string.message_delete_failed), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(getActivity(), getActivity().getString(R.string.message_delete_failed), Toast.LENGTH_LONG).show();
					//e.printStackTrace();
				}
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(getActivity()).handleException(e);
			}

		});
	}
	
	@Override
	public void onDestroy() {
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