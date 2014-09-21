package cn.edu.hit.pt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import cn.edu.hit.pt.impl.BitmapFunctions;
import cn.edu.hit.pt.impl.DirectoryUtil;
import cn.edu.hit.pt.impl.MD5Util;

public class Splash extends Activity{
	private ImageView ivAvatar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		ivAvatar = (ImageView)findViewById(R.id.ivAvatar);
		
		String md5 = MD5Util.MD5("Avatar:" + Params.CURUSER.id);
		Bitmap bitmap = BitmapFunctions.getBitmap(DirectoryUtil.avatarDirectory, md5 + ".jpg");
		if(bitmap != null)
			ivAvatar.setImageBitmap(bitmap);
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				startActivity(new Intent(Splash.this, MainActivity.class));
				finish();
			}
		}, 2000);
//		new Handler().postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
//				finish();
//			}
//		}, 2001);
	}
}
