package sep.pack.strategy;

import java.util.List;
import java.util.Map;

import sep.pack.data.Pair;
import sep.pack.data.Quotes;

import com.ib.controller.Types.Action;

public abstract class AbstractStrategy {
	protected TransCost transCost;
	protected ExpectedProfit expProfit;
	
	protected String tickerX;
	protected String tickerY;
	protected int tradeSizeX;
	protected int tradeSizeY;
	
	protected double windowSize;
	protected int tradeSize;
	protected double slope = 0.0;
	protected double threshold = 0.0;
	protected double scaling = 0.0;
	protected double oldBeta = 0.0;
	protected double alpha = 0.001;
	
	protected Pair<Action> strategyDecision(Quotes quotesX, Quotes quotesY){
		double orderImbaX = quotesX.getImbalance();
		double orderImbaY = quotesY.getImbalance();
		double midPriceX = quotesX.getMidPrice();
		double midPriceY = quotesY.getMidPrice();
		
		double residual = StrategyUtility.getResidual(oldBeta, scaling*slope, midPriceX, midPriceY);		
		System.out.println("Residual Computed: " + residual);
		double expectedReturn = Math.min(tradeSizeX, tradeSizeY) * expProfit.getExpectedProf(new Pair<String>(tickerX, tickerY), residual);
		System.out.println("Expected Profit Computed: " + expectedReturn);
		
		double transCostX = transCost.getTransCost(tickerX, orderImbaX);
		double transCostY = transCost.getTransCost(tickerX, orderImbaY);
		
		double longX = tradeSizeX * (StrategyConstants.IB_TRANS_COST + quotesX.getAsk() - midPriceX - transCostX);
		double shortX = tradeSizeX * (StrategyConstants.IB_TRANS_COST - quotesX.getBid() + midPriceX + transCostX);
		double longY = tradeSizeY * (StrategyConstants.IB_TRANS_COST + quotesY.getAsk() - midPriceY - transCostY);
		double shortY = tradeSizeY * (StrategyConstants.IB_TRANS_COST - quotesY.getBid() + midPriceY + transCostY);
		
		Pair<Action> pair = null;
		if (slope > 0){
			double longXShortY = expectedReturn - (longX + shortY);
			double shortXLongY = -expectedReturn - (shortX + longY);
			if (longXShortY > threshold) {
				pair = new Pair<Action>(Action.BUY, Action.SELL);
			}
			else if (shortXLongY > threshold){
				pair = new Pair<Action>(Action.SELL, Action.BUY);
			}
		}
		else if (slope < 0){
			double longBoth = expectedReturn - (longX + longY);
			double shortBoth = -expectedReturn - (shortX + shortY);
			if (longBoth > threshold) {
				pair = new Pair<Action>(Action.BUY, Action.BUY);
			}
			else if (shortBoth > threshold){
				pair = new Pair<Action>(Action.SELL, Action.SELL);
			}			
		}
		return pair;
	}
	
	protected abstract Map<String, List<Quotes>> getBurnIn();
	
	protected int computeAllFirstIntParams(){
		slope = (StrategyConstants.tickerLeverage.get(tickerY) + 0.0) / (StrategyConstants.tickerLeverage.get(tickerX) + 0.0);
		threshold = 0;
		
		Map<String, List<Quotes>> quotes = getBurnIn();
		System.out.println("Obtained Burnin Period...");
		
		scaling = StrategyUtility.computeScaling(quotes.get(tickerX), quotes.get(tickerY));
		oldBeta = StrategyUtility.computeBeta(quotes.get(tickerX), quotes.get(tickerY), slope, scaling);
		
		tradeSizeX = (int) (tradeSize * scaling * Math.abs(slope));
		tradeSizeY = tradeSize;
		return quotes.size();
	}
}
