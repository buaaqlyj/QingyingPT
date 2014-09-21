package cn.edu.hit.pt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.edu.hit.pt.http.URLContainer;

public class TopicMenu extends PopupWindow{
	private LinearLayout rlOnlyAuthor;
	private LinearLayout rlReverseOrder;
	private LinearLayout rlQRCode;
	private LinearLayout rlShare;

	private ImageView ivOnlyAuthor;
	private ImageView ivReverseOrder;
	private TextView tvOnlyAuthor;
	private TextView tvReverseOrder;
	
	private String subject;
	
	public TopicMenu(Context context){
		super(context);
	}

	public TopicMenu(final Context context, long topicid, final String subject){
		super(context);
		this.subject = subject;
		final String url = URLContainer.BASEURL_ALTERNATE + "forums.php?action=viewtopic&topicid=" + topicid;
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mView = inflater.inflate(R.layout.topic_menu, null);
		rlOnlyAuthor = (LinearLayout)mView.findViewById(R.id.rlOnlyAuthor);
		rlReverseOrder = (LinearLayout)mView.findViewById(R.id.rlReverseOrder);
		rlQRCode = (LinearLayout)mView.findViewById(R.id.rlQRCode);
		rlShare = (LinearLayout)mView.findViewById(R.id.rlShare);
		ivOnlyAuthor = (ImageView)mView.findViewById(R.id.ivOnlyAuthor);
		ivReverseOrder = (ImageView)mView.findViewById(R.id.ivReverseOrder);
		tvOnlyAuthor = (TextView)mView.findViewById(R.id.tvOnlyAuthor);
		tvReverseOrder = (TextView)mView.findViewById(R.id.tvReverseOrder);

		this.setContentView(mView);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setAnimationStyle(R.style.FadeTransAnimation);
		this.setTouchable(true);
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		this.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.menu_bg));
		this.update();
		
		if(((TopicPosts) context).order.equals("reverse")){
			ivReverseOrder.setImageDrawable(context.getResources().getDrawable(R.drawable.button_reverse_order));
			tvReverseOrder.setText(R.string.normal_order);
		}else{
			ivReverseOrder.setImageDrawable(context.getResources().getDrawable(R.drawable.button_order));
			tvReverseOrder.setText(R.string.reverse_order);
		}
		
		if(!((TopicPosts) context).author.equals("author")){
			ivOnlyAuthor.setImageDrawable(context.getResources().getDrawable(R.drawable.button_only_author));
			tvOnlyAuthor.setText(R.string.only_author);
		}else{
			ivOnlyAuthor.setImageDrawable(context.getResources().getDrawable(R.drawable.button_only_author_checked));
			tvOnlyAuthor.setText(R.string.view_all);
		}
		
		rlReverseOrder.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View v) {
				if(!((TopicPosts) context).order.equals("reverse")){
					((TopicPosts) context).order = "reverse";
					((TopicPosts) context).if_last_post = true;
					((TopicPosts) context).author = "";
				}else{
					((TopicPosts) context).order = "";
				}
				((TopicPosts) context).last_postid = 0;
				((TopicPosts) context).floor_total = 0;
				((TopicPosts) context).mPullToRefreshView.setHeaderRefreshing();
				dismiss();
			}
		});
		
		rlOnlyAuthor.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!((TopicPosts) context).author.equals("author")){
					((TopicPosts) context).author = "author";
					((TopicPosts) context).order = "";
				}else{
					((TopicPosts) context).author = "";
				}
				((TopicPosts) context).last_postid = 0;
				((TopicPosts) context).floor_total = 0;
				((TopicPosts) context).mPullToRefreshView.setHeaderRefreshing();
				dismiss();
			}
		});
		
		rlQRCode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
		        intent.putExtra("text", url);
		        intent.putExtra("type", QRCodeMaker.TYPE_TOPIC);
		        intent.setClass(context, QRCodeMaker.class);
		        context.startActivity(intent);
		        dismiss();
			}
		});

		rlShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
		        intent.setType("text/plain");
		        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.shareTitle));
		        intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.shareTopic) + TopicMenu.this.subject + context.getString(R.string.linkAddress) + url);    
		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
		        context.startActivity(Intent.createChooser(intent, ((Activity)context).getTitle()));
		        dismiss();
			}
		});
	}
}
