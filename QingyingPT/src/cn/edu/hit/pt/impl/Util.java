package cn.edu.hit.pt.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.text.format.Time;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import cn.edu.hit.pt.Params;
import cn.edu.hit.pt.R;
import cn.edu.hit.pt.http.UpdateDownload;
import cn.edu.hit.pt.model.PollMessage;
import cn.edu.hit.pt.widget.CustomDialog;
import cn.edu.hit.pt.widget.CustomToast;

public class Util{
	public static String LINE_SEPARATOR = System.getProperty("line.separator");
	
	public static String md5(String s) {
		try {
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String convertStreamToString(InputStream is) {

		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				is));
		final StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + LINE_SEPARATOR);
			}
		} catch (IOException e) {
		} finally {
			try {
				is.close();
			} catch (IOException e) {

			}
		}
		return sb.toString();
	}
		
	public static void set_sp_layout(LinearLayout l, int i) {
		switch (i) {
		case 2:
			l.setBackgroundResource(R.color.sp_free);
			break;
		case 3:
			l.setBackgroundResource(R.color.sp_2x);
			break;
		case 4:
			l.setBackgroundResource(R.color.sp_2xfree);
			break;
		case 5:
			l.setBackgroundResource(R.color.sp_halfdown);
			break;
		case 6:
			l.setBackgroundResource(R.color.sp_twouphalfdown);
			break;
		case 7:
			l.setBackgroundResource(R.color.sp_thirtypercent);
			break;

		default:
			break;
		}
	}
	
	public static String format_size(long size) {
		String result = null;
		if(size < 1024)
			result = (float)Math.round(size*100)/100 + " B";
		else if (size < 1048576)
			result = (float)Math.round(size*100/1024)/100 + " KB";
		else if (size < 1073741824)
			result = (float)Math.round(size*100/1048576)/100 + " MB";
		else if (size < 1099511627776L)
			result = (float)Math.round(size*100/1073741824)/100 + " GB";
		else
			result = (float)Math.round(size*100/1099511627776L)/100 + " TB";
		return result;
	}
	
	public static String format_time(Context context, float time) {
		String result = null;
		int weeks = (int)(time/604800);
		int days = (int)((time - weeks*7)/86400);
		result = weeks + context.getString(R.string.week) + days + context.getString(R.string.day);
		return result;
	}
	
	public static String format_ratio(Context context, long uploaded, long downloaded) {
		String result = null;
		if(downloaded == 0)
			result = context.getString(R.string.infinite);
		else
			result = (float)Math.round((double)uploaded*100/(double)downloaded)/100 + "";
		return result;
	}
	
	public static String get_today() {
		Time t=new Time("GMT+8");
		t.setToNow();
		return t.format3339(true);
	}

    @SuppressWarnings("deprecation")
    public static Rect getDefaultImageBounds(Context context) {
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = (int) (width * 3 / 4);
        
        Rect bounds = new Rect(0, 0, width, height);
        return bounds;
    }
    
    public static String replaceBlank(String str) {  
        String dest = "";  
        if (str!=null) {  
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");  
            Matcher m = p.matcher(str);  
            dest = m.replaceAll("");  
        }  
        return dest;  
    }

	// 判断一个字符串是否都为数字  
	public static boolean isDigit(String strNum) {  
		Pattern pattern = Pattern.compile("[0-9]{1,}");  
		Matcher matcher = pattern.matcher((CharSequence) strNum);  
		return matcher.matches();  
	}
	//截取数字  
	public static long getNumbers(String content) {  
		Pattern pattern = Pattern.compile("\\d+");  
		Matcher matcher = pattern.matcher(content);  
		while (matcher.find()) {  
			return Long.parseLong(matcher.group(0), 10);  
		}  
		return 0;  
	}	
	    
	public static String subString(String text, int length, String endWith) {      
		int textLength = text.length();
		int byteLength = 0;
		StringBuffer returnStr =  new StringBuffer();
		for(int i = 0; i<textLength && byteLength < length*2; i++){
			String str_i = text.substring(i, i+1); 
			if(str_i.getBytes().length == 1){//英文
				byteLength++;
			}else{//中文
				byteLength += 2 ;
			}
			returnStr.append(str_i);
		}
		try {
			if(byteLength<text.getBytes("GBK").length){//getBytes("GBK")每个汉字长2，getBytes("UTF-8")每个汉字长度为3
				returnStr.append(endWith);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return returnStr.toString();
	}
	
	public static void setShowLeftButton(Button b) {
		if(Params.unread_count != 0)
			b.setBackgroundResource(R.drawable.button_menu_new);
		else
			b.setBackgroundResource(R.drawable.button_menu);
	}
	
	public static String TimeStamp2Date(String timestampString, String formats){  
		Long timestamp = Long.parseLong(timestampString)*1000;
		String date = new SimpleDateFormat(formats).format(new Date(timestamp));
		return date;
	}
	
	public static View getRootView(Activity context){
		return ((ViewGroup)context.findViewById(android.R.id.content)).getChildAt(0);
	}

	public static boolean checkPostResult(Context context, String result) {
		if(result.equals("success")){
			CustomToast toast = new CustomToast(context, CustomToast.TYPE_SUCCESS, CustomToast.LENGTH_SHORT);
			toast.setTitle(context.getString(R.string.post_success));
			toast.setText(context.getString(R.string.post_success));
			toast.create().show();
			return true;
		}
		else{
			CustomToast toast = new CustomToast(context, CustomToast.TYPE_WARNING, CustomToast.LENGTH_lONG);
			toast.setTitle(context.getString(R.string.post_failed));
			if (result.equals("post_flood")) {
				toast.setText(context.getString(R.string.post_flood));
			}
			else if (result.equals("topic_locked")) {
				toast.setText(context.getString(R.string.topic_locked));
			}
			else if (result.equals("forum_not_found")) {
				toast.setText(context.getString(R.string.forum_not_found));
			}
			else if (result.equals("topic_not_found")) {
				toast.setText(context.getString(R.string.topic_not_found));
			}
			else if (result.equals("no_right_to_post")) {
				toast.setText(context.getString(R.string.no_right_to_post));
			}
			else if (result.equals("no_valid_action")) {
				toast.setText(context.getString(R.string.no_valid_action));
			}
			else if (result.equals("subject_not_valid")) {
				toast.setText(context.getString(R.string.subject_not_valid));
			}
			else if (result.equals("subject_over_length")) {
				toast.setText(context.getString(R.string.subject_over_length));
			}
			else if (result.equals("permission_denied")) {
				toast.setText(context.getString(R.string.permission_denied));
			}
			else if (result.equals("post_no_content")) {
				toast.setText(context.getString(R.string.post_no_content));
			}
			else if (result.equals("sql_error")) {
				toast.setText(context.getString(R.string.sql_error));
			}
			else{
				toast.setText(context.getString(R.string.unknown_error));
			}
			toast.create().show();
		}
		return false;
	}
	
	public static boolean checkMailResult(Context context, int status) {
		if(status == 0){
			return true;
		}else{
			CustomToast toast = new CustomToast(context, CustomToast.TYPE_WARNING, CustomToast.LENGTH_lONG);
			toast.setTitle(context.getString(R.string.mail_failed));
			switch (status) {
				case 1:
					toast.setText(context.getString(R.string.invalid_user));
					break;
				case 2:
					toast.setText(context.getString(R.string.user_disabled));
					break;
				case 3:
					toast.setText(context.getString(R.string.mail_blocked));
					break;
				case 4:
					toast.setText(context.getString(R.string.mail_accept_friend));
					break;
				case 5:
					toast.setText(context.getString(R.string.mail_accept_nobody));
					break;
				case -1:
					toast.setText(context.getString(R.string.no_body));
					break;
	
				default:
					toast.setText(context.getString(R.string.unknown_error));
					break;
			}
			toast.create().show();
		}
		return false;
	}
	
	public static void showUpdateDialog(final Context context, boolean showToast) {
		final PollMessage message = Params.message;
		if(message == null) return;
		if((message.version > Params.version)&&(Params.if_update == 1)){
			CustomDialog.Builder customBuilder = new CustomDialog.Builder(context);
            customBuilder.setTitle(R.string.software_update).setMessage(message.update_description)
            .setPositiveButton(R.string.text_update, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
					if(!message.update_url.equals("")){
						UpdateDownload ud = new UpdateDownload(context);
						ud.execute();
						Params.if_update = 0;
						dialog.dismiss();
					}
                }
            })
            .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
					Params.if_update = 0;
					dialog.dismiss();
                }
            }).create().show();
		}else{
			if(showToast)
				new CustomToast(context, CustomToast.TYPE_SUCCESS).setTitle(context.getString(R.string.no_update))
				.setText(context.getString(R.string.is_latest_version)).create().show();
		}
	}

}
