/* Copyright (C) 2013 Interactive Brokers LLC. All rights reserved.  This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package sep.pack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController.IConnectionHandler;

public class MyDemo {
	private static class MyLogger implements ILogger {
		@Override public void log(final String str) {
			//System.out.println("Logger: " + str);
		}
	}
	
	private static class LazyHandler implements IConnectionHandler {
		@Override
		public void connected() {
			System.out.println("Long is a funny guy, he is connected...boom");
		}

		@Override
		public void disconnected() {
			System.out.println("Long is a funny guy, he is disconnected...boom");	
		}

		@Override
		public void accountList(ArrayList<String> list) {
			System.out.println("Whats This");
		}

		@Override
		public void error(Exception e) {
			System.out.println("Long is a funny guy, he is errored...boom: "+e.toString());
		}

		@Override
		public void message(int id, int errorCode, String errorMsg) {
			System.out.println("Long is a funny guy, he is messaged...boom " + errorMsg);
		}

		@Override
		public void show(String string) {
			System.out.println("Long is a funny guy, he is shown...boom: MSG="+string);
		}
	}
	public static void main(String[] args) {
		
		MyLogger m_inLogger = new MyLogger();
		MyLogger m_outLogger = new MyLogger();

		QuotesOrderController retriever = new QuotesOrderController( new LazyHandler(), m_inLogger, m_outLogger );
		
		retriever.makeconnection();
		
		@SuppressWarnings("serial")
		List<String> tickers = new LinkedList<String>(){{add("SPY"); add("SH"); add("SSO"); add("SDS"); add("SPXU"); add("UPRC");}};

		retriever.reqMktData(tickers);
		//retriever.sendOrder("SPY", 100, Action.BUY);
	}
}
