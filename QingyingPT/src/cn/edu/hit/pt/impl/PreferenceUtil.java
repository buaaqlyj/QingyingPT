package cn.edu.hit.pt.impl;

import android.content.Context;
import android.preference.PreferenceManager;
import cn.edu.hit.pt.Params;
import cn.edu.hit.pt.model.User;

public class PreferenceUtil{
	public Context context;

	public PreferenceUtil(Context context){
		this.context = context;
	}

	public void clearPreference() {
		PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit();
	}

	public void savePreference(String key, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).commit();
	}
	
	public void savePreference(String key, int value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).commit();
	}
	
	public void savePreference(String key, long value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value).commit();
	}

	public String getStringPreference(String key) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
	}
	
	public int getIntPreference(String key) {
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, 0);
	}
	
	public long getLongPreference(String key) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, 0);
	}
	
	public void loadKeyPreference() {
		if(Params.cookie == null || Params.cookie.equals(""))
			Params.cookie = getStringPreference("cookie");
		if(Params.CURUSER == null)
			Params.CURUSER = new User(
				getLongPreference("CURUSER_id"),
				getIntPreference("CURUSER_class"),
				getStringPreference("CURUSER_name"),
				getStringPreference("CURUSER_class_name")
			);
	}
	
	public void saveKeyPreference() {
		savePreference("CURUSER_id", Params.CURUSER.id);
		savePreference("CURUSER_name", Params.CURUSER.name);
		savePreference("CURUSER_class", Params.CURUSER.uclass);
		savePreference("CURUSER_class_name", Params.CURUSER.ucname);
	}
}
