package cn.edu.hit.pt.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class WrapContentHeightViewPager extends ViewPager{
	public int widthMeasureSpec = 0;
	public int heightMeasureSpec = 0;
	
	public WrapContentHeightViewPager(Context context) {
		super(context);
	}

	public WrapContentHeightViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void refresh() {
		measure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		this.widthMeasureSpec = widthMeasureSpec;
		int height = 0;
		View child = getChildAt(getCurrentItem());
		if(child == null){
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}else{
			child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			int h = child.getMeasuredHeight();
			if (h > height)
				height = h;
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
			this.heightMeasureSpec = heightMeasureSpec;
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
	
}
