import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
import java.io.*;

public class Deck implements Serializable {
	
		Stack<Card> deck;
		public Deck()
		{
			deck = new Stack<Card>();
			reset();	
		}
		
		public void shuffle()
		{
			Collections.shuffle(deck);
		}
		
		public Card getTopCard()
		{
			return deck.pop();
		}
		
		public void reset()
		{
			deck.clear();
			char[] suits = new char[]{'c', 'd', 'h', 's'};
			for (int cnt = 0; cnt < 6; cnt++) {
				for (int i=0; i<4; i++)
				{
					deck.push(new Card(suits[i], 'a'));
					for (int j=2; j<=10; j++)
					{
						deck.push(new Card(suits[i], (char)(j + '0')));
					}
					deck.push(new Card(suits[i], 'j'));
					deck.push(new Card(suits[i], 'k'));
					deck.push(new Card(suits[i], 'q'));
				}
			}
			Collections.shuffle(deck);
		}
}