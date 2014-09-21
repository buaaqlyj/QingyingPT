package cn.edu.hit.pt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.FutureTask;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.edu.hit.pt.http.MyHttpExceptionHandler;
import cn.edu.hit.pt.http.MyHttpRequest;
import cn.edu.hit.pt.http.URLContainer;
import cn.edu.hit.pt.http.UploadAsyncTask;
import cn.edu.hit.pt.http.UploadAsyncTask.onUploadFinishedListener;
import cn.edu.hit.pt.http.UserAvatarTask;
import cn.edu.hit.pt.impl.BitmapFunctions;
import cn.edu.hit.pt.impl.DirectoryUtil;
import cn.edu.hit.pt.impl.MD5Util;
import cn.edu.hit.pt.impl.ScaleUtil;
import cn.edu.hit.pt.model.Attachment;
import cn.edu.hit.pt.model.User;
import cn.edu.hit.pt.widget.CustomDialog;
import cn.edu.hit.pt.widget.CustomDialog.MyOnItemClickListener;
import cn.edu.hit.pt.widget.CustomToast;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.data.HttpStatus;
import com.litesuits.http.data.NameValuePair;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.request.content.UrlEncodedFormBody;
import com.litesuits.http.request.param.HttpMethod;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpResponseHandler;

public class ProfileSettings extends SwipeBackActivity {
	/* 头像名称 */
	public static final String IMAGE_FILE_NAME = "temp.jpg";

	/* 请求码 */
	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int RESULT_REQUEST_CODE = 2;
	
	//private String TAG = "UserSettings";
	private LinearLayout rlChangeAvatar;
	private LinearLayout rlGender;
	private LinearLayout rlCountry;
	private LinearLayout rlSchool;
	private LinearLayout rlDescription;
	private TextView tvGender;
	private TextView tvCountry;
	private TextView tvSchool;
	private ImageView ivCountry;
	private ImageView ivUserAvatar;
	private Button btnReturn;
	private String[] items;
	private String[] genders;
	private String gender = "";
	private int country = 0;
	private int school = 0;
	private String info;
	private boolean ifSaveProfile = false;
	
	private HttpAsyncExecutor asyncExecutor;
	private ArrayList<FutureTask<Response>> httpTasks = new ArrayList<FutureTask<Response>>();
	private UserAvatarTask avatarTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_settings);
		SwipeBackLayout mSwipeBackLayout;
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

		items = new String[] {getString(R.string.select_local_image), getString(R.string.take_photo)};
		genders = new String[]{getString(R.string.male),getString(R.string.female),getString(R.string.unknown)};
		
		rlChangeAvatar = (LinearLayout)findViewById(R.id.rlChangeAvatar);
		rlGender = (LinearLayout)findViewById(R.id.rlGender);
		rlCountry = (LinearLayout)findViewById(R.id.rlCountry);
		rlSchool = (LinearLayout)findViewById(R.id.rlSchool);
		rlDescription = (LinearLayout)findViewById(R.id.rlDescription);
		tvGender = (TextView)findViewById(R.id.tvGender);
		tvCountry = (TextView)findViewById(R.id.tvCountry);
		tvSchool = (TextView)findViewById(R.id.tvSchool);
		ivCountry = (ImageView)findViewById(R.id.ivCountry);
		ivUserAvatar = (ImageView)findViewById(R.id.ivUserAvatar);
		btnReturn = (Button)findViewById(R.id.btnReturn);

		asyncExecutor = HttpAsyncExecutor.newInstance(LiteHttpClient.newApacheHttpClient(this));
		avatarTask = new UserAvatarTask(this, Params.CURUSER.id, ivUserAvatar, false);
		
		btnReturn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		rlChangeAvatar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				showDialog();
			}
		});
		
		loadProfile();
		loadLists();
		
		rlCountry.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(Params.countries != null && Params.countries.size() > 0){
					CustomDialog dialog = new CustomDialog.Builder(ProfileSettings.this)
					.setItems(getItems(Params.countries))
					.setOnItemClickListener(new MyOnItemClickListener() {
						
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							ifSaveProfile = true;
							country = Integer.parseInt(Params.countries.get(position).get("id").toString());
							tvCountry.setText(Params.countries.get(position).get("name").toString());
							String flagpic = Params.countries.get(position).get("flagpic").toString();
							if(!flagpic.equals("")){
								InputStream is;
								try {
									is = getAssets().open("flag/"+flagpic);
									Drawable d = Drawable.createFromResourceStream(getResources(), null, is, "");
									int scaleWidth = ScaleUtil.Dp2Px(ProfileSettings.this, d.getIntrinsicWidth());
									int scaleHeight = ScaleUtil.Dp2Px(ProfileSettings.this, d.getIntrinsicHeight());
									d.setBounds(0, 0, scaleWidth, scaleHeight);
						            is.close();
									ivCountry.setImageDrawable(d);
									ivCountry.setVisibility(View.VISIBLE);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
						
					}).create();
					dialog.show();
				}else{
					new CustomToast(ProfileSettings.this, CustomToast.TYPE_INFORMATION).setTitle(getString(R.string.loading))
					.setText(getString(R.string.list_not_valid)).create().show();
				}
			}
		});
		
		rlSchool.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(Params.schools != null && Params.schools.size() > 0){
					CustomDialog dialog = new CustomDialog.Builder(ProfileSettings.this)
					.setItems(getItems(Params.schools))
					.setOnItemClickListener(new MyOnItemClickListener() {
						
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							ifSaveProfile = true;
							school = Integer.parseInt(Params.schools.get(position).get("id").toString());
							tvSchool.setText(Params.schools.get(position).get("name").toString());
						}
						
					}).create();
					dialog.show();
				}else {
					new CustomToast(ProfileSettings.this, CustomToast.TYPE_INFORMATION).setTitle(getString(R.string.loading))
					.setText(getString(R.string.list_not_valid)).create().show();
				}
			}
		});

		rlGender.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				CustomDialog dialog = new CustomDialog.Builder(ProfileSettings.this)
				.setItems(genders)
				.setOnItemClickListener(new MyOnItemClickListener() {
					
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						ifSaveProfile = true;
						switch (position) {
						case 0:
							gender = "Male";
							tvGender.setText(getString(R.string.male));
							break;
						case 1:
							gender = "Female";
							tvGender.setText(getString(R.string.female));
							break;
						case 2:
							gender = "N/A";
							tvGender.setText(getString(R.string.unknown));
							break;

						default:
							break;
						}
					}
					
				}).create();
				dialog.show();
			}
		});
		
		rlDescription.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				final EditText etInfo = (EditText)getLayoutInflater().inflate(R.layout.alertdialog_edittext, null);
				etInfo.setText(info);
				CustomDialog.Builder builder = new CustomDialog.Builder(ProfileSettings.this);
				builder.setTitle(R.string.self_description)
				.setContentView(etInfo)
				.setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	info = etInfo.getText().toString();
                    	ifSaveProfile = true;
						dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
                    }
                });
	            Dialog dialog = builder.create();
	            dialog.show();
			}
		});
	}

	/**
	 * 显示选择对话框
	 */
	private void showDialog() {
		CustomDialog dialog = new CustomDialog.Builder(ProfileSettings.this)
		.setItems(items)
		.setOnItemClickListener(new MyOnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					Intent intentFromGallery = new Intent();
					intentFromGallery.setType("image/*"); // 设置文件类型
					intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(intentFromGallery,
							IMAGE_REQUEST_CODE);
					break;
				case 1:
					Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(DirectoryUtil.avatarDirectory, IMAGE_FILE_NAME)));
					startActivityForResult(intentFromCapture, CAMERA_REQUEST_CODE);
					break;
				}
			}
			
		}).create();
		dialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//结果码不等于取消时候
		if (resultCode != RESULT_CANCELED) {

			switch (requestCode) {
			case IMAGE_REQUEST_CODE:
				startPhotoZoom(data.getData());
				break;
			case CAMERA_REQUEST_CODE:
				File tempFile = new File(DirectoryUtil.avatarDirectory, IMAGE_FILE_NAME);
				startPhotoZoom(Uri.fromFile(tempFile));
				break;
			case RESULT_REQUEST_CODE:
				if (data != null) {
					getImageUploaded(data);
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 裁剪图片方法实现
	 * 
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 设置裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 250);
		intent.putExtra("outputY", 250);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 2);
	}

	/**
	 * 保存裁剪之后的图片数据并上传
	 * 
	 * @param picdata
	 */
	private void getImageUploaded(Intent data) {
		Bundle extras = data.getExtras();
		if (extras != null) {
			Bitmap bitmap = extras.getParcelable("data");
			BitmapFunctions.storeInSD(bitmap, DirectoryUtil.avatarDirectory, IMAGE_FILE_NAME);
			File file = new File(DirectoryUtil.avatarDirectory + IMAGE_FILE_NAME);
			Map<String, String> params = new HashMap<String, String>();
			params.put("type", "avatar");
			UploadAsyncTask uploadTask = new UploadAsyncTask(this, URLContainer.getUploadURL(), params, file);
			uploadTask.setOnUploadFinishedListener(new onUploadFinishedListener() {
				
				@Override
				public void onFinished(Attachment result) {
					if(result.error == 0){
						Bitmap bitmap = BitmapFunctions.getBitmap(DirectoryUtil.avatarDirectory, ProfileSettings.IMAGE_FILE_NAME);
						bitmap = BitmapFunctions.toRoundBitmap(bitmap);
						BitmapFunctions.storeInSD(bitmap, DirectoryUtil.avatarDirectory, MD5Util.MD5("Avatar:" + Params.CURUSER.id) + ".jpg");
						if(ivUserAvatar != null){
							new UserAvatarTask(ProfileSettings.this, Params.CURUSER.id, ivUserAvatar, false);
						}
					}
				}
			});
			uploadTask.execute();
		}
	}
	
	public void loadProfile() {
		loadUserInfo(Params.CURUSER);
		MyHttpRequest request = new MyHttpRequest(URLContainer.getUserInforUrl(0, false));
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				loadUserInfo(res.getObject(User.class));
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(ProfileSettings.this).handleException(e);
			}
		}));
	}
	
	public void loadLists() {
		MyHttpRequest request = new MyHttpRequest(URLContainer.getLists());
		httpTasks.add(asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status,
					com.litesuits.http.data.NameValuePair[] headers) {
				try {
					JSONObject json = new JSONObject(res.getString());
					String countries = json.getString("countries");
					String schools = json.getString("schools");

					Params.countries = new ArrayList<Map<String,Object>>();
					JSONArray arr_country = new JSONArray(countries);
					for (int i = 0; i < arr_country.length(); i++) {
						JSONObject c = (JSONObject)arr_country.get(i);
						int c_id = c.getInt("id");
						String c_name = c.getString("name");
						String c_flagpic = c.getString("flagpic");
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("id", c_id);
						map.put("name", c_name);
						map.put("flagpic", c_flagpic);
						Params.countries.add(map);
					}
					Params.schools = new ArrayList<Map<String,Object>>();
					JSONArray arr_school = new JSONArray(schools);
					for (int i = 0; i < arr_school.length(); i++) {
						JSONObject s = (JSONObject)arr_school.get(i);
						int s_id = s.getInt("id");
						String s_name = s.getString("name");
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("id", s_id);
						map.put("name", s_name);
						Params.schools.add(map);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(ProfileSettings.this).handleException(e);
			}
		}));
	}
	
	public void saveProfile() {
		MyHttpRequest request = new MyHttpRequest(URLContainer.getSaveProfileUrl());
		request.setMethod(HttpMethod.Post);
        LinkedList<NameValuePair> pList = new LinkedList<NameValuePair>();
        pList.add(new NameValuePair("gender", gender));
        pList.add(new NameValuePair("country", country+""));
        pList.add(new NameValuePair("school", school+""));
        try {
			info = URLEncoder.encode(info, "UTF-8");
			pList.add(new NameValuePair("info", info));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
        request.setHttpBody(new UrlEncodedFormBody(pList, "UTF-8"));
		asyncExecutor.execute(request, new HttpResponseHandler() {

			@Override
			protected void onSuccess(Response res, HttpStatus status, NameValuePair[] headers) {
				try {
					JSONObject json = new JSONObject(res.getString());
					String result = json.getString("result");
					if(result.equals("success"))
						Toast.makeText(ProfileSettings.this, getString(R.string.save_success), Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(ProfileSettings.this, getString(R.string.save_failed), Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void onFailure(Response res, HttpException e) {
				new MyHttpExceptionHandler(ProfileSettings.this).handleException(e);
			}
		});
	}

	private void loadUserInfo(User user){
		if(user == null) return;
		info = user.info_raw;
		if(user.gender.equals("Male"))
			tvGender.setText(getString(R.string.male));
		else if (user.gender.equals("Female")) 
			tvGender.setText(getString(R.string.female));
		else
			tvGender.setText(getString(R.string.unknown));
		if(user.country.equals(""))
			tvCountry.setText(getString(R.string.none));
		else
			tvCountry.setText(user.country);
		if(!user.school.equals(""))
			tvSchool.setText(user.school);
		if(!user.flagpic.equals("")){
			try {
				InputStream is =  getAssets().open("flag/" + user.flagpic);
				Drawable d = Drawable.createFromResourceStream(getResources(), null, is, "");
				int scaleWidth = ScaleUtil.Dp2Px(ProfileSettings.this, d.getIntrinsicWidth());
				int scaleHeight = ScaleUtil.Dp2Px(ProfileSettings.this, d.getIntrinsicHeight());
				d.setBounds(0, 0, scaleWidth, scaleHeight);
	            is.close();
				ivCountry.setImageDrawable(d);
				ivCountry.setVisibility(View.VISIBLE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String[] getItems(ArrayList<Map<String, Object>> list) {
		String[] items = null;
		ArrayList<String> itemStrings = new ArrayList<String>();
		for (Map<String, Object> map : list) {
			itemStrings.add(map.get("name").toString());
		}
		if(itemStrings.size() > 0)
			items = (String[])itemStrings.toArray(new String[itemStrings.size()]);
		return items;
	}
	
	@Override
	public void finish() {
		for (FutureTask<Response> httpTask : httpTasks) {
			httpTask.cancel(true);
		}
		if(avatarTask != null)
			avatarTask.cancel();
		if(ifSaveProfile == true){
			saveProfile();
		}
		super.finish();
	}
}
