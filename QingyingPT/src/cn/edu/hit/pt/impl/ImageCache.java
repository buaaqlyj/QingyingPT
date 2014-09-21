package cn.edu.hit.pt.impl;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;

public class ImageCache {
	public static ImageCache instance;
	public HashMap<String, SoftReference<Bitmap>> map;
	
	public static ImageCache instance() {
		if(instance == null)
			instance = new ImageCache();
		instance.map = new HashMap<String, SoftReference<Bitmap>>();
		return instance;
	}
	
	public void add(Bitmap bitmap, String tag) {
		SoftReference<Bitmap> softBitmap = new SoftReference<Bitmap>(bitmap);
		map.put(tag, softBitmap);
	}
	
	public Bitmap get(String tag) {
        SoftReference<Bitmap> softBitmap = map.get(tag);
        if (softBitmap == null) {
            return null;
        }
        Bitmap bitmap = softBitmap.get();
        return bitmap;
	}

}
