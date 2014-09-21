package cn.edu.hit.pt;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.model.AppItem;
import cn.edu.hit.pt.widget.CustomToast;

import com.litesuits.android.log.Log;
import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.Json;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

@SuppressLint("InflateParams") public class AppsFragment extends Fragment{

	private LayoutInflater i;
	private Button showLeft;
	private GridView gvList;
	private ImageView loading;
	private List<AppItem> list;
	
	private HttpAsyncExecutor asyncExecutor;
	private MyBroadcastReceiver buttonRefreshBroadcastReceiver;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		i = inflater;
		View mView = inflater.inflate(R.layout.appsfragment , null);
		showLeft = (Button) mView.findViewById(R.id.showLeft);
		gvList = (GridView)mView.findViewById(R.id.list);
		loading = (ImageView)mView.findViewById(R.id.loading);
		
		return mView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((MainActivity) getActivity()).mainFragmentPagerAdapter.restoreState(this);
		
		buttonRefreshBroadcastReceiver = new MyBroadcastReceiver();
		getActivity().registerReceiver(buttonRefreshBroadcastReceiver, new IntentFilter(MainActivity.ACTION_REFRESH_BUTTON));
		
		String[] name = {
				getString(R.string.shoutbox),
				getString(R.string.checkin),
				getString(R.string.qrcode)
			};
		String[] description = {
				getString(R.string.shoutbox),
				getString(R.string.checkin),
				getString(R.string.qrcode)
			};
		String[] type = {"local", "local", "local"};
		int[] min_sdk = {102, 102, 104};
		int[] version = {1, 1, 1};
		list = new ArrayList<AppItem>();
		for (int i = 0; i < name.length; i++) {
			if(min_sdk[i] > Params.version) continue;
			AppItem sa = new AppItem();
			sa.id = i + 1;
			sa.name = name[i];
			sa.description = description[i];
			sa.type = type[i];
			sa.min_sdk = min_sdk[i];
			sa.version = version[i];
			list.add(sa);
		}
		gvList.setAdapter(new MyAdapter());

		Util.setShowLeftButton(showLeft);
		showLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).showLeft();
			}
		});
		
		loading.setImageResource(R.drawable.loading);
		AnimationDrawable anim = (AnimationDrawable) loading.getDrawable();
		anim.start();
		
		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(getActivity()));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getApps());
		asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status,
					NameValuePair[] headers) {
				list = new ArrayList<AppItem>();
				JSONArray json;
				try {
					json = new JSONArray(res.getString());
					for (int i = 0; i < json.length(); i++) {
						list.add(Json.get().toObject(json.get(i).toString(), AppItem.class));
					}
					gvList.setAdapter(new MyAdapter());
				} catch (JSONException e) {
					Log.e("Json", e.toString());
				}
				loading.setVisibility(View.GONE);
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(getActivity()).handleException(e);
				loading.setVisibility(View.GONE);
			}
			
		});

	}
	
	public class MyAdapter extends BaseAdapter{			
        public int getCount() { 
        	if(list == null)
        		return 0;
            return list.size(); 
        }

        public Object getItem(int item) { 
            return item;
        }
 
        public long getItemId(int id) { 
            return id; 
        }

		public View getView(final int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				final AppItem sa = list.get(position);
				convertView = i.inflate(R.layout.app_item, null);
				TextView tv = (TextView)convertView.findViewById(R.id.tv);
				//TextView count = (TextView)convertView.findViewById(R.id.count);
				ImageView iv = (ImageView)convertView.findViewById(R.id.iv);
	            try {
					InputStream is = getActivity().getAssets().open("appicons/" + sa.id + ".png");
					Drawable d = Drawable.createFromResourceStream(getResources(), null, is, "");
					d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
					iv.setImageDrawable(d);
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				tv.setText(sa.name.toString());
				convertView.setBackgroundResource(R.drawable.list_item_reverse);
				convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View convertView) {
						Intent intent;
						if (sa.type.equals("local")) {
							intent = new Intent();
							switch (sa.id) {
							case 1:
								intent.setClass(getActivity(), ShoutboxActivity.class);
								break;
								
							case 2:
								intent.setClass(getActivity(), CheckinActivity.class);
								break;
								
							case 3:
								intent.setClass(getActivity(), QRCodeScanner.class);
								break;

							default:
								return;
							}
							startActivity(intent);
						}
					}
				});
				convertView.setOnLongClickListener(new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View convertView) {
						new CustomToast(getActivity(), CustomToast.TYPE_INFORMATION).setTitle(sa.name).setText(sa.description).create().show();
						return false;
					}
				});
			}
			return convertView; 
		}
	}
	
	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		if (this.getView() != null){
			this.getView().setVisibility(menuVisible ? View.VISIBLE : View.GONE);
		}
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