package cn.edu.hit.pt.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import cn.edu.hit.pt.Params;
import cn.edu.hit.pt.R;
import cn.edu.hit.pt.impl.DirectoryUtil;
import cn.edu.hit.pt.model.PollMessage;

public class UpdateDownload extends AsyncTask<Void, Integer, Integer> {
	public Context mContext;
	public NotificationManager manager;   
	public Notification notif;
	
	public long length;
	public int last_progress = 0;
	public PollMessage message;
	
	public UpdateDownload(Context c) {
		this.mContext = c;
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		manager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notif = new Notification();
		notif.flags = Notification.FLAG_NO_CLEAR;
		notif.icon = R.drawable.logo_notification;
		notif.contentView = new RemoteViews(mContext.getPackageName(), R.layout.notify_transfer);
		notif.contentView.setTextViewText(R.id.content_view_text, mContext.getString(R.string.updating));
		manager.notify(Params.UPDATE_NOTIFICATION_ID, notif);
		
		message = Params.message;
		if(message == null || message.update_url == null)
			return 0;
		HttpGet httpRequest = new HttpGet(message.update_url);
		HttpClient httpClient = new DefaultHttpClient();
		try {
			HttpResponse httpResponse = httpClient.execute(httpRequest);
			HttpEntity entity = httpResponse.getEntity();
			length = entity.getContentLength();
			InputStream inputStream = entity.getContent();
			byte[] b = new byte[1024];
			int readedLength = -1;
			File file = new File(DirectoryUtil.downloadDirectory, Params.message.update_version + ".apk");
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
			return 1;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{}
		return 0;
	}
	 
	@Override
	protected void onProgressUpdate(Integer... values) {
		notif.contentView.setTextViewText(R.id.content_view_text, mContext.getString(R.string.updating_and_downloading)+values[0]+"%");
		notif.contentView.setProgressBar(R.id.content_view_progress, 100, values[0], false);
		manager.notify(Params.UPDATE_NOTIFICATION_ID, notif);
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(Integer result) {
		if(result == 1){
			manager.cancel(Params.UPDATE_NOTIFICATION_ID);
			Intent intent = new Intent();
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        intent.setAction(android.content.Intent.ACTION_VIEW);
	        intent.setDataAndType(Uri.fromFile(new File(DirectoryUtil.downloadDirectory + message.update_version + ".apk")), "application/vnd.android.package-archive");
	        mContext.startActivity(intent);
		}
        super.onPostExecute(result);
	}
	
}