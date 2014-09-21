package cn.edu.hit.pt.http;

import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

@SuppressWarnings("deprecation")
public class URLDrawable extends BitmapDrawable {
    // the drawable that you need to set, you could set the initial drawing
    // with the loading image if you need to
    public Drawable drawable;
    public DisplayMetrics metrics;

    @Override
    public void draw(Canvas canvas) {
        // override the draw to facilitate refresh function later
    	this.setTargetDensity(metrics);
        if(drawable != null) {
            drawable.draw(canvas);
        }
    }
    
    
}