package sep.pack.strategy;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sep.pack.OrderContractContainer;
import sep.pack.QuotesOrderLogger;
import sep.pack.data.Pair;
import sep.pack.data.Quotes;
import sep.pack.data.TICKER;
import sep.pack.support.OrderUtility;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

import com.ib.controller.Types.Action;

public class TradeStrategy{
	
	private QuotesOrderLogger marketdata;
	private TransCost transCost;
	private ExpectedProfit expProfit;
	private final double IB_TRANS_COST = 0.005;
	private String tickerX;
	private String tickerY;
	private double windowSize;
	private int tradeSize;
	private long startMs;
	
	private double oldBeta = 0.0;
	private double meanX = 0.0;
	private double meanY = 0.0;
	
	public final Map<String, Integer> tickerLeverage;
	{
		tickerLeverage = new HashMap<String, Integer>();
		tickerLeverage.put(TICKER.SPY, 1);
		tickerLeverage.put(TICKER.SH, -1);
		tickerLeverage.put(TICKER.SDS, -2);
		tickerLeverage.put(TICKER.SPX, -3);
		tickerLeverage.put(TICKER.SSO, 2);
		tickerLeverage.put(TICKER.UPR, 3);
	}
	
	public TradeStrategy(QuotesOrderLogger md, TransCost tc, 
							ExpectedProfit profit, String tX, String tY, double wS, int tS){
		marketdata = md;
		transCost = tc;
		expProfit = profit;
		tickerX = tX;
		tickerY = tY;
		windowSize = wS*1000*60; //wS is in minute, windowSize in milisecond
		tradeSize = tS;
		startMs = Calendar.getInstance().getTimeInMillis();
	}
	
	private void removeQuotes(List<Quotes> quoteRecords, Quotes latestQuotes, double windowSizeMs){
		long msTs = latestQuotes.getLocalTimeStamp().getTime();
		if (quoteRecords.size()>0){
			long earlyMsTs = quoteRecords.get(0).getLocalTimeStamp().getTime();
			if ( (msTs - earlyMsTs) > windowSizeMs){ //outside time window
				quoteRecords.remove(0);
				removeQuotes(quoteRecords, latestQuotes, windowSizeMs);
			}
		}
	}
	
	//Must ensure quotes has at least 2*windowSize elements
	private DoubleArrayList convertQuoteToDList(List<Quotes> quotes){
		DoubleArrayList l = new DoubleArrayList();
		for (Quotes q : quotes){
			l.add(q.getMidPrice());
		}
		return l;
	}
	
	private ConcurrentHashMap<String, List<Quotes>> getAndTrimHistoricalQuotes(String tickerX, String tickerY, double windowSizeMs, 
																				Quotes latestQuotesX, Quotes latestQuotesY) throws InterruptedException{
		ConcurrentHashMap<String, List<Quotes>> histQuotes = marketdata.getStoredData();
		while (histQuotes.get(tickerY)==null || histQuotes.get(tickerX)==null 
				|| (Calendar.getInstance().getTimeInMillis()-startMs) < windowSize){ //wait for more quotes, time window not reached
			histQuotes = marketdata.getStoredData();
			System.out.println("Need to wait at least " + windowSize + " miliseconds.");
			if (histQuotes.get(tickerY) != null){
				System.out.println("TICKER " + tickerY + ": SIZE: " + histQuotes.get(tickerY).size());
			}
			if (histQuotes.get(tickerX) != null){
				System.out.println("TICKER " + tickerX + ": SIZE: " + histQuotes.get(tickerX).size());
			}
			Thread.sleep(10000);
		}
		removeQuotes(histQuotes.get(tickerY), latestQuotesY, windowSizeMs);
		removeQuotes(histQuotes.get(tickerX), latestQuotesX, windowSizeMs);

		return histQuotes;
	}
	
	private double getLatestResidual(List<Quotes> xs, List<Quotes> ys, double slope, boolean computeAgain){
		if(computeAgain){//If we decide to recompute the residual using a new beta
			DoubleArrayList avgTickXQ = convertQuoteToDList(xs);
			DoubleArrayList avgTickYQ = convertQuoteToDList(ys);
			
			double meanY = Descriptive.mean(avgTickYQ);
			double meanX = Descriptive.mean(avgTickXQ);
			
			double scaling = meanY / meanX;
			DoubleArrayList resYs = new DoubleArrayList();

//			int diffSize = ys.size() - xs.size();
//			if (diffSize > 0){
//				for(int i=0; i<diffSize; i++){
//					System.out.println(">Removing " + i);
//					avgTickYQ.remove(i);
//				}
//			}else if(diffSize < 0){
//				for(int i=0; i<diffSize; i++){
//					avgTickXQ.remove(i);
//					System.out.println("<Removing " + i);
//				}
//			}
//			System.out.println("SizeX: " + avgTickXQ.size());
//			System.out.println("SizeY: " + avgTickYQ.size());
			
			for (int i=avgTickXQ.size()-1, j=avgTickYQ.size()-1; i>0 && j>0; --i,--j){
				double y = avgTickYQ.get(j) - slope*avgTickXQ.get(i)*scaling;
				resYs.add(y);
			}
//			System.out.println("SizeRes: " + resYs.size());
			return resYs.get(resYs.size()-1) - Descriptive.mean(resYs);
		}else{//else we use the old beta already computed from old quotes
			return ys.get(ys.size()-1).getMidPrice() - this.oldBeta*xs.get(xs.size()-1).getMidPrice();
		}
	}
	
	//TODO, must think about unfilled positions (marketdata.getUnfilledPosition()); 
	public List<OrderContractContainer> getOrdersFromHistQuotes() throws InterruptedException{
		System.out.println("Running Strategy...");
		double slope = (tickerLeverage.get(tickerY) + 0.0) / (tickerLeverage.get(tickerX) + 0.0);
			
		double threshold = 0;
		Quotes quotesY = marketdata.getLatestNbbo(tickerY);
		Quotes quotesX = marketdata.getLatestNbbo(tickerX);
		
		ConcurrentHashMap<String, List<Quotes>> histQuotes = getAndTrimHistoricalQuotes(tickerX, tickerY, windowSize, quotesX, quotesY);
		
		double orderImbaY = quotesY.getImbalance();
		double orderImbaX = quotesX.getImbalance();
		
		double midPriceY = quotesY.getMidPrice();
		double midPriceX = quotesX.getMidPrice();
		
		// Current Window
		DoubleArrayList avgTickYQ = convertQuoteToDList(histQuotes.get(tickerY));
		DoubleArrayList avgTickXQ = convertQuoteToDList(histQuotes.get(tickerX));
		
		double alpha = 1 - 1 / windowSize;
		if (meanY == 0.0){
			meanY = Descriptive.mean(convertQuoteToDList(histQuotes.get(tickerY)));
		}else{
			meanY = alpha * meanY + (1 - alpha) * midPriceY;
		}
		if (meanX == 0.0){
			meanX = Descriptive.mean(convertQuoteToDList(histQuotes.get(tickerX)));
		}else{
			meanX = alpha * meanX + (1 - alpha) * midPriceX;
		}
		
		double scaling = Descriptive.mean(avgTickYQ) / Descriptive.mean(avgTickXQ);
		
		int tradeSizeX = tradeSize;
		int tradeSizeY = (int) (tradeSize * scaling * Math.abs(slope));
		
 		double residual =  getLatestResidual(histQuotes.get(tickerX), histQuotes.get(tickerY), slope, true);
 		
 		System.out.println("Residual Computed: " + residual);

		Action action1 = null;
		Action action2 = null;
		
		double expectedReturn = expProfit.getExpectedProf(new Pair<String>(tickerX, tickerY), residual);
		
		System.out.println("Expected Profit Computed: " + expectedReturn);
		System.out.println("Position for " + tickerX + " is " + marketdata.getPosition(tickerX));
		System.out.println("Position for " + tickerY + " is " + marketdata.getPosition(tickerY));
		
		if (slope < 0){ // small residual: buy both; large residual: sell both;
			// no position
			if ((marketdata.getPosition(tickerY) == 0) && (marketdata.getPosition(tickerX) == 0)){
				if ( expectedReturn > threshold 
						+ tradeSizeX * (IB_TRANS_COST + quotesY.getAsk() - midPriceY - transCost.getTransCost(tickerY, orderImbaY)) 
						+ tradeSizeY * (IB_TRANS_COST + quotesX.getAsk() - midPriceX - transCost.getTransCost(tickerX, orderImbaX))){
					// buy both at ask
					action1 = Action.BUY; action2 = Action.BUY;
				}
				else if ( -expectedReturn > threshold 
						+ tradeSizeX * (IB_TRANS_COST - quotesY.getBid() + midPriceY + transCost.getTransCost(tickerY, orderImbaY)) 
						+ tradeSizeY * (IB_TRANS_COST - quotesX.getBid() + midPriceX + transCost.getTransCost(tickerX, orderImbaX))){
					// sell both at bid
					action1 = Action.SELL; action2 = Action.SELL;					
				}
			}
			// long position, short only
			else if ((marketdata.getPosition(tickerY) > 0) && (marketdata.getPosition(tickerX) > 0)){
				if ( -expectedReturn > threshold 
						+ tradeSizeX * (IB_TRANS_COST - quotesY.getBid() + midPriceY + transCost.getTransCost(tickerY, orderImbaY)) 
						+ tradeSizeY * (IB_TRANS_COST - quotesX.getBid() + midPriceX + transCost.getTransCost(tickerX, orderImbaX))){
					// sell at both bid
					action1 = Action.SELL; action2 = Action.SELL;					
				}		
			}
			// short position, long only
			else if ((marketdata.getPosition(tickerY) < 0) && (marketdata.getPosition(tickerX) < 0)){
				if ( expectedReturn > threshold 
						+ tradeSizeX * (IB_TRANS_COST + quotesY.getAsk() - midPriceY - transCost.getTransCost(tickerY, orderImbaY)) 
						+ tradeSizeY * (IB_TRANS_COST + quotesX.getAsk() - midPriceX - transCost.getTransCost(tickerX, orderImbaX))){
					// buy at both ask
					action1 = Action.BUY; action2 = Action.BUY;
				}
			}
		}
		else if (slope > 0){ // small residual: buy ticker1 and sell ticker2; large residual: sell ticker1 and buy ticker2;
			// no position
			if ((marketdata.getPosition(tickerY) == 0) && (marketdata.getPosition(tickerX) == 0)){
				if ( expectedReturn > threshold 
						+ tradeSizeX * (IB_TRANS_COST + quotesY.getAsk() - midPriceY - transCost.getTransCost(tickerY, orderImbaY)) 
						+ tradeSizeY * (IB_TRANS_COST - quotesX.getBid() + midPriceX + transCost.getTransCost(tickerX, orderImbaX))){
					// buy ticker1 at ask; sell ticker2 at bid
					action1 = Action.BUY; action2 = Action.SELL;
				}
				else if ( -expectedReturn > threshold 
						+ tradeSizeX * (IB_TRANS_COST - quotesY.getBid() + midPriceY + transCost.getTransCost(tickerY, orderImbaY)) 
						+ tradeSizeY * (IB_TRANS_COST + quotesX.getAsk() - midPriceX - transCost.getTransCost(tickerX, orderImbaX))){
					// sell ticker1 at bid; buy ticker2 at ask
					action1 = Action.SELL; action2 = Action.BUY;					
				}
			}
			// ticker1 long position, sell ticker1 and buy ticker2 only
			else if ((marketdata.getPosition(tickerY) > 0) && (marketdata.getPosition(tickerX) < 0)){
				if ( -expectedReturn > threshold 
						+ tradeSizeX * (IB_TRANS_COST - quotesY.getBid() + midPriceY + transCost.getTransCost(tickerY, orderImbaY)) 
						+ tradeSizeY * (IB_TRANS_COST + quotesX.getAsk() - midPriceX - transCost.getTransCost(tickerX, orderImbaX))){
					// sell ticker1 at bid; buy ticker2 at ask
					action1 = Action.SELL; action2 = Action.BUY;
				}		
			}
			// ticker1 short position, buy ticker1 and sell ticker2 only
			else if ((marketdata.getPosition(tickerY) < 0) && (marketdata.getPosition(tickerX) < 0)){
				if (expectedReturn > threshold 
						+ tradeSizeX * (IB_TRANS_COST + quotesY.getAsk() - midPriceY - transCost.getTransCost(tickerY, orderImbaY)) 
						+ tradeSizeY * (IB_TRANS_COST - quotesX.getBid() + midPriceX + transCost.getTransCost(tickerX, orderImbaX))){
					// buy ticker1 at ask; sell ticker2 at bid
					action1 = Action.BUY; action2 = Action.SELL;
				}
			}
		}//end elseif
		
		return getOrderFromIntel(tickerY, tickerX, Math.abs(tradeSizeX), Math.abs(tradeSizeY), action1, action2);
	}
	
	private List<OrderContractContainer> getOrderFromIntel(String ticker1, String ticker2,
														  int size1, int size2,
														  Action action1, Action action2){
		List<OrderContractContainer> orders = new LinkedList<OrderContractContainer>();
		if (action1 == null || action2 == null){
			System.out.println("No Order Generated");
			return orders; // 0 size, no trades
		}
		
		OrderContractContainer oc1 = new OrderContractContainer(OrderUtility.createContract(ticker1), 
																OrderUtility.createNewOrder(size1, action1));
		orders.add(oc1);
		OrderContractContainer oc2 = new OrderContractContainer(OrderUtility.createContract(ticker2), 
																OrderUtility.createNewOrder(size2, action2));
		orders.add(oc2);
		System.out.println("Orders Generated");
		return orders;
	}

	@Override
	public String toString() {
		return "TradeStrategy [Pair Trading], Pairs: " + tickerX + ", and " + tickerY + "\n";
	}

}