package sep.pack.strategy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import sep.pack.OrderContractContainer;
import sep.pack.OrderUtility;
import sep.pack.Quotes;
import sep.pack.QuotesOrderLogger;
import sep.pack.data.Pair;
import sep.pack.data.TICKER;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

import com.ib.controller.Types.Action;

public class TradeStrategy{
	
	private QuotesOrderLogger marketdata;
	private CubicTransCost transCost;
	private ExpectedProfit expProfit;
	private final double IB_TRANS_COST = 0.005;
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
	
	public TradeStrategy(QuotesOrderLogger md, CubicTransCost tc, ExpectedProfit profit){
		marketdata = md;
		transCost = tc;
		expProfit = profit;
	}
	
//	private class Regression{
//		private double beta;
//		private double intercept;
//		
//		public Regression(DoubleArrayList daYs, DoubleArrayList daXs){
//			beta = Descriptive.covariance(daYs, daXs) / Descriptive.covariance(daXs, daXs);
//			intercept = Descriptive.mean(daYs) - Descriptive.mean(daXs) * beta;
//		}
//		
//		public double getBeta() {
//			return beta;
//		}
//		
//		public double getIntercept() {
//			return intercept;
//		}
//	}
	
	private Vector<Quotes> trimQuotes(Vector<Quotes> quotes, boolean usePreviousWindow, int windowSize){
		Vector<Quotes> l = new Vector<Quotes>();
		if (usePreviousWindow){
			for (int i=quotes.size()-2*windowSize; i < quotes.size()-windowSize; ++i){
				l.add(quotes.get(i));
			}
		}
		else{
			for (int i=quotes.size()-windowSize; i < quotes.size(); ++i){
				l.add(quotes.get(i));
			}
		}
		return l;
	}
	
	//Must ensure quotes has at least 2*windowSize elements
	private DoubleArrayList convertQuoteToDList(Vector<Quotes> quotes){
		DoubleArrayList l = new DoubleArrayList();
		for (Quotes q : quotes){
			l.add(q.getMidPrice());
		}
		return l;
	}
	
	private HashMap<String, Vector<Quotes>> getHistoricalQuotes(String tickerX, String tickerY, int windowSize) throws InterruptedException{
		HashMap<String, Vector<Quotes>> histQuotes = marketdata.getStoredData();
		while (histQuotes.get(tickerY)==null || histQuotes.get(tickerX)==null 
				|| histQuotes.get(tickerY).size() < windowSize*2 
				|| histQuotes.get(tickerX).size() < windowSize*2){ //wait for more quotes
			histQuotes = marketdata.getStoredData();
			Thread.sleep(500);
		}
		return histQuotes;
	}
	
	private double getLatestResidual(Vector<Quotes> xs, Vector<Quotes> ys, double slope){
		DoubleArrayList avgTickYQ = convertQuoteToDList(xs);
		DoubleArrayList avgTickXQ = convertQuoteToDList(ys);
		
		double meanY = Descriptive.mean(avgTickYQ);
		double meanX = Descriptive.mean(avgTickXQ);
		
		double scaling = meanY / meanX;
		Vector<Double> residuals = new Vector<Double>();
		for (int i=0; i<ys.size(); ++i){
			double r = avgTickYQ.get(i) - Math.abs(slope)*avgTickXQ.get(i)*scaling - meanY;
			residuals.add(r);
		}
		return residuals.get(ys.size()-1);
	}
	
	//TODO, must think about unfilled positions (marketdata.getUnfilledPosition()); does not need to consider for paper trading, since all positions are filled immediately
	public List<OrderContractContainer> getOrdersFromHistQuotes(String tickerY, String tickerX, 
																		int tradeSize, int windowSize) throws InterruptedException{
		
		double slope = (tickerLeverage.get(tickerY) + 0.0) / (tickerLeverage.get(tickerX) + 0.0);
		HashMap<String, Vector<Quotes>> histQuotes = getHistoricalQuotes(tickerX, tickerY, windowSize);
		
		double threshold = 0;
		Quotes quotesY = marketdata.getLatestNbbo(tickerY);
		Quotes quotesX = marketdata.getLatestNbbo(tickerX);
		double orderImbaY = quotesY.getImbalance();
		double orderImbaX = quotesX.getImbalance();
		
		double midPriceY = quotesY.getMidPrice();
		double midPriceX = quotesX.getMidPrice();
		
		// Previous Window
		DoubleArrayList avgTickYQHist = convertQuoteToDList(trimQuotes(histQuotes.get(tickerY), true, windowSize));
		DoubleArrayList avgTickXQHist = convertQuoteToDList(trimQuotes(histQuotes.get(tickerX), true, windowSize));
		
		// Current Window
		DoubleArrayList avgTickYQ = convertQuoteToDList(trimQuotes(histQuotes.get(tickerY), false, windowSize));
		DoubleArrayList avgTickXQ = convertQuoteToDList(trimQuotes(histQuotes.get(tickerX), false, windowSize));
		
		double alpha = 1 - 1 / windowSize;
		double meanY = Descriptive.mean(avgTickYQHist);
		double meanX = Descriptive.mean(avgTickXQHist);
		
		double scaling = Descriptive.mean(avgTickYQ) / Descriptive.mean(avgTickXQ);
		
		int tradeSizeX = tradeSize;
		int tradeSizeY = (int) (tradeSize * scaling * Math.abs(slope));
		
 		double residual =  getLatestResidual(trimQuotes(histQuotes.get(tickerX), false, windowSize),
 										trimQuotes(histQuotes.get(tickerY), false, windowSize), slope);

		meanY = alpha * meanY + (1 - alpha) * midPriceY;
		meanX = alpha * meanX + (1 - alpha) * midPriceX;

		Action action1 = null;
		Action action2 = null;
		
		double expectedReturn = expProfit.getExpectedProf(new Pair<String>(tickerY, tickerX), residual);
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
			return orders; // 0 size, no trades
		}
		
		OrderContractContainer oc1 = new OrderContractContainer(OrderUtility.createContract(ticker1), 
																OrderUtility.createNewOrder(size1, action1));
		orders.add(oc1);
		OrderContractContainer oc2 = new OrderContractContainer(OrderUtility.createContract(ticker2), 
																OrderUtility.createNewOrder(size2, action2));
		orders.add(oc2);
		return orders;
	}
}