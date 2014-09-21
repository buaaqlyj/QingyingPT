package cn.edu.hit.pt;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import cn.edu.hit.pt.LoginManager.ICallBack;
import cn.edu.hit.pt.http.UserAvatarTask;
import cn.edu.hit.pt.impl.BitmapFunctions;
import cn.edu.hit.pt.impl.DirectoryUtil;
import cn.edu.hit.pt.impl.MD5Util;
import cn.edu.hit.pt.impl.PreferenceUtil;
import cn.edu.hit.pt.model.User;

public class LoginActivity extends Activity{
	//private static final String TAG = "LoginActivity";
	private ImageView ivAvatar;
	private EditText etUserName;
	private EditText etPwd;
	private Button btnLogin;
	private ImageView ivClose;
	private ImageView btnClearName;
	private ImageView btnClearPassword;
	
	private User user;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		ivAvatar = (ImageView)findViewById(R.id.ivAvatar);
		etUserName = (EditText)findViewById(R.id.etUsername);
		etPwd = (EditText)findViewById(R.id.etPassword);
		btnLogin = (Button)findViewById(R.id.btnSubmit);
		ivClose = (ImageView)findViewById(R.id.ivClose);
		btnClearName = (ImageView)findViewById(R.id.btnClearName);
		btnClearPassword = (ImageView)findViewById(R.id.btnClearPassword);
		
		user = new User();
		if(Params.CURUSER != null){
			if(Params.CURUSER.id > 0){
				String md5 = MD5Util.MD5("Avatar:" + Params.CURUSER.id);
				Bitmap bitmap = BitmapFunctions.getBitmap(DirectoryUtil.avatarDirectory, md5 + ".jpg");
				if(bitmap != null)
					ivAvatar.setImageBitmap(bitmap);
			}
			if(!Params.CURUSER.name.equals("")){
				etUserName.setText(Params.CURUSER.name);
				if(Params.CURUSER.id > 0){
					ivAvatar.setTag(Params.CURUSER.id);
					new UserAvatarTask(this, Params.CURUSER.id, ivAvatar, false);
				}
			}
			user = Params.CURUSER;
		}
		
		ivAvatar.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				if(user.id > 0){
					ivClose.setVisibility(View.VISIBLE);
				}
				return true;
			}
		});
		
		ivClose.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(user.id > 0){
					DirectoryUtil.deleteUser(LoginActivity.this, user.id);
					PreferenceUtil preferenceUtil = new PreferenceUtil(LoginActivity.this);
					preferenceUtil.savePreference("CURUSER_id", 0l);
					preferenceUtil.savePreference("CURUSER_name", "");
					preferenceUtil.savePreference("CURUSER_class", 0);
					preferenceUtil.savePreference("CURUSER_class_name", "");
					etUserName.setText("");
					etPwd.setText("");
					ivAvatar.setImageResource(R.drawable.default_avatar);
					ivClose.setVisibility(View.GONE);
				}
			}
		});

		Params.ResetVariables();
		
		etUserName.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.length() > 0){
					btnClearName.setVisibility(View.VISIBLE);
				}else{
					btnClearName.setVisibility(View.GONE);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(etUserName.getText().toString().equals(Params.CURUSER.name)){
					if(Params.CURUSER.id > 0){
					    Bitmap bitmap = BitmapFunctions.getBitmap(DirectoryUtil.avatarDirectory, Params.CURUSER.id + ".jpg");
					    ivAvatar.setImageBitmap(bitmap);
					}
				}else{
					ivAvatar.setImageResource(R.drawable.default_avatar);
				}
			}
		});
		
		etPwd.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
				if(s.length() > 0){
					btnClearPassword.setVisibility(View.VISIBLE);
				}else{
					btnClearPassword.setVisibility(View.GONE);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		
		etUserName.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean focused) {
				if(focused && etUserName.getText().length() > 0){
					btnClearName.setVisibility(View.VISIBLE);
				}else{
					btnClearName.setVisibility(View.GONE);
				}
			}
		});
		
		etPwd.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean focused) {
				if(focused && etPwd.getText().length() > 0){
					btnClearPassword.setVisibility(View.VISIBLE);
				}else{
					btnClearPassword.setVisibility(View.GONE);
				}
			}
		});
		
		btnClearName.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				etUserName.setText("");
			}
		});
		
		btnClearPassword.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				etPwd.setText("");
			}
		});
		
		btnLogin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				btnLogin.setText(R.string.login_loading);
				btnLogin.setEnabled(false);
				String pwd = "";
				try {
					pwd = URLEncoder.encode(etPwd.getText().toString(),"UTF-8");
				} catch (UnsupportedEncodingException e) {
					Toast.makeText(LoginActivity.this, getString(R.string.password_invalid), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
					return;
				}
				new LoginManager().login(LoginActivity.this, etUserName.getText().toString(), pwd, new ICallBack() {
					
					@Override
					public void onSuccess() {
						startActivity(new Intent(LoginActivity.this, MainActivity.class));
						finish();
					}
					
					@Override
					public void onFailed(String error) {
						Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
						btnLogin.setText(R.string.login);
						btnLogin.setEnabled(true);
					}
				});
			}
		});
	}
}
