import java.io.*;

public class Player implements Serializable {
	
	public enum Status {
		LoggedOff,
		InLobby,
		InGame
	}
	
	private static Player shared_instance = null;
	
	private Status curStatus;
	private int user_id;
	private String username;
	private double money;
	
	private Player() {
		curStatus = Status.LoggedOff;
	}
	
	public Player(int uid, String username, double money) {
		curStatus = Status.LoggedOff;
		this.user_id = uid;
		this.username = username;
		this.money = money;
	}
	
	public static Player shared() {
		if (shared_instance == null) {
			shared_instance = new Player();
		}
		return shared_instance;
	}
	
	public void set_status(Status status) {
		this.curStatus = status;
	}
	
	public void set_uid(int uid) {
		this.user_id = uid;
	}
	
	public void set_username(String username) {
		this.username = username;
	}
	
	public void set_money(double money) {
		this.money = money;
	}
	
	public Status get_status() {
		return curStatus;
	}
	
	public int get_user_id() {
		return user_id;
	}
	
	public String get_username() {
		return username;
	}
	
	public double get_money() {
		return money;
	}
}
