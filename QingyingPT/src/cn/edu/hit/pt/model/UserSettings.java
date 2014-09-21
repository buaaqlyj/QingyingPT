package cn.edu.hit.pt.model;

import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.PrimaryKey.AssignType;
import com.litesuits.orm.db.annotation.Table;


@Table("settings")
public class UserSettings {
    @PrimaryKey(AssignType.BY_MYSELF)
    public int id = 1;

    public boolean nopic = false;
    public boolean receive_offline_message = true;
    
    public UserSettings(){}

}
