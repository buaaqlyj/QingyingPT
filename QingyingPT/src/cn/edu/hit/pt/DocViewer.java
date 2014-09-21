package cn.edu.hit.pt;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class DocViewer extends SwipeBackActivity {
	private WebView mWebView;
	private Button btnReturn;
	private TextView tvTitle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.doc_viewer);
		
		SwipeBackLayout mSwipeBackLayout;
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
		
		tvTitle = (TextView)findViewById(R.id.tvTitle);
		
		Intent intent = getIntent();
		String title = intent.getStringExtra("title");
		String doc = intent.getStringExtra("doc");
		if(doc.equals(""))
			finish();
		if(!title.equals(""))
			tvTitle.setText(title);
		
		btnReturn = (Button)findViewById(R.id.btnReturn);
		btnReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mWebView = (WebView) findViewById(R.id.mWebView);
		WebSettings webSettings = mWebView.getSettings();       
		webSettings.setJavaScriptEnabled(false);
		mWebView.loadUrl("file:///android_asset/doc/"+doc+".html");
	}

}
