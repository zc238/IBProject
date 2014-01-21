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
		
//		public Regression(double[] ys, double[] xs){
//			DoubleArrayList daXs = new DoubleArrayList(xs);
//			DoubleArrayList daYs = new DoubleArrayList(ys);
//			beta = Descriptive.covariance(daYs, daXs) / Descriptive.covariance(daXs, daXs);
//			intercept = Descriptive.mean(daYs) - Descriptive.mean(daXs) * beta;
//		}
		
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
		
	public double transCost(String ticker, double orderImba){
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
	public List<OrderContractContainer> getOrdersFromHistQuotes(String ticker1, String ticker2, 
																	double slope, int tradeSize, int windowSize){
		HashMap<String, Vector<Quotes>> histQuotes = marketdata.getStoredData();
		double threshold = 0;
		Quotes quote1 = marketdata.getLatestNbbo(ticker1);
		Quotes quote2 = marketdata.getLatestNbbo(ticker2);
		double orderImba1 = quote1.getImbalance();
		double orderImba2 = quote2.getImbalance();
		
		double tradePrice1 = quote1.getMidPrice();
		double tradePrice2 = quote2.getMidPrice();
		
		DoubleArrayList avgTick1Q = convertQuoteToDList(histQuotes.get(ticker1));
		DoubleArrayList avgTick2Q = convertQuoteToDList(histQuotes.get(ticker2));
		
		double alpha = 1 - 1 / windowSize;
		double mean1 = Descriptive.mean(avgTick1Q);
		double mean2 = Descriptive.mean(avgTick2Q);
		
		double scaling = mean1 / mean2;
		
		int tradeSize1 = tradeSize;
		int tradeSize2 = (int) (tradeSize * scaling * Math.abs(slope));
		
		Regression reg = new Regression(avgTick2Q, avgTick1Q);
		
		double residual = quote2.getMidPrice() - quote1.getMidPrice() * reg.getBeta() - reg.getIntercept();
		
		mean1 = alpha * mean1 + (1 - alpha) * tradePrice1;
		mean2 = alpha * mean2 + (1 - alpha) * tradePrice2;

		Action action1 = null;
		Action action2 = null;
		
		if (slope < 0){ // small residual: buy both; large residual: sell both;
			// no position
			if ((marketdata.getPosition(ticker1) == 0) && (marketdata.getPosition(ticker2) == 0)){
				if (expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 + quote1.getAsk() - tradePrice1 - transCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 + quote2.getAsk() - tradePrice2 - transCost(ticker2, orderImba2))){
					// buy both at ask
					action1 = Action.BUY; action2 = Action.BUY;
				}
				else if (-expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 - quote1.getBid() + tradePrice1 + transCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 - quote2.getBid() + tradePrice2 + transCost(ticker2, orderImba2))){
					// sell both at bid
					action1 = Action.SELL; action2 = Action.SELL;					
				}
			}
			// long position, short only
			else if ((marketdata.getPosition(ticker1) > 0) && (marketdata.getPosition(ticker2) > 0)){
				if (-expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 - quote1.getBid() + tradePrice1 + transCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 - quote2.getBid() + tradePrice2 + transCost(ticker2, orderImba2))){
					// sell at both bid
					action1 = Action.SELL; action2 = Action.SELL;					
				}		
			}
			// short position, long only
			else if ((marketdata.getPosition(ticker1) < 0) && (marketdata.getPosition(ticker2) < 0)){
				if (expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 + quote1.getAsk() - tradePrice1 - transCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 + quote2.getAsk() - tradePrice2 - transCost(ticker2, orderImba2))){
					// buy at both ask
					action1 = Action.BUY; action2 = Action.BUY;
				}
			}
		}
		else if (slope > 0){ // small residual: buy ticker1 and sell ticker2; large residual: sell ticker1 and buy ticker2;
			// no position
			if ((marketdata.getPosition(ticker1) == 0) && (marketdata.getPosition(ticker2) == 0)){
				if (expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 + quote1.getAsk() - tradePrice1 - transCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 - quote2.getBid() + tradePrice2 + transCost(ticker2, orderImba2))){
					// buy ticker1 at ask; sell ticker2 at bid
					action1 = Action.BUY; action2 = Action.SELL;
				}
				else if (-expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 - quote1.getBid() + tradePrice1 + transCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 + quote2.getAsk() - tradePrice2 - transCost(ticker2, orderImba2))){
					// sell ticker1 at bid; buy ticker2 at ask
					action1 = Action.SELL; action2 = Action.BUY;					
				}
			}
			// ticker1 long position, sell ticker1 and buy ticker2 only
			else if ((marketdata.getPosition(ticker1) > 0) && (marketdata.getPosition(ticker2) < 0)){
				if (-expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 - quote1.getBid() + tradePrice1 + transCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 + quote2.getAsk() - tradePrice2 - transCost(ticker2, orderImba2))){
					// sell ticker1 at bid; buy ticker2 at ask
					action1 = Action.SELL; action2 = Action.BUY;
				}		
			}
			// ticker1 short position, buy ticker1 and sell ticker2 only
			else if ((marketdata.getPosition(ticker1) < 0) && (marketdata.getPosition(ticker2) < 0)){
				if (expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 + quote1.getAsk() - tradePrice1 - transCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 - quote2.getBid() + tradePrice2 + transCost(ticker2, orderImba2))){
					// buy ticker1 at ask; sell ticker2 at bid
					action1 = Action.BUY; action2 = Action.SELL;
				}
			}
		}//end elseif
		
		return getOrderFromIntel(ticker1, ticker2, Math.abs(tradeSize1), Math.abs(tradeSize2), action1, action2);
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