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

import com.ib.controller.Types.Action;

public class TradeStrategy extends AbstractStrategy{
	
	private QuotesOrderLogger marketdata;

	private double windowSize;
	private long startMs;	
	private final int ABS_QUOTES_MIN = 10;
	private boolean isBurninSet = false;

	
	public TradeStrategy(QuotesOrderLogger md, TransCost tc, 
							ExpectedProfit profit, String tX, String tY, 
								double wS, int tS, double thres){
		marketdata = md;
		transCost = tc;
		expProfit = profit;
		tickerX = tX;
		tickerY = tY;
		windowSize = wS*1000*60; //wS is in minute, windowSize in milisecond
		tradeSize = tS;
		startMs = Calendar.getInstance().getTimeInMillis();
		threshold = thres;
	}
	
	@Override
	protected Map<String, List<Quotes>> getBurnIn(){
		ConcurrentHashMap<String, List<Quotes>> histQuotes = marketdata.getStoredData();
		while (histQuotes.get(tickerY)==null || histQuotes.get(tickerX)==null 
				|| histQuotes.get(tickerY).size() < ABS_QUOTES_MIN
				|| histQuotes.get(tickerX).size() < ABS_QUOTES_MIN
				|| (Calendar.getInstance().getTimeInMillis()-startMs) < windowSize){ //wait for more quotes, time window not reached
//			histQuotes = marketdata.getStoredData();
//			
//			System.out.println("Need to wait at least " + windowSize + " miliseconds.");
//			System.out.println("Need to obtain at least " + ABS_QUOTES_MIN + " quotes data points for each ticker. ");
//			
//			if (histQuotes.get(tickerY) != null){
//				System.out.println("TICKER " + tickerY + ": SIZE: " + histQuotes.get(tickerY).size());
//			}
//			if (histQuotes.get(tickerX) != null){
//				System.out.println("TICKER " + tickerX + ": SIZE: " + histQuotes.get(tickerX).size());
//			}
//			Thread.sleep(10000);
		}

		return histQuotes;
	}
	
//	private String getTradeInfo(String ticker, int pos, Quotes quotes, Action act, double transCost){
//		StringBuilder sb = new StringBuilder();
//		sb.append("[TRADE]: ");
//		sb.append(act.name());
//		sb.append(" " + ticker);
//		sb.append(" [Bid: ]");
//		sb.append(quotes.getBid());
//		sb.append("(" + quotes.getBidSize() + ")");
//		sb.append(" [Ask: ]");
//		sb.append(quotes.getAsk());
//		sb.append("(" + quotes.getAskSize() + ")");
//		sb.append(" [Imbalance: ]");
//		sb.append(quotes.getImbalance());
//		sb.append(" [Transaction Cost]: ");
//		sb.append(transCost);
//		sb.append(" [Existing Position]: ");
//		sb.append(pos);
//		return sb.toString();
//	}
	
//	private String getTradeRationale(Quotes quotesX, Quotes quotesY, 
//												Action actX, Action actY,
//													double residual, double exProfit,
//														double transCostX, double transCostY){
//		StringBuilder sb = new StringBuilder();
//		sb.append(getTradeInfo(tickerX, tradeSizeX, quotesX, actX, transCostX) + "\n");
//		sb.append(getTradeInfo(tickerY, tradeSizeY, quotesY, actY, transCostY));
//		sb.append("\n[Expected Profit]: ");
//		sb.append(exProfit);
//		sb.append(" [Residual]: ");
//		sb.append(residual);
//		return sb.toString();
//	}
	
	//TODO, must think about unfilled positions (marketdata.getUnfilledPosition()); 
	public List<OrderContractContainer> getOrdersFromHistQuotes() throws InterruptedException{
		System.out.println("Running Strategy...");
		
		if (!this.isBurninSet){
			computeAllFirstIntParams();
			isBurninSet = true;
			return null;
		}
		
		Quotes quotesY = marketdata.getLatestNbbo(tickerY);
		Quotes quotesX = marketdata.getLatestNbbo(tickerX);
		Pair<Action> decision = strategyDecision(quotesX, quotesY);
		
//		if (decision.getA() != null && decision.getB()!= null){
//			System.out.println(getTradeRationale(quotesX, quotesY, action1, action2, residual, expectedReturn, transCostX, transCostY));
//		}
		return getOrderFromIntel(tickerY, tickerX, Math.abs(tradeSizeX), Math.abs(tradeSizeY), decision.getA(), decision.getB());
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