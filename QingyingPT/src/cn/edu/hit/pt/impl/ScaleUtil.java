package cn.edu.hit.pt.impl;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class ScaleUtil {
	public static int Dp2Px(Context context, float dp) { 
	    final float scale = context.getResources().getDisplayMetrics().density; 
	    return (int) (dp * scale); 
	} 
	 
	public static int Px2Dp(Context context, float px) { 
	    final float scale = context.getResources().getDisplayMetrics().density; 
	    return (int) (px / scale); 
	}
	
	public static int widthPixels(Context context) {
		return metrics(context).widthPixels;
	}
	
	public static int heightPixels(Context context) {
		return metrics(context).heightPixels;
	}
	
	public static int imageSpanWidth(Context context) {
		return widthPixels(context) - 120;
	}
	
	public static DisplayMetrics metrics(Context context) {
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics;
	}
}
