package cn.edu.hit.pt.model;

import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.PrimaryKey.AssignType;
import com.litesuits.orm.db.annotation.Table;

@Table("tips")
public class Tips {
    @PrimaryKey(AssignType.BY_MYSELF)
    public int id = 1;

    public boolean swipeback = false;
    public boolean addpost = false;
    public boolean remotedownload = false;
    public boolean remotedownload_here = false;
    
    public Tips(){}

}
