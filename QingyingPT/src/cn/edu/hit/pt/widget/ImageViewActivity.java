package cn.edu.hit.pt.widget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import cn.edu.hit.pt.R;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.impl.DirectoryUtil;
import cn.edu.hit.pt.impl.MD5Util;
import cn.edu.hit.pt.widget.ImageControl.ICustomMethod;

public class ImageViewActivity extends Activity{
	private String TAG = "ImageViewActivity";
	private String urlString = "";
	private String img_name = "";
	private String img_path = "";
	private ProgressBar progressBar;
	private Bitmap bmp = null;
	public long length;
	public int last_progress = 0;

	private ImageControl imgControl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageview_activity);
		
		imgControl = (ImageControl)findViewById(R.id.imageControl);
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		
		Intent intent = getIntent();
		urlString = intent.getStringExtra("url");

		Pattern pattern = Pattern.compile("(attachments/([\\w]+)/([\\w]+).([\\w]+)).thumb.jpg");
		Matcher matcher = pattern.matcher(urlString);
		if(matcher.find()){
			String originURL = URLContainer.BASEURL + matcher.group(1);
	    	File originImage = new File(DirectoryUtil.forumImageDirectory + MD5Util.MD5(originURL) + ".jpg");
	    	if(originImage.exists()){
	    		bmp = BitmapFactory.decodeFile(originImage.getPath());
	    		init();
	    	}else{
	        	img_name = MD5Util.MD5(urlString);
	        	img_path = DirectoryUtil.forumImageDirectory + img_name + ".jpg";
	        	File img = new File(img_path);
	        	if(img.exists()){
	        		bmp = BitmapFactory.decodeFile(img_path);
	        		init();
	        	}
	    		urlString  = URLContainer.BASEURL + matcher.group(1);
				progressBar.setVisibility(View.VISIBLE);
				loadImage loadImage = new loadImage();
				loadImage.execute();
			}
		}else{
	    	img_name = MD5Util.MD5(urlString);
	    	img_path = DirectoryUtil.forumImageDirectory + img_name + ".jpg";
	    	File img = new File(img_path);
	    	if(img.exists()){
	    		bmp = BitmapFactory.decodeFile(img_path);
	    		init();
	    	}else{
				progressBar.setVisibility(View.VISIBLE);
				loadImage loadImage = new loadImage();
				loadImage.execute();
			}
		}
	}
	
	private void init() {
		Rect frame = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;
		int screenH = dm.heightPixels - statusBarHeight;
		if (bmp != null) {
			imgControl.imageInit(bmp, screenW, screenH, statusBarHeight,
					new ICustomMethod() {
                      
						@Override
						public void customMethod(Boolean currentStatus) {
							// 当图片处于放大或缩小状态时，控制标题是否显示
							/*if (currentStatus) {
								llTitle.setVisibility(View.GONE);
							} else {
								llTitle.setVisibility(View.VISIBLE);
							}*/
						}
					});
		}
		else{
			Toast.makeText(ImageViewActivity.this, getString(R.string.picture_load_failed), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			imgControl.mouseDown(event);			
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			imgControl.mousePointDown(event);
		
			break;
		case MotionEvent.ACTION_MOVE:
			imgControl.mouseMove(event);
			
			break;

		case MotionEvent.ACTION_UP:
			imgControl.mouseUp();
			break;
		}

		return true;
	}
	
	public class loadImage extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(urlString);
            HttpResponse response;
			try {
				response = httpClient.execute(request);
				HttpEntity entity = response.getEntity();
				length = entity.getContentLength();
				InputStream inputStream = response.getEntity().getContent();
				byte[] b = new byte[1024];
				int readedLength = -1;
				File file = new File(DirectoryUtil.forumImageDirectory, img_name);
				OutputStream outputStream = new FileOutputStream(file);
				long count = 0;
				while( (readedLength = inputStream.read(b)) != -1){
					outputStream.write(b, 0, readedLength);
					count += readedLength;
					int progress = (int)(count*100/length);
					if(last_progress + 5 < progress){
						publishProgress(progress);
						last_progress = progress;
					}
				}
				inputStream.close();
				outputStream.close();
			} catch (Exception e) {
				Log.e(TAG, e.toString());
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			File old_file = new File(DirectoryUtil.forumImageDirectory + img_name);
			File new_file = new File(img_path);
			old_file.renameTo(new_file);
			bmp = BitmapFactory.decodeFile(img_path);
            init();
    		progressBar.setVisibility(View.GONE);
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBar.setProgress(values[0]);
			super.onProgressUpdate(values);
		}
		
	}
}
