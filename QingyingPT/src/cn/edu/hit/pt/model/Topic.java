package cn.edu.hit.pt.model;

import java.util.ArrayList;

import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.PrimaryKey.AssignType;
import com.litesuits.orm.db.annotation.Table;

@Table("topics")
public class Topic {
	@PrimaryKey(AssignType.AUTO_INCREMENT)
	public long _id;
	
	public long id;
	public int userid;
	public String subject;
	public String locked = "yes";
	public long firstpost;
	public long lastpost;
	public String sticky = "no";
	public long hlcolor;
	public long views;
	public int reply_num;
	public String read = "unread";
	public String added;
	public String lastpostuser;
	public String body;
	public int minclasswrite = 255;
	public ArrayList<Post> posts;
	
	public Topic(){}
}
