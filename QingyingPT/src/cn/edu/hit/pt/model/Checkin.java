package cn.edu.hit.pt.model;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.PrimaryKey.AssignType;
import com.litesuits.orm.db.annotation.Table;

@Table("checkin")
public class Checkin {
    @Column("id")
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    public int id = 1;

    public int result = 0;
    public String lastCheckedin = "";
    public int totalDays = 0;
    public int bonusAdded = 0;
    public int redate = 0;

    public Checkin(){}
    public Checkin(int result, String lastCheckedin, int totalDays, int bonusAdded){
    	this.result = result;
    	this.lastCheckedin = lastCheckedin;
    	this.totalDays = totalDays;
    	this.bonusAdded = bonusAdded;
    }

}
