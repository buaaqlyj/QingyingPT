package cn.edu.hit.pt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.edu.hit.pt.http.URLImageParser;
import cn.edu.hit.pt.http.UrlTagHandler;
import cn.edu.hit.pt.impl.ScaleUtil;
import cn.edu.hit.pt.model.Torrent;

@SuppressLint("InflateParams")
public class TorrentDescription extends PopupWindow{
	private Context context;
	private TextView tvContent;
	private RelativeLayout rlParent;
	private RelativeLayout rlBackground;

	public TorrentDescription(Context context){
		super(context);
		this.context = context;
	}
	
	public TorrentDescription(final Context context, Torrent torrent){
		this(context);
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mView = inflater.inflate(R.layout.torrent_description, null);
		tvContent = (TextView)mView.findViewById(R.id.tvContent);
		
		setContentView(mView);
		setWidth(LayoutParams.MATCH_PARENT);
		setHeight((int)(ScaleUtil.heightPixels(context) * 3 / 4));
		setFocusable(true);
		setOutsideTouchable(true);
		setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_border_top));
		setAnimationStyle(R.style.PopupAnimation);

		URLImageParser p = new URLImageParser(tvContent, context);
		UrlTagHandler t = new UrlTagHandler(tvContent, context);
		Spanned sp = Html.fromHtml(torrent.descr, p, t);
		tvContent.setText(sp);
	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		rlBackground = new RelativeLayout(context);
		rlBackground.setLayoutParams(
				new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT,
						RelativeLayout.LayoutParams.MATCH_PARENT
				)
		);
		rlBackground.setBackgroundResource(R.color.bg_color_transparent_deep_black);
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_enter);
		rlBackground.setAnimation(anim);
		anim.start();
		rlParent = (RelativeLayout)parent;
		rlParent.addView(rlBackground);
		
		setOnDismissListener(new OnDismissListener() {			
			@Override
			public void onDismiss() {
				if(rlParent != null && rlBackground != null){
					Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_exit);
					anim.setFillAfter(true);
					anim.setRepeatCount(0);
					anim.setAnimationListener(new AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation arg0) {}
						
						@Override
						public void onAnimationRepeat(Animation arg0) {}
						
						@Override
						public void onAnimationEnd(Animation arg0) {
							rlParent.removeView(rlBackground);
						}
					});
					rlBackground.startAnimation(anim);
				}
			}
		});
		
		super.showAtLocation(parent, gravity, x, y);
	}
	
}
