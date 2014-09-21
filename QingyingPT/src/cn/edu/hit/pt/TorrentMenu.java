package cn.edu.hit.pt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import cn.edu.hit.pt.http.URLContainer;

public class TorrentMenu extends PopupWindow{
	private LinearLayout rlQRCode;
	private LinearLayout rlShare;
	
	public TorrentMenu(Context context){
		super(context);
	}

	public TorrentMenu(final Context context, long topicid, final String subject){
		super(context);
		final String url = URLContainer.BASEURL_ALTERNATE + "details.php?hit=1&id=" + topicid;
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mView = inflater.inflate(R.layout.torrent_menu, null);
		rlQRCode = (LinearLayout)mView.findViewById(R.id.rlQRCode);
		rlShare = (LinearLayout)mView.findViewById(R.id.rlShare);

		this.setContentView(mView);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setAnimationStyle(R.style.FadeTransAnimation);
		this.setTouchable(true);
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		this.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.menu_bg));
		this.update();
		
		rlQRCode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
		        intent.putExtra("text", url);
		        intent.putExtra("type", QRCodeMaker.TYPE_TORRENT);
		        intent.setClass(context, QRCodeMaker.class);
		        context.startActivity(intent);
		        dismiss();
			}
		});

		rlShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(Intent.ACTION_SEND);   
		        intent.setType("text/plain");
		        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.shareTitle));   
		        intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.shareDescr) + subject + context.getString(R.string.linkAddress) + url);    
		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
		        context.startActivity(Intent.createChooser(intent, ((Activity) context).getTitle()));
		        dismiss();
			}
		});
	}
}
