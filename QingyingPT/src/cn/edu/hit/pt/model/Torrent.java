package cn.edu.hit.pt.model;

import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.annotation.PrimaryKey.AssignType;

@Table("torrents")
public class Torrent {
	@PrimaryKey(AssignType.AUTO_INCREMENT)
	public long _id;
	
	public long torrentid;
	public String name;
	public String descr;
	public String small_descr;
	public String cat_name;
	public String source_name;
	public long size;
	public String added;
	public long owner;
	public int comments;
	public int leechers;
	public int seeders;
	public int sp_state;
	public String anonymous;
	public int url;
	public String owner_name;
	public float imdb_rating;
	public int copy_count;
	public long imdb_id;
	public int reservation_state;
	public int bookmark_state;
	
	public Torrent(){}
}
