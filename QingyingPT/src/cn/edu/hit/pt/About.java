package cn.edu.hit.pt;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.edu.hit.pt.impl.ApplicationInfo;

public class About extends SwipeBackActivity {
	private Button btnReturn;
	private TextView tvVersion;
	private LinearLayout rlSpecialThanks;
	private LinearLayout rlReport;
	private LinearLayout rlDonate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		SwipeBackLayout mSwipeBackLayout;
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
		
		btnReturn = (Button)findViewById(R.id.btnReturn);
		btnReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		rlSpecialThanks = (LinearLayout)findViewById(R.id.rlSpecialThanks);
		rlSpecialThanks.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(About.this, SpecialThanks.class);
				startActivity(intent);
			}
		});
		
		rlReport = (LinearLayout)findViewById(R.id.rlReport);
		rlReport.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				//intent.putExtra("topicid", "10051");
				intent.setClass(About.this, Report.class);
				startActivity(intent);
			}
		});
		
		rlDonate = (LinearLayout)findViewById(R.id.rlDonate);
		rlDonate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.putExtra("title", getString(R.string.make_donations));
				intent.putExtra("doc", "makeDonation");
				intent.setClass(About.this, DocViewer.class);
				startActivity(intent);
			}
		});
		
		tvVersion = (TextView)findViewById(R.id.tvVersion);
		
		ApplicationInfo info = new ApplicationInfo(this);
		String version = info.getVersionName();
		tvVersion.setText(version);
	}

}