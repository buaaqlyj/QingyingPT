package cn.edu.hit.pt;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.widget.CustomToast;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

public class ForumsClick extends PopupWindow{
	private LayoutInflater inflater;
	private View mView;
	private GridView list;
	private ImageView loading;
	private AssetManager am;
	private Context c;
	
	public ForumsClick(final Context context) {
		super(context);
		this.am = context.getAssets();
		this.c = context;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.forums_layout, null);
		list = (GridView)mView.findViewById(R.id.list);
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
		
		if((Params.forums == null)||(Params.forums.size() == 0)){
			HttpAsyncExecutor asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(context));
			MyHttpRequest request = new MyHttpRequest(URLContainer.getForumList());
			asyncExecutor.execute(request, new HttpResponseHandler() {

				@Override
				protected void onSuccess(Response res, HttpStatus status,
						NameValuePair[] headers) {
					setForums(res.getString());
				}

				@Override
				protected void onFailure(Response res, HttpException e) {
					new MyHttpExceptionHandler(context).handleException(e);
					dismiss();
				}
				
			});
		}else{
			list.setAdapter(new MyAdapter());
			loading.setVisibility(View.GONE);
			list.setVisibility(View.VISIBLE);
		}
	}
	
	public void setForums(String json) {
		if(json == null) return;
		try {
			JSONArray arr = new JSONArray(json);
			Params.forums = new ArrayList<Map<String, Object>>();
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("id", "latest");
			map.put("name", c.getString(R.string.latest_topics));
			map.put("posttodaycount", "0");
			map.put("minclasscreate", "255");
			map.put("minclasswrite", "0");
			map.put("description", c.getString(R.string.latest_topics));
			Params.forums.add(map);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject temp = (JSONObject) arr.get(i);
			    int id = temp.getInt("id");
			    String name = temp.getString("name");
			    String posttodaycount = temp.getString("posttodaycount");
			    int minclasscreate = temp.getInt("minclasscreate");
			    int minclasswrite = temp.getInt("minclasswrite");
			    String description = temp.getString("description");
			    
				map = new HashMap<String, Object>();
				map.put("id", id);
				map.put("name", name);
				map.put("posttodaycount", posttodaycount);
				map.put("minclasscreate", minclasscreate);
				map.put("minclasswrite", minclasswrite);
				map.put("description", description);
				Params.forums.add(map);
			}
			list.setAdapter(new MyAdapter());
			loading.setVisibility(View.GONE);
			list.setVisibility(View.VISIBLE);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public class MyAdapter extends BaseAdapter{			
        public int getCount() { 
        	if(Params.forums == null)
        		return 0;
            return Params.forums.size(); 
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
				convertView = inflater.inflate(R.layout.forum_item, null);
				TextView tvForum = (TextView)convertView.findViewById(R.id.tvForum);
				TextView tvPostTodayCount = (TextView)convertView.findViewById(R.id.tvPostTodayCount);
				ImageView iv = (ImageView)convertView.findViewById(R.id.iv);
	            try {
					InputStream is = am.open("forumicons/"+Params.forums.get(position).get("id").toString()+".png");
					Drawable d = Drawable.createFromResourceStream(c.getResources(), null, is, "");
					iv.setImageDrawable(d);
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				tvForum.setText(Params.forums.get(position).get("name").toString());
				if(Integer.parseInt(Params.forums.get(position).get("posttodaycount").toString()) > 0){
					tvPostTodayCount.setText(Params.forums.get(position).get("posttodaycount").toString());
					tvPostTodayCount.setVisibility(View.VISIBLE);
				}
				convertView.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.list_item));
				//convertView.setTag(position);
				convertView.setOnLongClickListener(new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View convertView) {
						new CustomToast(c, CustomToast.TYPE_INFORMATION).setTitle(Params.forums.get(position).get("name").toString())
						.setText(Params.forums.get(position).get("description").toString()).create().show();
						return false;
					}
				});
				convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View convertView) {
						ForumFragment.forumid = Params.forums.get(position).get("id").toString();
						ForumFragment.forumname = Params.forums.get(position).get("name").toString();
						ForumFragment.minclasscreate = Integer.parseInt(Params.forums.get(position).get("minclasscreate").toString());
						ForumFragment.minclasswrite = Integer.parseInt(Params.forums.get(position).get("minclasswrite").toString());
						ForumFragment.if_load = true;
						dismiss();
					}
				});
			}
			return convertView; 
		}
	}
}
