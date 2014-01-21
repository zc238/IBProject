package sep.pack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

import com.ib.controller.Types.Action;

public class TradeStrategy{
	
	private QuotesOrderLogger marketdata;
	
	public TradeStrategy(QuotesOrderLogger md){
		marketdata = md;
	}
	
	private class Regression{
		private double beta;
		private double intercept;
		
		public Regression(DoubleArrayList daYs, DoubleArrayList daXs){
			beta = Descriptive.covariance(daYs, daXs) / Descriptive.covariance(daXs, daXs);
			intercept = Descriptive.mean(daYs) - Descriptive.mean(daXs) * beta;
		}
		
		public double getBeta() {
			return beta;
		}
		
		public double getIntercept() {
			return intercept;
		}
	}
		
	public double tCost(String ticker, double orderImba){
		double TC = 0;
		if (ticker == "SPY"){
			TC = 0; // a function of orderImba
		}
		else if (ticker == "SH"){
			TC = 0;
		}
		else if (ticker == "SSO"){
			TC = 0;
		}
		else if (ticker == "SDS"){
			TC = 0;
		}
		else if (ticker == "UPR"){
			TC = 0;
		}
		else if (ticker == "SPX"){
			TC = 0;
		}	
		return TC;
	}
	
	public double expectedProfit(String ticker1, String ticker2, double residual){
		double expProfit = 0;
		if (ticker1 == "SPY"){
			if (ticker2 == "SH"){
				expProfit = 0; // a function of residual
			}
		}
		return expProfit;
	}
	
	private DoubleArrayList convertQuoteToDList(List<Quotes> quotes){
		DoubleArrayList l = new DoubleArrayList();
		for (Quotes q : quotes){
			l.add(q.getMidPrice());
		}
		return l;
	}
	
	//TODO, must think about unfilled positions (marketdata.getUnfilledPosition()); does not need to consider for paper trading, since all positions are filled immediately
	public List<OrderContractContainer> getOrdersFromHistQuotes(String tickerY, String tickerX, 
																	double slope, int tradeSize, int windowSize){
		HashMap<String, Vector<Quotes>> histQuotes = marketdata.getStoredData();
		double threshold = 0;
		Quotes quotesY = marketdata.getLatestNbbo(tickerY);
		Quotes quotesX = marketdata.getLatestNbbo(tickerX);
		double orderImbaY = quotesY.getImbalance();
		double orderImbaX = quotesX.getImbalance();
		
		double midPriceY = quotesY.getMidPrice();
		double midPriceX = quotesX.getMidPrice();
		
		DoubleArrayList avgTickYQ = convertQuoteToDList(histQuotes.get(tickerY));
		DoubleArrayList avgTickXQ = convertQuoteToDList(histQuotes.get(tickerX));
		
		double alpha = 1 - 1 / windowSize;
		double meanY = Descriptive.mean(avgTickYQ);
		double meanX = Descriptive.mean(avgTickXQ);
		
		double scaling = meanY / meanX;
		
		int tradeSize1 = tradeSize;
		int tradeSize2 = (int) (tradeSize * scaling * Math.abs(slope));
		
		Regression reg = new Regression(avgTickYQ, avgTickXQ);
		
		double residual = midPriceY - midPriceX * reg.getBeta() - reg.getIntercept();
		
		meanY = alpha * meanY + (1 - alpha) * midPriceY;
		meanX = alpha * meanX + (1 - alpha) * midPriceX;

		Action action1 = null;
		Action action2 = null;
		
		if (slope < 0){ // small residual: buy both; large residual: sell both;
			// no position
			if ((marketdata.getPosition(tickerY) == 0) && (marketdata.getPosition(tickerX) == 0)){
				if (expectedProfit(tickerY, tickerX, residual) > threshold 
						+ tradeSize1 * (0.005 + quotesY.getAsk() - midPriceY - tCost(tickerY, orderImbaY)) 
						+ tradeSize2 * (0.005 + quotesX.getAsk() - midPriceX - tCost(tickerX, orderImbaX))){
					// buy both at ask
					action1 = Action.BUY; action2 = Action.BUY;
				}
				else if (-expectedProfit(tickerY, tickerX, residual) > threshold 
						+ tradeSize1 * (0.005 - quotesY.getBid() + midPriceY + tCost(tickerY, orderImbaY)) 
						+ tradeSize2 * (0.005 - quotesX.getBid() + midPriceX + tCost(tickerX, orderImbaX))){
					// sell both at bid
					action1 = Action.SELL; action2 = Action.SELL;					
				}
			}
			// long position, short only
			else if ((marketdata.getPosition(tickerY) > 0) && (marketdata.getPosition(tickerX) > 0)){
				if (-expectedProfit(tickerY, tickerX, residual) > threshold 
						+ tradeSize1 * (0.005 - quotesY.getBid() + midPriceY + tCost(tickerY, orderImbaY)) 
						+ tradeSize2 * (0.005 - quotesX.getBid() + midPriceX + tCost(tickerX, orderImbaX))){
					// sell at both bid
					action1 = Action.SELL; action2 = Action.SELL;					
				}		
			}
			// short position, long only
			else if ((marketdata.getPosition(tickerY) < 0) && (marketdata.getPosition(tickerX) < 0)){
				if (expectedProfit(tickerY, tickerX, residual) > threshold 
						+ tradeSize1 * (0.005 + quotesY.getAsk() - midPriceY - tCost(tickerY, orderImbaY)) 
						+ tradeSize2 * (0.005 + quotesX.getAsk() - midPriceX - tCost(tickerX, orderImbaX))){
					// buy at both ask
					action1 = Action.BUY; action2 = Action.BUY;
				}
			}
		}
		else if (slope > 0){ // small residual: buy ticker1 and sell ticker2; large residual: sell ticker1 and buy ticker2;
			// no position
			if ((marketdata.getPosition(tickerY) == 0) && (marketdata.getPosition(tickerX) == 0)){
				if (expectedProfit(tickerY, tickerX, residual) > threshold 
						+ tradeSize1 * (0.005 + quotesY.getAsk() - midPriceY - tCost(tickerY, orderImbaY)) 
						+ tradeSize2 * (0.005 - quotesX.getBid() + midPriceX + tCost(tickerX, orderImbaX))){
					// buy ticker1 at ask; sell ticker2 at bid
					action1 = Action.BUY; action2 = Action.SELL;
				}
				else if (-expectedProfit(tickerY, tickerX, residual) > threshold 
						+ tradeSize1 * (0.005 - quotesY.getBid() + midPriceY + tCost(tickerY, orderImbaY)) 
						+ tradeSize2 * (0.005 + quotesX.getAsk() - midPriceX - tCost(tickerX, orderImbaX))){
					// sell ticker1 at bid; buy ticker2 at ask
					action1 = Action.SELL; action2 = Action.BUY;					
				}
			}
			// ticker1 long position, sell ticker1 and buy ticker2 only
			else if ((marketdata.getPosition(tickerY) > 0) && (marketdata.getPosition(tickerX) < 0)){
				if (-expectedProfit(tickerY, tickerX, residual) > threshold 
						+ tradeSize1 * (0.005 - quotesY.getBid() + midPriceY + tCost(tickerY, orderImbaY)) 
						+ tradeSize2 * (0.005 + quotesX.getAsk() - midPriceX - tCost(tickerX, orderImbaX))){
					// sell ticker1 at bid; buy ticker2 at ask
					action1 = Action.SELL; action2 = Action.BUY;
				}		
			}
			// ticker1 short position, buy ticker1 and sell ticker2 only
			else if ((marketdata.getPosition(tickerY) < 0) && (marketdata.getPosition(tickerX) < 0)){
				if (expectedProfit(tickerY, tickerX, residual) > threshold 
						+ tradeSize1 * (0.005 + quotesY.getAsk() - midPriceY - tCost(tickerY, orderImbaY)) 
						+ tradeSize2 * (0.005 - quotesX.getBid() + midPriceX + tCost(tickerX, orderImbaX))){
					// buy ticker1 at ask; sell ticker2 at bid
					action1 = Action.BUY; action2 = Action.SELL;
				}
			}
		}//end elseif
		
		return getOrderFromIntel(tickerY, tickerX, Math.abs(tradeSize1), Math.abs(tradeSize2), action1, action2);
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