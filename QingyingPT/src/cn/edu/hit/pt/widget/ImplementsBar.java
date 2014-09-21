package cn.edu.hit.pt.widget;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import cn.edu.hit.pt.Params;
import cn.edu.hit.pt.R;

public class ImplementsBar extends FrameLayout{
	public final static int minIMHeight = 300;
	public final static int ITEM_SMILIES = 0;
	public final static int ITEM_ATTACHMENT = 1;
	private Context context;
	private EditText target;
	private FragmentManager fragmentManager;
	private AttachmentFragment attachmentFragment;
	
	private int currentItem;

	public ImplementsBar(Context context, AttributeSet attr){
		super(context, attr);
		this.context = context;
		attachmentFragment = new AttachmentFragment();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(heightMeasureSpec < minIMHeight){
			heightMeasureSpec = minIMHeight;
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public void setTargetView(EditText target){
		this.target = target;
		attachmentFragment.setTargetView(target);
		
		target.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				hide();
			}
		});
		
		target.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				hide();
			}
		});
	}
	
	public int getCurrentItem(){
		return currentItem;
	}
	
	public void fold(int item){
		if(getHeight() == 0 || currentItem != item){
			switch (item) {
				case ITEM_SMILIES:
					setSmiliesLayout();
					break;
				case ITEM_ATTACHMENT:
					setAttachmentLayout();
					break;

				default:
					break;
			}
			show();
		}else{
			hide();
		}
	}
	
	public void clearCounter(){
		attachmentFragment.clearCounter();
	}
	
	public void setFragmentTransaction(FragmentManager fragmentManager){
		this.fragmentManager = fragmentManager;
	}

	public void setSmiliesLayout(){
		SmiliesFragment smiliesFragment = new SmiliesFragment();
		smiliesFragment.setTargetView(target);
		fragmentManager.beginTransaction().replace(R.id.implementsBar, smiliesFragment).commit();
		currentItem = ITEM_SMILIES;
	}
	
	public void setAttachmentLayout(){
		fragmentManager.beginTransaction().replace(R.id.implementsBar, attachmentFragment).commit();
		currentItem = ITEM_ATTACHMENT;
	}
	
	public void disableUploadPhoto() {
		attachmentFragment.disableUploadImage();
	}
	
	public void show(){
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(target.getWindowToken(), 0);
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, Params.systemSettings.im_height);
		param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
		setLayoutParams(param);
	}
	
	public void hide(){
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 0);
		param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
		setLayoutParams(param);
	}
}
