package cn.edu.hit.pt.impl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.edu.hit.pt.R;

public class CustomAlert{
	public static final int TYPE_SUCCESS = 1;
	public static final int TYPE_INFO = 2;
	public static final int TYPE_WARNING = 3;
	public static final int TYPE_DANGER = 4;
	Context context;
	LinearLayout mLayout;
	TextView tvContent;
	TextView tvClose;
	View border;

	public CustomAlert(Context context) {
		this.context = context;
	}

	public LinearLayout init(int type, String content) {
		LayoutInflater inflater = LayoutInflater.from(context);
		mLayout = (LinearLayout)inflater.inflate(R.layout.custom_alert, null);
		tvContent = (TextView) mLayout.findViewById(R.id.tvContent);
		tvClose = (TextView) mLayout.findViewById(R.id.tvClose);
		border = (View) mLayout.findViewById(R.id.border);
		tvClose.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mLayout.setVisibility(View.GONE);
			}
		});
		setType(type);
		setMessage(content);
		return mLayout;
	}
	
	public void setType(int type) {
		switch (type) {
		case TYPE_SUCCESS:
			mLayout.setBackgroundResource(R.color.alert_success_bg);
			tvContent.setTextColor(context.getResources().getColor(R.color.alert_success_text));
			tvClose.setTextColor(context.getResources().getColor(R.color.alert_success_text));
			border.setBackgroundResource(R.color.alert_success_border);
			break;
		case TYPE_WARNING:
			mLayout.setBackgroundResource(R.color.alert_warning_bg);
			tvContent.setTextColor(context.getResources().getColor(R.color.alert_warning_text));
			tvClose.setTextColor(context.getResources().getColor(R.color.alert_warning_text));
			border.setBackgroundResource(R.color.alert_warning_border);
			break;
		case TYPE_DANGER:
			mLayout.setBackgroundResource(R.color.alert_danger_bg);
			tvContent.setTextColor(context.getResources().getColor(R.color.alert_danger_text));
			tvClose.setTextColor(context.getResources().getColor(R.color.alert_danger_text));
			border.setBackgroundResource(R.color.alert_danger_border);
			break;

		default:
		case TYPE_INFO:
			mLayout.setBackgroundResource(R.color.alert_info_bg);
			tvContent.setTextColor(context.getResources().getColor(R.color.alert_info_text));
			tvClose.setTextColor(context.getResources().getColor(R.color.alert_info_text));
			border.setBackgroundResource(R.color.alert_info_border);
			break;
		}
	}
	
	public void setMessage(String content) {
		tvContent.setText(content);
	}
	
	public void close() {
		mLayout.setVisibility(View.GONE);
	}
}
