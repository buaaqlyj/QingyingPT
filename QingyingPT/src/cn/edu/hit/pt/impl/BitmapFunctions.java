package cn.edu.hit.pt.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;

public class BitmapFunctions {
	
	public static Bitmap getBitmap(String sdcard_dir, String filename) {
		File file = new File(sdcard_dir);
		if (!file.exists()) {
			file.mkdir();
		}
		File imageFile = new File(file, filename);
		Bitmap bitmap = null;
		if (imageFile.exists()) {
			try {
				bitmap = BitmapFactory.decodeStream(new FileInputStream(
						imageFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return bitmap;
	}
	
	public static Bitmap getPicture(String picUrl, String sdcard_dir, String filename, boolean avatar) {
		URL url = null;
		URLConnection conn = null;
		InputStream in = null;
		Bitmap mBitmap = null;
		try {
			url = new URL(picUrl);
			conn = url.openConnection();
			conn.setConnectTimeout(3 * 1000);
			conn.connect();
		
			in = conn.getInputStream();
			mBitmap = BitmapFactory.decodeStream(in);
			if(mBitmap == null)
				return null;
			if(avatar == true)
				mBitmap = toRoundBitmap(mBitmap);
			storeInSD(mBitmap, sdcard_dir, filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (url != null) {
				url = null;
			}
			if (conn != null) {
				conn = null;
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return mBitmap;
	}
	
	public static void storeInSD(Bitmap bitmap, String sdcard_dir, String filename) {
		if(bitmap == null) return;
		File file = new File(sdcard_dir);
		if (!file.exists()) {
			file.mkdir();
		}
		File imageFile = new File(file, filename);
		try {
			imageFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(imageFile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Bitmap DrawableToBitmap(Drawable drawable) {

		Bitmap bitmap = Bitmap.createBitmap(
			drawable.getIntrinsicWidth(),
			drawable.getIntrinsicHeight(),
			drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}
	
	/** 
	* 转换图片成圆形 
	* @param bitmap 传入Bitmap对象 
	* @return 
	*/ 
	public static Bitmap toRoundBitmap(Bitmap bitmap) { 
		int width = bitmap.getWidth(); 
		int height = bitmap.getHeight(); 
		float roundPx; 
		float left,top,right,bottom,dst_left,dst_top,dst_right,dst_bottom; 
		if (width <= height) { 
			roundPx = width / 2; 
			top = 0; 
			bottom = width; 
			left = 0; 
			right = width; 
			height = width; 
			dst_left = 0; 
			dst_top = 0; 
			dst_right = width; 
			dst_bottom = width; 
		} else { 
			roundPx = height / 2; 
			float clip = (width - height) / 2; 
			left = clip; 
			right = width - clip; 
			top = 0; 
			bottom = height; 
			width = height; 
			dst_left = 0; 
			dst_top = 0; 
			dst_right = height; 
			dst_bottom = height; 
		} 
		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888); 
		Canvas canvas = new Canvas(output); 
		final int color = 0xff424242; 
		final Paint paint = new Paint(); 
		final Rect src = new Rect((int)left, (int)top, (int)right, (int)bottom); 
		final Rect dst = new Rect((int)dst_left, (int)dst_top, (int)dst_right, (int)dst_bottom); 
		final RectF rectF = new RectF(dst); 
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color); 
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint); 
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); 
		canvas.drawBitmap(bitmap, src, dst, paint); 
		return output; 
	}
	
	public static Drawable zoomDrawable(Drawable drawable, int w, int h){
		int width = drawable.getIntrinsicWidth();
		int height= drawable.getIntrinsicHeight();
		Bitmap oldbmp = DrawableToBitmap(drawable);// drawable转换成bitmap
		Matrix matrix = new Matrix(); // 创建操作图片用的Matrix对象
		float scaleWidth = ((float)w / width); // 计算缩放比例
		float scaleHeight = ((float)h / height);
		matrix.postScale(scaleWidth, scaleHeight); // 设置缩放比例
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true); //建立新的bitmap，其内容是对原bitmap的缩放后的图
		return new BitmapDrawable(null, newbmp);
	}

	/** 
	* 为二维码加水印
	* @param src 原Bitmap对象 
	* @param watermark 水印Bitmap对象 
	* @param title 水印标题 
	* @return 
	*/ 
	public static Bitmap watermarkBitmap(Context context, Bitmap src, Bitmap watermark, String title) {
		if(src == null)
			return null;
		int w = src.getWidth();
		int h = src.getHeight();
		Bitmap newb = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas cv = new Canvas(newb);
		cv.drawBitmap(src, 0, 0, null);
		Paint paint = new Paint();
		if (watermark != null) {
			if(watermark.getHeight() > 48 || watermark.getWidth() > 48){
				watermark = DrawableToBitmap(zoomDrawable(new BitmapDrawable(null, watermark), 48, 48));
			}
			int ww = watermark.getWidth();
			int wh = watermark.getHeight();
			//paint.setAlpha(50);
			cv.drawBitmap(watermark, (w - ww)/2, (h - wh)/2, paint);
		}
		//加入文字
		if(title!=null){
			String familyName ="宋体";
			Typeface font = Typeface.create(familyName,Typeface.BOLD);			
			TextPaint textPaint=new TextPaint();
			textPaint.setColor(Color.RED);
			textPaint.setTypeface(font);
			textPaint.setTextSize(22);
			//这里是自动换行的
			StaticLayout layout = new StaticLayout(title,textPaint,w,Alignment.ALIGN_NORMAL,1.0F,0.0F,true);
			layout.draw(cv);
			//文字就加左上角算了
			//cv.drawText(title,0,40,paint); 
		}
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		cv.restore();// 存储
		return newb;
	}
}
