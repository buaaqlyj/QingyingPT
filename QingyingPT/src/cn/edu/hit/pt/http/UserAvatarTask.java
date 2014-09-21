package cn.edu.hit.pt.http;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.FutureTask;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import cn.edu.hit.pt.R;
import cn.edu.hit.pt.UserInformation;
import cn.edu.hit.pt.impl.BitmapFunctions;
import cn.edu.hit.pt.impl.DatabaseUtil;
import cn.edu.hit.pt.impl.DirectoryUtil;
import cn.edu.hit.pt.impl.MD5Util;
import cn.edu.hit.pt.model.Avatar;

import com.litesuits.android.async.AsyncExecutor;
import com.litesuits.android.async.AsyncExecutor.Worker;
import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.parser.BitmapParser;
import com.litesuits.http.response.Response;

public class UserAvatarTask {
	public ImageView ivAvatar;
	public Context context;
	public long userid;
	public boolean clickable;
	public String md5;
	public FutureTask<Bitmap> task;
	public FutureTask<Response> getAvatarImage;
	
	public UserAvatarTask(Context context, long userid, ImageView ivAvatar, boolean clickable) {
		this.userid = userid;
		this.ivAvatar = ivAvatar;
		this.context = context;
		this.clickable = clickable;
		this.md5 = MD5Util.MD5("Avatar:" + userid);
		setAvatar();
	}
	
	public void setAvatar() {
		task = new AsyncExecutor().execute(new Worker<Bitmap>() {

			@Override
			protected Bitmap doInBackground() {
				Bitmap bitmap = null;
				Date now = new Date();
				SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd");
				long nowDate = Long.parseLong(s.format(now));
				Avatar avatar = DatabaseUtil.systemDatabase(context).queryById(userid, Avatar.class);
				if(avatar != null){
					if((nowDate - avatar.storedDate != 0)||(avatar.updatedDate == 0)){	//Everyday Check
						long updatedDate = getUpdatedDate();
						if(updatedDate != avatar.updatedDate){
							bitmap = null;
						}else{
							bitmap = BitmapFunctions.getBitmap(DirectoryUtil.avatarDirectory, md5 + ".jpg");
						}
						avatar.set(updatedDate, nowDate);
						DatabaseUtil.systemDatabase(context).update(avatar);
					}else{
						bitmap = BitmapFunctions.getBitmap(DirectoryUtil.avatarDirectory, md5 + ".jpg");
					}
				}else{
					long updatedDate = getUpdatedDate();
					avatar = new Avatar(userid, updatedDate);
					DatabaseUtil.systemDatabase(context).save(avatar);
					bitmap = BitmapFunctions.getBitmap(DirectoryUtil.avatarDirectory, md5 + ".jpg");
				}
				if(bitmap == null)
					bitmap = getAvatar();
				return bitmap;
			}

			@Override
			protected void onPostExecute(Bitmap data) {
				if(data != null)
		    		ivAvatar.setImageBitmap(data);
				else 
					ivAvatar.setImageResource(R.drawable.default_avatar);
				if(clickable == true){
					ivAvatar.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent();
							intent.putExtra("userid", userid);
							intent.setClass(context, UserInformation.class);
							context.startActivity(intent);
						}
					});
				}
				super.onPostExecute(data);
			}

		});
	}
	
	public Bitmap getAvatar() {
		Bitmap bitmap;
		LiteHttpClient client = LiteHttpClient.newApacheHttpClient(context);
		MyHttpRequest request = new MyHttpRequest(URLContainer.getAvatarUrl(userid));
		request.setDataParser(new BitmapParser());
		Response response = client.execute(request);
		bitmap = response.getBitmap();
		if(bitmap != null){
			bitmap = BitmapFunctions.toRoundBitmap(bitmap);
			BitmapFunctions.storeInSD(bitmap, DirectoryUtil.avatarDirectory, md5 + ".jpg");
		}
		return bitmap;
	}
	
	public long getUpdatedDate() {
		long date = 0;
		LiteHttpClient client = LiteHttpClient.newApacheHttpClient(context);
		MyHttpRequest request = new MyHttpRequest(URLContainer.getProfileUrl(userid));
		Response response = client.execute(request);
		try {
			date = Long.parseLong(response.getString());
		} catch (NumberFormatException e) {
			date = 0;
		}
		return date;
	}
	
	public void cancel() {
		if(task != null)
			task.cancel(true);
	}
}
