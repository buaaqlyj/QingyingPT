package cn.edu.hit.pt.model;

import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.annotation.PrimaryKey.AssignType;

@Table("mails")
public class MailItem {
	@PrimaryKey(AssignType.AUTO_INCREMENT)
	public long id;
	
	public long sender;
	public long receiver;
	public String added;
	public String msg;
	public int unread_count;
	public String sender_name;

	public MailItem(){}
}
