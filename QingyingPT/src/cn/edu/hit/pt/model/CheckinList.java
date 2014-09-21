package cn.edu.hit.pt.model;

import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.PrimaryKey.AssignType;
import com.litesuits.orm.db.annotation.Table;


@Table("checkinlist")
public class CheckinList{
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    public int _id;
    
    public int id;
    public String name;
    public String lastCheckedin;

    public CheckinList(){}
    
    public CheckinList(int id, String name, String lastCheckedin){
    	this.id = id;
    	this.name = name;
    	this.lastCheckedin = lastCheckedin;
    }
	
}