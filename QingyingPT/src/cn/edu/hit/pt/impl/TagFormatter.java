package cn.edu.hit.pt.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import cn.edu.hit.pt.R;

public class TagFormatter {	
	private Context c;
	private String s;
	
	public TagFormatter(Context c){
		this.c = c;
	}
	
	public TagFormatter(Context c, String s){
		this(c);
		this.s = s;
	}
	
	public void setText(String s){
		this.s = s;
	}
	
	public class Marker{
		public String key;
		public int start;
		public int end;
		
		public Marker(String key, int start, int end){
			this.key = key;
			this.start = start;
			this.end = end;
		}
	}
	
	public ArrayList<Marker> findMarks(String s, String style){
		ArrayList<Marker> marks = new ArrayList<Marker>();
		int lstart = 0;
		int mlen = 0;
		String str = s;
		Pattern pattern = Pattern.compile(style, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			lstart += matcher.start(0) + mlen;
			mlen = matcher.end(0) - matcher.start(0);
			Marker marker = new Marker(matcher.group(2), lstart, lstart + mlen);
			marks.add(marker);
			if(matcher.end(0) < str.length()){
				str = str.substring(matcher.end(0), str.length());
			}else{
				break;
			}
			matcher = pattern.matcher(str);
		}
		return marks;
	}
	
	public SpannableString format(){
		SpannableString ss = new SpannableString(s.toString());
		ArrayList<Marker> markerSmilies = findMarks(s, "(\\[em([\\w]+)\\]){1}?");
		ArrayList<Marker> markerATs = findMarks(s, "(\\[@([\\w]+)\\]){1}?");
		
		//处理表情
		if(markerSmilies.size() > 0){
			for (Marker marker : markerSmilies) {
				try {
					InputStream is = c.getAssets().open("smilies/" + marker.key + ".gif");
					Drawable d = Drawable.createFromResourceStream(c.getResources(), null, is, "");
					float ratio = (float)d.getIntrinsicHeight()/(float)d.getIntrinsicWidth();
					int scaleWidth = ScaleUtil.Dp2Px(c, d.getIntrinsicWidth()-5);
					int scaleHeight = ScaleUtil.Dp2Px(c, (int)(d.getIntrinsicHeight()-5*ratio));
					d.setBounds(0, 0, scaleWidth, scaleHeight);
					ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
					ss.setSpan(span, marker.start, marker.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		//处理@
		if(markerATs.size() > 0){
			for (Marker marker : markerATs) {
				Bitmap b = getAtTagImage(marker.key);
				Drawable drawable = new BitmapDrawable(null, b) ;
				int scaleWidth = ScaleUtil.Dp2Px(c, drawable.getIntrinsicWidth());
				int scaleHeight = ScaleUtil.Dp2Px(c, drawable.getIntrinsicHeight());
				drawable.setBounds(0, 0, scaleWidth, scaleHeight);
				ImageSpan aspan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
				ss.setSpan(aspan, marker.start, marker.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return ss;
	}
	
	public Bitmap getAtTagImage(String name) {
		name = "@" + name;
		int font_size = c.getResources().getDimensionPixelSize(R.dimen.at_tag_text_size);
		int padding =  c.getResources().getDimensionPixelSize(R.dimen.at_tag_padding);
		int radius =  c.getResources().getDimensionPixelSize(R.dimen.at_tag_radius);
		int height = c.getResources().getDimensionPixelSize(R.dimen.at_tag_height) + padding * 2;
		
        Paint paint = new Paint();
        paint.setTextSize(font_size);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.SANS_SERIF);
        paint.setAntiAlias(true);
        Rect rect = new Rect();
        paint.getTextBounds(name, 0, name.length(), rect);
		int width = rect.width() + padding * 4;
        
        Bitmap newbit = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newbit);
        FontMetrics metrics = paint.getFontMetrics();

		Rect rectBorder = new Rect(padding, padding, width - padding, height - padding);
		RectF rectFBorder = new RectF(rectBorder);
		paint.setColor(c.getResources().getColor(R.color.border_grey)); 
		canvas.drawRoundRect(rectFBorder, radius, radius, paint);
		Rect rectBackground = new Rect(padding + 1, padding + 1, width - padding - 1, height - padding - 1);
		RectF rectFBackground = new RectF(rectBackground);
		paint.setColor(c.getResources().getColor(R.color.bg_color_white_grey)); 
		canvas.drawRoundRect(rectFBackground, radius, radius, paint);

        paint.setColor(Color.parseColor("#0b69b8"));
        canvas.drawText(name, padding * 2, height - metrics.bottom - padding * 2, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);

        canvas.restore();
        return newbit;
	}
}
