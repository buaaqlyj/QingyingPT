package cn.edu.hit.pt.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;
import cn.edu.hit.pt.R;
import cn.edu.hit.pt.ShoutboxActivity;
import cn.edu.hit.pt.TopicPosts;
import cn.edu.hit.pt.TorrentActivity;
import cn.edu.hit.pt.ViewMail;
import cn.edu.hit.pt.impl.ScaleUtil;
import cn.edu.hit.pt.impl.Util;
import cn.edu.hit.pt.widget.FlashPlayerActivity;
import cn.edu.hit.pt.widget.ImageViewActivity;

public class UrlTagHandler implements TagHandler {
	Context context;
    TextView container;
    private int startIndex = 0;
    private int stopIndex = 0;
    
	public UrlTagHandler(TextView t, Context context) {
		this.context = context;
        this.container = t;
	}
	
	@Override
	public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
		if(tag.equalsIgnoreCase("emoticon")) {
			if(opening)
				startTxt(tag, output, xmlReader);
			else
				endTxt(tag, output, xmlReader, TagType.TYPE_EMOTICON);
        }else if(tag.equalsIgnoreCase("topic")) {
			if(opening)
				startTxt(tag, output, xmlReader);
			else
				endTxt(tag, output, xmlReader, TagType.TYPE_TOPIC);
        }else if(tag.equalsIgnoreCase("torrent")) {
			if(opening)
				startTxt(tag, output, xmlReader);
			else
				endTxt(tag, output, xmlReader, TagType.TYPE_TORRENT);
        }else if(tag.equalsIgnoreCase("mailto")) {
			if(opening)
				startTxt(tag, output, xmlReader);
			else
				endTxt(tag, output, xmlReader, TagType.TYPE_MAILTO);
        }else if(tag.equalsIgnoreCase("flash")) {
			if(opening)
				startTxt(tag, output, xmlReader);
			else
				endTxt(tag, output, xmlReader, TagType.TYPE_FLASH);
        }else if(tag.equalsIgnoreCase("del")) {
			if(opening)
				startTxt(tag, output, xmlReader);
			else
				endTxt(tag, output, xmlReader, TagType.TYPE_DEL);
        }else if(tag.equalsIgnoreCase("code")) {
			if(opening)
				startTxt(tag, output, xmlReader);
			else
				endTxt(tag, output, xmlReader, TagType.TYPE_CODE);
        }else if(tag.equalsIgnoreCase("shoutbox")) {
			if(opening)
				startTxt(tag, output, xmlReader);
			else
				endTxt(tag, output, xmlReader, TagType.TYPE_SHOUTBOX);
        }
		
		if (tag.equalsIgnoreCase("img")) {
			int len = output.length();
			ImageSpan[] images = output.getSpans(len-1, len, ImageSpan.class);
			String imgURL = images[0].getSource();
			
			Pattern pattern = Pattern.compile("([\\w]+)/([\\w]+).gif");
			Matcher matcher = pattern.matcher(imgURL);
			if (!matcher.find())
			{
				pattern = Pattern.compile("^attachments/");
				matcher = pattern.matcher(imgURL);
				if (matcher.find())
				{
					imgURL = URLContainer.BASEURL + imgURL;
				}
			}
			output.setSpan(new ImageClick(context, imgURL), len-1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			UrlTagHandler.this.container.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}
	
	private class ImageClick extends ClickableSpan {

		private String url;
		private Context context;
		
		public ImageClick(Context context, String url) {
			this.context = context;
			this.url = url;
		}
		
		@Override
		public void onClick(View widget) {
			if (!url.equals("")) {
				Intent intent = new Intent();
				intent.putExtra("url", url);
				intent.setClass(context, ImageViewActivity.class);
				context.startActivity(intent);
			}
		}
		
	}

	public void startTxt(String tag, Editable output, XMLReader xmlReader) {    
	    startIndex = output.length();    
	}    

	public void endTxt(String tag, Editable output, XMLReader xmlReader, int type) {
	    stopIndex = output.length();
		int scaleWidth;
		int scaleHeight;
		Drawable drawable = null;
		switch (type) {
			case TagType.TYPE_EMOTICON:
				long emoticon_id;
				String raw_id = output.toString().substring(startIndex,stopIndex);
				if(Util.isDigit(raw_id) == false)
					emoticon_id = Util.getNumbers(raw_id);
				else
					emoticon_id = Long.parseLong(raw_id, 10);
				try {
					InputStream is = context.getAssets().open("smilies/" + emoticon_id + ".gif");
					drawable = Drawable.createFromResourceStream(context.getResources(), null, is, "");
					scaleWidth = ScaleUtil.Dp2Px(context, drawable.getIntrinsicWidth());
					scaleHeight = ScaleUtil.Dp2Px(context, drawable.getIntrinsicHeight());
					drawable.setBounds(0, 0, scaleWidth, scaleHeight);
		            is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			    break;
			case TagType.TYPE_TOPIC:
				scaleWidth = ScaleUtil.Dp2Px(context, 53);
				scaleHeight = ScaleUtil.Dp2Px(context, 20);
				drawable = context.getResources().getDrawable(R.drawable.button_show_topic);
			    drawable.setBounds(0, 0, scaleWidth, scaleHeight);
			    break;
			case TagType.TYPE_TORRENT:
				scaleWidth = ScaleUtil.Dp2Px(context, 53);
				scaleHeight = ScaleUtil.Dp2Px(context, 20);
				drawable = context.getResources().getDrawable(R.drawable.button_show_torrent);
			    drawable.setBounds(0, 0, scaleWidth, scaleHeight);
			    break;
			case TagType.TYPE_MAILTO:
				scaleWidth = ScaleUtil.Dp2Px(context, 53);
				scaleHeight = ScaleUtil.Dp2Px(context, 20);
				drawable = context.getResources().getDrawable(R.drawable.button_mailto);
			    drawable.setBounds(0, 0, scaleWidth, scaleHeight);
			    break;
			case TagType.TYPE_SHOUTBOX:
				scaleWidth = ScaleUtil.Dp2Px(context, 53);
				scaleHeight = ScaleUtil.Dp2Px(context, 20);
				drawable = context.getResources().getDrawable(R.drawable.button_show_shoutbox);
			    drawable.setBounds(0, 0, scaleWidth, scaleHeight);
			    break;
			case TagType.TYPE_FLASH:
				drawable = context.getResources().getDrawable(R.drawable.button_play);
				scaleWidth = ScaleUtil.imageSpanWidth(context);
				scaleHeight = (int)((float)drawable.getIntrinsicHeight()/(float)drawable.getIntrinsicWidth()*scaleWidth);
			    drawable.setBounds(0, 0, scaleWidth, scaleHeight);
			    break;
			case TagType.TYPE_DEL:
				output.setSpan(new StrikethroughSpan(), startIndex, stopIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			    break;
			case TagType.TYPE_CODE:
				output.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), startIndex, stopIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				output.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.text_color_deep_grey)), startIndex, stopIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				output.setSpan(new BackgroundColorSpan(context.getResources().getColor(R.color.bg_color_light_grey)), startIndex, stopIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				output.setSpan(new RelativeSizeSpan(0.8f), startIndex, stopIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			    break;
	
			default:
				break;
		}
		if(drawable != null){
			output.setSpan(new ImageSpan(drawable), startIndex, stopIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			output.setSpan(new FunctionalSpan(type, output.toString().substring(startIndex,stopIndex)), startIndex, stopIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	    container.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	public class TagType{
		public final static int TYPE_EMOTICON = 1;
		public final static int TYPE_TOPIC = 2;
		public final static int TYPE_TORRENT = 3;
		public final static int TYPE_MAILTO = 4;
		public final static int TYPE_FLASH = 5;
		public final static int TYPE_DEL = 6;
		public final static int TYPE_CODE = 7;
		public final static int TYPE_SHOUTBOX = 8;
	}
	
	public class FunctionalSpan extends ClickableSpan{		
		public long key;
		public String url;
		public int type;
		
		public FunctionalSpan(int type, String key){
			if(type == TagType.TYPE_FLASH){
				url = key;
			}else{
				if(Util.isDigit(key) == false)
					this.key = Util.getNumbers(key);
				else
					this.key = Long.parseLong(key, 10);
			}
			this.type = type;
		}

		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent();
			switch (type) {
				case TagType.TYPE_TOPIC:
					intent.putExtra("topicid", key);
					intent.setClass(context, TopicPosts.class);
					context.startActivity(intent);
					break;
				case TagType.TYPE_TORRENT:
					intent.putExtra("id", key);
					intent.setClass(context, TorrentActivity.class);
					context.startActivity(intent);
					break;
				case TagType.TYPE_MAILTO:
					intent.putExtra("sender", key);
					intent.setClass(context, ViewMail.class);
					context.startActivity(intent);
					break;
				case TagType.TYPE_SHOUTBOX:
					intent.setClass(context, ShoutboxActivity.class);
					context.startActivity(intent);
					break;
				case TagType.TYPE_FLASH:
					intent.putExtra("url", url);
					intent.setClass(context, FlashPlayerActivity.class);
			        context.startActivity(intent);
					break;
				default:
					break;
			}
		}
		
	}
}
