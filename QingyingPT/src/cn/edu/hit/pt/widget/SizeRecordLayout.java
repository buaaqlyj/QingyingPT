package cn.edu.hit.pt.widget;

import cn.edu.hit.pt.Params;
import cn.edu.hit.pt.impl.DatabaseUtil;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class SizeRecordLayout extends RelativeLayout{
	public Context context;

	public SizeRecordLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		int delta = Math.abs(oldh - h);
		if(delta > ImplementsBar.minIMHeight && delta != Params.systemSettings.im_height && delta < getMeasuredHeight()){
			Params.systemSettings.im_height = delta;
			DatabaseUtil.systemDatabase(context).update(Params.systemSettings);
		}
	}
}
