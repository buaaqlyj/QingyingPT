 package cn.edu.hit.pt;

import java.util.ArrayList;
import java.util.Map;

import android.widget.EditText;
import cn.edu.hit.pt.model.PollMessage;
import cn.edu.hit.pt.model.SystemSettings;
import cn.edu.hit.pt.model.Tips;
import cn.edu.hit.pt.model.User;
import cn.edu.hit.pt.model.UserSettings;

public class Params {
	
	//App Information
	public static String name;
	public static int version;
	public static boolean clean_low_version_profile = false;//clear old version caches
	
	//Current User
	public static User CURUSER;
	
	//Setting Information
	public static Tips tips;
	public static SystemSettings systemSettings;
	public static UserSettings userSettings;
	
	//Runtime Parameters
	public static String cookie;
	public static int if_update = 1;
	public static int UPDATE_NOTIFICATION_ID;
	public static int UPLOAD_NOTIFICATION_ID;
	public static int MESSAGE_NOTIFICATION_ID = 255;
	public static int unread_count;
	public static PollMessage message;
	public static EditText tempTarget;
	public static ArrayList<Map<String, Object>> forums;
	public static ArrayList<Map<String, Object>> forums_create;
	public static ArrayList<Map<String, Object>> cats;
	public static ArrayList<Map<String, Object>> countries;
	public static ArrayList<Map<String, Object>> schools;

	//Refresh Flags
	public static boolean refresh_mail = true;
	public static boolean refresh_friends = true;
	
	public static void ResetVariables() {
		CURUSER = new User();
		userSettings = new UserSettings();
		tips = new Tips();
		cookie = "";
		if_update = 1;
		unread_count = 0;
		message = null;
		
		ForumFragment.forumid = "latest";
		ForumFragment.forumname = "";
		ForumFragment.minclasscreate = 255;
		ForumFragment.minclasswrite = 255;
		ForumFragment.if_load = false;
		TorrentFragment.action = "new";
		TorrentFragment.categoryid = 0;
		TorrentFragment.categoryname = "";
		AddPost.id = 0;
		AddPost.name = "";
		AddPost.if_load = false;
		
		forums = null;
		forums_create = null;
		countries = null;
		schools = null;
		
		refresh_mail = true;
		refresh_friends = true;
		
		UPLOAD_NOTIFICATION_ID = 1;
	}
}
