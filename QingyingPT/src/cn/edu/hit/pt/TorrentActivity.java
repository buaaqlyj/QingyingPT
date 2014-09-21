package cn.edu.hit.pt;

import java.util.ArrayList;
import java.util.concurrent.FutureTask;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.TorrentImage;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.http.UserAvatarTask;
import cn.edu.hit.pt.impl.ScaleUtil;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.model.Torrent;
import cn.edu.hit.pt.widget.CustomScrollView;
import cn.edu.hit.pt.widget.CustomScrollView.OnScrollListener;
import cn.edu.hit.pt.widget.HeaderScrollLayout;
import cn.edu.hit.pt.widget.WrapContentHeightViewPager;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

@SuppressLint({ "InflateParams", "RtlHardcoded", "InlinedApi", "ClickableViewAccessibility" })
public class TorrentActivity extends SwipeBackActivity implements OnTouchListener, OnScrollListener{
	//private String TAG = "TorrentActivity";
	private LinearLayout mcontainer;
	private CustomScrollView svRefresh;
	private HeaderScrollLayout rlHeader;
	private TextView tvTitle;
	private TextView tvTorrentName;
	private TextView tvOwner;
	private TextView tvCategory;
	private TextView tvSource;
	private TextView tvSP;
	private TextView tvSize;
	private TextView tvSeeders;
	private TextView tvLeechers;
	private TextView uploaded_at;
	private ImageView ivCover;
	private ImageView ivUserAvatar;
	private LinearLayout llimdb;
	private LinearLayout rlDetails;
	private RatingBar rbIMDb;
	private TextView tvRating;
	private LinearLayout remoteDownload;
	private ImageView ivRemoteDownload;
	private LinearLayout setFavorite;
	private ImageView ivSetFavorite;
	private LinearLayout btnMore;
	private LinearLayout rlDescription;
	private LinearLayout btnComment;
	private LinearLayout btnOtherCopy;
	private TextView tvComment;
	private TextView tvOtherCopy;
	private View tabComment;
	private View tabOtherCopy;
	private WrapContentHeightViewPager viewPager;
	private RelativeLayout footer_view = null;
	
	private long id;
	private String title;
	private TorrentImage torrentimageTask;
	private TorrentComments torrentComments;
	private TorrentOtherCopy torrentOtherCopy;

	private HttpAsyncExecutor asyncExecutor;
	private UserAvatarTask avatarTask;
	private ArrayList<FutureTask<Response>> httpTasks = new ArrayList<FutureTask<Response>>();
	
    private int len = 50;
    private float startY;
    private int bottom;
    private boolean canScroll = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.torrent);
		
		SwipeBackLayout mSwipeBackLayout;
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

		Intent intent = getIntent();
		id = intent.getLongExtra("id", 0);
		title = intent.getStringExtra("title");
		mcontainer = (LinearLayout)findViewById(R.id.mcontainer);
		svRefresh = (CustomScrollView)findViewById(R.id.svRefresh);
		rlHeader = (HeaderScrollLayout)findViewById(R.id.rlHeader);
		tvTitle = (TextView)findViewById(R.id.tvTitle);
		tvTorrentName = (TextView)findViewById(R.id.tvTorrentName);
		tvOwner = (TextView)findViewById(R.id.tvOwner);
		uploaded_at = (TextView)findViewById(R.id.uploaded_at);
		tvCategory = (TextView)findViewById(R.id.tvCategory);
		tvSource = (TextView)findViewById(R.id.tvSource);
		tvSP = (TextView)findViewById(R.id.tvSP);
		tvSize = (TextView)findViewById(R.id.tvSize);
		tvSeeders = (TextView)findViewById(R.id.tvSeeders);
		tvLeechers = (TextView)findViewById(R.id.tvLeechers);
		rlDetails = (LinearLayout)findViewById(R.id.rlDetails);
		llimdb = (LinearLayout)findViewById(R.id.llimdb);
		rbIMDb = (RatingBar)findViewById(R.id.rbIMDb);
		tvRating = (TextView)findViewById(R.id.tvRating);
		ivCover = (ImageView)findViewById(R.id.ivCover);
		rlDescription = (LinearLayout)findViewById(R.id.rlDescription);
		btnComment = (LinearLayout)findViewById(R.id.btnComment);
		btnOtherCopy = (LinearLayout)findViewById(R.id.btnOtherCopy);
		tvComment = (TextView)findViewById(R.id.tvComment);
		tvOtherCopy = (TextView)findViewById(R.id.tvOtherCopy);
		tabComment = (View)findViewById(R.id.tabComment);
		tabOtherCopy = (View)findViewById(R.id.tabOtherCopy);
		viewPager = (WrapContentHeightViewPager)findViewById(R.id.viewPager);
		remoteDownload = (LinearLayout)findViewById(R.id.remoteDownload);
		ivRemoteDownload = (ImageView)findViewById(R.id.ivRemoteDownload);
		setFavorite = (LinearLayout)findViewById(R.id.setFavorite);
		ivSetFavorite = (ImageView)findViewById(R.id.ivSetFavorite);
		btnMore = (LinearLayout)findViewById(R.id.btnMore);
		ivUserAvatar = (ImageView)findViewById(R.id.ivUserAvatar);

		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(this));
		
		if(title!=null)
			tvTitle.setText(title);
		svRefresh.setOnScrollListener(this);
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(ScaleUtil.heightPixels(this) / 2));
		rlHeader.setLayoutParams(lp);
		
		if(footer_view == null){
			footer_view = (RelativeLayout) getLayoutInflater().inflate(R.layout.refresh_view, null);
			ImageView iv = (ImageView)footer_view.findViewById(R.id.iv);
			iv.setImageResource(R.drawable.loading);
			AnimationDrawable animationLoading = (AnimationDrawable) iv.getDrawable();
			animationLoading.start();
			mcontainer.addView(footer_view);
		}
		loadTorrentInfo();
		
		torrentimageTask = new TorrentImage(this, ivCover, id, TorrentImage.TYPE_BIG);
		
		Button btnReturn = (Button)findViewById(R.id.btnReturn);
		btnReturn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
	}
	
	public void initViewPager(int commentCount, long imdbID, int copyCount) {
		ArrayList<Fragment> fragments = new ArrayList<Fragment>();
		torrentComments = new TorrentComments();
		torrentComments.setArgs(id, commentCount);
		torrentOtherCopy = new TorrentOtherCopy();
		torrentOtherCopy.setArgs(id, imdbID, copyCount);
		fragments.add((Fragment)torrentComments);
		fragments.add((Fragment)torrentOtherCopy);
		viewPager.setAdapter(new myFragmentPagerAdapter(getSupportFragmentManager(), fragments));
		viewPager.setOnPageChangeListener(new myOnPageChangerListener());
		viewPager.setCurrentItem(0);
	}
	
	public void loadTorrentInfo() {
		ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new NameValuePair("action", "view"));
		param.add(new NameValuePair("id", id + ""));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getTorrent(param));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				setTorrent(res.getObject(Torrent.class));
				if(footer_view != null){
					mcontainer.removeView(footer_view);
					footer_view = null;
				}
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(TorrentActivity.this).handleException(e);
				if(footer_view != null){
					mcontainer.removeView(footer_view);
					footer_view = null;
				}
			}
		}));
	}
	
	public void setTorrent(final Torrent torrent) {
		if(torrent == null) return;
		rlDetails.setVisibility(View.VISIBLE);
		rlDescription.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				View rootView = Util.getRootView(TorrentActivity.this);
				TorrentDescription window = new TorrentDescription(TorrentActivity.this, torrent);
				window.showAtLocation(rootView, Gravity.BOTTOM|Gravity.LEFT, 0, 0);
			}
		});
		String sp_stateString = "";
		tvTitle.setText(torrent.small_descr);
		tvTorrentName.setText(torrent.name);
		if(torrent.owner_name.equals(""))
			tvOwner.setText(R.string.anonymous);
		else
			tvOwner.setText(torrent.owner_name);
		uploaded_at.setText(getString(R.string.uploaded_at) + " " + torrent.added);
		if(torrent.imdb_rating > 0){
			llimdb.setVisibility(View.VISIBLE);
			rbIMDb.setRating(torrent.imdb_rating);
			tvRating.setText(torrent.imdb_rating + "/10");
		}
		
		switch (torrent.reservation_state) {
			case 1:
				ivRemoteDownload.setImageDrawable(getResources().getDrawable(R.drawable.button_downloading));
				remoteDownload.setEnabled(false);
				break;
			case 2:
				ivRemoteDownload.setImageDrawable(getResources().getDrawable(R.drawable.button_downloading));
				remoteDownload.setEnabled(true);
				break;

			default:
				ivRemoteDownload.setImageDrawable(getResources().getDrawable(R.drawable.button_download));
				remoteDownload.setEnabled(true);
				break;
		}
		
		switch (torrent.bookmark_state) {
			case 1:
				ivSetFavorite.setImageDrawable(getResources().getDrawable(R.drawable.button_favorited));
				break;

			default:
				ivSetFavorite.setImageDrawable(getResources().getDrawable(R.drawable.button_favorite));
				break;
		}
		
		remoteDownload.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				makeReservation();
				remoteDownload.setEnabled(false);
			}
		});
		
		remoteDownload.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				Toast.makeText(TorrentActivity.this, getString(R.string.remoteDownload), Toast.LENGTH_SHORT).show();
				return false;
			}
		});
		
		setFavorite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				makeBookmark();
				setFavorite.setEnabled(false);
			}
		});
		
		setFavorite.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				Toast.makeText(TorrentActivity.this, getString(R.string.bookmark), Toast.LENGTH_SHORT).show();
				return false;
			}
		});
		
		btnMore.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TorrentMenu sWindow = new TorrentMenu(TorrentActivity.this, id, title);
				sWindow.showAtLocation(btnMore, Gravity.RIGHT|Gravity.TOP, 0, ScaleUtil.Dp2Px(TorrentActivity.this, 75));
			}
		});
		
		tvCategory.setText(torrent.cat_name);
		tvSource.setText(torrent.source_name);
		switch (torrent.sp_state) {
			case 2:
				sp_stateString = getString(R.string.sp_free);
				break;
			case 3:
				sp_stateString = getString(R.string.sp_2x);
				break;
			case 4:
				sp_stateString = getString(R.string.sp_2xfree);
				break;
			case 5:
				sp_stateString = getString(R.string.sp_50);
				break;
			case 6:
				sp_stateString = getString(R.string.sp_2x50);
				break;
			case 7:
				sp_stateString = getString(R.string.sp_30);
				break;

			default:
				sp_stateString = getString(R.string.sp_normal);
				break;
		}
		tvSP.setText(sp_stateString);
		tvSize.setText(Util.format_size(torrent.size));
		tvSeeders.setText(torrent.seeders + "");
		tvLeechers.setText(torrent.leechers + "");

		initViewPager(torrent.comments, torrent.imdb_id, torrent.copy_count);
		
		if(torrent.comments == 0)
			tvComment.setText(getString(R.string.torrent_comments));
		else{
			tvComment.setText(getString(R.string.torrent_comments) + "(" + torrent.comments + ")");
		}
		
		if(torrent.copy_count == 0)
			tvOtherCopy.setText(getString(R.string.other_copy));
		else{
			tvOtherCopy.setText(getString(R.string.other_copy) + "(" + torrent.copy_count + ")");
		}
		
		btnComment.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				viewPager.setCurrentItem(0);
			}
		});
		
		btnOtherCopy.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				viewPager.setCurrentItem(1);
			}
		});
		
		avatarTask = new UserAvatarTask(TorrentActivity.this, torrent.owner, ivUserAvatar, true);
	    
	    rlDetails.setVisibility(View.VISIBLE);
		
	}
	
	public void makeReservation() {
		ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new NameValuePair("action", "make_reservation"));
		param.add(new NameValuePair("id", id+""));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getTorrent(param));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status,
					NameValuePair[] headers) {
				try {
					JSONObject json_result = new JSONObject(res.getString());
					int response = json_result.getInt("result");
					if(response == 1){
						Toast.makeText(TorrentActivity.this, getString(R.string.remotedownload_added), Toast.LENGTH_SHORT).show();
						ivRemoteDownload.setImageDrawable(getResources().getDrawable(R.drawable.button_downloading));
					}else{
						Toast.makeText(TorrentActivity.this, getString(R.string.remotedownload_removed), Toast.LENGTH_SHORT).show();
						ivRemoteDownload.setImageDrawable(getResources().getDrawable(R.drawable.button_download));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				remoteDownload.setEnabled(true);
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(TorrentActivity.this).handleException(e);
			}
		}));
	}
	
	public void makeBookmark() {
		ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new NameValuePair("action", "make_bookmark"));
		param.add(new NameValuePair("id", id+""));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getTorrent(param));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status,
					NameValuePair[] headers) {
				try {
					JSONObject json_result = new JSONObject(res.getString());
					int response = json_result.getInt("result");
					if(response == 1){
						Toast.makeText(TorrentActivity.this, getString(R.string.bookmark_added), Toast.LENGTH_SHORT).show();
						ivSetFavorite.setImageDrawable(getResources().getDrawable(R.drawable.button_favorited));
					}else{
						Toast.makeText(TorrentActivity.this, getString(R.string.bookmark_removed), Toast.LENGTH_SHORT).show();
						ivSetFavorite.setImageDrawable(getResources().getDrawable(R.drawable.button_favorite));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				setFavorite.setEnabled(true);
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(TorrentActivity.this).handleException(e);
			}
		}));
	}

	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		return super.onTouchEvent(ev);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev){
	    float currentY = ev.getY();
	    switch (ev.getAction()) {
	    case MotionEvent.ACTION_DOWN:
	        startY = currentY;
	        bottom = rlHeader.getBottom();
	        rlHeader.setViewBottom(bottom);
	        break;
	    case MotionEvent.ACTION_MOVE:
	        if (canScroll && startY > bottom && rlHeader.isShown() && rlHeader.getTop() >= 0) {
	            int y = (int) (bottom + (currentY - startY) / 2.5f);
	            if (y < rlHeader.getBottom() + len && y >= bottom) {
	            	rlHeader.setLayoutParams(new RelativeLayout.LayoutParams(rlHeader.getWidth(), y));
	            	svRefresh.setEnabled(false);
	    	    	if(svRefresh.getScrollY() > 0){
	    	    		canScroll = false;
	    	    	}
	            }
	            rlHeader.setCanScroll(false);
	        }
	        break;
	    case MotionEvent.ACTION_UP:
	    case MotionEvent.ACTION_CANCEL:
	    	if(svRefresh.getScrollY() <= 0)
	    		canScroll = true;
	    	else
	    		canScroll = false;
	    	svRefresh.setEnabled(true);
	    	rlHeader.startScroll(0, rlHeader.getBottom(), 0, bottom - rlHeader.getBottom());
	        break;
	    }
		return super.dispatchTouchEvent(ev);
	}
	
	public class myFragmentPagerAdapter extends FragmentPagerAdapter{
		public ArrayList<Fragment> fragments;

		public myFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		public myFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
			super(fm);
			this.fragments = fragments;
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}
		
	}
	
	public class myOnPageChangerListener implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int arg0) {}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {}

		@Override
		public void onPageSelected(int position) {
			if(viewPager.getCurrentItem() == 0){
				tvComment.setTextColor(getResources().getColor(R.color.text_color_dark));
				tvOtherCopy.setTextColor(getResources().getColor(R.color.text_color_grey));
				tabComment.setVisibility(View.VISIBLE);
				tabOtherCopy.setVisibility(View.GONE);
				if(torrentComments != null && torrentComments.canLoadComments()){
					torrentComments.loadComments();
				}
			}else{
				tvOtherCopy.setTextColor(getResources().getColor(R.color.text_color_dark));
				tvComment.setTextColor(getResources().getColor(R.color.text_color_grey));
				tabOtherCopy.setVisibility(View.VISIBLE);
				tabComment.setVisibility(View.GONE);
				if(torrentOtherCopy != null && torrentOtherCopy.canLoadCopies()){
					torrentOtherCopy.loadTorrentList();
				}
			}
			viewPager.refresh();
		}
		
	}

	@Override
	public void finish() {
		for(FutureTask<Response> httpTask : httpTasks){
			httpTask.cancel(true);
		}
		if(torrentimageTask != null) torrentimageTask.cancel();
		if(avatarTask != null) avatarTask.cancel();
		super.finish();
	}

	@Override
	public void onScroll(int scrollY) {
		int rangeY = mcontainer.getMeasuredHeight();
		if(scrollY > rangeY -  svRefresh.getHeight() - 10 && viewPager.getChildCount() > 0){
			switch (viewPager.getCurrentItem()) {
			case 1:
				if(torrentOtherCopy != null && torrentOtherCopy.canLoadCopies()){
					torrentOtherCopy.loadTorrentList();
				}
				break;

			default:
				if(torrentComments != null && torrentComments.canLoadComments()){
					torrentComments.loadComments();
				}
				break;
			}
		}
	}

}
