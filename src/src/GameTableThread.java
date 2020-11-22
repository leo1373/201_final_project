import java.util.*;

import java.util.Map.Entry;
import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.util.concurrent.*;

public class GameTableThread extends Thread implements Serializable {
	
	/*
	 * Condition For GameInProgress:
	 * Every participating player has mad a bet(PlayerStatus.InRound)
	 * 
	 * Game does not start until every player is in round
	 * Therefore, other players can join the table when GameNotStarted.
	 */
	enum DealerStatus {
		GameInProgress, GameNotStarted, Busted
	}
	
	private Lobby lobby;
	
	private int table_id;
	private List<PlayerThread> hostingPlayers;
	
	private Lock tableLock = new ReentrantLock();
	private Condition sleepingCondition = tableLock.newCondition();
	private DealerStatus dealerStatus;
	
	private Dealer dealer;
	private Deck deck;
	
	private Map<PlayerThread, Integer> playerBets;
	private Map<PlayerThread, Hand> playerHands;
	
	public GameTableThread(Lobby lobby, int table_id) {
		this.lobby = lobby;
		this.table_id = table_id;
		dealerStatus = DealerStatus.GameNotStarted;
		hostingPlayers = new CopyOnWriteArrayList<PlayerThread>();
		playerBets = new HashMap<PlayerThread, Integer>();
		playerHands = new HashMap<PlayerThread, Hand>();
		dealer = new Dealer();
		deck = new Deck();
		System.out.println("table " + table_id + " has been created");
		this.start();
	}
	
	public synchronized void admitPlayerToTable(PlayerThread pt) {
		hostingPlayers.add(pt);
		playerBets.put(pt, null);
		playerHands.put(pt, new Hand());
		System.out.println(pt.get_player().get_username() + " has joined table " + table_id);
		try {
			tableLock.lock();
			sleepingCondition.signal();
		} finally {
			tableLock.unlock();
		}
	}
	
	private int askBetFrom(PlayerThread p) {
		return p.get_thread().askForBet();
	}
	
	private int askHitOrStandFrom(PlayerThread p) {
		return p.get_thread().askHitOrStand();
	}
	
	private boolean hasBetFrom(PlayerThread p) {
		Integer res = playerBets.get(p);
		return res != null;
	}
	
	private boolean hasBetFromEveryOne() {
		for (Map.Entry mapElement : playerBets.entrySet()) {
			if (mapElement.getValue() == null) {
				return false;
			}
		}
		return true;
	}
	
	private void resetPlayerBets() {
		playerBets.forEach((k, v) -> playerBets.put(k, null));
	}
	
	private void resetPlayerStatus() {
		for (PlayerThread p : hostingPlayers) {
			p.set_player_status(PlayerThread.PlayerStatus.NotInRound);
		}
	}
	
	private void resetPlayerHands() {
		playerHands.forEach((k, v) -> playerHands.put(k, new Hand()));
	}
	
	private void alertPlayersOfNewGame() {
		for (PlayerThread p : hostingPlayers) {
			p.get_thread().notifyOfNewGame();
		}
	}
	
	private void resetTable() {
		resetPlayerBets();
		resetPlayerStatus();
		resetPlayerHands();
		dealer.reset();
		dealerStatus = DealerStatus.GameNotStarted;
		deck.reset();
		try {
			Thread.sleep(20000);
			alertPlayersOfNewGame();
			Thread.sleep(1500); // Concurrency Issue
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void braodcastTableInformationToPlayers() {
		for (PlayerThread curPlayer : hostingPlayers) {
			curPlayer.get_thread().sendMessage("table info broadcast");
			curPlayer.get_thread().sendMessage(this);
		}
	}
	
	public void initialRound() {
		try {
			for (PlayerThread curPlayer : hostingPlayers) {
				Hand pHand = playerHands.get(curPlayer);
				pHand.getCardFromDealer(deck.getTopCard());
				braodcastTableInformationToPlayers();
				Thread.sleep(2000);
			}
			dealer.get_hand().getCardFromDealer(deck.getTopCard());
			braodcastTableInformationToPlayers();
			Thread.sleep(2000);
			for (PlayerThread curPlayer : hostingPlayers) {
				Hand pHand = playerHands.get(curPlayer);
				pHand.getCardFromDealer(deck.getTopCard());
				if (pHand.isBlackJack()) {
					curPlayer.set_player_status(PlayerThread.PlayerStatus.BlackJack);
				}
				braodcastTableInformationToPlayers();
				Thread.sleep(2000);
			}
			dealer.get_hand().getCardFromDealer(deck.getTopCard());
			if (dealer.get_hand().isBlackJack()) {
				dealer.set_blackJack();
			}
			braodcastTableInformationToPlayers();
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	private void evaluateHandOf(PlayerThread p) {
		Hand pHand = playerHands.get(p);
		if (pHand.is21()) {
			p.set_player_status(PlayerThread.PlayerStatus.is21);
		} else if (pHand.isBusted()) {
			p.set_player_status(PlayerThread.PlayerStatus.Busted);
		}
	}
	
	/*
	 * Decisions:
	 * 1: Hit
	 * 2: Stand
	 */
	
	public void secondRound() {
		try {
			for (PlayerThread curPlayer : hostingPlayers) {
				while (curPlayer.get_player_status() == PlayerThread.PlayerStatus.InRound) {
					int decision = askHitOrStandFrom(curPlayer);
					if (decision == 1) {
						System.out.println("Player " + curPlayer + " Hits");
						Hand pHand = playerHands.get(curPlayer);
						pHand.getCardFromDealer(deck.getTopCard());
						evaluateHandOf(curPlayer);
						braodcastTableInformationToPlayers();
						Thread.sleep(500);
					} else if (decision == 2) {
						System.out.println("Player " + curPlayer + " Stands");
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Notes to group:
	 * Still, inelegant way to update players' tokens
	 * (We are assuming how much token the player currently has,
	 * In reality, to prevent hacking, this token update should be left to the server alone)
	 * But works good enough for now
	 * :D
	 * - Leo
	 */
	private void settleBets() {
		if (dealer.get_blackJack_status()) {
			for (PlayerThread pt : hostingPlayers) {
				if (pt.get_player_status() == PlayerThread.PlayerStatus.BlackJack) {
				} else {
					pt.get_player().set_money(pt.get_player().get_money() - playerBets.get(pt));
					DBWrapper.shared().updateUser(pt.get_player());
				}
			}
		} else {
			for (PlayerThread pt : hostingPlayers) {
				if (pt.get_player_status() == PlayerThread.PlayerStatus.Busted) {
					pt.get_player().set_money(pt.get_player().get_money() - playerBets.get(pt));
					DBWrapper.shared().updateUser(pt.get_player());
				} else {
					if (dealerStatus == DealerStatus.Busted) {
						pt.get_player().set_money(pt.get_player().get_money() + (double)(playerBets.get(pt) * (pt.get_player_status() == PlayerThread.PlayerStatus.BlackJack ? 1.5 : 1.0)));
						DBWrapper.shared().updateUser(pt.get_player());
					} else {
						Hand pHand = playerHands.get(pt);
						int playerHandValue = (pHand.maxHandValue() <= 21 ? pHand.maxHandValue() : pHand.minHandValue());
						if (playerHandValue > dealer.get_hand().maxHandValue()) {
							pt.get_player().set_money(pt.get_player().get_money() + (double)(playerBets.get(pt) * (pt.get_player_status() == PlayerThread.PlayerStatus.BlackJack ? 1.5 : 1.0)));
							DBWrapper.shared().updateUser(pt.get_player());
						} else if (playerHandValue == dealer.get_hand().maxHandValue()) {
							
						} else {
							pt.get_player().set_money(pt.get_player().get_money() - playerBets.get(pt));
							DBWrapper.shared().updateUser(pt.get_player());
						}
					}
				}
			}
		}
	}
	
	/*
	 * Handle Tokens First Thing First to avoid concurrency issues
	 * Inelegant, but works :D
	 */
	public void concludeGame() {
		settleBets();
		for (PlayerThread curPlayer : hostingPlayers) {
			curPlayer.get_thread().sendMessage("conclusion info broadcast");
			curPlayer.get_thread().sendMessage(this);
			curPlayer.get_thread().sendMessage(curPlayer.get_player());
		}
		resetTable();
	}
	
	private void evaluateDealer() {
		if (dealer.get_hand().maxHandValue() > 21) {
			dealerStatus = DealerStatus.Busted;
		}
	}
	
	private boolean allPlayerBusted() {
		for (PlayerThread p : hostingPlayers) {
			if (p.get_player_status() != PlayerThread.PlayerStatus.Busted) {
				return false;
			}
		}
		return true;
	}
	
	private void dealerRound() {
		try {
			dealer.set_revealing_stage(true);
			braodcastTableInformationToPlayers();
			Thread.sleep(2000);
			while (dealer.get_hand().maxHandValue() < 17) {
				dealer.get_hand().getCardFromDealer(deck.getTopCard());
				evaluateDealer();
				braodcastTableInformationToPlayers();
				Thread.sleep(2000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			while (true) {
				while (!hostingPlayers.isEmpty()) {
					if (dealerStatus == DealerStatus.GameNotStarted) {
						if (!hasBetFromEveryOne()) { //Concurrency concerns
							for (PlayerThread curPlayer : hostingPlayers) {
								if (hasBetFrom(curPlayer)) continue;
								if (hostingPlayers.size() == 1) {
									Thread.sleep(500);
								}
								int bet = askBetFrom(curPlayer);
								playerBets.put(curPlayer, bet);
								curPlayer.set_player_status(PlayerThread.PlayerStatus.InRound);
								System.out.println("received bet of " + bet + " from " + curPlayer.get_player().get_username());
							}
						} else {
							dealerStatus = DealerStatus.GameInProgress;
						}
					} else if (dealerStatus == DealerStatus.GameInProgress) {						
						initialRound();
						if (dealer.get_blackJack_status()) {
							concludeGame();
							continue;
						}
						secondRound();
						if (!allPlayerBusted()) {
							dealerRound();
						}
						concludeGame();
//						try {
//							tableLock.lock();
//							sleepingCondition.await();
//						} catch (InterruptedException ie) {
//							System.out.println("ie while sleeping: " + ie.getMessage());
//						} finally {
//							tableLock.unlock();
//						}
					}
					
				}
				try {
					tableLock.lock();
					sleepingCondition.await();
				} catch (InterruptedException ie) {
					System.out.println("ie while sleeping: " + ie.getMessage());
				} finally {
					tableLock.unlock();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public int get_table_id() {
		return table_id;
	}
	
	public String description() {
		String str = "TABLE INFORMATION\n";
		str += "Dealer: " + dealer.handDescription() + "\n";
		for (PlayerThread p : hostingPlayers) {
			str += "Player " + p.get_player().get_username() + ", bet(" + playerBets.get(p) + "): ";
			Hand pHand = playerHands.get(p);
			str += pHand;
			if (p.get_player_status() == PlayerThread.PlayerStatus.BlackJack) {
				str += ": value(21), BLACKJACK!\n";
				continue;
			}
			int minHandValue = pHand.minHandValue();
			int maxHandValue = pHand.maxHandValue();
			if (p.get_player_status() == PlayerThread.PlayerStatus.Busted) {
				str += ": value(" + minHandValue + "), BUSTED!\n";
				continue;
			}
			if (minHandValue != maxHandValue) {
				if (maxHandValue > 21) {
					str += ": value(" + minHandValue + ")";
				} else {
					str += ": value(" + minHandValue + " / " + maxHandValue + ")";
				}
			} else {
				str += ": value(" + maxHandValue + ")";
			}
			str += "\n";
		}
		return str;
	}
	
	/*
	 * Logic for conclusion:
	 * If Dealer gets BlackJack,
	 * everyone loses unless BlackJack
	 * Else
	 * 
	 * if player busts, loses regardless of dealer
	 * else
	 * 	if dealer busts, win regardless of self
	 *   else
	 *    win if hand( max <= 21 ? max : min ) > dealerHand (always max)
	 *    push if hand = dealerHand
	 *    lose if hand < dealerHand
	 */
	public String conclusionDescription() {
		String str = "TABLE CONCLUSION\n";
		if (dealer.get_blackJack_status()) {
			for (PlayerThread pt : hostingPlayers) {
				if (pt.get_player_status() == PlayerThread.PlayerStatus.BlackJack) {
					str += "Player " + pt + "'s bet(" + playerBets.get(pt) + ") is pushed back\n";
				} else {
					str += "Player " + pt + " loses (" + playerBets.get(pt) + ") token(s) to dealer\n";
				}
			}
		} else {
			for (PlayerThread pt : hostingPlayers) {
				if (pt.get_player_status() == PlayerThread.PlayerStatus.Busted) {
					str += "Player " + pt + " loses (" + playerBets.get(pt) + ") token(s) to dealer\n";
				} else {
					if (dealerStatus == DealerStatus.Busted) {
						str += "Player " + pt + " wins (" + (double)(playerBets.get(pt) * (pt.get_player_status() == PlayerThread.PlayerStatus.BlackJack ? 2.5 : 2.0)) + ") token(s) from dealer\n";
					} else {
						Hand pHand = playerHands.get(pt);
						int playerHandValue = (pHand.maxHandValue() <= 21 ? pHand.maxHandValue() : pHand.minHandValue());
						if (playerHandValue > dealer.get_hand().maxHandValue()) {
							str += "Player " + pt + " wins (" + (double)(playerBets.get(pt) * (pt.get_player_status() == PlayerThread.PlayerStatus.BlackJack ? 2.5 : 2.0)) + ") token(s) from dealer\n";
						} else if (playerHandValue == dealer.get_hand().maxHandValue()) {
							str += "Player " + pt + "'s bet(" + playerBets.get(pt) + ") is pushed back\n";
						} else {
							str += "Player " + pt + " loses (" + playerBets.get(pt) + ") token(s) to dealer\n";
						}
					}
				}
			}
		}
		return str;
	}
	
	public String toString() {
		String str = "table id: " + table_id + ", active players [" + hostingPlayers.size() + " / 3]";
		if (dealerStatus == DealerStatus.GameInProgress) {
			str += ", game in progress";
		}
		return str;
	}
}
