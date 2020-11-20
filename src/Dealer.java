import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.*;

public class Dealer implements Serializable {
	
	private Hand hand;
	
	private boolean dealerRevealingStage;
	private boolean isBlackJack;
	
	public Dealer() {
		hand = new Hand();
		dealerRevealingStage = false;
		isBlackJack = false;
	}
	
	public void reset() {
		hand = new Hand();
		dealerRevealingStage = false;
		isBlackJack = false;
	}
	
	public Hand get_hand() {
		return hand;
	}
	
	public boolean get_blackJack_status() {
		return isBlackJack;
	}
	
	public void set_revealing_stage(boolean s) {
		dealerRevealingStage = s;
	}
	
	public void set_blackJack() {
		isBlackJack = true;
	}
	
	/*
	 * Dealer always has handMaximumValue
	 */
	public String handDescription() {
		String str = "";
		List<Card> cards = hand.get_cards();
		if (isBlackJack) {
			for (Card i : cards) {
				str += i;
				str += " ";
			}
			str += ": value(21), BLACKJACK!";
			return str;
		}
		if (dealerRevealingStage) {
			for (Card i : cards) {
				str += i;
				str += " ";
			}
			int maxHandValue = hand.maxHandValue();
			str += ": value(" + maxHandValue + ")";
			if (maxHandValue > 21) {
				str += ", BUSTED";
			}
		} else {
			if (cards.size() == 0) {
				
			} else if (cards.size() == 1) {
				str += "?";
			} else {
				Card tCard = cards.get(0);
				str += tCard;
				str += " ? : value(" + tCard.getMaxIntVal() + ")";
			}
		}
		return str;
	}
}
