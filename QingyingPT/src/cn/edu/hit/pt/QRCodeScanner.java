package cn.edu.hit.pt;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import cn.edu.hit.pt.widget.PreviewView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.android.PlanarYUVLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

public class QRCodeScanner extends Activity
	implements SurfaceHolder.Callback, Camera.PreviewCallback, Camera.AutoFocusCallback {
	
	private static final String TAG = "QRCode";
	
	private static final int MIN_PREVIEW_PIXCELS = 320 * 240;
	private static final int MAX_PREVIEW_PIXCELS = 800 * 480;

	private Camera myCamera;
	private SurfaceView surfaceView;
	private PreviewView wrapView;
	private View centerView;
	
	private Timer mTimer;
	private MyTimerTask mTimerTask;
	
	private Boolean hasSurface;
	private Boolean initialized;
	
	private Point screenPoint;
	private Point previewPoint;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        hasSurface = false;
        initialized = false;
        
        setContentView(R.layout.qrcode_scanner);
        wrapView = (PreviewView)findViewById(R.id.wrap_view);
        centerView = (View)findViewById(R.id.center_view);
    }
    
    @SuppressWarnings("deprecation")
	@Override
    protected void onResume() {
    	super.onResume();
    	
    	surfaceView = (SurfaceView)findViewById(R.id.preview_view);
    	SurfaceHolder holder = surfaceView.getHolder();
    	if (hasSurface) {
    		initCamera(holder);
    	} else {
    		holder.addCallback(this);
    		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    	}
    }
    
    @Override
    protected void onPause() {
    	closeCamera();
    	if (!hasSurface) {
    		SurfaceHolder holder = surfaceView.getHolder();
    		holder.removeCallback(this);
    	}
    	super.onPause();
    }
    
    class MyTimerTask extends TimerTask {  
        @Override  
        public void run() {
        	if(myCamera != null){
	    		if (!myCamera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_FIXED)) {
	    			myCamera.autoFocus(QRCodeScanner.this);
	    		}
        	}
        }  
    }  

    /** SurfaceHolder.Callback */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	/** Camera.AutoFocusCallback */
	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		if(success)
			myCamera.setOneShotPreviewCallback(this);
	}
	
	/** Camera.PreviewCallback */
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		
		int left = centerView.getLeft() * previewPoint.x / screenPoint.x;
		int top = centerView.getTop() * previewPoint.y / screenPoint.y;
		int width = centerView.getWidth() * previewPoint.x / screenPoint.x;
		int height = centerView.getHeight() * previewPoint.y / screenPoint.y;
		
		wrapView.getRect(centerView).refresh();
		
		final PlanarYUVLuminanceSource source= new PlanarYUVLuminanceSource(data,previewPoint.x,previewPoint.y,top,left,height,width,false);

		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		MultiFormatReader reader = new MultiFormatReader();
		try {
			Result result = reader.decode(bitmap);
			if(result != null){
				tagHandler(result.getText());
			}
		} catch (Exception e) {
			//Toast.makeText(this, "error: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){
	    	if(myCamera != null){
	    		if (!myCamera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_FIXED)) {
	    			myCamera.autoFocus(QRCodeScanner.this);
	    		}
	    	}
		}
		return super.onTouchEvent(event);
	}

	private void initCamera(SurfaceHolder holder) {
    	try {
    		openCamera(holder);
    		mTimer = new Timer();  
    		mTimerTask = new MyTimerTask();  
    		mTimer.schedule(mTimerTask, 0, 200);
    	} catch (Exception e) {
    		Log.w(TAG, e);
    	}
	}
	
	private void openCamera(SurfaceHolder holder) throws IOException {
		if (myCamera == null) {
			myCamera = Camera.open();
			
			if (myCamera == null) {
				throw new IOException();
			}
		}
		myCamera.setDisplayOrientation(90);
		myCamera.setPreviewDisplay(holder);
		
		if (!initialized) {
			initialized = true;
			initFromCameraParameters(myCamera);
		}
		
		setCameraParameters(myCamera);
		myCamera.setOneShotPreviewCallback(this);
		myCamera.startPreview();
	}

	private void closeCamera() {
		if(mTimer != null) mTimer.cancel();
		if(mTimerTask != null) mTimerTask.cancel();
		if (myCamera != null) {
			myCamera.stopPreview();
			myCamera.release();
			myCamera = null;
		}
	}

	private void setCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		
		parameters.setPreviewSize(previewPoint.x, previewPoint.y);
		camera.setParameters(parameters);
		
	}

	@SuppressWarnings("deprecation")
	private void initFromCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		WindowManager manager = (WindowManager)getApplication().getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		
		if (width < height) {
			int tmp = width;
			width = height;
			height = tmp;
		}
		
		screenPoint = new Point(width, height);
		Log.d(TAG, "screenPoint = " + screenPoint);
		previewPoint = findPreviewPoint(parameters, screenPoint, false);
		Log.d(TAG, "previewPoint = " + previewPoint);
	}

	private Point findPreviewPoint(Camera.Parameters parameters, Point screenPoint, boolean portrait) {
		Point previewPoint = null;
		int diff = Integer.MAX_VALUE;
		
		for (Camera.Size supportPreviewSize : parameters.getSupportedPreviewSizes()) {
			int pixels = supportPreviewSize.width * supportPreviewSize.height;
			if (pixels < MIN_PREVIEW_PIXCELS || pixels > MAX_PREVIEW_PIXCELS) {
				continue;
			}
			
			int supportedWidth = portrait ? supportPreviewSize.height : supportPreviewSize.width;
			int supportedHeight = portrait ? supportPreviewSize.width : supportPreviewSize.height;
			int newDiff = Math.abs(screenPoint.x * supportedHeight - supportedWidth * screenPoint.y);
			
			if (newDiff == 0) {
				previewPoint = new Point(supportedWidth, supportedHeight);
				break;
			}
			
			if (newDiff < diff) {
				previewPoint = new Point(supportedWidth, supportedHeight);
				diff = newDiff;
			}
		}
		if (previewPoint == null) {
			Camera.Size defaultPreviewSize = parameters.getPreviewSize();
			previewPoint = new Point(defaultPreviewSize.width, defaultPreviewSize.height);
		}
		
		return previewPoint;
	}
	
	private void tagHandler(String s) {
		Intent intent = new Intent();
		Pattern pattern = Pattern.compile("http:\\/\\/pt\\.hit\\.edu\\.cn\\/details\\.php\\?(.*)id=(\\d+)");
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			intent.putExtra("id", Long.parseLong(matcher.group(2)));
			intent.setClass(this, TorrentActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		pattern = Pattern.compile("http:\\/\\/pt\\.hit\\.edu\\.cn\\/forums\\.php\\?(.*)topicid=(\\d+)");
		matcher = pattern.matcher(s);
		while (matcher.find()) {
			intent.putExtra("topicid", Long.parseLong(matcher.group(2)));
			intent.setClass(this, TopicPosts.class);
			startActivity(intent);
			finish();
			return;
		}
		pattern = Pattern.compile("http:\\/\\/pt\\.hit\\.edu\\.cn\\/userdetails\\.php\\?(.*)id=(\\d+)");
		matcher = pattern.matcher(s);
		while (matcher.find()) {
			intent.putExtra("userid", Long.parseLong(matcher.group(2)));
			intent.setClass(this, UserInformation.class);
			startActivity(intent);
			finish();
			return;
		}
		
	}
	
}