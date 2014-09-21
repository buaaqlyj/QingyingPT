package cn.edu.hit.pt.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.annotation.PrimaryKey.AssignType;

@Table("avatar")
public class Avatar {
	@PrimaryKey(AssignType.BY_MYSELF)
    public long userid;
    public long updatedDate = 0;
    public long storedDate = 0;
    
    public Avatar(){}
    
    public Avatar(long userid){
    	this.userid = userid;
    }
    
    public Avatar(long userid, long updatedDate){
		Date now = new Date();
		SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd");
    	this.userid = userid;
    	this.updatedDate = updatedDate;
    	this.storedDate = Long.parseLong(s.format(now));
    }
    
    public Avatar set(long updatedDate, long storedDate) {
    	this.updatedDate = updatedDate;
    	this.storedDate = storedDate;
    	return this;
	}
}
