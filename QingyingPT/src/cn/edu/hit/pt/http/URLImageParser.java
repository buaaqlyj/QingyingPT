package cn.edu.hit.pt.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html.ImageGetter;
import android.widget.TextView;
import cn.edu.hit.pt.Params;
import cn.edu.hit.pt.R;
import cn.edu.hit.pt.impl.BitmapFunctions;
import cn.edu.hit.pt.impl.DirectoryUtil;
import cn.edu.hit.pt.impl.ImageCache;
import cn.edu.hit.pt.impl.MD5Util;
import cn.edu.hit.pt.impl.ScaleUtil;

public class URLImageParser implements ImageGetter {
    Context c;
    TextView container;
    float container_width;

    /***
     * Construct the URLImageParser which will execute AsyncTask and refresh the container
     * @param t
     * @param c
     */
    public URLImageParser(TextView t, Context c) {
        this.c = c;
        this.container = t;
    }

    public Drawable getDrawable(String source) {
    	if(DirectoryUtil.forumImageDirectory.equals("")) return null;
        URLDrawable urlDrawable = new URLDrawable();
        urlDrawable.drawable = c.getResources().getDrawable(R.drawable.img_loading_large);
        urlDrawable.metrics = ScaleUtil.metrics(c);
		int scaleWidth = ScaleUtil.imageSpanWidth(c);
		int scaleHeight = (int)((float)urlDrawable.drawable.getIntrinsicHeight()/(float)urlDrawable.drawable.getIntrinsicWidth()*scaleWidth);
		urlDrawable.drawable.setBounds(0, 0, scaleWidth, scaleHeight);
		urlDrawable.setBounds(0, 0, scaleWidth, scaleHeight);

		if(source.equalsIgnoreCase("pic/trans.gif")){
			Drawable d = c.getResources().getDrawable(R.drawable.dot);
			d.setBounds(0, 2, 18, 20);
            return d;
		}
		
		Pattern pattern = Pattern.compile("([\\w]+)/([\\w]+).gif");
		Matcher matcher = pattern.matcher(source);
		if (matcher.find()){
			AssetManager am = null;  
			am = c.getAssets();
			try {
				InputStream is = am.open(matcher.group(0));
				Drawable d = Drawable.createFromResourceStream(c.getResources(), null, is, "");
				scaleWidth = ScaleUtil.Dp2Px(c, d.getIntrinsicWidth());
				scaleHeight = ScaleUtil.Dp2Px(c, d.getIntrinsicHeight());
				d.setBounds(0, 0, scaleWidth, scaleHeight);
	            is.close();
	            return d;
			}catch(FileNotFoundException e){
				pattern = Pattern.compile("^attachments/");
				matcher = pattern.matcher(source);
				if (matcher.find()){
					source = URLContainer.BASEURL + source;
				}

		        ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(urlDrawable);
				if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
					asyncTask.executeOnExecutor(Executors.newCachedThreadPool(), source);
				}else{
			        asyncTask.execute(source);
				}

		        return urlDrawable;
			}catch(IOException e) {
				e.printStackTrace();
			}
            return null;
		}else {
			pattern = Pattern.compile("^attachments/");
			matcher = pattern.matcher(source);
			if (matcher.find()){
				source = URLContainer.BASEURL + source;
			}

	        ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(urlDrawable);
			if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
				asyncTask.executeOnExecutor(Executors.newCachedThreadPool(), source);
			}else{
		        asyncTask.execute(source);
			}

	        return urlDrawable;
		}
    }

    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable>  {
        URLDrawable urlDrawable;

        public ImageGetterAsyncTask(URLDrawable d) {
            this.urlDrawable = d;
        }

        @Override
        protected Drawable doInBackground(String... params) {
        	if(Params.userSettings != null && Params.userSettings.nopic == true) return null;
        	String source = params[0];
        	String img_name = MD5Util.MD5(source);
        	String file_name = DirectoryUtil.forumImageDirectory + img_name + ".jpg";
        	File file = new File(file_name);
        	if(!file.exists())
        		return fetchDrawable(source);
        	else{
				try {
	        		InputStream is = new FileInputStream(new File(file_name));
	        		Bitmap bitmap = BitmapFactory.decodeStream(is);
	        		ImageCache cache = ImageCache.instance();
	        		cache.add(bitmap, source);
	        		Drawable drawable = new BitmapDrawable(c.getResources(), cache.get(source));
	        		//drawable.setBounds(0, 0, (int)(drawable.getIntrinsicWidth() * App.metrics.density), (int)(drawable.getIntrinsicHeight() * App.metrics.density));

	        		return drawable;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
        	}
        	return null;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
        	if(Params.userSettings != null && Params.userSettings.nopic == true){
        		drawable = c.getResources().getDrawable(R.drawable.forum_nopic);
        	}
        	if(drawable == null){
        		drawable = c.getResources().getDrawable(R.drawable.img_broken_large);
        	}
    		int scaleWidth;
    		int scaleHeight;
    		if(ScaleUtil.imageSpanWidth(c) > drawable.getIntrinsicWidth()){
    			scaleWidth = drawable.getIntrinsicWidth();
    			scaleHeight = drawable.getIntrinsicHeight();
			}else{
    			scaleWidth = ScaleUtil.imageSpanWidth(c);
    			scaleHeight = (int)((float)drawable.getIntrinsicHeight()/(float)drawable.getIntrinsicWidth()*scaleWidth);
			}
			drawable.setBounds(0, 0, scaleWidth, scaleHeight);
			urlDrawable.setBounds(0, 0, scaleWidth, scaleHeight);

            urlDrawable.drawable = drawable;
            container.setText(container.getText());
        }

        /***
         * Get the Drawable from URL
         * @param urlString
         * @return
         */
        public Drawable fetchDrawable(String urlString) {
        	if(DirectoryUtil.forumImageDirectory.equals("")) return null;
        	Drawable drawable = null;
			try {
				InputStream is = fetch(urlString);
        		Bitmap bitmap = BitmapFactory.decodeStream(is);
        		ImageCache cache = ImageCache.instance();
        		cache.add(bitmap, urlString);
				drawable = new BitmapDrawable(c.getResources(), cache.get(urlString));
				if(drawable!=null){
					String img_name = MD5Util.MD5(urlString);
					BitmapFunctions.storeInSD(bitmap, DirectoryUtil.forumImageDirectory, img_name + ".jpg");
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            return drawable;
        }

        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(urlString);
            HttpResponse response = httpClient.execute(request);
            return response.getEntity().getContent();
        }
    }
}