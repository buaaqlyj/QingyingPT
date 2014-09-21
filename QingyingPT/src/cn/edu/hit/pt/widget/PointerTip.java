package cn.edu.hit.pt.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.edu.hit.pt.R;

public class PointerTip extends PopupWindow{
	private LayoutInflater inflater;
	private View mView;
	private TextView tv;
	
	public PointerTip(final Context context) {
		super(context);
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.pointer_tip_layout, null);
		tv = (TextView)mView.findViewById(R.id.tv);

		this.setContentView(mView);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setAnimationStyle(R.style.FadeTransAnimation);
		this.setTouchable(true);
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		ColorDrawable dw = new ColorDrawable(-00000);
		this.setBackgroundDrawable(dw);
		this.update();
	}
	
	public void setText(String text){
		tv.setText(text);
	}
}
