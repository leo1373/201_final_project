import java.io.*;

import java.util.*;


public class Lobby implements Serializable {

	private List<PlayerThread> hostingPlayers; //Players in lobby and in lobby only
	private List<GameTableThread> activeGameTables;
	
	public Lobby() {
		hostingPlayers = new ArrayList<PlayerThread>();
		activeGameTables = new ArrayList<GameTableThread>();
	}
	
	public void admitPlayer(PlayerThread p) {
		hostingPlayers.add(p);  
		System.out.println(p.get_player().get_username() + " has entered the lobby");
	}
	
	public boolean admitPlayerIntoTable(int table_id, PlayerThread pt) {
		GameTableThread desTable = getGameTable(table_id);
		if (desTable != null) {
			hostingPlayers.remove(pt);
			desTable.admitPlayerToTable(pt);
			return true;
		} else {
			return false;
		}
	}
	
	public void createTableAndAdmitPlayer(PlayerThread pt) {
		GameTableThread newGameTable = new GameTableThread(this, activeGameTables.size() + 1);
		activeGameTables.add(newGameTable);
		hostingPlayers.remove(pt);
		newGameTable.admitPlayerToTable(pt);
	}
	
	public List<GameTableThread> queryTables() {
		return  activeGameTables;
	}
	
	private GameTableThread getGameTable(int table_id) {
		for (GameTableThread i : activeGameTables) {
			if (i.get_table_id() == table_id) {
				return i;
			}
		}
		return null;
	}
}
