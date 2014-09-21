package cn.edu.hit.pt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import cn.edu.hit.pt.impl.ApplicationInfo;
import cn.edu.hit.pt.impl.DatabaseUtil;
import cn.edu.hit.pt.impl.DirectoryUtil;
import cn.edu.hit.pt.impl.PreferenceUtil;
import cn.edu.hit.pt.model.SystemSettings;

public class LauncherActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationInfo info = new ApplicationInfo(this);
		Params.version = info.getVersionCode();
		Params.systemSettings = DatabaseUtil.systemDatabase(this).queryById(1, SystemSettings.class);
		
		if(DirectoryUtil.checkdir(this)==false){
			Toast.makeText(this, R.string.no_sdcard, Toast.LENGTH_SHORT).show();
		}
		
		int version = 0;
		if(Params.systemSettings == null){
			Params.systemSettings = new SystemSettings(Params.version); //默认有过系统记录后就不载入welcome页面了
			DatabaseUtil.systemDatabase(this).save(Params.systemSettings);
		}else{
			version = Params.systemSettings.version;
		}

		PreferenceUtil preferenceUtil = new PreferenceUtil(this);
		
		if(version < Params.version){
			Params.systemSettings.version = Params.version;
			if(Params.clean_low_version_profile == true){
				DirectoryUtil.deleteFolderFile("/data/data/" + getPackageName() + "/databases", true);
				Params.systemSettings = new SystemSettings(Params.version);
				DatabaseUtil.systemDatabase(this).save(Params.systemSettings);
				preferenceUtil.clearPreference();
				startActivity(new Intent(this, Welcome.class));
			}else{
				DatabaseUtil.systemDatabase(this).save(Params.systemSettings);
			}
		}
		
		preferenceUtil.loadKeyPreference();

		if((!Params.cookie.equals(""))&&(Params.CURUSER.id > 0)&&(!Params.CURUSER.name.equals(""))){
			startActivity(new Intent(this, Splash.class));
		}else{
			startActivity(new Intent(this, LoginActivity.class));
		}
		
		finish();
	}

}
