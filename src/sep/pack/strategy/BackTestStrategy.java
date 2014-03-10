package sep.pack.strategy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sep.pack.data.Pair;
import sep.pack.data.Quotes;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

import com.ib.controller.Types.Action;

public class BackTestStrategy {
	
	private TransCost transCost;
	private ExpectedProfit expProfit;
	
	private String tickerX;
	private String tickerY;
	private double windowSize;
	private int tradeSize;
	
	private double meanX = 0.0;
	private double meanY = 0.0;
	private Map<String, List<Quotes>> histQuotes;
	
	private Map<String, Integer> currentPositions = new HashMap<String, Integer>();
	
	public BackTestStrategy(TransCost tc, ExpectedProfit profit, 
			String tX, String tY, double wS, int tS, Map<String, List<Quotes>> hq){
		transCost = tc;
		expProfit = profit;
		tickerX = tX;
		tickerY = tY;
		windowSize = wS*1000*60; //wS is in minute, windowSize in milisecond
		tradeSize = tS;
		histQuotes = hq;
	}
	
	private Map<String, List<Quotes>> getBurnIn(){
		Map<String, List<Quotes>> qs = new HashMap<String, List<Quotes>>();
		long startMs = histQuotes.get(tickerX).get(0).getLocalTimeStamp().getTime();
		qs.put(tickerX, new LinkedList<Quotes>());
		qs.put(tickerY, new LinkedList<Quotes>());
		for(Quotes q: histQuotes.get(tickerX)){
			if ((q.getLocalTimeStamp().getTime()-startMs) < windowSize){
				qs.get(tickerX).add(q);
				qs.get(tickerY).add(q);
			}
		}
		return qs;
	}
	
	private void addPos(String ticker, int amt){
		int extAmt = this.currentPositions.get(ticker);
		this.currentPositions.put(ticker, amt+extAmt);
	}
	
	public void runSimulation(){
		System.out.println("Running Simulation...");
		double slope = (StrategyConstants.tickerLeverage.get(tickerY) + 0.0) / (StrategyConstants.tickerLeverage.get(tickerX) + 0.0);
			
		double threshold = 0;
				
		Map<String, List<Quotes>> quotes = getBurnIn();
		
		for (int i=quotes.size(); i<histQuotes.size(); ++i){
			Quotes quotesY = histQuotes.get(tickerY).get(i);
			Quotes quotesX = histQuotes.get(tickerX).get(i);
		
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
			
	 		double residual =  StrategyUtility.getLatestResidual(histQuotes.get(tickerX), histQuotes.get(tickerY), slope, 0.0, true);
	 		
	 		System.out.println("Residual Computed: " + residual);
	
			Action action1 = null;
			Action action2 = null;
			
			double expectedReturn = expProfit.getExpectedProf(new Pair<String>(tickerX, tickerY), residual);
			
			System.out.println("Expected Profit Computed: " + expectedReturn);
			System.out.println("Position for " + tickerX + " is " + currentPositions.get(tickerX));
			System.out.println("Position for " + tickerY + " is " + currentPositions.get(tickerY));
			
			if (slope < 0){ // small residual: buy both; large residual: sell both;
				// no position
				if ((currentPositions.get(tickerY) == 0) && (currentPositions.get(tickerX) == 0)){
					if ( expectedReturn > threshold 
							+ tradeSizeX * (StrategyConstants.IB_TRANS_COST + quotesY.getAsk() - midPriceY - transCost.getTransCost(tickerY, orderImbaY)) 
							+ tradeSizeY * (StrategyConstants.IB_TRANS_COST + quotesX.getAsk() - midPriceX - transCost.getTransCost(tickerX, orderImbaX))){
						// buy both at ask
						addPos(tickerX, tradeSizeX);
						addPos(tickerY, tradeSizeY);
					}
					else if ( -expectedReturn > threshold 
							+ tradeSizeX * (StrategyConstants.IB_TRANS_COST - quotesY.getBid() + midPriceY + transCost.getTransCost(tickerY, orderImbaY)) 
							+ tradeSizeY * (StrategyConstants.IB_TRANS_COST - quotesX.getBid() + midPriceX + transCost.getTransCost(tickerX, orderImbaX))){
						// sell both at bid
						addPos(tickerX, -tradeSizeX);
						addPos(tickerY, -tradeSizeY);
					}
				}
				// long position, short only
				else if ((currentPositions.get(tickerY) > 0) && (currentPositions.get(tickerX) > 0)){
					if ( -expectedReturn > threshold 
							+ tradeSizeX * (StrategyConstants.IB_TRANS_COST - quotesY.getBid() + midPriceY + transCost.getTransCost(tickerY, orderImbaY)) 
							+ tradeSizeY * (StrategyConstants.IB_TRANS_COST - quotesX.getBid() + midPriceX + transCost.getTransCost(tickerX, orderImbaX))){
						// sell at both bid
						addPos(tickerX, -tradeSizeX);
						addPos(tickerY, -tradeSizeY);
					}		
				}
				// short position, long only
				else if ((currentPositions.get(tickerY) < 0) && (currentPositions.get(tickerX) < 0)){
					if ( expectedReturn > threshold 
							+ tradeSizeX * (StrategyConstants.IB_TRANS_COST + quotesY.getAsk() - midPriceY - transCost.getTransCost(tickerY, orderImbaY)) 
							+ tradeSizeY * (StrategyConstants.IB_TRANS_COST + quotesX.getAsk() - midPriceX - transCost.getTransCost(tickerX, orderImbaX))){
						// buy at both ask
						addPos(tickerX, tradeSizeX);
						addPos(tickerY, tradeSizeY);
					}
				}
			}
			else if (slope > 0){ // small residual: buy ticker1 and sell ticker2; large residual: sell ticker1 and buy ticker2;
				// no position
				if ((currentPositions.get(tickerY) == 0) && (currentPositions.get(tickerX) == 0)){
					if ( expectedReturn > threshold 
							+ tradeSizeX * (StrategyConstants.IB_TRANS_COST + quotesY.getAsk() - midPriceY - transCost.getTransCost(tickerY, orderImbaY)) 
							+ tradeSizeY * (StrategyConstants.IB_TRANS_COST - quotesX.getBid() + midPriceX + transCost.getTransCost(tickerX, orderImbaX))){
						// buy ticker1 at ask; sell ticker2 at bid
						addPos(tickerX, tradeSizeX);
						addPos(tickerY, -tradeSizeY);
					}
					else if ( -expectedReturn > threshold 
							+ tradeSizeX * (StrategyConstants.IB_TRANS_COST - quotesY.getBid() + midPriceY + transCost.getTransCost(tickerY, orderImbaY)) 
							+ tradeSizeY * (StrategyConstants.IB_TRANS_COST + quotesX.getAsk() - midPriceX - transCost.getTransCost(tickerX, orderImbaX))){
						// sell ticker1 at bid; buy ticker2 at ask
						addPos(tickerX, -tradeSizeX);
						addPos(tickerY, tradeSizeY);
					}
				}
				// ticker1 long position, sell ticker1 and buy ticker2 only
				else if ((currentPositions.get(tickerY) > 0) && (currentPositions.get(tickerX) < 0)){
					if ( -expectedReturn > threshold 
							+ tradeSizeX * (StrategyConstants.IB_TRANS_COST - quotesY.getBid() + midPriceY + transCost.getTransCost(tickerY, orderImbaY)) 
							+ tradeSizeY * (StrategyConstants.IB_TRANS_COST + quotesX.getAsk() - midPriceX - transCost.getTransCost(tickerX, orderImbaX))){
						// sell ticker1 at bid; buy ticker2 at ask
						addPos(tickerX, -tradeSizeX);
						addPos(tickerY, tradeSizeY);
					}		
				}
				// ticker1 short position, buy ticker1 and sell ticker2 only
				else if ((currentPositions.get(tickerY) < 0) && (currentPositions.get(tickerX) < 0)){
					if (expectedReturn > threshold 
							+ tradeSizeX * (StrategyConstants.IB_TRANS_COST + quotesY.getAsk() - midPriceY - transCost.getTransCost(tickerY, orderImbaY)) 
							+ tradeSizeY * (StrategyConstants.IB_TRANS_COST - quotesX.getBid() + midPriceX + transCost.getTransCost(tickerX, orderImbaX))){
						// buy ticker1 at ask; sell ticker2 at bid
						addPos(tickerX, tradeSizeX);
						addPos(tickerY, -tradeSizeY);
					}
				}
			}//end elseif
		}
	}
}
