package sep.pack;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

public class TradeStrategy{
	private HashMap<String, Integer> position = new HashMap<String, Integer>();

	private double quantity1;
	private double quantity2;
	private boolean isMarket1;
	private boolean isMarket2;
	private double limitPrice1;
	private double limitPrice2;

	public void initialPosition(String ticker){
		position.put(ticker, 0);
	}
	
	public void updatePosition(String ticker, int amount){
		position.put(ticker, position.get(ticker) + amount);
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
			l.add((q.getAsk() + q.getBid())/2);
		}
		return l;
	}
	
	public void updatePair(String ticker1, String ticker2, ConcurrentHashMap<String, Quotes> latestNbbo, 
			double slope, int tradeSize, int windowSize, HashMap<String, Integer> position, HashMap<String, List<Quotes>> histQuotes){
		
		double threshold = 0;
		Quotes quote1 = latestNbbo.get(ticker1);
		Quotes quote2 = latestNbbo.get(ticker2);
		double orderImba1 = quote1.getBidSize() / (quote1.getBidSize() + quote1.getAskSize());
		double orderImba2 = quote2.getBidSize() / (quote2.getBidSize() + quote2.getAskSize());
		double tradePrice1 =(quote1.getBidSize() + quote1.getAskSize()) / 2;
		double tradePrice2 =(quote2.getBidSize() + quote2.getAskSize()) / 2;
		
		DoubleArrayList avgTick1Q = convertQuoteToDList(histQuotes.get(ticker1));
		DoubleArrayList avgTick2Q = convertQuoteToDList(histQuotes.get(ticker2));
		
		double alpha = 1 - 1 / windowSize;
		double mean1 = Descriptive.mean(avgTick1Q);
		double mean2 = Descriptive.mean(avgTick2Q);
		
		double scaling = mean1 / mean2;
		
		double tradeSize1 = tradeSize;
		double tradeSize2 = tradeSize * scaling * Math.abs(slope);
		
		double residual = 0;
		
		mean1 = alpha * mean1 + (1 - alpha) * tradePrice1;
		mean2 = alpha * mean2 + (1 - alpha) * tradePrice2;

		if (slope < 0){ // small residual: buy both; large residual: sell both;
			// no position
			if ((position.get(ticker1) == 0) && (position.get(ticker2) == 0)){
				if (expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 + quote1.getAsk() - tradePrice1 - tCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 + quote2.getAsk() - tradePrice2 - tCost(ticker2, orderImba2))){
					// buy both at ask
					quantity1 = tradeSize1;
					quantity2 = tradeSize2;
					isMarket1 = true;
					isMarket2 = true;
				}
				else if (-expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 - quote1.getBid() + tradePrice1 + tCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 - quote2.getBid() + tradePrice2 + tCost(ticker2, orderImba2))){
					// sell both at bid
					quantity1 = -tradeSize1;
					quantity2 = -tradeSize2;
					isMarket1 = true;
					isMarket2 = true;					
				}
			}
			// long position, short only
			else if ((position.get(ticker1) > 0) && (position.get(ticker2) > 0)){
				if (-expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 - quote1.getBid() + tradePrice1 + tCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 - quote2.getBid() + tradePrice2 + tCost(ticker2, orderImba2))){
					// sell at both bid
					quantity1 = -tradeSize1;
					quantity2 = -tradeSize2;
					isMarket1 = true;
					isMarket2 = true;					
				}		
			}
			// short position, long only
			else if ((position.get(ticker1) < 0) && (position.get(ticker2) < 0)){
				if (expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 + quote1.getAsk() - tradePrice1 - tCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 + quote2.getAsk() - tradePrice2 - tCost(ticker2, orderImba2))){
					// buy at both ask
					quantity1 = 100;
					quantity2 = 100;
					isMarket1 = true;
					isMarket2 = true;
				}
			}
		}
		else if (slope > 0){ // small residual: buy ticker1 and sell ticker2; large residual: sell ticker1 and buy ticker2;
			// no position
			if ((position.get(ticker1) == 0) && (position.get(ticker2) == 0)){
				if (expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 + quote1.getAsk() - tradePrice1 - tCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 - quote2.getBid() + tradePrice2 + tCost(ticker2, orderImba2))){
					// buy ticker1 at ask; sell ticker2 at bid
					quantity1 = tradeSize1;
					quantity2 = -tradeSize2;
					isMarket1 = true;
					isMarket2 = true;
				}
				else if (-expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 - quote1.getBid() + tradePrice1 + tCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 + quote2.getAsk() - tradePrice2 - tCost(ticker2, orderImba2))){
					// sell ticker1 at bid; buy ticker2 at ask
					quantity1 = -tradeSize1;
					quantity2 = tradeSize2;
					isMarket1 = true;
					isMarket2 = true;					
				}
			}
			// ticker1 long position, sell ticker1 and buy ticker2 only
			else if ((position.get(ticker1) > 0) && (position.get(ticker2) < 0)){
				if (-expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 - quote1.getBid() + tradePrice1 + tCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 + quote2.getAsk() - tradePrice2 - tCost(ticker2, orderImba2))){
					// sell ticker1 at bid; buy ticker2 at ask
					quantity1 = -tradeSize1;
					quantity2 = tradeSize2;
					isMarket1 = true;
					isMarket2 = true;
				}		
			}
			// ticker1 short position, buy ticker1 and sell ticker2 only
			else if ((position.get(ticker1) < 0) && (position.get(ticker2) < 0)){
				if (expectedProfit(ticker1, ticker2, residual) > threshold 
						+ tradeSize1 * (0.005 + quote1.getAsk() - tradePrice1 - tCost(ticker1, orderImba1)) 
						+ tradeSize2 * (0.005 - quote2.getBid() + tradePrice2 + tCost(ticker2, orderImba2))){
					// buy ticker1 at ask; sell ticker2 at bid
					quantity1 = tradeSize1;
					quantity2 = -tradeSize2;
					isMarket1 = true;
					isMarket2 = true;
				}
			}
		}
	}
}