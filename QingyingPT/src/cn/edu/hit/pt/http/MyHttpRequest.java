package cn.edu.hit.pt.http;

import cn.edu.hit.pt.Params;

import com.litesuits.http.request.Request;

public class MyHttpRequest extends Request{

	public MyHttpRequest(String url) {
		super(url);
		this.addHeader("cookie", Params.cookie);
	}

}
