
import java.util.Map;
import java.util.Map.Entry;
import java.io.*;

public class Card implements Serializable {
	private char suit;
	private char val;

	private int getIntVal() {
		if (val == 'j' || val == 'q' || val == 'k') {
			return 10;
		} else {
			return (int)(val - '0'); 	
		}
	}
	//TODO
	
	public Card(char suit, char val) {
		this.suit = suit;
		this.val = Character.toLowerCase(val);
	}

	public char getSuit() {
		return suit;
	}

	public void setSuit(char suit) {
		this.suit = suit;
	}

	public char getVal() {
		return val;
	}

	public void setVal(char val) {
		this.val = Character.toLowerCase(val);
	}

	public int getMaxIntVal() {
		if(val == 'a') 
			return 11;
		else
			return getIntVal();
	}
	public int getMinIntVal() {
		if(val == 'a') 
			return 1;
		else
			return getIntVal();
	}
	
	public String toString() {
		String str = "";
		if (suit == 's') {
			str += "Spade";
		} else if (suit == 'd') {
			str += "Diamond";
		} else if (suit == 'h') {
			str += "Heart";
		} else if (suit == 'c') {
			str += "Club";
		}
		if (val == 'a') {
			str += "A";
		} else if (val == 'j') {
			str += 'J';
		} else if (val == 'k') {
			str += 'K';
		} else if (val == 'q') {
			str += 'Q';
		} else {
			str += (int)(val - '0');
		}
		return str;
	}
}
