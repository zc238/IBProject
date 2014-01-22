/* Copyright (C) 2013 Interactive Brokers LLC. All rights reserved.  This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package sep.pack;

import java.util.List;

import sep.pack.data.Pair;
import sep.pack.data.Quadralet;
import sep.pack.data.TICKER;
import sep.pack.strategy.CubicTransCost;
import sep.pack.strategy.ExpectedProfit;
import sep.pack.strategy.TradeStrategy;

public class MyDemo {
	public static void main(String[] args) throws InterruptedException {		
		// Wirings
		MyLogger m_inLogger = new MyLogger();
		LazyHandler handler = new LazyHandler();
		QuotesOrderLogger logger = new QuotesOrderLogger();
		QuotesOrderProcessor processor = new QuotesOrderProcessor(handler, m_inLogger, 
																	m_inLogger, logger, 
																	"C:/Users/demon4000/Dropbox/data/", 
																	"C:/Users/demon4000/Dropbox/cleanData/");
		
		QuotesOrderController retriever = new QuotesOrderController(handler, processor, logger);
		
		// Retrieve Market Data
		retriever.makeconnection();
		retriever.reqMktData(TICKER.TICKERS, false);
		
		CubicTransCost transCost = new CubicTransCost();
		transCost.insertCost(TICKER.SPY, new Quadralet(0.0148, -0.0221, 0.0139, -0.0033));
		transCost.insertCost(TICKER.SH, new Quadralet(0.0216, -0.0323, 0.0161, -0.0026));
		transCost.insertCost(TICKER.SSO, new Quadralet(0.0153, -0.0228, 0.0131, -0.0028));
		transCost.insertCost(TICKER.SDS, new Quadralet(0.018, -0.0271, 0.0148, -0.0028));
		transCost.insertCost(TICKER.SPX, new Quadralet(0.0035, -0.0072, 0.0071, -0.002));
		transCost.insertCost(TICKER.UPR, new Quadralet(0.0177, -0.026, 0.014, -0.0028));
		
		ExpectedProfit expProfit = new ExpectedProfit(); 
		expProfit.insertPair(new Pair<String>(TICKER.SPY, TICKER.SH), -0.7765, 0);
		expProfit.insertPair(new Pair<String>(TICKER.SPY, TICKER.SSO), -0.0014, 0);
		expProfit.insertPair(new Pair<String>(TICKER.SPY, TICKER.SDS), -0.0014, 0);
		expProfit.insertPair(new Pair<String>(TICKER.SPY, TICKER.UPR), -0.6331, 0);
		expProfit.insertPair(new Pair<String>(TICKER.SPY, TICKER.SPX), -0.811, 0);
		expProfit.insertPair(new Pair<String>(TICKER.SH, TICKER.SSO), -0.0023, 0);
		expProfit.insertPair(new Pair<String>(TICKER.SH, TICKER.SDS), -0.7948, 0);
		expProfit.insertPair(new Pair<String>(TICKER.SH, TICKER.UPR), -0.7556, 0);
		expProfit.insertPair(new Pair<String>(TICKER.SH, TICKER.SPX), -0.0010, 0);
		expProfit.insertPair(new Pair<String>(TICKER.SSO, TICKER.SDS), -0.0014, 0);
		expProfit.insertPair(new Pair<String>(TICKER.SSO, TICKER.UPR), -0.0010, 0);
		expProfit.insertPair(new Pair<String>(TICKER.SSO, TICKER.SPX), -0.7529, 0);
		expProfit.insertPair(new Pair<String>(TICKER.SDS, TICKER.UPR), -0.94313, 0);
		expProfit.insertPair(new Pair<String>(TICKER.SDS, TICKER.SPX), -0.94313, 0);
		expProfit.insertPair(new Pair<String>(TICKER.UPR, TICKER.SPX), -0.94313, 0);
		
		TradeStrategy strategy = new TradeStrategy(logger, transCost, expProfit);
				
		// Start Automated Trading Strategy
		while(true){
			for (int i=0; i<TICKER.TICKERS.size(); ++i){
				for (int j=i; j<TICKER.TICKERS.size(); ++j){
					int tradeSize = 100;
					int windowSize = 5;
					if(i==j){ continue; }
					List<OrderContractContainer> generatedOrders = 
							strategy.getOrdersFromHistQuotes(TICKER.TICKERS.get(i), TICKER.TICKERS.get(j), tradeSize, windowSize);
					
					if (generatedOrders.size() == 0){ //Nothing to submit, wait 1 second. 
						Thread.sleep(100); //right approach?
					}else{
						for (OrderContractContainer c : generatedOrders){
							retriever.sendOrder(c);
						}
					}
				}
			}
		}
		
//		retriever.sendOrder("SPY", 10000, Action.BUY);
//		retriever.sendOrder("SPY", 100, Action.BUY, 130);
	}
}
