package cn.edu.hit.pt.model;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.PrimaryKey.AssignType;
import com.litesuits.orm.db.annotation.Table;

@Table("settings")
public class SystemSettings {
    @Column("id")
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    public long id = 1;
    
    public int version;
    public int im_height = 340;
    
    public SystemSettings(){}

    public SystemSettings(int version){
    	this.version = version;
    }
	
}
