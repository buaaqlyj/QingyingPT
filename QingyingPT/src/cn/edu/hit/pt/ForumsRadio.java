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

public class ForumsRadio extends PopupWindow{
	private LayoutInflater inflater;
	private View mView;
	private GridView list;
	private ImageView loading;
	private AssetManager am;
	private Context c;
	
	public ForumsRadio(final Context context) {
		super(context);
		this.am = context.getAssets();
		this.c = context;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
		mView = inflater.inflate(R.layout.forums_reverse_layout, null);
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
		
		if((Params.forums_create == null)||(Params.forums_create.size() == 0)){
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
				Params.forums_create = new ArrayList<Map<String, Object>>();
				for (int i = 0; i < Params.forums.size(); i++) {
					if(Integer.parseInt(Params.forums.get(i).get("minclasscreate").toString()) <= Params.CURUSER.uclass)
						Params.forums_create.add(Params.forums.get(i));
				}
				list.setAdapter(new MyAdapter());
				loading.setVisibility(View.GONE);
				list.setVisibility(View.VISIBLE);
			}
		}else{
			list.setAdapter(new MyAdapter());
			loading.setVisibility(View.GONE);
			list.setVisibility(View.VISIBLE);
		}
	}
	
	public void setForums(String json) {
		try {
			JSONArray arr = new JSONArray(json);
			Params.forums_create = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < arr.length(); i++) {
				JSONObject temp = (JSONObject) arr.get(i);
			    int id = temp.getInt("id");
			    String name = temp.getString("name");
			    int minclasscreate = temp.getInt("minclasscreate");
			    String description = temp.getString("description");
			    if(minclasscreate <= Params.CURUSER.uclass){
			    	HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("id", id);
					map.put("name", name);
					map.put("description", description);
					Params.forums_create.add(map);
			    }
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
        	if(Params.forums_create == null)
        		return 0;
            return Params.forums_create.size(); 
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
				ImageView iv = (ImageView)convertView.findViewById(R.id.iv);
	            try {
					InputStream is = am.open("forumicons/"+Params.forums_create.get(position).get("id").toString()+".png");
					Drawable d = Drawable.createFromResourceStream(c.getResources(), null, is, "");
					iv.setImageDrawable(d);
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				tvForum.setText(Params.forums_create.get(position).get("name").toString());
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
						AddPost.id =  Integer.parseInt(Params.forums_create.get(position).get("id").toString());
						AddPost.name =  Params.forums_create.get(position).get("name").toString();
						AddPost.if_load = true;
						dismiss();
					}
				});
			}
			return convertView; 
		}
	}
}
