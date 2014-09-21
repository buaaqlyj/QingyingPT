package cn.edu.hit.pt.http;

import android.content.Context;
import android.widget.Toast;
import cn.edu.hit.pt.R;

import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.exception.HttpClientException;
import com.litesuits.http.exception.HttpClientException.ClientException;
import com.litesuits.http.exception.HttpNetException;
import com.litesuits.http.exception.HttpNetException.NetException;
import com.litesuits.http.exception.HttpServerException;
import com.litesuits.http.exception.HttpServerException.ServerException;
import com.litesuits.http.response.handler.HttpExceptionHandler;

public class MyHttpExceptionHandler extends HttpExceptionHandler{
	public Context context;
	
	public MyHttpExceptionHandler(Context context){
		this.context = context;
	}
	
	public void makeToast(String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
	
    @Override
    protected void onClientException(HttpClientException e, ClientException type) {
    	e.printStackTrace();
    }

    @Override
    protected void onNetException(HttpNetException e, NetException type) {
    	if (context != null){
            if (type == NetException.NetworkError) {
            	makeToast(context.getResources().getString(R.string.no_internet_error));
            } else if (type == NetException.UnReachable) {
            	makeToast(context.getResources().getString(R.string.no_internet_unreachable));
            } else if (type == NetException.NetworkDisabled) {
            	makeToast(context.getResources().getString(R.string.no_internet_disabled));
            }
    	}
    }

	@Override
	protected void onServerException(HttpServerException e, ServerException type, HttpStatus status) {
		if (context != null){
			switch (status.getCode()) {
			case 404:
				makeToast(context.getResources().getString(R.string.server_not_found));
				break;

			default:
				break;
			}
		}
	}

}
