import java.net.ServerSocket;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlackJackServer implements Serializable {

	private List<BlackJackServerThread> serverThreads;
	private Lobby lobby;

	public BlackJackServer(int port)
	{
		try
		{
			ServerSocket ss = new ServerSocket(port);
			serverThreads = new ArrayList<BlackJackServerThread>();
			lobby = new Lobby();
			System.out.println("Server Started");
			while(true)
			{
				Socket s = ss.accept();   //  Accept the incoming request
				BlackJackServerThread st = new BlackJackServerThread(s, this);
				serverThreads.add(st);
			}
		}
		catch (Exception ex) {}

	}
	
	public void admitPlayerIntoLobby(PlayerThread pt) {
		lobby.admitPlayer(pt);
	}
	
	public boolean admitPlayerIntoTable(int table_id, PlayerThread pt) {
		return lobby.admitPlayerIntoTable(table_id, pt);
	}
	
	public void createTableAndAdmitPlayer(PlayerThread pt) {
		lobby.createTableAndAdmitPlayer(pt);
	}

	public List<GameTableThread> queryTables() {
		return lobby.queryTables();
	}
	
	public static void main(String[] args) {
		new BlackJackServer(6789);
	}

}
