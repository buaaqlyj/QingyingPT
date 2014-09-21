package cn.edu.hit.pt.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import cn.edu.hit.pt.R;

public class PreviewView extends View{
	private Paint paint;
	private Rect rect;

	public PreviewView(Context context) {
		super(context);
	}
	
	public PreviewView(Context context, AttributeSet attr){
		super(context, attr);
		paint = new Paint();
		paint.setColor(context.getResources().getColor(R.color.bg_color_transparent_black)); 
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		if(rect != null){
			//Draw top part
			canvas.drawRect(0, 0, width, rect.top, paint);
			//Draw bottom part
			canvas.drawRect(0, rect.bottom, width, height, paint);
			//Draw left part
			canvas.drawRect(0, rect.top, rect.left, rect.bottom, paint);
			//Draw right part
			canvas.drawRect(rect.right, rect.top, width, rect.bottom, paint);
		}
	}
	
	public PreviewView getRect(View view){
		rect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
		return this;
	}
	
	public void refresh(){
		invalidate();
	}

}
