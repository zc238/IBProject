package sep.pack.strategy;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sep.pack.OrderContractContainer;
import sep.pack.QuotesOrderLogger;
import sep.pack.data.Pair;
import sep.pack.data.Quotes;
import sep.pack.support.OrderUtility;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

import com.ib.controller.Types.Action;

public class TradeStrategy{
	
	private QuotesOrderLogger marketdata;
	private TransCost transCost;
	private ExpectedProfit expProfit;
	
	private String tickerX;
	private String tickerY;
	private double windowSize;
	private int tradeSize;
	private long startMs;
	
	private double oldBeta = 0.0;
	private double meanX = 0.0;
	private double meanY = 0.0;
	private final int ABS_QUOTES_MIN = 10;
	
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
	
	
	
	private Map<String, List<Quotes>> getAndTrimHistoricalQuotes(String tickerX, String tickerY, double windowSizeMs, 
																				Quotes latestQuotesX, Quotes latestQuotesY) throws InterruptedException{
		ConcurrentHashMap<String, List<Quotes>> histQuotes = marketdata.getStoredData();
		while (histQuotes.get(tickerY)==null || histQuotes.get(tickerX)==null 
				|| histQuotes.get(tickerY).size() < ABS_QUOTES_MIN
				|| histQuotes.get(tickerX).size() < ABS_QUOTES_MIN
				|| (Calendar.getInstance().getTimeInMillis()-startMs) < windowSize){ //wait for more quotes, time window not reached
			histQuotes = marketdata.getStoredData();
			
			System.out.println("Need to wait at least " + windowSize + " miliseconds.");
			System.out.println("Need to obtain at least " + ABS_QUOTES_MIN + " quotes data points for each ticker. ");
			
			if (histQuotes.get(tickerY) != null){
				System.out.println("TICKER " + tickerY + ": SIZE: " + histQuotes.get(tickerY).size());
			}
			if (histQuotes.get(tickerX) != null){
				System.out.println("TICKER " + tickerX + ": SIZE: " + histQuotes.get(tickerX).size());
			}
			Thread.sleep(10000);
		}
		StrategyUtility.removeQuotes(histQuotes.get(tickerY), latestQuotesY, windowSizeMs, latestQuotesY.getLocalTimeStamp().getTime());
		StrategyUtility.removeQuotes(histQuotes.get(tickerX), latestQuotesX, windowSizeMs, latestQuotesX.getLocalTimeStamp().getTime());

		return histQuotes;
	}
	
	private String getTradeInfo(String ticker, int pos, Quotes quotes, Action act, double transCost){
		StringBuilder sb = new StringBuilder();
		sb.append("[TRADE]: ");
		sb.append(act.name());
		sb.append(" " + ticker);
		sb.append(" [Bid: ]");
		sb.append(quotes.getBid());
		sb.append("(" + quotes.getBidSize() + ")");
		sb.append(" [Ask: ]");
		sb.append(quotes.getAsk());
		sb.append("(" + quotes.getAskSize() + ")");
		sb.append(" [Imbalance: ]");
		sb.append(quotes.getImbalance());
		sb.append(" [Transaction Cost]: ");
		sb.append(transCost);
		sb.append(" [Existing Position]: ");
		sb.append(pos);
		return sb.toString();
	}
	
	private String getTradeRationale(String tickerX, String tickerY, 
										int posX, int posY, 
											Quotes quotesX, Quotes quotesY, 
												Action actX, Action actY,
													double residual, double exProfit,
														double transCostX, double transCostY){
		StringBuilder sb = new StringBuilder();
		sb.append(getTradeInfo(tickerX, posX, quotesX, actX, transCostX) + "\n");
		sb.append(getTradeInfo(tickerY, posY, quotesY, actY, transCostY));
		sb.append("\n[Expected Profit]: ");
		sb.append(exProfit);
		sb.append(" [Residual]: ");
		sb.append(residual);
		return sb.toString();
	}
	
	//TODO, must think about unfilled positions (marketdata.getUnfilledPosition()); 
	public List<OrderContractContainer> getOrdersFromHistQuotes() throws InterruptedException{
		System.out.println("Running Strategy...");

		double slope = (StrategyConstants.tickerLeverage.get(tickerY) + 0.0) / (StrategyConstants.tickerLeverage.get(tickerX) + 0.0);
			
		double threshold = 0;
		Quotes quotesY = marketdata.getLatestNbbo(tickerY);
		Quotes quotesX = marketdata.getLatestNbbo(tickerX);
		
		Map<String, List<Quotes>> histQuotes = getAndTrimHistoricalQuotes(tickerX, tickerY, windowSize, quotesX, quotesY);
		
		double orderImbaY = quotesY.getImbalance();
		double orderImbaX = quotesX.getImbalance();
		
		double midPriceY = quotesY.getMidPrice();
		double midPriceX = quotesX.getMidPrice();
		
		// Current Window
		DoubleArrayList avgTickYQ = StrategyUtility.convertQuoteToDList(histQuotes.get(tickerY));
		DoubleArrayList avgTickXQ = StrategyUtility.convertQuoteToDList(histQuotes.get(tickerX));
		
		double alpha = 1 - 1 / windowSize;
		if (meanY == 0.0){
			meanY = Descriptive.mean(avgTickYQ);
		}else{
			meanY = alpha * meanY + (1 - alpha) * midPriceY;
		}
		if (meanX == 0.0){
			meanX = Descriptive.mean(avgTickXQ);
		}else{
			meanX = alpha * meanX + (1 - alpha) * midPriceX;
		}
		
		double scaling = Descriptive.mean(avgTickYQ) / Descriptive.mean(avgTickXQ);
		
		int tradeSizeX = tradeSize;
		int tradeSizeY = (int) (tradeSize * scaling * Math.abs(slope));
		
 		double residual =  StrategyUtility.getLatestResidual(histQuotes.get(tickerX), histQuotes.get(tickerY), slope, this.oldBeta, true);
 		
 		System.out.println("Residual Computed: " + residual);

		Action action1 = null;
		Action action2 = null;
		
		double expectedReturn = expProfit.getExpectedProf(new Pair<String>(tickerX, tickerY), residual);
		
		System.out.println("Expected Profit Computed: " + expectedReturn);
		System.out.println("Position for " + tickerX + " is " + marketdata.getPosition(tickerX));
		System.out.println("Position for " + tickerY + " is " + marketdata.getPosition(tickerY));
		
		int xPosition = marketdata.getPosition(tickerX);
		int yPosition = marketdata.getPosition(tickerY);
		double transCostX = transCost.getTransCost(tickerX, orderImbaX);
		double transCostY = transCost.getTransCost(tickerY, orderImbaY);
		
		if (slope < 0){ // small residual: buy both; large residual: sell both;
			// no position
			if (xPosition == 0 && yPosition == 0){
				if ( expectedReturn > threshold 
						+ tradeSizeX * (StrategyConstants.IB_TRANS_COST + quotesY.getAsk() - midPriceY - transCostY) 
						+ tradeSizeY * (StrategyConstants.IB_TRANS_COST + quotesX.getAsk() - midPriceX - transCostX)){
					// buy both at ask
					action1 = Action.BUY; action2 = Action.BUY;
				}
				else if ( -expectedReturn > threshold 
						+ tradeSizeX * (StrategyConstants.IB_TRANS_COST - quotesY.getBid() + midPriceY + transCostY) 
						+ tradeSizeY * (StrategyConstants.IB_TRANS_COST - quotesX.getBid() + midPriceX + transCostX)){
					// sell both at bid
					action1 = Action.SELL; action2 = Action.SELL;					
				}
			}
			// long position, short only
			else if (xPosition > 0 && yPosition > 0){
				if ( -expectedReturn > threshold 
						+ tradeSizeX * (StrategyConstants.IB_TRANS_COST - quotesY.getBid() + midPriceY + transCostY) 
						+ tradeSizeY * (StrategyConstants.IB_TRANS_COST - quotesX.getBid() + midPriceX + transCostX)){
					// sell at both bid
					action1 = Action.SELL; action2 = Action.SELL;					
				}		
			}
			// short position, long only
			else if (xPosition < 0 && yPosition < 0){
				if ( expectedReturn > threshold 
						+ tradeSizeX * (StrategyConstants.IB_TRANS_COST + quotesY.getAsk() - midPriceY - transCostY) 
						+ tradeSizeY * (StrategyConstants.IB_TRANS_COST + quotesX.getAsk() - midPriceX - transCostX)){
					// buy at both ask
					action1 = Action.BUY; action2 = Action.BUY;
				}
			}
		}
		else if (slope > 0){ // small residual: buy ticker1 and sell ticker2; large residual: sell ticker1 and buy ticker2;
			// no position
			if (xPosition == 0 && yPosition == 0){
				if ( expectedReturn > threshold 
						+ tradeSizeX * (StrategyConstants.IB_TRANS_COST + quotesY.getAsk() - midPriceY - transCostY) 
						+ tradeSizeY * (StrategyConstants.IB_TRANS_COST - quotesX.getBid() + midPriceX + transCostX)){
					// buy ticker1 at ask; sell ticker2 at bid
					action1 = Action.BUY; action2 = Action.SELL;
				}
				else if ( -expectedReturn > threshold 
						+ tradeSizeX * (StrategyConstants.IB_TRANS_COST - quotesY.getBid() + midPriceY + transCostY) 
						+ tradeSizeY * (StrategyConstants.IB_TRANS_COST + quotesX.getAsk() - midPriceX - transCostX)){
					// sell ticker1 at bid; buy ticker2 at ask
					action1 = Action.SELL; action2 = Action.BUY;					
				}
			}
			// ticker1 long position, sell ticker1 and buy ticker2 only
			else if (xPosition < 0 && yPosition > 0){
				if ( -expectedReturn > threshold 
						+ tradeSizeX * (StrategyConstants.IB_TRANS_COST - quotesY.getBid() + midPriceY + transCostY) 
						+ tradeSizeY * (StrategyConstants.IB_TRANS_COST + quotesX.getAsk() - midPriceX - transCostX)){
					// sell ticker1 at bid; buy ticker2 at ask
					action1 = Action.SELL; action2 = Action.BUY;
				}		
			}
			// ticker1 short position, buy ticker1 and sell ticker2 only
			else if (xPosition < 0 && yPosition < 0){
				if (expectedReturn > threshold 
						+ tradeSizeX * (StrategyConstants.IB_TRANS_COST + quotesY.getAsk() - midPriceY - transCostY) 
						+ tradeSizeY * (StrategyConstants.IB_TRANS_COST - quotesX.getBid() + midPriceX + transCostX)){
					// buy ticker1 at ask; sell ticker2 at bid
					action1 = Action.BUY; action2 = Action.SELL;
				}
			}
		}//end elseif
		
		if (action1 != null && action2 != null){
			System.out.println(getTradeRationale(tickerX, tickerY, xPosition, yPosition, quotesX, quotesY, action1, action2, residual, expectedReturn, transCostX, transCostY));
		}
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