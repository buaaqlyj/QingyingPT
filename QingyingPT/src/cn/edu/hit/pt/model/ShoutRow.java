package cn.edu.hit.pt.model;

public class ShoutRow {
	public long id;
	public long userid;
	public String username;
	public String date;
	public String text;
	
	public ShoutRow(long userid, String username, String text){
		this.userid = userid;
		this.username = username;
		this.text = text;
	}
}
