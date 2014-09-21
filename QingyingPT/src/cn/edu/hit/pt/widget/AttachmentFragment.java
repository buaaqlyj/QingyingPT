package cn.edu.hit.pt.widget;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cn.edu.hit.pt.Params;
import cn.edu.hit.pt.ContactActivity;
import cn.edu.hit.pt.R;
import cn.edu.hit.pt.impl.DirectoryUtil;
import cn.edu.hit.pt.widget.CustomDialog.MyOnItemClickListener;

public class AttachmentFragment extends Fragment{
	private static final int UPLOAD_LIMIT = 2097152;
	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	
	private LayoutInflater inflater;
	private EditText target;
	private GrapeGridview gridview;
	private TextView tvCounter;
	
	private boolean canUploadImage = true;
	private String imageFilename;
	private ArrayList<FunctionItem> list;
	
	private class FunctionItem{
		public int id;
		public String name;
		
		public FunctionItem(int id, String name) {
			this.id = id;
			this.name = name;
		}
	}
	
	public void setTargetView(EditText target){
		this.target = target;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		list = new ArrayList<FunctionItem>();
		list.add(new FunctionItem(0, getString(R.string.at)));
		if(canUploadImage)
			list.add(new FunctionItem(1, getString(R.string.add_image)));
		gridview.setAdapter(new MyAdapter());
		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
				switch (position) {
					case 0:
						Params.tempTarget = target;
						Intent intent = new Intent();
						intent.putExtra("action", ContactActivity.ACTION_SELECT);
						intent.setClass(getActivity(), ContactActivity.class);
						startActivity(intent);
						break;
						
					case 1:
						showSelectImageDialog();
	
					default:
						break;
				}
			}
		});
	}
	
	public void disableUploadImage() {
		canUploadImage = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.inflater = inflater;
		View mView = inflater.inflate(R.layout.attachment_fragment, null);
		gridview = (GrapeGridview)mView.findViewById(R.id.gridView);
		return mView;
	}
	
	public class MyAdapter extends BaseAdapter{		
        public int getCount() { 
            return list.size(); 
        }

        public Object getItem(int item) { 
            return item;
        }
 
        public long getItemId(int id) { 
            return id; 
        }

		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = inflater.inflate(R.layout.attachment_item, null);
				TextView tv = (TextView)convertView.findViewById(R.id.tv);
				TextView count = (TextView)convertView.findViewById(R.id.count);
				ImageView iv = (ImageView)convertView.findViewById(R.id.iv);
				
				tv.setText(list.get(position).name);
				if(position == 1)
					tvCounter = count;
				iv.setBackgroundResource(R.drawable.attachment_item_bg);
				switch (list.get(position).id) {
					case 0:
						iv.setImageResource(R.drawable.button_at);
						break;
					case 1:
						iv.setImageResource(R.drawable.button_add_image);
						break;
	
					default:
						break;
				}
			}
			return convertView;
		}
	}
	
	private void showSelectImageDialog(){
		String[] items;
		File file = (File) target.getTag();
		if(file != null)
			items = new String[] {getString(R.string.select_local_image), getString(R.string.take_photo), getString(R.string.cancel_add_photo)};
		else
			items = new String[] {getString(R.string.select_local_image), getString(R.string.take_photo)};
		CustomDialog dialog = new CustomDialog.Builder(getActivity())
		.setItems(items)
		.setOnItemClickListener(new MyOnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					Intent intentFromGallery = new Intent();
					intentFromGallery.setType("image/*");
					intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(intentFromGallery, IMAGE_REQUEST_CODE);
					break;
				case 1:
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");       
					Date curDate = new Date(System.currentTimeMillis());
					imageFilename = formatter.format(curDate) + ".jpg";
					Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(DirectoryUtil.cameraDirectory, imageFilename)));
					startActivityForResult(intentFromCapture, CAMERA_REQUEST_CODE);
					break;
				case 2:
					target.setTag(null);
					clearCounter();
				}
			}
			
		}).create();
		dialog.show();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_CANCELED) {
			File source = null;
			switch (requestCode) {
				case CAMERA_REQUEST_CODE:
					if(imageFilename != null)
						source = new File(DirectoryUtil.cameraDirectory, imageFilename);
					break;
				case IMAGE_REQUEST_CODE:
					Uri uri = data.getData();
					String[] proj = { MediaStore.Images.Media.DATA };
					Cursor actualimagecursor = getActivity().managedQuery(uri,proj,null,null,null);
					if(actualimagecursor != null){
						int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
						actualimagecursor.moveToFirst();
						String img_path = actualimagecursor.getString(actual_image_column_index);
						source = new File(img_path);
					}else{
						source = new File(data.getData().getPath());
					}
					break;
			}
			if(source != null){
				if(source.length() > UPLOAD_LIMIT){
					new CustomToast(getActivity(), CustomToast.LENGTH_lONG).setTitle(getString(R.string.add_image_failed))
					.setText(getString(R.string.upload_file_oversized)).create().show();
				}else{
					tvCounter.setVisibility(View.VISIBLE);
					tvCounter.setText("1");
					target.setTag(source);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void clearCounter(){
		tvCounter.setText("");
		tvCounter.setVisibility(View.GONE);
	}

}
