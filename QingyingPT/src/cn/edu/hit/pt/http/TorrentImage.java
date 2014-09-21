package cn.edu.hit.pt.http;

import java.util.concurrent.FutureTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import cn.edu.hit.pt.Params;
import cn.edu.hit.pt.R;
import cn.edu.hit.pt.impl.BitmapFunctions;
import cn.edu.hit.pt.impl.DirectoryUtil;
import cn.edu.hit.pt.impl.MD5Util;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.parser.BitmapParser;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

public class TorrentImage{
	public final static int TYPE_SMALL = 0;
	public final static int TYPE_BIG = 1;
	public Context context;
	public FutureTask<Response> task;
	public String fileName;
	public String imageType;
	
	public TorrentImage(Context context, ImageView ivThumb, long id, int type){
		this.context = context;
		setImage(ivThumb, id, type);
	}
	
	public void setImage(ImageView ivThumb, long id, int type) {
		if(Params.userSettings.nopic == true){
			ivThumb.setImageResource(R.drawable.img_nopic);
			return;
		}else if(DirectoryUtil.torrentImageDirectory.equals("")){
			ivThumb.setImageResource(R.drawable.img_broken);
			return;
		}
		switch (type) {
			case TYPE_SMALL:
				fileName = MD5Util.MD5("Torrent:" + id) + ".jpg";
				imageType = "small";
				break;
			case TYPE_BIG:
				fileName = MD5Util.MD5("Torrent:" + id) + "_big.jpg";
				imageType = "big";
				break;
	
			default:
				return;
		}
		Bitmap bitmap = BitmapFunctions.getBitmap(DirectoryUtil.torrentImageDirectory, fileName);
		if (bitmap == null){
			task = execute(ivThumb, id);
		}else{
			ivThumb.setImageBitmap(bitmap);
		}
	}
	
	public FutureTask<Response> execute(final ImageView ivThumb, final long id) {
		HttpAsyncExecutor asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(context));
		MyHttpRequest request = new MyHttpRequest(URLContainer.getTorrentImg(id, imageType));
		return asyncExecutor.execute(request.setDataParser(new BitmapParser()), new HttpResponseHandler() {
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				Bitmap bitmap = res.getBitmap();
				if(bitmap != null){
					ivThumb.setImageBitmap(bitmap);
					BitmapFunctions.storeInSD(bitmap, DirectoryUtil.torrentImageDirectory, fileName);
				}else{
					ivThumb.setImageResource(R.drawable.img_broken);
				}
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				ivThumb.setImageResource(R.drawable.img_broken);
			}
        });
	}
	
	public void cancel() {
		if (task != null)
			task.cancel(true);
	}
}
