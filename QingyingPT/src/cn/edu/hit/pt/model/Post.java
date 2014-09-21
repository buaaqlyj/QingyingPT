package cn.edu.hit.pt.model;


public class Post {
	public long id;
	public long userid;
	public String added;
	public String body;
	public int likes;
	public int liked;
	public String username;
	public String result;

	public Post(){}
	
	public Post(long userid, String username, String added, String body){
		this.userid = userid;
		this.username = username;
		this.added = added;
		this.body = body;
	}
}
