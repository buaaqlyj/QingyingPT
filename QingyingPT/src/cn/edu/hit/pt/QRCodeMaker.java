package cn.edu.hit.pt;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import cn.edu.hit.pt.impl.BitmapFunctions;
import cn.edu.hit.pt.impl.DirectoryUtil;
import cn.edu.hit.pt.impl.MD5Util;

import com.google.zxing.WriterException;
import com.google.zxing.client.android.ZXingUtil;

public class QRCodeMaker extends SwipeBackActivity{
	public final static int TYPE_TOPIC = 0;
	public final static int TYPE_TORRENT = 1;
	public final static int TYPE_USER = 2;
	
	private ImageView ivQRCode;
	private Button btnSave;
	private Bitmap qrcodeBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qrcode_maker);
		ivQRCode = (ImageView)findViewById(R.id.ivQRCode);
		btnSave = (Button)findViewById(R.id.btnSave);
		
		SwipeBackLayout mSwipeBackLayout;
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);	
		
		Intent intent = getIntent();
		String text = intent.getStringExtra("text");
		int type = intent.getIntExtra("type", TYPE_TOPIC);
		if(text == null || text.equals(""))
			Toast.makeText(this, getString(R.string.invalid_qrcode_text), Toast.LENGTH_SHORT).show();
		try {
			Bitmap bitmap = ZXingUtil.Create2DCode(text);
			Bitmap watermark = null;
			switch (type) {
				case 0:
				case 1:
					watermark = BitmapFactory.decodeResource(getResources(), R.drawable.watermark);
					break;
				case 2:
					String md5 = MD5Util.MD5("Avatar:" + Params.CURUSER.id);
					watermark = BitmapFunctions.getBitmap(DirectoryUtil.avatarDirectory, md5 + ".jpg");
					break;
	
				default:
					finish();
					break;
			}
			
			qrcodeBitmap = BitmapFunctions.watermarkBitmap(this, bitmap, watermark, null);
			ivQRCode.setImageBitmap(qrcodeBitmap);
			
			btnSave.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(qrcodeBitmap == null){
						Toast.makeText(QRCodeMaker.this, getString(R.string.generate_qrcode_failed), Toast.LENGTH_SHORT).show();
						return;
					}
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
					String filename = getString(R.string.qrcode) + df.format(new Date()) + ".jpg";
					BitmapFunctions.storeInSD(qrcodeBitmap, DirectoryUtil.qrcodeDirectory, filename);
					Toast.makeText(QRCodeMaker.this, getString(R.string.qrcode_saved) + DirectoryUtil.qrcodeDirectory + filename, Toast.LENGTH_LONG).show();
				}
			});
			
		} catch (WriterException e) {
			Toast.makeText(this, getString(R.string.generate_qrcode_failed), Toast.LENGTH_SHORT).show();
		}
	}

}
