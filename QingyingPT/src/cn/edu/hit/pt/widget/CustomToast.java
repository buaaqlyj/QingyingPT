package cn.edu.hit.pt.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.edu.hit.pt.R;

public class CustomToast extends Toast{
	public final static int TYPE_SUCCESS = 0;
	public final static int TYPE_INFORMATION = 1;
	public final static int TYPE_WARNING = 2;
	public final static int LENGTH_SHORT = 0;
	public final static int LENGTH_lONG = 1;
	public int type;
	public int duration;
	public String title;
	public String text;
	public Context context;

	public CustomToast(Context context) {
		super(context);
		this.context = context;
	}

	public CustomToast(Context context, int type) {
		this(context);
		this.type = type;
	}
	
	public CustomToast(Context context, int type, int duration) {
		this(context, type);
		this.duration = duration;
	}
	
	public CustomToast setTitle(String title){
		this.title = title;
		return this;
	}
	
	public CustomToast setText(String text){
		this.text = text;
		return this;
	}
	
	public CustomToast create(){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.customtoast, null);
		TextView tvTitle = (TextView)layout.findViewById(R.id.tvTitle);
		TextView tvDescription = (TextView)layout.findViewById(R.id.tvText);
		ImageView image = (ImageView)layout.findViewById(R.id.image);
		switch (type) {
			case TYPE_SUCCESS:
				image.setImageResource(R.drawable.ic_ok);
				break;
				
			case TYPE_INFORMATION:
				image.setImageResource(R.drawable.ic_infor);
				break;
				
			case TYPE_WARNING:
				image.setImageResource(R.drawable.ic_warning);
				break;

			default:
				image.setImageResource(R.drawable.ic_ok);
				break;
		}
		
		tvTitle.setText(title);
		tvDescription.setText(text);
		setDuration(Toast.LENGTH_SHORT);
        setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM, 0, 100);
        setView(layout);
        return this;
	}

}
