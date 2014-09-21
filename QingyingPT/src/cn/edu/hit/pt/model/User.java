package cn.edu.hit.pt.model;

import java.util.ArrayList;

import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.PrimaryKey.AssignType;
import com.litesuits.orm.db.annotation.Table;

@Table("user")
public class User {
    @PrimaryKey(AssignType.BY_MYSELF)
	public long id = 0;
	public int uclass = 0;
	public int error = 0;
	public int torrentNum = 0;
	public String name = "";
	public String ucname  = "";
	public String title = "";
	public String info = "";
	public String info_raw = "";
	public String privacy = "";
	public String donor = "";
	public String gender = "";
	public String country = "";
	public String school = "";
	public String flagpic = "";
	public String friend = "";
	public long uploaded = 0;
	public long downloaded = 0;
	public long seedtime = 0;
	public long leechtime = 0;
	public ArrayList<Torrent> torrents;
	
	public User(){}

	public User(long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public User(long id, int uclass) {
		this.id = id;
		this.uclass = uclass;
	}
	
	public User(long id, int uclass, String name, String ucname) {
		this.id = id;
		this.uclass = uclass;
		this.name = name;
		this.ucname = ucname;
	}
	
}
