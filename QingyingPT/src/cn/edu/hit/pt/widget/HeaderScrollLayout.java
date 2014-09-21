package cn.edu.hit.pt.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class HeaderScrollLayout extends RelativeLayout {
	public Scroller scroller;
    public boolean canScroll = false;
    public int bottom;
    public ImageView ivCover;
    
    public HeaderScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
    }
    
    public void setViewBottom(int bottom){
    	this.bottom = bottom;
    }
    
    public void setCanScroll(boolean canScroll){
    	this.canScroll = canScroll;
    }
    
    public void startScroll(int startX, int startY, int dx, int dy){
    	setCanScroll(true);
    	scroller.startScroll(startX, startY, dx, dy);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            int y = scroller.getCurrY();
            //ivCover.layout(0, 0, ivCover.getWidth(), y);
            if (!scroller.isFinished() && canScroll && y >= bottom) {
            	if(getParent().getClass() == LinearLayout.class)
            		setLayoutParams(new LinearLayout.LayoutParams(getWidth(), y));
            	else if(getParent().getClass() == RelativeLayout.class)
            		setLayoutParams(new RelativeLayout.LayoutParams(getWidth(), y));
            }

            invalidate();
        }
    }

}
