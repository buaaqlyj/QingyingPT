package cn.edu.hit.pt.model;

import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.PrimaryKey.AssignType;
import com.litesuits.orm.db.annotation.Table;

@Table("news")
public class News{
    @PrimaryKey(AssignType.BY_MYSELF)
	public int _id = 1;
    
	public int id;
	public String title;
	public String body;
	public String description;
	
	public News(){}
	
	public News(int id, String title, String body, String description){
		this.id = id;
		this.title = title;
		this.body = body;
		this.description = description;
	}
}