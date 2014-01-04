package sep.pack;

import java.util.concurrent.ConcurrentHashMap;

import com.ib.controller.Types;
import com.ib.controller.Types.Action;

public class TradeStrategy{
	private ConcurrentHashMap<Integer, String> ticker = new ConcurrentHashMap<Integer, String>();
	private ConcurrentHashMap<Integer, Integer> quantity = new ConcurrentHashMap<Integer, Integer>();
	private ConcurrentHashMap<Integer, Action> buySell = new ConcurrentHashMap<Integer, Action>();
	private ConcurrentHashMap<Integer, Boolean> isMarket = new ConcurrentHashMap<Integer, Boolean>();
	private ConcurrentHashMap<Integer, Double> limitPrice = new ConcurrentHashMap<Integer, Double>();
	
	public void updateTicker(int reqId, String tickerName){
		ticker.put(reqId, tickerName);
	}
	
	public void updatePair(int reqId1, int reqId2, ConcurrentHashMap<Integer, Quotes> latestNbbo){
		Quotes quote1 = latestNbbo.get(reqId1);
		Quotes quote2 = latestNbbo.get(reqId2);
		double orderImba1 = quote1.getBidSize() / (quote1.getBidSize() + quote1.getAskSize());
		double orderImba2 = quote2.getBidSize() / (quote2.getBidSize() + quote2.getAskSize());
		
		double priceDiff = quote1.getAsk() - quote2.getAsk();
		
		if ((priceDiff < 0) && (orderImba1 > 0.1) && (orderImba2 < 0.9)){
			quantity.put(reqId1, 100);
			buySell.put(reqId1, Types.Action.BUY);
			isMarket.put(reqId1, false);
			limitPrice.put(reqId1, quote1.getBid());
			
			quantity.put(reqId2, 100);
			buySell.put(reqId2, Types.Action.SELL);	
			isMarket.put(reqId2, false);
			limitPrice.put(reqId2, quote2.getAsk());
		}
		else if ((priceDiff > 0) && (orderImba1 < 0.9) && (orderImba2 > 0.1)){
			quantity.put(reqId1, 100);
			buySell.put(reqId1, Types.Action.SELL);
			isMarket.put(reqId1, false);
			limitPrice.put(reqId1, quote1.getAsk());
			
			quantity.put(reqId2, 100);
			buySell.put(reqId2, Types.Action.BUY);	
			isMarket.put(reqId2, false);
			limitPrice.put(reqId2, quote2.getBid());
		}
	}
}