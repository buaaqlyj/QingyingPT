package cn.edu.hit.pt.widget;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import cn.edu.hit.pt.R;
import cn.edu.hit.pt.impl.ScaleUtil;
import cn.edu.hit.pt.impl.TagFormatter;

public class SmiliesPage extends Fragment{
	private final static String TAG = "SmiliesPage";
	public GrapeGridview gridView;
	public EditText target;

	public int pixel;
	public int[] IDArray;
	public ArrayList<Smilie> smilies;
	
	public void setArgs(EditText target, int[] IDArray, int pixel) {
		this.IDArray = IDArray;
		this.target = target;
		this.pixel = pixel;
	}
	
	public class Smilie{
		public int id;
		public Drawable image;
		public String text;
		
		public Smilie(int id, Drawable image){
			this.id = id;
			this.image = image;
			this.text = "[em"+id+"]";
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		smilies = new ArrayList<Smilie>();
		try {
			for(Integer id : IDArray){
				InputStream is = getActivity().getAssets().open("smilies/" + id + ".gif");
				Drawable d = Drawable.createFromResourceStream(getActivity().getResources(), null, is, "");
				Smilie smilie = new Smilie(id, d);
				smilies.add(smilie);
	            is.close();
			}
			if(IDArray.length > 20)
				gridView.setNumColumns(6);
			else if(IDArray.length >= 15)
				gridView.setNumColumns(5);
			else if(IDArray.length > 10)
				gridView.setNumColumns(4);
			gridView.setAdapter(new MyAdapter());
			gridView.setOnItemClickListener(new OnItemClickListener(){
				
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					int start = target.getSelectionStart();
					String toAppend = smilies.get(position).text;
					StringBuilder str = new StringBuilder(target.getText().toString());
					str.insert(start, toAppend);
					TagFormatter tagFormatter = new TagFormatter(getActivity());
					tagFormatter.setText(str.toString());
					target.setText(tagFormatter.format());
					target.setSelection(start + toAppend.length());
				}
				
			});
			
		} catch (IOException e) {
			Log.e(TAG, "setView Failed!");
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mView = (View)inflater.inflate(R.layout.smilies_page, null);
		gridView = (GrapeGridview)mView.findViewById(R.id.gridView);
		return mView;
	}

	public class MyAdapter extends BaseAdapter{		
        public int getCount() { 
            return smilies.size(); 
        }

        public Object getItem(int item) { 
            return item;
        }
 
        public long getItemId(int id) { 
            return id; 
        }

		public View getView(int position, View convertView, ViewGroup parent) { 
			ImageView imageView = null;
			Smilie smilie = smilies.get(position);
			if(smilie != null){
				if(convertView == null) {
					int px = ScaleUtil.Dp2Px(getActivity(), pixel);
					LayoutParams lParams = new GridView.LayoutParams(px, px);
					imageView = new ImageView(getActivity());
					imageView.setLayoutParams(lParams);
					imageView.setAdjustViewBounds(false);
					imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
					imageView.setPadding(8, 8, 8, 8);
				}
				else {
					imageView = (ImageView) convertView; 
				}
				imageView.setImageDrawable(smilie.image);
				imageView.setBackgroundResource(R.drawable.list_item);
			}
			return imageView; 
		}
	}
}
