import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import java.util.concurrent.*;

public class Hand implements Serializable {
	
	private List<Card> cards;
	
	public Hand() {
		cards = new CopyOnWriteArrayList<Card>();
	}
	
	public void getCardFromDealer(Card c) {
		cards.add(c);
	}
	
	public int maxHandValue() {
		int handValue = 0;
		for (Card i : cards) {
			handValue += i.getMaxIntVal();
		}
		return handValue;
	}
	
	public int minHandValue() {
		int handValue = 0;
		for (Card i : cards) {
			handValue += i.getMinIntVal();
		}
		return handValue;
	}
	
	/*
	 * Only A + J/Q/K
	 * So using getMaxIntVal is OK
	 */
	public boolean isBlackJack() {
		if (cards.size() != 2) return false;
		int handValue = 0;
		for (Card i : cards) {
			handValue += i.getMaxIntVal();
		}
		return handValue == 21;
	}
	
	public boolean is21() {
		int minHandValue = 0, maxHandValue = 0;
		for (Card i : cards) {
			minHandValue += i.getMinIntVal();
			maxHandValue += i.getMaxIntVal();
		}
		return (minHandValue == 21 || maxHandValue == 21);
	}
	
	/*
	 * Busted only if minimum value passes 21
	 * So using getMinIntVal is OK
	 */
	public boolean isBusted() {
		int handValue = 0;
		for (Card i : cards) {
			handValue += i.getMinIntVal();
		}
		return handValue > 21;
	}
	
	public List<Card> get_cards() {
		return cards;
	}
	
	public String toString() {
		String str = "";
		for (Card i : cards) {
			str += i;
			str += " ";
		}
		return str;
	}
}
