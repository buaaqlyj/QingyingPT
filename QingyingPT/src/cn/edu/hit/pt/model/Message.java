package cn.edu.hit.pt.model;

public class Message {
	public long id;
	public long sender;
	public int status = -1;
	public String subject;
	public String added;
	public String msg;
	public String type;
	
	public Message(long sender, String msg, String type){
		this.sender = sender;
		this.msg = msg;
		this.type = type;
	}
}
