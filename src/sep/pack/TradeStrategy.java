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
			position.put(ticker, position.get(ticker) + amount);				
		}
	}
	
	public double tCost(String ticker, double orderImba){
		double TC = 0;
		if (ticker == "SPY"){
			TC = orderImba;
		} else if (ticker == "SH"){
			TC = orderImba;
		} else if (ticker == "SSO"){
			TC = orderImba;
		} else if (ticker == "SDS"){
			TC = orderImba;
		} else if (ticker == "UPR"){
			TC = orderImba;
		} else if (ticker == "SPX"){
			TC = orderImba;
		}	
		return TC;
	}
	
	public double expectedProfit(String ticker1, String ticker2, double residual){
		double expProfit = 0;
		if (ticker1 == "SPY"){
			if (ticker2 == "SH"){
				expProfit = residual;
			}
		}
		return expProfit;
	}
	
	
	public void updatePair(String ticker1, String ticker2, ConcurrentHashMap<String, Quotes> latestNbbo, 
			double slope, int tradeSize, int windowSize, HashMap<String, Integer> position){
		double threshold = 0;
		Quotes quote1 = latestNbbo.get(ticker1);
		Quotes quote2 = latestNbbo.get(ticker2);
		double orderImba1 = quote1.getBidSize() / (quote1.getBidSize() + quote1.getAskSize());
		double orderImba2 = quote2.getBidSize() / (quote2.getBidSize() + quote2.getAskSize());
		double tradePrice1 =(quote1.getBidSize() + quote1.getAskSize()) / 2;
		double tradePrice2 =(quote2.getBidSize() + quote2.getAskSize()) / 2;
		
		double scaling = tradePrice1 / tradePrice2; // mean(tradePrice1)/mean(tradePrice2);
		
		double tradeSize1 = tradeSize;
		double tradeSize2 = tradeSize * scaling * Math.abs(slope);
		
		double residual = tradePrice1 - slope * tradePrice2;
				
		if (position.containsKey(ticker1) && position.containsKey(ticker2)){
			if ((position.get(ticker1) != 0) && (position.get(ticker2) != 0)) {
				return;
			}
		}
		
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