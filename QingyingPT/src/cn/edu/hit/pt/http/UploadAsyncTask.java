package cn.edu.hit.pt.http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;
import cn.edu.hit.pt.Params;
import cn.edu.hit.pt.R;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.model.Attachment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UploadAsyncTask extends AsyncTask<Void, Integer, String>{
	private final static String TAG = "UploadAsyncTask";
	
	private Context mContext;
	private NotificationManager manager;  
	private Notification notif;
	private onUploadFinishedListener onUploadFinishedListener;

	private File file;
	private String RequestURL;
	private Map<String, String> param;
	
	private String PREFIX = "--";
	private String LINE_END = "\r\n";
	private String CONTENT_TYPE = "multipart/form-data"; // 内容类型
	private String BOUNDARY =  UUID.randomUUID().toString(); //边界标识 随机生成
	
	private int last_progress = 0;
	
	public UploadAsyncTask(Context c, String RequestURL, Map<String, String> param, File file){
		this.mContext = c;
		this.RequestURL = RequestURL;
		this.param = param;
		this.file = file;
	}
	
	public void setOnUploadFinishedListener(onUploadFinishedListener listener){
		this.onUploadFinishedListener = listener;
	}

	@Override
	protected String doInBackground(Void... voids) {
		manager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notif = new Notification();
		notif.icon = R.drawable.logo_notification;
		notif.contentView = new RemoteViews(mContext.getPackageName(), R.layout.notify_transfer);
		notif.contentView.setTextViewText(R.id.content_view_text, mContext.getString(R.string.uploading));
		notif.flags = Notification.FLAG_NO_CLEAR|Notification.FLAG_AUTO_CANCEL;
		Params.UPLOAD_NOTIFICATION_ID++;
		manager.notify(Params.UPLOAD_NOTIFICATION_ID, notif);
		
		try {
			URL url = new URL(RequestURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			//conn.setReadTimeout(readTimeOut);
			//conn.setConnectTimeout(connectTimeout);
			conn.setDoInput(true); // 允许输入流
			conn.setDoOutput(true); // 允许输出流
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST"); // 请求方式
			conn.setRequestProperty("Charset", "UTF-8"); // 设置编码
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			conn.setRequestProperty("Cookie", Params.cookie); 
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
			//conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			/**
			 * 当文件不为空，把文件包装并且上传
			 */
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			StringBuffer sb = null;
			String params = "";
			
			/***
			 * 以下是用于上传参数
			 */
			if (param != null && param.size() > 0) {
				Iterator<String> it = param.keySet().iterator();
				while (it.hasNext()) {
					sb = null;
					sb = new StringBuffer();
					String key = it.next();
					String value = param.get(key);
					sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
					sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(LINE_END).append(LINE_END);
					sb.append(value).append(LINE_END);
					params = sb.toString();
					//Log.i(TAG, key+"="+params+"##");
					dos.write(params.getBytes());
					//dos.flush();
				}
			}
			
			sb = null;
			params = null;
			sb = new StringBuffer();
			/**
			 * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
			 * filename是文件的名字，包含后缀名的 比如:abc.png
			 */
			sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
			sb.append("Content-Disposition:form-data; name=\"" + "file"
					+ "\"; filename=\"" + file.getName() + "\"" + LINE_END);
			sb.append("Content-Type:image/pjpeg" + LINE_END); // 这里配置的Content-type很重要的 ，用于服务器端辨别文件的类型的
			sb.append(LINE_END);
			params = sb.toString();
			sb = null;
			
			dos.write(params.getBytes());
			/**上传文件*/
			InputStream is = new FileInputStream(file);
			byte[] bytes = new byte[1024];
			int len = 0;
			int curLen = 0;
			last_progress = 0;
			while ((len = is.read(bytes)) != -1) {
				curLen += len;
				dos.write(bytes, 0, len);
				int progress = (int)(curLen*100/file.length());
				if(last_progress + 5 < progress){
					publishProgress(progress);
					last_progress = progress;
				}
			}
			is.close();
			
			dos.write(LINE_END.getBytes());
			byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
			dos.write(end_data);
			dos.flush();

			int res = conn.getResponseCode();
			if (res == 200) {
				InputStream input = conn.getInputStream();
				String result = Util.convertStreamToString(input);
				//Log.e(TAG, result);
				return result;
			} else {
				Log.e(TAG, "Upload Failed : Server error");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		notif.contentView.setTextViewText(R.id.content_view_text, mContext.getString(R.string.uploading_percent)+values[0]+"%");
		notif.contentView.setProgressBar(R.id.content_view_progress, 100, values[0], false);
		manager.notify(Params.UPLOAD_NOTIFICATION_ID, notif);
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(String json) {
		notif = new Notification();
		notif.icon = R.drawable.logo_notification;
		notif.flags = Notification.FLAG_AUTO_CANCEL;
		notif.contentView = new RemoteViews(mContext.getPackageName(), R.layout.notify_result);
		int error;
		Gson gson = new GsonBuilder().create();
		Attachment result = gson.fromJson(json, Attachment.class);
		if(result != null)
			error = result.error;
		else
			error = -1;
		if(error == 0){
			notif.contentView.setImageViewResource(R.id.content_view_image, R.drawable.ic_success);
			notif.contentView.setTextViewText(R.id.content_view_title, mContext.getString(R.string.upload_success));
		}else{
			notif.contentView.setImageViewResource(R.id.content_view_image, R.drawable.ic_error);
			notif.contentView.setTextViewText(R.id.content_view_title, mContext.getString(R.string.upload_failed));
		}
		switch (error) {
			case 1:
				notif.contentView.setTextViewText(R.id.content_view_text,  mContext.getString(R.string.upload_file_error));
				break;

			case 2:
				notif.contentView.setTextViewText(R.id.content_view_text,  mContext.getString(R.string.upload_file_oversized));
				break;

			case 3:
				notif.contentView.setTextViewText(R.id.content_view_text,  mContext.getString(R.string.upload_file_type_error));
				break;

			case 4:
				notif.contentView.setTextViewText(R.id.content_view_text,  mContext.getString(R.string.upload_file_no_count_left));
				break;

			case 5:
				notif.contentView.setTextViewText(R.id.content_view_text,  mContext.getString(R.string.upload_file_invalid_image));
				break;

			case 6:
				notif.contentView.setTextViewText(R.id.content_view_text,  mContext.getString(R.string.upload_file_temp_file_cannot_moved));
				break;

			case 7:
				notif.contentView.setTextViewText(R.id.content_view_text,  mContext.getString(R.string.upload_file_attachment_disabled));
				break;
				
			case -1:
				notif.contentView.setTextViewText(R.id.content_view_text,  mContext.getString(R.string.server_error));
				break;

			default:
				if(result.limit > 0)
					notif.contentView.setTextViewText(R.id.content_view_text, mContext.getString(R.string.upload_limit) + result.limit + mContext.getString(R.string.unit)
							+ mContext.getString(R.string.comma) + mContext.getString(R.string.upload_left) + result.left + mContext.getString(R.string.unit));
				break;
		}
		manager.notify(Params.UPLOAD_NOTIFICATION_ID, notif);
		if(result != null)
			onUploadFinishedListener.onFinished(result);
	}

	public interface onUploadFinishedListener{
		public void onFinished(Attachment result);
	}
}
