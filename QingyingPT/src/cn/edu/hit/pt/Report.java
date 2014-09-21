package cn.edu.hit.pt;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.FutureTask;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.http.UploadAsyncTask;
import cn.edu.hit.pt.http.UploadAsyncTask.onUploadFinishedListener;
import cn.edu.hit.pt.impl.ApplicationInfo;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.model.Attachment;
import cn.edu.hit.pt.model.Post;
import cn.edu.hit.pt.widget.ImplementsBar;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.request.content.UrlEncodedFormBody;
import com.litesuits.http.request.param.HttpMethod;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

public class Report extends SwipeBackActivity{
	private final static String report_topicid = "10051";
	private Button btnReturn;
	private Button btnSend;
	private TextView tvModel;
	private EditText etDescr;
	private TextView tvVersion;
	private Button addSmile;
	private Button addOther;
	private ImplementsBar implementsBar;
	
	private String body;
	private String model;
	private String version;
	private HttpAsyncExecutor asyncExecutor;
	private FutureTask<Response> httpTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report);
		
		SwipeBackLayout mSwipeBackLayout;
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

		btnSend = (Button)findViewById(R.id.btnSend);
		btnReturn = (Button)findViewById(R.id.btnReturn);
		etDescr = (EditText)findViewById(R.id.etDescr);
		tvModel = (TextView)findViewById(R.id.tvModel);
		addSmile = (Button)findViewById(R.id.addSmile);
		addOther = (Button)findViewById(R.id.addOther);

		implementsBar = (ImplementsBar)findViewById(R.id.implementsBar);
		implementsBar.setFragmentTransaction(getSupportFragmentManager());
		implementsBar.setTargetView(etDescr);
		
		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(this));
		
		
		tvVersion = (TextView)findViewById(R.id.tvVersion);
		ApplicationInfo info = new ApplicationInfo(this);
		version = info.getVersionName();
		tvVersion.setText(version);
		
		model = Build.MODEL + ", API " + Build.VERSION.SDK_INT + ", " + Build.VERSION.RELEASE;
		tvModel.setText(model);
		
		btnReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		btnSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(model.equals("")){
					Toast.makeText(Report.this, getString(R.string.cellphone_model_null), Toast.LENGTH_SHORT).show();
				}else if(etDescr.getText().toString().equals("")){
					Toast.makeText(Report.this, getString(R.string.cellphone_model_null), Toast.LENGTH_SHORT).show();
				}else{
					body = getString(R.string.version) + version + "\n"
							+ getString(R.string.cellphone_model) + model + "\n"
							+ getString(R.string.problem_descr) + etDescr.getText().toString();
					
					addPost();
				}
			}
		});
		
		addOther.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				implementsBar.fold(ImplementsBar.ITEM_ATTACHMENT);
			}
		});

		addSmile.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				implementsBar.fold(ImplementsBar.ITEM_SMILIES);
			}
		});
		
	}
	
	public void addPost() {
		btnSend.setEnabled(false);
		File file = (File) etDescr.getTag();
		if(etDescr.getTag() != null){
			Map<String, String> params = new HashMap<String, String>();
			params.put("type", "attachment");
			UploadAsyncTask uploadTask = new UploadAsyncTask(this, URLContainer.getUploadURL(), params, file);
			uploadTask.setOnUploadFinishedListener(new onUploadFinishedListener() {
				
				@Override
				public void onFinished(Attachment result) {
					if(result.error == 0){
						etDescr.setTag(null);
						implementsBar.clearCounter();
						body = body + "\n[attach]" + result.dlkey + "[/attach]";
						postReply();
					}else{
						btnSend.setEnabled(true);
					}
				}
			});
			uploadTask.execute();
		}else if(!body.equals("")){
			postReply();
		}else{
			Toast.makeText(getApplicationContext(), R.string.no_subject_body, Toast.LENGTH_SHORT).show();
		}
	}
	
	public void postReply() {
		MyHttpRequest request = new MyHttpRequest(URLContainer.getPostURL());
		request.setMethod(HttpMethod.Post);
        LinkedList<NameValuePair> pList = new LinkedList<NameValuePair>();
        pList.add(new NameValuePair("id", report_topicid));
        pList.add(new NameValuePair("type", "reply"));
        pList.add(new NameValuePair("body", body));
        request.setHttpBody(new UrlEncodedFormBody(pList, "UTF-8"));
        httpTask = asyncExecutor.execute(request, new HttpResponseHandler() {
			
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				Post post = res.getObject(Post.class);
				if(post != null && Util.checkPostResult(Report.this, post.result) == true){
					finish();
				}else if(post == null){
					Toast.makeText(Report.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
				}
				btnSend.setEnabled(true);
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(Report.this).handleException(e);
				btnSend.setEnabled(true);
			}
		});
	}

	@Override
	public void finish() {
		if(httpTask != null) httpTask.cancel(true);
		super.finish();
	}
}
