package cn.edu.hit.pt;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.FutureTask;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.http.UploadAsyncTask;
import cn.edu.hit.pt.http.UploadAsyncTask.onUploadFinishedListener;
import cn.edu.hit.pt.impl.DatabaseUtil;
import cn.edu.hit.pt.impl.ScaleUtil;
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

public class AddPost extends SwipeBackActivity {
	private String subject;
	private String body;
	private View mView;
	private EditText etSubject;
	private EditText etBody;
	private Button btnReturn;
	private Button sendPost;
	private TextView tvForumList;
	private Button addSmile;
	private Button addOther;
	private ImplementsBar implementsBar;
	private RelativeLayout first_tip;
	
	public static int id = 0;
	public static String name = "";
	public static boolean if_load = false;

	private HttpAsyncExecutor asyncExecutor;
	private FutureTask<Response> httpTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_post);
		LayoutInflater inflater = LayoutInflater.from(this);
		mView = inflater.inflate(R.layout.add_post, null);
		etSubject = (EditText)findViewById(R.id.etSubject);
		etBody = (EditText)findViewById(R.id.etBody);
		btnReturn = (Button)findViewById(R.id.btnReturn);
		tvForumList = (TextView)findViewById(R.id.tvForumList);
		first_tip = (RelativeLayout)findViewById(R.id.first_tip);
		addSmile = (Button)findViewById(R.id.addSmile);
		addOther = (Button)findViewById(R.id.addOther);
		
		SwipeBackLayout mSwipeBackLayout;
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

		implementsBar = (ImplementsBar)findViewById(R.id.implementsBar);
		implementsBar.setFragmentTransaction(getSupportFragmentManager());
		implementsBar.setTargetView(etBody);
		
		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(this));
		
		Intent intent = getIntent();
		try {
			id = Integer.parseInt(intent.getStringExtra("forumid"));
		} catch (NumberFormatException e) {
			id = 8;
		}
		name = intent.getStringExtra("forumname");
		if((id == 8)||(name.equals(getString(R.string.latest_topics))))
			tvForumList.setText(getString(R.string.default_forum));
		else if(!name.equals(""))
			tvForumList.setText(name);
		
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
		
		btnReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		sendPost = (Button)findViewById(R.id.sendPost);
		sendPost.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				subject = etSubject.getText().toString();
				body = etBody.getText().toString();
				if(subject.equals("")){
					Toast.makeText(getApplicationContext(), R.string.no_subject_body, Toast.LENGTH_SHORT).show();
				}else{
		        	addPost();
				}
			}
		});
		
		tvForumList.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ForumsRadio sWindow = new ForumsRadio(AddPost.this);
				sWindow.showAtLocation(mView, Gravity.RIGHT|Gravity.BOTTOM, 0, ScaleUtil.Dp2Px(AddPost.this, 30));
				sWindow.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss() {
						if(if_load == true){
							tvForumList.setText(name);
							if_load = false;
						}
					}
				});
			}
		});
		
		if(Params.tips.addpost == false){
			first_tip.setVisibility(View.VISIBLE);
			Params.tips.addpost = true;
			DatabaseUtil.userDatabase(AddPost.this).save(Params.tips);
		}
		first_tip.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				first_tip.setVisibility(View.GONE);
			}
		});
	}
	
	public void addPost() {
    	sendPost.setEnabled(false);
		File file = (File) etBody.getTag();
		if(etBody.getTag() != null){
			Map<String, String> params = new HashMap<String, String>();
			params.put("type", "attachment");
			UploadAsyncTask uploadTask = new UploadAsyncTask(this, URLContainer.getUploadURL(), params, file);
			uploadTask.setOnUploadFinishedListener(new onUploadFinishedListener() {
				
				@Override
				public void onFinished(Attachment result) {
					if(result.error == 0){
						etBody.setTag(null);
						implementsBar.clearCounter();
						body = body + "\n[attach]" + result.dlkey + "[/attach]";
						postText();
					}else{
						sendPost.setEnabled(true);
					}
				}
			});
			uploadTask.execute();
		}else if(!body.equals("")){
			postText();
		}else{
			Toast.makeText(getApplicationContext(), R.string.no_subject_body, Toast.LENGTH_SHORT).show();
		}
	}

	public void postText(){
		MyHttpRequest request = new MyHttpRequest(URLContainer.getPostURL());
		request.setMethod(HttpMethod.Post);
        LinkedList<NameValuePair> pList = new LinkedList<NameValuePair>();
        pList.add(new NameValuePair("id",String.valueOf(id)));
        pList.add(new NameValuePair("type","new"));
        pList.add(new NameValuePair("subject",subject));
        pList.add(new NameValuePair("body",body));
        request.setHttpBody(new UrlEncodedFormBody(pList, "UTF-8"));
        httpTask = asyncExecutor.execute(request, new HttpResponseHandler() {
			
			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				Post post = res.getObject(Post.class);
				if(post != null && Util.checkPostResult(AddPost.this, post.result) == true){
					finish();
				}else if(post == null){
					Toast.makeText(AddPost.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
			    }
				sendPost.setEnabled(true);
			}
			
			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(AddPost.this).handleException(e);
				sendPost.setEnabled(true);
			}
		});
	}
	
	@Override
	public void finish() {
		if(httpTask != null) httpTask.cancel(true);
		super.finish();
	}
}
