import java.io.*;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.*;

public class BlackJackClient extends Thread {
	
	private enum UIPage {
		Welcome, Login, Register, 
		LobbyOutside, LobbyWaitingToJoin,
		InGameNoBet, InGameBroadCasting
	}
	
	private Scanner scan = new Scanner(System.in);
	private ObjectInputStream ois;
	private PrintWriter pw;
	private Socket s;
	private Player player;
	private UIPage curPage;
	private Semaphore dummySemaphore = new Semaphore(1);
	private Lock clientLock = new ReentrantLock();
	private Condition welcomeCondition = clientLock.newCondition();
	private Condition lobbyCondition = clientLock.newCondition();
	private Condition inGameCondition = clientLock.newCondition();
	
	public BlackJackClient(String hostname, int port) {
		try
		{
			s = new Socket(hostname, port);
			pw = new PrintWriter(s.getOutputStream(), true);
			ois = new ObjectInputStream(s.getInputStream());
			player = Player.shared();
			curPage = UIPage.Welcome;
			start();
			while (true) {
				Player.Status curStatus = player.get_status();
				int option1;
				if (curStatus == Player.Status.LoggedOff) {
					if (curPage == UIPage.Welcome) {
						clearConsole();
						System.out.println("Welcome to Blackjack!");
						System.out.println("1. login");
						System.out.println("2. sign up");
						System.out.println("3. quit");
						System.out.print("Select an option: ");
						try {
							option1 = Integer.parseInt(scan.nextLine());
							while (option1 != 1 && option1 != 2 && option1 != 3) {
								System.out.print("Invalid selection, please try again: ");
								option1 = Integer.parseInt(scan.nextLine());
							}
							if (option1 == 1) {
								curPage = UIPage.Login;
								continue;
							} else if (option1 == 2) {
								curPage = UIPage.Register;
								continue;
							} else if (option1 == 3) {
							
								try {
									ois.close();
									pw.close();
									s.close();
								}
								catch (IOException ex) {ex.printStackTrace();}
								break;
							}
						} catch (Exception e) {
							continue;
						}
					} else if (curPage == UIPage.Login) {
						clearConsole();
						String username, pwd;
						System.out.println("Log In");
						System.out.println("Please Enter Your Username:");
						username = scan.nextLine();
						System.out.println("Please Enter Your Password");
						pwd = scan.nextLine();
						
						pw.println("login");
						pw.println(username);
						pw.println(pwd);
						
						try {
							clientLock.lock();
							welcomeCondition.await();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							clientLock.unlock();
						}
						
					} else if (curPage == UIPage.Register) {
						clearConsole();
						String username, pwd;
						System.out.println("Register");
						System.out.println("Please Enter Your Username:");
						username = scan.nextLine();
						System.out.println("Please Enter Your Password");
						pwd = scan.nextLine();
						
						pw.println("register");
						pw.println(username);
						pw.println(pwd);
						
						try {
							clientLock.lock();
							welcomeCondition.await();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							clientLock.unlock();
						}
					}
				} else if (curStatus == Player.Status.InLobby) {
					if (curPage == UIPage.LobbyOutside) {
						clearConsole();
						System.out.println("1. chat");
						System.out.println("2. join a table");
						System.out.println("3. create a table");
						System.out.println("4. quit");
						System.out.print("Select an option: ");
						try {
							option1 = Integer.parseInt(scan.nextLine());
							while (option1 != 1 && option1 != 2 && option1 != 3 && option1 != 4) {
								System.out.print("Invalid selection, please try again: ");
								option1 = Integer.parseInt(scan.nextLine());
							}
							if (option1 == 1) {
							} else if (option1 == 2) {
								curPage = UIPage.LobbyWaitingToJoin;
								continue;
							} else if (option1 == 3) {
								pw.println("create table");
								try {
									clientLock.lock();
//									dummySemaphore.acquire();
									lobbyCondition.await();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} finally {
									clientLock.unlock();
								}
							} else if (option1 == 4) {
							
								try {
									ois.close();
									pw.close();
									s.close();
								}
								catch (IOException ex) {ex.printStackTrace();}
								break;
							}
						} catch (Exception e) {
							continue;
						}
					} else if (curPage == UIPage.LobbyWaitingToJoin) {
						clearConsole();
						System.out.println("current tables:");
						pw.println("query tables");
						try {
							clientLock.lock();
							lobbyCondition.await();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							clientLock.unlock();
						}
						System.out.print("Enter table id to join or -1 to go back: ");
						try {
							option1 = Integer.parseInt(scan.nextLine());
							if (option1 == -1) {
								curPage = UIPage.LobbyOutside;
								continue;
							} else {
								pw.println("join table");
								pw.println(option1);
								try {
									clientLock.lock();
									lobbyCondition.await();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} finally {
									clientLock.unlock();
								}
							} 
						} catch (Exception e) {
							continue;
						}
					}
				} else if (curStatus == Player.Status.InGame) {
					clearConsole();
					if (curPage == UIPage.InGameNoBet) {
						System.out.println("Awaiting dealer instructions...");
						System.out.println();
						try {
							clientLock.lock();
	//						dummySemaphore.release();
							inGameCondition.await();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							clientLock.unlock();
						}
						System.out.println("NEW GAME");
						System.out.println("Please enter integer bet amount: (you have " + Player.shared().get_money() + " tokens)");
						String bet = scan.nextLine();
						pw.println("make bet");
						pw.println(bet);
						curPage = UIPage.InGameBroadCasting;
					} else if (curPage == UIPage.InGameBroadCasting) {
						try {
							clientLock.lock();
							inGameCondition.await();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							clientLock.unlock();
						}
						
					}
				}
			}
		}
		catch (IOException ex) {ex.printStackTrace();}
	}
	
	public synchronized void run()
	{
		try {
			while(true)
			{
				String line = (String) ois.readObject();
//				System.out.println(line);
				if (line.equals("loginSuccess")) {
					Player temp = (Player) ois.readObject();
					System.out.println("Login Success. Welcome, " + temp.get_username() + ". Hit Enter to Continue...");
					Player.shared().set_uid(temp.get_user_id());
					Player.shared().set_username(temp.get_username());
					Player.shared().set_money(temp.get_money());
					Player.shared().set_status(Player.Status.InLobby);
					curPage = UIPage.LobbyOutside;
					scan.nextLine();
					try {
						clientLock.lock();
						welcomeCondition.signal();
					} finally {
						clientLock.unlock();
					}
				} else if (line.equals("loginFailed")) {
					System.out.println("Login Failed. Hit Enter to Continue...");
					scan.nextLine();
					try {
						clientLock.lock();
						welcomeCondition.signal();
					} finally {
						clientLock.unlock();
					}
				} else if (line.equals("registerSuccess")) {
					System.out.println("Register Success. Hit Enter to Continue...");
					curPage = UIPage.Welcome;
					scan.nextLine();
					try {
						clientLock.lock();
						welcomeCondition.signal();
					} finally {
						clientLock.unlock();
					}
				} else if (line.equals("registerFailed")) {
					System.out.println("Register Failed. Hit Enter to Continue...");
					scan.nextLine();
					try {
						clientLock.lock();
						welcomeCondition.signal();
					} finally {
						clientLock.unlock();
					}
				} else if (line.equals("query table result")) {
					List<GameTableThread> queryTableResult = (List<GameTableThread>) ois.readObject();
					for (GameTableThread i : queryTableResult) {
						System.out.println(i);
					}
					if (queryTableResult.isEmpty()) {
						System.out.println("Currently, there is no active table..");
					}
					System.out.println();
					try {
						clientLock.lock();
						lobbyCondition.signal();
					} finally {
						clientLock.unlock();
					}
				} else if (line.equals("join table result")) {
					boolean join_result = (boolean) ois.readObject();
					if (join_result) {
						Player.shared().set_status(Player.Status.InGame);
						curPage = UIPage.InGameNoBet;
						System.out.println("Join table successful...");
					} else {
						System.out.println("Join table failed, please check table id and try again. Hit Enter to Continue...");
						scan.nextLine();
					}
					try {
						clientLock.lock();
						lobbyCondition.signal();
					} finally {
						clientLock.unlock();
					}
				} else if (line.equals("table created")) {
					Player.shared().set_status(Player.Status.InGame);
					curPage = UIPage.InGameNoBet;
					System.out.println("Create table successful...");
					try {
						clientLock.lock();
						lobbyCondition.signal();
					} finally {
						clientLock.unlock();
					}
				} else if (line.equals("ask for bet")) {
					try {
//						dummySemaphore.acquire();
						clientLock.lock();
						inGameCondition.signal();
					} finally {
						clientLock.unlock();
					}
				} else if (line.equals("table info broadcast")) {
					clearConsole();
					GameTableThread gameTable = (GameTableThread) ois.readObject();
					System.out.println(gameTable.description());
					System.out.println();
					System.out.println("Awaiting dealer instructions...");
					System.out.println();
//					try {
//						clientLock.lock();
//						inGameCondition.signal();
//					} finally {
//						clientLock.unlock();
//					}
				} else if (line.equals("ask for hit or stand")) {
					System.out.print("1. Hit\n"
							+ "2. Stand\n"
							+ "Enter Option: ");
					String res = scan.nextLine();
					System.out.println();
					System.out.println("Awaiting dealer instructions...");
					System.out.println();
					pw.println("make hit or stand");
					pw.println(res);
				} else if (line.equals("conclusion info broadcast")) {
					clearConsole();
					GameTableThread gameTable = (GameTableThread) ois.readObject();
					System.out.println(gameTable.description());
					System.out.println();
					System.out.println(gameTable.conclusionDescription());
					System.out.println();
					System.out.println("New game will start in 20 seconds");
					Player temp = (Player) ois.readObject();
					Player.shared().set_money(temp.get_money());
				} else if (line.equals("new game notice")) {
					clearConsole();
					System.out.println("New Game Initiating");
					curPage = UIPage.InGameNoBet;
					Thread.sleep(1000);
					try {
						clientLock.lock();
						inGameCondition.signal();
					} finally {
						clientLock.unlock();
					}
				}
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
			System.out.println("Connection closed");
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	public void clearConsole() {
		for (int i = 0; i < 100; i++) {
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		new BlackJackClient("localhost", 6789);
	}

}
