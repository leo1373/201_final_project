import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

public class BlackJackServerThread extends Thread implements Serializable {
	
	private transient ObjectOutputStream dout;
	private transient BufferedReader din;
	private BlackJackServer server;
	private Player curPlayer = null;
	private PlayerThread curPT = null;
	private Semaphore dummySemaphore = new Semaphore(1);
	
	private int bet;
	private int hitOrStand;

	public BlackJackServerThread(Socket s, BlackJackServer srver)
	{
		try
		{
			this.server = srver;
			dout = new ObjectOutputStream(s.getOutputStream());
			din = new BufferedReader(new InputStreamReader(s.getInputStream()));
			start();
		}
		catch (IOException ex) {ex.printStackTrace();}
	}

	/*
	 * Poor use of Semaphore :P
	 * Alternative way is to synchronize the function
	 */
	public int askForBet() {
		sendMessage("ask for bet");
		try {
			dummySemaphore.acquire();
			dummySemaphore.acquire();
			dummySemaphore.release();
			return bet;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public int askHitOrStand() {
		sendMessage("ask for hit or stand");
		try {
			dummySemaphore.acquire();
			dummySemaphore.acquire();
			dummySemaphore.release();
			return hitOrStand;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void notifyOfNewGame() {
		sendMessage("new game notice");
	}
	
	public void sendMessage(Object message)
	{
		try {
			dout.writeObject(message);
			dout.reset();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run()
	{
		try {
			while(true)
			{
				String line = din.readLine();
				if(line == null) break;
				if (line.equals("login")) {
					String username = din.readLine();
					String pwd = din.readLine();
					curPlayer = DBWrapper.shared().login(username, pwd);
					if (curPlayer != null) {
						sendMessage("loginSuccess");
						sendMessage(curPlayer);
						curPT = new PlayerThread(curPlayer, this);
						server.admitPlayerIntoLobby(curPT);
					} else {
						sendMessage("loginFailed");
					}
				} else if (line.equals("register")) {
					String username = din.readLine();
					String pwd = din.readLine();
					boolean reg = DBWrapper.shared().addUser(username, pwd);
					if (reg) {
						sendMessage("registerSuccess");
					} else {
						sendMessage("registerFailed");
					}
				} else if (line.equals("query tables")) {
					sendMessage("query table result");
					sendMessage(server.queryTables());
				} else if (line.equals("join table")) {
					int table_id = Integer.parseInt(din.readLine().trim());
					boolean join_result = server.admitPlayerIntoTable(table_id, curPT);
					sendMessage("join table result");
					sendMessage(join_result);
				} else if (line.equals("create table")) {
					server.createTableAndAdmitPlayer(curPT);
					sendMessage("table created");
				} else if (line.equals("make bet")) {
					String betStr = din.readLine();
					bet = Integer.parseInt(betStr.trim());
					dummySemaphore.release();
				} else if (line.equals("make hit or stand")) {
					String hitOrStandStr = din.readLine();
					hitOrStand = Integer.parseInt(hitOrStandStr.trim());
					dummySemaphore.release();
				}
			}
		}
		catch (Exception ex) {System.out.println("Connection reset"); ex.printStackTrace();}
		finally {
			try {
				dout.close();
				din.close();
			}
			catch (Exception ex) {ex.printStackTrace();}
		}//finally
	}
}
