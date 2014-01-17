/* Copyright (C) 2013 Interactive Brokers LLC. All rights reserved.  This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package sep.pack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.ib.controller.ApiController.IConnectionHandler;

public class MyDemo {

	
	private static class LazyHandler implements IConnectionHandler {
		@Override
		public void connected() {
			System.out.println("Connection Established.");
		}

		@Override
		public void disconnected() {
			System.out.println("Disconnected.");	
		}

		@Override
		public void accountList(ArrayList<String> list) {
			UserInfo.acct = list.get(0); 
			System.out.println("Updating Account Information for Account: " + UserInfo.acct);
		}

		@Override
		public void error(Exception e) { //Go to EReader for ultimate cause
			System.out.println("It's all Long's fault...boom: "+e.getCause().toString());
		}

		@Override
		public void message(int id, int errorCode, String errorMsg) {
			System.out.println("MSG From IB: " + errorMsg);
		}

		@Override
		public void show(String string) {
			System.out.println("Long is a funny guy, he is shown...boom: MSG="+string);
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		@SuppressWarnings("serial")
		List<String> tickers = new LinkedList<String>(){{add("SPY"); add("SH"); add("SSO"); add("SDS"); add("SPXU"); add("UPRO");}};
		
		MyLogger m_inLogger = new MyLogger();
		LazyHandler handler = new LazyHandler();
		QuotesOrderLogger logger = new QuotesOrderLogger();
		QuotesOrderProcessor processor = new QuotesOrderProcessor(handler, m_inLogger, m_inLogger, logger);
		QuotesOrderController retriever = new QuotesOrderController(handler, processor, logger);
		TradeStrategy strategy = new TradeStrategy(logger);
		
		retriever.makeconnection();
		retriever.reqMktData(tickers);
		List<Double> betas = new LinkedList<Double>(); //TODO populate this list, Long's job
		
		while(true){
			for (int i=0; i<tickers.size(); ++i){
				for (int j=0; j<tickers.size(); ++j){
					double slope = betas.get(i+j);
					int tradeSize = 100;
					int windowSize = 1000;
					List<OrderContractContainer> generatedOrders = 
							strategy.getOrdersFromHistQuotes(tickers.get(i), tickers.get(j), slope, tradeSize, windowSize);
					if (generatedOrders.size() == 0){
						Thread.sleep(500);
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
