/* Copyright (C) 2013 Interactive Brokers LLC. All rights reserved.  This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package sep.pack;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.ib.controller.ApiController;
import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.Types.Action;

public class MyDemo {
	public static void main(String[] args) {
		JTextArea m_inLog = new JTextArea();
		JTextArea m_outLog = new JTextArea();
		
		Logger m_inLogger = new Logger(m_inLog);
		Logger m_outLogger = new Logger(m_outLog);

		QuotesOrderController retriever = new QuotesOrderController( null, m_inLogger, m_outLogger);
		
		retriever.makeconnection();
		retriever.sendOrder("SPY", 100, Action.BUY);
		retriever.reqMktData();
	}
	
	private static class Logger implements ILogger {
		final private JTextArea m_area;
		Logger( JTextArea area) {
			m_area = area;
		}
		@Override public void log(final String str) {
			SwingUtilities.invokeLater( new Runnable() {
				@Override public void run() {
				}
			});
		}
	}
}
