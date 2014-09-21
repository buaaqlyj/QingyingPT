package cn.edu.hit.pt;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

@SuppressLint("InflateParams")
public class TorrentCats extends PopupWindow{
	public static int seeding_count;
	public static int leeching_count;
	
	private LayoutInflater inflater;
	private View mView;
	private LinearLayout rlList;
	private GridView mList;
	private GridView cList;
	private ImageView loading;
	private AssetManager am;
	private Context c;
	private ArrayList<Map<String, Object>> tItem = new ArrayList<Map<String,Object>>();
	private HttpAsyncExecutor asyncExecutor;
	
	public TorrentCats(final Context context) {
		super(context);
		this.am = context.getAssets();
		this.c = context;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
		mView = inflater.inflate(R.layout.torrentcats_layout, null);
		rlList = (LinearLayout)mView.findViewById(R.id.rlList);
		mList = (GridView)mView.findViewById(R.id.mList);
		cList = (GridView)mView.findViewById(R.id.cList);
		loading = (ImageView)mView.findViewById(R.id.loading);

		loading.setImageResource(R.drawable.loading);
		AnimationDrawable anim = (AnimationDrawable) loading.getDrawable();
		anim.start();
		
		this.setContentView(mView);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setAnimationStyle(R.style.FadeTransAnimation);
		this.setTouchable(true);
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		ColorDrawable dw = new ColorDrawable(-00000);
		this.setBackgroundDrawable(dw);
		this.update();

		String[] tNames = new String[] {
			c.getString(R.string.myFavorites),
			c.getString(R.string.remoteDownload),
			c.getString(R.string.leeching),
			c.getString(R.string.seeding)
		};
		String[] tActions = new String[] {
			"bookmark",
			"remote",
			"leeching",
			"seeding",
		};
		
		HashMap<String, Object> map;
		for (int i = 0; i < 4; i++) {		    
			map = new HashMap<String, Object>();
			map.put("action", tActions[i]);
			map.put("name", tNames[i]);
			if(i==2)
				map.put("count", leeching_count);
			else if(i==3)
				map.put("count", seeding_count);
			else map.put("count", 0);
			tItem.add(map);
		}
		
		mList.setAdapter(new MyTaskAdapter());

		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(context));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getTaskCount());
		asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				getTaskCount(res.getString());
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(context).handleException(e);
				dismiss();
			}

		});
		
		if((Params.cats == null)||(Params.cats.size() == 0)){
			ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
			param.add(new NameValuePair("action", "categories"));
			MyHttpRequest requestCats = new MyHttpRequest(URLContainer.getTorrent(param));
			asyncExecutor.execute(requestCats, new HttpResponseHandler() {

				@Override
				protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
					setTorrentCats(res.getString());
				}
				
				@Override
				protected void onFailure(Response res, HttpException e) {
					new MyHttpExceptionHandler(context).handleException(e);
					dismiss();
				}

			});
		}else{
			cList.setAdapter(new MyCatAdapter());
			loading.setVisibility(View.GONE);
			rlList.setVisibility(View.VISIBLE);
		}
	}
	
	public void setTorrentCats(String json) {
		try {
			JSONArray arr = new JSONArray(json);
			Params.cats = new ArrayList<Map<String, Object>>();
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("id", "latest");
			map.put("name", c.getString(R.string.latest_torrent));
			map.put("action", "new");
			Params.cats.add(map);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject temp = (JSONObject) arr.get(i);
			    int id = temp.getInt("id");
			    String name = temp.getString("name");
			    
				map = new HashMap<String, Object>();
				map.put("id", id);
				map.put("name", name);
				map.put("action", "");
				Params.cats.add(map);
			}
			cList.setAdapter(new MyCatAdapter());
			loading.setVisibility(View.GONE);
			rlList.setVisibility(View.VISIBLE);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public class MyTaskAdapter extends BaseAdapter{			
        public int getCount() { 
        	if(tItem == null)
        		return 0;
            return tItem.size(); 
        }

        public Object getItem(int item) { 
            return item;
        }
 
        public long getItemId(int id) { 
            return id; 
        }

		@SuppressWarnings("deprecation")
		public View getView(final int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = inflater.inflate(R.layout.torrentcat_item, null);
				TextView tvCat = (TextView)convertView.findViewById(R.id.tvCat);
				ImageView iv = (ImageView)convertView.findViewById(R.id.iv);
	            try {
					InputStream is = am.open("torrenticons/"+ tItem.get(position).get("action").toString()+".png");
					Drawable d = Drawable.createFromResourceStream(c.getResources(), null, is, "");
					iv.setImageDrawable(d);
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	            tvCat.setText(tItem.get(position).get("name").toString());
				TextView tvCount = (TextView)convertView.findViewById(R.id.tvCount);
				
				if(position == 2){
					if(leeching_count > 0){
						tvCount.setText(leeching_count + "");
						tvCount.setVisibility(View.VISIBLE);
					}
				}else if(position == 3){
					if(seeding_count > 0){
						tvCount.setText(seeding_count + "");
						tvCount.setVisibility(View.VISIBLE);
					}
				}
				convertView.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.list_item));
				convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View convertView) {
						TorrentFragment.action = tItem.get(position).get("action").toString();
						TorrentFragment.categoryname = tItem.get(position).get("name").toString();
						TorrentFragment.if_load = true;
						dismiss();
					}
				});
			}
			return convertView; 
		}
	}
	
	public class MyCatAdapter extends BaseAdapter{			
        public int getCount() { 
        	if(Params.cats == null)
        		return 0;
            return Params.cats.size(); 
        }

        public Object getItem(int item) { 
            return item;
        }
 
        public long getItemId(int id) { 
            return id; 
        }

		@SuppressWarnings("deprecation")
		public View getView(final int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = inflater.inflate(R.layout.torrentcat_item, null);
				TextView tvCat = (TextView)convertView.findViewById(R.id.tvCat);
				ImageView iv = (ImageView)convertView.findViewById(R.id.iv);
	            try {
					InputStream is = am.open("torrenticons/"+Params.cats.get(position).get("id").toString()+".png");
					Drawable d = Drawable.createFromResourceStream(c.getResources(), null, is, "");
					iv.setImageDrawable(d);
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	            tvCat.setText(Params.cats.get(position).get("name").toString());
				convertView.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.list_item));
				convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View convertView) {
						TorrentFragment.action = Params.cats.get(position).get("action").toString();
						try {
							TorrentFragment.categoryid = Integer.parseInt(Params.cats.get(position).get("id").toString());
						} catch (NumberFormatException e) {
							TorrentFragment.categoryid = 0;
						}
						TorrentFragment.categoryname = Params.cats.get(position).get("name").toString();
						TorrentFragment.if_load = true;
						dismiss();
					}
				});
			}
			return convertView; 
		}
	}
	
	public void getTaskCount(String jsonString) {
		try {
			JSONObject json = new JSONObject(jsonString);
			leeching_count = Integer.parseInt(json.getString("leeching"));
			seeding_count = Integer.parseInt(json.getString("seeding"));
	
			String[] tNames = new String[] {
				c.getString(R.string.myFavorites),
				c.getString(R.string.remoteDownload),
				c.getString(R.string.leeching),
				c.getString(R.string.seeding)
			};
			String[] tAvtions = new String[] {
				"bookmark",
				"remote",
				"leeching",
				"seeding",
			};
			
			tItem = new ArrayList<Map<String,Object>>();
			HashMap<String, Object> map;
			for (int i = 0; i < 4; i++) {		    
				map = new HashMap<String, Object>();
				map.put("action", tAvtions[i]);
				map.put("name", tNames[i]);
				if(i==2)
					map.put("count", leeching_count);
				else if(i==3)
					map.put("count", seeding_count);
				else map.put("count", 0);
				tItem.add(map);
			}
			
			mList.setAdapter(new MyTaskAdapter());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
