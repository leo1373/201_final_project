import java.io.*;

public class PlayerThread implements Serializable {
	
	enum PlayerStatus {
		InRound, NotInRound, BlackJack, is21, Busted
	}
	
	private Player player;
	private BlackJackServerThread thread;
	
	private PlayerStatus playerStatus;
	
	public PlayerThread(Player p, BlackJackServerThread t) {
		this.player = p;
		this.thread = t;
		this.playerStatus = PlayerStatus.NotInRound;
	}
	
	public PlayerStatus get_player_status() {
		return playerStatus;
	}
	
	public Player get_player() {
		return player;
	}
	
	public BlackJackServerThread get_thread() {
		return thread;
	}
	
	public void set_player_status(PlayerStatus ps) {
		this.playerStatus = ps;
	}
	
	public String toString() {
		return player.get_username();
	}
}
