package cn.edu.hit.pt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import cn.edu.hit.pt.http.URLContainer;

@SuppressLint("InflateParams")
public class UserInfoMenu extends PopupWindow{
	private LinearLayout rlQRCode;

	public UserInfoMenu(Context context){
		super(context);
	}

	public UserInfoMenu(final Context context, long userid){
		super(context);
		final String url = URLContainer.BASEURL_ALTERNATE + "userdetails.php?id=" + userid;
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mView = inflater.inflate(R.layout.user_info_menu, null);
		rlQRCode = (LinearLayout)mView.findViewById(R.id.rlQRCode);

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
		        intent.putExtra("type", QRCodeMaker.TYPE_USER);
		        intent.setClass(context, QRCodeMaker.class);
		        context.startActivity(intent);
		        dismiss();
			}
		});
	}
}
