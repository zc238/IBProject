package sep.pack;

import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.ib.controller.Types;
import com.ib.controller.Types.Action;

public class TradeStrategy{
	private HashMap<String, Integer> position = new HashMap<String, Integer>();

	private Vector<Integer> quantity = new Vector<Integer>();
	private Vector<Action> buySell = new Vector<Action>();
	private Vector<Boolean> isMarket = new Vector<Boolean>();
	private Vector<Double> limitPrice = new Vector<Double>();
	
	public void updatePosition(String ticker, int amount){
		if (!position.containsKey(ticker)){
			position.put(ticker, amount);
		}
		else {
			position.put(ticker, position.get(ticker)+amount);				
		}
	}
	
	public void updatePair(String ticker1, String ticker2, ConcurrentHashMap<String, Quotes> latestNbbo, String pairType){
		Quotes quote1 = latestNbbo.get(ticker1);
		Quotes quote2 = latestNbbo.get(ticker2);
		double orderImba1 = quote1.getBidSize() / (quote1.getBidSize() + quote1.getAskSize());
		double orderImba2 = quote2.getBidSize() / (quote2.getBidSize() + quote2.getAskSize());
		
		double priceDiff = quote1.getAsk() - quote2.getAsk();
				
		if ((priceDiff < 0) && (orderImba1 > 0.1) && (orderImba2 < 0.9)){
			quantity.addElement(100);
			quantity.addElement(100);
			isMarket.addElement(false);
			isMarket.addElement(false);			
			if (pairType == "irreverse"){
				buySell.addElement(Types.Action.BUY);
				buySell.addElement(Types.Action.SELL);
				limitPrice.addElement(quote1.getBid());
				limitPrice.addElement(quote2.getAsk());
				updatePosition(ticker1,100);
				updatePosition(ticker2,-100);				
				
			} else if (pairType == "reverse") {
				buySell.addElement(Types.Action.BUY);
				buySell.addElement(Types.Action.BUY);
				limitPrice.addElement(quote1.getBid());
				limitPrice.addElement(quote2.getBid());
				updatePosition(ticker1,100);
				updatePosition(ticker2,100);
			}
		} else if ((priceDiff > 0) && (orderImba1 < 0.9) && (orderImba2 > 0.1)){
			quantity.addElement(100);
			quantity.addElement(100);
			isMarket.addElement(false);
			isMarket.addElement(false);			
			if (pairType == "irreverse"){
				buySell.addElement(Types.Action.SELL);
				buySell.addElement(Types.Action.BUY);
				limitPrice.addElement(quote1.getAsk());
				limitPrice.addElement(quote2.getBid());
				updatePosition(ticker1,-100);
				updatePosition(ticker2,100);				
				
			} else if (pairType == "reverse") {
				buySell.addElement(Types.Action.SELL);
				buySell.addElement(Types.Action.SELL);
				limitPrice.addElement(quote1.getAsk());
				limitPrice.addElement(quote2.getAsk());
				updatePosition(ticker1,-100);
				updatePosition(ticker2,-100);
			}
		}
	}
}