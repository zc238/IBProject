/* Copyright (C) 2013 Interactive Brokers LLC. All rights reserved.  This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package sep.pack;

import java.util.LinkedList;
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
		List<String> tickers = new LinkedList<String>(){
			private static final long serialVersionUID = 1526140478688889373L;

		{add(TICKER.SPY); add(TICKER.SH); add(TICKER.SSO); add(TICKER.SDS); add(TICKER.SPX); add(TICKER.UPR);}};
		MyLogger m_inLogger = new MyLogger();
		LazyHandler handler = new LazyHandler();
		QuotesOrderLogger logger = new QuotesOrderLogger();
		QuotesOrderProcessor processor = new QuotesOrderProcessor(handler, m_inLogger, 
																	m_inLogger, logger, 
																	"C:/Users/demon4000/Dropbox/data/", 
																	"C:/Users/demon4000/Dropbox/cleanData/");
		
		QuotesOrderController retriever = new QuotesOrderController(handler, processor, logger);
		
		CubicTransCost transCost = new CubicTransCost();
		transCost.insertCost(TICKER.SPY, new Quadralet(0.0148, -0.0221, 0.0139, -0.0033));
		transCost.insertCost(TICKER.SH, new Quadralet(0.0216, -0.0323, 0.0161, -0.0026));
		transCost.insertCost(TICKER.SSO, new Quadralet(0.0153, -0.0228, 0.0131, -0.0028));
		transCost.insertCost(TICKER.SDS, new Quadralet(0.018, -0.0271, 0.0148, -0.0028));
		transCost.insertCost(TICKER.SPX, new Quadralet(0.0035, -0.0072, 0.0071, -0.002));
		transCost.insertCost(TICKER.UPR, new Quadralet(0.0177, -0.026, 0.014, -0.0028));
		
		ExpectedProfit expProfit = new ExpectedProfit(); 
		expProfit.insertPair(new Pair<String>(TICKER.SDS, TICKER.SPX), -94.313, 0.0082);
		expProfit.insertPair(new Pair<String>("SDS", "UPRO"), -94.313, 0.0082);
		expProfit.insertPair(new Pair<String>("SH", "SDS"), -94.313, 0.0082);
		expProfit.insertPair(new Pair<String>("SH", "SPXU"), -94.313, 0.0082);
		expProfit.insertPair(new Pair<String>("SDS", "SPX"), -94.313, 0.0082);
		expProfit.insertPair(new Pair<String>("SDS", "SPX"), -94.313, 0.0082);
		expProfit.insertPair(new Pair<String>("SDS", "SPX"), -94.313, 0.0082);
		expProfit.insertPair(new Pair<String>("SDS", "SPX"), -94.313, 0.0082);
		expProfit.insertPair(new Pair<String>("SDS", "SPX"), -94.313, 0.0082);
		
		TradeStrategy strategy = new TradeStrategy(logger, transCost, expProfit);
		
		// Retrieve Market Data
		retriever.makeconnection();
		retriever.reqMktData(tickers, false);
	
		// Record Pairs Data
//		CleanDataWriter writer = new CleanDataWriter(processor, 10); //10 miliseconds
//		Thread cleanDataRecordThread = new Thread(writer);
//		cleanDataRecordThread.start();
		
		// Start Automated Trading Strategy
		while(true){
			for (int i=0; i<tickers.size(); ++i){
				for (int j=i; j<tickers.size(); ++j){
					int tradeSize = 100;
					int windowSize = 20;
					List<OrderContractContainer> generatedOrders = 
							strategy.getOrdersFromHistQuotes(tickers.get(i), tickers.get(j), tradeSize, windowSize);
					
					if (generatedOrders.size() == 0){ //Nothing to submit, wait 1 second. 
						Thread.sleep(1000); //right approach?
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
